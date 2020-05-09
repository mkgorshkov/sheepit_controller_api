package com.mgorshkov.sheepitcontrollerapi;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(){
        return "<h1>Something went wrong</h1><p>Please try your request again.</p>";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
