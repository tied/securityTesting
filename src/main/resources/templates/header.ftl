<style>
    .feedback_float {
        cursor: pointer;
        transform: rotate(-90deg);
        padding: 15px;
        position: fixed;
        width: auto;
        height: auto;
        top: 340px;
        right: -25px;
        color: #FFF;
        background: chocolate;
        text-align: center;
        border-radius: 8px;
        border-width: 0px;
        z-index: 10000;
    }

    .feedback_panel {
        background-color: #fff;
        display: inline-block;
        height: auto;
        position: fixed;
        top: 125px;
        width: 320px;
        float: right;
        height: auto;
        min-height: 459px;
        right: -335px;
        z-index: 10000;
        border-radius: 8px;
        border-style: groove;
    }

    .form-element {
        background-color: #f4f5f7;
        border-radius: 5px;
        border: 1px solid #dfe1e6;
        box-shadow: none;
        box-sizing: border-box;
        color: #172b4d;
        font-family: inherit;
        font-size: 14px;
        line-height: 20px;
        max-width: 100%;
        outline: none;
        padding: 9px 7px;
        transition: background-color .2s ease-in-out, border-color .2s ease-in-out;
        width: 100%;
    }

    #widget-header {
        border-radius: 5px 5px 0 0;
        height: 40px;
        max-height: 40px;
        min-height: 40px;
        display: flex;
        align-items: center;
        background-color: #0052cc;
        position: relative;
    }

    #widget-header .header-text, .header-close-icon-container {
        color: #fff;
        padding: 10px;
        display: inline-block;
        white-space: nowrap;
        overflow: hidden;
        flex-grow: 1;
        text-overflow: ellipsis;
        text-align: center;
    }

    .background.diamond {
        font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Noto Sans, Oxygen, Ubuntu, Droid Sans, Helvetica Neue, sans-serif;
    }

    .form-labels {
        color: #6b778c;
        display: block;
        font-size: 12px;
        font-weight: 600;
        line-height: 1;
        padding: 0;
        margin-bottom: 6px;
        font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Noto Sans, Oxygen, Ubuntu, Droid Sans, Helvetica Neue, sans-serif;
    }
</style>
<div class="aui-page-panel">
    <div class="aui-page-panel-inner">
        <section id="content" role="main">
            <header class="aui-page-header" style="background-color:#f5f5f5 !important;">
                <div class="aui-page-header-inner">
                    <div class="aui-page-header-image">
                    	<span class="aui-avatar aui-avatar-large">
                        	<span class="aui-avatar-inner">
                            	<img src="/rest/plugins/1.0/com.miniorange.oauth.bamboo-oauth-key/media/plugin-logo">
                        	</span>
                    	</span>
                    </div>
                    <div class="aui-page-header-main">
                        <h1>miniOrange OAuth Configuration</h1>
                        <div class="aui-buttons">
                            <a href="/plugins/servlet/upm" target="_blank" class="aui-button aui-button-subtle" style="color:blue"><span class="aui-icon aui-icon-small aui-iconfont-addon" style="color: blue;vertical-align: middle"></span><span style="vertical-align:middle">Manage apps</span></a>
                            <a href="https://forum.miniorange.com/forums/forum/single-sign-on/single-sign-on/" target="_blank" class="aui-button aui-button-subtle" style="color:blue"><span class="aui-icon aui-icon-small aui-iconfont-vid-raised-hand" style="color: blue;vertical-align: middle"></span><span style="vertical-align:middle">Ask Us On Forum</span></a>
                        	<a href="https://faq.miniorange.com/kb/atlassian/" target="_blank" class="aui-button aui-button-subtle" style="color: blue"><span class="aui-icon aui-icon-small aui-iconfont-question-circle" style="color:blue;vertical-align: middle"></span><span style="vertical-align:middle">&nbsp;Frequently Asked Questions</span></a>
                        </div>
                    </div>
                </div>
            </header>
            <script data-jsd-embedded data-key="5c807c59-f240-4b09-8425-b0284714e5e1" data-base-url="https://embedded-jsd.atlassian.io" src="https://embedded-jsd.atlassian.io/assets/embed.js"></script>
            <nav class="aui-navgroup aui-navgroup-horizontal" id="min_topmenu">
                <div class="aui-navgroup-inner">
                    <div class="aui-navgroup-primary">
                        <ul class="aui-nav">
                            <li id="configureoauth"><a href="configure.action">Configure OAuth</a></li>
                            <li id="userprofile"><a href="attributemappingconfig.action">User Profile</a></li>
                            <li id="usergroups"><a href="groupmappingconfig.action">User Groups</a></li>
                            <li id="signinsettings"><a href="signinsettings.action">Sign In Settings</a></li>
                            <li id="importexport"><a href="importexport.action">Backup/Restore Configurations</a></li>
                        </ul>
                    </div>
                    <span style="float:right;margin-right:25px;margin-top: 4px;">
                            <a href="contactus.action"><input type="button" class="aui-button aui-button-primary" value="Troubleshooting"></a>
                     </span>
                </div>
            </nav>
            
            <div class="aui-tabs horizontal-tabs" id="tabs-example1" role="application">
				<#if ( !settings.isLicenseDefine() )>
				<div class="aui-message aui-message-warning warning closeable shadowed">
                    <p class="title">
                        <strong>Warning!</strong>
                    </p>
                    <p>No license found for app. Single Sign-On will not work unless app license is applied.</p>
                    <p>If you've already generated a license
                        <a href="https://my.atlassian.com/product" target="_blank">click here</a> to access it and update it in License key box on
                        <a href="/plugins/servlet/upm" target="_blank">
                            <b>Manage apps</b>
                        </a> page.
                    </p>
                </div>
                </#if>

				<#if ( action.hasActionMessages() )>
				<div class="aui-message aui-message-success success closeable shadowed">
					<#foreach actionMessage in action.actionMessages >
                        <p>${actionMessage}</p>
                    </#foreach>
                </div>
                </#if>
				<#if ( action.hasActionErrors() )>
				<div class="aui-message closeable error config_form_error">
					<#foreach actionError in action.actionErrors >
                        <p>${actionError} </p>
                    </#foreach>
                </div>
                </#if>

                