<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Member - Library Management System</title>
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
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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
        }

        .header-left h1 {
            font-size: 24px;
            font-weight: 700;
            letter-spacing: -0.5px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .header-left p {
            font-size: 14px;
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

        .header-btn.btn-view {
            background-color: #3498db;
            color: #ffffff;
        }

        .header-btn.btn-view:hover {
            background-color: #2980b9;
            transform: translateY(-2px);
        }

        .header-btn.btn-home {
            background-color: rgba(255, 255, 255, 0.15);
            color: #ffffff;
            border: 1px solid rgba(255, 255, 255, 0.25);
        }

        .header-btn.btn-home:hover {
            background-color: rgba(255, 255, 255, 0.25);
            transform: translateY(-2px);
        }

        .main-wrapper {
            flex: 1;
            padding: 20px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(18px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .form-card {
            background: #ffffff;
            width: 100%;
            max-width: 580px;
            margin: 30px auto;
            border-radius: 16px;
            box-shadow: 0 4px 24px rgba(0, 0, 0, 0.07);
            padding: 40px;
            animation: fadeIn 0.5s ease-out forwards;
        }

        .card-header {
            margin-bottom: 28px;
            padding-bottom: 16px;
            border-bottom: 1px solid #edf2f7;
        }

        .card-header h2 {
            font-size: 20px;
            font-weight: 700;
            color: #2F3E34;
        }

        .card-header p {
            font-size: 13px;
            color: #7f8c8d;
            margin-top: 4px;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            font-size: 14px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 8px;
        }

        .form-group input,
        .form-group textarea {
            width: 100%;
            padding: 14px 16px;
            border: 2px solid #e0e6ed;
            border-radius: 10px;
            font-size: 14px;
            font-family: 'Inter', sans-serif;
            color: #2c3e50;
            background-color: #fafbfc;
            transition: all 0.2s ease-in-out;
        }

        .form-group input:focus,
        .form-group textarea:focus {
            outline: none;
            border-color: #3498db;
            background-color: #ffffff;
            box-shadow: 0 0 0 4px rgba(52, 152, 219, 0.1);
        }

        .btn-submit {
            width: 100%;
            background: linear-gradient(135deg, #3498db, #2980b9);
            color: #ffffff;
            padding: 14px;
            border: none;
            border-radius: 10px;
            font-size: 15px;
            font-weight: 600;
            font-family: 'Inter', sans-serif;
            cursor: pointer;
            box-shadow: 0 4px 12px rgba(52, 152, 219, 0.25);
            transition: all 0.25s ease;
            margin-top: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }

        .btn-submit:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 16px rgba(52, 152, 219, 0.35);
        }

        .nav-links {
            display: flex;
            justify-content: center;
            gap: 12px;
            margin-top: 24px;
            flex-wrap: wrap;
        }

        .pill-btn {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 10px 20px;
            background: #ffffff;
            color: #4a5568;
            border: 1px solid #e2e8f0;
            border-radius: 9999px;
            font-size: 13px;
            font-weight: 500;
            text-decoration: none;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
            transition: all 0.25s ease;
        }

        .pill-btn:hover {
            background: #f8fafc;
            color: #2F3E34;
            transform: translateY(-2px);
        }

        .footer {
            text-align: center;
            padding: 24px;
            color: #718096;
            font-size: 13px;
            margin-top: auto;
        }
    </style>
</head>
<body>

<header class="header">
    <div class="header-left">
        <h1>&#128101; Add Member</h1>
        <p>Register a new library member</p>
    </div>
    <div class="header-right">
        <a href="viewMember.jsp" class="header-btn btn-view">View Members</a>
        <a href="index.html" class="header-btn btn-home">Home</a>
    </div>
</header>

<div class="main-wrapper">
    <div class="form-card">
        <div class="card-header">
            <h2>Member Information</h2>
            <p>Please enter the member's details to issue library cards.</p>
        </div>

        <form action="saveMember" method="post">
            <div class="form-group">
                <label for="fullName">Full Name *</label>
                <input type="text" id="fullName" name="fullName" placeholder="Enter Full Name" required autofocus>
            </div>

            <div class="form-group">
                <label for="email">Email Address *</label>
                <input type="email" id="email" name="email" placeholder="member@example.com" required>
            </div>

            <div class="form-group">
                <label for="phone">Phone Number</label>
                <input type="text" id="phone" name="phone" placeholder="+1 (555) 000-0000">
            </div>

            <div class="form-group">
                <label for="address">Address</label>
                <textarea id="address" name="address" rows="3" placeholder="Residential or campus address"></textarea>
            </div>

            <button type="submit" class="btn-submit">
                <span>&#10003;</span> Save Member
            </button>
        </form>

        <div class="nav-links">
            <a href="viewMember.jsp" class="pill-btn">
                <span>&#128101;</span> View Members
            </a>
            <a href="index.html" class="pill-btn">
                <span>&#127960;</span> Back to Home
            </a>
        </div>
    </div>
</div>

<footer class="footer">
    Library Management System
</footer>

</body>
</html>

