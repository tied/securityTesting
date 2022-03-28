<section role="dialog" id="quick_or_custom_setup_dialog" class="aui-layer aui-dialog2 aui-dialog2-medium" aria-hidden="true"
         style="height: auto">
    <header class="aui-dialog2-header">
        <h2 class="aui-dialog2-header-main" style="max-width: none">How would you like to setup your IDP?</h2>
        <a id="quick_or_custom_setup_dialog_close_button" class="aui-dialog2-header-close">
            <span class="aui-icon aui-icon-small aui-iconfont-close-dialog">Close</span>
        </a>
    </header>
    <div class="aui-dialog2-content" style="padding: 20px 20px; height: auto">
        <a href="supportedidps.action" style="text-decoration: none">
            <div id="quick-setup-container" class="setup-container" style="cursor: pointer">
                <img src="${req.contextPath}/download/resources/com.miniorange.sso.saml.bamboo-sso:samlsso.plugin.icons.resource/images/quick_setup.png"
                     class="setup-icon">
                <div class="description">
                    <p><strong>Quick Setup</strong></p>
                    <p style="font-size: 12px;">
                        Quickly configure all the details required for Basic SSO.
                        With this setup minimal initial configuration is required to perform SSO.You will be
                        able to access all the advanced features after quick setup.</p></div>
            </div>
        </a>
        <br/>
        <a href="addidp.action?operation=add" style="text-decoration: none">
            <div id="manual-setup-container" class="setup-container" style="cursor: pointer">
                <img src="${req.contextPath}/download/resources/com.miniorange.sso.saml.bamboo-sso:samlsso.plugin.icons.resource/images/manual_setup_2.png"
                     class="setup-icon">
                <div class="description">
                    <p><strong>Manual Setup</strong></p>
                    <p style="font-size: 12px;">
                        A more Hands-on approach to configure your IDP. This setup
                        directly gives you access to all the advanced features so that you can customise your
                        IDP settings.</p>
                </div>
            </div>
        </a>
    </div>
</section>
