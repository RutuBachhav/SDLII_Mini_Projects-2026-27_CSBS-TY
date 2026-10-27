
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>FlatDesk | Smart Real Estate Management</title>

    <link rel="stylesheet" href="assets/style.css">

    <style>
        * {
            box-sizing: border-box;
        }

        html {
            scroll-behavior: smooth;
        }

        body {
            margin: 0;
            font-family: Arial, Helvetica, sans-serif;
            background: #f5f1ea;
            color: #24211f;
        }

        a {
            text-decoration: none;
        }

        .home-navbar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 20px;
            padding: 18px 7%;
            background: #fff;
            border-bottom: 1px solid #e8ded2;
        }

        .home-logo {
            color: #171717;
            font-size: 31px;
            font-weight: 800;
            letter-spacing: -1px;
        }

        .home-logo span {
            color: #c9783a;
        }

        .home-nav-actions {
            display: flex;
            align-items: center;
            gap: 13px;
        }

        .nav-login,
        .nav-register {
            padding: 11px 21px;
            border-radius: 8px;
            font-weight: 700;
            transition: .2s;
        }

        .nav-login {
            color: #a85f2a;
            border: 1px solid #c9783a;
            background: #fff;
        }

        .nav-login:hover {
            background: #fff5ec;
        }

        .nav-register {
            color: #fff;
            background: #c9783a;
        }

        .nav-register:hover {
            background: #a85f2a;
        }

        .hero {
            position: relative;
            overflow: hidden;
            padding: 85px 7% 90px;
            background: linear-gradient(135deg, #fffaf3 0%, #f3e4d3 100%);
        }

        .hero::before {
            content: "";
            position: absolute;
            width: 350px;
            height: 350px;
            right: -100px;
            top: -130px;
            border-radius: 50%;
            background: rgba(201, 120, 58, .10);
        }

        .hero-content {
            position: relative;
            z-index: 1;
            max-width: 820px;
            margin: 0 auto;
            text-align: center;
        }

        .hero-label {
            display: inline-block;
            padding: 9px 16px;
            margin-bottom: 22px;
            color: #a85f2a;
            background: #f8e7d6;
            border: 1px solid #efd3b8;
            border-radius: 30px;
            font-size: 12px;
            font-weight: 800;
            letter-spacing: 1.8px;
        }

        .hero h1 {
            margin: 0 0 20px;
            color: #152b4a;
            font-size: clamp(43px, 6vw, 72px);
            line-height: 1.12;
            letter-spacing: -2px;
        }

        .hero h1 span {
            color: #c9783a;
        }

        .hero-description {
            max-width: 650px;
            margin: 0 auto;
            color: #77716b;
            font-size: 18px;
            line-height: 1.8;
        }

        .hero-actions {
            display: flex;
            justify-content: center;
            gap: 14px;
            flex-wrap: wrap;
            margin-top: 32px;
        }

        .hero-primary,
        .hero-secondary {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-height: 52px;
            padding: 14px 25px;
            border-radius: 9px;
            font-size: 15px;
            font-weight: 700;
            transition: .2s;
        }

        .hero-primary {
            color: #fff;
            background: #c9783a;
            box-shadow: 0 7px 18px rgba(201, 120, 58, .2);
        }

        .hero-primary:hover {
            color: #fff;
            background: #a85f2a;
            transform: translateY(-2px);
        }

        .hero-secondary {
            color: #152b4a;
            background: #fff;
            border: 1px solid #e8ded2;
        }

        .hero-secondary:hover {
            background: #f8eee3;
        }

        .hero-stats {
            display: flex;
            justify-content: center;
            gap: 45px;
            flex-wrap: wrap;
            margin-top: 48px;
        }

        .hero-stat strong {
            display: block;
            color: #152b4a;
            font-size: 21px;
        }

        .hero-stat span {
            color: #77716b;
            font-size: 13px;
        }

        .section {
            max-width: 1200px;
            margin: 0 auto;
            padding: 75px 25px;
        }

        .section-heading {
            margin-bottom: 35px;
            text-align: center;
        }

        .section-heading .eyebrow {
            color: #c9783a;
            font-size: 12px;
            font-weight: 800;
            letter-spacing: 2px;
        }

        .section-heading h2 {
            margin: 12px 0;
            color: #152b4a;
            font-size: clamp(28px, 4vw, 40px);
        }

        .section-heading p {
            max-width: 600px;
            margin: 0 auto;
            color: #77716b;
            line-height: 1.8;
        }

        .feature-grid {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 22px;
        }

        .feature-card {
            padding: 28px;
            background: #fff;
            border: 1px solid #e8ded2;
            border-radius: 15px;
            box-shadow: 0 8px 24px rgba(55, 39, 24, .04);
            transition: transform .2s, box-shadow .2s;
        }

        .feature-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 32px rgba(55, 39, 24, .09);
        }

        .feature-icon {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 53px;
            height: 53px;
            margin-bottom: 19px;
            border-radius: 13px;
            background: #f8e7d6;
            font-size: 25px;
        }

        .feature-card h3 {
            margin: 0 0 10px;
            color: #152b4a;
            font-size: 19px;
        }

        .feature-card p {
            margin: 0;
            color: #77716b;
            font-size: 14px;
            line-height: 1.8;
        }

        .cta-section {
            margin: 0 7% 65px;
            padding: 48px 25px;
            text-align: center;
            background: #152b4a;
            border-radius: 18px;
        }

        .cta-section h2 {
            margin: 0 0 12px;
            color: #fff;
            font-size: clamp(26px, 4vw, 36px);
        }

        .cta-section p {
            margin: 0 auto;
            max-width: 600px;
            color: #d4dbe6;
            line-height: 1.8;
        }

        .cta-section .hero-primary {
            margin-top: 23px;
        }

        .home-footer {
            padding: 25px 7%;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 15px;
            background: #fff;
            border-top: 1px solid #e8ded2;
        }

        .home-footer p {
            color: #77716b;
            font-size: 13px;
        }

        .home-footer a {
            color: #a85f2a;
            font-size: 14px;
            font-weight: 700;
        }

        @media (max-width: 760px) {
            .home-navbar {
                padding: 16px 5%;
            }

            .home-logo {
                font-size: 27px;
            }

            .hero {
                padding: 65px 6%;
            }

            .hero h1 {
                letter-spacing: -1px;
            }

            .hero-stats {
                gap: 25px;
            }

            .feature-grid {
                grid-template-columns: 1fr;
            }

            .section {
                padding: 55px 18px;
            }

            .cta-section {
                margin: 0 18px 45px;
                padding: 38px 20px;
            }
        }

        @media (max-width: 420px) {
            .home-navbar {
                align-items: flex-start;
                flex-direction: column;
            }

            .home-nav-actions {
                width: 100%;
            }

            .nav-login,
            .nav-register {
                flex: 1;
                text-align: center;
            }

            .hero-actions {
                flex-direction: column;
            }

            .hero-primary,
            .hero-secondary {
                width: 100%;
            }

            .home-footer {
                flex-direction: column;
                text-align: center;
            }
        }
    </style>
