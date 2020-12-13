package org.keycloak.sms.auth;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class VerificationCodeProviderFactory implements RealmResourceProviderFactory{

	public static final String ID = "verificationCode";
	
	@Override
	public RealmResourceProvider create(KeycloakSession session) {
		// TODO Auto-generated method stub
		return new VerificationCodeProvider(session);
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
		return ID;
	}

}
