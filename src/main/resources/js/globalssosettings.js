AJS.$(document).ready(function() {

    showWarningForAPIRestriction();
    showSSOSwitch();

    AJS.$(document).on('change','#pluginApiAccessRestriction',function(){
        showWarningForAPIRestriction();
    });

    AJS.$(document).on("change","#enableSAMLSSO",function () {
        showSSOSwitchOnChange();
    });
    setTimeout(showWarningForAPIRestriction,100);
});

function showSSOSwitchOnChange(){
        if (document.getElementById("enableSAMLSSO").checked) {
            AJS.$('#sso-url-warning').hide("slow");
        } else {
            AJS.$('#sso-url-warning').show("slow");
        }
    }

function showSSOSwitch(){

        if (document.getElementById("enableSAMLSSO").checked) {
            AJS.$('#sso-url-warning').hide();
        } else {
            AJS.$('#sso-url-warning').show();
        }
    }

function showWarningForAPIRestriction() {
    warningforapirestriction = document.getElementById('pluginApiAccessRestriction');

    if(warningforapirestriction.checked) {
        AJS.$("#warningforapirestriction").show();
    } else {
        AJS.$("#warningforapirestriction").hide();
    }
}

