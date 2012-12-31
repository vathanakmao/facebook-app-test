package com.vathanakmao.testproj.facebookapptest.web.interceptor;

import com.vathanakmao.testproj.facebookapptest.model.FBAccessToken;
import com.vathanakmao.testproj.facebookapptest.service.FacebookAuthService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

public class FBAuthorizationInterceptor extends HandlerInterceptorAdapter {
    private static final Logger log = LoggerFactory.getLogger(FBAuthorizationInterceptor.class);

    private List<String> excludingPaths;

    private FacebookAuthService facebookAuthService = new FacebookAuthService();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug(">> Referer={}", request.getHeader("Referer"));

        // No need to authorize for the excluding paths
        if (excludingPaths != null) {
            for (String path : excludingPaths) {
                if (request.getRequestURI().contains(path)) {
                    log.info(">> The request URI '{}' does not need authorization.", request.getRequestURI());
                    return true;
                }
            }
        }

        if (StringUtils.isEmpty((String) request.getSession().getAttribute("accessToken"))) { // if the user first time accesses this app or the session has expired

            if ("access_denied".equals(request.getParameter("error")) && "user_denied".equals(request.getParameter("error_reason"))) {
                log.info(">> The user has rejected to authorize the app.");
                return false;

            } else {
                String code = request.getParameter("code");

                if (StringUtils.isEmpty(code)) { // if the user hasn't authorized the app yet
                    String state = UUID.randomUUID().toString();
                    request.getSession().setAttribute("state", state);
                    response.sendRedirect(FacebookAuthService.getAuthorizationURL(state));
                    return false;

                } else { // if the user has just authorized the app (redirecting from the Login Dialog)
                    final String stateInSession = (String) request.getSession().getAttribute("state");
                    final String stateInRequest = request.getParameter("state");

                    if (StringUtils.equals(stateInRequest, stateInSession)) {
                        try {
                            FBAccessToken shortLivedAccessToken = facebookAuthService.exchangeCodeForAccessToken(code); // short-lived token may be valid for one or two hours
                            FBAccessToken longLivedAccessToken = facebookAuthService.getLongLivedAccessToken(shortLivedAccessToken.getAccess_token());
                            request.getSession().setAttribute("accessToken", longLivedAccessToken);
                        } catch (RestClientException ex) {
                            log.error(">> Failed to exchange code {} for access token.", code);
                            return false;
                        }

                    } else {
                        log.error(">> The state in request object does not match the one in session. You may be a victim of CSRF.");
                        return false;
                    }
                }
            }
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
