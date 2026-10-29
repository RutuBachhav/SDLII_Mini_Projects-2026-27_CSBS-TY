USE expense_management;

-- Add clearly labeled examples for the previous calendar month and the current month.
-- Safe to rerun: an existing demo title for the same category and month is skipped.
INSERT INTO expenses (title, amount, category, expenseDate, paymentMethod, description)
SELECT CONCAT('Demo - ', category, ' - ', DATE_FORMAT(sample_date, '%Y-%m')),
       amount, category, sample_date, payment_method,
       'Illustrative sample record; safe to edit or delete.'
FROM (
    SELECT s.category, s.amount, s.payment_method,
           DATE_SUB(LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), INTERVAL s.day_offset DAY) AS sample_date
    FROM (
        SELECT 'Food' AS category, 124.80 AS amount, 'Card' AS payment_method, 0 AS day_offset
        UNION ALL SELECT 'Travel', 286.40, 'Bank transfer', 4
        UNION ALL SELECT 'Office', 42.35, 'Cash', 10
        UNION ALL SELECT 'Software', 79.00, 'Card', 16
        UNION ALL SELECT 'Meals', 96.50, 'Cash', 22
    ) s
) demo
WHERE NOT EXISTS (
    SELECT 1 FROM expenses e
    WHERE e.title = CONCAT('Demo - ', demo.category, ' - ', DATE_FORMAT(demo.sample_date, '%Y-%m'))
);

INSERT INTO expenses (title, amount, category, expenseDate, paymentMethod, description)
SELECT CONCAT('Demo - ', category, ' - ', DATE_FORMAT(sample_date, '%Y-%m')),
       amount, category, sample_date, payment_method,
       'Illustrative sample record; safe to edit or delete.'
FROM (
    SELECT s.category, s.amount, s.payment_method,
           DATE_SUB(CURDATE(), INTERVAL LEAST(s.day_offset, DAYOFMONTH(CURDATE()) - 1) DAY) AS sample_date
    FROM (
        SELECT 'Food' AS category, 48.25 AS amount, 'Cash' AS payment_method, 0 AS day_offset
        UNION ALL SELECT 'Travel', 163.00, 'Card', 1
        UNION ALL SELECT 'Utilities', 112.45, 'Bank transfer', 2
        UNION ALL SELECT 'Office', 23.90, 'Cash', 3
    ) s
) demo
WHERE NOT EXISTS (
    SELECT 1 FROM expenses e
    WHERE e.title = CONCAT('Demo - ', demo.category, ' - ', DATE_FORMAT(demo.sample_date, '%Y-%m'))
);

SELECT COUNT(*) AS sample_expenses
FROM expenses
WHERE title LIKE CONCAT('Demo - % - ', DATE_FORMAT(CURDATE(), '%Y-%m'))
   OR title LIKE CONCAT('Demo - % - ', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m'));
