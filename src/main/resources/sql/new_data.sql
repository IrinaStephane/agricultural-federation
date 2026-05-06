INSERT INTO federation (id, cotisation_percentage)
VALUES ('fed-1', 10.00)
    ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- MEMBRES (16 membres uniques)
-- Membres 1-8  : appartiennent aux collectivités 1 ET 2 (mêmes personnes)
-- Membres 9-16 : appartiennent uniquement à la collectivité 3
-- enrolment_date fixé à 2020-01-01 → valides comme parrains (>90j, >6mois)
-- =============================================================================

INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date,
                    address, email, phone_number, profession, gender)
VALUES
-- ── Collectivités 1 & 2 ──────────────────────────────────────────────────────
('C1-M1', 'Prenom membre 1',  'Nom membre 1',  '1980-02-01', '2020-01-01 00:00:00',
 'Lot II V M Ambato.', 'member.1@fed-agri.mg', '0341234567', 'Riziculteur',  'MALE'),

('C1-M2', 'Prenom membre 2',  'Nom membre 2',  '1982-03-05', '2020-01-01 00:00:00',
 'Lot II F Ambato.',  'member.2@fed-agri.mg', '0321234567', 'Agriculteur',  'MALE'),

('C1-M3', 'Prenom membre 3',  'Nom membre 3',  '1992-03-10', '2020-01-01 00:00:00',
 'Lot II J Ambato.',  'member.3@fed-agri.mg', '0331234567', 'Collecteur',   'MALE'),

('C1-M4', 'Prenom membre 4',  'Nom membre 4',  '1988-05-22', '2020-01-01 00:00:00',
 'Lot A K 50 Ambato.','member.4@fed-agri.mg', '0381234567', 'Distributeur', 'FEMALE'),

('C1-M5', 'Prenom membre 5',  'Nom membre 5',  '1999-08-21', '2020-01-01 00:00:00',
 'Lot UV 80 Ambato.', 'member.5@fed-agri.mg', '0373434567', 'Riziculteur',  'MALE'),

('C1-M6', 'Prenom membre 6',  'Nom membre 6',  '1998-08-22', '2020-01-01 00:00:00',
 'Lot UV 6 Ambato.',  'member.6@fed-agri.mg', '0372234567', 'Riziculteur',  'FEMALE'),

('C1-M7', 'Prenom membre 7',  'Nom membre 7',  '1998-01-31', '2020-01-01 00:00:00',
 'Lot UV 7 Ambato.',  'member.7@fed-agri.mg', '0374234567', 'Riziculteur',  'MALE'),

('C1-M8', 'Prenom membre 8',  'Nom membre 8',  '1975-08-20', '2020-01-01 00:00:00',
 'Lot UV 8 Ambato.',  'member.8@fed-agri.mg', '0370234567', 'Riziculteur',  'MALE'),

-- ── Collectivité 3 uniquement ─────────────────────────────────────────────────
('C3-M1', 'Prenom membre 9',  'Nom membre 9',  '1988-01-02', '2020-01-01 00:00:00',
 'Lot 33 J Antsirabe','member.9@fed-agri.mg',  '034034567',  'Apiculteur',   'MALE'),

('C3-M2', 'Prenom membre 10', 'Nom membre 10', '1982-03-05', '2020-01-01 00:00:00',
 'Lot 2 J Antsirabe', 'member.10@fed-agri.mg', '0338634567', 'Agriculteur',  'MALE'),

('C3-M3', 'Prenom membre 11', 'Nom membre 11', '1992-03-12', '2020-01-01 00:00:00',
 'Lot 8 KM Antsirabe','member.11@fed-agri.mg', '0338234567', 'Collecteur',   'MALE'),

('C3-M4', 'Prenom membre 12', 'Nom membre 12', '1988-05-10', '2020-01-01 00:00:00',
 'Lot A K 50 Antsirabe','member.12@fed-agri.mg','0382334567','Distributeur', 'FEMALE'),

('C3-M5', 'Prenom membre 13', 'Nom membre 13', '1999-08-11', '2020-01-01 00:00:00',
 'Lot UV 80 Antsirabe','member.13@fed-agri.mg','0373365567', 'Apiculteur',   'MALE'),

('C3-M6', 'Prenom membre 14', 'Nom membre 14', '1998-08-09', '2020-01-01 00:00:00',
 'Lot UV 6 Antsirabe','member.14@fed-agri.mg', '0378234567', 'Apiculteur',   'FEMALE'),

('C3-M7', 'Prenom membre 15', 'Nom membre 15', '1998-01-13', '2020-01-01 00:00:00',
 'Lot UV 7 Antsirabe','member.15@fed-agri.mg', '0374914567', 'Apiculteur',   'MALE'),

('C3-M8', 'Prenom membre 16', 'Nom membre 16', '1975-08-02', '2020-01-01 00:00:00',
 'Lot UV 8 Antsirabe','member.16@fed-agri.mg', '0370634567', 'Apiculteur',   'MALE')

    ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- COLLECTIVITÉS  (Table 1, page 11)
