<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("loginTitle",realm.name)}
    <#elseif section = "header">

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


        ${msg("loginTitleHtml",scene)}
    <#elseif section = "form">
        <form id="kc-totp-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="totp" class="${properties.kcLabelClass!}" >手机号</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input id="totp"
                        name="phoneNumber" 
                        type="text" 
                        class="${properties.kcInputClass!}"
                        value="${phoneNumber!''}"
                        />
                </div>
            </div>
           
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="totp" class="${properties.kcLabelClass!}">验证码</label>
                </div>
                <div class="col-xs-8 col-sm-8 col-md-8 col-lg-8">
                    <input id="totp" name="code" type="text" class="${properties.kcInputClass!}"/>
                </div>
                <div class="col-xs-4 col-sm-4 col-md-4 col-lg-4">
                    <button tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                        name="submitAction" 
                        id="getcode" 
                        type="submit"
                        value="getcode">验证码</button>
                </div>
                
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <#--  <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                    
                        <span><a href="${url.loginUrl}">${kcSanitize(msg("usernameOrEmail"))?no_esc}</a></span>
                    </div>
                </div>  -->

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <div class="${properties.kcFormButtonsWrapperClass!}">
                        <button tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                         name="submitAction" 
                         id="kc-login" 
                         type="submit" value="ok">确定</button>
                        <#--  <button class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                               name="submitAction" id="kc-login" type="submit" value="ok">登陆</button>
                        <button class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                               name="submitAction" id="kc-cancel" type="submit" value="cancel">${msg("doCancel")}</button>  -->
                    </div>
                </div>
            </div>
        </form>
        
        
        <#--  <#if client?? && client.baseUrl?has_content>
            <p><a href="${client.baseUrl}">${kcSanitize(msg("backToApplication"))?no_esc}</a></p>
        </#if>  -->
        <#if sendCode??>
            <script>
                let getcodeBtn =  document.querySelector("#getcode");
                getcodeBtn.disabled = "disabled"
                let a = 60;
                let code =  setInterval(()=>{
                    a--
                    if(a>0){
                        getcodeBtn.innerHTML=a+"s"
                    }else{
                        clearInterval(code)
                        getcodeBtn.disabled = ""
                        getcodeBtn.innerHTML = "验证码"
                    }
                },1000)
            </script>
        </#if>

        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration" style="text-align:center">
                <#if realm.resetPasswordAllowed>
                    <span><a tabindex="5" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
                </#if>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <span>${msg("noAccount")} <a tabindex="6" href="${url.registrationUrl}">${msg("doRegister")}</a></span>
            </div>
        </#if>
    </#if>
</@layout.registrationLayout>