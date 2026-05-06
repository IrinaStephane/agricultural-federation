INSERT INTO collectivity_activity
(id, id_collectivity, label, activity_type,
 executive_date, recurrence_week_ordinal, recurrence_day_of_week)
VALUES
    ('act-c1-ag1', 'col-1', 'Assemblée générale – Février 2026',
     'MEETING', '2026-02-15', NULL, NULL),
    ('act-c1-tr1', 'col-1', 'Formation obligatoire – Mars 2026',
     'TRAINING', '2026-03-20', NULL, NULL),
    ('act-c1-rec1', 'col-1', 'Réunion mensuelle',
     'MEETING', NULL, 2, 'SU'),
    ('act-c2-ag1', 'col-2', 'Assemblée générale – Février 2026',
     'MEETING', '2026-02-15', NULL, NULL),
    ('act-c3-ex1', 'col-3', 'Formation apiculture avancée',
     'TRAINING', '2026-04-10', NULL, NULL)

    ON CONFLICT (id) DO NOTHING;

INSERT INTO activity_occupation_concerned (id_activity, occupation)
VALUES
    ('act-c1-tr1', 'JUNIOR')
    ON CONFLICT (id_activity, occupation) DO NOTHING;


INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status)
VALUES
    ('att-ag1-1', 'act-c1-ag1', 'C1-M1', 'ATTENDED'),
    ('att-ag1-2', 'act-c1-ag1', 'C1-M2', 'ATTENDED'),
    ('att-ag1-3', 'act-c1-ag1', 'C1-M3', 'ATTENDED'),
    ('att-ag1-4', 'act-c1-ag1', 'C1-M4', 'ATTENDED'),
    ('att-ag1-5', 'act-c1-ag1', 'C1-M5', 'ATTENDED'),
    ('att-ag1-6', 'act-c1-ag1', 'C1-M6', 'ATTENDED'),
    ('att-ag1-7', 'act-c1-ag1', 'C1-M7', 'MISSING'),
    ('att-ag1-8', 'act-c1-ag1', 'C1-M8', 'MISSING')
    ON CONFLICT (id_activity, id_member) DO NOTHING;

INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status)
VALUES
    ('att-tr1-1', 'act-c1-tr1', 'C1-M1', 'ATTENDED'),
    ('att-tr1-2', 'act-c1-tr1', 'C1-M2', 'ATTENDED'),
    ('att-tr1-3', 'act-c1-tr1', 'C3-M1', 'ATTENDED')
    ON CONFLICT (id_activity, id_member) DO NOTHING;

INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status)
VALUES
    ('att-ag2-1', 'act-c2-ag1', 'C1-M1', 'ATTENDED'),
    ('att-ag2-2', 'act-c2-ag1', 'C1-M2', 'ATTENDED'),
    ('att-ag2-3', 'act-c2-ag1', 'C1-M3', 'MISSING'),
    ('att-ag2-4', 'act-c2-ag1', 'C1-M4', 'ATTENDED'),
    ('att-ag2-5', 'act-c2-ag1', 'C1-M5', 'ATTENDED'),
    ('att-ag2-6', 'act-c2-ag1', 'C1-M6', 'MISSING'),
    ('att-ag2-7', 'act-c2-ag1', 'C1-M7', 'ATTENDED'),
    ('att-ag2-8', 'act-c2-ag1', 'C1-M8', 'ATTENDED')
    ON CONFLICT (id_activity, id_member) DO NOTHING;