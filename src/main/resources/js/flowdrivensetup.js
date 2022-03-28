function submitForm(formName) {
    document.getElementById(formName).submit();
}

function normalizeDimentions() {
    var height = document.getElementById('progress-div').style.height;
    console.log("height : " + height);
    document.getElementById('config-pages-div').style.height = height;
}

function copyToClipboard(element, copyButton) {
    var temp = AJS.$("<input>");
    AJS.$("body").append(temp);
    temp.val(AJS.$(element).text()).select();
    document.execCommand("copy");
    temp.remove();
    AJS.$(copyButton).show();
    setTimeout(function () {
        AJS.$(copyButton).hide("slow");
    }, 2500);
}

function loadCurrentPage() {
    var page = 0;
    if (document.getElementById('spmetadata').style.display === "block") {
        document.getElementById('circle1').classList.add("active");
        page = 1;
    } else if (document.getElementById('configureidp').style.display === "block") {
        document.getElementById('circle2').classList.add("active");
        page = 2;
    } else if (document.getElementById('userprofile').style.display === "block") {
        document.getElementById('circle3').classList.add("active");
        page = 3;
    } else if (document.getElementById('usergroups').style.display === "block") {
        document.getElementById('circle4').classList.add("active");
        page = 4;
    } else if (document.getElementById('testconfig').style.display === "block") {
        document.getElementById('circle5').classList.add("active");
        page = 5;
    }

    for (var i = 1; i < page; i++) {
        var classname = "circle" + i;
        document.getElementById(classname).classList.add("done");
    }
}

function displayPrevPage(pageNumber) {
    console.log("1");
    document.getElementById('spmetadata').style.display = "none";
    document.getElementById('configureidp').style.display = "none";
    document.getElementById('userprofile').style.display = "none";
    document.getElementById('usergroups').style.display = "none";
    document.getElementById('testconfig').style.display = "none";
    console.log("2");
    if (pageNumber == 1) {
        console.log("3");
        document.getElementById('spmetadata').style.display = "block";
        document.getElementById('circle1').classList.remove("done");
        document.getElementById('circle1').classList.add("active");
        document.getElementById('circle2').classList.remove("active");
    } else if (pageNumber == 2) {
        console.log("4");
        document.getElementById('configureidp').style.display = "block";
        document.getElementById('circle2').classList.remove("done");
        document.getElementById('circle2').classList.add("active");
        document.getElementById('circle3').classList.remove("active");
    } else if (pageNumber == 3) {
        document.getElementById('userprofile').style.display = "block";
        document.getElementById('circle3').classList.remove("done");
        document.getElementById('circle3').classList.add("active");
        document.getElementById('circle4').classList.remove("active");
    } else if (pageNumber == 4) {
        document.getElementById('usergroups').style.display = "block";
        document.getElementById('circle4').classList.remove("done");
        document.getElementById('circle4').classList.add("active");
        document.getElementById('circle5').classList.remove("active");
    }
}

function showCertificateDetails() {
    var x = document.getElementById('certificateInfoDiv');
    if (x.style.display === 'none') {
        x.style.display = 'block';
        document.getElementById('show-certificate-details').innerHTML = "Hide Certificate Details";

    } else {
        x.style.display = 'none';
        document.getElementById('show-certificate-details').innerHTML = "Show Certificate Details";

    }
}

function copyCertificate(element, copyButton) {
    var $temp = AJS.$("<textarea id=\"certificateAsText\" cols=\"64\" rows=\"4\"></textarea>");
    AJS.$("body").append($temp);
    document.getElementById('certificateAsText').value += AJS.$(element).text();
    document.querySelector("#certificateAsText").select();
    document.execCommand("copy");
    $temp.remove();
    AJS.$(copyButton).show();
    setTimeout(function () {
        AJS.$(copyButton).hide("slow");
    }, 2500);
}

function showMetadataUrl() {
    console.log("metadataurl");
    document.getElementById('metadata-url').style.display = "block";
    document.getElementById('metadata').style.display = "none";
}

function showMetadata() {
    console.log("metadata");
    document.getElementById('metadata-url').style.display = "none";
    document.getElementById('metadata').style.display = "block";
}

function showUpdateForm() {
    console.log("update urls");
    if (document.getElementById('update-urls').style.display === "none") {
        document.getElementById('update-urls').style.display = "block";
    } else {
        document.getElementById('update-urls').style.display = "none";
    }
}

