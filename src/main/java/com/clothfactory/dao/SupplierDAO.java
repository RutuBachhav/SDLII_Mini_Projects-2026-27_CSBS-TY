package com.clothfactory.dao;

import com.clothfactory.model.Supplier;
import com.clothfactory.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {
    private static final String BASE_SELECT = "SELECT supplier_id, supplier_name, contact_number, email, address, active FROM suppliers";

    public List<Supplier> findAll(String search) throws SQLException {
        String sql = BASE_SELECT + " WHERE active = TRUE AND (supplier_name LIKE ? OR contact_number LIKE ? OR email LIKE ?) ORDER BY supplier_name";
        String pattern = "%" + (search == null ? "" : search.trim()) + "%";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Supplier> suppliers = new ArrayList<>();
                while (resultSet.next()) suppliers.add(map(resultSet));
                return suppliers;
            }
        }
    }

    public Supplier findById(long supplierId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(BASE_SELECT + " WHERE supplier_id = ?")) {
            statement.setLong(1, supplierId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public void create(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO suppliers (supplier_name, contact_number, email, address) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, supplier);
            statement.executeUpdate();
        }
    }

    public boolean update(Supplier supplier) throws SQLException {
        String sql = "UPDATE suppliers SET supplier_name = ?, contact_number = ?, email = ?, address = ? WHERE supplier_id = ? AND active = TRUE";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, supplier);
            statement.setLong(5, supplier.getSupplierId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteIfUnused(long supplierId) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ? AND NOT EXISTS (SELECT 1 FROM purchases WHERE supplier_id = ?)";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, supplierId);
            statement.setLong(2, supplierId);
            return statement.executeUpdate() == 1;
        }
    }

    private static void bind(PreparedStatement statement, Supplier supplier) throws SQLException {
        statement.setString(1, supplier.getSupplierName());
        statement.setString(2, supplier.getContactNumber());
        statement.setString(3, supplier.getEmail());
        statement.setString(4, supplier.getAddress());
    }

    private static Supplier map(ResultSet resultSet) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setSupplierId(resultSet.getLong("supplier_id"));
        supplier.setSupplierName(resultSet.getString("supplier_name"));
        supplier.setContactNumber(resultSet.getString("contact_number"));
        supplier.setEmail(resultSet.getString("email"));
        supplier.setAddress(resultSet.getString("address"));
        supplier.setActive(resultSet.getBoolean("active"));
        return supplier;
    }
}