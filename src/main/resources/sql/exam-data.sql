-- =============================================================================
-- DONNÉES DE TEST – ÉVALUATION DU MERCREDI 6 MAI 2026
-- Conformément aux consignes (pages 24-29 du sujet) et au schéma scheme-gh.sql
--
-- ÉTAPE 1 : nettoyage complet de la base
-- ÉTAPE 2 : ré-insertion des données de base conservées
-- ÉTAPE 3 : insertion des nouvelles données
-- =============================================================================

-- ─── ÉTAPE 1 : Nettoyage (ordre inverse des FK) ───────────────────────────────
TRUNCATE TABLE activity_attendance           CASCADE;
TRUNCATE TABLE activity_occupation_concerned CASCADE;
TRUNCATE TABLE collectivity_activity         CASCADE;
TRUNCATE TABLE "transaction"                 CASCADE;
TRUNCATE TABLE membership_fee                CASCADE;
TRUNCATE TABLE mobile_money_account          CASCADE;
TRUNCATE TABLE bank_account                  CASCADE;
TRUNCATE TABLE cash_account                  CASCADE;
TRUNCATE TABLE account                       CASCADE;
TRUNCATE TABLE member_referee                CASCADE;
TRUNCATE TABLE member_collectivity           CASCADE;
TRUNCATE TABLE collectivity                  CASCADE;
TRUNCATE TABLE mandate_federation            CASCADE;
TRUNCATE TABLE member                        CASCADE;
TRUNCATE TABLE federation                    CASCADE;

-- =============================================================================
-- ÉTAPE 2 : Ré-insertion des données de base
-- =============================================================================

-- ─── Fédération ───────────────────────────────────────────────────────────────
INSERT INTO federation (id, cotisation_percentage)
VALUES ('fed-1', 10.00);

-- =============================================================================
-- MEMBRES (16 membres originaux)
-- Conformément aux tableaux 2, 3 et 4 du sujet
-- enrolment_date mis à jour à 01/01/2026 comme demandé
-- =============================================================================
INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date,
                    address, email, phone_number, profession, gender)
VALUES
-- ── Collectivités 1 & 2 (mêmes personnes) ────────────────────────────────────
('C1-M1', 'Prenom membre 1',  'Nom membre 1',  '1980-02-01', '2026-01-01 00:00:00',
 'Lot II V M Ambato.',   'member.1@fed-agri.mg',  '0341234567',  'Riziculteur',  'MALE'),

('C1-M2', 'Prenom membre 2',  'Nom membre 2',  '1982-03-05', '2026-01-01 00:00:00',
 'Lot II F Ambato.',     'member.2@fed-agri.mg',  '0321234567',  'Agriculteur',  'MALE'),

('C1-M3', 'Prenom membre 3',  'Nom membre 3',  '1992-03-10', '2026-01-01 00:00:00',
 'Lot II J Ambato.',     'member.3@fed-agri.mg',  '0331234567',  'Collecteur',   'MALE'),

('C1-M4', 'Prenom membre 4',  'Nom membre 4',  '1988-05-22', '2026-01-01 00:00:00',
 'Lot A K 50 Ambato.',   'member.4@fed-agri.mg',  '0381234567',  'Distributeur', 'FEMALE'),

('C1-M5', 'Prenom membre 5',  'Nom membre 5',  '1999-08-21', '2026-01-01 00:00:00',
 'Lot UV 80 Ambato.',    'member.5@fed-agri.mg',  '0373434567',  'Riziculteur',  'MALE'),

('C1-M6', 'Prenom membre 6',  'Nom membre 6',  '1998-08-22', '2026-01-01 00:00:00',
 'Lot UV 6 Ambato.',     'member.6@fed-agri.mg',  '0372234567',  'Riziculteur',  'FEMALE'),

('C1-M7', 'Prenom membre 7',  'Nom membre 7',  '1998-01-31', '2026-01-01 00:00:00',
 'Lot UV 7 Ambato.',     'member.7@fed-agri.mg',  '0374234567',  'Riziculteur',  'MALE'),

('C1-M8', 'Prenom membre 8',  'Nom membre 8',  '1975-08-20', '2026-01-01 00:00:00',
 'Lot UV 8 Ambato.',     'member.8@fed-agri.mg',  '0370234567',  'Riziculteur',  'MALE'),

-- ── Collectivité 3 uniquement ─────────────────────────────────────────────────
('C3-M1', 'Prenom membre 9',  'Nom membre 9',  '1988-01-02', '2026-01-01 00:00:00',
 'Lot 33 J Antsirabe',   'member.9@fed-agri.mg',  '034034567',   'Apiculteur',   'MALE'),

