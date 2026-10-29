
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Broker Dashboard | FlatDesk</title>
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <link rel="stylesheet" href="assets/style.css">

    <style>
        .action-buttons {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }

        .action-buttons button {
            padding: 7px 12px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            color: white;
        }

        .edit-btn { background: #176b57; }
        .delete-btn { background: #dc3545; }
        .cancel-btn { background: #667085; }

        .message {
            margin: 12px 0;
            overflow-wrap: anywhere;
        }

        .hidden { display: none !important; }

        .tablewrap table { min-width: 1050px; }

        .tablewrap td, .tablewrap th {
            padding: 12px;
            text-align: left;
            vertical-align: middle;
        }

        #editPanel { scroll-margin-top: 20px; }

        .form-actions {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }

        .status-controls {
            display: flex;
            flex-direction: column;
            gap: 8px;
            min-width: 125px;
        }

        .status-select {
            padding: 8px;
            width: 100%;
            border: 1px solid #d0d5dd;
            border-radius: 6px;
            background: #fff;
            color: #333;
        }

        .status-save-btn {
            padding: 8px 10px;
            border: none;
            border-radius: 5px;
            background: #176b57;
            color: #fff;
            cursor: pointer;
        }

        .status-save-btn:disabled,
        .action-buttons button:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }
    </style>
</head>
<body>

<nav class="top">
    <div class="logo">Flat<span>Desk</span></div>
    <div>
        <span id="welcome">Broker Dashboard</span>
        <button class="btn" type="button" onclick="logout()">Logout</button>
    </div>
</nav>

<main class="dash">
    <div class="stats">
        <div>My/All Listings <b id="count">0</b></div>
        <div>Role <b>BROKER</b></div>
        <div>Status <b>ACTIVE</b></div>
    </div>

    <section class="panel">
        <h2>Add Property</h2>

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
            <input name="price" type="number" min="0"
                   step="0.01" placeholder="Sale price">

            <input name="rent" type="number" min="0"
                   step="0.01" placeholder="Monthly rent">

            <input name="area" type="number" min="0"
                   step="0.01" placeholder="Area sq.ft.">

            <input name="owner" placeholder="Owner name">

            <!-- Property contact number -->
            <input name="contactNumber"
                   type="tel"
                   placeholder="Property contact number"
                   pattern="[0-9+() -]{7,20}"
                   title="Enter a valid contact number"
                   required>

            <textarea name="notes"
                      placeholder="Property description / notes"></textarea>

            <button class="btn primary" id="addBtn" type="submit">
                + Add Property
            </button>
        </form>

        <div class="message" id="addMessage" role="status"></div>
    </section>

    <section class="panel hidden" id="editPanel">
        <h2>Edit Property</h2>
        <p>Update the selected property's details and save your changes.</p>

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
            <input name="price" type="number" min="0"
                   step="0.01" placeholder="Sale price">

            <input name="rent" type="number" min="0"
                   step="0.01" placeholder="Monthly rent">

            <input name="area" type="number" min="0"
                   step="0.01" placeholder="Area sq.ft.">

            <input name="owner" placeholder="Owner name">

            <!-- Editable property contact number -->
            <input name="contactNumber"
                   type="tel"
                   placeholder="Property contact number"
                   pattern="[0-9+() -]{7,20}"
                   title="Enter a valid contact number"
                   required>

            <textarea name="notes"
                      placeholder="Property description / notes"></textarea>

            <div class="form-actions">
                <button class="btn primary" id="saveEditBtn" type="submit">
                    Save Changes
                </button>

                <button class="btn cancel-btn" type="button"
                        onclick="cancelEdit()">
                    Cancel
                </button>
            </div>
        </form>

        <div class="message" id="editMessage" role="status"></div>
    </section>

    <section class="panel">
        <h2>Property Listings</h2>

        <p>Change a property's availability using the status dropdown and Save Status button.</p>

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
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody id="table">
                    <tr>
                        <td colspan="7">Loading properties...</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="message" id="listMessage" role="status"></div>
    </section>
</main>

<script>
    const table = document.getElementById("table");
    const addForm = document.getElementById("addForm");
    const editForm = document.getElementById("editForm");
    const editPanel = document.getElementById("editPanel");

    let properties = [];
    let editingId = null;

    function money(value) {
        return Number(value || 0).toLocaleString("en-IN", {
            maximumFractionDigits: 2
        });
    }

    function showMessage(id, message, success) {
        const element = document.getElementById(id);
        element.textContent = message;
        element.style.color = success ? "#166534" : "#b42318";
    }

    async function apiRequest(url, options = {}) {
        const response = await fetch(url, {
            credentials: "same-origin",
            cache: "no-store",
            ...options,
            headers: {
                ...(options.headers || {})
            }
        });

        const result = await response.json().catch(() => ({}));

        if (response.status === 401) {
            window.location.href = "login.jsp";
            throw new Error(
                "Your session has expired. Please log in again."
            );
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

    async function checkSession() {
        const user = await apiRequest("api/auth");

        if (!user || user.role !== "BROKER") {
            window.location.href = "login.jsp";
            return false;
        }

        document.getElementById("welcome").textContent =
            "Welcome, " + (user.name || "Broker");

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

            // Status dropdown and Save Status button
            const statusCell = makeCell(row, "");
            const statusControls = document.createElement("div");
            statusControls.className = "status-controls";

            const statusSelect = document.createElement("select");
            statusSelect.className = "status-select";
            statusSelect.setAttribute(
                "aria-label",
                "Status for " + property.title
            );

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
            statusButton.className = "status-save-btn";
            statusButton.textContent = "Save Status";

            statusButton.addEventListener("click", () => {
                updatePropertyStatus(
                    property,
                    statusSelect,
                    statusButton
                );
            });

            statusControls.append(statusSelect, statusButton);
            statusCell.appendChild(statusControls);

            // Existing Edit and Delete actions
            const actions = makeCell(row, "");
            const actionBox = document.createElement("div");
            actionBox.className = "action-buttons";

            const editButton = document.createElement("button");
            editButton.type = "button";
            editButton.className = "edit-btn";
            editButton.textContent = "Edit";
            editButton.addEventListener(
                "click",
                () => startEdit(property)
            );

            const deleteButton = document.createElement("button");
            deleteButton.type = "button";
            deleteButton.className = "delete-btn";
            deleteButton.textContent = "Delete";
            deleteButton.addEventListener(
                "click",
                () => deleteProperty(property)
            );

            actionBox.append(editButton, deleteButton);
            actions.appendChild(actionBox);

            table.appendChild(row);
        });
    }

    async function updatePropertyStatus(property, select, button) {
        const newStatus = select.value;
        const previousStatus = property.status || "Available";

        if (newStatus === previousStatus) {
            showMessage(
                "listMessage",
                "No status change for property ID " + property.id + ".",
                true
            );
            return;
        }

        button.disabled = true;
        button.textContent = "Saving...";

        try {
            await apiRequest(
                "api/properties?id=" + encodeURIComponent(property.id),
                {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ status: newStatus })
                }
            );

            showMessage(
                "listMessage",
                "Status updated to " + newStatus +
                " for property ID " + property.id + ".",
                true
            );

            await loadProperties();
        } catch (error) {
            showMessage("listMessage", error.message, false);
            select.value = previousStatus;
            button.disabled = false;
            button.textContent = "Save Status";
        }
    }

    async function loadProperties() {
        try {
            properties = await apiRequest("api/properties");

            if (!Array.isArray(properties)) {
                throw new Error("Unexpected property list response.");
            }

            renderProperties();
        } catch (error) {
            showMessage("listMessage", error.message, false);
        }
    }

    function readForm(form) {
        const data = new FormData(form);

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

            // Send contact number to the backend
            contactNumber: String(
                data.get("contactNumber") || ""
            ).trim(),

            notes: String(data.get("notes") || "").trim()
        };
    }

    addForm.addEventListener("submit", async function(event) {
        event.preventDefault();

        const button = document.getElementById("addBtn");
        button.disabled = true;
        button.textContent = "Adding...";

        try {
            await apiRequest("api/properties", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(readForm(addForm))
            });

            addForm.reset();
            addForm.elements.country.value = "India";

            showMessage(
                "addMessage",
                "Property added successfully.",
                true
            );

            await loadProperties();
        } catch (error) {
            showMessage("addMessage", error.message, false);
        } finally {
            button.disabled = false;
            button.textContent = "+ Add Property";
        }
    });

    function startEdit(property) {
        editingId = property.id;

        for (const field of editForm.elements) {
            if (!field.name) continue;

            const value = property[field.name];
            field.value = value == null ? "" : value;
        }

        editPanel.classList.remove("hidden");
        showMessage("editMessage", "", true);
        editPanel.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    }

    function cancelEdit() {
        editingId = null;
        editForm.reset();
        editPanel.classList.add("hidden");
        showMessage("editMessage", "", true);
    }

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
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(readForm(editForm))
                }
            );

            editingId = null;
            editForm.reset();
            editPanel.classList.add("hidden");

            showMessage(
                "editMessage",
                "Property updated successfully.",
                true
            );

            await loadProperties();
        } catch (error) {
            showMessage("editMessage", error.message, false);
        } finally {
            button.disabled = false;
            button.textContent = "Save Changes";
        }
    });

    async function deleteProperty(property) {
        const confirmed = window.confirm(
            'Delete property "' + property.title + '" (ID ' +
            property.id + ")? This cannot be undone."
        );

        if (!confirmed) return;

        try {
            await apiRequest(
                "api/properties?id=" + encodeURIComponent(property.id),
                { method: "DELETE" }
            );

            showMessage(
                "listMessage",
                "Property deleted successfully.",
                true
            );

            if (editingId === property.id) {
                cancelEdit();
            }

            await loadProperties();
        } catch (error) {
            showMessage("listMessage", error.message, false);
        }
    }

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
            if (await checkSession()) {
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
