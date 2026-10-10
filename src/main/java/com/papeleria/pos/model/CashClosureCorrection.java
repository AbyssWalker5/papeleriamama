package com.papeleria.pos.model;

import java.time.LocalDateTime;

public class CashClosureCorrection {
    private int id;
    private int cashClosureId;
    private String fieldCorrected;
    private String oldValue;
    private String newValue;
    private String justification;
    private LocalDateTime createdAt;

    public CashClosureCorrection() {
    }

    public CashClosureCorrection(int id, int cashClosureId, String fieldCorrected, String oldValue, String newValue, String justification, LocalDateTime createdAt) {
        this.id = id;
        this.cashClosureId = cashClosureId;
        this.fieldCorrected = fieldCorrected;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.justification = justification;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCashClosureId() {
        return cashClosureId;
    }

    public void setCashClosureId(int cashClosureId) {
        this.cashClosureId = cashClosureId;
    }

    public String getFieldCorrected() {
        return fieldCorrected;
    }

    public void setFieldCorrected(String fieldCorrected) {
        this.fieldCorrected = fieldCorrected;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CashClosureCorrection{" +
                "id=" + id +
                ", cashClosureId=" + cashClosureId +
                ", fieldCorrected='" + fieldCorrected + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                ", justification='" + justification + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
