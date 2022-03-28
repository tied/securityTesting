AJS.$(document).ready(function (){
    AJS.$(".aui-nav li").removeClass("aui-nav-selected");
    AJS.$("#mo-post-logout-rules").addClass("aui-nav-selected");
    AJS.$("#mo-saml").addClass("aui-nav-selected");

    logoutDivDisplay();
    AJS.$(document).on('change','#enableLogoutTemplate',function(){
        logoutDivDisplay();
    });

    setTimeout(logoutDivDisplay, 100);

});

function logoutDivDisplay(){
    if(document.getElementById('enableLogoutTemplate').checked) {
        AJS.$("#logoutTemplate").prop("disabled", false);
        AJS.$("#customLogoutURL").prop("disabled", true);
    } else {
        AJS.$("#customLogoutURL").prop("disabled", false);
        AJS.$("#logoutTemplate").prop("disabled", true);
    }
}
