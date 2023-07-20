package com.cafe.cafemanagement.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cafe.cafemanagement.JWT.JwtFilter;
import com.cafe.cafemanagement.POJO.Category;
import com.cafe.cafemanagement.constents.CafeConstant;
import com.cafe.cafemanagement.dao.CategoryDao;
import com.cafe.cafemanagement.service.CategoryService;
import com.cafe.cafemanagement.utils.CafeUtils;
import com.google.common.base.Strings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	CategoryDao categoryDao;

	@Autowired
	JwtFilter jwtFilter;

	@Override
	public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
		try {
			if (jwtFilter.isAdmin()) {
				if (validatecategoryMap(requestMap, false)) {
					categoryDao.save(getCategoryFromMap(requestMap, false));
					return CafeUtils.getResponseEntity("Category Added Successfully", HttpStatus.OK);
				}
			} else {
				return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORIZED_ACESS, HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private boolean validatecategoryMap(Map<String, String> requestMap, Boolean validateId) {
		if (requestMap.containsKey("name")) {
			if (requestMap.containsKey("id") && validateId) {
				return true;
			} else if (!validateId) {
				return true;
			}
		}
		return false;
	}

	private Category getCategoryFromMap(Map<String, String> requestMap, Boolean isAdd) {
		Category category = new Category();
		if (isAdd) {
			category.setId(Integer.parseInt(requestMap.get("id")));
		}
		category.setName(requestMap.get("name"));
		return category;
	}

	@Override
	public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
		try {
			if (!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase("true")) {
				log.info("Inside if");
				return new ResponseEntity<List<Category>>(categoryDao.getALlCategory(), HttpStatus.OK);
			}
			return new ResponseEntity<List<Category>>(categoryDao.findAll(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<List<Category>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
		try {
			if (jwtFilter.isAdmin()) {
				if (validatecategoryMap(requestMap, true)) {
					Optional optional = categoryDao.findById(Integer.parseInt(requestMap.get("id")));
					if (!optional.isEmpty()) {
						categoryDao.save(getCategoryFromMap(requestMap, true));
						return CafeUtils.getResponseEntity("Category Updated Successfully", HttpStatus.OK);
					} else {
						return CafeUtils.getResponseEntity("category id does not exist", HttpStatus.OK);
					}
				}
				return CafeUtils.getResponseEntity(CafeConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
			} else {
				return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORIZED_ACESS, HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
