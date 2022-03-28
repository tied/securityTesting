<html>
<head>
    <title>Sign In Settings</title>
    <#assign count = settings.getNoSSOUrls().size()>
    <meta name="decorator" content="atl.general">
    <script>
            <#include "/js/signinsettings.js">

            AJS.$(function () {
                var cnt = ${count};
                document.getElementById("idTotalNumberOfRoles").value = cnt;
            });

            var counter = ${count};

            function GetDynamicTextBox(value) {
                    return '<br/>' + '<input id = "userAttrValue[' + counter + ']" name = "userAttrValue[' + counter + ']"  type="text" class="text" placeholder="Enter the URL pattern" style="width: 620px value = "' + value + '" />' + '&nbsp&nbsp;' +
                            '<input type="button" value="Remove" class="aui-button aui-button-primary"  onclick = "RemoveTextBox(this,' + counter + ')" />'
            }

            function RemoveTextBox(div, loopCount) {
                document.getElementById("urlInnerContainer").removeChild(div.parentNode);
            }

            function AddTextBox() {
                var div = document.createElement('DIV');
                div.innerHTML = GetDynamicTextBox("");
                var urlInnerContainer = document.getElementById("urlInnerContainer");
                urlInnerContainer.insertBefore(div, urlInnerContainer.childNodes[0]);
                counter++;

                document.getElementById("idTotalNumberOfRoles").value = parseInt(document.getElementById("idTotalNumberOfRoles").value) + 1;
            }
    </script>
    <style>
            <#include "/css/addidp.css">
            <#include "/css/redirectrules.css">
    </style>