('C3-M2', 'Prenom membre 10', 'Nom membre 10', '1982-03-05', '2026-01-01 00:00:00',
 'Lot 2 J Antsirabe',    'member.10@fed-agri.mg', '0338634567',  'Agriculteur',  'MALE'),

('C3-M3', 'Prenom membre 11', 'Nom membre 11', '1992-03-12', '2026-01-01 00:00:00',
 'Lot 8 KM Antsirabe',   'member.11@fed-agri.mg', '0338234567',  'Collecteur',   'MALE'),

('C3-M4', 'Prenom membre 12', 'Nom membre 12', '1988-05-10', '2026-01-01 00:00:00',
 'Lot A K 50 Antsirabe', 'member.12@fed-agri.mg', '0382334567',  'Distributeur', 'FEMALE'),

('C3-M5', 'Prenom membre 13', 'Nom membre 13', '1999-08-11', '2026-01-01 00:00:00',
 'Lot UV 80 Antsirabe',  'member.13@fed-agri.mg', '0373365567',  'Apiculteur',   'MALE'),

('C3-M6', 'Prenom membre 14', 'Nom membre 14', '1998-08-09', '2026-01-01 00:00:00',
 'Lot UV 6 Antsirabe',   'member.14@fed-agri.mg', '0378234567',  'Apiculteur',   'FEMALE'),

('C3-M7', 'Prenom membre 15', 'Nom membre 15', '1998-01-13', '2026-01-01 00:00:00',
 'Lot UV 7 Antsirabe',   'member.15@fed-agri.mg', '0374914567',  'Apiculteur',   'MALE'),

('C3-M8', 'Prenom membre 16', 'Nom membre 16', '1975-08-02', '2026-01-01 00:00:00',
 'Lot UV 8 Antsirabe',   'member.16@fed-agri.mg', '0370634567',  'Apiculteur',   'MALE');

-- =============================================================================
-- COLLECTIVITÉS (Tableau 1)
-- =============================================================================
INSERT INTO collectivity (id, number, name, speciality, creation_datetime,
                          federation_approval, authorization_date, id_federation, location)
VALUES
    ('col-1', '1', 'Mpanorina',      'Riziculture', '2020-01-01 00:00:00',
     TRUE, '2020-01-01 00:00:00', 'fed-1', 'Ambatondrazaka'),

    ('col-2', '2', 'Dobo voalohany', 'Pisciculture','2020-01-01 00:00:00',
     TRUE, '2020-01-01 00:00:00', 'fed-1', 'Ambatondrazaka'),

    ('col-3', '3', 'Tantely mamy',   'Apiculture',  '2020-01-01 00:00:00',
     TRUE, '2020-01-01 00:00:00', 'fed-1', 'Brickaville');

-- =============================================================================
-- MEMBRE ↔ COLLECTIVITÉ (Tableaux 2, 3, 4)
-- =============================================================================
INSERT INTO member_collectivity (id, id_member, id_collectivity, occupation, start_date)
VALUES
-- ── Collectivité 1 ────────────────────────────────────────────────────────────
('mc-c1-1', 'C1-M1', 'col-1', 'PRESIDENT',      '2026-01-01 00:00:00'),
('mc-c1-2', 'C1-M2', 'col-1', 'VICE_PRESIDENT', '2026-01-01 00:00:00'),
('mc-c1-3', 'C1-M3', 'col-1', 'SECRETARY',      '2026-01-01 00:00:00'),
('mc-c1-4', 'C1-M4', 'col-1', 'TREASURER',      '2026-01-01 00:00:00'),
('mc-c1-5', 'C1-M5', 'col-1', 'SENIOR',         '2026-01-01 00:00:00'),
('mc-c1-6', 'C1-M6', 'col-1', 'SENIOR',         '2026-01-01 00:00:00'),
('mc-c1-7', 'C1-M7', 'col-1', 'SENIOR',         '2026-01-01 00:00:00'),
('mc-c1-8', 'C1-M8', 'col-1', 'SENIOR',         '2026-01-01 00:00:00'),

