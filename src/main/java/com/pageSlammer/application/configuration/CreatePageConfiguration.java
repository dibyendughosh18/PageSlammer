package com.pageSlammer.application.configuration;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.annotation.Order;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@Order(2)
public class CreatePageConfiguration extends WebSecurityConfigurerAdapter{

	private static String REALM="zU`qek6w-{;{Bg3M";
    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
    	auth.inMemoryAuthentication().withUser("[B@76ed55282C14DA21-1954-4798-A628-2BC4810BF5401519205568107814b6262@B[").password("[B@76ed5528A9DC144D-F6D3-433F-9E3F-42833DEAE9EE1519205703772[B@2c7b84de").roles("USER");	
    }
 
    @Override
    protected void configure(HttpSecurity http) throws Exception {
  
      http.csrf().disable().httpBasic().realmName(REALM).authenticationEntryPoint(getBasicAuthEntryPoint())
        .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);//We don't need session.
      http.httpBasic().and().antMatcher( "/PageSlammer/authorized/*").authorizeRequests().anyRequest().authenticated();
    }
	
	@Bean
    public CustomBasicAuthenticationEntryPoint getBasicAuthEntryPoint(){
        return new CustomBasicAuthenticationEntryPoint();
    }
     
    /* To allow Pre-flight [OPTIONS] request from browser */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
    }
}
