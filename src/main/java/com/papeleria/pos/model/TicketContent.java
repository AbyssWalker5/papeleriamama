package com.papeleria.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TicketContent {
    private String storeName;
    private String storeAddress;
    private String storePhone;
    private String saleNumber;
    private LocalDateTime dateTime;
    private List<TicketItem> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;
    private String paymentMethod;
    private BigDecimal amountPaid;
    private BigDecimal change;
    private String thankYouMessage;
    private int paperWidthMm;

    public TicketContent() {
    }

    public TicketContent(String storeName, String storeAddress, String storePhone, String saleNumber, LocalDateTime dateTime, List<TicketItem> items, BigDecimal subtotal, BigDecimal discount, BigDecimal total, String paymentMethod, BigDecimal amountPaid, BigDecimal change, String thankYouMessage, int paperWidthMm) {
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.storePhone = storePhone;
        this.saleNumber = saleNumber;
        this.dateTime = dateTime;
        this.items = items;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.amountPaid = amountPaid;
        this.change = change;
        this.thankYouMessage = thankYouMessage;
        this.paperWidthMm = paperWidthMm;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public String getStorePhone() {
        return storePhone;
    }

    public void setStorePhone(String storePhone) {
        this.storePhone = storePhone;
    }

    public String getSaleNumber() {
        return saleNumber;
    }

    public void setSaleNumber(String saleNumber) {
        this.saleNumber = saleNumber;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public List<TicketItem> getItems() {
        return items;
    }

    public void setItems(List<TicketItem> items) {
        this.items = items;
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

    public String getThankYouMessage() {
        return thankYouMessage;
    }

    public void setThankYouMessage(String thankYouMessage) {
        this.thankYouMessage = thankYouMessage;
    }

    public int getPaperWidthMm() {
        return paperWidthMm;
    }

    public void setPaperWidthMm(int paperWidthMm) {
        this.paperWidthMm = paperWidthMm;
    }

    @Override
    public String toString() {
        return "TicketContent{" +
                "storeName='" + storeName + '\'' +
                ", storeAddress='" + storeAddress + '\'' +
                ", storePhone='" + storePhone + '\'' +
                ", saleNumber='" + saleNumber + '\'' +
                ", dateTime=" + dateTime +
                ", items=" + items +
                ", subtotal=" + subtotal +
                ", discount=" + discount +
                ", total=" + total +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", amountPaid=" + amountPaid +
                ", change=" + change +
                ", thankYouMessage='" + thankYouMessage + '\'' +
                ", paperWidthMm=" + paperWidthMm +
                '}';
    }

    public static class TicketItem {
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal discount;
        private BigDecimal subtotal;

        public TicketItem() {
        }

        public TicketItem(String productName, int quantity, BigDecimal unitPrice, BigDecimal discount, BigDecimal subtotal) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.discount = discount;
            this.subtotal = subtotal;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
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
            return "TicketItem{" +
                    "productName='" + productName + '\'' +
                    ", quantity=" + quantity +
                    ", unitPrice=" + unitPrice +
                    ", discount=" + discount +
                    ", subtotal=" + subtotal +
                    '}';
        }
    }
}