-- ── Collectivité 2 ────────────────────────────────────────────────────────────
('mc-c2-1', 'C1-M1', 'col-2', 'SENIOR',         '2026-01-01 00:00:00'),
('mc-c2-2', 'C1-M2', 'col-2', 'SENIOR',         '2026-01-01 00:00:00'),
('mc-c2-3', 'C1-M3', 'col-2', 'SENIOR',         '2026-01-01 00:00:00'),
('mc-c2-4', 'C1-M4', 'col-2', 'SENIOR',         '2026-01-01 00:00:00'),
('mc-c2-5', 'C1-M5', 'col-2', 'PRESIDENT',      '2026-01-01 00:00:00'),
('mc-c2-6', 'C1-M6', 'col-2', 'VICE_PRESIDENT', '2026-01-01 00:00:00'),
('mc-c2-7', 'C1-M7', 'col-2', 'SECRETARY',      '2026-01-01 00:00:00'),
('mc-c2-8', 'C1-M8', 'col-2', 'TREASURER',      '2026-01-01 00:00:00'),

-- ── Collectivité 3 ────────────────────────────────────────────────────────────
('mc-c3-1', 'C3-M1', 'col-3', 'PRESIDENT',      '2026-01-01 00:00:00'),
('mc-c3-2', 'C3-M2', 'col-3', 'VICE_PRESIDENT', '2026-01-01 00:00:00'),
('mc-c3-3', 'C3-M3', 'col-3', 'SECRETARY',      '2026-01-01 00:00:00'),
('mc-c3-4', 'C3-M4', 'col-3', 'TREASURER',      '2026-01-01 00:00:00'),
('mc-c3-5', 'C3-M5', 'col-3', 'SENIOR',         '2026-01-01 00:00:00'),
('mc-c3-6', 'C3-M6', 'col-3', 'SENIOR',         '2026-01-01 00:00:00'),
('mc-c3-7', 'C3-M7', 'col-3', 'SENIOR',         '2026-01-01 00:00:00'),
('mc-c3-8', 'C3-M8', 'col-3', 'SENIOR',         '2026-01-01 00:00:00');

-- =============================================================================
-- PARRAINAGES (member_referee) – données de base conservées
-- =============================================================================
INSERT INTO member_referee (id, id_candidate, id_referee, id_collectivity, relationship)
VALUES
-- ── Collectivité 1 ────────────────────────────────────────────────────────────
('mr-1',  'C1-M3', 'C1-M1', 'col-1', 'Parrain'),
('mr-2',  'C1-M3', 'C1-M2', 'col-1', 'Parrain'),
('mr-3',  'C1-M4', 'C1-M1', 'col-1', 'Parrain'),
('mr-4',  'C1-M4', 'C1-M2', 'col-1', 'Parrain'),
('mr-5',  'C1-M5', 'C1-M1', 'col-1', 'Parrain'),
('mr-6',  'C1-M5', 'C1-M2', 'col-1', 'Parrain'),
('mr-7',  'C1-M6', 'C1-M1', 'col-1', 'Parrain'),
('mr-8',  'C1-M6', 'C1-M2', 'col-1', 'Parrain'),
('mr-9',  'C1-M7', 'C1-M1', 'col-1', 'Parrain'),
('mr-10', 'C1-M7', 'C1-M2', 'col-1', 'Parrain'),
('mr-11', 'C1-M8', 'C1-M6', 'col-1', 'Parrain'),
('mr-12', 'C1-M8', 'C1-M7', 'col-1', 'Parrain'),

-- ── Collectivité 3 ────────────────────────────────────────────────────────────
('mr-13', 'C3-M1', 'C1-M1', 'col-3', 'Parrain'),
('mr-14', 'C3-M1', 'C1-M2', 'col-3', 'Parrain'),
('mr-15', 'C3-M2', 'C1-M1', 'col-3', 'Parrain'),
('mr-16', 'C3-M2', 'C1-M2', 'col-3', 'Parrain'),
('mr-17', 'C3-M3', 'C3-M1', 'col-3', 'Parrain'),
('mr-18', 'C3-M3', 'C3-M2', 'col-3', 'Parrain'),
('mr-19', 'C3-M4', 'C3-M1', 'col-3', 'Parrain'),
('mr-20', 'C3-M4', 'C3-M2', 'col-3', 'Parrain'),
('mr-21', 'C3-M5', 'C3-M1', 'col-3', 'Parrain'),
('mr-22', 'C3-M5', 'C3-M2', 'col-3', 'Parrain'),
('mr-23', 'C3-M6', 'C3-M1', 'col-3', 'Parrain'),
('mr-24', 'C3-M6', 'C3-M2', 'col-3', 'Parrain'),
('mr-25', 'C3-M7', 'C3-M1', 'col-3', 'Parrain'),
('mr-26', 'C3-M7', 'C3-M2', 'col-3', 'Parrain'),
('mr-27', 'C3-M8', 'C3-M1', 'col-3', 'Parrain'),
('mr-28', 'C3-M8', 'C3-M2', 'col-3', 'Parrain');

