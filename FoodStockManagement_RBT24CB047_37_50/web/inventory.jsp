
<%@ page import="java.util.List" %>
<%@ page import="com.entity.FoodItem" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    List<FoodItem> foodList =
            (List<FoodItem>) request.getAttribute("foodList");

    String search =
            request.getAttribute("search") != null
            ? (String) request.getAttribute("search")
            : "";

    String category =
            request.getAttribute("category") != null
            ? (String) request.getAttribute("category")
            : "ALL";

    String status =
            request.getAttribute("status") != null
            ? (String) request.getAttribute("status")
            : "ALL";
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FoodStock - Food Inventory</title>

    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
        }

        body {
            background: #f4f7f5;
            display: flex;
            min-height: 100vh;
        }

        /* SIDEBAR */
        .sidebar {
            width: 240px;
            height: 100vh;
            background: #1b5e20;
            color: white;
            position: fixed;
            left: 0;
            top: 0;
            padding: 25px 15px;
            overflow-y: auto;
        }

        .logo {
            text-align: center;
            font-size: 23px;
            font-weight: bold;
            margin-bottom: 35px;
        }

        .logo span {
            color: #7ed957;
        }

        .menu {
            list-style: none;
        }

        .menu li {
            margin-bottom: 8px;
        }

        .menu a {
            display: block;
            padding: 13px 15px;
            color: white;
            text-decoration: none;
            border-radius: 8px;
            font-size: 14px;
            transition: 0.2s;
        }

        .menu a:hover,
        .menu a.active {
            background: #2e7d32;
        }

        /* MAIN */
        .main {
            margin-left: 240px;
            width: calc(100% - 240px);
            padding: 30px;
        }

        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
        }

        .header h1 {
            color: #1b5e20;
            margin-bottom: 6px;
            font-size: 28px;
        }

        .header p {
            color: #777;
            font-size: 14px;
        }

        .add-btn {
            background: #2e7d32;
            color: white;
            text-decoration: none;
            padding: 12px 20px;
            border-radius: 8px;
            font-weight: bold;
            transition: 0.2s;
        }

        .add-btn:hover {
            background: #1b5e20;
        }

        /* FILTER */
        .filter-card {
            background: white;
            padding: 20px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
            margin-bottom: 20px;
        }

        .filter-form {
            display: flex;
            align-items: end;
            gap: 15px;
            flex-wrap: wrap;
        }

        .filter-group {
            display: flex;
            flex-direction: column;
            gap: 7px;
        }

        .filter-group label {
            font-size: 13px;
            font-weight: bold;
            color: #555;
        }

        .filter-group input,
        .filter-group select {
            padding: 11px 14px;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 14px;
            min-width: 180px;
            outline: none;
            background: white;
        }

        .filter-group input:focus,
        .filter-group select:focus {
            border-color: #2e7d32;
            box-shadow: 0 0 0 2px rgba(46,125,50,0.08);
        }

        .filter-buttons {
            display: flex;
            gap: 10px;
        }

        .search-btn {
            padding: 11px 20px;
            border: none;
            border-radius: 8px;
            background: #2e7d32;
            color: white;
            font-weight: bold;
            cursor: pointer;
        }

        .search-btn:hover {
            background: #1b5e20;
        }

        .clear-btn {
            padding: 11px 20px;
            border-radius: 8px;
            background: #eeeeee;
            color: #333;
            text-decoration: none;
            font-weight: bold;
        }

        .clear-btn:hover {
            background: #ddd;
        }

        /* TABLE */
        .card {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 1100px;
        }

        th {
            background: #1b5e20;
            color: white;
            padding: 14px 10px;
            text-align: left;
            font-size: 13px;
        }

        td {
            padding: 13px 10px;
            border-bottom: 1px solid #e5e5e5;
            font-size: 13px;
            color: #444;
        }

        tbody tr:hover {
            background: #f5faf7;
        }

        /* STATUS */
        .low-stock {
            display: inline-block;
            color: #b42318;
            background: #fde8e7;
            padding: 5px 9px;
            border-radius: 6px;
            font-size: 11px;
            font-weight: bold;
        }

        .normal-stock {
            display: inline-block;
            color: #176b35;
            background: #e7f6ec;
            padding: 5px 9px;
            border-radius: 6px;
            font-size: 11px;
            font-weight: bold;
        }

        /* ACTION BUTTONS */
        .edit-btn {
            display: inline-block;
            background: #2e7d32;
            color: white;
            text-decoration: none;
            padding: 7px 12px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: bold;
        }

        .edit-btn:hover {
            background: #1b5e20;
        }

        .delete-btn {
            display: inline-block;
            background: #dc3545;
            color: white;
            text-decoration: none;
            padding: 7px 12px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: bold;
            margin-left: 5px;
        }

        .delete-btn:hover {
            background: #b02a37;
        }

        .empty {
            text-align: center;
            padding: 40px;
            color: #777;
        }

        /* RESPONSIVE */
        @media (max-width: 900px) {
            .sidebar {
                width: 200px;
            }

            .main {
                margin-left: 200px;
                width: calc(100% - 200px);
            }

            .header {
                flex-direction: column;
                align-items: flex-start;
                gap: 15px;
            }
        }

        @media (max-width: 650px) {
            body {
                display: block;
            }

            .sidebar {
                position: relative;
                width: 100%;
                height: auto;
            }

            .main {
                margin-left: 0;
                width: 100%;
                padding: 20px;
            }
        }
    </style>
</head>

<body>

