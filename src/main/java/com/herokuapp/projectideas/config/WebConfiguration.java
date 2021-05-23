package com.herokuapp.projectideas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//This class is necessary to forward requests to root so they can be handled by the React Router
//Without this class, non-root requests are not handled by the React Router
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry
            .addViewController("/**/{path:[^\\.]*}")
            .setViewName("forward:/");
    }
}
