package com.main;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;

/**
 * Created by nagypeter on 2016. 03. 19..
 */

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties(MultipartProperties.class)
public class Config {

    @Bean
    public MultipartConfigElement multipartConfigElement(){
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("10Mb");
        factory.setMaxRequestSize("10Mb");
        return factory.createMultipartConfig();
    }

}
