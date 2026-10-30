ProductRegistration - ABC Technologies
=====================================
Same type as WebApplication1 (Employee Management) but for products,
with the bugs fixed and extra features + improved UI.

STACK
- NetBeans Ant web project, Java 11, Jakarta EE 10 (jakarta.*), GlassFish 7
- Servlets + JPA/EclipseLink (RESOURCE_LOCAL) + MySQL + JAX-RS + static HTML/CSS/JS
- DB: product_db (auto-created), user root / NewPassword123! (see src/conf/persistence.xml)

OPEN & RUN
1. NetBeans -> File -> Open Project -> select ProductRegistration folder.
2. Check Libraries: mysql-connector-j-9.7.0.jar resolves (same relative path as
   WebApplication1: ../../../../Downloads/mysql-connector-j-...). If broken,
   right-click Libraries -> Add JAR and point to your connector.
3. Start GlassFish 7 domain + MySQL on localhost:3306.
4. Run (F6). Context root: /ProductRegistration. Start page: index.html.

WHAT IT DOES
- Categories: AddCategoryServlet (POST /addCategory) with duplicate-name check.
- Products: AddProductServlet (POST /saveProduct) with full server-side
  validation (SKU format, unique SKU, price > 0, qty >= 0, category exists).
- List: ProductListServlet (GET /viewProducts?q=&categoryId=) server-rendered
  table with stock totals; viewProducts.html is the live REST version.
- Update: GET/POST /getProductForUpdate renders pre-filled form ->
  POST /updateProduct applies edit (this 2nd servlet was missing entirely
  in the Employee sample).
- Delete: POST /deleteProduct with not-found handling + confirm dialog.
  Accepts either id= or name= (case-insensitive exact product name; if several
  products share the name, a pick-one table is shown).
- REST (/api): ProductResource full CRUD (GET list w/ filters, GET one,
  POST, PUT, DELETE) + CategoryResource (GET list/one, POST).
  Employee sample had only 2 read-only GETs.

IMPROVEMENTS OVER WebApplication1
- Single shared JpaUtil listener (one EMF for whole app) instead of one EMF
  per servlet; proper shutdown.
- Bean Validation actually enforced + number-format safe parsing.
- No hardcoded department/category IDs: dropdowns load live via /api/categories.
- Implemented the missing pieces: view-all, update-commit, category CRUD.
- XSS escaping (Html.esc), UTF-8 everywhere, PRG redirect after addCategory.
- New features: SKU, search by name/SKU, filter by category, stock value
  totals, low-stock badges (<10), dashboard stats, delete confirmation.
- Shared theme: web/css/style.css + web/js/app.js, responsive sidebar layout.

ENDPOINTS (context root /ProductRegistration)
- index.html, addCategory.html, addProduct.html, viewProducts.html,
  updateProduct.html, deleteProduct.html
- Servlets: /addCategory /saveProduct /viewProducts /getProductForUpdate
  /updateProduct /deleteProduct
- REST: /api/products /api/products/{id} /api/categories /api/categories/{id}

TABLES
- category(id, name UNIQUE, description)
- product(id, sku UNIQUE, name, price, quantity, description, category_id FK)
