package com.mgorshkov.sheepitcontrollerapi;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class IndexController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(){
        return "error";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
