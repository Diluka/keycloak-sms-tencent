package org.keycloak.sms.auth;

import org.jboss.logging.Logger;
import org.keycloak.authentication.*;
import org.keycloak.events.Details;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author yanfeiwuji
 * @description
 * @date 15:09 2020/2/21
 */
public class SmsAuthenticator implements Authenticator {

	private static Logger logger = Logger.getLogger(SmsAuthenticator.class);
	private static final String PAGE_FTL = "phone-verify.ftl";

	private static final String FLOW_REGISTRATION = "registration";
	private static final String FLOW_REST_CREDENTIALS = "reset-credentials";
	private static final String FLOW_FIRST_BROKER_LOGIN = "first-broker-login";
	private static final String FLOW_AUTHENTICATE = "authenticate";
	private static final String SUBMIT_GETCODE = "getcode";
	private static final String SUBMIT_OK = "ok";

	private static final String USER_ATTR_phoneNumber_KEY = "phoneNumber";

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		logger.infof("auth");
		String flowPath = context.getFlowPath();

		if (FLOW_AUTHENTICATE.equals(flowPath)) {
			UserModel userModel = context.getUser();
			if (userModel != null) {
				// 判断用户是否有手机号 有就直接成功
				List phoneNumberAttr = userModel.getAttribute(USER_ATTR_phoneNumber_KEY);
				if (phoneNumberAttr != null && phoneNumberAttr.size() == 1) {
					logger.infof("content success");
					context.success();
					return;
				}
			}
		}

