<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
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

        ${msg("emailForgotTitle")}
    <#elseif section = "form">
        <form id="kc-reset-password-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="username" class="${properties.kcLabelClass!}"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <#if auth?has_content && auth.showUsername()>
                        <input type="text" id="username" name="username" class="${properties.kcInputClass!}" autofocus value="${auth.attemptedUsername}"/>
                    <#else>
                        <input type="text" id="username" name="username" class="${properties.kcInputClass!}" autofocus/>
                    </#if>
                </div>
            </div>
            <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                        <span><a href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
                    </div>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
                </div>
            </div>
        </form>
    <#elseif section = "info" >
        ${msg("emailInstruction")}
    </#if>
</@layout.registrationLayout>
