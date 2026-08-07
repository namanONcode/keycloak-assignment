<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('mobile') displayInfo=false; section>
    <#if section = "header">
        Enter Mobile Number
    <#elseif section = "form">
        <form id="kc-mobile-form" action="${url.loginAction}" method="post">
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
                <label for="mobile" class="${properties.kcLabelClass!}">Mobile Number</label>
                <input type="text" id="mobile" name="mobile" class="${properties.kcInputClass!}" autofocus/>
            </div>
            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="Send OTP"/>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
