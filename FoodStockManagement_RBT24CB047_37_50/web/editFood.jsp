<%@ page import="com.entity.FoodItem" %>

<%
    FoodItem food = (FoodItem) request.getAttribute("food");
%>

<!DOCTYPE html>
<html>
<head>

    <meta charset="UTF-8">

    <title>Edit Food Item</title>

    <style>

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
        }

        body {
            background: #f4f7f5;
            min-height: 100vh;
            display: flex;
        }

        .sidebar {
            width: 240px;
            background: #12372a;
            color: white;
            padding: 25px 15px;
            min-height: 100vh;
        }

        .logo {
            font-size: 22px;
            font-weight: bold;
            text-align: center;
            margin-bottom: 35px;
        }

        .logo span {
            color: #7ed957;
        }

        .menu a {
            display: block;
            color: white;
            text-decoration: none;
            padding: 14px 16px;
            margin: 8px 0;
            border-radius: 8px;
        }

        .menu a:hover,
        .menu .active {
            background: #1d6048;
        }

        .main {
            flex: 1;
            padding: 40px;
        }

        .header {
            margin-bottom: 25px;
        }

        .header h1 {
            color: #12372a;
            margin-bottom: 8px;
        }

        .card {
            max-width: 900px;
            background: white;
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
        }

        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }

        .form-group {
            display: flex;
            flex-direction: column;
        }

        .form-group.full {
            grid-column: span 2;
        }

        label {
            margin-bottom: 8px;
            font-weight: bold;
            color: #333;
        }

        input,
        select {
            padding: 12px;
            border: 1px solid #ccc;
            border-radius: 7px;
            font-size: 15px;
        }

        input:focus,
        select:focus {
            outline: none;
            border-color: #198754;
        }

        .buttons {
            margin-top: 25px;
            display: flex;
            gap: 12px;
        }

        .btn {
            border: none;
            padding: 13px 22px;
            border-radius: 8px;
            font-weight: bold;
            cursor: pointer;
            text-decoration: none;
        }

        .update {
            background: #198754;
            color: white;
        }

        .cancel {
            background: #ddd;
            color: #333;
        }

        .update:hover {
            background: #146c43;
        }

        .cancel:hover {
            background: #bbb;
        }

    </style>

</head>

<body>

<div class="sidebar">

    <div class="logo">
        Food<span>Stock</span>
    </div>

    <div class="menu">

        <a href="index.html">
            Dashboard
        </a>

        <a href="inventory" class="active">
            Food Inventory
        </a>

        <a href="index.html">
            Add Food
        </a>

        <a href="#">
            Stock Transactions
        </a>

        <a href="#">
            Reports
        </a>

    </div>

</div>


<div class="main">

    <div class="header">

        <h1>Edit Food Item</h1>

        <p>
            Update food stock information
        </p>

    </div>


    <div class="card">

        <form action="updateFood" method="post">

            <input type="hidden"
                   name="id"
                   value="<%= food.getId() %>">


            <div class="form-grid">


                <div class="form-group">

                    <label>Food Name</label>

                    <input type="text"
                           name="foodName"
                           value="<%= food.getFoodName() %>"
                           required>

                </div>


                <div class="form-group">

                    <label>Category</label>

                    <select name="category" required>

                        <option value="Grains"
                            <%= "Grains".equals(food.getCategory()) ? "selected" : "" %>>
                            Grains
                        </option>

                        <option value="Dairy"
                            <%= "Dairy".equals(food.getCategory()) ? "selected" : "" %>>
                            Dairy
                        </option>

                        <option value="Vegetables"
                            <%= "Vegetables".equals(food.getCategory()) ? "selected" : "" %>>
                            Vegetables
                        </option>

                        <option value="Fruits"
                            <%= "Fruits".equals(food.getCategory()) ? "selected" : "" %>>
                            Fruits
                        </option>

                        <option value="Beverages"
                            <%= "Beverages".equals(food.getCategory()) ? "selected" : "" %>>
                            Beverages
                        </option>

                        <option value="Frozen Food"
                            <%= "Frozen Food".equals(food.getCategory()) ? "selected" : "" %>>
                            Frozen Food
                        </option>

                        <option value="Bakery"
                            <%= "Bakery".equals(food.getCategory()) ? "selected" : "" %>>
                            Bakery
                        </option>

                        <option value="Other"
                            <%= "Other".equals(food.getCategory()) ? "selected" : "" %>>
                            Other
                        </option>

                    </select>

                </div>


                <div class="form-group">

                    <label>Quantity</label>

                    <input type="number"
                           name="quantity"
                           value="<%= food.getQuantity() %>"
                           min="0"
                           required>

                </div>


                <div class="form-group">

                    <label>Unit</label>

                    <select name="unit" required>

                        <option value="kg"
                            <%= "kg".equals(food.getUnit()) ? "selected" : "" %>>
                            Kilogram (kg)
                        </option>

                        <option value="litre"
                            <%= "litre".equals(food.getUnit()) ? "selected" : "" %>>
                            Litre
                        </option>

                        <option value="piece"
                            <%= "piece".equals(food.getUnit()) ? "selected" : "" %>>
                            Piece
                        </option>

                        <option value="packet"
                            <%= "packet".equals(food.getUnit()) ? "selected" : "" %>>
                            Packet
                        </option>

                    </select>

                </div>


                <div class="form-group">

                    <label>Price</label>

                    <input type="number"
                           name="price"
                           value="<%= food.getPrice() %>"
                           min="0"
                           step="0.01"
                           required>

                </div>


                <div class="form-group">

                    <label>Minimum Stock</label>

                    <input type="number"
                           name="minimumStock"
                           value="<%= food.getMinimumStock() %>"
                           min="0"
                           required>

                </div>


                <div class="form-group">

                    <label>Supplier</label>

                    <input type="text"
                           name="supplier"
                           value="<%= food.getSupplier() %>"
                           required>

                </div>


                <div class="form-group">

                    <label>Expiry Date</label>

                    <input type="date"
                           name="expiryDate"
                           value="<%= food.getExpiryDate() %>"
                           required>

                </div>

            </div>


            <div class="buttons">

                <button type="submit"
                        class="btn update">
                    Update Food Item
                </button>

                <a href="inventory"
                   class="btn cancel">
                    Cancel
                </a>

            </div>

        </form>

    </div>

</div>

</body>
</html>