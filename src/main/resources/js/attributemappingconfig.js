AJS.$(function () {
	AJS.$(".aui-nav li").removeClass("aui-nav-selected");
	AJS.$("#mo-idps-profile").addClass("aui-nav-selected");
	AJS.$("#mo-saml").addClass("aui-nav-selected");
	showEmailWarningMessage();
	isRegexEnabled();
	isSeparateNameAttributeEnabled();



	AJS.$("#test-regex").keyup(function (){
         testregexConfigurations();
    });

    AJS.$("#test-regex").click(function (){
          testregexConfigurations();
    });

	function testregexConfigurations() {
		var osDestination = "testregex";
		var samlAuthUrl = AJS.contextPath() + '/plugins/servlet/saml/auth';
		samlAuthUrl += "?return_to=" + encodeURIComponent(osDestination) + "&regexp=" +encodeURIComponent(AJS.$("#regexPattern").val());
		var myWindow = window.open(samlAuthUrl, "TEST Regex Configuration", "scrollbars=1 width=600, height=400");
	}

	function showEmailWarningMessage() {
		value = AJS.$("#loginUserAttribute").val();
		if (value != "email") {
			AJS.$("#warningforemail").hide();
		} else {
			AJS.$("#warningforemail").show();
		}
	}

	AJS.$("#regexPatternEnabled").change(function () {
		isRegexEnabled();
	});

	AJS.$("#useSeparateNameAttributes").change(function () {
		isSeparateNameAttributeEnabled();
	});

	AJS.$("#loginUserAttribute").change(function () {
		value = AJS.$("#loginUserAttribute").val();
		if (value != "email") {
			AJS.$("#warningforemail").hide();
		} else {
			AJS.$("#warningforemail").show();
		}
	});


	function isSeparateNameAttributeEnabled() {
		if (AJS.$("#useSeparateNameAttributes").is(":checked")) {
			AJS.$("#fullNameAttributeDiv").hide();
			AJS.$("#separateNameAttributes").show();
		} else {
		    AJS.$("#separateNameAttributes").hide();
			AJS.$("#fullNameAttributeDiv").show();
		}
	}

	function isRegexEnabled() {
		if (AJS.$("#regexPatternEnabled").is(":checked")) {
			AJS.$("#regexfield").show();
			AJS.$("#regexPattern").prop("required", true);
		} else {
			AJS.$("#regexfield").hide();
			AJS.$("#regexPattern").prop("required", false);
		}
	}

	AJS.$(document).on('keyup', '#regexfield', function (e) {
    		setTimeout(function enable() {
    			if (AJS.$("#regexPattern").val() != "") {
    				AJS.$("#test-regex").prop("disabled", false);
    			} else {
    				AJS.$("#test-regex").prop("disabled", true);
    			}
    		}, 500);
    	});

});