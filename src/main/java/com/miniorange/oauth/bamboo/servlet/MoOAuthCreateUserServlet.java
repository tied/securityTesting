package com.miniorange.oauth.bamboo.servlet;

import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.crowd.exception.runtime.UserNotFoundException;
import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.miniorange.oauth.bamboo.MoOAuthPluginConfigurationsHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class MoOAuthCreateUserServlet extends HttpServlet {
	private Log LOGGER = LogFactory.getLog(MoOAuthCreateUserServlet.class);

	private BambooUserManager bambooUserManager;
	private BambooPermissionManager bambooPermissionManager;
	private MoOAuthPluginConfigurationsHandler moOAuthPluginConfigurationsHandler;

	public MoOAuthCreateUserServlet(BambooUserManager bambooUserManager,
			BambooPermissionManager bambooPermissionManager, MoOAuthPluginConfigurationsHandler moOAuthPluginConfigurationsHandler) {
		this.bambooUserManager = bambooUserManager;
		this.bambooPermissionManager = bambooPermissionManager;
		this.moOAuthPluginConfigurationsHandler = moOAuthPluginConfigurationsHandler;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		LOGGER.debug("POST: User create servlet");
		
		String actioneType = req.getParameter("action");

		LOGGER.debug("action to perform : " + actioneType);

		if (StringUtils.equals(actioneType, "createUser")) {
			streamResponse(resp, createUser(req));
		} else if (StringUtils.equals(actioneType, "saveUser")) {
			streamResponse(resp, saveUser(req));
		} else if (StringUtils.equals(actioneType, "updateGroups")) {
			streamResponse(resp, addOrRemoveUserToGroups(req));
		} else if (StringUtils.equals(actioneType, "createGroup")) {
			streamResponse(resp, createGroup(req));
		} else if(StringUtils.equals(actioneType, "resetSettings")) {
			moOAuthPluginConfigurationsHandler.clearPluginSettings();
		}

	}

	public BambooUser createUser(HttpServletRequest req) {

		LOGGER.debug("Creating new users");
		BambooUser createdBambooUser = null;
		String username = req.getParameter("username");
		String email = req.getParameter("email");
		String fullName = req.getParameter("fullname");
		String password = getRandomPassword();
		String groupListJson = req.getParameter("groups");

		LOGGER.debug("Parameter received in Create user servlet");
		LOGGER.debug(username + " " + email + " " + fullName + " " + password + " " + groupListJson);

		List<String> groupsToAssign = parseGroups(groupListJson);

		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(email)) {
			LOGGER.debug("Calling add user function");
			createdBambooUser = bambooUserManager.addUser(username, password, email, fullName, "", groupsToAssign);
		}
		return createdBambooUser;
	}

	public BambooUser saveUser(HttpServletRequest req) {
		LOGGER.debug("Updating existing users profile.");
		BambooUser createdBambooUser = null;
		String username = req.getParameter("username");
		String email = req.getParameter("email");
		String fullName = req.getParameter("fullname");

		User user = bambooUserManager.getUser(username);
		DefaultUser defaultUser = new DefaultUser(user);
		defaultUser.setFullName(fullName);
		defaultUser.setEmail(email);
		bambooUserManager.saveUser(defaultUser);

		createdBambooUser = bambooUserManager.getBambooUser(user);
		return createdBambooUser;
	}

	public BambooUser createGroup(HttpServletRequest req) {
		LOGGER.info("Creating New group");
		String username = req.getParameter("username");
		String groupToCreate = req.getParameter("group_to_create");
		bambooUserManager.createGroup(groupToCreate);

		User user = bambooUserManager.getUser(username);
		return bambooUserManager.getBambooUser(user);
	}

	public BambooUser addOrRemoveUserToGroups(HttpServletRequest req) {
		LOGGER.debug("Updating groups of the user ");
		
		BambooUser createdBambooUser = null;
		String username = req.getParameter("username");
		
		LOGGER.debug("Bamboo user : "+username);
		
		List<String> groupsToAssign = parseGroups(req.getParameter("groups"));
		
		LOGGER.debug("group to assign to the user : "+groupsToAssign.toString());

		List<String> existingGroupsOfUser = new ArrayList<>();

		existingGroupsOfUser = bambooUserManager.getGroupNamesAsList(bambooUserManager.getBambooUser(username));

		LOGGER.debug("Existing groups of the user : "+existingGroupsOfUser);
		
		if (!groupsToAssign.isEmpty()) {
			for (String newGroups : groupsToAssign) {
				if (!existingGroupsOfUser.contains(newGroups)) {
					LOGGER.debug("Adding user: "+username+" to group : "+newGroups);
					bambooUserManager.addMembership(newGroups, username);
				}
			}
		}

		for (String groups : existingGroupsOfUser) {
			if (!groupsToAssign.contains(groups)) {
				LOGGER.debug("Removing user: "+username+" from group : "+groups);
				bambooUserManager.removeMembership(groups, username);
			}
		}
		// }

		User user = bambooUserManager.getUser(username);
		createdBambooUser = bambooUserManager.getBambooUser(user);
		
		LOGGER.debug("Bamboo user : "+createdBambooUser.toString());
		
		return createdBambooUser;
	}

	public List<String> parseGroups(String groupListJson) {
		if(StringUtils.isBlank(groupListJson)){
			return new ArrayList<>();
		}
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonArray groupListJsonArray = parser.parse(groupListJson).getAsJsonArray();
		Type listType = new TypeToken<List<String>>() {
		}.getType();
		List<String> groupsToAssign = gson.fromJson(groupListJsonArray, listType);

		return groupsToAssign;
	}

	public void streamResponse(HttpServletResponse resp, BambooUser createdBambooUser) throws IOException {
		try {
			LOGGER.debug("streamResponse : Forming response");
			String attrsJSON = createdBambooUser.getName();

			LOGGER.debug("Response attribute : " + attrsJSON);

			resp.setContentType(MediaType.TEXT_PLAIN);
			resp.getOutputStream().write(attrsJSON.getBytes());
			resp.getOutputStream().close();
		}catch (Exception e){
			LOGGER.error("Exception occurred while getting bamboo user.");
		}
	}

	public String getRandomPassword() {
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < 18) {
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
		return saltStr;

	}
}
