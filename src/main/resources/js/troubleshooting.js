
AJS.$(document).ready(function() {
    AJS.$(".aui-nav li").removeClass("aui-nav-selected");
    AJS.$("#").addClass("aui-nav-selected");

    AJS.$("#test-configurations-section").click(function () {
        var x = AJS.$("#test-configurations-section-div");
        if (x.css('display') == 'none') {
            x.show();
            AJS.$("#test-configurations-section").text(" Hide Test Configuration");
            setCookie('mo.jira-sso.test-configurations-section', true);
        } else {
            x.hide();
            AJS.$("#test-configurations-section").text(" Show Test Configuration");
            setCookie('mo.jira-sso.test-configurations-section', false);
        }
    });



    AJS.$("#troubleshooting-section").click(function () {
        var x = AJS.$("#troubleshooting-section-div");
        if (x.css('display') == 'none') {
            x.show();
            AJS.$("#troubleshooting-section").text(" Hide Troubleshooting Section");
            setCookie('mo.jira-sso.troubleshooting-section', true);
        } else {
            x.hide();
            AJS.$("#troubleshooting-section").text(" Show Troubleshooting Section");
            setCookie('mo.jira-sso.troubleshooting-section', false);
        }
    });

});

function showLogFile() {
    var x = document.getElementById('log-file-steps-div');
    if (x.style.display === "none") {
        AJS.$('#log-file-steps-div').slideDown("slow");
        x.style.display = "block";
    } else {
        x.style.display = "none";
    }
}

function downloadRequest() {
    var textToWrite = document.getElementById('reqmessage').value;
    var blob = new Blob([textToWrite],{ type: 'text/xml;charset=utf-8;'});
    var fileNameToSaveAs = 'SAMLRequest.xml';
    var downloadLink = document.createElement('a');
    downloadLink.download = fileNameToSaveAs;
    downloadLink.innerHTML = 'Download File';
    if(window.navigator.msSaveOrOpenBlob){
        navigator.msSaveBlob(blob, 'SAMLRequest.xml');
    }
    if (window.webkitURL != null){
        downloadLink.href = window.webkitURL.createObjectURL(blob);
    }
    else{
        downloadLink.href = window.URL.createObjectURL(blob);
        downloadLink.onclick = destroyClickedElement;
        downloadLink.style.display = 'none';
        document.body.appendChild(downloadLink);
    }
    downloadLink.click();
}

function destroyClickedElement(event){
    document.body.removeChild(event.target);
}

function downloadResponse() {
    var textToWrite = document.getElementById('resmessage').value;
    var blob = new Blob([textToWrite],{type: 'text/xml;charset=utf-8;'});
    var fileNameToSaveAs = 'SAMLResponse.xml';
    var downloadLink = document.createElement('a');
    downloadLink.download = fileNameToSaveAs;
    downloadLink.innerHTML = 'Download File';
    if(window.navigator.msSaveOrOpenBlob){
        navigator.msSaveBlob(blob, 'SAMLResponse.xml');
    }

    if (window.webkitURL != null){
        downloadLink.href = window.webkitURL.createObjectURL(blob);
    } else{
        downloadLink.href = window.URL.createObjectURL(blob);
        downloadLink.onclick = destroyClickedElement;
        downloadLink.style.display = 'none';
        document.body.appendChild(downloadLink);
    }
    downloadLink.click();
}

function destroyClickedElement(event){
    document.body.removeChild(event.target);
}
