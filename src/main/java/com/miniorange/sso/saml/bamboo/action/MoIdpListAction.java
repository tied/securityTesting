package com.miniorange.sso.saml.bamboo.action;


import com.atlassian.bamboo.security.xsrf.XsrfTokenUtils;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.miniorange.sso.saml.MoPluginException;
import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoSAMLUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.*;

public class MoIdpListAction extends BambooActionSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(MoIdpListAction.class);
	private MoSAMLSettings settings;
	private UserManager userManager;
	private LoginUriProvider loginUriProvider;

	private ArrayList<String> idpList;
	private HashMap<String,String> idpMap;
	private Boolean domainsSubmitted;
	private String[] domains;
	private List<String> domainsList;
	private Boolean useDomainMapping;
	private Boolean showAcsUrlMessage;
	private String xsrfToken;
	private Boolean showIntroPage;
	private ArrayList<Boolean> enableSsoForIdp;
	private String idpID;
	private HashMap<String,Boolean> enableSsoForIdpMap;


	public MoIdpListAction(UserManager userManager, LoginUriProvider loginUriProvider, MoSAMLSettings settings) {
		super();
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.settings = settings;
	}
	

	private Boolean doValidate() {
		LOGGER.info("Validating IdP List and Domain Mapping");
		Boolean error = false;
		 if(BooleanUtils.toBoolean(this.domainsSubmitted)){
			if(!MoPluginHandler.isValidDomainMapping(domains, useDomainMapping)){
				addActionError("Domains cannot be left empty. Also each IDP should have at least one domain name.");
				error = true;
			}/* else if (!pluginHandler.isValidDomainEntered(domains, useDomainMapping)) {
				addActionError("Invalid domain name entered. Please enter valid domain");
				error = true;
			}*/
		}

		return error;
	}

	@Override
	public String execute() throws Exception {
		// TODO Auto-generated method stub
		LOGGER.debug("doExecute: MoIdpListAction doExecute Called");

		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		xsrfToken = XsrfTokenUtils.getXsrfToken(request);

		final UserProfile user = userManager.getRemoteUser();

		if (user != null && userManager.isAdmin(user.getUserKey())) {
			try {
				if (!settings.isValidLicense()) {
					LOGGER.error("No valid license found");
					return "upm";
				}
				idpID = "";

				idpMap = settings.getIdpMap();
				idpList = settings.getIdPList();
				String status = request.getParameter("status");
				String operation = request.getParameter("operation");

				if (StringUtils.isNotBlank(settings.getSsoServiceUrl())) {
					String idpID = UUID.randomUUID().toString();
					String idpName = "IDP";
					MoPluginHandler.replaceOldSettingsWithNew(idpID, idpName);
				}

				if(StringUtils.isNotBlank(request.getParameter("reset"))){
					if(StringUtils.equals(request.getParameter("reset"), "success")){
						addActionMessage("Plugin reset successful.");
					}
				}

				//cannot save details while get method, shows error message every time
				/*if (settings.getIdPList().isEmpty()) {
					settings.setPrivateSPCertificate(settings.getNewPrivateSPCertificate());
					settings.setPublicSPCertificate(settings.getNewPublicSPCertificate());
				}*/

				String validTo = getCertificateInfo().get("Valid To");
				//settings.setSPCertExpireOn(validTo);


				if (StringUtils.isNotBlank(operation) && operation.equals("delete")) {
					if (StringUtils.isNotBlank(status)) {
						if (status.equals("success")) {
							addActionMessage("IDP Deleted Successfully");
						} else if (status.equals("empty")) {
							addActionError("IDP ID received was empty");
						} else if (status.equals("missing")) {
							addActionError("IDP does not exist");
						} else if (status.equals("error")) {
							addActionError("Problem with deleting IDP");
						}
					}
				} else if (StringUtils.isNotBlank(status)) {
					if (status.equals("success")) {
						addActionMessage("SAML settings updated");
						if (settings.getIdPList().size() >= 2) {
							this.showAcsUrlMessage = true;
						}
					} else if (status.equals("duplicate")) {
						addActionError("IDP already exists. IDP Names must be unique.");
					}
				}
				initializeIdpListConfig();
				return SUCCESS;
			} catch (MoPluginException e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
				addActionError(e.getMessage());
				initializeIdpListConfig();
				return INPUT;
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
				addActionError("An error occurred while saving your details. Please check logs for more info.");
				LOGGER.error("An error occurred while saving Idp config details " + e.getMessage() + " with Cause"
						+ e.getCause());
				initializeIdpListConfig();
				return INPUT;
			}
		} else {
			response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
			return null;
		}
	}

	private void initializeIdpListConfig() {
		this.idpMap = settings.getIdpMap();
		this.domainsList = settings.getDomains();
		this.useDomainMapping = settings.getUseDomainMapping();
		this.enableSsoForIdpMap = getEnableSsoForIdpMap();
		this.showIntroPage = settings.getShowIntroPage();
	}

	public HashMap<String, Boolean> getEnableSsoForIdpMap() {
		ArrayList<String> idpList = getIdpList();
		HashMap<String, Boolean> enableSsoForIdpMap = new HashMap<String, Boolean>();

		for(String idpID : idpList){
			JSONObject idpConfigObj = settings.getIdpConfig(idpID);
			enableSsoForIdpMap.put(idpID, idpConfigObj.optBoolean(MoPluginConstants.ENABLE_SSO_FOR_IDP, Boolean.TRUE));
		}
		return enableSsoForIdpMap;
	}

	private URI getUri(HttpServletRequest request) {
		StringBuffer builder = request.getRequestURL();
		if (request.getQueryString() != null) {
			builder.append("?");
			builder.append(request.getQueryString());
		}
		return URI.create(builder.toString());
	}

	public MoSAMLSettings getSettings() {
		return settings;
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
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

	/**
	 * @return the domains
	 */
	public String[] getDomains() {
		return domains;
	}

	/**
	 * @param domains the domains to set
	 */
	public void setDomains(String[] domains) {
		this.domains = domains;
	}

	/**
	 * @return the domainsSubmitted
	 */
	public Boolean getDomainsSubmitted() {
		return domainsSubmitted;
	}

	/**
	 * @param domainsSubmitted the domainsSubmitted to set
	 */
	public void setDomainsSubmitted(Boolean domainsSubmitted) {
		this.domainsSubmitted = domainsSubmitted;
	}

	/**
	 * @return the domainsList
	 */
	public List<String> getDomainsList() {
		return domainsList;
	}

	/**
	 * @param domainsList the domainsList to set
	 */
	public void setDomainsList(List<String> domainsList) {
		this.domainsList = domainsList;
	}

	/**
	 * @return the useDomainMapping
	 */
	public Boolean getUseDomainMapping() {
		return useDomainMapping;
	}

	/**
	 * @param useDomainMapping the useDomainMapping to set
	 */
	public void setUseDomainMapping(Boolean useDomainMapping) {
		this.useDomainMapping = useDomainMapping;
	}

	/**
	 * @return the idpMap
	 */
	public HashMap<String, String> getIdpMap() {
		return idpMap;
	}

	/**
	 * @param idpMap the idpMap to set
	 */
	public void setIdpMap(HashMap<String, String> idpMap) {
		this.idpMap = idpMap;
	}

	/**
	 * @return the idpList
	 */
	public ArrayList<String> getIdpList() {
		idpList = settings.getIdPList();
		
		return this.idpList;
	}

	/**
	 * @param idpList the idpList to set
	 */
	public void setIdpList(ArrayList<String> idpList) {
		idpList = settings.getIdPList();
		
		this.idpList = idpList;
	}

	public Boolean getShowAcsUrlMessage() {
		return showAcsUrlMessage;
	}

	public void setShowAcsUrlMessage(Boolean showAcsUrlMessage) {
		this.showAcsUrlMessage = showAcsUrlMessage;
	}

	public String getXsrfToken() {
		return xsrfToken;
	}

	public void setXsrfToken(String xsrfToken) {
		this.xsrfToken = xsrfToken;
	}

	public String getIdpID() {
		return idpID;
	}

	public void setIdpID(String idpID) {
		this.idpID = idpID;
	}

	public Boolean getShowIntroPage() {
		return showIntroPage;
	}

	public void setShowIntroPage(Boolean showIntroPage) {
		this.showIntroPage = showIntroPage;
	}

	public ArrayList<Boolean> getEnableSsoForIdp() {
		return enableSsoForIdp;
	}

	public void setEnableSsoForIdp(ArrayList<Boolean> enableSsoForIdp) {
		this.enableSsoForIdp = enableSsoForIdp;
	}

	public Map<String, String> getCertificateInfo() {
		return MoSAMLUtils.getCertificateInfo(settings.getPublicSPCertificate());
	}
}
