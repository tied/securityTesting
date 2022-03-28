AJS.$(function() {
    AJS.$(".aui-nav li").removeClass("aui-nav-selected");
    AJS.$("#mo-session-management").addClass("aui-nav-selected");
    AJS.$("#mo-saml").addClass("aui-nav-selected");
});


function enableRememberMe() {
    var enableRememberMeCookie = document.getElementById('enableRememberMeCookie').checked;
    console.log('enableRememberMeCookie');
    AJS.$.ajax({
        url: AJS.contextPath() + "/plugins/servlet/saml/moapi",
        data: {
            "action": "setRememberMeCookie",
            "value": enableRememberMeCookie
        },
        type: "POST",
        success: function (response) {
            console.log("THIS IS SUCCESS res");
            console.log(response);
            console.log('enableRememberMe success');
            if (enableRememberMeCookie) {
                AJS.flag({
                    title: 'Success!',
                    type: 'success',
                    close: 'auto',
                    body: '<p>Remember Me-Cookie enabled</p><br/>'
                });
            }else{
                AJS.flag({
                    title: 'Success!',
                    type: 'success',
                    close: 'auto',
                    body: '<p>Remember Me-Cookie disabled</p><br/>'
                });
            }
        },
        error:function(response){
        console.log("THIS IS ERROR RES");
        console.log(response);
            AJS.flag({
                title: 'Error!',
                type: 'error',
                body: '<p>Error saving settings</p><br/>'
            });
        }
    });
}