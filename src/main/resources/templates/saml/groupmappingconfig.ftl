<html>
<head>

    <#if roleMapping.isEmpty()>
        <#assign count = existingGroups.size()>
    <#else>
        <#assign count = roleMapping.keySet().size()>
    </#if>

    <#assign groups = existingGroups>

    <meta name="decorator" content="atl.general"/>
    <script>
		<#include "/js/groupmappingconfig.js">
        AJS.$(document).ready(function () {

            var idp = '${idpName}';
            var idpCount = '${idpMap.size()}';
            if(idpCount > 1) {
                AJS.flag({
                    title: 'User Groups Configuration',
                    type: 'info',
                    close: 'auto',
                    body: '<p>Now viewing '+idp+'\'s settings</p>'
                });
                AJS.$("#selectIDPAmGm").show();
            }

            var cnt = '${count}';

            document.getElementById("idTotalNumberOfRoles").value = cnt + 1;

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
                var groupName = "${group}";
                onTheFlyDoNotRemoveGroups.push({
                    "id": groupName,
                    "text": groupName
                });
            </#foreach>

            AJS.$("#onTheFlyDoNotRemoveGroups").auiSelect2('data', onTheFlyDoNotRemoveGroups);
        });

        var counter = '${count}';

        function GetDynamicTextBox(value) {
            var htmlElement = '<br/>' + '<input id = "userGroupKey_' + counter + '"  name = "userGroupKey_' + counter + '"  placeholder="Bamboo Group" style="width:250px">'
                    + '</input> &nbsp&nbsp;'
                    + '<input id = "userGroupValue_' + counter + '" name = "userGroupValue_' + counter + '"  type="text" class="text" placeholder="Groups from IdP" style="width: 322px; vertical-align: bottom;margin-left: 5px;" value = "' + value + '" />' + '&nbsp&nbsp;'
                    + '<input type="button" value="-" class="aui-button aui-button-primary" style="vertical-align: bottom;margin-left: 2px;"  onclick = "RemoveTextBox(this,' + counter + ')" />'
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
                counter++;

                document.getElementById("idTotalNumberOfRoles").value = parseInt(document.getElementById("idTotalNumberOfRoles").value) + 1;
            }
        }

    </script>
    <style>
        <#include "/css/groupmappingconfig.css">
    </style>

