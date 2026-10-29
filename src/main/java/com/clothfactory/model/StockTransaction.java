package com.clothfactory.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockTransaction {
    public enum Type { STOCK_IN, STOCK_OUT }

    private long transactionId;
    private long productId;
    private String productName;
    private String unit;
    private Type type;
    private BigDecimal quantity;
    private String reason;
    private LocalDateTime transactionDate;

    public long getTransactionId() { return transactionId; }
    public void setTransactionId(long transactionId) { this.transactionId = transactionId; }
    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
}