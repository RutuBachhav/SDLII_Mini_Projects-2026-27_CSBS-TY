<%@page import="com.entity.Member"%>
<%@page import="jakarta.persistence.EntityManager"%>
<%@page import="jakarta.persistence.EntityManagerFactory"%>
<%@page import="jakarta.persistence.Persistence"%>
<%@page import="java.util.List"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Update Member - Library Management System</title>
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
            padding: 24px 20px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
        }

        .header-inner {
            max-width: 1000px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 16px;
        }

        .header-title-group h1 {
            font-size: 1.75rem;
            font-weight: 700;
            letter-spacing: -0.5px;
            margin-bottom: 4px;
        }

        .header-title-group p {
            font-size: 0.95rem;
            color: #d1dcce;
            font-weight: 400;
        }

        .header-nav {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .header-btn {
            display: inline-flex;
            align-items: center;
            padding: 10px 18px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            text-decoration: none;
            color: #ffffff;
            transition: transform 0.2s ease;
        }

        .btn-view {
            background-color: #27ae60;
        }

        .btn-view:hover {
            background-color: #219653;
            transform: translateY(-2px);
        }

        .btn-home {
            background-color: rgba(255, 255, 255, 0.15);
        }

        .btn-home:hover {
            background-color: rgba(255, 255, 255, 0.25);
            transform: translateY(-2px);
        }

        .main-container {
            flex: 1;
            width: 90%;
            max-width: 580px;
            margin: 30px auto;
            animation: fadeIn 0.5s ease-out;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(15px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .form-card {
            background: #ffffff;
            border-radius: 16px;
            box-shadow: 0 4px 24px rgba(0, 0, 0, 0.07);
            padding: 40px;
        }

        .form-group {
            margin-bottom: 20px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-size: 14px;
            font-weight: 600;
            color: #2c3e50;
        }

        input[type="text"],
        input[type="email"],
        textarea,
        select {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e0e6ed;
            border-radius: 10px;
            font-size: 14px;
            font-family: 'Inter', sans-serif;
            color: #2c3e50;
            background-color: #ffffff;
            transition: border-color 0.2s ease;
        }

        input:focus,
        textarea:focus,
        select:focus {
            border-color: #f39c12;
            box-shadow: 0 0 0 3px rgba(243, 156, 18, 0.2);
            outline: none;
        }

        button[type="submit"] {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #f39c12, #e67e22);
            color: #ffffff;
            border: none;
            border-radius: 10px;
            font-size: 15px;
            font-weight: 600;
            font-family: 'Inter', sans-serif;
            cursor: pointer;
            box-shadow: 0 4px 14px rgba(243, 156, 18, 0.3);
            transition: transform 0.2s ease;
            margin-top: 10px;
        }

        button[type="submit"]:hover {
            transform: translateY(-2px);
        }

        .card-footer-nav {
            display: flex;
            justify-content: center;
            gap: 12px;
            margin-top: 28px;
            padding-top: 20px;
            border-top: 1px solid #edf2f7;
        }

        .pill-link {
            display: inline-block;
            padding: 8px 18px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 500;
            color: #4a5568;
            background-color: #edf2f7;
            text-decoration: none;
        }

        .pill-link:hover {
            background-color: #e2e8f0;
            color: #1a202c;
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

<header class="header">
    <div class="header-inner">
        <div class="header-title-group">
            <h1>&#9999;&#65039; Update Member</h1>
            <p>Update member registration information</p>
        </div>
        <nav class="header-nav">
            <a href="viewMember.jsp" class="header-btn btn-view">View Members</a>
            <a href="index.html" class="header-btn btn-home">Home</a>
        </nav>
    </div>
</header>

<main class="main-container">
    <div class="form-card">
        <%
            EntityManagerFactory emf = null;
            EntityManager em = null;
            try {
                emf = Persistence.createEntityManagerFactory("LibraryPU");
                em = emf.createEntityManager();
                List<Member> members = em.createNamedQuery("Member.findAll", Member.class).getResultList();
        %>
        <form action="updateMember" method="post">
            <div class="form-group">
                <label for="memberId">Select Member</label>
                <select id="memberId" name="memberId" required onchange="populateMemberFields(this)">
                    <option value="">-- Select a Member --</option>
                    <% for (Member m : members) { %>
                    <option value="<%= m.getMemberId() %>"
                            data-name="<%= m.getFullName() %>"
                            data-email="<%= m.getEmail() %>"
                            data-phone="<%= m.getPhone() != null ? m.getPhone() : "" %>"
                            data-address="<%= m.getAddress() != null ? m.getAddress() : "" %>">
                        #<%= m.getMemberId() %> - <%= m.getFullName() %> (<%= m.getEmail() %>)
                    </option>
                    <% } %>
                </select>
            </div>

            <div class="form-group">
                <label for="fullName">Full Name</label>
                <input type="text" id="fullName" name="fullName" required>
            </div>

            <div class="form-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" required>
            </div>

            <div class="form-group">
                <label for="phone">Phone</label>
                <input type="text" id="phone" name="phone">
            </div>

            <div class="form-group">
                <label for="address">Address</label>
                <textarea id="address" name="address" rows="3"></textarea>
            </div>

            <button type="submit">&#9999;&#65039; Update Member</button>
        </form>
        <%
            } catch (Exception e) {
        %>
            <div style="color:red;">Error: <%= e.getMessage() %></div>
        <%
            } finally {
                if (em != null && em.isOpen()) { em.close(); }
                if (emf != null && emf.isOpen()) { emf.close(); }
            }
        %>

        <div class="card-footer-nav">
            <a href="viewMember.jsp" class="pill-link">View Members</a>
            <a href="index.html" class="pill-link">Back to Home</a>
        </div>
    </div>
</main>

<footer class="footer">
    Library Management System
</footer>

<script>
    function populateMemberFields(select) {
        var opt = select.options[select.selectedIndex];
        if (opt.value) {
            document.getElementById("fullName").value = opt.getAttribute("data-name") || "";
            document.getElementById("email").value = opt.getAttribute("data-email") || "";
            document.getElementById("phone").value = opt.getAttribute("data-phone") || "";
            document.getElementById("address").value = opt.getAttribute("data-address") || "";
        }
    }
</script>

</body>
</html>
