<html>
<head>
    <meta name="decorator" content="atl.general"/>
    <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
    <script>

        jQuery(document).ready(function () {
            jQuery(".aui-nav li").removeClass("aui-nav-selected");
            jQuery("#CONFIGURE").addClass("aui-nav-selected");

        });

        /**
         * Displays or hides certificate details. Later on a cookie can be added to save the state of details
         */
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

        function copyToClipboard(element, copyButton) {
            var $temp = $("<input>");
            $("body").append($temp);
            $temp.val($(element).text()).select();
            document.execCommand("copy");
            $temp.remove();
            $(copyButton).show();
            setTimeout(function () {
                $(copyButton).hide("slow");
            }, 5000);
        }

        function copyCertificate(element, copyButton) {
            var $temp = $("<textarea id=\"certificateAsText\" cols=\"64\" rows=\"4\"></textarea>");
            $("body").append($temp);
            document.getElementById('certificateAsText').value += $(element).text();
            document.querySelector("#certificateAsText").select();
            document.execCommand("copy");
            $temp.remove();
            $(copyButton).show();
            setTimeout(function () {
                $(copyButton).hide("slow");
            }, 5000);
        }

        function showPopUp() {
            AJS.$("#metadata_success").remove();
            document.getElementById('metadata_popup_form').style.display = "block";
        }

        function closePopUp() {
            AJS.$("#metadata_success").remove();
            document.getElementById('metadata_popup_form').style.display = "none";
        }

    </script>
    <style>
        .aui-page-panel-content {
            padding: 0px !important;
        }

        .aui-page-panel {
            margin: 0px !important;
        }

        .show-title:after {
            content: attr(title);
            font-weight: bold;
            color: white;
            padding: 5px 10px;
            margin-left: 20px;
            border-radius: 2px;
            border: 1px solid #2D4D4D;
            background: #2D4D4D;
        }

        /* Popup box BEGIN */
        .metadata_popup_form {
            background: rgba(0, 0, 0, 0.4);
            cursor: pointer;
            display: none;
            height: 100%;
            position: fixed;
            text-align: center;
            top: 0;
            width: 100%;
            z-index: 10000;
            margin-left: -31px;
            overflow: auto;
        }

        .metadata_popup_form .vertical_align {
            display: inline-block;
            height: 100%;
            vertical-align: middle;
        }

        .metadata_popup_form > div {
            background-color: #fff;
            box-shadow: 10px 10px 60px #555;
            display: inline-block;
            height: auto;
            max-width: 551px;
            min-height: 100px;
            vertical-align: middle;
            width: 60%;
            position: relative;
            border-radius: 8px;
            padding: 15px 5%;
            overflow: auto;
        }

        .close_popup {
            background-color: #fff;
            border-radius: 8px;
            cursor: pointer;
            display: inline-block;
            font-family: arial;
            font-weight: bold;
            position: absolute;
            top: -0px;
            right: -0px;
            font-size: px;
            line-height: 30px;
            width: 30px;
            height: 30px;
            text-align: center;
        }

        .close_popup:hover {
            background-color: #ccc;
        }

        /* Popup box BEGIN */
    </style>