-- =============================================================================

INSERT INTO collectivity (id, number, name, speciality, creation_datetime,
                          federation_approval, authorization_date, id_federation, location)
VALUES
    ('col-1', '1', 'Mpanorina',      'Riziculture', '2020-01-01 00:00:00',
     TRUE, '2020-01-01 00:00:00', 'fed-1', 'Ambatondrazaka'),

    ('col-2', '2', 'Dobo voalohany', 'Pisciculture','2020-01-01 00:00:00',
     TRUE, '2020-01-01 00:00:00', 'fed-1', 'Ambatondrazaka'),

    ('col-3', '3', 'Tantely mamy',   'Apiculture',  '2020-01-01 00:00:00',
     TRUE, '2020-01-01 00:00:00', 'fed-1', 'Brickaville')

    ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- MEMBRE ↔ COLLECTIVITÉ  (Tables 2, 3, 4)
-- =============================================================================

INSERT INTO member_collectivity (id, id_member, id_collectivity, occupation, start_date)
VALUES
-- ── Collectivité 1 (Table 2) ─────────────────────────────────────────────────
('mc-c1-1', 'C1-M1', 'col-1', 'PRESIDENT',      '2020-01-01 00:00:00'),
('mc-c1-2', 'C1-M2', 'col-1', 'VICE_PRESIDENT', '2020-01-01 00:00:00'),
('mc-c1-3', 'C1-M3', 'col-1', 'SECRETARY',      '2020-01-01 00:00:00'),
('mc-c1-4', 'C1-M4', 'col-1', 'TREASURER',      '2020-01-01 00:00:00'),
('mc-c1-5', 'C1-M5', 'col-1', 'SENIOR',         '2020-01-01 00:00:00'),
('mc-c1-6', 'C1-M6', 'col-1', 'SENIOR',         '2020-01-01 00:00:00'),
('mc-c1-7', 'C1-M7', 'col-1', 'SENIOR',         '2020-01-01 00:00:00'),
('mc-c1-8', 'C1-M8', 'col-1', 'SENIOR',         '2020-01-01 00:00:00'),

-- ── Collectivité 2 (Table 3) ─────────────────────────────────────────────────
('mc-c2-1', 'C1-M1', 'col-2', 'SENIOR',         '2020-01-01 00:00:00'),
('mc-c2-2', 'C1-M2', 'col-2', 'SENIOR',         '2020-01-01 00:00:00'),
('mc-c2-3', 'C1-M3', 'col-2', 'SENIOR',         '2020-01-01 00:00:00'),
('mc-c2-4', 'C1-M4', 'col-2', 'SENIOR',         '2020-01-01 00:00:00'),
('mc-c2-5', 'C1-M5', 'col-2', 'PRESIDENT',      '2020-01-01 00:00:00'),
('mc-c2-6', 'C1-M6', 'col-2', 'VICE_PRESIDENT', '2020-01-01 00:00:00'),
('mc-c2-7', 'C1-M7', 'col-2', 'SECRETARY',      '2020-01-01 00:00:00'),
('mc-c2-8', 'C1-M8', 'col-2', 'TREASURER',      '2020-01-01 00:00:00'),

-- ── Collectivité 3 (Table 4) ─────────────────────────────────────────────────
('mc-c3-1', 'C3-M1', 'col-3', 'PRESIDENT',      '2020-01-01 00:00:00'),
('mc-c3-2', 'C3-M2', 'col-3', 'VICE_PRESIDENT', '2020-01-01 00:00:00'),
('mc-c3-3', 'C3-M3', 'col-3', 'SECRETARY',      '2020-01-01 00:00:00'),
('mc-c3-4', 'C3-M4', 'col-3', 'TREASURER',      '2020-01-01 00:00:00'),
('mc-c3-5', 'C3-M5', 'col-3', 'SENIOR',         '2020-01-01 00:00:00'),
('mc-c3-6', 'C3-M6', 'col-3', 'SENIOR',         '2020-01-01 00:00:00'),
('mc-c3-7', 'C3-M7', 'col-3', 'SENIOR',         '2020-01-01 00:00:00'),
('mc-c3-8', 'C3-M8', 'col-3', 'SENIOR',         '2020-01-01 00:00:00')

    ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- PARRAINAGES (member_referee)
-- UNIQUE(id_candidate, id_referee)
-- =============================================================================

INSERT INTO member_referee (id, id_candidate, id_referee, id_collectivity, relationship)
VALUES
-- ── Collectivité 1 ───────────────────────────────────────────────────────────
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

-- ── Collectivité 3 ───────────────────────────────────────────────────────────
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
('mr-28', 'C3-M8', 'C3-M2', 'col-3', 'Parrain')

    ON CONFLICT (id_candidate, id_referee) DO NOTHING;

-- =============================================================================
-- COTISATIONS  (Tables 5, 6, 7 – page 15)
-- =============================================================================

