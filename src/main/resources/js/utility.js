/** This functions is used to read the value of a cookie whose name is passed to it.
 * Currently it's being used to read the Logout cookie on admin screen which holds the
 * id of IDP used by admin to log in initially.
 * @param cname: This is a cookie name
 * @return string of cookie cname
 * */
function ReadCookie(cname) {
    var name = cname + "=";
    var decodedCookie = document.cookie;
    var ca = decodedCookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}



function createSSOUrl(idpName) {
    var osDestination = getQueryParameterByName("os_destination");
    var webSudoDestination = getQueryParameterByName("webSudoDestination");
    var destination = getQueryParameterByName("destination");
    var samlAuthUrl = AJS.contextPath() + '/plugins/servlet/saml/auth';

    if (!(!osDestination || /^\s*$/.test(osDestination)) && osDestination != "/") {
        samlAuthUrl += "?return_to=" + encodeURIComponent(osDestination);
    } else if (!(!webSudoDestination || /^\s*$/.test(webSudoDestination)) && webSudoDestination != "/") {
        samlAuthUrl += "?return_to=" + encodeURIComponent(webSudoDestination);
    }else if(!(!destination || /^\s*$/.test(destination)) &&
        destination != "/"){
        samlAuthUrl += "?return_to=" + encodeURIComponent(destination);
    } else {
        var url = window.location.href;
        if (!url.match(/userlogin/i)) {
            samlAuthUrl += "?return_to=" + encodeURIComponent(window.location.href);
        }
    }

    var isAdmin = localStorage.getItem('isAdmin');

    if (samlAuthUrl.indexOf("return_to") > 0) {
            samlAuthUrl += "&isAdmin=" + isAdmin;
    } else {
            samlAuthUrl += "?isAdmin=" + isAdmin;
        }

    if(idpName)
     samlAuthUrl += "&idp=" + idpName;
    return samlAuthUrl;
}

function backdoorUrlEnteredAndEnabled(configurations) {
    if (configurations.backdoorEnabled) {
        var backdoorValue = getQueryParameterByName(configurations.backdoorKey);
        if (backdoorValue === configurations.backdoorValue)
            return true;
    }
    return false;
}

function getSubmitUsernameFormHtml(formConstant){
    var html = '<div id="username-form-note" class="aui-message aui-message-info">' + formConstant["info-note"] + '</div>' +
        '<form id="submit-username-form" method="get" action="" name="submit-username-form" class="aui gdt">' +
        '<input type="hidden" id="action" name="action" value="' + formConstant.action + '"/>' +
        '<div class="field-group">' +
        '<label accesskey="u" for="login-form-email" id="usernamelabel" style="display: block;"><u>U</u>sername/Email</label>' +
        '<input class="text" id="login-form-email" name="mo_username" type="text"/>' +
        '</div>' +
        '<div class="field-group">' +
        '<button id="submit-username" class="aui aui-button aui-button-primary">Submit</button>' +
        '</div>' +
        '</form>';
    return html;
}

/**
 * This function is written to insert a delay box on the login page so that user has the
 * option to cancel the auto-redirect functionality provided by the plugin. This inserts
 * a div with a link to initiate SSO, a cancel button to cancel the auto-redirect and a
 * delay loader just for UX purposes.
 */
function insertDelay(loginForm, configurations, idpName) {
    var samlAuthUrl = createSSOUrl(idpName);
    var loginButtonText = configurations.loginButtonText;
    /** The Text to show once user has been redirected */
    /*var textOnCancel = '<a href="' + samlAuthUrl + '" id="redirectMessage" class="aui-button aui-button-link">' +
        configurations.loginButtonText + '</a>';*/

    /** Create the delay box on the login page */
    var html = '<div id="moRedirectBox" class="aui-message aui-message-info">' +
        '<p>' +
        '<span id="defaultText">Redirecting to IDP.</span>' +
        '<button id="stopAutoRedirect" onClick="stopAutoRedirect(\''+samlAuthUrl+'\',\''+loginButtonText+'\')" class="aui-button aui-button-primary" style="float:right;" ' +
        'resolved="">Cancel</button>' +
        '</p>' +
        //'<input type="hidden" id="samlAuthUrl" value="' + samlAuthUrl + '"></input>' +
        '<div id="moRedirectProgress" class="aui-progress-indicator" style="margin-top: 1.5em;' +
        'background: #e9e9e9;border-radius: 3px;height: 5px;overflow: hidden;position: relative;' +
        'width: 100%;" data-value="0.01">' +
        '<span class="aui-progress-indicator-value" style="position: absolute;display: block;' +
        'height: 5px;animation: progressSlide 1s infinite linear;animation-play-state: running;' +
        '-webkit-animation-play-state: paused;animation-play-state: paused;background: #3572b0;' +
        'border-radius: 3px 0 0 3px;transition: width .5s;-webkit-transform: skewX(0);' +
        'transform: skewX(0);width:0%"></span>' +
        '</div>' +
        '</div>';


    /** Inserting the div after the Default Login Form */
    AJS.$("#gadget-0").css("height", "355px");
    AJS.$(html).insertAfter(loginForm);
    updateProgressBar(0.005,idpName,loginForm);

}

