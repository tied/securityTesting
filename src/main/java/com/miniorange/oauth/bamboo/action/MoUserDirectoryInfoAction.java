package com.miniorange.oauth.bamboo.action;

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.miniorange.oauth.bamboo.MoOAuthSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MoUserDirectoryInfoAction extends BambooActionSupport {
    private static final Log LOGGER = LogFactory.getLog(MoUserDirectoryInfoAction.class);
    private MoOAuthSettings settings;

    public MoUserDirectoryInfoAction(MoOAuthSettings settings){
        this.settings = settings;
    }

    @Override
    public String execute() throws Exception {
        LOGGER.info("MoUserDirectoryInfoAction execute called");
        try{
            if(!settings.isLicenseValid()){
                LOGGER.error("No valid license found");
                return "upm";
            }
            return "success";
        }
        catch (Exception e){
            LOGGER.error(e.getMessage());
            addActionError("An error occurred. Please check logs for more info.");
            e.printStackTrace();
            return "input";
        }
    }

    public MoOAuthSettings getSettings() {
        return settings;
    }

    public void setSettings(MoOAuthSettings settings) {
        this.settings = settings;
    }
}
