<html>
<head>
    <title>SAML SSO Configuration</title>
    <meta name="decorator" content="atl.general">
    <script>
        AJS.$(function () {
            AJS.$("#update_urls").click(function () {
                console.log("update urls submitted");
                AJS.$.ajax({
                    url: AJS.contextPath() + "/plugins/servlet/saml/moapi",
                    type: "POST",
                    data: {
                        "action": "updateUrls",
                        "updatedspbaseurl": AJS.$("#updatedSpBaseUrl").val(),
                        "updatedspentityid": AJS.$("#updatedSpEntityId").val()
                    },
                    success: function (result) {
                        updateMetadata(result.newentityid, result.loginurl);
                        AJS.$("#sp-entity-id").html(result.newentityid);
                        AJS.$("#acs-url").html(result.loginurl);
                        AJS.$("#sp-metadata-url").html('<b>' + result.metadataurl + '</b>');
                        AJS.$("#sp-metadata-url").prop("href", result.metadataurl);
                        AJS.$("#update-urls").hide();
                        AJS.flag({
                            title: 'Success!',
                            type: 'success',
                            close: 'auto',
                            body: 'Saved updated URLs'
                        });
                    },
                    error: {}
                });
            });

        <#--Default Groups select start-->
            AJS.$("#defaultGroups").auiSelect2({
                placeholder: 'Select the Default Bamboo Groups',
                ajax: {
                    url: AJS.contextPath() + '/plugins/servlet/saml/moapi',
                    data: function (params) {
                        var query = {
                            search: params,
                            action: 'fetchGroups'
                        }

                        // Query parameters will be ?search=[term]&type=public
                        return query;
                    },
                    results: function (data, page) {
                        return {
                            results: data.results
                        };
                    },

                },
                multiple: true
            });

            function decodeHtml(input) {
                var txt = document.createElement("textarea");
                txt.innerHTML = input;
                return txt.value;
            }
            var defaultGroups = [];

            <#foreach group in defaultGroupsList>
                var groupName = "${group}";
                groupName = decodeHtml(groupName);
                defaultGroups.push({
                    "id": groupName,
                    "text": groupName
                });
            </#foreach>

            AJS.$("#defaultGroups").auiSelect2('data', defaultGroups);
        <#--Default Groups select end-->

        <#--pre-fill attributes value-->

            AJS.$(".attributemapping").auiSelect2({
                placeholder: 'Add attribute value',
                width: '400px',
                ajax: {
                    url: AJS.contextPath() + '/plugins/servlet/saml/moapi',
                    data: function (params) {
                        var query = {
                            idpname: AJS.$("#idpID").val(),
                            search: params,
                            action: 'getAttributes'
                        }
                        return query;
                    },
                    results: function (data) {
                        return {
                            results: data.results
                        };
                    },
                },
            });
            AJS.$("#userNameAttribute").auiSelect2('data', {
                id: AJS.$("#userNameAttribute").val(),
                text: AJS.$("#userNameAttribute").val()
            });

            AJS.$("#userEmailAttribute").auiSelect2('data', {
                id: AJS.$("#userEmailAttribute").val(),
                text: AJS.$("#userEmailAttribute").val()
            });

            AJS.$("#fullNameAttribute1").auiSelect2('data', {
                id: AJS.$("#fullNameAttribute1").val(),
                text: AJS.$("#fullNameAttribute1").val()
            });

            AJS.$("#firstNameAttribute1").auiSelect2('data', {
                id: AJS.$("#firstNameAttribute1").val(),
                text: AJS.$("#firstNameAttribute1").val()
            });
            AJS.$("#lastNameAttribute1").auiSelect2('data', {
                id: AJS.$("#lastNameAttribute1").val(),
                text: AJS.$("#lastNameAttribute1").val()
            });

            AJS.$("#spmetadata_q1").auiSelect2({minimumResultsForSearch: -1});
            AJS.$("#customIdpName").auiSelect2({minimumResultsForSearch: -1});
            AJS.$("#metadataOpt").auiSelect2({minimumResultsForSearch: -1});
            AJS.$("#loginAttribute").auiSelect2({minimumResultsForSearch: -1});
            AJS.$("#enableDefaultGroupsFor").auiSelect2({minimumResultsForSearch: -1});
            AJS.$("#useSeparateNameAttributes").auiSelect2({minimumResultsForSearch: -1});

            showEffectiveMetadataUrlQuick()

            AJS.$(document).on('keyup keypress blur change', '#inputUrl', function () {
                showEffectiveMetadataUrlQuick();
            });

            AJS.$(".show-sp-metadata-popup").click(function (e) {
                e.preventDefault();
                AJS.dialog2("#sp_metadata_dialog").show();
            });

            AJS.$("#sp_metadata_dialog_close_button").click(function (e) {
                e.preventDefault();
                AJS.dialog2("#sp_metadata_dialog").hide();
            });

            AJS.$("#newIdpname, #inputUrl, #xmlFile, #httpRedirect, #httpPost, #ssoUrl, #idpEntityId, #x509Certificate").change(function () {
                console.log("change in idp config");
                if (AJS.$("#config-idp-save").prop("display", "none")) {
                    AJS.$("#config-idp-save").show();
                    console.log("Showing save button");
                }
            });

        });

        <#include "/js/troubleshooting.js">
        <#include "/js/flowdrivensetup.js">

    </script>
    <style>
        <#include "/css/flowdrivensetup.css" >
    </style>
