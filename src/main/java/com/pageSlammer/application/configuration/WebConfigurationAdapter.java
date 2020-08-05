package com.pageSlammer.application.configuration;

import org.springframework.context.annotation.Configuration;

import org.springframework.core.annotation.Order;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(1)
public class WebConfigurationAdapter extends WebSecurityConfigurerAdapter {
 
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().antMatcher("/PageSlammer/register").authorizeRequests().anyRequest().permitAll()
        .antMatchers("/PageSlammer/login").permitAll()
	    .antMatchers("/PageSlammer/createGuestPage").permitAll()
	    .antMatchers("/PageSlammer/allPages").permitAll()
	    .antMatchers("/PageSlammer/facebookLogin").permitAll()
	    .antMatchers("/PageSlammer/emailLinkToResetPassword").permitAll()
	    .antMatchers("/PageSlammer/uploadImage").permitAll()
	    .antMatchers("/PageSlammer/updateGuestPage").permitAll()
	    .antMatchers("/PageSlammer/checkTocken").permitAll()
	    .antMatchers("/PageSlammer/resetPassword").permitAll()
	    .antMatchers("/PageSlammer/getClickedPage").permitAll()
	    .antMatchers("/PageSlammer/deleteImage").permitAll();
    }
}
