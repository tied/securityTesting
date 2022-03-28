<html>
<head>
    <title>SAML SSO Configuration</title>
    <meta name="decorator" content="atl.general">
    <script>
            <#include "/js/sessionmanagement.js">
    </script>
</head>
<body>
    <#include "*/saml/headers/samlheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="configure-sp" role="tabpanel">
    <h2>Remember-Me Settings</h2>
    <hr class="header"/>
    <form id="remember-me-form" name="remember-me-form" class="aui long-label" action="" method="POST">
    <input type="hidden" name="atl_token" value="${xsrfToken}" />
        <input type="hidden" name="rememberMeSubmitted" value="true"/>
        <div id="enable-remember-my-login" class="field-group">
            <label for="enableRememberMeCookie">Set Remember Me-Cookie</label>
            <aui-toggle id="enableRememberMeCookie" name="enableRememberMeCookie" <#if enableRememberMeCookie==true>
                        checked </#if> label="Enable Remember-Me Cookie" value="true"
                        onchange="enableRememberMe()"></aui-toggle>
            <div class="description">User stays logged in until user explicitly
                logs out.
            </div>
            <br>
        </div>
    </form>
    <br>
    <p style="font-size:16pt;">User Session Timeout</p>
    <hr class="header"/>
    <div style="padding-left: 1%">
        <p>
            An end-user's session in Bamboo will timeout after 30 minutes of inactivity. This is the
            default
            value for session timeout.
        </p>
        <p>You can change the user session timeout.</p>
        <p>
        <ol style="line-height: 220%">
            <li>
                Open<code id="p2" class="copyable" style="display: inline; border:0px !important; padding: 5px 0 5px 0px !important; margin: 0em !important;" >
                &lt;bamboo-installation folder&gt;\atlassian-bamboo\WEB-INF\web.xml </code>
                <span id="p2-copied-message" title="Copied" class="copy-title" style="display:none"></span>
            </li>
            <li>
                Set value of &lt;session-timeout&gt; tag in minutes. By default it is 30. You can set
                it
                to 0 for unlimited login time.
            </li>
        </ol>
        </p>
    </div>
</div>
<br><br>
    <#include "*/footer.ftl" parse=true>
</body>
</html>