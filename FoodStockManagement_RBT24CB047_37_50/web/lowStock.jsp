<%@ page import="java.util.List" %>
<%@ page import="com.entity.FoodItem" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    List<FoodItem> lowStockList =
            (List<FoodItem>) request.getAttribute("lowStockList");
%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Low Stock Alerts - FoodStock</title>

    <style>

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
        }

        body {
            background: #f4f7f5;
            color: #263238;
            min-height: 100vh;
        }

        /* ================= SIDEBAR ================= */

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

        /* ================= MAIN ================= */

        .main {
            margin-left: 240px;
            width: calc(100% - 240px);
            padding: 30px;
        }

        /* ================= HEADER ================= */

        .header {
            margin-bottom: 25px;
        }

        .header h1 {
            color: #1b5e20;
            font-size: 28px;
            margin-bottom: 6px;
        }

        .header p {
            color: #777;
            font-size: 14px;
        }

        /* ================= ALERT BOX ================= */

        .alert-box {
            background: #fff8e1;
            border-left: 5px solid #f59e0b;
            padding: 18px 20px;
            border-radius: 10px;
            margin-bottom: 20px;
            box-shadow: 0 3px 12px rgba(0,0,0,0.05);
        }

        .alert-box h3 {
            color: #8a5a00;
            font-size: 16px;
            margin-bottom: 5px;
        }

        .alert-box p {
            color: #795548;
            font-size: 13px;
        }

        /* ================= TABLE ================= */

        .table-container {
            background: white;
            border-radius: 15px;
            padding: 25px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 900px;
        }

        th {
            background: #1b5e20;
            color: white;
            padding: 14px 12px;
            text-align: left;
            font-size: 13px;
        }

        td {
            padding: 14px 12px;
            border-bottom: 1px solid #eeeeee;
            color: #444;
            font-size: 13px;
        }

        tbody tr:hover {
            background: #f5faf7;
        }

        /* ================= STATUS ================= */

        .low {
            display: inline-block;
            color: #b42318;
            background: #fde8e7;
            padding: 6px 10px;
            border-radius: 15px;
            font-size: 11px;
            font-weight: bold;
        }

        .quantity {
            color: #b42318;
            font-weight: bold;
        }

        .minimum {
            color: #555;
            font-weight: bold;
        }

        /* ================= NO STOCK ================= */

        .no-stock {
            text-align: center;
            padding: 45px 20px;
            color: #176b35;
            font-size: 16px;
            font-weight: bold;
        }

        /* ================= RESPONSIVE ================= */

        @media (max-width: 900px) {

            .sidebar {
                width: 200px;
            }

            .main {
                margin-left: 200px;
                width: calc(100% - 200px);
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

            .table-container {
                padding: 15px;
            }

        }

    </style>

</head>

<body>


<!-- ================= SIDEBAR ================= -->

<aside class="sidebar">

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
            <a href="${pageContext.request.contextPath}/inventory">
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
            <a href="${pageContext.request.contextPath}/lowStock"
               class="active">
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

</aside>


<!-- ================= MAIN CONTENT ================= -->

<main class="main">

    <div class="header">

        <h1>Low Stock Alerts</h1>

        <p>
            Food items that have reached or fallen below their minimum stock level.
        </p>

    </div>


    <% if (lowStockList != null && !lowStockList.isEmpty()) { %>

        <!-- ALERT -->

        <div class="alert-box">

            <h3>Stock Alert</h3>

            <p>
                <%= lowStockList.size() %>
                food item(s) require attention.
            </p>

        </div>


        <!-- TABLE -->

        <div class="table-container">

            <table>

                <thead>

                    <tr>

                        <th>ID</th>
                        <th>Food Name</th>
                        <th>Category</th>
                        <th>Current Quantity</th>
                        <th>Minimum Stock</th>
                        <th>Unit</th>
                        <th>Supplier</th>
                        <th>Status</th>

                    </tr>

                </thead>

                <tbody>

                    <% for (FoodItem food : lowStockList) { %>

                        <tr>

                            <td>
                                <%= food.getId() %>
                            </td>

                            <td>
                                <strong>
                                    <%= food.getFoodName() %>
                                </strong>
                            </td>

                            <td>
                                <%= food.getCategory() %>
                            </td>

                            <td class="quantity">
                                <%= food.getQuantity() %>
                            </td>

                            <td class="minimum">
                                <%= food.getMinimumStock() %>
                            </td>

                            <td>
                                <%= food.getUnit() %>
                            </td>

                            <td>
                                <%= food.getSupplier() %>
                            </td>

                            <td>

                                <span class="low">
                                    LOW STOCK
                                </span>

                            </td>

                        </tr>

                    <% } %>

                </tbody>

            </table>

        </div>


    <% } else { %>

        <!-- NO LOW STOCK -->

        <div class="table-container">

            <div class="no-stock">

                All food items have sufficient stock.

            </div>

        </div>

    <% } %>

</main>

</body>

</html>