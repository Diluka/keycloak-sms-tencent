package org.keycloak.sms.auth;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

public class VerificationCodeProvider implements RealmResourceProvider {

	private KeycloakSession session;
	public static final String FIELD_VERIFICATIONCODE = "verificationCode";

	public VerificationCodeProvider(KeycloakSession session) {
		// TODO Auto-generated constructor stub
		this.session = session;
	}

	@GET
	@Produces("text/plain; charset=utf-8")
	public String get() {
		Map<String, String> map = null;
		try {
			map = ImageCodeUtil.generateCodeAndPic();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String code = map.get("code");
		storeVerificationCode(code);
		String codeImg = map.get("codePic");

		return codeImg;
	}

	// 验证码的值存储到session
	private void storeVerificationCode(String code) {
		this.session.setAttribute(FIELD_VERIFICATIONCODE, code);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getResource() {
		// TODO Auto-generated method stub
		return this;
	}

}
