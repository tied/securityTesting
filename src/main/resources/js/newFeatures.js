var newFeatures = {
    "menus": [
        "mo-redirection-rules",
        "mo-look-and-feel",
        "mo-global-sso-settings",
        "mo-idps-overview",
        "mo-headerbasedauth"
    ],
    "fields": [
        "show-login-button-switch"
    ]
};

AJS.$(function() {
    addNewFeaturesLozenge();
});

function addNewFeaturesLozenge() {

    var html = '<span>&emsp;</span><span class="aui-lozenge aui-lozenge-new">New</span>';

    newFeatures.menus.forEach(function (menu) {
        AJS.$(html).appendTo(AJS.$("#"+menu).children("a")[0]);
    });

    newFeatures.fields.forEach(function (field) {
        AJS.$(html).insertAfter(AJS.$("#" + field));
    })
}