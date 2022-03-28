AJS.$(function () {
    AJS.$("#show_sp_metadata_popup").click(function() {
        AJS.dialog2(".sp_metadata_dialog").show();
    });
    AJS.$("#sp_metadata_dialog_close_button").click(function(e) {
        e.preventDefault();
        AJS.dialog2(".sp_metadata_dialog").hide();
    });

    AJS.$("#show_customize_metadata_popup").click(function() {
        AJS.dialog2("#sp_customize_metadata_dialog").show();
        document.forms['customize_metadata_form'].elements['customOrganizationName'].focus();
    });
    AJS.$("#sp_customize_metadata_dialog_close_button").click(function(e) {
        e.preventDefault();
        AJS.dialog2("#sp_customize_metadata_dialog").hide();
    });

    AJS.$("#customize_metadata_form").keyup(function (event) {
        event.preventDefault();
        if (event.keyCode === 13) { //on clicking Enter key
            document.getElementById("submit_metadata").click();
        }
    });
    AJS.$("#submit_metadata").click(function () {
        AJS.$.ajax({
            url: AJS.contextPath() + "/plugins/servlet/saml/updatemetadata",
            type: "POST",
            data: AJS.$("#customize_metadata_form").serialize(),
            success: function () {
                showSuccessMessage("Settings Updated Successfully");
            },
        });
    });
});

function copyToClipboard(element, copyButton) {
    var $temp = AJS.$("<input>");
    AJS.$("body").append($temp);
    $temp.val(AJS.$(element).text()).select();
    document.execCommand("copy");
    $temp.remove();
    AJS.$(copyButton).show();
    setTimeout(function () {
        AJS.$(copyButton).hide("slow");
    }, 2500);
}

/* Displays or hides certificate details. Later on a cookie can be added to save the state of details*/
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

AJS.$(document).on('click',"#show_sp_metadata_popup",function(){
    AJS.dialog2(".sp_metadata_dialog").show();
});

/*
 * copyCertificate is same as copyToclipboard except <textarea> is used instead of <input> to maintain the formatting of certificate
 * @param  {string} element    ID of element p which has certificate text
 * @param  {string} copyButton ID of 'Copied' text
 */
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