		LoginFormsProvider loginFormsProvider = context.form();
		setScene(context, loginFormsProvider);
		Response challenge = loginFormsProvider.createForm(PAGE_FTL);
		context.challenge(challenge);
	}

	public UserModel findUserModelByphoneNumber(String phoneNumber, UserProvider userProvider, RealmModel realmModel) {
		List<UserModel> userModels = userProvider.searchForUserByUserAttribute(USER_ATTR_phoneNumber_KEY, phoneNumber, realmModel);
		return userModels != null && userModels.size() == 1 ? userModels.get(0) : null;
	}

	@Override
	public void action(AuthenticationFlowContext context) {

		logger.infof("action plow  %s", context.getFlowPath());

		UserProvider userProvider = context.getSession().users();
		RealmModel realmModel = context.getRealm();

		String flowPath = context.getFlowPath();
		String phoneNumber = (context.getHttpRequest().getDecodedFormParameters().getFirst("phoneNumber"));
		String submitAction = (context.getHttpRequest().getDecodedFormParameters().getFirst("submitAction"));
		LoginFormsProvider loginFormsProvider = context.form();
		// 设置场景字段
		this.setScene(context, loginFormsProvider);

		loginFormsProvider.setAttribute("phoneNumber", phoneNumber);

		String code = (context.getHttpRequest().getDecodedFormParameters().getFirst("code"));

		if (Validation.isBlank(phoneNumber)) {
			setError("请输入手机号", context, loginFormsProvider);
			return;
		}
		if (phoneNumber.length() != 11) {
			setError("请输入正确的手机号", context, loginFormsProvider);
			return;
		}
		switch (flowPath) {
		case FLOW_REGISTRATION:
			logger.infof("注册");
			UserModel newUser = this.findUserModelByphoneNumber(phoneNumber, userProvider, realmModel);
			if (newUser != null) {
				setError("该手机号已被注册", context, loginFormsProvider);
				return;
			}
			break;
		case FLOW_REST_CREDENTIALS:
		case FLOW_AUTHENTICATE:
			UserModel hasUser = this.findUserModelByphoneNumber(phoneNumber, userProvider, realmModel);
			if (hasUser == null) {
				setError("该手机号未注册", context, loginFormsProvider);
				return;
			}
			context.setUser(hasUser);
			break;
		default:
		}

		switch (submitAction) {
		case SUBMIT_GETCODE:
			logger.infof("发送验证码");
			// context.getAuthenticatorConfig();
			String sendSmsMsg = SmsUtil.sendSms(phoneNumber, context);

			boolean su = SmsUtil.SUCCESS_FLAG.equals(sendSmsMsg);
			if (su) {
				loginFormsProvider.setAttribute("sendCode", true);
				setSuccess("发送验证码成功", context, loginFormsProvider);
				return;
			} else {
				setError(sendSmsMsg, context, loginFormsProvider);
			}
			return;
		case SUBMIT_OK:
			String msg = SmsUtil.checkCode(phoneNumber, code, context);
			if (SmsUtil.SUCCESS_FLAG.equals(msg)) {
				// 说明成功
				UserModel user;
				if (FLOW_REST_CREDENTIALS.equals(flowPath)) {
					user = this.findUserModelByphoneNumber(phoneNumber, userProvider, realmModel);
				} else if (FLOW_REGISTRATION.equals(flowPath)) {
					UserModel cUser = context.getUser();
					user = cUser == null ? userProvider.addUser(realmModel, phoneNumber) : cUser;
					user.setSingleAttribute(USER_ATTR_phoneNumber_KEY, phoneNumber);
				} else if (FLOW_FIRST_BROKER_LOGIN.equals(flowPath)) {
					user = this.findUserModelByphoneNumber(phoneNumber, userProvider, realmModel);
					if (user == null) {
						UserModel cUser = context.getUser();
						user = cUser == null ? userProvider.addUser(realmModel, phoneNumber) : cUser;
						user.setSingleAttribute(USER_ATTR_phoneNumber_KEY, phoneNumber);
					}
				} else if (FLOW_AUTHENTICATE.equals(flowPath)) {
					user = this.findUserModelByphoneNumber(phoneNumber, userProvider, realmModel);
				} else {
					// cant
					setError("执行到未定义流程", context, loginFormsProvider);
					return;
				}
				user.setEnabled(true);
				context.setUser(user);
				context.getEvent().user(user);
				context.getEvent().success();
				context.newEvent().event(EventType.LOGIN);
				context.getEvent().client(context.getAuthenticationSession().getClient().getClientId())
						.detail(Details.REDIRECT_URI, context.getAuthenticationSession().getRedirectUri())
						.detail(Details.AUTH_METHOD, context.getAuthenticationSession().getProtocol());
				String authType = context.getAuthenticationSession().getAuthNote(Details.AUTH_TYPE);
				if (authType != null) {
					context.getEvent().detail(Details.AUTH_TYPE, authType);
				}
				context.success();
				return;
			} else {
				setError("验证码错误", context, loginFormsProvider);
				return;
			}
		}

	}

	private void setScene(AuthenticationFlowContext context, LoginFormsProvider loginFormsProvider) {
		String flowPath = context.getFlowPath();
		switch (flowPath) {
		case FLOW_REGISTRATION:
			loginFormsProvider.setAttribute("scene", "注册");
			return;
		case FLOW_REST_CREDENTIALS:
			loginFormsProvider.setAttribute("scene", "忘记密码？");
			return;
		case FLOW_FIRST_BROKER_LOGIN:
			loginFormsProvider.setAttribute("scene", "绑定/注册新用户");
			return;
		case FLOW_AUTHENTICATE:
			loginFormsProvider.setAttribute("scene", "登录");
			return;
		default:
			return;
		}
	}

	private void setError(String msg, AuthenticationFlowContext context, LoginFormsProvider loginFormsProvider) {
		Response response = loginFormsProvider.addError(new FormMessage(msg)).createForm(PAGE_FTL);
		context.challenge(response);
	}

	private void setSuccess(String msg, AuthenticationFlowContext context, LoginFormsProvider loginFormsProvider) {
		Response response = loginFormsProvider.addSuccess(new FormMessage(msg)).createForm(PAGE_FTL);
		context.challenge(response);
	}

	@Override
	public boolean requiresUser() {
		return false;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		return true;
	}

	// 提供手机号
	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
		logger.infof("set req ");
		// logger.info("setRequiredActions");
		// user.addRequiredAction(phoneNumberActionFactory.PROVIDER_ID);
		user.addRequiredAction(FLOW_REGISTRATION);
	}

	@Override
	public void close() {
	}
}
