<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('otp') displayInfo=false; section>
    <#if section = "header">
        Verify OTP
    <#elseif section = "form">
        <form id="kc-otp-form" action="${url.loginAction}" method="post">
            <#if errors??>
                <div class="alert alert-error">
                    <ul>
                        <#list errors as error>
                            <li>${error}</li>
                        </#list>
                    </ul>
                </div>
            </#if>
            <div class="${properties.kcFormGroupClass!}">
                <label for="otp" class="${properties.kcLabelClass!}">Enter 6-digit OTP</label>
                <input type="text" id="otp" name="otp" class="${properties.kcInputClass!}" autofocus/>
            </div>
            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="Verify"/>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
