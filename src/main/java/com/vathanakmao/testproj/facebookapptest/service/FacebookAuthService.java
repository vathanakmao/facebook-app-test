package com.vathanakmao.testproj.facebookapptest.service;


import com.vathanakmao.testproj.facebookapptest.model.FBSignedRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
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

    public static String getLoginRedirectURL() {
        return "https://graph.facebook.com/oauth/authorize?client_id=" + appId + "&display=page&redirect_uri=" + redirect_uri + "&scope=" + StringUtils.join(perms, ",");
    }

    public static String getAuthURL(String authCode) {
        return "https://graph.facebook.com/oauth/access_token?client_id=" + appId + "&redirect_uri=" + redirect_uri + "&client_secret=" + appSecret + "&code=" + authCode;
    }

    /**
     * Get authorization URL or request-permission-dialog URL
     *
     * @return authorization URL
     */
    public static String getAuthURL() {
        return "https://www.facebook.com/dialog/oauth?client_id=" + appId + "&redirect_uri=" + URLEncoder.encode(redirect_uri) + "&scope=" + StringUtils.join(perms, ",");
    }

    /**
     * Get a long-lived access token via the short-lived access token and its expired time and set it to the given signed request object.
     *
     * @param signedRequest
     */
    public void generateLongLivedAccessToken(FBSignedRequest signedRequest) {
        String shortLivedAccessToken = signedRequest.getOauth_token();

        try {
            String longLivedAccessTokenAndExpires = restTemplate.getForObject("https://graph.facebook.com/oauth/access_token?grant_type=fb_exchange_token&client_id=" + appId + "&client_secret=" + appSecret + "&fb_exchange_token=" + shortLivedAccessToken, String.class);
            log.debug(">> longLivedAccessTokenAndExpires={}", longLivedAccessTokenAndExpires);

            if (StringUtils.isNotEmpty(longLivedAccessTokenAndExpires)) {
                String params[] = longLivedAccessTokenAndExpires.split("&");

                signedRequest.setOauth_token(params[0].split("=")[1]);
                signedRequest.setExpires(Long.parseLong(params[1].split("=")[1]));
            }
        } catch (RestClientException ex) {
            log.error(">> Failed to get a long-lived access token (userId={}, shortLivedAccessToken={})", new Object[]{signedRequest.getUser_id(), shortLivedAccessToken});

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    public static FBSignedRequest getFacebookSignedRequest(String signedRequest) throws Exception {
        if (StringUtils.isEmpty(signedRequest)) {
            return null;
        }

        String payLoad = signedRequest.split("\\.", 2)[1];
        log.debug(">> payload: {}", payLoad);

        payLoad = payLoad.replace("-", "+").replace("_", "/").trim();

        log.debug("<< payload: {}", payLoad);

        Base64 base64 = new Base64(true);
        String jsonString = new String(base64.decode(payLoad.getBytes("UTF8")));

        log.debug(">> jsonString: {}", jsonString);

        FBSignedRequest result = new ObjectMapper().readValue(jsonString, FBSignedRequest.class);
        return result;
    }

    public static FBSignedRequest getFacebookSignedRequestFromSession(HttpServletRequest request) {
        return (FBSignedRequest) request.getSession().getAttribute("signedRequest");
    }
}
