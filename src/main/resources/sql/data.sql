-- Initial members to serve as referees or structure members
INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, occupation, registration_fee_paid, membership_dues_paid)
VALUES 
('member_001', 'Jean', 'Dupont', '1980-05-15', 'MALE', 'Rue 1, Antananarivo', 'Agriculteur', '0341234567', 'jean.dupont@email.com', 'PRESIDENT', true, true),
('member_002', 'Marie', 'Rasoa', '1985-08-20', 'FEMALE', 'Rue 2, Antsirabe', 'Eleveur', '0347654321', 'marie.rasoa@email.com', 'VICE_PRESIDENT', true, true),
('member_003', 'Paul', 'Rabe', '1990-12-10', 'MALE', 'Rue 3, Toamasina', 'Pecheur', '0321122334', 'paul.rabe@email.com', 'TREASURER', true, true),
('member_004', 'Liva', 'Rakoto', '1992-03-03', 'FEMALE', 'Rue 4, Mahajanga', 'Apiculteur', '0339988776', 'liva.rakoto@email.com', 'SECRETARY', true, true)
ON CONFLICT (id) DO NOTHING;
