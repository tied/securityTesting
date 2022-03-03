package com.miniorange.oauth.bamboo.action;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.miniorange.oauth.MoOAuthPluginException;
import com.miniorange.oauth.bamboo.MoOAuthPluginConstants;
import com.miniorange.oauth.bamboo.MoOAuthSettings;
import com.miniorange.oauth.bamboo.MoOAuthPluginHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.apache.struts2.ServletActionContext;
import com.atlassian.user.Group;
import com.atlassian.user.search.page.Pager;
import com.atlassian.bamboo.user.BambooUserManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class MoOAuthGroupMappingAction extends BambooActionSupport {

    private static final Log LOGGER = LogFactory.getLog(MoOAuthGroupMappingAction.class);

    private MoOAuthSettings settings;
    private PluginLicenseManager pluginLicenseManager;
    
    private String defaultGroup;
    private ArrayList<String> existingGroups;
    private Boolean roleMappingSubmitted;
    private String roleAttribute;
	private HashMap<String, String> roleMapping;
	private Boolean createUsersIfRoleMapped;
	private Boolean restrictUserCreation;
	private Boolean keepExistingUserRoles;
	private String xsrfToken;
	private String[] defaultGroups;
	private List<String> defaultGroupsList;
	private String enableDefaultGroupsFor;
	private Boolean onTheFlyGroupCreation;
	private String[] onTheFlyDoNotRemoveGroups;
	private List<String> onTheFlyDoNotRemoveGroupsList;
	private Boolean onTheFlyAssignNewGroupsOnly;
	private Boolean onTheFlyCreateNewGroups;
	private String lowerBuild;
	private String onTheFlyFilterIDPGroupsOption;
	private String onTheFlyFilterIDPGroupsKey;

	//Group regex
	private Boolean groupRegexPatternEnabled = false;
	private String regexPatternForGroup;
	private String regexGroups;
	private String testRegex;

	private UserManager userManager;
	private LoginUriProvider loginUriProvider;
	private BambooUserManager bambooUserManager;
    
    public MoOAuthGroupMappingAction(MoOAuthSettings settings, 
    		PluginLicenseManager pluginLicenseManager,UserManager userManager, LoginUriProvider loginUriProvider,BambooUserManager bambooUserManager){
        this.settings = settings;
        this.pluginLicenseManager=pluginLicenseManager;
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.bambooUserManager = bambooUserManager;
    }

	public void validate() {
        LOGGER.info("Role Mapping Configuration Action validate called");
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		if (!BooleanUtils.toBoolean(this.roleMappingSubmitted)) {
            return;
        }

		if (super.hasActionErrors()) {
			xsrfToken = XsrfTokenUtils.getXsrfToken(request);
			initializeOAuthConfig();
		}
		super.validate();
	}

	public Boolean doValidate() {
		LOGGER.info("Role Mapping Configuration Action doValidate called");
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		Boolean isError = Boolean.FALSE;
		if (!BooleanUtils.toBoolean(roleMappingSubmitted)) {
			return isError;
		}

		List<String> defaultGroupsList = new ArrayList<>();
		if (defaultGroups != null) {
			if (StringUtils.equalsIgnoreCase("true", lowerBuild)) {
				defaultGroupsList = new ArrayList<String>(Arrays.asList(defaultGroups));
			} else {
				defaultGroupsList = Arrays.asList(StringUtils.split(defaultGroups[0], ","));
			}
			defaultGroupsList.removeAll(Arrays.asList(null, ""));
		}
		if (defaultGroup == null && defaultGroupsList.size() == 0) {
			LOGGER.error("Default group is blank. Please select atleast one default group.");
			addActionError("Please select atleast one default group.");
			isError = Boolean.TRUE;
		}

		if (onTheFlyGroupCreation && StringUtils.isBlank(roleAttribute)) {
			LOGGER.error("Group Attribute can't be blank when On The Fly Group Mapping is enabled.");
			addActionError("Group Attribute is Required if On the Fly Group Mapping is enabled.");
			isError = Boolean.TRUE;
		}

		if(StringUtils.equals(onTheFlyFilterIDPGroupsOption, MoOAuthPluginConstants.ON_THE_FLY_FILTER_GROUPS_WITH_REGEX)){
			if(!isRegexValid(onTheFlyFilterIDPGroupsKey)){
				LOGGER.error("Group regex pattern is invalid");
				addActionError("Invalid regex pattern.");
				isError = Boolean.TRUE;
			}
		}

		if(!StringUtils.equals(onTheFlyFilterIDPGroupsOption, MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER)){
			if(StringUtils.isBlank(onTheFlyFilterIDPGroupsKey)){
				onTheFlyFilterIDPGroupsOption = MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER;
			}
		}

		if (BooleanUtils.toBoolean(isError)) {
			xsrfToken = XsrfTokenUtils.getXsrfToken(request);
			initializeOAuthConfig();
		}
		return isError;
	}

	public String execute() throws Exception {
        LOGGER.info("Role Mapping Configuration Action doExecute called");
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		lowerBuild = getLowerBuild();

		final UserProfile user = userManager.getRemoteUser();
		if (user != null && userManager.isAdmin(user.getUserKey())) {
			try {
				LOGGER.debug("roleMappingSubmitted : "+ roleMappingSubmitted);
				if (!settings.isLicenseValid()) {
					try {
						response.sendRedirect(settings.getManageAddOnURL());
						return null;
					} catch (IOException e) {
						e.printStackTrace();

					}
				}
				xsrfToken = XsrfTokenUtils.getXsrfToken(request);
				if ((this.roleMappingSubmitted != null) && !BooleanUtils.toBoolean(doValidate())){

					HashMap<String, String> roleMapping = new HashMap<>();
					int totalNumberOfRoles = Integer.parseInt(request.getParameter("totalNumberOfRoles"));

					for (int i=0;i<totalNumberOfRoles;i++) {
						String key = StringUtils.trimToEmpty(request.getParameter("userGroupKey_" + i));
						String value = StringUtils.trimToEmpty(request.getParameter("userGroupValue_" + i));

						if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value) && !StringUtils.equalsIgnoreCase("select-group-to-map", key)) {
							if (!BooleanUtils.toBoolean(roleMapping.containsKey(key))) {
								roleMapping.put(key.trim(), value.trim());
							}
						}
					}

					List<String> defaultGroupsList = new ArrayList<String>();
					if (defaultGroup != null) {
						defaultGroupsList.add(defaultGroup);
					}
					if (defaultGroups != null) {
						if (StringUtils.equalsIgnoreCase("true", lowerBuild)) {
							defaultGroupsList = new ArrayList<String>(Arrays.asList(defaultGroups));
						} else {
							defaultGroupsList = Arrays.asList(StringUtils.split(defaultGroups[0], ","));
						}
						defaultGroupsList.removeAll(Arrays.asList(null, ""));
					}

					if (onTheFlyDoNotRemoveGroups != null) {
						if (StringUtils.equalsIgnoreCase("true", lowerBuild)) {
							onTheFlyDoNotRemoveGroupsList = new ArrayList<String>(Arrays.asList(onTheFlyDoNotRemoveGroups));
						} else {
							onTheFlyDoNotRemoveGroupsList = (Arrays.asList(StringUtils.split(onTheFlyDoNotRemoveGroups[0], ",")));
						}
						onTheFlyDoNotRemoveGroupsList.removeAll(Arrays.asList(null, ""));
					}

					MoOAuthPluginHandler.saveRoleMapping(roleAttribute, createUsersIfRoleMapped, keepExistingUserRoles, roleMapping,
							defaultGroup, defaultGroupsList, restrictUserCreation, enableDefaultGroupsFor, onTheFlyGroupCreation,
							onTheFlyDoNotRemoveGroupsList, onTheFlyAssignNewGroupsOnly, onTheFlyCreateNewGroups, onTheFlyFilterIDPGroupsOption, onTheFlyFilterIDPGroupsKey,
							groupRegexPatternEnabled,regexPatternForGroup,regexGroups,testRegex);
					LOGGER.info("roleMapping saved");
					addActionMessage(getText("oauth.success.config"));
				}
				initializeOAuthConfig();
				return "success";
			} catch (MoOAuthPluginException e) {
				e.printStackTrace();
				LOGGER.error("error is : " + e);
				addActionError(e.getMessage());
				return "input";
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("error is : " + e);
				addActionError("An error occurred.");
				return "input";
			}
		}else{
			response.sendRedirect(loginUriProvider.getLoginUri(MoOAuthPluginHandler.getUri(request)).toASCIIString());
			return null;
		}

    }

    public ArrayList<String> getExistingGroups() {
		try {
			Pager groupObjects = bambooUserManager.getGroups();
			this.existingGroups = new ArrayList<String>();
			Iterator<Group> itr = groupObjects.iterator();
			while (itr.hasNext()) {
				Group group = itr.next();
				this.existingGroups.add(group.getName());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.existingGroups;
    }

    private void initializeOAuthConfig() {
		LOGGER.info("initialize OAuth Config called");
        this.defaultGroup = settings.getDefaultGroup();
		this.keepExistingUserRoles = settings.getKeepExistingUserRoles();
		this.roleMapping = settings.getRoleMapping();
		this.roleAttribute = settings.getRoleAttribute();
		this.createUsersIfRoleMapped = settings.getCreateUsersIfRoleMapped();
		this.restrictUserCreation = settings.getRestrictUserCreation();
		this.defaultGroupsList = settings.getDefaultGroupsList();
		this.enableDefaultGroupsFor = settings.getEnableDefaultGroupsFor();
		this.onTheFlyGroupCreation = settings.getOnTheFlyGroupMapping();
		this.onTheFlyDoNotRemoveGroupsList = (List<String>) settings.getOnTheFlyDoNotRemoveGroups();
		this.onTheFlyAssignNewGroupsOnly = settings.getOnTheFlyAssignNewGroupsOnly();
		this.onTheFlyCreateNewGroups = settings.getOnTheFlyCreateNewGroups();
		this.lowerBuild = getLowerBuild();
		this.onTheFlyFilterIDPGroupsOption = settings.getOnTheFlyFilterIDPGroupsOption();
		this.onTheFlyFilterIDPGroupsKey = settings.getOnTheFlyFilterIDPGroupsKey();

		//Group regex
		this.groupRegexPatternEnabled=BooleanUtils.toBooleanDefaultIfNull(settings.getGroupRegexPatternEnabled(),false);
		this.regexPatternForGroup=StringUtils.defaultIfBlank(settings.getRegexPatternForGroup(),StringUtils.EMPTY);
		this.regexGroups=StringUtils.defaultString(settings.getRegexGroups(),StringUtils.EMPTY);
		this.testRegex=StringUtils.defaultIfBlank(settings.getTestRegex(),StringUtils.EMPTY);
	}

	private Boolean isRegexValid(String regex) {
		try {
			Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

    public PluginLicenseManager getPluginLicenseManager() {
		return pluginLicenseManager;
	}

	public void setPluginLicenseManager(PluginLicenseManager pluginLicenseManager) {
		this.pluginLicenseManager = pluginLicenseManager;
	}

    public MoOAuthSettings getSettings() {
        return settings;
    }

    public void setSettings(MoOAuthSettings settings) {
        this.settings = settings;
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public void setExistingGroups(ArrayList existingGroups) {
        this.existingGroups = existingGroups;
    }

    public Boolean getRoleMappingSubmitted() {
        return roleMappingSubmitted;
    }

    public void setRoleMappingSubmitted(Boolean roleMappingSubmitted) {
        this.roleMappingSubmitted = roleMappingSubmitted;
    }

	public String getRoleAttribute() {
		return roleAttribute;
	}

	public void setRoleAttribute(String roleAttribute) {
		this.roleAttribute = roleAttribute;
	}

	public String getOnTheFlyFilterIDPGroupsOption() {
		return onTheFlyFilterIDPGroupsOption;
	}

	public void setOnTheFlyFilterIDPGroupsOption(String onTheFlyFilterIDPGroupsOption) {
		this.onTheFlyFilterIDPGroupsOption = onTheFlyFilterIDPGroupsOption;
	}

	public String getOnTheFlyFilterIDPGroupsKey() {
		return onTheFlyFilterIDPGroupsKey;
	}

	public void setOnTheFlyFilterIDPGroupsKey(String onTheFlyFilterIDPGroupsKey) {
		this.onTheFlyFilterIDPGroupsKey = onTheFlyFilterIDPGroupsKey;
	}

	public List<String> getGroupFilterOptionsList(){
    	List<String> filteredGroups = new ArrayList<>();
    	filteredGroups.add(MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER);
		filteredGroups.add(MoOAuthPluginConstants.ON_THE_FLY_FILTER_GROUPS_STARTS_WITH);
		filteredGroups.add(MoOAuthPluginConstants.ON_THE_FLY_FILTER_GROUPS_CONTAINS);
		filteredGroups.add(MoOAuthPluginConstants.ON_THE_FLY_FILTER_GROUPS_WITH_REGEX);

		return filteredGroups;
	}

	public String[] getDefaultGroups() {
		return defaultGroups;
	}

	public void setDefaultGroups(String[] defaultGroups) {
		this.defaultGroups = defaultGroups;
	}

	public List<String> getDefaultGroupsList() {
		return defaultGroupsList;
	}

	public void setDefaultGroupsList(List<String> defaultGroupsList) {
		this.defaultGroupsList = defaultGroupsList;
	}

	public String getEnableDefaultGroupsFor() {
		return enableDefaultGroupsFor;
	}

	public void setEnableDefaultGroupsFor(String enableDefaultGroupsFor) {
		this.enableDefaultGroupsFor = enableDefaultGroupsFor;
	}

	public Boolean getOnTheFlyGroupCreation() {
		return onTheFlyGroupCreation;
	}

	public void setOnTheFlyGroupCreation(Boolean onTheFlyGroupCreation) {
		this.onTheFlyGroupCreation = onTheFlyGroupCreation;
	}

	public String[] getOnTheFlyDoNotRemoveGroups() {
		return onTheFlyDoNotRemoveGroups;
	}

	public void setOnTheFlyDoNotRemoveGroups(String[] onTheFlyDoNotRemoveGroups) {
		this.onTheFlyDoNotRemoveGroups = onTheFlyDoNotRemoveGroups;
	}

	public List<String> getOnTheFlyDoNotRemoveGroupsList() {
		return onTheFlyDoNotRemoveGroupsList;
	}

	public void setOnTheFlyDoNotRemoveGroupsList(List<String> onTheFlyDoNotRemoveGroupsList) {
		this.onTheFlyDoNotRemoveGroupsList = onTheFlyDoNotRemoveGroupsList;
	}

	public Boolean getOnTheFlyAssignNewGroupsOnly() {
		return onTheFlyAssignNewGroupsOnly;
	}

	public void setOnTheFlyAssignNewGroupsOnly(Boolean onTheFlyAssignNewGroupsOnly) {
		this.onTheFlyAssignNewGroupsOnly = onTheFlyAssignNewGroupsOnly;
	}

	public Boolean getOnTheFlyCreateNewGroups() {
		return onTheFlyCreateNewGroups;
	}

	public void setOnTheFlyCreateNewGroups(Boolean onTheFlyCreateNewGroups) {
		this.onTheFlyCreateNewGroups = onTheFlyCreateNewGroups;
	}

	public HashMap<String, String> getRoleMapping() {
		return roleMapping;
	}

	public void setRoleMapping(HashMap<String, String> roleMapping) {
		this.roleMapping = roleMapping;
	}

	public Boolean getCreateUsersIfRoleMapped() {
		return createUsersIfRoleMapped;
	}

	public void setCreateUsersIfRoleMapped(Boolean createUsersIfRoleMapped) {
		this.createUsersIfRoleMapped = createUsersIfRoleMapped;
	}

	public Boolean getKeepExistingUserRoles() {
		return keepExistingUserRoles;
	}

	public void setKeepExistingUserRoles(Boolean keepExistingUserRoles) {
		this.keepExistingUserRoles = keepExistingUserRoles;
	}

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}

	public Boolean getRestrictUserCreation() {
		return restrictUserCreation;
	}

	public void setRestrictUserCreation(Boolean restrictUserCreation) {
		this.restrictUserCreation = restrictUserCreation;
	}

	public String getLowerBuild() {
    	this.lowerBuild = Boolean.toString(settings.isLowerBuild());
		return this.lowerBuild;
	}

	public void setLowerBuild(String lowerBuild) {
		this.lowerBuild = lowerBuild;
	}

	public Boolean getGroupRegexPatternEnabled() {
		return groupRegexPatternEnabled;
	}

	public void setGroupRegexPatternEnabled(Boolean groupRegexPatternEnabled) {
		this.groupRegexPatternEnabled = groupRegexPatternEnabled;
	}

	public String getRegexPatternForGroup() {
		return regexPatternForGroup;
	}

	public void setRegexPatternForGroup(String regexPatternForGroup) {
		this.regexPatternForGroup = regexPatternForGroup;
	}

	public String getRegexGroups() {
		return regexGroups;
	}

	public void setRegexGroups(String regexGroups) {
		this.regexGroups = regexGroups;
	}

	public String getTestRegex() {
		return testRegex;
	}

	public void setTestRegex(String testRegex) {
		this.testRegex = testRegex;
	}

}
