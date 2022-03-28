<html>
<head>
    <meta name="decorator" content="atl.general">
    <script>
            <#include "/js/troubleshooting.js">
        AJS.$(function() {
            AJS.$(".aui-nav li").removeClass("aui-nav-selected");
            AJS.$("#mo-troubleshooting").addClass("aui-nav-selected");
        });
    </script>
    <style>
            <#include "/css/troubleshooting.css">
    </style>
</head>

<body>
<#include "*/saml/headers/samlheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="troubleshooting" role="tabpanel">
    <h1>Troubleshooting and Support</h1>
    <hr class="header"/>
    <#include "*/saml/troubleshootingcontent.ftl" parse=true>
</div>
<#include "*/footer.ftl">
</body>
</html>