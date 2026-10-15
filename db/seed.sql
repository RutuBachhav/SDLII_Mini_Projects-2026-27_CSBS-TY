-- FoundIt - sample data so the application is demonstrable right after setup.
-- Run after schema.sql:  mysql -u root -p foundit_db < seed.sql
--
-- This file also seeds item_photos, which reference the image files committed
-- under uploads/items/. Keep the two together: loading this seed against an
-- empty uploads directory leaves six reports pointing at photographs that are
-- not there, and PhotoServlet will answer 404 for them.
--
-- LOGIN CREDENTIALS seeded by this file
--
--   role   email                  password
--   ADMIN  admin@foundit.local    123456789
--   USER   aarti@campus.edu       Demo@1234
--   USER   rohan@campus.edu       Demo@1234
--   USER   sneha@campus.edu       Demo@1234
--   USER   imran@campus.edu       Demo@1234
--   USER   meera@campus.edu       Demo@1234
--
-- These are demo credentials for a lab machine, stored as PBKDF2-HMAC-SHA256
-- hashes (never plaintext). Change the administrator password before this is
-- used anywhere real:
--
--     java -cp "build/web/WEB-INF/classes;D:\mysql-connector-j-9.7.0\mysql-connector-j-9.7.0.jar" --          com.util.AdminInit admin@foundit.local "<your password>"
--
-- To ship with the administrator locked instead, set its password_hash to '!',
-- which no password can produce, and run AdminInit to open it.

USE foundit_db;

-- ---------------------------------------------------------------- categories

INSERT INTO categories (name) VALUES
  ('Electronics'),
  ('Wallets & Purses'),
  ('ID Cards & Documents'),
  ('Keys'),
  ('Bags'),
  ('Books & Stationery'),
  ('Clothing & Accessories'),
  ('Other');

-- ---------------------------------------------------------------- users

INSERT INTO users (name, email, password_hash, role) VALUES
  ('Campus Administrator', 'admin@foundit.local', 'pbkdf2$sha256$120000$gwDNmCCYiQmEMUKjXgi/FA==$Pa0+VJUe0WzMKXGaKB1+k1UNanReo9W1hXNj942QpH0=', 'ADMIN'),
  ('Aarti Deshmukh',  'aarti@campus.edu',  'pbkdf2$sha256$120000$Nueh7c9R6XdjTzx3M/taNg==$D37kj7t3T5RkkLP3FCwnwVsdBdUIsvQM8yYVruqL26Q=', 'USER'),
  ('Rohan Kulkarni',  'rohan@campus.edu',  'pbkdf2$sha256$120000$Nueh7c9R6XdjTzx3M/taNg==$D37kj7t3T5RkkLP3FCwnwVsdBdUIsvQM8yYVruqL26Q=', 'USER'),
  ('Sneha Patil',     'sneha@campus.edu',  'pbkdf2$sha256$120000$Nueh7c9R6XdjTzx3M/taNg==$D37kj7t3T5RkkLP3FCwnwVsdBdUIsvQM8yYVruqL26Q=', 'USER'),
  ('Imran Shaikh',    'imran@campus.edu',  'pbkdf2$sha256$120000$Nueh7c9R6XdjTzx3M/taNg==$D37kj7t3T5RkkLP3FCwnwVsdBdUIsvQM8yYVruqL26Q=', 'USER'),
  ('Meera Joshi',     'meera@campus.edu',  'pbkdf2$sha256$120000$Nueh7c9R6XdjTzx3M/taNg==$D37kj7t3T5RkkLP3FCwnwVsdBdUIsvQM8yYVruqL26Q=', 'USER');

-- ---------------------------------------------------------------- reports
-- Pairs below are deliberately close so the matching algorithm has real work
-- to do: a wallet, a pair of earphones, a hostel key and an ID card.

INSERT INTO items
  (reporter_id, type, title, category_id, description, color, private_marks, location, item_date, status)
