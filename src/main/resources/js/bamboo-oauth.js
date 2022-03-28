(function($) {
    var count = 0;
    var currentUrl = $(location).attr('href');
    if (currentUrl.match(/userlogin/i) || currentUrl.match(/start.action/i)) {
        renderLoginPage();
    }

    AJS.$(document).on('click','#test-oauth-configuration',function(){
        var osDestination = "testoauthconfiguration";
        var OAuthUrl = AJS.contextPath() + '/plugins/servlet/oauth/auth';
        OAuthUrl += "?return_to=" + osDestination;
        var myWindow = window.open(OAuthUrl, "TEST OAuth Login", "scrollbars=1 width=800, height=600");
    });

    AJS.$(document).on('change','#enableCheckIssuerFor',function(){

        if(this.checked) {
            AJS.$("#checkForIssuerContentDiv").show();
            AJS.$("#checkForIssuerContentDiv").show("slow");
            if (AJS.$("#checkIssuerForCustom").is(":checked")) {
                AJS.$("#customIssuerDiv").show();
                AJS.$("#customIssuerValue").prop("required", true);
            } else {
                AJS.$("#customIssuerDiv").hide();
                AJS.$("#customIssuerValue").prop("required", false);
            }
        } else {
            AJS.$("#checkForIssuerContentDiv").hide();
            AJS.$("#customIssuerDiv").hide();
            AJS.$("#customIssuerValue").prop("required", false);
        }
    });


    

    function validateEmail($email) {
        var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
        return emailReg.test( $email );
    }

    
    AJS.$(document).on('change',"#checkIssuerForCustom",function(){
        if(this.checked) {
            AJS.$("#customIssuerDiv").show();
            AJS.$("#customIssuerValue").prop("required", true);
        } else {
            AJS.$("#customIssuerDiv").hide();
            AJS.$("#customIssuerValue").prop("required", false);
        }
    });

    AJS.$(document).on('change',"#checkIssuerForDefault",function(){
        if(this.checked) {
            AJS.$("#customIssuerDiv").hide();
            AJS.$("#customIssuerValue").prop("required", false);
        } else {
            AJS.$("#customIssuerDiv").show();
            AJS.$("#customIssuerValue").prop("required", true);
        }
    });
    AJS.$(document).on('click',"#edit-backdoor-button",function(){
        AJS.$("#saved_backdoor_div").hide();
        AJS.$("#edit_backdoor_div").show();
    });
    AJS.$(document).on('click', "#save-backdoor-button", function () {
        checkForBackdoorSubmitted();
        AJS.$("#backdoor_submitted").val(true);
        AJS.$("#signin_settings_submitted").val(false);
        AJS.$("#saved_backdoor_div").show();
        AJS.$("#edit_backdoor_div").hide();

    });
    AJS.$(document).on('click',"#cancel-backdoor-button",function(){
        AJS.$("#saved_backdoor_div").show();
        AJS.$("#edit_backdoor_div").hide();
    });
    /* Show Hide Auto-redirect to IDP Options */
//	if (AJS.$("#disableDefaultLogin").is(":checked")) {
//		AJS.$("#advance-auto-redirect-to-idp-div").show();
//	} else {
//		AJS.$("#advance-auto-redirect-to-idp-div").hide();
//	}
//
//	AJS.$("#disableDefaultLogin").change(function() {
//		if (this.checked) {
//			AJS.$("#advance-auto-redirect-to-idp-div").show();
//			AJS.$("#enableBackdoor").prop("checked", true);
//		} else {
//			AJS.$("#advance-auto-redirect-to-idp-div").hide();
//			AJS.$("#enableBackdoor").prop("checked", false);
//			AJS.$("#enableAutoRedirectDelay").prop("checked", false);
//		}
//	});

    /*Create user if role mapped and restrict user creation */

    AJS.$(document).on('change','#restrictUserCreation',function() {
        if (this.checked) {
            AJS.$("#createUsersIfRoleMapped").prop("disabled", true);
            AJS.$("#defaultGroup").prop("disabled", true);
        }else{
            AJS.$("#createUsersIfRoleMapped").prop("disabled", false);
            AJS.$("#defaultGroup").prop("disabled", false);
        }
    });


    AJS.$(document).on('change','#createUsersIfRoleMapped',function(){
        if(this.checked) {
            AJS.$("#defaultGroup").prop("disabled", true);
        } else {
            AJS.$("#defaultGroup").prop("disabled", false);
        }
    });

    //Enable disable auto-redirect to IDP

    if (AJS.$("#disableDefaultLogin").is(":checked")) {
        AJS.$("#advance-auto-redirect-to-idp-div").show("slow");
    } else {
        AJS.$("#advance-auto-redirect-to-idp-div").hide("slow");
    }

    AJS.$(document).on('change','#disableDefaultLogin',function(){
        if(this.checked) {
            AJS.$("#advance-auto-redirect-to-idp-div").show("slow");
            AJS.$("#enableBackdoor").prop("checked", true);
            AJS.$('#enable-backdoor-using-api').hide();
        } else {
            AJS.$("#advance-auto-redirect-to-idp-div").hide("slow");
            AJS.$("#enableBackdoor").prop("checked", false);
            AJS.$("#enableAutoRedirectDelay").prop("checked", false);
            AJS.$("#custom-delay-div").hide("slow");
        }
    });

    if (AJS.$("#restrictBackdoor").is(":checked")) {
            AJS.$("#backdoorAccessGroupsList").show("slow");
        } else {
            AJS.$("#backdoorAccessGroupsList").hide("slow");
        }

        AJS.$(document).on('change','#restrictBackdoor',function(){
            if(this.checked) {
                AJS.$("#backdoorAccessGroupsList").show("slow");
            } else {
                AJS.$("#backdoorAccessGroupsList").hide("slow");
            }
        });

    //Toggle custom logout template
    if (AJS.$("#enableLogoutTemplate").is(":checked")) {
        AJS.$("#custom_logout_template").show("slow");
        AJS.$("#customLogoutURL").prop("disabled", true);
    }
    else {
        AJS.$("#custom_logout_template").hide("slow");
        AJS.$("#customLogoutURL").prop("disabled", false);
    }

    AJS.$(document).on('change','#enableLogoutTemplate',function(){
        if(this.checked) {
            AJS.$("#custom_logout_template").show("slow");
            AJS.$("#customLogoutURL").prop("disabled", true);
        } else {
            AJS.$("#custom_logout_template").hide("slow");
            AJS.$("#customLogoutURL").prop("disabled", false);
        }
    });

    /*Separate Name Attribute */
    AJS.$(document).on('change','#useSeparateNameAttributes',function(){
        if (this.checked) {
            AJS.$("#fullNameAttributeDiv").hide();
            AJS.$("#separateNameAttributeDiv").show();
        } else {
            AJS.$("#fullNameAttributeDiv").show();
            AJS.$("#separateNameAttributeDiv").hide();
        }
    });


    AJS.$(document).on('click','#test-regex',function(){
        var osDestination = "testregex";
        var oAuthUrl = AJS.contextPath()+ '/plugins/servlet/oauth/auth';
        var regex = document.getElementById('regexPattern').value;
        oAuthUrl += "?return_to="+ encodeURIComponent(osDestination)	+ "&regexp=" + encodeURIComponent(regex);
        var testWindow = window.open(oAuthUrl, "","width=600,height=400");
    });

    AJS.$(document).on('click','#checkGroupRegex',function(){
        var osDestination = "testgroupregex";
        var oAuthUrl = AJS.contextPath()+ '/plugins/servlet/oauth/auth';
        var regex = document.getElementById('onTheFlyFilterIDPGroupsKey').value;
        oAuthUrl += "?return_to="+ encodeURIComponent(osDestination)	+ "&regexp=" + encodeURIComponent(regex);
        var testWindow = window.open(oAuthUrl, "","width=600,height=400");
    });

    AJS.$(document).on('click','#test-group-regex',function(){
            var osDestination = "testgroupregexcreate";
            var value=AJS.$("#testGroupRegex").val();
            var testUrl = AJS.contextPath() + '/plugins/servlet/oauth/auth';
            testUrl += "?return_to=" + encodeURIComponent(osDestination) + "&regexp=" +
            encodeURIComponent(AJS.$("#regexPatternForGroup").val())+"&regexg="+
            encodeURIComponent(AJS.$("#regexGroups").val())+"&groupName="+
            encodeURIComponent(AJS.$("#testGroupRegex").val());
            var testWindow = window.open(testUrl, "", "width=600,height=400");
        });

    AJS.$(document).on('keyup', '#regexfield', function (e) {
        setTimeout(function enable() {
            if (AJS.$("#regexPattern").val() != "") {
                AJS.$("#test-regex").prop("disabled", false);
            } else {
                AJS.$("#test-regex").prop("disabled", true);
            }
        }, 500);
    });
    /*Check Regex Pattern */
    AJS.$(document).on('change','#regexPatternEnabled',function(){
        if (this.checked) {
            AJS.$("#regexfield").show();
        } else {
            AJS.$("#regexfield").hide();
        }
    });

    if (AJS.$("#regexPatternEnabled").is(":checked")) {
        AJS.$("#regexfield").show();
    } else {
        AJS.$("#regexfield").hide();
    }
    AJS.$("#regexPattern").change(function () {
        if (AJS.$("#regexPattern").val() != "") {
            AJS.$("#test-regex").prop("disabled", false);
        } else {
            AJS.$("#test-regex").prop("disabled", true);
        }
    });


    AJS.$(document).on('change','#appName',function() {
        var appName = AJS.$('#appName').find(":selected").text();
        if (AJS.$.trim(appName) === "Keycloak" || AJS.$.trim(appName) === "ADFS" || AJS.$.trim(appName) === "AWS Cognito"
            || AJS.$.trim(appName) === "Okta" || AJS.$.trim(appName) === "Azure B2C" || AJS.$.trim(appName) === "Custom OpenID") {
            AJS.$("#scopeDiv").show();
            AJS.$("#scope").val("openid");
        } else {
            AJS.$("#scope").val("");
        }
    });


    /*On change of login user attribute */
    AJS.$(document).on('change','#loginUserAttribute',function(){
        value = AJS.$("#loginUserAttribute").val();
        if (value != "email") {
            AJS.$("#warningforemail").hide();
        } else {
            AJS.$("#warningforemail").show();
        }
    });

    AJS.$(document).on('click','#verify-credentials',function(){
        var osDestination = "verifycredentials";
        var OAuthUrl = AJS.contextPath() + '/plugins/servlet/oauth/auth';
        OAuthUrl += "?return_to=" + osDestination;
        var myWindow = window.open(OAuthUrl, "Verify Credentials", "scrollbars=1 width=800, height=600");
    });

    AJS.$(document).on('change','#appName',function() {
        var appName = $('#appName').find(":selected").text();
        if((appName != "Select Application") && ($.trim(appName) !== "Custom OAuth") && ($.trim(appName) !== "Custom OpenID")) {
            AJS.$("#appsetupguide").prop("disabled",false);
        } else {
            AJS.$("#appsetupguide").prop("disabled",true);
        }
    });

    AJS.$(document).on('click','#appsetupguide',function(){
        var appName = $('#appName').find(":selected").text();
        var OAuthUrl = AJS.contextPath() + '/plugins/servlet/oauth/downloadappguides';
        OAuthUrl += "?appName=" + appName;
        window.open(OAuthUrl,'_blank');
    });

    function renderLoginPage() {
        count++;
        if ($("#loginForm").length > 0) {
            loadLoginForm($("#loginForm"));
        } else {
            var url = window.location.href;
            if (url.match(/userlogin/i) && count <= 50) {
                setTimeout(renderLoginPage, 100);
            }
        }
    }
    AJS.$(document).on('change','#onTheFlyGroupCreation',function(){
        if (this.checked) {
            AJS.$("#groupMappingDiv").hide();
            AJS.$("#onTheFlyGroupMappingDiv").show();
        } else {
            AJS.$("#groupMappingDiv").show();
            AJS.$("#onTheFlyGroupMappingDiv").hide();
        }
    });

    AJS.$(document).on('change', '#onTheFlyFilterIDPGroupsOption', function(){
        if(AJS.$("#onTheFlyFilterIDPGroupsOption").val() == "None"){
            AJS.$("#onTheFlyFilterIDPGroupsKey").hide();
        }else{
            AJS.$("#onTheFlyFilterIDPGroupsKey").show();
        }
    });

    AJS.$(document).on('change', '#onTheFlyFilterIDPGroupsOption', function(){
        if(AJS.$("#onTheFlyFilterIDPGroupsOption").val() == "Regex"){
            AJS.$("#checkGroupRegex").show();
            if(AJS.$("#onTheFlyFilterIDPGroupsKey").val() == ""){
                AJS.$("#checkGroupRegex").prop("disabled", true);
            }else{
                AJS.$("#checkGroupRegex").prop("disabled", false);
            }
        }else{
            AJS.$("#checkGroupRegex").hide();
        }
    });

    AJS.$(document).on('keyup', '#onTheFlyFilterIDPGroupsKey', function(){
        if(AJS.$("#onTheFlyFilterIDPGroupsKey").val() == ""){
            AJS.$("#checkGroupRegex").prop("disabled", true);
        }else{
            AJS.$("#checkGroupRegex").prop("disabled", false);
        }
    });

    AJS.$(document).on('change','#onTheFlyAssignNewGroupsOnly',function(){
        if (this.checked) {
            AJS.$("#onTheFlyGroupMappingDiv").hide();
        } else {
            AJS.$("#onTheFlyGroupMappingDiv").show();
        }
    });

    AJS.$(document).on('click', '#group-mapping-pill', function () {
        AJS.$('#group-mapping-pill').attr("class", "active");
        AJS.$("#on-the-fly-group-mapping-pill").removeClass("active");
        toggleOnTheFlyGroupMapping();
    });

    AJS.$(document).on('click', '#on-the-fly-group-mapping-pill', function () {
        AJS.$('#on-the-fly-group-mapping-pill').attr("class", "active");
        AJS.$("#group-mapping-pill").removeClass("active");
        toggleOnTheFlyGroupMapping();
    });

    AJS.$(document).on('change', '#cloudHost', function () {
            toggleAppHosting();
    });

    AJS.$(document).on('change', '#selfHost', function () {
            toggleAppHosting();
    });

    AJS.$(document).on('change', "#appName", function (){
            var appname = AJS.$("#appName").val();
            if( appname == "GitLab"){
                toggleAppHosting();
            }
    });
    function toggleAppHosting()
    {
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
    enableDisableButtons();
    toggleOnTheFlyGroupMapping();

    function toggleOnTheFlyGroupMapping() {
        var groupMappingClass = AJS.$('#group-mapping-pill').attr("class");
        if (groupMappingClass == "active") {
            AJS.$("#onthefly-group-mapping-instructions-div").hide();
            AJS.$("#onTheFlyGroupMappingDiv").hide();
            AJS.$("#onTheFlyCreateNewGroupsDiv").hide();
            AJS.$("#onTheFlyAssignNewGroupsOnlyDiv").hide();
            AJS.$("#on-the-fly-group-mapping-main-inst").hide();
            AJS.$("#onTheFlyFilterIdpGroups").hide();
            AJS.$("#groupMappingDiv").show();
            AJS.$("#group-mapping-main-inst").show();

            AJS.$("#roleAttribute").prop("required", false);
            AJS.$("#roleAttributeAsterisk").hide();
            AJS.$("#onTheFlyGroupCreation").val("false");
        } else {
            AJS.$("#groupMappingDiv").hide();
            AJS.$("#group-mapping-instructions-div").hide();
            AJS.$("#group-mapping-main-inst").hide();
            AJS.$("#onTheFlyCreateNewGroupsDiv").show();
            AJS.$("#onTheFlyAssignNewGroupsOnlyDiv").show();
            AJS.$("#on-the-fly-group-mapping-main-inst").show();
            AJS.$("#onTheFlyFilterIdpGroups").show();

            if(AJS.$("#onTheFlyAssignNewGroupsOnly").is(":checked")) {
                AJS.$("#onTheFlyGroupMappingDiv").hide();
            } else {
                AJS.$("#onTheFlyGroupMappingDiv").show();
            }

            AJS.$("#roleAttribute").prop("required", true);
            AJS.$("#roleAttributeAsterisk").show();
            AJS.$("#onTheFlyGroupCreation").val("true");
        }
    }

    function enableDisableButtons() {
        AJS.$.ajax({
            url: AJS.contextPath() + "/plugins/servlet/oauth/getconfig",
            type: "GET",
            error: function () {},
            success: function (response) {
                if (response.isConfigured === true) {
                    AJS.$("#amSubmit").prop("disabled", false);
                    AJS.$("#gmSubmit").prop("disabled", false);
                    AJS.$("#gmSubmit_top").prop("disabled", false);
                } else {
                    AJS.$("#amSubmit").prop("disabled", true);
                    AJS.$("#gmSubmit").prop("disabled", true);
                    AJS.$("#gmSubmit_top").prop("disabled", true);
                }
            }
        });
    }

    function loadLoginForm(loginForm) {
        if (loginForm.length > 0) {
            $
                .ajax({
                    url : AJS.contextPath()
                    + "/plugins/servlet/oauth/getconfig",
                    type : "GET",
                    error : function() {
                    },
                    success : function(response) {
                        if (response.isConfigured === true) {
                            if (response.enableLoginTemplate == true && !response.disableDefaultLogin) {
                                if (getQueryParameterByName('show_login_form') != "true") {
                                    AJS.$('body').hide();
                                    var html = '<div>' + response.loginTemplate + '</div>';
                                    AJS.$("body").html(html);
                                    AJS.$('body').show();
                                    return;
                                }
                            }
                            else{
                                loginForm.show();
                            }
                        	if (response.disableDefaultLogin) {
                        	    if(response.enableBackdoor && getQueryParameterByName(response.backdoorKey)==response.backdoorValue
                        	    && response.restrictBackdoor == true ){
                                    showBackdoorForm(loginForm)
                                    return;
                        	    } else if(response.enableBackdoor && getQueryParameterByName(response.backdoorKey)==response.backdoorValue){
                                    $("#loginForm")
                                            .append(
                                                    '<input type="hidden" name=${backdoorKey} value=${backdoorValue} />');
                                    loginForm.show(); 
                                    return;
                                }
                                else if(response.enableAutoRedirectDelay){
                                    insertDelay(loginForm, response);
                                    loginForm.show();
                                }else{
                                    redirectToIDP(loginForm);
                                }
                                return;
                            } else {
                                showLoginButton(loginForm, response);
                                loginForm.show();
                                return;
                            }

                        }
                        loginForm.show();
                        return;
                    }
                });
        }
    }
    function checkForBackdoorSubmitted() {
        var backdoorKey = AJS.$("#backdoor_key").val();
        var backdoorValue = AJS.$("#backdoor_value").val();
        var atl_token = AJS.$("#atl_token").val();
        if(backdoorKey === ""){
            backdoorKey = "oauth_sso";
        }
        if(backdoorValue === ""){
            backdoorValue = "false";
        }
        AJS.$.ajax({
            url: AJS.contextPath() + "/plugins/servlet/oauth/moapi",
            data: {
                "action": "updateBackdoorDetails",
                "backdoorKey" : backdoorKey,
                "backdoorValue" : backdoorValue,
                "atl_token" : atl_token
            },
            type: "POST",
            success: function (response) {
            var loginUrl = window.location.origin;
                AJS.$("#saved_Backdoor_Div").html(loginUrl + AJS.contextPath() + '/userlogin!doDefault.action?' + backdoorKey + '=' + backdoorValue);
                require('aui/flag')({
                    title: 'Backdoor/Emergency URL updated successfully',
                    type: 'success',
                    close: 'auto',
                });
            },
            error: function (response) {
                require('aui/flag')({
                    title: 'Error while updating Backdoor/Emergency URL',
                    type: 'error',
                    close: 'auto',
                });
            }
        });
    }
    function showLoginButton(loginForm,response) {

        var next = getQueryParameterByName("next");
        var osDestination = getQueryParameterByName("os_destination");
        var destination = getQueryParameterByName("destination");

        var OauthUrl = AJS.contextPath() + '/plugins/servlet/oauth/auth';
        if(!(!next || /^\s*$/.test(next)) && next != "/") {
            OauthUrl += "?return_to=" + encodeURIComponent(next);
        }else if(!(!osDestination || /^\s*$/.test(osDestination)) && osDestination != "/"){
            OauthUrl += "?return_to=" + encodeURIComponent(osDestination);
        }else if(!(!destination || /^\s*$/.test(destination)) && destination != "/"){
            OauthUrl += "?return_to=" + encodeURIComponent(destination);
        }

        var html = '<a class="aui-button aui-style aui-button-primary" href="' + OauthUrl + '" style="align:center;">' + response.loginButtonText + '</a>';
        // $(html).insertAfter($("#loginForm_save"));

        if (response.ssoButtonLocation == "Before Login Button") {
            AJS.$(html).insertBefore(AJS.$("#loginForm_save"));
        } else {
            AJS.$(html).insertAfter(AJS.$("#loginForm_save"));
        }

        var oautherror=getQueryParameterByName('oautherror');
        //console.log(oautherror);
        if(oautherror!=null){
            var htmlerror='<div class="aui-message aui-message-error closeable"><p>We couldn\'t sign you in. Please contact your Administrator. </p></div>';
            AJS.$(htmlerror).insertBefore(AJS.$("div[id='fieldArea_loginForm_os_username']"));
        }

    }

    function redirectToIDP(loginForm) {
        var loginForm = document.getElementById("loginForm");
        loginForm.innerHTML = "";
        var html = '<div class="aui-message aui-message-info"><span>Please wait while we redirect you for authentication...</span></div>';
        AJS.$(".aui-page-panel-content").append(html);
        var osDestination = getQueryParameterByName("os_destination");
        var oauthCallbackUrl = AJS.contextPath() + '/plugins/servlet/oauth/auth';
        if(!(!osDestination || /^\s*$/.test(osDestination)) && osDestination != "/") {
            oauthCallbackUrl += "?return_to=" + encodeURIComponent(osDestination);
        }
        window.location.href = oauthCallbackUrl;
    }

    function showBackdoorForm(loginForm) {

                var html = '<div id="backdoor_restriction_note" class="aui-message aui-message-info">Please submit your username to access login form</div>' +
                    '<form id="backdoor-restriction-form" method="post" action="" name="backdoor-restriction-form" class="aui gdt">' +
                    '<div class="field-group">' +
                    '<label accesskey="u" for="login-form-email" id="usernamelabel" style="display: block;"><u>U</u>sername</label>' +
                    '<input class="text medium-field" id="login-form-email" name="mo_username" type="text"/>' +
                    '</div>' +
                    '<div class="field-group">' +
                    '<button id="check_backdoor_access" class="aui aui-button aui-button-primary">Submit</button>' +
                    '</div>' +
                    '</form>';
                AJS.$("#gadget-0").css("height", "355px");
                //AJS.$(html).appendTo(AJS.$(".aui.login-form-container"));
                var loginForm = document.getElementById("loginForm");
                        AJS.$("#" + loginForm.id + "").hide();
                        AJS.$(".aui-page-panel-content").append(html);

    }

                AJS.$(document).on('click', "#check_backdoor_access", function (e) {
                            e.preventDefault();
                            checkForBackdoorAccess();
                });

                function checkForBackdoorAccess() {
                            var loginForm = document.getElementById("loginForm");
                            var username = AJS.$("#login-form-email").val();
                            if (!username) {
                                noAccessHtml = '<div id="backdoor_access_verification_error" class=\"aui-message aui-message-error closeable\"><p>Username Cannot be Empty</p></div>';
                                AJS.$("#backdoor_access_verification_error").remove();
                                AJS.$(noAccessHtml).insertBefore(AJS.$("#backdoor-restriction-form"));
                                return;
                            }
                            AJS.$.ajax({
                                url: AJS.contextPath() + "/plugins/servlet/oauth/moapi",
                                data: {
                                    "action": "checkBackdoorAccess",
                                    "username": username
                                },
                                type: "GET",
                                error: function (response) {
                                    noAccessHtml = '<div id="backdoor_access_verification_error" class=\"aui-message aui-message-error closeable\"><p>An Error Occurred while verifying access</p></div>';
                                    AJS.$("#backdoor_access_verification_error").remove();
                                    AJS.$(noAccessHtml).insertBefore(AJS.$("#backdoor-restriction-form"));
                                    AJS.$("#backdoor_restriction_note").remove();
                                },
                                success: function (response) {
                                    console.log("response.isUserAllowedBackdoorAccess = " + response.isUserAllowedBackdoorAccess);
                                    if (response.isUserAllowedBackdoorAccess === true) {
                                        AJS.$("#no_backdoor_access_error").remove();
                                        AJS.$("#login-form-email").val(username);
                                        AJS.$("#backdoor_restriction_note").remove();
                                        AJS.$("#backdoor-restriction-form").remove();
                                        AJS.$("#" + loginForm.id + "").show();

                                    } else {
                                        noAccessHtml = '<div id="no_backdoor_access_error" class=\"aui-message aui-message-error closeable\"><p>Sorry. You are not authorized to access the login page. Please enter valid username for getting Login page.</p></div>';
                                        if (!AJS.$("#no_backdoor_access_error").length)
                                            AJS.$(noAccessHtml).insertBefore(AJS.$("#backdoor-restriction-form"));

                                    }
                                }
                            });
                        }

    function insertDelay(loginForm, response) {
		/**
		 * The following code has been copied from showLoginButton
		 * function below
		 */

          var totalAutoRedirectDelayInterval = parseInt(response.autoRedirectDelayInterval) * 5;
    	  var osDestination = getQueryParameterByName("os_destination");
          var oauthCallbackUrl = AJS.contextPath() + '/plugins/servlet/oauth/auth';
          if(!(!osDestination || /^\s*$/.test(osDestination)) && osDestination != "/") {
         	 oauthCallbackUrl += "?return_to=" + encodeURIComponent(osDestination);
          }

		/** The Text to show once user has been redirected */
		var textOnCancel = '<a href="'
				+ oauthCallbackUrl
				+ '" id="redirectMessage" class="aui-button aui-button-link">'
				+ response.loginButtonText + '</a>';
		
		/** Create the delay box on the login page */
		var html = '<div id="moRedirectBox" class="aui-message aui-message-info">'
				+ '<ssoDIV>'
				+ '<span id="defaultText">Redirecting to IDP.</span>'
				+ '<span id="textOnCancel" style="display:none;">'
				+ textOnCancel
				+ '</span>'
				+ '<button id="stopAutoRedirect" class="aui-button aui-button-primary" style="float:right;"'
				+ 'resolved="" onClick="stopAutoRedirectFlow()">Cancel</button>'
				+ '</ssoDIV>'
				+ '<input type="hidden" id="oauthCallbackUrl" value="'
				+ oauthCallbackUrl
				+ '"></input>'
				+ '<div id="moRedirectProgress" class="aui-progress-indicator" style="margin-top: 1.5em;'
				+ 'background: #e9e9e9;border-radius: 3px;height: 5px;overflow: hidden;position: relative;'
				+ 'width: 100%;" data-value="0.01">'
				+ '<span class="aui-progress-indicator-value" style="position: absolute;display: block;'
				+ 'height: 5px;animation: progressSlide 1s infinite linear;animation-play-state: running;'
				+ '-webkit-animation-play-state: paused;animation-play-state: paused;background: #3572b0;'
				+ 'border-radius: 3px 0 0 3px;transition: width .5s;-webkit-transform: skewX(0);'
				+ 'transform: skewX(0);width:0%"></span>'
				+ '</div>'
				+ '</div>'
				+ '<script>'
				+ 'var delayFunc;'
				+ 'var progress = 0.005;'
				/**
				 * Function is used to stop the Auto redirect and show a
				 * link to redirect the user to the IDP for
				 * authentication.
				 */
				+ 'function stopAutoRedirectFlow(){'
				+ 'clearTimeout(delayFunc);'
				+ 'AJS.$("#moRedirectProgress").remove();'
				+ 'AJS.$("#defaultText").remove();'
				+ 'AJS.$("#stopAutoRedirect").remove();'
				+ 'AJS.$("#textOnCancel").css("display","block");'
				+ '}'
				/**
				 * Function is used to update the progressBar added to
				 * the login page if admin has set a delay for the
				 * auto-redirect. If the progress bar reaches 100% then
				 * redirect the user to the IDP.
				 */
				+ 'function updateProgressBarState(){'
				+ 'if(progress<0.995){'
				+ 'progress = progress+0.005;'
				+ 'var percentage = progress*100+"%";'
				+ 'AJS.$("#moRedirectProgress").attr("data-value",progress);'
				+ 'AJS.$(".aui-progress-indicator-value").css("width",percentage);'
				+ 'delayFunc = setTimeout(function(){ updateProgressBarState(); } , '+totalAutoRedirectDelayInterval+'  );'
				+ '}else{'
				+ 'progress=0.1;'
				+ 'var url = AJS.$("#oauthCallbackUrl").val();'
				+ 'var currentUrl = window.location.href.split("'
				+ '?'
				+ '")[0];' 
				+ 'window.location.href = url;'
				+ '}'
				+ '}'
				+ 'updateProgressBarState();' 
				+ '</script>';

		/** Inserting the div after the Default Login Form */
		AJS.$("#gadget-0").css("height", "355px");
		AJS.$(html).insertAfter($("#loginForm_save"));
	} 	 	

    function getQueryParameterByName(name) {

        var url = window.location.href;
        name = name.replace(/[\[\]]/g, "\\$&");
        var regex = new RegExp("[?&]" + name + "(=([^&]*)|&|#|$)"), results = regex
            .exec(url);
        if (!results)
            return null;
        if (!results[2])
            return '';
        return decodeURIComponent(results[2].replace(/\+/g, " "));
    }


})(AJS.$ || jQuery);