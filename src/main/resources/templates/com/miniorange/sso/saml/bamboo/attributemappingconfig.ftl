<html>
    <head>
        <meta name="decorator" content="atl.general"/>
         <#assign idpCount = idpMap.size()>
         <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
         <script>
        	
        	$(document).ready(function() {
	            $(".aui-nav li").removeClass("aui-nav-selected");
	            $("#ATTRIBUTEMAPPINGCONFIG").addClass("aui-nav-selected");
	            
	            
	            if(document.getElementById("useSeparateNameAttributes").checked){	
					document.getElementById("fullNameAttribute").disabled = true;
					document.getElementById("firstNameAttribute").disabled = false;
					document.getElementById("lastNameAttribute").disabled = false;
				} else {
					document.getElementById("firstNameAttribute").disabled = true;
					document.getElementById("lastNameAttribute").disabled = true;
					document.getElementById("fullNameAttribute").disabled = false;
				}
				
				if(document.getElementById("regexPatternEnabled").checked) {
					document.getElementById("regexfield").style.display = "block";
					var regexPattern = document.getElementById("regexPattern").value;
					if (regexPattern != "") {
					    document.getElementById("test-regex").disabled = false;
					} else {
					    document.getElementById("test-regex").disabled = true;
					}
				} else {
					document.getElementById("regexfield").style.display = "none";
					var regexPattern = document.getElementById("regexPattern").value;
					if (regexPattern != "") {
                        document.getElementById("test-regex").disabled = false;
                    } else {
                        document.getElementById("test-regex").disabled = true;
                    }
				}
				
				if(document.getElementById("loginUserAttribute").value=== "email") {
					document.getElementById("warningforemail").style.display = 'block';
				} else {
					document.getElementById("warningforemail").style.display = 'none';
				}
	            enableDisableButtons();

	            var e = document.getElementById("amIdpName");
	            if (e.options[e.selectedIndex] != undefined) {
                    var idpNm = e.options[e.selectedIndex].text;
                    var idpCnt = ${idpCount};
                    if(idpCnt > 1) {
                        AJS.flag({
                            title: 'SAML IDP Configuration',
                            type: 'info',
                            close: 'auto',
                            body: '<p>Now viewing ' + idpNm + '\'s settings</p>'
                        });
                    }
                }
        	});
        	function enableDisableButtons() {
                AJS.$.ajax({
                    url: AJS.contextPath() + "/plugins/servlet/saml/getconfig",
                    type: "GET",
                    error: function () {},
                    success: function (response) {
                        if (response.canUserSaveDetails == true) {
                            AJS.$("#amSubmit").prop("disabled", false);
                            AJS.$("#gmSubmit").prop("disabled", false);
                        } else {
                            AJS.$("#amSubmit").prop("disabled", true);
                            AJS.$("#gmSubmit").prop("disabled", true);
                        }
                        if (response.idpList.length <= 1) {
                            AJS.$("#selectIDPAmGm").hide();
                            AJS.$("#newAcsUrl").hide();
                        }
                    }
                });
            }
        </script>
        <style>
    		.aui-page-panel-content{
				padding: 0px !important;
			}

			.aui-page-panel{
				margin: 0px !important;
			}
			
			.labelClass {
    			width: 275px !important;
    			margin-left: -286px !important;
			}
			
			.field-group{
				margin: 10px 100px 1px 145px !important;
			}
			
    	</style>
    </head>
    <body >
    	 <#include "*/header.ftl" parse=true>
			<div class="tabs-pane active-pane" id="attribute-mapping" role="tabpanel" >
         		<p style="font-size:13pt;"><b>Step 5:</b> Configure User Profile Attributes Mapping</p>
         		<hr class="header"/>
         		<div class="aui-message aui-message-info">
				    <p>Enter SAML Attributes which are configured in your IDP. Not sure what to enter? Click on Test Configuration button on <a href="listidp.action"> Configure IDP</a>. From the table, copy Attribute Name and paste it against the attributes below.</p>
                </div>
				<form id="attribute-mapping-idp-select-form" class="aui long-label" action="" method="POST">
                    <input type="hidden" name="atl_token" value="${xsrfToken}" />
                    <input type="hidden" name="amIdpChanged" value="true"/>
                    <input type="hidden" id="changedIdpName" name="idpID" value=""/>
                </form>
				<form id="attribute-mapping-form" class="aui" action="" method="POST">
                    <input type="hidden" name="attributeMappingSubmitted" value="true"/>
                    <input type="hidden" name="atl_token" value="${xsrfToken}" />

                    <#if idpMap.keySet().size() <= 1>
                    <div id="selectIDPAmGm" class="field-group" style="display:none">
                    <#else>
                    <div id="selectIDPAmGm" class="field-group" >
                    </#if>
                        <label for="amIdpName">Select IDP : <span style="color: red">*</span></label>
                        <select class="select" name="idpID" id="amIdpName" style="padding: 3px !important;">
                        <#assign idpList = idpMap.keySet()>
                            <#foreach idp in idpList>
                                <#assign idpName = idpMap.get(idp)>
                                <option value="${idp}"
                                <#if idpID.equals(idp)>
                                selected
                                </#if>>
                                ${idpName}</option>
                            </#foreach>
                        </select>
                    </div>

                     <div class="field-group">
                        <label for="keepExistingUserAttributes" class="labelClass">Disable User Profile Mapping:</label>
                        <#if keepExistingUserAttributes == true>
                        <input class="checkbox" type="checkbox" name="keepExistingUserAttributes" checked="true" value="true" id="keepExistingUserAttributes"/>
                        <#else>
                        <input class="checkbox" type="checkbox" name="keepExistingUserAttributes" value="true" id="keepExistingUserAttributes"/>
                        </#if>
                        <span>Do not update attributes of existing users.</span>
                        <div class="description aui-message aui-message-info" style="width: 83%;">
                            <p>If users are manged from the external user directory for e.g. AD/LDAP with the <b>read only</b> permission. It is recommended to enable <b>"Disable User Profile Mapping"</b> option. <a href="https://miniorange.atlassian.net/wiki/spaces/JSDoc/pages/1237417989/User+Directory+Information" target="_blank">Click here</a> for more details.</p>
                        </div>
                    </div>
                    
                    
                    <div class="field-group">
                        <label for="loginUserAttribute" class="labelClass">Create Bamboo user account by:
                            <span class="aui-icon icon-required">(required)</span>
                        </label>
                        <select class="select" name="loginUserAttribute" id="loginUserAttribute">
                            <#foreach options in userLoginOptions>
                            <option value="${options}"
                                <#if loginUserAttribute.equals(options)>
                                selected
                                </#if>
                                >${options}
                            </option>
                            </#foreach>
                        </select>
                        <div id="warningforemail" class="aui-message aui-message-warning" style="display: block;width: 800px;">
			              	Email is not recommended. Select this option only when email of all the user is unique otherwise SSO will not work. 
			        	</div>
			        	</div>
                    
                    <div class="field-group">
                        <label for="usernameAttribute" class="labelClass">Username:
                            <span class="aui-icon icon-required">(required)</span>
                        </label>
                        <input type="text" required="true" id="usernameAttribute" name="usernameAttribute"
                               value="${usernameAttribute}" class="text long-field"/>
                        <div class="description">Enter the SAML-response attribute that contains Bamboo Username. Use
                            <em>NameID</em>
                            if Username is in Subject element.
                        </div>
                        
                         <br/>
			                <div >
			                 	<#if regexPatternEnabled == true>
			                    	<input class="checkbox" type="checkbox" name="regexPatternEnabled" checked="true" value="true" id="regexPatternEnabled"/>
			               		<#else>
			                    	<input class="checkbox" type="checkbox" name="regexPatternEnabled" value="true" id="regexPatternEnabled"/>
			                	</#if>
			                	<span> Apply regular expression on username field.</span>
			                </div>
			                <br/>
			                <div id="regexfield"  >
			                <div>
			                  <input type="text"  id="regexPattern" name="regexPattern" value="${regexPattern}"
			                         placeholder="Regular Expression" class="text long-field" style="width: 322px"/>&nbsp;
			                  <input type="button" id="test-regex" value="Test Regex" class="aui-button" style="width:170px;"  />
			                </div>
			                <div class="description">Enter the regular expression here. It will be applied on value for Attribute Name provided in Username field.<br>
			                        For example, you can use regular expression <b>^.*?(?=@)</b> to extract <b>demo</b> from username <b>demo@example.com</b>
			                </div>

			            </div>
                    </div>
                    
                    <div class="field-group">
                        <label for="emailAttribute" class="labelClass">Email:
                            <span class="aui-icon icon-required">(required)</span>
                        </label>
                        <input type="text" required="true" id="emailAttribute" name="emailAttribute"
                               value="${emailAttribute}" class="text long-field"/>
                        <div class="description">Enter the SAML-response attribute that contains Email. Use
                            <em>NameID</em>
                            if Email is in Subject element.
                        </div>
                    </div>
                    
                    <div class="field-group">
                        <label for="fullNameAttribute" class="labelClass">Full Name Attribute:</label>

                        <input type="text" id="fullNameAttribute" name="fullNameAttribute"
                               value="${fullNameAttribute}" class="text long-field"/>

                        <div class="description">Enter the SAML-response attribute that contains Full Name.</div>
                    </div>
                    
                    <div class="field-group">
                        <label for="useSeparateNameAttributes" class="labelClass">Separate Name Attributes:</label>
                        <#if useSeparateNameAttributes == true >
                        <input class="checkbox" type="checkbox" checked="true" value="true"
                               name="useSeparateNameAttributes" id="useSeparateNameAttributes"/>
                        <#else>
                        <input class="checkbox" type="checkbox" value="true"
                               name="useSeparateNameAttributes" id="useSeparateNameAttributes" />
                        </#if>
                        <span> Map First name and Last name as separate attributes.</span>
                    </div>
                    
                    <div class="field-group">
                        <label for="firstNameAttribute" class="labelClass">First Name:</label>
                        <input type="text" id="firstNameAttribute" name="firstNameAttribute"
                               value="${firstNameAttribute}" class="text long-field"/>

                        <div class="description">Enter the SAML-response attribute that contains First Name.</div>
                    </div>
                    
                    <div class="field-group">
                        <label for="lastNameAttribute" class="labelClass">Last Name:</label>
                        	<input type="text" id="lastNameAttribute" name="lastNameAttribute"
                              	 value="${lastNameAttribute}" class="text long-field"/>

                        <div class="description">Enter the SAML-response attribute that contains Last Name. </div>
                    </div>
                    <br/>
                    <div class="field-group">
                        <input id="amSubmit" type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>&nbsp;&nbsp;&nbsp;
						<a href="groupmappingconfig.action"><input type="button" value="Next" class="aui-button aui-button-primary" style="width:100px;"/></a>&nbsp;&nbsp;&nbsp;
                        <a href="listidp.action" style="width:300px;">Back to configuration</a>
                    </div>
                </form>
				
           </div>
		</div>
	   </section>
      </div>
     </div>
    </body>
</html>
