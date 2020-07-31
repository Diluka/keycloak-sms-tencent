<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo displayWide=(realm.password && social.providers??); section>
    <#if section = "header">
        <!-- 添加用户名密码 和 手机号 切换 begin -->
        <script type="text/javascript">
            function fillAndSubmit(authExecId) {
                document.getElementById('authexec-hidden-input').value = authExecId;
                document.getElementById('kc-select-credential-form').submit();
            }
            /**
            * [通过参数名获取url中的参数值]
            * 示例URL:http://htmlJsTest/getrequest.html?uid=admin&rid=1&fid=2&name=小明
            * @param  {[string]} queryName [参数名]
            * @return {[string]}           [参数值]
            */
            function GetQueryValue(queryName) {
                var query = decodeURI(window.location.search.substring(1));
                var vars = query.split("&");
                for (var i = 0; i < vars.length; i++) {
                    var pair = vars[i].split("=");
                    if (pair[0] == queryName) { return pair[1]; }
                }
                return null;
            }

            window.onload = function(){
                var containerUl = document.querySelector(".zk_loginActionMethods"); 
                if(!containerUl || containerUl.children.length<2){
                    containerUl.style.display = "none";
                    return;
                }
                var executionStr = GetQueryValue("execution");
                var el = document.getElementById(executionStr);
                if(el){
                    el.classList.add("active");
                }else{
                    containerUl.firstElementChild.classList.add("active");
                }
            }

        </script>
        <style>
            .zk_loginActionMethods{
                font-size:16pt;
            }
            .thirdExtendLogin img{
                width:26px
            }
        </style>
        <form style="display:none" id="kc-select-credential-form" action="${url.loginAction}" method="post">
            <input type="hidden" id="authexec-hidden-input" name="authenticationExecution" />
        </form>
        <ul class="nav nav-pills nav-justified zk_loginActionMethods">
            <#list auth.authenticationSelections as authenticationSelection>
                <li id="${authenticationSelection.authExecId}" role="presentation" title="${msg('${authenticationSelection.helpText}')}"><a href="#" onclick="fillAndSubmit('${authenticationSelection.authExecId}')">${msg('${authenticationSelection.displayName}')}</a></li>
            </#list>
        </ul>
        <!-- 添加用户名密码 和 手机号 切换 end -->
        
        ${msg("doLogIn")}
    <#elseif section = "form">
    
    <div id="kc-form" <#if realm.password && social.providers??>class="${properties.kcContentWrapperClass!}"</#if>>
      <div id="kc-form-wrapper" <#if realm.password && social.providers??>class="${properties.kcFormSocialAccountContentClass!} ${properties.kcFormSocialAccountClass!}"</#if>>
        <#if realm.password>
            <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <div class="${properties.kcFormGroupClass!}">
                    <label for="username" class="${properties.kcLabelClass!}"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>

                    <#if usernameEditDisabled??>
                        <input tabindex="1" id="username" class="${properties.kcInputClass!}" name="username" value="${(login.username!'')}" type="text" disabled />
                    <#else>
                        <input tabindex="1" id="username" class="${properties.kcInputClass!}" name="username" value="${(login.username!'')}"  type="text" autofocus autocomplete="off" />
                    </#if>
                </div>

                <div class="${properties.kcFormGroupClass!}">
                    <label for="password" class="${properties.kcLabelClass!}">${msg("password")}</label>
                    <input tabindex="2" id="password" class="${properties.kcInputClass!}" name="password" type="password" autocomplete="off" />
                </div>

                <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                    <div id="kc-form-options">
                        <#if realm.rememberMe && !usernameEditDisabled??>
                            <div class="checkbox">
                                <label>
                                    <#if login.rememberMe??>
                                        <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" checked> ${msg("rememberMe")}
                                    <#else>
                                        <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox"> ${msg("rememberMe")}
                                    </#if>
                                </label>
                            </div>
                        </#if>
                        </div>
                        <div class="${properties.kcFormOptionsWrapperClass!}">
                            <#if realm.resetPasswordAllowed>
                                <span><a tabindex="5" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
                            </#if>
                        </div>

                  </div>

                  <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                      <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                      <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
                  </div>

                <!-- 第三方登录  begin-->
                  <div class="thirdExtendLogin">
                    <span style="font-size: 15pt;vertical-align: middle;">第三方登录</span>
                    <div style="display:inline-block">
                        <button>
                            <span class="mat-button-wrapper">
                                <img _ngcontent-lmq-c139="" alt="" src="https://account.cnblogs.com/images/oauth/WeChat.png">
                            </span>
                            <div matripple="" class="mat-ripple mat-button-ripple">
                            </div>
                            <div class="mat-button-focus-overlay"></div>
                        </button><!---->
                        <button _ngcontent-lmq-c139="" mat-stroked-button="" class="mat-focus-indicator mat-tooltip-trigger mat-stroked-button mat-button-base ng-tns-c139-2 ng-star-inserted" aria-describedby="cdk-describedby-message-1" cdk-describedby-host="">
                            <span class="mat-button-wrapper">
                                <img _ngcontent-lmq-c139="" alt="" src="https://account.cnblogs.com/images/oauth/QQ.png">
                            </span>
                            <div matripple="" class="mat-ripple mat-button-ripple">
                            </div>
                            <div class="mat-button-focus-overlay"></div>
                        </button><!---->
                        <button _ngcontent-lmq-c139="" mat-stroked-button="" class="mat-focus-indicator mat-tooltip-trigger mat-stroked-button mat-button-base ng-tns-c139-2 ng-star-inserted" aria-describedby="cdk-describedby-message-2" cdk-describedby-host="">
                            <span class="mat-button-wrapper"><img _ngcontent-lmq-c139="" alt="" src="https://account.cnblogs.com/images/oauth/GitHub.png"></span>
                            <div matripple="" class="mat-ripple mat-button-ripple"></div>
                            <div class="mat-button-focus-overlay"></div></button>
                            <!----><!----></div><!----><!----><!----><!----></div>
                            <!-- 第三方登录  end-->
            </form>
        </#if>
        </div>
        <#if realm.password && social.providers??>
            <div id="kc-social-providers" class="${properties.kcFormSocialAccountContentClass!} ${properties.kcFormSocialAccountClass!}">
                <ul class="${properties.kcFormSocialAccountListClass!} <#if social.providers?size gt 4>${properties.kcFormSocialAccountDoubleListClass!}</#if>">
                    <#list social.providers as p>
                        <li class="${properties.kcFormSocialAccountListLinkClass!}"><a href="${p.loginUrl}" id="zocial-${p.alias}" class="zocial ${p.providerId}"> <span>${p.displayName}</span></a></li>
                    </#list>
                </ul>
            </div>
        </#if>
      </div>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration">
                <span>${msg("noAccount")} <a tabindex="6" href="${url.registrationUrl}">${msg("doRegister")}</a></span>
            </div>
        </#if>
    </#if>

</@layout.registrationLayout>
