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
    <title>Delete Member - Library Management System</title>
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
            max-width: 520px;
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

        .warning-banner {
            background: #fef5e7;
            border-left: 4px solid #f39c12;
            padding: 14px 18px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 24px;
            color: #8a6d3b;
            font-size: 13.5px;
            line-height: 1.45;
        }

        .warning-icon {
            font-size: 18px;
            flex-shrink: 0;
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
            border-color: #e74c3c;
            box-shadow: 0 0 0 3px rgba(231, 76, 60, 0.15);
            outline: none;
        }

        .btn-submit {
            width: 100%;
            background: linear-gradient(135deg, #e74c3c, #c0392b);
            color: #ffffff;
            border: none;
            padding: 14px;
            border-radius: 10px;
            font-size: 15px;
            font-weight: 600;
            font-family: 'Inter', sans-serif;
            cursor: pointer;
            transition: all 0.25s ease;
            box-shadow: 0 4px 14px rgba(231, 76, 60, 0.3);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }

        .btn-submit:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 18px rgba(231, 76, 60, 0.4);
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
        <h1>&#128465; Delete Member</h1>
        <p>Remove a member record</p>
    </div>
    <div class="header-right">
        <a href="viewMember.jsp" class="header-btn btn-view">View Members</a>
        <a href="index.html" class="header-btn btn-home">Home</a>
    </div>
</div>

<div class="main-container">
    <div class="form-card">
        <div class="warning-banner">
            <span class="warning-icon">&#9888;&#65039;</span>
            <span>Members with active unreturned books cannot be deleted.</span>
        </div>

        <%
            EntityManagerFactory emf = null;
            EntityManager em = null;
            try {
                emf = Persistence.createEntityManagerFactory("LibraryPU");
                em = emf.createEntityManager();
                List<Member> members = em.createNamedQuery("Member.findAll", Member.class).getResultList();
        %>
        <form action="deleteMember" method="post" onsubmit="return confirm('Are you sure you want to delete this member?');">
            <div class="form-group">
                <label>Select Member</label>
                <select name="memberId" required>
                    <option value="">-- Select Member --</option>
                    <%
                        for (Member m : members) {
                    %>
                    <option value="<%= m.getMemberId() %>">
                        #<%= m.getMemberId() %> - <%= m.getFullName() %> (<%= m.getEmail() %>)
                    </option>
                    <%
                        }
                    %>
                </select>
            </div>
            <button type="submit" class="btn-submit">&#128465; Delete Member</button>
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

        <div class="nav-links">
            <a href="viewMember.jsp" class="pill-button">View Members</a>
            <a href="index.html" class="pill-button">Back to Home</a>
        </div>
    </div>
</div>

<footer class="footer">
    Library Management System
</footer>

</body>
</html>