</head>
<body>
<#include "*/saml/headers/idpconfigheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="role-mapping" role="tabpanel">
    <h1>User Group
        <span class="buttons_in_heading">
            <#if enableButtons == true>
                <input type="button" class="aui-button aui-button-primary" value="Save"
                   onclick="document.forms['role-mapping-form'].submit();"/>
            <#else>
                <input type="button" class="aui-button aui-button-primary" value="Save"
                     onclick="document.forms['role-mapping-form'].submit();" disabled/>
            </#if>
        </span>
    </h1>
    <hr class="header"/>

    <form id="role-mapping-form" class="aui long-label" action="" method="POST">
        <input type="hidden" name="roleMappingSubmitted" id="role_mapping_submitted" value="true"/>
        <input type="hidden" name="totalNumberOfRoles" id="idTotalNumberOfRoles"/>
        <input type="hidden" name="atl_token" value="${xsrfToken}" />
        <input type="hidden" id="idpID" name="idpID" value="${idpID}">

        <#--<p>If users are managed in the External User Directory like LDAP/AD, Click
        <a href="https://miniorange.atlassian.net/l/c/BK43jwLq">here</a> to check the the Group Mapping behaviour of the Group Assignment of the plugin.</p>
        <br/>-->

        <h3>Default Group Configurations</h3>
        <br/>
        <div class="field-group">
            <label for="defaultGroups">Default Group:
                <span class="aui-icon icon-required">(required)</span>
            </label>

            <input class="select long-field" name="defaultGroups" id="defaultGroups" style="width: 500px">
            <div class="description">Select Default Group(s) to assign to <strong>New Users / All users</strong>. Select
                <b>None</b> to disable default group assignment.<br/>
                <a href="https://miniorange.atlassian.net/l/c/BK43jwLq" target="_blank">Click Here</a> for recommended default group settings for different user directory permission
            </div>
        </div>

        <div id="defaultGroupSelect">
            <fieldset class="group">
                <legend>
                    <span>Assign Default Groups To:</span>
                </legend>
                <#if enableDefaultGroupsFor == "newUsers">
                    <input type="radio" class="radio" id="newUsers"
                        name="enableDefaultGroupsFor" value="newUsers" checked="checked">
                <#else>
                    <input type="radio" class="radio" id="newUsers"
                        name="enableDefaultGroupsFor" value="newUsers">
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
            <br/>
            <div class="aui-message aui-message-info">
                Group Mapping can be configured in one of the following two ways.
                <a id="group-mapping-recommendation-link" onclick="toggleRecommendations()" style="cursor: pointer">Click Here</a> to know which group mapping configuration you should use.
                <div id="group-mapping-recommendation" style="display:none">
                    <ol>
                        <li>
                            If the names of groups in Bamboo are different than the corresponding groups in IDP, then you should use <strong>Manual group mapping</strong>.
                        </li>
                        <li>
                            If the names of groups in Bamboo and IDP are same, you should use <strong>On-The-Fly group mapping</strong>.
                        </li>
                    </ol>
                </div>
                <p><a href="https://miniorange.atlassian.net/l/c/BK43jwLq" target="_blank">Click Here</a> for recommended group mapping settings for different primary user directory permissions.
                </p>
                <p>Don't want to map groups? Disable <b>Group Mapping</b>, and save
                    Settings</p>
            </div>

            <div class="pill-nav">
                <a id="group-mapping-pill" style="cursor: pointer"
                   <#if onTheFlyGroupCreation == false> class="active" </#if> >Manual Group Mapping</a>
                <a id="on-the-fly-group-mapping-pill" style="cursor: pointer"
                   <#if onTheFlyGroupCreation == true> class="active" </#if> >On-The Fly Group Mapping</a>
            </div>

            <input type="hidden" name="onTheFlyGroupCreation" id="onTheFlyGroupCreation"
                    <#if onTheFlyGroupCreation == true> value="true"
                    <#else> value="false"  </#if> />
            <div class="aui-message aui-message-info">
                <p id="group-mapping-main-inst" class="title">Group Mapping allows you to map your IDP's groups to your
                    Bamboo groups. You can follow <a id="group-mapping-instructions" onclick="showMappingInstruction()"
                                                         style="cursor:pointer">these steps</a> for Group Mapping.</p>

                <p id="on-the-fly-group-mapping-main-inst" class="title" style="margin: 0px;">Users will be assigned to Groups in Bamboo
                    whose group name is same as groups from IDP. If the Group doesn't exists in Bamboo then it will
                    be created.
                    <br/>
                    You can follow <a id="onthefly-group-mapping-instructions"
                                      onclick="showOnTheFlyMappingInstruction()" style="cursor:pointer">these steps</a>
                    for On-The-Fly Group Mapping.</p>

                <div id="group-mapping-instructions-div" style="display:none">
                    <ol>
                        <li>Go to <a href="addidp.action?idpid=${idpID}">SSO Endpoints</a> and click on Test Configuration.</li>
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
                        <li>Go to <a href="addidp.action?idpid=${idpID}">SSO Endpoints</a> and click on Test Configuration.</li>
                        <li>Copy the <i>Attribute Name</i> against the group value and enter in <i>Group Attribute</i>
                            textbox
                            below.
                        </li>
                        <li>If the user is part of some group in Bamboo and that group is not present in SAML response
                            returned by IDP, then the user will be removed from that group in Bamboo.
                        </li>
                        <li>If you don't want On-The-Fly group mapping to affect Bamboo groups which are managed
                            locally (for eg. bamboo-agents, Bamboo-admin), than add those groups in Exclude Groups field</li>
                    </ol>
                </div>
            </div>

            <div class="field-group">
                <label for="keepExistingUserRoles">Group Mapping:</label>
                <aui-toggle id="keepExistingUserRoles" name="keepExistingUserRoles" <#if keepExistingUserRoles==true>
                            checked="true" </#if> label="keepExistingUserRoles" value="true"></aui-toggle>
                <span>Update groups of existing users.</span><br><br>
                <div class="description aui-message aui-message-info">
                    <p>If users are manged from the external user directory for e.g. AD/LDAP with the <b>read only</b>
                        permission. It is recommended to Disable <b>"Update groups of existing users"</b> option.<b><a href="https://miniorange.atlassian.net/l/c/BK43jwLq" target="_blank">Click Here</a></b> for more details.
                    </p>
                </div>
            </div>


            <div class="field-group">
                <label for="roleAttribute">Group Attribute:
                </label>
                <input type="text" id="roleAttribute" name="roleAttribute" placeholder="Enter the Group attribute name."
                       value="${roleAttribute}" class="text long-field"/>
                <div class="description">Enter the Attribute Name that contains Groups of the User.</div>
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
                        For example, you can use regular expression <b>(.*):group:(.*)#</b> and replacement <b>$2</b> to extract <b>bamboo-software-users</b> from groupName
                        <b>urn:group:bamboo-software-users#idp</b>
                    </div>
                    <br>
                </div>
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

            <div id="onTheFlyDoNotRemoveGroupsDiv">
                <div class="field-group">
                    <label>Exclude Groups:</label>
                    <input class="aui" id="onTheFlyDoNotRemoveGroups" name="onTheFlyDoNotRemoveGroups"
                           style="width: 500px"> </input>
                    <div class="description">Do not remove user from these groups after SSO.</div>
                </div>
            </div>

            <div>

            </div>

            <div id="groupMappingDiv">
                <div class="field-group">
                    <label for="createUsersIfRoleMapped">Restrict User Creation based on Group Mapping:</label>
                    <#if createUsersIfRoleMapped == true>
                        <input class="checkbox" type="checkbox" name="createUsersIfRoleMapped" checked="true"
                            value="true" id="createUsersIfRoleMapped"/>
                    <#else>
                        <input class="checkbox" type="checkbox" name="createUsersIfRoleMapped" value="true"
                            id="createUsersIfRoleMapped"/>
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
                        <#if roleMapping.isEmpty()>
                            <#foreach group in existingGroups>
                                <#if loopCount == 50>
                                    <#break>
                                </#if>
                                <div>
                                    <br/>
                                    <input name="userGroupKey_${loopCount}" id="userGroupKey_${loopCount}"
                                           placeholder="Bamboo Group" class="groupmapping" style="width:250px"
                                           value="${group}">
                                    &nbsp&nbsp;
                                    <input type="text" id="userGroupValue_${loopCount}" style="vertical-align:bottom"
                                           name="userGroupValue_${loopCount}" value="" placeholder="Groups from IdP"
                                           class="text"/>&nbsp&nbsp;
                                    <input type="button" value="-" style="vertical-align:bottom"
                                           class="aui-button aui-button-primary"
                                           onclick="RemoveTextBox(this, ${loopCount})"/>
                                    <#assign loopCount = loopCount+1>
                                </div>
                            </#foreach>
                        <#else>
                            <#foreach key in roleMapping.keySet()>
                                <div>
                                    <br/>
                                    <input name="userGroupKey_${loopCount}" id="userGroupKey_${loopCount}"
                                           placeholder="Bamboo Group" class="groupmapping" style="width:250px"
                                           value="${key}">
                                    &nbsp&nbsp;
                                    <input type="text" id="userGroupValue_${loopCount}" style="vertical-align:bottom"
                                           name="userGroupValue_${loopCount}" value="${roleMapping.get(key)}"
                                           placeholder="Groups from IdP" class="text"/>&nbsp&nbsp;
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
                <#if enableButtons == true>
                    <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>
                <#else>
                    <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;" disabled />
                </#if>
            </div>
    </form>
</div>
</div>
<#include "*/footer.ftl" parse=true>
</body>
</html>