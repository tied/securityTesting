<html>
<head>
	<title>SSO Logout</title>
	<meta name='decorator' content='atl.general'>
	<style>
		.aui-page-panel {
			margin-left: auto;
			margin-right: auto;
			max-width: 600px;
		}
		 #main.aui-page-panel {
			 border: 1px solid #ccc;
			 border-radius: 5px;
			 margin: 50px auto 0 auto;
			 min-height: 200px;
			 overflow: auto;
			 max-width: 500px;
		 }
	</style>
</head>
<body class='aui-layout aui-theme-default page-type-login' >
<div class='aui-page-panel-inner'>
	<section class='aui-page-panel-content'>
		<div class="aui-message info">
			<span class="aui-icon icon-info"></span>
			<p class="title">You are now logged out. Any automatic login has also been stopped.</p>
			<p>Didn't mean to log out?<a href="${baseURL}"> Log in again.</a><p>
		</div>
	</section>
</div>

</body>
</html>