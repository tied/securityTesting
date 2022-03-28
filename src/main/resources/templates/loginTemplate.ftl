    <html>
    <head>
        <title>Custom Login</title>
        <meta name="decorator" content="atl.general">
        <style>
            #main.aui-page-panel {
                border: 1px solid #ccc;
                border-radius: 5px;
                margin: 50px auto 0 auto;
                min-height: 200px;
                overflow: auto;
                max-width: 1000px !important;
                width: 1000px !important;
                padding: 20px;
            }
        </style>
            <script src="https://code.jquery.com/jquery-1.10.2.js"></script>
            <script>
                AJS.$(document).ready(function () {
                    var oautherror=getQueryParameterByName('oautherror');

                    if(oautherror!=null){
                        var htmlerror='<div class="aui-message aui-message-error closeable" style="width: 33%;margin-left: 500px;"><p>We couldn\'t sign you in. Please contact your Administrator. </p></div>';
                        AJS.$(htmlerror).insertBefore(AJS.$("#main"));
                    }
                });

                function getQueryParameterByName(name) {
                    var url = window.location.href;
                    name = name.replace(/[\[\]]/g, "\\$&");
                    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
                    results = regex.exec(url);

                    if (!results) return null;

                    if (!results[2]) return '';

                    return decodeURIComponent(results[2].replace(/\+/g, " "));
                }

                function AuthUrl() {
                    AJS.$.ajax({
                    url: AJS.contextPath()
                        + "/plugins/servlet/oauth/getconfig",
                    type: "GET",
                    error: function () {
                    },
                    success: function (response) {
                        var osDestination = getQueryParameterByName("os_destination");
                        var oauthAuthUrl = AJS.contextPath() + '/plugins/servlet/oauth/auth';
                        if(response.isConfigured == true){
                            if(osDestination){
                                oauthAuthUrl += "?return_to=" + encodeURIComponent(osDestination);
                            }
                            window.location.href=oauthAuthUrl;
                            return;
                        }
                        else{
                            window.alert("You have not configured your OAuth provider");
                            return;
                        }
                    }
                })
                }

                function defaultPage() {
                    var loginPageUrl = AJS.contextPath() + '/userlogin!doDefault.action?show_login_form=true';
                    window.location.href = loginPageUrl;
                }
            </script>
    </head>
    <body id="com-atlassian-bamboo" class="theme-default login aui-layout aui-theme-default">
        <div id="page">
            <div id="full-height-container">
                <header id="header" role="banner">
                    <nav class="aui-header aui-dropdown2-trigger-group" role="navigation" data-aui-responsive="true">
                        <div class="aui-header-inner">
                            <div class="aui-header-primary">
                                <h1 id="logo" class="aui-header-logo aui-header-logo-bamboo"><a href="/"><span
                                        class="aui-header-logo-device">Bamboo</span></a></h1>
                                <ul class="aui-nav" resolved="" style="width: auto;">
                                    <li class="aui-buttons">
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </nav>
                    <br class="clear">
                </header>

                <div id="main" class="aui-page-panel">
                    <table style=" width: 100%;">
                        <tr>
                            <td style=" width: 49%;vertical-align: initial;">
                                <div id="custom-bamboo-login" style="border-radius: 5px;padding: 20px;border: 1px solid lightgrey;">
                                    <h2>Login</h2>
                                    <hr class="header">
                                    <p>Login with your Bamboo username and password</p>
                                    <br><br>
                                        <div class="field-group">
                                            <input type="button" class="aui-button aui-button-primary" value="Login"
                                                onclick="defaultPage()"/>
                                        </div>
                                    </fieldset>
                                </div>
                            </td>
                            <td style="width: 49%;vertical-align: initial;">
                                <div id="custom-sso-login" style="border-radius: 5px;padding: 20px;border: 1px solid lightgrey;">
                                    <h2>Single Sign-On</h2>
                                    <hr class="header">
                                    <p>Login with your corporate account</p>
                                    <br/> &nbsp;&nbsp;
                                    <div class="field-group">
                                        <input type="button" name="oauth" id="oauth" class="aui-button aui-button-primary" value="Login using OAuth" onclick="AuthUrl()"/>
                                    </div>
                                </div>
                                <br>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>
        <div id="footer-logo"><a href="http://www.atlassian.com/" rel="nofollow">Atlassian</a></div>
    </body>
    </html>