<html>
    <head>
        <title>Backup/Restore Configurations</title>
        <meta name="decorator" content="atl.general">
        <link rel="stylesheet">

        <script>
            AJS.$(document).ready(function() {
                AJS.$(".aui-nav li").removeClass("aui-nav-selected");
                AJS.$("#importexport").addClass("aui-nav-selected");
            });
        </script>
        <style>
            .aui-page-panel-content{
                padding: 0px !important;
            }

            .aui-page-panel{
                margin: 0px !important;
            }
        </style>
    </head>
    <body>
        <#include "*/header.ftl" parse=true>
        <div class="tabs-pane active-pane" id="importexport"  role="tabpanel" >
            <p style="font-size:16pt;">1. Import/Export app Configurations</p>
            <hr class="header"/>
            <p>This tab will help you to transfer your app configurations when you change your Bamboo instance;e.g. when you switch from test environment to production. Follow these 3 simple steps to do that:
            <ol>
                <li>Download app configuration file by clicking on the link given below.</li>
                <li>Install the app on new Bamboo instance.</li>
                <li>Upload the configuration file in Import app Configurations section.</li>
            </ol>
            </p>
            <p> And just like that, all your app configurations will be transferred! </p>
            <p>You can also send us this file along with your support query.</p>

            <hr class="header"/>

            <p style="font-size:16pt;"><b>Export app Configurations</b></p><br>
            <#if isApplicationConfigured == true>
                <a href="downloadconfigurations"><b>Click Here</b></a> to download your app configurations file
            <#else>
                <b style="color:grey">Click Here</b> to download your app configurations file
            </#if>

            <hr class="header" />
            <#if (settings.getCurrentBuildNumber() < 60001)>
                <p style="font-size: 13pt"><b>Import app Configurations</b></p><p style="color: red;">This feature is not working with current Bamboo Server version.</p>
            <#else>
                <p style="font-size: 13pt"><b>Import app Configurations</b></p>
            </#if>

      	    <#assign uploadJSONFile = settings.getBaseUrl() + "/plugins/servlet/bamboo-oauth/uploadconfigurations">
            <form id="importConfigurations" class="aui long-label" action="${uploadJSONFile}" method="post" enctype="multipart/form-data">
                <input type="hidden" name="importFileSubmitted" value="true" />
                <table style="width:100%">
                    <tr>
                        <td width="20%">Choose File
                            <span style="color:red">*</span>: </td>
                        <td>
                            <input type="file" id="configFile" name="configFile" class="ignore-inline-attach" accept=".json" required/></input> &nbsp; &nbsp;
                            <input type="submit" value="Import" class="aui-button aui-button-primary" />
                        </td>
                    </tr>
                    <tr>
                        <td width="20%"></td>
                        <td>
                            <div class="description">Upload app_configurations.json file here</div>
                        </td>
                    </tr>
                </table>
            </form>
        <br />

    </div>

    <div>
<p style="font-size:16pt;">2. Configure/Fetch app configuration via REST API</p>
<hr class="header"/>
</div>
<div class="aui-message aui-message-info">
    <p>Note: The user must have administrator credentials to fetch the app configuration. To fetch app
        configuration, you need to use GET and to configure the app use POST.</p>
    <p>Use Basic Authentication and it will be base64 encoded.</p>