function showIdpMetadataUrl() {
    var listOfURLElements = document.getElementsByClassName('idp_metadata_url');
    var i;
    for (i = 0; i < listOfURLElements.length; i++) {
        listOfURLElements[i].style.display = "revert";
    }

    var listOfFileElements = document.getElementsByClassName('idp_metadata_file');
    var j;
    for (j = 0; j < listOfFileElements.length; j++) {
        listOfFileElements[j].style.display = "none";
    }

    var listOfManualElements = document.getElementsByClassName('idp_metadata_manual');
    var k;
    for (k = 0; k < listOfManualElements.length; k++) {
        listOfManualElements[k].style.display = "none";
    }
    AJS.$("#configureidp-form").prop("action", "");
    showEffectiveMetadataUrlQuick();
}

function showIdpMetadataFile() {
    var listOfURLElements = document.getElementsByClassName('idp_metadata_url');
    var i;
    for (i = 0; i < listOfURLElements.length; i++) {
        listOfURLElements[i].style.display = "none";
    }

    var listOfFileElements = document.getElementsByClassName('idp_metadata_file');
    var j;
    for (j = 0; j < listOfFileElements.length; j++) {
        listOfFileElements[j].style.display = "revert";
    }

    var listOfManualElements = document.getElementsByClassName('idp_metadata_manual');
    var k;
    for (k = 0; k < listOfManualElements.length; k++) {
        listOfManualElements[k].style.display = "none";
    }
    AJS.$("#configureidp-form").prop("action", "metadataupload");
}

function showManualMetadata() {
    var listOfURLElements = document.getElementsByClassName('idp_metadata_url');
    var i;
    for (i = 0; i < listOfURLElements.length; i++) {
        listOfURLElements[i].style.display = "none";
    }

    var listOfFileElements = document.getElementsByClassName('idp_metadata_file');
    var j;
    for (j = 0; j < listOfFileElements.length; j++) {
        listOfFileElements[j].style.display = "none";
    }

    var listOfManualElements = document.getElementsByClassName('idp_metadata_manual');
    var k;
    for (k = 0; k < listOfManualElements.length; k++) {
        listOfManualElements[k].style.display = "revert";
    }
    AJS.$("#configureidp-form").prop("action", "");
}

function showSeparateAttr() {
    document.getElementById('separatename_attr').style.display = "block";
    document.getElementById('fullname_attr').style.display = "none";
}

function showFullNameAttr() {
    document.getElementById('separatename_attr').style.display = "none";
    document.getElementById('fullname_attr').style.display = "block";
}

function showIdpName() {
    var listOfElements = document.getElementsByClassName('custom-idp-name');
    var i;
    for (i = 0; i < listOfElements.length; i++) {
        listOfElements[i].style.display = "revert";
    }
    document.getElementById('newIdpname').style.display= "block";
    document.getElementById('newIdpname').required = true;
}

function hideIdpName() {
    var listOfElements = document.getElementsByClassName('custom-idp-name');
    var i;
    for (i = 0; i < listOfElements.length; i++) {
        listOfElements[i].style.display = "none";
    }
    document.getElementById('newIdpname').style.display= "block";
    document.getElementById('newIdpname').required = false;
}

function showIdpMetadataFunctions() {
    document.getElementById('config-idp-save').style.display = "inline";
    document.getElementById('config-idp-next').style.display = "none";
    document.getElementById('config-idp-next').disabled = true;
    document.getElementById('quick-setup-test').style.display = "none";

    var value = document.getElementById('metadataOpt').value;
    if (value == "fromUrl") {
        showIdpMetadataUrl();
    } else if (value == "fromFile") {
        showIdpMetadataFile();
    } else if (value == "manual") {
        showManualMetadata();
    }
}

function showSPMetadataFunctions() {
    var value = document.getElementById('spmetadata_q1').value;
    if (value == "metadataUrl") {
        showMetadataUrl();
    } else if (value == "metadataManual") {
        showMetadata();
    }
}

function showCustomIdpNameFunctions() {
    var value = document.getElementById('customIdpName').value;
    if (value == "yes") {
        showIdpName();
    } else if (value == "no") {
        hideIdpName();
    }
}

function showNameAttributes() {
    var value = document.getElementById('useSeparateNameAttributes').value;
    if (value == "true") {
        showSeparateAttr();
    } else if (value == "false") {
        showFullNameAttr();
    }
}

