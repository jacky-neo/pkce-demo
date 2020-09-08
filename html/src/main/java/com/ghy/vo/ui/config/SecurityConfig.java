package com.ghy.vo.ui.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class SecurityConfig extends WebSecurityConfigurerAdapter{
	private final static Logger logger = LoggerFactory.getLogger(SecurityConfig.class) ;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		//方法1：关闭CSRFM
		.csrf().disable()
		.authorizeRequests()
		    .antMatchers("/**").permitAll()
		;


	}

	public void configure(WebSecurity web) throws Exception {
	    web
	       .ignoring()
	       .antMatchers("/static/**");
	}
	
	
}




