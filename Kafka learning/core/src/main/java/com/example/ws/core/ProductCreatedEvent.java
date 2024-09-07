package com.example.ws.core;

import lombok.Data;

@Data
public class ProductCreatedEvent {
	private String productID;
	private String title ;
	private String price ;
	private String quantity ;
}
