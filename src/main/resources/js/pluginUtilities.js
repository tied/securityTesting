var helpButton = document.getElementById("Help_Button");
var sticky = helpButton.offsetTop;
helpButton.classList.add("sticky");

AJS.$("#title-text").remove();
var url=window.location.href;
if (url.split("?")[0].match(/\/bamboo-sso/i)) {
    AJS.$('#main.aui-page-panel').addClass("pad1");
}

AJS.$(document).on("click",".copyable",function(){
    var inp = document.createElement('input');
    document.body.appendChild(inp);
    inp.value = document.getElementById(this.id).textContent.trim();
    inp.select();
    document.execCommand('copy', false);
    inp.remove();
    message='#'+this.id+'-copied-message';
    AJS.$(message).show();
    setTimeout(function () {
        AJS.$(message).hide("slow");
    }, 2500);
});

AJS.$(document).on("mouseenter",".idp-heading.collabsible",function(){
    var actions = AJS.$(this).children('ul')[0];
    AJS.$(actions).show("slow");
});

AJS.$(document).on("mouseleave", ".idp-heading.collabsible", function () {
    var actions = AJS.$(this).children('ul')[0];
    AJS.$(actions).hide();
});

function showSuccessMessage(message){
    AJS.flag({
        title: 'Success!',
        type: 'success',
        close: 'auto',
        body: '<p>'+message+'</p><br/>'
    });
}

function showErrorMessage(message){
    AJS.flag({
        title: 'Error!',
        type: 'error',
        body: '<p>  '+message.responseText+'</p><br/>'
    });
}

AJS.$(document).on('click',"#clear-migration-button",function(){
    AJS.$.ajax({
        url:AJS.contextPath()+ "/plugins/servlet/saml/moapi?action=clearMigrationValue",
        type:'DELETE',
        success:function (response) {
            showSuccessMessage(response);
        }
    })
});
