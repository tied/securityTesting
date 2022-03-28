AJS.$(function () {
    AJS.$(".aui-nav li").removeClass("aui-nav-selected");
    AJS.$("#mo-sign-in-settings").addClass("aui-nav-selected");
    AJS.$("#mo-saml").addClass("aui-nav-selected");
    AJS.$("#reveal-text-trigger").trigger("aui-expander-expand");
    window.onbeforeunload = null; //To disable the Leave Site popup
    var moApi = AJS.contextPath() + "/plugins/servlet/saml/moapi";

    showRestrictBackdoorSwitch();
    showBackdoorGroups();
    initializeBackdoorGroupsDropdown();


    function initializeBackdoorGroupsDropdown() {
        AJS.$("#backdoorGroups").auiSelect2({
            placeholder: 'Select the groups to allow backdoor URL access to',
            ajax: {
                url: AJS.contextPath() + '/plugins/servlet/saml/moapi',
                data: function (params) {
                    var query = {
                        search: params,
                        action: 'fetchGroups'
                    }

                    // Query parameters will be ?search=[term]&type=public
                    return query;
                },
                results: function (data, page) {
                    return {
                        results: data.results
                    };
                },

            },
            multiple: true
        });


        var backdoorGroups = [];

        AJS.$.ajax({
            url: AJS.contextPath()+'/plugins/servlet/saml/moapi',
            type:'GET',
            data:{
                'action':'getGroups',
                'type':'backdoor'
            },
            success:function (response) {
                backdoorGroups=response.results;
                if (backdoorGroups.length > 0)
                    AJS.$("#backdoorGroups").auiSelect2('data', backdoorGroups);
            }

        });
    }

    AJS.$(document).on('click',"#edit-backdoor-button",function(){
        showEditBackdoorForm();

    });

    AJS.$(document).on('click',"#save-backdoor-button",function(){
        AJS.$("#backdoor_submitted").val(true);
        AJS.$("#advanced_settings_submitted").val(false);
        AJS.$.ajax({
            url:AJS.contextPath()+"/plugins/servlet/bamboo-sso/redirectionrules.action",
            type: "POST",
            data:AJS.$("#save-redirection-toggles-form").serialize(),
            success:function(response){
                showSuccessMessage("Backdoor URL Saved Successfully");
                AJS.$('#backdoor-key').html(AJS.$("#backdoor_key").val());
                AJS.$('#backdoor-value').html(AJS.$("#backdoor_value").val());
                hideEditBackdoorForm();
            },
            error:function(response){
                AJS.flag({
                    title: 'Error!',
                    type: 'error',
                     close: 'auto',
                    body: '<p>Error saving backdoor url.</p><br/>'
                    });
            }
        });
        //AJS.$("#save-redirection-toggles-form").submit();
    });

    AJS.$(document).on('click',"#cancel-backdoor-button",function(){
        hideEditBackdoorForm();
    });

    function showEditBackdoorForm(){
        AJS.$("#saved_backdoor_div").hide();
        AJS.$("#edit_backdoor_div").show();
    }

    function hideEditBackdoorForm(){
        AJS.$("#saved_backdoor_div").show();
        AJS.$("#edit_backdoor_div").hide();
    }

    AJS.$(document).on("change","#enableBackdoor",function () {

        showRestrictBackdoorSwitch();
    });

    AJS.$(document).on("change","#restrictBackdoor",function () {
        showBackdoorGroups();
    });

    AJS.$(document).on("change","#enableAutoRedirect",function () {

            showDelaySwitch();
        });


    AJS.$(document).on("click","#show-url-details",function () {
        var backdoorUrlDiv = document.getElementById('backdoorUrlDiv');
        if (backdoorUrlDiv.style.display === 'none') {
            backdoorUrlDiv.style.display = 'block';
            this.innerHTML = "Hide Backdoor URL";
        } else {
            backdoorUrlDiv.style.display = 'none';
            this.innerHTML = "Show Backdoor URL";
        }
    });






    function showRestrictBackdoorSwitch(){
        console.log("enable backdoor "+document.getElementById("enableBackdoor"));
        if (document.getElementById("enableBackdoor").checked) {
            AJS.$("#restrict-backdoor-suboptions").show();
            AJS.$("#Login-attempts-div").show();
            AJS.$('#backdoor-url-warning').hide();
        } else {
            AJS.$("#restrict-backdoor-suboptions").hide();
            AJS.$("#Login-attempts-div").hide();
            document.getElementById("restrictBackdoor").checked=false;
            AJS.$('#backdoor-url-warning').show();
        }
    }

    function showDelaySwitch(){
            console.log("Auto redirect "+document.getElementById("enableAutoRedirect"));
            if (document.getElementById("enableAutoRedirect").checked) {
                AJS.$("#auto-redirect-delay").show();

            } else {
                AJS.$("#auto-redirect-delay").hide();
                document.getElementById("enableAutoRedirectDelay").checked=false;

            }
        }

    function showBackdoorGroups() {
        if (document.getElementById("restrictBackdoor").checked) {
            AJS.$("#backdoorAccessGroupsList").show();
        } else {
            AJS.$("#backdoorAccessGroupsList").hide();
        }
    }

    function showDelay(){
        var enableAutoRedirect = document.getElementById("enableAutoRedirect");
                    enableAutoRedirect = enableAutoRedirect.checked ? true : false;
                    if(enableAutoRedirect) {
                         document.getElementById('auto-redirect-delay').style.display = "block";
                    } else {
                        document.getElementById('auto-redirect-delay').style.display = "none";
                    }
    }


    setTimeout(showRestrictBackdoorSwitch, 100);
    setTimeout(showBackdoorGroups,100);

});