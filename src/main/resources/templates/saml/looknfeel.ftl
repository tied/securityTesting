<html>
<head>
    <title>Look and Feel Configurations</title>
    <meta name="decorator" content="atl.general">
    <script>
        <#include "/js/looknfeel.js">
    </script>
    <style>
        <#include "/css/looknfeel.css">
    </style>
</head>

<body>
<#include "*/saml/headers/samlheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="mo-look-and-feel"  role="tabpanel" >

    <h1>Look and Feel Configurations</h1>
    <hr class="header"/>

    <form id="looknfeel-settings-form" class="aui long-label" action="" method="POST">
        <input type="hidden" id="looknfeel_settings_submitted" name="looknfeelSettingsSubmitted" value="true"/>
        <input type="hidden" name="atl_token" value="${xsrfToken}" />


        <h2>Login Page</h2>


        <hr class="header"/>

        <div class="login-settings-div" id="login-settings-div">

            <br/>

            <div id="show-login-buttons" class="field-group">
                <label for="show-login-button-switch">Show Login Buttons</label>
                <aui-toggle id="show-login-button-switch" name="showLoginButtons" label="Show Login Buttons"
                <#if showLoginButtons==true> checked="true" </#if>  value="true"></aui-toggle>
                <div class="description">This options decides whether SSO buttons will be shown on your login page or not. <br>
                    Disable if you want to allow IDP-initiated SSO only.
                </div>
                <br>
            </div>

            <div class="field-group">
                <label for="loginButtonText">Login Button Text: </label>
                <input type="text"  id="loginButtonText" name="loginButtonText" value="${loginButtonText}"
                       class="text long-field"/>
                <div class="description">Set button label for SSO button shown on login page.
                    This button will be shown only if single IDP is configured.
                    <br>In case of multiple IDP Configuration you will see the IDP name on login Page.
                </div>
                <br>


                <br>
            </div>

            <div class="field-group" id="enableLoginTemplatediv">
                <label for="loginButtonText">Enable Custom Login Template: </label>
                <aui-toggle id="enableLoginTemplate" value="true" label="Custom Login Template For Bamboo"

                            name="enableLoginTemplate" value="true" <#if enableLoginTemplate==true> checked </#if> ></aui-toggle>

                &nbsp;&nbsp;<span class="mo-label" for="enableLoginTemplate" > Show users the below Custom Login Page For Bamboo </span>

            </div>
            <div id="custom_login_template" class="group">

				<textarea id="loginTemplate" name="loginTemplate" class="textarea long-field"
                          cols="100" rows="6">${loginTemplate?html}</textarea>
                <div class="description">
                    This is default template for Single IDP setup but you can customize it according to your need or you
                    can design your own SSO login template.<br>
                    Use this <b>Code to add new SSO button</b> option to add SSO button in case of multiple IDP configuration.
                </div>
                <br/>
            </div>

            <div id="loginTemplateSubfields" class="subfield">
                <div id="loginTemplateBackdoor" class="field-group">
                    <label for="loginTemplateBackdoor">Default login page URL: </label>
                    <table class="mo-table">
                        <tbody>
                        <tr>
                            <td class="td-show-box">
                                <div  class="show-box">
                                    <p id="p1"class="copyable"><em>${settings.getLoginPageUrl()}?show_login_form=true</em></p>
                                </div>
                            </td>

                            <td>  <span id="p1-copied-message" title="Copied" class="copy-title" style="display:none"></span></td>
                        </tr>

                        </tbody>

                    </table>
                    <div class="description">Use this URL to access Bamboo default login page when custom login template is turned on.
                    </div>
                </div>
                <br>
                <div id="loginTemplateNewSSOButton"class="field-group">
                    <label for="loginTemplateNewSSOButton">Code to add new SSO button </label>
                    <table class="mo-table">
                        <tbody>
                        <tr>
                            <td class="td-show-box">
                                <div class="show-box">
                                    <code id="p3" class="copyable">&lt;div class="field-group"&gt; &lt;input type="button" class="aui-button aui-button-primary"
                                        value="Single Sign-On" onclick="AuthUrl('IDP ID')"&gt;
                                        &lt;/div&gt;
                                    </code>
                                </div>
                            </td>
                            <td>  <span id="p3-copied-message" title="Copied" class="copy-title" style="display:none"></span></td>
                        </tr>
                        </tbody>
                    </table>
                    <div class="description">IDP ID can be obtained from <b>IDP Configuration</b> tab for each IDP.
                        Please get the IDP ID from <b>ACS URL for IDP-Initiated SSO</b>.
                        <br> If you have configured only one IDP, this is optional.
                    </div>

                </div>
            </div>
        </div>

        <h2> SSO Error Message</h2>


        <hr class="header"/>

        <div id="sso-error-settings-div">
            <div id="customTemplates" class="field-group">

                <aui-toggle id="enableErrorMsgTemplate" value="true" label="Custom Error Template For Bamboo"

                            name="enableErrorMsgTemplate" value="true" <#if enableErrorMsgTemplate==true> checked </#if> ></aui-toggle>

                &nbsp;&nbsp;<span class="mo-label" for="enableErrorMsgTemplate" > On error, redirect users to this custom error page </span>
                <br><br>
                <textarea id="errorMsgTemplate" name="errorMsgTemplate"
                          class="textarea long-field"
                          cols="100" rows="6">${errorMsgTemplate?html}</textarea>
                <div class="description">
                    This template will be shown to the user when SSO
                    fails. <br>Use <b>$baseUrl</b> key for login page URL, suggested use for href in &lt; a&gt; .
                </div>
            </div>
        </div>

        <div class="field-group">
            <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>
        </div>
    </form>
</div>
<#include "*/footer.ftl" parse=true>
</body>
</html>