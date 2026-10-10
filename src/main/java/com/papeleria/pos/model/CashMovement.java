package com.papeleria.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CashMovement {
    private int id;
    private int cashSessionId;
    private String type;
    private BigDecimal amount;
    private String reason;
    private Integer relatedSaleId;
    private Integer relatedReturnId;
    private LocalDateTime createdAt;

    public CashMovement() {
    }

    public CashMovement(int id, int cashSessionId, String type, BigDecimal amount, String reason, Integer relatedSaleId, Integer relatedReturnId, LocalDateTime createdAt) {
        this.id = id;
        this.cashSessionId = cashSessionId;
        this.type = type;
        this.amount = amount;
        this.reason = reason;
        this.relatedSaleId = relatedSaleId;
        this.relatedReturnId = relatedReturnId;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCashSessionId() {
        return cashSessionId;
    }

    public void setCashSessionId(int cashSessionId) {
        this.cashSessionId = cashSessionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getRelatedSaleId() {
        return relatedSaleId;
    }

    public void setRelatedSaleId(Integer relatedSaleId) {
        this.relatedSaleId = relatedSaleId;
    }

    public Integer getRelatedReturnId() {
        return relatedReturnId;
    }

    public void setRelatedReturnId(Integer relatedReturnId) {
        this.relatedReturnId = relatedReturnId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CashMovement{" +
                "id=" + id +
                ", cashSessionId=" + cashSessionId +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", reason='" + reason + '\'' +
                ", relatedSaleId=" + relatedSaleId +
                ", relatedReturnId=" + relatedReturnId +
                ", createdAt=" + createdAt +
                '}';
    }
}
