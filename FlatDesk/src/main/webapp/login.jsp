<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>


<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>FlatDesk Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css?v=2">
</head>


<body>

<div class="auth-page">

    <div class="auth-card">

        <div class="logo">
            Flat<span>Desk</span>
        </div>

        <h2>Login</h2>

        <p>
            Login to your FlatDesk account
        </p>


        <form id="loginForm">

            <div class="form-group">

                <label for="email">
                    Email
                </label>

                <input
                    type="email"
                    id="email"
                    name="email"
                    placeholder="Enter your email"
                    required>

            </div>


            <div class="form-group">

                <label for="password">
                    Password
                </label>

                <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="Enter your password"
                    required>

            </div>


            <button
                type="submit"
                class="btn">
                Login
            </button>

        </form>


        <p id="message"></p>

    </div>

</div>


<script>

document
    .getElementById("loginForm")
    .addEventListener("submit", async function(event) {

        event.preventDefault();


        const email =
            document.getElementById("email").value.trim();

        const password =
            document.getElementById("password").value;


        const message =
            document.getElementById("message");


        message.textContent =
            "Logging in...";


        try {

            const response =
                await fetch("api/auth", {

                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({

                        email: email,

                        password: password

                    })

                });


            const d =
                await response.json();


            if (!response.ok) {

                message.textContent =
                    d.message || "Login failed.";

                return;
            }


            /*
             * ROLE BASED REDIRECTION
             */

            if (d.role === "ADMIN") {

                window.location.href =
                    "admin.jsp";

            }

            else if (d.role === "BROKER") {

                window.location.href =
                    "broker.jsp";

            }

            else if (d.role === "OWNER") {

                window.location.href =
                    "owner.jsp";

            }

            else {

                window.location.href =
                    "user.jsp";

            }

        }

        catch (error) {

            console.error(
                "Login error:",
                error
            );


            message.textContent =
                "Unable to connect to server.";

        }

    });

</script>

</body>

</html>