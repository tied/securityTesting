<html>
<head>
    <title>Header Based Auth - Configuration</title>
    <meta name="decorator" content="atl.general">
    <script>
            <#include "/js/headerAuthentication.js" parse=true>
    </script>
</head>

<body>
    <#include "/templates/headerauthentication/headers/headerbasedauthheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="headerbasedauth" role="tabpanel" >
    <p style="font-size:16pt;">Header Authentication</p>
    <hr class="header"/>
    <form id="header-based-auth-form" class="aui long-label" action="" method="POST">
    <input type="hidden" name="atl_token" value="${xsrfToken}" />
        <input type="hidden" name="headerbasedauthSubmitted" value="true"/>
        <br/>
        <br/>
        <div>
            <label for="enableHeaderAuthentication"><strong>Enable Header Based Authentication: </strong></label>
            <span style="vertical-align:middle;">
                <aui-toggle id="enableHeaderAuthentication" value="true" label="Enable Header Authentication" name="enableHeaderAuthentication"
                        value="true" <#if enableHeaderAuthentication== true> checked="true" </#if></aui-toggle>
            </span>

            <div id = "headerAuthenticationAttributeDiv" class="field-group">
                <br>
                <label for="headerAuthenticationAttribute">Authentication Attribute Name :
                    <span class="aui-icon icon-required">(required)</span>
                </label>
                <input type="text" required="true" id="headerAuthenticationAttribute" name="headerAuthenticationAttribute"
                       style="width: 20%;" value="${headerAuthenticationAttribute}" placeholder="Authentication Attribute Name"
                       class="text long-field"/>
                <div class="description">Enter name of the attribute in request header which contains the username of bamboo user.</div>
                <br>
            </div>
            <div>
                <br>
                <div style="display: block;width: 90%;">
                    <div class="aui-message aui-message-info">
                        <span>
                           For Single Sign-on, redirect the user to the following endpoint with the authentication information in the HTTP Header. <br><br>
                            <code>
                                <strong>Redirect Endpoint</strong> : ${settings.getSpBaseUrl()}/plugins/servlet/saml/header<br>
                                <strong>Method</strong> : GET/POST<br>
                                <strong>Header Parameters</strong> :-
                                <ul>
                                    <li>
                                       Authentication Attribute Name : <span id="effective_username" style="font-weight: bold"></span>
                                    </li>
                                    <li>
                                        relay-state-url : Redirect URL after login
                                    </li>
                                </ul>
                            </code>
                        </span>
                    </div>
                </div>

                <br/>
            </div>
        </div>
        <br/>

        <div>
            <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>
        </div>

    </form>
</div>
    <#include "*/footer.ftl" parse=true>
</body>
</html>