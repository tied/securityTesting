package com.miniorange.sso.saml.bamboo;

import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.user.User;
import com.atlassian.user.search.SearchResult;
import com.atlassian.user.search.page.Pager;
import com.miniorange.sso.saml.MoSAMLException;
import com.miniorange.sso.saml.utils.MoHttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;


public class MoSAMLUserManager {
    private static Log LOGGER = LogFactory.getLog(MoSAMLUserManager.class);
    private BambooUserManager bambooUserManager;

    public MoSAMLUserManager(BambooUserManager bambooUserManager) {
        this.bambooUserManager = bambooUserManager;
    }

    public static void replaceOldSettingsWithNew(String url, String idpID, String idpName) {
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();

        postParameters.add(new BasicNameValuePair("idpID",idpID));
        postParameters.add(new BasicNameValuePair("idpName",idpName));

        String result = MoHttpUtils.sendPostRequest(url, postParameters,"application/x-www-form-urlencoded",null);
    }

    public BambooUser getUserByUsernameOrEmail(String usernameOrEmail) {
        BambooUser user = searchUserByUsername(usernameOrEmail);
        if(user != null)
            return user;
        try {
            String username = searchUserByEmail(usernameOrEmail);
             user= searchUserByUsername(username);
            if(username != null)
                return user;
        }catch(MoSAMLException e){
            LOGGER.error("An error occurred while searching for user ",e);

        }
        return null;
    }

    public BambooUser searchUserByUsername(String username){
        LOGGER.debug("Searching for Bamboo user with username : " + username);
        BambooUser user = bambooUserManager.getBambooUser(username);
        return user;
    }

    public String searchUserByEmail(String email) {
        /*If User Found: Return Bamboo user. Else return null. In case more than one user found with the same email throw Multiple_User_Found Exception*/
        LOGGER.debug("Searching for Bamboo user with email : " + email);
        int count = 0;
        String username = StringUtils.EMPTY;
        SearchResult searchResult = bambooUserManager.getUsersByEmail(email);
        Pager<User> pager = searchResult.pager();
        for (User user : pager) {
            username = user.getName();
            count++;
        }
        if (count > 1) {
            //Instead of returning -1 throw Multiple_User_Found Exception.
            throw new MoSAMLException(MoSAMLException.SAMLErrorCode.MULTIPLE_USER_FOUND);
        }
        LOGGER.debug("Found user by email = " + username);
        return username;
    }

    public Boolean isUserPresentInGroups(String username, List<String> groups) {
        LOGGER.debug("Testing for user " + username);
        try {
            BambooUser bambooUser = bambooUserManager.getBambooUser(username);
            List<String> existingGroupsOfUser = new ArrayList<>();
            existingGroupsOfUser = bambooUserManager.getGroupNamesAsList(bambooUser);
            if (bambooUser != null) {
                for (String group : groups) {
                    if (existingGroupsOfUser.contains(group)) {
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            return false;
        }
        return false;
    }

    public List<String> getUserGroupName(String username)
    {
        BambooUser bambooUser = bambooUserManager.getBambooUser(username);
        List<String> existingGroupsOfUser = new ArrayList<>();
        existingGroupsOfUser = bambooUserManager.getGroupNamesAsList(bambooUser);
        return existingGroupsOfUser;
    }

    public BambooUserManager getBambooUserManager() {
        return bambooUserManager;
    }

    public void setBambooUserManager(BambooUserManager bambooUserManager) {
        this.bambooUserManager = bambooUserManager;
    }
}
