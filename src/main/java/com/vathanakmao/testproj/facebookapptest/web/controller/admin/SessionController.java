package com.vathanakmao.testproj.facebookapptest.web.controller.admin;

import com.vathanakmao.testproj.facebookapptest.service.FacebookOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping(value = "/admin/session")
public class SessionController {

    /**
     * When users access our home page on Facebook, Facebook makes a post request to our application hosted on GAE
     * so our handler method must support POST method too.
     */
    @RequestMapping(value="clear", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String clear(HttpServletRequest request) {
        request.getSession().invalidate();
        return "cleared";
    }

}