-- =============================================================================
-- COMPTES FINANCIERS DE BASE (solde initial = 0)
-- Collectivités 1 et 2 : cash + mobile money
-- Collectivité 3 : uniquement la caisse de base
-- =============================================================================

-- ── Comptes parents (table account) ──────────────────────────────────────────
-- Soldes calculés à partir de toutes les transactions insérées ci-dessous :
--   C1-A-CASH     : 200k+200k+100k+60k+90k          = 650 000
--   C1-A-MOBILE-1 : 200k+200k+150k                  = 550 000
--   C2-A-CASH     : 120k+180k+200k+200k+200k+200k   = 1 100 000
--   C2-A-MOBILE-1 : 80k+120k                        = 200 000
--   C3-A-CASH     : 25k+25k+5k+5k                   = 60 000
--   C3-A-BANK-1   : 25k×4(avr)+25k×2(mai)           = 150 000
--   C3-A-BANK-2   : 25k×2(avr)+20k+25k(mai)         = 95 000
--   C3-A-MOBILE-1 : 15k+15k                         = 30 000
INSERT INTO account (id, id_collectivity, id_federation, balance)
VALUES
    ('C1-A-CASH',     'col-1', NULL,    650000.00),
    ('C1-A-MOBILE-1', 'col-1', NULL,    550000.00),
    ('C2-A-CASH',     'col-2', NULL,   1100000.00),
    ('C2-A-MOBILE-1', 'col-2', NULL,    200000.00),
    ('C3-A-CASH',     'col-3', NULL,     60000.00),
    ('C3-A-BANK-1',   'col-3', NULL,    150000.00),
    ('C3-A-BANK-2',   'col-3', NULL,     95000.00),
    ('C3-A-MOBILE-1', 'col-3', NULL,     30000.00);

-- ── Caisses ───────────────────────────────────────────────────────────────────
INSERT INTO cash_account (id, id_account)
VALUES
    ('ca-1', 'C1-A-CASH'),
    ('ca-2', 'C2-A-CASH'),
    ('ca-3', 'C3-A-CASH');

-- ── Comptes mobile money ──────────────────────────────────────────────────────
INSERT INTO mobile_money_account (id, id_account, holder_name, service_name, phone_number)
VALUES
    ('mma-1', 'C1-A-MOBILE-1', 'Mpanorina',      'ORANGE_MONEY', '0370489612'),
    ('mma-2', 'C2-A-MOBILE-1', 'Dobo voalohany', 'ORANGE_MONEY', '0320489612');

-- =============================================================================
-- ÉTAPE 3 : Nouvelles données
-- =============================================================================

-- =============================================================================
-- 1) Nouveaux comptes bancaires et mobile money pour la collectivité 3
-- (page 24 du sujet)
-- NB : account_number est CHAR(11) → padding à 11 caractères par zéro de tête
-- =============================================================================

-- ── Compte bancaire BMOI ──────────────────────────────────────────────────────
INSERT INTO bank_account (id, id_account, holder_name, bank_name,
                          bank_code, branch_code, account_number, rib_key)
VALUES
    ('ba-1', 'C3-A-BANK-1', 'Koto',  'BMOI', '00004', '00001', '01234567890', '12'),
    ('ba-2', 'C3-A-BANK-2', 'Naivo', 'BRED', '00008', '00003', '04567890123', '58');

-- ── Compte mobile money MVOLA ─────────────────────────────────────────────────
INSERT INTO mobile_money_account (id, id_account, holder_name, service_name, phone_number)
VALUES
    ('mma-3', 'C3-A-MOBILE-1', 'Kolo', 'MVOLA', '0341889612');

-- =============================================================================
-- 2) Cotisations (Tableaux 12, 13, 14 – pages 25 du sujet)
-- =============================================================================

-- a) Collectivité 1 – Tableau 12
INSERT INTO membership_fee (id, id_collectivity, label, frequency, amount, eligible_from, is_active)
VALUES
    ('cot-1', 'col-1', 'Cotisation annuelle', 'ANNUALLY',   200000.00, '2026-01-01', TRUE),
    ('cot-2', 'col-1', 'Famangiana',          'PUNCTUALLY',  20000.00, '2026-04-30', TRUE);