VALUES
  (2, 'LOST', 'Brown leather wallet', 2,
   'Small bi-fold leather wallet. Lost somewhere between the library reading hall and the canteen. Had my bus pass and about 300 rupees in it.',
   'Brown', 'Driving licence inside with my name, and a torn photo of my dog behind the card slot.',
   'Central Library', CURDATE() - INTERVAL 4 DAY, 'OPEN'),

  (3, 'FOUND', 'Leather wallet found near canteen', 2,
   'Found a brown bi-fold wallet on the bench outside the main canteen. Cards are inside. Deposited with me, will hand over to the right owner.',
   'Brown', 'Contains a driving licence and a small photo tucked behind the cards.',
   'Main Canteen', CURDATE() - INTERVAL 3 DAY, 'OPEN'),

  (4, 'LOST', 'Black wireless earphones case', 1,
   'Matte black earphones case, left it on a lab desk during the DBMS practical. Right earbud has a small scratch.',
   'Black', 'Serial number sticker peeled off, and there is a blue dot sticker inside the lid.',
   'Computer Lab 2', CURDATE() - INTERVAL 7 DAY, 'OPEN'),

  (5, 'FOUND', 'Black earphone case on lab table', 1,
   'Picked up a black earphones case from a table in the computer lab block after the evening practical. Kept it safe.',
   'Black', 'There is a small coloured sticker inside the lid.',
   'Computer Lab 1', CURDATE() - INTERVAL 6 DAY, 'OPEN'),

  (6, 'LOST', 'Hostel room key with red tag', 4,
   'Bunch of two keys on a red plastic tag, room number written with marker. Probably dropped near the sports ground gate.',
   'Red', 'Room number 214 written on the tag in black marker.',
   'Sports Ground', CURDATE() - INTERVAL 2 DAY, 'OPEN'),

  (2, 'FOUND', 'Keys with red tag near ground gate', 4,
   'Two keys on a red tag, found at the gate near the sports ground in the evening. Handed to me by a junior.',
   'Red', 'A room number is written on the tag.',
   'Sports Ground Gate', CURDATE() - INTERVAL 1 DAY, 'OPEN'),

  (3, 'LOST', 'College ID card', 3,
   'Lost my college identity card, probably in the admin building queue. Third year Computer Engineering.',
   'White', 'Roll number ends with 045 and the card has a bent corner.',
   'Admin Building', CURDATE() - INTERVAL 10 DAY, 'OPEN'),

  (5, 'FOUND', 'Blue backpack left in bus bay', 5,
   'A blue backpack was left in the bus bay. It has notebooks inside. Please describe the contents before claiming.',
   'Blue', 'Contains a chemistry notebook with a name written on the first page.',
   'Bus Bay', CURDATE() - INTERVAL 5 DAY, 'OPEN'),

  (6, 'FOUND', 'Scientific calculator found in exam hall', 6,
   'Casio scientific calculator found on a desk in exam hall B after the afternoon paper.',
   'Grey', 'Owner name scratched on the back of the slide cover.',
   'Exam Hall B', CURDATE() - INTERVAL 8 DAY, 'OPEN'),

  (4, 'LOST', 'Grey hoodie left in classroom', 7,
   'Grey hooded sweatshirt, size M, left on a chair in room 304 after the last lecture.',
   'Grey', 'Small ink stain on the left sleeve.',
   'Room 304', CURDATE() - INTERVAL 12 DAY, 'CLOSED');

-- A finished case, so the dashboards and the admin history are not empty:
-- Meera lost a water bottle, Imran found it, the claim was approved and the
-- return was recorded.

INSERT INTO items
  (reporter_id, type, title, category_id, description, color, private_marks, location, item_date, status, resolved_at)
VALUES
  (6, 'LOST', 'Steel water bottle', 8,
   'One litre steel bottle with a dented base, left in the seminar hall.',
   'Silver', 'Dented base and a faded sticker of a mountain on the side.',
   'Seminar Hall', CURDATE() - INTERVAL 20 DAY, 'RESOLVED', NOW() - INTERVAL 14 DAY),
  (5, 'FOUND', 'Steel bottle found in seminar hall', 8,
   'Steel water bottle found under a chair in the seminar hall. Returned to the owner after verification.',
   'Silver', 'Dented base, sticker on the side.',
   'Seminar Hall', CURDATE() - INTERVAL 19 DAY, 'RESOLVED', NOW() - INTERVAL 14 DAY);

-- ---------------------------------------------------------------- photographs
-- Photographs for the found-item reports, so a fresh install shows the board
-- the way a user would see it rather than a grid of "No photo" placeholders.
--
-- Only the storage key lives here; the bytes are the files committed under
-- uploads/items/. The key is the server-generated name Storage.save() would
-- have produced, and PhotoServlet streams the file by photo id, so these rows
-- behave exactly like an uploaded photograph.

INSERT INTO item_photos (item_id, storage_key, content_type, size_bytes) VALUES
  (2 , 'items/d76c730dfe9177ca8947e4db4b84adf8.jpg', 'image/jpeg', 16626),  -- Leather wallet found near canteen
  (4 , 'items/093ffeb4da7eaed8c8e5846b5604efee.jpg', 'image/jpeg', 13063),  -- Black earphone case on lab table
  (6 , 'items/34b37a387b6ef369f525a4ce23c0938a.jpg', 'image/jpeg', 15012),  -- Keys with red tag near ground gate
  (8 , 'items/275e1ba534cd33c72a7a4c8bf3c73ced.jpg', 'image/jpeg', 19469),  -- Blue backpack left in bus bay
  (9 , 'items/a6c9694a56aedbb93d0065f1090e7417.jpg', 'image/jpeg', 23953),  -- Scientific calculator found in exam hall
  (12, 'items/ed3c669b18b51c98e4e7219260590f6c.jpg', 'image/jpeg', 12979); -- Steel bottle found in seminar hall

