<html>
    <head>
        <meta name="decorator" content="atl.general"/>
        <content tag="show-main-header">false</content>
    	<content tag="show-main-container">false</content>
    	<script>
	        $(document).ready(function() {
	            $(".aui-nav li").removeClass("aui-nav-selected");
	            $("#DOWNLOADSETTING").addClass("aui-nav-selected");
	        });
    	</script>
    	<style>
    		.aui-page-panel-content{
				padding: 0px !important;
			}

			.aui-page-panel{
				margin: 0px !important;
			}
    	</style>
    	
     </head>
    <body>
       <#include "*/header.ftl" parse=true>
            <div class="tabs-pane active-pane" id="account"  role="tabpanel" style="min-height: 200px;">
                <p style="font-size:13pt;">Account Info</p>
                <hr class="header"/>
                <table class="aui aui-table-interactive">
                    <tbody>
                        <tr>
                            <td headers="name">
                                <b>Allowed No of Remote Agents</b>
                            </td>
                            <#if (settings.getMaxRemoteAgentsCount() > 0)  >
                            	<td headers="type">${settings.getMaxRemoteAgentsCount()}</td>
                            <#elseif settings.getMaxRemoteAgentsCount()==0 >
                            	<td headers="type">Unlimited Remote Agents</td>
                            <#else>
                            	<td headers="type">NA</td>
                            </#if>

                        </tr>
                        <tr>
                            <td headers="name">
                                <b>No of Active Remote Agents</b>
                            </td>
                            <td headers="type">${settings.getActiveRemoteAgents()}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
		</div>
	   </section>
	  </div>
	 </div>
    </body>
</html>