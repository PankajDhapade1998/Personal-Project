package com.cafe.cafemanagement.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.cafe.cafemanagement.JWT.CustomerUserDetailsService;
import com.cafe.cafemanagement.JWT.JwtFilter;
import com.cafe.cafemanagement.JWT.JwtUtil;
import com.cafe.cafemanagement.POJO.User;
import com.cafe.cafemanagement.constents.CafeConstant;
import com.cafe.cafemanagement.dao.UserDao;
import com.cafe.cafemanagement.service.UserService;
import com.cafe.cafemanagement.utils.CafeUtils;
import com.cafe.cafemanagement.utils.EmailUtils;
import com.cafe.cafemanagement.wrapper.UserWrapper;
import com.google.common.base.Strings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserDao userDao;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	CustomerUserDetailsService customerUserDetailsService;

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	JwtFilter jwtFilter;

	@Autowired
	EmailUtils emailUtils;

	@Override
	public ResponseEntity<String> signUp(Map<String, String> requestMap) {
		log.info("Inside signUp {}", requestMap);

		try {
			if (validateSignUpMap(requestMap)) {
				User user = userDao.findByEmailId(requestMap.get("email"));
				if (Objects.isNull(user)) {
					userDao.save(getUserFromMap(requestMap));
					return CafeUtils.getResponseEntity("Successdully Registered", HttpStatus.OK);
				} else {
					return CafeUtils.getResponseEntity("Email ALready Exists", HttpStatus.BAD_REQUEST);
				}
			} else {
				return CafeUtils.getResponseEntity(CafeConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private boolean validateSignUpMap(Map<String, String> requestMap) {
		if (requestMap.containsKey("name") && requestMap.containsKey("contactNumber") && requestMap.containsKey("email")
				&& requestMap.containsKey("password")) {
			return true;
		}
		return false;
	}

	private User getUserFromMap(Map<String, String> requestMap) {
		User user = new User();
		user.setName(requestMap.get("name"));
		user.setContactNumber(requestMap.get("contactNumber"));
		user.setEmail(requestMap.get("email"));
		user.setPassword(requestMap.get("password"));
		user.setStatus("false");
		user.setRole("user");
		return user;
	}

	@Override
	public ResponseEntity<String> login(Map<String, String> requestMap) {
		log.info("Inside login");
		try {
			Authentication auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password")));
			if (auth.isAuthenticated()) {
				if (customerUserDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")) {
					return new ResponseEntity<String>(
							"{\"token\":\""
									+ jwtUtil.generateToken(customerUserDetailsService.getUserDetail().getEmail(),
											customerUserDetailsService.getUserDetail().getRole())
									+ "\"}",
							HttpStatus.OK);
				} else {
					return new ResponseEntity<String>("{\"message\":\"" + "wait for admin approval." + "\"}",
							HttpStatus.BAD_REQUEST);
				}
			}
		} catch (Exception e) {
			log.error("{}", e);
		}
		return new ResponseEntity<String>("{\"message\":\"" + "Bad Credentials" + "\"}", HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<List<UserWrapper>> getAllUser() {
		try {
			if (jwtFilter.isAdmin()) {
				return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> update(Map<String, String> requestMap) {
		try {
			if (jwtFilter.isAdmin()) {
				Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
				if (!optional.isEmpty()) {
					userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
					sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userDao.getAllAdmin());
					return CafeUtils.getResponseEntity("User Status Updated Successfully", HttpStatus.OK);
				} else {
					return CafeUtils.getResponseEntity("User Id doesn't exist", HttpStatus.OK);
				}
			} else {
				return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORIZED_ACESS, HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
		allAdmin.remove(jwtFilter.getCurrentUser());
		if (status != null && status.equalsIgnoreCase("true")) {
			emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Approved",
					"User :- " + user + " \n is approved by \nAdmin :- " + jwtFilter.getCurrentUser(), allAdmin);
		} else {
			emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Disabled",
					"User :- " + user + " \n is disabled by \nAdmin :-  " + jwtFilter.getCurrentUser(), allAdmin);
		}

	}

	@Override
	public ResponseEntity<String> checkToken() {
		return CafeUtils.getResponseEntity("true", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
		try {
			User userObj = userDao.findByEmail(jwtFilter.getCurrentUser());
			if (!userObj.equals(null)) {
				if (userObj.getPassword().equals(requestMap.get("oldPassword"))) {
					userObj.setPassword(requestMap.get("newPassword"));
					userDao.save(userObj);
					return CafeUtils.getResponseEntity("Password updated Successfully", HttpStatus.OK);
				}
				return CafeUtils.getResponseEntity("Incorrect Old Password", HttpStatus.BAD_REQUEST);
			}
			return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
		try {
			User user = userDao.findByEmail(requestMap.get("email"));
			if (!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail()))
				emailUtils.forgotMail(user.getEmail(), "Credentials by Cafe Management System", user.getPassword());

			return CafeUtils.getResponseEntity("Check your mail for Credentials", HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	

}
