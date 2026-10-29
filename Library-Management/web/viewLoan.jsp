<%@page import="com.entity.Loan"%>
<%@page import="jakarta.persistence.EntityManager"%>
<%@page import="jakarta.persistence.EntityManagerFactory"%>
<%@page import="jakarta.persistence.Persistence"%>
<%@page import="java.util.List"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>View Loans - Library Management System</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Inter', Arial, sans-serif;
            background: linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        .header {
            background: linear-gradient(135deg, #2F3E34 0%, #3d5243 100%);
            color: white;
            padding: 28px 40px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            box-shadow: 0 4px 20px rgba(0,0,0,0.15);
        }

        .header-left h1 {
            font-size: 22px;
            font-weight: 700;
            letter-spacing: -0.5px;
        }

        .header-left p {
            font-size: 13px;
            opacity: 0.8;
            margin-top: 4px;
        }

        .header-right {
            display: flex;
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

        .btn-add {
            background: #f39c12;
            color: white;
        }

        .btn-add:hover {
            background: #e67e22;
            transform: translateY(-2px);
        }

        .btn-home {
            background: rgba(255,255,255,0.15);
            color: white;
            border: 1px solid rgba(255,255,255,0.3);
        }

        .btn-home:hover {
            background: rgba(255,255,255,0.25);
            transform: translateY(-2px);
        }

        .container {
            max-width: 1100px;
            width: 92%;
            margin: 30px auto;
            padding: 0 20px;
            flex: 1;
        }

        .stats-bar {
            display: flex;
            gap: 20px;
            margin-bottom: 24px;
            flex-wrap: wrap;
        }

        .stat-card {
            background: white;
            border-radius: 12px;
            padding: 20px 28px;
            min-width: 220px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            display: flex;
            align-items: center;
            gap: 16px;
            transition: transform 0.2s ease;
        }

        .stat-card:hover {
            transform: translateY(-3px);
        }

        .stat-icon {
            width: 48px;
            height: 48px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
        }

        .stat-icon.orange { background: #fef5e7; }
        .stat-icon.green { background: #eafaf1; }

        .stat-info h3 {
            font-size: 24px;
            font-weight: 700;
            color: #2c3e50;
        }

        .stat-info p {
            font-size: 13px;
            color: #95a5a6;
            margin-top: 2px;
        }

        .toolbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            flex-wrap: wrap;
            gap: 12px;
        }

        .search-box {
            position: relative;
            width: 320px;
        }

        .search-box input {
            width: 100%;
            padding: 12px 16px 12px 42px;
            border: 2px solid #e0e6ed;
            border-radius: 10px;
            font-size: 14px;
            font-family: 'Inter', sans-serif;
            background: white;
        }

        .search-box input:focus {
            outline: none;
            border-color: #3498db;
            box-shadow: 0 0 0 4px rgba(52,152,219,0.1);
        }

        .search-box::before {
            content: "\u{1F50D}";
            position: absolute;
            left: 14px;
            top: 50%;
            transform: translateY(-50%);
            font-size: 16px;
            opacity: 0.4;
        }

        .record-count {
            font-size: 14px;
            color: #7f8c8d;
            font-weight: 500;
        }

        .table-card {
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 24px rgba(0,0,0,0.07);
            overflow: hidden;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        thead th {
            background: #f8fafb;
            color: #2c3e50;
            padding: 16px 20px;
            text-align: left;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.8px;
            border-bottom: 2px solid #eef2f7;
        }

        tbody td {
            padding: 16px 20px;
            color: #34495e;
            font-size: 14px;
            border-bottom: 1px solid #f0f3f6;
            transition: background 0.2s ease;
        }

        tbody tr:nth-child(even) {
            background-color: #fafbfc;
        }

        tbody tr:hover {
            background-color: #ebf5fb;
        }

        .id-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #2F3E34, #3d5243);
            color: white;
            min-width: 36px;
            height: 36px;
            padding: 0 10px;
            border-radius: 10px;
            font-size: 13px;
            font-weight: 600;
        }

        .badge-status {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
        }
        .status-issued { background: #fff3cd; color: #856404; }
        .status-returned { background: #d4edda; color: #155724; }
        .status-overdue { background: #f8d7da; color: #721c24; font-weight: bold; }

        .empty-state {
            text-align: center;
            padding: 60px 20px;
        }

        .footer {
            text-align: center;
            padding: 24px;
            color: #95a5a6;
            font-size: 13px;
        }

        @media (max-width: 768px) {
            .header {
                flex-direction: column;
                gap: 16px;
                text-align: center;
            }
            .table-card {
                overflow-x: auto;
            }
        }
    </style>
</head>
<body>

<div class="header">
    <div class="header-left">
        <h1>&#128214; Book Loan Records</h1>
        <p>Circulation & borrowing activity</p>
    </div>
    <div class="header-right">
        <a href="issueLoan.jsp" class="header-btn btn-add">&#43; Issue Book</a>
        <a href="index.html" class="header-btn btn-home">&#8962; Home</a>
    </div>
</div>

<div class="container">

    <%
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {
            emf = Persistence.createEntityManagerFactory("LibraryPU");
            em = emf.createEntityManager();

            List<Loan> loans = em.createNamedQuery("Loan.findAll", Loan.class).getResultList();
            int totalLoans = loans.size();
            int activeLoans = 0;
            int returnedLoans = 0;
            for (Loan l : loans) {
                if ("RETURNED".equals(l.getStatus())) {
                    returnedLoans++;
                } else {
                    activeLoans++;
                }
            }
    %>

    <div class="stats-bar">
        <div class="stat-card">
            <div class="stat-icon orange">&#128214;</div>
            <div class="stat-info">
                <h3><%= activeLoans %></h3>
                <p>Currently Issued</p>
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-icon green">&#9989;</div>
            <div class="stat-info">
                <h3><%= returnedLoans %></h3>
                <p>Returned Books</p>
            </div>
        </div>
    </div>

    <% if (loans.isEmpty()) { %>

    <div class="table-card">
        <div class="empty-state">
            <div style="font-size: 56px; margin-bottom: 16px;">&#128214;</div>
            <h3>No Loan Records Found</h3>
            <p>Issue a book to a member to start tracking circulation.</p>
            <br>
            <a href="issueLoan.jsp" style="display:inline-block;padding:10px 24px;background:#3498db;color:white;border-radius:8px;text-decoration:none;">Issue Book</a>
        </div>
    </div>

    <% } else { %>

    <div class="toolbar">
        <div class="search-box">
            <input type="text"
                   id="searchInput"
                   placeholder="Search loans..."
                   onkeyup="filterTable()">
        </div>
        <div class="record-count">
            Showing <strong id="visibleCount"><%= totalLoans %></strong> of
            <strong><%= totalLoans %></strong> loan records
        </div>
    </div>

    <div class="table-card">
        <table id="loanTable">
            <thead>
                <tr>
                    <th style="width: 70px;">ID</th>
                    <th>Book Title</th>
                    <th>Member Name</th>
                    <th>Issue Date</th>
                    <th>Due Date</th>
                    <th>Return Date</th>
                    <th>Status</th>
                    <th>Fine</th>
                </tr>
            </thead>
            <tbody>
                <%
                    for (Loan l : loans) {
                        String statusBadge = "status-issued";
                        String statusText = l.getStatus();
                        if (l.isReturned()) {
                            statusBadge = "status-returned";
                        } else if (l.isOverdue()) {
                            statusBadge = "status-overdue";
                            statusText = "OVERDUE";
                        }
                %>
                <tr>
                    <td>
                        <span class="id-badge"><%= l.getLoanId() %></span>
                    </td>
                    <td><strong><%= l.getBook().getTitle() %></strong></td>
                    <td><%= l.getMember().getFullName() %></td>
                    <td><%= l.getIssueDate() %></td>
                    <td><%= l.getDueDate() %></td>
                    <td><%= l.getReturnDate() != null ? l.getReturnDate() : "-" %></td>
                    <td>
                        <span class="badge-status <%= statusBadge %>"><%= statusText %></span>
                    </td>
                    <td>$<%= l.getFineAmount() != null ? l.getFineAmount() : "0.00" %></td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>
    </div>

    <% } %>

    <%
        } catch (Exception e) {
            e.printStackTrace();
    %>

    <div style="color:red;padding:20px;background:white;border-radius:10px;">
        Error: <%= e.getMessage() %>
    </div>

    <%
        } finally {
            if (em != null && em.isOpen()) { em.close(); }
            if (emf != null && emf.isOpen()) { emf.close(); }
        }
    %>

</div>

<div class="footer">
    Library Management System
</div>

<script>
    function filterTable() {
        var input = document.getElementById("searchInput");
        var filter = input.value.toLowerCase();
        var table = document.getElementById("loanTable");
        if (!table) return;

        var rows = table.getElementsByTagName("tbody")[0].getElementsByTagName("tr");
        var visible = 0;

        for (var i = 0; i < rows.length; i++) {
            var text = rows[i].textContent.toLowerCase();
            if (text.indexOf(filter) > -1) {
                rows[i].style.display = "";
                visible++;
            } else {
                rows[i].style.display = "none";
            }
        }

        var countEl = document.getElementById("visibleCount");
        if (countEl) countEl.textContent = visible;
    }
</script>

</body>
</html>
