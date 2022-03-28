AJS.$(function () {
    AJS.$(".aui-nav li").removeClass("aui-nav-selected");
    AJS.$("#mo-redirection-rules").addClass("aui-nav-selected");
    AJS.$("#mo-saml").addClass("aui-nav-selected");
    AJS.$("#reveal-text-trigger").trigger("aui-expander-expand");
    window.onbeforeunload=null; //To disable the Leave Site popup
    var moApi = AJS.contextPath()+"/plugins/servlet/saml/moapi";

    AJS.$(".add-rule").click(function () {
        AJS.$("#redirection-rules").hide();
        AJS.$("#create-rule").show();
        AJS.$("#rule-window-title").html("Add Rule");
        AJS.$("#ruleName").removeAttr("readonly");
        AJS.$("#formaction").prop("name","addRedirectionRuleSubmitted");
        clearFieldValues();
    });

    AJS.$(document).on("click",".edit-rule",function () {
        var id = AJS.$(this).attr('data-rule-id');
        AJS.$("#redirection-rules").hide();
        AJS.$("#create-rule").show();
        AJS.$("#rule-window-title").html("Edit Rule");
        AJS.$("#formaction").prop("name","editRedirectionRuleSubmitted");
        AJS.$("#ruleName").prop("readonly","true");

        var getRule = moApi;
        AJS.$.ajax({
            url:getRule,
            type:'GET',
            data:{
                'action':'getRule',
                'key':id
            },
            success:function(response){
                populateEditRuleForm(response);
            }
        });
    });

    function clearFieldValues(){
        var elements = document.querySelectorAll("#add-redirection-rule-form input[type=text]");
        for (var i = 0, element; element = elements[i++];) {
            element.value = "";
        }

        elements = document.querySelectorAll("#add-redirection-rule-form aui-select");
        for (var i = 0, element; element = elements[i++];) {
            element.value = "";
        }
    }

    function populateEditRuleForm(rule){
        AJS.$("#ruleID").val(rule.name);
        AJS.$("#ruleName").val(rule.name);
        var condition = rule.condition;
        var decisionFactor = condition.decisionFactor;
        AJS.$("#decisionFactor").val(decisionFactor);
        var decisionObject = condition[decisionFactor];
        var conditionOperation = decisionObject.conditionOperation;
        AJS.$("#conditionOperation").val(conditionOperation);
        var value = decisionObject[conditionOperation];
        var idp = rule.idp;
        AJS.$("#idp").val(idp);
        switch(decisionFactor){
            case 'domain':
                AJS.$('#textConditionValue').val(value);
                break;
            case 'directory':
                if(conditionOperation==="regex"){
                    console.log(value);
                    AJS.$("#directoryRegex").val(value)                               //Setting a value to derectoryregex field
                    // console.log(AJS.$("#directoryRegex").val(value));
                }else {
                    console.log("this is the directory value" + value)
                     AJS.$("#directoryList").auiSelect2('data',
                     {
                     'id': value,
                     'text': value
                     }
                     );
                }
                break;
            case 'group':
                if(conditionOperation==="regex"){
                console.log(value);
                    AJS.$("#groupRegex").val(value)
                }else{
                    console.log("under directory regex"+value);
                    AJS.$("#groupsList").auiSelect2('data',
                        {
                            'id':value,
                            'text':value
                        }
                    );
                }
                break;
        }
    }

    AJS.$("#cancel-create-rule").click(function (e) {
        AJS.$("#redirection-rules").show();
        AJS.$("#create-rule").hide();
    });

    AJS.$(document).on("click",".delete-bamboo-rule",function (e){
        var id = AJS.$(this).attr('data-rule-id');
        var deleteUrl = AJS.contextPath()+"/plugins/servlet/saml/moapi?action=deleteRule&key="+id;

        var dialog = new AJS.Dialog({
            width: 400,
            height: 150,
            id: "delete-dialog",
            closeOnOutsideClick: true
        });
        dialog.addPanel("Panel 1", "<p>Are you sure you want to delete rule "+id+"?</p>", "panel-body");

        dialog.addButton("Yes", function (dialog) {
            dialog.hide();
            AJS.$.ajax({
                url:deleteUrl,
                type:'DELETE',
                success:function(response){
                    AJS.$("#row-"+id).remove();
                    if(AJS.$(".bamboo-move-up").length===1){
                        AJS.$(".bamboo-move-up").remove();
                        AJS.$(".bamboo-move-down").remove();
                    }
                    showSuccessMessage(response);
                }
            });

        });

        dialog.addLink("No", function (dialog) {
            dialog.hide();
        }, "#");

        dialog.show();


    });



    AJS.$(document).on("change","#conditionOperation",function(){
        var decisionFactor = AJS.$('#decisionFactor').val();
        AJS.$(".condition-value").hide();
        AJS.$(".condition-value").prop("disabled",true);
        AJS.$(".condition-value").prop("required",false);
        if(this.value==="regex") {
            console.log("conditionOperationSwitched to regex" + decisionFactor);
            switch (decisionFactor) {
                case "directory":
                    AJS.$("#directoryRegex").show();
                    AJS.$("#directoryRegex").prop("disabled", false);
                    AJS.$("#directoryRegex").prop("required", true);
                    AJS.$("#directoryRegex").prop("placeholder", "Enter directory regex");
                    break;
                case "group":
                    AJS.$("#groupRegex").show();
                    AJS.$("#groupRegex").prop("disabled", false);
                    AJS.$("#groupRegex").prop("required", true);
                    AJS.$("#groupRegex").prop("placeholder", "Enter group regex");
                    break;
                default:
                    AJS.$("#textConditionValue").show();
                    AJS.$("#textConditionValue").prop("disabled", false);
                    AJS.$("#textConditionValue").prop("required", true);
            }
        }else {
         console.log("conditionOperationSwitched to equals : " + decisionFactor);

            switch (decisionFactor) {
                case "directory":
                 console.log("direactory is selected");
                    AJS.$("#directoryList").show();
                    AJS.$("#directoryList").prop("disabled", false);
                    AJS.$("#directoryList").prop("required", true);
                    initializeDirectoryListDropdown();
                    break;
                case "group":
                    AJS.$("#groupsList").show();
                    AJS.$("#groupsList").prop("disabled", false);
                    AJS.$("#groupsList").prop("required", true);
                    initializeGroupsDropdown();
                    break;
                default:
                console.log("equals default is selected");
                    AJS.$("#textConditionValue").show();
                    AJS.$("#textConditionValue").prop("disabled", false);
                    AJS.$("#textConditionValue").prop("required", true);

            }
        }
    });

    AJS.$(document).on("change", "#decisionFactor", function () {
        AJS.$(".condition-value").hide();
        AJS.$(".condition-value").prop("disabled", true);
        AJS.$(".condition-value").prop("required", false);
        console.log("this.value "+ this.value);
        switch (decisionFactor) {
            case "directory":
                AJS.$("#directoryList").show();
                AJS.$("#directoryList").prop("disabled", false);
                AJS.$("#directoryList").prop("required", true);
                initializeDirectoryListDropdown();
                break;
            case "group":
                AJS.$("#groupsList").show();
                AJS.$("#groupsList").prop("disabled", false);
                AJS.$("#groupsList").prop("required", true);
                initializeGroupsDropdown();
                break;
            default:
                AJS.$("#textConditionValue").show();
                AJS.$("#textConditionValue").prop("disabled", false);
                AJS.$("#textConditionValue").prop("required", true);

        }
    });

    function swapInputNames(row1, row2) {
        var key1 = (row1.attr('id').split('row-'))[1];
        var key2 = (row2.attr('id').split('row-'))[1];

        var ruleName = "#bamboo-rule";
        var name1 = AJS.$(ruleName + key1).attr('name');
        AJS.$(ruleName + key1).attr('name', AJS.$(ruleName + key2).attr('name'));
        AJS.$(ruleName + key2).attr('name', name1);

        //Swapping Expression Keys
        var expressionName = "#bamboo-rule-expression-";
        var expression1 = AJS.$(expressionName + key1).attr('name');
        AJS.$(expressionName + key1).attr('name', AJS.$(expressionName + key2).attr('name'));
        AJS.$(expressionName + key2).attr('name', expression1);

    }

    AJS.$(".bamboo-move-up,.bamboo-move-down").click(function () {
        var row = AJS.$(this).parents("tr:first");
        var message = AJS.$("#bamboo-order-saved");
        var html = "<button id=\"save-bamboo-order\" class=\"aui-button aui-button-primary\" value=\"Save\" title=\"Save Order\"><span class=\"aui-icon aui-icon-small aui-iconfont-clone-small\">save-icon</span> Save</button>";
        AJS.$(html).insertAfter(message);
        AJS.$(message).remove();

        if (AJS.$(this).is(".bamboo-move-up")) {
            var prev = row.prev();
            if(!prev.attr('id'))
                return;
            AJS.$("#bamboo-saved-message").hide();
            AJS.$("#bamboo-order-spinner").show();
            swapInputNames(AJS.$(row), AJS.$(row.prev()));
            row.insertBefore(row.prev());
        } else {
            var next = row.next();
            if(!next.attr('id'))
                return;
            AJS.$("#bamboo-saved-message").hide();
            AJS.$("#bamboo-order-spinner").show();
            swapInputNames(row, row.next());
            row.insertAfter(row.next());
        }
        saveOrder();
    });

    function saveOrder() {
        AJS.$.ajax({
            url:AJS.contextPath()+ "/plugins/servlet/saml/moapi",
            type:"POST",
            data:AJS.$("#save-bamboo-order-form").serialize(),
            error:function(response){},
            success:function(response){
               setTimeout(hideSpinner,2000);
            }
        });
    }

    function hideSpinner() {
        AJS.$("#bamboo-order-spinner").hide();
        AJS.$("#bamboo-saved-message").show("show");
    }

    AJS.$(".redirection-rules-tab").click(function(evt){
       var tabName = 'rules-div';
       if(this.id==='toggles-tab')
           tabName = 'toggles-div';

        var tabcontent = document.getElementsByClassName("tabcontent");
        for (i = 0; i < tabcontent.length; i++) {
            tabcontent[i].style.display = "none";
        }
        var tablinks = document.getElementsByClassName("tablinks");
        for (i = 0; i < tablinks.length; i++) {
            tablinks[i].className = tablinks[i].className.replace(" active", "");
        }
        document.getElementById(tabName).style.display = "block";
        evt.currentTarget.className += " active ";
    });

    if (AJS.$("#defaultBambooIDP").val() === "redirectUrl") {
        if (AJS.$("#defaultBambooIDP-redirect-url").is(":hidden")) {
            AJS.$("#defaultBambooIDP-redirect-url").css("display", "contents");
        }
    } else {
        if (AJS.$("#defaultBambooIDP-redirect-url").is(":visible")) {
            AJS.$("#defaultBambooIDP-redirect-url").css("display", "none");
        }
    }

    AJS.$(document).on("change",".default-idp-select",function(e){
        if (AJS.$("#" + this.id).val() === "redirectUrl") {
            if (AJS.$("#" + this.id + "-redirect-url").is(":hidden")) {
                AJS.$("#" + this.id + "-redirect-url").css("display", "contents");
            }
            if (AJS.$(".default-redirect-url").val()){
                var value = AJS.$(".default-redirect-url").val();
                saveDefaultURL(value);
            }
        } else {
            if (AJS.$("#" + this.id + "-redirect-url").css('display') === 'contents') {
                AJS.$("#" + this.id + "-redirect-url").css("display", "none");
            }
            AJS.$.ajax({
                url: AJS.contextPath() + "/plugins/servlet/saml/moapi",
                data: {
                    "action": "saveDefaultRule",
                    "defaultIDP": AJS.$("#" + this.id).val()
                },
                type: 'POST',
                error: function (response) {
                    showErrorMessage(response);
                },
                success: function (response) {
                    showSuccessMessage(response);
                }
            });
        }
    });

    AJS.$(document).on("click", ".default-redirect-url", function (e) {
        if(AJS.$("#save-redirect-url-rule").is(":hidden")){
            AJS.$("#save-redirect-url-rule").css("display","inline");
        }
    });

    AJS.$(document).on("click","#save-redirect-url-button", function (e) {
        var value = AJS.$("#defaultRedirectUrl").val();
        saveDefaultURL(value);
    });

    AJS.$(document).on("click","#cancel-redirect-url-button", function (e) {
        AJS.$("#defaultRedirectUrl").val(" ");
    });

    AJS.$(".default-redirect-url").keypress(function (e) {
        if (e.which === 13) {
            var value = AJS.$(".default-redirect-url").val();
            saveDefaultURL(value);
            return false;    //<---- Add this line
        }
    });

    function saveDefaultURL(value){
        AJS.$.ajax({
            url: AJS.contextPath() + "/plugins/servlet/saml/moapi",
            data: {
                "action": "saveDefaultRedirectURL",
                "defaultRedirectURL": value
            },
            async: false,
            type: 'POST',
            error: function (response) {
                showErrorMessage(response);
            },
            success: function (response) {
                showSuccessMessage(response);
                if(AJS.$("#save-redirect-url-rule").is(":visible")){
                    AJS.$("#save-redirect-url-rule").css("display","none");
                }
            }
        });
    }

    function initializeGroupsDropdown() {
        AJS.$("#groupsList").auiSelect2({
            placeholder: 'Select the groups to apply this rule',
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

            }
        });

    }
        function initializeDirectoryListDropdown() {
                AJS.$("#directoryList").auiSelect2({
                    placeholder: 'Select the Directory List',
                    ajax: {
                        url: AJS.contextPath() + '/plugins/servlet/saml/moapi',
                        data: function (params) {
                            var query = {
                                search: params,
                                action: 'fetchDirectory'
                            }
                            // Query parameters will be ?search=[term]&type=public
                            return query;
                        },
                        results: function (data, page) {
                            return {
                                results: data.results
                            };
                        },

                    }
                });

            }

        AJS.$(document).on("change", "#conditionOperation", function () {
            if (this.value==="regex"){
                AJS.$("#regex-info").show();
            } else{
                AJS.$("#regex-info").hide();
            }
        });

    if ( window.history.replaceState ) {
          window.history.replaceState( null, null, window.location.href );
        }

});