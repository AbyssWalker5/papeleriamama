package com.papeleria.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Sale {
    private int id;
    private String saleNumber;
    private String idempotencyToken;
    private int cashSessionId;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;
    private String paymentMethod;
    private BigDecimal amountPaid;
    private BigDecimal change;
    private String status;
    private LocalDateTime createdAt;
    private transient List<SaleItem> items;

    public Sale() {
    }

    public Sale(int id, String saleNumber, String idempotencyToken, int cashSessionId, BigDecimal subtotal, BigDecimal discount, BigDecimal total, String paymentMethod, BigDecimal amountPaid, BigDecimal change, String status, LocalDateTime createdAt, List<SaleItem> items) {
        this.id = id;
        this.saleNumber = saleNumber;
        this.idempotencyToken = idempotencyToken;
        this.cashSessionId = cashSessionId;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.amountPaid = amountPaid;
        this.change = change;
        this.status = status;
        this.createdAt = createdAt;
        this.items = items;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSaleNumber() {
        return saleNumber;
    }

    public void setSaleNumber(String saleNumber) {
        this.saleNumber = saleNumber;
    }

    public String getIdempotencyToken() {
        return idempotencyToken;
    }

    public void setIdempotencyToken(String idempotencyToken) {
        this.idempotencyToken = idempotencyToken;
    }

    public int getCashSessionId() {
        return cashSessionId;
    }

    public void setCashSessionId(int cashSessionId) {
        this.cashSessionId = cashSessionId;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getChange() {
        return change;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", saleNumber='" + saleNumber + '\'' +
                ", idempotencyToken='" + idempotencyToken + '\'' +
                ", cashSessionId=" + cashSessionId +
                ", subtotal=" + subtotal +
                ", discount=" + discount +
                ", total=" + total +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", amountPaid=" + amountPaid +
                ", change=" + change +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", items=" + items +
                '}';
    }
}
