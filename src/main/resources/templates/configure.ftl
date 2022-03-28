<html>
<head>
    <meta name="decorator" content="atl.general"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
    <#assign count = oauth_request_parameters.keySet().size()>
    <script>
         $(document).ready(function() {
                       AJS.$(".aui-nav li").removeClass("aui-nav-selected");
                       AJS.$("#configureoauth").addClass("aui-nav-selected");

                        var appName =AJS.$('#appName').find(":selected").text();
                        if((appName != "Select Application") && (AJS.$.trim(appName) !== "Custom OAuth") && (AJS.$.trim(appName) !== "Custom OpenID")) {
                           AJS.$("#appsetupguide").prop("disabled",false);
                        } else {
                           AJS.$("#appsetupguide").prop("disabled",true);
                        }
                        selectAppOnLoad();
         });


        function showfield(name) {
            document.getElementById('div1').innerHTML = '';
        }

        function selectApp() {
            var appname = document.getElementById("appName").value;
            if (appname == "Custom OAuth") {
               AJS.$('#authorizeEndpoint').val("");
               AJS.$("#accessTokenEndpoint").val("");
               AJS.$("#userInfoEndpoint").val("");
               AJS.$("#fetchGroupsEndpoint").val("");
               AJS.$("#customAppName").val("");
            }
            else if(appname == "Custom OpenID"){
               AJS.$('#authorizeEndpoint').val("");
               AJS.$("#accessTokenEndpoint").val("");
               AJS.$("#customAppName").val("");
            }
            selectAppOnLoad();
        }

            function selectAppOnLoad() {
                var appname = document.getElementById("appName").value;
                if (appname == "Custom OAuth" || appname == "Custom OpenID") {
                   AJS.$("#authorize_endpoint").show();
                   AJS.$("#access_token_endpoint").show();
                   AJS.$("#custom_app_name").show();
                   AJS.$("#use_state_parameter").show();
                   AJS.$("#authorizeEndpoint").prop("required",true);
                   AJS.$("#accessTokenEndpoint").prop("required",true);
                   AJS.$("#customAppName").prop("required",true);
                   AJS.$("#acr_value_check").show();
                } else {
                   AJS.$("#authorize_endpoint").hide();
                   AJS.$("#access_token_endpoint").hide();
                   AJS.$("#custom_app_name").hide();
                   AJS.$("#use_state_parameter").hide();
                   AJS.$("#authorizeEndpoint").prop("required",false);
                   AJS.$("#accessTokenEndpoint").prop("required",false);
                   AJS.$("#customAppName").prop("required",false);
                   AJS.$("#acr_value_check").hide();
                }

                if (appname == "ADFS" || appname == "Custom OpenID" || appname == "AWS Cognito" || appname == "Okta" ||
                    appname == "Keycloak" || appname == "Azure B2C") {

                    AJS.$("#scope").show();
                    AJS.$("#enableCheckIssuerForDiv").show();
					AJS.$("#publicKeyDiv").show();
                    AJS.$("#nonce_div").css("display","inline");
                    document.getElementById("yt-player").href = "https://www.youtube.com/embed/dZflj0IIPTI" ;


                    var enableCheckIssuerFor = AJS.$("#enableCheckIssuerFor").is(":checked") ? true : false;
                    if (enableCheckIssuerFor == true) {
                        AJS.$("#checkForIssuerContentDiv").show();
                        var checkIssuerForCustom = AJS.$("#checkIssuerForCustom").is(":checked") ? true : false;
                        if (checkIssuerForCustom == true) {
                            jQuery("#customIssuerDiv").show();
                            jQuery("#customIssuerValue").prop("required",true);
                        } else {
                            jQuery("#customIssuerDiv").hide();
                            jQuery("#customIssuerValue").prop("required", false);
                        }
                    } else {
                        jQuery("#checkForIssuerContentDiv").hide();
                        jQuery("#customIssuerDiv").hide();
                        jQuery("#customIssuerValue").prop("required",false);
                    }
                } else {

					AJS.$("#publicKeyDiv").hide();
                    AJS.$("#enableCheckIssuerForDiv").hide();
                    AJS.$("#checkForIssuerContentDiv").hide();
					AJS.$("#customIssuerDiv").hide();
					AJS.$("#customIssuerValue").prop("required", false);
                    AJS.$("#nonce_div").hide();
                    //AJS.$("#scope").val("");
                    document.getElementById("yt-player").href = "https://www.youtube.com/embed/Z3K_zKIiQSI";

                }

                if (appname == "Custom OAuth") {
                    AJS.$("#user_info_endpoint").show();
                    AJS.$("#fetch_groups_endpoint").show();
                    AJS.$("#userInfoEndpoint").prop("required",true);
                } else {
                    AJS.$("#user_info_endpoint").hide();
                    AJS.$("#fetch_groups_endpoint").hide();
                    AJS.$("#userInfoEndpoint").prop("required",false);
                }

                if (appname == "Keycloak") {

                    AJS.$("#hostNameDiv").show();
                    AJS.$("#hostName").prop("required",true);
                    AJS.$("#realmNameDiv").show();
                    AJS.$("#realmName").prop("required",true);
                    AJS.$("#scopeDiv").hide();
                } else {
                    AJS.$("#hostNameDiv").hide();
                    AJS.$("#hostName").prop("required",false);
                    AJS.$("#realmNameDiv").hide();
                    AJS.$("#realmName").prop("required",false);
                    AJS.$("#scopeDiv").show();
                }

                if(appname == "ADFS" || appname == "AWS Cognito" || appname == "Okta" || appname == "Salesforce" || appname == "Gluu Server" || appname == "miniOrange") {
            	    jQuery("#domainNameDiv").show();
        		    jQuery("#domainName").prop("required",true);
                } else {
            	    jQuery("#domainNameDiv").hide();
				    jQuery("#domainName").prop("required",false);
                }

                if (appname == "Google") {
                    jQuery("#verify_credentials").show();
                } else {
                    jQuery("#verify_credentials").hide();
                }

                if(appname == "GitLab") {
                    jQuery("#appHostedOnDiv").show();
                    toggleAppHosting();
                } else{
                    jQuery("#appHostedOnDiv").hide();
        		    jQuery("#domainName").prop("type","text");
                }

                if(appname=="Slack" || appname =="Azure AD" || appname =="Azure B2C" ||
                 appname == "Okta" || appname =="Facebook" || appname =="Meetup" || appname == "Gluu Server"){
                     AJS.$("#scopeSpan").show();
                     AJS.$("#scope").prop("required",true);
                }else {
                  	  AJS.$("#scopeSpan").hide();
                     AJS.$("#scope").prop("required",false);
                }

                if(appname == "Azure AD" || appname == "Azure B2C") {
                    AJS.$("#tenantIdDiv").show();
                    AJS.$("#tenantID").prop("required",true);
                } else {
                    AJS.$("#tenantIdDiv").hide();
                    AJS.$("#tenantID").prop("required",false);
                }
            }

        function copyToClipboard(element, copyButton) {
            var temp = AJS.$("<input>");
            AJS.$("body").append(temp);
            temp.val(AJS.$(element).text()).select();
            document.execCommand("copy");
            temp.remove();
            AJS.$(copyButton).show();
            setTimeout(function() { AJS.$(copyButton).hide("slow"); }, 5000);
        }

        function toggleAppHosting(){
            var hostType = AJS.$("input[name='appHostedOn']:checked").val();
            if(hostType ==  "cloud"){
                AJS.$("#domainNameDiv").hide();
                AJS.$("#domainName").prop("required", false);
                AJS.$("#domainName").prop("type", "text");
            }else{
                AJS.$("#domainNameDiv").show();
                AJS.$("#domainName").prop("required", true);
                AJS.$("#domainName").prop("type", "url");
            }
        }

                 $(document).ready(function() {
                    var cnt = ${count};
                    document.getElementById("idTotalOauthParameters").value = cnt;
                 });
                 var counter = ${count};
                 function getDynamicTextBox(value){
                    return '<input id = "oauthParameterKey['+counter+']" name = "oauthParameterKey['+counter+']" type="text" class="text" placeholder="Parameter Name" style="width: 322px" value = "' + value + '" /> &nbsp&nbsp; <input id = "oauthParameterValue['+counter+']" name = "oauthParameterValue['+counter+']"  type="text" class="text" placeholder="Parameter Value" style="width: 322px" value = "' + value + '" />' +'&nbsp&nbsp;'+
                                        '<input type="button" value="-" class="aui-button" style="background: #0052cc;color: white;" onclick = "removeTextBox(this,'+counter+')" />'+'<br><br>'
                 }

                 function removeTextBox(div, loopCount) {
                    document.getElementById("userMappingInnerContainer").removeChild(div.parentNode);
                 }

                            function addTextBox() {
                                var div = document.createElement('DIV');
                                div.innerHTML = getDynamicTextBox("");
                                var userMappingInnerContainer = document.getElementById("userMappingInnerContainer");
                                userMappingInnerContainer.insertBefore(div,userMappingInnerContainer.childNodes[0] );
                                counter++;
                                document.getElementById("idTotalOauthParameters").value = parseInt(document.getElementById("idTotalOauthParameters").value) + 1;
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
             padding:5px 10px;
             margin-left: 20px;
             border-radius: 2px;
             border:1px solid #2D4D4D;
             background: #2D4D4D;
         }
        #mo-yt-icon{
          margin-top: -0.60%;
        }

    </style>
