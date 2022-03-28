<#assign map = settings.getIdpMap()>
<#assign idps = settings.getIdPList()>
<#assign i = 0>

<#if idps.size() gt 0>
    <ul class="aui-nav">
        <#foreach idp in idps>
            <li class="idp-heading <#if i gt 0 >collabsible </#if>">
                <a id="idp-heading-${idp}" class="aui-nav-heading" style="cursor: pointer">${map.get(idp)}</a>
                <ul class="aui-nav" <#if i gt 0>style="display:none" </#if> >
                    <li>
                        <a href="overview.action?idpid=${idp}" style="color: blue;font-size: smaller">Overview</a>
                    </li>
                    <li>
                        <a href="addidp.action?idpid=${idp}" style="color: blue;font-size: smaller">SSO Endpoints</a>
                    </li>
                    <li>
                        <a href="attributemappingconfig.action?idpid=${idp}" style="color: blue;font-size: smaller">User Profile</a>
                    </li>
                    <li>
                        <a href="groupmappingconfig.action?idpid=${idp}" style="color: blue;font-size: smaller">User Groups</a>
                    </li>
                    <li>
                        <a href="advancedsso.action?idpid=${idp}" style="color: blue;font-size: smaller">Advanced Settings</a>
                    </li>

                </ul>
            </li>
        <#assign i = i + 1>
        </#foreach>
    </ul>
</#if>

