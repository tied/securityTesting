<html>
<head>
    <title>Post Logout Configurations</title>

    <meta name="decorator" content="atl.general">
    <script>
        <#include "/js/postlogout.js">
    </script>
</head>

<body>
<#include "*/saml/headers/samlheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="mo-post-logout-rules"  role="tabpanel" >
    <h1>Post Logout Configurations</h1>
    <hr class="header"/>
    <form id="postlogout-settings-form" class="aui long-label" action="" method="POST">
    <input type="hidden" name="atl_token" value="${xsrfToken}" />
        <input type="hidden" id="postlogout_settings_submitted" name="postLogoutSettingsSubmitted" value="true"/>

        <div id="post-logout-url-div" >

            <div class="field-group">
                <label for="customLogoutURL">Custom Logout URL :</label>
                <input type="url" id="customLogoutURL" name="customLogoutURL" value="${customLogoutURL}"
                       placeholder="Custom Logout URL" class="text long-field"
                />
                <div class="description">
                    Redirect here after logging out of Bamboo. Leave empty to redirect to Bamboo login page.
                </div>

            </div>
        </div>

            <br>

            <div id="logoutTemplateDiv" class="group" style="display: block;" >
                <h4>OR</h4>
                <br>
                <div>

                    <aui-toggle id="enableLogoutTemplate" value="true" label="Custom Logout Template For Bamboo Software"

                                name="enableLogoutTemplate" value="true"<#if enableLogoutTemplate == true> checked="true" </#if></aui-toggle>

                    &nbsp;&nbsp;<span class="mo-label" for="enableLogoutTemplate" > Show a custom logout page for Bamboo</span>
                </div>
                <br>
                <legend><span>Custom Logout Template for Bamboo:</span></legend>
                <textarea id="logoutTemplate" name="logoutTemplate" class="textarea long-field"
                          cols="100" rows="6">${logoutTemplate?html}</textarea>
                <div class="description">
                    Set a custom logout page for Bamboo when user logs out. Use <b>$baseUrl</b> for Bamboo login page URL.<br>
                    <b>Note</b>: This page will also be shown when Single Logout is setup for Bamboo so that users are not
                    sent on a the login loop again.
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