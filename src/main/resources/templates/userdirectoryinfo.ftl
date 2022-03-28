<html>
<head>
    <meta name="decorator" content="atl.general">

    <script>
       AJS.$(document).ready(function() {
           AJS.$(".aui-nav li").removeClass("aui-nav-selected");
           AJS.$("#userdirectoryinfo").addClass("aui-nav-selected");
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

    <div class="tabs-pane active-pane" id="user-directory-info" role="tabpanel" >
        <p><strong>If any external user directory such as LDAP is configured in Bamboo, the behaviour of Oauth SSO will
                           change according to the user directory permission.
                           <br>
                           Here are the behavioural changes listed for each directory permission type:</strong>
        </p>
        <hr class="header">
        <h3>Read Only</h3>
        <p>
            <ol>
                <li><strong>User Creation: </strong>
                    <ul>
                        <li>New users won't be created during SSO. If any new user tries to perform SSO, this error message will be shown to users: "We couldn't sign you in. Please contact Administrator"</li>
                    </ul></li>
                <li><strong>User Profiles Mapping:</strong>
                    <ul>
                        <li>The profile of users won't be updated</li>
                        <li>It is recommended to check <b>Disable Attribute Mapping</b> option in User
                            Profile tab.
                        </li>
                    </ul>
                </li>
                <li><strong>User Group Mapping:</strong>
                    <ul>
                        <li>The groups of users won't be updated</li>
                        <li>It is recommended to check <b>Disable Group Mapping</b> option in User Groups tab</li>
                        <li>If you're using On-The-Fly group mapping, new groups won't be created and only existing groups returned by Oauth Provider will be mapped. Make sure that all the groups in Oauth Provider Response are present in Bamboo or synced from the external directory, otherwise the SSO will fail.</li>
                    </ul>
                </li>

                <li><strong>Default Groups:</strong>
                    <ul>
                        <li>The default groups won't be assigned to any user</li>
                    </ul>
                </li>
            </ol>
        </p>
        <hr class="header">
        <h3> Read Only With Local Groups</h3>
            <ol>
                <li><strong>User Creation: </strong>
                    <ul>
                        <li>New users won't be created during SSO. If any user tries to perform SSO, this error message will be shown to the user: "We couldn't sign you in. Please contact Administrator"</li>
                    </ul></li>
                <li><strong>User Profiles Mapping:</strong>
                    <ul>
                        <li>The profile and groups of users wii not be updated</li>
                        <li>It is recommended to check <b>Disable Attribute Mapping</b> option in User
                            Profile tab.
                        </li>
                    </ul>
                </li>
                <li><strong>User Group Mapping:</strong>
                    <ul>
                        <li>Users can be added to or removed from local Bamboo groups</li>
                        <li>If you're using On-The-Fly group mapping, the new groups will be created only if the Bamboo's internal directory is primary user directory</li>
                    </ul>
                </li>

                <li><strong>Default Groups:</strong>
                    <ul>
                        <li>Local Bamboo groups can be assigned as default groups to all users. As all users are treated as existing users, it is recommended to Change <strong>Assign Default Group To</strong> settings to <strong>All</strong></li>
                    </ul>
                </li>
            </ol>

        <p>If you are looking for a different kind of behaviour for any User Directory Permission, please let us know.</p>

    </div>

    </div>
    </section>
    </div>
    </div>
</body>
</html>