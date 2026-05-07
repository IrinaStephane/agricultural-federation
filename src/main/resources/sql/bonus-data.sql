ALTER TABLE "public"."activity_attendance"
    ADD COLUMN IF NOT EXISTS "occurrence_date" date NOT NULL DEFAULT CURRENT_DATE;

-- Remplacer l'ancienne contrainte unique par une nouvelle qui inclut la date
ALTER TABLE "public"."activity_attendance"
DROP CONSTRAINT IF EXISTS activity_attendance_id_activity_id_member_key;

ALTER TABLE "public"."activity_attendance"
    ADD CONSTRAINT "uq_attendance_activity_member_date"
        UNIQUE ("id_activity", "id_member", "occurrence_date");

-- =============================================================================
-- 1. ACTIVITÉS
-- =============================================================================

INSERT INTO "public"."collectivity_activity"
(id, id_collectivity, label, activity_type,
 executive_date, recurrence_week_ordinal, recurrence_day_of_week)
VALUES
-- ── Collectivité 1 ────────────────────────────────────────────────────────
-- act-1 : AG1 — 1er samedi de chaque mois (récurrent)
('act-1', 'col-1', 'AG1',             'MEETING',  NULL,         1, 'SA'),
-- act-2 : Formation de base — 2è dimanche de chaque mois (récurrent)
('act-2', 'col-1', 'Formation de base','TRAINING', NULL,         2, 'SU'),

