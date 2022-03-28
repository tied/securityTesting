AJS.$(function () {
    AJS.$(".aui-nav li").removeClass("aui-nav-selected");
    AJS.$("#mo-idps").addClass("aui-nav-selected");
    AJS.$("#mo-saml").addClass("aui-nav-selected");

    AJS.$("#add-idp-button").click(function() {
        AJS.dialog2("#quick_or_custom_setup_dialog").show();
    });
    AJS.$("#add-idp-main").click(function() {
        AJS.dialog2("#quick_or_custom_setup_dialog").show();
    });
    AJS.$("#quick_or_custom_setup_dialog_close_button").click(function(e) {
        e.preventDefault();
        AJS.dialog2("#quick_or_custom_setup_dialog").hide();
    });

    AJS.$("#new_users_popup_proceed_button").click(function(e) {
        e.preventDefault();
        setShowIntroPageToFalse();
        AJS.dialog2("#intro_for_new_users_dialog").hide();
        AJS.dialog2("#intro_for_existing_users_dialog").hide();
    });

    AJS.$("#existing_users_popup_proceed_button").click(function(e) {
        e.preventDefault();
        setShowIntroPageToFalse();
        AJS.dialog2("#intro_for_new_users_dialog").hide();
        AJS.dialog2("#intro_for_existing_users_dialog").hide();
    });

    AJS.$("#intro_for_existing_users_dialog_close_button").click(function(e) {
        e.preventDefault();
        AJS.dialog2("#intro_for_existing_users_dialog").hide();
    });
    AJS.$("#intro_for_new_users_dialog_close_button").click(function(e) {
        e.preventDefault();
        AJS.dialog2("#intro_for_new_users_dialog").hide();
    });

    AJS.$("#show-intro").click(function() {
        AJS.dialog2("#intro_for_new_users_dialog").show();
    });

    AJS.$(".idp-options-dropdown").auiSelect2({minimumResultsForSearch: -1});

});
function testConfigurations(idpname){
    var osDestination = "testidpconfiguration" ;
    var samlAuthUrl = AJS.contextPath() + '/plugins/servlet/saml/auth';
    samlAuthUrl += "?return_to=" + osDestination+"&idp="+idpname;
    var myWindow = window.open(samlAuthUrl, "TEST SAML IDP", "scrollbars=1 width=800, height=600");
}

function showDeleteDialog(idp) {
    var dialog = new AJS.Dialog({
        width: 400,
        height: 150,
        id: "delete-dialog",
        closeOnOutsideClick: true
    });
    dialog.addPanel("Panel 1", "<p>Are you sure you want to delete this idp?</p> ", "panel-body");
    dialog.addButton("Yes", function (dialog) {
        dialog.hide();
        document.getElementById("idpID").value = idp;
        document.getElementById("delete_idp_form").submit();
    });
    dialog.addLink("No", function (dialog) {
        dialog.hide();
    }, "#");

    dialog.show();
}

function handleSelect(element){
    window.location = element.value;
}

function showSetupFlow() {
    var dialog = new AJS.Dialog({
        width: 400,
        height: 150,
        id: "show-setup",
        closeOnOutsideClick: true
    });
    dialog.addLink("Close", function (dialog) {
        dialog.hide();
    }, "#");
    dialog.addPanel("Panel 1", "<a class=\"aui-button aui-button-primary\" href=\"samlsso.supportedidps.jspa\">Quick Setup</a>", "panel-body");
    dialog.addPanel("Panel 2","<a class=\"aui-button aui-button-primary\" href=\"samlsso.addidp.jspa?operation=add\">Manual Setup</a>", "panel-body");
    dialog.show();
}

function hideContinueSetup(element){
    console.log("close continue setup");
    var tag = "in-progress-" + element;
    var buttonDiv = "continue-setup-div-" + element;
    document.getElementById(tag).style.display = 'none';
    document.getElementById(buttonDiv).style.display = 'none';
    AJS.$.ajax({
        url: AJS.contextPath() + "/plugins/servlet/saml/moapi",
        data: {
            "action": "finishQuickSetup",
            "idp": element
        },
        type: "POST",
    });
}

function submitEnableSSOChange(idpid) {
    console.log("enableSSO Options changed.");
    var isChecked = document.getElementById("enableSsoForIdp_"+idpid).checked;
    AJS.$.ajax({
        url: AJS.contextPath() + "/plugins/servlet/saml/moapi",
        type: "POST",
        data:{
            "action":"saveSsoConfig",
            "idpid":idpid,
            "isChecked":isChecked
        },
        success: function (response) {
            console.log('Enable SSO success');
            AJS.flag({
                title: 'Success!',
                type: 'success',
                close: 'auto',
                body: '<p>SSO settings saved</p><br/>'
            });
        },
        error:function(response){
            AJS.flag({
                title: 'Error!',
                type: 'error',
                body: '<p>Error saving settings</p><br/>'
            });
        }
    });
}