</head>
<body>
<#include "*/header.ftl" parse=true >
<div class="tabs-pane active-pane" id="configure-idp" role="tabpanel">
    <p style="font-size:13pt;">Configure Service Provider</p>
    <hr class="header"/>
    <form id="admin" class="aui" action="${req.contextPath}/plugins/servlet/downloadidpguides" method=""
          target="_blank">
        <p style="font-size:13pt;"><b>Step 1:</b> Choose your Identity Provider from the list below to view its setup
            guide:</p>
        <div class="field-group">
            <select class="select" name="idpGuides" id="idpGuides" style="width:200px;">
                <#foreach guide in idpGuides>
                    <option value="${guide}"
                            <#if idpGuides.equals(guide)>
                                selected
                            </#if>
                    >${guide}
                    </option>
                </#foreach>
            </select>
            <input type="submit" value="View" class="aui-button aui-button-primary" style="width:100px;"/>
        </div>
    </form>
    <p>Your IdP is not in the list? Contact us using <b>Support/Feedback</b> widget and we will help you set it up very
        quickly.
    <p>
    <hr class="header"/>
    <p>Provide this metadata to your Identity Provider to enable Bamboo as a service provider/relying party:<br>

        <#assign metadata = spBaseUrl + "/plugins/servlet/saml/metadata">
        <a href="${metadata}" target="_blank" class="aui-button aui-button-link">${metadata}</a>

        <#assign downloadMetadataEndpoint = spBaseUrl + "/plugins/servlet/saml/downloadmetadata">
        <a href="${downloadMetadataEndpoint}">
            <input type="button" class="aui-button" value="Download Metadata">
        </a>

        <input type="button" class="aui-button" value="Customize metadata" onclick="showPopUp()" id="metadata_form">
        <div class="metadata_popup_form" id="metadata_popup_form">
            <span class="vertical_align"></span>
            <div style="text-align: left">
                <div class="close_popup" id="close_popup" onclick="closePopUp()">x</div>
                <div style="text-align: center"><h2>Customize Metadata</h2></div>
                <br>
                <form id="set-certificates-form" class="aui" action="" method="POST">
                    <input type="hidden" name="certificatesSubmitted" value="true"/>
    <p><b>Organization Details</b></p>
    <hr class="header">
    <div class="field-group">
        <label for="customOrganizationName">Organization name:</label>
        <input type="text" id="customOrganizationName" name="customOrganizationName"
               value="${customOrganizationName}" class="text long-field" required>
    </div>
    <div class="field-group">
        <label for="customOrganizationDisplayName">Organization display name:</label>
        <input type="text" id="customOrganizationDisplayName" name="customOrganizationDisplayName"
               value="${customOrganizationDisplayName}" class="text long-field" required>
    </div>
    <div class="field-group">
        <label for="customOrganizationUrl">Organization URL:</label>
        <input type="url" id="customOrganizationUrl" name="customOrganizationUrl"
               value="${customOrganizationUrl}" class="text long-field">
    </div>
    <p><b>Technical contact Details</b></p>
    <hr class="header">
    <div class="field-group">
        <label for="technicalContactName">Technical contact name:</label>
        <input type="text" id="technicalContactName" name="technicalContactName"
               value="${technicalContactName}" class="text long-field" required>
    </div>
    <div class="field-group">
        <label for="technicalContactEmail">Technical contact email:</label>
        <input type="email" id="technicalContactEmail" name="technicalContactEmail"
               value="${technicalContactEmail}" class="text long-field" required>
    </div>
    <p><b>Support contact Details</b></p>
    <hr class="header">
    <div class="field-group">
        <label for="supportContactName">Support contact name:</label>
        <input type="text" id="supportContactName" name="supportContactName"
               value="${supportContactName}"
               class="text long-field" required>
    </div>
    <div class="field-group">
        <label for="supportContactEmail">Support contact email:</label>
        <input type="email" id="supportContactEmail" name="supportContactEmail"
               value="${supportContactEmail}"
               class="text long-field" required>
    </div>
    <hr/>
    <div class="">
        <#if signing == true>
            <input type="checkbox" checked="true" id="signing" value="true" name="signing">
            <span>Include Signing Certificate in Metadata</span>
        <#else>
            <input type="checkbox" id="signing" value="true" name="signing">
            <span>Include Signing Certificate in Metadata</span>
        </#if>
        <div class="description">
                        <span> Include signing certificate in the SAML Metadata to inform IdP that SP has signed request. To disable signing request, please uncheck this value and also uncheck <b>Send Signed Requests</b> in <a
                                    href="listidp.action">Configure IDP</a>.</span>
        </div>
        <#if  settings.getSignedRequest() == false && settings.getSigning() == true>
            <div class="description">
                <div class="aui-message aui-message-warning">
                    <span> Warning: Signed Request is disabled in Configure IDP tab.</span>
                </div>
            </div>
        </#if>
    </div>

    <div class="">
        <#if encryption == true>
            <input type="checkbox" checked="true" id="encryption" value="true" name="encryption">
            <span>Include Encryption Certificate in Metadata</span>
        <#else>
            <input type="checkbox" id="encryption" value="true" name="encryption">
            <span>Include Encryption Certificate in Metadata</span>
        </#if>
        <div class="description">
            <span> Include encryption certificate in the SAML Metadata. This allows the IdP to encrypt Assertions.</span>
        </div>
        <#if isJCEInstalled == false && encryption == true>
            <div class="description">
                <div class="aui-message aui-message-warning">
								<span>Warning: The Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files are not installed on this system. It is likely that decrypting Assertions will fail.
								<br>Download JCE Unlimited Strength Jurisdiction Policy Files for your java version. Extract the jar files from the zip and save them in <b>${java.home}/jre/lib/security/</b>.</span>
                </div>
            </div>
        </#if>
    </div>
    <div class="field-group">
        <input type="button" value="Save" id="submit_metadata" class="aui-button aui-button-primary"
               style="width:100px; margin-left: 20%"/>
    </div>
    </form>
