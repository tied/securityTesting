<hr class="header">
<ul class="aui-nav">
    <li id="mo-documentation">
        <a href="https://docs.miniorange.com/documentation/saml" target="_blank">
            Documentation <span class="aui-icon aui-icon-small aui-iconfont-shortcut"></span>
        </a>
    </li>
    <li>&nbsp;</li>
    <li id="mo-troubleshooting">
        <#if idpID?? && idpID != "">
            <a href="troubleshooting.action?idpID=${idpID}">
                Troubleshooting and Support
            </a>
        <#else>
            <a href="troubleshooting.action">
                Troubleshooting and Support
            </a>
        </#if>
    </li>
    <li>&nbsp;</li>
    <li id="mo-migration-info">
        <a href="https://miniorange.atlassian.net/wiki/spaces/JSDoc/pages/1333231661/SAML+-+Info+On+settings+which+got+migrated" target="_blank">
            UI Migration Guide <span class="aui-icon aui-icon-small aui-iconfont-shortcut"></span>
        </a>
    </li>
    <li>&nbsp;</li>
</ul>