<div class="aui-tabs horizontal-tabs" id="tabs-example1" role="application">

    <#if settings.getMaxUsers()==0>
        <div class="aui-message aui-message-warning warning closeable shadowed">
            <p class="title">
                <strong>Warning!</strong>
            </p>
            <p>No license found for app. Header Authentication will not work unless app license is applied.</p>
            <p>If you've already generated a license
                <a href="https://my.atlassian.com/product" target="_blank">click here</a> to access it and update it in
                License key box on
                <a href="${req.contextPath}/plugins/servlet/upm#manage/com.miniorange.sso.saml.bamboo-sso" target="_blank">
                    <b>Manage apps</b>
                </a> page.
            </p>
        </div>
        <br>
    </#if>
    <#if settings.getIsCertificateExpired()>
        <div class="aui-message aui-message-error">
            <p class="title">
                <strong>Warning!</strong>
            </p>
            <p>Your Certificate has expired. Some IDPs might start denying your SSO request. Please update your
                certificate.</p>
        </div>
        <br>
    </#if>