-- ---------------------------------------------------------------- closed case

INSERT INTO claims (item_id, claimant_id, evidence, contact, status, review_note, reviewed_by, reviewed_at, created_at)
VALUES
  (12, 6,
   'It is a one litre steel bottle, the base is dented because it fell down the hostel stairs, and there is a faded mountain sticker on the side.',
   '+91 98765 43210', 'COMPLETED', 'Description of the dent and the sticker matched.', 5,
   NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 16 DAY);

INSERT INTO item_returns (item_id, claim_id, handled_by, note, returned_at)
VALUES (12, 1, 5, 'Handed over at the hostel office.', NOW() - INTERVAL 14 DAY);

-- One pending claim waiting for review, so the review inbox has something in it.

INSERT INTO claims (item_id, claimant_id, evidence, contact, status, created_at)
VALUES
  (8, 4,
   'It is my blue backpack. The chemistry notebook inside has my name on the first page and the front zip pocket has a broken zip pull.',
   'sneha@campus.edu', 'PENDING', NOW() - INTERVAL 1 DAY);

-- ---------------------------------------------------------------- matches
-- MatchService writes these whenever a report is filed or edited. They are
-- seeded here with the exact scores and reasons the algorithm produces for the
-- rows above, so a fresh install looks like the board has been in use rather
-- than needing somebody to file a report first. Re-running matching is
-- idempotent: the unique key on (lost_item_id, found_item_id) refreshes these
-- rows instead of adding more.

INSERT INTO matches (lost_item_id, found_item_id, score, reasons) VALUES
  (5, 6, 92, 'Same category: Keys | Description words in common: ground, sport, gate | Both reported as red | Nearby places: Sports Ground and Sports Ground Gate | Dates are one day apart'),
  (3, 4, 86, 'Same category: Electronics | Description words in common: practical, earphon, black | Both reported as black | Nearby places: Computer Lab 2 and Computer Lab 1 | Dates are one day apart'),
  (1, 2, 76, 'Same category: Wallets & Purses | Description words in common: leather, canteen, wallet | Both reported as brown | Dates are one day apart');

-- ---------------------------------------------------------------- notifications
-- The dedupe_key values match what MatchService and ClaimWorkflow generate, so
-- the application will not post a second copy of any of these later.

INSERT INTO notifications (user_id, kind, message, link, is_read, dedupe_key, created_at) VALUES
  (6, 'RETURN_DONE', 'Your steel water bottle was marked as returned.', '/items/view?id=12', 1, 'ret-1', NOW() - INTERVAL 14 DAY),
  (5, 'CLAIM_NEW',   'Sneha Patil submitted a claim on "Blue backpack left in bus bay".', '/claims/review', 0, 'claim-2', NOW() - INTERVAL 1 DAY),
  (2, 'MATCH', 'A found report looks like your lost "Brown leather wallet" (76% match).', '/items/view?id=1', 0, 'match-1-2-u2', NOW() - INTERVAL 3 DAY),
  (3, 'MATCH', 'Someone''s lost report looks like the "Leather wallet found near canteen" you found (76% match).', '/items/view?id=2', 0, 'match-1-2-u3', NOW() - INTERVAL 3 DAY),
  (4, 'MATCH', 'A found report looks like your lost "Black wireless earphones case" (86% match).', '/items/view?id=3', 0, 'match-3-4-u4', NOW() - INTERVAL 6 DAY),
  (5, 'MATCH', 'Someone''s lost report looks like the "Black earphone case on lab table" you found (86% match).', '/items/view?id=4', 0, 'match-3-4-u5', NOW() - INTERVAL 6 DAY),
  (6, 'MATCH', 'A found report looks like your lost "Hostel room key with red tag" (92% match).', '/items/view?id=5', 0, 'match-5-6-u6', NOW() - INTERVAL 1 DAY),
  (2, 'MATCH', 'Someone''s lost report looks like the "Keys with red tag near ground gate" you found (92% match).', '/items/view?id=6', 0, 'match-5-6-u2', NOW() - INTERVAL 1 DAY);

-- ---------------------------------------------------------------- audit

INSERT INTO audit_log (actor_id, action, entity, entity_id, detail, created_at) VALUES
  (5, 'CLAIM_APPROVED', 'claim', 1, 'Steel water bottle claim approved after verification', NOW() - INTERVAL 15 DAY),
  (5, 'RETURN_RECORDED', 'item', 12, 'Item handed over to Meera Joshi', NOW() - INTERVAL 14 DAY);
