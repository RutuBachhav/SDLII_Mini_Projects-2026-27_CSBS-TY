
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.entity.FoodItem" %>

<%
    Integer totalItems = (Integer) request.getAttribute("totalItems");
    Integer totalQuantity = (Integer) request.getAttribute("totalQuantity");
    Integer lowStockItems = (Integer) request.getAttribute("lowStockItems");
    Double stockValue = (Double) request.getAttribute("stockValue");

    List<FoodItem> foodList =
            (List<FoodItem>) request.getAttribute("foodList");

    if (totalItems == null) totalItems = 0;
    if (totalQuantity == null) totalQuantity = 0;
    if (lowStockItems == null) lowStockItems = 0;
    if (stockValue == null) stockValue = 0.0;
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reports - FoodStock</title>

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

        /* HEADER */
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

        /* SUMMARY */
        .summary {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-bottom: 25px;
        }

        .summary-card {
            background: white;
            padding: 22px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
            transition: 0.2s;
        }

        .summary-card:hover {
            transform: translateY(-2px);
        }

        .summary-title {
            color: #777;
            font-size: 13px;
            margin-bottom: 10px;
        }

        .summary-value {
            font-size: 27px;
            font-weight: bold;
            color: #1b5e20;
        }

        /* REPORT CARD */
        .card {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
            overflow-x: auto;
        }

        .card-header {
            margin-bottom: 20px;
        }

        .card-header h2 {
            color: #1b5e20;
            font-size: 20px;
            margin-bottom: 5px;
        }

        .card-header p {
            color: #777;
            font-size: 13px;
        }

        /* TABLE */
        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 950px;
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

        td strong {
            color: #263238;
        }

        /* STATUS */
        .status-badge {
            display: inline-block;
            padding: 6px 10px;
            border-radius: 15px;
            font-size: 11px;
            font-weight: bold;
        }

        .low {
            background: #fde8e7;
            color: #b42318;
        }

        .normal {
            background: #e7f6ec;
            color: #176b35;
        }

        /* EMPTY */
        .empty {
            text-align: center;
            padding: 40px !important;
            color: #777;
            font-size: 15px;
        }

        /* RESPONSIVE */
        @media (max-width: 1100px) {
            .summary {
                grid-template-columns: repeat(2, 1fr);
            }
        }

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

            .summary {
                grid-template-columns: 1fr;
            }

            .summary-card {
                width: 100%;
            }

            .card {
                padding: 15px;
            }
        }
    </style>
</head>

<body>

<!-- SIDEBAR -->
<aside class="sidebar">

    <div class="logo">
        Food<span>Stock</span>
    </div>

    <ul class="menu">
        <li><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
        <li><a href="${pageContext.request.contextPath}/inventory">Food Inventory</a></li>
        <li><a href="${pageContext.request.contextPath}/index.html">Add Food</a></li>
        <li><a href="${pageContext.request.contextPath}/stockTransaction">Stock Transactions</a></li>
        <li><a href="${pageContext.request.contextPath}/transactionHistory">Transaction History</a></li>
        <li><a href="${pageContext.request.contextPath}/lowStock">Low Stock Alerts</a></li>
        <li><a href="${pageContext.request.contextPath}/expiry">Expiry Tracking</a></li>
        <li><a href="${pageContext.request.contextPath}/reports" class="active">Reports</a></li>
    </ul>

</aside>

<!-- MAIN CONTENT -->
<main class="main">

    <div class="header">
        <h1>Inventory Reports</h1>
        <p>Overview of your food stock and inventory value.</p>
    </div>

    <!-- SUMMARY CARDS -->
    <div class="summary">

        <div class="summary-card">
            <div class="summary-title">Total Food Items</div>
            <div class="summary-value"><%= totalItems %></div>
        </div>

        <div class="summary-card">
            <div class="summary-title">Total Quantity</div>
            <div class="summary-value"><%= totalQuantity %></div>
        </div>

        <div class="summary-card">
            <div class="summary-title">Low Stock Items</div>
            <div class="summary-value"><%= lowStockItems %></div>
        </div>

        <div class="summary-card">
            <div class="summary-title">Stock Value</div>
            <div class="summary-value">
                ₹<%= String.format("%.2f", stockValue) %>
            </div>
        </div>

    </div>

    <!-- INVENTORY REPORT -->
    <div class="card">

        <div class="card-header">
            <h2>Food Inventory Report</h2>
            <p>Detailed overview of current food inventory and stock value.</p>
        </div>

        <table>

            <thead>
                <tr>
                    <th>Sr. No.</th>
                    <th>Food Name</th>
                    <th>Category</th>
                    <th>Quantity</th>
                    <th>Unit</th>
                    <th>Price</th>
                    <th>Stock Value</th>
                    <th>Minimum Stock</th>
                    <th>Status</th>
                </tr>
            </thead>

            <tbody>

            <%
                if (foodList != null && !foodList.isEmpty()) {

                    int rowNumber = 1;

                    for (FoodItem food : foodList) {

                        boolean lowStock =
                                food.getQuantity() <= food.getMinimumStock();

                        double itemValue =
                                food.getQuantity() * food.getPrice();
            %>

                <tr>

                    <td><%= rowNumber++ %></td>

                    <td>
                        <strong><%= food.getFoodName() %></strong>
                    </td>

                    <td><%= food.getCategory() %></td>
                    <td><%= food.getQuantity() %></td>
                    <td><%= food.getUnit() %></td>

                    <td>
                        ₹<%= String.format("%.2f", food.getPrice()) %>
                    </td>

                    <td>
                        ₹<%= String.format("%.2f", itemValue) %>
                    </td>

                    <td><%= food.getMinimumStock() %></td>

                    <td>
                        <% if (lowStock) { %>
                            <span class="status-badge low">LOW STOCK</span>
                        <% } else { %>
                            <span class="status-badge normal">IN STOCK</span>
                        <% } %>
                    </td>

                </tr>

            <%
                    }

                } else {
            %>

                <tr>
                    <td colspan="9" class="empty">
                        No food items available.
                    </td>
                </tr>

            <%
                }
            %>

            </tbody>

        </table>

    </div>

</main>

</body>
</html>
