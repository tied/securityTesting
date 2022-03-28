<!-- Contact Us tab of the plugin, lets you send a support query from the plugin -->

<html>
<head>
    <meta name="decorator" content="atl.general"/>


    <style>
        .aui-page-panel-content{
            padding: 0px !important;
        }

        .aui-page-panel{
            margin: 0px !important;
        }
    </style>
</head>

<body>
    <#include "*/header.ftl" parse=true>
<div class="tabs-pane active-pane" id="contact-us"  role="tabpanel" style ="min-height: 75px;" >
    <p style="font-size:16pt;">Troubleshooting</p>
    <hr class="header"/><br>
	
	<ul>
		<li><b>Log File</b> : You can follow these steps to download logs,
			<br>
			<div>
				<ol>
					<li>Go to <b>System -> Log Settings</b><a href="/admin/configLog4j.action" target="_blank"><span class="aui-icon aui-icon-small aui-iconfont-shortcut"></span></a>.</li>
					<li>Enter <i>com.miniorange.oauth</i> in class path field and select Debug in type. Click on add.</li>
				</ol>
				<br>
				After these steps, perform single sign-on again to record logs. Then download <i>support zip</i> using these steps,
				<ol>
					<li>Go to <b>System -> Troubleshooting and support tools</b><a href="/plugins/servlet/troubleshooting/view/" target="_blank"><span class="aui-icon aui-icon-small aui-iconfont-shortcut"></span></a>.</li>
					<li>navigate to the create support zip tab.</li>
					<li>Keep only <b>Logs files</b> option selected.</li>
					<li>Click on <b>Create zip</b> and then <b>Download zip</b>.</li>
				</ol>
			</div>
		</li><br>
		
	</ul>
	
    <p>Send us a message, query or feedback using the Support/Feedback widget (requires Internet) and we will get back to you. Have a lot to say? You can also reach us at <a href="mailto:info@xecurify.com">info@xecurify.com</a>.</p>
</div>
</section>
</div>
</div>

</body>
</html>