AJS.$(function() {
	var count = 0;
	var pathname = window.location.pathname;
	var url = window.location.href;
	var sessionCount;

	var intervalId = setInterval(function() {
        try{
         sessionCount = ReadCookie("SESSIONCHECKCOUNT");
         if(sessionCount=='NaN'){
             sessionCount = 0;
         }
            if(!getCurrentUserName() && sessionCount < 16){
                  if(ReadCookie("IDPSESSIONEXIST")=="true"){
                        var loginForm = AJS.$('.aui.login-form-container');
                        redirectToIDP(loginForm);
                       deleteCookie("IDPSESSIONEXIST");
                        clearInterval(intervalId);
                  }
                  else{
                          var iframe = document.getElementById("autoIFrame");
                          sessionCount = parseInt(sessionCount) + 1;
                          var d = new Date();
                          d.setTime(d.getTime() + (15*60*1000));
                          var expires = "expires=" + d.toUTCString();
                          document.cookie = "SESSIONCHECKCOUNT" + "=" + sessionCount + ";" + expires + "path=/";
                          iframe.src = iframe.src;
                  }
              }
              else{
                if(getCurrentUserName()){
                    console.log("current logged in user found");
                    clearInterval(intervalId);
                }
                else{
                     console.log("IDP session was not found for 15 iterations, it will be checked again after 15 minutes");
                }
              }
          }
          catch(err){
            console.log("error occured" + err);
          }
    }, 15000);

	if(url.split("?")[0].match(/\/confluence-oauth/i)){
		AJS.$('#main.aui-page-panel').addClass("pad1");
	}

	if(AJS.params.remoteUser == "" || AJS.params.remoteUser == null) {
        renderLoginPage();
    }

    toggleOnTheFlyGroupMapping();

    AJS.$(document).on('click', '#group-mapping-pill', function () {
        AJS.$('#group-mapping-pill').attr("class", "active");
        AJS.$("#on-the-fly-group-mapping-pill").removeClass("active");
        toggleOnTheFlyGroupMapping();
    });

    AJS.$(document).on("click","#remind-me-again",function(){
        flag.close();
    });

    AJS.$(document).on("click","#do-not-show-again",function (){
        flag.close();
        AJS.$.ajax({
            url: AJS.contextPath() + "/plugins/servlet/oauth/moapi",
            type: "GET",
            data: {
                "action": "doNotShowRestApiMsgAgain"
            }
        });
    });

    AJS.$(document).on('click', '#on-the-fly-group-mapping-pill', function () {
        AJS.$('#on-the-fly-group-mapping-pill').attr("class", "active");
        AJS.$("#group-mapping-pill").removeClass("active");
        toggleOnTheFlyGroupMapping();
    });

    if (AJS.$("#onTheFlyAssignNewGroupsOnly").is(":checked")) {
        AJS.$("#onTheFlyDoNotRemoveGroupsDiv").hide();
        AJS.$("#onTheFlyDoNotRemoveGroups").prop("required", false);
    } else {
        AJS.$("#onTheFlyDoNotRemoveGroupsDiv").show();
    }

    AJS.$("#onTheFlyAssignNewGroupsOnly").change(function () {
        if (this.checked) {
            AJS.$("#onTheFlyDoNotRemoveGroupsDiv").hide();
            AJS.$("#onTheFlyDoNotRemoveGroups").prop("required", false);
        } else {
            AJS.$("#onTheFlyDoNotRemoveGroupsDiv").show();
        }
    });

    AJS.$(document).on('click', "#domain-mapping-login", function (e) {
        processDomainAndRedirect();
        e.preventDefault();
    });

    AJS.$(document).on('click',"#edit-callback-url-button",function(){
        AJS.$("#saved-callback-div").hide();
        AJS.$("#edit-callback-div").show();
        var callbackParam = AJS.$("#customCallbackParameter").val();
        if (callbackParam.charAt(0)=="/")
            document.getElementById("customCallbackParameter").value=callbackParam.substring(1);

    });
    AJS.$(document).on('click', "#save-callback-button", function () {
        checkForCallbackSubmitted();
        AJS.$("#saved-callback-div").show();
        AJS.$("#edit-callback-div").hide();

    });
    AJS.$(document).on('click',"#cancel-callback-button",function(){
        AJS.$("#saved-callback-div").show();
        AJS.$("#edit-callback-div").hide();
    });

    AJS.$(document).on('keypress', '#login-form-email', function (e) {
        if (e.which === 13) {
            if (AJS.$("#domain-mapping-form").length > 0) {
                processDomainAndRedirect();
            } else if (AJS.$("#backdoor-restriction-form").length > 0) {
                checkForBackdoorAccess();
            }
            e.preventDefault();
        }
    });

    

    AJS.$(document).on('click', "#check_backdoor_access", function (e) {
        e.preventDefault();
        checkForBackdoorAccess();
    });

    if (AJS.$("#restrictBackdoor").is(":checked")) {
        AJS.$("#backdoorAccessGroupsList").show();
    } else {
        AJS.$("#backdoorAccessGroupsList").hide();
    }

    AJS.$("#restrictBackdoor").change(function () {
        if (this.checked) {
            AJS.$("#backdoorAccessGroupsList").show();
        } else {
            AJS.$("#backdoorAccessGroupsList").hide();
        }
    });

    /*Start*/
    
    function createUserSession() {
        console.log("VALIDATESESSIONCOOKIE", ReadCookie("VALIDATESESSIONCOOKIE"));

        if (ReadCookie("VALIDATESESSIONCOOKIE") != "") {
            console.log("VALIDATESESSIONCOOKIE set...");
            var osDestination = getQueryParameterByName("os_destination");
            var OAuthUrl= AJS.contextPath() + '/plugins/servlet/oauth/sessiontimeout';
            
            console.log("osDestination : ", osDestination);
                    
            if(!(!osDestination || /^\s*$/.test(osDestination)) && osDestination != "/" &&osDestination) {
                 if(!(osDestination.indexOf("/") == 0)){
                    osDestination = "/"+osDestination;
                 }
                 OAuthUrl += "?osDestination=" + encodeURIComponent(osDestination);
            } else {
                 console.log("url : ", url);
                 OAuthUrl += "?osDestination=" + encodeURIComponent(url);
            }
                   
           deleteCookie("VALIDATESESSIONCOOKIE");
           console.log("OAuthUrl = ", OAuthUrl);
           window.location.href = OAuthUrl;
           
        }
        
    }               
    
    function checkSessionCookie() {
        console.log("inside checkSessionCookie()..");
        if (ReadCookie("SESSIONCOOKIE") == "" && ReadCookie("LOGOUTCOOKIE") != "" && pathname.indexOf("login.action") < 0) {
            console.log("SESSIONCOOKIE : ", ReadCookie("SESSIONCOOKIE"));
            console.log("LOGOUTCOOKIE : ", ReadCookie("LOGOUTCOOKIE"));
            console.log("index : ", pathname.indexOf("login.action") < 0);
            createUserSession();
        }
    }   
    
    console.log("make a call to checkSessionCookie()...");
    checkSessionCookie();

    AJS.$("#timeout_extend_steps_link").click(function () {
        if (AJS.$("#extendtimeout").css('display') == 'none') {
            AJS.$("#extendtimeout").show();
        } else {
            AJS.$("#extendtimeout").hide();
        }
    });
    
    /*End*/

    function backdoorParametersSubmitted(response) {
    	console.log("inside backdoorParametersSubmitted");
        var backdoorValue = getQueryParameterByName(response.backdoorKey);
        console.log("backdoorValue : ", backdoorValue);
        if (backdoorValue && backdoorValue === response.backdoorValue) {
            return true;
        }
        return false;
    }

    function processDomainAndRedirect() {
        var loginForm;

        var username = AJS.$("#login-form-email").val();
        AJS.$.ajax({
            url: AJS.contextPath() + "/plugins/servlet/oauth/getconfig",
            data: {
                //"action":"checkDomainMapping",
                "username": username
            },
            type: "GET",
            error: function () {
            },
            success: function (response) {
                if (response.idp) {
                    redirectToIDP(loginForm);
                } else {
                    autoRedirectToIdP = shouldAutoRedirect(response);
                    //if (autoRedirectToIdP == true) {
                        notFoundHtml = '<div id="idp_not_found_error" class=\"aui-message aui-message-error closeable\"><p>Sorry. We couldn\'t find your IdP. Please contact your administrator </p></div>';
                        if (!AJS.$("#idp_not_found_error").length)
                            AJS.$(notFoundHtml).insertBefore(AJS.$("#domain-mapping-form"));
                }
            }
        });
    }

    function getCurrentUserName() {
        console.log("logged in user = " + AJS.params.remoteUser );
        return AJS.params.remoteUser;
    }

    function checkForBackdoorAccess() {
        var loginForm = AJS.$('.aui.login-form-container');

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
            },
            success: function (response) {
                if (response.isUserAllowedBackdoorAccess === true) {
                    AJS.$("#login-form-email").val(username);
                    AJS.$("#backdoor_restriction_note").remove();
                    AJS.$("#backdoor-restriction-form").remove();
                    if (AJS.$(".compact-form-fields").length > 0) {
                        AJS.$(".compact-form-fields").show();
                        autoFill(username);
                    }
                } else {
                    noAccessHtml = '<div id="no_backdoor_access_error" class=\"aui-message aui-message-error closeable\"><p><span>Sorry. You don\'t have access to this page. Please wait while we redirect you for authentication...</span><aui-spinner size="medium"></aui-spinner></p></div>';
                    AJS.$("#backdoor_restriction_note").remove();
                    AJS.$("#backdoor-restriction-form").hide();
                    if (!AJS.$("#no_backdoor_access_error").length)
                        AJS.$(noAccessHtml).insertBefore(AJS.$("#backdoor-restriction-form"));
                    setTimeout(function () {
                        window.onbeforeunload = null;
                        window.location.href = AJS.contextPath() + '/login.action';
                    }, 2000);
                }
            }
        });
    }

    function autoFill(username){
        AJS.$("#os_username").add(username);
        AJS.$("#os_username").val(username);
    }

    function showBackdoorForm(loginFormID) {
        AJS.$("#idpSelect").remove();
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

        AJS.$(".aui.login-form-container").hide();
        if (AJS.$(".compact-form-fields").length > 0) {
            AJS.$(".compact-form-fields").hide();
            AJS.$(html).appendTo(AJS.$(".aui.login-form-container"));
        }
        AJS.$(".aui.login-form-container").show();
        showOAuthErrorMessage(AJS.$("#backdoor-restriction-form"));
    }

     function showOAuthErrorMessage(loginForm) {
        var oautherror = getQueryParameterByName('oautherror');
        if (oautherror != null) {
            var htmlerror = '<div id="sso_failed_error" class="aui-message aui-message-error closeable"><p>We couldn\'t sign you in. Please contact your Administrator. </p></div>';
            AJS.$(htmlerror).insertBefore(loginForm);

        }
    }


	function validateEmail($email) {
		var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
		return emailReg.test( $email );
	}

	AJS.$("#useSeparateNameAttributes").change(function(){
		if(this.checked) {
			AJS.$("#fullNameAttributeDiv").hide();
			AJS.$("#separateNameAttributes").show();
		} else {
			AJS.$("#fullNameAttributeDiv").show();
			AJS.$("#separateNameAttributes").hide();
		}
	});
	
	if(AJS.$("#useSeparateNameAttributes").is(":checked")) {
		AJS.$("#fullNameAttributeDiv").hide();
		AJS.$("#separateNameAttributes").show();
	} else {
		AJS.$("#fullNameAttributeDiv").show();
		AJS.$("#separateNameAttributes").hide();
	}

	AJS.$(document).on('click',"#save-groupregex-button",function(){
        AJS.$("#role_mapping_submitted").val(true);
        AJS.$("#role-mapping-form").submit();
    });
	
	AJS.$("#test-oauth-configuration").click(function(){
	   var osDestination = "testoauthconfiguration";
	   var OAuthUrl = AJS.contextPath() + '/plugins/servlet/oauth/auth';
	   OAuthUrl += "?return_to=" + osDestination;
	   var myWindow = window.open(OAuthUrl, "TEST OAuth Login", "scrollbars=1 width=800, height=600");
	});
	
	AJS.$("#verify-credentials").click(function(){
       var osDestination = "verifycredentials";
       var OAuthUrl = AJS.contextPath() + '/plugins/servlet/oauth/auth';
       OAuthUrl += "?return_to=" + osDestination;
       var myWindow = window.open(OAuthUrl, "Verify Credentials", "scrollbars=1 width=800, height=600");
    })
    
    var appName = $('#appName').find(":selected").text();
    if ($.trim(appName) === "ADFS" || $.trim(appName) === "AWS Cognito" || $.trim(appName) === "OKTA"
            || $.trim(appName) === "Keycloak" || $.trim(appName) === "Azure B2C" || $.trim(appName) === "OpenID") {
        AJS.$("#validateSignatureMethodDiv").show();
        validateSignatureMethodDisplay();
    }
    else{
        AJS.$("#publicKeyDiv").hide();
        AJS.$("#JWKSEndpointURLDiv").hide();
        AJS.$("#validateSignatureMethodDiv").hide();
    }


	if ($.trim(appName) === "OpenID") {
		AJS.$("#authorizeEndpoint").prop("required", true);
		AJS.$("#accessTokenEndpoint").prop("required", true);
        jQuery("#load_fetch_metadata_page").show();
		jQuery("#authorize_endpoint").show();
		jQuery("#access_token_endpoint").show();
		jQuery("#acr_value").show();
	}
    
    AJS.$("#appName").change(function() {
    	var appName = $('#appName').find(":selected").text();
    	if ($.trim(appName) === "ADFS" || $.trim(appName) === "AWS Cognito" || $.trim(appName) === "OKTA"
    	        || $.trim(appName) === "Keycloak" || $.trim(appName) === "Azure B2C" || $.trim(appName) === "OpenID" ) {
            AJS.$("#validateSignatureMethodDiv").show();
            validateSignatureMethodDisplay();
        }
        else{
            AJS.$("#publicKeyDiv").hide();
            AJS.$("#JWKSEndpointURLDiv").hide();
            AJS.$("#validateSignatureMethodDiv").hide();
        }

    	
    	if ($.trim(appName) === "OpenID") {
    		AJS.$("#authorizeEndpoint").prop("required", true);
    		AJS.$("#accessTokenEndpoint").prop("required", true);
            jQuery("#load_fetch_metadata_page").show();
    		jQuery("#authorize_endpoint").show();
    		jQuery("#access_token_endpoint").show();
    		jQuery("#acr_value").show();
        }
        if ($.trim(appName) != "Discord" && $.trim(appName) != "Facebook" && $.trim(appName) != "Meetup" && $.trim(appName) != "Slack" && $.trim(appName) != "GitHub Enterprise" && $.trim(appName) != "Gluu Server") {
	        jQuery("#appvideoguide").show();
	    }
	    else{
	        jQuery("#appvideoguide").hide();
	    }

	    if($.trim(appName) === "OKTA"){
	        AJS.$("#domainName").attr("placeholder", "Enter Domain Name e.g. https://test.okta.com");
	    }
	    else{
	        AJS.$("#domainName").attr("placeholder", "Enter Domain Name");
	    }
    }); 

    AJS.$("#validateSignatureMethod").change(function () {
        validateSignatureMethodDisplay();
    });

    AJS.$("#appsetupguide").click(function(){
		var appName = $('#appName').find(":selected").text();
		var OAuthUrl = AJS.contextPath() + '/plugins/servlet/oauth/downloadappguides';
		OAuthUrl += "?appName=" + appName;
		//window.location.href = OAuthUrl;
		window.open(OAuthUrl, '_blank');	
	});

	AJS.$("#appvideoguide").click(function(){
        var appName = $('#appName').find(":selected").text();
        var OAuthUrl = AJS.contextPath() + '/plugins/servlet/oauth/downloadappvideoguides';
        OAuthUrl += "?appName=" + appName;
        //window.location.href = OAuthUrl;
        window.open(OAuthUrl, '_blank');
    });

    AJS.$("#timeout_extend_steps_link").click(function () {
        if (AJS.$("#extendtimeout").css('display') == 'none') {
            AJS.$("#extendtimeout").css('display') == 'block';
        } else {
            AJS.$("#extendtimeout").css('display') == 'none';
        }
    });


    /* Regex Pattern */
    if (AJS.$("#regexPatternEnabled").is(":checked")) {
		AJS.$("#regexfield").show();
		AJS.$("#regexPattern").prop("required", true);
		AJS.$("#usernameAttribute").prop("required", true);
	} else {
		AJS.$("#regexfield").hide();
		AJS.$("#regexPattern").prop("required", false);
		AJS.$("#usernameAttribute").prop("required", false);
	}
    
    AJS.$("#regexPatternEnabled").change(function () {
		if (this.checked) {
			AJS.$("#regexfield").show();
			AJS.$("#regexPattern").prop("required", true);
			AJS.$("#usernameAttribute").prop("required", true);
		} else {
			AJS.$("#regexfield").hide();
			AJS.$("#regexPattern").prop("required", false);
			AJS.$("#usernameAttribute").prop("required", false);
		}
	});
    
    AJS.$("#test-regex").click(function () {
		var osDestination = "testregex";
		var OAuthUrl = AJS.contextPath() + '/plugins/servlet/oauth/auth';

		var regex = document.getElementById('regexPattern').value;
		OAuthUrl += "?return_to=" + encodeURIComponent(osDestination) + "&regexp=" + encodeURIComponent(regex);
		var testWindow = window.open(OAuthUrl, "", "width=600,height=400");
	});

    AJS.$("#checkGroupRegex").click(function () {
        var osDestination = "testregex";
        var OAuthUrl = AJS.contextPath() + '/plugins/servlet/oauth/auth';

        var regex = document.getElementById('onTheFlyFilterIDPGroupsKey').value;
        OAuthUrl += "?return_to=" + encodeURIComponent(osDestination) + "&regexp=" + encodeURIComponent(regex);
        var testWindow = window.open(OAuthUrl, "", "width=600,height=400");
    });
    
    /*Login using Email Warning Message */
	
	showEmailWarningMessage();
	
	function showEmailWarningMessage(){
		value = AJS.$("#loginUserAttribute").val();
		if(value!="email"){
			AJS.$("#warningforemail").hide();
		}else{
			AJS.$("#warningforemail").show();
		}
	}
	
	AJS.$("#loginUserAttribute").change(function(){
		value = AJS.$("#loginUserAttribute").val();
		if(value!="email"){
			AJS.$("#warningforemail").hide();
			AJS.$("#emailAttribute").prop("required", false);
		}else{
			AJS.$("#warningforemail").show();
			AJS.$("#emailAttribute").prop("required", true);
		}
	});
         AJS.$("#validateSignatureMethod").change(function () {
         if($("#validateSignatureMethodDiv").is(":hidden"))
         {
         AJS.$("#publicKeyDiv").hide();
          AJS.$("#JWKSEndpointURLDiv").hide();
          }
         else{
              console.log($("#validateSignatureMethodDiv").is(":hidden"))  ;
             value = AJS.$("#validateSignatureMethod").val();
             if (value == "JWKSEndPointURLSelect") {
                 AJS.$("#publicKeyDiv").hide();
                 AJS.$("#JWKSEndpointURLDiv").show();
             } else {
                 AJS.$("#publicKeyDiv").show();
                 AJS.$("#JWKSEndpointURLDiv").hide();
             }
         }
        });
	
	/*Enable Custom Logout Template*/
	
	toggleAutoRedirectVisibility();
	 
	AJS.$("#disableDefaultLogin").change(function () {
		AJS.$("#enableBackdoor").toggle(this.checked);
		toggleAutoRedirectVisibility();
	});

    function validateSignatureMethodDisplay(){
         value = AJS.$("#validateSignatureMethod").val();
         if (value == "JWKSEndPointURLSelect") {
             AJS.$("#publicKeyDiv").hide();
             AJS.$("#JWKSEndpointURLDiv").show();
         } else {
             AJS.$("#publicKeyDiv").show();
             AJS.$("#JWKSEndpointURLDiv").hide();
         }
    }

	function toggleAutoRedirectVisibility(){
		if(AJS.$("#disableDefaultLogin").is(":checked")) {
            AJS.$("#backdoor-url").show("slow");
            AJS.$("#auto-redirect-delay").show("slow");
            if(AJS.$("#enableBackdoor").is(":checked")){
                AJS.$("#enable-backdoor-using-api").hide();
                AJS.$("#restrict-backdoor-div").show("slow");
                if(AJS.$("#restrictBackdoor").is(":checked")){
                AJS.$("#backdoorAccessGroupsList").show("slow");
                }
            } else{
                AJS.$("#enable-backdoor-using-api").show("slow");
                AJS.$("#restrict-backdoor-div").prop("checked", false);
                AJS.$("#restrict-backdoor-div").hide("slow");
                AJS.$("#backdoorAccessGroupsList").hide("slow");
            }
        } else {
            AJS.$("#backdoor-url").hide("slow");
            AJS.$("#auto-redirect-delay").hide("slow");
            AJS.$("#enableAutoRedirectDelay").prop("checked", false);
            AJS.$("#enable-backdoor-using-api").hide();
            AJS.$("#restrict-backdoor-div").prop("checked", false);
            AJS.$("#restrict-backdoor-div").hide("slow"); 
            AJS.$("#backdoorAccessGroupsList").hide("slow");
        }
	}

	/*START: Show/Hide backdoor enable API information  */
    if (AJS.$("#enableBackdoor").is(":checked")) {
        AJS.$("#enable-backdoor-using-api").hide();
        AJS.$("#backdoor-url-warning").hide();
        AJS.$("#restrict-backdoor-div").show();
        AJS.$("#disable-anonymous-access").show();
        if (AJS.$("#disableAnonymousAccess").is(":checked")) {
            AJS.$("#guest-session-div").show();
            AJS.$("#guest-Session-Timeout").show();
        }
        else{
            AJS.$("#guest-session-div").hide();
            AJS.$("#guest-Session-Timeout").hide();
        }
    } else {
        AJS.$("#enable-backdoor-using-api").show();
        AJS.$("#backdoor-url-warning").show();
        AJS.$("#restrict-backdoor-div").hide();
    }

    AJS.$("#enableBackdoor").change(function () {
        if (this.checked) {
            AJS.$("#enable-backdoor-using-api").hide();
            AJS.$("#backdoor-url-warning").hide();
            AJS.$("#restrict-backdoor-div").show();
            if (AJS.$("#restrictBackdoor").is(":checked")){
             AJS.$("#backdoorAccessGroupsList").show();
            }
        } else {
            AJS.$("#enable-backdoor-using-api").show();
            AJS.$("#backdoor-url-warning").show();
            AJS.$("#restrict-backdoor-div").hide();
            AJS.$("#backdoorAccessGroupsList").hide();
        }
    });

	AJS.$("#enableCheckIssuerFor").change(function(){
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

    AJS.$("#checkIssuerForCustom").change(function(){
        if(this.checked) {
            AJS.$("#customIssuerDiv").show();
            AJS.$("#customIssuerValue").prop("required", true);
        } else {
            AJS.$("#customIssuerDiv").hide();
            AJS.$("#customIssuerValue").prop("required", false);
        }
    });

    AJS.$("#checkIssuerForDefault").change(function(){
        if(this.checked) {
            AJS.$("#customIssuerDiv").hide();
            AJS.$("#customIssuerValue").prop("required", false);
        } else {
            AJS.$("#customIssuerDiv").show();
            AJS.$("#customIssuerValue").prop("required", true);
        }
    });

    AJS.$("#onTheFlyFilterIDPGroupsOption").change(function(){
        if(AJS.$("#onTheFlyFilterIDPGroupsOption").val() == "None"){
            AJS.$("#onTheFlyFilterIDPGroupsKey").hide();
        }else{
            AJS.$("#onTheFlyFilterIDPGroupsKey").show();

            if(AJS.$("#onTheFlyFilterIDPGroupsOption").val() == "Regex"){
                AJS.$("#checkGroupRegex").show();
                AJS.$("#onTheFlyGroupRegexDescription").show();

                if(AJS.$("#onTheFlyFilterIDPGroupsKey").val() == ""){
                    AJS.$("#checkGroupRegex").prop("disabled", true);
                }else{
                    AJS.$("#checkGroupRegex").prop("disabled", false);
                }
            }else{
                AJS.$("#checkGroupRegex").hide();
                AJS.$("#onTheFlyGroupRegexDescription").hide();
            }
        }
    });

    AJS.$(document).on('keyup',"#onTheFlyFilterIDPGroupsKey",function(){
        if(AJS.$("#onTheFlyFilterIDPGroupsKey").val() == ""){
            AJS.$("#checkGroupRegex").prop("disabled", true);
        }else{
            AJS.$("#checkGroupRegex").prop("disabled", false);
        }
    });


	AJS.$("#sign-in-settings").click(function () {
		var x = AJS.$("#sign-in-settings-div");
		if (x.css('display') == 'none') {
			x.show();
			AJS.$("#sign-in-settings").text("1. Hide Sign In Settings");
			setCookie('mo.confluence-oauth.sign-in-settings', true);
		} else {
			x.hide();
			AJS.$("#sign-in-settings").text("1. Show Sign In Settings");
			setCookie('mo.confluence-oauth.sign-in-settings', false);
		}
	});

	if (ReadCookie("mo.confluence-oauth.sign-in-settings") == "false") {
		AJS.$("#sign-in-settings-div").hide();
		AJS.$("#sign-in-settings").text("1. Show Sign In Settings");
	} else {
		AJS.$("#sign-in-settings-div").show();
		AJS.$("#sign-in-settings").text("1. Hide Sign In Settings");
	}

    AJS.$("#login-template-settings").click(function () {
        var x = AJS.$("#login-template-settings-div");
        if (x.css('display') == 'none') {
            x.show();
            AJS.$("#login-template-settings").text("2. Hide Custom Login Template");
            setCookie('mo.confluence-oauth.login-template-settings', true);
        } else {
            x.hide();
            AJS.$("#login-template-settings").text("2. Show Custom Login Template");
            setCookie('mo.confluence-oauth.login-template-settings', false);
        }
    });

    if (ReadCookie("mo.confluence-oauth.login-template-settings") == "false") {
        AJS.$("#login-template-settings-div").hide();
        AJS.$("#login-template-settings").text("2. Show Custom Login Template");
    } else {
        AJS.$("#login-template-settings-div").show();
        AJS.$("#login-template-settings").text("2. Hide Custom Login Template");
    }

	AJS.$("#sign-out-settings").click(function () {
		var x = AJS.$("#sign-out-settings-div");
		if (x.css('display') == 'none') {
			x.show();
			AJS.$("#sign-out-settings").text("3. Hide Sign Out Settings");
			setCookie('mo.confluence-oauth.sign-out-settings', true);
		} else {
			x.hide();
			AJS.$("#sign-out-settings").text("3. Show Sign Out Settings");
			setCookie('mo.confluence-oauth.sign-out-settings', false);
		}
	});

	if (ReadCookie("mo.confluence-oauth.sign-out-settings") == "false") {
		AJS.$("#sign-out-settings-div").hide();
		AJS.$("#sign-out-settings").text("3. Show Sign Out Settings");
	} else {
		AJS.$("#sign-out-settings-div").show();
		AJS.$("#sign-out-settings").text("3. Hide Sign Out Settings");
	}

	AJS.$("#sso-error-settings").click(function () {
		var x = AJS.$("#sso-error-settings-div");
		if (x.css('display') == 'none') {
			x.show();
			AJS.$("#sso-error-settings").text("4. Hide SSO Error Settings");
			setCookie('mo.confluence-oauth.sso-error-settings', true);
		} else {
			x.hide();
			AJS.$("#sso-error-settings").text("4. Show SSO Error Settings");
			setCookie('mo.confluence-oauth.sso-error-settings', false);
		}
	});

	if (ReadCookie("mo.confluence-oauth.sso-error-settings") == "false") {
		AJS.$("#sso-error-settings-div").hide();
		AJS.$("#sso-error-settings").text("4. Show SSO Error Settings");
	} else {
		AJS.$("#sso-error-settings-div").show();
		AJS.$("#sso-error-settings").text("4. Hide SSO Error Settings");
	}

	if (ReadCookie("mo.confluence-oauth.show-url-details") == "true") {
        AJS.$("#backdoorUrlDiv").show();
        AJS.$("#show-url-details").text("Hide Backdoor URL");
    } else {
        AJS.$("#backdoorUrlDiv").hide();
        AJS.$("#show-url-details").text("Show Backdoor URL");
    }

	AJS.$("#advanced-sso-settings").click(function () {
    		var x = AJS.$("#advanced-sso-settings-div");
    		if (x.css('display') == 'none') {
    			x.show();
    			AJS.$("#advanced-sso-settings").text("5. Hide Advanced SSO Settings");
    			setCookie('advanced-sso-settings', true);
    		} else {
    			x.hide();
    			AJS.$("#advanced-sso-settings").text("5. Show Advanced SSO Settings");
    			setCookie('advanced-sso-settings', false);
    		}
    	});

    if (ReadCookie("advanced-sso-settings") == "false") {
    	AJS.$("#advanced-sso-settings-div").hide();
    	AJS.$("#advanced-sso-settings").text("5. Show Advanced SSO Settings");
    } else {
    	AJS.$("#advanced-sso-settings-div").show();
    	AJS.$("#advanced-sso-settings").text("5. Hide Advance SSO Settings");
    }


    AJS.$("#show-url-details").click(function () {
        var x = AJS.$("#backdoorUrlDiv");
        if (x.css('display') == 'none') {
            x.show();
            AJS.$("#show-url-details").text("Hide Backdoor URL");
            setCookie('mo.confluence-oauth.show-url-details', true);
        } else {
            x.hide();
            AJS.$("#show-url-details").text("Show Backdoor URL");
            setCookie('mo.confluence-oauth.show-url-details', false);
        }
    });
	
	AJS.$("#enablelogoutTemplate").change(function(){
		if(this.checked) {
			AJS.$("#customLogoutURL").prop("disabled", true);
			AJS.$("#custom_logout_template").show();
		} else {
			AJS.$("#customLogoutURL").prop("disabled", false);
			AJS.$("#custom_logout_template").hide();
		}
	});

	if(AJS.$("#enablelogoutTemplate").is(":checked")) {
		AJS.$("#customLogoutURL").prop("disabled", true);
		AJS.$("#custom_logout_template").show();
	} else {
		AJS.$("#customLogoutURL").prop("disabled", false);
		AJS.$("#custom_logout_template").hide();
	}

	AJS.$("#checkIDPSession").change(function () {
        if(this.checked){
            AJS.$("#auto-login-callbackURL").show();
        }else{
            AJS.$("#auto-login-callbackURL").hide();
        }
    });

    if(AJS.$("#checkIDPSession").is(":checked")) {
        AJS.$("#auto-login-callbackURL").show();
    } else {
       AJS.$("#auto-login-callbackURL").hide();
    }
	
	AJS.$("#disableDefaultLogin").change(function () {
		if(this.checked){
			AJS.$("#enableBackdoor").prop("checked",true);
		}else{
			AJS.$("#enableBackdoor").prop("checked",false);
		}
		toggleAutoRedirectVisibility();
    });

    AJS.$("#disableAnonymousAccess").change(function () {
    		if(this.checked){
    			AJS.$("#guest-Session-Timeout").show();
    		}else{
    			AJS.$("#guest-Session-Timeout").hide();
    		}
        });

    if(pathname.indexOf("authenticate.action") > 0){
        if(ReadCookie("mo.confluence-oauth.LOGOUTCOOKIE")!=""){
            AJS.$.ajax({
                url: AJS.contextPath() + "/plugins/servlet/oauth/getconfig",
                type: "GET",
                error: function() {},
                success: function(response) {
                    var loginForm = AJS.$(".aui.login-form-container");
                    if(response.disableDefaultLogin==true)
                    {
                        if (response.enableAutoRedirectDelay === true) {
                            console.log("enableAutoRedirectDelay : ", response.enableAutoRedirectDelay);
                            insertDelay(loginForm, response);
                            loginForm.hidden = false;
                            return;
                        }
                        redirectToIDP(loginForm);
                    }
                    else{
                        showLoginButton(AJS.$(".aui.login-form-container"),response);
                    }
                }
            });
        }
    }


	function renderLoginPage() {
            count++;
            if (AJS.$(".compact-form-fields").length > 0) {
                loadLoginForm(AJS.$(".aui.login-form-container"));
            } else if (AJS.$(".aui.login-form-container").length > 0) {
                loadLoginForm(AJS.$(".aui.login-form-container"));
            } else {
                var url = window.location.href;
                if ((url.match(/login.action/i)) || (url.match(/index.action/i)) && count <= 50) {
                    setTimeout(renderLoginPage, 100);
                }
            }

            if (window.location.href.match(/\?logout=true/i)) {
                if (ReadCookie("mo.confluence-oauth.LOGOUTCOOKIE") != "") {
                    deleteCookie("mo.confluence-oauth.LOGOUTCOOKIE");
                    return;
                }
            }

    }
	
	function insertDelay(loginForm, response) {
		/** The following code has been copied from showLoginButton function below  */
         var totalAutoRedirectDelayInterval = parseInt(response.autoRedirectDelayInterval) * 5;
		 var osDestination = getQueryParameterByName("os_destination");
	     var destination = getQueryParameterByName("destination");
		 var OAuthUrl = AJS.contextPath() + '/plugins/servlet/oauth/auth';

		 if(!(!osDestination || /^\s*$/.test(osDestination)) && osDestination != "/" &&osDestination) {
	         if(!(osDestination.indexOf("/") == 0)){
	            osDestination = "/"+osDestination;
	         }
	         OAuthUrl += "?return_to=" + encodeURIComponent(osDestination);
	      }else if(!(!destination || /^\s*$/.test(destination)) && destination != "/" && destination){
	         if(!(destination.indexOf("/") == 0)){
	            destination = "/"+destination;
	         }
	         OAuthUrl += "?return_to=" + encodeURIComponent(destination);
	      }
		 
		 /** The Text to show once user has been redirected */
	      var textOnCancel = '<a href="'+OAuthUrl+'" id="redirectMessage" class="aui-button aui-button-link">'
	                  + response.loginButtonText + '</a>';

	      /** Create the delay box on the login page */
	      var html = '<div id="moRedirectBox" class="aui-message aui-message-info">'
	                +'<p>'
	                    +'<span id="defaultText">Redirecting to IDP.</span>'
	                    +'<span id="textOnCancel" style="display:none;">'+textOnCancel+'</span>'
	                    +'<button id="stopAutoRedirect" class="aui-button aui-button-primary" style="float:right;"'
	                    + 'resolved="" onClick="stopAutoRedirect()">Cancel</button>'
	                +'</p>'
	                +'<input type="hidden" id="OAuthUrl" value="'+ OAuthUrl +'"></input>'
	                +'<div id="moRedirectProgress" class="aui-progress-indicator" style="margin-top: 1.5em;'
	                +'background: #e9e9e9;border-radius: 3px;height: 5px;overflow: hidden;position: relative;'
	                +'width: 100%;" data-value="0.01">'
	                    +'<span class="aui-progress-indicator-value" style="position: absolute;display: block;'
	                    +'height: 5px;animation: progressSlide 1s infinite linear;animation-play-state: running;'
	                    +'-webkit-animation-play-state: paused;animation-play-state: paused;background: #3572b0;'
	                    +'border-radius: 3px 0 0 3px;transition: width .5s;-webkit-transform: skewX(0);'
	                    +'transform: skewX(0);width:0%"></span>'
	                +'</div>'
	            +'</div>'
	            +'<script>'
	                +'var delayFunc;'
	                +'var progress = 0.005;'
	                /**
	                 * Function is used to stop the Auto redirect and show a link to redirect the user
	                 * to the IDP for authentication.
	                 */
	                 +'function stopAutoRedirect(){'
	                    +'clearTimeout(delayFunc);'
	                    +'AJS.$("#moRedirectProgress").remove();'
	                    +'AJS.$("#defaultText").remove();'
	                    +'AJS.$("#stopAutoRedirect").remove();'
	                    +'AJS.$("#textOnCancel").css("display","block");'
	                 +'}'
	                 /**
	                  * Function is used to update the progressBar added to the login page if admin
	                  * has set a delay for the auto-redirect. If the progress bar reaches 100% then
	                  * redirect the user to the IDP.
	                  */
	                 +'function updateProgressBar(){'
	                       +'if(progress<0.995){'
	                           +'progress = progress+0.005;'
	                           +'var percentage = progress*100+"%";'
	                           + 'AJS.$("#moRedirectProgress").attr("data-value",progress);'
	                           + 'AJS.$(".aui-progress-indicator-value").css("width",percentage);'
	                           +'delayFunc = setTimeout(function(){ updateProgressBar(); } , '+totalAutoRedirectDelayInterval+' );'
	                       +'}else{'
	                           + 'var url = AJS.$("#OAuthUrl").val();'
	                           +'window.location.href = url;'
	                       +'}'
	                  +'}'
	                  +'updateProgressBar();'
	            +'</script>';
	      
		 /** Inserting the div after the Default Login Form */
	      AJS.$("#gadget-0").css("height","355px");
	      AJS.$(html).insertAfter(AJS.$(".aui.login-form-container"));
	}

    AJS.$(document).on('click', '#fetch', function () {
//        console.log("Inside fetchMetadata function");
        var metadata_url = AJS.$("#metadata_url").val();
        if (Boolean(metadata_url)){
            AJS.$.ajax({
                url: AJS.contextPath() + "/plugins/servlet/oauth/fetchmetadata",
                type: "GET",
                data: {
                    "metadata_url": metadata_url
                },
                success: function (response) {
                    if(response.authorization_endpoint == null){
                        require('aui/flag')({
                            title: 'An error occurred',
                            type: 'error',
                            close: 'auto',
                            body: '<p> Unable to find Authorize Endpoint. Please enter a valid URL. </p>'
                        });
                    } else if(response.token_endpoint == null){
                        require('aui/flag')({
                            title: 'An error occurred',
                            type: 'error',
                            close: 'auto',
                            body: '<p> Unable to find Access Token Endpoint. Please enter a valid URL. </p>'
                        });
                    } else{

                        var appName = $('#appName').find(":selected").text();
                        if ($.trim(appName) === "ADFS" || $.trim(appName) === "AWS Cognito" || $.trim(appName) === "OKTA"
                                || $.trim(appName) === "Keycloak" || $.trim(appName) === "Azure B2C" || $.trim(appName) === "OpenID") {
                            if(response.jwks_uri != null){
                                AJS.$("#validateSignatureMethod").val("JWKSEndPointURLSelect");
                                AJS.$("#publicKeyDiv").hide();
                                AJS.$("#JWKSEndpointURLDiv").show();
                                AJS.$("#jWKSEndpointURL").val(response.jwks_uri);
                            }
                        }
                    
                        AJS.$("#authorizeEndpoint").val(response.authorization_endpoint);
                        AJS.$("#accessTokenEndpoint").val(response.token_endpoint);
                        if(($.trim(appName) === "Custom App" || $.trim(appName) === "miniOrange") && (response.userinfo_endpoint != null)){
                            AJS.$("#userInfoEndpoint").val(response.userinfo_endpoint);
                        }
                        var scopes_supported = response.scopes_supported;
                        var scope = "";
                        var x;
                        for(x in scopes_supported){
                            if(scope == ""){
                                scope = scopes_supported[x];
                            }else{
                                scope = scope + " " + scopes_supported[x];
                            }
                        }
                        AJS.$("#scope").val(scope);
                        AJS.$("#fetch_metadata_page").hide();
                        require('aui/flag')({
                            title: 'Metadata imported successfully',
                            type: 'success',
                            close: 'auto'
                        });
                    }
                },
                error: function () {
                    require('aui/flag')({
                        title: 'An error occurred',
                        type: 'error',
                        close: 'auto',
                        body: '<p> Please enter a valid URL. </p>'
                    });
                }
            });
        } else{
            require('aui/flag')({
                title: 'An error occurred',
                type: 'error',
                close: 'auto',
                body: '<p> Please enter a valid URL. </p>'
            });
        }
    });

    AJS.$(document).on('click', '#load_fetch_metadata_page', function () {
        AJS.$("#fetch_metadata_page").show();
    });

    AJS.$(document).on('click', '#close', function () {
        AJS.$("#error").hide();
        AJS.$("#fetch_metadata_page").hide();
    });

    function checkForCallbackSubmitted() {
        var callbackParam = AJS.$("#customCallbackParameter").val(); 
        if(callbackParam.charAt(0) == "/")
            callbackParam=callbackParam.substring(1);  
        AJS.$.ajax({
            url: AJS.contextPath() + "/plugins/servlet/oauth/moapi",
            type: "POST",
            data: {
                "action": "updateCallbackParameter",
                "callbackParam" : callbackParam,
            },
            success: function () {
                var defaultURL=document.getElementById("default-callback-url").innerText;
                if(callbackParam != "")
                    callbackParam="/"+callbackParam;
                AJS.$("#display-callback-div").html(defaultURL+callbackParam);
                require('aui/flag')({
                    title: 'Callback URL updated successfully',
                    type: 'success',
                    close: 'auto',
                });
            },
            error: function () {
                require('aui/flag')({
                    title: 'Error while updating Callback URL',
                    type: 'error',
                    close: 'auto',
                });
            }
        });
    }

	function loadLoginForm(loginForm) {
		console.log("loadLoginForm");
        var formName = AJS.$(".aui.login-form-container").attr("name");
        var cookieName = "mo.confluence-oauth.FORM_COOKIE";
        var cookieValue = formName;
        document.cookie = cookieName+"="+cookieValue ;

        if(loginForm.length > 0) {
            var id = loginForm.id;
            AJS.$.ajax({
                url: AJS.contextPath() + "/plugins/servlet/oauth/getconfig",
                type: "GET",
                error: function() {},
                success: function(response) {
                    if (response.enableOAuthSSO === true && response.configured === true) {
                    	console.log("plugin configured");
                    	loginForm.hidden = false;
                    	console.log("response.disableDefaultLogin : ", response.disableDefaultLogin);
                    	console.log("response.backdoorEnabled : ", response.backdoorEnabled);
                    	//console.log("backdoorParametersSubmitted(response) : ", backdoorParametersSubmitted(response));
                    	console.log("response.restrictBackdoor : ", response.restrictBackdoor);
                    	if (response.disableDefaultLogin && response.backdoorEnabled && backdoorParametersSubmitted(response) && response.restrictBackdoor) {
                    		console.log("calling showBackdoorForm()");
                            
                            showBackdoorForm(loginForm);
                            //AJS.$(".aui.login-form-container").show();
                            return;
                        }else if(response.disableDefaultLogin && response.backdoorEnabled && backdoorParametersSubmitted(response)){
                        	console.log("show login form");
                            AJS.$(".aui.login-form-container").show();
                            return;
                        }

                        var autoRedirectToIdP = shouldAutoRedirect(response);
                        console.log("autoRedirectToIdP : ",autoRedirectToIdP);
                        if (pathname.indexOf("authenticate.action") > 0) {
                            OAuthProviderName = ReadCookie("mo.confluence-oauth.LOGOUTCOOKIE");
                            console.log("OAuthProviderName : ",OAuthProviderName)
                            if (!OAuthProviderName) {
                                AJS.$(".aui.login-form-container").show();
                                return;
                            }
                        }
                        
//                        if(response.loginButtonText!="") {
//                        	showLoginButton(loginForm, response);
//                        }	
                   
                        if (autoRedirectToIdP == true) {
                        	console.log("autoRedirectToIdP : ", autoRedirectToIdP);
                        	if (response.enableAutoRedirectDelay === true) {
                        		console.log("enableAutoRedirectDelay : ", response.enableAutoRedirectDelay);
								insertDelay(loginForm, response);
								loginForm.hidden = false;
								return;
							}
                        	redirectToIDP(loginForm);
                        } else {
                            if (response.enableLoginTemplate == true) {
                                if (getQueryParameterByName('show_login_form') != "true") {
                                    AJS.$('body').hide();
                                    var html = '<div>' + response.loginTemplate + '</div>';
                                    AJS.$("body").html(html);
                                    AJS.$('body').show();
                                    return;
                                }
                            }
                        	if(response.loginButtonText!="" && getQueryParameterByName('show_login_form') != "true"){
                            	//console.log("calling showLoginButton...");
                                showLoginButton(loginForm, response);
                            }
                        }

                        console.log("response.userSessionDetails : ", response.userSessionDetails);
                        
                        if (response.userSessionDetails > 0) {
                            console.log("userSessionDetails set");
                            checkSessionCookie();
                        }

                        return;
                    }
                }
            });
        }
    }
	
	function redirectToIDP(loginForm) {
        var html = '<div class="aui-message aui-message-info"><span>Please wait while we redirect you for authentication...</span></div>';
        loginForm.innerHTML = html;
        var oauthUrl = AJS.contextPath() + '/plugins/servlet/oauth/auth';
        var osDestination = getQueryParameterByName("os_destination");
        var destination = getQueryParameterByName("destination");
        if(!(!osDestination || /^\s*$/.test(osDestination)) && osDestination != "/" &&osDestination) {
            if(!(osDestination.indexOf("/") == 0)){
                osDestination = "/"+osDestination;
            }
            oauthUrl += "?return_to=" + encodeURIComponent(osDestination);
        }else if(!(!destination || /^\s*$/.test(destination)) && destination != "/" && destination){
            if(!(destination.indexOf("/") == 0)){
                destination = "/"+destination;
            }
            oauthUrl += "?return_to=" + encodeURIComponent(destination);
        } 
        
        window.location.href = oauthUrl;
    }

    function shouldAutoRedirect(response) {
        if (!(response.backdoorEnabled === true && getQueryParameterByName("oauth_sso") == "false")) {
            if (response.disableDefaultLogin == true) {
                return true;
            }
        }
        return false;
    }

	function showLoginButton(loginForm, response) {
        var osDestination = getQueryParameterByName("os_destination");
		var destination = getQueryParameterByName("destination");
		if(!(!osDestination || /^\s*$/.test(osDestination)) && osDestination != "/") {
		    if (response.ssoButtonLocation == 'Before Login Button') {
			    AJS.$('<a class="aui-button aui-style aui-button-primary" id="use_idp_button_js" href="plugins/servlet/oauth/auth?return_to=' + encodeURIComponent(osDestination) +'" style="align:center;">'+ response.loginButtonText +'</a>').insertBefore(AJS.$("#loginButton"));
            } else {
			    AJS.$('<a class="aui-button aui-style aui-button-primary" id="use_idp_button_js" href="plugins/servlet/oauth/auth?return_to=' + encodeURIComponent(osDestination) +'" style="align:center;">'+ response.loginButtonText +'</a>').insertAfter(AJS.$("#loginButton"));
            }

            console.log(response);

            if(response.checkIDPSession){
                AJS.$('<iframe src="' + response.autoLoginURL +'" id="autoIFrame" hidden></iframe>').insertAfter(AJS.$("#use_idp_button_js"));
            }
		}
		else {
			var login_html = '<a class="aui-button aui-style aui-button-primary" id="use_idp_button_js" href="plugins/servlet/oauth/auth" style="align:center;">'+ response.loginButtonText +'</a>';
	  		var authenticate_html = '<a class="aui-button aui-style aui-button-primary" id="use_idp_button_js" href="plugins/servlet/oauth/auth?return_to=' + encodeURIComponent(destination) +'" style="align:center;">'+ response.loginButtonText +'</a>';
			if(AJS.$("#loginButton")){
				AJS.$("#loginButton").addClass("mobutton-margin");
				if (response.ssoButtonLocation == 'Before Login Button') {
				    AJS.$(login_html).insertBefore(AJS.$("#loginButton"));
				} else {
				    AJS.$(login_html).insertAfter(AJS.$("#loginButton"));
				}
			}
			if(AJS.$("#authenticateButton")){
				AJS.$("#authenticateButton").addClass("mobutton-margin");
				if (response.ssoButtonLocation == 'Before Login Button') {
				    AJS.$(authenticate_html).insertBefore(AJS.$("#authenticateButton"));
				} else {
				    AJS.$(authenticate_html).insertAfter(AJS.$("#authenticateButton"));
				}
			}

			if(response.checkIDPSession){
                AJS.$('<iframe src="' + response.autoLoginURL +'" id="autoIFrame" hidden></iframe>').insertAfter(AJS.$("#use_idp_button_js"));
            }
		}
		var oautherror=getQueryParameterByName('oautherror');
        if(oautherror!=null){
            var htmlerror='<div class="aui-message aui-message-error closeable"><p>We couldn\'t sign you in. Please contact your Administrator. </p></div>';
            AJS.$(htmlerror).insertBefore(AJS.$(".aui.login-form-container"));
        }
         
    }

   function getQueryParameterByName(name) {
		name = name.replace(/[\[\]]/g, "\\$&");
		var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
			results = regex.exec(url);
		if (!results) return null;
		if (!results[2]) return '';
		return decodeURIComponent(results[2].replace(/\+/g, " "));
	}
   
   function ReadCookie(cname) {
		//alert(cname);
		var name = cname + "=";
		var decodedCookie = decodeURIComponent(document.cookie);
		var ca = decodedCookie.split(';');
		for (var i = 0; i < ca.length; i++) {
			var c = ca[i];
			while (c.charAt(0) == ' ') {
				c = c.substring(1);
			}
			if (c.indexOf(name) == 0) {
				//alert(" found");
				return c.substring(name.length, c.length);
			}
		}
		//alert("not found");
		return "";
	}

	function deleteCookie(name) {
		document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:01 GMT; path=/;';
	};

    function toggleOnTheFlyGroupMapping() {
        var groupMappingClass = AJS.$('#group-mapping-pill').attr("class");
        if (groupMappingClass == "active") {
            AJS.$("#onthefly-group-mapping-instructions-div").hide();
            AJS.$("#onTheFlyGroupMappingDiv").hide();
            AJS.$("#onTheFlyAssignNewGroupsOnlyDiv").hide();
            AJS.$("#onTheFlyCreateNewGroupsDiv").hide();
            AJS.$("#on-the-fly-group-mapping-main-inst").hide();
            //AJS.$("#onTheFlyDoNotRemoveGroupsDiv").hide();
            AJS.$("#groupMappingDiv").show();
            AJS.$("#group-mapping-main-inst").show()
            AJS.$("#roleAttribute").prop("required", false);
            AJS.$("#onTheFlyGroupCreation").val(false);
        } else {
            if (AJS.$("#onTheFlyAssignNewGroupsOnly").is(":checked")){
                AJS.$("#onTheFlyDoNotRemoveGroupsDiv").hide();
            } else{
                AJS.$("#onTheFlyDoNotRemoveGroupsDiv").show();
            }
            AJS.$("#groupMappingDiv").hide();
            AJS.$("#group-mapping-instructions-div").hide();
            AJS.$("#group-mapping-main-inst").hide();
            AJS.$("#onTheFlyAssignNewGroupsOnlyDiv").show();
            AJS.$("#onTheFlyCreateNewGroupsDiv").show();
            AJS.$("#onTheFlyGroupMappingDiv").show();
            AJS.$("#on-the-fly-group-mapping-main-inst").show();
            AJS.$("#roleAttribute").prop("required", true);
            AJS.$("#onTheFlyGroupCreation").val(true);
        }
    }
});


function copy(that, message) {
    var inp = document.createElement('input');
    document.body.appendChild(inp);
    inp.value = that.textContent;
    inp.select();
    document.execCommand('copy', false);
    inp.remove();
    AJS.$(message).show();
    setTimeout(function() { AJS.$(message).hide("slow"); }, 5000);
}