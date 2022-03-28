
<div class="aui-page-panel" style="border:none !important;">
    <div class="aui-page-panel-inner">
        <div id="mo-saml-configure" class="aui-page-panel-nav mo-vert-menu" style=""> <!--width:250px-->
            <nav class="aui-navgroup aui-navgroup-vertical" style="margin-left:-30px;">
                <div class="aui-navgroup-inner">
                    <div class="aui-navgroup-primary">
                        <div id="saml-navigation-links">
                            <ul class="aui-nav">
                                <li id="mo-sp-metadata">
                                    <a href="spmetadata.action" id="sp_metadata_tour" aria-controls="sp_metadata_dialog" >
                                        SP Information
                                    </a>
                                </li>
                                <li>&nbsp;</li>
                                <li id="mo-idps">
                                    <a href="listidp.action" id="idps_tour" aria-controls="idps_dialog">
                                        Configured IDPs
                                    </a>
                                    <#include "*/saml/menu/idpMenu.ftl" parse=true>
                                </li>
                                <li>&nbsp;</li>
                                <li>
                                    <span class="aui-nav-heading"> SSO Settings </span>
                                    <ul class="aui-nav">
                                        <li id="mo-sign-in-settings">
                                            <a href="signinsettings.action" id="signin_settings_tour" aria-controls="signin_settings_dialog">
                                                Sign-In Settings
                                            </a>
                                        </li>
                                        <li>&nbsp;</li>
                                        <li id="mo-redirection-rules">
                                            <a href="redirectionrules.action" id="redirection_rules_tour" aria-controls="redirection_rules_dialog">
                                                Redirection Rules
                                            </a>
                                        </li>
                                        <li>&nbsp;</li>
                                        <li id="mo-look-and-feel">
                                            <a href="looknfeel.action" id="look_and_feel_tour" aria-controls="look_and_feel_dialog">
                                                Look and Feel
                                            </a>
                                        </li>
                                        <li>&nbsp;</li>
                                        <li id="mo-post-logout-rules">
                                            <a href="postlogout.action">
                                                Post Logout Configurations
                                            </a>
                                        </li>
                                        <li>&nbsp;</li>
                                        <li id="mo-session-management">
                                            <a href="sessionmanagement.action">
                                                Remember My Login
                                            </a>
                                        </li>
                                        <li>&nbsp;</li>
                                        <li id="mo-global-sso-settings">
                                            <a href="globalssosettings.action" id="global_sso_settings_tour" aria-controls="global_sso_settings_dialog">
                                                Global SSO Settings
                                            </a>
                                        </li>
                                    </ul>
                                </li>
                                <li>&nbsp;</li>
                                <li id="mo-custom-certs">
                                    <a href="customcerts.action">
                                        Certificates
                                    </a>
                                </li>
                                <li>&nbsp;</li>
                                <li id="mo-backup-and-restore">
                                    <a href="backuprestore.action" id="backup_and_restore_tour" aria-controls="backup_and_restore_dialog">
                                        Backup and Restore
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
        <!--<section class="aui-page-panel-content" style="width:100%;">-->
