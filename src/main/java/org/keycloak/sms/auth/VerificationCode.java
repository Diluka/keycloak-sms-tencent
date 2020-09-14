/**
 * add by zhangzhl
 * 2020-07-27
 * 验证码
 * 
 */
package org.keycloak.sms.auth;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.credential.CredentialModel;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VerificationCode implements FormAction, FormActionFactory {

	public static final String PROVIDER_ID = "login-verificationCode-action";
	public static final String FIELD_VERIFICATIONCODE = "user.verificationCode";
	public static final String FIELD_VERIFICATIONCODE_IMAGE = "user.imageVericationCode";

	// 验证码不能为空
	public static final String MISSING_FIELD_VERIFICATIONCODE = "missingVerificationCodeMessage";

	private LoginFormsProvider loginFormsProvider;

	@Override
	public String getHelpText() {
		return "验证码校验";
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return null;
	}

	@Override
	public void validate(ValidationContext context) {
		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		List<FormMessage> errors = new ArrayList<>();
		context.getEvent().detail(Details.REGISTER_METHOD, "form");
		String verificationCode = formData.getFirst(FIELD_VERIFICATIONCODE);
		if (Validation.isBlank(verificationCode)) {
			errors.add(new FormMessage(FIELD_VERIFICATIONCODE, MISSING_FIELD_VERIFICATIONCODE));
		}

		// TODO: 校验验证码
		if (verificationCode != getStoreVerificationCode(context)) {
			// TODO:刷新验证码
			refreshVerificationCode(context);
		}
		if (errors.size() > 0) {
			context.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
			context.validationError(formData, errors);
			return;
		} else {
			context.success();
		}
	}

	@Override
	public void success(FormContext context) {
		UserModel user = context.getUser();
		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
//		user.setUsername(formData.getFirst(FIELD_USERNAME));
	}

	@Override
	public void buildPage(FormContext context, LoginFormsProvider form) {
		loginFormsProvider = form;
		form.setAttribute("verificationCodeRequired", true);
		// TODO:此处调用验证码服务，生成验证码图片
		refreshVerificationCode(context);
	}

	// 刷新验证码
	private void refreshVerificationCode(FormContext context) {
		// TODO: 生成验证码
		String code = ""; // FIXME: 调用获取验证码到接口
		loginFormsProvider.setAttribute(FIELD_VERIFICATIONCODE_IMAGE, "456789_img"); // TODO: 生成验证码的图片
		storeVerificationCode(context, code);
	}
	
	// 存储验证码到数据库，集群也可以使用
	private void storeVerificationCode(FormContext context, String code) {
		CredentialModel credentialsCode = new CredentialModel();

		credentialsCode.setType(FIELD_VERIFICATIONCODE);
		credentialsCode.setValue(code);

		UserCredentialManager userCredentialManager = context.getSession().userCredentialManager();

		if (userCredentialManager.isConfiguredFor(context.getRealm(), context.getUser(),
				FIELD_VERIFICATIONCODE)) {
			userCredentialManager.updateCredential(context.getRealm(), context.getUser(), credentialsCode);
		} else {
			userCredentialManager.createCredential(context.getRealm(), context.getUser(), credentialsCode);
		}
	}

	// 获取存储的验证码
	private String getStoreVerificationCode(FormContext context) {
		String result = null;
		// TODO: zhangzhl
		List<CredentialModel> creds = context.getSession().userCredentialManager()
				.getStoredCredentials(context.getRealm(), context.getUser());
		for (CredentialModel cred : creds) {
			if (cred.getType().equals(FIELD_VERIFICATIONCODE)) {
				result = cred.getValue();
			}
		}
		return result;
	}
	
	@Override
	public boolean requiresUser() {
		return false;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		return true;
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

	}

	@Override
	public boolean isUserSetupAllowed() {
		return false;
	}

	@Override
	public void close() {

	}

	@Override
	public String getDisplayType() {
		return "verificationCode Validation";
	}

	@Override
	public String getReferenceCategory() {
		return null;
	}

	@Override
	public boolean isConfigurable() {
		return false;
	}

	private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
			AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED };

	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
		return REQUIREMENT_CHOICES;
	}

	@Override
	public FormAction create(KeycloakSession session) {
		return this;
	}

	@Override
	public void init(Config.Scope config) {

	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {

	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}
}
