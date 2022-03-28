AJS.$(function ()
{
     AJS.$(".aui-nav li").removeClass("aui-nav-selected");
     AJS.$("#mo-look-and-feel").addClass("aui-nav-selected");
     AJS.$("#mo-saml").addClass("aui-nav-selected");


    loginTemplateDivDisplay();
    errorTemplateDivDisplay();

	AJS.$(document).on('change','#enableErrorMsgTemplate',function(){
          errorTemplateDivDisplay();
    });

    AJS.$(document).on('change','#enableLoginTemplate',function(){
          loginTemplateDivDisplay();
    });

   setTimeout(loginTemplateDivDisplay, 100);
   setTimeout(errorTemplateDivDisplay, 100);

});
    function loginTemplateDivDisplay(){
        if(document.getElementById('enableLoginTemplate').checked) {
            AJS.$("#loginTemplate").prop("disabled", false);
        } else {
            AJS.$("#loginTemplate").prop("disabled", true);
        }
    }

    function errorTemplateDivDisplay(){
        if(document.getElementById('enableErrorMsgTemplate').checked){
            AJS.$("#errorMsgTemplate").prop("disabled", false);
        } else {
            AJS.$("#errorMsgTemplate").prop("disabled", true);
        }
    }

function copyToClipboard(element, copyButton) {
    var temp = AJS.$("<input>");
    console.log(temp);
    AJS.$("body").append(temp);
    temp.val(AJS.$(element).text()).select();
    document.execCommand("copy");
    temp.remove();
    AJS.$(copyButton).show();
    setTimeout(function() { AJS.$(copyButton).hide("slow"); }, 5000);
}
