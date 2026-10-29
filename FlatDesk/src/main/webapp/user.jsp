```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>FlatDesk User Dashboard</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="assets/style.css">

    <style>
        .dash {
            max-width: 1200px;
            margin: 30px auto;
            padding: 0 20px;
        }

        .panel {
            padding: 24px;
            background: #fff;
            border-radius: 12px;
        }

        .property-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-top: 22px;
        }

        .property {
            overflow: hidden;
            padding: 18px;
            background: #fff;
            border: 1px solid #e8ded2;
            border-radius: 12px;
        }

        .photo {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 110px;
            margin-bottom: 15px;
            background: #f8e7d6;
            border-radius: 8px;
            font-size: 42px;
        }

        .property h3 {
            margin-bottom: 10px;
            color: #152b4a;
            overflow-wrap: anywhere;
        }

        .property p {
            margin: 8px 0;
            color: #667085;
            overflow-wrap: anywhere;
        }

        .price {
            display: block;
            margin: 12px 0;
            color: #a85f2a;
            font-size: 20px;
        }

        .property-status {
            display: inline-block;
            margin-top: 8px;
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: bold;
        }

        .available {
            color: #166534;
            background: #dcfce7;
        }

        .rented {
            color: #854d0e;
            background: #fef3c7;
        }

        .sold {
            color: #991b1b;
            background: #fee2e2;
        }

        .refresh-btn {
            padding: 10px 15px;
            color: white;
            background: #c9783a;
            border: none;
            border-radius: 7px;
            cursor: pointer;
        }

        .refresh-btn:disabled {
            opacity: .6;
            cursor: not-allowed;
        }

        .panel-heading {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;
        }

        .filters {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 12px;
            margin-top: 20px;
        }

        .filters input,
        .filters select {
            width: 100%;
            padding: 11px 12px;
            border: 1px solid #e0d5c9;
            border-radius: 7px;
            background: white;
            font: inherit;
        }

        .message {
            margin-top: 15px;
            color: #667085;
            overflow-wrap: anywhere;
        }

        .results-count {
            margin-top: 18px;
            color: #667085;
        }

        /* Contact details for property owner or broker */
        .contact-number {
            margin-top: 12px;
            color: #344054;
            overflow-wrap: anywhere;
        }

        .call-link {
            display: inline-block;
            margin-top: 8px;
            padding: 9px 14px;
            color: white;
            background: #166534;
            border-radius: 7px;
            text-decoration: none;
        }

        .call-link:hover {
            background: #14532d;
        }
    </style>
</head>

<body>

<nav class="top navbar">
    <a href="index.jsp" class="logo">
        Flat<span>Desk</span>
    </a>

    <div class="nav-right">
        <span id="welcome">Loading...</span>
        <button type="button" class="logout" id="logoutBtn">
            Logout
        </button>
    </div>
</nav>

<main class="dash">
    <section class="panel">

        <div class="panel-heading">
            <h2>Property Listings</h2>
            <button type="button" class="refresh-btn" id="refreshBtn">
                Refresh Listings
            </button>
        </div>

        <p>Search properties and check their latest availability status.</p>

        <div class="filters">
            <input
                type="search"
                id="searchInput"
                placeholder="Search by title, city or type..."
                aria-label="Search properties"
            >

            <select id="statusFilter" aria-label="Filter by property status">
                <option value="ALL">All Statuses</option>
                <option value="Available">Available</option>
                <option value="Rented">Rented</option>
                <option value="Sold">Sold</option>
            </select>
        </div>

        <p class="results-count" id="resultsCount">Loading properties...</p>

        <div id="cards" class="property-grid">
            <p>Loading properties...</p>
        </div>

        <div id="message" class="message" role="status"></div>
    </section>
</main>

<script>
    const cards = document.getElementById("cards");
    const message = document.getElementById("message");
    const refreshBtn = document.getElementById("refreshBtn");
    const searchInput = document.getElementById("searchInput");
    const statusFilter = document.getElementById("statusFilter");
    const resultsCount = document.getElementById("resultsCount");

    let allProperties = [];

    async function apiRequest(url, options = {}) {
        const response = await fetch(url, {
            credentials: "same-origin",
            cache: "no-store",
            ...options
        });

        const result = await response.json().catch(() => ({}));

        if (response.status === 401) {
            window.location.href = "login.jsp";
            throw new Error("Session expired. Please log in again.");
        }

        if (!response.ok) {
            throw new Error(
                result.error || result.message || "Request failed."
            );
        }

        return result;
    }

    function addText(parent, tag, text, className) {
        const element = document.createElement(tag);
        element.textContent = text == null ? "" : String(text);

        if (className) {
            element.className = className;
        }

        parent.appendChild(element);
        return element;
    }

    function formatMoney(value) {
        return Number(value || 0).toLocaleString("en-IN", {
            maximumFractionDigits: 2
        });
    }

    function createPropertyCard(property) {
        const article = document.createElement("article");
        article.className = "property";

        addText(article, "div", "🏡", "photo");
        addText(article, "h3", property.title || "Property");

        const details = [
            property.bhk,
            property.type,
            property.city
        ].filter(value =>
            value != null && String(value).trim() !== ""
        );

        if (details.length) {
            addText(article, "p", details.join(" • "));
        }

        if (Number(property.price || 0) > 0) {
            addText(
                article,
                "strong",
                "₹" + formatMoney(property.price),
                "price"
            );
        } else if (Number(property.rent || 0) > 0) {
            addText(
                article,
                "strong",
                "₹" + formatMoney(property.rent) + "/month",
                "price"
            );
        }

        if (property.area) {
            addText(article, "p", "Area: " + property.area + " sq.ft.");
        }

        if (property.address) {
            addText(article, "p", property.address);
        }

        // NEW: Display the associated owner's or broker's contact number.
        const phone = String(property.contactNumber || "").trim();

        if (phone) {
            addText(article, "p", "Contact: " + phone, "contact-number");

            const callLink = document.createElement("a");
            callLink.className = "call-link";
            callLink.href = "tel:" + phone.replace(/[^\d+]/g, "");
            callLink.textContent = "📞 Call Owner / Broker";
            article.appendChild(callLink);
        } else {
            addText(
                article,
                "p",
                "Contact number not available.",
                "contact-number"
            );
        }

        const status = property.status || "Unknown";
        let statusClass = "";

        if (status === "Available") statusClass = "available";
        if (status === "Rented") statusClass = "rented";
        if (status === "Sold") statusClass = "sold";

        addText(
            article,
            "span",
            "Status: " + status,
            "property-status " + statusClass
        );

        return article;
    }

    function renderProperties() {
        const searchTerm = searchInput.value.trim().toLowerCase();
        const selectedStatus = statusFilter.value;

        const filtered = allProperties.filter(property => {
            const searchableText = [
                property.title,
                property.city,
                property.type,
                property.bhk,
                property.address
            ].filter(Boolean).join(" ").toLowerCase();

            const matchesSearch = searchableText.includes(searchTerm);

            const matchesStatus =
                selectedStatus === "ALL" ||
                (property.status || "Unknown") === selectedStatus;

            return matchesSearch && matchesStatus;
        });

        cards.replaceChildren();

        resultsCount.textContent =
            "Showing " + filtered.length + " of " +
            allProperties.length + " properties";

        if (filtered.length === 0) {
            addText(cards, "p", "No properties match your search.");
            return;
        }

        filtered.forEach(property => {
            cards.appendChild(createPropertyCard(property));
        });
    }

    async function loadProperties() {
        refreshBtn.disabled = true;
        message.textContent = "";

        try {
            const user = await apiRequest("api/auth");

            if (!user || user.role !== "USER") {
                window.location.href = "login.jsp";
                return;
            }

            document.getElementById("welcome").textContent =
                "Welcome, " + (user.name || "User");

            const properties = await apiRequest("api/properties");

            if (!Array.isArray(properties)) {
                throw new Error("The server returned an unexpected response.");
            }

            allProperties = properties;
            renderProperties();

            showMessage(
                "Loaded " + properties.length + " properties successfully.",
                true
            );

        } catch (error) {
            cards.replaceChildren();
            resultsCount.textContent = "Could not load properties.";
            showMessage(error.message, false);
            console.error("Property loading error:", error);
        } finally {
            refreshBtn.disabled = false;
        }
    }

    function showMessage(text, success) {
        message.textContent = text;
        message.style.color = success ? "#166534" : "#b42318";
    }

    searchInput.addEventListener("input", renderProperties);
    statusFilter.addEventListener("change", renderProperties);
    refreshBtn.addEventListener("click", loadProperties);

    document.getElementById("logoutBtn").addEventListener(
        "click",
        async function() {
            try {
                await fetch("api/auth", {
                    method: "DELETE",
                    credentials: "same-origin"
                });
            } finally {
                window.location.href = "login.jsp";
            }
        }
    );

    loadProperties();
</script>

</body>
</html>
```