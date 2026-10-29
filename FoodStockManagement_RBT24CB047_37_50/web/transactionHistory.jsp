
<%@ page import="java.util.List" %>
<%@ page import="com.entity.StockTransaction" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    List<StockTransaction> transactionList =
            (List<StockTransaction>) request.getAttribute("transactionList");
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Transaction History - FoodStock</title>

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

        /* TABLE */
        .table-card {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 700px;
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

        /* TRANSACTION BADGES */
        .stock-in {
            display: inline-block;
            background: #e7f6ec;
            color: #176b35;
            padding: 6px 11px;
            border-radius: 15px;
            font-weight: bold;
            font-size: 11px;
        }

        .stock-out {
            display: inline-block;
            background: #fde8e7;
            color: #b42318;
            padding: 6px 11px;
            border-radius: 15px;
            font-weight: bold;
            font-size: 11px;
        }

        /* EMPTY STATE */
        .empty {
            text-align: center;
            padding: 45px;
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

            .table-card {
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
            <a href="${pageContext.request.contextPath}/transactionHistory"
               class="active">
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

<!-- MAIN CONTENT -->
<main class="main">

    <div class="header">
        <h1>Transaction History</h1>
        <p>View all stock-in and stock-out transactions</p>
    </div>

    <div class="table-card">

        <table>
            <thead>
                <tr>
                    <th>Sr. No.</th>
                    <th>Food Item</th>
                    <th>Transaction Type</th>
                    <th>Quantity</th>
                    <th>Date</th>
                </tr>
            </thead>

            <tbody>
                <%
                    if (transactionList != null
                            && !transactionList.isEmpty()) {

                        int rowNumber = 1;

                        for (StockTransaction transaction
                                : transactionList) {
                %>

                <tr>
                    <td>
                        <%= rowNumber++ %>
                    </td>

                    <td>
                        <strong>
                            <%= transaction.getFoodItem().getFoodName() %>
                        </strong>
                    </td>

                    <td>
                        <%
                            if ("STOCK IN".equals(transaction.getType())
                                    || "IN".equalsIgnoreCase(transaction.getType())) {
                        %>
                            <span class="stock-in">STOCK IN</span>
                        <%
                            } else {
                        %>
                            <span class="stock-out">STOCK OUT</span>
                        <%
                            }
                        %>
                    </td>

                    <td>
                        <%= transaction.getQuantity() %>
                    </td>

                    <td>
                        <%= transaction.getTransactionDate() %>
                    </td>
                </tr>

                <%
                        }
                    } else {
                %>

                <tr>
                    <td colspan="5" class="empty">
                        No transactions found.
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
