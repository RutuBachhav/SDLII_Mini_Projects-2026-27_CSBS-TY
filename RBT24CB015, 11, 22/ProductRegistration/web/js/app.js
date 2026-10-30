/* Shared front-end: dynamic category dropdowns, dashboard stats, live table. */
async function api(path, options) {
  const res = await fetch("api" + path, options);
  if (!res.ok) {
    const txt = await res.text().catch(() => "");
    throw new Error(txt || ("HTTP " + res.status));
  }
  if (res.status === 204) return null;
  return res.json();
}

async function loadCategories(selectId, filterSelectId) {
  try {
    const cats = await api("/categories");
    const sel = selectId ? document.getElementById(selectId) : null;
    if (sel) {
      sel.innerHTML = '<option value="">-- Select Category --</option>'
        + cats.map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join("");
    }
    const fsel = filterSelectId ? document.getElementById(filterSelectId) : null;
    if (fsel) {
      fsel.innerHTML = '<option value="">All Categories</option>'
        + cats.map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join("");
    }
    const count = document.getElementById("catCount");
    if (count) count.textContent = cats.length;
    return cats;
  } catch (e) {
    console.warn("Categories unavailable:", e);
    return [];
  }
}

async function loadStats() {
  try {
    const products = await api("/products");
    const total = products.length;
    const qty = products.reduce((s, p) => s + (p.quantity || 0), 0);
    const value = products.reduce((s, p) => s + (p.price || 0) * (p.quantity || 0), 0);
    const low = products.filter(p => (p.quantity || 0) < 10).length;
    set("prodCount", total);
    set("qtyCount", qty);
    set("valueCount", "$" + value.toFixed(2));
    set("lowCount", low);
  } catch (e) {
    console.warn("Stats unavailable:", e);
  }
}

function set(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val;
}

async function renderTable() {
  const tbody = document.getElementById("productRows");
  if (!tbody) return;
  const q = (document.getElementById("q") || {}).value || "";
  const cat = (document.getElementById("filterCat") || {}).value || "";
  let url = "/products";
  const params = new URLSearchParams();
  if (q.trim()) params.set("q", q.trim());
  if (cat) params.set("categoryId", cat);
  if ([...params].length) url += "?" + params.toString();
  tbody.innerHTML = '<tr><td colspan="9">Loading...</td></tr>';
  try {
    const products = await api(url);
    if (!products.length) {
      tbody.innerHTML = '<tr><td colspan="9">No products found.</td></tr>';
      return;
    }
    tbody.innerHTML = products.map(p => {
      const badge = (p.quantity || 0) < 10
        ? "<span class='badge low'>Low stock</span>"
        : "<span class='badge ok'>In stock</span>";
      const catName = p.category ? escapeHtml(p.category.name) : "-";
      const val = ((p.price || 0) * (p.quantity || 0)).toFixed(2);
      return `<tr><td>${p.id}</td><td>${escapeHtml(p.sku || "")}</td>`
        + `<td>${escapeHtml(p.name || "")}</td><td>${catName}</td>`
        + `<td>$${Number(p.price || 0).toFixed(2)}</td><td>${p.quantity}</td>`
        + `<td>$${val}</td><td>${badge}</td>`
        + `<td><a class='btn secondary' href='getProductForUpdate?id=${p.id}'>Edit</a></td></tr>`;
    }).join("");
  } catch (e) {
    tbody.innerHTML = '<tr><td colspan="9">Could not load products. Is the app deployed?</td></tr>';
  }
}

function escapeHtml(s) {
  return String(s == null ? "" : s).replace(/[&<>"']/g, c =>
    ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#x27;" }[c]));
}

function showSuccessFromQuery() {
  const id = new URLSearchParams(location.search).get("success");
  if (id) {
    const el = document.getElementById("successBox");
    if (el) {
      el.innerHTML = `<div class='alert success'>Saved successfully (ID ${escapeHtml(id)}).</div>`;
    }
  }
}

document.addEventListener("DOMContentLoaded", () => {
  showSuccessFromQuery();
  if (document.getElementById("categoryId")) loadCategories("categoryId", null);
  if (document.getElementById("filterCat")) {
    loadCategories(null, "filterCat").then(renderTable);
  }
  if (document.getElementById("prodCount")) {
    loadStats();
    loadCategories(null, null);
  }
  const form = document.getElementById("searchForm");
  if (form) form.addEventListener("submit", e => { e.preventDefault(); renderTable(); });
});
