package com.main;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CompositeFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


@SpringBootApplication
@EnableOAuth2Client
@RestController
@EnableJpaRepositories
@Configuration
@EnableResourceServer
@EnableAuthorizationServer
@Order(6)
public class OauthApplication extends WebSecurityConfigurerAdapter{

    @Autowired
    OAuth2ClientContext oAuth2ClientContext;


    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http.antMatcher("/**")
                .authorizeRequests()
                .antMatchers("/","/login**","/webjars/**","/home.html","/profile.html","index.html","/addFbUser")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and().logout().logoutSuccessUrl("/").permitAll()
                .and().csrf().csrfTokenRepository(csrfTokenRepository())
                .and().addFilterAfter(csrfHeaderFilter(), CsrfFilter.class)
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
        http.csrf().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.inMemoryAuthentication().withUser("user").password("secret").roles("ADMIN");
    }



    private Filter csrfHeaderFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                if (csrf != null) {
                    Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
                    String token = csrf.getToken();
                    if (cookie == null || token != null && !token.equals(cookie.getValue())) {
                        cookie = new Cookie("XSRF-TOKEN", token);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                    }
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }

    private Filter ssoFilter(){
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(ssoFilter(facebook(),"/login/facebook"));
        filter.setFilters(filters);
        return filter;

    }

    private Filter ssoFilter(ClientResources client,String path){
        OAuth2ClientAuthenticationProcessingFilter facebookFilter = new OAuth2ClientAuthenticationProcessingFilter(path);
        OAuth2RestTemplate facebookTemplate = new OAuth2RestTemplate(client.getClient(),oAuth2ClientContext);
        facebookFilter.setRestTemplate(facebookTemplate);
        facebookFilter.setTokenServices(new UserInfoTokenServices(client.getResource().getUserInfoUri(),client.getClient().getClientId()));
        return facebookFilter;
    }


    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter){
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        registrationBean.setOrder(-100);
        return registrationBean;
    }

    @Bean
    @ConfigurationProperties("facebook")
    ClientResources facebook(){
        return new ClientResources();
    }


    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter{
        @Override
        public void configure(HttpSecurity http) throws Exception{
            http.antMatcher("/me").authorizeRequests().anyRequest().authenticated();
        }
    }

    public static void main(String[] args) {
       SpringApplication.run(OauthApplication.class, args);
	}
}

class ClientResources{
    private OAuth2ProtectedResourceDetails client = new AuthorizationCodeResourceDetails();
    private ResourceServerProperties resource = new ResourceServerProperties();

    public OAuth2ProtectedResourceDetails getClient(){
        return client;
    }

    public ResourceServerProperties getResource(){
        return resource;
    }
}