INSERT INTO membership_fee (id, id_collectivity, label, frequency, amount, eligible_from, is_active)
VALUES
    ('cot-1', 'col-1', 'Cotisation annuelle', 'ANNUALLY', 100000.00, '2026-01-01', true),
    ('cot-2', 'col-2', 'Cotisation annuelle', 'ANNUALLY', 100000.00, '2026-01-01', true),
    ('cot-3', 'col-3', 'Cotisation annuelle', 'ANNUALLY',  50000.00, '2026-01-01', true)
    ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- COMPTES (page 16)
-- C1-A-CASH     solde = 100k×6 + 60k + 90k = 750 000
-- C1-A-MOBILE-1 solde = 0
-- C2-A-CASH     solde = 60k + 90k + 100k×4 = 550 000
-- C2-A-MOBILE-1 solde = 40k + 60k           = 100 000
-- C3-A-CASH     solde = 0 (aucun paiement)
-- =============================================================================

INSERT INTO account (id, id_collectivity, id_federation, balance)
VALUES
    ('C1-A-CASH',     'col-1', NULL, 750000.00),
    ('C1-A-MOBILE-1', 'col-1', NULL,      0.00),
    ('C2-A-CASH',     'col-2', NULL, 550000.00),
    ('C2-A-MOBILE-1', 'col-2', NULL, 100000.00),
    ('C3-A-CASH',     'col-3', NULL,      0.00)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO cash_account (id, id_account)
VALUES
    ('ca-1', 'C1-A-CASH'),
    ('ca-2', 'C2-A-CASH'),
    ('ca-3', 'C3-A-CASH')
    ON CONFLICT (id_account) DO NOTHING;

INSERT INTO mobile_money_account (id, id_account, holder_name, service_name, phone_number)
VALUES
    ('mma-1', 'C1-A-MOBILE-1', 'Mpanorina',      'ORANGE_MONEY', '0370489612'),
    ('mma-2', 'C2-A-MOBILE-1', 'Dobo voalohany', 'ORANGE_MONEY', '0320489612')
    ON CONFLICT (id_account) DO NOTHING;

-- =============================================================================
-- TRANSACTIONS  (Tables 8-11, pages 17-20) – toutes datées du 01/01/2026
-- Collectivité 3 : aucune transaction
-- =============================================================================

INSERT INTO transaction (id, id_member, id_collectivity, id_membership_fee,
                         id_account, transaction_type, amount, transaction_date, payment_mode)
VALUES
-- ── Collectivité 1 (Table 9, page 18) → C1-A-CASH ───────────────────────────
('tx-1',  'C1-M1', 'col-1', 'cot-1', 'C1-A-CASH', 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),
('tx-2',  'C1-M2', 'col-1', 'cot-1', 'C1-A-CASH', 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),
('tx-3',  'C1-M3', 'col-1', 'cot-1', 'C1-A-CASH', 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),
('tx-4',  'C1-M4', 'col-1', 'cot-1', 'C1-A-CASH', 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),
('tx-5',  'C1-M5', 'col-1', 'cot-1', 'C1-A-CASH', 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),
('tx-6',  'C1-M6', 'col-1', 'cot-1', 'C1-A-CASH', 'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),
('tx-7',  'C1-M7', 'col-1', 'cot-1', 'C1-A-CASH', 'IN',  60000.00, '2026-01-01 00:00:00', 'CASH'),
('tx-8',  'C1-M8', 'col-1', 'cot-1', 'C1-A-CASH', 'IN',  90000.00, '2026-01-01 00:00:00', 'CASH'),

-- ── Collectivité 2 (Table 11, page 20) ───────────────────────────────────────
-- Membres 1-6 → C2-A-CASH
('tx-9',  'C1-M1', 'col-2', 'cot-2', 'C2-A-CASH',     'IN',  60000.00, '2026-01-01 00:00:00', 'CASH'),
('tx-10', 'C1-M2', 'col-2', 'cot-2', 'C2-A-CASH',     'IN',  90000.00, '2026-01-01 00:00:00', 'CASH'),
('tx-11', 'C1-M3', 'col-2', 'cot-2', 'C2-A-CASH',     'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),
('tx-12', 'C1-M4', 'col-2', 'cot-2', 'C2-A-CASH',     'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),
('tx-13', 'C1-M5', 'col-2', 'cot-2', 'C2-A-CASH',     'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),
('tx-14', 'C1-M6', 'col-2', 'cot-2', 'C2-A-CASH',     'IN', 100000.00, '2026-01-01 00:00:00', 'CASH'),
-- Membres 7-8 → C2-A-MOBILE-1
('tx-15', 'C1-M7', 'col-2', 'cot-2', 'C2-A-MOBILE-1', 'IN',  40000.00, '2026-01-01 00:00:00', 'MOBILE_BANKING'),
('tx-16', 'C1-M8', 'col-2', 'cot-2', 'C2-A-MOBILE-1', 'IN',  60000.00, '2026-01-01 00:00:00', 'MOBILE_BANKING')

    ON CONFLICT (id) DO NOTHING;