</div>
</div>
<p style="margin-left: 200px;">OR</p>
<p>Use these values below to add Bamboo as service provider/relying party in your Identity Provider::</p>

<table class="aui aui-table-interactive">
    <tbody>
    <tr>
        <td headers="name" width="30%">
            <b>SP Entity ID / Issuer</b>
        </td>
        <td headers="type" width="40%">
            <p id="p1">${settings.getSpEntityId()}</p>
        </td>
        <td width="30%">
            <button class="mo-copy aui-button" title="Copy to clipboard" onclick="copyToClipboard('#p1','#c1')"><i
                        class="fa fa-copy"></i> Copy
            </button>
            <span id="c1" title="Copied" class="show-title" hidden></span>
        </td>
    </tr>
    <tr>
        <td headers="name">
            <b>ACS URL</b>
        </td>
        <td headers="type">
            <p id="p2">${settings.getLoginServletUrl()}</p>
        </td>
        <td>
            <button class="mo-copy aui-button" title="Copy to clipboard" onclick="copyToClipboard('#p2','#c2')"><i
                        class="fa fa-copy"></i> Copy
            </button>
            <span id="c2" title="Copied" class="show-title" hidden></span>
        </td>
    </tr>
    <tr>
        <td headers="name">
            <b>Single Logout URL</b>
        </td>
        <td headers="type">
            <p id="p3">${settings.getLogoutServletUrl()}</p>
        </td>
        <td>
            <button class="mo-copy aui-button" title="Copy to clipboard" onclick="copyToClipboard('#p3','#c3')"><i
                        class="fa fa-copy"></i> Copy
            </button>
            <span id="c3" title="Copied" class="show-title" hidden></span>
        </td>
    </tr>
    <tr>
        <td headers="name">
            <b>Audience URI</b>
        </td>
        <td headers="type">
            <p id="p4">${settings.getSpEntityId()}</p>
        </td>
        <td>
            <button class="mo-copy aui-button" title="Copy to clipboard" onclick="copyToClipboard('#p4','#c4')"><i
                        class="fa fa-copy"></i> Copy
            </button>
            <span id="c4" title="Copied" class="show-title" hidden></span>
        </td>
    </tr>
    <tr>
        <td headers="name">
            <b>Recipient URL</b>
        </td>
        <td headers="type">
            <p id="p5">${settings.getLoginServletUrl()}</p>
        </td>
        <td>
            <button class="mo-copy aui-button" title="Copy to clipboard" onclick="copyToClipboard('#p5','#c5')"><i
                        class="fa fa-copy"></i> Copy
            </button>
            <span id="c5" title="Copied" class="show-title" hidden></span>
        </td>
    </tr>
    <tr>
        <td headers="name">
            <b>Destination URL</b>
        </td>
        <td headers="type">
            <p id="p6">${settings.getLoginServletUrl()}</p>
        </td>
        <td>
            <button class="mo-copy aui-button" title="Copy to Clipboard" onclick="copyToClipboard('#p6','#c6')"><i
                        class="fa fa-copy"></i> Copy
            </button>
            <span id="c6" title="Copied" class="show-title" hidden></span>
        </td>
    </tr>
    <tr>
        <td headers="name">
            <b>Certificate</b>
        </td>
        <td headers="type">
            <a href="downloadcertificate.action">
                <input type="button" class="aui-button aui-button-link" value="Download"/>
            </a>
            &nbsp;&nbsp;&nbsp;
            <!-- Link to show or hide certificate details -->
            <a id="show-certificate-details"
               data-replace-text="Hide Certificate Details"
               class="show-certificate-details"
               aria-controls="certificateInfoDiv"
               onclick="showCertificateDetails()"
               style="cursor:pointer">Show Certificate Details
            </a>
            <!-- Certificate details. certificateInfo map is used. Hidden by default. -->
            <div id="certificateInfoDiv" style="display: none;">
                <table style="border: 1px solid;">
                    <#foreach key in certificateInfo.keySet()>
                        <#assign certificateInfoValue = certificateInfo.get(key)>
                        <tr>
                            <th>${key}:</th>
                            <td>${certificateInfoValue}</td>
                        </tr>
                    </#foreach>
                </table>
            </div>
            <div class="description" style="font-size:12px;">Note: If the IdP requires signed requests, the IdP will
                need this certificate to validate requests. It is also used to decrypt encrypted SAML Assertions
                from the IdP.After download, open in notepad to copy certificate. You can configure your own
                certificates from <a href="customcerts.action">here</a>.
            </div>
        </td>

        <td width="30%">
            <p id="p7"
               style="font-family:Courier New;display: none" cols="64" rows="4">${settings.getPublicSPCertificate()}
            </p>
            <button class="mo-copy aui-button" title="Copy to clipboard" onclick="copyCertificate('#p7','#c7')"><i
                        class="fa fa-copy"></i> Copy
            </button>
            <span id="c7" title="Copied" class="show-title" hidden></span>
        </td>
    </tr>
    </tbody>
