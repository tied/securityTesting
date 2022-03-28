
    /** Initializing all the configurations so that multiple calls are not needed */
    function getConfigurations() {
    console.log("In get config method...3");
        AJS.$.ajax({
            url: AJS.contextPath() + "/plugins/servlet/saml/getconfig",
            type: "GET",
            error: function () {
                console.log("miniOrange SAML: configuration couldn't be fetched. exiting");
                return;
            },
            success: function (response) {
                    console.log(response);
                if (response.enableSAMLSSO) {
                    initializeSSO(response);
                }
                if (response.enablePasswordChange === false){
                    disablePasswordChange();
                }
            }
        });
    }


    /** After pressing enter on username field in submit-username form */
    AJS.$(document).on('keypress', '#login-form-email', function (e) {
        if (e.which != 13) {
            return;
        }
        e.preventDefault();
        submitUsernameForm();

    });

AJS.$(document).on('click', '#submit-username', function (e) {
    e.preventDefault();
    submitUsernameForm();
});

function submitUsernameForm(){
    AJS.$.ajax({
        url: AJS.contextPath() + "/plugins/servlet/saml/moapi",
        data: AJS.$("#submit-username-form").serialize(),
        type: "GET",
        async:true,
        error: function (response){},
        success: function(response){
            var action = AJS.$("#action").val();
            console.log("Sucesssss");
            switch (action){
                case "checkBackdoorAccess":
                    console.log("checkBackdoorAccess!!!!!!!!!");
                    checkBackdoorAccess(response);
                    break;
                case "processRedirectionRules":
                    console.log("processRedirectionRules!!!!!!!!!");
                    processRedirectionRules(response);
                    break;
            }
        }
    });
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