</head>
<body>

    <#include "*/saml/headers/samlheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="sign-in-settings" role="tabpanel">
    <h1>Sign-In Settings</h1>
    <hr class="header"/>
 ${webResourceManager.requireResource('com.atlassian.auiplugin:aui-toggle')}
    <div id="toggles-div" class="tabcontent">
        <form class="aui long-label" id="save-redirection-toggles-form" action="" method="post">
            <input type="hidden" id="advanced_settings_submitted" name="advancedSettingsSubmitted" value="true"/>
            <input type="hidden" name="atl_token" value="${xsrfToken}" />
            <input type="hidden" name="totalNumberOfRoles" id="idTotalNumberOfRoles"/>



            <#if ssoEnabledIdpList.size()==1>
                <h2 class="subsection">
                    Force SSO
                </h2>
                <hr class="header subsection"/>

                <div id="auto-redirect-for-single-idp" class="field-group">
                    <label for="enableAutoRedirect">Auto-Redirect to IDP</label>
                    <aui-toggle id="enableAutoRedirect" name="enableAutoRedirect" <#if enableAutoRedirect==true>
                                checked </#if> label="Auto Redirect to IDP" value="true"></aui-toggle>
                    <div class="description">Redirect Users to IDP without displaying the login page.</div>
                    <br>
                </div>
                <div id="auto-redirect-delay" class="field-group" <#if enableAutoRedirect == false> style="display:none;" </#if> >
                    <label for="enableAutoRedirectDelay">Delay Auto-Redirect to IDP</label>
                    <aui-toggle id="enableAutoRedirectDelay"
                            name="enableAutoRedirectDelay" <#if enableAutoRedirectDelay==true>
                            checked </#if> label="Delay Auto Redirect to IDP" value="true"></aui-toggle>
                    <div class="description">Introduce a delay on the login page before user is redirected to the IDP
                      for
                      authentication. This lets user
                     go to login page if they don't want to get redirected. Recommended for testing Auto-redirect.
                    </div>
                    <br>
                </div>
            </#if>

            <h2 class="subsection">
                No / Skip SSO
            </h2>
            <hr class="header subsection"/>

            <div class="aui-message aui-message-info">
            <p>Enter the pattern for the URL. SSO will be disabled/bypassed for the configured Url patterns.For example, you want to dissable SSO for <strong> ${settings.getSpBaseUrl()}/servlet/applinks </strong>, just enter <b>/applinks</b> in the url pattern text field.</p>
                </div>

                 <div class="field-group" id="NoSsoUrlContainer" name="NoSsoUrlContainer">

                    <div id="urlInnerContainer" name="urlInnerContainer">
                        <#assign loopCount = 0>
                        <#foreach url in noSsoUrls>
                            <div>
                                <br/>
                                <input type="text" id="userAttrValue[${loopCount}]" name="userAttrValue[${loopCount}]"
                                       value="${url}" class="text"/>&nbsp&nbsp;
                                <input type="button" value="Remove" class="aui-button aui-button-primary"
                                       onclick="RemoveTextBox(this, ${loopCount})"/>
                                <#assign loopCount = loopCount + 1>
                            </div>
                        </#foreach>
                    </div>
                    <br>
                <input type='button' value='+ Add Another' id='addUrl' class="aui-button aui-button-primary"
                       onclick="AddTextBox()">
                <br>
            </div>



            <h2 class="subsection">
                Backdoor / Emergency Login
            </h2>
            <hr class="header subsection"/>
            <div id="backdoor-url" class="field-group">

                <label for="enableBackdoor">Enable Backdoor Login</label>
                <aui-toggle id="enableBackdoor" name="enableBackdoor" <#if enableBackdoor==true>
                                                checked="true" </#if> label="Restrict Backdoor URL Access" value="true"></aui-toggle>&nbsp;
                &emsp;
                <a id="show-url-details"
                   data-replace-text="Show Backdoor URL"
                   class="show-url-details"
                   aria-controls="backdoorUrlDiv"
                   style="cursor:pointer">Hide Backdoor URL
                </a>
                <br/>


                <div id="backdoorUrlDiv" class="subfield" style="display: block;">
                    <input type="hidden" id="backdoor_submitted" name="backdoorSubmitted" value="false">
                    <table class="aui aui-list-table" style="word-wrap: break-word; width:100%">
                        <tr>
                            <td width="11%">For Bamboo Software , use</td>
                            <td>
		                            <span id="saved_backdoor_div" title="click to copy" style="cursor:default">
                                        <code id="bamboo-backdoor-url" style="vertical-align:top"
                                              class="copyable">${settings.getLoginPageUrl()}?<span
                                                id="backdoor-key">${backdoorKey}</span>=<span
                                                id='backdoor-value'>${backdoorValue}</span></code>
										&emsp;<a id="edit-backdoor-button"
                                                 class="aui-button aui-button-subtle aui-icon aui-icon-small aui-iconfont-edit"
                                                 title="edit backdoor"></a>
		                            </span>
                                <span id="bamboo-backdoor-url-copied-message" style="vertical-align:top"
                                      title="Copied" class="show-title" hidden></span>
                                <span id="edit_backdoor_div" style="cursor:default;display: none">
										<p>${settings.getLoginPageUrl()}?<input type="text" class="text"
                                                                              style="width: 20%" name="backdoorKey"
                                                                              id="backdoor_key" value="${backdoorKey}"/> = <input
                                                type="text" class="text" style="width: 20%" name="backdoorValue"
                                                id="backdoor_value" value="${backdoorValue}"/> </p>
										<a id="save-backdoor-button"
                                           class="aui-button aui-button-subtle aui-icon aui-icon-small aui-iconfont-success"
                                           title="save"></a> <a
                                        id="cancel-backdoor-button"
                                        class="aui-button aui-button-subtle aui-icon aui-icon-small aui-iconfont-close-dialog"
                                        title="cancel"></a>
		                            </span>

                            </td>
                        </tr>

                    </table>
                </div>


                <div id="backdoor-url-warning" class="subfield">
                    <br/>
                    <div class="aui-message aui-message-info">
                    <span>
                        Note: Backdoor login is used by Administrators to log into their local Bamboo account. After disabling backdoor, you won't be able to log in with Bamboo's credentials.
                        In case you get locked out & you want to enable the Backdoor Login, use this API request<br><br>
                        <code>
                            <strong>Post</strong> : ${settings.getBaseUrl()}/plugins/servlet/enablebackdoorurl<br>
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
                <br/>
            </div>

            <div id="Login-attempts-div" class="subfield">
                <div class="field-group">
                    <label for="numberOfLoginAttempts">Backdoor Login Attempts</label>
                    <input class="text short-field" type="number" name="numberOfLoginAttempts"
                           id="numberOfLoginAttempts" min="1" value="${numberOfLoginAttempts}"/>
                    <div class="description">Provide number of login attempts to be allowed.</div>
                </div>
                <br/>

            </div>

            <br>
            <div id="restrict-backdoor-suboptions" class="subfield">
                <div id="restrict-backdoor-div" class="field-group">

                    <label for="backdoorGroups">Restrict Backdoor URL Access</label>
                    <aui-toggle id="restrictBackdoor" name="restrictBackdoor" <#if restrictBackdoor==true>
                                checked </#if> label="Restrict Backdoor URL Access" value="true"></aui-toggle>&nbsp;
                    <div class="description">Enable this option to allow backdoor URL access to selected groups only.</div>
                    <br>
                </div>
                <div id="backdoorAccessGroupsList" style="display:none;">

                    <div class="field-group">
                        <label for="backdoorGroups">Groups</label>
                        <input class="select long-field" name="backdoorGroups" id="backdoorGroups"/>
                        <div class="description">Select Groups to allow backdoor url access to.</div>
                    </div>
                </div>
            </div>




            <div class="buttons-container">
                <div class="buttons">
                    <input type="submit" class="aui-button aui-button-primary" value="Save"/>
                </div>
            </div>
        </form>
    </div>
</div>
<#include "*/footer.ftl" parse=true>
</body>
</html>