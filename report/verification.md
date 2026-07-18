Verification Report - Database Migration and Integration Tests

Summary:
- Applied normalized schema with `cart_items` and `order_items`.
- Implemented JDBC repositories and normalized cart storage.
- Automated schema application, optional cart migration, and full integration test run.

Key Evidence (sample rows):
- Admins: id=1 | username=admin
- Customers: id=1 | username=cust1
- Delivery partners: id=1 | username=driver1
- Menu items: id=1 (Paneer Butter Masala), id=2 (Margherita Pizza)
- Cart items: two rows for customer_id=1 (paneer x2, pizza x1)
- Orders: id=1 | status=DELIVERED | final_amount=809.1
- Order items: two rows linked to order_id=1

Notes:
- IntegrationTestRunner completes successfully when `SchemaApplier` runs first.
- `CartMigration` is safe: it only migrates serialized `carts.items` if the column exists.

Next steps (optional):
- Remove migration and inspection helpers if you want a production-only codebase.
- Add CI to run `SchemaApplier` + integration test on clean DB automatically.

Date: 2026-07-18
# Integration Verification Report

Date: 2026-07-18

Summary: Automated integration test run executed against PostgreSQL `food_app_db`. All main flows succeeded.

-- Observed sample rows (selected):

- admins

1. id=1 | username=admin | password=admin123 | name=System Admin | is_active=true | created_at=2026-07-18 16:45:25

- customers

1. id=1 | username=cust1 | password=pass123 | name=Customer One | phone=8888888888 | address=123 Park | is_active=true | created_at=2026-07-18 16:45:28

- delivery_partners

1. id=1 | username=driver1 | password=pass123 | name=Driver One | phone=9999999999 | vehicle_type=Bike | is_available=true | is_active=true | created_at=2026-07-18 16:45:26

- menu_items

1. id=1 | Paneer Butter Masala | price=250 | category=VEG | cuisine_type=INDIAN
2. id=2 | Margherita Pizza | price=399 | category=VEG | cuisine_type=ITALIAN

- orders

1. id=1 | customer_id=1 | customer_name=Customer One | total_amount=899 | discount_amount=89.9 | final_amount=809.1 | payment_mode=CASH | delivery_partner_id=1 | status=DELIVERED | ordered_at=2026-07-18 16:45:32

- order_items

1. id=7 | order_id=1 | menu_item_id=1 | item_name=Paneer Butter Masala | price=250 | quantity=2
2. id=8 | order_id=1 | menu_item_id=2 | item_name=Margherita Pizza | price=399 | quantity=1

Feature checklist:

- Default admin seed: OK
- Driver registration: OK
- Customer registration: OK
- Menu item creation: OK
- Cart operations (add/clear): OK
- Place order (transactionally insert orders + order_items): OK
- Order status transitions: OK

Notes:

- `carts` table is used with `customer_id` as primary key and stores serialized `items` (text); after placing an order cart rows are cleared — current design works but normalization to `cart_items` is optional.
- Temporary helper files used during verification were removed from the project.

Next actions (optional):

- Implement `UserJdbcRepository` if desired.
- Normalize `carts` into `cart_items` for relational integrity.