</head>

<body>

    <!-- Navigation -->
    <header class="home-navbar">
        <a href="index.jsp" class="home-logo">
            Flat<span>Desk</span>
        </a>

        <div class="home-nav-actions">
            <a href="login.jsp" class="nav-login">Login</a>
            <a href="login.jsp" class="nav-register">Get Started &rarr;</a>
        </div>
    </header>

    <!-- Hero Section -->
    <main>
        <section class="hero">
            <div class="hero-content">

                <div class="hero-label">
                    SMART REAL ESTATE MANAGEMENT
                </div>

                <h1>
                    Find. Manage.<br>
                    <span>Grow.</span>
                </h1>

                <p class="hero-description">
                    Your property journey starts here.
                    Discover listings, manage properties, and connect
                    with the right people through one simple platform.
                </p>

                <div class="hero-actions">
                    <a href="login.jsp" class="hero-primary">
                        Open FlatDesk &rarr;
                    </a>

                    <a href="#features" class="hero-secondary">
                        Explore Features
                    </a>
                </div>

                <div class="hero-stats">
                    <div class="hero-stat">
                        <strong>Simple</strong>
                        <span>Property Management</span>
                    </div>

                    <div class="hero-stat">
                        <strong>Connected</strong>
                        <span>Owners &amp; Brokers</span>
                    </div>

                    <div class="hero-stat">
                        <strong>Organized</strong>
                        <span>Property Listings</span>
                    </div>
                </div>

            </div>
        </section>

        <!-- Features -->
        <section class="section" id="features">

            <div class="section-heading">
                <div class="eyebrow">WHAT YOU CAN DO</div>
                <h2>Everything in One Place</h2>
                <p>
                    A simple and organized way to manage your real estate
                    activities, whether you are looking for a property
                    or managing your own listings.
                </p>
            </div>

            <div class="feature-grid">

                <article class="feature-card">
                    <div class="feature-icon">🏠</div>
                    <h3>Find Properties</h3>
                    <p>
                        Browse available property listings and explore
                        details to help you find the right place.
                    </p>
                </article>

                <article class="feature-card">
                    <div class="feature-icon">🔑</div>
                    <h3>Manage Listings</h3>
                    <p>
                        Owners and brokers can organize property
                        information and keep listing details updated.
                    </p>
                </article>

                <article class="feature-card">
                    <div class="feature-icon">📊</div>
                    <h3>Stay Organized</h3>
                    <p>
                        Use a dedicated dashboard to keep property
                        information organized in one convenient place.
                    </p>
                </article>

            </div>
        </section>

        <!-- Call to Action -->
        <section class="cta-section">
            <h2>Ready to Get Started?</h2>
            <p>
                Enter FlatDesk and explore a simpler way to manage
                your real estate activities.
            </p>

            <a href="login.jsp" class="hero-primary">
                Login to FlatDesk &rarr;
            </a>
        </section>
    </main>

    <!-- Footer -->
    <footer class="home-footer">
        <a href="index.jsp" class="home-logo">
            Flat<span>Desk</span>
        </a>

        <p>&copy; 2026 FlatDesk. Smart Real Estate Management.</p>

        <a href="login.jsp">Login to your account</a>
    </footer>

</body>
</html>
