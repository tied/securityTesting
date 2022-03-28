AJS.$(function () {

	AJS.$(".aui-nav li").removeClass("aui-nav-selected");
	AJS.$("#mo-idps-advanced").addClass("aui-nav-selected");
	AJS.$("#mo-saml").addClass("aui-nav-selected");

	refreshMetadataSettings();

	AJS.$("#refreshMetadata").change(function () {
		refreshMetadataSettings();
	});

	AJS.$("#refreshInterval").change(function () {
		var value = AJS.$("#refreshInterval").val();
		if (value == "custom") {
			AJS.$("#customRefreshValue").show();
			AJS.$("#customRefreshInterval").prop("required", true);
		} else {
			AJS.$("#customRefreshValue").hide();
			AJS.$("#customRefreshInterval").prop("required", false);
		}
	});

	function refreshMetadataSettings() {
		var refreshMetadata = document.getElementById('refreshMetadata');
		if (refreshMetadata.checked) {
			AJS.$("#refreshInterval").prop("disabled", false);
			AJS.$("#inputUrl").prop("required", true);
			if (AJS.$("#refreshInterval").val() == "custom") {
				AJS.$("#customRefreshValue").show();
				AJS.$("#customRefreshInterval").prop("required", true);
			} else {
				AJS.$("#customRefreshValue").hide();
				AJS.$("#customRefreshInterval").prop("required", false);
			}
		} else {
			console.log("refresh not set to custom");
			AJS.$("#refreshInterval").prop("disabled", true);
			AJS.$("#inputUrl").prop("required", false);
			AJS.$("#customRefreshValue").hide();
			AJS.$("#customRefreshInterval").prop("required", false);
		}

	}

});