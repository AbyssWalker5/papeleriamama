package com.papeleria.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CashSession {
    private int id;
    private BigDecimal initialFund;
    private String status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;

    public CashSession() {
    }

    public CashSession(int id, BigDecimal initialFund, String status, LocalDateTime openedAt, LocalDateTime closedAt) {
        this.id = id;
        this.initialFund = initialFund;
        this.status = status;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getInitialFund() {
        return initialFund;
    }

    public void setInitialFund(BigDecimal initialFund) {
        this.initialFund = initialFund;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    @Override
    public String toString() {
        return "CashSession{" +
                "id=" + id +
                ", initialFund=" + initialFund +
                ", status='" + status + '\'' +
                ", openedAt=" + openedAt +
                ", closedAt=" + closedAt +
                '}';
    }
}
