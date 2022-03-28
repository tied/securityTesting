<div class="aui-page-panel">
    <div class="aui-page-panel-inner">
        <div id="mo-saml-configure" class="aui-page-panel-nav mo-vert-menu">
            <nav class="aui-navgroup aui-navgroup-vertical">
                <div class="aui-navgroup-inner">
                    <div class="aui-navgroup-primary">
                        <div id="saml-navigation-links">
                            <button class="aui-button aui-button-subtle" onclick="window.location.href='listidp.action';" style="width: 100%;text-align: center;border-color: #d0d0d0;">
                                <span style="text-align:center;" class="aui-icon aui-icon-small aui-iconfont-arrow-left-circle"></span>&nbsp;Back to common setting</button>
                            <ul class="aui-nav">
                                <li>&nbsp;</li>
                                <li id="mo-idps-overview">
                                    <a href="overview.action?idpid=${idpID}" id="idps_overview_tour" aria-controls="idps_overview_dialog">
                                        Overview
                                    </a>
                                </li>
                                <li>&nbsp;</li>
                                <li id="mo-idps-config">
                                    <a href="addidp.action?idpid=${idpID}" id="idps_config_tour" aria-controls="idps_config_dialog">
                                        SSO Endpoints
                                    </a>
                                </li>
                                <li>&nbsp;</li>
                                <li id="mo-idps-profile">
                                    <a href="attributemappingconfig.action?idpid=${idpID}" id="idps_profile_tour" aria-controls="idps_profile_dialog">
                                        User Profile
                                    </a>
                                </li>
                                <li>&nbsp;</li>
                                <li id="mo-idps-group">
                                    <a href="groupmappingconfig.action?idpid=${idpID}" id="idps_group_tour" aria-controls="idps_group_dialog">
                                        User Groups
                                    </a>
                                </li>
                                <li>&nbsp;</li>
                                <li id="mo-idps-advanced">
                                    <a href="advancedsso.action?idpid=${idpID}">
                                        Advanced SSO options
                                    </a>
                                </li>
                                <li>&nbsp;</li>
                            </ul>
                            <#include "*/globalmenu.ftl" parse=true>
                        </div>
                    </div>

                </div>
            </nav>
        </div>

        <!--<section class="aui-page-panel-content">-->