-- b) Collectivité 2 – Tableau 13
INSERT INTO membership_fee (id, id_collectivity, label, frequency, amount, eligible_from, is_active)
VALUES
    ('cot-3', 'col-2', 'Cotisation annuelle', 'ANNUALLY', 200000.00, '2026-01-01', TRUE),
    ('cot-4', 'col-2', 'Cotisation 2025',     'ANNUALLY', 100000.00, '2025-01-01', FALSE);

-- c) Collectivité 3 – Tableau 14
INSERT INTO membership_fee (id, id_collectivity, label, frequency, amount, eligible_from, is_active)
VALUES
    ('cot-5', 'col-3', 'Cotisation mensuelle', 'MONTHLY', 25000.00, '2026-04-01', TRUE);

-- =============================================================================
-- 3) Transactions / Paiements
-- Les paiements enregistrés génèrent chacun une transaction de type IN.
-- =============================================================================

-- ── Collectivité 1 – Tableau 15 (page 26) ────────────────────────────────────
INSERT INTO "transaction" (id, id_member, id_collectivity, id_membership_fee,
                           id_account, transaction_type, amount, transaction_date, payment_mode)
VALUES
    ('tx-c1-1', 'C1-M1', 'col-1', 'cot-1', 'C1-A-CASH',     'IN', 200000.00, '2026-01-01 00:00:00', 'CASH'),
    ('tx-c1-2', 'C1-M2', 'col-1', 'cot-1', 'C1-A-CASH',     'IN', 200000.00, '2026-01-01 00:00:00', 'CASH'),
    ('tx-c1-3', 'C1-M3', 'col-1', 'cot-1', 'C1-A-MOBILE-1', 'IN', 200000.00, '2026-01-01 00:00:00', 'MOBILE_BANKING'),
    ('tx-c1-4', 'C1-M4', 'col-1', 'cot-1', 'C1-A-MOBILE-1', 'IN', 200000.00, '2026-01-01 00:00:00', 'MOBILE_BANKING'),
    ('tx-c1-5', 'C1-M5', 'col-1', 'cot-1', 'C1-A-MOBILE-1', 'IN', 150000.00, '2026-01-01 00:00:00', 'MOBILE_BANKING'),
    ('tx-c1-6', 'C1-M6', 'col-1', 'cot-1', 'C1-A-CASH',     'IN', 100000.00, '2026-05-01 00:00:00', 'CASH'),
    ('tx-c1-7', 'C1-M7', 'col-1', 'cot-1', 'C1-A-CASH',     'IN',  60000.00, '2026-05-01 00:00:00', 'CASH'),
    ('tx-c1-8', 'C1-M8', 'col-1', 'cot-1', 'C1-A-CASH',     'IN',  90000.00, '2026-05-01 00:00:00', 'CASH');

-- ── Collectivité 2 – Tableau 16 (page 26) ────────────────────────────────────
INSERT INTO "transaction" (id, id_member, id_collectivity, id_membership_fee,
                           id_account, transaction_type, amount, transaction_date, payment_mode)
VALUES
    ('tx-c2-1', 'C1-M1', 'col-2', 'cot-3', 'C2-A-CASH',     'IN', 120000.00, '2026-01-01 00:00:00', 'CASH'),
    ('tx-c2-2', 'C1-M2', 'col-2', 'cot-3', 'C2-A-CASH',     'IN', 180000.00, '2026-01-01 00:00:00', 'CASH'),
    ('tx-c2-3', 'C1-M3', 'col-2', 'cot-3', 'C2-A-CASH',     'IN', 200000.00, '2026-01-01 00:00:00', 'CASH'),
    ('tx-c2-4', 'C1-M4', 'col-2', 'cot-3', 'C2-A-CASH',     'IN', 200000.00, '2026-01-01 00:00:00', 'CASH'),
    ('tx-c2-5', 'C1-M5', 'col-2', 'cot-3', 'C2-A-CASH',     'IN', 200000.00, '2026-01-01 00:00:00', 'CASH'),
    ('tx-c2-6', 'C1-M6', 'col-2', 'cot-3', 'C2-A-CASH',     'IN', 200000.00, '2026-01-01 00:00:00', 'CASH'),
    ('tx-c2-7', 'C1-M7', 'col-2', 'cot-3', 'C2-A-MOBILE-1', 'IN',  80000.00, '2026-01-01 00:00:00', 'MOBILE_BANKING'),
    ('tx-c2-8', 'C1-M8', 'col-2', 'cot-3', 'C2-A-MOBILE-1', 'IN', 120000.00, '2026-01-01 00:00:00', 'MOBILE_BANKING');

