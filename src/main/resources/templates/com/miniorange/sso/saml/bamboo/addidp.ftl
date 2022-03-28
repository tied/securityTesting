<html>
   <head>
      <meta name="decorator" content="atl.general"/>
      <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
      <script>
        $(document).ready(function() {
            $(".aui-nav li").removeClass("aui-nav-selected");
            $("#LISTIDP").addClass("aui-nav-selected");

            var operation = getQueryParameterByName('operation');
            if(operation == 'add'){
                AJS.flag({
                    title: 'Configuring New IDP',
                    type: 'info',
                    close: 'auto',
                    body: '<p>You will be able to see all IDPs settings after you Save this IDP\'s settings</p>'
                });
            }

            shouldShowAcsUrl();

            var idpNm = document.getElementById("idpNameImport").value;
            if(idpNm != '') {
                AJS.flag({
                    title: 'SAML IDP Configuration',
                    type: 'info',
                    close: 'auto',
                    body: '<p>Now viewing ' + idpNm + '\'s settings</p>'
                });
            }
        });

        function shouldShowAcsUrl() {
            AJS.$.ajax({
                url: AJS.contextPath() + "/plugins/servlet/saml/getconfig",
                type: "GET",
                error: function () {},
                success: function (response) {
                    if (response.idpList.length <= 1) {
                        AJS.$("#newAcsUrl").hide();
                    }
                }
            });
        }

        function getQueryParameterByName(name) {
            var url = window.location.href;
            name = name.replace(/[\[\]]/g, "\\$&");
            var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
                    results = regex.exec(url);
            if (!results) return null;
            if (!results[2]) return '';
            return decodeURIComponent(results[2].replace(/\+/g, " "));
        }

        function popup(mylink, windowname) {
            if (! window.focus)return true;
            var href;
            if (typeof(mylink) == 'string') href=mylink;
            else href=mylink.href;
            window.open(href, windowname, 'width=800,height=600,scrollbars=yes');
            return false;
        }

        function showmeta(){
            document.getElementById('upload-metadata').style.display= "block";
            document.getElementById('config').style.display= "none";
        }

        function showmetadata(){
            document.getElementById('upload-metadata').style.display= "none";
            document.getElementById('config').style.display= "block";
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
            evt.currentTarget.className += " active";
        }


         function DisplayRequest(){
         	var osDestination = "displaysamlrequest";
         	var samlAuthUrl = AJS.contextPath() + '/plugins/servlet/saml/auth';
         	samlAuthUrl += "?return_to=" + osDestination;
         	var idpID = document.getElementById("idpID").value;
         	samlAuthUrl += "&idp=" + encodeURIComponent(idpID);
         	var myWindow = window.open(samlAuthUrl, "TEST SAML IDP", "scrollbars=1 width=800, height=600");
         }

         function DisplayResponse(){
         	var osDestination = "displaysamlresponse";
         	var samlAuthUrl = AJS.contextPath() + '/plugins/servlet/saml/auth';
         	samlAuthUrl += "?return_to=" + osDestination;
         	var idpID = document.getElementById("idpID").value;
         	samlAuthUrl += "&idp=" + encodeURIComponent(idpID);
         	var myWindow = window.open(samlAuthUrl, "TEST SAML IDP", "scrollbars=1 width=800, height=600");
         }

         function copy(that, message) {
             var inp = document.createElement('input');
             document.body.appendChild(inp)
             inp.value = that.textContent
             inp.select();
             document.execCommand('copy', false);
             inp.remove();
             $(message).show();
             setTimeout(function () { $(message).hide("slow"); }, 5000);
         }

      </script>
      <style>
         body {
         font-family: Arial;
         }
         /* Style the tab */
         .tab {
         overflow: hidden;
         display: flex; //justify-content: center;
         }
         /* Style the buttons inside the tab */
         .tab a {
         //border: 1px solid #ccc;
         //background-color: #f1f1f1;
         cursor: pointer;
         padding: 14px 16px;
         font-size: 17px; //text-align:middle;
         margin-left: 1%;
         }
         .tab a:hover {
         transition: 0.3s; //background-color: inherit;
         border: none;
         outline: none;
         background: none !important;
         text-decoration: none !important;
         }
         /* Create an active/current tablink class */
         .tab a.active {
         border-bottom: 3px solid blue;
         background: none !important;
         text-decoration: none !important;
         }
         .tab li {
         margin: 0 auto;
         float: left;
         }
         #navbar {
         float: left;
         }
         /* Style the tab content */
         .tabcontent {
         display: none;
         padding: 0px 0px;
         -webkit-animation: fadeEffect 1s;
         animation: fadeEffect 1s;
         }
         /* Fade in tabs
         @-webkit-keyframes fadeEffect {
         from {opacity: 0;}
         to {opacity: 1;}
         }
         @keyframes fadeEffect {
         from {opacity: 0;}
         to {opacity: 1;}
         }*/
         .field-group{
         margin: 8px 45px 1px 55px !important;
         }
         .group{
         margin: 8px 45px 1px 50px !important;
         }
         .legends{
         margin-left: -140px !important;
         }
         	.aui-page-panel-content{
         			padding: 0px !important;
         		}

         .aui-page-panel{
         			margin: 0px !important;
         		}
      </style>
   </head>
   <body>
      <#include "*/header.ftl" parse=true>
      <div class="tabs-pane active-pane" id="configure-sp" aria-labelledby="configure" role="tabpanel" aria-hidden="true">

         <nav class="aui-navgroup aui-navgroup-horizontal" id="min_topmenu">
            <div class="aui-navgroup-inner">
               <div id="navbar" class="aui-navgroup-primary">
                  <span style="float:right;margin-right:25px;">
                     <div class="tab idp-config">
                        <ul class="aui-nav">
                           <li>
                              <a class="tablinks active" onclick="openTab(event, 'config')" id="defaultOpen">Manual Configuration</a>
                           </li>
                           <li>
                              <a id= "importFromMetadataButton" class="tablinks " onclick="openTab(event, 'upload-metadata')">Import From Metadata</a>
                           </li>
                        </ul>
                     </div>
                  </span>
               </div>
            </div>
         </nav>
         <!-- Import From Metadata Starts Here -->
         <div id="upload-metadata" class="tabcontent" style="display: none;" style="margin-top:15px;">
            <p style="font-size:13pt;">
               <span style="float:right;margin-right:25px;">
               </span>
            </p>
            <form id="metadata_import_form" name="metadata_import_form" method="POST" action="metadataupload" class="aui long-label" enctype="multipart/form-data">
               <input type="hidden" name="atl_token" value="${xsrfToken}" />
               <input type="hidden" id="metadataImported" name="metadataImported" value="true" />
               <input type="hidden" id="idpID" name="idpID" value="${idpID}"/>
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
                              <option disabled hidden <#if metadataOption.equals( "")>selected </#if> >Select IDP</option>
                              <optgroup label = "Supported IDPs">
                                 <#foreach option in metadataOptions>
                                 <option id="${option}" value="${option}"
                                 <#if metadataOption?? && metadataOption.equals(option)>selected</#if>
                                 >${option}
                                 </option>
                                 </#foreach>
                              </optgroup>
                              <optgroup label = "Custom">
                                 <option value="fromFile" <#if metadataOption?? && metadataOption.equals("fromFile")>selected</#if>
                                 >Import From Metadata File
                                 </option>
                                 <option value="fromUrl" <#if metadataOption?? && metadataOption.equals("fromUrl")>selected </#if>
                                 >Import From Metadata URL
                                 </option>
                              </optgroup>
                           </select>
                        </div>
                     </td>
                  </tr>
               </table>
               <br/>
               <div id="importByUrl" style="display: none;">

                  <table style="width:100%;">
                     <tr>
                        <td id="metadata_url_label" width="20%"></td>
                        <td>
                           <input type="hidden" id="fetchMetadataUrl" name="fetchMetadataUrl" value="true" />
                           <input type="url" id="metadataUrl" name="metadataUrl" required value="${metadataUrl}" placeholder="Enter metadata URL of your IdP" style="width:70%; padding: 5px;" class="text long-field"/>
                           <div id="metadata_url_description" class="description"></div>
                        </td>
                     </tr>
                     <tr id="effective_metadata_url_div" style="outline: thin solid black;" hidden>

                        <td width="20%">Effective Metadata URL:</td>
                        <td>
                           <p id="effective_metadata_url" style="background-color: #f5f5f5; font-weight: bold"> </p>
                        </td>
                     </tr>
                     <tr>
                        <td width="20%" rowspan="3">&nbsp;</td>
                        <td>
                           <br/>
                           <#if refreshMetadata == true>
                           <input type="checkbox" id="refreshMetadata" checked="true" name="refreshMetadata"
                              value="true" class="checkbox"/>
                           <#else>
                           <input type="checkbox" id="refreshMetadata" name="refreshMetadata" value="true" class="checkbox"/>
                           </#if>
                           Refresh Metadata periodically?
                           <div class="description">We will store the metadata URL and refresh IdP settings periodically.</div>
                        </td>
                     </tr>
                     <tr>
                        <td>
                           <br/>
                           <select class="select" id="refreshInterval" name="refreshInterval" disabled>
                           <option value="hourly" <#if refreshInterval.equals("hourly")>selected</#if>>Hourly</option>
                           <option value="twicedaily" <#if refreshInterval.equals("twicedaily")>selected</#if>>Twice
                           Daily</option>
                           <option value="daily" <#if refreshInterval.equals("daily")>selected</#if>>Daily</option>
                           <option value="weekly" <#if refreshInterval.equals("weekly")>selected</#if>>Weekly</option>
                           <option value="monthly" <#if refreshInterval.equals("monthly")>selected</#if>>Monthly</option>
                           <option value="custom" <#if refreshInterval.equals("custom")>selected</#if>>Custom</option>
                           </select>&nbsp;&nbsp;&nbsp;&nbsp;
                           <span id="customRefreshValue">
                           <#if customRefreshInterval??>
                           <input type="number" min="1" max="525600" id="customRefreshInterval" name="customRefreshInterval"
                              value="${customRefreshInterval}" class="text"/>&nbsp;&nbsp;
                           </#if>
                           <select name="customRefreshIntervalUnit" class="select">
                           <option value="minutes" <#if customRefreshIntervalUnit.equals("minutes")>selected</#if>>Minutes</option>
                           <option value="hours" <#if customRefreshIntervalUnit.equals("hours")>selected</#if>>Hours</option>
                           <option value="days" <#if customRefreshIntervalUnit.equals("days")>selected</#if>>Days</option>
                           </select>
                           </span>
                        </td>
                     </tr>
                  </table>
               </div>
               <br/>
                <div id="importByFile" style="display: none;">
                   <table style="width:100%;">
                      <tr>
                         <td id="metadata_file_label" width="20%"></td>
                         <td>
                            <!--<form name="saml_form_file" id="saml_form_file" method="POST" action="metadataupload" enctype="multipart/form-data">-->
                               <input type="hidden" id="fetchMetadataFile" name="fetchMetadataFile" value="true" />
                               <input type="file" id="xmlFile" name="xmlFile" class="ignore-inline-attach" accept=".xml" required/>
                               <#if (settings.getCurrentBuildNumber() < 60001)>
                               <p style="color: red;">This feature does not work with current Bamboo Server version.</p>
                               </#if>
                               <br/><br/>
                               <div id="metadata_file_description" class="description"></div>
                               <br/>
                            <!--</form>-->
                         </td>
                      </tr>
                   </table>
                </div>

                <div id="importButtons" style="display: none;">
                    <table style="width: 100%">
                        <tr>
                            <td style="width: 20%">&nbsp;</td>
                            <td>
                                <input type="submit" class="aui-button aui-button-primary" value="Import"/>&nbsp;&nbsp;&nbsp;
                            </td>
                        </tr>
                    </table>
                </div>
            </form>
         </div>
         <!-- Import From Metadata Ends Here -->
         <div id="config" class="tabcontent" style="display: block;">
            <#assign idpSize = idpList?size >
            <div style="margin-top:15px;">
                 <div class="toolbar agents-toolbar">
                 	<div class="aui-toolbar inline">
                 		<ul class="toolbar-group">
                 		    <#if shouldContainsAddIdp == true>
                                <li class="toolbar-item">
                                    <a class="aui-button aui-button-primary" href="addidp.action?operation=add">
                                        Add new IdP
                                    </a>
                                </li>
                            </#if>

                            <#if shouldShowBackButton == true>
                                <li class="toolbar-item">
                                    <a class="aui-button aui-button-primary" href="listidp.action">
                                        Back
                                    </a>
                 			    </li>
                            </#if>
                 		</ul>
                 	</div>
                 </div>
                 <h2>Add Identity Provider</h2>
             </div>
            <hr class="header"/>

            <form id="configure-sp-form" class="aui long-label" action="" method="POST">
               <input type="hidden" name="atl_token" value="${xsrfToken}" />
               <input type="hidden" name="submitted" value="true"/>
               <input type="hidden" id="idpID" name="idpID" value="${idpID}" />
               <p style="font-size:13pt;">
                  <b>Step 3:</b> Configure IDP
               </p>
               <p>Click on <b>Import From Metadata</b> to upload IDP's metadata URL or XML <i>OR</i> copy the URLs from Step 2 below to setup IDP details.</p>
               <p>Need help with the configuration? You can reach out to us using <b>Support/Feedback</b> widget and avail <b>free support</b> to setup the app. You can also submit a support request using our website's
                  <a target="_blank"
                     href="http://miniorange.com/contact">contact us</a> form or write to us at
                  <a href="mailto:info@xecurify.com">info@xecurify.com</a>
               </p>
               <div id="newAcsUrl" class="field-group">
                   <label for="acsUrl"> ACS URL for IDP-Initiated SSO</label>
                   <span title="click to copy" style="cursor:default"> <span id="acsUrl" onmousedown="copy(this,'#c1')"><strong>${acsUrl}</strong></span>&nbsp;&nbsp;<span id="c1" title="Copied" class="show-title" hidden></span></span>
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
                  <label for="idpEntityId" class="labelClass">IDP Entity ID:
                  <span class="aui-icon icon-required">(required)</span>
                  </label>
                  <#if action.idpEntityId == "">
                  <input type="text" required="true" id="idpEntityId" name="idpEntityId" value=""
                     class="text long-field"/>
                  <#else>
                  <input type="text" required="true" id="idpEntityId" name="idpEntityId" value="${action.idpEntityId}"
                     class="text long-field"/>
                  </#if>
                  <div class="description">
                     Enter the Entity ID or Issuer value of your Identity Provider. You can find its value in the entityID attribute of EntityDescriptor tag in <br>IdP-Metadata XML file.
                  </div>
               </div>

               <div class="field-group">
                  <label for="signedRequest" class="labelClass">Send Signed Requests:</label>
                  <#if signedRequest == true>
                  <input class="checkbox" data-size="small" data-on-text="Yes" data-off-text="No" type="checkbox"
                     name="signedRequest" checked="true" value="true" id="signedRequest"/>
                  <#else>
                  <input class="checkbox" data-size="small" data-on-text="Yes" data-off-text="No" type="checkbox"
                     name="signedRequest" value="true" id="signedRequest"/>
                  </#if>
                  <div class="description">
                     It is recommended to keep it checked. Uncheck it, only if your IdP is not accepting Signed SAML Request.
                  </div>
                  <#if  settings.getSignedRequest() == true && settings.getSigning() == false>
                  <div class="description">
                     <div class="aui-message aui-message-warning">
                        <span> Warning: Signing certificate is disabled in IDP configuration. Please enable <b>Include Signing Certificate in Metadata</b> in <a href="configure.action">Service Provider Info</a>.</span>
                     </div>
                  </div>
                  </#if>
               </div>

               <fieldset class="group">
                  <legend>
                  <span>SSO Binding Type:</span>
                  </legend>
                  <#if ssoBindingType == "HttpRedirect">
                  <input class="radio" type="radio" name="ssoBindingType" id="httpRedirectsso" value="HttpRedirect"  checked="checked"/>
                  <#else>
                  <input class="radio" type="radio" name="ssoBindingType" id="httpRedirectsso" value="HttpRedirect"/>
                  </#if>
                  <label for="httpRedirectsso">Use HTTP-Redirect Binding for SSO</label>
                  <#if ssoBindingType == "HttpPost" >
                  <input class="radio" type="radio" name="ssoBindingType" id="httpPostsso" value="HttpPost"
                     checked="checked"/>
                  <#else>
                  <input class="radio" type="radio" name="ssoBindingType" id="httpPostsso" value="HttpPost"/>
                  </#if>
                  <label for="httpPostsso">Use HTTP-Post Binding for SSO</label>
               </fieldset>

               <div class="field-group">
                  <label for="ssoUrl" class="labelClass">Single Sign On URL:
                  <span class="aui-icon icon-required">(required)</span>
                  </label>
                  <#if action.ssoUrl == "">
                  <input type="url" required="true" id="ssoUrl" name="ssoUrl" value="" class="text long-field"/>
                  <#else>
                  <input type="url" required="true" id="ssoUrl" name="ssoUrl" value="${action.ssoUrl}" class="text long-field"/>
                  </#if>
                  <div class="description">
                     Enter the Single Sign-on Service endpoint of your Identity Provider. You can find its value in SingleSignOnService tag<br> (Binding type: HTTP-Redirect) in IdP-Metadata XML file.
                  </div>
               </div>

               <fieldset class="group">
                  <legend>
                     <span>SLO Binding Type:</span>
                  </legend>
                  <#if sloBindingType == "HttpRedirect">
                  <input class="radio" type="radio" name="sloBindingType" id="httpRedirectslo" value="HttpRedirect"
                     checked="checked"/>
                  <#else>
                  <input class="radio" type="radio" name="sloBindingType" id="httpRedirectslo" value="HttpRedirect"/>
                  </#if>
                  <label for="httpRedirectslo">Use HTTP-Redirect Binding for SLO</label>
                  <#if sloBindingType == "HttpPost">
                  <input class="radio" type="radio" name="sloBindingType" id="httpPostslo" value="HttpPost"
                     checked="checked"/>
                  <#else>
                  <input class="radio" type="radio" name="sloBindingType" id="httpPostslo" value="HttpPost"/>
                  </#if>
                  <label for="httpPostslo">Use HTTP-Post Binding for SLO</label>
               </fieldset>

               <div class="field-group" class="labelClass">
                  <label for="sloUrl" class="labelClass">Single Logout URL:</label>
                  <input type="url" id="sloUrl" name="sloUrl" value="${sloUrl}" class="text long-field"/>
                  <div class="description">
                     Enter the Single Logout Service endpoint of your Identity Provider. You can find its value in SingleLogoutService tag in IdP-Metadata XML file.<br> Leave blank if SLO not supported by IDP
                  </div>
               </div>

               <div class="field-group">
                  <label for="nameIdFormat">NameID Format:</label>
                  <select class="select" name="nameIdFormat" id="nameIdFormat" style="max-width: 400px;">
                  <#foreach nameID in  nameIdFormats>
                  <option id="${nameID}" value="${nameID}"
                  <#if nameIdFormat?? && nameIdFormat.equals(nameID)>selected</#if>
                  >${nameID}
                  </option>
                  </#foreach>
                  </select>
                  <div class="description">
                     Select the name identifier format supported by the IdP.Select <b>unspecified</b> by default.
                  </div>
               </div>

               <div class="field-group">
                  <label for="x509Certificate">IDP Signing Certificate:
                  <span class="aui-icon icon-required">(required)</span>
                  </label>
                  <#if certificates?has_content>
                  <#foreach x509Certificate in certificates>
                  <div>
                     <textarea id="x509Certificate" name="allX509certificates" class="textarea long-field" style="font-family:Courier New;" cols="64" rows="4">${x509Certificate}</textarea>
                  </div>
                  </#foreach>
                  <#else>
                  <div>
                     <textarea id="x509Certificate" required="true" name="x509Certificate" class="textarea long-field" style="font-family:Courier New;" cols="64" rows="4">${x509Certificate}</textarea>
                  </div>
                  </#if>
                  <div class="description">This Certificate is used to validate SAML response from Identity Provider.<br> You can find its value in X509Certificate tag in IdP-Metadata XML file. (parent tag: KeyDescriptor use="signing"). If your IDP provided you the certificate file, open it
                     in Notepad and copy/paste the content here.
                  </div>
               </div>

               <div class="field-group">
                  <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:170px;"/>
                  &nbsp;&nbsp;&nbsp;
                  <#if (action.idpEntityId??) && (action.idpEntityId != "")>
                  <a href="${req.contextPath}/plugins/servlet/saml/auth?return_to=testidpconfiguration&idp=${idpID}" onClick="return popup(this, 'Test Configuration Result')"><input type="button" style="width:170px;" value="Test Configuration" class="aui-button" /></a>
                  <#else>
                  <button class="aui-button" aria-disabled="true" style="width:170px;" disabled>Test Configuration</button>
                  </#if>
               </div>
               <div class="field-group">
                  <#if (action.idpEntityId??) && (action.idpEntityId != "")>
                  <input type="button" id="display-saml-request" style="width:170px;" value="View SAML Request"  class="aui-button"  onclick="DisplayRequest()"/>
                  &nbsp;
                  <input type="button" id="display-saml-response" style="width:170px;" value="View SAML Response" class="aui-button" onclick="DisplayResponse()" />
                  <#else>
                  <button class="aui-button" aria-disabled="true" style="width:170px;" disabled>View SAML Request</button>
                  &nbsp;&nbsp;&nbsp;
                  <button class="aui-button" aria-disabled="true" style="width:170px;" disabled>View SAML Response</button>
                  </#if>
                  <p>
                      <b>Note:</b>
                      <b>View SAML Request</b> and
                      <b>View SAML Response</b> buttons are needed for SAML debugging. In case, you don't get an appropriate
                      result for Test Configuration and plan on asking for support, please copy the Authentication(SAML)
                      Request and SAML Response you get and email it to us at
                      <a href="mailto:info@xecurify.com"><b>info@xecurify.com</b></a> for further help.
                  </p>
               </div>
            </form>
            <hr class="header" />
            <p style="font-size:13pt;">
               <b>Step 4:</b> Setup IDP to send user attributes
            </p>
            <p>Configure your IDP to send SAML attributes in SAML Response when user logs in. These attributes will be mapped
               to user. Each IDP has a different way of setting this up, contact the IDP or click on
               <b>Support/Feedback</b> widget to help you out.
            </p>
            <br>
            <br>
            <div class="field-group">
               <a href="attributemappingconfig.action">
               <input type="submit" value="Next" class="aui-button aui-button-primary" style="width:100px;" />
               </a>
            </div>
         </div>
      </div>

      </section>

   </body>
</html>