<!-- SIDEBAR -->
<div class="sidebar">

    <div class="logo">
        Food<span>Stock</span>
    </div>

    <ul class="menu">
        <li>
            <a href="${pageContext.request.contextPath}/dashboard">
                Dashboard
            </a>
        </li>

        <li>
            <a href="${pageContext.request.contextPath}/inventory" class="active">
                Food Inventory
            </a>
        </li>

        <li>
            <a href="${pageContext.request.contextPath}/index.html">
                Add Food
            </a>
        </li>

        <li>
            <a href="${pageContext.request.contextPath}/stockTransaction">
                Stock Transactions
            </a>
        </li>

        <li>
            <a href="${pageContext.request.contextPath}/transactionHistory">
                Transaction History
            </a>
        </li>

        <li>
            <a href="${pageContext.request.contextPath}/lowStock">
                Low Stock Alerts
            </a>
        </li>

        <li>
            <a href="${pageContext.request.contextPath}/expiry">
                Expiry Tracking
            </a>
        </li>

        <li>
            <a href="${pageContext.request.contextPath}/reports">
                Reports
            </a>
        </li>
    </ul>
</div>

<!-- MAIN CONTENT -->
<div class="main">

    <div class="header">
        <div>
            <h1>Food Inventory</h1>
            <p>Manage and monitor your food stock</p>
        </div>

        <a href="${pageContext.request.contextPath}/index.html" class="add-btn">
            + Add Food
        </a>
    </div>

    <!-- FILTER -->
    <div class="filter-card">

        <form action="${pageContext.request.contextPath}/inventory"
              method="get"
              class="filter-form">

            <div class="filter-group">
                <label>Search Food</label>
                <input type="text"
                       name="search"
                       placeholder="Enter food name..."
                       value="<%= search %>">
            </div>

            <div class="filter-group">
                <label>Category</label>

                <select name="category">
                    <option value="ALL"
                        <%= "ALL".equals(category) ? "selected" : "" %>>
                        All Categories
                    </option>

                    <option value="Grains"
                        <%= "Grains".equals(category) ? "selected" : "" %>>
                        Grains
                    </option>

                    <option value="Fast Food"
                        <%= "Fast Food".equals(category) ? "selected" : "" %>>
                        Fast Food
                    </option>

                    <option value="Beverages"
                        <%= "Beverages".equals(category) ? "selected" : "" %>>
                        Beverages
                    </option>

                    <option value="Dairy"
                        <%= "Dairy".equals(category) ? "selected" : "" %>>
                        Dairy
                    </option>

                    <option value="Bakery"
                        <%= "Bakery".equals(category) ? "selected" : "" %>>
                        Bakery
                    </option>

                    <option value="Vegetables"
                        <%= "Vegetables".equals(category) ? "selected" : "" %>>
                        Vegetables
                    </option>

                    <option value="Fruits"
                        <%= "Fruits".equals(category) ? "selected" : "" %>>
                        Fruits
                    </option>

                    <option value="Meat"
                        <%= "Meat".equals(category) ? "selected" : "" %>>
                        Meat
                    </option>
                </select>
            </div>

            <div class="filter-group">
                <label>Stock Status</label>

                <select name="status">
                    <option value="ALL"
                        <%= "ALL".equals(status) ? "selected" : "" %>>
                        All Stock
                    </option>

                    <option value="LOW"
                        <%= "LOW".equals(status) ? "selected" : "" %>>
                        Low Stock
                    </option>

                    <option value="AVAILABLE"
                        <%= "AVAILABLE".equals(status) ? "selected" : "" %>>
                        Available Stock
                    </option>
                </select>
            </div>

            <div class="filter-buttons">
                <button type="submit" class="search-btn">
                    Search
                </button>

                <a href="${pageContext.request.contextPath}/inventory"
                   class="clear-btn">
                    Clear
                </a>
            </div>
        </form>
    </div>

    <!-- INVENTORY TABLE -->
    <div class="card">
        <table>

            <thead>
                <tr>
                    <th>ID</th>
                    <th>Food Name</th>
                    <th>Category</th>
                    <th>Quantity</th>
                    <th>Unit</th>
                    <th>Price</th>
                    <th>Minimum Stock</th>
                    <th>Supplier</th>
                    <th>Expiry Date</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>

            <tbody>
            <%
                if (foodList != null && !foodList.isEmpty()) {

                    int rowNumber = 1;

                    for (FoodItem food : foodList) {

                        boolean lowStock =
                                food.getQuantity() <= food.getMinimumStock();
            %>

                <tr>
                    <!-- Sequential display number; database ID stays unchanged -->
                    <td><%= rowNumber++ %></td>

                    <td>
                        <strong><%= food.getFoodName() %></strong>
                    </td>

                    <td><%= food.getCategory() %></td>
                    <td><%= food.getQuantity() %></td>
                    <td><%= food.getUnit() %></td>

                    <td>
                        ₹ <%= String.format("%.2f", food.getPrice()) %>
                    </td>

                    <td><%= food.getMinimumStock() %></td>
                    <td><%= food.getSupplier() %></td>
                    <td><%= food.getExpiryDate() %></td>

                    <td>
                        <% if (lowStock) { %>
                            <span class="low-stock">LOW STOCK</span>
                        <% } else { %>
                            <span class="normal-stock">IN STOCK</span>
                        <% } %>
                    </td>

                    <td>
                        <a class="edit-btn"
                           href="${pageContext.request.contextPath}/editFood?id=<%= food.getId() %>">
                            Edit
                        </a>

                        <a class="delete-btn"
                           href="${pageContext.request.contextPath}/deleteFood?id=<%= food.getId() %>"
                           onclick="return confirm('Are you sure you want to delete this food item?');">
                            Delete
                        </a>
                    </td>
                </tr>

            <%
                    }
                } else {
            %>

                <tr>
                    <td colspan="11" class="empty">
                        No food items available.
                    </td>
                </tr>

            <%
                }
            %>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>
