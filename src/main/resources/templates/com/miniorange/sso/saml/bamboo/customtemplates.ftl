<html>
<head>
    <meta name="decorator" content="atl.general">
    <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
    <script>
        $(document).ready(function() {
            $(".aui-nav li").removeClass("aui-nav-selected");
            $("#aui-uid-7").addClass("aui-nav-selected");
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
    <div class="tabs-pane active-pane" id="customtemplates"  role="tabpanel" >
        <p style="font-size:13pt;">Configure Custom Templates</p>

        <hr class="header"/>

        <form id="configure-customtemplates" class="aui long-label" action="" method="POST">
            <input type="hidden" name="atl_token" value="${xsrfToken}" />
            <input type="hidden" name="customtemplatesSubmitted" value="true"/>

            <fieldset class="group">
                <legend> <span>SSO Error Message Template:</span></legend>

                <#if enableErrorMsgTemplate == true>
                    <input class="checkbox" type="checkbox" name="enableErrorMsgTemplate" checked="true" value="true" id="enableErrorMsgTemplate"/>
                <#else>
                    <input class="checkbox" type="checkbox" name="enableErrorMsgTemplate" value="true" id="enableErrorMsgTemplate"/>
                </#if>

                <label for="enableErrorMsgTemplate">On error, redirect users to this custom error page</label>
                <br>
                <textarea id="errorMsgTemplate"  name="errorMsgTemplate" class="textarea long-field" style="font-family:Courier New;" cols="64" rows="5">${errorMsgTemplate?html}</textarea>
                <div class="description">
                    Define the Custom SSO error message templates. This template will be shown to the user when SSO fails. <br>Use <b>$baseUrl</b> for login page URL.
                </div>
            </fieldset>

            <br/>

            <fieldset class="group">
                <legend> <span>Custom Logout Template:</span></legend>

                <textarea id="logoutTemplate"  name="logoutTemplate" class="textarea long-field" style="font-family:Courier New;" cols="64" rows="5">${logoutTemplate?html}</textarea>
                <div class="description">
                    Define the Custom logout page. This template will be shown to the user when user logs out with Single Logout URL defined. <br>Enable Custom Logout template from <a href="signinsettings.action">SSO Settings</a> tab. Use <b>$baseUrl</b> for login page.
                </div>
            </fieldset>

            <br/>
            <div class="field-group">
                <input type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>
                <p><b>Note:</b> To reset the template, empty the textarea and click on the save</p>
            </div>
        </form>
    </div>
    </div>
    </section>
    </div>
    </div>
</body>
</html>