package com.miniorange.oauth.bamboo.servlet;

import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.sal.api.component.ComponentLocator;
import com.miniorange.oauth.bamboo.MoOAuthPluginHandler;
import com.miniorange.oauth.bamboo.MoOAuthSettings;
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
    private static final Log LOGGER = LogFactory.getLog(MoAjaxCallsServlet.class);

    private MoOAuthSettings settings;
    private BambooUserManager bambooUserManager;
    private MoOAuthPluginHandler pluginHandler;
    private BambooUser bambooUser;

    public MoAjaxCallsServlet(MoOAuthSettings oauthSettings, BambooUserManager bambooUserManager, MoOAuthPluginHandler pluginHandler) {
        super();
        this.settings = oauthSettings;
        this.bambooUserManager = bambooUserManager;
        this.pluginHandler = pluginHandler;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.debug("Ajax Servlet doGet Called");
        String action = req.getParameter("action");
        String referer = req.getHeader("referer");
        if(settings.getPluginApiAccessRestriction()
               && !StringUtils.startsWith(referer, this.settings.getBaseUrl())){
            LOGGER.error(
                    "Access Denied. API Restriction is enabled and request is not originated from the Bamboo.");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. You are not allowed to access this page.");
            return;
        }

        if(StringUtils.isBlank(action)){
            resp.setContentType("text/html");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"The requested parameter [action] not found or is empty");
            return;

        }

        switch (action){
            case "fetchGroups":
                if (settings.getCurrentBuildNumber() > 60604) {
                    fetchGroups(req, resp);
                }
                break;
            case "checkBackdoorAccess":
                checkBackdoorAccess(req, resp);
                break;
            default:
                resp.setContentType("text/html");
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,"The requested action is not allowed. Choose valid Action");
                return;
        }

        return;
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if(StringUtils.isBlank(action)){
            resp.setContentType("text/html");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"The requested parameter [action] not found or is empty");
            return;
        }
        switch (action){
            case "updateBackdoorDetails":
                updateBackdoorDetails(req, resp);
                break;
            default:
                resp.setContentType("text/html");
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,"The requested action is not allowed. Choose valid Action");
                return;
        }
    }

    private void checkBackdoorAccess(HttpServletRequest req, HttpServletResponse resp)throws IOException {
        String username = org.apache.commons.lang.StringUtils.trim(req.getParameter("username"));
        if (org.apache.commons.lang.StringUtils.isBlank(username)) {
            resp.setContentType(MediaType.APPLICATION_JSON);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The username is empty");
            return;
        }
        BambooUser bambooUser = bambooUserManager.getBambooUser(username);
        if (bambooUser != null) {
            List<String> groupList = settings.getBackdoorGroups();
            if (groupList.size() <= 0) {
                groupList.add("bamboo-admin");
            }
            boolean isUserPresentInGroups = pluginHandler.isUserPresentInGroups(username, groupList);
            JSONObject result = new JSONObject();
            LOGGER.debug("isUserPresentInGroups " + isUserPresentInGroups);
            try {
                result.put("isUserAllowedBackdoorAccess", isUserPresentInGroups);
            } catch (JSONException e) {
                e.printStackTrace();
                LOGGER.error("An Error Occurred while checking Backdoor Access for User ", e);
            }
            sendSuccessFullResponse(result.toString(), resp);
            return;
        }
    }

    private void fetchGroups(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.debug("Ajax Servlet fetchGroup Called");
        try {
            String search = StringUtils.trim(req.getParameter("search"));
            if (StringUtils.equalsIgnoreCase(search, "*")) {
                search = StringUtils.EMPTY;
            }

            com.atlassian.crowd.embedded.api.CrowdService crowdService = ComponentLocator.getComponent(com.atlassian.crowd.embedded.api.CrowdService.class);

            EntityQuery<com.atlassian.crowd.embedded.api.Group> query = QueryBuilder.queryFor(com.atlassian.crowd.embedded.api.Group.class, EntityDescriptor.group())
                    .with(Restriction.on(GroupTermKeys.NAME).startingWith(search))
                    .returningAtMost(10);

            LOGGER.debug("query =  " + query);

            Iterable<com.atlassian.crowd.embedded.api.Group> groups = crowdService.search(query);

            JSONObject data = new JSONObject();
            JSONArray resultArray = new JSONArray();

            //JsonObject data = new JsonObject();
            //JsonArray resultArray = new JsonArray();

            for (com.atlassian.crowd.embedded.api.Group groupName : groups) {
                Map<String, Object> groupResultsMap = new HashMap<>();
                groupResultsMap.put("id", groupName.getName());
                groupResultsMap.put("text", groupName.getName());
                resultArray.put(groupResultsMap);
                //resultArray.add();
            }
            LOGGER.debug("resultArray :" +resultArray.toString());

            data.put("results", resultArray);
            sendSuccessFullResponse(data.toString(), resp);
        }catch (Exception e){
            e.printStackTrace();
            LOGGER.error("An error occurred while fetching groups ",e);
        }
    }


    private void updateBackdoorDetails(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JSONObject result = new JSONObject();
        settings.setBackdoorKey(req.getParameter("backdoorKey"));
        settings.setBackdoorValue(req.getParameter("backdoorValue"));
        sendSuccessFullResponse(result.toString(), resp);
    }


    private void sendSuccessFullResponse(String result, HttpServletResponse resp) throws IOException {
        resp.setContentType(MediaType.APPLICATION_JSON);
        resp.setStatus(HttpServletResponse.SC_OK);
        if(result!=null) {
            resp.getOutputStream().write(result.getBytes());
            resp.getOutputStream().close();
        }
        return;
    }

    public MoOAuthSettings getSettings() {
        return settings;
    }

    public void setSettings(MoOAuthSettings settings) {
        this.settings = settings;
    }

    public BambooUserManager getBambooUserManager() {
        return bambooUserManager;
    }

    public void setBambooUserManager(BambooUserManager bambooUserManager) {
        this.bambooUserManager = bambooUserManager;
    }

    public MoOAuthPluginHandler getPluginHandler() {
        return pluginHandler;
    }

    public void setPluginHandler(MoOAuthPluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;
    }

}
