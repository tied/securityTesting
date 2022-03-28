package com.miniorange.sso.saml.bamboo.action;

import java.net.URI;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.bamboo.util.BuildUtils;
import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import com.miniorange.sso.saml.bamboo.MoSAMLUserManager;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.plugin.PluginException;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.user.Group;
import com.atlassian.user.search.page.Pager;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;

import de.schlichtherle.truezip.socket.SynchronizedOutputShop;

public class MoGroupMappingAction extends BambooActionSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(MoGroupMappingAction.class);

	private UserManager userManager;
	private LoginUriProvider loginUriProvider;
	private MoSAMLSettings settings;
	private BambooUserManager bambooUserManager;

	private String roleAttribute;
	private String defaultGroup;
	private String[] defaultGroups;
	private List<String> defaultGroupsList;
	private Map<String, String> roleMapping;
	private Boolean createUsersIfRoleMapped;
	private Boolean keepExistingUserRoles;
	private ArrayList existingGroups;
	private String xsrfToken;
	private Boolean roleMappingSubmitted;
	private Boolean restrictUserCreation;
	private String enableDefaultGroupsFor;

	//Group regex
	private Boolean groupRegexPatternEnabled = false;
	private String regexPatternForGroup;
	private String regexGroups;
	private String testRegex;

	private Boolean onTheFlyGroupCreation;
	private String[] onTheFlyDoNotRemoveGroups;
	private List<String> onTheFlyDoNotRemoveGroupsList;
	private Boolean createNewGroups;

	//It will assign new groups but will not affect existing groups of users.
	private Boolean onTheFlyAssignNewGroupsOnly;

	private Boolean gmIdpChanged;
	private String idpName;
	private String idpID;
	private HashMap<String, String> idpMap;
	private String lowerBuild;



	private MoSAMLUserManager moSAMLUserManager;
	private Boolean enableButtons;

	public MoGroupMappingAction(UserManager userManager, LoginUriProvider loginUriProvider, MoSAMLSettings settings,
			BambooUserManager bambooUserManager) {
		super();
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.settings = settings;
		this.bambooUserManager = bambooUserManager;
	}

	public Boolean doValidate() {
		LOGGER.debug("GroupMapping Action execute");
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		Boolean isError = Boolean.FALSE;
		if (!BooleanUtils.toBoolean(roleMappingSubmitted)) {
			return isError;
		}

		if (defaultGroups != null && defaultGroups.length <= 0) {
			LOGGER.error("Default group is blank. Please select atleast one default group.");
			addActionError("Please select atleast one default group.");
			isError = true;
		}

		if (BooleanUtils.toBoolean(onTheFlyGroupCreation) && StringUtils.isBlank(roleAttribute)) {
			LOGGER.error("Group Attribute can't be blank when On The Fly Group Mapping is enabled.");
			addActionError("Group Attribute is Required if On the Fly Group Mapping is enabled.");
			isError = true;
		}

		if (BooleanUtils.toBoolean(isError)) {
			xsrfToken = XsrfTokenUtils.getXsrfToken(request);
			initializeSAMLConfig();
		}

		return isError;
	}

	@Override
	public String execute() throws Exception {
		LOGGER.debug("GroupMapping action execute() called");
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		xsrfToken = XsrfTokenUtils.getXsrfToken(request);
		LOGGER.debug("Regexpatternenabled = " + this.groupRegexPatternEnabled);
		lowerBuild = "false";
		LOGGER.debug("Current Build Number : "+settings.getCurrentBuildNumber());
		lowerBuild = getLowerBuild();

		final UserProfile user = userManager.getRemoteUser();

		if (user != null && userManager.isAdmin(user.getUserKey())) {
			try {
				LOGGER.debug("GroupMapping Action execute Submitted: " + this.roleMappingSubmitted);

				if (!settings.isValidLicense()) {
					return "upm";
				}

				if(StringUtils.isNotBlank(request.getParameter("idpid"))){
					this.idpID = request.getParameter("idpid");
				}

				if (BooleanUtils.toBoolean(this.roleMappingSubmitted) && !BooleanUtils.toBoolean(doValidate())) {
					List<String> defaultGroupList = new ArrayList<String>();
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

					if (defaultGroup != null) {
						LOGGER.debug("single default group is -" + this.defaultGroup);
						defaultGroupList.add(defaultGroup);
					}
					if (defaultGroups != null) {
						LOGGER.debug("multiple default groups -" + defaultGroups.length);
						if (StringUtils.equalsIgnoreCase("true", lowerBuild)) {
							defaultGroupList = new ArrayList<String>(Arrays.asList(defaultGroups));
						} else {
							defaultGroupList = Arrays.asList(StringUtils.split(defaultGroups[0], ","));
						}
						defaultGroupList.removeAll(Arrays.asList(null, ""));
					}

					if (onTheFlyDoNotRemoveGroups != null) {
						LOGGER.debug("Excluded groups from on-the-fly group mapping-" + onTheFlyDoNotRemoveGroups.length);
						if (StringUtils.equalsIgnoreCase("true", lowerBuild)) {
							onTheFlyDoNotRemoveGroupsList = new ArrayList<String>(Arrays.asList(onTheFlyDoNotRemoveGroups));
						} else {
							onTheFlyDoNotRemoveGroupsList = (Arrays.asList(StringUtils.split(onTheFlyDoNotRemoveGroups[0], ",")));
						}
						onTheFlyDoNotRemoveGroupsList.removeAll(Arrays.asList(null, ""));
					}

					if (StringUtils.isBlank(idpID)) {
						idpID = MoPluginConstants.DEFAULT_IDP_ID;
					}

					MoPluginHandler.saveRoleMapping(idpID, roleAttribute, BooleanUtils.toBoolean(createUsersIfRoleMapped),
							BooleanUtils.toBoolean(keepExistingUserRoles), roleMapping, this.defaultGroup, defaultGroupList,
							BooleanUtils.toBoolean(restrictUserCreation), enableDefaultGroupsFor,
							BooleanUtils.toBoolean(onTheFlyGroupCreation), onTheFlyDoNotRemoveGroupsList,
							onTheFlyAssignNewGroupsOnly, createNewGroups,groupRegexPatternEnabled,regexPatternForGroup,regexGroups,testRegex);
					//addActionMessage(getText("samlsso.success.config"));
				}

				if (StringUtils.isBlank(this.idpID)) {
                    if (!(settings.getIdpMap().isEmpty())) {
                        this.idpID = settings.getIdPList().get(0);
                    } else {
                        this.idpID = UUID.randomUUID().toString();
                        if (StringUtils.isNotBlank(settings.getSsoServiceUrl())) {
                            this.idpName = "IDP";
							MoPluginHandler.replaceOldSettingsWithNew(this.idpID, this.idpName);
						} else {
							initializeNewForm();
							return "success";
						}
					}
				}

				initializeSAMLConfig();
				return "success";
			} catch (PluginException e) {
				e.printStackTrace();
				addActionError(e.getMessage());
				return "input";
			} catch (Exception e) {
				e.printStackTrace();
				addActionError("An error occurred while saving your details. Please check logs for more info.");
				return "input";
			}
		} else {
			response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
			return null;
		}
	}

	private URI getUri(HttpServletRequest request) {
		StringBuffer builder = request.getRequestURL();
		if (request.getQueryString() != null) {
			builder.append("?");
			builder.append(request.getQueryString());
		}
		return URI.create(builder.toString());
	}

	private void initializeNewForm() {

		this.createUsersIfRoleMapped = settings.getCreateUsersIfRoleMapped();
		this.keepExistingUserRoles = settings.getKeepExistingUserRoles();
		this.defaultGroupsList = (List<String>) settings.getDefaultGroups();
		this.roleMapping = new TreeMap<>(settings.getRoleMapping());
		this.roleAttribute = settings.getRoleAttribute();
		this.restrictUserCreation = settings.getRestrictUserCreation();
		this.enableDefaultGroupsFor = settings.getEnableDefaultGroupsFor();
		this.onTheFlyGroupCreation = settings.getOnTheFlyGroupMapping();
		this.onTheFlyAssignNewGroupsOnly = settings.getOnTheFlyAssignNewGroupsOnly();
		this.onTheFlyDoNotRemoveGroupsList = (List<String>) settings.getOnTheFlyDoNotRemoveGroups();
		this.createNewGroups = settings.getCreateNewGroups();
		ArrayList<String> existingGroup = getExistingGroups();
		this.defaultGroup = settings.getDefaultGroup();


		//group regex
		this.groupRegexPatternEnabled=BooleanUtils.toBooleanDefaultIfNull(settings.getGroupRegexPatternEnabled(),false);
		this.regexPatternForGroup=StringUtils.defaultIfBlank(settings.getRegexPatternForGroup(),StringUtils.EMPTY);
		this.regexGroups=StringUtils.defaultString(settings.getRegexGroups(),StringUtils.EMPTY);
		this.testRegex=StringUtils.defaultIfBlank(settings.getTestRegex(),StringUtils.EMPTY);

		this.lowerBuild = getLowerBuild();
		this.idpName = "";
		this.enableButtons = false;
	}

	private void initializeSAMLConfig() {
		MoIDPConfig idpConfig = MoPluginHandler.constructIdpConfigObject(this.idpID);
		if(idpConfig == null){
			initializeNewForm();
			return;
		}

		this.createUsersIfRoleMapped = idpConfig.getCreateUsersIfRoleMapped();
		this.keepExistingUserRoles = idpConfig.getKeepExistingUserRoles();
		this.defaultGroupsList = (List<String>) idpConfig.getDefaultGroupsList();
		this.roleMapping = new TreeMap<>(idpConfig.getRoleMapping());
		this.roleAttribute = idpConfig.getRoleAttribute();
		this.restrictUserCreation = idpConfig.getRestrictUserCreation();
		this.enableDefaultGroupsFor = idpConfig.getEnableDefaultGroupsFor();
		this.onTheFlyGroupCreation = BooleanUtils.toBooleanDefaultIfNull(idpConfig.getOnTheFlyGroupCreation(), false);
		this.onTheFlyDoNotRemoveGroupsList = idpConfig.getOnTheFlyDoNotRemoveGroups();
		this.onTheFlyAssignNewGroupsOnly = idpConfig.getOnTheFlyAssignNewGroupsOnly();
		this.createNewGroups = BooleanUtils.toBooleanDefaultIfNull(idpConfig.getCreateNewGroups(), true);
		ArrayList<String> existingGroup = getExistingGroups();
		this.defaultGroup = idpConfig.getDefaultGroup();
		//group regex
		this.groupRegexPatternEnabled=BooleanUtils.toBooleanDefaultIfNull(idpConfig.getGroupRegexPatternEnabled(),false);
		this.regexPatternForGroup=StringUtils.defaultIfBlank(idpConfig.getRegexPatternForGroup(),StringUtils.EMPTY);
		this.regexGroups=StringUtils.defaultString(idpConfig.getRegexGroups(),StringUtils.EMPTY);
		this.testRegex=StringUtils.defaultIfBlank(idpConfig.getTestRegex(),StringUtils.EMPTY);

		this.lowerBuild = getLowerBuild();
		this.idpName = idpConfig.getIdpName();
		this.enableButtons = true;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public LoginUriProvider getLoginUriProvider() {
		return loginUriProvider;
	}

	public void setLoginUriProvider(LoginUriProvider loginUriProvider) {
		this.loginUriProvider = loginUriProvider;
	}

	public MoSAMLSettings getSettings() {
		return settings;
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}

	public String getRoleAttribute() {
		return roleAttribute;
	}

	public void setRoleAttribute(String roleAttribute) {
		this.roleAttribute = roleAttribute;
	}

	public String getDefaultGroup() {
		return defaultGroup;
	}

	public void setDefaultGroup(String defaultGroup) {
		this.defaultGroup = defaultGroup;
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

	public void setExistingGroups(ArrayList existingGroups) {
		this.existingGroups = existingGroups;
	}

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}

	public Boolean getRoleMappingSubmitted() {
		return roleMappingSubmitted;
	}

	public void setRoleMappingSubmitted(Boolean roleMappingSubmitted) {
		this.roleMappingSubmitted = roleMappingSubmitted;
	}

	public BambooUserManager getBambooUserManager() {
		return bambooUserManager;
	}

	public void setBambooUserManager(BambooUserManager bambooUserManager) {
		this.bambooUserManager = bambooUserManager;
	}

	public Boolean getRestrictUserCreation() {
		return restrictUserCreation;
	}

	public void setRestrictUserCreation(Boolean restrictUserCreation) {
		this.restrictUserCreation = restrictUserCreation;
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

	public Boolean getGmIdpChanged() {
		return gmIdpChanged;
	}

	public void setGmIdpChanged(Boolean gmIdpChanged) {
		this.gmIdpChanged = gmIdpChanged;
	}

	public String getIdpName() {
		return idpName;
	}

	public void setIdpName(String idpName) {
		this.idpName = idpName;
	}

	public String getIdpID() {
		return idpID;
	}

	public void setIdpID(String idpID) {
		this.idpID = idpID;
	}

	public HashMap<String, String> getIdpMap() {
		idpMap = settings.getIdpMap();
		return idpMap;
	}

	public void setIdpMap(HashMap<String, String> idpMap) {
		this.idpMap = idpMap;
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

	public Boolean getCreateNewGroups() {
		return createNewGroups;
	}

	public void setCreateNewGroups(Boolean createNewGroups) {
		this.createNewGroups = createNewGroups;
	}

	public String getLowerBuild() {
		if (settings.getCurrentBuildNumber() < 60604) {
			LOGGER.debug("Current Bamboo version is lesser than 6.6.0");
			this.lowerBuild = "true";
		} else {
			this.lowerBuild = "false";
			LOGGER.debug("Current Bamboo version is greater than or equal to 6.6.0");
		}
		return  this.lowerBuild;
	}

	public void setLowerBuild(String lowerBuild) {
		this.lowerBuild = lowerBuild;
	}

	public Boolean getEnableButtons() {
		return enableButtons;
	}

	public void setEnableButtons(Boolean enableButtons) {
		this.enableButtons = enableButtons;
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