function showOrHide(elementID) {
    if (document.getElementById(elementID).style.display == "block") {
        document.getElementById(elementID).style.display == "none";
    } else if (document.getElementById(elementID).style.display == "none") {
        document.getElementById(elementID).style.display == "block";
    }
}

function showOrHide(elementID) {
    if (document.getElementById(elementID).style.display == "block") {
        document.getElementById(elementID).style.display = "none";
    } else if (document.getElementById(elementID).style.display == "none") {
        document.getElementById(elementID).style.display = "block";
    }
}


function showBackWarningMessage() {

    if ((document.getElementById('spmetadata').style.display == "block") || (document.getElementById('configureidp').style.display == "block" && document.getElementById('quick-setup-test').style.display == "none")) {
        var dialog = new AJS.Dialog({
            width: 400,
            height: 150,
            id: "delete-dialog",
            closeOnOutsideClick: true
        });
        dialog.addPanel("Panel 1", "<p><strong>WARNING</strong> : Your IDP hasn't been added yet.</p><p>Are you sure you wish to exit setup?</p>", "panel-body-1");
        dialog.addButton("Yes", function (dialog) {
            dialog.hide();
            window.location = "listidp.action";

        });
        dialog.addLink("No", function (dialog) {
            dialog.hide();
        }, "#");

        dialog.show();
    } else if ((document.getElementById('configureidp').style.display == "block" && document.getElementById('quick-setup-test').style.display == "inline") || (document.getElementById('userprofile').style.display == "block") || (document.getElementById('usergroups').style.display == "block")) {
        var dialog = new AJS.Dialog({
            width: 400,
            height: 200,
            id: "delete-dialog",
            closeOnOutsideClick: true
        });
        dialog.addPanel("Panel 1", "<p>All configurations done so far have been saved. Use <strong>Continue Quick Setup</strong> link next to your IDP in the main menu to return to this page.</p><p> Do you still wish to exit quick setup ?</p>", "panel-body");
        dialog.addButton("Yes", function (dialog) {
            dialog.hide();
            window.location = "listidp.action";

        });
        dialog.addLink("No", function (dialog) {
            dialog.hide();
        }, "#");

        dialog.show();
    } else {
        window.location = "listidp.action";
    }

}

function showEffectiveMetadataUrlQuick() {
    value = AJS.$("#inputUrl").val();
    idpName = AJS.$("#idpName").val();
    if (value != null) {
        if (value.replace(/\s/g, '').length) {
            AJS.$("#effective_metadata_url_div").show();
        } else {
            AJS.$("#effective_metadata_url_div").hide();
        }
        if (value.indexOf("https:\/\/") >= 0 || value.indexOf("http:\/\/") >= 0) {
            idpName = "fromUrl";
        }
        if(idpName) {
            switch (idpName) {
                case "ADFS":
                    document.getElementById('effective_metadata_url').innerHTML = "https://" + value + "/federationmetadata/2007-06/federationmetadata.xml";
                    break;
                case "Azure_AD":
                    document.getElementById('effective_metadata_url').innerHTML = "https://login.microsoftonline.com/" + value + "/FederationMetadata/2007-06/FederationMetadata.xml";
                    break;
                default:
                    document.getElementById('effective_metadata_url').innerHTML = value;
            }
        }
    }
}

function showLogFile() {
    var x = document.getElementById('log-file-steps-div');
    if (x.style.display === "none") {
        AJS.$('#log-file-steps-div').fadeIn(2000);
        x.style.display = "block";
    } else {
        x.style.display = "none";
    }
}

function showEmailWarning() {
    if (document.getElementById('loginAttribute').value == "email"){
        document.getElementById('warningforemail').style.display = "block";
    } else {
        document.getElementById('warningforemail').style.display = "none";
    }
}
function updateMetadata(spEntityID,acsUrl) {
    var table = document.getElementById("metadata-table");
    table.rows[0].cells[1].innerHTML = "<p id=\"sp-entity-id\" class=\"copyable\" title=\"Click to Copy\">" + spEntityID + "</p>";
    table.rows[1].cells[1].innerHTML = "<p id=\"acs-url\" class=\"copyable\" title=\"Click to Copy\">" + acsUrl + "</p>";
    console.log("update metadata");
}

function showSaveButton(){
    if (document.getElementById('config-idp-save').style.display = "none"){
        document.getElementById('config-idp-save').style.display = "inline";
    }
}