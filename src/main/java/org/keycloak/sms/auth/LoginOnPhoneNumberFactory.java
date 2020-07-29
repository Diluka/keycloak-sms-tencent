package org.keycloak.sms.auth;

import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.authentication.DisplayTypeAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

public class LoginOnPhoneNumberFactory implements AuthenticatorFactory, DisplayTypeAuthenticatorFactory {

	public static final String PROVIDER_ID = "phoneNumberAsUserName-authenticator";

	// 单例
	private static final LoginOnPhoneNumberAsUserName SINGLETON = new LoginOnPhoneNumberAsUserName();

	@Override
	public Authenticator create(KeycloakSession session) {
		// TODO Auto-generated method stub
		return SINGLETON;
	}

	@Override
	public void init(Scope config) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		// TODO Auto-generated method stub
		return "手机作为用户名登录";
	}

	@Override
	public String getReferenceCategory() {
		// TODO Auto-generated method stub
		return PasswordCredentialModel.TYPE;
	}

	@Override
	public boolean isConfigurable() {
		// TODO Auto-generated method stub
		return false;
	}

	public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
			AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.ALTERNATIVE,
			AuthenticationExecutionModel.Requirement.DISABLED };

	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
		return REQUIREMENT_CHOICES;
	}

	@Override
	public boolean isUserSetupAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getHelpText() {
		// TODO Auto-generated method stub
		return "手机作为用户名登录";
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Authenticator createDisplay(KeycloakSession session, String displayType) {
		// TODO Auto-generated method stub
		return null;
	}

}
