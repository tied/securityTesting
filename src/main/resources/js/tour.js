AJS.$(function() {
    var tour_for_new_users = [
        ["sp_metadata_tour", "sp_metadata_dialog", "spmetadata"],
        ["idps_tour", "idps_dialog", "listidp"],
        ["signin_settings_tour", "signin_settings_dialog", "signinsettings"],
        ["redirection_rules_tour", "redirection_rules_dialog", "redirectionrules"],
        ["look_and_feel_tour", "look_and_feel_dialog", "looknfeel"],
        ["global_sso_settings_tour", "global_sso_settings_dialog", "globalssosettings"],
        ["backup_and_restore_tour", "backup_and_restore_dialog", "backuprestore"],
    ];

    var tour_for_existing_users = [
        ["sp_metadata_tour", "sp_metadata_dialog", "spmetadata"],
        ["idps_tour", "idps_dialog", "listidp"],
        ["actions_tour", "actions_dialog", "listidp"],
        ["idps_overview_tour", "idps_overview_dialog", "overview"],
        ["idps_config_tour", "idps_config_dialog", "addidp"],
        ["idps_profile_tour", "idps_profile_dialog", "attributemappingconfig"],
        ["idps_group_tour", "idps_group_dialog", "groupmappingconfig"],
        ["signin_settings_tour", "signin_settings_dialog", "signinsettings"],
        ["redirection_rules_tour", "redirection_rules_dialog", "redirectionrules"],
        ["look_and_feel_tour", "look_and_feel_dialog", "looknfeel"],
        ["global_sso_settings_tour", "global_sso_settings_dialog", "globalssosettings"],
        ["backup_and_restore_tour", "backup_and_restore_dialog", "backuprestore"],
    ];

    function getTour(tourName){
        var arr;
        switch(tourName){
            case "tour_for_new_users":
                arr = tour_for_new_users;
                break;
            case "tour_for_existing_users":
                arr = tour_for_existing_users;
                break;
        }
        return arr;
    }

    function getLengthOfArray(tour){
        return tour.length;
    }
    function getElementId(tour, index){
        return tour[index][0];
    }
    function getDialogId(tour, index){
        return tour[index][1];
    }
    function getPage(tour, index){
        return tour[index][2];
    }

   //check if tour is in progress
    if(sessionStorage.tourIndex){
        console.log("inside sessionStorage.tourIndex if statement");
        var tourName = sessionStorage.tourName;
        var index = sessionStorage.tourIndex;
        var tour = getTour(tourName);
        var dialogId = getDialogId(tour, index);
        var dialog = document.getElementById(dialogId);

        var waitForDialogContentToLoad = function(callback) {
            console.log("Waiting for content of inline-dialog to load");
            if (dialog.querySelector(".aui-inline-dialog-contents")) {
                callback();
            } else {
                setTimeout(function() {
                    waitForDialogContentToLoad(callback);
                }, 100);
            }
        };

        waitForDialogContentToLoad(function() {
            //Display the tour element only after "aui-inline-dialog-contents" is loaded in all "aui-inline-dialog" elements
            console.log("Calling displayTourStep");
            displayTourStep(sessionStorage.tourName, sessionStorage.tourIndex);
        });
    }

	AJS.$(document).on('click', "#start_tour_for_new_users_button", function(){
        console.log("starting tour for new users");
        AJS.dialog2("#intro_for_new_users_dialog").hide();
        startTour("tour_for_new_users");
    });

	AJS.$(document).on('click', "#start_tour_for_existing_users_button", function(){
        console.log("starting tour for existing users");
        startTour("tour_for_existing_users");
    });

	AJS.$(document).on('click', "#start_tour_for_existing_users_from_intro", function(){
        console.log("starting tour for existing users");
        AJS.dialog2("#intro_for_existing_users_dialog").hide();
        startTour("tour_for_existing_users");
    });

    function startTour(tourName){
        sessionStorage.setItem("tourIndex", 0);
        sessionStorage.setItem("tourName", tourName);
        var tour = getTour(tourName);
        window.location.href = getPage(tour, 0) + ".action";
    }

    function displayTourStep(tourName, index){
        console.log("inside displayTourStep");
        var tour = getTour(tourName);
        var elementId = getElementId(tour, index);      //Id of Element to be highlighted in this step
        var dialogId = getDialogId(tour, index);        //Id of the tour aui-inline-dialog for this step
        var inlineDialog = document.getElementById(dialogId);

        if(getPage(tour, index) == "listidp"){
            document.getElementById("start_tour_for_existing_users_button").disabled = true;
        }

        console.log("tourName: "+tourName+", index: "+index+ ", elementId:"+elementId+", dialogId:"+dialogId);

        //For highlighting - Add 'a' tag next to the element so that the dialog points to the element
//        connectDialogToElement(elementId, dialogId);

        //Add step number to dialog - eg: [3/10]
        addStepNumberToDialog(dialogId, parseInt(index)+1, getLengthOfArray(tour));

        //Add 'prev', 'close' and 'next' buttons to dialog div
        addButtonsToDialog(dialogId, index);

        //Show dialog
        console.log("Showing the tour step");
        inlineDialog.persistent = true;
        inlineDialog.open = true;
        window.scrollTo(0, document.getElementById(elementId).offsetTop);

        //functionality of the buttons
        if(index > 0){
            inlineDialog.querySelector('.prev-button').addEventListener('click', function() {
                console.log("prev button clicked");
                inlineDialog.open = false;
                index--;
                sessionStorage.setItem("tourIndex", index);
                sessionStorage.setItem("tourName", tourName);
                if(tourName=="tour_for_existing_users" && index >= 2 && index<=5){
                    AJS.$.ajax({
                        url: AJS.contextPath() + "/plugins/servlet/saml/getconfig",
                        type: "GET",
                        success: function (response) {
                            window.location.href = getPage(tour, index) + ".action?idpid="+response.idpList[0];
                        }
                    });
                } else{
                    window.location.href = getPage(tour, index) + ".action";
                }
            });
        }

        inlineDialog.querySelector('.next-button').addEventListener('click', function() {
            console.log("next button clicked");
            inlineDialog.open = false;
            index++;
            if(index == getLengthOfArray(tour)){    //End of the tour
                showSupportDialog();        //Highlight support when the tour ends
            }
            sessionStorage.setItem("tourIndex", index);
            sessionStorage.setItem("tourName", tourName);
            if(tourName=="tour_for_existing_users" && index >= 2 && index<=5){
                AJS.$.ajax({
                    url: AJS.contextPath() + "/plugins/servlet/saml/getconfig",
                    type: "GET",
                    success: function (response) {
                        window.location.href = getPage(tour, index) + ".action?idpid="+response.idpList[0];
                    }
                });
            } else{
                window.location.href = getPage(tour, index) + ".action";
            }
        });

        inlineDialog.querySelector('.close-button').addEventListener('click', function() {
            console.log("close button clicked");
            inlineDialog.open = false;
            showSupportDialog();             //Whenever the a Close button is clicked - Highlight support
        });
    }

    function showSupportDialog(){
        console.log("showing SupportDialog");
        var supportButton = document.getElementById("contact-us");
        var dialog = document.getElementById("support_div_dialog");
        var dialogContent = dialog.querySelector(".aui-inline-dialog-contents");
        var closeButton = getButton("aui-button close-button", "Close");
        closeButton.style.cssFloat = "right";
        closeButton.style.color = "white";
        closeButton.style.backgroundColor  = "red";
        dialogContent.appendChild(closeButton);

        dialog.open = true;
        window.scrollTo(0, supportButton.offsetTop);

        dialog.querySelector('.close-button').addEventListener('click', function() {
            dialog.open = false;
            sessionStorage.removeItem("tourIndex");
            sessionStorage.removeItem("tourName");
            setShowIntroPageToFalse();
        });
    }

    function addStepNumberToDialog(dialogId, numerator, denominator){
        var dialog = document.getElementById(dialogId);
        var dialogContent = dialog.querySelector(".aui-inline-dialog-contents");
        var heading = dialogContent.querySelector("h3");
        var para = document.createElement('p');
        para.setAttribute("style", "float:right");
    	var textNode = document.createTextNode("["+numerator+"/"+denominator+"]");
    	para.appendChild(textNode);
        heading.appendChild(para);
    }

//    function connectDialogToElement(elementId, dialogId){
//        var element = document.getElementById(elementId);		// element = Element to be highlighted
//        var a = document.createElement('a');
//        a.setAttribute("aria-controls", dialogId);
//        element.appendChild(a);     // adding anchor tag inside the element [so that element gets highlighted]
//    }

    function addButtonsToDialog(dialogId, index){
        var dialog = document.getElementById(dialogId);
        var dialogContent = dialog.querySelector(".aui-inline-dialog-contents");

        if(index > 0){
            var prevButton = getButton("aui-button aui-button-primary prev-button", "Prev");
            dialogContent.appendChild(prevButton);
        }

        var nextButton = getButton("aui-button aui-button-primary next-button", "Next");
        dialogContent.appendChild(nextButton);

        var closeButton = getButton("aui-button close-button", "Close");
        closeButton.style.cssFloat = "right";
        closeButton.style.color = "white";
        closeButton.style.backgroundColor  = "red";
        dialogContent.appendChild(closeButton);
    }

    function getButton(buttonClass, buttonName){
    	var button = document.createElement("a");
    	button.setAttribute("class", buttonClass);
    	var textNode = document.createTextNode(buttonName);
    	button.appendChild(textNode);
    	return button;
    }
});

function setShowIntroPageToFalse(){
    console.log("setting intro page to false");
    AJS.$.ajax({
        url: AJS.contextPath() + "/plugins/servlet/saml/moapi",
        type: "POST",
        data:{
            "action":"setIntroPage"
        },
        success: function (response) {
            window.location.href = "listidp.action";
        },
        error:function(response){
            window.location.href = "listidp.action";
        }
    });
}