<html>
   <head>
      <meta name="decorator" content="atl.general"/>
      <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
      <#assign islowerBuild = lowerBuild>
      <script>
         $(document).ready(function() {
            $(".aui-nav li").removeClass("aui-nav-selected");
            $("#SIGNINSETTING").addClass("aui-nav-selected");

            if (getCookie("show-advanced-sso-settings") == "false") {
                 document.getElementById('advanced-sso-settings-div').style.display = 'none';
                 document.getElementById('advanced-sso-settings').innerHTML = "5. Show Advanced SSO Settings";
            } else {
            	document.getElementById('advanced-sso-settings-div').style.display = 'block';
                document.getElementById('advanced-sso-settings').innerHTML = "5. Hide Advanced SSO Settings";
               
            }
            if(getCookie("sso-error-settings") == "false") {
                document.getElementById('sso-error-settings-div').style.display = 'none';
                document.getElementById('sso-error-settings').innerHTML = "4. Show SSO Error Settings";
            } else {
            	document.getElementById('sso-error-settings-div').style.display = 'block';
                document.getElementById('sso-error-settings').innerHTML = "4. Hide SSO Error Settings";
               
            }
		    if(getCookie("show-sign-out-settings") == "false") {
               	document.getElementById('sign-out-settings-div').style.display = 'none';
                document.getElementById('sign-out-settings').innerHTML = "3. Show Sign Out Settings";
            } else {
              	document.getElementById('sign-out-settings-div').style.display = 'block';
                document.getElementById('sign-out-settings').innerHTML = "3. Hide Sign Out Settings";
               
            }
            if(getCookie("show-custom-login-template") == "false") {
                document.getElementById('custom-login-template-div').style.display = 'none';
                document.getElementById('custom-login-template').innerHTML = "2. Show Custom Login template";
            } else {
                document.getElementById('custom-login-template-div').style.display = 'block';
                document.getElementById('custom-login-template').innerHTML = "2. Hide Custom Login template";
            }
            if(getCookie("show-sign-in-settings") == "false") {
                document.getElementById('sign-in-settings-div').style.display = 'none';
                document.getElementById('sign-in-settings').innerHTML = "1. Show Sign In Settings";
            } else {
             	document.getElementById('sign-in-settings-div').style.display = 'block';
                document.getElementById('sign-in-settings').innerHTML = "1. Hide Sign In Settings";
            }

			var logoutTemplate = document.getElementById("enablelogoutTemplate");
            logoutTemplate = logoutTemplate.checked ? false : true;
            if(!logoutTemplate) {
           	 document.getElementById('custom_logout_template').style.display = "block";
            } else {
           	 document.getElementById('custom_logout_template').style.display = "none";
            }

            var disableDefaultLogin = document.getElementById("disableDefaultLogin");
            disableDefaultLogin = disableDefaultLogin.checked ? true : false;
            if(disableDefaultLogin) {
           	     document.getElementById('advancedAutoRedirectDiv').style.display = "block";
            } else {
           	    document.getElementById('advancedAutoRedirectDiv').style.display = "none";
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
                        url: AJS.contextPath() + '/plugins/servlet/saml/moapi',
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
                    var groupName = '${group}';
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

		 function enableAutoRedirectToIdp() {
		    var disableDefaultLogin = document.getElementById("disableDefaultLogin");
            disableDefaultLogin = disableDefaultLogin.checked ? true : false;
            if(disableDefaultLogin) {
                 document.getElementById('advancedAutoRedirectDiv').style.display = "block";
            } else {
                document.getElementById('advancedAutoRedirectDiv').style.display = "none";
            }

            if (document.getElementById("disableDefaultLogin").checked){
               document.getElementById("enableBackdoor").checked = true;
               document.getElementById("Login-attempts-div").style.display = "block";
               document.getElementById('backdoor-url-warning').style.display = "none";
            } else {
                document.getElementById("enableBackdoor").checked = false;
                document.getElementById('backdoor-url-warning').style.display = "block";
            }
		 }

		 function checkBackdoorOption() {
		    var enableBackdoor = document.getElementById("enableBackdoor");
		    enableBackdoor = enableBackdoor.checked ? true : false;
            if(enableBackdoor) {
                document.getElementById('backdoor-url-warning').style.display = "none";
                document.getElementById('Login-attempts-div').style.display = "block";
            } else {
                document.getElementById('backdoor-url-warning').style.display = "block";
                document.getElementById('Login-attempts-div').style.display = "none";
            }
		 }

		  function enableCustomLogoutTemplate(){
                var logoutTemplate = document.getElementById("enablelogoutTemplate");
                logoutTemplate = logoutTemplate.checked ? false :true ;
                if(!logoutTemplate) {
                    document.getElementById('custom_logout_template').style.display = "block";
                } else {
                    document.getElementById('custom_logout_template').style.display = "none";
                }
          }

          function showBackdoorUrls() {
              var x = document.getElementById('backdoorUrlDiv');
              if (x.style.display === 'none') {
                  x.style.display = 'block';
                  document.getElementById('show-url-details').innerHTML = "Hide Backdoor URLs";

              } else {
                  x.style.display = 'none';
                  document.getElementById('show-url-details').innerHTML = "Show Backdoor URLs";
              }
          }

          function showEditBackdoorUrls() {
            document.getElementById('saved_backdoor_div').style.display = "none";
            document.getElementById('edit_backdoor_div').style.display = "block";
          }

          function saveBackdoorButtonClick() {
                document.getElementById('backdoor_submitted').value = true;
                document.getElementById('signin_settings_submitted').value = false;
                document.getElementById('signin-settings-form').submit();
          }

          function cancelBackdoorButtonClick() {
              document.getElementById('edit_backdoor_div').style.display = "none";
              document.getElementById('saved_backdoor_div').style.display = "block";
          }

            function copy(that, message){
                var inp =document.createElement('input');
                document.body.appendChild(inp)
                inp.value =that.textContent
                inp.select();
                document.execCommand('copy',false);
                inp.remove();
                $(message).show();
                setTimeout(function() { $(message).hide("slow"); }, 5000);
            }
			/*window.onload = function() {
			 defaultLoginChange();
			 };*/

         function showinfo() {
         	    var x = document.getElementById('extendtimeout');
         	    if (x.style.display === "none") {
         	      $('#extendtimeout').show("slow");
         	    } else {
         	    x.style.display = "none";
         	   }
         }
        function showAdvanceOption() {
            var x = document.getElementById('advanced-sso-settings-div');
            if (x.style.display === 'none') {
                x.style.display = 'block';
                document.getElementById('advanced-sso-settings').innerHTML = "5. Hide Advanced SSO Settings";
                setCookie('show-advanced-sso-settings',true);
            } else {
                x.style.display = 'none';
                document.getElementById('advanced-sso-settings').innerHTML = "5. Show Advanced SSO Settings";
                setCookie('show-advanced-sso-settings',false);
            }
        }

        function showSignInOption() {
            var x = document.getElementById('sign-in-settings-div');
            if (x.style.display === 'none') {
                x.style.display = 'block';
                document.getElementById('sign-in-settings').innerHTML = "1. Hide Sign In Settings";
                setCookie('show-sign-in-settings',true);
            } else {
                x.style.display = 'none';
                document.getElementById('sign-in-settings').innerHTML = "1. Show Sign In Settings";
                setCookie('show-sign-in-settings',false);
            }
        }

        function showLoginTemplateSettings() {
            var x = document.getElementById('custom-login-template-div');
            if (x.style.display === 'none') {
                x.style.display = 'block';
                document.getElementById('custom-login-template').innerHTML = "2. Hide Custom Login template";
                setCookie('show-custom-login-template',true);
            } else {
                x.style.display = 'none';
                document.getElementById('custom-login-template').innerHTML = "2. Show Custom Login template";
                setCookie('show-custom-login-template',false);
            }
        }

        function showSignOutsettings() {
            var x = document.getElementById('sign-out-settings-div');
            if (x.style.display === 'none') {
                x.style.display = 'block';
                document.getElementById('sign-out-settings').innerHTML = "3. Hide Sign Out Settings";
                setCookie('show-sign-out-settings',true);
            } else {
                x.style.display = 'none';
                document.getElementById('sign-out-settings').innerHTML = "3. Show Sign Out Settings";
                setCookie('show-sign-out-settings',false);
            }
        }

        function showSSOErrorsettings() {
            var x = document.getElementById('sso-error-settings-div');
            if (x.style.display === 'none') {
                x.style.display = 'block';
                document.getElementById('sso-error-settings').innerHTML = "4. Hide SSO Error Settings";
                setCookie('sso-error-settings',true);
            } else {
                x.style.display = 'none';
                document.getElementById('sso-error-settings').innerHTML = "4. Show SSO Error Settings";
                setCookie('sso-error-settings',false);
            }
        }

		      // Utility functions
        function setCookie(key, value) {
            var d = new Date();
            d.setTime(d.getTime() + (7 * 24 * 60 * 60 * 1000));
            var expires = "expires=" + d.toUTCString();
            document.cookie = key + "=" + value + ";" + expires + "path=/";
        }
		function enableDisableCustomLogoutTextBox(enablelogoutTemplate) {
            var customLogoutURL = document.getElementById("customLogoutURL");
            customLogoutURL.disabled = enablelogoutTemplate.checked ? true : false;
            enableCustomLogoutTemplate();
        }

        function getCookie(key) {
            //alert("key: " + key);
            var name = key + "=";
            //alert(document.cookie);
            var decodedCookie = decodeURIComponent(document.cookie);
            //alert(decodedCookie);
            var ca = decodedCookie.split(';');
            for (var i = 0; i < ca.length; i++) {
                var c = ca[i];
                while (c.charAt(0) == ' ') {
                    c = c.substring(1);
                }
                if (c.indexOf(name) == 0) {
                    //alert(" found " + c.substring(name.length, c.length));
                    return c.substring(name.length, c.length);
                }
            }
            //alert("not found");
            return "";
        }

        function showHideErrorMessageTemplate() {
            var errorMsgChecked = document.getElementById("enableErrorMsgTemplate").checked ? true : false;
            var x = document.getElementById('enableErrorMsgTemplateDiv');
            if (errorMsgChecked) {
                x.style.display = 'block';
            } else {
                x.style.display = 'none';
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
       
      </script>
      <style>
         .aui-page-panel-content{
         padding: 0px !important;
         }
         .aui-page-panel{
         margin: 0px !important;
         }
         .field-group{
         margin: 10px 100px 1px 100px !important;
         margin-left: 100px !important;
         }
         .show-title:after {
         content:attr(title);
         font-weight: bold;
         color: white;
         padding:5px 10px;
         margin-left: 20px;
         border-radius: 2px;
         border:1px solid #2D4D4D;
         background: #2D4D4D;
         }
         .labelClass {
            width: 275px !important;
            margin-left: -286px !important;
            margin-left: -250px;
            width: 235px;
            padding: 0 0 0 0;
         }
      </style>
   </head>
   <body>
   <#include "*/header.ftl" parse=true>
	<div class="tabs-pane active-pane" id="signin-settings" role="tabpanel" >
      <p style="font-size:13pt;"> <b>Step 7:</b> Sign In Settings</p>
      <hr class="header"/>

		<form id="signin-settings-form" class="aui" action="" method="POST">
		<input type="hidden" name="atl_token" value="${xsrfToken}" />
		<input type="hidden" id="signin_settings_submitted" name="signinSettingsSubmitted" value="true"/>
		<p>
            <a id="sign-in-settings"
               data-replace-text="1. Show Sign In Settings"
               class="sign-in-settings"
               aria-controls="sign-in-settings-div"
               onclick="showSignInOption()"
               style="font-size:13pt;cursor:pointer">1. Hide Sign In Settings
            </a>
        </p>
        <hr class="header" />
		<div class="sign-in-settings-div" id="sign-in-settings-div">

		<!--<div class="field-group">
					<label for="enableSAMLSSO" >Enable SAML SSO:</label>
					<#if enableSAMLSSO == true>
					<input class="checkbox" data-size="small" data-on-text="Yes" data-off-text="No" type="checkbox"
					name="enableSAMLSSO" checked="true" value="true" id="enableSAMLSSO"/>
					<#else>
					<input class="checkbox" data-size="small" data-on-text="Yes" data-off-text="No" type="checkbox"
					name="enableSAMLSSO" value="true" id="enableSAMLSSO"/>
					</#if>
					<div class="description">Enable SSO for Bamboo Server.</div>
		</div>-->

		<div class="field-group">
            <span style="vertical-align:middle;">
                <label class="switch" style="vertical-align:middle;">
                    <#if enableSAMLSSO == true>
                    <input type="checkbox" id="enableSAMLSSO" value="true" name="enableSAMLSSO"
                           checked="true">
                    <span class="slider round"></span> <#else>
                        <input type="checkbox" id="enableSAMLSSO" value="true" name="enableSAMLSSO">
                    <span class="slider round"></span></#if>
                </label>
                <span style="height:16px; width: 16px">
                    <strong>Enable SAML SSO for Bamboo Server</strong>
                </span>
            </span>
        </div>

        <div class="field-group">
            <span style="vertical-align:middle;">
                <label class="switch" style="vertical-align:middle;">
                    <#if headerAuthentication == true>
                        <input class="checkbox" type="checkbox" id="headerAuthentication" value="true"
                               name="headerAuthentication" checked="true">
                        <span class="slider round"></span>
                    <#else>
                        <input class="checkbox" type="checkbox" id="headerAuthentication" value="true"
                                   name="headerAuthentication">
                        <span class="slider round"></span>
                    </#if>
                </label>
                <strong>Enable Header Based Authentication</strong>
            </span>
            <div id = "headerAuthenticationAttributeDiv"
                    <#if headerAuthentication != true>
                        style="display: none;"
                    </#if> >
                <br/>
                <label for="headerAuthenticationAttribute">Authentication Attribute Name :
                    <span class="aui-icon icon-required">(required)</span>
                </label>
                <input type="text" required="true" id="headerAuthenticationAttribute" name="headerAuthenticationAttribute" style="width: 20%;"
                       value="${headerAuthenticationAttribute}" placeholder="Authentication Attribute Name" class="text long-field"/>
                <div class="description">Enter name of the attribute in request header which contains the username of Bamboo user.</div>
                <br>
                <div style="display: block;width: 90%;">
                    <div class="aui-message aui-message-info">
                        <span>
                           For Single Sign-on, redirect the user to the following endpoint with the authentication information in the
                           HTTP Header. <br><br>
                            <code>
                                <strong>Redirect Endpoint</strong> : ${settings.getSpBaseUrl()}/plugins/servlet/saml/header<br>
                                <strong>Method</strong> : GET/POST<br>
                                <strong>Header Parameters</strong> :-
                                <ul>
                                    <li>
                                       Authentication Attribute Name: Username
                                    </li>
                                    <li>
                                        ${settings.getHeaderAuthenticationRelayStateAttribute()} (optional): The URL that users
                                        will be directed to after successful authentication (Redirects to dashboard by default)
                                    </base64></li>
                                </ul>
                                </code>
                        </span>
                    </div>
                </div>
            </div>
        </div>
        <br/>

		<div class="field-group">
               <label for="loginButtonText" class="labelClass">Login Button Text:
               <span class="aui-icon icon-required">(required)</span>
               </label>
               <input type="text" required="true" id="loginButtonText" name="loginButtonText"
                  value="${loginButtonText}" class="text long-field"/>
               <div class="description">Set button label for SSO button shown on login page.</div>
               <br>
               <label for="relayState" class="labelClass">Relay State URL:</label>
               <input type="radio" class="radio" name="relayStateRedirectionType" id="forceRedirect" value="forceRedirect"
                    <#if relayStateRedirectionType == "forceRedirect">
                        checked="true"
                    </#if> >Force Redirect &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

                <input type="radio" class="radio" name="relayStateRedirectionType" id="redirectOnNoRelayState" value="redirectOnNoRelayState"
                    <#if relayStateRedirectionType == "redirectOnNoRelayState">
                        checked="true"
                    </#if> >Redirect On No Relay State
                    <br><br>
               <input type="url" id="relayState" value="${relayState}" name="relayState" placeholder="Enter the relay state url" class="text long-field"/>
               <div class="description">Enter the absolute URL where you want to redirect users after single sign-on. Keep empty to redirect user to the same URL they started with.<br>
                       <b>Force Redirect</b> option will always redirect user to the Relay State URL after SSO.<br>
                       <b>Redirect On No Relay State</b> option will redirect user to the page he was trying to visit, otherwise to the Relay State URL.
               </div>
            </div>
            <div class="field-group">
               <label for="disableDefaultLogin" class="labelClass">Auto-redirect to IdP:</label>
               <#if disableDefaultLogin == true>
               <input class="checkbox" data-size="small" data-on-text="Yes" data-off-text="No" type="checkbox"
                  name="disableDefaultLogin" checked="true" value="true" onclick="enableAutoRedirectToIdp()" id="disableDefaultLogin"/>
               <#else>
               <input class="checkbox" data-size="small" data-on-text="Yes" data-off-text="No" type="checkbox"
                  name="disableDefaultLogin" value="true" onclick="enableAutoRedirectToIdp()" id="disableDefaultLogin"/>
               </#if>
               <div class="description">This option redirects users to IdP on access of Bamboo login page.</div>
            </div>
            <div id="advancedAutoRedirectDiv" style="display: none;">
                <div class="field-group">
                    <div class="aui-message aui-message-info" style="width: 85%;">
                        <p>
                            1. If Single Logout is configured, the user will see the IDP login page once the user performs logout from Bamboo.
                        </p>
                        <p>
                            2. If Single Logout is not configured, the user will auto Login in to Bamboo after performing a logout in Bamboo.
                        </p>
                        <p>
                            <b>Note:-</b> If you want to show a logout page on Bamboo then enable Logout template.
                        </p>
                    </div>
                </div>

                <div class="field-group">
                   <label for="enableBackdoor" class="labelClass">Enable backdoor:</label>
                   <table>
                       <tr>
                           <td>
                            <input class="checkbox" type="checkbox" name="enableBackdoor" 
                                          value="true" id="enableBackdoor" onclick="checkBackdoorOption()" 
                                          <#if enableBackdoor == true> checked="true" </#if> />       
                           </td>
                           <td>
                               <span>Enable Backdoor Login &nbsp;&nbsp;&nbsp;</span>
                           </td>
                           <td>
                               <a id="show-url-details"
                                  data-replace-text="Show Backdoor URLs"
                                  class="show-url-details"
                                  aria-controls="backdoorUrlDiv"
                                  onclick="showBackdoorUrls()"
                                  style="cursor:pointer">Hide Backdoor URLs
                               </a>
                           </td>
                       </tr>
                   </table>
                   <br>
                   <div id="backdoorUrlDiv" style="display: block;">
                       <input type="hidden" id="backdoor_submitted" name="backdoorSubmitted" value="false">
                       <table style="word-wrap: break-word;" width="100%">
                           <tr>
                               <td width="33%">
                                   For Backdoor / Emergency url, use
                               </td>
                               <td width="33%">
                                <span id="saved_backdoor_div" title="click to copy" style="cursor:default">
                                    <p onmousedown="copy(this, '#c1')"><em>${settings.getLoginPageUrl()}?${backdoorKey}=${backdoorValue}</em></p>
                                    <a id="edit-backdoor-button"
                                       class="aui-button aui-button-subtle aui-icon aui-icon-small aui-iconfont-edit"
                                       title="edit backdoor" onclick="showEditBackdoorUrls()">
                                    </a>
                                </span>
                                <span id="edit_backdoor_div" style="cursor:default;display: none">
                                    <p>${settings.getLoginPageUrl()}?<input type="text" class="text"
                                                                          style="width: 20%" name="backdoorKey"
                                                                          id="backdoor_key" value="${backdoorKey}"/> = <input
                                            type="text" class="text" style="width: 20%" name="backdoorValue"
                                            id="backdoor_value" value="${backdoorValue}"/> </p>
                                    <a id="save-backdoor-button"
                                       class="aui-button aui-button-subtle aui-icon aui-icon-small aui-iconfont-success"
                                       title="save" onclick="saveBackdoorButtonClick()" ></a> <a
                                       id="cancel-backdoor-button"
                                       class="aui-button aui-button-subtle aui-icon aui-icon-small aui-iconfont-close-dialog"
                                       title="cancel" onclick="cancelBackdoorButtonClick()"></a>
                                </span>
                                <span id="c1" title="Copied" class="show-title" hidden></span>
                               </td>
                               <td style="position:relative; left:20px;bottom:2px">
                                <div id="Login-attempts-div" <#if enableBackdoor == false > style="display:none" </#if> >
                                  Backdoor Login Attempts:
                                  <input class="text short-field" type="number" name="numberOfLoginAttempts" id="numberOfLoginAttempts" min="1" value="${numberOfLoginAttempts}"/>
                                </div>
                                </td>
                           </tr>
                       </table>
                   </div>

                   <#if enableBackdoor == true>
                   <div id="backdoor-url-warning" style="display:none">
                   <#else>
                   <div id="backdoor-url-warning">
                   </#if>
                       <div class="aui-message aui-message-info" style="width: 85%;">
                           <span>
                               Note: Backdoor login is used by Administrators to log into their local Bamboo account. After disabling backdoor, you won't be able to log in with Bamboo's credentials.
                               In case you get locked out & you want to enable the Backdoor Login, use this API request<br><br>
                               <i>
                                   <strong>Post</strong> : ${settings.getBaseUrl()}/plugins/servlet/enablebackdoorurl<br>
                                   <strong>Headers</strong> :-
                                   <ul>
                                       <li>
                                           Content-Type : application/json
                                       </li>
                                       <li>
                                           Authorization : Basic &ltbase64 encoded admin_username:password&gt
                                       </li>
                                   </ul>
                               </i>
                           </span>
                       </div>
                   </div>
                </div>
                <div id="restrict-backdoor-div" class="field-group">
                    <input type="checkbox" id="restrictBackdoor" value="true"
                           name="restrictBackdoor" <#if restrictBackdoor == true> checked="true" </#if> > Restrict Backdoor
                    URL Access
                    <div class="description">Check this box to restrict backdoor URL access to certain groups.</div>
                </div>
                <br/>
                <div id="backdoorAccessGroupsList" style="display:none;">

                    <div class="field-group">
                        <label for="backdoorGroups">Groups</label>
                        <#if lowerBuild == "true">
                            <select class="select" name="backdoorGroups" id="backdoorGroups" multiple size="3" style="max-width: 500px !important;width: 500px;">
                                 <#foreach group in existingGroups>
                                 <option value="${group}"
                                     <#foreach selectedGroup in backdoorGroupsList>
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
                <br/>

                <div class="field-group">
                    <label for="enableAutoRedirectDelay" class="labelClass">Enable Auto Redirect Delay:</label>
                    <#if enableAutoRedirectDelay == true>
                        <input class="checkbox" type="checkbox" name="enableAutoRedirectDelay" checked="true"
                               value="true" id="enableAutoRedirectDelay"/>
                    <#else>
                        <input class="checkbox" type="checkbox" name="enableAutoRedirectDelay" value="true"
                               id="enableAutoRedirectDelay"/>
                    </#if>
                    <span>
                            Delay auto-redirect to IDP. This setting will apply only if auto-redirect is turned on.
                        </span>
                    <div class="description">Introduce a delay on the login page before user is redirected to the IDP
                        for authentication. This lets user go to login page if they don't want to get redirected.
                        Recommended for testing Auto-redirect.
                    </div>
                </div>
            </div>
         </div>


        <p>
            <a id="custom-login-template"
               data-replace-text="2. Show Custom Login template"
               class="custom-login-template"
               aria-controls="custom-login-template-div"
               onclick="showLoginTemplateSettings()"
               style="font-size:13pt;cursor:pointer">2. Hide Custom Login template
            </a>
        </p>
        <hr class="header"/>
        <div class="custom-login-template-div" id="custom-login-template-div">
            <div class="field-group">
                <label for="enableLoginTemplate" class="labelClass">Custom Login Template:</label>
                <#if enableLoginTemplate == true>
                    <input class="checkbox" type="checkbox" name="enableLoginTemplate" checked="true" value="true"
                           id="enableLoginTemplate" onclick="showHideLoginTemplate()">
                <#else>
                    <input class="checkbox" type="checkbox" name="enableLoginTemplate" value="true"
                           id="enableLoginTemplate" onclick="showHideLoginTemplate()">
                </#if>
            </div>

            <#if enableLoginTemplate == true>
                <div id="custom_login_template" class="field-group" style="display:block">
            <#else>
                <div id="custom_login_template" class="field-group" style="display:none">
            </#if>
                <textarea id="loginTemplate" name="loginTemplate" class="textarea long-field"
                    style="font-family:Courier New;" cols="64" rows="4">${loginTemplate?html}</textarea>
                <div class="description">
                    This is default template for Single IDP setup but you can customize it according to your need or you
                    can design your own SSO login template.
                </div>
                <br/>
                <div>
                    <p>Use the below code to add new SSO button in login template</p>
                    <div style="border: 1px solid grey;border-radius: 5px;padding: 7px; background-color: lightgray;max-width: 850px;">
                        <i>&lt;div class="field-group"&gt; &lt;input type="button" class="aui-button aui-button-primary"
                            value="Single Sign-On" onclick="AuthUrl('IDP ID')"&gt;
                            &lt;/div&gt;
                        </i>
                    </div>
                    <div class="description">To get the IDP ID, edit the SAML configuration from Configure IDP tab, you will find it in the <b>ACS URL for IDP-Initiated SSO</b>. It is options in case if you have only one IDP configured.
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
            <a id="sign-out-settings"
               data-replace-text="3. Show Sign Out Settings"
               class="sign-out-settings"
               aria-controls="sign-out-settings-div"
               onclick="showSignOutsettings()"
               style="font-size:13pt;cursor:pointer">
                3. Hide Sign Out Settings
            </a>
        </p>
		<hr class="header" />
		<div class="sign-out-settings-div" id="sign-out-settings-div">
            <div  class="field-group"  >
               <label for="customLogoutURL" class="labelClass">Custom Logout URL:</label>
               <div>
                  <#if enablelogoutTemplate == true>
                  <input type="url"  id="customLogoutURL" name="customLogoutURL" value="${customLogoutURL}"
                     placeholder="Custom Logout URL" class="text long-field" disabled="disabled"/>
                  <#else>
                  <input type="url"  id="customLogoutURL" name="customLogoutURL" value="${customLogoutURL}"
                     placeholder="Custom Logout URL" class="text long-field" />
                  </#if>
                  <div class="description">
                     Redirect to this URL after logging out (e.g. your IdP logout page). Leave empty to redirect on default bamboo login page.
                  </div>
               </div>
               <p style="font-size:13pt;">&nbsp;&nbsp;&nbsp;OR</p>
               <br>
               <div >
                  <#if  enablelogoutTemplate == true>
                  <input class="checkbox" type="checkbox"   name="enablelogoutTemplate" checked="true" value="true" id="enablelogoutTemplate" onclick="enableDisableCustomLogoutTextBox(this)"/>
                  <#else>
                  <input class="checkbox" type="checkbox"   name="enablelogoutTemplate" value="true" id="enablelogoutTemplate" onclick="enableDisableCustomLogoutTextBox(this)"/>
				  </#if>
                   <span>Use Custom Logout Template</span>
               </div>
			  </div>
				<div id="custom_logout_template" class="field-group" style="display: none;">

                  <legend><span>Custom Logout Template:</span></legend>

                  <textarea id="logoutTemplate"  name="logoutTemplate" class="textarea long-field" style="font-family:Courier New;" cols="64" rows="5">${logoutTemplate?html}</textarea>

               <div class="description">Your IdP is not in the list? Click here
                  Define the Custom logout page. This template will be shown to the user when user logs out. <br>Enable Custom Logout template and use <b>$baseUrl</b> for login page.
               </div>
            </div>
		 </div>
        <p>
            <a id="sso-error-settings"
               data-replace-text="4. Show Error Settings"
               class="sso-error-settings"
               aria-controls="sso-error-settings-div"
               onclick="showSSOErrorsettings()"
               style="font-size:13pt;cursor:pointer">
                4. Hide SSO Error Settings
            </a>
        </p>
		<hr class="header" />
		<div class="sso-error-settings-div" id="sso-error-settings-div" >
            <div class="field-group">
               <legend> <span>SSO Error Message Template:</span></legend>
               <#if enableErrorMsgTemplate == true>
                    <input class="checkbox" type="checkbox" name="enableErrorMsgTemplate" checked="true" value="true"
                            id="enableErrorMsgTemplate" onclick="showHideErrorMessageTemplate()"/>
               <#else>
                    <input class="checkbox" type="checkbox" name="enableErrorMsgTemplate" value="true"
                            id="enableErrorMsgTemplate" onclick="showHideErrorMessageTemplate()"/>
               </#if>
               <label for="enableErrorMsgTemplate" class="labelClass">SSO Error Message Template:</label>
               <br><br>
               <#if enableErrorMsgTemplate == true>
               <div id="enableErrorMsgTemplateDiv" style="display:block">
               <#else>
               <div id="enableErrorMsgTemplateDiv" style="display:none">
               </#if>
                   <textarea id="errorMsgTemplate"  name="errorMsgTemplate" class="textarea long-field" style="font-family:Courier New;" cols="64" rows="5">${errorMsgTemplate?html}</textarea>
                   <div class="description">
                      Define the Custom SSO error message templates. This template will be shown to the user when SSO fails. <br>Use <b>$baseUrl</b> for login page URL.
                   </div>
               </div>
            </div>
         </div>
        <p>
            <a id="advanced-sso-settings"
               data-replace-text="5. Show Advanced SSO Settings"
               class="advanced-sso-settings"
               aria-controls="advanced-sso-settings-div"
               onclick="showAdvanceOption()"
               style="font-size:13pt;cursor:pointer">5. Hide Advanced SSO Settings
            </a>
        </p>
		<hr class="header"/>
		<div class="advanced-sso-settings-div" id="advanced-sso-settings-div" >
                        <div class="field-group">
                            <span style="vertical-align:middle;">
                                <label class="switch" style="vertical-align:middle;">
                                    <#if enablePasswordChange == true>
                                        <input type="checkbox" id="enablePasswordChange" value="true" name="enablePasswordChange"
                                               checked="true">
                                            <span class="slider round"></span> <#else>
                                            <input type="checkbox" id="enablePasswordChange" value="true" name="enablePasswordChange">
                                            <span class="slider round"></span></#if>
                                </label>
                                <span style="height:16px; width: 16px">
                                    Allow Users to Change Password
                                </span>
                            </span>
                        </div>

                        <div class="field-group">
                                <span style="vertical-align:middle;">

                                <label class="switch" style="vertical-align:middle;">
                                    <#if pluginApiAccessRestriction == true>
                                        <input type="checkbox" id="pluginApiAccessRestriction" value="true" name="pluginApiAccessRestriction"
                                               checked="true">
                                            <span class="slider round"></span> <#else>
                                            <input type="checkbox" id="pluginApiAccessRestriction" value="true" name="pluginApiAccessRestriction">
                                            <span class="slider round"></span></#if>
                                </label>

                                    <span style="height:16px; width: 16px">
                                        Restrict access to plugin APIs
                                    </span>
                                </span>
                            <div class="description">When enabled, the plugin's APIs will not be accessible form outside this Bamboo instance.</div>
                            <div id="warningforapirestriction" class="aui-message aui-message-warning closeable" style="display:none;">
                                Make sure to check your Referrer Policy for the appropriate setting if Bamboo runs behind a reverse proxy.
                            </div>
                        </div>
            <div class="field-group">
                <label for="remember_me">Remember Me:</label>
                <div>
                    <#if enableRememberMeCookie == true>
                        <input class="checkbox" type="checkbox" name="enableRememberMeCookie"
                               id="enableRememberMeCookie"
                               checked="true" value="true" id="enableRememberMeCookie"/>
                    <#else>
                        <input class="checkbox" type="checkbox" name="enableRememberMeCookie"
                               id="enableRememberMeCookie"
                               value="true" id="enableRememberMeCookie"/>
                    </#if>
                    <span>Set the RememberMe-Cookie after authentication. If enabled, user stays logged in until user explicitly logs out.</span>
                </div>
                <p style="font-size:13pt;">&nbsp;&nbsp;&nbsp;OR</p>
            </div>

            <div id="sessionTimeOut" class="field-group">
				<label for="sessionTime" class="labelClass"> Set Session Timeout:</label>
               Inactive user session will last for Bamboo's default session timeout of <strong>30 Minutes.</strong>
			   <a id="timeout_extend_steps_link" onclick="showinfo()" style="cursor:pointer">Click here</a> to know how to extend
			   Bamboo's default session timeout
               <div id="extendtimeout" class="aui-message aui-message-info" style="display:none;">
                  <ol>
                     <li>
                        <i>Open&lt;bamboo-installation folder&gt;\atlassian-bamboo\WEB-INF\web.xml</i>
                     </li>
                     <li>Set value of<i>&lt;session-timeout&gt;</i> tag in minutes. By default it is 30(min).</li>
                     <li>Please restart your server.</li>
                  </ol>
               </div>
			</div>
            <div class="field-group">
               <label for="samlResponseValidity" class="labelClass">Validate IDP's SAML Response:<br>(recommended)</label>
               <div>
                  <input type="number" name="timeDelay" placeholder="MM"  id="timeDelay" value="${timeDelay}" class="text"/>
                  <div class="description">
                     Accept SAML Response with invalid timestamps <i>in minutes</i> as long as their values differ within this value.
                  </div>
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
   </div>
   </div>
   </body>
</html>