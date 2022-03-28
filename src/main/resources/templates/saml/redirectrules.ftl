<html>
<head>
    <title>Redirection Rules</title>
    <meta name="decorator" content="atl.general">
    <script>
            <#include "/js/redirectrules.js">
    </script>
    <style>
            <#include "/css/addidp.css">
            <#include "/css/redirectrules.css">
    </style>
</head>
<body>
    <#include "*/saml/headers/samlheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="redirection-rules" role="tabpanel">
    <h1>Redirection Rules</h1>
    <hr class="header"/>
    ${webResourceManager.requireResource('com.atlassian.auiplugin:aui-form-validation')}

    <p>Redirection Rules allow you to define rules on how the users should be redirected to IDP.</p>
    <p>You can set rules based on user's email domain, directory or group.</p>

    <div id="expander-with-replace-text-content" class="aui-expander-content" aria-expanded="true">

        <p><b>Here's how you can do that:</b></p>
        <ol>
            <li>Click on
                <b> Add Rule</b> button
            </li>
            <li>In the dropdown after <b>If</b>, select the deciding parameter of the rule, i.e. the parameter against
                which the user will be checked. for example, you can select directory, group or domain
            </li>
            <li>In the field after <b>equals</b>, enter the value for that parameter. For example, you can enter <i>bamboo-users</i>
                value for <i>group</i> parameter
            </li>
            <li>In the field after <b>Redirect To</b>, select the IDP where you want to redirect the user if the
                conditions passes. You can also select Bamboo's login page here.
            </li>
            <li>Rules will be applied top-to-bottom. Use the arrows given beside the rules to change the order in which
                they'll be applied
            </li>
        </ol>
    </div>
   <p>
    <a id="replace-text-trigger" data-replace-text="Show More ..." class="aui-expander-trigger" aria-controls="expander-with-replace-text-content">Show less</a>
   </p>

    <div id="bamboo-rules">
        <div>
            <h2 style="float: left;">Bamboo Rules&emsp;</h2>
            <button id="add-bamboo-rule" class="aui-button aui-button-primary add-rule">
                <span class="aui-icon aui-icon-small aui-iconfont-add-circle"></span> Add Rule
            </button>&emsp;
            <aui-spinner id="bamboo-order-spinner" class="order-spinner" size="small"></aui-spinner>
            <span id="bamboo-saved-message" class="saved-message"><span
                    class="aui-icon aui-icon-small aui-iconfont-successful-build"></span>&nbsp;All Changes Saved</span>
        </div>
        <hr class="header subsection"/>



        <form id="save-bamboo-order-form" name="save-bamboo-order-form" class="aui" action="" method="POST">

            <input type="hidden" name="action" value="saveOrder"/>
            <table class="aui aui-table-list">
                <thead>
                <tr>
                    <th colspan="2" width="70%">Rules</th>
                    <th width="20%">Order</th>
                    <th width="10%">
                        Actions
                    </th>
                </tr>
                </thead>
                <tbody>

                    <#assign counter = 0>
                    <#assign sizeOfMap = bambooRedirectionRulesMap.size()>
                    <#foreach key in bambooRedirectionRulesMap.keySet()>
                    <tr id="row-${key}">
                        <td colspan="2">
                            <input type="hidden" id="bamboo-rule-${key}" name="ruleKey" value="${key}"/>
                            <b>${key}: </b>
                             <#-- Adding WithHtml to the variable makes sure that the HTML string is not escaped. Weird flex but Ok-->
                            <#assign expressionWithHtml = bambooRedirectionRulesMap.get(key)>
                            ${expressionWithHtml}

                        </td>
                        <td>
                            <#if sizeOfMap gt 1>

                                <a class="bamboo-move-up" style="cursor:pointer"><span
                                        class="aui-icon aui-icon-small aui-iconfont-chevron-double-up">

                                        move up</span></a>
                                &nbsp;
                                <a class="bamboo-move-down" style="cursor:pointer"><span
                                        class="aui-icon aui-icon-small aui-iconfont-chevron-double-down">

                                        move down</span></a>

                            </#if>
                        </td>
                        <td>
                            <a class="edit-rule" data-rule-id="${key}" class="aui-button aui-button-subtle"><span
                                    class="aui-icon aui-icon-small aui-iconfont-edit-filled">edit</span></a>
                            <a class="delete-bamboo-rule" data-rule-id="${key}"
                               class="aui-button aui-button-subtle"><span
                                    class="aui-icon aui-icon-small aui-iconfont-trash"></span></a>
                        </td>
                    </tr>

                        <#assign counter = counter +1>
                    </#foreach>
                </tbody>
            </table>
            <table class="aui">
                <tbody>
                <tr id="row-bamboo-default">
                    <td>
                        <span><b>Default Rule: </b>Redirect the users to</span>&emsp;
                        <aui-select id="defaultBambooIDP" name="defaultBambooIDP"
                                    placeholder="Select Default IDP"
                                    class="default-idp-select">
                            <#foreach idpId in ssoEnabledIdpList>
                                <aui-option id="${idpId}" value="${idpId}"
                                    <#if defaultBambooIDP.equals(idpId)> selected </#if>
                                >${idpMap.get(idpId)}</aui-option>
                            </#foreach>
                            <aui-option id="loginPage" value="loginPage"
                                <#if defaultBambooIDP.equals("loginPage")> selected </#if>
                            >Login Page</aui-option>
                            <aui-option value="redirectUrl"
                                <#if defaultBambooIDP.equals("redirectUrl")> selected </#if>
                            >Redirect to a URL</aui-option>
                        </aui-select>
                    </td>
                </tr>
                <tr id="defaultBambooIDP-redirect-url" style="display: none;">
                    <td>
                        <span><b>URL to redirect Users to :</b></span>
                        <input type="text" value="${defaultRedirectUrl}" id="defaultRedirectUrl" name="defaultRedirectUrl" class="text long-field default-redirect-url">
                        <div id="save-redirect-url-rule" style="display: none; vertical-align: middle;">
                            <a id="save-redirect-url-button"
                               class="aui-button aui-button-subtle aui-icon aui-icon-small aui-iconfont-success"
                               title="save"></a>
                            <a
                                id="cancel-redirect-url-button"
                                class="aui-button aui-button-subtle aui-icon aui-icon-small aui-iconfont-close-dialog"
                                title="cancel"></a>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </form>
    </div>


</div>
    <#include "*/saml/createRuleDialogue.ftl" parse=true>
    <#include "*/footer.ftl" parse=true>
</body>
</html>