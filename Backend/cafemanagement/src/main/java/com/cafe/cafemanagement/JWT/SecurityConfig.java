package com.cafe.cafemanagement.JWT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	CustomerUserDetailsService customerUserDetailsService;

	@Autowired
	JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Autowired
	JwtFilter jwtFilter;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// TODO Auto-generated method stub
		auth.userDetailsService(customerUserDetailsService);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
//		return new BCryptPasswordEncoder();
		return NoOpPasswordEncoder.getInstance();
	}

	@Bean(name = BeanIds.AUTHENTICATION_MANAGER)
	@Override
	protected AuthenticationManager authenticationManager() throws Exception {
		// TODO Auto-generated method stub
		return super.authenticationManager();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// TODO Auto-generated method stub
		http.cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues()).and().csrf()
				.disable().authorizeRequests().antMatchers("/user/login","/user/forgotPassword","/user/signup")
				.permitAll().anyRequest().authenticated().and().exceptionHandling().and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

	}

//	@Bean
//	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//		http.csrf(csrf -> csrf.disable())
//		.cors(cors -> cors.disable())
//		.authorizeHttpRequests(
//		  auth -> auth.antMatchers("/user/login","/user/forgotPassword","/user/signup")
//		  .permitAll()
//		  .anyRequest()
//		  .authenticated())
//		 .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
//		.sessionManagement(sessMng -> sessMng.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//				
//	http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//	return http.build();
//	}
//
}
