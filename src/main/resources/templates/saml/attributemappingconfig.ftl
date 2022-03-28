<html>
<head>
    <meta name="decorator" content="atl.general"/>
    <style>
        .attributemapping, .long-field{
            width: 700px !important;
        }
    </style>
    <script>
        AJS.$(document).ready(function () {
            <#include "/js/attributemappingconfig.js">
            var idp = '${idpName}';
            var idpCount = '${idpMap.size()}';
            if (idpCount > 1) {
                AJS.flag({
                    title: 'User Profile Configuration',
                    type: 'info',
                    close: 'auto',
                    body: '<p>Now viewing ' + idp + '\'s settings</p>'
                });
                AJS.$("#selectIDPAmGm").show();
            }



            AJS.$(".attributemapping").auiSelect2({
                placeholder: 'Add attribute value',
                width: '400px',
                ajax: {
                    url: AJS.contextPath() + '/plugins/servlet/saml/moapi',
                    data: function (params) {
                        var query = {
                            idpname: AJS.$("#idpID").val(),
                            search: params,
                            action: 'getAttributes'
                        }
                        return query;
                    },
                    results: function (data, page) {
                        return {
                            results: data.results
                        };
                    },
                },
            });
            AJS.$("#usernameAttribute").auiSelect2('data', {
                id: AJS.$("#usernameAttribute").val(),
                text: AJS.$("#usernameAttribute").val()
            });
            AJS.$("#emailAttribute").auiSelect2('data', {
                id: AJS.$("#emailAttribute").val(),
                text: AJS.$("#emailAttribute").val()
            });
            AJS.$("#fullNameAttribute").auiSelect2('data', {
                id: AJS.$("#fullNameAttribute").val(),
                text: AJS.$("#fullNameAttribute").val()
            });
            AJS.$("#firstNameAttribute").auiSelect2('data', {
                id: AJS.$("#firstNameAttribute").val(),
                text: AJS.$("#firstNameAttribute").val()
            });
            AJS.$("#lastNameAttribute").auiSelect2('data', {
                id: AJS.$("#lastNameAttribute").val(),
                text: AJS.$("#lastNameAttribute").val()
            });

        });

        function toggleRecommendations() {
            AJS.$("#group-mapping-recommendation").toggle(400);
        }

    </script>
