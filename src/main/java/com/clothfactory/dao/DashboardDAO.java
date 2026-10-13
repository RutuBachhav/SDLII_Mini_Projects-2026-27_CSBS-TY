package com.clothfactory.dao;

import com.clothfactory.model.DashboardData;
import com.clothfactory.model.Product;
import com.clothfactory.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public DashboardData load() throws SQLException {
        DashboardData data = new DashboardData();
        String summarySql = "SELECT COUNT(*) total_products, SUM(category = 'RAW_MATERIAL') raw_materials, SUM(category = 'FABRIC') fabric_products, SUM(category = 'FINISHED_PRODUCT') finished_products, COALESCE(SUM(current_quantity * unit_price), 0) inventory_value FROM products WHERE active = TRUE";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(summarySql); ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                data.setTotalProducts(resultSet.getInt("total_products"));
                data.setRawMaterials(resultSet.getInt("raw_materials"));
                data.setFabricProducts(resultSet.getInt("fabric_products"));
                data.setFinishedProducts(resultSet.getInt("finished_products"));
                data.setInventoryValue(resultSet.getBigDecimal("inventory_value"));
            }
        }
        data.setLowStockProducts(findLowStock());
        data.setRecentTransactions(inventoryDAO.findRecent(8));
        return data;
    }

    public List<Product> findLowStock() throws SQLException {
        String sql = "SELECT product_id, product_code, product_name, category, unit, unit_price, current_quantity, minimum_stock, active FROM products WHERE active = TRUE AND current_quantity <= minimum_stock ORDER BY current_quantity / NULLIF(minimum_stock, 0), product_name";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            List<Product> products = new ArrayList<>();
            while (resultSet.next()) products.add(mapProduct(resultSet));
            return products;
        }
    }

    public List<Product> findInventory() throws SQLException {
        return productDAO.findAll(null, null);
    }

    private static Product mapProduct(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setProductId(resultSet.getLong("product_id"));
        product.setProductCode(resultSet.getString("product_code"));
        product.setProductName(resultSet.getString("product_name"));
        product.setCategory(Product.Category.valueOf(resultSet.getString("category")));
        product.setUnit(resultSet.getString("unit"));
        product.setUnitPrice(resultSet.getBigDecimal("unit_price"));
        product.setCurrentQuantity(resultSet.getBigDecimal("current_quantity"));
        product.setMinimumStock(resultSet.getBigDecimal("minimum_stock"));
        product.setActive(resultSet.getBoolean("active"));
        return product;
    }
}