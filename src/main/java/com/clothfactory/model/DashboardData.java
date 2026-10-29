package com.clothfactory.model;

import java.math.BigDecimal;
import java.util.List;

public class DashboardData {
    private int totalProducts;
    private int rawMaterials;
    private int fabricProducts;
    private int finishedProducts;
    private BigDecimal inventoryValue = BigDecimal.ZERO;
    private List<Product> lowStockProducts = List.of();
    private List<StockTransaction> recentTransactions = List.of();

    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
    public int getRawMaterials() { return rawMaterials; }
    public void setRawMaterials(int rawMaterials) { this.rawMaterials = rawMaterials; }
    public int getFabricProducts() { return fabricProducts; }
    public void setFabricProducts(int fabricProducts) { this.fabricProducts = fabricProducts; }
    public int getFinishedProducts() { return finishedProducts; }
    public void setFinishedProducts(int finishedProducts) { this.finishedProducts = finishedProducts; }
    public BigDecimal getInventoryValue() { return inventoryValue; }
    public void setInventoryValue(BigDecimal inventoryValue) { this.inventoryValue = inventoryValue; }
    public List<Product> getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(List<Product> lowStockProducts) { this.lowStockProducts = lowStockProducts; }
    public List<StockTransaction> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<StockTransaction> recentTransactions) { this.recentTransactions = recentTransactions; }
}