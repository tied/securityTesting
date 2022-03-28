<html>

<head>

    <title>OAuth Configuration</title>
    <meta name="decorator" content="atl.general">
    <script>
        AJS.$(document).ready(function() {
            AJS.$(".aui-nav li").removeClass("aui-nav-selected");
            AJS.$("#userprofile").addClass("aui-nav-selected");

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

            if (AJS.$("#useSeparateNameAttributes").is(":checked")) {
                AJS.$("#fullNameAttributeDiv").hide();
                AJS.$("#separateNameAttributeDiv").show();

            } else {
                AJS.$("#fullNameAttributeDiv").show();
                AJS.$("#separateNameAttributeDiv").hide();
            }

            if (AJS.$("#loginUserAttribute").val() != "email") {
                AJS.$("#warningforemail").hide();
            } else {
                AJS.$("#warningforemail").show();
            }
        });
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
<div class="tabs-pane active-pane" id="attribute-mapping" role="tabpanel" >
    <p style="font-size:16pt;">Configure User Attributes</p>
    <hr class="header"/>

    <p>User Attribute allows you to map your OAuth Provider profile attributes to your Bamboo attributes. Follow these steps to get started:
    <ol>
        <li>Go to <a href="configure.action">Configure OAuth</a> and click on Test Configuration.</li>
        <li>Copy the <i>Attribute Name</i> and paste it against the <i>Attributes</i>
            below.</li>
    </ol>
    </p>
    <p>Don't want to map user profile attributes? Disable Attribute Mapping, save and skip to <a href="groupmappingconfig.action">User Groups</a></p><br/>

    <form id="attribute-mapping-form" class="aui long-label" action="" method="POST">
        <input type="hidden" name="attributeMappingSubmitted" value="true"/>
        <input type="hidden" name="atl_token" value="${xsrfToken}" />

        <div class="field-group">
            <label for="keepExistingUserAttributes" class="labelClass">Disable Attribute Mapping:</label>
            <#if keepExistingUserAttributes == true>
                <input class="checkbox" type="checkbox" name="keepExistingUserAttributes" checked="true" value="true" id="keepExistingUserAttributes"/>
            <#else>
                <input class="checkbox" type="checkbox" name="keepExistingUserAttributes" value="true" id="keepExistingUserAttributes"/>
            </#if>
            <span>Do not update attributes of existing users.</span>
            <div class="description aui-message aui-message-info" style="width:92%">
                <p>If users are manged from the external user directory for e.g. AD/LDAP with the <b>read only</b> permission. It is recommended to enable <b>"Disable User Profile Mapping"</b> option. <a href="userdirectoryinfo.action" target="_blank">Click here</a> for more details.
            </div>
        </div>


        <div class="field-group">
            <label for="loginUserAttribute" class="labelClass">Login/Search Bamboo user account by:</label>
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
            <label for="usernameAttribute" class="labelClass">Username:</label>
            <input type="text"  id="usernameAttribute" name="usernameAttribute" placeholder="Enter the Username attribute name."
                   value="${usernameAttribute}" class="text long-field"/>
            <div class="description">Enter the OAuth-response attribute that contains Bamboo Username.
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
            <label for="emailAttribute" class="labelClass">Email:</label>
            <input type="text"  id="emailAttribute" name="emailAttribute" placeholder="Enter the Email attribute name."
                   value="${emailAttribute}" class="text long-field"/>
            <div class="description">Enter the OAuth-response attribute that contains Email. You can specify a semicolon-separated (;) list of email addresses.
            </div>
        </div>

        <div id="fullNameAttributeDiv" class="field-group">
            <label for="fullNameAttribute" class="labelClass">Full Name Attribute:</label>

            <input type="text" id="fullNameAttribute" name="fullNameAttribute" placeholder="Enter the Full Name attribute name."
                   value="${fullNameAttribute}" class="text long-field"/>

            <div class="description">Enter the OAuth-response attribute that contains Full Name.</div>
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

        <div id="separateNameAttributeDiv">
            <div class="field-group">
                <label for="firstNameAttribute" class="labelClass">First Name:</label>
                <input type="text" id="firstNameAttribute" name="firstNameAttribute" placeholder="Enter the First Name attribute name."
                       value="${firstNameAttribute}" class="text long-field"/>

                <div class="description">Enter the OAuth-response attribute that contains First Name.</div>
            </div>

            <div class="field-group">
                <label for="lastNameAttribute" class="labelClass">Last Name:</label>
                <input type="text" id="lastNameAttribute" name="lastNameAttribute" placeholder="Enter the Last Name attribute name."
                       value="${lastNameAttribute}" class="text long-field"/>

                <div class="description">Enter the OAuth-response attribute that contains Last Name. </div>
            </div>
        </div>
        <br/>
        <div class="field-group">
            <input id="amSubmit" type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>&nbsp;&nbsp;&nbsp;
            <a href="groupmappingconfig.action"><input type="button" value="Next" class="aui-button aui-button-primary" style="width:100px;"/></a>&nbsp;&nbsp;&nbsp;
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