/**
 * add by zhangzhl
 * 2020-07-27
 * 注册页手机号码必填验证
 * 
 */
package org.keycloak.sms.auth;

import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class RegistrationPhoneNumber implements FormAction, FormActionFactory {

	public static final String PROVIDER_ID = "registration-phonenumber-action";
	public static final String FIELD_PHONENUMBER = "user.attributes.phoneNumber";

	//	手机号码不能为空
	public static final String MISSING_FIELD_PHONENUMBER = "missingPhoneNumberMessage";
	
	// 手机号码已注册
	public static final String EXIST_FIELD_PHONENUMBER = "hasExistPhoneNumberMessage";

	@Override
	public String getHelpText() {
		return "手机号码不能为空";
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
		String phoneNumberValueString = formData.getFirst(FIELD_PHONENUMBER);
		if (Validation.isBlank(phoneNumberValueString)) {
			errors.add(new FormMessage(FIELD_PHONENUMBER, MISSING_FIELD_PHONENUMBER));
		} else {
			List<UserModel> userModels = context.getSession().users().searchForUserByUserAttribute("phoneNumber",
					phoneNumberValueString, context.getRealm());

			if (userModels != null && userModels.size() > 0) {
				errors.add(new FormMessage(FIELD_PHONENUMBER, EXIST_FIELD_PHONENUMBER));
			}
		}

		if (errors.size() > 0) {
			context.error(Errors.INVALID_REGISTRATION);
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
		user.setSingleAttribute("phoneNumber", formData.getFirst(FIELD_PHONENUMBER));
	}

	@Override
	public void buildPage(FormContext context, LoginFormsProvider form) {
		form.setAttribute("phoneNumberRequired", true);
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
		return "phoneNumber Validation";
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
