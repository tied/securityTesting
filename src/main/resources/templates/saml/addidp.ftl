<html>
<head>
    <meta name="decorator" content="atl.general"/>
    <script>
        <#include "/js/addidp.js">
        <#include "/js/spmetadata.js">
    </script>
    <style>
        <#include "/css/addidp.css">
        <#include "/css/spmetadata.css">
    </style>
</head>
<body>
<#include "*/saml/headers/idpconfigheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="configure-sp" role="tabpanel">
    <h1 style="font-size: 28px; font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Noto Sans, Oxygen, Ubuntu, Droid Sans, Helvetica Neue, sans-serif">SSO Endpoints
        <span class="buttons_in_heading">
             <a href = "https://plugins.miniorange.com/bamboo-saml-sso-guides" class="aui-button aui-button-primary" target="_blank">View Setup Guides</a>
        </span>
        <span class="buttons_in_heading">
            <button id="show_sp_metadata_popup" class="aui-button aui-button-primary" target="_blank" resolved="">SP Information</button>
        </span>
    </h1>
    <#include "*/saml/spmetadatapopup.ftl" parse=true>
    <hr class="header"/>

    <nav class="aui-navgroup aui-navgroup-horizontal" id="min_topmenu">
        <div class="aui-navgroup-inner">
            <div id="navbar" class="aui-navgroup-primary">
                <div class="tab idp-config">
                    <ul class="aui-nav">
                        <li>
                            <a class="tablinks active" onclick="openTab(event, 'conf-sp')" id="defaultOpen">Manual Configuration</a>
                        </li>
                        <li>
                            <a class="tablinks" onclick="openTab(event, 'upload-metadata')">Import From Metadata</a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </nav>

    <div id="upload-metadata" class="tabcontent" style="display: none; margin-top:15px;">

        <form id="metadata_import_form" name="metadata_import_form" method="post" action="metadataupload" class="aui long-label" enctype="multipart/form-data">
            <input type="hidden" name="metadataImported" value="true">
            <input type="hidden" id="idpID" name="idpID" value="${idpID}">
            <input type="hidden" name="atl_token" value="${xsrfToken}" />
            <input type="hidden" name="setupType" value="manualSetup">
            <table style="width:100%">
                <tr>
                    <td width="20%">
                        IDP Name:
                        <span style="color: red">*</span>
                    </td>
                    <td>
                        <input type="text" required="true" id="idpNameImport" name="idpName" value="${idpName}" placeholder="Enter the name for your IDP"
                               class="text long-field" />
                        <div class="description">This IDP Name will be shown in the login widget to users.</div>
                    </td>
                </tr>
                <tr><td>&nbsp;</td></tr>
                <tr>
                    <td width="20%">Select IDP:</td>
                    <td>
                        <div id="selectIDP">
                            <select class="select" id="metadataOption" name="metadataOption" style="width:200px">

                                <option disabled hidden <#if metadataOption.equals("")> selected </#if> >Select IDP</option>

                                <optgroup label = "Supported IDPs">
                                    <#foreach option in metadataOptions>
                                        <option value="${option}"
                                                <#if metadataOption.equals(option)> selected </#if> >${option}</option>
                                    </#foreach>
                                </optgroup>
                                <optgroup label = "Custom">
                                    <option value="fromFile" <#if metadataOption.equals("fromFile")> selected </#if> >Import From Metadata File </option>
                            <option value="fromUrl" <#if metadataOption.equals("fromUrl")>selected </#if> > Import From Metadata URL </option>
                                </optgroup>
                            </select>
                        </div>
                    </td>
                </tr>
            </table>
            <br/>
            <div id="importByFile" style="display: block;">
                <table style="width:100%;">
                    <tr>
                        <td id="metadata_file_label" width="20%"></td>

                        <td>
                            <input type="hidden" id="fileSubmitted" name="fileSubmitted" value="true"/>
                            <input type="file" id="xmlFile" name="xmlFile" class="ignore-inline-attach" accept=".xml" style=" display:none;"/>
                            <div id="metadata_file_description" class="description"></div>
                        </td>
                    </tr>
                </table>
            </div>
            <div id="importByUrl" style="display: none;">
                <table style="width:100%;">
                    <tr>
                        <td id="metadata_url_label" width="20%"></td>
                        <td>
                            <input type="hidden" id="urlSubmitted" name="urlSubmitted" value="true" />
                            <input type="url" id="inputUrl" name="inputUrl" required value="${inputUrl}" placeholder="Enter metadata URL of your IdP" style="width:70%; padding: 5px;" class="text long-field"/>
                            <div id="metadata_url_description" class="description"></div>
                        </td>
                    </tr>
                    <tr id="effective_metadata_url_div" style="outline: thin solid black;" hidden>
                        <td width="20%">Effective Metadata URL:</td>
                        <td>
                            <p id="effective_metadata_url" style="background-color: #f5f5f5; font-weight: bold"> </p>
                            <div class="description">You can go to <a href="advancedsso.action?idpid=${idpID}"> Advanced SSO Settings </a> tab to enable automatic metadata rollover</div>
                        </td>
                    </tr>
                </table>
            </div>
            <br/>
            <div id="importButtons" style="display: none;">
                <table style="width: 100%">
                    <tr>
                        <td style="width: 20%">&nbsp;</td>
                        <td>
                            <input type="submit" class="aui-button aui-button-primary" value="Import"/>
                        </td>
                    </tr>
                </table>
            </div>
        </form>
    </div>
    <div id="conf-sp" class="tabcontent" style="display: block;">

        <form id="configure-sp-form" class="aui long-label" action="" method="POST">
            <p>Click on
                <b>Import From Metadata</b> to fetch IDP's settings from IDP metadata URL or XML file.</p>
            <p>Need help with the configuration?
                Contact us using the <b>support/Feedback</b> widget or write to us at <a href="mailto:info@xecurify.com">info@xecurify.com</a> and we will help you set it up very quickly.<p>
            <br>

            <input type="hidden" name="addIdpSubmitted" value="true"/>
            <input type="hidden" name="idpID" value="${idpID}">
            <input type="hidden" name="atl_token" value="${xsrfToken}" />
            <div id="newAcsUrl" class="field-group">
                <label for="acsUrl"> ACS URL for IDP-Initiated SSO</label>
                <span title="click to copy" style="cursor:default"> <span id="acsUrl" onmousedown="copy(this,'#c1')"><strong>${acsUrl}</strong></span>&nbsp;&nbsp;
                    <span id="c1" title="Copied" class="show-title" style="display:none"></span></span>
                <div class="description">Update this ACS URL in IDP if you want to use IDP-Initiated SSO (optional). For copying click on ACS url.</div>

            </div>
            <div class="field-group">
                <label for="idpName">IDP Name:
                    <span class="aui-icon icon-required">(required)</span>
                </label>
                <input type="text" required="true" id="idpNameImport" name="idpName" value="${idpName}" placeholder="Enter the name for your IDP"
                       class="text long-field" />
                <div class="description">This IDP Name will be shown in the login widget to users.</div>
            </div>
            <div class="field-group">
                <label for="idpEntityId">IDP Entity ID / Issuer:
                    <span class="aui-icon icon-required">(required)</span>
                </label>
                <input type="text" required="true" id="idpEntityId" name="idpEntityId" value="${idpEntityId}" class="text long-field" />
                <div class="description">
                    Enter the Entity ID or Issuer value of your Identity Provider. You can find its value in the entityID attribute of EntityDescriptor tag in IdP-Metadata XML file.
                </div>
            </div>
            <br>
            <div class="field-group">
                <label for="signedRequest">Send Signed Requests:</label>
                <aui-toggle id="signedRequest" name="signedRequest"
                    <#if signedRequest==true> checked="true" </#if> label="signedRequest" value="true"></aui-toggle>
                <#if signedRequest == true && settings.getSigning() == false>
                    <div class="description">
                        <div class="aui-message aui-message-warning">
                            <span> Warning: Signing certificate is disabled in IDP configuration. Please enable <b>Include Signing Certificate in Metadata</b> in
                                <a href="spmetadata.action?id=mo-sp-metadata">Service Provider Information</a>.</span>
                        </div>
                    </div>
                </#if>
                <div class="description">
                    It is recommended to keep it checked. Uncheck, only if your IdP is not accepting Signed SAML Request.
                </div>
            </div>
            <br>
            <fieldset class="group">
                <legend>
                    <span>SSO Binding Type:</span>
                </legend>
                <#if ssoBindingType == "HttpRedirect">
                    <input class="radio" type="radio" name="ssoBindingType" id="httpRedirectsso" value="HttpRedirect"
                           checked="checked"/>
                <#else>
                    <input class="radio" type="radio" name="ssoBindingType" id="httpRedirectsso" value="HttpRedirect"/>
                </#if>
                <label for="httpRedirectsso">Use HTTP-Redirect Binding for SSO</label>

                <#if ssoBindingType == "HttpPost">
                    <input class="radio" type="radio" name="ssoBindingType" id="httpPostsso" value="HttpPost"
                           checked="checked"/>
                <#else>
                    <input class="radio" type="radio" name="ssoBindingType" id="httpPostsso" value="HttpPost"/>
                </#if>
                <label for="httpPostsso">Use HTTP-Post Binding for SSO</label>
            </fieldset>
            <br>
            <div class="field-group">
                <label for="ssoUrl">Single Sign On URL:
                    <span class="aui-icon icon-required">(required)</span>
                </label>
                <input type="url" required="true" id="ssoUrl" name="ssoUrl" value="${ssoUrl}" class="text long-field" />
                <div class="description">
                    Enter the Single Sign-on Service endpoint of your Identity Provider. You can find its value in SingleSignOnService tag (Binding type: HTTP-Redirect) in IdP-Metadata XML file.
                </div>
            </div>
            <br/>

            <fieldset class="group">
                <legend>
                    <span>SLO Binding Type:</span>
                </legend>
                <#if sloBindingType == "HttpRedirect">
                    <input class="radio" type="radio" name="sloBindingType" id="httpRedirectslo" value="HttpRedirect" checked="checked" />
                <#else>
                    <input class="radio" type="radio" name="sloBindingType" id="httpRedirectslo" value="HttpRedirect" />
                </#if>
                    <label for="httpRedirectslo">Use HTTP-Redirect Binding for SLO</label>

                <#if sloBindingType == "HttpPost">
                    <input class="radio" type="radio" name="sloBindingType" id="httpPostslo" value="HttpPost" checked="checked" />
                <#else>
                    <input class="radio" type="radio" name="sloBindingType" id="httpPostslo" value="HttpPost" />
                </#if>
                    <label for="httpPostslo">Use HTTP-Post Binding for SLO</label>
            </fieldset>
            <br>
            <div class="field-group">
                <label for="sloUrl">Single Logout URL:</label>
                <input type="url" id="sloUrl" name="sloUrl" value="${sloUrl}" class="text long-field"/>
                <div class="description">
                    Enter the Single Logout Service endpoint of your Identity Provider. You can find its value in SingleLogoutService tag in
                    IdP-Metadata XML file. Leave blank if SLO not supported by IDP
                </div>
            </div>
            <br>
            <div class="field-group">
                <label for="nameIdFormat">NameID Format:</label>
                <select class="select" name="nameIdFormat" id="nameIdFormat" style="max-width: 500px;">
                    <#foreach nameID in nameIdFormats>
                        <option value="${nameID}"
                            <#if nameIdFormat.equals(nameID)>
                                selected
                            </#if>
                        >${nameID}
                        </option>
                    </#foreach>
                </select>
                <div class="description">
                    Select the name identifier format supported by the IdP.Select <b>unspecified</b> by default.
                </div>
            </div>
            <br>

            <div class="field-group" id="idpSigningCertificates" >
                <label for="x509Certificate">IDP Signing Certificate:
                    <span class="aui-icon icon-required">(required)</span>
                </label>
                <#if !certificates.isEmpty()>
                    <#assign loopCount = 0>
                    <#foreach x509Certificate in certificates>
                        <div>
                            <textarea id="x509Certificate" name="x509AllCertificates" class="textarea long-field"
                                      style="font-family:Courier New;" cols="64" rows="4">${x509Certificate}</textarea>
                                <#if loopCount > 0 >
                                    <input type='button' value='-' id='removeCertificate' onclick='RemoveCertificate(this)'
                                        class="aui-button" style="vertical-align: text-bottom;">
                                </#if>
                        </div>
                        <#assign loopCount++>
                    </#foreach>
                <#else>
                    <div>
                        <textarea id="x509Certificate" required="true" name="x509Certificate" class="textarea long-field"
                                  style="font-family:Courier New;" cols="64" rows="4">${x509Certificate}</textarea>
                    </div>
                </#if>
                <input type='button' value='+' id='addIDPCertificate' class="aui-button aui-button-primary">
                
                <div class="description">This Certificate is used to validate SAML response from Identity Provider. You can find its value in X509Certificate tag in IdP-Metadata XML file. (parent tag: KeyDescriptor use="signing"). If your IDP provided you the certificate file, open it
                    in Notepad and copy/paste the content here.
                    <br>
                    Click on the "+" button to configure more than one IDP certificates.    
                </div>
            </div>
            <br/>
            <div class="field-group">
                <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:170px;"/>
                &nbsp;&nbsp;&nbsp;
                <#if "${idpEntityId}" != "">
                    <input type="button" id="test-saml-configuration" value="Test Configuration" style="width:170px;" class="aui-button" />
                <#else>
                    <button class="aui-button" aria-disabled="true" style="width:170px;" disabled>Test Configuration</button>
                </#if>
            </div>
        </form>
    </div>
</div>
<#include "*/footer.ftl" parse = true>
</body>
</html>