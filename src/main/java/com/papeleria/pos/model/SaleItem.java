package com.papeleria.pos.model;

import java.math.BigDecimal;

public class SaleItem {
    private int id;
    private int saleId;
    private int productId;
    private String productNameSnapshot;
    private String productBarcodeSnapshot;
    private BigDecimal unitPriceSnapshot;
    private int quantity;
    private BigDecimal discount;
    private BigDecimal subtotal;

    public SaleItem() {
    }

    public SaleItem(int id, int saleId, int productId, String productNameSnapshot, String productBarcodeSnapshot, BigDecimal unitPriceSnapshot, int quantity, BigDecimal discount, BigDecimal subtotal) {
        this.id = id;
        this.saleId = saleId;
        this.productId = productId;
        this.productNameSnapshot = productNameSnapshot;
        this.productBarcodeSnapshot = productBarcodeSnapshot;
        this.unitPriceSnapshot = unitPriceSnapshot;
        this.quantity = quantity;
        this.discount = discount;
        this.subtotal = subtotal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductNameSnapshot() {
        return productNameSnapshot;
    }

    public void setProductNameSnapshot(String productNameSnapshot) {
        this.productNameSnapshot = productNameSnapshot;
    }

    public String getProductBarcodeSnapshot() {
        return productBarcodeSnapshot;
    }

    public void setProductBarcodeSnapshot(String productBarcodeSnapshot) {
        this.productBarcodeSnapshot = productBarcodeSnapshot;
    }

    public BigDecimal getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public void setUnitPriceSnapshot(BigDecimal unitPriceSnapshot) {
        this.unitPriceSnapshot = unitPriceSnapshot;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "SaleItem{" +
                "id=" + id +
                ", saleId=" + saleId +
                ", productId=" + productId +
                ", productNameSnapshot='" + productNameSnapshot + '\'' +
                ", productBarcodeSnapshot='" + productBarcodeSnapshot + '\'' +
                ", unitPriceSnapshot=" + unitPriceSnapshot +
                ", quantity=" + quantity +
                ", discount=" + discount +
                ", subtotal=" + subtotal +
                '}';
    }
}
