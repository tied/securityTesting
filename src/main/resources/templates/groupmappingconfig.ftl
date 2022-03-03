<html>
    <head>
        <title>OAuth Configuration</title>
        <meta name="decorator" content="atl.general"/>


        <#if (action.roleMapping??) && (action.roleMapping != "") && !roleMapping.isEmpty()>
            <#assign count = roleMapping.keySet().size()>
        <#else>
            <#assign count = existingGroups.size()>
        </#if>
        <#assign groups = existingGroups>
        <#assign islowerBuild = lowerBuild>

        <script>
        <#include "/js/groupmappingconfig.js">
            AJS.$(document).ready(function () {
                AJS.$(".aui-nav li").removeClass("aui-nav-selected");
                AJS.$("#usergroups").addClass("aui-nav-selected");

                check();

                var cnt = ${count};
                document.getElementById("idTotalNumberOfRoles").value = cnt + 1;

                var lowerBuild = ""+${islowerBuild};
                if (lowerBuild === "false") {
                    AJS.$("#userGroupKey_0").auiSelect2({
                        placeholder: 'Select the Bamboo Group',
                        ajax: {
                            url: AJS.contextPath() + '/plugins/servlet/oauth/moapi',
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
                                url: AJS.contextPath() + '/plugins/servlet/oauth/moapi',
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
                            url: AJS.contextPath() + '/plugins/servlet/oauth/moapi',
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
                            url: AJS.contextPath() + '/plugins/servlet/oauth/moapi',
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
                        var groupName = "${group}";
                        onTheFlyDoNotRemoveGroups.push({
                            "id": groupName,
                            "text": groupName
                        });
                    </#foreach>

                    AJS.$("#onTheFlyDoNotRemoveGroups").auiSelect2('data', onTheFlyDoNotRemoveGroups);
                } else {
                    AJS.$("#defaultGroup").auiSelect2();
                    AJS.$("#defaultGroups").auiSelect2();
                    AJS.$("#onTheFlyDoNotRemoveGroups").auiSelect2();
                    for (var i = 0; i < cnt; i++) {
                        AJS.$("#userGroupKey_" + i).auiSelect2();
                    }
                }

            });

            function check() {
                var groupMappingClass = document.getElementById("group-mapping-pill").getAttribute("class");
                if (groupMappingClass == "active") {
                    document.getElementById("onthefly-group-mapping-instructions-div").style.display = "none";
                    document.getElementById("onTheFlyGroupMappingDiv").style.display = "none";
                    document.getElementById("onTheFlyAssignNewGroupsOnlyDiv").style.display = "none";
                    document.getElementById("onTheFlyCreateNewGroupsDiv").style.display = "none";
                    document.getElementById("on-the-fly-group-mapping-main-inst").style.display = "none";
                    document.getElementById("onTheFlyFilterIdpGroups").style.display = "none";
                    document.getElementById("groupMappingDiv").style.display = "block";
                    document.getElementById("group-mapping-main-inst").style.display = "block";

                    document.getElementById("roleAttribute").required = false;
                    document.getElementById("roleAttributeAsterisk").style.display = "none";
                    document.getElementById("onTheFlyGroupCreation").value = false;
                } else {
                    document.getElementById("groupMappingDiv").style.display = "none";
                    document.getElementById("group-mapping-instructions-div").style.display = "none";
                    document.getElementById("group-mapping-main-inst").style.display = "none";
                    document.getElementById("onTheFlyGroupMappingDiv").style.display = "block";
                    document.getElementById("onTheFlyAssignNewGroupsOnlyDiv").style.display = "block";
                    document.getElementById("onTheFlyCreateNewGroupsDiv").style.display = "block";
                    document.getElementById("on-the-fly-group-mapping-main-inst").style.display = "block";
                    document.getElementById("onTheFlyFilterIdpGroups").style.display = "block";

                    document.getElementById("roleAttribute").required = true;
                    document.getElementById("roleAttributeAsterisk").style.display = "block";
                    document.getElementById("onTheFlyGroupCreation").value = true;

                    var onTheFlyAssignNewGroupsOnly = document.getElementById("onTheFlyAssignNewGroupsOnly").checked;
                    if (onTheFlyAssignNewGroupsOnly == true) {
                        document.getElementById("onTheFlyGroupMappingDiv").style.display = "none";
                    } else {
                        document.getElementById("onTheFlyGroupMappingDiv").style.display = "block";
                    }
                }
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
                var lowerBuild = ""+${islowerBuild};
                if (lowerBuild === "false") {
                    htmlElement = '<br/>' + '<input id = "userGroupKey_' + counter + '"  name = "userGroupKey_' + counter + '"  placeholder="Bamboo Group" style="width:250px">'
                                                    + '</input>&nbsp&nbsp&nbsp&nbsp;'
                                                    + '<input id = "userGroupValue_'+counter+'" name = "userGroupValue_'+counter+'"  type="text" class="text" placeholder="Groups from Application" style="vertical-align:bottom" value = "' + value + '" />' +'&nbsp&nbsp&nbsp;'
                                                    + '<input type="button" value="-" class="aui-button aui-button-primary"  onclick = "RemoveTextBox(this,'+counter+')" style="vertical-align:bottom" />'
                } else {
                    htmlElement = '<br/>'+'<select id = "userGroupKey_'+counter+'"  name = "userGroupKey_'+counter+'" style="width:250px" placeholder="Bamboo Group">'
                                  +'<option value ="select-group-to-map">Select Group to Map</option>'
                                  +optionElement
                                  +'</select> &nbsp&nbsp;'
                                  +'<input id = "userGroupValue_'+counter+'" name = "userGroupValue_'+counter+'"  type="text" class="text" placeholder="Groups from Application" style="vertical-align:bottom" value = "' + value + '" />' +'&nbsp&nbsp;'
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
                    var lowerBuild = ""+${islowerBuild};
                    if (lowerBuild === "false") {
                        AJS.$("#userGroupKey_" + counter).auiSelect2({
                            placeholder: 'Select the Bamboo Group',
                            ajax: {
                                url: AJS.contextPath() + '/plugins/servlet/oauth/moapi',
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
                AJS.$('#group-mapping-recommendation').toggle(400);
            }
        </script>
        <style>
            .aui-page-panel-content{
                padding: 0px !important;
            }

            .aui-page-panel{
                margin: 0px !important;
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
    <body>
        <#include "*/header.ftl" parse=true>
        <div class="tabs-pane active-pane" id="role-mapping" role="tabpanel">

        <div  id="role-mapping" role="tabpanel">
            <p style="font-size:16pt;">Configure User Groups
                <span style="float:right;margin-right:25px;">
                    <input id="gmSubmit_top" type="submit" form="role-mapping-form" value="Save" class="aui-button aui-button-primary" style="width:100px;" resolved="">
                </span>
            </p>
            <hr class="header"/>

            <form id="role-mapping-form" class="aui  long-label" action="" method="POST">
                <input type="hidden" name="atl_token" value="${xsrfToken}"/>
                <input type="hidden" name="roleMappingSubmitted" value="true"/>
                <input type="hidden" name="totalNumberOfRoles" id="idTotalNumberOfRoles"/>

                <div class="field-group">
                    <label for="restrictUserCreation" class="labelClass">Disable User Creation:</label>
                    <#if restrictUserCreation == true>
                        <input class="checkbox" type="checkbox" name="restrictUserCreation" checked="true" value="true"
                               id="restrictUserCreation"/>
                    <#else>
                        <input class="checkbox" type="checkbox" name="restrictUserCreation" value="true"
                               id="restrictUserCreation"/>
                    </#if>
                    <span>If checked, New user will not be created.</span>
                    <div class="description aui-message aui-message-info" style="width:92%"> User creation will not work for read only directories by default, <a href="https://miniorange.atlassian.net/wiki/spaces/JSDoc/pages/1237417989/User+Directory+Information" target="_blank">Click here</a> for more details</div>
                </div>

                <hr class="header"/>
                <h3>Default Group Configurations</h3>
                <br/>

                <div id="defaultGroupDiv">
                    <div class="field-group">
                        <label for="defaultGroups">Default Group:
                            <span class="aui-icon icon-required">(required)</span>
                        </label>
                        <#if lowerBuild == "true">
                            <#if defaultGroupsList?has_content>
                                <select class="select" name="defaultGroups" id="defaultGroups" multiple size="3" required
                                        style="max-width: 500px !important;width: 500px;">
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
                                <select class="select" name="defaultGroup" id="defaultGroup" multiple size="3" required
                                        style="max-width: 500px !important;width: 500px;">
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
                            <input class="select long-field" name="defaultGroups" id="defaultGroups"
                                   style="width: 500px" required>
                        </#if>
                        <div class="description" >Select Default Group(s) to assign to <strong>New Users / All users</strong>. Select
                            <b>None</b> to disable default group assignment.
                    </div>
                </div>

                <fieldset class="group">
                    <legend>
                        <span>Assign Default Group To:</span>
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
                    <a id="group-mapping-recommendation-link" onclick="toggleRecommendations()" style="cursor: pointer">Click
                        here</a> to know which group mapping configuration you should use.
                    <div id="group-mapping-recommendation" hidden>
                        <ol>
                            <li>
                                If the names of groups in Bamboo are different than the corresponding groups in Application,
                                then you should use <strong>Manual group mapping</strong>.
                            </li>
                            <li>
                                If the names of groups in Bamboo and Applications are same, you should use <strong>On-The-Fly 
                                group mapping</strong>.
                            </li>
                        </ol>
                    </div>
                </div>
                <p class="aui-message aui-message-info" style="width:93%">
  					<a href="https://miniorange.atlassian.net/wiki/spaces/JSDoc/pages/1237417989/User+Directory+Information" target="_blank">Click here</a> for recommended group mapping settings for different primary user directory permissions.
     					Don't want to map user groups? Disable Group Mapping, save and skip to <a href="signinsettings.action">Sign In Settings</a></p>
     				<br/>

                <div class="mapping-nav">
                    <div class="pill-nav">
                        <a id="group-mapping-pill" style="cursor: pointer"
                                <#if onTheFlyGroupCreation == false> class="active" </#if> >Manual Group Mapping</a>
                        <a id="on-the-fly-group-mapping-pill" style="cursor: pointer"
                                <#if onTheFlyGroupCreation == true> class="active" </#if> >On-The Fly Group Mapping</a>
                    </div>
                </div>

                <input type="hidden" name="onTheFlyGroupCreation" id="onTheFlyGroupCreation" value="${onTheFlyGroupCreation?string}"/>
                <div class="aui-message aui-message-info">
                    <p id="group-mapping-main-inst" class="title">Group Mapping allows you to map your Application's groups to
                        your Bamboo groups.
                        <br/>
                        You can follow <a id="group-mapping-instructions" onclick="showMappingInstruction()"
                                                  style="cursor:pointer">these steps</a> for Group Mapping.</p>

                    <p id="on-the-fly-group-mapping-main-inst" class="title" style="margin-top:0">User will be assigned to Groups in Bamboo whose group
                        name is same as groups from Application. If the Group doesn't exists in Bamboo then it will be created.
                        <br/>
                        You can follow <a id="onthefly-group-mapping-instructions" onclick="showOnTheFlyMappingInstruction()"
                                          style="cursor:pointer">these steps</a> for On-The-Fly Group Mapping.</p>


                    <div id="group-mapping-instructions-div" style="display:none">
                        <ol>
                            <li>Go to <a href="configure.action">Configure OAuth</a> and click on Test Configuration.</li>
                            <li>Copy the <i>Attribute Name</i> against the group value and enter in <i>Group Attribute</i>
                                textbox below.
                            </li>
                            <li>Against the Bamboo group given below, enter the name of the group(s) whose users should be added
                                in that Bamboo group.
                            </li>
                        </ol>
                    </div>
                    <div id="onthefly-group-mapping-instructions-div" style="display:none">
                        <ol>
                            <li>Go to <a href="configure.action">Configure OAuth</a> and click on Test Configuration.</li>
                            <li>Copy the <i>Attribute Name</i> against the group value and enter in <i>Group Attribute</i>
                                textbox below.
                            </li>
                            <li>If the user is part of some group in Bamboo and that group is not present in response
                                returned by Application, then the user will be removed from that group in Bamboo.
                            </li>
                            <li>If you don't want On-The-Fly group mapping to affect Bamboo groups which are managed locally
                                (for eg. Bamboo-users, Bamboo-administrator), then add those groups in <strong>Exclude
                                    Groups</strong> field
                            </li>
                        </ol>
                    </div>
                </div>
                <br/>

                <div class="field-group">
                    <label for="keepExistingUserRoles" class="labelClass">Disable Group Mapping:</label>
                    <#if keepExistingUserRoles == true >
                        <input class="checkbox" type="checkbox" name="keepExistingUserRoles" checked="true" value="true"
                               id="keepExistingUserRoles"/>
                    <#else>
                        <input class="checkbox" type="checkbox" name="keepExistingUserRoles" value="true"
                               id="keepExistingUserRoles"/>
                    </#if>
                    <span>If checked, groups of existing users will not be updated.</span>
                    <div class="description">If users are manged from the external user directory for e.g. AD/LDAP with the <b>read only</b>
                    permission. It is recommended to enable <b>"Disable Group Mapping"</b> option.<a href="https://miniorange.atlassian.net/wiki/spaces/JSDoc/pages/1237417989/User+Directory+Information" target="_blank"> Click here</a> for more details.
                    </div>
                </div>

                <div class="field-group">
                    <label for="roleAttribute" class="labelClass">Group Attribute:
	                    <span id="roleAttributeAsterisk" class="aui-icon icon-required">(required)</span>
                    </label>
                    <input type="text" id="roleAttribute" name="roleAttribute" placeholder="Enter the group attribute name."
                           value="${roleAttribute}" class="text long-field"/>
                    <div class="description">Enter the Attribute Name that contains groups of the User.</div>
                </div>

                <div class="field-group" id="onTheFlyFilterIdpGroups">
                    <label>Filter Groups:</label>
                    <select class="select" id="onTheFlyFilterIDPGroupsOption" name="onTheFlyFilterIDPGroupsOption" style="width:225px;">
                        <#foreach option in groupFilterOptionsList>
                            <option value="${option}"
                                <#if option.equals(onTheFlyFilterIDPGroupsOption)>
                                selected
                                </#if> > ${option}</option>
                        </#foreach>
                    </select>&nbsp&nbsp&nbsp&nbsp&nbsp

                    <input type="text" placeholder="Enter filter/regex pattern" class="text" value="${onTheFlyFilterIDPGroupsKey}"
                           name="onTheFlyFilterIDPGroupsKey" id="onTheFlyFilterIDPGroupsKey"
                            <#if onTheFlyFilterIDPGroupsOption.equals("None")> style="display:none;" </#if> />
                    &nbsp&nbsp&nbsp&nbsp&nbsp

                    <input type="button" class="button" id="checkGroupRegex" value="Test Regex"
                           <#if !onTheFlyFilterIDPGroupsOption.equals("Regex")> style="display:none" </#if>
                            <#if onTheFlyFilterIDPGroupsKey.equals("")> disabled </#if> />

                    <div class="description">Select how you want to <b>filter groups</b> receiving from the IDP.</div>
                </div>

                <div id="onTheFlyGroupRegexDiv">
                    <div class="field-group">
                        <#if groupRegexPatternEnabled == true>
                        <input class="checkbox" type="checkbox" name="groupRegexPatternEnabled" checked="true" value="true" id="groupRegexPatternEnabled"/>
                        <#else>
                        <input class="checkbox" type="checkbox" name="groupRegexPatternEnabled" value="true" id="groupRegexPatternEnabled"/>
                        </#if>
                        <span> Apply regular expression on <strong>Group Name</strong>.</span>
                    </div>

                <div class="field-group" id="groupRegexfield">
                    <div>
                        <label for="regexPatternForGroup">Replace</label>
                        <input type="text medium-field" id="regexPatternForGroup" name="regexPatternForGroup" value="${regexPatternForGroup}"
                               placeholder="Regular Expression" class="text long-field" style="width: 150px"/>&nbsp;&nbsp;&nbsp;&nbsp;
                        <label for="regexGroups">With</label>
                        <input  type="text medium-field" id="regexGroups"  name="regexGroups" value="${regexGroups}"
                                placeholder="Replace With" class="text long-field" style="width: 150px"/>
                    </div><br>
                    <div style = "padding-left: 53px">
                        <input type="text" id="testGroupRegex" name="testRegex" value="${testRegex}" placeholder="Group Name to Test Regex " class="text long-field" style="width:210px;" />&nbsp;&nbsp
                        <input type="button" id="test-group-regex" value="Test Group Regex" class="aui-button" style="width:130px;" />
                    </div>
                    <div class="description">Enter the regular expression here. It will be applied on value for group
                        Name provided in the field.<br>
                        For example, you can use regular expression <b>(.*):group:(.*)#</b> and replacement <b>$2</b> to extract <b>jira-software-users</b> from groupName
                        <b>urn:group:jira-software-users#idp</b>
                    </div>
                    <br>
                </div>
              </div>


                <div class="field-group" id="onTheFlyCreateNewGroupsDiv">
                    <label for="onTheFlyCreateNewGroups">Create New Groups:</label>
                    <#if onTheFlyCreateNewGroups == true>
                        <input class="checkbox" type="checkbox" name="onTheFlyCreateNewGroups" checked="true" value="true" id="onTheFlyCreateNewGroups"/>
                    <#else>
                        <input class="checkbox" type="checkbox" name="onTheFlyCreateNewGroups" value="true" id="onTheFlyCreateNewGroups"/>
                    </#if>
                    <span>New groups from Application will be created if not found in Bamboo.</span>
                </div>

                <div class="field-group" id="onTheFlyAssignNewGroupsOnlyDiv">
                    <label for="onTheFlyAssignNewGroupsOnly" class="labelClass">Keep Existing Users Groups:</label>
                    <#if onTheFlyAssignNewGroupsOnly == true>
                        <input class="checkbox" type="checkbox" name="onTheFlyAssignNewGroupsOnly" checked="true"
                               value="true"
                               id="onTheFlyAssignNewGroupsOnly"/>
                    <#else>
                        <input class="checkbox" type="checkbox" name="onTheFlyAssignNewGroupsOnly" value="true"
                               id="onTheFlyAssignNewGroupsOnly"/>
                    </#if>
                    <span>New groups will be assigned but user's existing groups will be not be affected.</span>
                </div>

                <div class="field-group" id="onTheFlyGroupMappingDiv">
                        <label>Exclude Groups:</label>
                        <#if lowerBuild == "true">
                            <select class="select long-field" name="onTheFlyDoNotRemoveGroups" id="onTheFlyDoNotRemoveGroups" multiple size="3" >
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
                            <input class="select long-field" name="onTheFlyDoNotRemoveGroups" id="onTheFlyDoNotRemoveGroups"></input>
                        </#if>
                        <div class="description">Do not remove user from these groups after SSO.</div>
                </div>

                <div id="groupMappingDiv">
                    <div id="createUsersIfRoleMappedDiv">
                        <div class="field-group">
                            <label for="createUsersIfRoleMapped" class="labelClass">Restrict User Creation based on Group
                                Mapping:</label>
                            <#if createUsersIfRoleMapped == true>
                                <input class="checkbox" type="checkbox" name="createUsersIfRoleMapped" checked="true"
                                       value="true" id="createUsersIfRoleMapped" onchange="checkDefaultGroup()"/>
                            <#else>
                                <input class="checkbox" type="checkbox" name="createUsersIfRoleMapped" value="true"
                                       id="createUsersIfRoleMapped"/>
                            </#if>
                            <span>If checked, users will be created only if groups are mapped.</span>
                            <div class="description aui-message aui-message-info" style="width:92%"> User creation will not work for read only directories by default, <a href="https://miniorange.atlassian.net/wiki/spaces/JSDoc/pages/1237417989/User+Directory+Information" target="_blank">Click here</a> for more details</div>
                        </div>
                    </div>

                    <div class="field-group" id="roleMappingContainer" name="roleMappingContainer">
                        <span style="margin-right: 14px; font-weight: 600;">Add Groups</span>
                        <input type='button' value='+' id='addGroupAttr' class="aui-button aui-button-primary"
                               onclick="AddTextBox(1)">
                        <input type='button' value='+10' id='add10GroupAttr' class="aui-button aui-button-primary"
                               onclick="AddTextBox(10)">
                        <div class="description">
                            All unmapped groups will be removed on saving the configuration. You can re-add them using
                            <b>"+"</b> or <b>"+10"</b> buttons.
                        </div>

                        <div id="roleMappingInnerContainer" name="roleMappingInnerContainer">
                            <#assign loopCount = 0>
                            <#if (action.roleMapping??) && (action.roleMapping != "") && !roleMapping.isEmpty()>
                                <#foreach key in roleMapping.keySet()>
                                    <div>
                                        <br/>
                                        <#if lowerBuild == "true">
                                            <select name="userGroupKey_${loopCount}" id="userGroupKey_${loopCount}"
                                                    placeholder="Bamboo Group" class="groupmapping">
                                                <#foreach key in existingGroups>
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
                                        <input type="text" id="userGroupValue_${loopCount}" name="userGroupValue_${loopCount}"
                                               style="vertical-align:bottom" value="${groupName}" placeholder="Groups from Application"
                                               class="text"/>&nbsp&nbsp;
                                        <input type="button" value="-" class="aui-button aui-button-primary"
                                               style="vertical-align:bottom" onclick="RemoveTextBox(this, ${loopCount})"/>
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
                                            <select name="userGroupKey_${loopCount}" id="userGroupKey_${loopCount}"
                                                    placeholder="Bamboo Group" class="groupmapping">
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
                                            &nbsp&nbsp;
                                        </#if>
                                        <input type="text" id="userGroupValue_${loopCount}" style="vertical-align:bottom"
                                               name="userGroupValue_${loopCount}" value="" placeholder="Groups from Application"
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
                    <a href="signinsettings.action"><input type="button" value="Next" class="aui-button aui-button-primary"
                                                           style="width:100px;"/></a>&nbsp;&nbsp;&nbsp;
                    <a href="configure.action" style="width:300px;">Back to configuration</a>
                </div>
            </form>
        </div>
        </div>
        </section>
        </div>
        </div>
    </body>
</html>