<style>
	.feedback_float {
		cursor: pointer;
		transform: rotate(-90deg);
		padding: 15px;
		position: fixed;
		width: auto;
		height: auto;
		top: 320px;
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
		top: 100px;
		width: 320px;
		float: right;
		height: auto;
		min-height: 450px;
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
	#widget-header .header-text ,.header-close-icon-container{
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
		font-family: -apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Noto Sans,Oxygen,Ubuntu,Droid Sans,Helvetica Neue,sans-serif;
	}
	.form-labels{
		color: #6b778c;
		display: block;
		font-size: 12px;
		font-weight: 600;
		line-height: 1;
		padding: 0;
		margin-bottom: 6px;
		font-family: -apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Noto Sans,Oxygen,Ubuntu,Droid Sans,Helvetica Neue,sans-serif;
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
                            	<img src="${req.contextPath}/rest/plugins/1.0/com.miniorange.sso.saml.bamboo-sso-key/media/plugin-logo">
                        	</span>
                    	</span>
        			</div>
        			<div class="aui-page-header-main">
            			<!--<ol class="aui-nav aui-nav-breadcrumbs">
                			<li><a href="/plugins/servlet/upm">Add-ons</a></li>
            			</ol>-->
            			<h1>miniOrange SAML Single Sign-On Configuration</h1>
            			  <div class="aui-buttons">
                        							<a href="${req.contextPath}/plugins/servlet/upm" target="_blank" class="aui-button aui-button-subtle" style="color:blue"><span class="aui-icon aui-icon-small aui-iconfont-addon" style="color: blue;vertical-align: middle"></span><span style="vertical-align:middle">Manage apps</span></a>
                                    			<!--<ol class="aui-nav aui-nav-breadcrumbs">
                                        			<li><a href="$req.contextPath/plugins/servlet/upm">Add-ons</a></li>
                                    			</ol>-->
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
             				<li id="CONFIGURE"><a href="configure.action">Service Provider Info</a></li>
               				<li id="LISTIDP"><a href="listidp.action" >Configure IDP</a></li>
                 			<li id="ATTRIBUTEMAPPINGCONFIG"><a href="attributemappingconfig.action" >User Profile</a></li>
                 			<li id="GROUPMAPPINGCONFIG"><a href="groupmappingconfig.action">User Groups</a></li>
                       		<li id="SIGNINSETTING"><a href="signinsettings.action" >SSO Settings</a></li>
                       		<li id="CONFIGURECERTIFICATE"><a href="customcerts.action" >Certificates</a></li>
                       		<li id="DOWNLOADSETTING"><a href="downloadsettings.action">Backup/Restore Configurations</a></li>
           				</ul>
                    </div>
                     <span style="float:right;margin-right:25px;margin-top: 4px;">
                            <a href="contactus.action"><input type="button" class="aui-button aui-button-primary" value="Troubleshooting"></a>
                     </span>
                </div>
 			</nav>
			<button class="feedback_float" id="feedback_button" name="feedback_button">
				<b>Feedback</b>
			</button>
			<div class="feedback_panel" id="feedpack_tab" name="feedback_tab">
				<div id="widget-header" class="background diamond">
					<div class="back-button"></div>
					<div class="header-text">Submit Feedback</div>
				</div>
				<div style="padding: 8px">
					<div name="feedback_success" id="feedback_success" class="aui-message aui-message-success"
						 style="display: none; top: 150px;">Feedback Submitted
					</div>
					<div name="feedback_pending" id="feedback_pending" class="aui-message aui-message-info"
						 style="display: none; top: 150px;">We are submitting your feedback...
					</div>
					<div name="feedback_error" id="feedback_error" class="aui-message aui-message-error"
						 style="display: none; top: 35%;">Error please try again
					</div>
					<form id="feedback_form" name="feedback_form" class="aui" method="post" action=""
						  style="margin: 1px 0 0; top: -15px">
						<br>
						<div class="feedback_reason">
							<label for="reason" class="form-labels">Choose Reason<font color="red">*</font></label>
							<select name="reason" id="reason" class="form-element" style="max-width: 500px">
								<option value="Select Reason">Select Reason</option>
								<option value="Bugs in app">Bugs in app</option>
								<option value="Confusing interface">Confusing interface</option>
								<option value="Does not meet my needs">Does not meet my needs</option>
								<option value="Not useful">Not useful</option>
								<option value="Too expensive">Too expensive</option>
								<option value="Re-installing app">Re-installing app</option>
								<option value="Others(Please specify below)">Others(Please specify below)</option>
							</select>
						</div>
						<br>
						<div>
							<label for="feedback_content" class="form-labels">Feedback<font color="red">*</font></label>
							<textarea id="feedback_content" name="feedback_content" class="form-element"
									  style="font-family:Courier New; resize: none;" rows="4"></textarea>
						</div>
						<br>
						<div>
							<label for="feedback_email" class="form-labels">Your email:</label>
							<input type="text" id="feedback_email" class="form-element" name="feedback_email"
								   style="font-family:Courier New; resize: none;border-color: #a79898"/>
						</div>
						<div id="email_error" hidden>
							<font color="#8b0000">Please enter an email ID.</font>
						</div>
						<br>
						<div>
							<label for="can_contact" class="form-labels"><input type="checkbox" value="true" id="can_contact" name="can_contact" checked="true"/>Allow us to contact you</label>
						</div>
						<br>
						<div>
							<label for="send_configurations" class="form-labels"> <input type="checkbox" value="true" id="send_configurations" name="send_configurations"
																						 checked="true"/>Send plugin configurations</label>
						</div>
						<br>
						<input type="button" value="Send" id="send-feedback" name="send-feedback"
							   class="aui-button aui-button-primary"/>
						<input type="button" value="Cancel" id="cancel-feedback" name="cancel-feedback"
							   class="aui-button"/>
					</form>
				</div>
			</div>
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

				<#if ( !settings.getUseDomainMapping() && settings.getIdPList().size() > 1 && settings.getDefaultLoginDisabled() )>
                <div class="aui-message aui-message-warning warning closeable shadowed">
                    <p class="title">
                          <strong>Warning!</strong>
                    </p>
                    <p>It seems that you haven't configured
                        <a href="listidp.action">Domain Mapping</a>. It is required if multiple IDP is setup and you want to auto redirect to IDP. Auto-redirect won't
                        work without domain mapping
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

