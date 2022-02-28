package com.miniorange.oauth.confluence;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.miniorange.oauth.confluence.MoOAuthWebSudoSessionManager;

public class MoOAuthWebSudoSessionManager {
	//private static final long DEFAULT_EXPIRY_MILLIS = TimeUnit.SECONDS.toMillis(10 * 60); //   TimeUnit.MINUTES
    // requires Java 6
    private static final String WEBSUDO_SESSION_KEY = MoOAuthWebSudoSessionManager.class.getName() + "-session";
    /*@VisibleForTesting
    protected static final String WEB_SUDO_CHECKING_DISABLED_PROPERTY = "atlassian.refapp.websudo.disabled";*/

    public void createWebSudoSession(final HttpServletRequest request, final HttpServletResponse response) {
        final HttpSession session = request.getSession(true);
        if (null == session) {
            throw new SecurityException("Unable to create a WebSudo session.");
        }
        session.setAttribute("confluence.websudo.timestamp", (Object)this.currentTimeMillis());
        request.setAttribute("confluence.websudo.request", (Object)Boolean.TRUE);
        session.setAttribute(WEBSUDO_SESSION_KEY, currentTimeMillis());
        response.setHeader("X-Atlassian-WebSudo", "Has-Authentication");
    }

    /**
     * @return the difference, measured in milliseconds, between
     * the current time and midnight, January 1, 1970 UTC.
     *
     * Mainly used for testing purposes.
     */
    private long currentTimeMillis() {
        // We could introduce an explicit Clock interface and inject a clock instance instead...
        return System.currentTimeMillis();
    }

}
