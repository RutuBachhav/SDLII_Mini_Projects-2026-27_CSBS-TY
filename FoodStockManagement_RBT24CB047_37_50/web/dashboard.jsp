<%@ page contentType="text/html;charset=UTF-8" %>

<%
    Integer totalItems = (Integer) request.getAttribute("totalItems");
    Integer totalQuantity = (Integer) request.getAttribute("totalQuantity");
    Integer lowStockItems = (Integer) request.getAttribute("lowStockItems");
    Integer expiringSoon = (Integer) request.getAttribute("expiringSoon");

    if (totalItems == null) totalItems = 0;
    if (totalQuantity == null) totalQuantity = 0;
    if (lowStockItems == null) lowStockItems = 0;
    if (expiringSoon == null) expiringSoon = 0;
%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>FoodStock Dashboard</title>

    <style>

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
        }

        body {
            background: #f4f7f4;
            display: flex;
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
        }

        .logo {
            text-align: center;
            font-size: 23px;
            font-weight: bold;
            margin-bottom: 35px;
        }
        .logo span{
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
            margin-bottom: 25px;
        }

        .header h1 {
            color: #1b5e20;
            margin-bottom: 6px;
        }

        .header p {
            color: #777;
        }

        /* STAT CARDS */

        .stats {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-bottom: 25px;
        }

        .stat-card {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
        }

        .stat-title {
            color: #777;
            font-size: 14px;
            margin-bottom: 12px;
        }

        .stat-value {
            font-size: 32px;
            font-weight: bold;
            color: #1b5e20;
        }

        .stat-description {
            margin-top: 8px;
            color: #888;
            font-size: 13px;
        }

        /* QUICK ACTIONS */

        .card {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
        }

        .card h2 {
            color: #1b5e20;
            margin-bottom: 20px;
        }

        .actions {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 15px;
        }

        .action {
            padding: 20px;
            border: 1px solid #e0e0e0;
            border-radius: 10px;
            text-decoration: none;
            color: #333;
            transition: 0.2s;
        }

        .action:hover {
            border-color: #2e7d32;
            background: #f1f8f1;
        }

        .action h3 {
            color: #2e7d32;
            margin-bottom: 7px;
        }

        .action p {
            color: #777;
            font-size: 13px;
        }

        /* RESPONSIVE */

        @media (max-width: 1100px) {

            .stats {
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

            .actions {
                grid-template-columns: 1fr;
            }

        }

        @media (max-width: 650px) {

            .sidebar {
                position: relative;
                width: 100%;
                height: auto;
            }

            body {
                display: block;
            }

            .main {
                margin-left: 0;
                width: 100%;
            }

            .stats {
                grid-template-columns: 1fr;
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
                <a href="${pageContext.request.contextPath}/dashboard"
                   class="active">
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

            <h1>Dashboard</h1>

            <p>
                Food Stock Management System
            </p>

        </div>


        <!-- STATISTICS -->

        <div class="stats">

            <div class="stat-card">

                <div class="stat-title">
                    Total Food Items
                </div>

                <div class="stat-value">
                    <%= totalItems %>
                </div>

                <div class="stat-description">
                    Items currently registered
                </div>

            </div>


            <div class="stat-card">

                <div class="stat-title">
                    Total Stock Quantity
                </div>

                <div class="stat-value">
                    <%= totalQuantity %>
                </div>

                <div class="stat-description">
                    Combined available quantity
                </div>

            </div>


            <div class="stat-card">

                <div class="stat-title">
                    Low Stock Items
                </div>

                <div class="stat-value">
                    <%= lowStockItems %>
                </div>

                <div class="stat-description">
                    Items requiring attention
                </div>

            </div>


            <div class="stat-card">

                <div class="stat-title">
                    Expiring Soon
                </div>

                <div class="stat-value">
                    <%= expiringSoon %>
                </div>

                <div class="stat-description">
                    Items expiring within 7 days
                </div>

            </div>

        </div>


        <!-- QUICK ACTIONS -->

        <div class="card">

            <h2>Quick Actions</h2>

            <div class="actions">

                <a href="${pageContext.request.contextPath}/index.html"
                   class="action">

                    <h3>Add Food</h3>

                    <p>
                        Add a new food item to inventory.
                    </p>

                </a>


                <a href="${pageContext.request.contextPath}/stockTransaction"
                   class="action">

                    <h3>Stock Transaction</h3>

                    <p>
                        Add or remove stock quantity.
                    </p>

                </a>


                <a href="${pageContext.request.contextPath}/inventory"
                   class="action">

                    <h3>View Inventory</h3>

                    <p>
                        Search and manage food inventory.
                    </p>

                </a>

            </div>

        </div>

    </div>

</body>

</html>