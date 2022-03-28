
<html>
<head>
    <meta name="decorator" content="atl.general"/>
    <script>
        <#include "/js/spmetadata.js">
        AJS.$(function () {
            AJS.$(".aui-nav li").removeClass("aui-nav-selected");
            AJS.$("#mo-sp-metadata").addClass("aui-nav-selected");
            AJS.$("#mo-saml").addClass("aui-nav-selected");
        });
    </script>
    <style>
        <#include "/css/spmetadata.css">
    </style>
</head>
<body>
    <#include "*/saml/headers/samlheader.ftl" parse=true>
    <div class="tabs-pane active-pane" id="configure-idp" role="tabpanel">
        <h1>Service Provider Information</h1>
        <hr class="header"/>
        <p>Provide the metadata given in this tab to your Identity Provider to enable Bamboo as a service provider/relying party:<br>
        <ul>
            <li>
                If your IDP supports importing metadata using URL then provide this metadata URL to your IDP:
                    <#assign metadata = spBaseUrl + "/plugins/servlet/saml/metadata">
                    <a href="${metadata}" target="_blank" class="aui-button aui-button-link"><b>${metadata}</b></a>
            </li>
            <p style="margin-left: 40px;">OR</p>
            <li>
                If your IDP supports importing metadata using a File then,

                    <#assign downloadMetadataEndpoint = spBaseUrl + "/plugins/servlet/saml/downloadmetadata">
                    <a href = "${downloadMetadataEndpoint}">
                        <input type="button" class="aui-button" value="Download Metadata" id="download_metadata">
                    </a>
                    and provide that file to your IDP <br>
                </p>
            </li>
            <p style="margin-left: 40px;">OR</p>
            <li>
                <p>Use the values below to add Bamboo as SP in your IDP:</p>
                <table class="aui aui-table-interactive" width="75%">
                    <tbody>
                    <tr>
                        <td headers="name" width="35%">
                            <b>SP Entity ID / Issuer</b>
                            <br>
                            <p style="font-size: 13px"><i>Note: Also known as <b>Audience URL</b></i></p>
                        </td>
                        <td headers="type" width="45%">
                            <p id="p1">${settings.getSpEntityId()}</p>
                        </td>
                        <td width="20%">
                            <button class="mo-copy aui-button" title="Copy to clipboard" onclick="copyToClipboard('#p1','#c1')"><i
                                    class="fa fa-copy"></i> Copy
                            </button>
                            <span id="c1" title="Copied" class="show-title" style="display:none"></span>
                        </td>
                    </tr>
                    <tr>
                        <td headers="name">
                            <b>ACS URL</b>
                            <br>
                            <p style="font-size: 13px"><i>Note: Also known as <b>Destination</b> or <b>Recipient URL</b></i></p>
                        </td>
                        <td headers="type">
                            <p id="p2">${settings.getLoginServletUrl()}</p>
                        </td>
                        <td>
                            <button class="mo-copy aui-button" title="Copy to clipboard" onclick="copyToClipboard('#p2','#c2')"><i
                                    class="fa fa-copy"></i> Copy
                            </button>
                            <span id="c2" title="Copied" class="show-title" style="display:none"></span>
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
                            <span id="c3" title="Copied" class="show-title" style="display:none"></span>
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
                            <a id="show-certificate-details" data-replace-text="Hide Certificate Details" class="show-certificate-details"
                                    aria-controls="certificateInfoDiv" onclick="showCertificateDetails()" style="cursor:pointer">
                               Show Certificate Details
                            </a>
                            <!-- Certificate details. certificateInfo map is used. Hidden by default. -->
                            <div id="certificateInfoDiv" style="display: none;">
                                <table style="border: 1px solid;">
                                    <#foreach key in certificateInfo.keySet()>
                                        <tr>
                                            <th>${key}:</th>
                                            <td>${certificateInfo.get(key)}</td>
                                        </tr>
                                    </#foreach>
                                </table>
                            </div>
                            <div class="description" style="font-size:12px;">Note: If the IdP requires signed requests, the IdP will
                                need this certificate to validate requests. It is also used to decrypt encrypted SAML Assertions
                                from the IdP. After download, open in notepad to copy certificate. You can configure your own
                                certificates from <a href="customcerts.action">here</a>.
                            </div>
                        </td>
                        <td width="30%">
                            <p id="p4"
                               style="font-family:Courier New;display: none" cols="64" rows="4">${settings.getPublicSPCertificate()}
                            </p>
                            <button class="mo-copy aui-button" title="Copy to clipboard" onclick="copyCertificate('#p4','#c4')"><i
                                    class="fa fa-copy"></i> Copy
                            </button>
                            <span id="c4" title="Copied" class="show-title" style="display:none"></span>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </li>
        </ul>
        <br>
        <h1>Configure Service Provider</h1>
        <hr class="header">

        <p style="font-size:12pt;">You can customize the metadata with your own organization and contact details: &nbsp;
            <a id="show_customize_metadata_popup" style="cursor:pointer;">Customize Metadata</a>
        </p>

        <#include "*/saml/customizemetadatapopup.ftl" parse=true>

        <br>
        <p style="font-size:12pt;">Change Base URLs (Optional)</p>
        <form id="configure-idp-form" class="aui long-label" action="" method="POST">
            <input type="hidden" name="submitted" value="true"/>
            <input type="hidden" name="atl_token" value="${xsrfToken}" />
            <div class="field-group">
                <label for="spBaseUrl">SP Base URL:
                    <span class="aui-icon icon-required">(required)</span>
                </label>
                <input type="url" required="true" id="spBaseUrl" name="spBaseUrl" value="${spBaseUrl}"
                       class="text long-field"/>
                <div class="description">
                    <span>If your site is behind a proxy you can modify SP Base URL for Single Sign-On to work.</span>
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
    </div>
<#include "*/footer.ftl" parse=true>
</body>
</html>