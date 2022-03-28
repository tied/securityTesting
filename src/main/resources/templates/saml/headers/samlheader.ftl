<script>
    AJS.$(function() {
        AJS.$("#mo-saml").addClass("aui-nav-selected");
    });

    <#include "/js/tour.js">
</script>

<#include "*/header.ftl" parse=true>
<#include "*/saml/menu/samlmenu.ftl" parse=true>

<#include "*/licensewarning.ftl" parse=true>
<#include "*/saml/tour/tour_for_existing_and_new_users.ftl" parse=true>