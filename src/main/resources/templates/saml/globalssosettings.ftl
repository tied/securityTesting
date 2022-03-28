<html>
<head>
    <meta name="decorator" content="atl.general">
    <script>
        AJS.$(function() {
            AJS.$(".aui-nav li").removeClass("aui-nav-selected");
            AJS.$("#mo-global-sso-settings").addClass("aui-nav-selected");
            AJS.$("#mo-saml").addClass("aui-nav-selected");
        });
         <#include "/js/globalssosettings.js">
    </script>
</head>
<body>
<#include "*/saml/headers/samlheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="globalssosettings" role="tabpanel" >
    <h1>Global SSO Settings</h1>
    <hr class="header"/>
    <p>Control how your users and administrators login using SSO.</p>
    <form id="global-sso-settings-form" class="aui long-label" action="" method="POST">
        <input type="hidden" name="globalSettingsSubmitted" value="true"/>
        <input type="hidden" name="atl_token" value="${xsrfToken}" />
        <br/>
        <div class="sso-settings-div" id="sso-settings-div">
            <div class="field-group">
                <label for="enableSAMLSSO" ><strong>Enable SAML SSO for Bamboo server:</strong></label>
                <aui-toggle id="enableSAMLSSO" value="true" label="Enable SAML SSO for bamboo server" name="enableSAMLSSO"
                            value="true" <#if enableSAMLSSO==true> checked </#if> ></aui-toggle>
                <span style="vertical-align:middle;">
                        <div class="description">Enabling <strong>Single Sign On for Bamboo</strong> allows users to login via SSO for Bamboo Application.</div>
	                </span>
	            <div id="sso-url-warning" class="subfield">
                    <div class="aui-message aui-message-info">
                        <span>
                            In case you want to <strong>enable</strong> the SSO Login via API call, use this API request<br>
                            <code>
                                <strong>Post</strong> : ${settings.getBaseUrl()}/plugins/servlet/enablessoapi?action=enableSSO<br>
                            </code>
                        </span>
                        <br>
                        <span>
                            In case you want to <strong>disable</strong> the SSO Login via API call, use this API request<br>
                            <code>
                                <strong>Post</strong> : ${settings.getBaseUrl()}/plugins/servlet/enablessoapi?action=disableSSO<br>
                                <br>
                                <strong>Headers</strong> :-
                                <ul>
                                    <li>
                                        Content-Type : application/json
                                    </li>
                                    <li>
                                        Authorization : Basic &lt;base64 encoded admin_username:password&gt;
                                    </li>
                                </ul>
                            </code>
                        </span>
                    </div>
                </div>
            </div>
            <br/>
        </div>
        <br/>

        <div class="advanced-sso-settings-div" id="advanced-sso-settings-div">

            <div class="field-group">

                <label for="enablePasswordChange" ><strong>Allow Users to Change Password:</strong></label>
                <aui-toggle id="enablePasswordChange" value="true" label="Allow Users to Change Password" name="enablePasswordChange"
                            value="true" <#if enablePasswordChange==true> checked </#if> ></aui-toggle>
                <span style="vertical-align:middle;">
                    <div class="description">If enabled, end-users will be able to reset their local application password as they will be able to access change password, forget password link and if disabled only system admin can reset their password.</div>
                </span>
            </div>
            <br/>
            <div class="field-group">
                <label for="pluginApiAccessRestriction" ><strong>Restrict access to plugin APIs:</strong></label>
                <aui-toggle id="pluginApiAccessRestriction" value="true" label="Restrict access to plugin APIs" name="pluginApiAccessRestriction"
                            value="true" <#if pluginApiAccessRestriction==true> checked </#if> ></aui-toggle>
                <span style="vertical-align:middle;">
                        <div class="description">When enabled, the plugin's APIs will not be accessible from outside this Bamboo instance.</div>
                        <div id="warningforapirestriction" class="aui-message aui-message-warning closeable" style="display:none;">
                            Make sure to check your Referrer Policy for the appropriate setting if Bamboo runs behind a reverse proxy.
                        </div>
            </div>
            <br/>
        </div>

        <br/>
        <div class="field-group">
            <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>
        </div>
    </form>
</div>
<#include "*/footer.ftl" parse=true>
</body>
</html>