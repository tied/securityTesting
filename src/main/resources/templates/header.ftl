<a href="https://miniorange.atlassian.net/servicedesk/customer/portal/2/group/6/create/15" target="_blank"><div class="header" id="Help_Button">miniOrange Support
</div></a>

<script>
    <#if (action.hasActionMessages())>
        <#foreach actionMessage in action.actionMessages>
            AJS.flag({
                title: 'Success!',
                type: 'success',
                close: 'auto',
                body: '<p>${actionMessage}</p><br/>'
            });
        </#foreach>
    </#if>
    <#if action.hasActionErrors() >
        <#foreach error in action.actionErrors >
            AJS.flag({
                title: 'Error!',
                type: 'error',
                close : 'auto',
                body: '<p>${error}</p><br/>'
            });
        </#foreach>
    </#if>

</script>
<style>
    <#include "/css/bamboo-sso.css">
    #include("/css/pluginPages.css");
</style>

<script>
    <#include "/js/newFeatures.js">
    <#include "/js/pluginUtilities.js">
</script>

<header class="aui-page-header" style="background-color:#f5f5f5 !important; margin:-21px -20px 0px -20px; padding:0px 10px 10px 20px;">
    <div class="aui-page-header-inner" style="width:70%;">
        <div class="aui-page-header-image">
            <span class="aui-avatar aui-avatar-large">
                <span class="aui-avatar-inner">
                    <img src="${req.contextPath}/rest/plugins/1.0/com.miniorange.sso.saml.bamboo-sso-key/media/plugin-logo">
                </span>
            </span>
        </div>
        <div class="aui-page-header-main">
            <h1>miniOrange SAML Single Sign-On Configuration</h1>
            <div class="aui-buttons">
                <a href="${req.contextPath}/plugins/servlet/upm" target="_blank" class="aui-button aui-button-subtle" style="color:blue">
                    <span class="aui-icon aui-icon-small aui-iconfont-addon" style="color: blue;vertical-align: middle"></span>
                    <span style="vertical-align:middle">Manage apps</span></a>
                <a href="https://forum.miniorange.com/forums/forum/single-sign-on/single-sign-on/" target="_blank" class="aui-button aui-button-subtle" style="color:blue">
                    <span class="aui-icon aui-icon-small aui-iconfont-vid-raised-hand" style="color: blue;vertical-align: middle"></span>
                    <span style="vertical-align:middle">Ask Us On Forum</span></a>
                <a href="https://faq.miniorange.com/kb/atlassian/" target="_blank" class="aui-button aui-button-subtle" style="color: blue">
                    <span class="aui-icon aui-icon-small aui-iconfont-question-circle" style="color:blue;vertical-align: middle"></span>
                    <span style="vertical-align:middle">&nbsp;Frequently Asked Questions</span></a>
                <a href="https://miniorange.atlassian.net/servicedesk/customer/portal/2/group/6/create/66" target="_blank" class="aui-button aui-button-subtle" style="color: blue">
                    <span class="aui-icon aui-icon-small aui-iconfont-video-filled" style="color:blue;vertical-align: middle"></span>
                    <span style="vertical-align:middle">&nbsp;Request a Demo</span></a>&emsp;


            </div>
        </div>
    </div>
    <div style="float:right; margin-right:20px; margin-top:-60px;">
        <div class="contact-us" id="contact-us" aria-controls="support_div_dialog">
            <div class="contact-us-heading">
                <h3> Contact us:</h3>
                <div>
                    <ul style="list-style-type:none; padding-left: 0;">
                        <li>
                            <span class="aui-icon aui-icon-small aui-iconfont-email">Contact-us email</span>
                            <span><a href="mailto:atlassiansupport@xecurify.com">atlassiansupport@xecurify.com</a></span>
                        </li>
                        <li>
                            <span class="aui-icon aui-icon-small aui-iconfont-vid-hang-up">Contact-us phone</span>
                            <span>+1 978 658 9387</span>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <br>
    <nav id="main-navigation-heading" class="aui-navgroup aui-navgroup-horizontal" style="margin:-10px;">
        <div class="aui-navgroup-inner">
            <div class="aui-navgroup-primary">
                <div id="mo-navigation-links" class="mo-navigation-links">
                    <ul class="aui-nav" resolved="">
                        <li id="mo-saml">
                            <a href="listidp.action"><span>SAML</span></a>
                        </li>
                        <li id="mo-headerbasedauth">
                            <a href="headerbasedauth.action"><span>Header Authentication</span></a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </nav>
</header>
