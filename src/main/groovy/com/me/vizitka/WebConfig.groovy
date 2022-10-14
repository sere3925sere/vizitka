//package com.me.vizitka
//
//import org.springframework.context.annotation.Configuration
//import org.springframework.http.CacheControl
//import org.springframework.web.reactive.config.EnableWebFlux
//import org.springframework.web.reactive.config.ResourceHandlerRegistry
//import org.springframework.web.reactive.config.WebFluxConfigurer
//
//import java.util.concurrent.TimeUnit
//
//@Configuration
//@EnableWebFlux
//class WebConfig implements WebFluxConfigurer {
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/foto.jpg")
//                .addResourceLocations("/public/")
//                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
//    }
//}
