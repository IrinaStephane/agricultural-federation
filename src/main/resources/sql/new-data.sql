-- =============================================================================
-- data.sql – Agricultural Federation
-- Données EXACTES conformes au sujet (pages 11–20)
-- =============================================================================

-- ─── Fédération ──────────────────────────────────────────────────────────────

INSERT INTO federation (id, cotisation_percentage)
VALUES (1, 10.00)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- MEMBRES (16 membres uniques)
-- Membres 1-8  : appartiennent aux collectivités 1 ET 2 (mêmes personnes)
-- Membres 9-16 : appartiennent uniquement à la collectivité 3
-- enrolment_date fixé à 2020-01-01 pour tous → valides comme parrains (>90j, >6mois)
-- =============================================================================

INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date,
                    address, email, phone_number, profession, gender)
VALUES
-- ── Collectivités 1 & 2 ──────────────────────────────────────────────────────
(1,  'Prenom membre 1',  'Nom membre 1',  '1980-02-01', '2020-01-01',
 'Lot II V M Ambato.', 'member.1@fed-agri.mg', '0341234567', 'Riziculteur',  'MALE'),

(2,  'Prenom membre 2',  'Nom membre 2',  '1982-03-05', '2020-01-01',
 'Lot II F Ambato.',  'member.2@fed-agri.mg', '0321234567', 'Agriculteur',  'MALE'),

(3,  'Prenom membre 3',  'Nom membre 3',  '1992-03-10', '2020-01-01',
 'Lot II J Ambato.',  'member.3@fed-agri.mg', '0331234567', 'Collecteur',   'MALE'),

(4,  'Prenom membre 4',  'Nom membre 4',  '1988-05-22', '2020-01-01',
 'Lot A K 50 Ambato.','member.4@fed-agri.mg', '0381234567', 'Distributeur', 'FEMALE'),

(5,  'Prenom membre 5',  'Nom membre 5',  '1999-08-21', '2020-01-01',
 'Lot UV 80 Ambato.', 'member.5@fed-agri.mg', '0373434567', 'Riziculteur',  'MALE'),

(6,  'Prenom membre 6',  'Nom membre 6',  '1998-08-22', '2020-01-01',
 'Lot UV 6 Ambato.',  'member.6@fed-agri.mg', '0372234567', 'Riziculteur',  'FEMALE'),

(7,  'Prenom membre 7',  'Nom membre 7',  '1998-01-31', '2020-01-01',
 'Lot UV 7 Ambato.',  'member.7@fed-agri.mg', '0374234567', 'Riziculteur',  'MALE'),

(8,  'Prenom membre 8',  'Nom membre 8',  '1975-08-20', '2020-01-01',
 'Lot UV 8 Ambato.',  'member.8@fed-agri.mg', '0370234567', 'Riziculteur',  'MALE'),

-- ── Collectivité 3 uniquement ─────────────────────────────────────────────────
(9,  'Prenom membre 9',  'Nom membre 9',  '1988-01-02', '2020-01-01',
 'Lot 33 J Antsirabe','member.9@fed-agri.mg',  '034034567',  'Apiculteur',   'MALE'),

(10, 'Prenom membre 10', 'Nom membre 10', '1982-03-05', '2020-01-01',
 'Lot 2 J Antsirabe', 'member.10@fed-agri.mg', '0338634567', 'Agriculteur',  'MALE'),

(11, 'Prenom membre 11', 'Nom membre 11', '1992-03-12', '2020-01-01',
 'Lot 8 KM Antsirabe','member.11@fed-agri.mg', '0338234567', 'Collecteur',   'MALE'),

(12, 'Prenom membre 12', 'Nom membre 12', '1988-05-10', '2020-01-01',
 'Lot A K 50 Antsirabe','member.12@fed-agri.mg','0382334567','Distributeur', 'FEMALE'),

(13, 'Prenom membre 13', 'Nom membre 13', '1999-08-11', '2020-01-01',
 'Lot UV 80 Antsirabe','member.13@fed-agri.mg','0373365567', 'Apiculteur',   'MALE'),

(14, 'Prenom membre 14', 'Nom membre 14', '1998-08-09', '2020-01-01',
 'Lot UV 6 Antsirabe','member.14@fed-agri.mg', '0378234567', 'Apiculteur',   'FEMALE'),

(15, 'Prenom membre 15', 'Nom membre 15', '1998-01-13', '2020-01-01',
 'Lot UV 7 Antsirabe','member.15@fed-agri.mg', '0374914567', 'Apiculteur',   'MALE'),

