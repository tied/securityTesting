(function ($) {

	AJS.$(".aui-nav li").removeClass("aui-nav-selected");
	AJS.$("#mo-custom-certs").addClass("aui-nav-selected");
    AJS.$("#mo-saml").addClass("aui-nav-selected");

    var pathname = window.location.pathname;

	if (pathname.indexOf("/admin") > -1)  {
		//renderNotification();
	}

     AJS.$(document).on('click', '#revert-old-configured-cert', function () {
        		AJS.$.ajax({
        			url: AJS.contextPath() + "/plugins/servlet/saml/mogencert",
        			data: {
        				"action": "revertoldconfiguredcert"
        			},
        			type: "POST",
        			error: function (response) {
        				var a = document.getElementsByClassName("aui-flag");
        				for (var i = a.length - 1; i >= 0; i--) {
        					a[i].remove();
        				}
        				AJS.flag({
        					title: 'Error',
        					type: 'error',
        					close: 'auto',
        					body: '<p>Please re-try something went wrong.</p>'
        				});
        			},
        			success: function (response) {
        				if (response.status === true) {
        					var a = document.getElementsByClassName("aui-flag");
        					for (var i = a.length - 1; i >= 0; i--) {
        						a[i].remove();
        					}
                            var url = window.location.href;
                            if ((url.indexOf('?') > -1) && (url.indexOf('operation=updated') === -1)){
                                url += '&operation=updated'
                            }else if(url.indexOf('operation=updated') === -1){
                                url += '?operation=updated'
                            }
                            window.location.href = url;
        				}
        			}


        		});

        	});
	//Show the dialog

	AJS.$(document).on('click', '#dialog-show-button-cert', function () {
		AJS.dialog2("#newcert-dialog").show();
	});



	AJS.$(document).on('click', '#dialog-submit-button-cert', function () {
		AJS.dialog2("#newcert-dialog").hide();
	});


	AJS.$(document).on('click', '#dialog-close-button-cert', function () {
    	 location.reload(false);
    });


    AJS.$(document).on('click', '#generate-cert', function () {
        	    AJS.flag({
                        title: 'Operation running',
                        type: 'info',
                        close: 'never',
                        body: '<p>Please wait. Generating certificates....</p><p><aui-spinner size="large"></aui-spinner></p>'
                    });
        		AJS.$.ajax({
        			url: AJS.contextPath() + "/plugins/servlet/saml/mogencert?emailAdsress=" + encodeURIComponent(AJS.$("#emailAdsress").val()) + "&companyName=" + encodeURIComponent(AJS.$("#companyName").val()) + "&orgUnit=" + encodeURIComponent(AJS.$("#orgUnit").val()) + "&locationName=" + encodeURIComponent(AJS.$("#locationName").val()) + "&countryCode=" + encodeURIComponent(AJS.$("#countryCode").val()) + "&validityDays=" + encodeURIComponent(AJS.$("#validityDays").val()),
        			data: {
        				"action": "gennewcert"
        			},
        			type: "POST",
        			error: function (response) {
        			    var a = document.getElementsByClassName("aui-flag");
                                    for (var i=a.length-1;i>=0;i--) {
                                        a[i].remove();
                         }
        				AJS.flag({
        					title: 'Error',
        					type: 'error',
        					close: 'auto',
        					body: '<p>Something went wrong, please fill all the details.</p>'
        				});
        			},
        			success: function (response) {
        				if (response.resultStatus === true) {
        					var a = document.getElementsByClassName("aui-flag");
        					for (var i = a.length - 1; i >= 0; i--) {
        						a[i].remove();
        					}
                            AJS.flag({
                                title: 'Success',
                                type: 'success',
                                close: 'auto',
                                body: '<p>Certificates generated successfully. Please update the public key in your IDP.</p>'
                            });
        				}
        			}


        		});

    });

	AJS.$(document).on('click', '#revert-new-cert', function () {
		AJS.$.ajax({
			url: AJS.contextPath() + "/plugins/servlet/saml/mogencert",
			data: {
				"action": "revertnewcert"
			},
			type: "POST",
			error: function (response) {
				var a = document.getElementsByClassName("aui-flag");
				for (var i = a.length - 1; i >= 0; i--) {
					a[i].remove();
				}
				AJS.flag({
					title: 'Error',
					type: 'error',
					close: 'auto',
					body: '<p>Please re-try something went wrong.</p>'
				});
			},
			success: function (response) {
				if (response.status === true) {
					var a = document.getElementsByClassName("aui-flag");
					for (var i = a.length - 1; i >= 0; i--) {
						a[i].remove();
					}
                    var url = window.location.href;
                    if ((url.indexOf('?') > -1) && (url.indexOf('operation=updated') === -1)){
                        url += '&operation=updated'
                    }else if(url.indexOf('operation=updated') === -1){
                        url += '?operation=updated'
                    }
                    window.location.href = url;
				}
			}


		});

	});


    AJS.$(document).on('click', '#replace-new-cert', function () {
		AJS.$.ajax({
			url: AJS.contextPath() + "/plugins/servlet/saml/mogencert",
			data: {
				"action": "revertnewcert"
			},
			type: "POST",
			error: function (response) {
				var a = document.getElementsByClassName("aui-flag");
				for (var i = a.length - 1; i >= 0; i--) {
					a[i].remove();
				}
				AJS.flag({
					title: 'Error',
					type: 'error',
					close: 'auto',
					body: '<p>Please re-try something went wrong.</p>'
				});
			},
			success: function (response) {
				if (response.status === true) {
					var a = document.getElementsByClassName("aui-flag");
					for (var i = a.length - 1; i >= 0; i--) {
						a[i].remove();
					}
                    var url = window.location.href;
                    if ((url.indexOf('?') > -1) && (url.indexOf('operation=updated') === -1)){
                        url += '&operation=updated'
                    }else if(url.indexOf('operation=updated') === -1){
                        url += '?operation=updated'
                    }
                    window.location.href = url;
				}
			}


		});

	});

    AJS.$(document).on('click', '#revert-old-cert', function () {
    		AJS.$.ajax({
    			url: AJS.contextPath() + "/plugins/servlet/saml/mogencert",
    			data: {
    				"action": "revertoldcert"
    			},
    			type: "POST",
    			error: function (response) {
    				var a = document.getElementsByClassName("aui-flag");
    				for (var i = a.length - 1; i >= 0; i--) {
    					a[i].remove();
    				}
    				AJS.flag({
    					title: 'Error',
    					type: 'error',
    					close: 'auto',
    					body: '<p>Please re-try something went wrong.</p>'
    				});
    			},
    			success: function (response) {
    				if (response.status === true) {
    					var a = document.getElementsByClassName("aui-flag");
    					for (var i = a.length - 1; i >= 0; i--) {
    						a[i].remove();
    					}
                        var url = window.location.href;
                        if ((url.indexOf('?') > -1) && (url.indexOf('operation=updated') === -1)){
                            url += '&operation=updated'
                        }else if(url.indexOf('operation=updated') === -1){
                            url += '?operation=updated'
                        }
                        window.location.href = url;
    				}
    			}


    		});

    	});




	function renderNotification() {
		AJS.$.ajax({
			url: AJS.contextPath() + "/plugins/servlet/saml/mogencert",
			type: "POST",
			data: {
				"action": "getdays"
			},
			error: function () {},
			success: function (response) {
			var	expireIn = response.days;
				if (expireIn < 60) {
					AJS.flag({
						title: 'Attention!',
						type: 'warning',
						body: '<p>miniOrange SAML SSO certificates are due to expire in ' + expireIn + ' days. Kindly visit plugin settings and update certificates.</p>'
					});
				}
			}
		});

	}
})(AJS.$ || jQuery);