</head>
</head>
<body>
<#include "*/saml/headers/idpconfigheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="attribute-mapping" role="tabpanel">
    <h1>User Profile</h1>
    <hr class="header"/>

    <p class="description ">Enter SAML Attributes which are configured in your IDP. Not sure what to enter? Click on <b>Test
        Configuration</b> button on <a href="addidp.action?idpid=${idpID}">SSO Endpoints</a>. From the table, copy <b>Attribute
        Name</b> and paste it against the attributes below.</p>
    <form id="attribute-mapping-form" class="aui long-label" action="" method="POST">
        <input type="hidden" name="attributeMappingSubmitted" value="true"/>
        <input type="hidden" name="totalNumberOfRoles" id="idTotalNumberOfRoles"/>
        <input type="hidden" name="idpID" id="idpID" value="${idpID}">
        <input type="hidden" name="atl_token" value="${xsrfToken}" />
        <div class="field-group">
            <label for="keepExistingUserAttributes">User Profile Mapping:</label>
            <aui-toggle id="keepExistingUserAttributes" name="keepExistingUserAttributes" <#if keepExistingUserAttributes==true>
                        checked="true" </#if> label="keepExistingUserAttributes" value="true"></aui-toggle>

            <span>Update attributes of existing users</span><br><br>
            <div class="description aui-message aui-message-info">
                <p>If users are manged from the external user directory for e.g. AD/LDAP with the <b>read only</b>
                    permission. It is recommended to disable <b>"User Profile Mapping"</b> option. <b><a
                            href="https://miniorange.atlassian.net/l/c/BK43jwLq" target="_blank">Click Here</a></b> for
                    more details.</p>
            </div>
        </div>

        <div class="field-group">
            <label for="loginUserAttribute">Login Bamboo user account by:</label>
            <select class="select" name="loginUserAttribute" id="loginUserAttribute" style="width:200px;">
                <option value="username" <#if loginUserAttribute.equals("username")> selected </#if> >username</option>
                <option value="email" <#if loginUserAttribute.equals("email")> selected </#if> >email</option>
            </select>
            <div id="warningforemail" class="aui-message aui-message-warning closeable" style="display:none;">
                Email is not recommended. Select this option only when email of all the user is unique otherwise SSO
                will not work.
            </div>
        </div>

        <div class="field-group">
            <label for="usernameAttribute">Username:
                <span class="aui-icon icon-required">(required)</span>
            </label>
            <#if testConfigPerformed == true>
                <input required="true" id="usernameAttribute" name="usernameAttribute" value="${usernameAttribute}"
                    class="select long-field attributemapping" placeholder="Enter username attribute" style="width: 600px !important; max-width: none"/>
            <#else>
                <input required="true" id="usernameAttribute" name="usernameAttribute" value="${usernameAttribute}"
                    class="text long-field" placeholder="Enter username attribute" style="width:600px !important; max-width: none"/>
            </#if>
            <div class="description">Enter the SAML-response attribute that contains Bamboo Username. Use
                <em>NameID</em>
                if Username is in Subject element.
            </div>

            <br/>
            <div>
                <#if regexPatternEnabled == true>
                    <input class="checkbox" type="checkbox" name="regexPatternEnabled" checked="true" value="true"
                           id="regexPatternEnabled"/>
                <#else>
                    <input class="checkbox" type="checkbox" name="regexPatternEnabled" value="true"
                           id="regexPatternEnabled"/>
                </#if>
                <span> Apply regular expression on username field</span>
            </div>
            <br/>
            <div id="regexfield">
                <div>
                    <input type="text" id="regexPattern" name="regexPattern" value="${regexPattern}"
                           placeholder="Regular Expression" class="text long-field" style="width: 322px"/>&nbsp;
                    <input type="button" id="test-regex" value="Test Regex" class="aui-button" style="width:170px;"/>
                </div>
                <div class="description">Enter the regular expression here. It will be applied on value for Attribute
                    Name provided in Username field.<br>
                    For example, you can use regular expression <b>^.*?(?=@)</b> to extract <b>demo</b> from username
                    <b>demo@example.com</b>
                </div>
            </div>
        </div>
        <div class="field-group">
            <label for="emailAttribute">Email:
                <span class="aui-icon icon-required">(required)</span>
            </label>
            <#if testConfigPerformed == true>
                <input required="true" id="emailAttribute" name="emailAttribute"
                       value="${emailAttribute}" class="select long-field attributemapping"
                       placeholder="Enter email attribute" style="width: 600px !important; max-width: none"/>
            <#else>
                <input required="true" id="emailAttribute" name="emailAttribute"
                       value="${emailAttribute}" class="text long-field"
                       placeholder="Enter email attribute" style="width: 600px !important; max-width: none"/>
            </#if>
            <div class="description">Enter the SAML-response attribute that contains Email. Use
                <em>NameID</em>
                if Email is in Subject element.
            </div>
        </div>

        <div class="field-group">
            <label for="useSeparateNameAttributes">Separate Name Attributes:</label>
            <#if useSeparateNameAttributes == true>
                <input class="checkbox" type="checkbox" checked="true" value="true"
                       name="useSeparateNameAttributes" id="useSeparateNameAttributes"/>
            <#else>
                <input class="checkbox" type="checkbox" value="true"
                       name="useSeparateNameAttributes" id="useSeparateNameAttributes"/>
            </#if>
            <span>Map First name and Last name as separate attributes</span>
        </div>
        <div id="fullNameAttributeDiv">
            <div class="field-group">
                <label for="fullNameAttribute">Full Name Attribute:</label>
                <#if testConfigPerformed == true>
                <input id="fullNameAttribute" name="fullNameAttribute"
                       value="${fullNameAttribute}" class="select long-field attributemapping" style="width: 400px; max-width: none"/>
                <#else>
                <input id="fullNameAttribute" name="fullNameAttribute"
                       value="${fullNameAttribute}" class="text long-field" style="width: 600px !important; max-width: none"/>
                </#if>
                <div class="description">Enter the SAML-response attribute that contains Full Name.</div>
            </div>
        </div>
        <div id="separateNameAttributes">
            <div class="field-group">
                <label for="firstNameAttribute">First Name:</label>
                <#if testConfigPerformed == true>
                    <input id="firstNameAttribute" name="firstNameAttribute"
                           value="${firstNameAttribute}" class="select long-field attributemapping" style="width: 600px !important; max-width: none"/>
                <#else>
                    <input id="firstNameAttribute" name="firstNameAttribute"
                           value="${firstNameAttribute}" class="text long-field" style="width: 600px !important; max-width: none"/>
                </#if>
                <div class="description">Enter the SAML-response attribute that contains First Name.</div>
            </div>
            <div class="field-group">
                <label for="lastNameAttribute">Last Name:</label>
                <#if testConfigPerformed == true>
                    <input id="lastNameAttribute" name="lastNameAttribute"
                           value="${lastNameAttribute}" class="select long-field attributemapping" style="width: 600px !important; max-width: none"/>
                <#else>
                    <input id="lastNameAttribute" name="lastNameAttribute"
                           value="${lastNameAttribute}" class="text long-field" style="width: 600px !important; max-width: none"/>
                </#if>
                <div class="description">Enter the SAML-response attribute that contains Last Name.</div>
            </div>
        </div>


        <br/>


        <br/>
        <div class="field-group">
            <#if enableButtons == true>
                <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>
            <#else>
                <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;" disabled/>
            </#if>
        </div>
    </form>
</div>

<#include "*/footer.ftl" parse=true>
</body>
</html>