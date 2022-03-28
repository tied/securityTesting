<html>
<head>
    <meta name="decorator" content="atl.general">
    <style>
        <#include "/css/idpconfig.css">
        <#include "/css/spmetadata.css">
    </style>
    <script>
        <#include "/js/overview.js">
        <#include "/js/spmetadata.js">
    </script></head>
<body>
<#include "*/saml/headers/idpconfigheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="overview" role="tabpanel">
    <h1>Overview
        <span class="buttons_in_heading">
            <button id="show_sp_metadata_popup" class="aui-button aui-button-primary" target="_blank" resolved="">SP Information</button>
        </span>
    </h1>
    <hr class="header"/>
    <#include "*/saml/spmetadatapopup.ftl" parse=true>
    <h3>1. IDP Configurations</h3><br>
    <div class="container">
        <form id="config-idp-form" name="config-idp-form" method="post" action="" class="aui long-label">
            <span style="float:right;">
                <a class="aui-button aui-button-primary" href="addidp.action?idpid=${idpID}"  resolved="">
                    Edit
                </a>
            </span>
            <br>
            <br>
            <table>
                <tbody>
                <tr>
                    <td>
                        <label> IDP Name </label>
                    </td>
                    <td>
                        ${idpName}
                    </td>
                </tr>
                <tr>
                    <td>
                        <label> IDP Entity ID / Issuer </label>
                    </td>
                    <td >
                        ${idpEntityId}
                    </td>
                </tr>
                <tr>
                    <td>
                        <label> Send Signed Request </label>
                    </td>
                    <td >
                            <span style="align: center">
                                ${signedRequest}
                    </td>
                </tr>
                <tr>
                    <td>
                        <label> SSO Binding Type  </label>
                    </td>
                    <td >
                        ${ssoBindingType} Binding for Single Sign On
                    </td>
                </tr>
                <tr>
                    <td >
                        <label> Single Sign On URL </label>
                    </td>
                    <td >
                        ${ssoUrl}
                    </td>
                </tr>
                <tr>
                    <td>
                        <label> Single-Logout Binding Type </label>
                    </td>
                    <td >
                        ${sloBindingType} Binding for Single-Logout
                    </td>
                </tr>
                <tr>
                    <td >
                        <label>  Single Logout URL </label>
                    </td>
                    <td >
                        <#if sloUrl=="">
                            -
                        <#else>
                            ${sloUrl}
                        </#if>
                    </td>
                </tr>
                <tr>
                    <td>
                        <label> IDP Signing Certificate: </label>
                    </td>
                    <td>
                        <#if certificates?? >
                            <#assign loopCount = 0>
                            <#foreach x509Certificate in certificates >
                                <div>
                                    <textarea disabled id="x509Certificate" name="x509AllCertificates" class="textarea long-field"
                                        style="font-family:Courier New; font-size: 12px;" cols="64" rows="4">${x509Certificate}</textarea>
                                </div>
                                <#assign loopCount = loopCount+1>
                                <#if numberOfCertificates != loopCount > 
                                <hr>
                                </#if>
                            </#foreach>
					    <#else>
						<div>
						    	<textarea disabled id="x509Certificate"  name="x509Certificate" class="textarea long-field" style="font-family:Courier New;font-size: 12px;"
                                                                  cols="64" rows="4">${x509Certificate}</textarea>
						 </div>
					    </#if>

                    </td>
                </tr>
                </tbody>
            </table>
        </form>
    </div>
    <br>

    <h3>2. User Profile</h3>
    <br>
    <div class="container">
        <form id="config-idp-form" name="attributeMapping-idp-form" method="post" action="" class="aui long-label">
                 <span style="float:right;">
                        <a class="aui-button aui-button-primary" href="attributemappingconfig.action?idpid=${idpID}" resolved="">
                        Edit
                    </a>
                 </span>
            <br>
            <br>
            <table>
                <tbody>

                <tr>
                    <td>
                        <label> Do not update attributes of existing users? </label>
                    </td>
                    <td>
                        ${keepExistingUserAttributes}
                    </td>
                </tr>
                <tr>
                    <td>
                        <label> Login Bamboo user account by  </label>
                    </td>
                    <td>
                        ${loginUserAttribute}
                    </td>
                </tr>
                <tr>
                    <td>
                        <label> IDP attributes which represent the value of Username  </label>
                    </td>
                    <td>
                        ${username}
                    </td>
                </tr>

                <tr>
                    <td>
                        <label> IDP attributes which represent the value of Email  </label>
                    </td>
                    <td>
                        ${email}
                    </td>
                </tr>

                <tr>
                    <td >
                        <label> Do you have separate IDP attributes for First Name and Last Name ?  </label>
                    </td>
                    <td>
                        ${useSeparateNameAttribute}
                    </td>
                </tr>
                <#if useSeparateNameAttribute == "no">
                    <tr>
                        <td>
                            <label> IDP attributes which represent the value of Full Name  </label>
                        </td>
                        <td>
                            <span style="align: center">
                                ${fullName}
                        </td>
                    </tr>
                <#else>
                    <tr>
                        <td>
                            <label> IDP attributes which represent the value of First Name  </label>
                        </td>
                        <td>
                            <span style="align: center">
                                ${firstNameAttribute}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label> IDP attributes which represent the value of  Last Name </label>
                        </td>
                        <td>
                            <span style="align: center">
                                ${lastNameAttribute}
                        </td>
                    </tr>
                </#if>
                <tr>
                    <td>
                        <label> Is Regex Pattern Enabled?   </label>
                    </td>
                    <td>
                        ${regexPatternEnabled}
                    </td>
                </tr>
                <tr>
                    <td>
                        <label> Configured Regex Pattern </label>
                    </td>
                    <td>
                        <#if regexPattern=="">
                            -
                        <#else>
                            ${regexPattern}
                        </#if>
                    </td>
                </tr>

                </tbody>
            </table>
        </form>
    </div>
    <br>
    <h3>3. User Groups</h3>
    <br>
    <div class="container">
        <form id="config-idp-form" name="groupmapping-idp-form" method="post" action="" class="aui long-label">
                <span style="float:right;">
                    <a class="aui-button aui-button-primary" href="groupmappingconfig.action?idpid=${idpID}" resolved="">
                        Edit
                    </a>
                </span>
            <br>
            <br>
            <table>
                <tbody>
                <tr>
                    <td>
                        <label> Configured  Default Groups  </label>
                    </td>
                    <td>
                        <#assign  groupSize = defaultGroups.size()>
                        <#foreach keySet in defaultGroups>
                            <#if groupSize < 2>
                                ${keySet}
                            <#else>
                                ${keySet}
                                <hr/>
                            </#if>
                            <#assign groupSize = groupSize - 1>
                        </#foreach>
                    </td>
                </tr>
                <tr>
                    <td>
                        <label>  Assign Default Group To  </label>
                    </td>
                    <td>
                        ${enableDefaultGroupsFor}
                    </td>
                </tr>
                <tr>
                    <td>
                        <label> Update groups of existing users?  </label>
                    </td>
                    <td>
                        ${keepExistingUserRoles}
                    </td>
                </tr>

                <tr>
                    <td>
                        <label> IDP attributes which represent the value of groups &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  </label>
                    </td>
                    <td>
                        <#if roleAttribute=="">
                            -
                        <#else>
                            ${roleAttribute}
                        </#if>
                    </td>
                </tr>
                <tr>
                    <td>
                        <label> Restrict User Creation based on Group Mapping? </label>
                    </td>
                    <td>
                        ${createUsersIfRoleMapped}
                    </td>
                </tr>
                <tr>
                    <td>
                        <label> Is On-The-Fly Group Mapping Enabled?  </label>
                    </td>
                    <td>
                        ${onTheFlyGroupMapping}
                    </td>
                </tr>
                <#if onTheFlyGroupMapping == "yes">
                    <tr>
                        <td>
                            <label>Create New Group if the group does not exist</label>
                        </td>
                        <td>
                            ${createNewGroups}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label> Keep Existing Users Groups Enabled? </label>
                        </td>
                        <td>
                            ${onTheFlyAssignNewGroupsOnly}
                        </td>
                    </tr>
                    <#if onTheFlyAssignNewGroupsOnly == "no">
                        <tr>
                            <td>
                                <label> Do not remove user from these groups after SSO </label>
                            </td>
                            <td>
                                <span style="align: center">
                                    <#assign groupSize = onTheFlyDoNotRemoveGroupsList.size()>
                                    <#foreach keySet in onTheFlyDoNotRemoveGroupsList>
                                        <#if groupSize < 2 >
                                            ${keySet}
                                        <#else>
                                            ${keySet}
                                            <hr/>
                                        </#if>
                                        <#assign groupSize = groupSize - 1>
                                    </#foreach>
                            </td>
                        </tr>
                    </#if>
                <#else>
                    <tr>
                        <td>
                            <label> Configure Group Mapping</label>
                        </td>
                        <td>
                            <span style="align: center">
                                <#assign groupSize = roleMapping.size()>
                                <#foreach keySet in roleMapping.entrySet()>
                                    <#if groupSize < 2>
                                        ${keySet.key} - ${keySet.value}
                                    <#else>
                                        ${keySet.key} - ${keySet.value}
                                        <hr/>
                                    </#if>
                                    <#assign groupSize = groupSize - 1>
                                </#foreach>
                        </td>
                    </tr>
                </#if>
                </tbody>
            </table>
        </form>
    </div>
</div>
<#include "*/footer.ftl" parse=true>
</body>
</html>