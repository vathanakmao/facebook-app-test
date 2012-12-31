package com.vathanakmao.testproj.facebookapptest.service;


import com.vathanakmao.testproj.facebookapptest.model.FBAccessToken;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;

public class FacebookAuthService {
    private static final Logger log = LoggerFactory.getLogger(FacebookAuthService.class);

    private static final String appId = "304455099666589";
    private static final String appSecret = "7a4636a87cd11d9dfd37629ae442bfb5";
    private static final String redirect_uri = "http://facebook-app-test-mvthnk.appspot.com/";
    private static final String[] perms = new String[]{"publish_stream", "email", "manage_pages"};

    private RestTemplate restTemplate;

    public FacebookAuthService() {
        restTemplate = new RestTemplate();
    }

    public static String getAppId() {
        return appId;
    }

    public static String getAppSecret() {
        return appSecret;
    }

    public static String getAccessTokenRequestURL(String authCode) {
        return "https://graph.facebook.com/oauth/access_token?client_id=" + appId + "&redirect_uri=" + redirect_uri + "&client_secret=" + appSecret + "&code=" + authCode;
    }

    /**
     * Get authorization URL or request-permission-dialog URL
     *
     * @return authorization URL
     */
    public static String getAuthorizationURL(String state) {
        return "https://www.facebook.com/dialog/oauth?client_id=" + appId + "&redirect_uri=" + URLEncoder.encode(redirect_uri) + "&scope=" + StringUtils.join(perms, ",") + "&state=" + state;
    }

    /**
     * Exchange code for access token
     *
     * @param code - the request param obtained when user has authorized the app
     * @return access token
     * @throws RestClientException when error connecting to Facebook server, etc.
     */
    public FBAccessToken exchangeCodeForAccessToken(String code) throws RestClientException {
        String longLivedAccessTokenAndExpires = restTemplate.getForObject(getAccessTokenRequestURL(code), String.class);

        try {
            FBAccessToken result = new FBAccessToken();
            String params[] = longLivedAccessTokenAndExpires.split("&");
            result.setAccess_token(params[0].split("=")[1]);
            result.setExpires(Long.parseLong(params[1].split("=")[1]));
            return result;
        } catch (Exception ex) {
            log.error(">> Failed to parse access token and expires (longLivedAccessTokenAndExpires={})", longLivedAccessTokenAndExpires);
        }

        return null;
    }

    /**
     * Get a long-lived access token via the short-lived access token and its expired time and set it to the given signed request object.
     *
     * @param shortLivedAccessToken
     */
    public FBAccessToken getLongLivedAccessToken(String shortLivedAccessToken) {

        try {
            FBAccessToken result = new FBAccessToken();
            String longLivedAccessTokenAndExpires = restTemplate.getForObject("https://graph.facebook.com/oauth/access_token?grant_type=fb_exchange_token&client_id=" + appId + "&client_secret=" + appSecret + "&fb_exchange_token=" + shortLivedAccessToken, String.class);
            log.debug(">> longLivedAccessTokenAndExpires={}", longLivedAccessTokenAndExpires);

            if (StringUtils.isNotEmpty(longLivedAccessTokenAndExpires)) {
                String params[] = longLivedAccessTokenAndExpires.split("&");

                result.setAccess_token(params[0].split("=")[1]);
                result.setExpires(Long.parseLong(params[1].split("=")[1]));
            }
        } catch (RestClientException ex) {
            log.error(">> Failed to get a long-lived access token (shortLivedAccessToken={})", shortLivedAccessToken);

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        return null;
    }
}