-- ── Collectivité 3 – Tableau 17 (page 27) ────────────────────────────────────
-- Mois d'avril 2026
INSERT INTO "transaction" (id, id_member, id_collectivity, id_membership_fee,
                           id_account, transaction_type, amount, transaction_date, payment_mode)
VALUES
    ('tx-c3-apr-1', 'C3-M1', 'col-3', 'cot-5', 'C3-A-BANK-1',   'IN', 25000.00, '2026-04-01 00:00:00', 'BANK_TRANSFER'),
    ('tx-c3-apr-2', 'C3-M2', 'col-3', 'cot-5', 'C3-A-BANK-1',   'IN', 25000.00, '2026-04-01 00:00:00', 'BANK_TRANSFER'),
    ('tx-c3-apr-3', 'C3-M3', 'col-3', 'cot-5', 'C3-A-BANK-1',   'IN', 25000.00, '2026-04-01 00:00:00', 'BANK_TRANSFER'),
    ('tx-c3-apr-4', 'C3-M4', 'col-3', 'cot-5', 'C3-A-BANK-1',   'IN', 25000.00, '2026-04-01 00:00:00', 'BANK_TRANSFER'),
    ('tx-c3-apr-5', 'C3-M5', 'col-3', 'cot-5', 'C3-A-BANK-2',   'IN', 25000.00, '2026-04-01 00:00:00', 'BANK_TRANSFER'),
    ('tx-c3-apr-6', 'C3-M6', 'col-3', 'cot-5', 'C3-A-BANK-2',   'IN', 25000.00, '2026-04-01 00:00:00', 'BANK_TRANSFER'),
    ('tx-c3-apr-7', 'C3-M7', 'col-3', 'cot-5', 'C3-A-CASH',     'IN', 25000.00, '2026-04-01 00:00:00', 'CASH'),
    ('tx-c3-apr-8', 'C3-M8', 'col-3', 'cot-5', 'C3-A-CASH',     'IN', 25000.00, '2026-04-01 00:00:00', 'CASH');

-- Mois de mai 2026
-- NB : C3-M3 et C3-M4 paient via C3-A-MOBILE-1 (MOBILE_BANKING) en mai
INSERT INTO "transaction" (id, id_member, id_collectivity, id_membership_fee,
                           id_account, transaction_type, amount, transaction_date, payment_mode)
VALUES
    ('tx-c3-may-1', 'C3-M1', 'col-3', 'cot-5', 'C3-A-BANK-1',   'IN', 25000.00, '2026-05-01 00:00:00', 'BANK_TRANSFER'),
    ('tx-c3-may-2', 'C3-M2', 'col-3', 'cot-5', 'C3-A-BANK-1',   'IN', 25000.00, '2026-05-01 00:00:00', 'BANK_TRANSFER'),
    ('tx-c3-may-3', 'C3-M3', 'col-3', 'cot-5', 'C3-A-MOBILE-1', 'IN', 15000.00, '2026-05-01 00:00:00', 'MOBILE_BANKING'),
    ('tx-c3-may-4', 'C3-M4', 'col-3', 'cot-5', 'C3-A-MOBILE-1', 'IN', 15000.00, '2026-05-01 00:00:00', 'MOBILE_BANKING'),
    ('tx-c3-may-5', 'C3-M5', 'col-3', 'cot-5', 'C3-A-BANK-2',   'IN', 20000.00, '2026-05-01 00:00:00', 'BANK_TRANSFER'),
    ('tx-c3-may-6', 'C3-M6', 'col-3', 'cot-5', 'C3-A-BANK-2',   'IN', 25000.00, '2026-05-01 00:00:00', 'BANK_TRANSFER'),
    ('tx-c3-may-7', 'C3-M7', 'col-3', 'cot-5', 'C3-A-CASH',     'IN',  5000.00, '2026-05-01 00:00:00', 'CASH'),
    ('tx-c3-may-8', 'C3-M8', 'col-3', 'cot-5', 'C3-A-CASH',     'IN',  5000.00, '2026-05-01 00:00:00', 'CASH');

-- =============================================================================
-- 4) Nouveaux membres adhérents (Tableaux 18, 19, 20 – pages 28-29)
-- Les données marquées <random> peuvent être remplies librement.
-- Les informations critiques (occupation, parrains, date d'adhésion) sont fixes.
-- =============================================================================

-- ── Nouveaux membres – Collectivité 1 (Tableau 18) ───────────────────────────
INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date,
                    address, email, phone_number, profession, gender)
