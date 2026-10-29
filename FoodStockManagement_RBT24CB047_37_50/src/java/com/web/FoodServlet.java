package com.web;

import com.entity.FoodItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/save")
public class FoodServlet extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("FoodStockPU");
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        EntityManager em = emf.createEntityManager();

        try {

            String foodName = request.getParameter("foodName");
            String category = request.getParameter("category");
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String unit = request.getParameter("unit");
            double price = Double.parseDouble(request.getParameter("price"));
            int minimumStock =
                    Integer.parseInt(request.getParameter("minimumStock"));
            String supplier = request.getParameter("supplier");

            LocalDate expiryDate =
                    LocalDate.parse(request.getParameter("expiryDate"));

            FoodItem food = new FoodItem();

            food.setFoodName(foodName);
            food.setCategory(category);
            food.setQuantity(quantity);
            food.setUnit(unit);
            food.setPrice(price);
            food.setMinimumStock(minimumStock);
            food.setSupplier(supplier);
            food.setExpiryDate(expiryDate);

            em.getTransaction().begin();

            em.persist(food);

            em.getTransaction().commit();

            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().println("""
                <!DOCTYPE html>
                <html lang="en">

                <head>

                    <meta charset="UTF-8">

                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">

                    <title>Food Added - FoodStock</title>

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
                            color: #263238;
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
                        }

                        .menu a:hover,
                        .menu a.active {
                            background: #2e7d32;
                        }

                        /* MAIN */

                        .main {
                            margin-left: 240px;
                            width: calc(100% - 240px);
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 30px;
                        }

                        /* SUCCESS CARD */

                        .success-card {
                            background: white;
                            width: 100%;
                            max-width: 600px;
                            padding: 40px;
                            border-radius: 18px;
                            text-align: center;
                            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
                        }

                        .success-icon {
                            width: 75px;
                            height: 75px;
                            margin: 0 auto 20px;
                            border-radius: 50%;
                            background: #e7f6ec;
                            color: #176b35;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 38px;
                            font-weight: bold;
                        }

                        .success-card h1 {
                            color: #1b5e20;
                            font-size: 26px;
                            margin-bottom: 10px;
                        }

                        .success-card .message {
                            color: #777;
                            font-size: 14px;
                            margin-bottom: 28px;
                        }

                        /* DETAILS */

                        .details {
                            background: #f7faf8;
                            border-radius: 12px;
                            padding: 20px;
                            margin-bottom: 28px;
                            text-align: left;
                        }

                        .detail-row {
                            display: flex;
                            justify-content: space-between;
                            padding: 10px 0;
                            border-bottom: 1px solid #e5eae6;
                            font-size: 14px;
                        }

                        .detail-row:last-child {
                            border-bottom: none;
                        }

                        .detail-label {
                            color: #777;
                        }

                        .detail-value {
                            color: #263238;
                            font-weight: bold;
                            text-align: right;
                        }

                        /* BUTTONS */

                        .buttons {
                            display: flex;
                            justify-content: center;
                            gap: 12px;
                        }

                        .btn {
                            padding: 12px 20px;
                            border-radius: 8px;
                            text-decoration: none;
                            font-size: 14px;
                            font-weight: bold;
                            transition: 0.2s;
                        }

                        .btn-primary {
                            background: #1b5e20;
                            color: white;
                        }

                        .btn-primary:hover {
                            background: #2e7d32;
                        }

                        .btn-secondary {
                            background: #e8f0e9;
                            color: #1b5e20;
                        }

                        .btn-secondary:hover {
                            background: #dce8de;
                        }

                        /* RESPONSIVE */

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
                                min-height: auto;
                                padding: 25px 15px;
                            }

                            .success-card {
                                padding: 30px 20px;
                            }

                            .buttons {
                                flex-direction: column;
                            }

                            .btn {
                                width: 100%;
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

                            <li>
                                <a href="/FoodStockManagement/dashboard">
                                    Dashboard
                                </a>
                            </li>

                            <li>
                                <a href="/FoodStockManagement/inventory">
                                    Food Inventory
                                </a>
                            </li>

                            <li>
                                <a href="/FoodStockManagement/index.html"
                                   class="active">
                                    Add Food
                                </a>
                            </li>

                            <li>
                                <a href="/FoodStockManagement/stockTransaction">
                                    Stock Transactions
                                </a>
                            </li>

                            <li>
                                <a href="/FoodStockManagement/transactionHistory">
                                    Transaction History
                                </a>
                            </li>

                            <li>
                                <a href="/FoodStockManagement/lowStock">
                                    Low Stock Alerts
                                </a>
                            </li>

                            <li>
                                <a href="/FoodStockManagement/expiry">
                                    Expiry Tracking
                                </a>
                            </li>

                            <li>
                                <a href="/FoodStockManagement/reports">
                                    Reports
                                </a>
                            </li>

                        </ul>

                    </aside>


                    <main class="main">

                        <div class="success-card">

                            <div class="success-icon">
                                ✓
                            </div>

                            <h1>
                                Food Added Successfully
                            </h1>

                            <p class="message">
                                The food item has been successfully added
                                to your inventory.
                            </p>

                            <div class="details">

                                <div class="detail-row">
                                    <span class="detail-label">
                                        Food Name
                                    </span>

                                    <span class="detail-value">
                                        """ + foodName + """
                                    </span>
                                </div>

                                <div class="detail-row">
                                    <span class="detail-label">
                                        Category
                                    </span>

                                    <span class="detail-value">
                                        """ + category + """
                                    </span>
                                </div>

                                <div class="detail-row">
                                    <span class="detail-label">
                                        Quantity
                                    </span>

                                    <span class="detail-value">
                                        """ + quantity + " " + unit + """
                                    </span>
                                </div>

                                <div class="detail-row">
                                    <span class="detail-label">
                                        Price
                                    </span>

                                    <span class="detail-value">
                                        ₹""" + String.format("%.2f", price) + """
                                    </span>
                                </div>

                                <div class="detail-row">
                                    <span class="detail-label">
                                        Supplier
                                    </span>

                                    <span class="detail-value">
                                        """ + supplier + """
                                    </span>
                                </div>

                                <div class="detail-row">
                                    <span class="detail-label">
                                        Expiry Date
                                    </span>

                                    <span class="detail-value">
                                        """ + expiryDate + """
                                    </span>
                                </div>

                            </div>

                            <div class="buttons">

                                <a class="btn btn-primary"
                                   href="/FoodStockManagement/inventory">
                                    View Inventory
                                </a>

                                <a class="btn btn-secondary"
                                   href="/FoodStockManagement/index.html">
                                    Add Another Food
                                </a>

                            </div>

                        </div>

                    </main>

                </body>

                </html>
                """);

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().println("""
                <!DOCTYPE html>
                <html lang="en">

                <head>

                    <meta charset="UTF-8">

                    <title>Error - FoodStock</title>

                    <style>

                        body {
                            margin: 0;
                            background: #f4f7f5;
                            font-family: Arial, sans-serif;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            min-height: 100vh;
                        }

                        .error-card {
                            background: white;
                            padding: 40px;
                            border-radius: 15px;
                            box-shadow: 0 5px 20px rgba(0,0,0,0.08);
                            text-align: center;
                            max-width: 500px;
                        }

                        h1 {
                            color: #b42318;
                        }

                        p {
                            color: #666;
                        }

                        a {
                            display: inline-block;
                            margin-top: 15px;
                            padding: 12px 20px;
                            background: #1b5e20;
                            color: white;
                            text-decoration: none;
                            border-radius: 8px;
                        }

                    </style>

                </head>

                <body>

                    <div class="error-card">

                        <h1>
                            Error Saving Food Item
                        </h1>

                        <p>
                            """ + e.getMessage() + """
                        </p>

                        <a href="/FoodStockManagement/index.html">
                            Back to Add Food
                        </a>

                    </div>

                </body>

                </html>
                """);

        } finally {

            if (em != null && em.isOpen()) {
                em.close();
            }

        }
    }

    @Override
    public void destroy() {

        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}