</head>
<body>

    <#include "*/saml/headers/quicksetupheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="flow-driven-setup" role="tabpanel">
    <#include "*/saml/spmetadatapopup.ftl" parse=true>
    <div class="config-pages" id="config-pages-div">
        <div id="spmetadata"
            <#if pageNumber == "" || pageNumber == "1">
                style="display: block"
            <#else>
                style="display: none"
            </#if>
        >
            <p style="font-size:17pt;"><strong>Service Provider Metadata</strong><a
                    class="aui-button aui-button-primary" href="${idpGuide}" target="_blank"
                    style="float: right">View Setup Guide</a>
            </p>

            <hr>
            <br>
            <div class="questions">
                <p class="p-question">You will need to create an Application for Bamboo on your IDP before
                    proceeding
                    with further configuration. Use the <strong>View Setup Guide</strong> button to view the
                    step-by-step setup guide.</p>
            </div>
            <br>
            <div class="questions" id="spmetadata_q">
                <table class="question-table">
                    <tr>
                        <td class="question-td">
                            <p class="p-question">You will need to provide Bamboo's metadata to your IDP. How would
                                you
                                like
                                to
                                this?</p>
                        </td>
                        <td>
                            <select style="width: 400px; max-width: none" id="spmetadata_q1"
                                    name="spmetadata_q1"
                                    onchange="showSPMetadataFunctions()">
                                <option value="metadataUrl" selected>By providing a metadata URL/File to the IDP
                                </option>
                                <option value="metadataManual">By manually configuring the metadata on your IDP
                                </option>
                            </select>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="divs-to-display">
                <div id="metadata-url" style="display: block;">
                    <table class="custom-table" width="90%">
                        <tr>
                            <td>
                                <p><i>Provide this metadata to your Identity Provider to enable Bamboo as a service
                                    provider/relying
                                    party:</i><br><br>
                                    <#assign metadata = settings.getSpBaseUrl() + "/plugins/servlet/saml/metadata">
                                    <a href="${metadata}" target="_blank" id="sp-metadata-rl"
                                       class="aui-button aui-button-link"><b>${metadata}</b></a>

                                    <#assign downloadMetadataEndpoint = settings.getSpBaseUrl() + "/plugins/servlet/saml/downloadmetadata">
                                    <br>
                                <p style="padding-left: 60px">OR</p>
                                <br>
                                <a href = "${downloadMetadataEndpoint}">
                                <input type="button" class="aui-button" value="Download Metadata" id="download_metadata">
                                </a>
                                </p>
                            </td>
                        </tr>
                    </table>
                </div>
                <div id="metadata" style="display: none;">
                    <table class="custom-table" id="metadata-table">
                        <tbody>
                        <tr>
                            <td headers="name" width="20%">
                                <b>SP Entity ID / Issuer</b>
                            </td>
                            <td headers="type" width="60%">
                                <p id="sp-entity-id" class="copyable" style="cursor:pointer;"
                                   title="Click to Copy">${settings.getSpEntityId()}</p>
                            </td>
                            <td width="20%">
                                <span id="sp-entity-id-copied-message" title="Copied" class="show-title"
                                      style="display:none"></span>
                            </td>
                        </tr>
                        <tr>
                            <td headers="name">
                                <b>ACS URL</b>
                            </td>
                            <td headers="type">
                                <p id="acs-url" class="copyable" style="cursor:pointer;"
                                   title="Click to Copy">${settings.getLoginServletUrl()}</p>
                            </td>
                            <td>
                                <span id="acs-url-copied-message" title="Copied" class="show-title"
                                      style="display:none"></span>
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
                                <a class="aui-button aui-button-link" title="Copy to clipboard"
                                   onclick="copyCertificate('#p7','#c7')"><i
                                        class="fa fa-copy"></i> Copy Certificate
                                </a>
                                &nbsp;&nbsp;&nbsp;
                                <!-- Link to show or hide certificate details -->

                                <!-- Certificate details. certificateInfo map is used. Hidden by default. -->

                                <div class="description" style="font-size:12px;">Note: If the IdP requires signed
                                    requests,
                                    the IdP will need this certificate to validate requests. It is also used to decrypt
                                    encrypted
                                    SAML Assertions from the IdP. After download, open in notepad to copy certificate.
                                    You can
                                    configure your own certificates from <a href="customcerts.action">here</a>.
                                </div>
                            </td>
                            <td width="30%">
                                <p id="p7"
                                   style="font-family:Courier New;display: none" cols="64"
                                   rows="4">${settings.getPublicSPCertificate()}
                                </p>
                                <span id="c7" title="Copied" class="show-title" style="display:none"></span>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <form id="spmetadata-form" name="spmetadata-form" class="aui long-label" action="" method="POST">
                <input type="hidden" name="spMetaDataSubmitted" value="true"/>
                <input type="hidden" name="atl_token" value="${xsrfToken}" />
                <input type="hidden" name="pageNumber" value="1"/>

                <div class=" aui-message aui-message-info" style="width: 95.5%">
                    <p>Running your server with a proxy? Click <a
                            onclick="showUpdateForm()">here</a> to
                        change
                        your base
                        URL.</p>
                    <div class="divs-to-display" id="update-urls" style="display: none">
                        <input type="hidden" name="updatedUrls" value="true"/>
                        <input type="hidden" name="pageNumber" value="1"/>
                        <table style="width: 90%;" class="custom-table">
                            <tr>
                                <td width="20%">SP Base URL :</td>
                                <td><input type="url" required="true" id="updatedSpBaseUrl" name="updatedSpBaseUrl"
                                           value="${settings.getSpBaseUrl()}"
                                           class="text long-field"/>
                                    <div class="description">
                                        <span>If your site is behind a proxy you can modify SP Base URL for Single Sign-On to work.</span>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td width="20%">SP Entity ID:</td>
                                <td>
                                    <input type="text" required="true" id="updatedSpEntityId" name="updatedSpEntityId"
                                           value="${settings.getSpEntityId()}"
                                           class="text long-field"/>
                                    <div class="description">
                                        <span>Set the Entity ID for the Service Provider. If not provided, Bamboo base URL will be used by default.</span>
                                    </div>
                                </td>
                            </tr>
                        </table>
                        <div class="field-group">
                            <input type="button" id="update_urls" value="Update" class="aui-button aui-button-primary"
                                   style="width:100px;"/>
                        </div>
                    </div>
                </div>
                <div class="end-buttons">
                    <input type="submit" value="Proceed" class="aui-button aui-button-primary next-button">
                    <a href="listidp.action" class="back-link">Back</a>
                </div>
            </form>
        </div>
        <div id="configureidp"
            <#if pageNumber == "2">
                style="display: block"
            <#else>
                style="display: none"
            </#if>
        >
            <form id="configureidp-form" name="configureidp-form" class="aui long-label" action="" method="POST"
                  enctype="multipart/form-data">
                <input type="hidden" name="configureIdpSubmitted" value="true"/>
                <input type="hidden" name="atl_token" value="${xsrfToken}" />
                <input type="hidden" name="idpID" value="${idpID}" id="idpID"/>
                <input type="hidden" name="idpName" value="${idpName}" id="idpName"/>
                <input type="hidden" name="pageNumber" value="2"/>
                <input type="hidden" name="setupType" value="quickSetup"/>
                <input type="hidden" name="fileSubmitted" value="true">

                <p style="font-size:17pt;"><strong>Configure your Identity Provider</strong> <button
                        id="show-sp-metadata-popup-1" class="aui-button aui-button-primary show-sp-metadata-popup"
                        style="float: right" resolved="">SP Information</button></p>

                <hr>
                <br>


                <div class="questions">
                    <p class="p-question">You can give your IDP a custom name. This is the name that will be displayed
                    in the Configured IDPs section and also on SSO button for Bamboo's login page. If you choose not
                    to configure a custom name, then the selected
                    IDP's name will be used by default.</p>
                </div>


                <div class="questions">
                    <table class="q-a-table">

                    <tr>
                        <th id="col1">
                        </th>
                        <th id="col2">
                        </th>
                    </tr>

                    <tr>
                        <td headers="col1" class="question-td-main"><p> Would you like to customize the name of your IDP?</p>
                        </td>
                        <td headers="col2"><select style="width: 400px; max-width: none;" id="customIdpName"
                                                   name="customIdpName"
                                                   onchange="showCustomIdpNameFunctions()">
                            <option value="yes" <#if customIdpName == "yes"||customIdpName ==
                                ""> selected </#if> >
                                Yes
                            </option>
                            <option value="no" <#if customIdpName == "no">
                                    selected </#if> > No
                            </option>
                        </select></td>
                    </tr>

                    <tr class="divs-to-display custom-idp-name"
                        <#if customIdpName == "yes">
                        style="display: revert;"
                        <#else>
                        style="display: none;"
                        </#if> >

                        <td headers="col1" class="question-td">Custom IDP Name :</td>
                        <td headers="col2" class="input-td">
                            <input type="text" id="newIdpname" name="newIdpname" style="width: 400px"
                                   value="${newIdpname}"
                                   placeholder="Enter the name for your IDP"
                                   class="text long-field" required onchange="showSaveButton()"/>
                        </td>

                    </tr>

                    <tr class="divs-to-display custom-idp-name"
                        <#if customIdpName == "yes">
                        style="display: revert;"
                        <#else>
                        style="display: none;"
                        </#if> >

                        <td headers="col1"></td>
                        <td headers="col2">
                            <div class="description">This IDP Name will be shown in the login widget to
                                users.
                            </div>
                        </td>

                    </tr>

                    <tr>
                        <td class="question-td-main"><p class="p-question">You need to configure your IDP's
                            metadata on
                            Bamboo. How would you like to add
                            this metadata?</p></td>
                        <td>
                            <br>
                            <select style="width: 400px; max-width: none;" id="metadataOpt"
                                    name="metadataOption"
                                    onchange="showIdpMetadataFunctions()">
                                <option value="fromUrl"  <#if metadataOption ==
                                    "fromUrl" || metadataOption == "">
                                        selected </#if> >I have the metadata URL for my IDP
                                </option>
                                <option value="fromFile"  <#if metadataOption == "fromFile" || idpName ==
                                    "G_Suite"> selected </#if> >I have a file
                                    which contains the metadata
                                </option>
                                <option value="manual"  <#if metadataOption == "manual"> selected </#if> >I
                                    want to
                                    manually
                                    configure the IDP
                                </option>
                            </select>
                        </td>

                    </tr>

                    <tr class="idp_metadata_url"
                        <#if metadataOption == "fromUrl">
                        style="display: revert"
                        <#else>
                        style="display:none;"
                        </#if> >

                        <#if idpName == 'ADFS'>
                        <td id="metadata_url_label" width="20%"
                            class="question-td">
                            Enter ADFS hostname/Metadata URL :
                        <#elseif idpName == 'Azure_AD'>
                        <td id="metadata_url_label" width="20%"
                            class="question-td">
                            Enter Azure AD domain name/Metadata URL :
                        <#else>
                        <td id="metadata_url_label" width="20%" class="question-td">
                            Enter Metadata URL :
                        </#if>
                        </td>
                        <td class="input-td">
                            <input type="text" id="inputUrl" name="inputUrl" style="width: 400px"
                                   placeholder="Enter metadata URL of your IdP"
                                   style="width:70%; padding: 5px;"
                                   class="text long-field" value="${inputUrl}" onchange="showSaveButton()"/>
                        </td>

                    <tr class="idp_metadata_url"
                        <#if metadataOption == "fromUrl">
                        style="display: revert"
                        <#else>
                        style="display:none;"
                        </#if> >
                        <td></td>
                        <td>
                            <div id=" metadata_url_description" class="description">
                                <#if idpName == "Okta">
                                    You can find the metadata URL for your Okta Application by navigating to
                                    :<br/>
                                    <i>Okta Admin Console > Applications > (Your Application) > Sign On >
                                        Identity Provider Metadata</i>
                                </#if>
                            </div>
                        </td>
                    </tr>
                    <tr id="effective_metadata_url_div" class="idp_metadata_url" style="display:none">
                        <td width="20%" class="question-td">Effective Metadata URL :</td>
                        <td class="input-td">
                            <p id="effective_metadata_url" style="width: 405px; background-color: #f5f5f5; font-weight: bold"></p>
                            <#if foundIdpConfiguration == true>
                            <div class="description">You can go to <a href="advancedsso.action?idpid=${idpID}"> Advanced SSO Settings </a> tab to enable automatic metadata rollover</div>
                            </#if>
                        </td>

                    </tr>


                    <tr class="idp_metadata_file"
                        <#if metadataOption == "fromFile">
                            style="display: revert"
                        <#else>
                            style="display:none;"
                        </#if>
                    >
                        <td id="metadata_file_label" width="20%" class="question-td" style="/*text-align: center*/">Browse :</td>
                        <td class="input-td">
                            <!-- <form name="saml_form_file" id="saml_form_file" method="post" action="" enctype="multipart/form-data"> -->
                            <input type="file" id="xmlFile" name="xmlFile" class="ignore-inline-attach"
                                   accept=".xml" onchange="showSaveButton()"/>
                            <div id=" metadata_file_description" class="description">
                            </div>
                            <!-- <input type="submit" onclick="showConfSP()" class="aui-button aui-button-primary" value="Upload"/> -->
                            <!-- </form> -->
                        </td>

                    </tr>
                    <tr class="idp_metadata_manual"
                        <#if metadataOption == "manual">
                        style="display: revert"
                        <#else>
                        style="display:none;"
                        </#if> >
                        <td width="20%" class="question-td">Single Sign On URL :</td>
                        <td width="250px">
                            <input class="radio" type="radio" name="ssoBindingType" id="httpRedirect"
                                   value="HttpRedirect" <#if ssoBindingType == "HttpRedirect">
                                   checked</#if> onchange="showSaveButton()"/>

                            <label for="httpRedirect">HTTP-Redirect Binding</label>&nbsp;&nbsp;
                            <input class="radio" type="radio" name="ssoBindingType" id="httpPost"
                                   value="HttpPost"  <#if ssoBindingType == "HttpPost">
                                   checked </#if>
                                   onchange="showSaveButton()"/>

                            <label for="httpPost">HTTP-Post Binding</label>
                            <br>
                            <input type="url" id="ssoUrl" name="ssoUrl" value="${ssoUrl}"
                                   class="text long-field" onchange="showSaveButton()"/>
                            <div class="description" >
                                Enter the Single Sign-on Service endpoint of your Identity Provider. You can
                                find its
                                value in SingleSignOnService tag (Binding
                                type: HTTP-Redirect) in IdP-Metadata XML file.
                            </div>
                        </td>
                    </tr>
                    <tr class="idp_metadata_manual"
                        <#if metadataOption == "manual">
                        style="display: revert"
                        <#else>
                        style="display:none;"
                        </#if>>
                        <td width="20%" class="question-td">IDP Entity ID / Issuer :</td>
                        <td width="250px">
                            <input type="text" id="idpEntityId" name="idpEntityId"
                                   value="${idpEntityId}"
                                   class="text long-field" onchange="showSaveButton()"/>
                            <div class="description">
                                Enter the Entity ID or Issuer value of your Identity Provider. You can find
                                its
                                value in
                                the entityID attribute of EntityDescriptor
                                tag in IdP-Metadata XML file.
                            </div>
                        </td>
                    </tr>
                    <tr class="idp_metadata_manual"
                        <#if metadataOption == "manual">
                        style="display: revert"
                        <#else>
                        style="display:none;"
                        </#if> >
                        <td width="20%" class="question-td">IDP Signing Certificate :</td>
                        <td width="250px">
                            <#if certificates??>
                                <#foreach x509Certificate in certificates>
                                    <div>
                                        <textarea id="x509Certificate" name="x509AllCertificates"
                                                  class="textarea long-field"
                                                  style="font-family:Courier New; font-size: 12px;" cols="64"
                                                  rows="4" onchange="showSaveButton()">${x509Certificate}
                                        </textarea>
                                    </div>
                                </#foreach>
                            <#else>
                                <div>
                                        <textarea id="x509Certificate" name="x509Certificate"
                                                  class="textarea long-field"
                                                  style="font-family:Courier New;font-size: 12px;"
                                                  cols="64" rows="4">${x509Certificate}</textarea>
                                </div>
                            </#if>
                            <div class="description">This Certificate is used to validate SAML response from
                                Identity
                                Provider. You can find its value in
                                X509Certificate tag in IdP-Metadata XML file. (parent tag: KeyDescriptor
                                use="signing").
                                If your
                                IDP provided you the certificate file, open it in Notepad and copy/paste the
                                content
                                here.
                            </div>
                        </td>
                    </tr>
                    <tr class="idp_metadata_manual"
                        <#if metadataOption == "manual">
                        style="display: revert"
                        <#else>
                        style="display:none;"
                        </#if> >
                        <td width="20%" class="question-td">Send Signed Requests :</td>
                        <td width="250px">
                            <input class="checkbox" data-size="small" data-on-text="Yes" data-off-text="No"
                                   type="checkbox"
                                   name="signedRequest"
                                   value="true"
                                   id="signedRequest" <#if signedRequest == true || signedRequest == "">
                                   checked </#if> >
                            <div class="description">
                                It is recommended to keep it checked. Uncheck, only if your IdP is not
                                accepting Signed SAML Request.
                            </div>
                        </td>
                    </tr>

                    </table>
                </div>

                <#assign testConfigUrl  = spBaseUrl + "/plugins/servlet/saml/testconfig?idp="+ idpID >
                 <#if foundIdpConfiguration == false>
                    <div id="quick-setup-test" style="display:none">
                 <#else>
                    <div id="quick-setup-test" style="display:inline">
                 </#if>
                  <table style="width:65%; margin-left:14%" class="custom-table">
                  <tr>
                        <td style="width: 28%; text-align: center;">Test and Get Attributes URL :</td>
                        <td style="margin-left:15px">
                            <p id="quick-setup-test-url" style="width:450px; cursor:pointer"
                               onclick="copyToClipboard('#quick-setup-test-url','#quick-setup-test-url-copied')"><b>${testConfigUrl}</b></p>
                         <div class="description" style="width:450px;">
                               Copy the URL & paste it in separate window to Get the Attributes from IDP.

                        </div>
                        </td>
                        <td>
                              <span id="quick-setup-test-url-copied" title="Copied" class="show-title" style="display:none"></span>
                        </td>
                  </tr>
                  </table>
                   </div>

                <div class="end-buttons">
                    <input type="submit" value="Save" id="config-idp-save"
                           class="aui-button aui-button-primary next-button" <#if foundIdpConfiguration == true>
                           style="display: none;" <#else> style="display: inline;" </#if> >

                    <input type="button" value="Proceed" id="config-idp-next"
                           class="aui-button aui-button-primary next-button" <#if foundIdpConfiguration == false>
                           style="display: none" <#else> style="display: inline" </#if>
                           onclick="submitForm('config-idp-form-submit-next')">
                    <a onclick="displayPrevPage(1)" class="back-link">Back</a>
                </div>
            </form>
            <form id="config-idp-form-submit-next" name="config-idp-form-submit-next" class="aui long-label" action=""
                  method="POST">
                <input type="hidden" name="configureIdpNextSubmitted" value="true"/>
                <input type="hidden" name="atl_token" value="${xsrfToken}" />
                <input type="hidden" name="pageNumber" value="2"/>
                <input type="hidden" name="idpID" value="${idpID}" id="idpID">
            </form>
        </div>
        <div id="userprofile"
            <#if pageNumber == "3">
             style="display: block"
            <#else>
             style="display: none"
            </#if>
        >
            <form id="userprofile-form" name="userprofile-form" class="aui long-label" action="" method="POST">
                <input type="hidden" name="userProfileSubmitted" value="true"/>
                <input type="hidden" name="atl_token" value="${xsrfToken}" />
                <input type="hidden" name="pageNumber" value="3"/>
                <input type="hidden" name="idpID" value="${idpID}" id="idpID">

                <p style="font-size:17pt;"><strong>User Profile</strong><button
                        id="show-sp-metadata-popup-2" class="aui-button aui-button-primary show-sp-metadata-popup"
                        style="float: right" resolved="">SP Information</button></p>

                <hr>
                <br>
                <div class="questions">
                    <p class="p-question">You can choose the user attribute with which you want the users to login. So
                        if you
                        choose Username, then the user will be logged into Bamboo with IDP's username.</p></div>
                <div class="questions divs-to-display" id="loginAttribute_q">
                    <table class="question-table">
                        <tr>
                            <td class="question-td">
                                <p> Which attribute would you like the users to login with?</p>
                            </td>
                            <td>
                                <select style="width:700px !important; max-width: none;" id="loginAttribute"
                                        name="loginAttribute" onchange="showEmailWarning()">
                                    <option value="username" <#if loginAttribute == ""|| loginAttribute == "username">
                                            selected </#if> >Username
                                    </option>
                                    <option value="email" <#if loginAttribute == "email"> selected </#if> >Email</option>
                                </select>
                            </td>
                        </tr>
                    </table>
                    <div id="warningforemail" class="aui-message aui-message-warning closeable" style="display:none;">
                        Email is not recommended. Select this option only when email of all the user is unique otherwise
                        SSO will not work.
                    </div>
                </div>
                <br>
                <div class="questions" id="attributes_q">
                    <p class="p-question">The users profile attributes in Bamboo will be set based on their
                        attributes in
                        the IDP.
                        You will need to map the attributes received from the IDP which correspond to the following
                        profile attributes in Bamboo. The drop-down
                        contains all the attributes received from the IDP during the test in the previous step. To help
                        select the right attributes, the
                        values received for each attribute
                        are displayed within square brackets next to the attribute itself. Please select the attribute
                        corresponding to the profile value</p>
                </div>
                <div class="questions divs-to-display">
                    <table class="question-table">
                        <tr>
                            <td class="question-main">Username Attribute :</td>
                            <td class="input-td">
                                <#if testConfigPerformed == true>
                                    <input required="true" id="userNameAttribute"
                                           name="userNameAttribute"
                                           value="${userNameAttribute}"
                                           class="select long-field attributemapping"
                                           placeholder="Enter username attribute" style="width:700px !important; max-width: none;"/>
                                <#else>
                                    <input required="true" id="userNameAttribute" name="userNameAttribute"
                                           value="${userNameAttribute}" class="text long-field"
                                           placeholder="Enter username attribute" style="width:700px !important; max-width: none;"/>
                                </#if> </td>
                        </tr>
                        <br>
                        <tr>
                            <td class="question-main">Email
                                Attribute :
                            </td>
                            <td class="input-td">
                                <#if testConfigPerformed == true>
                                    <input required="true" id="userEmailAttribute" name="userEmailAttribute"
                                           value="${userEmailAttribute}" class="select long-field attributemapping"
                                           placeholder="Enter email attribute" style="width:700px !important; max-width: none;"/>
                                <#else>
                                    <input required="true" id="userEmailAttribute" name="userEmailAttribute"
                                           value="${userEmailAttribute}" class="text long-field"
                                           placeholder="Enter email attribute" style="width:700px !important; max-width: none;"/>
                                </#if> </td>
                        </tr>
                    </table>
                </div>
                <div class="questions divs-to-display" id="separate_attributes_q">
                    <table class="question-table">
                        <tr>
                            <td class="question-td">
                                <p class="p-question">It's possible that the IDP has separate attributes for First name
                                    and Last
                                    name. Is this the case with your IDP? </p>
                            </td>
                            <td>
                                <select style="width:700px !important; max-width: none;" id="useSeparateNameAttributes"
                                        name="useSeparateNameAttributes"
                                        onchange="showNameAttributes()">
                                    <option value="true" <#if useSeparateNameAttributes == true> selected </#if> >Yes
                                    </option>
                                    <option value="false" <#if useSeparateNameAttributes == false> selected</#if> >No
                                    </option>
                                </select>
                            </td>
                        </tr>
                    </table>
                </div>
                <div class="questions divs-to-display">
                    <div id="fullname_attr"
                        <#if useSeparateNameAttributes == true>
                         style="display: none;"
                        <#else>
                         style="display: block;"
                        </#if> >
                        <table class="question-table">
                            <tr>
                                <td class="question-main">Fullname Attribute :</td>
                                <td class="input-td">
                                    <#if testConfigPerformed == true>
                                        <input id="fullNameAttribute1" name="fullNameAttribute"
                                               value="${fullNameAttribute}" class="select long-field attributemapping"
                                               style="width:700px !important; max-width: none;"/>
                                    <#else>
                                        <input id="fullNameAttribute1" name="fullNameAttribute"
                                               value="${fullNameAttribute}" class="text long-field" style="width:700px !important; max-width: none;"/>
                                    </#if>
                                </td>
                            </tr>
                        </table>
                    </div>
                    <div id="separatename_attr"
                        <#if useSeparateNameAttributes == true>
                         style="display: block;"
                        <#else>
                         style="display: none;"
                        </#if> >
                        <table class="question-table">
                            <tr>
                                <td class="question-main">First name Attribute :</td>
                                <td class="input-td">
                                    <#if testConfigPerformed == true>
                                        <input id="firstNameAttribute1" name="firstNameAttribute"
                                               value="${firstNameAttribute}" class="select long-field attributemapping"
                                               style="width:400px; max-width: none;"/>
                                    <#else>
                                        <input id="firstNameAttribute1" name="firstNameAttribute"
                                               value="${firstNameAttribute}" class="text long-field"
                                               style="width:400px; max-width: none;"/>

                                    </#if>
                                </td>
                            </tr>
                            <br>
                            <tr>
                                <td class="question-main">Last name Attribute :</td>
                                <td class="input-td">
                                    <#if testConfigPerformed == true>
                                        <input id="lastNameAttribute1"
                                               name="lastNameAttribute"
                                               value="${lastNameAttribute}" class="select long-field attributemapping"
                                               style="width:400px; max-width: none;"/>
                                    <#else>
                                        <input id="lastNameAttribute1"
                                            name="lastNameAttribute"
                                            value="${lastNameAttribute}" class="text long-field"
                                            style="width:400px; max-width: none;"/>
                                    </#if>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
                <div class="end-buttons">
                    <input type="submit" value="Save and Proceed" class="aui-button aui-button-primary next-button">
                    <a onclick="displayPrevPage(2)" class="back-link">Back</a>
                </div>
            </form>
        </div>
        <div id="usergroups"
            <#if pageNumber == "4">
             style="display: block"
            <#else>
             style="display: none"
            </#if>
        >
            <form id="usergroups-form" name="usergroups-form" class="aui long-label" action="" method="POST">
                <input type="hidden" name="userGroupsSubmitted" value="true"/>
                <input type="hidden" name="atl_token" value="${xsrfToken}" />
                <input type="hidden" name="pageNumber" value="4"/>
                <input type="hidden" name="idpID" value="${idpID}" id="idpID">

                <p style="font-size:17pt;"><strong>User Groups - Default groups</strong><button
                        id="show-sp-metadata-popup-3" class="aui-button aui-button-primary show-sp-metadata-popup"
                        style="float: right" resolved="">SP Information</button></p>

                <hr>
                <div class="questions" id="default-groups">
                    <p class="p-question">In-order to allow users access to Bamboo, they must be part of atleast one
                        of
                        the default groups of Bamboo. Select the default groups that will be assigned to the user
                        after
                        successful SSO</p>
                </div>
                <div class="questions divs-to-display">
                    <table class="question-table" >
                        <tr>
                            <td class="question-main">Default Groups :</td>
                            <td><input class="select long-field" name="defaultGroups" id="defaultGroups"
                                       style="width: 405px">
                            </input>
                                <div class="description">Select Default Group(s) to assign to <strong>New Users / All
                                    Users</strong>. Select
                                    <b>None</b> to disable default group assignment.<br>
                                    <a href="https://miniorange.atlassian.net/l/c/BK43jwLq" target="_blank">Click
                                        here</a> for recommended
                                    default group settings for different
                                    primary user directory permissions.
                                </div>
                            </td>
                        </tr>
                    </table>
                </div>
                <br>
                <div class="questions divs-to-display" id="default-group-assign">
                    <table class="question-table">
                        <tr>
                            <td class="question-td"><p class="p-question">Which users will be assigned the default
                                groups?</p>
                            </td>
                            <td>
                                <select style="width: 400px; max-width: none;" id="enableDefaultGroupsFor"
                                        name="enableDefaultGroupsFor">
                                    <option value="newUsers" <#if enableDefaultGroupsFor=="newUsers">selected </#if> >
                                        New
                                        Users
                                    </option>
                                    <option value="allUsers" <#if enableDefaultGroupsFor=="allUsers">selected </#if> >
                                        All
                                        Users
                                    </option>
                                    <option value="NoIDPGroupUsers" #if($enableDefaultGroupsFor=="NoIDPGroupUsers")selected #end>
                                        Users with no IDP Groups
                                    </option>
                                    <option value="doNotAssignDefaultGroup" <#if enableDefaultGroupsFor==
                                        "doNotAssignDefaultGroup"> selected </#if> >None
                                    </option>
                                </select>
                            </td>
                        </tr>
                    </table>
                </div>
                <div class="end-buttons">
                    <input type="submit"
                        <#if testConfigPerformed == true>
                           value="View Results"
                        <#else>
                           value="Save and Proceed"
                        </#if>
                           class="aui-button aui-button-primary next-button"
                           id="view-results">
                    <a onclick="displayPrevPage(3)" class="back-link">Back</a>
                </div>
            </form>
        </div>
        <div id="testconfig"
            <#if pageNumber == "5">
             style="display: block"
            <#else>
             style="display: none"
            </#if>
        >
            <form id="testconfig-form" name="testconfig-form" class="aui long-label" action="" method="POST">
                <input type="hidden" name="testConfigSubmitted" value="true"/>
                <input type="hidden" name="atl_token" value="${xsrfToken}" />
                <input type="hidden" name="pageNumber" value="5"/>
                <input type="hidden" name="idpID" value="${idpID}">
                <#include "*/saml/troubleshootingcontent.ftl" parse=true>
                <div class="end-buttons">
                    <a class="aui-button aui-button-primary"
                       href="listidp.action">Finish Quick Setup</a>
                    <a onclick="displayPrevPage(4)" class="back-link">Back</a>
                </div>
            </form>
        </div>
    </div>
    <script>
        loadCurrentPage();
    </script>
    <#include "*/footer.ftl" parse=true>
</body>
</html>