</table>
<p style="font-size:13pt;">Configure Service Provider URLs (Optional)</p>

<form id="configure-idp-form" class="aui" action="" method="POST">
    <input type="hidden" name="configureIdpSubmitted" value="true"/>
    <input type="hidden" name="atl_token" value="${xsrfToken}"/>
    <div class="field-group">
        <label for="spBaseUrl">SP Base URL:
            <span class="aui-icon icon-required">(required)</span>
        </label>
        <input type="url" required="true" id="spBaseUrl" name="spBaseUrl" value="${spBaseUrl}"
               class="text long-field"/>
        <div class="description">
            <span>If your site is behind a proxy you can modify SP Base URL for Single Sign-On to work. If not provided, Bamboo base URL will be used by default.</span>
        </div>
    </div>
    <div class="field-group">
        <label for="spEntityId">SP Entity ID:
            <span class="aui-icon icon-required">(required)</span>
        </label>
        <input type="text" required="true" id="spEntityId" name="spEntityId" value="${spEntityId}"
               class="text long-field"/>
        <div class="description">
            <span>Set the Entity ID for the Service Provider. If not provided, Bamboo base URL will be used by default.</span>
        </div>
    </div>
    <div class="field-group">
        <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>
    </div>
</form>
<hr class="header"/>

<p style="font-size:13pt;"><b>Step 2:</b> <a href="listidp.action">Import IdP Metadata </a> or note down the
    following information from your IdP and keep it handy. Click the next button below when you are ready.</p>
<ol>
    <li>X.509 Signing Certificate</li>
    <li>IdP Entity ID or Issuer</li>
    <li>Single Sign-On Service URL</li>
    <li>Single Logout URL(optional)</li>
</ol>
<br>
<div class="field-group"><a href="listidp.action"><input type="submit" value="Next"
                                                         class="aui-button aui-button-primary"
                                                         style="width:100px;"/></a></div>
</div>
</div>
</section>
</div>
</div>
</body>
</html>
