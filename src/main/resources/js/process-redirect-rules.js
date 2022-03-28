function getRedirectionRules(){
    var redirectionRules;
    AJS.$.ajax({
       url:AJS.contextPath()+"/plugins/servlet/saml/moapi",
       data:{
           "action":"getBambooRedirectionRules"
       },
        type:"GET",
        async:false,
        success:function(response){
           console.log(JSON.stringify(response));
           redirectionRules = response;
        }
    });
    return redirectionRules;
}

/** Function to Process Redirection Rules */
function checkAndShowRedirectionRulesForm(loginForm, configurations) {
    //Get Bamboo Redirection Rules
    var redirectionRules = getRedirectionRules();
    console.log("Redirection Rules "+redirectionRules);
    console.log("Size "+Object.keys(redirectionRules).length);
//If !Empty
    if (Object.keys(redirectionRules).length>0) {
        //Show Redirection Rules Form and return             ##Function to Process Redirection Rules Form is needed
        loginForm.hide();
        showSubmitUsernameForm(loginForm, formConstants["redirection-rules-form"]);

        return;
    }
    //Get Default IDP
    var defaultIdp = configurations.defaultBambooIDP;
    //If Default IDP is loginPage
    if (defaultIdp === 'loginPage') {

        if(configurations.enableLoginTemplate===true && getQueryParameterByName('show_login_form') != "true") {
            if (configurations.ssoEnabledForIdpList.length != 0) {
                showCustomLoginForm(configurations.loginTemplate);
                return;
            }
        }

        if (configurations.showLoginButtons)
            showLoginFormWithButtons(loginForm, configurations); //Show Login Form with SSO buttons and return
        else
            showLoginFormWithoutButtons(loginForm);
        return;
    }

    if (defaultIdp === 'redirectUrl'){
        var defaultRedirectUrl = configurations.defaultRedirectUrl;
        console.log("Default rule set to redirectUrl. Redirecting to :" + defaultRedirectUrl);
        window.location.href = defaultRedirectUrl;
        return;
    }

    //If Delay is Enabled
    if (configurations.enableAutoRedirectDelay) {
        //Show Progress Bar
        insertDelay(loginForm, configurations, defaultIdp);
        return;
    }
    //Redirect to selected IDP
    redirectToIDP(loginForm, defaultIdp);
}

function processRedirectionRules(response) {
console.log("processRedirectionRules process-redirect-rules.js");
    var idp = response.idp;
console.log("this is the idp "+ idp);
    if (idp === 'loginPage') {
    console.log("idp is loginPage (under processRedirectionRules)")
        var loginForm=AJS.$("#loginForm");
        console.log(AJS.$("#loginForm"));
        console.log("Reaching here "+loginForm);
        showLoginFormWithoutButtons(loginForm);
        return;
    }

    if(idp === 'redirectUrl'){
        var url = response.defaultRedirectUrl;
        console.log("Default rule set to redirectUrl. Redirecting to :" + url);
        window.location.href = url;
        return;
    }
    console.log("did not not in any if condition (under processRedirectionRules)")
    redirectToIDP(AJS.$("#submit-username-form"), idp);
}