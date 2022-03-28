
var numberOfLoginAttempts = null;
var currentUrl = AJS.$(location).attr('href');
if(currentUrl.match(/start.action/i)){
    updateLoginAttempts("deleteLoginAttempts");
}

getConfigurations();
function showLoginFormWithoutButtons(loginForm) {
    console.log("Showing loginform without buttons");
    AJS.$("#sso_failed_error").remove();
    var submitUsernameForm = AJS.$("#submit-username-form");
    AJS.$("#username-form-note").remove();
    submitUsernameForm.remove();
    loginForm.show();

    showSamlErrorMessage(loginForm);
}

function initializeSSO(configurations) {
console.log("Inside initialize saml sso config");
    var url = window.location.href;

    if (url.match(/\/userlogin/i) && url.match(/\?logout=true/i)) {
        localStorage.removeItem('isAdmin');
        if (ReadCookie(LOGOUTCOOKIE) !== "") {
            AJS.$(".aui.login-form-container").hide();
            var html = '<p>Please wait while we log you out...</p><aui-spinner size="medium"></aui-spinner>';
            AJS.$(html).insertBefore(AJS.$(".aui.login-form-container"));
            window.location.href = AJS.contextPath() + '/plugins/servlet/saml/logout';
            return;
        }
        renderLoginPage(0, configurations);
    } else if (url.match(/\/userlogin/i)) {
        console.log("No admin case!!!");
        renderLoginPage(0, configurations);

    }  else {

        if (AJS.params.remoteUser !== "" && AJS.params.remoteUser !== null)
            return;
        var pathname = window.location.pathname;
        var finalPath = pathname.replace(AJS.contextPath(), '');
        window.location.href = AJS.contextPath() + '/userlogin!doDefault.action?os_destination=' + encodeURIComponent(finalPath + window.location.search);
    }
}



/** Function to Process Login Page*
 *
 * @param loginForm : object of BAmboo login form which is loaded
 * @param configurations : configurations we got from /getconfig endpoint.
 */
function processBambooLogin(loginForm, configurations) {
    //If Backdoor URL entered
    // If Backdoor is enabled
    console.log("In Process bamboo Login Form");
    if (backdoorUrlEnteredAndEnabled(configurations)) {
        if(updateLoginAttempts("getLoginAttempts")===undefined){
            // If backdoor is restricted
            if (configurations.restrictBackdoor) {
                // Show Backdoor Access Form and return
                showSubmitUsernameForm(loginForm, formConstants["backdoor-form"]);		//Function to Process Backdoor Form is needed
                return;
            }
            // Show Login Form and return
            if (configurations.enableLoginTemplate === true && getQueryParameterByName('show_login_form') != "true") {
                if (configurations.ssoEnabledForIdpList.length != 0) {
                    showCustomLoginForm(configurations.loginTemplate);
                    return;
                }
            }

            updateLoginAttempts("setLoginAttempts");
            showLoginFormWithoutButtons(loginForm);
            return;
        }
    }

     if(getQueryParameterByName('moskipsso') == "true"){
                 showLoginFormWithoutButtons(loginForm);
                 return;
         }
    if(configurations.backdoorEnabled){
        var maxAttempts= parseInt(configurations.numberOfLoginAttempts);
        numberOfLoginAttempts = updateLoginAttempts("getLoginAttempts");

        if(numberOfLoginAttempts!=null || numberOfLoginAttempts!=undefined){        //checking if loginAttempts were made previously
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
    //Function to Process Redirection Rules and return
    checkAndShowRedirectionRulesForm(loginForm, configurations);
}


function showSubmitUsernameForm(loginForm, formConstant) {

    var html = getSubmitUsernameFormHtml(formConstant);

    AJS.$(html).insertAfter(loginForm);
    loginForm.hide();

    showSamlErrorMessage(AJS.$("#submit-username-form"));
}
function hideChangePasswordLink(count) {
    if (AJS.$("a:contains('Change password')").length > 0) {
        AJS.$("a:contains('Change password')").remove();
    } else if (count < 50) {
        count++;
        setTimeout(hideChangePasswordLink,100,count)
    }
}

function disablePasswordChange() {

    var pathname = window.location.pathname;
    /*Check for the forget password change URL*/
    if(pathname.indexOf("/forgotPassword.action")!== -1){
        AJS.$.ajax({
            url: AJS.contextPath() + "/plugins/servlet/saml/getconfig",
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
        AJS.$.ajax({
            url: AJS.contextPath() + "/plugins/servlet/saml/getconfig",
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
        AJS.$.ajax({
            url: AJS.contextPath() + "/plugins/servlet/saml/getconfig",
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

function hideChangePasswordLinklogin(count) {
    if (document.querySelector('a[href="/forgotPassword.action"]')) {
        document.querySelector('a[href="/forgotPassword.action"]').remove();
    } else if (count < 50) {
        count++;
        setTimeout(hideChangePasswordLinklogin, 100, count)
    }
}