(16, 'Prenom membre 16', 'Nom membre 16', '1975-08-02', '2020-01-01',
 'Lot UV 8 Antsirabe','member.16@fed-agri.mg', '0370634567', 'Apiculteur',   'MALE')

ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- COLLECTIVITÉS  (Table 1, page 11)
-- number stocké en VARCHAR comme demandé dans le schéma
-- =============================================================================

INSERT INTO collectivity (id, number, name, speciality, creation_datetime,
                          federation_approval, authorization_date, id_federation, location)
VALUES
-- col-1 : ID=1, numéro=1, nom=Mpanorina, Ambatondrazaka, Riziculture
(1, '1', 'Mpanorina',       'Riziculture', '2020-01-01 00:00:00',
 TRUE, '2020-01-01 00:00:00', 1, 'Ambatondrazaka'),

-- col-2 : ID=2, numéro=2, nom=Dobo voalohany, Ambatondrazaka, Pisciculture
(2, '2', 'Dobo voalohany',  'Pisciculture','2020-01-01 00:00:00',
 TRUE, '2020-01-01 00:00:00', 1, 'Ambatondrazaka'),

-- col-3 : ID=3, numéro=3, nom=Tantely mamy, Brickaville, Apiculture
(3, '3', 'Tantely mamy',    'Apiculture',  '2020-01-01 00:00:00',
 TRUE, '2020-01-01 00:00:00', 1, 'Brickaville')

ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- MEMBRE ↔ COLLECTIVITÉ  (Tables 2, 3, 4)
-- Collectivité 1 : membres 1-8
-- Collectivité 2 : mêmes membres 1-8 avec rôles différents
-- Collectivité 3 : membres 9-16
-- =============================================================================

INSERT INTO member_collectivity (id_member, id_collectivity, occupation, start_date)
VALUES
-- ── Collectivité 1 (Table 2) ─────────────────────────────────────────────────
(1, 1, 'PRESIDENT',      '2020-01-01 00:00:00'),  -- C1-M1
(2, 1, 'VICE_PRESIDENT', '2020-01-01 00:00:00'),  -- C1-M2
(3, 1, 'SECRETARY',      '2020-01-01 00:00:00'),  -- C1-M3
(4, 1, 'TREASURER',      '2020-01-01 00:00:00'),  -- C1-M4
(5, 1, 'SENIOR',         '2020-01-01 00:00:00'),  -- C1-M5 (Confirmé)
(6, 1, 'SENIOR',         '2020-01-01 00:00:00'),  -- C1-M6 (Confirmé)
(7, 1, 'SENIOR',         '2020-01-01 00:00:00'),  -- C1-M7 (Confirmé)
(8, 1, 'SENIOR',         '2020-01-01 00:00:00'),  -- C1-M8 (Confirmé)

-- ── Collectivité 2 (Table 3) ─────────────────────────────────────────────────
(1, 2, 'SENIOR',         '2020-01-01 00:00:00'),  -- C2-M1 (Confirmé)
(2, 2, 'SENIOR',         '2020-01-01 00:00:00'),  -- C2-M2 (Confirmé)
(3, 2, 'SENIOR',         '2020-01-01 00:00:00'),  -- C2-M3 (Confirmé)
(4, 2, 'SENIOR',         '2020-01-01 00:00:00'),  -- C2-M4 (Confirmé)
(5, 2, 'PRESIDENT',      '2020-01-01 00:00:00'),  -- C2-M5
(6, 2, 'VICE_PRESIDENT', '2020-01-01 00:00:00'),  -- C2-M6
(7, 2, 'SECRETARY',      '2020-01-01 00:00:00'),  -- C2-M7
(8, 2, 'TREASURER',      '2020-01-01 00:00:00'),  -- C2-M8

-- ── Collectivité 3 (Table 4) ─────────────────────────────────────────────────
(9,  3, 'PRESIDENT',      '2020-01-01 00:00:00'), -- C3-M1
(10, 3, 'VICE_PRESIDENT', '2020-01-01 00:00:00'), -- C3-M2
(11, 3, 'SECRETARY',      '2020-01-01 00:00:00'), -- C3-M3
(12, 3, 'TREASURER',      '2020-01-01 00:00:00'), -- C3-M4
(13, 3, 'SENIOR',         '2020-01-01 00:00:00'), -- C3-M5 (Confirmé)
(14, 3, 'SENIOR',         '2020-01-01 00:00:00'), -- C3-M6 (Confirmé)
(15, 3, 'SENIOR',         '2020-01-01 00:00:00'), -- C3-M7 (Confirmé)
(16, 3, 'SENIOR',         '2020-01-01 00:00:00')  -- C3-M8 (Confirmé)

