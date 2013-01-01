package com.vathanakmao.testproj.facebookapptest.web.controller;

import org.springframework.social.connect.Connection;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping(value = "/")
public class HomeController {

    @Inject
    private FacebookConnectionFactory facebookConnectionFactory;


    /**
     * When users access our home page on Facebook, Facebook makes a post request to our application hosted on GAE
     * so our handler method must support POST method too.
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public String getHomePage(HttpServletRequest request, Model model) {
        AccessGrant accessGrant = (AccessGrant) request.getSession().getAttribute("accessGrant");
        Connection<Facebook> connection = facebookConnectionFactory.createConnection(accessGrant);

        Facebook facebook = connection.getApi();
        model.addAttribute("user", facebook.userOperations().getUserProfile());
        return "index";
    }

}
