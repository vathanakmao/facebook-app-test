package com.vathanakmao.testproj.facebookapptest.web.interceptor;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.connect.Connection;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class FBAuthorizationInterceptor extends HandlerInterceptorAdapter {
    private static final Logger log = LoggerFactory.getLogger(FBAuthorizationInterceptor.class);

    private List<String> excludingPaths;

    @Inject
    private FacebookConnectionFactory facebookConnectionFactory;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug(">> Redirect URL: {}, Request URI: {}", request.getRequestURL().toString(), request.getRequestURI());

        // No need to authorize for the excluding paths
        if (excludingPaths != null) {
            for (String path : excludingPaths) {
                if (request.getRequestURI().contains(path)) {
                    log.info(">> The request URI '{}' does not need authorization.", request.getRequestURI());
                    return true;
                }
            }
        }

        AccessGrant accessGrant = (AccessGrant) request.getSession().getAttribute("accessGrant");
        String code = request.getParameter("code");
        OAuth2Operations oauthOperations = facebookConnectionFactory.getOAuthOperations();

        if (accessGrant == null && StringUtils.isEmpty(code)) {
            OAuth2Parameters params = new OAuth2Parameters();
            params.setRedirectUri(request.getRequestURL().toString());

            String authorizeUrl = oauthOperations.buildAuthorizeUrl(GrantType.AUTHORIZATION_CODE, params);
            response.sendRedirect(authorizeUrl);
            return false;
        }
        else if (StringUtils.isNotEmpty(code)) {
            accessGrant = oauthOperations.exchangeForAccess(code, request.getRequestURL().toString(), null);
            request.getSession().setAttribute("accessGrant", accessGrant);
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
