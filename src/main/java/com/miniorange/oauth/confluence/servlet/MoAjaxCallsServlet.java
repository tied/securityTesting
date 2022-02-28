package com.miniorange.oauth.confluence.servlet;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.spring.container.ContainerManager;
import com.miniorange.oauth.confluence.MoOAuthPluginHandler;
import com.miniorange.oauth.confluence.MoOAuthSettings;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoAjaxCallsServlet extends HttpServlet {
    private static Log LOGGER = LogFactory.getFactory().getInstance(MoAjaxCallsServlet.class);
    private MoOAuthSettings settings;
    private CrowdService crowdService;
    private PermissionManager permissionManager;

    public MoAjaxCallsServlet(MoOAuthSettings settings, CrowdService crowdService, PermissionManager permissionManager) {
        this.settings = settings;
        this.crowdService = crowdService;
        this.permissionManager = permissionManager;
    }
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.debug("MoAjaxCallsServlet doGet called.");
        String action = request.getParameter("action");
		String referer = request.getHeader("referer");
		if (settings.getPluginApiAccessRestriction()
				&& !org.apache.commons.lang.StringUtils.startsWith(referer, this.settings.getBaseUrl())) {
			LOGGER.error(
					"Access Denied. API Restriction is enabled and request is not originated from the Confluence.");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. You are not allowed to access this page.");
			return;
		}
        if (org.apache.commons.lang.StringUtils.isBlank(action)) {
            response.setContentType("text/html");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "The requested parameter [action] not found or it's empty");
            return;
        }
        
        switch (action) {
		case "checkBackdoorAccess":
			checkBackdoorAccess(request, response);
			break;
		case "fetchGroups":
			fetchGroups(request, response);
			break;
		case "doNotShowRestApiMsgAgain":
			doNotShowRestApiMsgAgain(request, response);
			break;
		default:
			response.setContentType("text/html");
			response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
					"The requested action is not allowed. Choose valid Action");
			return;
        }

        return;
	}
	
	@Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOGGER.debug("MoAjaxCallsServlet doPost called.");
        String action = req.getParameter("action");
        if (org.apache.commons.lang.StringUtils.isBlank(action)) {
            resp.setContentType("text/html");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "The requested parameter [action] not found or it's empty");
            return;
		}

		ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
		if (!permissionManager.isSystemAdministrator(confluenceUser)){
			resp.setContentType("text/html");
			resp.sendError(HttpServletResponse.SC_FORBIDDEN,"User is not authorized");
			return;
		}

		switch (action) {
			case "updateCallbackParameter":
                updateCallbackParameter(req, resp);
                break;
            default:
                resp.setContentType("text/html");
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,"The requested action is not allowed. Choose valid Action");
                return;
        }
	}
	private void updateCallbackParameter(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		com.atlassian.json.jsonorg.JSONObject result = new com.atlassian.json.jsonorg.JSONObject();
		String customCallbackParameter=req.getParameter("callbackParam");
		if (StringUtils.isNoneBlank(customCallbackParameter)){    
			char firstChar = customCallbackParameter.charAt(0);  
			if(firstChar != '/'){  
				customCallbackParameter = "/" + customCallbackParameter;  
			}  
		}
        settings.setCustomCallbackParameter(customCallbackParameter);
        sendSuccessFullResponse(result.toString(), resp);
    }
    
    private void checkBackdoorAccess(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String username = org.apache.commons.lang.StringUtils.trim(req.getParameter("username"));
		if (org.apache.commons.lang.StringUtils.isBlank(username)) {
			resp.setContentType(MediaType.APPLICATION_JSON);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The username is empty");
			return;
		}
		if (!settings.getBackdoorEnabled()) {
			resp.setContentType(MediaType.APPLICATION_JSON);
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "not authorized");
			return;
		}
		UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
		ConfluenceUser confluenceUser = userAccessor.getUserByName(username);
		if (confluenceUser != null) {
			List<String> groupList = settings.getBackdoorGroups();
			if (groupList.size() <= 0) {
				groupList.add("confluence-administrators");
			}
			boolean isUserPresentInGroups = MoOAuthPluginHandler.isUserPresentInGroups(username, groupList);
			JSONObject result = new JSONObject();
			LOGGER.debug("isUserPresentInGroups " + isUserPresentInGroups);
			try {
				result.put("isUserAllowedBackdoorAccess", isUserPresentInGroups);
			} catch (JSONException e) {
				LOGGER.error(e.getMessage());
				LOGGER.error("An Error Occurred while checking Backdoor Access for User ", e);
			}
			sendSuccessFullResponse(result.toString(), resp);
			return;
		}

	}

	private void fetchGroups(HttpServletRequest req, HttpServletResponse resp) {
		try {
			String search = StringUtils.trim(req.getParameter("search"));

			if (StringUtils.equalsIgnoreCase(search, "*")) {
				search = StringUtils.EMPTY;
			}

			EntityQuery<Group> query = QueryBuilder.queryFor(Group.class, EntityDescriptor.group())
					.with(Restriction.on(GroupTermKeys.NAME).startingWith(search))
					.returningAtMost(10);

			LOGGER.debug("query =  " + query);

			Iterable<Group> groups = crowdService.search(query);

			JSONObject data = new JSONObject();
			JSONArray resultArray = new JSONArray();

			for (Group groupName : groups) {
				Map<String, Object> groupResultsMap = new HashMap<>();
				groupResultsMap.put("id", groupName.getName());
				groupResultsMap.put("text", groupName.getName());
				resultArray.put(groupResultsMap);
			}

			data.put("results", resultArray);
			sendSuccessFullResponse(data.toString(), resp);
		} catch (JSONException | IOException e) {
			LOGGER.error(e.getMessage());
			LOGGER.error("An error occurred while fetching groups ", e);
		}
	}

    private void doNotShowRestApiMsgAgain(HttpServletRequest req, HttpServletResponse resp) {
        settings.setShowRestApiMsg(false);
    }

    private void sendSuccessFullResponse(String result, HttpServletResponse resp) throws IOException {
		resp.setContentType(MediaType.APPLICATION_JSON);
		resp.setStatus(HttpServletResponse.SC_OK);
		if (result != null) {
			resp.getOutputStream().write(result.getBytes());
			resp.getOutputStream().close();
		}
		return;
	}

	private void sendErrorResponse(String errorMessages, HttpServletResponse resp) throws IOException {
		resp.setContentType(MediaType.APPLICATION_JSON);
		resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		resp.getOutputStream().write(errorMessages.getBytes());
		resp.getOutputStream().close();
		return;
	}
	
    public MoOAuthSettings getSettings() {
        return settings;
    }

    public void setSettings(MoOAuthSettings settings) {
        this.settings = settings;
    }
}
