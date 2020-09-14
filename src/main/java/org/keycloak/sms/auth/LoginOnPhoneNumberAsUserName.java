/**
 * 通过手机号作为用户名进行登录
 * 修改记录：
 * 		2020-09-11:添加验证码
 */
package org.keycloak.sms.auth;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;

public class LoginOnPhoneNumberAsUserName extends AbstractUsernameFormAuthenticator implements Authenticator {
	protected static ServicesLogger log = ServicesLogger.LOGGER;
	public static final String FIELD_VERIFICATIONCODE = "verificationCode";
	private static final String FIELD_VERIFICATIONCODE_IMAGE = "verificationCodeImg";

	@Override
	public void action(AuthenticationFlowContext context) {
		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		if (formData.containsKey("cancel")) {
			context.cancelLogin();
			return;
		}
		if (!validateForm(context, formData)) {
			return;
		}
		context.success();
	}

	protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
		String userName = formData.getFirst(AuthenticationManager.FORM_USERNAME);
		String password = formData.getFirst(CredentialRepresentation.PASSWORD);
		String verificationCode = formData.getFirst(FIELD_VERIFICATIONCODE);
		String errString = "";

		if (userName == null || userName.isEmpty()) {
			context.getEvent().error(Errors.USERNAME_MISSING);
			errString = Errors.USERNAME_MISSING;
			Response challengeResponse = challenge(context, errString);
			context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
			refreshVerificationCode(context);
			return false;
		}
		if (password == null || password.isEmpty()) {
			context.getEvent().error(Errors.PASSWORD_MISSING);
			errString = Errors.PASSWORD_MISSING;
			Response challengeResponse = challenge(context, errString);
			context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
			refreshVerificationCode(context);
			return false;
		}

		if (verificationCode == null || verificationCode.isEmpty()) {
			context.getEvent().error(Errors.CODE_VERIFIER_MISSING);
			errString = Errors.CODE_VERIFIER_MISSING;
			Response challengeResponse = challenge(context, errString);
			context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
			refreshVerificationCode(context);
			return false;
		}

