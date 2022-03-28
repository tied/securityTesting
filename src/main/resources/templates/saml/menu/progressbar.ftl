<style>
    <#include "/css/progressbar.css">
</style>
<div class="aui-page-panel" style="margin-top:0px">
    <div class="aui-page-panel-inner">
        <div id="mo-saml-configure" class="aui-page-panel-nav mo-vert-menu">
            <button class="aui-button aui-button-subtle" onclick="showBackWarningMessage()" style="width: 100%;text-align: center;border-color: #d0d0d0;">
            <span style="text-align:center;" class="aui-icon aui-icon-small aui-iconfont-arrow-left-circle"></span>&nbsp;Back to main page</button>
            <br><br>
            <div class="progress" id="progress-div">
                <p style="font-size: 13px; width: 100%; text-align: center;"><strong>Quick setup</strong></p>
                <div id="circle1" class="circle">
                    <span class="label">1</span>
                    <span class="title">SP Information</span>
                    <span class="bar"></span>
                </div>

                <div id="circle2" class="circle">
                    <span class="label">2</span>
                    <span class="title">Configure IDP</span>
                    <span class="bar"></span>
                </div>

                <div id="circle3" class="circle">
                    <span class="label">3</span>
                    <span class="title">User Profile</span>
                    <span class="bar"></span>
                </div>

                <div id="circle4" class="circle">
                    <span class="label">4</span>
                    <span class="title">User Groups</span>
                    <span class="bar"></span>
                </div>
                <div id="circle5" class="circle">
                    <span class="label">5</span>
                    <span class="title">Test Config</span>
                </div>
            </div>
            <br><br>
            <a class="aui-button aui-button-primary" href="${idpGuide}" target="_blank" style="width: 100%;text-align: center;">View Setup Guide</a>
        </div>

      <!--  <section class="aui-page-panel-content"> -->