ON CONFLICT DO NOTHING;

-- =============================================================================
-- PARRAINAGES (member_referee)
-- UNIQUE(id_candidate, id_referee) → une paire ne peut apparaître qu'une fois
-- Les membres 1 & 2 sont fondateurs → pas de parrains ("Aucun")
-- id_collectivity = collectivité dans laquelle le candidat a été parrainé
-- =============================================================================

INSERT INTO member_referee (id_candidate, id_referee, id_collectivity, relationship)
VALUES
-- ── Collectivité 1 ───────────────────────────────────────────────────────────
-- C1-M3 parrainé par C1-M1 et C1-M2
(3,  1, 1, 'Parrain'),
(3,  2, 1, 'Parrain'),
-- C1-M4 parrainé par C1-M1 et C1-M2
(4,  1, 1, 'Parrain'),
(4,  2, 1, 'Parrain'),
-- C1-M5 parrainé par C1-M1 et C1-M2
(5,  1, 1, 'Parrain'),
(5,  2, 1, 'Parrain'),
-- C1-M6 parrainé par C1-M1 et C1-M2
(6,  1, 1, 'Parrain'),
(6,  2, 1, 'Parrain'),
-- C1-M7 parrainé par C1-M1 et C1-M2
(7,  1, 1, 'Parrain'),
(7,  2, 1, 'Parrain'),
-- C1-M8 parrainé par C1-M6 et C1-M7
(8,  6, 1, 'Parrain'),
(8,  7, 1, 'Parrain'),

-- ── Collectivité 3 ───────────────────────────────────────────────────────────
-- C3-M1 parrainé par C1-M1 et C1-M2
(9,  1, 3, 'Parrain'),
(9,  2, 3, 'Parrain'),
-- C3-M2 parrainé par C1-M1 et C1-M2
(10, 1, 3, 'Parrain'),
(10, 2, 3, 'Parrain'),
-- C3-M3 parrainé par C3-M1(=9) et C3-M2(=10)
(11, 9,  3, 'Parrain'),
(11, 10, 3, 'Parrain'),
-- C3-M4 parrainé par C3-M1(=9) et C3-M2(=10)
(12, 9,  3, 'Parrain'),
(12, 10, 3, 'Parrain'),
-- C3-M5 parrainé par C3-M1(=9) et C3-M2(=10)
(13, 9,  3, 'Parrain'),
(13, 10, 3, 'Parrain'),
-- C3-M6 parrainé par C3-M1(=9) et C3-M2(=10)
(14, 9,  3, 'Parrain'),
(14, 10, 3, 'Parrain'),
-- C3-M7 parrainé par C3-M1(=9) et C3-M2(=10)
(15, 9,  3, 'Parrain'),
(15, 10, 3, 'Parrain'),
-- C3-M8 parrainé par C3-M1(=9) et C3-M2(=10)
(16, 9,  3, 'Parrain'),
(16, 10, 3, 'Parrain')

ON CONFLICT (id_candidate, id_referee) DO NOTHING;

-- =============================================================================
-- COTISATIONS  (Tables 5, 6, 7 – pages 15)
-- cot-1 → id=1, collectivité 1, 100 000 Ar, annuelle
-- cot-2 → id=2, collectivité 2, 100 000 Ar, annuelle
-- cot-3 → id=3, collectivité 3,  50 000 Ar, annuelle
-- =============================================================================

INSERT INTO membership_fee (id, id_collectivity, label, frequency, amount, eligible_from, is_active)
VALUES
    (1, 1, 'Cotisation annuelle', 'ANNUALLY', 100000.00, '2026-01-01', true),  -- cot-1
    (2, 2, 'Cotisation annuelle', 'ANNUALLY', 100000.00, '2026-01-01', true),  -- cot-2
    (3, 3, 'Cotisation annuelle', 'ANNUALLY',  50000.00, '2026-01-01', true)   -- cot-3
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- COMPTES (page 16)
-- Collectivité 1 : C1-A-CASH (id=1), C1-A-MOBILE-1 ORANGE_MONEY (id=2)
-- Collectivité 2 : C2-A-CASH (id=3), C2-A-MOBILE-1 ORANGE_MONEY (id=4)
-- Collectivité 3 : C3-A-CASH (id=5)
-- Balances calculées à partir des transactions (voir ci-dessous)
--   C1-A-CASH     : 100k×6 + 60k + 90k = 750 000
--   C1-A-MOBILE-1 : 0
--   C2-A-CASH     : 60k + 90k + 100k×4  = 550 000
--   C2-A-MOBILE-1 : 40k + 60k            = 100 000
--   C3-A-CASH     : 0 (aucun paiement)
-- =============================================================================

