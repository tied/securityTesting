<html>
<head>
    <title>SAML SSO Configuration</title>
    <meta name="decorator" content="atl.general">
    <style>
        <#include "/css/supportedidps.css">
    </style>
    <script>
         <#include "/js/supportedidps.js">
    </script>
</head>

<body>
    <#include "*/saml/headers/samlheader.ftl" parse=true>

<div class="tabs-pane active-pane" id="supported-idps" role="tabpanel" >
    <h2>Select your IDP
    </h2>
    <hr>
    <div class="field-group" >
        <label for="Search"></label>
        <input class="mo-search" type="text"
               id="search" name="search" placeholder="Search SAML IDP" onkeyup="searchIDP()">
    </div>
    <div  class="mo-grid"id="top-grid">
        <div class="mo-idp" id="custom">
            <a href="flowdrivensetup.action?idp=custom" class="mo-block" id="customidp">
                <img src="${req.contextPath}/download/resources/com.miniorange.sso.saml.bamboo-sso:samlsso.plugin.images.resource/images/custom.png" >
            </a>
        </div>
        <div class="mo-idp" id="miniOrange">
            <a href="flowdrivensetup.action?idp=miniOrange" class="mo-block" id="miniorangeidp">
                <img src="${req.contextPath}/download/resources/com.miniorange.sso.saml.bamboo-sso:samlsso.plugin.images.resource/images/miniOrange.png" >
            </a>
        </div>
    </div>

    <div  class="mo-grid"id="mo-grid">
        <#foreach idp in idpGuides>
        <div class="mo-idp">
            <a href="flowdrivensetup.action?idp=${idp}" class="mo-block" id="${idp}">
                <img src="${req.contextPath}/download/resources/com.miniorange.sso.saml.bamboo-sso:samlsso.plugin.images.resource/images/${idp}.png" >
            </a>
        </div>
        </#foreach>
    </div>



</div>
    <#include "*/footer.ftl" parse=true>
</body>
</html>