</div>

    <div>
    <table style=" width: 100%;">
        <tbody>
            <td style=" width: 39%;vertical-align: initial;">
                <br>
                <p style="font-size:12pt;">A. Download/Fetch App configuration.</p><br>
                <div style="margin-left: 35px;">
                    <strong>URL&emsp;&emsp;</strong>
                    : ${settings.getBaseUrl()}/plugins/servlet/oauth/configureplugin<br>
                    <strong>Method</strong>
                    : GET<br>
                    <strong>Request Parameters:</strong><br>
                    <ul>
                        <strong>Headers</strong>
                        :-
                        <div style="margin-left: 35px;">
                            <li>
                                Content-Type : application/json
                            </li>
                            <li>
                                Authorization : Basic Authentication
                                <base64 encoded="encoded" admin_username:password="admin_username:password"></li>
                            </div>
                        </ul>
                        <p>You will get JSON raw data in response.Copy and store raw data in the JSON file that you will
                            need during the configuration.</p><br>

                    </td>
                    <td>
                        <div></div>
                    </td>
                    <td style=" width: 59%;vertical-align: initial;"><br>
                        <p style="font-size:12pt;">Sample Response</p>
                        <textarea
                            rows="12"
                            class="textarea long-field"
                            cols="74"
                            style="background-color: rgb(235, 236, 240);line-height:1.8; width:671px;"
                            readonly="readonly">{
                            { "PLUGIN_NAME": "OAuth/OpenID Connect (OIDC) for bamboo SSO", "PLUGIN_VERSION": "1.2.4", "Identity
                            Providers": [{ "ID": "304ec56e-3931-4392-b109-33a0dcfe51cd", "Name": "Custom OpenID", "Application
                            Configuration": { "IDP_ID": "304ec56e-3931-4392-b109-33a0dcfe51cd", "DEFAULT_APP_NAME": "Custom
                            OpenID", "CUSTOM_APP_NAME": "Custom OpenID", "CLIENT_ID": "", "CLIENT_SECRET": "",
                            "SINGLE_LOGOUT_URL": "", "SCOPE": "openid email profile", "USE_STATE_PARAMETER": true, "PUBLIC_KEY":
                            "", "ENABLE_CHECK_ISSUER_FOR": false, "CHECK_ISSUER_FOR": "Default", "CUSTOM_ISSUER_VALUE": "",
                            "AUTHORIZE_END_POINT": "https://login.xecurify.com/moas/idp/openidsso", "ACCESSTOKEN_END_POINT":
                            "https://login.xecurify.com/moas/rest/oauth/token", "USERINFO_END_POINT": "",
                            "FETCH_GROUPS_ENDPOINT": "", "SEND_TOKEN_AUTHORIZATION_PARAMETERS_IN": "HttpHeader",
                            "ACR_VALUE_CHECK": false, "DIRECTORY_ID": "", "TENANT_ID": "", "DOMAIN_NAME": "", "HOST_NAME": "",
                            "REALM_NAME": "" }, "Attribute Mapping": { "USERNAME_ATTRIBUTE": "sub", "EMAIL_ATTRIBUTE": "",
                            "ENABLE_REGEX_PATTERN": false, "REGEX_PATTERN": "", "KEEP_EXISTING_USER_ATTRIBUTES": true,
                            "FIRST_NAME_ATTRIBUTE": "", "LAST_NAME_ATTRIBUTE": "", "FULL_NAME_ATTRIBUTE": "",
                            "USE_SEPARATE_NAME_ATTRIBUTE": false, "LOGIN_USER_BY_ATTRIBUTE": "username",
                            "ENABLE_LANGUAGE_MAPPING": false, "LANGUAGEMAP": "", "CUSTOM_LANGUAGE_ATTRIBUTE": "" }, "Group
                            Mapping": { "ROLE_ATTRIBUTE": "", "KEEP_EXISTING_USER_ROLES": true, "CREATE_USER_IF_ROLE_MAPPED":
                            false, "ROLE_MAPPING": {}, "DEFAULT_GROUPS": ["bamboo-software-users"], "RESTRICT_USER_CREATION":
                            false, "ENABLE_DEFAULT_GROUPS_FOR": "newUsers", "ON_THE_FLY_GROUP_MAPPING": false,
                            "CREATE_NEW_GROUPS": false, "ON_THE_FLY_ASSIGN_NEW_GROUPS_ONLY": false,
                            "ON_THE_FLY_DO_NOT_REMOVE_GROUPS": [""] } }], "Sign In Settings": { "ENABLE_FOR_SOFTWARE": "true",
                            "LOGIN_BUTTON_TEXT": "Use OAuth Login", "SSO_BUTTON_LOCATION": "After Login Button", "RELAY_STATE":
                            "", "DISABLE_DEFAULT_LOGIN": "false", "ENABLE_BACKDOOR": "false", "ENABLE_AUTO_REDIRECT_DELAY": "",
                            "ALLOWED_DOMAINS": "", "ADMIN_SESSION_OPTION": "", "BACKDOOR_KEY": "", "BACKDOOR_VALUE": "",
                            "RESTRICT_BACKDOOR": "false", "BACKDOOR_GROUPS": ["bamboo-software-users"],
                            "PLUGIN_API_ACCESS_RESTRICTION": "false", "ENABLE_FOR_SERVICE_DESK": "true", "ENABLE_FOR_AGENTS":
                            "false", "AGENT_GROUPS": ["bamboo-software-users"], "AGENTS_GROUP_REGEX_PATTERN": "",
                            "DISABLE_DEFAULT_LOGIN_JSD": "false", "ENABLE_BACKDOOR_JSD": "false", "RESTRICT_AUTO_REDIRECT_URL":
                            "", "CUSTOM_LOGOUT_URL": "", "ENABLE_LOGOUT_TEMPLATE": "false", "LOGOUT_TEMPLATE": ""
                            ENABLE_LOGIN_TEMPLATE": "", "ENABLE_LOGIN_TEMPLATE_FOR_SERVICEDESK": "", "LOGIN_TEMPLATE": "",
                            "LOGIN_TEMPLATE_FOR_SERVICEDESK": "", "REMEMBERME_COOKIE": "", "ENABLE_PASSWORD_CHANGE": "",
                            "ENABLE_ERRORMSG_TEMPLATE": "false", "ERROR_MSG_TEMPLATE": "" CUSTOM_ERROR_TEMPLATE": "" }, "Domain
                            Mapping Configurations": { "USE_DOMAIN_MAPPING": "false", "DOMAIN_MAPPING": {} } } }
                        </textarea>
                    </td>
                </tbody>
            </table>
        </div>

        <p style="font-size:12pt;">B. Update App configuration</p><br>
<div style="margin-left: 35px;">
    <strong>URL&emsp;&emsp;</strong>
    : ${settings.getBaseUrl()}/plugins/servlet/oauth/configureplugin<br>
    <strong>Method</strong>
    : POST<br>
    <strong>Request Parameters</strong>
    :<br>
    <ul>
        <strong>Headers</strong>
        :-
        <div style="margin-left: 35px;">
            <li>
                Content-Type : application/json
            </li>
            <li>
                Authorization : Basic Authentication
                <base64 encoded="encoded" admin_username:password="admin_username:password">
            </li>
        </div>
    </ul>
    <ul>
        <strong>Body</strong>
        :-
        <div style="margin-left: 35px;">
            <li>
                The body content type should be in Raw format(application/json).
            </li>
            <li>
                Paste JSON raw data you receive in response to your GET request or download raw data from the link
                above in the Download app Configurations.
            </li>
        </div>
    </ul>
</div>
    </section>
    </div>
    </div>
    </body>
</html>