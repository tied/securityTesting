(function ($) {
    var count = 0;
    var numberOfLoginAttempts=null;
    var currentUrl = $(location).attr('href');

    if(currentUrl.match(/start.action/i)){
        updateLoginAttempts("deleteLoginAttempts");
    }

    if (currentUrl.match(/userlogin/i) || currentUrl.match(/start.action/i)) {
       /* if(ReadCookie("LOGOUTCOOKIE") != ""){
            window.location.href = AJS.contextPath()+"/";
            return;
        }*/
        renderLoginPage();
    }

    var adfs = {
        label: "Enter AD FS hostname:",
        description: "<p>Enter your AD FS hostname here. For example, <i>adfs.yourdomain.com</i></p>",
    };

    var azureAD = {
        label: "Enter Azure AD domain name:",
        description: "<p>Enter your Azure AD domain name here. For example, <i>contoso.onmicrosoft.com</i></p>"
    };

    var oneLogin = {
        label: "Enter OneLogin metadata url:",
        description: "Open your SP app on OneLogin. Proceed to app settings -> More Actions -> Right click on SAML Metadata -> Copy Link Address"
    };

    var fromUrl = {
        label: "Enter IDP Metadata URL:",
        description: "This url is used to fetch your idp settings. Please make sure that url is accessible. Reach out to us using <b>Support/Feedback</b> widget if you need any help"
    };

    var okta = {
        label: "Upload Okta Metadata File:",
        description: "Open your application on Okta, Go to <b>Sign on</b> tab Click on <b>Identity Provider metadata</b> to download metadata file"

    };

    var google = {
        label: "Upload Google G suite Metadata File:",
        description: "While creating a saml app on Google, Click on download button beside <b>IDP metadata</b> in step 2 <b>Google IDP Information</b>"
    };

    var fromFile = {
        label: "Upload IDP Metadata File:",
        description: "This file is used to fetch your idp settings. Reach out to us using <b>Support/Feedback</b> widget  if you need any help"
    }

    /**
     * map is used to store the idp objects with the option value. This map is used in changeFieldValues() and changeFileFieldValues() functions
     */
    var map = {};
    map['ADFS'] = adfs;
    map['Azure AD'] = azureAD;
    map['OneLogin'] = oneLogin;
    map['fromUrl'] = fromUrl;
    map['Okta'] = okta;
    map['Google'] = google;
    map['fromFile'] = fromFile;

    showImportFromMetadata();

    AJS.$(document).on('change', '#refreshMetadata', function () {
        if (this.checked) {
            AJS.$("#refreshInterval").prop("disabled", false);
            if (AJS.$("#refreshInterval").val() == "custom") {
                AJS.$("#customRefreshValue").show();
                AJS.$("#customRefreshInterval").prop("required", true);
            } else {
                AJS.$("#customRefreshValue").hide();
                AJS.$("#customRefreshInterval").prop("required", false);
            }
        } else {
            AJS.$("#refreshInterval").prop("disabled", true);
            AJS.$("#customRefreshValue").hide();
            AJS.$("#customRefreshInterval").prop("required", false);
        }
    });

    AJS.$(document).on('change', '#refreshInterval', function () {
        var value = AJS.$("#refreshInterval").val();
        if (value == "custom") {
            AJS.$("#customRefreshValue").show();
            AJS.$("#customRefreshInterval").prop("required", true);
        } else {
            AJS.$("#customRefreshValue").hide();
            AJS.$("#customRefreshInterval").prop("required", false);
        }
    });

    AJS.$("#test-saml-configuration").click(function () {
        var next = "testidpconfiguration"
        var samlAuthUrl = AJS.contextPath()
            + '/plugins/servlet/saml/auth';
        samlAuthUrl += "?return_to=" + next;
        var myWindow = window.open(samlAuthUrl, "TEST SAML IDP",
            "scrollbars=1 width=800, height=600");
    });

    AJS.$(document).on('change', '#metadataOption', function () {
        showImportFromMetadata();

    });

    AJS.$(document).on('click', '#importFromMetadataButton', function () {
        showImportFromMetadata();
        periodicRefreshMetadataSettings();
    });

    AJS.$(document).on('click', '#test-regex', function () {
        var osDestination = "testregex";
        var samlAuthUrl = AJS.contextPath() + '/plugins/servlet/saml/auth';
        var regex = document.getElementById('regexPattern').value;
        
        samlAuthUrl += "?return_to=" + encodeURIComponent(osDestination) + "&regexp=" + encodeURIComponent(regex);
        var testWindow = window.open(samlAuthUrl, "", "width=600,height=400");
    });

    disablePasswordChange();

    AJS.$(document).on('keyup', '#regexfield', function (e) {
        setTimeout(function enable() {
            if (AJS.$("#regexPattern").val() != "") {
                AJS.$("#test-regex").prop("disabled", false);
            } else {
                AJS.$("#test-regex").prop("disabled", true);
            }
        }, 500);
    });

    AJS.$(document).on('click', '#feedback_button', function () {
        var pos = AJS.$(".feedback_panel").position();
        AJS.$("#email_error").hide();
        if (pos.left < 1280) {
            AJS.$("#feedback_email").css("border-width", "1px");
            AJS.$("#feedback_content").css("border-color", "#a79898");
            AJS.$("#reason").css("border-color", "#a79898");
            AJS.$("#feedback_error").hide();
            AJS.$("#feedback_success").hide();
            AJS.$(".feedback_panel").animate({right: -335}, 700);
            AJS.$(".feedback_float").animate({right: -30}, 700);
            AJS.$("#reason").val("Select Reason");
            AJS.$("#feedback_content").val("");
            AJS.$("#feedback_email").val("");
        } else {
            AJS.$("#feedback_email").css("border-width", "1px");
            AJS.$("#feedback_content").css("border-color", "#a79898");
            AJS.$("#reason").css("border-color", "#a79898");
            AJS.$("#feedback_error").hide();
            AJS.$("#feedback_success").hide();
            AJS.$("#feedback_pending").hide();
            AJS.$("#feedback_form").show();
            AJS.$(".feedback_panel").animate({right: 0}, 700);
            AJS.$(".feedback_float").animate({right: 300}, 700);
            AJS.$("#reason").val("Select Reason");
            AJS.$("#feedback_content").val("");
            AJS.$("#feedback_email").val("");
        }
    });

    AJS.$(document).on('click', '#cancel-feedback', function () {
        AJS.$("#email_error").hide();
        AJS.$("#email_error").toggle(400);
        AJS.$("#feedback_email").css("border-width", "1px");
        AJS.$("#feedback_content").css("border-color", "#a79898");
        AJS.$("#reason").css("border-color", "#a79898");
        AJS.$("#feedback_error").hide();
        AJS.$("#feedback_success").hide();
        AJS.$(".feedback_panel").animate({right: -335}, 700);
        AJS.$(".feedback_float").animate({right: -30}, 700);
        AJS.$("#reason").val("Select Reason");
        AJS.$("#feedback_content").val("");
        AJS.$("#feedback_email").val("");
    });

    AJS.$(document).on('click', '#send-feedback', function () {
        AJS.$("#email_error").prop("hidden", true);
        if (AJS.$("#feedback_content").val() !== "") {
            AJS.$("#feedback_content").css("border-color", "#a79898");
            if (AJS.$("#reason").val() !== "Select Reason") {
                AJS.$("#reason").css("border-color", "#a79898");
                if (AJS.$("#can_contact").is(":checked")) {
                    if ((AJS.$("#feedback_email").val() !== "") && validateEmail(AJS.$("#feedback_email").val())) {
                        AJS.$("#feedback_email").css("border-color", "#a79898");
                        send_feedback();
                    } else {
                        AJS.$("#feedback_email").css("border-width", "1px");
                        AJS.$("#feedback_email").css("border-color", "#d51212");
                        AJS.$("#email_error").toggle(400);
                    }
                } else if (AJS.$("#feedback_email").val() !== "" && !validateEmail(AJS.$("#feedback_email").val())) {
                    AJS.$("#feedback_email").css("border-width", "1px");
                    AJS.$("#feedback_email").css("border-color", "#d51212");
                    AJS.$("#email_error").toggle(400);
                } else {
                    AJS.$("#feedback_email").css("border-color", "#a79898");
                    send_feedback();
                }
            } else {
                AJS.$("#reason").css("border-color", "#d51212");
            }
        } else {
            AJS.$("#feedback_content").css("border-color", "#d51212");
        }
    });


    /*Warning for API Restriction*/
    if (AJS.$("#pluginApiAccessRestriction").is(":checked")) {
        AJS.$("#warningforapirestriction").show();
    } else {
        AJS.$("#warningforapirestriction").hide();
    }

    AJS.$("#pluginApiAccessRestriction").change(function () {
        if (this.checked) {
            AJS.$("#warningforapirestriction").show();
        } else {
            AJS.$("#warningforapirestriction").hide();
        }
    });

    function send_feedback() {
        AJS.$("#feedback_form").hide();
        AJS.$("#email_error").hide();
        AJS.$("#feedback_pending").show();
        jQuery.ajax({
            url: AJS.contextPath() + "/plugins/servlet/saml/sendfeedback",
            type: "POST",
            data: $("#feedback_form").serialize(),
            success: function () {
                AJS.$("#feedback_error").hide();
                AJS.$("#feedback_pending").hide();
                AJS.$("#feedback_form").hide();
                AJS.$("#feedback_success").show();
                setTimeout(function () {
                    AJS.$(".feedback_panel").animate({right: -335}, 700);
                    AJS.$(".feedback_float").animate({right: -30}, 700);
                    AJS.$("#reason").val("Select Reason");
                    AJS.$("#feedback_content").val("");
                    AJS.$("#feedback_email").val("");
                }, 1500);
            },
            error: function () {
                AJS.$("#feedback_error").show();
            }
        });
    }

    function validateEmail($email) {
        var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
        return emailReg.test($email);
    }

    AJS.$(document).on('click', '#submit_metadata', function () {
        AJS.$("#metadata_success").remove();
            jQuery.ajax({
                url: AJS.contextPath() + "/plugins/servlet/saml/updatemetadata",
                type: "POST",
                data: $("#set-certificates-form").serialize(),
                success: function () {
                    var html = "<div id=\"metadata_success\" class=\"aui-message aui-message-success\">Settings\n" +
                        "Updated\n"+
                        "</div>";
                    AJS.$(html).insertBefore(AJS.$("#set-certificates-form"));
                },
            });
    });

    function periodicRefreshMetadataSettings() {
        if (AJS.$("#refreshMetadata").is(":checked")) {
            AJS.$("#refreshInterval").prop("disabled", false);
            if (AJS.$("#refreshInterval").val() == "custom") {
                AJS.$("#customRefreshValue").show();
                AJS.$("#customRefreshInterval").prop("required", true);
            } else {
                AJS.$("#customRefreshValue").hide();
                AJS.$("#customRefreshInterval").prop("required", false);
            }
        } else {
            AJS.$("#refreshInterval").prop("disabled", true);
            AJS.$("#customRefreshValue").hide();
            AJS.$("#customRefreshInterval").prop("required", false);
        }
    }

    function hideAllForms() {
        AJS.$("#importByUrl").hide();
        AJS.$("#importByFile").hide();
        AJS.$("#importButtons").hide();
    }

    function showUrlForm() {
        AJS.$("#importByUrl").show();
        AJS.$("#importByFile").hide();
        AJS.$("#importButtons").show();
        AJS.$("#metadataUrl").prop("required", true);
        AJS.$("#xmlFile").prop("required", false);
        AJS.$("#fetchMetadataFile").val("false");
        AJS.$("#fetchMetadataUrl").val("true");

        document.getElementById('effective_metadata_url').innerHTML = "";
    }

    function showFileForm() {
        AJS.$("#importByFile").show();
        AJS.$("#importByUrl").hide();
        AJS.$("#importButtons").show();
        AJS.$("#metadataUrl").prop("required", false);
        AJS.$("#xmlFile").prop("required", true);
        AJS.$("#fetchMetadataFile").val("true");
        AJS.$("#fetchMetadataUrl").val("false");

    }

    AJS.$(document).on('keyup keypress blur change', '#metadataUrl', function () {
        value = document.getElementById('metadataUrl').value;
        metadataOption = AJS.$("#metadataOption").val();
        if (value != null) {
            if (value.replace(/\s/g, '').length) {
                AJS.$("#effective_metadata_url_div").show();
            } else {
                AJS.$("#effective_metadata_url_div").hide();
            }

            if (value.indexOf("https:\/\/") >= 0 || value.indexOf("http:\/\/") >= 0) {
                metadataOption = "fromUrl";
            }

            switch (metadataOption) {
                case "ADFS":
                    document.getElementById('effective_metadata_url').innerHTML = "https://" + value + "/federationmetadata/2007-06/federationmetadata.xml";
                    break;
                case "Azure AD":
                    document.getElementById('effective_metadata_url').innerHTML = "https://login.microsoftonline.com/" + value + "/FederationMetadata/2007-06/FederationMetadata.xml";
                    break;
                default:
                    document.getElementById('effective_metadata_url').innerHTML = value;
            }
        }

    });

    function changeFileFieldValues(idp) {
        var idpObj = map[idp];

        document.getElementById("metadata_file_label").innerHTML = idpObj.label;
        document.getElementById("metadata_file_description").innerHTML = idpObj.description;
    }

    function changeFieldValues(idp, type) {
        var idpObj = map[idp];

        document.getElementById("metadata_url_label").innerHTML = idpObj.label;

        document.getElementById("metadata_url_description").innerHTML = idpObj.description;

        var metadataUrl = document.getElementById("metadataUrl");

        var hostname = document.createElement('input');
        hostname.id = metadataUrl.id;
        hostname.name = metadataUrl.name;
        hostname.style = metadataUrl.style;
        hostname.className = "text long-field";
        hostname.placeholder = idpObj.label;
        hostname.type = type;
        hostname.value = metadataUrl.value;
        metadataUrl.parentNode.replaceChild(hostname, metadataUrl);

    }

    function showImportFromMetadata() {
        value = AJS.$("#metadataOption").val();
        if (value) {
            switch (value) {
                case "":
                    hideAllForms();
                    break;
                case "ADFS":
                    changeFieldValues('ADFS', 'text');
                    showUrlForm();
                    break;
                case "Azure AD":
                    changeFieldValues('Azure AD', 'text');
                    showUrlForm();
                    break;
                case "OneLogin":
                    changeFieldValues('OneLogin', 'url');
                    showUrlForm();
                    break;
                case "fromUrl":
                    changeFieldValues('fromUrl', 'url');
                    showUrlForm();
                    break;
                case "Okta":
                    changeFileFieldValues('Okta');
                    showFileForm();
                    break;
                case "Google G Suite":
                    changeFileFieldValues('Google');
                    showFileForm();
                    break;
                case "fromFile":
                    changeFileFieldValues('fromFile');
                    showFileForm();
                    break;
            }
        }
    }


    AJS.$("#timeout_extend_steps_link").click(function () {
        if (AJS.$("#extendtimeout").css('display') == 'none') {
            AJS.$("#extendtimeout").show();
        } else {
            AJS.$("#extendtimeout").hide();
        }
    });
    /*Create user if role mapped */
    AJS.$(document).on('change', '#createUsersIfRoleMapped', function () {
        if (this.checked) {
            AJS.$("#defaultGroup").prop("disabled", true);
        } else {
            AJS.$("#defaultGroup").prop("disabled", false);
        }
    });

    AJS.$(document).on('change', '#idpSelect', function () {
        AJS.$("#sso_failed_error").remove();
        var selectedIdP = AJS.$("#idpSelect").val();
        redirectToIDP(selectedIdP);
    });

    /*Restrict new user creation*/
    AJS.$(document).on('change', '#restrictUserCreation', function () {
        if (this.checked) {
            AJS.$("#defaultGroup").prop("disabled", true);
            AJS.$("#createUsersIfRoleMapped").prop("disabled", true);
        } else {
            AJS.$("#defaultGroup").prop("disabled", false);
            AJS.$("#createUsersIfRoleMapped").prop("disabled", false);
        }
    });

    AJS.$(document).on('change', '#amIdpName', function () {
        idp_name = AJS.$("#amIdpName").val();
        AJS.$("#changedIdpName").val(idp_name);
        AJS.$("#attribute-mapping-idp-select-form").submit();
    });

    AJS.$(document).on('change', '#gmIdpName', function () {
        idp_name = AJS.$("#gmIdpName").val();
        AJS.$("#changedIdpName").val(idp_name);
        AJS.$("#group-mapping-idp-select-form").submit();
    });

    AJS.$(document).on('click', '#use_idp_button', function () {
        var loginForm = document.getElementById("loginForm");
        AJS.$("#" + loginForm.id + "").hide();
        AJS.$("#sso_failed_error").remove();
        AJS.$("#sso_failed_error").remove();
        showDomainMappingForm(true, false);
    });

    AJS.$(document).on('click', "#domain-mapping-login", function (e) {
        processDomainAndRedirect();
        e.preventDefault();
    });

    AJS.$(document).on('click', "#check_backdoor_access", function (e) {
        checkForBackdoorAccess();
        e.preventDefault();
    });

    AJS.$(document).on('keypress', '#login-form-email', function (e) {
        if (e.which == 13) {
            if (AJS.$("#domain-mapping-form").length > 0) {
                processDomainAndRedirect();
            } else if (AJS.$("#backdoor-restriction-form").length > 0) {
                checkForBackdoorAccess();
            }
            e.preventDefault();
        }
    });

    /*Check Regex Pattern */
    AJS.$(document).on('change', '#regexPatternEnabled', function () {
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


    /*Check for the custom logout */

    /*Check Regex Pattern */
    AJS.$(document).on('change', '#enablelogoutTemplate', function () {
        if (this.checked) {
            AJS.$("#custom_logout_template").show();
        } else {
            AJS.$("#custom_logout_template").hide();
        }
    });

    if (AJS.$("#enablelogoutTemplate").is(":checked")) {
        AJS.$("#logoutTemplate").show();
    } else {
        AJS.$("#logoutTemplate").hide();
    }

    AJS.$(document).on('change', '#onTheFlyGroupCreation', function () {
        if (this.checked) {
            AJS.$("#groupMappingDiv").hide();
            AJS.$("#onTheFlyGroupMappingDiv").show();
        } else {
            AJS.$("#groupMappingDiv").show();
            AJS.$("#onTheFlyGroupMappingDiv").hide();
        }
    });

    /*Separate Name Attribute */
    AJS.$(document).on('change', '#useSeparateNameAttributes', function () {
        if (this.checked) {
            AJS.$("#fullNameAttribute").prop("disabled", true);
            AJS.$("#firstNameAttribute").prop("disabled", false);
            AJS.$("#lastNameAttribute").prop("disabled", false);
        } else {
            AJS.$("#fullNameAttribute").prop("disabled", false);
            AJS.$("#firstNameAttribute").prop("disabled", true);
            AJS.$("#lastNameAttribute").prop("disabled", true);
        }
    });

    AJS.$(document).on('change', '#restrictBackdoor', function () {
        if (this.checked) {
            AJS.$("#backdoorAccessGroupsList").show();
        } else {
            AJS.$("#backdoorAccessGroupsList").hide();
        }
    });

    /*On chage of login user attribute */
    AJS.$(document).on('change', '#loginUserAttribute', function () {
        value = AJS.$("#loginUserAttribute").val();
        if (value != "email") {
            AJS.$("#warningforemail").hide();
        } else {
            AJS.$("#warningforemail").show();
        }
    });

    AJS.$(document).on('change', '#onTheFlyAssignNewGroupsOnly', function () {
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

    AJS.$(document).on('click', "#back-to-login-form", function () {
        AJS.$("#domain_mapping_note").remove();
        AJS.$("#domain-mapping-form").remove();
        AJS.$("#idp_not_found_error").remove();
        AJS.$("#sso_failed_error").remove();
        var loginForm = $("#loginForm")
        loginForm.show();
    });

    AJS.$(document).on('click','#personalDetails',function(){
        hideChangePasswordLink(0);
    });

    function toggleOnTheFlyGroupMapping() {
        var groupMappingClass = AJS.$('#group-mapping-pill').attr("class");
        if (groupMappingClass == "active") {
            AJS.$("#onthefly-group-mapping-instructions-div").hide();
            AJS.$("#onTheFlyGroupMappingDiv").hide();
            AJS.$("#onTheFlyCreateNewGroupsDiv").hide();
            AJS.$("#onTheFlyAssignNewGroupsOnlyDiv").hide();
            AJS.$("#on-the-fly-group-mapping-main-inst").hide();
            AJS.$("#groupMappingDiv").show();
            AJS.$("#group-mapping-main-inst").show();

            AJS.$("#roleAttribute").prop("required", false);
            AJS.$("#onTheFlyGroupCreation").val("false");
        } else {
            AJS.$("#groupMappingDiv").hide();
            AJS.$("#group-mapping-instructions-div").hide();
            AJS.$("#group-mapping-main-inst").hide();
            AJS.$("#onTheFlyCreateNewGroupsDiv").show();
            AJS.$("#onTheFlyAssignNewGroupsOnlyDiv").show();
            AJS.$("#on-the-fly-group-mapping-main-inst").show();

            if (AJS.$("#onTheFlyAssignNewGroupsOnly").is(":checked")) {
                AJS.$("#onTheFlyGroupMappingDiv").hide();
            } else {
                AJS.$("#onTheFlyGroupMappingDiv").show();
            }

            AJS.$("#roleAttribute").prop("required", true);
            AJS.$("#onTheFlyGroupCreation").val("true");
        }
    }

    /*Header based authentication*/
    toggleHeaderAuthentication();

    function toggleHeaderAuthentication(){
        if (AJS.$("#headerAuthentication").is(":checked")) {
            AJS.$("#headerAuthenticationAttributeDiv").show();
            AJS.$("#headerAuthenticationAttribute").prop("required", true);
        } else {
            AJS.$("#headerAuthenticationAttributeDiv").hide();
            AJS.$("#headerAuthenticationAttribute").prop("required", false);
        }
    }

    AJS.$(document).on('change', '#headerAuthentication', function () {
        toggleHeaderAuthentication();
    });

    function renderLoginPage() {
        count++;
        var os_destination = getQueryParameterByName("os_destination");
        
        if (ReadCookie("PERFORM_LOGOUT") == "PerformLogout" && ReadCookie("LOGOUTCOOKIE") != "") {
            $.ajax({
                url: AJS.contextPath()
                    + "/plugins/servlet/saml/getconfig",
                type: "GET",
                async: false,
                error: function () {
                },
                success: function (response) {
                    if (response.configured === true) {
//                        if (response.sloConfigured === true || response.customLogoutUrlConfigured || response.customLogoutTemplateEnabled) {
                            window.location.href = AJS.contextPath()
                                + "/plugins/servlet/saml/logout";
                            setTimeout(test, 5000);
                            return;
//                        }
                    }
                }
            });

        } else {
            test();
        }
    }

    function test() {
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

    function updateLoginAttempts(setOrDelete){
        AJS.$.ajax({
            async:false,
            url: AJS.contextPath() + "/plugins/servlet/saml/moapi",
            data: {
                "action": setOrDelete
            },
            type: "GET",
            error: function () {
            },
            success: function (response) { 
                numberOfLoginAttempts= response.numberOfAttempts;
            }
        });
        return numberOfLoginAttempts;
    }


    function loadLoginForm(loginForm) {
        if (loginForm.length > 0) {
            loginForm.hide();
            $.ajax({
                url: AJS.contextPath()
                    + "/plugins/servlet/saml/getconfig",
                type: "GET",
                error: function () {
                },
                success: function (response) {
                    
                    if (response.configured === true) {
                        if (response.idpList.length >= 1) {
                            firstIdpName = response.idpList[0];
                        } else {
                            firstIdpName = 'miniorange.saml.DEFAULT_IDP_ID';
                        }

                        if (response.disableDefaultLogin == true) {
                            if (response.backdoorEnabled == true) {
                                var samlSso = backdoorParametersSubmitted(response);
                                if (samlSso == true) {
                                    if(updateLoginAttempts("getLoginAttempts")===undefined){
                                        if (response.restrictBackdoor == true) {
                                            showBackdoorForm();
                                            updateLoginAttempts("setLoginAttempts");
                                            return;
                                        } else {
                                            updateLoginAttempts("setLoginAttempts");
//                                        showLoginButton(response, false, false);
                                            $("#loginForm")
                                            .append(
                                                '<input type="hidden" name="' + response.backdoorKey + '" value="' + response.backdoorValue + '" />');
                                        loginForm.show();
                                        return;
                                        }
                                    }
                                }
                                var maxAttempts= parseInt(response.numberOfLoginAttempts);
                                numberOfLoginAttempts = updateLoginAttempts("getLoginAttempts");
                                if(numberOfLoginAttempts!=null || numberOfLoginAttempts!=undefined){    
                                if(parseInt(numberOfLoginAttempts)<maxAttempts){
                                    numberOfLoginAttempts=updateLoginAttempts("setLoginAttempts");
                                    loginForm.show();
                                    return;
                                }else{
                                    if(parseInt(numberOfLoginAttempts)==maxAttempts){
                                    updateLoginAttempts("setLoginAttempts");
                                    }
                                    loginForm.hide();
                                    var loginAttemptMessage = '<div class="aui-message aui-message-error"><span>Login Attempts Exhausted!</span></div><br>';
                                    AJS.$(loginAttemptMessage).insertBefore(AJS.$("#loginForm"));
                                }
                            }
                            }
                        }
                        var autoRedirectToIDP = shouldAutoRedirect(response);
                        if (autoRedirectToIDP == true) {
                            if (response.idpList.length > 1) {
                                if (response.useDomainMapping == true) {
                                    showDomainMappingForm(false, true);
                                } else {
                                    showSeparateMultipleIdpList(response);
                                }
                                return;
                            } else {
                                if (response.enableAutoRedirectDelay === true) {
                                    insertDelay(response, loginForm);
                                    if(updateLoginAttempts("getLoginAttempts")==null || updateLoginAttempts("getLoginAttempts")===undefined)
                                    loginForm.show();
                                    else loginForm.hide();
                                    return;
                                }
                                redirectToIDP(firstIdpName);
                                if(updateLoginAttempts("getLoginAttempts")==null || updateLoginAttempts("getLoginAttempts")===undefined)
                                    loginForm.show();
                                    else loginForm.hide();
                                return;
                            }
                        }

                        if (response.useDomainMapping == true) {
                            showDomainMappingForm(true, true);
                            showLoginButton(response, false, true);
                            return;
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
                            showLoginButton(response, false, false);
                        }
                    }
                    loginForm.show();
                }
            });
        }
    }

    function showSeparateMultipleIdpList(response) {
        var loginForm = document.getElementById("loginForm");
        AJS.$("#" + loginForm.id + "").hide();

        dropdownHtml = "";
        if (response.idpList.length > 1) {
            dropdownHtml = '<div id="idp_mapping_note" class="aui-message aui-message-info"><span>Please select you application from the list</span></div>' +
                '<form id="idpListDropdown" name="idpListDropdown" action="" class="aui"><div class="buttons-container"><div class="buttons">' +
                '<select class="aui-button aui-style aui-button-primary" id="idpSelect">' +
                '<option selected disabled hidden>Choose IdP to Login</option>';
            for (i = 0; i < response.idpList.length; i++) {
                idpID = response.idpList[i];
                idpName = response.idpMap[idpID];
                dropdownHtml += '<option class="aui-button aui-style aui-button-primary" value=' + idpID + '>' + idpName + '</option>';
            }
            dropdownHtml += '</select></div></div></form>';
        }
        AJS.$(dropdownHtml).insertAfter(loginForm);

        var samlerror = getQueryParameterByName('samlerror');
        if (samlerror != null) {
            var htmlerror = '<div id="sso_failed_error" class="aui-message aui-message-error closeable"><p>We couldn\'t sign you in. Please contact your Administrator. </p></div>';
            AJS.$(htmlerror).insertBefore(AJS.$("#idp_mapping_note"));
        }

    }

    function showBackdoorForm(loginFormID) {
        AJS.$("#idpSelect").remove();
        AJS.$("#idp_not_found_error").remove();
        AJS.$("#sso_failed_error").remove();
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

        var loginForm = document.getElementById("loginForm");
        AJS.$("#" + loginForm.id + "").hide();
        AJS.$(".aui-page-panel-content").append(html);
        showSamlErrorMessage();
    }

    function backdoorParametersSubmitted(response) {
        var backdoorValue = getQueryParameterByName(response.backdoorKey);
        if (backdoorValue && backdoorValue === response.backdoorValue) {
            return true;
        }
        return false;
    }

    function insertDelay(response, loginForm) {
        var osDestination = getQueryParameterByName("os_destination");
        var destination = getQueryParameterByName("destination");
        var samlAuthUrl = AJS.contextPath() + '/plugins/servlet/saml/auth';
        if (!(!osDestination || /^\s*$/.test(osDestination)) && osDestination != "/" && osDestination) {
            if (!(osDestination.indexOf("/") == 0)) {
                osDestination = "/" + osDestination;
            }
            samlAuthUrl += "?return_to=" + encodeURIComponent(osDestination);
        } else if (!(!destination || /^\s*$/.test(destination)) && destination != "/" && destination) {
            if (!(destination.indexOf("/") == 0)) {
                destination = "/" + destination;
            }
            samlAuthUrl += "?return_to=" + encodeURIComponent(destination);
        }

        /** The Text to show once user has been redirected */
        var textOnCancel = '<a href="' + samlAuthUrl + '" id="redirectMessage" class="aui-button aui-button-link">'
            + response.loginButtonText + '</a>';

        /** Create the delay box on the login page */
        var html = '<div id="moRedirectBox" class="aui-message aui-message-info">'
            + '<p>'
            + '<span id="defaultText">Redirecting to IDP.</span>'
            + '<span id="textOnCancel" style="display:none;">' + textOnCancel + '</span>'
            + '<button id="stopAutoRedirect" class="aui-button aui-button-primary" style="float:right;"'
            + 'resolved="" onClick="stopAutoRedirect()">Cancel</button>'
            + '</p>'
            + '<input type="hidden" id="samlAuthUrl" value="' + samlAuthUrl + '"></input>'
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
             * Function is used to stop the Auto redirect and show a link to redirect the user
             * to the IDP for authentication.
             */
            + 'function stopAutoRedirect(){'
            + 'clearTimeout(delayFunc);'
            + 'AJS.$("#moRedirectProgress").remove();'
            + 'AJS.$("#defaultText").remove();'
            + 'AJS.$("#stopAutoRedirect").remove();'
            + 'AJS.$("#textOnCancel").css("display","block");'
            + '}'
            /**
             * Function is used to update the progressBar added to the login page if admin
             * has set a delay for the auto-redirect. If the progress bar reaches 100% then
             * redirect the user to the IDP.
             */
            + 'function updateProgressBar(){'
            + 'if(progress<0.995){'
            + 'progress = progress+0.005;'
            + 'var percentage = progress*100+"%";'
            + 'AJS.$("#moRedirectProgress").attr("data-value",progress);'
            + 'AJS.$(".aui-progress-indicator-value").css("width",percentage);'
            + 'delayFunc = setTimeout(function(){ updateProgressBar(); } , 25 );'
            + '}else{'
            + 'progress=0.005;'
            + 'var url = AJS.$("#samlAuthUrl").val();'
            + 'window.location.href = url;'
            + '}'
            + '}'
            + 'updateProgressBar();'
            + '</script>';
        /** Inserting the div after the Default Login Form */
        AJS.$("#gadget-0").css("height", "355px");
        AJS.$(html).insertAfter(loginForm);

    }

    function shouldAutoRedirect(response) {
        if (response.disableDefaultLogin == true) {
            return true;
        }
        return false;
    }

    function showLoginButton(response, shouldShowDropdown, isDomainMappingEnabled) {

        AJS.$("#use_idp_button").remove();
        var next = getQueryParameterByName("os_destination");
        var samlAuthUrl = AJS.contextPath() + '/plugins/servlet/saml/auth';
        if (!(!next || /^\s*$/.test(next)) && next != "/") {
            samlAuthUrl += "?return_to=" + encodeURIComponent(next);
        }

        var dropdownHtml = '';
        if (response.idpList.length >= 1) {
            firstIdpName = response.idpList[0];
        } else {
            firstIdpName = 'miniorange.saml.DEFAULT_IDP_ID';
        }

        if (response.useDomainMapping == true && shouldShowDropdown == false) {
            dropdownHtml = '<a id="use_idp_button" class="aui-button aui-style aui-button-primary" style="align:center;">' + response.loginButtonText + '</a>';
        } else if (response.idpList.length > 1) {
            dropdownHtml = '<select class="aui-button aui-style aui-button-primary" id="idpSelect">' +
                '<option selected disabled hidden>Choose IdP to Login</option>';
            for (i = 0; i < response.idpList.length; i++) {
                idpID = response.idpList[i];
                idpName = response.idpMap[idpID];
                dropdownHtml += '<option class="aui-button aui-style aui-button-primary" value=' + idpID + '>' + idpName + '</option>';
            }
            dropdownHtml += '</select>';
        } else {
            if (samlAuthUrl.indexOf("return_to") > 0) {
                dropdownHtml = '<a class="aui-button aui-style aui-button-primary" href="' + samlAuthUrl + '&idp=' + encodeURIComponent(firstIdpName) + '" style="align:center;">' + response.loginButtonText + '</a>';
            } else {
                dropdownHtml = '<a class="aui-button aui-style aui-button-primary" href="' + samlAuthUrl + '?idp=' + encodeURIComponent(firstIdpName) + '" style="align:center;">' + response.loginButtonText + '</a>';
            }
        }

        $(dropdownHtml).insertAfter($("#loginForm_save"));

        if (!isDomainMappingEnabled) {
            var samlerror = getQueryParameterByName('samlerror');
            
            if (samlerror != null) {
                var htmlerror = '<div id="sso_failed_error" class="aui-message aui-message-error closeable"><p>We couldn\'t sign you in. Please contact your Administrator. </p></div>';
                AJS.$(htmlerror).insertBefore(AJS.$("div[id='fieldArea_loginForm_os_username']"));
            }
        }
    }


    function getQueryParameterByName(name) {

        var url = window.location.href;
        name = name.replace(/[\[\]]/g, "\\$&");
        var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"), results = regex
            .exec(url);
        if (!results)
            return null;
        if (!results[2])
            return '';
        return decodeURIComponent(results[2].replace(/\+/g, " "));
    }

    function showDomainMappingForm(showBackToFormButton, shouldShowError) {
        var osDestination = getQueryParameterByName("os_destination");
        var samlAuthUrl = AJS.contextPath() + '/plugins/servlet/saml/auth';
        if (!(!osDestination || /^\s*$/.test(osDestination)) && osDestination != "/") {
            samlAuthUrl += "?return_to=" + encodeURIComponent(osDestination);
        }
        AJS.$("#idpSelect").remove();
        AJS.$("#idp_not_found_error").remove();
        AJS.$("#sso_failed_error").remove();
        var notFoundHtml = '<div id=\\"idp_not_found_error\\" class=\\"aui-message aui-message-error closeable\\"><p>Sorry. We couldn\'t find your IdP. Please login with Bamboo credentials or choose your IdP </p></div>';

        var html = '<div id="domain_mapping_note" class="aui-message aui-message-info">Please submit your email address to get redirected to IDP</div>' +
            '<form id="domain-mapping-form" method="post" action="" name="domain-mapping-form" class="aui gdt">' +
            '<div class="field-group">' +
            '<label accesskey="e" for="login-form-email" id="emaillabel" style="display: block;"><u>E</u>mail</label>' +
            '<input class="text medium-field" id="login-form-email" name="os_email" type="text"/>' +
            '</div>' +
            '<div class="field-group">' +
            '<button id="domain-mapping-login" class="aui aui-button aui-button-primary">Submit</button>' +
            '<a id="back-to-login-form" class="cancel">Go back to login form</a>' +
            '</div>' +
            '</form>';

        var loginForm = document.getElementById("loginForm");
        AJS.$("#" + loginForm.id + "").hide();
        AJS.$(".aui-page-panel-content").append(html);

        if (showBackToFormButton === false) {
            AJS.$("#back-to-login-form").remove();
        }

        if (shouldShowError) {
            showSamlErrorMessage();
        }
    }

    function showSamlErrorMessage() {
        var loginForm = document.getElementById("loginForm");
        var samlerror = getQueryParameterByName('samlerror');
        if (samlerror != null) {
            var htmlerror = '<div id="sso_failed_error" class="aui-message aui-message-error closeable"><p>We couldn\'t sign you in. Please contact your Administrator. </p></div>';
            AJS.$(htmlerror).insertBefore(AJS.$("#loginForm"));

        }
    }

    function redirectToIDP(idpName) {
        var loginForm = document.getElementById("loginForm");
        AJS.$("#" + loginForm.id + "").hide();

        var idpListDropdown = document.getElementById("idpListDropdown");
        if (idpListDropdown) {
            AJS.$("#" + idpListDropdown.id + "").remove();

        }
        AJS.$("#idp_mapping_note").remove();

        var html = '<div class="aui-message aui-message-info"><span>Please wait while we redirect you for authentication...</span></div>';
        AJS.$(".aui-page-panel-content").append(html);
        var osDestination = getQueryParameterByName("os_destination");
        var samlAuthUrl = AJS.contextPath() + '/plugins/servlet/saml/auth';
        if (!(!osDestination || /^\s*$/.test(osDestination)) && osDestination != "/") {
            samlAuthUrl += "?return_to=" + encodeURIComponent(osDestination);
        }
        if (samlAuthUrl.indexOf("return_to") > 0) {
            samlAuthUrl += "&idp=" + encodeURIComponent(idpName);
        } else {
            samlAuthUrl += "?idp=" + encodeURIComponent(idpName);
        }
        window.location.href = samlAuthUrl;
    }

    function processDomainAndRedirect() {
        var loginForm = document.getElementById("loginForm");

        var username = AJS.$("#login-form-email").val();
        AJS.$.ajax({
            url: AJS.contextPath() + "/plugins/servlet/saml/getconfig",
            data: {
                "username": username
            },
            type: "GET",
            error: function () {
                notFoundHtml = '<div id="idp_not_found_error" class=\"aui-message aui-message-error closeable\"><p>Sorry. We couldn\'t find your IdP. Please contact your administrator </p></div>';
                AJS.$("#login-form-email").val(username);
                AJS.$("#sso_failed_error").remove();
                if (!AJS.$("#idp_not_found_error").length)
                    AJS.$(notFoundHtml).insertBefore(AJS.$("#" + loginForm.id));
                showLoginButton(response, false, true);
            },
            success: function (response) {
                if (response.idp != "") {
                    AJS.$("#domain_mapping_note").remove();
                    AJS.$("#domain-mapping-form").remove();
                    AJS.$("#idp_not_found_error").remove();
                    AJS.$("#sso_failed_error").remove();
                    redirectToIDP(response.idp);
                } else {
                    autoRedirectToIdP = shouldAutoRedirect(response);
                    if (autoRedirectToIdP == true) {
                        notFoundHtml = '<div id="idp_not_found_error" class=\"aui-message aui-message-error closeable\"><p>Sorry. We couldn\'t find your IdP. Please contact your administrator </p></div>';
                        if (!AJS.$("#idp_not_found_error").length)
                            AJS.$(notFoundHtml).insertBefore(AJS.$("#domain-mapping-form"));
                    } else {
                        notFoundHtml = '<div id="idp_not_found_error" class=\"aui-message aui-message-error closeable\"><p>Sorry. We couldn\'t find your IdP. Please contact your administrator </p></div>';
                        AJS.$("#os_username").val(username);
                        AJS.$("#sso_failed_error").remove();
                        if (AJS.$("#idp_not_found_error").length <= 0) {
                            AJS.$(notFoundHtml).insertBefore(AJS.$("#" + loginForm.id));
                        }
                        showLoginButton(response, false, true);
                    }
                }
            }
        });
    }

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
            url: AJS.contextPath() + "/plugins/servlet/saml/moapi",
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
                
                if (response.isUserAllowedBackdoorAccess == true) {
                    AJS.$("#login-form-email").val(username);
                    AJS.$("#loginForm_os_username").val(username);
                    AJS.$("#backdoor_access_verification_error").remove();
                    AJS.$("#no_backdoor_access_error").remove();
                    AJS.$("#backdoor_restriction_note").remove();
                    AJS.$("#backdoor-restriction-form").remove();
//                    showLoginButton(response, true, false);
                    AJS.$("#" + loginForm.id + "").show();
                } else {
                    noAccessHtml = '<div id="no_backdoor_access_error" class=\"aui-message aui-message-error closeable\"><p><span>Sorry. You don\'t have access to this page. Please enter valid username for getting Login page.</span></p></div>';
                    if (!AJS.$("#no_backdoor_access_error").length)
                        AJS.$(noAccessHtml).insertBefore(AJS.$("#backdoor-restriction-form"));
                }
            }
        });
    }

    function ReadCookie(cname) {
		//alert(cname);
		var name = cname + "=";
		var decodedCookie = document.cookie;
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

    function hideChangePasswordLink(count) {
        if(AJS.$("a:contains('Change password')").length>0) {
           AJS.$("a:contains('Change password')").remove();
        } else if(count<50){
            count++;
            setTimeout(hideChangePasswordLink,100,count)
        }
    }

    function hideChangePasswordLinklogin(count) {
        if(document.querySelector('a[href="/forgotPassword.action"]')) {
            document.querySelector('a[href="/forgotPassword.action"]').remove();


        } else if (count < 50) {
            count++;
            setTimeout(hideChangePasswordLinklogin, 100, count)
        }

        }


    function disablePasswordChange(){
        var pathname=window.location.pathname;
        /*Check for the forget password change URL*/
        if(pathname.indexOf("/forgotPassword.action")!== -1){
                $.ajax({
                          url: AJS.contextPath()+
                          "/plugins/servlet/saml/getconfig",
                                type: "GET",
                                async: false,
                                error: function () {},
                                success: function (response) {
                                    if(response.enablePasswordChange===false) {
                                        window.location.href='/userlogin!doDefault.action';
                                    }
                                }
                       });
        }
        /*Check for the password change URL*/
        if(pathname.indexOf("/profile/changePassword.action")!== -1){
            $.ajax({
                      url: AJS.contextPath()+
                      "/plugins/servlet/saml/getconfig",
                            type: "GET",
                            async: false,
                            error: function () {},
                            success: function (response) {
                                if(response.enablePasswordChange===false) {
                                    window.location.href='/profile/userProfile.action';
                                }
                            }
                   });
        }
	    if((pathname.indexOf("/userlogin!doDefault.action")!== -1)||(pathname.indexOf("/profile/userProfile.action")!== -1)) {
	        $.ajax({
	            url: AJS.contextPath()
	                + "/plugins/servlet/saml/getconfig",
	            type: "GET",
	            async: false,
	            error: function () {},
	            success: function (response) {
	                if(response.enablePasswordChange===false) {
	                    var count = 0;
	                    hideChangePasswordLinklogin(count);
	                    hideChangePasswordLink(count);
	                }
	            }
	        });
	    }

    }


})(AJS.$ || jQuery);