INSERT INTO account (id, id_collectivity, id_federation, balance)
VALUES
    (1, 1, NULL, 750000.00),   -- C1-A-CASH
    (2, 1, NULL,      0.00),   -- C1-A-MOBILE-1
    (3, 2, NULL, 550000.00),   -- C2-A-CASH
    (4, 2, NULL, 100000.00),   -- C2-A-MOBILE-1
    (5, 3, NULL,      0.00)    -- C3-A-CASH
ON CONFLICT (id) DO NOTHING;

-- Sous-tables de comptes
INSERT INTO cash_account (id_account)
VALUES (1), (3), (5)
ON CONFLICT (id_account) DO NOTHING;

INSERT INTO mobile_money_account (id_account, holder_name, service_name, phone_number)
VALUES
    (2, 'Mpanorina',      'ORANGE_MONEY', '0370489612'),  -- C1-A-MOBILE-1
    (4, 'Dobo voalohany', 'ORANGE_MONEY', '0320489612')   -- C2-A-MOBILE-1
ON CONFLICT (id_account) DO NOTHING;

-- =============================================================================
-- TRANSACTIONS  (Tables 8-11, pages 17-20)
-- Toutes datées du 01/01/2026
-- Collectivité 3 : aucune transaction
-- =============================================================================

INSERT INTO transaction (id, id_member, id_collectivity, id_membership_fee,
                         id_account, transaction_type, amount, transaction_date, payment_mode)
VALUES
-- ── Collectivité 1 (Table 9, page 18) → tout vers C1-A-CASH (account id=1) ──
(1,  1, 1, 1, 1, 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),  -- C1-M1
(2,  2, 1, 1, 1, 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),  -- C1-M2
(3,  3, 1, 1, 1, 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),  -- C1-M3
(4,  4, 1, 1, 1, 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),  -- C1-M4
(5,  5, 1, 1, 1, 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),  -- C1-M5
(6,  6, 1, 1, 1, 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),  -- C1-M6
(7,  7, 1, 1, 1, 'IN',  60000.00, '2026-01-01 00:00:00', 'CASH'),  -- C1-M7
(8,  8, 1, 1, 1, 'IN',  90000.00, '2026-01-01 00:00:00', 'CASH'),  -- C1-M8

-- ── Collectivité 2 (Table 11, page 20) ───────────────────────────────────────
-- Membres 1-6 → C2-A-CASH (account id=3), CASH
(9,  1, 2, 2, 3, 'IN',  60000.00, '2026-01-01 00:00:00', 'CASH'),  -- C2-M1
(10, 2, 2, 2, 3, 'IN',  90000.00, '2026-01-01 00:00:00', 'CASH'),  -- C2-M2
(11, 3, 2, 2, 3, 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),  -- C2-M3
(12, 4, 2, 2, 3, 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),  -- C2-M4
(13, 5, 2, 2, 3, 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),  -- C2-M5
(14, 6, 2, 2, 3, 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),  -- C2-M6
-- Membres 7-8 → C2-A-MOBILE-1 (account id=4), MOBILE_BANKING
(15, 7, 2, 2, 4, 'IN',  40000.00, '2026-01-01 00:00:00', 'MOBILE_BANKING'),  -- C2-M7
(16, 8, 2, 2, 4, 'IN',  60000.00, '2026-01-01 00:00:00', 'MOBILE_BANKING')   -- C2-M8

ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- RESET DES SÉQUENCES
-- =============================================================================

SELECT setval('member_id_seq',         (SELECT MAX(id) FROM member));
SELECT setval('collectivity_id_seq',   (SELECT MAX(id) FROM collectivity));
SELECT setval('federation_id_seq',     (SELECT MAX(id) FROM federation));
SELECT setval('account_id_seq',        (SELECT MAX(id) FROM account));
SELECT setval('membership_fee_id_seq', (SELECT MAX(id) FROM membership_fee));
SELECT setval('transaction_id_seq',    (SELECT MAX(id) FROM transaction));