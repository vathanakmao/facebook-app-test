package com.vathanakmao.testproj.facebookapptest.web.controller;

import com.vathanakmao.testproj.facebookapptest.model.FBUser;
import com.vathanakmao.testproj.facebookapptest.service.FacebookAuthService;
import com.vathanakmao.testproj.facebookapptest.service.FacebookOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping(value = "/")
public class HomeController {
    private FacebookOperations facebookOperations;

    public HomeController() {
        facebookOperations = new FacebookOperations();
    }

    /**
     * When users access our home page on Facebook, Facebook makes a post request to our application hosted on GAE
     * so our handler method must support POST method too.
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public String getHomePage(HttpServletRequest request, Model model) {
        FBUser user = facebookOperations.getMyAccount(FacebookAuthService.getFacebookSignedRequestFromSession(request).getOauth_token());
        model.addAttribute("user", user);

        return "index";
    }

}
