<section id="newcert-dialog" class="aui-dialog2 aui-dialog2-small aui-layer" role="dialog" aria-hidden="true" style="height: auto;width: 50%">
    <header class="aui-dialog2-header">
        <h2 class="aui-dialog2-header-main">Enter details to sign Key Pairs</h2>
        <a id="dialog-close-button-cert" class="aui-dialog2-header-close">
            <span class="aui-icon aui-icon-small aui-iconfont-close-dialog">Close</span>
        </a>
    </header>
    <div class="aui-dialog2-content" style="height: auto;" >
                  <form id="gen-certificates-form" class="aui" action="" method="POST" style="top: -20px">
					<div class="field-group">
                        <label for="emailAdsress">Email Address:</label>
                        <input type="text" id="emailAdsress" name="emailAdsress"
                           value="${emailAdsress}" class="text long-field"/>
                        <div class="description">Enter the Email address.</div>
                     </div>
                     <div class="field-group">
                        <label for="companyName">Company Name (CN):</label>
                        <input type="text" id="companyName" name="companyName"
                           value="${companyName}" class="text long-field"/>
                        <div class="description">Enter the Company Name.</div>
                     </div>
					 <div class="field-group">
                        <label for="orgUnit">Organisation Unit (OU):</label>
                        <input type="text" id="orgUnit" name="orgUnit"
                           value="${orgUnit}" class="text long-field"/>
                        <div class="description">Enter the Organisation unit name.</div>
                     </div>
                     <div class="field-group">
                        <label for="locationName">Location (City):</label>
                        <input type="text" id="locationName" name="loactionName"
                           value="${locationName}" class="text long-field"/>
                        <div class="description">Enter the location. For example "London", "New York"</div>
                     </div>
                     <div class="field-group">
                        <label for="countryCode">Country Code:</label>
                        <input type="text" id="countryCode" name="countryCode"
                           value="${countryCode}" class="text long-field"/>
                        <div class="description">Enter your Country Code. For example "IN", "GB"</div>
                     </div>
                     <div class="field-group">
                        <label for="validityDays">Certificate Validity (Days):</label>
                        <input type="text" id="validityDays" name="validityDays"
                           value="${validityDays}" class="text long-field"/>
                        <div class="description">Enter the value in days for the Certificate validity</div>
                     </div>
                     <div class="field-group">
                        <input id="generate-cert" type="button" class="aui-button aui-button-primary" value="Generate New Certificates"  >
                     </div>
                  </form>
            </div>
</section>