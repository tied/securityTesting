<html>
    <head>
        <meta name="decorator" content="atl.general"/>
        <content tag="show-main-header">false</content>
    	<content tag="show-main-container">false</content>
        <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
    	<script>
	        $(document).ready(function() {
	            $(".aui-nav li").removeClass("aui-nav-selected");
	            $("#LISTIDP").addClass("aui-nav-selected");

	            checkDomainMapping();
	        });

	        function checkDomainMapping() {
                if (AJS.$("#domain-mapping").is(":checked")) {
                    AJS.$("[name='domains']").prop("disabled", false);
                } else {
                    AJS.$("[name='domains']").prop("disabled", true);
                }
            }

	        function testConfigurations(idpname){
                var osDestination = "testidpconfiguration" ;
                var samlAuthUrl = AJS.contextPath() + '/plugins/servlet/saml/auth';
                samlAuthUrl += "?return_to=" + osDestination+"&idp="+idpname;
                var myWindow = window.open(samlAuthUrl, "TEST SAML IDP", "scrollbars=1 width=800, height=600");
            }

            function suppressNewWindow(e) {
                if(e.keyCode == 13){
                    e.preventdefault();
                }
            }

            function showDeleteDialog(idp) {
                var dialog = new AJS.Dialog({
                    width: 400,
                    height: 150,
                    id: "delete-dialog",
                    closeOnOutsideClick: true
                });
                dialog.addPanel("Panel 1", "<p>Are you sure you want to delete this IDP?</p>", "panel-body");
                dialog.addButton("Yes", function (dialog) {
                    dialog.hide();
//                    window.location = "deleteidp.action?idpID="+idp;
                    document.getElementById("idpID").value = idp;
                    document.getElementById("delete_idp_form").submit();
                });
                dialog.addLink("No", function (dialog) {
                    dialog.hide();
                }, "#");

                dialog.show();
            }

            function showIdpSsoInfo(){
                var idpSsoInfoDiv = document.getElementById("multiple-idp-sso-info");
                if(idpSsoInfoDiv.style.display == "none") {
                    idpSsoInfoDiv.style.display = "block";
                } else {
                    idpSsoInfoDiv.style.display = "none";
                }
            }
    	</script>
    	<style>
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

            <div id="idp-list" style="display: block;">
                <hr class="header"/>
                <div>
                    <p style="font-size:13pt;">List of IdPs</p>
                </div>
                <hr class="header"/>
                <form id="delete_idp_form" name="metadata_import_form" method="POST" action="deleteidp" class="aui long-label">
                    <input type="hidden" name="atl_token" value="${xsrfToken}" />
                    <input type="hidden" id="idpID" name="idpID"/>
                </form>

                <form id="list-idp-form" name="list-idp-form" method="post" action="" class="aui long-label">
                    <input type="hidden" name="domainsSubmitted" value="true">
                    <input type="hidden" name="atl_token" value="${xsrfToken}" />

                    <a id="multiple-idp-sso-info-link" onclick="showIdpSsoInfo()" style="cursor:pointer;"><strong>How does User SSO work with multiple IDP?</strong></a>

                    <span style="margin-left:65%;float: right;padding-bottom: 10px;" style="vertical-align:middle;">
                        <strong>Use Domain Mapping</strong>
                        <span style="vertical-align:middle;">
                            <label class="switch" style="vertical-align:middle;">
                                <#if useDomainMapping == true>
                                    <input type="checkbox" id="domain-mapping" value="true" name="useDomainMapping" checked="true" lable="Use Domain Mapping" onclick="checkDomainMapping()">
                                    <span class="slider round"></span>
                                <#else>
                                    <input type="checkbox" id="domain-mapping" value="true" name="useDomainMapping" lable="Use Domain Mapping" onclick="checkDomainMapping()">
                                    <span class="slider round"></span>
                                </#if>
                            </label>
                            <span title="Domain mapping determines user's IDP using his email address domain; e.g. gmail.com for IDP1 will redirect demo@gmail.com to IDP1" class="aui-icon aui-icon-small aui-iconfont-info-filled"></span>
                        </span>
                    </span>

                    <div id="multiple-idp-sso-info" class="aui-message aui-message-info" style="display:none; margin-top: 30px;">
                        You can control which IDP the user will be authenticated through on this page. The user can be authenticated in the following ways:
                        <ol>
                            <li><strong>IDP selection dropdown</strong>: A dropdown of all IDPs will be shown on Bamboo's login page. The user can select their IDP for SSO. The IDP names configured in the app will be listed in the dropdown list. Make sure you configure easy to understand IDP names.</li>
                            <li><strong>Domain Mapping</strong>: Domain mapping determines user's IDP using configured email domains against IDPs. For domain mapping option, each IDP must be configured with atleast one domain name. For example, if IDP1 is configured with miniorange.com and IDP2 with example.com, then user with email user@miniorange.com will get redirected to IDP1. The user experience during SSO will work as follows -
                                <ul>
                                    <li>A form similar to the login page with only email address field will be shown to users when they try to access the login page.</li>
                                    <li>If the email address domain matches any of the domains configured, the user will be redirected to that IDP to proceed with SSO.</li>
                                    <li>If the email address does not match any domains configured, then user will see an error message on the Domain Mapping form.</li>
                                    <li>If the email address does not match any domains configured, the user will be redirected to the login form to login manually.
                                        <ul>
                                            <li><strong>If Auto-Redirect to IDP is disabled:</strong> User will be shown Bamboo login form for manual login </li>
                                            <li><strong>If Auto-Redirect to IDP is enabled:</strong> An error message will be shown to the user and the user will be asked to enter email address again.
                                        </ul>
                                    </li>
                                </ul>
                            </li>
                        </ol>
                    </div>

                    <table class="aui aui-table-interactive">
                    <tr>
                        <td width="20%"><b>IdP Name</b></td>
                        <td width="40%" style="text-align:center;"><b>Domain Mapping</b></td>
                        <td width="40%" style="text-align:center;"><b>Actions</b></td>
                    </tr>
                    <#assign index = 0>
                    <#foreach idp in idpList>
                        <#assign idpName = idpMap.get(idp)>
                        <tr>
                            <td width="20%">${idpName}</td>
                            <td width="40%" style="text-align:center;">
                                <#assign domainListSize = domainsList?size >
                                <#assign domainListSizeCheck = (domainListSize > index) >
                                <#assign domainValue = domainListSizeCheck?then(domainsList.get(index), '') >
                                <#if domainListSizeCheck == true && !(domainValue == "")>
                                    <input type="text" name="domains" value="${domainValue}" placeholder="Enter ; separated domains e.g. gmail.com;miniorange.com" class="text long-field"
                                    />
                                <#else>
                                    <input type="text" name="domains" value="" placeholder="Enter ; separated domains eg. gmail.com;miniorange.com;example.com" class="text long-field" onkeypress="suppressNewWindow(event);" />
                                </#if>
                            </td>
                            <td width="40%" style="text-align:center;"><a class="aui-button" href="addidp.action?idpid=${idp}&operation=edit">Edit</a>&nbsp;&nbsp;
                                <a class="aui-button" id="test-saml-config" name="test-saml-config" onclick="testConfigurations('${idp}')">Test</a>&nbsp;&nbsp;
                                <a class="aui-button" onclick="showDeleteDialog('${idp}')">Delete</a></td>
                        </tr>
                        <#assign index = index+1>
                    </#foreach>
                </table>
                <br/>
                <div class="buttons-container" style=" margin-left: 18%; width: 60%; ">
                    <input type="submit" class="aui-button aui-button-primary" value="Save">
                    <a class="aui-button aui-button-primary" href="addidp.action?operation=add">
                        Add new IdP
                    </a>
                </div>
                </form>
            </div>
		</div>
	   </section>
	  </div>
	 </div>
    </body>
</html>