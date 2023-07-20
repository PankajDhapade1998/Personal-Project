package com.cafe.cafemanagement.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.cafe.cafemanagement.wrapper.UserWrapper;

public interface UserService {
	
	public ResponseEntity<String> signUp(Map<String, String> requestMap);
	
	public ResponseEntity<String> login(Map<String, String> requestMap);

	public ResponseEntity<List<UserWrapper>> getAllUser();

	public ResponseEntity<String> update(Map<String, String> requestMap);

	public ResponseEntity<String> checkToken();

	public ResponseEntity<String> changePassword(Map<String, String> requestMap);

	public ResponseEntity<String> forgotPassword(Map<String, String> requestMap);
}
