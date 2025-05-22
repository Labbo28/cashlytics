-- Inserisci l'utente
INSERT INTO cashlytics_user (id, email, first_name, last_name, phone_number, created_at)
VALUES (1, 'admin@cashlytics.com', 'Admin', 'User', '1234567890', NOW());

-- Inserisci le credenziali legate all'utente
INSERT INTO credentials (id, username, password, user_id)
VALUES (1, 'admin', '$2a$10$6dqiSPdKEhdl.7RZlpiIn.xbgpTb5b6/i5gDOGV3tRArykkOQcCyC', 1);
