<html>
<head>
    <meta name="decorator" content="atl.general">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <script>
        <#include "/js/listidp.js">
        AJS.$(function () {
            if("${showIntroPage?c}" == "true"){
                if(${settings.getIdPList().size()}>0){
                    AJS.dialog2("#intro_for_existing_users_dialog").show();
                }else{
                    AJS.dialog2("#intro_for_new_users_dialog").show();
                }
            }
        });
    </script>
    <style>
        <#include "/css/listidp.css">
    </style>
</head>
<body>
<#include "*/saml/headers/samlheader.ftl" parse=true>

<div class="tabs-pane active-pane" id="configure-idp" role="tabpanel">

    <#include "*/saml/introductionForNewUsersPopup.ftl" parse=true>
    <#include "*/saml/introductionForExistingUsersPopup.ftl" parse=true>
    <#include "*/saml/quickOrCustomSetupPopup.ftl" parse=true>

    <div <#if settings.getIdPList().size() gt 0> style="display:none;" </#if> >
        <div style="text-align:center;">
            <br><br><br><br><br><br>
            <i class="fa fa-plus-circle fa-5x" aria-hidden="true"></i>
            <h1> You haven't configured any IDP yet</h1>
            <h2> Click on the button below to start configuring an IDP</h2>
            <br>
            <span>
                <button id="add-idp-main" class="aui-button aui-button-primary"
                        style="width:auto; height:auto; font-size:25px">
                   Add New IDP
                </button>
            </span>
        </div>
    </div>
    <div id="idp-list" <#if settings.getIdPList().size() gt 0>
                            style="display:block;"
                        <#else> style="display:none;"
                        </#if> >
        <div>
            <h1 style="float:left">Configured IDPs</h1>
            <p style="margin-left:70%;">
                <button id="add-idp-button" class="aui-button aui-button-primary">Add New IDP</button>
                <input type="button" value="Take a Plugin Tour" class="aui-button aui-button-primary" id="start_tour_for_existing_users_button">
            </p>
        </div>

        <form id="delete_idp_form" name="metadata_import_form" method="POST" action="deleteidp" class="aui long-label">
            <input type="hidden" name="atl_token" value="${xsrfToken}" />
            <input type="hidden" id="idpID" name="idpID"/>
        </form>

        <form id="list-idp-form" name="list-idp-form" method="post" action="" class="aui long-label">
            <input type="hidden" name="listIdpSubmitted" value="true">
            <table class="aui aui-table-interactive" style="width: 95%">
                <tr>
                    <th width="25%" id="idpName" <#if !settings.checkIfAllQuickSetupsComplete()> colspan="2" </#if>
                        style="text-align:center;"><b>IDP Name</b>
                    </th>
                    <th width="40%" id="actions"
                        <#if !settings.checkIfAllQuickSetupsComplete()> colspan="2" </#if>
                        style="text-align:center;">
                        <a id="actions_tour" aria-controls="actions_dialog"></a>
                        <b>Actions</b>
                    </th>
                    <th width="35%" id="enableSSO" style="text-align:center;"></th>
                </tr>

                <#foreach idp in idpList>
                    <tr>
                        <td width="15%"
                            <#if settings.checkIfAllQuickSetupsComplete()>
                                style="text-align:center; vertical-align: middle;"
                            <#else>
                                style="text-align:right; vertical-align: middle; padding-left:40px"
                            </#if> >
                            ${idpMap.get(idp)}
                        </td>
                        <#if settings.checkIfQuickSetupComplete(idp) && !settings.checkIfAllQuickSetupsComplete()>
                            <td></td>
                        </#if>
                        <#if !settings.checkIfQuickSetupComplete(idp)>
                            <td width="10%" style="vertical-align: middle">
                                <span class="aui-lozenge aui-lozenge-subtle aui-lozenge-inprogress" id="in-progress-${idp}">in progress</span>
                            </td>
                        </#if>

                        <td width="25%"
                            <#if settings.checkIfAllQuickSetupsComplete()>
                                style="text-align:center; vertical-align: middle; padding-right: 1px"
                            <#else>
                                style="text-align:right; vertical-align: middle; padding-left:20px; padding-right: 1px"
                            </#if> >
                            <a style="font-size: 16px;" id="edit-idp-${idp}"
                               href="overview.action?idpid=${idp}">View</a>&nbsp;&nbsp;
                            <a style="font-size: 16px; cursor:pointer;" id="test-saml-config" name="test-saml-config"
                               onclick="testConfigurations('${idp}')">Test</a>&nbsp;
                            <select class="idp-options-dropdown" id="editOption" name="editOption"
                                    style="width:110px; text-align-last:center; vertical-align: middle;" onchange="handleSelect(this)">
                                <option disabled selected class="optInvisible">Edit</option>
                                <option value="addidp.action?idpid=${idp}">SSO Endpoints</option>
                                <option value="attributemappingconfig.action?idpid=${idp}">User Profile</option>
                                <option value="groupmappingconfig.action?idpid=${idp}">User Groups</option>
                                <option value="advancedsso.action?idpid=${idp}">Advanced Settings</option>
                            </select>&nbsp;
                            <a class="fa fa-trash fa-2x" style="vertical-align: middle; cursor:pointer;"
                               onclick="showDeleteDialog('${idp}')"></a>&nbsp;
                        </td>
                        <#if settings.checkIfQuickSetupComplete(idp) && !settings.checkIfAllQuickSetupsComplete()>
                        <td></td>
                        </#if>
                        <#if !settings.checkIfQuickSetupComplete(idp)>
                            <td width="25%">
                                <div class="continue-setup-div" id="continue-setup-div-${idp}"
                                     onmouseover="this.style.background='rgba(9,30,66,.13)'"
                                     onmouseleave="this.style.background='#FFFFFF'">
                                    <input type="button" class="continue-close-button"
                                           id="close-quick-setup-${idp}" value="x"
                                           onclick="hideContinueSetup('${idp}')"
                                           onmouseover="this.style.background='#E1E1E1'"
                                           onmouseleave="this.style.background='#FFFFFF'">
                                    <a style="font-size: 16px;" class="aui-button aui-button-subtle" id="quick-setup-${idp}"
                                       href="flowdrivensetup.action?idp=${idpMap.get(idp)}&idpid=${idp}">Continue
                                        Quick Setup</a>
                                </div>
                            </td>
                        </#if>
                        <td width="15%" style="text-align:center; vertical-align: middle;">
                            <span style="vertical-align:middle;">
                                <label class="switch" style="vertical-align:middle;"> </label>
                                <aui-toggle id="enableSsoForIdp_${idp}" name="enableSsoForIdp_${idp}" value="true"
                                    name="enableSsoForIdp_${idp}" onchange="submitEnableSSOChange('${idp}')"
                                    <#if enableSsoForIdpMap.get(idp) == true> checked="true" </#if> ></aui-toggle>
                                <span style="height:16px; width: 16px">
                                    <strong>SSO</strong>
                                </span>
                            </span>
                        </td>
                    </tr>
                </#foreach>
            </table>
            <br/>
        </form>
    </div>
</div>

<#include "*/footer.ftl" parse=true>
</body>
</html>
