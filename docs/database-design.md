# Database Design

The `cloth_factory` database uses InnoDB tables and foreign keys so product, supplier, purchase, production, and audit records remain connected.

## Main tables

- `users` stores login identities and roles. Passwords will be stored as BCrypt hashes when authentication is implemented.
- `products` stores the catalog and current stock. `current_quantity` is deliberate operational denormalization: it makes dashboard reads fast, while every change is recorded in `stock_transactions`.
- `suppliers` stores supplier contact information.
- `purchases` and `purchase_items` model a purchase header and its one-or-more product lines.
- `stock_transactions` is the audit trail for stock-in, stock-out, and adjustment events.
- `production_batches` stores finished production output and `production_materials` stores the raw materials consumed by each batch.

## Important integrity rules

- Quantities and prices use `DECIMAL`, not floating-point types.
- Positive quantity checks prevent meaningless movement records.
- Product stock has a database-level non-negative check. Transactional Java service code will also lock the product row before updating it, preventing concurrent stock-out operations from overselling inventory.
- Foreign keys prevent orphaned purchases, transactions, and production records.

## Sample-data warning

The sample admin row intentionally contains `{REPLACE_WITH_BCRYPT_HASH}`. It is a schema placeholder only, not a usable password. The authentication phase will generate a BCrypt hash and replace it with a real development account through a controlled setup step.