-- ── Collectivité 2 ────────────────────────────────────────────────────────
-- act-3 : AG2 — 1er dimanche de chaque mois (récurrent)
('act-3', 'col-2', 'AG2',             'MEETING',  NULL,         1, 'SU'),
-- act-4 : Formation de base — 3è dimanche de chaque mois (récurrent)
('act-4', 'col-2', 'Formation de base','TRAINING', NULL,         3, 'SU'),
-- act-5 : Perfectionnement — ponctuel le 30/04/2026 (PUNCTUAL → OTHER dans l'enum)
('act-5', 'col-2', 'Perfectionnement','OTHER',    '2026-04-30', NULL, NULL),
('act-6', 'col-3', 'AG3',             'MEETING',  NULL,         1, 'FR'),
('act-7', 'col-3', 'Formation de base','TRAINING', NULL,         4, 'WE')

    ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 2. OCCUPATIONS CONCERNÉES
-- =============================================================================

INSERT INTO "public"."activity_occupation_concerned" (id_activity, occupation)
VALUES
-- act-1 : AG col-1 → tous les postes
('act-1', 'JUNIOR'),
('act-1', 'SENIOR'),
('act-1', 'SECRETARY'),
('act-1', 'TREASURER'),
('act-1', 'VICE_PRESIDENT'),
('act-1', 'PRESIDENT'),

-- act-2 : Formation de base col-1 → JUNIOR uniquement
('act-2', 'JUNIOR'),

-- act-3 : AG col-2 → tous les postes
('act-3', 'JUNIOR'),
('act-3', 'SENIOR'),
('act-3', 'SECRETARY'),
('act-3', 'TREASURER'),
('act-3', 'VICE_PRESIDENT'),
('act-3', 'PRESIDENT'),

-- act-4 : Formation de base col-2 → JUNIOR uniquement
('act-4', 'JUNIOR'),

-- act-5 : Perfectionnement col-2 → SENIOR uniquement
('act-5', 'SENIOR'),

-- act-6 : AG col-3 → tous les postes
('act-6', 'JUNIOR'),
('act-6', 'SENIOR'),
('act-6', 'SECRETARY'),
('act-6', 'TREASURER'),
('act-6', 'VICE_PRESIDENT'),
('act-6', 'PRESIDENT'),
('act-7', 'JUNIOR')
ON CONFLICT (id_activity, occupation) DO NOTHING;


-- Séance du 07/03/2026
INSERT INTO "public"."activity_attendance"
(id, id_activity, id_member, attendance_status, occurrence_date)
VALUES
    ('att-act1-C1M1-20260307', 'act-1', 'C1-M1', 'ATTENDED',  '2026-03-07'),
    ('att-act1-C1M2-20260307', 'act-1', 'C1-M2', 'ATTENDED',  '2026-03-07'),
    ('att-act1-C1M3-20260307', 'act-1', 'C1-M3', 'ATTENDED',  '2026-03-07'),
    ('att-act1-C1M4-20260307', 'act-1', 'C1-M4', 'ATTENDED',  '2026-03-07'),
    ('att-act1-C1M5-20260307', 'act-1', 'C1-M5', 'ATTENDED',  '2026-03-07'),
    ('att-act1-C1M6-20260307', 'act-1', 'C1-M6', 'ATTENDED',  '2026-03-07'),
    ('att-act1-C1M7-20260307', 'act-1', 'C1-M7', 'MISSING',   '2026-03-07'),
    ('att-act1-C1M8-20260307', 'act-1', 'C1-M8', 'MISSING',   '2026-03-07')
    ON CONFLICT (id_activity, id_member, occurrence_date) DO NOTHING;

-- Séance du 04/04/2026
INSERT INTO "public"."activity_attendance"
(id, id_activity, id_member, attendance_status, occurrence_date)
VALUES
    ('att-act1-C1M1-20260404', 'act-1', 'C1-M1', 'ATTENDED',  '2026-04-04'),
    ('att-act1-C1M2-20260404', 'act-1', 'C1-M2', 'ATTENDED',  '2026-04-04'),
    ('att-act1-C1M3-20260404', 'act-1', 'C1-M3', 'MISSING',   '2026-04-04'),
    ('att-act1-C1M4-20260404', 'act-1', 'C1-M4', 'MISSING',   '2026-04-04'),
    ('att-act1-C1M5-20260404', 'act-1', 'C1-M5', 'ATTENDED',  '2026-04-04'),
    ('att-act1-C1M6-20260404', 'act-1', 'C1-M6', 'ATTENDED',  '2026-04-04'),
    ('att-act1-C1M7-20260404', 'act-1', 'C1-M7', 'ATTENDED',  '2026-04-04'),
    ('att-act1-C1M8-20260404', 'act-1', 'C1-M8', 'ATTENDED',  '2026-04-04')
    ON CONFLICT (id_activity, id_member, occurrence_date) DO NOTHING;

-- ── Collectivité 2 — act-3 (AG2) ──────────────────────────────────────────

-- Séance du 08/03/2026
INSERT INTO "public"."activity_attendance"
(id, id_activity, id_member, attendance_status, occurrence_date)
VALUES
    ('att-act3-C1M1-20260308', 'act-3', 'C1-M1', 'ATTENDED',  '2026-03-08'),
    ('att-act3-C1M2-20260308', 'act-3', 'C1-M2', 'ATTENDED',  '2026-03-08'),
    ('att-act3-C1M3-20260308', 'act-3', 'C1-M3', 'MISSING',   '2026-03-08'),
    ('att-act3-C1M4-20260308', 'act-3', 'C1-M4', 'MISSING',   '2026-03-08'),
    ('att-act3-C1M5-20260308', 'act-3', 'C1-M5', 'ATTENDED',  '2026-03-08'),
    ('att-act3-C1M6-20260308', 'act-3', 'C1-M6', 'ATTENDED',  '2026-03-08'),
    ('att-act3-C1M7-20260308', 'act-3', 'C1-M7', 'ATTENDED',  '2026-03-08'),
    ('att-act3-C1M8-20260308', 'act-3', 'C1-M8', 'ATTENDED',  '2026-03-08')
    ON CONFLICT (id_activity, id_member, occurrence_date) DO NOTHING;

-- Séance du 05/04/2026
INSERT INTO "public"."activity_attendance"
(id, id_activity, id_member, attendance_status, occurrence_date)
VALUES
    ('att-act3-C1M1-20260405', 'act-3', 'C1-M1', 'ATTENDED',  '2026-04-05'),
    ('att-act3-C1M2-20260405', 'act-3', 'C1-M2', 'ATTENDED',  '2026-04-05'),
    ('att-act3-C1M3-20260405', 'act-3', 'C1-M3', 'MISSING',   '2026-04-05'),
    ('att-act3-C1M4-20260405', 'act-3', 'C1-M4', 'ATTENDED',  '2026-04-05'),
    ('att-act3-C1M5-20260405', 'act-3', 'C1-M5', 'ATTENDED',  '2026-04-05'),
    ('att-act3-C1M6-20260405', 'act-3', 'C1-M6', 'ATTENDED',  '2026-04-05'),
    ('att-act3-C1M7-20260405', 'act-3', 'C1-M7', 'ATTENDED',  '2026-04-05'),
    ('att-act3-C1M8-20260405', 'act-3', 'C1-M8', 'MISSING',   '2026-04-05')
    ON CONFLICT (id_activity, id_member, occurrence_date) DO NOTHING;

-- ── Collectivité 2 — act-5 (Perfectionnement, ponctuel 30/04/2026) ────────

INSERT INTO "public"."activity_attendance"
(id, id_activity, id_member, attendance_status, occurrence_date)
VALUES
    ('att-act5-C1M1-20260430', 'act-5', 'C1-M1', 'ATTENDED',   '2026-04-30'),
    ('att-act5-C1M2-20260430', 'act-5', 'C1-M2', 'ATTENDED',   '2026-04-30'),
    ('att-act5-C1M3-20260430', 'act-5', 'C1-M3', 'ATTENDED',   '2026-04-30'),
    ('att-act5-C1M4-20260430', 'act-5', 'C1-M4', 'MISSING',    '2026-04-30'),
    ('att-act5-C1M5-20260430', 'act-5', 'C1-M5', 'UNDEFINED',  '2026-04-30'),
    ('att-act5-C1M6-20260430', 'act-5', 'C1-M6', 'UNDEFINED',  '2026-04-30'),
    ('att-act5-C1M7-20260430', 'act-5', 'C1-M7', 'UNDEFINED',  '2026-04-30'),
    ('att-act5-C1M8-20260430', 'act-5', 'C1-M8', 'UNDEFINED',  '2026-04-30')
    ON CONFLICT (id_activity, id_member, occurrence_date) DO NOTHING;

-- ── Collectivité 3 — act-6 (AG3) ──────────────────────────────────────────

-- Séance du 06/03/2026
INSERT INTO "public"."activity_attendance"
(id, id_activity, id_member, attendance_status, occurrence_date)
VALUES
    ('att-act6-C3M1-20260306', 'act-6', 'C3-M1', 'ATTENDED',  '2026-03-06'),
    ('att-act6-C3M2-20260306', 'act-6', 'C3-M2', 'ATTENDED',  '2026-03-06'),
    ('att-act6-C3M3-20260306', 'act-6', 'C3-M3', 'ATTENDED',  '2026-03-06'),
    ('att-act6-C3M4-20260306', 'act-6', 'C3-M4', 'ATTENDED',  '2026-03-06'),
    ('att-act6-C3M5-20260306', 'act-6', 'C3-M5', 'ATTENDED',  '2026-03-06'),
    ('att-act6-C3M6-20260306', 'act-6', 'C3-M6', 'ATTENDED',  '2026-03-06'),
    ('att-act6-C3M7-20260306', 'act-6', 'C3-M7', 'MISSING',   '2026-03-06'),
    ('att-act6-C3M8-20260306', 'act-6', 'C3-M8', 'MISSING',   '2026-03-06')
    ON CONFLICT (id_activity, id_member, occurrence_date) DO NOTHING;

-- Séance du 03/04/2026
-- Note : C1-M1 est un membre externe (collectivité 1) qui assiste à l'AG de col-3
INSERT INTO "public"."activity_attendance"
(id, id_activity, id_member, attendance_status, occurrence_date)
VALUES
    ('att-act6-C3M1-20260403', 'act-6', 'C3-M1', 'ATTENDED',  '2026-04-03'),
    ('att-act6-C3M2-20260403', 'act-6', 'C3-M2', 'ATTENDED',  '2026-04-03'),
    ('att-act6-C3M3-20260403', 'act-6', 'C3-M3', 'MISSING',   '2026-04-03'),
    ('att-act6-C3M4-20260403', 'act-6', 'C3-M4', 'MISSING',   '2026-04-03'),
    ('att-act6-C3M5-20260403', 'act-6', 'C3-M5', 'ATTENDED',  '2026-04-03'),
    ('att-act6-C3M6-20260403', 'act-6', 'C3-M6', 'ATTENDED',  '2026-04-03'),
    ('att-act6-C3M7-20260403', 'act-6', 'C3-M7', 'MISSING',   '2026-04-03'),
    ('att-act6-C3M8-20260403', 'act-6', 'C3-M8', 'ATTENDED',  '2026-04-03'),
    ('att-act6-C1M1-20260403', 'act-6', 'C1-M1', 'ATTENDED',  '2026-04-03')
    ON CONFLICT (id_activity, id_member, occurrence_date) DO NOTHING;