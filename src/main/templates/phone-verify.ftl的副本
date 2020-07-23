<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("loginTitle",realm.name)}
    <#elseif section = "header">
        ${msg("loginTitleHtml",scene)}
    <#elseif section = "form">
        <form id="kc-totp-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="totp" class="${properties.kcLabelClass!}" >手机号</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input id="totp"
                        name="phone" 
                        type="text" 
                        class="${properties.kcInputClass!}"
                        value="${phone!''}"
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
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                    
                        <span><a href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
                    </div>
                </div>

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
        <#if client?? && client.baseUrl?has_content>
            
        </#if>
        <#if sendCode??>
            <script>
                window.onLo
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
    </#if>
</@layout.registrationLayout>