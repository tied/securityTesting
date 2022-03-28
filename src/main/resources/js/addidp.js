AJS.$(function () {
	AJS.$(".aui-nav li").removeClass("aui-nav-selected");
	AJS.$("#mo-idps-config").addClass("aui-nav-selected");
	AJS.$("#mo-saml").addClass("aui-nav-selected");
	var adfs = {
		label: "Enter AD FS hostname:",
		description: "Enter your AD FS hostname here. For example, <code>adfs.yourdomain.com</code>",
	};

	var azureAD = {
		label: "Enter Azure AD domain name:",
		description: "Enter your Azure AD domain name here. For example, <code>contoso.onmicrosoft.com</code>"
	};

	var oneLogin = {
		label: "Enter OneLogin metadata url:",
		description: "Open your SP app on OneLogin. Proceed to app settings -> More Actions -> Right click on SAML Metadata -> Copy Link Address"
	};

	var fromUrl = {
		label: "Enter IDP Metadata URL:",
		description: "This URL is used to fetch your IDP settings. Please make sure that URL is accessible. Reach out to us using Support if you need any help"
	};

	var okta = {
		label: "Enter Okta Metadata URL:",
		description: "Open your application on Okta, Go to <b>Sign on</b> tab Click on <b>Identity Provider metadata</b> to get  metadata URL"

	};

	var google = {
		label: "Upload Google G suite Metadata File:",
		description: "While creating a SAML app on Google, click on download button beside <b>IDP metadata</b> in step 2 <b>Google IDP Information</b>"
	};

	var fromFile = {
		label: "Upload IDP Metadata File:",
		description: "This file is used to fetch your IDP settings. Reach out to us using Support if you need any help"
	}

	/**
	 * map is used to store the idp objects with the option value. This map is used in changeFieldValues() and changeFileFieldValues() functions
	 */
	var map = {};
	map['ADFS'] = adfs;
	map['Azure AD'] = azureAD;
	map['OneLogin'] = oneLogin;
	map['fromUrl'] = fromUrl;
	map['Okta'] = okta;
	map['Google'] = google;
	map['fromFile'] = fromFile;
	loadAuthContextSetting();
	showImportFromMetadata();
	showEffectiveMetadataUrl();


	function getQueryParameterByName(name) {
		var url = window.location.href;
		name = name.replace(/[\[\]]/g, "\\$&");
		var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
			results = regex.exec(url);
		if (!results) return null;
		if (!results[2]) return '';
		return decodeURIComponent(results[2].replace(/\+/g, " "));
	}

	function enableRefreshMetadataOptions() {
		var metadataRollover = document.getElementById("refreshMetadata");
		metadataRollover = metadataRollover.checked ? false : true;
		if (!metadataRollover) {
			document.getElementById('refreshMetadataOptions').style.display = "block";
		} else {
			document.getElementById('refreshMetadataOptions').style.display = "none";
		}
	}
/*	AJS.$(".manual-configuration-tab").click(function (evt) {
		var tabName = 'conf-sp';
		if (this.id === 'import-tab')
			tabName = 'upload-metadata';

		var tabcontent = document.getElementsByClassName("tabcontent");
		for (i = 0; i < tabcontent.length; i++) {
			tabcontent[i].style.display = "none";
		}
		var tablinks = document.getElementsByClassName("tablinks");
		for (i = 0; i < tablinks.length; i++) {
			tablinks[i].className = tablinks[i].className.replace(" active", "");
		}
		document.getElementById(tabName).style.display = "block";
		evt.currentTarget.className += " active ";
	});*/

	function changeFileFieldValues(idp) {
		var idpObj = map[idp];

		document.getElementById("metadata_file_label").innerHTML = idpObj.label;

		document.getElementById("metadata_file_description").innerHTML = idpObj.description;
	}
	AJS.$("#test-saml-configuration").click(function () {
		var osDestination = "testidpconfiguration";
		var samlAuthUrl = AJS.contextPath() + '/plugins/servlet/saml/auth';
		samlAuthUrl += "?return_to=" + encodeURIComponent(osDestination);
		idpID = AJS.$("#idpID").val();
		samlAuthUrl += "&idp=" + encodeURIComponent(idpID);
		var myWindow = window.open(samlAuthUrl, "TEST SAML IDP", "scrollbars=1 width=800, height=600");
	});

	AJS.$(document).on('keyup keypress blur', '#inputUrl', function () {
		showEffectiveMetadataUrl();

	});

	function showEffectiveMetadataUrl() {
		value = AJS.$("#inputUrl").val();
		metadataOption = AJS.$("#metadataOption").val();
		if (value != null) {
			if (value.replace(/\s/g, '').length) {
				AJS.$("#effective_metadata_url_div").show();
			} else {
				AJS.$("#effective_metadata_url_div").hide();
			}
			if (value.indexOf("https:\/\/") >= 0 || value.indexOf("http:\/\/") >= 0) {
				metadataOption = "fromUrl";
			}
			switch (metadataOption) {
				case "ADFS":
					document.getElementById('effective_metadata_url').innerHTML = "https://" + value + "/federationmetadata/2007-06/federationmetadata.xml";
					break;
				case "Azure AD":
					document.getElementById('effective_metadata_url').innerHTML = "https://login.microsoftonline.com/" + value + "/FederationMetadata/2007-06/FederationMetadata.xml";
					break;
				default:
					document.getElementById('effective_metadata_url').innerHTML = value;
			}
		}
	}
	AJS.$("#metadataOption").change(function () {
		showImportFromMetadata();
	});

	function showImportFromMetadata() {
		value = AJS.$("#metadataOption").val();
		if (value != null) {
			switch (value) {
				case "":
					hideAllForms();
					break;
				case "ADFS":
					changeFieldValues('ADFS', 'text');
					showUrlForm();
					break;
				case "Azure AD":
					changeFieldValues('Azure AD', 'text');
					showUrlForm();
					break;
				case "OneLogin":
					changeFieldValues('OneLogin', 'url');
					showUrlForm();
					break;
				case "fromUrl":
					changeFieldValues('fromUrl', 'url');
					showUrlForm();
					break;
				case "Okta":
					changeFieldValues('Okta',"url");
					showUrlForm();
					break;
				case "Google G Suite":
					changeFileFieldValues('Google');
					showFileForm();
					break;
				case "fromFile":
					changeFileFieldValues('fromFile');
					showFileForm();
					break;
			}
		}

	}

	function showFileForm() {
		AJS.$("#importByFile").show();
		AJS.$("#importByUrl").hide();
		AJS.$("#importButtons").show();
		AJS.$("#xmlFile").show();
		AJS.$("#inputUrl").prop("required", false);
		AJS.$("#xmlFile").prop("required", true);
		AJS.$("#fileSubmitted").val("true");
		AJS.$("#urlSubmitted").val("false");

	}
	AJS.$("#authnContextClass").change(function () {
		loadAuthContextSetting();
	});

	function loadAuthContextSetting() {
		if (AJS.$("#authnContextClass").val() == "Others") {
			AJS.$("#otherAuthnContextClass").prop('required', true);
			AJS.$("#otherAuthnContextClassDiv").show();
		} else {
			AJS.$("#otherAuthnContextClass").prop('required', false);
			AJS.$("#otherAuthnContextClassDiv").hide();
		}
	}

	function changeFieldValues(idp, type) {
		var idpObj = map[idp];

		document.getElementById("metadata_url_label").innerHTML = idpObj.label;
		document.getElementById("metadata_url_description").innerHTML = idpObj.description;
		var inputUrl = document.getElementById("inputUrl");
		var hostname = document.createElement('input');
		hostname.id = inputUrl.id;
		hostname.name = inputUrl.name;
		hostname.style = inputUrl.style;
		hostname.className = "text long-field";
		hostname.placeholder = idpObj.label;
		hostname.type = type;
		hostname.value = inputUrl.value;
		inputUrl.parentNode.replaceChild(hostname, inputUrl);
	}

	function showUrlForm() {
		AJS.$("#importByUrl").show();
		AJS.$("#importByFile").hide();
		AJS.$("#importButtons").show();
		AJS.$("#inputUrl").prop("required", true);
		AJS.$("#xmlFile").prop("required", false);
		AJS.$("#fileSubmitted").val("false");
		AJS.$("#urlSubmitted").val("true");
		AJS.$("#effective_metadata_url_label").hide();
		document.getElementById('effective_metadata_url').innerHTML = "";
	}
	function GetDynamicTextArea(){
        return '<textarea id="x509Certificate" name="x509AllCertificates" class="textarea long-field" '+
        'style="font-family:Courier New;" cols="64" rows="4"></textarea>'+
        '&nbsp;<input type="button" value="-" id="removeCertificate" onclick="RemoveCertificate(this)" class="aui-button" style="vertical-align: text-bottom;"/>'
    }

    AJS.$(document).on('click','#addIDPCertificate',function(){
        var div = document.createElement('DIV');
        div.innerHTML = GetDynamicTextArea();
        var idpSigningCertificates = document.getElementById("idpSigningCertificates");
        var addIDPCertificateButton = document.getElementById("addIDPCertificate");
        idpSigningCertificates.insertBefore(div, addIDPCertificateButton);
    });
})

function RemoveCertificate(div) {
    document.getElementById("idpSigningCertificates").removeChild(div.parentNode);
}

function openTab(evt, tabName) {
    var i, tabcontent, tablinks;

    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    document.getElementById(tabName).style.display = "block";
    evt.currentTarget.className += " active ";
}

