
<div class="test-results-section" id="test-results-section" <#if testConfigPerformed == true> style="display:block" <#else> style="display:none" </#if> >
    <p>
    <h2>
        Test Configuration
    </h2>
    </p>
    <hr class="header"/>
    <div class="test-configurations-section-div" id="test-configurations-section-div">
        <div class="container" style="overflow-x: auto">
            <div class="divs-to-display" id="test-config-details" style="display: block">
                <div style='color: #3c763d;background-color: #dff0d8; padding:2%;margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;'>Test Configuration</div>
                <table style="border-collapse:collapse;border-spacing:0; display:table;width:100%;
                    font-size:14pt;">
                    <tr style="text-align:center;">
                        <td style="font-weight:bold;border:2px solid #949090;padding:2%;">ATTRIBUTE NAME</td>
                        <td style="font-weight:bold;padding:2%;border:2px solid #949090; word-wrap:break-word;">
                            ATTRIBUTE VALUE
                        </td>
                    </tr>
                    <#if attributesMap?? && attributesMap.size() != 0 >
                        <#foreach attribute in attributesMap.keySet()>
                            <tr>
                                <td style="font-weight:bold;border:2px solid #949090;padding:2%;font-size: medium;">
                                    ${attribute}
                                </td>
                                <td style="padding:2%;border:2px solid #949090;word-wrap:break-word;font-size: medium;">
                                    <#foreach value in attributesMap.get(attribute)>
                                        ${value} <br>
                                    </#foreach>
                                </td>
                            </tr>
                        </#foreach>
                    </#if>
                </table>
            </div>
        </div>
    </div>
    <br/>
    <p>
    <h2 >
        SAML Request
    </h2>
    </p>
    <hr class="header"/>
    <div class="saml-request-section-div" id="saml-request-section-div">
        <br>
        <div class="container">
            <div style='color: #3c763d;background-color: #dff0d8; padding:2%;margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;'>Authentication(SAML) Request</div>
            <#if displaySamlRequest?? >
                <textarea rows='6' cols='100' word-wrap='break-word;' style='width:695px; margin:0px; height:350px;' id ='reqmessage' readonly>
                    ${displaySamlRequest}
                </textarea>
            </#if>
            <div
                    style=margin:3%;display:block;text-align:center;>
                <input id = "download-button" style="padding:1%;
                   width:200px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer; font-size:15px;
                   border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;
                   box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;
                   color: #FFF;"  type="button" value="Download SAML Request" onclick="downloadRequest()" />
            </div>

        </div>
    </div>
    <br/>
    <p>
    <h2>
        SAML Response
    </h2>
    </p>
    <hr class="header"/>
    <div class="saml-response-section-div" id="saml-response-section-div">
        <br>
        <div class="container">
            <div style='color: #3c763d;background-color: #dff0d8; padding:2%;margin-bottom:20px;text-align:center; border:1px solid #AEDB9A; font-size:18pt;'>Authentication(SAML) Response</div>
            <#if displaySamlResponse?? >
                <textarea rows='6' cols='100' word-wrap='break-word;' style='width:695px; margin:0px; height:350px;' id ='resmessage' readonly>
                    ${displaySamlResponse}
                </textarea>
            </#if>
            <div style='margin:3%;display:block;text-align:center;'>
                <input id ='download-button' style='padding:1%;
						width:200px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;
						border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;
						box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;
						color: #FFF;' type='button' value='Download SAML Response' onclick='downloadResponse()'>
            </div>
        </div>
    </div>
</div>
<br/>
<p>
<h2 >
    Troubleshooting
</h2>
</p>
<hr class="header"/>
<div class="troubleshooting-section-div" id="troubleshooting-section-div">
    <div class="tabs-pane active-pane" id="support" aria-labelledby="aui-uid-7" role="tabpanel" >
        <p>For troubleshooting use,</p>
        <ul>
            <li><b style="font-weight:600">Log File</b> : You can follow
                <a id="log-file-steps" onclick="showLogFile()" style="cursor:pointer"> these steps</a>
                to download logs,
                <br>
                <div id="log-file-steps-div" style="display:none">
                    <ol>
                        <li>Go to <b>System -> Log Settings</b>.<a href="${logSettingsUrl}" target="_blank"><span class="aui-icon aui-icon-small aui-iconfont-shortcut"></span></a>.</li>
                        <li>Enter <i>com.miniorange.sso.saml</i> in class path field and select Debug in type. Click on add.</li>
                    </ol>
                    <br>
                    After these steps, perform single sign-on again to record logs. Then download <i>support zip</i> using
                    these steps,
                    <ol>
                        <li>Go to <b>System -> Troubleshooting and support tools</b>.<a href="${troubleshootingUrl}" target="_blank"><span class="aui-icon aui-icon-small aui-iconfont-shortcut"></span></a>.</li>
                        <li>navigate to the create support zip tab.</li>
                        <li>Keep only <b>Logs files</b> option selected.</li>
                        <li>Click on <b>Create zip</b> and then <b>Download zip</b>.</li>
                    </ol>
                    <br>
                </div>
            </li>

            <li>
                <b style="font-weight:600">SAML Request</b>: Download SAML Request from below. If you don't find the SAML Request below, follow
                these steps,

                <div id="saml-request-steps-div" style="display:block">
                    <ol>
                        <li>Go to <a href="listidp.action" target="_blank">Configure IDP</a> tab.</li>
                        <li><i>Select the required IDP (In case of multiple IDP).</i> Click on <b style="font-weight:600">>Test</b> option.</li>
                        <li>Click on  <b style="font-weight:600">Download SAML Request</b> button from Panel<b style="font-weight:600"> Authentication(SAML) Request</b>.</li>
                    </ol>
                    <br>
                </div>
            </li>

            <li>
                <b style="font-weight:600">SAML Response</b>: Download SAML Response from below. If you don't find the SAML Request below, follow
                these steps,

                <div id="saml-response-steps-div" style="display:block">
                    <ol>
                        <li>Go to <a href="listidp.action" target="_blank">Configure IDP</a> tab.</li>
                        <li><i>Select the required IDP (In case of multiple IDP).</i> Click on <b style="font-weight:600">Test</b> option.</li>
                        <li>Click on <b style="font-weight:600">Download SAML Response</b> button from Panel<b style="font-weight:600"> Authentication(SAML) Response.</b></li>
                    </ol>
                    <br>
                </div>
            </li>

            <#assign downloadConfigurationEndPoint = settings.getSpBaseUrl() + "/plugins/servlet/saml/downloadconfiguration">
            <li>
                <b style="font-weight:600">Test Configuration</b>: Capture the Test Configuration results and send it to us.
            </li>

            <li><b style="font-weight:600"> App Configuration</b> : <a href="${downloadConfigurationEndPoint}">Download</a> App Configuration
                file and send it to us for debugging.</li>
        </ul>
        <p>Send us a message, query or feedback attaching below files using the Support widget (requires Internet) and we will get back to you.
            Have a lot to say? You can also reach us at <a href="mailto:info@xecurify.com">info@xecurify.com</a>.</p>
    </div>
</div>