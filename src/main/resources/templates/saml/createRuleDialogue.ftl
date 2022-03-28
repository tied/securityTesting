<div id="create-rule" class="tabs-pane active-pane" role="tabpanel" style="margin-top: 15px;display: none" >
    <h1 id="rule-window-title">Add Rule</h1>
    <hr class="header">
    <br>
    <form id="add-redirection-rule-form" class="aui" action="" method="POST" style="top: -20px">
        <input type="hidden" id="formaction" name="addRedirectionRuleSubmitted" value="true">
        <input type="hidden" id="ruleID" name="ruleID">
        <input type="hidden" name="atl_token" value="${xsrfToken}" />
        <table style="width:80%">
            <tbody>
            <tr class="field-group">
                <td class="rule-label" width="20%">
                    <label for="ruleName">Rule Name<span class="aui-icon icon-required">(required)</span>:</label></td>
                <td colspan="80%">
                    <input type="text" id="ruleName" name="ruleName" class="text long-field" required data-aui-validation-field pattern="[A-Za-z0-9]+" data-aui-validation-pattern-msg="Only alphanumerical characters are allowed">
                </td>
            </tr>
            <tr><td>&nbsp;</td></tr>
            <tr class="field-group">
                <td class="rule-label" width="20%">
                    <label for="domain">IF</label></td>
                <td width="30%"><aui-select id="decisionFactor" name="decisionFactor" required="true">
                    <aui-option value="domain" selected>Email Domain</aui-option>
                    <aui-option value="directory">Directory</aui-option>
                    <aui-option value="group">Group</aui-option>
                </aui-select></td>
                <td width="30%">
                    <aui-select id="conditionOperation" name="conditionOperation" required="true">
                        <aui-option value="equals" selected>equals</aui-option>
                        <aui-option value="regex" >contains</aui-option>
                    </aui-select>
                </td>

                <td width="30%">

                    <#-- Default -->
                    <input type="text" class="text condition-value" id="textConditionValue" placeholder="Email domain like abc.com" name="conditionValue" required="true"/>

                   <#-- If Directory is Selected-->
                   <input class="condition-value" name="conditionValue" id="directoryList" style="display:none" disabled/>

                    <input type="text" class="text condition-value" placeholder="Enter directory regex" id="directoryRegex" name="conditionValue" style="display: none;"/>

                    <#-- If Groups are selected-->
                    <input class="condition-value" name="conditionValue" id="groupsList" style="display:none" disabled/>

                    <input type="text" class="text condition-value" placeholder="Enter group regex" id="groupRegex" name="conditionValue" style="display: none" required="true"/>

                </td>
            </tr>
            <tr><td>&nbsp;</td></tr>
            <tr class="field-group">
                <td class="rule-label"><label for="idp">Then Redirect To</label></td>
                <td colspan="3"><aui-select id="idp" name="idp" placeholder="Select IDP">
                    <#foreach idpId in ssoEnabledIdpList>
                        <aui-option id="${idpId}" value="${idpId}"
                        >${idpMap.get(idpId)}</aui-option>
                    </#foreach>
                    <aui-option id="loginPage" value="loginPage">Login Page</aui-option>
                </aui-select></td>
            </tr>
            </tbody>
        </table>

        <br><div class="buttons-container">
        <input class="aui-button aui-button-primary" type="submit" value="Save" id="comment-save-button" resolved="">&emsp;
        <a id="cancel-create-rule">Cancel</a>
    </div>

    </form>

    <div style="display: none;" id="regex-info" class="aui-message aui-message-info">The "contains" option can be used in situations where the rule needs to be applied to users multiple domains,groups or Directories that share a common substring in their names
        <br>
            For example if the rule needs to be applied to email IDs like john@de.mail.com, jim@fr.mail.com and jack@es.mail.com then the "contains" option can be set to <b>.mail.com</b>
    </div>
</div>