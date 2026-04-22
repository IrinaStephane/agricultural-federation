INSERT INTO federation (id, cotisation_percentage) VALUES (1, 10.00) ON CONFLICT (id) DO NOTHING;

INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date, address, email, phone_number, profession, gender)
VALUES
    (1,'Jean','Rakoto','1980-05-15', NOW() - INTERVAL '2 years', 'Lot 12 Analakely Antananarivo','jean.rakoto@email.com','0341000001','Agriculteur','MALE'),
    (2,'Marie','Rasoa','1985-08-20', NOW() - INTERVAL '18 months', 'Lot 34 Isotry Antananarivo','marie.rasoa@email.com','0341000002','Eleveuse','FEMALE'),
    (3,'Paul','Rabe','1978-12-10', NOW() - INTERVAL '3 years', 'Lot 7 Ampefiloha Antananarivo','paul.rabe@email.com','0341000003','Pecheur','MALE'),
    (4,'Liva','Andrianina','1990-03-03', NOW() - INTERVAL '14 months', 'Lot 5 Ankorondrano Antananarivo','liva.andrianina@email.com','0341000004','Apicultrice','FEMALE'),
    (5,'Hery','Ramiandrisoa','1983-07-22', NOW() - INTERVAL '2 years', 'Antsirabe','hery.rami@email.com','0341000005','Riziculteur','MALE'),
    (6,'Voahary','Rakotoarison','1987-11-05', NOW() - INTERVAL '20 months', 'Antsirabe','voahary.rkt@email.com','0341000006','Maraichere','FEMALE'),
    (7,'Tojo','Randriamanana','1992-01-30', NOW() - INTERVAL '15 months', 'Antsirabe','tojo.rand@email.com','0341000007','Eleveur','MALE'),
    (8,'Soa','Razafindrabe','1995-09-14', NOW() - INTERVAL '13 months', 'Antsirabe','soa.razaf@email.com','0341000008','Floriculture','FEMALE'),
    (9,'Fidy','Rasolofo','1993-04-18', NOW() - INTERVAL '4 months', 'Antananarivo','fidy.rasolo@email.com','0341000009','Technicien','MALE'),
    (10,'Zo','Randria','1997-06-25', NOW() - INTERVAL '4 months', 'Antananarivo','zo.randria@email.com','0341000010','Agronome','FEMALE'),
    (11,'Aina','Rabeson','2000-02-12', NOW() - INTERVAL '1 month', 'Antananarivo','aina.rabeson@email.com','0341000011','Etudiant','FEMALE'),
    (12,'Niry','Rakotondrabe','1999-08-08', NOW() - INTERVAL '2 weeks', 'Antananarivo','niry.rkt@email.com','0341000012','Etudiant','MALE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO collectivity (id, number, name, speciality, creation_datetime, federation_approval, authorization_date, id_federation, location)
VALUES
    (1, NULL, NULL, 'Riziculture', NOW() - INTERVAL '1 year', TRUE, NOW() - INTERVAL '1 year', 1, 'Antananarivo'),
    (2, 'COL-002', 'Collectivite Antsirabe', 'Maraichage', NOW() - INTERVAL '2 years', TRUE, NOW() - INTERVAL '2 years', 1, 'Antsirabe')
ON CONFLICT (id) DO NOTHING;

INSERT INTO member_collectivity (id_member, id_collectivity, occupation, start_date)
VALUES
    (1,1,'PRESIDENT','2024-01-01'),(2,1,'VICE_PRESIDENT','2024-01-01'),(3,1,'TREASURER','2024-01-01'),(4,1,'SECRETARY','2024-01-01'),
    (9,1,'SENIOR','2024-12-01'),(10,1,'SENIOR','2024-12-01'),
    (5,2,'PRESIDENT','2023-01-01'),(6,2,'VICE_PRESIDENT','2023-01-01'),(7,2,'TREASURER','2023-01-01'),(8,2,'SECRETARY','2023-01-01')
ON CONFLICT DO NOTHING;

INSERT INTO member_referee (id_candidate, id_referee, id_collectivity, relationship)
VALUES (11,1,1,'Famille'),(11,2,1,'Collegues'),(12,9,1,'Amis'),(12,5,2,'Collegues')
ON CONFLICT DO NOTHING;

-- Accounts for collectivity 1
INSERT INTO account (id, id_collectivity, id_federation, balance) VALUES (1, 1, NULL, 0) ON CONFLICT (id) DO NOTHING;
INSERT INTO account (id, id_collectivity, id_federation, balance) VALUES (2, 1, NULL, 0) ON CONFLICT (id) DO NOTHING;
INSERT INTO account (id, id_collectivity, id_federation, balance) VALUES (3, 2, NULL, 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO cash_account (id_account) VALUES (1) ON CONFLICT (id_account) DO NOTHING;
INSERT INTO mobile_money_account (id_account, holder_name, service_name, phone_number) VALUES (2,'Jean Rakoto','MVOLA','0341000001') ON CONFLICT (id_account) DO NOTHING;
INSERT INTO cash_account (id_account) VALUES (3) ON CONFLICT (id_account) DO NOTHING;

-- Membership fees for collectivity 1
INSERT INTO membership_fee (id, id_collectivity, label, frequency, amount, eligible_from, is_active)
VALUES
    (1, 1, 'Cotisation mensuelle 2026', 'MONTHLY', 10000.00, '2026-01-01', true),
    (2, 1, 'Cotisation annuelle 2026',  'ANNUALLY', 100000.00, '2026-01-01', true)
ON CONFLICT (id) DO NOTHING;

-- Reset sequences
SELECT setval('member_id_seq',       (SELECT MAX(id) FROM member));
SELECT setval('collectivity_id_seq', (SELECT MAX(id) FROM collectivity));
SELECT setval('federation_id_seq',   (SELECT MAX(id) FROM federation));
SELECT setval('account_id_seq',      (SELECT MAX(id) FROM account));
SELECT setval('membership_fee_id_seq',(SELECT MAX(id) FROM membership_fee));
