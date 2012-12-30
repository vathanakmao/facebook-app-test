package com.vathanakmao.testproj.facebookapptest.service;

import com.vathanakmao.testproj.facebookapptest.model.FBUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class FacebookOperations {
    private static final Logger log = LoggerFactory.getLogger(FacebookOperations.class);

    private RestTemplate restTemplate;

    public FacebookOperations() {
        restTemplate = new RestTemplate();
    }

    /**
     * Post a message on the wall of the currently logged-in user.
     *
     * @param message
     * @param accessToken
     */
    public void postOnWall(String message, String accessToken) {

    }

    /**
     * Get currently logged-in user's account information.
     */
    public FBUser getMyAccount(String accessToken) {
        try {
            FBUser user = restTemplate.getForObject("https://graph.facebook.com/me?access_token=" + accessToken, FBUser.class);
            return user;
        } catch (RestClientException ex) {
            log.error(">> Failed to get my account (message: {})", ex.getMessage());
        }

        return null;
    }
}
