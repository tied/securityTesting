AJS.$(function () {
	AJS.$(".aui-nav li").removeClass("aui-nav-selected");
	AJS.$("#mo-idps-group").addClass("aui-nav-selected");
	AJS.$("#mo-saml").addClass("aui-nav-selected");
	if (AJS.$("#groupRegexPatternEnabled").is(":checked")) {
            AJS.$("#groupRegexfield").show();
            AJS.$("#regexPatternForGroup").prop("required", true);
            AJS.$("#regexGroups").prop("required", true);
        } else {
            AJS.$("#groupRegexfield").hide();
            AJS.$("#regexPatternForGroup").prop("required", false);
            AJS.$("#regexGroups").prop("required", false);
        }

    AJS.$("#groupRegexPatternEnabled").change(function () {
        if (this.checked) {
            AJS.$("#groupRegexfield").show();
            AJS.$("#regexPatternForGroup").prop("required", true);
            AJS.$("#regexGroups").prop("required", true);
        } else {
            AJS.$("#groupRegexfield").hide();
            AJS.$("#regexPatternForGroup").prop("required", false);
            AJS.$("#regexGroups").prop("required", false);
        }
    });

    AJS.$("#test-group-regex").click(function () {
        var osDestination = "testgroupregex";
        var value=AJS.$("#testGroupRegex").val();
        var testUrl = AJS.contextPath() + '/plugins/servlet/saml/auth';
        testUrl += "?return_to=" + encodeURIComponent(osDestination) + "&regexp=" +
        encodeURIComponent(AJS.$("#regexPatternForGroup").val())+"&regexg="+
        encodeURIComponent(AJS.$("#regexGroups").val())+"&groupName="+
        encodeURIComponent(AJS.$("#testGroupRegex").val());
        var testWindow = window.open(testUrl, "", "width=600,height=400");
        });

    if (AJS.$("#onTheFlyAssignNewGroupsOnly").is(":checked")) {
    		AJS.$("#onTheFlyDoNotRemoveGroupsDiv").hide();
    	} else {
    		AJS.$("#onTheFlyDoNotRemoveGroupsDiv").show();
    	}
	isUserCreationRestricted();
	toggleOnTheFlyGroupMapping();

});


AJS.$(document).on('change',"#onTheFlyAssignNewGroupsOnly",function () {
	if (this.checked) {
		AJS.$("#onTheFlyDoNotRemoveGroupsDiv").hide();
	} else {
		AJS.$("#onTheFlyDoNotRemoveGroupsDiv").show();
	}
});

function showMappingInstruction() {
	var value = document.getElementById("group-mapping-instructions-div");
	if (value.style.display === "none") {
		AJS.$('#group-mapping-instructions-div').show("slow");
	} else {
		AJS.$('#group-mapping-instructions-div').hide("slow");
	}
}

function showOnTheFlyMappingInstruction() {
	var value = document.getElementById("onthefly-group-mapping-instructions-div");
	if (value.style.display === "none") {
		AJS.$('#onthefly-group-mapping-instructions-div').show("slow");
	} else {
		AJS.$('#onthefly-group-mapping-instructions-div').hide("slow");
	}
}

function toggleRecommendations() {
	AJS.$("#group-mapping-recommendation").toggle(400);
}


AJS.$(document).on('click', '#group-mapping-pill', function () {
	AJS.$('#group-mapping-pill').attr("class", "active");
	AJS.$("#on-the-fly-group-mapping-pill").removeClass("active");
	toggleOnTheFlyGroupMapping();
});

AJS.$(document).on('click', '#on-the-fly-group-mapping-pill', function () {
	AJS.$('#on-the-fly-group-mapping-pill').attr("class", "active");
	AJS.$("#group-mapping-pill").removeClass("active");
	toggleOnTheFlyGroupMapping();
});

function toggleOnTheFlyGroupMapping() {
	var groupMappingClass = AJS.$('#group-mapping-pill').attr("class");
	if (groupMappingClass == "active") {
		AJS.$("#onthefly-group-mapping-instructions-div").hide();
		AJS.$("#onTheFlyGroupMappingDiv").hide();
		AJS.$("#onTheFlyCreateNewGroupsDiv").hide();
		AJS.$("#onTheFlyAssignNewGroupsOnlyDiv").hide();
		AJS.$("#on-the-fly-group-mapping-main-inst").hide();
		AJS.$("#groupMappingDiv").show();
		AJS.$("#group-mapping-main-inst").show()
		AJS.$("#onTheFlyDoNotRemoveGroupsDiv").hide();
		AJS.$("#onTheFlyGroupRegexDiv").hide();
		AJS.$("#roleAttribute").prop("required", false);
		AJS.$("#onTheFlyGroupCreation").val(false);
	} else {
		AJS.$("#groupMappingDiv").hide();
		AJS.$("#group-mapping-instructions-div").hide();
		AJS.$("#group-mapping-main-inst").hide();
		AJS.$("#onTheFlyAssignNewGroupsOnlyDiv").show();
		AJS.$("#onTheFlyGroupMappingDiv").show();
		AJS.$("#onTheFlyCreateNewGroupsDiv").show();
		AJS.$("#onTheFlyGroupRegexDiv").show();
		AJS.$("#on-the-fly-group-mapping-main-inst").show();
		if(AJS.$("#onTheFlyAssignNewGroupsOnly").is(":checked") ==false)
	    {
		    AJS.$("#onTheFlyDoNotRemoveGroupsDiv").show();
        }

		AJS.$("#roleAttribute").prop("required", true);
		AJS.$("#onTheFlyGroupCreation").val(true);

	}
}

AJS.$("#restrictUserCreation").change(function () {
	isUserCreationRestricted();
});


function isUserCreationRestricted() {
	if (AJS.$("#restrictUserCreation").is(":checked")) {
		AJS.$("#createUsersIfRoleMapped").prop("disabled", true);
	} else {
		AJS.$("#createUsersIfRoleMapped").prop("disabled", false);
	}
}