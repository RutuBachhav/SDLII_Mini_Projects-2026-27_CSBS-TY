package com.clothfactory.dao;

import com.clothfactory.model.StockTransaction;
import com.clothfactory.service.InsufficientStockException;
import com.clothfactory.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {
    public void recordMovement(long productId, StockTransaction.Type type, BigDecimal quantity, String reason, Long createdBy)
            throws SQLException, InsufficientStockException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal currentQuantity = lockProductAndReadQuantity(connection, productId);
                BigDecimal updatedQuantity;
                if (type == StockTransaction.Type.STOCK_OUT) {
                    if (currentQuantity.compareTo(quantity) < 0) throw new InsufficientStockException();
                    updatedQuantity = currentQuantity.subtract(quantity);
                } else {
                    updatedQuantity = currentQuantity.add(quantity);
                }
                try (PreparedStatement update = connection.prepareStatement("UPDATE products SET current_quantity = ? WHERE product_id = ?")) {
                    update.setBigDecimal(1, updatedQuantity);
                    update.setLong(2, productId);
                    update.executeUpdate();
                }
                String insertSql = "INSERT INTO stock_transactions (product_id, transaction_type, quantity, reason, created_by) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                    insert.setLong(1, productId);
                    insert.setString(2, type.name());
                    insert.setBigDecimal(3, quantity);
                    insert.setString(4, reason);
                    if (createdBy == null) insert.setNull(5, java.sql.Types.BIGINT);
                    else insert.setLong(5, createdBy);
                    insert.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | InsufficientStockException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<StockTransaction> findRecent(int limit) throws SQLException {
        String sql = "SELECT st.transaction_id, st.product_id, p.product_name, p.unit, st.transaction_type, st.quantity, st.reason, st.transaction_date FROM stock_transactions st JOIN products p ON p.product_id = st.product_id ORDER BY st.transaction_date DESC, st.transaction_id DESC LIMIT ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<StockTransaction> transactions = new ArrayList<>();
                while (resultSet.next()) transactions.add(map(resultSet));
                return transactions;
            }
        }
    }

    private static BigDecimal lockProductAndReadQuantity(Connection connection, long productId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT current_quantity FROM products WHERE product_id = ? AND active = TRUE FOR UPDATE")) {
            statement.setLong(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) throw new SQLException("Product not found or inactive.");
                return resultSet.getBigDecimal("current_quantity");
            }
        }
    }

    private static StockTransaction map(ResultSet resultSet) throws SQLException {
        StockTransaction transaction = new StockTransaction();
        transaction.setTransactionId(resultSet.getLong("transaction_id"));
        transaction.setProductId(resultSet.getLong("product_id"));
        transaction.setProductName(resultSet.getString("product_name"));
        transaction.setUnit(resultSet.getString("unit"));
        transaction.setType(StockTransaction.Type.valueOf(resultSet.getString("transaction_type")));
        transaction.setQuantity(resultSet.getBigDecimal("quantity"));
        transaction.setReason(resultSet.getString("reason"));
        Timestamp timestamp = resultSet.getTimestamp("transaction_date");
        transaction.setTransactionDate(timestamp == null ? null : timestamp.toLocalDateTime());
        return transaction;
    }
}