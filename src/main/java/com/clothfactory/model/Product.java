package com.clothfactory.model;

import java.math.BigDecimal;

public class Product {
    public enum Category {
        RAW_MATERIAL, FABRIC, FINISHED_PRODUCT, CHEMICAL, OTHER
    }

    private long productId;
    private String productCode;
    private String productName;
    private Category category;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal currentQuantity;
    private BigDecimal minimumStock;
    private boolean active = true;

    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(BigDecimal currentQuantity) { this.currentQuantity = currentQuantity; }
    public BigDecimal getMinimumStock() { return minimumStock; }
    public void setMinimumStock(BigDecimal minimumStock) { this.minimumStock = minimumStock; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}