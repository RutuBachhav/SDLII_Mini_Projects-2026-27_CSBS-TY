
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FlatDesk | Owner Dashboard</title>

    <style>
        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f7fb;
            color: #172033;
        }

        .navbar {
            background: #152b4a;
            color: white;
            padding: 18px 5%;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 15px;
            flex-wrap: wrap;
        }

        .brand { font-size: 24px; font-weight: bold; }
        .brand span { color: #58d5b0; }

        .nav-right {
            display: flex;
            align-items: center;
            gap: 15px;
        }

        button { font-family: inherit; cursor: pointer; }

        .logout {
            background: #e85d5d;
            color: white;
            border: none;
            padding: 10px 15px;
            border-radius: 7px;
        }

        .container {
            max-width: 1200px;
            margin: 30px auto;
            padding: 0 20px;
        }

        .welcome { margin-bottom: 25px; }
        .welcome h1 { margin-bottom: 8px; }
        .welcome p { color: #667085; }

        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 18px;
            margin-bottom: 28px;
        }

        .stat-card, .panel {
            background: white;
            border-radius: 12px;
            padding: 22px;
            box-shadow: 0 3px 15px rgba(0,0,0,.05);
        }

        .stat-card p { color: #667085; margin: 0 0 10px; }
        .stat-card h2 { margin: 0; font-size: 28px; }
        .panel { margin-bottom: 25px; }
        .panel h2 { margin-top: 0; margin-bottom: 20px; }

        .form-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 16px;
        }

        .field { display: flex; flex-direction: column; gap: 7px; }
        .field.full { grid-column: 1 / -1; }

        label { font-size: 14px; font-weight: bold; }

        input, select, textarea {
            width: 100%;
            padding: 12px;
            border: 1px solid #d5dce7;
            border-radius: 7px;
            font-size: 14px;
            background: white;
        }

        textarea { resize: vertical; min-height: 85px; }

        .primary-btn {
            background: #176b57;
            color: white;
            border: none;
            padding: 12px 20px;
            border-radius: 7px;
            font-weight: bold;
            margin-top: 18px;
        }

        .secondary-btn {
            background: #667085;
            color: white;
            border: none;
            padding: 12px 20px;
            border-radius: 7px;
            margin-top: 18px;
        }

        .delete-btn {
            background: #dc3545;
            color: white;
            border: none;
            padding: 9px 12px;
            border-radius: 6px;
        }

        .edit-btn {
            background: #176b57;
            color: white;
            border: none;
            padding: 9px 12px;
            border-radius: 6px;
        }

        button:disabled { opacity: .6; cursor: not-allowed; }

        .message {
            margin-top: 15px;
            font-size: 14px;
            overflow-wrap: anywhere;
        }

        .property-list {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
            gap: 18px;
        }

        .property-card {
            border: 1px solid #e3e8f0;
            border-radius: 10px;
            padding: 18px;
            background: #fff;
        }

        .property-card h3 { margin: 0 0 10px; }

        .property-card p {
            color: #586579;
            font-size: 14px;
            line-height: 1.6;
            overflow-wrap: anywhere;
        }

        .contact-link {
            color: #176b57;
            font-weight: bold;
            text-decoration: none;
        }

        .contact-link:hover { text-decoration: underline; }

        .status {
            display: inline-block;
            padding: 5px 9px;
            border-radius: 20px;
            background: #dcfce7;
            color: #166534;
            font-size: 12px;
            font-weight: bold;
        }

        .status-actions {
            margin-top: 15px;
            padding: 12px;
            background: #f4f7fb;
            border-radius: 8px;
        }

        .status-actions label {
            display: block;
            margin-bottom: 7px;
        }

        .status-controls {
            display: flex;
            gap: 8px;
            align-items: center;
        }

        .status-controls select {
            min-width: 0;
            flex: 1;
            padding: 9px;
        }

        .save-status-btn {
            background: #2459a9;
            color: white;
            border: none;
            border-radius: 6px;
            padding: 10px 12px;
            white-space: nowrap;
        }

        .card-actions {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
            margin-top: 15px;
        }

        .empty { color: #667085; padding: 12px 0; }
        .hidden { display: none !important; }
        #editPanel { scroll-margin-top: 20px; }

        @media (max-width: 600px) {
            .form-grid { grid-template-columns: 1fr; }
            .field.full { grid-column: auto; }
            .container { padding: 0 12px; }
            .status-controls {
                flex-direction: column;
                align-items: stretch;
            }
        }
    </style>
</head>

<body>
<header class="navbar">
    <div class="brand">Flat<span>Desk</span></div>
    <div class="nav-right">
        <span id="welcomeName">Owner</span>
        <button class="logout" id="logoutBtn" type="button">Logout</button>
    </div>
</header>

<main class="container">
    <section class="welcome">
        <h1>Owner Dashboard</h1>
        <p>Manage your properties and keep track of your listings.</p>
    </section>

    <section class="stats">
        <div class="stat-card">
            <p>My Properties</p>
            <h2 id="totalProperties">0</h2>
        </div>
        <div class="stat-card">
            <p>Available</p>
            <h2 id="availableProperties">0</h2>
        </div>
        <div class="stat-card">
            <p>Rented</p>
            <h2 id="rentedProperties">0</h2>
        </div>
        <div class="stat-card">
            <p>Sold</p>
            <h2 id="soldProperties">0</h2>
        </div>
    </section>

    <section class="panel" id="addPanel">
        <h2 id="formHeading">Add New Property</h2>

        <form id="propertyForm">
            <div class="form-grid">
                <div class="field">
                    <label for="title">Property Title *</label>
                    <input id="title" name="title" required maxlength="180"
                           placeholder="e.g. Neelmohar Residency">
                </div>

                <div class="field">
                    <label for="country">Country</label>
                    <input id="country" name="country" value="India">
                </div>

                <div class="field">
                    <label for="city">City *</label>
                    <input id="city" name="city" required placeholder="Pune">
                </div>

                <div class="field">
                    <label for="address">Address</label>
                    <input id="address" name="address" placeholder="Area and street">
                </div>

                <div class="field">
                    <label for="type">Property Type *</label>
                    <select id="type" name="type" required>
                        <option value="">Select property type</option>
                        <option value="Apartment">Apartment</option>
                        <option value="Flat">Flat</option>
                        <option value="House">House</option>
                        <option value="Villa">Villa</option>
                        <option value="Plot">Plot</option>
                        <option value="Commercial">Commercial</option>
                        <option value="Office">Office</option>
                        <option value="Shop">Shop</option>
                    </select>
                </div>

                <div class="field">
                    <label for="bhk">BHK</label>
                    <select id="bhk" name="bhk">
                        <option value="">Not applicable</option>
                        <option value="1 BHK">1 BHK</option>
                        <option value="2 BHK">2 BHK</option>
                        <option value="3 BHK">3 BHK</option>
                        <option value="4 BHK">4 BHK</option>
                        <option value="5+ BHK">5+ BHK</option>
                    </select>
                </div>

                <div class="field">
                    <label for="rent">Monthly Rent (₹)</label>
                    <input id="rent" name="rent" type="number"
                           min="0" step="0.01" value="0">
                </div>

                <div class="field">
                    <label for="price">Property Price (₹)</label>
                    <input id="price" name="price" type="number"
                           min="0" step="0.01" value="0">
                </div>

                <div class="field">
                    <label for="area">Area (sq. ft.)</label>
                    <input id="area" name="area" type="number"
                           min="0" step="0.01" value="0">
                </div>

                <div class="field">
                    <label for="owner">Owner Name</label>
                    <input id="owner" name="owner" placeholder="Property owner's name">
                </div>

                <!-- NEW: Property contact number -->
                <div class="field">
                    <label for="contactNumber">Contact Number *</label>
                    <input id="contactNumber" name="contactNumber"
                           type="tel" required
                           pattern="[0-9+() -]{7,20}"
                           title="Enter a valid contact number"
                           placeholder="e.g. +91 9876543210">
                </div>

                <div class="field full">
                    <label for="notes">Description / Notes</label>
                    <textarea id="notes" name="notes"
                              placeholder="Enter property details"></textarea>
                </div>
            </div>

            <button class="primary-btn" id="submitBtn" type="submit">
                Add Property
            </button>

            <button class="secondary-btn hidden" id="cancelEditBtn"
                    type="button">Cancel Edit</button>

            <div class="message" id="formMessage" role="status"></div>
        </form>
    </section>

    <section class="panel" id="editPanel">
        <h2>Edit Property</h2>
        <p>Choose Edit on a property card to load its details into the form above.</p>
        <p class="empty">The form will switch to edit mode when you select a property.</p>
    </section>

    <section class="panel">
        <h2>My Properties</h2>
        <div id="propertyList" class="property-list">
            <p class="empty">Loading your properties...</p>
        </div>
        <div class="message" id="listMessage" role="status"></div>
    </section>
</main>

<script>
    const propertyForm = document.getElementById("propertyForm");
    const formMessage = document.getElementById("formMessage");
    const listMessage = document.getElementById("listMessage");
    const propertyList = document.getElementById("propertyList");
    const submitBtn = document.getElementById("submitBtn");
    const cancelEditBtn = document.getElementById("cancelEditBtn");

    let currentProperties = [];
    let editingPropertyId = null;

    function showMoney(value) {
        return Number(value || 0).toLocaleString("en-IN", {
            maximumFractionDigits: 2
        });
    }

    function addTextElement(parent, tag, text, className) {
        const element = document.createElement(tag);
        element.textContent = text == null ? "" : String(text);
        if (className) element.className = className;
        parent.appendChild(element);
        return element;
    }

    function showMessage(element, message, success) {
        element.textContent = message;
        element.style.color = success ? "#166534" : "#b42318";
    }

    async function apiRequest(url, options = {}) {
        const response = await fetch(url, {
            credentials: "same-origin",
            cache: "no-store",
            ...options,
            headers: { ...(options.headers || {}) }
        });

        const result = await response.json().catch(() => ({}));

        if (response.status === 401) {
            window.location.href = "login.jsp";
            throw new Error("Your session has expired. Please log in again.");
        }

        if (!response.ok) {
            throw new Error(
                result.error || result.message ||
                "Request failed (HTTP " + response.status + ")."
            );
        }

        return result;
    }

    async function checkOwnerSession() {
        const user = await apiRequest("api/auth");

        if (!user || user.role !== "OWNER") {
            window.location.href = "login.jsp";
            return false;
        }

        document.getElementById("welcomeName").textContent =
            "Welcome, " + (user.name || "Owner");

        return true;
    }

    async function updatePropertyStatus(property, select, button) {
        const newStatus = select.value;
        const oldText = button.textContent;

        button.disabled = true;
        select.disabled = true;
        button.textContent = "Saving...";
        showMessage(listMessage, "", true);

        try {
            await apiRequest(
                "api/properties?id=" + encodeURIComponent(property.id),
                {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ status: newStatus })
                }
            );

            showMessage(
                listMessage,
                'Status for "' + (property.title || "property") +
                '" updated to ' + newStatus + ".",
                true
            );

            await loadProperties();
        } catch (error) {
            showMessage(listMessage, error.message, false);
            button.disabled = false;
            select.disabled = false;
            button.textContent = oldText;
        }
    }

    async function loadProperties() {
        propertyList.replaceChildren();
        addTextElement(propertyList, "p", "Loading your properties...", "empty");

        try {
            const data = await apiRequest("api/properties");

            if (!Array.isArray(data)) {
                throw new Error("Unexpected response from the server.");
            }

            currentProperties = data;

            document.getElementById("totalProperties").textContent = data.length;
            document.getElementById("availableProperties").textContent =
                data.filter(p => p.status === "Available").length;
            document.getElementById("rentedProperties").textContent =
                data.filter(p => p.status === "Rented").length;
            document.getElementById("soldProperties").textContent =
                data.filter(p => p.status === "Sold").length;

            propertyList.replaceChildren();

            if (data.length === 0) {
                addTextElement(
                    propertyList, "p",
                    "You have not added any properties yet.", "empty"
                );
                return;
            }

            data.forEach(property => {
                const card = document.createElement("article");
                card.className = "property-card";

                addTextElement(card, "h3", property.title || "Untitled Property");

                addTextElement(
                    card, "p",
                    (property.city || "City not specified") +
                    (property.country ? ", " + property.country : "")
                );

                if (property.address) {
                    addTextElement(card, "p", "Address: " + property.address);
                }

                addTextElement(
                    card, "p", "Type: " + (property.type || "Not specified")
                );

                if (property.bhk) {
                    addTextElement(card, "p", "Configuration: " + property.bhk);
                }

                addTextElement(card, "p", "Monthly rent: ₹" + showMoney(property.rent));
                addTextElement(card, "p", "Price: ₹" + showMoney(property.price));
                addTextElement(
                    card, "p", "Area: " + showMoney(property.area) + " sq. ft."
                );
                addTextElement(
                    card, "p", "Owner: " + (property.owner || "Not specified")
                );

                // NEW: Display the property's saved contact number.
                const contactParagraph = document.createElement("p");
                contactParagraph.appendChild(
                    document.createTextNode("Contact: ")
                );

                const phone = String(property.contactNumber || "").trim();

                if (phone) {
                    const phoneLink = document.createElement("a");
                    phoneLink.className = "contact-link";
                    phoneLink.href = "tel:" + phone.replace(/[^0-9+]/g, "");
                    phoneLink.textContent = phone;
                    contactParagraph.appendChild(phoneLink);
                } else {
                    contactParagraph.appendChild(
                        document.createTextNode("Not available")
                    );
                }

                card.appendChild(contactParagraph);

                addTextElement(
                    card, "span", property.status || "Available", "status"
                );

                if (property.notes) {
                    addTextElement(card, "p", "Details: " + property.notes);
                }

                // Property status selector
                const statusActions = document.createElement("div");
                statusActions.className = "status-actions";

                const statusLabel = document.createElement("label");
                statusLabel.textContent = "Change Property Status";
                statusActions.appendChild(statusLabel);

                const statusControls = document.createElement("div");
                statusControls.className = "status-controls";

                const statusSelect = document.createElement("select");
                statusSelect.setAttribute(
                    "aria-label", "Status for " + (property.title || "property")
                );

                ["Available", "Rented", "Sold"].forEach(status => {
                    const option = document.createElement("option");
                    option.value = status;
                    option.textContent = status;
                    statusSelect.appendChild(option);
                });

                statusSelect.value =
                    ["Available", "Rented", "Sold"].includes(property.status)
                    ? property.status : "Available";

                const saveStatusButton = document.createElement("button");
                saveStatusButton.type = "button";
                saveStatusButton.className = "save-status-btn";
                saveStatusButton.textContent = "Save Status";

                saveStatusButton.addEventListener("click", () => {
                    updatePropertyStatus(property, statusSelect, saveStatusButton);
                });

                statusControls.append(statusSelect, saveStatusButton);
                statusActions.appendChild(statusControls);
                card.appendChild(statusActions);

                const actions = document.createElement("div");
                actions.className = "card-actions";

                const editButton = document.createElement("button");
                editButton.type = "button";
                editButton.className = "edit-btn";
                editButton.textContent = "Edit";
                editButton.addEventListener("click", () => startEdit(property));

                const deleteButton = document.createElement("button");
                deleteButton.type = "button";
                deleteButton.className = "delete-btn";
                deleteButton.textContent = "Delete";
                deleteButton.addEventListener("click", () => deleteProperty(property));

                actions.append(editButton, deleteButton);
                card.appendChild(actions);
                propertyList.appendChild(card);
            });

            showMessage(listMessage, "", true);
        } catch (error) {
            propertyList.replaceChildren();
            addTextElement(propertyList, "p", error.message, "empty");
        }
    }

    function readForm() {
        const data = new FormData(propertyForm);

        return {
            title: String(data.get("title") || "").trim(),
            country: String(data.get("country") || "India").trim() || "India",
            city: String(data.get("city") || "").trim(),
            address: String(data.get("address") || "").trim(),
            type: String(data.get("type") || "").trim(),
            bhk: String(data.get("bhk") || "").trim(),
            rent: Number(data.get("rent") || 0),
            price: Number(data.get("price") || 0),
            area: Number(data.get("area") || 0),
            owner: String(data.get("owner") || "").trim(),

            // NEW: Send the contact number to the backend.
            contactNumber: String(data.get("contactNumber") || "").trim(),

            notes: String(data.get("notes") || "").trim()
        };
    }

    function startEdit(property) {
        editingPropertyId = property.id;

        for (const field of propertyForm.elements) {
            if (!field.name) continue;
            const value = property[field.name];
            field.value = value == null ? "" : value;
        }

        document.getElementById("formHeading").textContent = "Edit Property";
        submitBtn.textContent = "Save Changes";
        cancelEditBtn.classList.remove("hidden");
        showMessage(formMessage, "Editing property ID " + property.id, true);

        document.getElementById("addPanel").scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    }

    function resetForm() {
        editingPropertyId = null;
        propertyForm.reset();
        document.getElementById("country").value = "India";
        document.getElementById("rent").value = "0";
        document.getElementById("price").value = "0";
        document.getElementById("area").value = "0";
        document.getElementById("formHeading").textContent = "Add New Property";
        submitBtn.textContent = "Add Property";
        cancelEditBtn.classList.add("hidden");
        showMessage(formMessage, "", true);
    }

    cancelEditBtn.addEventListener("click", resetForm);

    propertyForm.addEventListener("submit", async function(event) {
        event.preventDefault();

        const isEditing = editingPropertyId !== null;
        const idBeingEdited = editingPropertyId;

        submitBtn.disabled = true;
        cancelEditBtn.disabled = true;
        submitBtn.textContent = isEditing ? "Saving..." : "Adding...";

        try {
            if (isEditing) {
                await apiRequest(
                    "api/properties?id=" + encodeURIComponent(idBeingEdited),
                    {
                        method: "PUT",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify(readForm())
                    }
                );

                showMessage(formMessage, "Property updated successfully!", true);
            } else {
                await apiRequest("api/properties", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(readForm())
                });

                showMessage(formMessage, "Property added successfully!", true);
            }

            resetForm();
            await loadProperties();
        } catch (error) {
            showMessage(formMessage, error.message, false);
        } finally {
            submitBtn.disabled = false;
            cancelEditBtn.disabled = false;
            submitBtn.textContent =
                editingPropertyId !== null ? "Save Changes" : "Add Property";
        }
    });

    async function deleteProperty(property) {
        const confirmed = window.confirm(
            'Delete "' + property.title + '" (ID ' + property.id +
            ')? This cannot be undone.'
        );

        if (!confirmed) return;

        try {
            await apiRequest(
                "api/properties?id=" + encodeURIComponent(property.id),
                { method: "DELETE" }
            );

            if (String(editingPropertyId) === String(property.id)) {
                resetForm();
            }

            showMessage(listMessage, "Property deleted successfully.", true);
            await loadProperties();
        } catch (error) {
            showMessage(listMessage, error.message, false);
        }
    }

    document.getElementById("logoutBtn").addEventListener("click", async function() {
        try {
            await fetch("api/auth", {
                method: "DELETE",
                credentials: "same-origin"
            });
        } finally {
            window.location.href = "login.jsp";
        }
    });

    async function initializeOwnerDashboard() {
        try {
            if (await checkOwnerSession()) {
                await loadProperties();
            }
        } catch (error) {
            propertyList.replaceChildren();
            addTextElement(propertyList, "p", error.message, "empty");
        }
    }

    initializeOwnerDashboard();
</script>
</body>
</html>
