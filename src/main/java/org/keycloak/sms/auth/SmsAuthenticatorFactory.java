package org.keycloak.sms.auth;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yanfeiwuji
 * @description
 * @date 16:40  2020/2/18
 */
public class SmsAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {


    public static final String PROVIDER_ID = "sms-authenticator";

    // 日志
    private static Logger logger = Logger.getLogger(SmsAuthenticatorFactory.class);

    // 单例
    private static final SmsAuthenticator SINGLETON = new SmsAuthenticator();

    // 权限认证模式
    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
    };


    // 配置属性
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        // appid
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(SmsAuthenticatorContstants.CONF_APP_ID);
        property.setLabel(SmsAuthenticatorContstants.CONF_APP_ID);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("appid");
        configProperties.add(property);

        // appKey
        property = new ProviderConfigProperty();
        property.setName(SmsAuthenticatorContstants.CONF_APP_KEY);
        property.setLabel(SmsAuthenticatorContstants.CONF_APP_KEY);
        property.setType(ProviderConfigProperty.PASSWORD);
        property.setHelpText("appKey");
        property.setSecret(true);
        configProperties.add(property);


        // sign
        property = new ProviderConfigProperty();
        property.setName(SmsAuthenticatorContstants.CONF_SIGN);
        property.setLabel(SmsAuthenticatorContstants.CONF_SIGN);
        property.setType(ProviderConfigProperty.STRING_TYPE);

        property.setHelpText("短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名，签名信息可登录 [短信控制台] 查看");
        configProperties.add(property);


        // templateId
        property = new ProviderConfigProperty();
        property.setName(SmsAuthenticatorContstants.CONF_TWMPLATE_ID);
        property.setLabel(SmsAuthenticatorContstants.CONF_TWMPLATE_ID);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("模板 ID: 必须填写已审核通过的模板 ID。模板ID可登录 [短信控制台] 查看");

        configProperties.add(property);

        // templateParmas
        property = new ProviderConfigProperty();
        property.setName(SmsAuthenticatorContstants.CONF_TEMPLATE_PARAMS);
        property.setLabel(SmsAuthenticatorContstants.CONF_TEMPLATE_PARAMS);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("模板参数: 不同使用 #区分 {code}来表示验证码 {exp}来表示 过期时间 例如 123#{code}#{exp}");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(SmsAuthenticatorContstants.CONF_SMS_CODE_LENGTH);
        property.setLabel(SmsAuthenticatorContstants.CONF_SMS_CODE_LENGTH);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setDefaultValue("6");
        property.setHelpText("验证码长度");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(SmsAuthenticatorContstants.CONF_SMS_CODE_EXP);
        property.setLabel(SmsAuthenticatorContstants.CONF_SMS_CODE_EXP);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("验证码过期时间以分钟为单位");
        property.setDefaultValue("5");
        configProperties.add(property);

    }


    // id
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    // 单例
    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    // 访问内容
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    // 允许用户安装
    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    // 允许用户配置
    @Override
    public boolean isConfigurable() {
        return true;
    }

    // 帮助文字
    @Override
    public String getHelpText() {
        return "短信验证";
    }

    // 显示内容
    @Override
    public String getDisplayType() {
        return "Tencent sms auth";
    }

    // 目录名称
    @Override
    public String getReferenceCategory() {
        return "sms-auth-code";
    }


    // 返回配置信息
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }


    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }


    @Override
    public int order() {
        return 0;
    }
}
