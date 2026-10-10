package com.papeleria.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CashClosure {
    private int id;
    private int cashSessionId;
    private BigDecimal initialFund;
    private BigDecimal cashSales;
    private BigDecimal cardSales;
    private BigDecimal transferSales;
    private BigDecimal otherSales;
    private BigDecimal totalSales;
    private int totalTransactions;
    private BigDecimal cashIn;
    private BigDecimal cashOut;
    private BigDecimal returns;
    private BigDecimal expectedCash;
    private BigDecimal countedCash;
    private BigDecimal difference;
    private String observations;
    private LocalDateTime createdAt;

    public CashClosure() {
    }

    public CashClosure(int id, int cashSessionId, BigDecimal initialFund, BigDecimal cashSales, BigDecimal cardSales, BigDecimal transferSales, BigDecimal otherSales, BigDecimal totalSales, int totalTransactions, BigDecimal cashIn, BigDecimal cashOut, BigDecimal returns, BigDecimal expectedCash, BigDecimal countedCash, BigDecimal difference, String observations, LocalDateTime createdAt) {
        this.id = id;
        this.cashSessionId = cashSessionId;
        this.initialFund = initialFund;
        this.cashSales = cashSales;
        this.cardSales = cardSales;
        this.transferSales = transferSales;
        this.otherSales = otherSales;
        this.totalSales = totalSales;
        this.totalTransactions = totalTransactions;
        this.cashIn = cashIn;
        this.cashOut = cashOut;
        this.returns = returns;
        this.expectedCash = expectedCash;
        this.countedCash = countedCash;
        this.difference = difference;
        this.observations = observations;
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

    public BigDecimal getInitialFund() {
        return initialFund;
    }

    public void setInitialFund(BigDecimal initialFund) {
        this.initialFund = initialFund;
    }

    public BigDecimal getCashSales() {
        return cashSales;
    }

    public void setCashSales(BigDecimal cashSales) {
        this.cashSales = cashSales;
    }

    public BigDecimal getCardSales() {
        return cardSales;
    }

    public void setCardSales(BigDecimal cardSales) {
        this.cardSales = cardSales;
    }

    public BigDecimal getTransferSales() {
        return transferSales;
    }

    public void setTransferSales(BigDecimal transferSales) {
        this.transferSales = transferSales;
    }

    public BigDecimal getOtherSales() {
        return otherSales;
    }

    public void setOtherSales(BigDecimal otherSales) {
        this.otherSales = otherSales;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public BigDecimal getCashIn() {
        return cashIn;
    }

    public void setCashIn(BigDecimal cashIn) {
        this.cashIn = cashIn;
    }

    public BigDecimal getCashOut() {
        return cashOut;
    }

    public void setCashOut(BigDecimal cashOut) {
        this.cashOut = cashOut;
    }

    public BigDecimal getReturns() {
        return returns;
    }

    public void setReturns(BigDecimal returns) {
        this.returns = returns;
    }

    public BigDecimal getExpectedCash() {
        return expectedCash;
    }

    public void setExpectedCash(BigDecimal expectedCash) {
        this.expectedCash = expectedCash;
    }

    public BigDecimal getCountedCash() {
        return countedCash;
    }

    public void setCountedCash(BigDecimal countedCash) {
        this.countedCash = countedCash;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public void setDifference(BigDecimal difference) {
        this.difference = difference;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CashClosure{" +
                "id=" + id +
                ", cashSessionId=" + cashSessionId +
                ", initialFund=" + initialFund +
                ", cashSales=" + cashSales +
                ", cardSales=" + cardSales +
                ", transferSales=" + transferSales +
                ", otherSales=" + otherSales +
                ", totalSales=" + totalSales +
                ", totalTransactions=" + totalTransactions +
                ", cashIn=" + cashIn +
                ", cashOut=" + cashOut +
                ", returns=" + returns +
                ", expectedCash=" + expectedCash +
                ", countedCash=" + countedCash +
                ", difference=" + difference +
                ", observations='" + observations + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
