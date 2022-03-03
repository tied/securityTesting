<html>
    <head>
        <title>OAuth Configuration</title>
        <meta name="decorator" content="atl.general"/>
        <link rel="stylesheet">

        <#assign islowerBuild = lowerBuild>
        <script>
            AJS.$(document).ready(function() {
                 AJS.$(".aui-nav li").removeClass("aui-nav-selected");
                 AJS.$("#signinsettings").addClass("aui-nav-selected");

                 if(AJS.Cookie.read("show-sign-in-settings")=="true"){
                    document.getElementById('sign-in-settings-div').style.display = 'block';
                    document.getElementById('sign-in-settings').innerHTML = "1. Hide Sign In Settings";
                 }
                 else{
                    document.getElementById('sign-in-settings-div').style.display = 'none';
                    document.getElementById('sign-in-settings').innerHTML = "1. Show Sign In Settings";
                 }

                 if(AJS.Cookie.read("show-custom-login-template") == "true") {
                    document.getElementById('custom-login-template-div').style.display = 'block';
                    document.getElementById('custom-login-template').innerHTML = "2. Hide Custom Login template";
                } else {
                    document.getElementById('custom-login-template-div').style.display = 'none';
                    document.getElementById('custom-login-template').innerHTML = "2. Show Custom Login template";
                }

                 if(AJS.Cookie.read("show-sign-out-settings")=="true"){
                    document.getElementById('sign-out-settings-div').style.display = 'block';
                    document.getElementById('sign-out-settings').innerHTML = "3. Hide Sign Out Settings";
                 }
                 else{
                    document.getElementById('sign-out-settings-div').style.display = 'none';
                    document.getElementById('sign-out-settings').innerHTML = "3. Show Sign Out Settings";
                 }

                 if(AJS.Cookie.read("show-sso-error-settings")=="true"){
                    document.getElementById('sso-error-settings-div').style.display = 'block';
                    document.getElementById('sso-error-settings').innerHTML = "4. Hide SSO Error Settings";
                 }
                 else{
                    document.getElementById('sso-error-settings-div').style.display = 'none';
                    document.getElementById('sso-error-settings').innerHTML = "4. Show SSO Error Settings";
                 }

                 if(AJS.Cookie.read("show-advanced-sso-settings")=="true"){
                    document.getElementById('advanced-sso-settings-div').style.display = 'block';
                    document.getElementById('advanced-sso-settings').innerHTML = "5. Hide Advanced SSO Settings";
                 }
                 else{
                    document.getElementById('advanced-sso-settings-div').style.display = 'none';
                    document.getElementById('advanced-sso-settings').innerHTML = "5. Show Advanced SSO Settings";
                 }
                
                AJS.$("#enableBackdoor").change(function () {
                showRestrictBackdoorSwitch();
                });
                function showRestrictBackdoorSwitch(){
                    if(AJS.$("#enableBackdoor").is(":checked")){
                    AJS.$("#enable-backdoor-using-api").hide();
                    AJS.$("#restrict-backdoor-div").show("slow");
                    if(AJS.$("#restrictBackdoor").is(":checked")){
                    AJS.$("#backdoorAccessGroupsList").show("slow");
                    }
                } else{
                    AJS.$("#enable-backdoor-using-api").show("slow");
                    AJS.$("#restrict-backdoor-div").prop("checked", false);
                    AJS.$("#restrictBackdoor").prop("checked", false);
                    AJS.$("#restrict-backdoor-div").hide("");
                    AJS.$("#backdoorAccessGroupsList").hide("");
                }
            }
                var restrictBackdoor = document.getElementById("restrictBackdoor");
                    restrictBackdoor = restrictBackdoor.checked ? true : false;
                    if(restrictBackdoor) {
                        document.getElementById('backdoorAccessGroupsList').style.display = "block";
                    } else {
                        document.getElementById('backdoorAccessGroupsList').style.display = "none";
                    }

                var lowerBuild = ""+${islowerBuild};
                console.log("isLowerBuild :"+lowerBuild);
                if (lowerBuild === "false") {
                    AJS.$("#backdoorGroups").auiSelect2({
                        placeholder: 'Select Bamboo Groups',
                        ajax: {
                            url: AJS.contextPath() + '/plugins/servlet/oauth/moapi',
                            data: function (params) {
                                var query = {
                                    search: params,
                                    action: 'fetchGroups'
                                };

                                 // Query parameters will be ?search=[term]&type=public
                                 return query;
                            },
                             results: function (data, page) {
                                return {
                                    results: data.results
                                };
                             },

                        },
                        multiple: true
                });

                var backdoorGroups = [];

                <#foreach group in backdoorGroupsList>
                    var groupName = "${group}";
                    backdoorGroups.push({
                        "id": groupName,
                        "text": groupName
                    });
                </#foreach>

                AJS.$("#backdoorGroups").auiSelect2('data', backdoorGroups);
                } else {
                    AJS.$("#backdoorGroups").auiSelect2();
                }

            });

            window.onload = function() {
				defaultLoginChanges();
				customLogoutTemplateDisplay();
			};

			 function showSignInOption() {
                var x = document.getElementById('sign-in-settings-div');
                if (x.style.display === 'none') {
                    x.style.display = 'block';
                    document.getElementById('sign-in-settings').innerHTML = "1. Hide Sign In Settings";
                    AJS.Cookie.save("show-sign-in-settings", "true");
                } else {
                    x.style.display = 'none';
                    document.getElementById('sign-in-settings').innerHTML = "1. Show Sign In Settings";
                    AJS.Cookie.save("show-sign-in-settings", "false");
                }
             }

             function showLoginTemplateSettings() {
                var x = document.getElementById('custom-login-template-div');
              if (x.style.display === 'none') {
                x.style.display = 'block';
                document.getElementById('custom-login-template').innerHTML = "2. Hide Custom Login template";
                AJS.Cookie.save('show-custom-login-template','true');
             } else {
                x.style.display = 'none';
                document.getElementById('custom-login-template').innerHTML = "2. Show Custom Login template";
                AJS.Cookie.save('show-custom-login-template','false');
                }
            }

            function showHideLoginTemplate() {
                var loginTemplateChecked = document.getElementById("enableLoginTemplate").checked ? true : false;
                var x = document.getElementById('custom_login_template');
                if (loginTemplateChecked) {
                    x.style.display = 'block';
                } else {
                    x.style.display = 'none';
                }
            }

             function showSignOutOption() {
                var x = document.getElementById('sign-out-settings-div');
                if (x.style.display === 'none') {
                    x.style.display = 'block';
                    document.getElementById('sign-out-settings').innerHTML = "3. Hide Sign Out Settings";
                    AJS.Cookie.save("show-sign-out-settings", "true");
                } else {
                    x.style.display = 'none';
                    document.getElementById('sign-out-settings').innerHTML = "3. Show Sign Out Settings";
                    AJS.Cookie.save("show-sign-out-settings", "false");
                }
             }

             function showSSOErrorOption(){
                var x = document.getElementById('sso-error-settings-div');
                if (x.style.display === 'none') {
                    x.style.display = 'block';
                    document.getElementById('sso-error-settings').innerHTML = "4. Hide SSO Error Settings";
                    AJS.Cookie.save("show-sso-error-settings", "true");
                } else {
                    x.style.display = 'none';
                    document.getElementById('sso-error-settings').innerHTML = "4. Show SSO Error Settings";
                    AJS.Cookie.save("show-sso-error-settings", "false");
                }
             }


             function showAdvSignInOption() {
                 var x = document.getElementById('adv-signin-settings-div');
                 if (x.style.display === 'none') {
                     x.style.display = 'block';
                     document.getElementById('adv-signin-settings').innerHTML = "4. Hide Advance Sign In Settings";
                     AJS.Cookie.save("show-adv-sign-in-settings", "true");
                 } else {
                     x.style.display = 'none';
                     document.getElementById('adv-signin-settings').innerHTML = "4. Show Advance Sign In Settings";
                     AJS.Cookie.save("show-adv-sign-in-settings", "false");
                 }
              }
             function showAdvancedSSOOption(){
                var x = document.getElementById('advanced-sso-settings-div');
                if (x.style.display === 'none') {
                    x.style.display = 'block';
                    document.getElementById('advanced-sso-settings').innerHTML = "5. Hide Advanced SSO Settings";
                    AJS.Cookie.save("show-advanced-sso-settings", "true");
                } else {
                    x.style.display = 'none';
                    document.getElementById('advanced-sso-settings').innerHTML = "5. Show Advanced SSO Settings";
                    AJS.Cookie.save("show-advanced-sso-settings", "false");
                }
             }

			function defaultLoginChanges(){
            	if (document.getElementById("disableDefaultLogin").checked){
            		AJS.$("#advance-auto-redirect-to-idp-div").show("slow");
                    if(document.getElementById("enableBackdoor").checked){
                        AJS.$('#enable-backdoor-using-api').hide("slow");
                    }
                    else{
                        AJS.$('#enable-backdoor-using-api').show("slow");
                    }
             	} else {
             		AJS.$("#advance-auto-redirect-to-idp-div").hide("slow");
             		document.getElementById("enableBackdoor").checked = false;
                    document.getElementById("enableAutoRedirectDelay").checked= false;
                    document.getElementById("custom-delay-div").style.display= "none";
                }
			}

            
            AJS.$("#disableDefaultLogin").change(function () {
                defaultLoginChanges();
                });

			function customLogoutTemplateDisplay(){
			    if (document.getElementById("enableLogoutTemplate").checked){
                    AJS.$("#custom_logout_template").show("slow");
                }
                else {
                    AJS.$("#custom_logout_template").hide("slow");
                }
			}

            function copy(that, message) {
	            var inp = document.createElement('input');
	            document.body.appendChild(inp)
	            inp.value = that.textContent
	            inp.select();
	            document.execCommand('copy', false);
	            inp.remove();
	            AJS.$(message).show();
	            setTimeout(function () {
	                AJS.$(message).hide("slow");
	            }, 5000);
	        }
	        function showCustomDelayDiv(){
        		var isAutoredirectDelay = document.getElementById("enableAutoRedirectDelay");
        		if(isAutoredirectDelay.checked){
        		document.getElementById("custom-delay-div").style.display = "inline-block";
        		}else{
        			document.getElementById("custom-delay-div").style.display = "none";
        		}
        	}

               
		function showBackdoorUrls(){

            var x = AJS.$("#backdoorUrlDiv");
            if (x.css('display') == 'none') {
                x.show();
                AJS.$("#show-url-details").text("Hide Backdoor URLs");
                setCookie('show-url-details', true);
            } else {
                x.hide();
                AJS.$("#show-url-details").text("Show Backdoor URLs");
                setCookie('show-url-details', false);
            }
        }



		</script>
		<style>
	        .aui-page-panel-content{
	            padding: 0px !important;
	        }

	        .aui-page-panel{
	            margin: 0px !important;
	        }

	        .show-title:after {
	             content:attr(title);
	             font-weight: bold;
	             color: white;
	             padding: 0px 10px;
	             border-radius: 2px;
	             border:1px solid #2D4D4D;
	             background: #2D4D4D;
	         }
	    </style>
	</head>
	<body>
    <#include "*/header.ftl" parse=true>
    <div class="tabs-pane active-pane" id="oauth-signin-settings"  role="tabpanel" >
        <p style="font-size:16pt;">Sign In Settings
        <span style="float:right;margin-right:25px;">
                        <input type="submit" form="signin-settings-form" value="Save" class="aui-button aui-button-primary"
                               style="width:100px;">
                    </span>
        </p>
        <hr class="header"/><br>

        <form id="signin-settings-form" class="aui long-label" action="" method="POST">
            <input type="hidden" name="signinSettingsSubmitted" value="true"/>
            <input id="atl_token" type="hidden" name="atl_token" value="${xsrfToken}" />

            <p>
                <a id="sign-in-settings" style="font-size:13pt;cursor:pointer"
                    class="sign-in-settings"
                    aria-controls="sign-in-settings-div"
                    onclick="showSignInOption()"
                >
                    1. Show Sign In Settings
                </a>
            </p>
            <hr class="header"/>

            <div class="sign-in-settings-div" id="sign-in-settings-div">
                <div class="field-group">
                    <label for="loginButtonText">Login Button Text:
                       <span class="aui-icon icon-required">(required)</span>
                    </label>
                    <input type="text" required="true" id="loginButtonText" name="loginButtonText"
                          value="${loginButtonText}" class="text long-field" />
                    <div class="description">Set button label for OAuth SSO button shown on login page.</div>
                    <br>
                </div>

                <div class="field-group">
                    <label for="showSSOLoginButton">SSO button position:</label>
                    <#if ssoButtonLocation == "Before Login Button">
                    <input class="radio" type="radio" name="ssoButtonLocation" id="beforeLoginButton"
                           value="Before Login Button" checked="checked" />
                    <#else>
                    <input class="radio" type="radio" name="ssoButtonLocation" id="beforeLoginButton"
                           value="Before Login Button" />
                    </#if>
                    <span>Before Login Button</span>

                    <#if ssoButtonLocation == "After Login Button">
                    <input class="radio" type="radio" name="ssoButtonLocation" id="afterLoginButton"
                           value="After Login Button" checked="checked" />
                    <#else>
                    <input class="radio" type="radio" name="ssoButtonLocation" id="afterLoginButton"
                           value="After Login Button" />
                    </#if>
                    <span>After Login Button</span>
                </div>

                <div class="field-group">
                    <label for="relayState">Relay State:</label>
                    <input type="url" id="relayState" name="relayState"
                          value="${relayState}" class="text long-field" />
                    <div class="description">Enter the URL which you want to directly redirect after login. The URL must be absolute URL. Keep empty to redirect user to the same URL they started with.</div>
                    <br>
                </div>

                <div class="field-group">
                    <label for="disableDefaultLogin" class="labelClass">Auto Redirect to Application:</label>
                    <input class="checkbox" data-size="small" data-on-text="Yes" data-off-text="No" type="checkbox"
                            name="disableDefaultLogin" value="true"  id="disableDefaultLogin"
                    <#if disableDefaultLogin == true>
                        checked="true"
                    </#if>
                    />
                    <div class="description">This option redirects users to Application on access of Bamboo login page.</div>
                </div>

                <div id="advance-auto-redirect-to-idp-div" style="display: none;">
                    <div class="field-group">
                    <input class="checkbox" type="checkbox" name="enableBackdoor" value="true" id="enableBackdoor"
                        <#if enableBackdoor == true>
                            checked="true"
                        </#if>
                        />
                        <table>
                        <tr>
                        <td>
                        <span for="enableBackdoor">Enable backdoor login:</span>
                        </td>
                        <td>
                                   <td>

                        				  <a id="show-url-details" style="font-size:10pt;cursor:pointer" onclick="showBackdoorUrls()">
                                                        	  Hide Backdoor URLs
                                                         </a>

                        	                        </td>
                        	                    </tr>
                        	                </table>


                       <div id="backdoorUrlDiv" style="display: block;">
                            <br><p><b>Backdoor/Emergency URL:</b></p><br>
                            <input type="hidden" id="backdoor_submitted" name="backdoorSubmitted" value="false">
                            <div style="display: block;">
                            <span id="saved_backdoor_div" title="click to copy" style="cursor:default">
                                <p id="saved_Backdoor_Div" onmousedown="copy(this, '#c2')">${settings.getLoginPageUrl()}?${backdoorKey}=${backdoorValue}</p>
                                <a id="edit-backdoor-button"
                                   class="aui-button aui-button-subtle aui-icon aui-icon-small aui-iconfont-edit" title="edit backdoor"></a>
                            </span>
                                <span id="edit_backdoor_div" style="cursor:default;display: none">
                                <p>${settings.getLoginPageUrl()}?<input type="text" class="text"
                                                                        style="width: 20%" name="backdoorKey"
                                                                        id="backdoor_key" value="${backdoorKey}"/> = <input
                                            type="text" class="text" style="width: 20%" name="backdoorValue"
                                            id="backdoor_value" value="${backdoorValue}"/> </p>
                                <a id="save-backdoor-button"
                                   class="aui-button aui-button-subtle aui-icon aui-icon-small aui-iconfont-success" title="save"></a> <a
                                            id="cancel-backdoor-button"
                                            class="aui-button aui-button-subtle aui-icon aui-icon-small aui-iconfont-close-dialog" title="cancel"></a>
                            </span>
                            </div>
                            <span id="c2" title="Copied" class="show-title" hidden></span>
                        </div>

                        <div id="enable-backdoor-using-api" style="width: 90%;">
                        <div class="aui-message aui-message-info">
                                <span>
                                    Backdoor/Emergency URL allows users to access Bamboo with their local credentials and will be useful when provider is not available.
                                    In case you get locked out &amp; you want to enable the Backdoor Login, use this REST API.<br><br>
                                    <code>
                                        <strong>Post</strong> : ${settings.getBaseUrl()}/plugins/servlet/oauth/enablebackdoorurl<br>
                                        <strong>Header Parameters</strong> :
                                        <ul>
                                            <li>
                                                Content-Type - application/json
                                            </li>
                                            <li>
                                                Authorization - Basic
                                            </base64></li>
                                        </ul>
                                        </code>
                                </span>
                        </div>
                    </div>
	            </div>


                        <div id="restrict-backdoor-div" class="field-group">
                        <input type="checkbox" id="restrictBackdoor" value="true"
                            name="restrictBackdoor"  <#if restrictBackdoor == true> checked="true" </#if> > Restrict Backdoor
                        URL Access
                        <div class="description">Check this box to restrict backdoor URL access to certain groups.</div>
                        </div>

                        <div id="backdoorAccessGroupsList" style="display:none;">
                        <div class="field-group">
                            <label for="backdoorGroups">Groups</label>
                            <#if lowerBuild == "true">
                                <select class="select" name="backdoorGroups" id="backdoorGroups" multiple size="3" style="max-width: 500px !important;width: 500px;">
                                    <#foreach group in existingGroups>
                                        <option value="${group}"
                                            <#foreach selectedGroup in disableDefaultLogin>
                                                <#if selectedGroup.equals(group)>
                                                    selected
                                                </#if>
                                            </#foreach>
                                        >${group}
                                        </option>
                                    </#foreach>
                                </select>
                            <#else>
                                <input class="select long-field" name="backdoorGroups" id="backdoorGroups"
                                style="width: 500px"></input>
                            </#if>
                            <div class="description">Select Groups to allow backdoor url access to.</div>
                        </div>
                    </div>

                    <div class="field-group" >
                    	<table style="word-wrap: break-word">
							<tr>
								<td> 
                            		<input class="checkbox" type="checkbox" name="enableAutoRedirectDelay"
                                	value="true" id="enableAutoRedirectDelay" onclick="showCustomDelayDiv()" <#if enableAutoRedirectDelay == true> checked="true" </#if> />
                        			<span>
                            		Enable AutoRedirect Delay
                        			</span>
                    			</td>
								<td width="20%"></td>
								<td>
									<div id="custom-delay-div" <#if enableAutoRedirectDelay == false> style="display: none" </#if>
								<span>Delay Time: </span>
								<input class="text short-field" type="number" min="0" name="autoRedirectDelayInterval" placeholder="5 Sec" value="${autoRedirectDelayInterval}" id="autoRedirectDelayInterval" />
									</div>
							</td>
						</tr>
					</table>
                        <div class="description">Introduce a delay on the login page before user is redirected to the Application for
                            authentication. This lets user go to login page if they don't want to get redirected. Recommended for testing Auto-redirect.
                        </div>
                        <br>
                    </div>
                </div>
                <div class="field-group">
                    <label for="allowDomains">Allowed Domains:</label>
                    <input type="text" id="allowedDomains" name="allowedDomains"
                          value="${allowedDomains}" class="text long-field" placeholder="domain1.com;domain2.com;domain3.com" />
                    <div class="description">Enter the <b>semicolon (;)</b> separated domain names which should be allowed for logging in. Keep empty to allow all the domains. </br> <b>Note: </b>Your OAuth Provider must send email in response.</div>
                    <br>
                </div>
            </div>
            <p>
                <a id="custom-login-template"
                class="custom-login-template"
                aria-controls="custom-login-template-div"
                onclick="showLoginTemplateSettings()"
                style="font-size:13pt;cursor:pointer">2. Show Custom Login template
                </a>
            </p>
            <hr class="header"/>
            <div class="custom-login-template-div" id="custom-login-template-div">
                <div class="field-group">
                    <label for="enableLoginTemplate" class="labelClass">Custom Login Template:</label>
                    <input class="checkbox" type="checkbox" name="enableLoginTemplate" value="true"
                           id="enableLoginTemplate" onclick="showHideLoginTemplate()"
                    <#if enableLoginTemplate == true>
                        checked="true"
                    </#if>
                    />
                </div>

                <div id="custom_login_template" class="field-group"
                <#if enableLoginTemplate == true>
                    style="display:block"
                <#else>
                    style="display:none"
                </#if>
                />
                    <textarea id="loginTemplate" name="loginTemplate" class="textarea long-field"
                        style="font-family:Courier New;" cols="64" rows="4">${loginTemplate?html}</textarea>
                        <div class="description">
                        This is default template for Single OAuth setup but you can customize it according to your need or you
                        can design your own SSO login template.
                        </div>
                        <br/>
                        <div>
                        <p>Use the below code to add new SSO button in login template</p>
                        <div style="border: 1px solid grey;border-radius: 5px;padding: 7px; background-color: lightgray;max-width: 850px;">
                            <i>&lt;div class="field-group"&gt; &lt;input type="button" class="aui-button aui-button-primary"
                                value="Single Sign-On" onclick="AuthUrl()"&gt;
                                &lt;/div&gt;
                            </i>
                        </div>
                        <div class="description">To get the CLient ID, edit the OAuth configuration from Configure OAuth tab. It is options in case if you have only one OAuth configured.
                        </div>

                        <br><p>Default login page URL</p>
                        <div>
                            <div style="border: 1px solid grey;border-radius: 5px;padding: 7px; background-color: lightgray;max-width: 500px;">
                                <span title="click to copy" style="cursor:default">
                                    <p onmousedown="copy(this, '#p1')"><em>${settings.getLoginPageUrl()}?show_login_form=true</em></p>
                                </span>
                            </div>
                            <span id="p1" title="Copied" class="show-title" hidden=""></span>
                            <div class="description">
                                Use this option to access Bamboo default login page when custom login template is turned on.
                            </div>
                        </div>
                    </div>
                </div>
            </div>    

            <p>
                <a id="sign-out-settings" style="font-size:13pt;cursor:pointer"
                    class="sign-out-settings"
                    aria-controls="sign-out-settings-div"
                    onclick="showSignOutOption()"
                >
                    3. Show Sign Out Settings
                </a>
            </p>
            <hr class="header"/>

            <div id="sign-out-settings-div">
                <div class="aui-message aui-message-warning">
                    <span> Warning: Custom Logout URL/Template will not work if Single Logout URL is enabled for respective application</span>
                </div>

                <div class="field-group">
                    <label for="customLogoutURL">Custom Logout URL:</label>
                    <div>
                        <input type="url"  id="customLogoutURL" name="customLogoutURL" value="${customLogoutURL}"
                        placeholder="Custom Logout URL" class="text long-field"
                        <#if enableLogoutTemplate == true>
                            disabled
                        </#if>
                        />

                        <div class="description">
                            Redirect to this URL after logging out (e.g. your application logout page). Leave empty to redirect on default Bamboo login page.
                        </div>
                    </div>
                    <p style="font-size:13pt;">&nbsp;&nbsp;&nbsp;OR</p>
                    <br>
                    <div>
                        <input class="checkbox" type="checkbox"  name="enableLogoutTemplate" value="true" id="enableLogoutTemplate"
                        <#if enableLogoutTemplate == true>
                            checked="true"
                        </#if>
                        />
                        <span>Use Custom Logout Template</span>
                    </div>
                </div>

                <div id="custom_logout_template" class="group" style="display: none;">
                    <legend> <span>Custom Logout Template:</span></legend>
                    <textarea id="logoutTemplate"  name="logoutTemplate" class="textarea long-field"
                    style="font-family:Courier New;" cols="64" rows="4">
                    ${logoutTemplate?html}
                    </textarea>
                    <div class="description">
                      Define the Custom logout page. Enable Custom Logout template and use $baseUrl for login page.
                    </div>
                </div>
            </div>

            <p>
                <a id="sso-error-settings" style="font-size:13pt;cursor:pointer"
                    class="sso-error-settings"
                    aria-controls="ssso-error-settings-div"
                    onclick="showSSOErrorOption()"
                >
                    4. Show SSO Error Settings
                </a>
            </p>
            <hr class="header"/>
            <div id="sso-error-settings-div" style="display: block;">
                <br>
                <div id="errorTemplate" class="field-group">
                    <legend><span>SSO Error Message Template:</span></legend>
                        <input class="checkbox" type="checkbox" name="enableErrorMsgTemplate" value="true"
                            id="enableErrorMsgTemplate"
                        <#if enableErrorMsgTemplate == true>
                            checked="true"
                        </#if>
                        />
                        <label for="enableErrorMsgTemplate">SSO Error Message Template:</label>
                        <br>
                        <textarea id="errorMsgTemplate" name="errorMsgTemplate"
                            class="textarea long-field"
                            style="font-family:Courier New; display: block" cols="64" rows="4">
                            ${errorMsgTemplate?html}
                        </textarea>
                        <div class="description" >
                            Define the Custom SSO error message templates. This template will be shown to the user when SSO
                            fails. <br>Use <b>${baseUrl}</b> for login page URL.
                        </div>
                </div>
            </div>

            <p>
                <a id="advanced-sso-settings" style="font-size:13pt;cursor:pointer"
                    class="advanced-sso-settings"
                    aria-controls="advanced-sso-settings-div"
                    onclick="showAdvancedSSOOption()"
                >
                    5. Show Advanced SSO Settings
                </a>
            </p>
            <hr class="header"/>

            <div class="advanced-sso-settings-div" id="advanced-sso-settings-div">
				<div class="field-group">
                    <span style="vertical-align:middle;">
                        <label class="switch" style="vertical-align:middle;">
                            <input type="checkbox" id="pluginApiAccessRestriction" value="true"
                                name="pluginApiAccessRestriction"
                                <#if pluginApiAccessRestriction == true>
                                    checked
                                </#if>
                            >
                                <span class="slider round"></span>
                        </label>
                        <span style="height:16px; width: 16px">
                            <strong>Restrict access to plugin APIs</strong>
                        </span>
                    </span>
                    <div class="description">When enabled, the plugin's APIs will not be accessible form outside this Bamboo instance.</div>
                    <div id="warningforapirestriction" class="aui-message aui-message-warning closeable" style="display:none;">
                    Make sure to check your Referrer Policy for the appropriate setting if Bamboo runs behind a reverse proxy.
                    </div>
                </div>

                <div class="field-group">
                    <label for="sessionTime"> Remember Me-Cookie:</label>
                        <div>
                                <input class="checkbox" type="checkbox" name="enableRememberMeCookie"
                                               id="enableRememberMeCookie"
                                              <#if enableRememberMeCookie> checked="true" </#if> value="true" id="enableRememberMeCookie" />
                            <div class="description">Set the RememberMe-Cookie after authentication. If enabled, user stays logged in until user explicitly logs out.</div>
                        </div>
                    </div>
			    </div>

            <div class="field-group">
                <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>
            </div>
        </form>
    </div>
    </div>
    </section>
</body>
</html>