</head>
<body>
    <#include "*/header.ftl" parse=true>
	<div class="tabs-pane active-pane" id="configure-oauth" role="tabpanel">
    <p style="font-size:16pt;">Configure OAuth</p>
    <hr class="header" /><br>


    <div class="field-group">
        <p style="font-size:12pt;">Use Callback URL to configure applications in the OAuth Provider under Authorized Callback URL's.</p>
        </br>
        <table class="aui aui-table-interactive">
            <tbody>
                <tr>
                    <td headers="name"  width="20%">
                        <b>Callback URL</b>
                    </td>
                    <td headers="type"  width="45%">
                        <p id = "p1">${settings.getCallBackUrl()}</p>
                    </td>
                    <td  width="35%">
                        <button class="mo-copy aui-button" title="Copy to clipboard" onclick="copyToClipboard('#p1','#c1')"><i class="fa fa-copy"></i> Copy</button>
                        <span id="c1" title="Copied" class="show-title" hidden></span>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    </br></br>
    <div class="field-group">
        <p style="font-size:12pt;">
            Choose your Application from the list below to view its setup guide.
        </p>
    </div>

    <form id="configure-oauth-form" name="configure-oauth-form" class="aui long-label" action="" method="POST">
        <input type="hidden" name="atl_token" id="atl_token" value="${xsrfToken}" />
        <input type="hidden" name="submitted" value="true" />
        <input type="hidden" name="totalOauthParameters" id="idTotalOauthParameters" />
        <div class="field-group">
            <label for="selectApp">Select Application:
                <span class="aui-icon icon-required">(required)</span>
            </label>
            <select required="true" class="select" name="appName" id="appName" value="${appName}" onchange="selectApp()">
                <optgroup label="OAuth Apps">
                    <#foreach app in defaultOauthApps>
                        <option value="${app}"
                            <#if appName.equals(app)>
                            selected
                            </#if>
                        >${app}
                        </option>
                    </#foreach>
                </optgroup>
                <optgroup label="OpenID Apps">
                    <#foreach app in defaultOpenIdApps>
                        <option value="${app}"
                            <#if appName.equals(app)>
                                selected
                            </#if>
                        >${app}
                        </option>
                    </#foreach>
                </optgroup>
            </select>
            &nbsp;<button type="button" id="appsetupguide" class="aui-button aui-button-primary" style="width:150px;vertical-align: top;">View Setup Guide</button>
         &emsp;
        <a href="https://www.youtube.com/embed/Z3K_zKIiQSI" target="_blank" style="text-decoration: none" id="yt-player">
        <img src="/download/resources/com.miniorange.oauth.bamboo-oauth:oauth.plugin.auth.resources/yt_icon/youtube_icon.ico" id="mo-yt-icon" alt="yt-icon">
        </a>

        </div>
        <div class="field-group" id="custom_app_name">
            <label for="customAppName">App Name:
                <span class="aui-icon icon-required">(required)</span>
            </label>
            <input type="text" required="true" id="customAppName" name="customAppName" value="${customAppName}" class="text long-field" placeholder="Enter custom app name." />
        </div>

        <h2 style="margin: 10px 10px 0 6%;">General Settings: </h2>
        <hr style="margin: 5px 0px 14px;">
        <div class="field-group">
            <label for="">Client ID:
                <span class="aui-icon icon-required">(required)</span>
            </label>
            <input type="text" id="clientID" name="clientID" value="${clientID}" class="text long-field" placeholder="Enter the client ID." required/>
        </div>
        <div class="field-group">
            <label for="">Client Secret:
                <span class="aui-icon icon-required">(required)</span>
            </label>
            <input type="text" required="true" id="clientSecret" name="clientSecret" value="${clientSecret}" class="text long-field" placeholder="Enter the client secret." />
        </div>

        <#if appName!="Keycloak">
        	<div id="scopeDiv" class="field-group">
            	<label for=""> Scope:
            		<span id="scopeSpan" class="aui-icon icon-required">(required)</span>
            	</label>
            	<input type="text" id="scope" required="true" name="scope" value="${scope}" class="text long-field" placeholder="Enter space separated values."/>
            	<div class="description">Enter space separated values, eg: 'scope1 scope2' .</div>
        	</div>
        <#else>
        	<div id="scopeDiv" style="display: none;" class="field-group">
            	<label for=""> Scope:
            		<span id="scopeSpan" class="aui-icon icon-required">(required)</span>
            	</label>
            	<input type="text" id="scope" required="true" name="scope" value="${scope}" class="text long-field" placeholder="Enter space separated values." />
            	<div class="description">Enter space separated values, eg: 'scope1 scope2' .</div>
        	</div>
        </#if>
        <#if appName=="Keycloak">
            <div id="hostNameDiv" class="field-group">
	            <label for="">Host Name:
	                <span class="aui-icon icon-required">(required)</span>
	            </label>
	            <input type="text" id="hostName" name="hostName" value="${hostName}" class="text long-field" placeholder="Enter Host Name. For eg: https://example.com"/>
	            <div class="description">
                    Enter host name of your keycloak instance. Eg: https://&lt;domain name&gt;
	            </div>
	        </div>
	        <div id="realmNameDiv" class="field-group">
	            <label for="">Realm Name:
	                <span class="aui-icon icon-required">(required)</span>
	            </label>
	            <input type="text" id="realmName" name="realmName" value="${realmName}" class="text long-field" placeholder="Enter realm name." />
	            <div class="description">
                    Enter realm name of your application on keycloak. It is case sensitive, so please make sure you entered correctly.
	            </div>
	        </div>
	    <#else>
            <div id="hostNameDiv" style="display: none;" class="field-group">
	            <label for="">Host Name:
	                <span class="aui-icon icon-required">(required)</span>
	            </label>
	            <input type="text" id="hostName" name="hostName" value="${hostName}" class="text long-field" placeholder="Enter host name. Eg: https://&lt;domain name&gt"/>
	            <div class="description">
                    Enter host name of your keycloak instance. Eg: https://&lt;domain name&gt;
	            </div>
	        </div>
	        <div id="realmNameDiv" style="display: none;"s class="field-group">
	            <label for="">Realm Name:
	                <span class="aui-icon icon-required">(required)</span>
	            </label>
	            <input type="text" id="realmName" name="realmName" value="${realmName}" class="text long-field" placeholder="Enter realm name."/>
	            <div class="description">
                    Enter realm name of your application on keycloak. It is case sensitive, so please make sure you entered correctly.
	            </div>
	        </div>
        </#if>

        <#if appName =="Gitlab">
            <fieldset id="appHostedOnDiv" class="group">
                <legend>
                    <span>Hosted On:</span>
                </legend>
                <input class="radio" type="radio" name="appHostedOn" id="cloudHost" value="cloud" <#if appHostedOn == "Cloud"> checked="checked" </#if> />
                <label for="cloudHost">Cloud</label>

                <input class="radio" type="radio" name="appHostedOn" id="selfHost" value="self-hosted" <#if appHostedOn == "SelfHost"> checked="checked" </#if> />
                <label for="selfHost">Self Hosted</label>
            </fieldset>
        <#else>
            <fieldset id="appHostedOnDiv" class="group" style="display:none">
                <legend>
                    <span>Hosted On:</span>
                </legend>
                <input class="radio" type="radio" name="appHostedOn" id="cloudHost" value="cloud" <#if appHostedOn == "cloud"> checked="checked" </#if> />
                <label for="cloudHost">Cloud</label>

                <input class="radio" type="radio" name="appHostedOn" id="selfHost" value="self-hosted" <#if appHostedOn == "self-hosted"> checked="checked" </#if> />
                <label for="selfHost">Self Hosted</label>
            </fieldset>
        </#if>

        <#if appName == "ADFS" || appName == "AWS Cognito" || appName == "Okta" || appName == "Salesforce" || (appName == "GitLab" && appHostedOn == "self-hosted")  || appName == "Gluu Server" || appName == "miniOrange">
            <div id="domainNameDiv" class="field-group">
	            <label for="">Domain Name:
	                <span class="aui-icon icon-required">(required)</span>
	            </label>
	            <input type="text" id="domainName" placeholder="Enter domain name" name="domainName" value="${domainName}" class="text long-field" />
	        </div>
	    <#else>
	        <div style="display: none" id="domainNameDiv" class="field-group">
		        <label for="">Domain Name:
		            <span class="aui-icon icon-required">(required)</span>
		        </label>
		        <input type="text" id="domainName" placeholder="Enter domain name" name="domainName" value="${domainName}" class="text long-field" />
		    </div>
        </#if>
        <#if appName == "Azure AD" || appName == "Azure B2C">
            <div id="tenantIdDiv" class="field-group">
                <label for="">Tenant ID:
                    <span class="aui-icon icon-required">(required)</span>
                </label>
                <input type="text" id="tenantID" placeholder="Enter Tenant ID." name="tenantID" value="${tenantID}" class="text long-field" />
            </div>
        <#else>
            <div style="display: none" id="tenantIdDiv" class="field-group">
                <label for="">Tenant ID:
                    <span class="aui-icon icon-required">(required)</span>
                </label>
                <input type="text" id="tenantID" placeholder="Enter Tenant ID." name="tenantID" value="${tenantID}" class="text long-field" />
            </div>
        </#if>
        <div id="authorize_endpoint" class="field-group">
            <label for="">Authorize Endpoint:
                <span class="aui-icon icon-required"></span>
            </label>
            <input type="url" id="authorizeEndpoint" name="authorizeEndpoint" value="${authorizeEndpoint}" class="text long-field" placeholder="Enter authorize endpoint URL."/>
        </div>
        <div id="access_token_endpoint" class="field-group">
            <label for="">Access Token Endpoint:
                <span class="aui-icon icon-required"></span>
            </label>
            <input type="url" id="accessTokenEndpoint" name="accessTokenEndpoint" value="${accessTokenEndpoint}" class="text long-field" placeholder="Enter access token endpoint URL." />
        </div>
        <div id="user_info_endpoint" class="field-group">
            <label for="">Get User Info Endpoint:
                <span class="aui-icon icon-required"></span>
            </label>
            <input type="url" id="userInfoEndpoint" name="userInfoEndpoint" value="${userInfoEndpoint}" class="text long-field" placeholder="Enter user info endpoint URL." />
        </div>
        <div id="fetch_groups_endpoint" class="field-group">
            <label for="">Fetch Groups Endpoint:</label>
            <input type="url" id="fetchGroupsEndpoint" name="fetchGroupsEndpoint" value="${fetchGroupsEndpoint}" class="text long-field" placeholder="Enter groups endpoint URL." />
        </div>

        <div class="field-group">
            <label for="">Logout Endpoint:</label>
            <input type="url"  id="singleLogoutURL" placeholder="Enter the single logout URL."
                   name="singleLogoutURL" value="${singleLogoutURL}" class="text long-field" />
            <div class="description">
                Enter the Logout endpoint of your OAuth/OpenID Provider. Leave blank if Logout endpoint not supported by provider.<br>
                e.g. If Keycloak Logout endpoint is configured with </i>{hostname}/auth/realms/{realm-name}/protocol/openid-connect/logout</i> URL
                <br>then, on Bamboo logout you will get logged out from Keycloak too.
            </div>
        </div>

        <h2 style="margin: 10px 10px 0 4%;">Advanced Settings: </h2>
        <hr style="margin: 5px 0px 14px;">

         <#if isOpenIdProtocol == "true">
            <div class="field-group" id="enableCheckIssuerForDiv" style="display: block" >
                <label for="enableCheckIssuerFor">Check issuer in Response:</label>
                 <#if enableCheckIssuerFor == true>
                    <input class="checkbox" type="checkbox" name="enableCheckIssuerFor" checked="true" value="true" id="enableCheckIssuerFor" />
                 <#else>
                    <input class="checkbox" type="checkbox" name="enableCheckIssuerFor" value="true" id="enableCheckIssuerFor" />
                 </#if>
            </div>
         <#else>
            <div class="field-group" id="enableCheckIssuerForDiv" style="display: block" >
                <label for="enableCheckIssuerFor">Check issuer in Response:</label>
                <#if enableCheckIssuerFor == true>
                    <input class="checkbox" type="checkbox" name="enableCheckIssuerFor" checked="true" value="true" id="enableCheckIssuerFor" />
                <#else>
                    <input class="checkbox" type="checkbox" name="enableCheckIssuerFor" value="true" id="enableCheckIssuerFor" />
                </#if>
            </div>
         </#if>

         <#if enableCheckIssuerFor == true && isOpenIdProtocol == "true">
            <div class="field-group" id="checkForIssuerContentDiv">
                <#if checkIssuerFor == "Default">
                    <input class="radio" type="radio" name="checkIssuerFor" id="checkIssuerForDefault" value="Default" checked="checked"/>
                <#else>
                    <input class="radio" type="radio" name="checkIssuerFor" id="checkIssuerForDefault" value="Default"/>
                </#if>
                <span>Default</span>
                <#if checkIssuerFor == "Custom">
                    <input class="radio" type="radio" name="checkIssuerFor" id="checkIssuerForCustom" value="Custom" checked="checked" />
                <#else>
                    <input class="radio" type="radio" name="checkIssuerFor" id="checkIssuerForCustom" value="Custom" />
                </#if>
                <span>Custom</span>
            </div>
         <#else>
            <div class="field-group" id="checkForIssuerContentDiv" style="display:none">
                <#if checkIssuerFor == "Default">
                    <input class="radio" type="radio" name="checkIssuerFor" id="checkIssuerForDefault" value="Default" checked="checked"/>
                <#else>
                    <input class="radio" type="radio" name="checkIssuerFor" id="checkIssuerForDefault" value="Default"/>
                </#if>
                <span>Default</span>
                <#if checkIssuerFor == "Custom">
                    <input class="radio" type="radio" name="checkIssuerFor" id="checkIssuerForCustom" value="Custom" checked="checked" />
                <#else>
                    <input class="radio" type="radio" name="checkIssuerFor" id="checkIssuerForCustom" value="Custom" />
                </#if>
                <span>Custom</span>
            </div>
         </#if>

         <#if enableCheckIssuerFor == true && checkIssuerFor == "Custom" && isOpenIdProtocol == "true">
            <div class="field-group" id="customIssuerDiv">
                <label for="">Custom Issuer:</label>
                <input type="text" id="customIssuerValue" placeholder="Enter Custom Issuer" name="customIssuerValue" value="${customIssuerValue}" class="text long-field" required="true"/>
            </div>
         <#else>
            <div class="field-group" id="customIssuerDiv" style="display:none">
                <label for="">Custom Issuer:</label>
                <input type="text" id="customIssuerValue" placeholder="Enter Custom Issuer" name="customIssuerValue" value="${customIssuerValue}" class="text long-field"/>
            </div>
         </#if>

        <#if isOpenIdProtocol == "true">
            <div class="field-group" id="publicKeyDiv">
                <label for="">Public Key:</label>
                <textarea id="publicKey" placeholder="Enter public key." name="publicKey" class="textarea long-field" style="font-family:Courier New;" cols="64"
                    rows="4">${publicKey}</textarea>
            </div>
        <#else>
            <div class="field-group" id="publicKeyDiv" style="display: none">
                <label for="">Public Key:</label>
                <textarea id="publicKey" placeholder="Enter public key." name="publicKey" class="textarea long-field" style="font-family:Courier New;" cols="64"
                    rows="4">${publicKey}</textarea>
            </div>
        </#if>

        <#if appName=="Google">
        <div id="verify_credentials" class="field-group">
            <label for="verifyCredentials">Verify Credentials:</label>
            <input type="button" id="verify-credentials" value="Verify Admin Credentials" class="aui-button" />
            <div class="description">Verify Credentials using Admin Account only, else users will not be able to login.</div>

        </div>
        <#else>
        <div id="verify_credentials" style="display: none;" class="field-group">
            <label for="verifyCredentials">Verify Credentials</label>
            <#if clientID != ""  &&  clientSecret != "" >
            <input type="button" id="verify-credentials" value="Verify Admin Credentials" class="aui-button">
            <#else>
            <button class="aui-button" aria-disabled="true" disabled style="width:170px;">Verify Admin Credentials</button>
            </#if>
            <div class="description">Verify Credentials using Admin Account only. If not verified, the groups of users will not be fetched.</div>
        </div>
        </#if>
        <div class="field-group" id="UserCustomAttributeContainer" name = "UserCustomAttributeContainer">
            <label>Authorization Request Parameters:</label>
            <div style="margin-top:10px">
            <#if acrValueCheck == true >
                <input class="checkbox" type="checkbox" name="acrValueCheck" checked="true" value="true"
                id="acrValueCheck"/>ACR Value &nbsp;&nbsp;
            <#else>
                <input class="checkbox" type="checkbox" name="acrValueCheck" value="true"
                id="acrValueCheck"/>ACR Value &nbsp;&nbsp;
            </#if>
            <#if useStateParameter == true >
                <input class="checkbox" type="checkbox" name="useStateParameter" checked="true" value="true"
                 id="useStateParameter"/> State Parameter &nbsp;&nbsp;
            <#else>
                <input class="checkbox" type="checkbox" name="useStateParameter" value="true"
                id="useStateParameter"/> State Parameter &nbsp;&nbsp;
            </#if>

           <#if isOpenIdProtocol == "true">
                <div id="nonce_div"  style="display:inline">
                <#if nonceCheck == true >
                    <input class="checkbox" type="checkbox" name="nonceCheck" checked="true" value="true"
                     id="nonceCheck"/> Nonce &nbsp;&nbsp;
                 <#else>
                     <input class="checkbox" type="checkbox" name="nonceCheck" value="true"
                     id="nonceCheck"/> Nonce &nbsp;&nbsp;
                </#if>
                </div>
           <#else>
                <div id="nonce_div" style="display:none" >
                <#if nonceCheck == true >
                    <input class="checkbox" type="checkbox" name="nonceCheck" checked="true" value="true"
                    id="nonceCheck"/> Nonce &nbsp;&nbsp;
                <#else>
                    <input class="checkbox" type="checkbox" name="nonceCheck" value="true"
                     id="nonceCheck"/> Nonce &nbsp;&nbsp;
                </#if>
                </div>
           </#if>
            <br><div class="description">Selected parameter will be added in the authorized server request.</div>
             </div>
                <div style="margin-top:10px"><span>Add More Parameters </span>&nbsp&nbsp
                 <input type='button' style="background: #0052cc;color: white; width:4%" value='+' id='addUserAttr' class="aui-button" onclick="addTextBox()">
                  <div class="description">
                           Enter the parameters and associate values which will added in the authorization request.<br>
                 </div></div>
                 <div id="userMappingInnerContainer" name="userMappingInnerContainer">
                 <#assign loopCount = 0>
                 <#foreach key in oauth_request_parameters.keySet()>
                    <div>
                     <br/>
                     <input type="text" id="oauthParameterKey[${loopCount}]" name="oauthParameterKey[${loopCount}]" value="${key}" class="text"/>&nbsp&nbsp;
                     <input type="text" id="oauthParameterValue[${loopCount}]" name="oauthParameterValue[${loopCount}]" value="${oauth_request_parameters[key]}" class="text"/>&nbsp&nbsp;
                     <input type="button" style="background: #0052cc;color: white;" value="-" class="aui-button"  onclick ="removeTextBox(this, ${loopCount})" />
                     <#assign loopCount = loopCount+1>
                     </div>
                 </#foreach>
                 </div>

             </div>
        <div class="field-group">
            <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:170px;" /> &nbsp;&nbsp;&nbsp;
            <#if appName != "Custom App">
            <#if clientID != ""  && clientSecret != "" >
            <input type="button" id="test-oauth-configuration" value="Test Configuration" class="aui-button" style="width:170px;"  />
            <#else>
            <button class="aui-button" aria-disabled="true" disabled style="width:170px;">Test Configuration</button>
            </#if>
            <#else>
            <#if customAppName != ""  && clientID != ""  && clientSecret != ""  && authorizeEndpoint != ""  &&
            accessTokenEndpoint != "" && userInfoEndpoint != "">
            <input type="button" id="test-oauth-configuration" value="Test Configuration" class="aui-button" style="width:170px;"  />
            <#else>
            <button class="aui-button" aria-disabled="true" disabled style="width:170px;">Test Configuration</button>
            </#if>
            </#if>
        </div>

    </form>
    <br />


</div>
</section>
</div>
</div>
</body>
</html>
