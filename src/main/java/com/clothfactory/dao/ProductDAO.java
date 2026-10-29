package com.clothfactory.dao;

import com.clothfactory.model.Product;
import com.clothfactory.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private static final String BASE_SELECT = "SELECT product_id, product_code, product_name, category, unit, unit_price, current_quantity, minimum_stock, active FROM products";

    public List<Product> findAll(String search, Product.Category category) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE active = TRUE");
        List<Object> parameters = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (product_name LIKE ? OR product_code LIKE ?)");
            String pattern = "%" + search.trim() + "%";
            parameters.add(pattern);
            parameters.add(pattern);
        }
        if (category != null) {
            sql.append(" AND category = ?");
            parameters.add(category.name());
        }
        sql.append(" ORDER BY product_name");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int index = 0; index < parameters.size(); index++) {
                statement.setObject(index + 1, parameters.get(index));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (resultSet.next()) {
                    products.add(map(resultSet));
                }
                return products;
            }
        }
    }

    public Product findById(long productId) throws SQLException {
        String sql = BASE_SELECT + " WHERE product_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public void create(Product product) throws SQLException {
        String sql = "INSERT INTO products (product_code, product_name, category, unit, unit_price, current_quantity, minimum_stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindProduct(statement, product);
            statement.executeUpdate();
        }
    }

    public boolean update(Product product) throws SQLException {
        String sql = "UPDATE products SET product_code = ?, product_name = ?, category = ?, unit = ?, unit_price = ?, minimum_stock = ? WHERE product_id = ? AND active = TRUE";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindProduct(statement, product);
            statement.setLong(7, product.getProductId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteIfUnused(long productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ? AND NOT EXISTS (SELECT 1 FROM purchase_items WHERE product_id = ?) AND NOT EXISTS (SELECT 1 FROM stock_transactions WHERE product_id = ?) AND NOT EXISTS (SELECT 1 FROM production_batches WHERE product_id = ?) AND NOT EXISTS (SELECT 1 FROM production_materials WHERE product_id = ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 5; index++) {
                statement.setLong(index, productId);
            }
            return statement.executeUpdate() == 1;
        }
    }

    private static void bindProduct(PreparedStatement statement, Product product) throws SQLException {
        statement.setString(1, product.getProductCode());
        statement.setString(2, product.getProductName());
        statement.setString(3, product.getCategory().name());
        statement.setString(4, product.getUnit());
        statement.setBigDecimal(5, product.getUnitPrice());
        statement.setBigDecimal(6, product.getCurrentQuantity());
        statement.setBigDecimal(7, product.getMinimumStock());
    }

    private static Product map(ResultSet resultSet) throws SQLException {
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