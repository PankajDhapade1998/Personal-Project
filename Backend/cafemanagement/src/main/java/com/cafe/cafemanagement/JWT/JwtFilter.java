package com.cafe.cafemanagement.JWT;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;

@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CustomerUserDetailsService service;

	Claims claims = null;

	private String userName = null;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
//
		if (request.getServletPath().matches("/user/login|user/forgotPassword|/user/signup")) {
			filterChain.doFilter(request, response);
		} else {
			String autorizationHeader = request.getHeader("Authorization");
			String token = null;

			if (autorizationHeader != null && autorizationHeader.startsWith("Bearer ")) {
				token = autorizationHeader.substring(7);
				userName = jwtUtil.extractUsername(token);
				claims = jwtUtil.extractAllClaims(token);
			}

			if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = service.loadUserByUsername(userName);
				if(jwtUtil.validateToken(token, userDetails)) {
					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
							new UsernamePasswordAuthenticationToken(userDetails, null ,userDetails.getAuthorities());
					usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
				}
			}
			filterChain.doFilter(request, response);
		}

	}
	
	public boolean isAdmin() {
		return "admin".equalsIgnoreCase((String) claims.get("role"));
	}
	
	public boolean isUser() {
		return "user".equalsIgnoreCase((String) claims.get("role"));
	}
	
	public String getCurrentUser() {
		return userName;
	}

}
