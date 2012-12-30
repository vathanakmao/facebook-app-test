package com.vathanakmao.testproj.facebookapptest.web.interceptor;

import com.vathanakmao.testproj.facebookapptest.service.FacebookAuthService;
import com.vathanakmao.testproj.facebookapptest.model.FBSignedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class FBAuthorizationInterceptor extends HandlerInterceptorAdapter {
    private static final Logger log = LoggerFactory.getLogger(FBAuthorizationInterceptor.class);

    private List<String> excludingPaths;

    private FacebookAuthService facebookAuthService = new FacebookAuthService();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // No need to authorize for the excluding path
        if (excludingPaths != null) {
            for (String path : excludingPaths) {
                if (request.getRequestURI().contains(path)) {
                    log.info(">> The request URI '{}' does not need authorization.", request.getRequestURI());
                    return true;
                }
            }
        }

        log.debug(">> Referer={}", request.getHeader("Referer"));

        if ("access_denied".equals(request.getParameter("error"))) { // if the user has chosen not to accept the Login dialog
            log.info(">> User has chosen not to accept the Login Dialog.");
            return false;
        }

        FBSignedRequest signedRequest = FacebookAuthService.getFacebookSignedRequestFromSession(request);
        log.debug(">> signedRequest (in session): {}", signedRequest);

        if (signedRequest == null) { // it means the user's never accessed this app
            signedRequest = FacebookAuthService.getFacebookSignedRequest(request.getParameter("signed_request"));

            if (signedRequest == null) { // if it's not a redirect back from the Login dialog, then ask for login
                log.info(">> Redirecting to {}", FacebookAuthService.getAuthURL());

                response.sendRedirect(FacebookAuthService.getAuthURL());
                return false;

            } else { // if this is the redirect back from the Login Dialog and the user has granted the permissions to the app
                log.info(">> User has granted the permissions to the app (user_id={}, oauth_token={})", new Object[]{signedRequest.getUser_id(), signedRequest.getOauth_token()});

                facebookAuthService.generateLongLivedAccessToken(signedRequest);

                request.getSession().setAttribute("signedRequest", signedRequest);
            }
        } else {
            log.debug(">> userId={}, accessToken={}, expires={}", new Object[]{signedRequest.getUser_id(), signedRequest.getOauth_token(), signedRequest.getExpires()});
        }

        return true;
    }

    public List<String> getExcludingPaths() {
        return excludingPaths;
    }

    public void setExcludingPaths(List<String> excludingPaths) {
        this.excludingPaths = excludingPaths;
    }
}
