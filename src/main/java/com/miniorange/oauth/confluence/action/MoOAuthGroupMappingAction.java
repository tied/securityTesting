package com.miniorange.oauth.confluence.action;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.xwork.SimpleXsrfTokenGenerator;
import com.atlassian.xwork.XsrfTokenGenerator;
import com.miniorange.oauth.confluence.MoOAuthPluginConstants;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.user.Group;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.miniorange.oauth.MoOAuthPluginException;
import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import com.miniorange.oauth.confluence.MoOAuthSettings;
import com.miniorange.oauth.confluence.action.MoOAuthGroupMappingAction;
import com.opensymphony.webwork.ServletActionContext;

public class MoOAuthGroupMappingAction extends ConfluenceActionSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(MoOAuthGroupMappingAction.class);

	private MoOAuthSettings settings;
	private CrowdService crowdService;
	private PluginLicenseManager pluginLicenseManager;

	private String defaultGroup;
	private String enableDefaultGroupsFor;
    private ArrayList existingGroups;
    private Boolean roleMappingSubmitted;

    private String roleAttribute;
	private String[] defaultGroups;
	private List<String> defaultGroupsList;
	private Map<String, String> roleMapping;

	private Boolean createUsersIfRoleMapped;
	private Boolean keepExistingUserRoles;
	private Boolean restrictUserCreation;
    
	private Boolean onTheFlyGroupCreation;
	private Boolean createNewGroups;

	private String onTheFlyFilterIDPGroupsOption;
	private String onTheFlyFilterIDPGroupsKey;
	private Boolean onTheFlyAssignNewGroupsOnly;
	private String[] onTheFlyDoNotRemoveGroups;
	private List<String> onTheFlyDoNotRemoveGroupsList;

	private Boolean groupRegexPatternEnabled;
	private String regexGroups;
	private String regexPatternForGroup;
	private String testRegex;

	private MoOAuthPluginHandler pluginHandler;
	private String groupRegexPattern;
	private String xsrfToken;
	
	public MoOAuthGroupMappingAction(MoOAuthSettings settings, MoOAuthPluginHandler pluginHandler,
    		PluginLicenseManager pluginLicenseManager, CrowdService crowdService){
        this.settings = settings;
        this.pluginHandler = pluginHandler;
        this.pluginLicenseManager=pluginLicenseManager;
        this.crowdService = crowdService;
    }
    
	public Boolean validation() {
        LOGGER.debug("Role Mapping Configuration Action doValidate");
		Boolean error = false;
        if (!BooleanUtils.toBoolean(this.roleMappingSubmitted)) {
            error = true;
        }

		if ((defaultGroups != null && defaultGroups.length <= 0)) {
			LOGGER.error("Default group is blank. Please select atleast one default group.");
			error = true;
			addActionError(getText("oauth.error.config.defaultgroup.empty"));
		}

		if (BooleanUtils.toBoolean(onTheFlyGroupCreation) && StringUtils.isBlank(roleAttribute)) {
			LOGGER.error("Group Attribute can't be blank when On The Fly Group Mapping is enabled.");
			addActionError("Group Attribute can't be blank when On The Fly Group Mapping is enabled.");
			error = true;
		}

		if(StringUtils.equals(onTheFlyFilterIDPGroupsOption, MoOAuthPluginConstants.ON_THE_FLY_FILTER_GROUPS_WITH_REGEX)){
			if (!isRegexValid(onTheFlyFilterIDPGroupsKey)) {
				LOGGER.error("Regex value for group filtering is invalid.");
				addActionError("Regex pattern is invalid.");
				error = true;
			}
		}

		if(!StringUtils.equals(onTheFlyFilterIDPGroupsOption, MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER)){
			if(StringUtils.isBlank(onTheFlyFilterIDPGroupsKey)){
				onTheFlyFilterIDPGroupsOption = MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER;
			}
		}

		if (BooleanUtils.toBoolean(error)) {
			initializeOAuthConfig();
		}

		return error;
    }
	
	public String execute() throws Exception {
        LOGGER.debug("RoleMappingConfigurationAction doExecute");
        try{
        	if(!settings.isLicenseValid()){
				return "invalid";
			}

			HttpServletRequest request = ServletActionContext.getRequest();
			XsrfTokenGenerator tokenGenerator = new SimpleXsrfTokenGenerator();
			xsrfToken = tokenGenerator.generateToken(request);

			SettingsManager settingsManager = (SettingsManager) ContainerManager.getComponent("settingsManager");
			LOGGER.debug("Default Group for settings: " + settingsManager.getGlobalSettings().getDefaultUsersGroup());
            LOGGER.debug(" before roleMappingSubmitted... " + this.roleMappingSubmitted);
            if (this.roleMappingSubmitted != null) {  
            	LOGGER.debug("roleMappingSubmitted... ");
            	HashMap<String, String> roleMapping = new HashMap<>();
				List<String> defaultGroupList = new ArrayList<String>();

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
				
				if (defaultGroup != null) {
					LOGGER.debug("single default group is -" + this.defaultGroup);
					defaultGroupList.add(defaultGroup);
				}
				
				if (defaultGroups != null) {
					LOGGER.debug("multiple default groups -" + defaultGroups.length);
					defaultGroupList = new ArrayList<String>(Arrays.asList(StringUtils.split(defaultGroups[0], ",")));
					defaultGroupList.removeAll(Arrays.asList(null, ""));
				}

				LOGGER.debug("Enable groups for : " + enableDefaultGroupsFor);
				LOGGER.debug("OTF : " + onTheFlyGroupCreation);


				if (onTheFlyDoNotRemoveGroups != null) {
					LOGGER.debug("multiple Do not remove groups - " + onTheFlyDoNotRemoveGroups.length);
					onTheFlyDoNotRemoveGroupsList = new ArrayList<String>(Arrays.asList(StringUtils.split(onTheFlyDoNotRemoveGroups[0], ",")));
					onTheFlyDoNotRemoveGroupsList.removeAll(Arrays.asList(null, ""));
				} else {
					onTheFlyDoNotRemoveGroupsList = (List<String>) settings.getOnTheFlyDoNotRemoveGroups();
				}


				Boolean error;
				error = validation();
				if(!error) {

					pluginHandler.saveRoleMapping(roleAttribute, keepExistingUserRoles, createUsersIfRoleMapped,
							roleMapping, defaultGroup, defaultGroupList, restrictUserCreation, enableDefaultGroupsFor,
							onTheFlyGroupCreation,groupRegexPatternEnabled,regexPatternForGroup,regexGroups,testRegex, createNewGroups, onTheFlyAssignNewGroupsOnly, onTheFlyDoNotRemoveGroupsList,
							onTheFlyFilterIDPGroupsOption, onTheFlyFilterIDPGroupsKey);
					LOGGER.debug("roleMapping saved");
					addActionMessage(getText("oauth.success.config"), "success", true, null);
				}
            }
            initializeOAuthConfig();
            LOGGER.debug("initializeOAuthConfig called... ");
            return "success";
        } catch (MoOAuthPluginException e) {
            LOGGER.error(e.getMessage());
            addActionError(e.getMessage());
            return "input";
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.debug("error is : " + e);
			addActionError("An error occurred.");
            return "input";
        }
    }

	public ArrayList<String> getExistingGroups() {
		//	String startWith = this.groupRegexPattern;
		Integer groupCount = 50;
		if (StringUtils.equalsIgnoreCase(this.groupRegexPattern, "*")) {
			//startWith = StringUtils.EMPTY;
			groupCount = Integer.MAX_VALUE;
		}
		EntityQuery<com.atlassian.crowd.embedded.api.Group> query = QueryBuilder.queryFor(com.atlassian.crowd.embedded.api.Group.class, EntityDescriptor.group())
				.with(Restriction.on(GroupTermKeys.NAME).startingWith(StringUtils.EMPTY)).returningAtMost(groupCount);

		LOGGER.debug("query =  " + query);
		this.existingGroups = new ArrayList<String>();
		Iterable<com.atlassian.crowd.embedded.api.Group> groups = crowdService.search(query);

		LOGGER.debug("groups =  " + groups);
		this.existingGroups = new ArrayList<String>();
		for (com.atlassian.crowd.embedded.api.Group groupObject : groups) {
			LOGGER.debug("groups from Confluence=  " + groupObject.getName());
			if (!this.existingGroups.contains(groupObject.getName().trim())) {
				this.existingGroups.add(groupObject.getName().trim());
			}
		}

		List<String> defaultGroups = ((List<String>)settings.getDefaultGroups());
		if (defaultGroups != null && defaultGroups.size() > 0) {
			for (String group : defaultGroups) {
				if (!this.existingGroups.contains(group.trim())) {
					this.existingGroups.add(group.trim());
				}
			}
		} else {
			if (!this.existingGroups.contains(settings.getDefaultGroup().trim())) {
				this.existingGroups.add(settings.getDefaultGroup().trim());
			}
		}

		if (settings.getRoleMapping() != null) {
			for (String group : settings.getRoleMapping().keySet()) {
				if (!this.existingGroups.contains(group.trim())) {
					this.existingGroups.add(group.trim());
				}
			}
		}

		return this.existingGroups;
	}

    private void initializeOAuthConfig() {
        this.defaultGroup = settings.getDefaultGroup();
		LOGGER.debug("Existing Groups Config: "+this.existingGroups);
		this.keepExistingUserRoles = BooleanUtils.toBooleanDefaultIfNull(settings.getKeepExistingUserRoles(), Boolean.TRUE);
		//this.defaultGroup = idpConfig.getDefaultGroup();
		this.defaultGroupsList = (List<String>) settings.getDefaultGroups();
		this.roleMapping = new TreeMap<>(settings.getRoleMapping());
		this.roleAttribute = settings.getRoleAttribute();
		this.createUsersIfRoleMapped = settings.getCreateUsersIfRoleMapped();
		this.restrictUserCreation = settings.getRestrictUserCreation();
		this.enableDefaultGroupsFor =StringUtils.defaultIfBlank(settings.getEnableDefaultGroupsFor(),"newUsers");
		this.onTheFlyGroupCreation = BooleanUtils.toBoolean(settings.getOnTheFlyGroupMapping());
		this.groupRegexPatternEnabled = BooleanUtils.toBoolean(settings.getGroupRegexPatternEnabled());
		this.regexPatternForGroup = StringUtils.defaultIfBlank(settings.getRegexPatternForGroup(),"");
		this.regexGroups = StringUtils.defaultIfBlank(settings.getRegexGroups(),"");
		this.testRegex = StringUtils.defaultIfBlank(settings.getTestRegex(),"");
		this.createNewGroups = BooleanUtils.toBooleanDefaultIfNull(settings.getCreateNewGroups(),true);
		this.onTheFlyAssignNewGroupsOnly =	BooleanUtils.toBooleanDefaultIfNull(settings.getOnTheFlyAssignNewGroupsOnly(),true);
		//this.onTheFlyDoNotRemoveGroups = settings.getOnTheFlyDoNotRemoveGroups();
		this.onTheFlyDoNotRemoveGroupsList = (List<String>)settings.getOnTheFlyDoNotRemoveGroups();
		this.onTheFlyFilterIDPGroupsOption = settings.getOnTheFlyFilterIDPGroupsOption();
		this.onTheFlyFilterIDPGroupsKey = settings.getOnTheFlyFilterIDPGroupsKey();

		LOGGER.debug( "\n initializeOAuthConfig for Group Mapping" + "\nKeep existing user roles" + this.keepExistingUserRoles + "\ndefault group list" + this.defaultGroupsList + "\nrole mapping " + this.roleMapping
				+ "\nrole Attribute" + this.roleAttribute + "\ncreate users if roled mapped" + this.createUsersIfRoleMapped + "\nristrict users creation"+ this.restrictUserCreation
				+ "\nenable default groups for" +this.enableDefaultGroupsFor +"\nOn The Fly Group Creation"+ this.onTheFlyGroupCreation + "\ncreate New Groups" + this.createNewGroups
				+ "\nonTheFlyAssignNewGroupsOnly" + this.onTheFlyAssignNewGroupsOnly + "\nonTheFlyDoNotRemoveGroups" + this.onTheFlyDoNotRemoveGroups
				+ "\nonTheFlyDoNotRemoveGroupsList"+ this.onTheFlyDoNotRemoveGroupsList + "\nonTheFlyFilterIDPGroupsOption" + this.onTheFlyFilterIDPGroupsOption
				+ "\nonTheFlyFilterIDPGroupsKey" + onTheFlyFilterIDPGroupsKey);

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

	public String getDefaultGroup() {
		return defaultGroup;
	}

	public void setDefaultGroup(String defaultGroup) {
		this.defaultGroup = defaultGroup;
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

	public Boolean getGroupRegexPatternEnabled() {
		return groupRegexPatternEnabled;
	}

	public void setGroupRegexPatternEnabled(Boolean groupRegexPatternEnabled) {
		this.groupRegexPatternEnabled = groupRegexPatternEnabled;
	}

	public String getRegexGroups() {
		return regexGroups;
	}

	public void setRegexGroups(String regexGroups) {
		this.regexGroups = regexGroups;
	}

	public String getRegexPatternForGroup() {
		return regexPatternForGroup;
	}

	public void setRegexPatternForGroup(String regexPatternForGroup) {
		this.regexPatternForGroup = regexPatternForGroup;
	}

	public String getTestRegex() {
		return testRegex;
	}

	public void setTestRegex(String testRegex) {
		this.testRegex = testRegex;
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
		List<String> groupFilterOptionsList = new ArrayList();
		groupFilterOptionsList.add(MoOAuthPluginConstants.ON_THE_FLY_NO_GROUP_FILTER);
		groupFilterOptionsList.add(MoOAuthPluginConstants.ON_THE_FLY_FILTER_GROUPS_STARTS_WITH);
		groupFilterOptionsList.add(MoOAuthPluginConstants.ON_THE_FLY_FILTER_GROUPS_CONTAINS);
		groupFilterOptionsList.add(MoOAuthPluginConstants.ON_THE_FLY_FILTER_GROUPS_WITH_REGEX);

		return groupFilterOptionsList;
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

	public Map<String, String> getRoleMapping() {
		return roleMapping;
	}

	public void setRoleMapping(Map<String, String> roleMapping) {
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

	public MoOAuthPluginHandler getPluginHandler() {
		return pluginHandler;
	}

	public void setPluginHandler(MoOAuthPluginHandler pluginHandler) {
		this.pluginHandler = pluginHandler;
	}

	public void setExistingGroups(ArrayList existingGroups) {
		this.existingGroups = existingGroups;
	}

	public MoOAuthSettings getSettings() {
		return settings;
	}

	public void setSettings(MoOAuthSettings settings) {
		this.settings = settings;
	}
	
	public Boolean getRestrictUserCreation() {
		return restrictUserCreation;
	}

	public void setRestrictUserCreation(Boolean restrictUserCreation) {
		this.restrictUserCreation = restrictUserCreation;
	}

	public String getGroupRegexPattern() {
		return groupRegexPattern;
	}

	public void setGroupRegexPattern(String groupRegexPattern) {
		this.groupRegexPattern = groupRegexPattern;
	}

	public CrowdService getCrowdService() {
		return crowdService;
	}

	public void setCrowdService(CrowdService crowdService) {
		this.crowdService = crowdService;
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

	public Boolean getCreateNewGroups() {
		return createNewGroups;
	}

	public void setCreateNewGroups(Boolean createNewGroups) {
		this.createNewGroups = createNewGroups;
	}

	public Boolean getOnTheFlyAssignNewGroupsOnly() {
		return onTheFlyAssignNewGroupsOnly;
	}

	public void setOnTheFlyAssignNewGroupsOnly(Boolean onTheFlyAssignNewGroupsOnly) {
		this.onTheFlyAssignNewGroupsOnly = onTheFlyAssignNewGroupsOnly;
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
	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}


}