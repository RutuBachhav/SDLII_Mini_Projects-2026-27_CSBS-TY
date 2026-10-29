<%@page import="com.entity.Loan"%>
<%@page import="jakarta.persistence.EntityManager"%>
<%@page import="jakarta.persistence.EntityManagerFactory"%>
<%@page import="jakarta.persistence.Persistence"%>
<%@page import="java.util.List"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Return Book - Library Management System</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Inter', sans-serif;
            background: linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            color: #2c3e50;
        }

        .header {
            background: linear-gradient(135deg, #2F3E34 0%, #3d5243 100%);
            color: #ffffff;
            padding: 24px 40px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
            flex-wrap: wrap;
            gap: 16px;
        }

        .header-left h1 {
            font-size: 22px;
            font-weight: 700;
            letter-spacing: -0.5px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .header-left p {
            font-size: 13px;
            color: #d1dcce;
            margin-top: 4px;
        }

        .header-right {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .header-btn {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 20px;
            border-radius: 8px;
            text-decoration: none;
            font-size: 14px;
            font-weight: 500;
            transition: all 0.3s ease;
        }

        .btn-view {
            background-color: #27ae60;
            color: #ffffff;
        }

        .btn-view:hover {
            background-color: #219653;
            transform: translateY(-2px);
        }

        .btn-home {
            background-color: rgba(255, 255, 255, 0.15);
            color: #ffffff;
            border: 1px solid rgba(255, 255, 255, 0.25);
        }

        .btn-home:hover {
            background-color: rgba(255, 255, 255, 0.25);
            transform: translateY(-2px);
        }

        .main-container {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .form-card {
            background: #ffffff;
            max-width: 540px;
            width: 100%;
            margin: 30px auto;
            border-radius: 16px;
            box-shadow: 0 4px 24px rgba(0, 0, 0, 0.07);
            padding: 40px;
            animation: fadeIn 0.5s ease-out;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(15px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .info-banner {
            background: #eafaf1;
            border-left: 4px solid #27ae60;
            padding: 14px 18px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 24px;
            color: #1e8449;
            font-size: 13.5px;
            line-height: 1.45;
        }

        .form-group {
            margin-bottom: 24px;
        }

        label {
            display: block;
            font-size: 14px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 8px;
        }

        select {
            width: 100%;
            padding: 14px 16px;
            border: 2px solid #e0e6ed;
            border-radius: 10px;
            font-size: 14px;
            font-family: 'Inter', sans-serif;
            color: #2c3e50;
            background-color: #ffffff;
            transition: border-color 0.25s ease;
        }

        select:focus {
            border-color: #27ae60;
            box-shadow: 0 0 0 3px rgba(39, 174, 96, 0.15);
            outline: none;
        }

        .btn-submit {
            width: 100%;
            background: linear-gradient(135deg, #27ae60, #219653);
            color: #ffffff;
            border: none;
            padding: 14px;
            border-radius: 10px;
            font-size: 15px;
            font-weight: 600;
            font-family: 'Inter', sans-serif;
            cursor: pointer;
            transition: all 0.25s ease;
            box-shadow: 0 4px 14px rgba(39, 174, 96, 0.3);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }

        .btn-submit:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 18px rgba(39, 174, 96, 0.4);
        }

        .nav-links {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 12px;
            margin-top: 24px;
            padding-top: 20px;
            border-top: 1px solid #edf2f7;
            flex-wrap: wrap;
        }

        .pill-button {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 8px 16px;
            border-radius: 50px;
            font-size: 13px;
            font-weight: 500;
            text-decoration: none;
            color: #4a5568;
            background: #f0f4f8;
            transition: all 0.25s ease;
        }

        .pill-button:hover {
            background: #e2e8f0;
            color: #2d3748;
            transform: translateY(-1px);
        }

        .footer {
            text-align: center;
            color: #95a5a6;
            font-size: 13px;
            padding: 24px 16px;
            margin-top: auto;
        }
    </style>
</head>
<body>

<div class="header">
    <div class="header-left">
        <h1>&#9989; Return Book</h1>
        <p>Process a book return and calculate overdue fines</p>
    </div>
    <div class="header-right">
        <a href="viewLoan.jsp" class="header-btn btn-view">View Loans</a>
        <a href="index.html" class="header-btn btn-home">Home</a>
    </div>
</div>

<div class="main-container">
    <div class="form-card">
        <div class="info-banner">
            <span>&#128161; Returning a book automatically restores the inventory copy. Overdue fines ($5.00/day) are calculated automatically.</span>
        </div>

        <%
            EntityManagerFactory emf = null;
            EntityManager em = null;
            try {
                emf = Persistence.createEntityManagerFactory("LibraryPU");
                em = emf.createEntityManager();
                List<Loan> activeLoans = em.createQuery(
                    "SELECT l FROM Loan l WHERE l.status = 'ISSUED' ORDER BY l.dueDate",
                    Loan.class
                ).getResultList();

                if (activeLoans.isEmpty()) {
        %>
            <div style="text-align:center;padding:20px 0;">
                <h3>No Active Loans</h3>
                <p style="color:#7f8c8d;margin-top:6px;">All issued books have been returned.</p>
                <br>
                <a href="viewLoan.jsp" class="pill-button">View Loan History</a>
            </div>
        <%
                } else {
        %>
        <form action="returnLoan" method="post">
            <div class="form-group">
                <label>Select Loan Record to Return *</label>
                <select name="loanId" required>
                    <option value="">-- Select Active Loan --</option>
                    <%
                        for (Loan l : activeLoans) {
                            String overdueNote = l.isOverdue() ? " [OVERDUE!]" : "";
                    %>
                    <option value="<%= l.getLoanId() %>">
                        #<%= l.getLoanId() %>: "<%= l.getBook().getTitle() %>" &rarr; <%= l.getMember().getFullName() %> (Due: <%= l.getDueDate() %>)<%= overdueNote %>
                    </option>
                    <%
                        }
                    %>
                </select>
            </div>
            <button type="submit" class="btn-submit">&#9989; Confirm Book Return</button>
        </form>
        <%
                }
            } catch (Exception e) {
        %>
            <div style="color:red;">Error: <%= e.getMessage() %></div>
        <%
            } finally {
                if (em != null && em.isOpen()) { em.close(); }
                if (emf != null && emf.isOpen()) { emf.close(); }
            }
        %>

        <div class="nav-links">
            <a href="viewLoan.jsp" class="pill-button">View Loans</a>
            <a href="index.html" class="pill-button">Back to Home</a>
        </div>
    </div>
</div>

<footer class="footer">
    Library Management System
</footer>

</body>
</html>
