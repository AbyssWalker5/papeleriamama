package com.papeleria.pos.model;

import java.math.BigDecimal;

public class ReturnItem {
    private int id;
    private int returnId;
    private int productId;
    private int saleItemId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public ReturnItem() {
    }

    public ReturnItem(int id, int returnId, int productId, int saleItemId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.id = id;
        this.returnId = returnId;
        this.productId = productId;
        this.saleItemId = saleItemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReturnId() {
        return returnId;
    }

    public void setReturnId(int returnId) {
        this.returnId = returnId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getSaleItemId() {
        return saleItemId;
    }

    public void setSaleItemId(int saleItemId) {
        this.saleItemId = saleItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "ReturnItem{" +
                "id=" + id +
                ", returnId=" + returnId +
                ", productId=" + productId +
                ", saleItemId=" + saleItemId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                '}';
    }
}
