package com.appsdeveloperblog.core.dto.commands;

import java.math.BigDecimal;
import java.util.UUID;

public class ProcessPaymentCommand {
	private UUID orderId;
	private UUID productId;
	private Integer productQuantity;
	private BigDecimal productPrice;
	
	
	public ProcessPaymentCommand() {
	}
	public ProcessPaymentCommand(UUID orderId, UUID productId, Integer productQuantity, BigDecimal productPrice) {
		this.orderId = orderId;
		this.productId = productId;
		this.productQuantity = productQuantity;
		this.productPrice = productPrice;
	}
	public UUID getOrderId() {
		return orderId;
	}
	public void setOrderId(UUID orderId) {
		this.orderId = orderId;
	}
	public UUID getProductId() {
		return productId;
	}
	public void setProductId(UUID productId) {
		this.productId = productId;
	}
	public Integer getProductQuantity() {
		return productQuantity;
	}
	public void setProductQuantity(Integer productQuantity) {
		this.productQuantity = productQuantity;
	}
	public BigDecimal getProductPrice() {
		return productPrice;
	}
	public void setProductPrice(BigDecimal productPrice) {
		this.productPrice = productPrice;
	}
	
	
}
