<%@ page import="java.util.List" %>
<%@ page import="com.entity.FoodItem" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    List<FoodItem> foodList =
            (List<FoodItem>) request.getAttribute("foodList");
%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Stock Transactions - FoodStock</title>

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

        /* ================= CARD ================= */

        .card {
            background: white;
            max-width: 750px;
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
        }

        .card-title {
            margin-bottom: 25px;
        }

        .card-title h2 {
            color: #1b5e20;
            font-size: 20px;
            margin-bottom: 6px;
        }

        .card-title p {
            color: #777;
            font-size: 13px;
        }

        /* ================= FORM ================= */

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-size: 13px;
            font-weight: bold;
            color: #555;
        }

        .form-group input,
        .form-group select {
            width: 100%;
            padding: 12px 14px;
            border: 1px solid #ddd;
            border-radius: 8px;
            outline: none;
            font-size: 14px;
            background: white;
            color: #333;
            transition: 0.2s;
        }

        .form-group input:focus,
        .form-group select:focus {
            border-color: #2e7d32;
            box-shadow: 0 0 0 2px rgba(46,125,50,0.08);
        }

        .form-group input:hover,
        .form-group select:hover {
            border-color: #b5b5b5;
        }

        /* ================= TRANSACTION TYPE ================= */

        .transaction-info {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
            margin-bottom: 20px;
        }

        .info-box {
            background: #f5faf7;
            border: 1px solid #e1eee4;
            padding: 14px;
            border-radius: 8px;
        }

        .info-box strong {
            display: block;
            color: #1b5e20;
            font-size: 13px;
            margin-bottom: 4px;
        }

        .info-box span {
            color: #777;
            font-size: 12px;
        }

        /* ================= BUTTON ================= */

        .btn {
            width: 100%;
            border: none;
            padding: 13px;
            border-radius: 8px;
            background: #2e7d32;
            color: white;
            font-size: 14px;
            font-weight: bold;
            cursor: pointer;
            transition: 0.2s;
            margin-top: 5px;
        }

        .btn:hover {
            background: #1b5e20;
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

            .transaction-info {
                grid-template-columns: 1fr;
            }

            .card {
                padding: 20px;
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
            <a href="${pageContext.request.contextPath}/stockTransaction"
               class="active">
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

</aside>


<!-- ================= MAIN CONTENT ================= -->

<main class="main">


    <!-- HEADER -->

    <div class="header">

        <h1>Stock Transactions</h1>

        <p>
            Add stock in or stock out transactions
        </p>

    </div>


    <!-- TRANSACTION CARD -->

    <div class="card">

        <div class="card-title">

            <h2>Record Stock Transaction</h2>

            <p>
                Update the quantity of an existing food item.
            </p>

        </div>


        <!-- INFORMATION BOXES -->

        <div class="transaction-info">

            <div class="info-box">

                <strong>STOCK IN</strong>

                <span>
                    Adds quantity to available stock.
                </span>

            </div>

            <div class="info-box">

                <strong>STOCK OUT</strong>

                <span>
                    Removes quantity from available stock.
                </span>

            </div>

        </div>


        <!-- FORM -->

        <form action="${pageContext.request.contextPath}/saveTransaction"
              method="post">


            <!-- FOOD ITEM -->

            <div class="form-group">

                <label for="foodId">
                    Food Item
                </label>

                <select
                    id="foodId"
                    name="foodId"
                    required>

                    <option value="">
                        Select Food Item
                    </option>

                    <%
                        if (foodList != null) {

                            for (FoodItem food : foodList) {
                    %>

                    <option value="<%= food.getId() %>">
                        <%= food.getFoodName() %>
                    </option>

                    <%
                            }
                        }
                    %>

                </select>

            </div>


            <!-- TRANSACTION TYPE -->

            <div class="form-group">

                <label for="type">
                    Transaction Type
                </label>

                <select
                    id="type"
                    name="type"
                    required>

                    <option value="">
                        Select Transaction Type
                    </option>

                    <option value="STOCK IN">
                        STOCK IN
                    </option>

                    <option value="STOCK OUT">
                        STOCK OUT
                    </option>

                </select>

            </div>


            <!-- QUANTITY -->

            <div class="form-group">

                <label for="quantity">
                    Quantity
                </label>

                <input
                    type="number"
                    id="quantity"
                    name="quantity"
                    min="1"
                    placeholder="Enter quantity"
                    required>

            </div>


            <!-- DATE -->

            <div class="form-group">

                <label for="transactionDate">
                    Transaction Date
                </label>

                <input
                    type="date"
                    id="transactionDate"
                    name="transactionDate"
                    required>

            </div>


            <!-- BUTTON -->

            <button
                type="submit"
                class="btn">

                Save Transaction

            </button>

        </form>

    </div>

</main>

</body>

</html>