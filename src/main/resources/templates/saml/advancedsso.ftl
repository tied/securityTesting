<html>
<head>
    <meta name="decorator" content="atl.general">
    <script>
         <#include "/js/advancedsso.js">
    </script>
</head>
<body>
<#include "*/saml/headers/idpconfigheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="advancedsso"  role="tabpanel" >
    <h1>Advanced SSO Options</h1>

    <hr class="header"/>

    <form id="advanced-sso-form" class="aui long-label" action="" method="POST">
        <input type="hidden" id="advanced_SSO_submitted" name="advancedSsoSubmitted" value="true"/>
        <input type="hidden" id="idpID" name="idpID" value="${idpID}">
        <input type="hidden" name="atl_token" value="${xsrfToken}" />

        <div class="field-group">
            <label for="allowUserCreation" class="labelClass">Allow User Creation:</label>
            <aui-toggle id="allowUserCreation" value="true" label="Allow User Creation"
                        name="allowUserCreation" value="true"
                        <#if allowUserCreation==true> checked </#if> ></aui-toggle>
            <span>Enabling this will allow you to create new users through SSO.</span>
            <div class="description aui-message aui-message-info"> User creation will not work for read only directories by default,
                <b><a href="https://miniorange.atlassian.net/l/c/BK43jwLq" target="_blank">Click Here</a></b> for more details.</div>
        </div>

        <br>

        <div class="field-group">
                    <label for="forceAuthentication" class="labelClass"> Force Authentication: </label>
                    <aui-toggle id="forceAuthentication" value="true" label="Force Authentication:"
                                name="forceAuthentication" value="true"
                                <#if forceAuthentication==true> checked </#if> ></aui-toggle>
                    <span>Force authentication forces users to re-authenticate even if the user has an SSO session at the IdP.</span>
                </div>

        <br>

        <div class="field-group">
            <label for="inputUrl" >Metadata Rollover:</label>
            <input type="url" id="inputUrl" name="inputUrl" required value="${inputUrl}" placeholder="Enter metadata URL of your IdP" style="width:70%; padding: 5px;"
                   class="text long-field" />
            <div id="metadata_url_description" class="description"></div>
        </div>
        <div class="field-group">
            <aui-toggle id="refreshMetadata" name="refreshMetadata" value="true" label="Refresh Metadata"
                        <#if refreshMetadata==true> checked </#if> ></aui-toggle>
            <span>Refresh Certificate periodically?</span>
            <div class="description">We will store the metadata URL and refresh IdP Certificate periodically.</div>

            <select class="select" id="refreshInterval" name="refreshInterval" disabled>
                <option value="hourly" <#if refreshInterval.equals("hourly")> selected </#if> >Hourly</option>
                <option value="twicedaily" <#if refreshInterval.equals("twicedaily")> selected </#if> >Twice Daily</option>
                <option value="daily" <#if refreshInterval.equals("daily")> selected </#if> >Daily</option>
                <option value="weekly" <#if refreshInterval.equals("weekly")> selected </#if> >Weekly</option>
                <option value="monthly" <#if refreshInterval.equals("monthly")> selected </#if> >Monthly</option>
                <option value="custom" <#if refreshInterval.equals("custom")> selected </#if> >Custom</option>
            </select>&nbsp;&nbsp;&nbsp;&nbsp;
            <span id="customRefreshValue">
                <input type="number" min="1" max="525600" id="customRefreshInterval" name="customRefreshInterval" value="${customRefreshInterval}"
                       class="text" />&nbsp;&nbsp;
                <select name="customRefreshIntervalUnit" class="select">
                    <option value="minutes" <#if customRefreshIntervalUnit.equals("minutes")> selected </#if> >Minutes</option>
                    <option value="hours" <#if customRefreshIntervalUnit.equals("hours")> selected </#if> >Hours</option>
                    <option value="days" <#if customRefreshIntervalUnit.equals("days")> selected </#if> >Days</option>
                </select>
            </span>
            <div class="description">Set Time Interval to update IDP Certificate.</div>
        </div><br>

        <fieldset class="group">
            <legend>
                <span>Relay State URL:</span>
            </legend>
            <input type="radio" class="radio" name="relayStateRedirectionType" id="forceRedirect" value="forceRedirect" style="margin-top:10px;"
                   <#if relayStateRedirectionType == "forceRedirect"> checked="checked" </#if> >
            <label for="forceRedirect">Force Redirect</label>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <input type="radio" class="radio" name="relayStateRedirectionType" id="redirectOnNoRelayState" value="redirectOnNoRelayState"
                   <#if relayStateRedirectionType == "redirectOnNoRelayState"> checked="checked" </#if> >
            <label for="redirectOnNoRelayState">Redirect On No Relay State</label>
        </fieldset>
        <div class="field-group">
            <input type="url" id="relayState" value="${relayState}" name="relayState" class="text long-field" placeholder="${settings.getSpBaseUrl()}/example-path"/>
            <div class="description">Enter the absolute URL where you want to redirect users after Single Sign On. Keep empty to redirect user to the same URL they started with.<br>
                <b>Force Redirect</b> option will always redirect user to the Relay State URL after SSO.<br>
                <b>Redirect On No Relay State</b> option will redirect user to the page he was trying to visit, otherwise to the Relay State URL.
            </div>
        </div><br>
        <div class="field-group">
            <label for="samlResponseValidity">Validate IDP's SAML Response:
                <br>(recommended)</label>
            <div>
                <input type="number" min="0" name="timeDelay" placeholder="MM" id="timeDelay" value="${timeDelay}"
                       class="text"/>
                <div class="description">
                    Accept SAML Response with invalid timestamps
                    <i>in minutes</i> as long as their values differ within this value.
                </div>
            </div>
        </div><br>

        <div class="field-group">
            <label for="enablePassiveSso" class="labelClass">Enable Passive SSO:</label>
            <aui-toggle id="enablePassiveSso" value="true" label="Enable Passive SSO"
                        name="enablePassiveSso" value="true"
                    <#if enablePassiveSso==true> checked </#if> ></aui-toggle>
            <span> If enabled, the user should have Pre-SSO Session on IDP to login into Bamboo. If not, IDP will not prompt the user for authentication and will instead send the failed SAML Response to Bamboo.</span>
        </div>
        <br>

        <div class="field-group">
            <#if enableButtons>
                <input id="advanceSubmit" type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;"/>
            <#else>
                <input id="advanceSubmit" type="submit" value="Save" class="aui-button aui-button-primary" style="width:100px;" disabled/>
            </#if>
        </div>

    </form>

</div>
<#include "*/footer.ftl" parse=true>
</body>
</html>