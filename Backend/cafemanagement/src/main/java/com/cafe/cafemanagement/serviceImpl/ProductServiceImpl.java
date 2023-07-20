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
import com.cafe.cafemanagement.POJO.Product;
import com.cafe.cafemanagement.constents.CafeConstant;
import com.cafe.cafemanagement.dao.ProductDao;
import com.cafe.cafemanagement.service.ProductService;
import com.cafe.cafemanagement.utils.CafeUtils;
import com.cafe.cafemanagement.wrapper.ProductWrapper;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	JwtFilter jwtFilter;

	@Autowired
	ProductDao productDao;

	@Override
	public ResponseEntity<String> addNewProduct(Map<String, String> requestmap) {
		try {
			if (jwtFilter.isAdmin()) {
				if (validateProductMap(requestmap, false)) {
					productDao.save(getProductFromMap(requestmap, false));
					return CafeUtils.getResponseEntity("Product Added Successfully", HttpStatus.OK);
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

	private boolean validateProductMap(Map<String, String> requestmap, boolean validateId) {
		if (requestmap.containsKey("name")) {
			if (requestmap.containsKey("id") && validateId) {
				return true;
			} else if (!validateId) {
				return true;
			}
		}
		return false;
	}

	private Product getProductFromMap(Map<String, String> requestmap, boolean isAdd) {
		Category category = new Category();
		category.setId(Integer.parseInt(requestmap.get("categoryId")));

		Product product = new Product();
		if (isAdd) {
			product.setId(Integer.parseInt(requestmap.get("id")));
		} else {
			product.setStatus("true");
		}
		product.setCategory(category);
		product.setName(requestmap.get("name"));
		product.setDescription(requestmap.get("description"));
		product.setPrice(Integer.parseInt(requestmap.get("price")));
		return product;
	}

	@Override
	public ResponseEntity<List<ProductWrapper>> getAllProduct() {
		try {
			return new ResponseEntity<>(productDao.getAllProduct(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);

	}

	@Override
	public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
		try {
			if (jwtFilter.isAdmin()) {
				if (validateProductMap(requestMap, true)) {
					Optional<Product> optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
					if (!optional.isEmpty()) {
						Product product = getProductFromMap(requestMap, true);
						product.setStatus(optional.get().getStatus());
						productDao.save(product);
						return CafeUtils.getResponseEntity("Product Updated Successfully", HttpStatus.OK);
					} else {
						return CafeUtils.getResponseEntity("Product Id not exist", HttpStatus.OK);
					}
				} else {
					return CafeUtils.getResponseEntity(CafeConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
				}
			} else {
				return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORIZED_ACESS, HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> deleteProduct(Integer id) {
		try {
			if (jwtFilter.isAdmin()) {
				Optional optional = productDao.findById(id);
				if (!optional.isEmpty()) {
					productDao.deleteById(id);
					return CafeUtils.getResponseEntity("Product deleted successfully", HttpStatus.OK);
				} else {
					return CafeUtils.getResponseEntity("Product ID does not exist", HttpStatus.OK);
				}
			} else {
				return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORIZED_ACESS, HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
		try {
			if (jwtFilter.isAdmin()) {
				Optional optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
				if (!optional.isEmpty()) {
					productDao.updateProductStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
					return CafeUtils.getResponseEntity("Product status updated successfully", HttpStatus.OK);
				} else {
					return CafeUtils.getResponseEntity("Product does not exist", HttpStatus.OK);
				}
			} else {
				return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORIZED_ACESS, HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
		try {
			return new ResponseEntity<>(productDao.getProductByCategory(id),HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<ProductWrapper> getProductById(Integer id) {
		try {
			return new ResponseEntity<>(productDao.getProductById(id),HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(new ProductWrapper(),HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	

}