function stopAutoRedirect(samlAuthUrl,loginButtonText){
    AJS.$("#moRedirectBox").remove();
    var ssoButton='<a href="'+samlAuthUrl+'" width="100%" id="redirectMessage" class="aui-button aui-button-primary" resolved="">'+loginButtonText+'</a>'
    AJS.$(ssoButton).insertAfter(AJS.$("#loginForm_save"));


}

/**
 * Function is used to update the progressBar added to the login page if admin
 * has set a delay for the auto-redirect. If the progress bar reaches 100% then
 * redirect the user to the IDP.
 */

function updateProgressBar(progress, idpName, loginForm) {
    if(AJS.$("#moRedirectProgress").length === 0){
        return;
    }
    if (progress < 0.995) {
        progress = progress + 0.005;
        var percentage = progress * 100 + "%";
        AJS.$("#moRedirectProgress").attr("data-value", progress);
        AJS.$(".aui-progress-indicator-value").css("width", percentage);
        setTimeout(updateProgressBar, 25, progress,idpName,loginForm);
    } else {
        progress = 0.1;
        AJS.$("#moRedirectBox").remove();
        redirectToIDP(loginForm, idpName);
    }
}

function redirectToIDP(loginForm, idpName) {

    var html = '<div class="aui-message aui-message-info"><span>Please wait while we redirect you for authentication...</span><aui-spinner size="medium" style="margin-left: 50%;margin-top: 20px;"></aui-spinner></div>';
    loginForm.html(html);
    var samlAuthUrl = createSSOUrl(idpName);
    window.onbeforeunload = null;
    window.location.href = samlAuthUrl;
}

function showSamlErrorMessage(loginForm) {
    var samlerror = getQueryParameterByName('samlerror');
    if (samlerror) {
        var htmlerror;
        switch (samlerror) {
            case 'cant_signin_no_access':
                htmlerror = '<div id="sso_failed_error" class="aui-message aui-message-error closeable"><p>Sorry. It seems like you don\'t have an access to the Application. Please contact your administrator with this message. </p></div>';
                break;
            default:
                htmlerror = '<div id="sso_failed_error" class="aui-message aui-message-error closeable"><p>We couldn\'t sign you in. Please contact your administrator with this message. </p></div>';
                break;
        }

        AJS.$(htmlerror).insertBefore(loginForm);

    }
}

function checkBackdoorAccess(response) {
    if (response.isUserAllowedBackdoorAccess === true) {
        updateLoginAttempts("setLoginAttempts");
        var loginForm=AJS.$('#loginForm');
        AJS.$("#os_username").val(response.usernamePreFill);
        AJS.$("#backdoor_restriction_note").remove();
        AJS.$("#submit-username-form").remove();
        showLoginFormWithoutButtons(loginForm);
    } else {
        updateLoginAttempts("deleteLoginAttempts");
        console.log("access denied");
        var noAccessHtml = '<div id="no_backdoor_access_error" class=\"aui-message aui-message-error closeable\"><p>Sorry. You don\'t have access to this page, Please wait while we redirect you to IDP</p></div><div class="mospinner"><aui-spinner size="large"></aui-spinner></div>';
        AJS.$("#username-form-note").remove();
        AJS.$("#submit-username-form").hide();
        if (!AJS.$("#no_backdoor_access_error").length)
            AJS.$(noAccessHtml).insertBefore(AJS.$("#submit-username-form"));
        var pathname = window.location.pathname;
        var loginUrl = AJS.contextPath() + 'userlogin!doDefault.action';

        setTimeout(function () {
            window.onbeforeunload = null;
            window.location.href = loginUrl;
        }, 2000);
    }
}

