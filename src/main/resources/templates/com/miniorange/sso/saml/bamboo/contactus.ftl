<html>
    <head>
    <meta name="decorator" content="atl.general"/>
    <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
	<script>
            $(document).ready(function() {
	            $(".aui-nav li").removeClass("aui-nav-selected");
	            $("#aui-uid-8").addClass("aui-nav-selected");
        	});
        	
        	function showSAMLRequest() {
	            var x = document.getElementById('saml-request-steps-div');
	            if (x.style.display === "none") {
	                $('#saml-request-steps-div').show("slow");
	            } else {
	                x.style.display = "none";
	            }
	        }
	        function showSAMLResponse() {
	            var x = document.getElementById('saml-response-steps-div');
	            if (x.style.display === "none") {
	                $('#saml-response-steps-div').show("slow");
	                x.style.display = "block";
	            } else {
	                x.style.display = "none";
	            }
	        }
	        function showLogFile() {
	            var x = document.getElementById('log-file-steps-div');
	            if (x.style.display === "none") {
	                $('#log-file-steps-div').show("slow");
	                x.style.display = "block";
	            } else {
	                x.style.display = "none";
	            }
	        }
        	
        </script>
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
        	<#assign downloadConfigurationEndPoint = settings.getSpBaseUrl() + "/plugins/servlet/saml/downloadconfiguration">
        	<#assign logSettingsUrl = settings.getSpBaseUrl() + "/admin/configLog4j.action">
        	<#assign troubleshootingUrl = settings.getSpBaseUrl() + "/plugins/servlet/troubleshooting/view/">
            	<p style="font-size:13pt;">Troubleshooting</p>
                <hr class="header"/>
                
                <ul>
			        <li><b>Log File</b> : You can follow
			            <a id="log-file-steps" onclick="showLogFile()" style="cursor:pointer"> these steps</a>
			            to download logs,
			            <br>
			            <div id="log-file-steps-div" style="display:none">
			                <ol>
			                    <li>Go to <b>System -> Log Settings</b>.<a href="${logSettingsUrl}" target="_blank"><span class="aui-icon aui-icon-small aui-iconfont-shortcut"></span></a>.</li>
			                    <li>Enter <i>com.miniorange.sso.saml</i> in class path field and select Debug in type. Click on add.</li>
			                </ol>
			                <br>
			                After these steps, perform single sign-on again to record logs. Then download <i>support zip</i> using these steps,
			                <ol>
			                    <li>Go to <b>System -> Troubleshooting and support tools</b>.<a href="${troubleshootingUrl}" target="_blank"><span class="aui-icon aui-icon-small aui-iconfont-shortcut"></span></a>.</li>
			                    <li>navigate to the create support zip tab.</li>
			                    <li>Keep only <b>Logs files</b> option selected.</li>
			                    <li>Click on <b>Create zip</b> and then <b>Download zip</b>.</li>
			                </ol>
			            </div>
			        </li>
			
			        <li>
			            <b>SAML Request</b>: Download SAML Request using
			            <a id="saml-request-steps" onclick="showSAMLRequest()" style="cursor:pointer"> these steps</a>,
			
			            <div id="saml-request-steps-div" style="display:none">
			                <ol>
			                    <li>Go to <a href="listidp.action">Configure IDP</a> tab.</li>
			                    <li>Click on <b>View SAML Request</b> button and click on <b>Download SAML Request</b>.</li>
			                </ol>
			            </div>
			        </li>
			
			        <li>
			            <b>SAML Response</b>: Download SAML Response using
			            <a id="saml-response-steps" onclick="showSAMLResponse()" style="cursor:pointer"> these steps</a>,
			
			            <div id="saml-response-steps-div" style="display:none">
			                <ol>
			                    <li>Go to <a href="listidp.action">Configure IDP</a> tab.</li>
			                    <li>Click on <b>View SAML Response</b> button and click on <b>Download SAML Response</b>.</li>
			                </ol>
			            </div>
			        </li>
			
			        <li><b>Plugin Configuration</b> : <a href="${downloadConfigurationEndPoint}" target="_blank" class="aui-button aui-button-link">Download</a> Plugin Configuration
			            file and send it to us for debugging.</li>
			    </ul>
                <p>Send us a message, query or feedback using the Support/Feedback widget (requires Internet) and we will get back to you. Have a lot to say? You can also reach us at <a href="mailto:info@xecurify.com">info@xecurify.com</a>.</p>
                
            </div>
		</div>
        </section>
        </div>
        </div>
    </body>
</html>