		if (false == validateVerificationCode(context, verificationCode)) {
			errString = Errors.INVALID_CODE_VERIFIER;
			Response challengeResponse = challenge(context, errString);
			context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
			refreshVerificationCode(context);
			return false;
		}
		// 如果用户名和邮件验证不通过，则验证手机号
		if (validateUserAndPassword(context, formData) == false) {
			// 此处添加通过手机号码作为用户名来验证
			context.clearUser();
			UserModel user = getUserByPhoneNumber(context, formData);
			return user != null && validatePassword(context, user, formData) && validateUser(context, user, formData);
		}
		return true;
	}

	// 比对验证码
	private boolean validateVerificationCode(AuthenticationFlowContext context, String verificationCode) {
		return getStoreVerificationCode(context).equalsIgnoreCase(verificationCode);
	}

	// 刷新验证码
	private void refreshVerificationCode(AuthenticationFlowContext context) {
		String codeImg = getVerificationCode(context);
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
		formData.add(FIELD_VERIFICATIONCODE_IMAGE, codeImg);
		Response challengeResponse = challenge(context, formData);
		context.challenge(challengeResponse);
	}

	private String getVerificationCode(AuthenticationFlowContext context) {
		Map<String, String> map = null;
		try {
			map = ImageCodeUtil.generateCodeAndPic();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String code = map.get("code");
		storeVerificationCode(context, code);
		String codeImg = map.get("codePic");
		return codeImg;
	}

	// 存储验证码到数据库，集群也可以使用
	private void storeVerificationCode(AuthenticationFlowContext context, String code) {

//		String sessionIdString = context.getAuthenticationSession().getParentSession().getId();

//		context.getSession().setAttribute(FIELD_VERIFICATIONCODE + "_" + sessionIdString, code);
		context.getAuthenticationSession().setAuthNote(FIELD_VERIFICATIONCODE, code);

//		CredentialModel credentialsCode = new CredentialModel();
//
//		credentialsCode.setType(FIELD_VERIFICATIONCODE);
//		credentialsCode.setValue(code);
//
//		UserCredentialManager userCredentialManager = context.getSession().userCredentialManager();
//
//		if (userCredentialManager.isConfiguredFor(context.getRealm(), context.getUser(), FIELD_VERIFICATIONCODE)) {
//			userCredentialManager.updateCredential(context.getRealm(), context.getUser(), credentialsCode);
//		} else {
//			userCredentialManager.createCredential(context.getRealm(), context.getUser(), credentialsCode);
//		}
	}

	// 获取存储的验证码
	private String getStoreVerificationCode(AuthenticationFlowContext context) {
		String codeString = context.getAuthenticationSession().getAuthNote(FIELD_VERIFICATIONCODE);
		return codeString;
//		String sessionIdString = context.getAuthenticationSession().getParentSession().getId();
//		Object oVerificationCodeObject = context.getSession().getAttribute(FIELD_VERIFICATIONCODE + "_" + sessionIdString);
//		return oVerificationCodeObject != null ? oVerificationCodeObject.toString() : "";

//		Object oVerificationCodeObject = context.getSession().getAttribute(FIELD_VERIFICATIONCODE);
//		return oVerificationCodeObject != null ? oVerificationCodeObject.toString() : "";
//		String result = null;
//		// TODO: zhangzhl
//		List<CredentialModel> creds = context.getSession().usserCredentialManager()
//				.getStoredCredentials(context.getRealm(), context.getUser());
//		for (CredentialModel cred : creds) {
//			if (cred.getType().equals(FIELD_VERIFICATIONCODE)) {
//				result = cred.getValue();
//			}
//		}
//		return result;
	}

	private boolean validateUser(AuthenticationFlowContext context, UserModel user,
			MultivaluedMap<String, String> inputData) {
		if (!enabledUser(context, user)) {
			return false;
		}
		String rememberMe = inputData.getFirst("rememberMe");
		boolean remember = rememberMe != null && rememberMe.equalsIgnoreCase("on");
		if (remember) {
			context.getAuthenticationSession().setAuthNote(Details.REMEMBER_ME, "true");
			context.getEvent().detail(Details.REMEMBER_ME, "true");
		} else {
			context.getAuthenticationSession().removeAuthNote(Details.REMEMBER_ME);
		}
		context.setUser(user);
		return true;
	}

	private UserModel getUserByPhoneNumber(AuthenticationFlowContext context,
			MultivaluedMap<String, String> inputData) {
		String username = inputData.getFirst(AuthenticationManager.FORM_USERNAME);
		if (username == null) {
			context.getEvent().error(Errors.USER_NOT_FOUND);
			Response challengeResponse = challenge(context, getDefaultChallengeMessage(context));
			context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
			return null;
		}

		// remove leading and trailing whitespace
		username = username.trim();

		context.getEvent().detail(Details.USERNAME, username);
		context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

		UserModel user = null;
		try {

			user = context.getSession().users().getUserByPhoneNumber(username, context.getRealm());

		} catch (ModelDuplicateException mde) {
			ServicesLogger.LOGGER.modelDuplicateException(mde);

			// Could happen during federation import
			if (mde.getDuplicateFieldName() != null && mde.getDuplicateFieldName().equals(UserModel.EMAIL)) {
				setDuplicateUserChallenge(context, Errors.EMAIL_IN_USE, Messages.EMAIL_EXISTS,
						AuthenticationFlowError.INVALID_USER);
			} else {
				setDuplicateUserChallenge(context, Errors.USERNAME_IN_USE, Messages.USERNAME_EXISTS,
						AuthenticationFlowError.INVALID_USER);
			}
			return user;
		}

		testInvalidUser(context, user);
		return user;
	}

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
		String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);

		String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getRealm(),
				context.getHttpRequest().getHttpHeaders());

		if (loginHint != null || rememberMeUsername != null) {
			if (loginHint != null) {
				formData.add(AuthenticationManager.FORM_USERNAME, loginHint);
			} else {
				formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
				formData.add("rememberMe", "on");
			}
		}

		// 初始化验证码
		String codeImgString = getVerificationCode(context);
		formData.add(FIELD_VERIFICATIONCODE_IMAGE, codeImgString);
		Response challengeResponse = challenge(context, formData);
		context.challenge(challengeResponse);
	}

	@Override
	public boolean requiresUser() {
		return false;
	}

	protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
		LoginFormsProvider forms = context.form();

		if (formData.size() > 0)
			forms.setFormData(formData);

		return forms.createLoginUsernamePassword();
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		// never called
		return true;
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
		// never called
	}

	@Override
	public void close() {

	}
}