function showLoginFormWithoutButtons(loginForm){
    console.log("Showing loginform without buttons");
    AJS.$("#sso_failed_error").remove();
    var submitUsernameForm = AJS.$("#submit-username-form");
    AJS.$("#username-form-note").remove();
    submitUsernameForm.remove();
    loginForm.show();

    showSamlErrorMessage(loginForm);
}

function getQueryParameterByName(name){
    var url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&]*)|&|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function renderLoginPage(count,configurations) {
    count++;
    var loginForm;
    if (AJS.$("#loginForm").length > 0) {
        loginForm = AJS.$("#loginForm");
            processBambooLogin(loginForm,configurations);

    } else if (count <= 100) {
        setTimeout(renderLoginPage, 100, count,configurations);
    }
    return loginForm;
}

function showCustomLoginForm(loginPageTemplate) {
    AJS.$('body').hide();
    var html = '<div>' + loginPageTemplate + '</div>';
    AJS.$("body").html(html);
    AJS.$('body').show();
}

function showLoginFormWithButtons(loginForm, configurations) {
    console.log("SHOWING BUTTONS!!!!!!!");
    AJS.$("#sso_failed_error").remove();

    var idpButtonsHtml;
    var isAdmin = localStorage.getItem('isAdmin');
    if (configurations.ssoEnabledForIdpList.length===0) {
        loginForm.show();
        showSamlErrorMessage(loginForm);
        return;
    }

    if (isAdmin === 'true') {
        firstIdpName = ReadCookie(LOGOUTCOOKIE);
    } else {
        firstIdpName = configurations.ssoEnabledForIdpList[0];
    }

    if (isAdmin==='true'){
        showAdminLoginFormWithButton(firstIdpName,loginForm,configurations.loginButtonText);
        return;
    }

    if (configurations.ssoEnabledForIdpList.length > 1) {

        idpButtonsHtml = '<hr/><div id="mo_sso_form"><h2>' + configurations.loginButtonText +
            '</h2></div><div id="oauth_application_list" style="text-align: center;">';

        for (i = 0; i < configurations.ssoEnabledForIdpList.length; i++) {
            idpID = configurations.ssoEnabledForIdpList[i];
            idpName = configurations.idpMap[idpID];
            ssoURL = createSSOUrl(idpID);

            idpButtonsHtml += '<div class="buttons">';
            idpButtonsHtml += '<a class="aui-button aui-style aui-button-primary" href="' + ssoURL + '"' +
                'style="align:center;width: 50%;margin-bottom: 2px;margin-top: 2px; margin-left: 0px;">' +
                '<p style="text-align:center;">' + idpName + '</p></a>';
            idpButtonsHtml += '</div>';
        }

        idpButtonsHtml += '</div>';

        idpButtonsHtml = '<div style="margin-left: -4em;max-width: 26em;">' + idpButtonsHtml + '</div>';

    } else {
        idpButtonsHtml = '<a class="aui-button aui-style aui-button-primary" href="'+createSSOUrl(firstIdpName)+'" style="align:center;">' + configurations.loginButtonText + '</a>';
    }

    if (configurations.ssoEnabledForIdpList.length > 1) {
        console.log("Showing multiple button");
        AJS.$(idpButtonsHtml).insertAfter(AJS.$("#loginForm_save"));
    } else if(configurations.ssoEnabledForIdpList.length == 1){
        console.log("Showing single button");
        AJS.$(idpButtonsHtml).insertAfter(AJS.$("#loginForm_save"));
    }

    showSamlErrorMessage(loginForm);
}

function showAdminLoginFormWithButton(firstIdpName,loginForm,loginButtonText){

    var idpButtonsHtml = '<a class="aui-button aui-style aui-button-primary" href="'+createSSOUrl(firstIdpName)+'" style="align:center;">' + loginButtonText + '</a>';
    AJS.$(idpButtonsHtml).insertAfter(AJS.$("#authenticateButton"));
    showSamlErrorMessage(loginForm);
}
