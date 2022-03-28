<html>
    <head>
        <meta name="decorator" content="atl.general"/>
        <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>

         <#if (action.roleMapping??) && (action.roleMapping != "")>        
            <#assign count = roleMapping.keySet().size()> 
        <#else> 
            <#assign count = existingGroups.size()> 
        </#if> 
        
        <#assign groups = existingGroups>
        <#assign idpCount = idpMap.size()>
        <#assign islowerBuild = lowerBuild>
        <script>

            $(document).ready(function() {
	            $(".aui-nav li").removeClass("aui-nav-selected");
	            $("#GROUPMAPPINGCONFIG").addClass("aui-nav-selected");
	            check();
	            enableDisableButtons();

	            var cnt = ${count};
                document.getElementById("idTotalNumberOfRoles").value = cnt + 1;

                var lowerBuild = ""+${islowerBuild};
                console.log("isLowerBuild :"+lowerBuild);
                if (lowerBuild === "false") {
                    console.log("Higher Build");
                    AJS.$("#userGroupKey_0").auiSelect2({
                        placeholder: 'Select the Bamboo Group',
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
                        }
                    });

                    AJS.$("#userGroupKey_0").auiSelect2('data', {
                        id: AJS.$("#userGroupKey_0").val(),
                        text: AJS.$("#userGroupKey_0").val()
                    });

                    <#assign loopCount = 0>

                    for (var i = 1; i < cnt; i++) {
                        AJS.$("#userGroupKey_" + i).auiSelect2({
                            placeholder: 'Select the Bamboo Group',
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
                            }
                        });
                        AJS.$("#userGroupKey_" + i).auiSelect2('data', {
                            id: AJS.$("#userGroupKey_" + i).val(),
                            text: AJS.$("#userGroupKey_" + i).val()
                        });

                        <#assign loopCount = loopCount+1>
                    }

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

                    var defaultGroups = [];

                    <#foreach group in defaultGroupsList>
                        var groupName = "${group}";
                        defaultGroups.push({
                            "id": groupName,
                            "text": groupName
                        });
                    </#foreach>

                    AJS.$("#defaultGroups").auiSelect2('data', defaultGroups);

                    AJS.$("#onTheFlyDoNotRemoveGroups").auiSelect2({
                        placeholder: 'Select the Bamboo Groups',
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

                    var onTheFlyDoNotRemoveGroups = [];

                    <#foreach group in onTheFlyDoNotRemoveGroupsList>
                        var groupName = '${group}';
                        onTheFlyDoNotRemoveGroups.push({
                            "id": groupName,
                            "text": groupName
                        });
                    </#foreach>

                    AJS.$("#onTheFlyDoNotRemoveGroups").auiSelect2('data', onTheFlyDoNotRemoveGroups);
                } else {
                    console.log("Lower Build");
                    AJS.$("#defaultGroup").auiSelect2();
                    AJS.$("#defaultGroups").auiSelect2();
                    AJS.$("#onTheFlyDoNotRemoveGroups").auiSelect2();
                    for (var i = 0; i < cnt; i++) {
                        AJS.$("#userGroupKey_" + i).auiSelect2();
                    }
                }

                var e = document.getElementById("gmIdpName");

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

        	function check() {
                var groupMappingClass = document.getElementById("group-mapping-pill").getAttribute("class");
                console.log("groupMappingClass = "+groupMappingClass);
                if (groupMappingClass == "active") {
                    document.getElementById("onthefly-group-mapping-instructions-div").style.display = "none";
                    document.getElementById("onTheFlyGroupMappingDiv").style.display = "none";
                    document.getElementById("onTheFlyAssignNewGroupsOnlyDiv").style.display = "none";
                    document.getElementById("onTheFlyCreateNewGroupsDiv").style.display = "none";
                    document.getElementById("on-the-fly-group-mapping-main-inst").style.display = "none";
                    document.getElementById("groupMappingDiv").style.display = "block";
                    document.getElementById("group-mapping-main-inst").style.display = "block";

                    document.getElementById("roleAttribute").required = false;
                    document.getElementById("onTheFlyGroupCreation").value = "false";
                } else {
                    document.getElementById("groupMappingDiv").style.display = "none";
                    document.getElementById("group-mapping-instructions-div").style.display = "none";
                    document.getElementById("group-mapping-main-inst").style.display = "none";
                    document.getElementById("onTheFlyGroupMappingDiv").style.display = "block";
                    document.getElementById("onTheFlyAssignNewGroupsOnlyDiv").style.display = "block";
                    document.getElementById("onTheFlyCreateNewGroupsDiv").style.display = "block";
                    document.getElementById("on-the-fly-group-mapping-main-inst").style.display = "block";

                    document.getElementById("roleAttribute").required = true;
                    document.getElementById("onTheFlyGroupCreation").value = "true";

                    var onTheFlyAssignNewGroupsOnly = document.getElementById("onTheFlyAssignNewGroupsOnly").checked;
                    if (onTheFlyAssignNewGroupsOnly == true) {
                        document.getElementById("onTheFlyGroupMappingDiv").style.display = "none";
                    } else {
                        document.getElementById("onTheFlyGroupMappingDiv").style.display = "block";
                    }
                }
        	}

        	function enableDisableButtons() {
                AJS.$.ajax({
                    url: AJS.contextPath() + "/plugins/servlet/saml/getconfig",
                    type: "GET",
                    error: function () {},
                    success: function (response) {
                        if (response.canUserSaveDetails == true) {
                            AJS.$("#amSubmit").prop("disabled", false);
                            AJS.$("#gmSubmit").prop("disabled", false);
                            AJS.$("#gmSubmit_top").prop("disabled", false);
                        } else {
                            AJS.$("#amSubmit").prop("disabled", true);
                            AJS.$("#gmSubmit").prop("disabled", true);
                            AJS.$("#gmSubmit_top").prop("disabled", true);
                        }
                        if (response.idpList.length <= 1) {
                            AJS.$("#selectIDPAmGm").hide();
                        }
                    }
                });
            }

            var counter = ${count};
            var bambooGroups = "${groups}";

            var bambooGroup =  bambooGroups.substr(1, (bambooGroups.length-2)).split(',');
            var optionElement ="";

            for(var group in bambooGroup) {
                  optionElement=optionElement +'<option value ="'+bambooGroup[group]+'">'+bambooGroup[group]+'</option>'
            }

            function GetDynamicTextBox(value){
                var htmlElement;
                var lowerBuild = ${islowerBuild};
                if (lowerBuild == "false") {
                    htmlElement = '<br/>' + '<input id = "userGroupKey_' + counter + '"  name = "userGroupKey_' + counter + '"  placeholder="Bamboo Group" style="width:250px">'
                                                    + '</input> &nbsp&nbsp;'
                                                    + '<input id = "userGroupValue_'+counter+'" name = "userGroupValue_'+counter+'"  type="text" class="text" placeholder="Groups from IdP" style="vertical-align:bottom" value = "' + value + '" />' +'&nbsp&nbsp;'
                                                    + '<input type="button" value="-" class="aui-button aui-button-primary"  onclick = "RemoveTextBox(this,'+counter+')" style="vertical-align:bottom" />'
                } else {
                    htmlElement = '<br/>'+'<select id = "userGroupKey_'+counter+'"  name = "userGroupKey_'+counter+'" style="width:250px" placeholder="Bamboo Group">'
                                  +'<option value ="select-group-to-map">Select Group to Map</option>'
                                  +optionElement
                                  +'</select> &nbsp&nbsp;'
                                  +'<input id = "userGroupValue_'+counter+'" name = "userGroupValue_'+counter+'"  type="text" class="text" placeholder="Groups from IdP" style="vertical-align:bottom" value = "' + value + '" />' +'&nbsp&nbsp;'
                                  +'<input type="button" value="-" class="aui-button aui-button-primary"  onclick = "RemoveTextBox(this,'+counter+')" style="vertical-align:bottom" />'
                }
                return htmlElement;
            }

            function RemoveTextBox(div, loopCount) {
                document.getElementById("roleMappingInnerContainer").removeChild(div.parentNode);
            }

            function AddTextBox(value) {
                var newField;
                for (newField = 0; newField < value; newField++) {
                    var div = document.createElement('DIV');
                    div.innerHTML = GetDynamicTextBox("");
                    var roleMappingInnerContainer = document.getElementById("roleMappingInnerContainer");
                    roleMappingInnerContainer.insertBefore(div, roleMappingInnerContainer.childNodes[0]);
                    var lowerBuild = ${islowerBuild};
                    if (lowerBuild == "false") {
                        AJS.$("#userGroupKey_" + counter).auiSelect2({
                            placeholder: 'Select the Bamboo Group',
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

                            }
                        });
                    } else {
                        AJS.$("#userGroupKey_" + counter).auiSelect2();
                    }
                    counter++;

                    document.getElementById("idTotalNumberOfRoles").value = parseInt(document.getElementById("idTotalNumberOfRoles").value) + 1;
                }
            }

            function showMappingInstruction() {
                var value = document.getElementById("group-mapping-instructions-div");
                if (value.style.display === "none") {
                    AJS.$('#group-mapping-instructions-div').show("slow");
                } else {
                    AJS.$('#group-mapping-instructions-div').hide("slow");
                }
            }

            function showOnTheFlyMappingInstruction() {
                var value = document.getElementById("onthefly-group-mapping-instructions-div");
                if (value.style.display === "none") {
                    AJS.$('#onthefly-group-mapping-instructions-div').show("slow");
                } else {
                    AJS.$('#onthefly-group-mapping-instructions-div').hide("slow");
                }
            }

            function toggleRecommendations() {
                AJS.$("#group-mapping-recommendation").toggle(400);
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
    			width: 284px !important;
    			margin-left: -294px !important;
			}
			
			.field-group{
				margin: 10px 100px 1px 100px !important;
			}

            .pill-nav {
                margin-top: 20px;
            }

            .pill-nav a {
                display: inline-block;
                color: black;
                text-align: center;
                padding: 14px;
                text-decoration: none;
                font-size: 15px;
                border-radius: 5px;
                background: #cec9c9;
            }

            .pill-nav a:hover {
                background-color: #ddd;
                color: black;
            }

            .pill-nav a.active {
                background-color: #0052cc;
                color: white;
            }
    	</style>
    </head>
    <body >
         <#include "*/header.ftl" parse=true>
		<div class="tabs-pane active-pane" id="role-mapping"  role="tabpanel">
        	<div style="margin-top:15px;">
             <div class="toolbar agents-toolbar">
             	<div class="aui-toolbar inline">
             		<ul class="toolbar-group">
                        <li class="toolbar-item">
                            <input id="gmSubmit_top" form="role-mapping-form" type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>
                        </li>
             		</ul>
             	</div>
             </div>
             <p style="font-size:13pt;"><b>Step 6:</b> Configure User Groups Mapping</p>
            </div>
            <br>
            <hr class="header"/>

        	<form id="group-mapping-idp-select-form" class="aui long-label" action="" method="POST">
                <input type="hidden" name="gmIdpChanged" value="true"/>
                <input type="hidden" name="atl_token" value="${xsrfToken}" />
                <input type="hidden" id="changedIdpName" name="idpID"/>
            </form>

			<form id="role-mapping-form" class="aui long-label" action="" method="POST">
                <input type="hidden" name="atl_token" value="${xsrfToken}" />
                <input type="hidden" name="roleMappingSubmitted" value="true"/>
                <input type="hidden" name="totalNumberOfRoles" id="idTotalNumberOfRoles" />

                <#if idpMap.keySet().size() <= 1>
                <div id="selectIDPAmGm" class="field-group" style="display:none">
                <#else>
                <div id="selectIDPAmGm" class="field-group" >
                </#if>
                    <label for="amIdpName">Select IDP : <span style="color: red">*</span></label>
                    <select class="select" name="idpID" id="gmIdpName" style="padding: 3px !important;">
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
                    <label for="restrictUserCreation" class="labelClass">Disable User Creation:</label>
                    <#if restrictUserCreation == true>
                        <input class="checkbox" type="checkbox" name="restrictUserCreation" checked="true" value="true" id="restrictUserCreation"/>
                    <#else>
                        <input class="checkbox" type="checkbox" name="restrictUserCreation" value="true" id="restrictUserCreation" />
                    </#if>
                    <span>New user will not be created only existing users will be able to login through SSO.</span>
                    <div class="description aui-message aui-message-info" style="width: 85%;">
                        User creation will not work for read only directories by default, <a href="https://miniorange.atlassian.net/wiki/spaces/JSDoc/pages/1237417989/User+Directory+Information" target="_blank">Click here</a> for more details. </div>
                </div>

                <hr class="header"/>

                <h3>Default Group Configurations</h3>

                <div class="field-group">
                    <label for="defaultGroup" class="labelClass">Default Group:
                        <span class="aui-icon icon-required">(required)</span>
                    </label>
                    <#if lowerBuild == "true">
                        <#if defaultGroupsList?has_content>
                            <select class="select" name="defaultGroups" id="defaultGroups" multiple size="3" required style="max-width: 500px !important;width: 500px;">
                                <#foreach group in existingGroups>
                                <option value="${group}"
                                    <#foreach selectedGroup in defaultGroupsList>
                                        <#if selectedGroup.equals(group)>
                                            selected
                                        </#if>
                                    </#foreach>
                                    >${group}
                                </option>
                                </#foreach>
                            </select>
                        <#else>
                            <select class="select" name="defaultGroup" id="defaultGroup" multiple size="3" required  style="max-width: 500px !important;width: 500px;">
                                <#foreach group in existingGroups>
                                <option value="${group}"
                                    <#if defaultGroup.equals(group)>
                                        selected
                                    </#if>
                                    >${group}
                                </option>
                                </#foreach>
                            </select>
                        </#if>
                    <#else>
                        <input class="select long-field" name="defaultGroups" id="defaultGroups" style="width: 500px"></input>
                    </#if>

                    <div class="description" >Select Default Group(s) to assign to <strong>New Users / All users</strong>. Select
                        <b>None</b> to disable default group assignment.<br/>
                        <a href="https://miniorange.atlassian.net/wiki/spaces/JSDoc/pages/1237417989/User+Directory+Information" target="_blank">Click here</a> for recommended default group settings for different primary user directory permissions.
                    </div>
                </div>

                <fieldset class="group" style="margin-left: 90px;">
                    <legend>
                        <span>Assign Default Groups To:</span>
                    </legend>
                    <#if enableDefaultGroupsFor == "newUsers">
                        <input type="radio" class="radio" id="newUsers"
                               name="enableDefaultGroupsFor" value="newUsers" checked="checked" style="margin-left: 11px;">
                    <#else>
                        <input type="radio" class="radio" id="newUsers"
                               name="enableDefaultGroupsFor" value="newUsers" style="margin-left: 11px;">
                    </#if>
                    <label for="newUsers">New Users</label>
                    <#if enableDefaultGroupsFor == "allUsers">
                        <input type="radio" class="radio" id="allUsers"
                               name="enableDefaultGroupsFor" value="allUsers" checked="checked">
                    <#else>
                        <input type="radio" class="radio" id="allUsers"
                               name="enableDefaultGroupsFor" value="allUsers">
                    </#if>
                    <label for="allUsers">All Users</label>
                    <#if enableDefaultGroupsFor == "NoIDPGroupUsers">
                        <input type="radio" class="radio" id="enableForNoIDPGroupUsers"
                               name="enableDefaultGroupsFor" value="NoIDPGroupUsers" checked="checked" style="margin-left: 11px;">
                    <#else>
                        <input type="radio" class="radio" id="enableForNoIDPGroupUsers"
                               name="enableDefaultGroupsFor" value="NoIDPGroupUsers" style="margin-left: 11px;">
                    </#if>
                    <label for="enableForNoIDPGroupUsers">Users with no IDP Groups</label>
                    <#if enableDefaultGroupsFor == "doNotAssignDefaultGroup">
                        <input type="radio" class="radio" id="doNotAssignDefaultGroup"
                               name="enableDefaultGroupsFor" value="doNotAssignDefaultGroup" checked="checked">
                    <#else>
                        <input type="radio" class="radio" id="doNotAssignDefaultGroup"
                               name="enableDefaultGroupsFor" value="doNotAssignDefaultGroup">
                    </#if>
                    <label for="doNotAssignDefaultGroup">None</label>
                </fieldset>

                <hr class="header"/>

                <h3>Group Mapping Configurations</h3>
                <div class="aui-message aui-message-info">
                    Group Mapping can be configured in one of the following two ways.
                    <a id="group-mapping-recommendation-link" onclick="toggleRecommendations()" style="cursor: pointer">Click here</a> to know which group mapping configuration you should use.<br>
                    <div id="group-mapping-recommendation" hidden>
                        <ol>
                            <li>
                                If the names of groups in Bamboo are different than the corresponding groups in IDP, then you should use <strong>Manual group mapping</strong>.
                            </li>
                            <li>
                                If the names of groups in Bamboo and IDP are same, you should use <strong>On-The-Fly group mapping</strong>.
                            </li>
                        </ol>
                    </div>
                    <a href="https://miniorange.atlassian.net/wiki/spaces/JSDoc/pages/1237417989/User+Directory+Information" target="_blank">Click here</a> for recommended group settings for different primary user directory permissions.
                    </p>
                </div>
                <p class="aui-message aui-message-info">Don't want to map groups? Enable <b>Disable group mapping</b>, save and skip to <a href="signinsettings.action">SSO Settings</a></p>

                <div class="pill-nav">
                    <a id="group-mapping-pill" style="cursor: pointer"
                       <#if onTheFlyGroupCreation == "false"> class="active" </#if> >Manual Group Mapping</a>
                    <a id="on-the-fly-group-mapping-pill" style="cursor: pointer"
                       <#if onTheFlyGroupCreation == "true"> class="active" </#if> >On-The Fly Group Mapping</a>
                </div>

                <input type="hidden" name="onTheFlyGroupCreation" id="onTheFlyGroupCreation"
                                   value="${onTheFlyGroupCreation}"/>

                <div class="aui-message aui-message-info">
                    <p id="group-mapping-main-inst" class="title">Group Mapping allows you to map your IDP's groups to your
                        Bamboo groups. You can follow <a id="group-mapping-instructions" onclick="showMappingInstruction()"
                                                       style="cursor:pointer">these steps</a> for Group Mapping.
                    </p>
                    <p id="on-the-fly-group-mapping-main-inst" class="title">Users will be assigned to Groups in Bamboo
                        whose group name is same as groups from IDP. If the Group doesn't exists in Bamboo then it will
                        be created.
                        <br/>
                        You can follow <a id="onthefly-group-mapping-instructions"
                                          onclick="showOnTheFlyMappingInstruction()" style="cursor:pointer">these steps</a>
                        for On-The-Fly Group Mapping.
                    </p>
                    <div id="group-mapping-instructions-div" style="display:none">
                        <ol>
                            <li>Go to <a href="listidp.action">Configure IDP</a> and click on Test Configuration.</li>
                            <li>Copy the <i>Attribute Name</i> against the group value and enter in <i>Group Attribute</i>
                                textbox
                                below.
                            </li>
                            <li>Against the Bamboo group given below, enter the name of the group(s) whose users should be
                                added in that Bamboo group.
                            </li>
                            <li>Click on '+' button to add a new row and '+10' button to add 10 new rows for mapping groups
                            </li>
                            <li>
                                You can remove a row by clicking on '-' button. All the unmapped groups will be removed automatically on saving the settings
                            </li>
                        </ol>
                    </div>
                    <div id="onthefly-group-mapping-instructions-div" style="display:none">
                        <ol>
                            <li>Go to <a href="listidp.action">Configure IDP</a> and click on Test Configuration.</li>
                            <li>Copy the <i>Attribute Name</i> against the group value and enter in <i>Group Attribute</i>
                                textbox
                                below.
                            </li>
                            <li>If the user is part of some group in Bamboo and that group is not present in SAML response returned by IDP, then the user will be removed from that group in Bamboo.
                            </li>
                            <li>If you don't want On-The-Fly group mapping to affect Bamboo groups which are managed locally (for eg. Bamboo-users, Bamboo-administrator), than add those groups in Exclude Groups field</li>
                        </ol>
                    </div>
                </div>

                <div class="field-group">
                    <label for="keepExistingUserRoles" class="labelClass">Disable Group Mapping:</label>
                    <#if keepExistingUserRoles == true >
                        <input class="checkbox" type="checkbox" name="keepExistingUserRoles" checked="true" value="true" id="keepExistingUserRoles"/>
                    <#else>
                        <input class="checkbox" type="checkbox" name="keepExistingUserRoles" value="true" id="keepExistingUserRoles"/>
                    </#if>
                    <span>Do not update groups of existing users</span>
                    <div class="description aui-message aui-message-info" style="width: 85%;" >
                        <p>If users are manged from the external user directory for e.g. AD/LDAP with the <b>read only</b>
                           permission. It is recommended to enable <b>"Disable Group Mapping for existing users"</b> option.
                        </p>
                    </div>
                </div>

                <div class="field-group">
                    <label for="roleAttribute" class="labelClass">Group Attribute:</label>
                    <input type="text" id="roleAttribute" name="roleAttribute" placeholder="Enter the group attribute name."
                           value="${roleAttribute}" class="text long-field"/>
                    <div class="description">Enter the SAML-response Attribute Name that contains groups of the User.</div>
                </div>

                <div class="field-group" id="onTheFlyCreateNewGroupsDiv">
                    <label for="createNewGroups">Create New Groups:</label>
                    <#if createNewGroups == true>
                        <input class="checkbox" type="checkbox" name="createNewGroups" checked="true" value="true" id="createNewGroups"/>
                    <#else>
                        <input class="checkbox" type="checkbox" name="createNewGroups" value="true" id="createNewGroups"/>
                    </#if>
                    <span>Create new groups coming from IDP if the group does not already exist in Bamboo.</span>
                </div>

                <div class="field-group" id="onTheFlyAssignNewGroupsOnlyDiv">
                    <label for="onTheFlyAssignNewGroupsOnly" class="labelClass">Keep Existing Users Groups</label>
                    <#if onTheFlyAssignNewGroupsOnly == true>
                        <input class="checkbox" type="checkbox" name="onTheFlyAssignNewGroupsOnly" checked="true" value="true"
                               id="onTheFlyAssignNewGroupsOnly"/>
                    <#else>
                        <input class="checkbox" type="checkbox" name="onTheFlyAssignNewGroupsOnly" value="true"
                               id="onTheFlyAssignNewGroupsOnly"/>
                    </#if>
                    <span>New groups will be assigned but user's existing groups will be not be affected.</span>
                </div>

                <div id="onTheFlyGroupMappingDiv">
                    <div class="field-group">
                         <label>Exclude Groups:</label>
                         <#if lowerBuild == "true">
                             <select class="select" name="onTheFlyDoNotRemoveGroups" id="onTheFlyDoNotRemoveGroups" multiple size="3" style="max-width: 500px !important;width: 500px;">
                                 <#foreach group in existingGroups>
                                     <option value="${group}"
                                         <#foreach selectedGroup in onTheFlyDoNotRemoveGroupsList>
                                             <#if selectedGroup.equals(group)>
                                                 selected
                                             </#if>
                                         </#foreach>
                                         >${group}
                                     </option>
                                 </#foreach>
                             </select>
                         <#else>
                            <input class="select long-field" name="onTheFlyDoNotRemoveGroups" id="onTheFlyDoNotRemoveGroups" style="width: 500px"></input>
                         </#if>
                         <div class="description">Do not remove user from these groups after SSO.</div>
                    </div>
                </div>

                <div id="groupMappingDiv">
                     <div class="field-group">
                         <label for="createUsersIfRoleMapped" class="labelClass">Restrict User Creation based on Group Mapping:</label>
                         <#if createUsersIfRoleMapped == true>
                             <input class="checkbox" type="checkbox" name="createUsersIfRoleMapped" checked="true" value="true" id="createUsersIfRoleMapped" />
                         <#else>
                             <input class="checkbox" type="checkbox" name="createUsersIfRoleMapped" value="true" id="createUsersIfRoleMapped" />
                         </#if>
                             <span>Create users only if their groups are mapped</span>
                     </div>

                    <div class="field-group" id="roleMappingContainer" name="roleMappingContainer">
                        <span style="margin-right: 14px; font-weight: 600;">Add Groups</span>
                        <input type='button' value='+' id='addGroupAttr' class="aui-button aui-button-primary"
                               onclick="AddTextBox(1)">
                        <input type='button' value='+10' id='add10GroupAttr' class="aui-button aui-button-primary"
                               onclick="AddTextBox(10)">
                        <div class="description">
                            By default first 50 groups will shown for mapping and you can add more using <b>"+"</b> or <b>"+10"</b> buttons. All unmapped groups will be removed on saving the configuration.
                        </div>
                        <div id="roleMappingInnerContainer" name="roleMappingInnerContainer">
                            <#assign loopCount = 0>
                            <#if (action.roleMapping??) && (action.roleMapping != "")>
                                <#foreach key in roleMapping.keySet()>
                                    <div>
                                        <br/>
                                        <#if lowerBuild == "true">
                                            <select name="userGroupKey_${loopCount}" id="userGroupKey_${loopCount}" placeholder="Bamboo Group" class="groupmapping">
                                                <#foreach group in existingGroups>
                                                    <option value="${group}"
                                                       <#if key.equals(group)>
                                                              selected
                                                       </#if>
                                                       >${group}
                                                    </option>
                                                </#foreach>
                                            </select>&nbsp&nbsp;
                                        <#else>
                                            <input name="userGroupKey_${loopCount}" id="userGroupKey_${loopCount}"
                                                   placeholder="Bamboo Group" class="select groupmapping" style="width:250px"
                                                   value="${key}">
                                            </input>&nbsp&nbsp;
                                        </#if>
                                        <#assign groupName = roleMapping.get(key)>
                                        <input type="text" id="userGroupValue_${loopCount}" name="userGroupValue_${loopCount}" style="vertical-align:bottom" value="${groupName}" placeholder="IdP's Group" class="text"/>&nbsp&nbsp;
                                        <input type="button" value="-" class="aui-button aui-button-primary" style="vertical-align:bottom" onclick = "RemoveTextBox(this, ${loopCount})" />
                                        <#assign loopCount = loopCount+1>
                                    </div>
                                </#foreach>
                            <#else>
                            	<#foreach key in existingGroups>
                                    <#if loopCount == 50>
                                        <#break>
                                    </#if>
                                    <div>
                                        <br/>
                                        <#if lowerBuild == "true">
                                            <select name="userGroupKey_${loopCount}" id="userGroupKey_${loopCount}" placeholder="Bamboo Group" class="groupmapping">
                                                <#foreach group in existingGroups>
                                                    <option value="${group}"
                                                       <#if key.equals(group)>
                                                              selected
                                                       </#if>
                                                       >${group}
                                                    </option>
                                                </#foreach>
                                            </select>&nbsp&nbsp;
                                        <#else>
                                            <input name="userGroupKey_${loopCount}" id="userGroupKey_${loopCount}"
                                                   placeholder="Bamboo Group" class="select groupmapping" style="width:250px"
                                                   value="${key}">
                                            </input>&nbsp&nbsp;
                                        </#if>
                                        <input type="text" id="userGroupValue_${loopCount}" style="vertical-align:bottom"
                                               name="userGroupValue_${loopCount}" value="" placeholder="Groups from IdP"
                                               class="text"/>&nbsp&nbsp;
                                        <input type="button" value="-" style="vertical-align:bottom"
                                               class="aui-button aui-button-primary"
                                               onclick="RemoveTextBox(this, ${loopCount})"/>
                                        <#assign loopCount = loopCount+1>
                                    </div>
                                </#foreach>
                            </#if>
                        </div>
                    </div>
                </div>
                <br/>
                <div class="field-group">
                    <input id="gmSubmit" type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>&nbsp;&nbsp;&nbsp;
                    <a href="signinsettings.action"><input type="button" value="Next" class="aui-button aui-button-primary" style="width:100px;"/></a>&nbsp;&nbsp;&nbsp;
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