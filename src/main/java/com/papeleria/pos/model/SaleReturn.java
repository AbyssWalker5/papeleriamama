package com.papeleria.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SaleReturn {
    private int id;
    private int saleId;
    private String returnNumber;
    private BigDecimal total;
    private String reason;
    private String paymentMethodRefund;
    private String status;
    private LocalDateTime createdAt;
    private transient List<ReturnItem> items;

    public SaleReturn() {
    }

    public SaleReturn(int id, int saleId, String returnNumber, BigDecimal total, String reason, String paymentMethodRefund, String status, LocalDateTime createdAt, List<ReturnItem> items) {
        this.id = id;
        this.saleId = saleId;
        this.returnNumber = returnNumber;
        this.total = total;
        this.reason = reason;
        this.paymentMethodRefund = paymentMethodRefund;
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

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public String getReturnNumber() {
        return returnNumber;
    }

    public void setReturnNumber(String returnNumber) {
        this.returnNumber = returnNumber;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPaymentMethodRefund() {
        return paymentMethodRefund;
    }

    public void setPaymentMethodRefund(String paymentMethodRefund) {
        this.paymentMethodRefund = paymentMethodRefund;
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

    public List<ReturnItem> getItems() {
        return items;
    }

    public void setItems(List<ReturnItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "SaleReturn{" +
                "id=" + id +
                ", saleId=" + saleId +
                ", returnNumber='" + returnNumber + '\'' +
                ", total=" + total +
                ", reason='" + reason + '\'' +
                ", paymentMethodRefund='" + paymentMethodRefund + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", items=" + items +
                '}';
    }
}
