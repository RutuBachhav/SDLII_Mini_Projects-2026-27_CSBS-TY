```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard | FlatDesk</title>
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <link rel="stylesheet" href="assets/style.css">

    <style>
        .hidden { display: none !important; }

        .message {
            margin: 12px 0;
            overflow-wrap: anywhere;
        }

        .tablewrap table { min-width: 1050px; }

        .tablewrap td, .tablewrap th {
            padding: 12px;
            text-align: left;
            vertical-align: middle;
        }

        .admin-actions {
            display: flex;
            flex-direction: column;
            gap: 8px;
            min-width: 125px;
        }

        .admin-actions button {
            padding: 8px 10px;
            border: none;
            border-radius: 5px;
            color: white;
            cursor: pointer;
        }

        .edit-btn, .status-btn { background: #176b57; }
        .delete-btn { background: #dc3545; }

        .status-select {
            padding: 8px;
            width: 100%;
            border: 1px solid #d0d5dd;
            border-radius: 5px;
            background: white;
        }

        .admin-actions button:disabled {
            opacity: .6;
            cursor: not-allowed;
        }

        .form-actions {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }

        #editPanel { scroll-margin-top: 20px; }
    </style>
</head>

<body>

<nav class="top">
    <div class="logo">Flat<span>Desk</span></div>
    <div>
        <span id="welcome">Admin Dashboard</span>
        <button class="btn" type="button" onclick="logout()">Logout</button>
    </div>
</nav>

<main class="dash">

    <div class="stats">
        <div>Total Properties <b id="count">0</b></div>
        <div>Role <b>ADMIN</b></div>
        <div>Database <b>MySQL</b></div>
    </div>

    <!-- ADD PROPERTY -->
    <section class="panel" id="addPanel">
        <h2>Add Property</h2>
        <p>Add a new property listing to the FlatDesk database.</p>

        <form id="addForm" class="property-form">
            <input name="title" placeholder="Property title"
                   required maxlength="180">

            <input name="country" placeholder="Country" value="India">
            <input name="city" placeholder="City" required>
            <input name="address" placeholder="Address">

            <select name="type" required>
                <option value="Apartment">Apartment</option>
                <option value="Flat">Flat</option>
                <option value="House">House</option>
                <option value="Villa">Villa</option>
                <option value="Plot">Plot</option>
                <option value="Office">Office</option>
                <option value="Commercial">Commercial</option>
                <option value="Shop">Shop</option>
            </select>

            <input name="bhk" placeholder="e.g. 2 BHK">
            <input name="price" type="number" min="0" step="0.01"
                   placeholder="Sale price">
            <input name="rent" type="number" min="0" step="0.01"
                   placeholder="Monthly rent">
            <input name="area" type="number" min="0" step="0.01"
                   placeholder="Area sq.ft.">
            <input name="owner" placeholder="Owner name">

            <!-- NEW: REQUIRED PROPERTY CONTACT NUMBER -->
            <input name="contactNumber"
                   type="tel"
                   placeholder="Contact number"
                   pattern="[0-9+() -]{7,20}"
                   title="Enter a valid contact number"
                   required>

            <textarea name="notes"
                      placeholder="Property description / notes"></textarea>

            <div class="form-actions">
                <button class="btn primary" id="addPropertyBtn" type="submit">
                    Add Property
                </button>
                <button class="btn" type="reset">Clear</button>
            </div>
        </form>

        <div class="message" id="addMessage" role="status"></div>
    </section>

    <!-- PROPERTY LIST -->
    <section class="panel">
        <h2>Admin Dashboard</h2>
        <p>View and manage property listings stored in the FlatDesk database.</p>
        <div class="message" id="listMessage" role="status"></div>

        <div class="tablewrap">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Title</th>
                        <th>City</th>
                        <th>Type</th>
                        <th>Price / Rent</th>
                        <th>Status</th>
                        <th>Manage</th>
                    </tr>
                </thead>
                <tbody id="table">
                    <tr><td colspan="7">Loading properties...</td></tr>
                </tbody>
            </table>
        </div>
    </section>

    <!-- EDIT PROPERTY -->
    <section class="panel hidden" id="editPanel">
        <h2>Edit Property</h2>
        <p id="editingLabel">Update the selected property.</p>

        <form id="editForm" class="property-form">
            <input name="title" placeholder="Property title"
                   required maxlength="180">
            <input name="country" placeholder="Country">
            <input name="city" placeholder="City" required>
            <input name="address" placeholder="Address">

            <select name="type" required>
                <option value="Apartment">Apartment</option>
                <option value="Flat">Flat</option>
                <option value="House">House</option>
                <option value="Villa">Villa</option>
                <option value="Plot">Plot</option>
                <option value="Office">Office</option>
                <option value="Commercial">Commercial</option>
                <option value="Shop">Shop</option>
            </select>

            <input name="bhk" placeholder="e.g. 2 BHK">
            <input name="price" type="number" min="0" step="0.01"
                   placeholder="Sale price">
            <input name="rent" type="number" min="0" step="0.01"
                   placeholder="Monthly rent">
            <input name="area" type="number" min="0" step="0.01"
                   placeholder="Area sq.ft.">
            <input name="owner" placeholder="Owner name">

            <!-- NEW: EDIT PROPERTY CONTACT NUMBER -->
            <input name="contactNumber"
                   type="tel"
                   placeholder="Contact number"
                   pattern="[0-9+() -]{7,20}"
                   title="Enter a valid contact number"
                   required>

            <textarea name="notes"
                      placeholder="Property description / notes"></textarea>

            <div class="form-actions">
                <button class="btn primary" id="saveEditBtn" type="submit">
                    Save Changes
                </button>
                <button class="btn" type="button" onclick="cancelEdit()">
                    Cancel
                </button>
            </div>
        </form>

        <div class="message" id="editMessage" role="status"></div>
    </section>

</main>

<script>
    const table = document.getElementById("table");
    const editPanel = document.getElementById("editPanel");
    const editForm = document.getElementById("editForm");
    const addForm = document.getElementById("addForm");

    let properties = [];
    let editingId = null;

    function showMessage(id, message, success) {
        const element = document.getElementById(id);
        element.textContent = message;
        element.style.color = success ? "#166534" : "#b42318";
    }

    function money(value) {
        return Number(value || 0).toLocaleString("en-IN", {
            maximumFractionDigits: 2
        });
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
            throw new Error("Session expired. Please log in again.");
        }

        if (!response.ok) {
            throw new Error(
                result.error ||
                result.message ||
                "Request failed (HTTP " + response.status + ")."
            );
        }

        return result;
    }

    async function checkAdminSession() {
        const user = await apiRequest("api/auth");

        if (!user || user.role !== "ADMIN") {
            window.location.href = "login.jsp";
            return false;
        }

        document.getElementById("welcome").textContent =
            "Welcome, " + (user.name || "Admin");

        return true;
    }

    function makeCell(row, value) {
        const cell = document.createElement("td");
        cell.textContent = value == null ? "" : String(value);
        row.appendChild(cell);
        return cell;
    }

    function renderProperties() {
        table.replaceChildren();
        document.getElementById("count").textContent = properties.length;

        if (properties.length === 0) {
            const row = document.createElement("tr");
            const cell = makeCell(row, "No properties found.");
            cell.colSpan = 7;
            table.appendChild(row);
            return;
        }

        properties.forEach(property => {
            const row = document.createElement("tr");

            makeCell(row, property.id);
            makeCell(row, property.title);
            makeCell(row, property.city);
            makeCell(row, property.type);

            makeCell(
                row,
                "Price: ₹" + money(property.price) +
                " | Rent: ₹" + money(property.rent)
            );

            // Existing status dropdown
            const statusCell = makeCell(row, "");
            const actions = document.createElement("div");
            actions.className = "admin-actions";

            const statusSelect = document.createElement("select");
            statusSelect.className = "status-select";
            statusSelect.setAttribute("aria-label", "Property status");

            ["Available", "Rented", "Sold"].forEach(status => {
                const option = document.createElement("option");
                option.value = status;
                option.textContent = status;
                statusSelect.appendChild(option);
            });

            const currentStatus = property.status || "Available";
            statusSelect.value = currentStatus;

            if (statusSelect.value !== currentStatus) {
                const option = document.createElement("option");
                option.value = currentStatus;
                option.textContent = currentStatus;
                statusSelect.appendChild(option);
                statusSelect.value = currentStatus;
            }

            const statusButton = document.createElement("button");
            statusButton.type = "button";
            statusButton.className = "status-btn";
            statusButton.textContent = "Save Status";
            statusButton.addEventListener("click", () =>
                updateStatus(property, statusSelect, statusButton)
            );

            actions.append(statusSelect, statusButton);
            statusCell.appendChild(actions);

            // Existing Edit and Delete buttons
            const manageCell = makeCell(row, "");
            const manage = document.createElement("div");
            manage.className = "admin-actions";

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

            manage.append(editButton, deleteButton);
            manageCell.appendChild(manage);

            table.appendChild(row);
        });
    }

    async function loadProperties() {
        try {
            const data = await apiRequest("api/properties");

            if (!Array.isArray(data)) {
                throw new Error("Unexpected response from the properties API.");
            }

            properties = data;
            renderProperties();
            showMessage("listMessage", "", true);
        } catch (error) {
            showMessage("listMessage", error.message, false);
        }
    }

    // Existing status update function
    async function updateStatus(property, select, button) {
        const newStatus = select.value;
        const oldStatus = property.status || "Available";

        if (newStatus === oldStatus) {
            showMessage("listMessage", "The status has not changed.", true);
            return;
        }

        button.disabled = true;
        button.textContent = "Saving...";

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
                "listMessage",
                "Property #" + property.id + " status updated to " + newStatus + ".",
                true
            );

            await loadProperties();
        } catch (error) {
            showMessage("listMessage", error.message, false);
            select.value = oldStatus;
            button.disabled = false;
            button.textContent = "Save Status";
        }
    }

    // Existing Edit function
    function startEdit(property) {
        editingId = property.id;

        for (const field of editForm.elements) {
            if (!field.name) continue;

            const value = property[field.name];
            field.value = value == null ? "" : value;
        }

        document.getElementById("editingLabel").textContent =
            "Editing property #" + property.id + ": " + property.title;

        editPanel.classList.remove("hidden");
        showMessage("editMessage", "", true);
        editPanel.scrollIntoView({ behavior: "smooth", block: "start" });
    }

    function cancelEdit() {
        editingId = null;
        editForm.reset();
        editPanel.classList.add("hidden");
        showMessage("editMessage", "", true);
    }

    function readEditForm() {
        const data = new FormData(editForm);

        return {
            title: String(data.get("title") || "").trim(),
            country: String(data.get("country") || "India").trim() || "India",
            city: String(data.get("city") || "").trim(),
            address: String(data.get("address") || "").trim(),
            type: String(data.get("type") || "").trim(),
            bhk: String(data.get("bhk") || "").trim(),
            price: Number(data.get("price") || 0),
            rent: Number(data.get("rent") || 0),
            area: Number(data.get("area") || 0),
            owner: String(data.get("owner") || "").trim(),
            contactNumber: String(data.get("contactNumber") || "").trim(),
            notes: String(data.get("notes") || "").trim()
        };
    }

    // Read Add Property form, including contact number
    function readAddForm() {
        const data = new FormData(addForm);

        return {
            title: String(data.get("title") || "").trim(),
            country: String(data.get("country") || "India").trim() || "India",
            city: String(data.get("city") || "").trim(),
            address: String(data.get("address") || "").trim(),
            type: String(data.get("type") || "").trim(),
            bhk: String(data.get("bhk") || "").trim(),
            price: Number(data.get("price") || 0),
            rent: Number(data.get("rent") || 0),
            area: Number(data.get("area") || 0),
            owner: String(data.get("owner") || "").trim(),
            contactNumber: String(data.get("contactNumber") || "").trim(),
            notes: String(data.get("notes") || "").trim()
        };
    }

    // Submit Add Property form
    addForm.addEventListener("submit", async function(event) {
        event.preventDefault();

        const button = document.getElementById("addPropertyBtn");
        button.disabled = true;
        button.textContent = "Adding...";
        showMessage("addMessage", "", true);

        try {
            await apiRequest("api/properties", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(readAddForm())
            });

            addForm.reset();
            showMessage("addMessage", "Property added successfully.", true);
            await loadProperties();
        } catch (error) {
            showMessage("addMessage", error.message, false);
        } finally {
            button.disabled = false;
            button.textContent = "Add Property";
        }
    });

    // Existing Edit submission
    editForm.addEventListener("submit", async function(event) {
        event.preventDefault();

        if (editingId === null) return;

        const button = document.getElementById("saveEditBtn");
        button.disabled = true;
        button.textContent = "Saving...";

        try {
            await apiRequest(
                "api/properties?id=" + encodeURIComponent(editingId),
                {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(readEditForm())
                }
            );

            showMessage("editMessage", "Property updated successfully.", true);
            cancelEdit();
            await loadProperties();
        } catch (error) {
            showMessage("editMessage", error.message, false);
        } finally {
            button.disabled = false;
            button.textContent = "Save Changes";
        }
    });

    // Existing Delete function
    async function deleteProperty(property) {
        const confirmed = window.confirm(
            'Delete "' + property.title + '" (ID ' + property.id +
            ")? This action cannot be undone."
        );

        if (!confirmed) return;

        try {
            await apiRequest(
                "api/properties?id=" + encodeURIComponent(property.id),
                { method: "DELETE" }
            );

            if (editingId === property.id) {
                cancelEdit();
            }

            showMessage(
                "listMessage",
                "Property #" + property.id + " deleted successfully.",
                true
            );

            await loadProperties();
        } catch (error) {
            showMessage("listMessage", error.message, false);
        }
    }

    // Existing logout function
    async function logout() {
        try {
            await fetch("api/auth", {
                method: "DELETE",
                credentials: "same-origin"
            });
        } finally {
            window.location.href = "login.jsp";
        }
    }

    async function initialize() {
        try {
            if (await checkAdminSession()) {
                await loadProperties();
            }
        } catch (error) {
            showMessage("listMessage", error.message, false);
        }
    }

    initialize();
</script>

</body>
</html>
```