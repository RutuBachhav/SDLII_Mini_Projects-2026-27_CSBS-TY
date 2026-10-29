
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.temporal.ChronoUnit" %>
<%@ page import="com.entity.FoodItem" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Expiry Tracking - FoodStock</title>

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

        .main {
            margin-left: 240px;
            width: calc(100% - 240px);
            padding: 30px;
        }

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

        .cards {
            display: flex;
            gap: 20px;
            margin-bottom: 25px;
        }

        .card {
            background: white;
            padding: 20px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
            width: 230px;
        }

        .card h3 {
            font-size: 13px;
            color: #666;
            margin-bottom: 10px;
        }

        .card p {
            font-size: 28px;
            font-weight: bold;
        }

        .expired-count {
            color: #b42318;
        }

        .warning-count {
            color: #b7791f;
        }

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

        .expired {
            color: #b42318;
            font-weight: bold;
        }

        .near-expiry {
            color: #b7791f;
            font-weight: bold;
        }

        .safe {
            color: #176b35;
            font-weight: bold;
        }

        .badge {
            display: inline-block;
            padding: 6px 10px;
            border-radius: 15px;
            font-size: 11px;
            font-weight: bold;
        }

        .badge-expired {
            background: #fde8e7;
            color: #b42318;
        }

        .badge-warning {
            background: #fff4d6;
            color: #9a6700;
        }

        .badge-safe {
            background: #e7f6ec;
            color: #176b35;
        }

        .no-items {
            text-align: center;
            padding: 45px 20px;
            color: #176b35;
            font-size: 16px;
            font-weight: bold;
        }

        @media (max-width: 900px) {
            .sidebar {
                width: 200px;
            }

            .main {
                margin-left: 200px;
                width: calc(100% - 200px);
            }

            .cards {
                flex-wrap: wrap;
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

            .cards {
                flex-direction: column;
            }

            .card {
                width: 100%;
            }

            .table-container {
                padding: 15px;
            }
        }
    </style>
</head>

<body>

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
        <li><a href="${pageContext.request.contextPath}/expiry" class="active">Expiry Tracking</a></li>
        <li><a href="${pageContext.request.contextPath}/reports">Reports</a></li>
    </ul>

</aside>

<main class="main">

    <div class="header">
        <h1>Expiry Tracking</h1>
        <p>Monitor expired and upcoming food expiry dates.</p>
    </div>

    <%
        List<FoodItem> expiryList =
                (List<FoodItem>) request.getAttribute("expiryList");

        LocalDate today =
                (LocalDate) request.getAttribute("today");

        int expiredCount = 0;
        int nearExpiryCount = 0;

        if (expiryList != null && today != null) {
            for (FoodItem food : expiryList) {
                if (food.getExpiryDate() != null) {
                    long days = ChronoUnit.DAYS.between(
                            today, food.getExpiryDate());

                    if (days < 0) {
                        expiredCount++;
                    } else if (days <= 7) {
                        nearExpiryCount++;
                    }
                }
            }
        }
    %>

    <div class="cards">

        <div class="card">
            <h3>Expired Items</h3>
            <p class="expired-count"><%= expiredCount %></p>
        </div>

        <div class="card">
            <h3>Expiring Within 7 Days</h3>
            <p class="warning-count"><%= nearExpiryCount %></p>
        </div>

    </div>

    <div class="table-container">

        <% if (expiryList != null && !expiryList.isEmpty()) { %>

            <table>
                <thead>
                    <tr>
                        <th>Sr. No.</th>
                        <th>Food Name</th>
                        <th>Category</th>
                        <th>Quantity</th>
                        <th>Unit</th>
                        <th>Expiry Date</th>
                        <th>Days Remaining</th>
                        <th>Status</th>
                    </tr>
                </thead>

                <tbody>

                    <%
                        int rowNumber = 1;

                        for (FoodItem food : expiryList) {

                            if (food.getExpiryDate() == null || today == null) {
                                continue;
                            }

                            long days = ChronoUnit.DAYS.between(
                                    today, food.getExpiryDate());
                    %>

                    <tr>
                        <td><%= rowNumber++ %></td>

                        <td>
                            <strong><%= food.getFoodName() %></strong>
                        </td>

                        <td><%= food.getCategory() %></td>
                        <td><%= food.getQuantity() %></td>
                        <td><%= food.getUnit() %></td>
                        <td><%= food.getExpiryDate() %></td>

                        <td>
                            <% if (days < 0) { %>
                                <span class="expired">
                                    Expired <%= Math.abs(days) %> day(s) ago
                                </span>
                            <% } else { %>
                                <span class="<%= days <= 7 ? "near-expiry" : "safe" %>">
                                    <%= days %> day(s)
                                </span>
                            <% } %>
                        </td>

                        <td>
                            <% if (days < 0) { %>
                                <span class="badge badge-expired">EXPIRED</span>
                            <% } else if (days <= 7) { %>
                                <span class="badge badge-warning">EXPIRING SOON</span>
                            <% } else { %>
                                <span class="badge badge-safe">SAFE</span>
                            <% } %>
                        </td>
                    </tr>

                    <% } %>

                </tbody>
            </table>

        <% } else { %>

            <div class="no-items">
                No expiry information available.
            </div>

        <% } %>

    </div>

</main>

</body>
</html>
