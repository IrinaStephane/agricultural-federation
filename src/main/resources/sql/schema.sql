CREATE TABLE IF NOT EXISTS member (
    id VARCHAR(255) PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    birth_date DATE,
    gender VARCHAR(10),
    address VARCHAR(255),
    profession VARCHAR(255),
    phone_number VARCHAR(20),
    email VARCHAR(255),
    occupation VARCHAR(50),
    registration_fee_paid BOOLEAN DEFAULT FALSE,
    membership_dues_paid BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS member_referee (
    member_id VARCHAR(255) REFERENCES member(id),
    referee_id VARCHAR(255) REFERENCES member(id),
    PRIMARY KEY (member_id, referee_id)
);

CREATE TABLE IF NOT EXISTS collectivity (
    id VARCHAR(255) PRIMARY KEY,
    location VARCHAR(255) NOT NULL,
    federation_approval BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS collectivity_member (
    collectivity_id VARCHAR(255) REFERENCES collectivity(id),
    member_id VARCHAR(255) REFERENCES member(id),
    PRIMARY KEY (collectivity_id, member_id)
);

CREATE TABLE IF NOT EXISTS collectivity_structure (
    collectivity_id VARCHAR(255) PRIMARY KEY REFERENCES collectivity(id),
    president_id VARCHAR(255) REFERENCES member(id),
    vice_president_id VARCHAR(255) REFERENCES member(id),
    treasurer_id VARCHAR(255) REFERENCES member(id),
    secretary_id VARCHAR(255) REFERENCES member(id)
);
