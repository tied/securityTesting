AJS.$(document).ready(function() {
	AJS.$('.aui-nav li').removeClass('aui-nav-selected');
	AJS.$('#mo-headerauthentication').addClass('aui-nav-selected');
	AJS.$('#mo-documentation-link').attr(
		'href',
		'https://docs.miniorange.com/documentation/header-based-authentication'
	);
});
function showEffectiveUsername() {
	var value = AJS.$('#headerAuthenticationAttribute').val();

	if (value != null) {
		document.getElementById('effective_username').innerHTML = value;
	}
}
AJS.$(function() {
	showEffectiveUsername();
	AJS.$(document).on('keyup keypress blur change', '#headerAuthenticationAttribute', function() {
		showEffectiveUsername();
	});
});
