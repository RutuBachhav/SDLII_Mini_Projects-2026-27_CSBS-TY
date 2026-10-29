package com.clothfactory.service;

import com.clothfactory.dao.InventoryDAO;
import com.clothfactory.model.StockTransaction;

import java.math.BigDecimal;
import java.sql.SQLException;

public class InventoryService {
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public void recordMovement(long productId, StockTransaction.Type type, BigDecimal quantity, String reason)
            throws SQLException, InsufficientStockException {
        if (quantity == null || quantity.signum() <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
        if (reason == null || reason.isBlank() || reason.length() > 255) throw new IllegalArgumentException("A reason of up to 255 characters is required.");
        inventoryDAO.recordMovement(productId, type, quantity, reason.trim(), null);
    }
}