VALUES
    ('C1-M9',  'Prenom new1-c1', 'Nom new1-c1', '2000-03-15', '2026-04-01 00:00:00',
     'Lot XX Ambato.', 'new1.c1@fed-agri.mg', '0340000001', 'Agriculteur', 'MALE'),
    ('C1-M10', 'Prenom new2-c1', 'Nom new2-c1', '2001-06-20', '2026-04-01 00:00:00',
     'Lot XY Ambato.', 'new2.c1@fed-agri.mg', '0340000002', 'Agriculteur', 'FEMALE'),
    ('C1-M11', 'Prenom new3-c1', 'Nom new3-c1', '1999-11-05', '2026-05-01 00:00:00',
     'Lot XZ Ambato.', 'new3.c1@fed-agri.mg', '0340000003', 'Riziculteur', 'MALE'),
    ('C1-M12', 'Prenom new4-c1', 'Nom new4-c1', '2002-01-10', '2026-06-01 00:00:00',
     'Lot YA Ambato.', 'new4.c1@fed-agri.mg', '0340000004', 'Riziculteur', 'MALE');

INSERT INTO member_collectivity (id, id_member, id_collectivity, occupation, start_date)
VALUES
    ('mc-c1-9',  'C1-M9',  'col-1', 'JUNIOR', '2026-04-01 00:00:00'),
    ('mc-c1-10', 'C1-M10', 'col-1', 'JUNIOR', '2026-04-01 00:00:00'),
    ('mc-c1-11', 'C1-M11', 'col-1', 'JUNIOR', '2026-05-01 00:00:00'),
    ('mc-c1-12', 'C1-M12', 'col-1', 'JUNIOR', '2026-06-01 00:00:00');

INSERT INTO member_referee (id, id_candidate, id_referee, id_collectivity, relationship)
VALUES
    ('mr-c1-n1a', 'C1-M9',  'C1-M1', 'col-1', 'Parrain'),
    ('mr-c1-n1b', 'C1-M9',  'C1-M2', 'col-1', 'Parrain'),
    ('mr-c1-n2a', 'C1-M10', 'C1-M1', 'col-1', 'Parrain'),
    ('mr-c1-n2b', 'C1-M10', 'C1-M2', 'col-1', 'Parrain'),
    ('mr-c1-n3a', 'C1-M11', 'C1-M1', 'col-1', 'Parrain'),
    ('mr-c1-n3b', 'C1-M11', 'C1-M2', 'col-1', 'Parrain'),
    ('mr-c1-n4a', 'C1-M12', 'C1-M1', 'col-1', 'Parrain'),
    ('mr-c1-n4b', 'C1-M12', 'C1-M2', 'col-1', 'Parrain');

-- ── Nouveaux membres – Collectivité 2 (Tableau 19) ───────────────────────────
INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date,
                    address, email, phone_number, profession, gender)
VALUES
    ('C2-M9',  'Prenom new1-c2', 'Nom new1-c2', '2000-07-12', '2026-03-01 00:00:00',
     'Lot XX Ambato.', 'new1.c2@fed-agri.mg', '0340000011', 'Pisciculteur', 'MALE'),
    ('C2-M10', 'Prenom new2-c2', 'Nom new2-c2', '1998-02-28', '2026-03-01 00:00:00',
     'Lot XY Ambato.', 'new2.c2@fed-agri.mg', '0340000012', 'Pisciculteur', 'FEMALE'),
    ('C2-M11', 'Prenom new3-c2', 'Nom new3-c2', '2001-09-17', '2026-03-01 00:00:00',
     'Lot XZ Ambato.', 'new3.c2@fed-agri.mg', '0340000013', 'Agriculteur',  'MALE');

INSERT INTO member_collectivity (id, id_member, id_collectivity, occupation, start_date)
VALUES
    ('mc-c2-9',  'C2-M9',  'col-2', 'JUNIOR', '2026-03-01 00:00:00'),
    ('mc-c2-10', 'C2-M10', 'col-2', 'JUNIOR', '2026-03-01 00:00:00'),
    ('mc-c2-11', 'C2-M11', 'col-2', 'JUNIOR', '2026-03-01 00:00:00');

INSERT INTO member_referee (id, id_candidate, id_referee, id_collectivity, relationship)
VALUES
    ('mr-c2-n1a', 'C2-M9',  'C1-M1', 'col-2', 'Parrain'),
    ('mr-c2-n1b', 'C2-M9',  'C1-M2', 'col-2', 'Parrain'),
    ('mr-c2-n2a', 'C2-M10', 'C1-M1', 'col-2', 'Parrain'),
    ('mr-c2-n2b', 'C2-M10', 'C1-M2', 'col-2', 'Parrain'),
    ('mr-c2-n3a', 'C2-M11', 'C1-M1', 'col-2', 'Parrain'),
    ('mr-c2-n3b', 'C2-M11', 'C1-M2', 'col-2', 'Parrain');

