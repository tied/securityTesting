<html>
<head>
    <meta name="decorator" content="atl.general">
	<script>
        <#include "/js/backuprestore.js">
    </script>
</head>
<body>
<#include "*/saml/headers/samlheader.ftl" parse=true>
<div class="tabs-pane active-pane" id="importexport"  role="tabpanel" >
    <h1>Backup/Restore App Configurations</h1>
    <hr class="header"/>
     <div class="aui-message aui-message-info">
            <p>This tab will help you to transfer your app configurations when you change your Bamboo instance; e.g. when you migrate from test environment to production, the app configurations can be transferred in two ways:</p>
                    <p style="font-size:12pt;">1. Download/Upload app Configurations by File.</p>
                    <ul>
                        <li>
    						Download app configuration file by clicking on the link given below.
    					</li>
                        <li>
    						Install the app on new Bamboo instance.
    					</li>
                        <li>
    						Upload the configuration file in Download/Upload app Configurations by File section.
    					</li>
                    </ul>
                    <p style="font-size:12pt;">2. Configure/Fetch app configuration via REST API.</p>
                    <ul>
                        <li>
    						To fetch app configuration from test instance you need URL from test environment and you need to use GET.
    					</li>
                        <li>
    						To configure app at production instance you need URL from production and you need to use POST.
    					</li>
                        <li>
    						URLs and steps you will find below in Configure app via REST API section.
    					</li>
                    </ul>
            <p> And just like that, all your app configurations will be transferred! </p>

        </div>



    <h2>1. Download/Upload app Configurations by File.</h2>
	<hr class="header"/>
	<br>
	<p style="font-size:12pt;">A. Download App configuration File.</p><br>
	<div style="margin-left: 35px;">
   <#assign downloadConfigurationEndPoint = settings.getSpBaseUrl() + "/plugins/servlet/saml/downloadconfiguration">
   	    <#if (settings.getIdPList().size()>0) >
   	    	<a href="${downloadConfigurationEndPoint}" target="_blank" class="aui-button aui-button-link"><b>Click Here </b></a> to download your app configurations file.
   	    <#else>
   	        <b style="color:grey">Click Here</b> to download your app configurations file
   	    </#if>
    <br>
	</div><br>
    <p style="font-size: 12pt">B. Upload App Configurations File.</p>
	<div style="margin-left: 35px;">
	<#assign uploadJSONFile = settings.getSpBaseUrl() + "/plugins/servlet/saml/uploadconfiguration">
		<form id="importConfigurations" class="aui long-label" action="${uploadJSONFile}" method="post" enctype="multipart/form-data">
			<input type="hidden" name="importFileSubmitted" value="true" />
			<input type="hidden" name="atl_token" value="${xsrfToken}" />
			<table style="width:50%">
				<tr>
					<td width="20%">Choose File
						<span style="color:red">*</span>: </td>
					<td>
						<input type="file" id="configFile" name="configFile" class="ignore-inline-attach" accept=".json" required/></input> &nbsp; &nbsp;
						<input type="submit" value="Import" class="aui-button aui-button-primary" />
					</td></tr>
				<tr><td width="20%"></td>
					<td>
						<div class="description">Upload plugin_configurations.json file here</div>
					</td>

				</tr>
			</table>
		</form>
	</div>
		<h2>2. Configure/Fetch app configuration via REST API.</h2>
		<hr class="header"/>
		<div class="aui-message aui-message-info">
        <p><span>Note: The user must have administrator credentials to fetch the app configuration. To fetch app configuration, you need to use GET and to configure the app use POST.</p>
        <p>Use Basic Authentication and it will be base64 encoded.</p>
        </div>
        <div>
		<table style=" width: 100%;">
		<tbody>
		<td style=" width: 39%;vertical-align: initial;">
		<br>
                        <p style="font-size:12pt;">A. Download/Fetch App configuration</p><br>
						<div style="margin-left: 35px;">
							URL&emsp;&emsp; : ${settings.getBaseUrl()}/plugins/servlet/configureplugin<br>
							Method : GET<br>
							Request Parameters:<br>
								   <ul>
									Headers :-
									<div style="margin-left: 35px;">
									<li>
										Content-Type : application/json
									</li>
									<li>
										Authorization: Basic &lt;base64 encoded admin_username:password&gt;
									</li>
									</div>
									</ul>
								   <p>You will get JSON raw data in response. Copy and store raw data in the JSON file that you will need during the configuration.</p><br>
		</td>
		<td>
		<div></div>
		</td>
		<td style=" width: 59%;vertical-align: initial;"><br>
		<p style="font-size:12pt;">Sample Response</p>
									<textarea rows = "12" class="textarea long-field" cols = "74" style="background-color: rgb(235, 236, 240);line-height:1.8; width:671px;" readonly>{
  "Identity Providers": [{
    "ID": "ab514008-bf95-4992-8290-b622c16d479d",
    "Name": "Okta",
    "Configure SP": {
      "IDP Entity ID / Issuer": "http://www.okta.com/exkfskjjp7lLVPO1u0h7",
      "Send Signed Requests": true,
      "SSO Binding Type": "HttpRedirect",
      "Single Sign On URL": "https://dev-303074.oktapreview.com/app/localdev303074_bamboosaml_1/exkfskjjp7lLVPO1u0h7/sso/saml",
      "SLO Binding Type": "HttpRedirect",
      "Single Logout URL": "https://dev-303074.oktapreview.com/app/localdev303074_bamboosaml_1/exkfskjjp7lLVPO1u0h7/slo/saml",
      "NameID Format": "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress",
      "IdP Signing Certificates": ["-----BEGIN CERTIFICATE-----\r\nMIIDpDCCAoygAwIBAgIGAWTLyihiMA0GCSqGSIb3DQEBCwUAMIGSMQswCQYDVQQG\r\nEwJVUzETMBEGA1UECAwKQ2FsaWZvcm5pYTEWMBQGA1UEBwwNU2FuIEZyYW5jaXNj\r\nbzENMAsGA1UECgwET2t0YTEUMBIGA1UECwwLU1NPUHJvdmlkZXIxEzARBgNVBAMM\r\nCmRldi0zMDMwNzQxHDAaBgkqhkiG9w0BCQEWDWluZm9Ab2t0YS5jb20wHhcNMTgw\r\nNzI0MTAxNTIwWhcNMjgwNzI0MTAxNjIwWjCBkjELMAkGA1UEBhMCVVMxEzARBgNV\r\nBAgMCkNhbGlmb3JuaWExFjAUBgNVBAcMDVNhbiBGcmFuY2lzY28xDTALBgNVBAoM\r\nBE9rdGExFDASBgNVBAsMC1NTT1Byb3ZpZGVyMRMwEQYDVQQDDApkZXYtMzAzMDc0\r\nMRwwGgYJKoZIhvcNAQkBFg1pbmZvQG9rdGEuY29tMIIBIjANBgkqhkiG9w0BAQEF\r\nAAOCAQ8AMIIBCgKCAQEAwnpVMyvJZwtDNEgoMnmX8Di1U0HMeb+Gf5Esk8uEqZWp\r\nclIuHQsiihQqlO5x2++0EIop2OoMhREhsLxwWOYUQiiuE7vctMjFQXwzNdqgOzNk\r\nsUzx+I7QYi3BG8FA6i/1wrIhOVoQVpYCb65xlYnj6hjPMMTn9YTe81KYGqYSDmJ0\r\nnCM62yvDPLLLLsm7D0G2QQz+e9tw+ODQSfmWy9tndEqtR25Y/68ovNwd33WkjRW2\r\nTl1we06wP50tiY9pThSLKuaJbjVzdVlFmwRzNVpX55zkQeLM5h9RRQnPgI+poABM\r\nkT39w8m+QWPEx3dZB40xgylZ5lEbquWNrd2XKJXE/QIDAQABMA0GCSqGSIb3DQEB\r\nCwUAA4IBAQAiENDnT+wkYRzbq0cHOxlQXPEqK0mcwDAv8DBfvUlAadFjnqSxVJx5\r\nk5GjYR1Qv2Ra74Bb5lZlEsr1jQV1Htu2duxd9A/DG3JeSbSx9MfhiZoKGZkvqxPp\r\nJAbRJjIHj5A12Z5bSesqCiOVP9uFxMy0hHR9DvwO259jwz6BqPKuvoE3Yv43Jfmn\r\nwWpiu5RCzlNHbmQbkyn5jpmL1SJiBazno7TjgBHiO6AVg6q4uGGmfGvBDzo00CxY\r\npnjusHeDM4vXuMM07h2c4Q7yhGWs+dFr0RpHa9TtmGLctvvq1RbFavasaKA1VjBJ\r\nd/pjHACHRe+xl+EA7TW3LWNG1doeWx+r\r\n-----END CERTIFICATE-----"],
      "Enable SSO For IDP": true
    },
    "Attribute Mapping": {
      "Username": "NameID",
      "Email": "NameID",
      "Fullname": "",
      "First Name": "",
      "Last Name": "",
      "Disable Attribute Mapping": true,
      "Separate Name Attributes": false,
      "Regex Enabled": false,
      "Login/Create Bamboo user account by": "username",
      "Extended Attribute Mapping": {}
    },
    "Group Mapping": {
      "Disable Group Mapping": true,
      "Group Attribute": "",
      "Restrict User Creation": false,
      "Restrict User Creation based on Group Mapping": false,
      "Default Group": "bamboo-users",
      "On The Fly Group Mapping": false,
      "Create New Groups": true,
      "Enable Default Group For": "newUsers",
      "Default Groups": ["bamboo-users"],
      "Keep users in existing groups": true,
      "Mapping": {}
    },
    "Import Metadata": {
      "IDP/Metadata Option": "fromUrl",
      "Refresh Metadata": false,
      "Metadata Url/Domain": "https://dev-303074.oktapreview.com/app/exkfskjjp7lLVPO1u0h7/sso/saml/metadata",
      "Effective Metadata Url": ""
    }
  }],
  "SP Info": {
    "SP Entity ID": "http://localhost:8090",
    "SP Base URL": "http://localhost:8090",
    "Include Signing Certificate in Metadata": true,
    "Include Encryption Certificate in Metadata": false,
    "Organization Name": "miniOrange",
    "Organizaton Display name": "miniOrange",
    "Organization Url": "https://www.miniorange.com/",
    "Technical Contact Name": "Xecurify",
    "Technical Contact Email": "info@xecurify.com",
    "Support Contact Name": "Xecurify",
    "Support Contact Email": "info@xecurify.com"
  },
  "Sign In Settings": {
    "Enable SAML SSO": true,
    "Login Button Text": "Use Corporate Login",
    "Relay State URL": "",
    "Auto Redirect to IdP": false,
    "Enable backdoor login": false,
    "miniorange.saml.BACKDOOR_KEY": "saml_sso",
    "miniorange.saml.BACKDOOR_VALUE": "false",
    "Enable AutoRedirect Delay": false,
    "Secure Admin Login Options": "noAdminSessionOption",
    "Remember-Me cookie": false,
    "Time Skew": "01",
    "Custom Logout URL": "",
    "Use Custom Logout Template": false,
    "Logout Template": " ",
	"Redirect after Logout": false,
    "Error Message Template": " ",
	"Enable Custom Error Message Template": false,
    "Use Custom Login Template": false,
    "Login Template": "
	"Restrict Backdoor Access": false,
    "Restrict Backdoor Access For": ["bamboo-administrators"]
  },
  "Certificates": {
    "Public SP Certificate": "-----BEGIN CERTIFICATE-----\r\nMIID7TCCAtWgAwIBAgIJAMcsf4R7oVMZMA0GCSqGSIb3DQEBCwUAMIGMMQswCQYD\r\nVQQGEwJJTjELMAkGA1UECAwCTUgxDTALBgNVBAcMBFBVTkUxEzARBgNVBAoMCk1J\r\nTklPUkFOR0UxEzARBgNVBAsMCk1JTklPUkFOR0UxEzARBgNVBAMMCk1JTklPUkFO\r\nR0UxIjAgBgkqhkiG9w0BCQEWE2luZm9AbWluaW9yYW5nZS5jb20wHhcNMTUxMDMw\r\nMTA1NDQ4WhcNMjAxMDI4MTA1NDQ4WjCBjDELMAkGA1UEBhMCSU4xCzAJBgNVBAgM\r\nAk1IMQ0wCwYDVQQHDARQVU5FMRMwEQYDVQQKDApNSU5JT1JBTkdFMRMwEQYDVQQL\r\nDApNSU5JT1JBTkdFMRMwEQYDVQQDDApNSU5JT1JBTkdFMSIwIAYJKoZIhvcNAQkB\r\nFhNpbmZvQG1pbmlvcmFuZ2UuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\r\nCgKCAQEAsxulwAiXvaJvT6JEckasFcHY7eME2hjClXPKtGJ6okiPOPQjMAv+zYxZ\r\n2beAUPWxg1pfE7HIdTLh6A0yD2Afnw9ayKmCGiq6rX8TqXzEo8J01M/zGRBXxw+Q\r\nCjB7BpWpHUVcdfagUEJrURHRcx6VXXf/9xprbtv7Wsx/WVhqGl6MCtj4m5tTsHyY\r\nD9BOawxtmaq7dNSECkt9qNUfu+EvTYk3LHI3IoJR4HcMTsYjTbJo6lHNT18FQqRe\r\nWcjNXCTvH17Zit4MaH8WGlL32KV62EyTPZwjqrmUHqoXfj87e+1XOpYk+Z/dApMC\r\n47I6++yq+FlyvVne0w48SAHYt4M1rQIDAQABo1AwTjAdBgNVHQ4EFgQUyihK6rNy\r\nl3Sx9Onzzup0qko7z7QwHwYDVR0jBBgwFoAUyihK6rNyl3Sx9Onzzup0qko7z7Qw\r\nDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAQEAPnp6Q5jfZ33/0hbeeVr+\r\nts5PTxKKdPakoGJWAbgqXzf4h8TuCZMjPBE6g7jk1JqvRFWxg7zx+qhvlWRnwfWl\r\n9yAffY0TbBx+EU3kyTYBg2UnffUaSvoko1UzFK1v4dOP2u+wTP8nM/I+HxBjVVcg\r\nT+7zOK9Y6GXe1spjdQb2ELdBQ2p7NFXFF4uy6jzN9yw2xBid7ZLkJwGeOykZrrd1\r\nYJzGZJoGedpTxrkqbIqRUFnCqKRgB5IzhXO1Xj+xgv8qr0KNJ4oqf58OEHnx2XF2\r\n1RY0F9vpQ6/BPQKqO4pWjEuWanV36AZ5nHw6PeJXEsK2RUnABeA/xzjxH/NT6Fh3\r\ndQ==\r\n-----END CERTIFICATE-----",
    "Private SP Certificate": "-----BEGIN PRIVATE KEY-----\r\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCzG6XACJe9om9P\r\nokRyRqwVwdjt4wTaGMKVc8q0YnqiSI849CMwC/7NjFnZt4BQ9bGDWl8Tsch1MuHo\r\nDTIPYB+fD1rIqYIaKrqtfxOpfMSjwnTUz/MZEFfHD5AKMHsGlakdRVx19qBQQmtR\r\nEdFzHpVdd//3Gmtu2/tazH9ZWGoaXowK2Pibm1OwfJgP0E5rDG2Zqrt01IQKS32o\r\n1R+74S9NiTcscjciglHgdwxOxiNNsmjqUc1PXwVCpF5ZyM1cJO8fXtmK3gxofxYa\r\nUvfYpXrYTJM9nCOquZQeqhd+Pzt77Vc6liT5n90CkwLjsjr77Kr4WXK9Wd7TDjxI\r\nAdi3gzWtAgMBAAECggEAXxvSU3v8n92OwDnJoOk4XkFWoVQI8ottYJFhVZJpzp07\r\nOKIjwVLNVncuCzevMs6VGcw2aq3gZaPEZEYFjDad9AfiTGX+yUhhYtn1Je40OUV8\r\nZrmaPHf6NKFl+pMt8Rwt9nYrmBSDorryY9VTZI8b/8tGKABmhjLYcangAiC6Dw+7\r\nKEPYqrNHfz6W7Xc8jydReWF/Cl0+qu9gVU2yiRh7/MDyE6NLs7DdkQY2E0OwKkSF\r\nDLeGZe/Bn99rSWuZKgWJRTe/oatoeXmwP+oyutlw1jktLihXKDH0fHtgFSUc/M6D\r\naAjnag+W1yEA0vPEUi02mFwCYazax8e5d+bqFKqJfQKBgQDthutSu2t43sh6RimP\r\n7vVM98QTgdh4oeLxgt8vcE/4lcXg3xlcKObl77KgYKp/R1KH186lSJPV7Nv4so9y\r\nFkS3jdUpqW2VrR+C/muCjFCJTus56H1mP2MPDRUcH7/60KoMJ+lGWDMA1EOzR6iY\r\n99xaYL+5eCIuwVDyySdH1I2sdwKBgQDBCaAm3lOEByUkSGGWz3NgNqgPT5apk94y\r\n+PaQqtJFRDa4DZ5NSgFWsmV9gfmSSEP5ofPHDg2TuCQawwfvEyDUu85QNDejBD3k\r\nnhyBdv75ljHLzGkIyTsEhHaefK9j2LSJ/FjhssVa1UerFycKZGNYTBQFXUNOnGI3\r\ndCe5bd4L+wKBgQCAykoolsTkSe/sFMUObSQGssVXSm1ko2qZRzvazgwenp8nmfB3\r\nPxpLjNePDDsGPlbqn0JF7n9yXDa9t5v94UMrP7VYHKz1nmRas7b5lzlH6kmzIXN1\r\nEGOW0qIimLiQt7msU6ux37rv9SgsOmuZXbPWMWVjnFb0gQ9yRU2OLhsPawKBgQCs\r\n3rPfWwarm8J1RaSva8RFC21hmMKxxkwclbwpPfDkOvxNw1RansWoIEanKPiZOcI2\r\nEgS/5CPgf+1FUhcO0LB8Elvmk3ynrirHS5j3j9szJIAfpEUDq0IA/6dCsyJWZkD3\r\nuVbXeEMo3ws5ephpxBD7h9X+H9Eg2wdR5eGVO4C4dQKBgFPv0IBl2LAYz+pDTbq9\r\nUndfb7fZ3OwJq9Y5QChj8zIhgf5EQZdk0YkPGvc2n2QbVjNUPAZ4wKeXfOUvj8zR\r\nx0F/+RsJfG1TrDtFe0/8pWnt5oXYxLN0/vHEUU/GXaHNiuXvy3FcGSWMjtg2Ekm4\r\nLWf07xNzN+vCkgBQdU8iAKoP\r\n-----END PRIVATE KEY-----"
  },
  "Domain Mapping Configurations": {
    "Use Domain Mapping": false,
    "Domain Mapping": {}
  }
}

        </textarea>
</td>
</tbody>
</table>
						</div>

                         <p style="font-size:12pt;">B. Update App configuration</p><br>
							<div style="margin-left: 35px;">
                                URL&emsp;&emsp;    : ${settings.getBaseUrl()}/plugins/servlet/configureplugin<br>
								Method : POST<br>
                                Request Parameters :<br>
                                  <ul>
								  Headers :-
								  <div style="margin-left: 35px;">
                                  <li>
                                     Content-Type : application/json
                                  </li>
                                  <li>
                                     Authorization : Basic &lt;base64 encoded admin_username:password&gt;
                                  </li>
								  </div>
                                  </ul>
                                <ul>
								Body :-
								<div style="margin-left: 35px;">
                                <li>
                                    The body content type should be in Raw format(application/json).
                                </li>
                                <li>
                                    Paste JSON raw data you receive in response to your GET request or download raw data from the link above in the Download app Configurations.
                                </li>
								</div>
                                </ul>
							</div>



        		</span>
        </div>
</div>
<#include "*/footer.ftl" parse=true>
</body>
</html>