/**
 * 通过手机号作为用户名进行登录
 */
package org.keycloak.sms.auth;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;

public class LoginOnPhoneNumberAsUserName extends AbstractUsernameFormAuthenticator implements Authenticator {
	protected static ServicesLogger log = ServicesLogger.LOGGER;
	private static final String USER_ATTR_phoneNumber_KEY = "phoneNumber";

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
		// 如果用户名和邮件验证不通过，则验证手机号
		if (validateUserAndPassword(context, formData) == false) {
			// 此处添加通过手机号码作为用户名来验证
			context.clearUser();
			UserModel user = getUserByPhoneNumber(context, formData);
			return user != null && validatePassword(context, user, formData) && validateUser(context, user, formData);
		}
		return true;
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

			List<UserModel> userModels = context.getSession().users()
					.searchForUserByUserAttribute(USER_ATTR_phoneNumber_KEY, username, context.getRealm());

			user = userModels != null && userModels.size() == 1 ? userModels.get(0) : null;

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
