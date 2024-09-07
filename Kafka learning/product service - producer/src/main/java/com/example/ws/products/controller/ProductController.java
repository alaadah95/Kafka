package com.example.ws.products.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ws.products.model.CreateProductRestModel;
import com.example.ws.products.services.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {
	
	private ProductService productService ;
	
	
	
	public ProductController(ProductService productService) {
		this.productService = productService;
	}



	@PostMapping
	public ResponseEntity<String> createProduct(@RequestBody CreateProductRestModel model){
		String productId = productService.createProduct(model);
		return ResponseEntity.status(HttpStatus.CREATED).body(productId);
	}
	
}