-- ── Nouveaux membres – Collectivité 3 (Tableau 20) ───────────────────────────
INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date,
                    address, email, phone_number, profession, gender)
VALUES
    ('C3-M9',  'Prenom new1-c3', 'Nom new1-c3', '2000-05-08', '2026-01-01 00:00:00',
     'Lot AA Antsirabe', 'new1.c3@fed-agri.mg', '0340000021', 'Apiculteur', 'MALE'),
    ('C3-M10', 'Prenom new2-c3', 'Nom new2-c3', '2001-11-22', '2026-02-01 00:00:00',
     'Lot AB Antsirabe', 'new2.c3@fed-agri.mg', '0340000022', 'Apiculteur', 'FEMALE'),
    ('C3-M11', 'Prenom new3-c3', 'Nom new3-c3', '1999-04-30', '2026-02-01 00:00:00',
     'Lot AC Antsirabe', 'new3.c3@fed-agri.mg', '0340000023', 'Apiculteur', 'MALE'),
    ('C3-M12', 'Prenom new4-c3', 'Nom new4-c3', '2002-08-14', '2026-03-01 00:00:00',
     'Lot AD Antsirabe', 'new4.c3@fed-agri.mg', '0340000024', 'Apiculteur', 'MALE'),
    ('C3-M13', 'Prenom new5-c3', 'Nom new5-c3', '1997-12-03', '2026-03-01 00:00:00',
     'Lot AE Antsirabe', 'new5.c3@fed-agri.mg', '0340000025', 'Collecteur', 'FEMALE'),
    ('C3-M14', 'Prenom new6-c3', 'Nom new6-c3', '2000-07-19', '2026-03-01 00:00:00',
     'Lot AF Antsirabe', 'new6.c3@fed-agri.mg', '0340000026', 'Agriculteur','MALE');

INSERT INTO member_collectivity (id, id_member, id_collectivity, occupation, start_date)
VALUES
    ('mc-c3-9',  'C3-M9',  'col-3', 'JUNIOR', '2026-01-01 00:00:00'),
    ('mc-c3-10', 'C3-M10', 'col-3', 'JUNIOR', '2026-02-01 00:00:00'),
    ('mc-c3-11', 'C3-M11', 'col-3', 'JUNIOR', '2026-02-01 00:00:00'),
    ('mc-c3-12', 'C3-M12', 'col-3', 'JUNIOR', '2026-03-01 00:00:00'),
    ('mc-c3-13', 'C3-M13', 'col-3', 'JUNIOR', '2026-03-01 00:00:00'),
    ('mc-c3-14', 'C3-M14', 'col-3', 'JUNIOR', '2026-03-01 00:00:00');

INSERT INTO member_referee (id, id_candidate, id_referee, id_collectivity, relationship)
VALUES
    ('mr-c3-n1a', 'C3-M9',  'C3-M1', 'col-3', 'Parrain'),
    ('mr-c3-n1b', 'C3-M9',  'C3-M2', 'col-3', 'Parrain'),
    ('mr-c3-n2a', 'C3-M10', 'C3-M1', 'col-3', 'Parrain'),
    ('mr-c3-n2b', 'C3-M10', 'C3-M2', 'col-3', 'Parrain'),
    ('mr-c3-n3a', 'C3-M11', 'C3-M1', 'col-3', 'Parrain'),
    ('mr-c3-n3b', 'C3-M11', 'C3-M2', 'col-3', 'Parrain'),
    ('mr-c3-n4a', 'C3-M12', 'C3-M1', 'col-3', 'Parrain'),
    ('mr-c3-n4b', 'C3-M12', 'C3-M2', 'col-3', 'Parrain'),
    ('mr-c3-n5a', 'C3-M13', 'C3-M1', 'col-3', 'Parrain'),
    ('mr-c3-n5b', 'C3-M13', 'C3-M2', 'col-3', 'Parrain'),
    ('mr-c3-n6a', 'C3-M14', 'C3-M1', 'col-3', 'Parrain'),
    ('mr-c3-n6b', 'C3-M14', 'C3-M2', 'col-3', 'Parrain');

-- =============================================================================
-- FIN DU SCRIPT
-- =============================================================================