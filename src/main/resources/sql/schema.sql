-- ============================================================
-- SCHEMA COMPLET - Fédération de collectivités agricoles
-- Version : v0.0.2 - Fonctionnalité J incluse
-- ============================================================

-- ============================================================
-- EXTENSIONS & CONFIGURATION
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- ENUMERATIONS
-- ============================================================

CREATE TYPE gender AS ENUM (
    'MALE',
    'FEMALE'
    );

CREATE TYPE member_occupation AS ENUM (
    'JUNIOR',
    'SENIOR',
    'SECRETARY',
    'TREASURER',
    'VICE_PRESIDENT',
    'PRESIDENT'
    );

CREATE TYPE account_type AS ENUM (
    'CAISSE',
    'BANK',
    'MOBILE_MONEY'
    );

CREATE TYPE payment_mode AS ENUM (
    'CASH',
    'BANK_TRANSFER',
    'MOBILE_MONEY'
    );

CREATE TYPE contribution_type AS ENUM (
    'MONTHLY',
    'ANNUAL',
    'EXCEPTIONAL'
    );

CREATE TYPE activity_type AS ENUM (
    'MONTHLY_GENERAL_ASSEMBLY', -- Assemblée générale mensuelle (obligatoire)
    'MANDATORY_JUNIOR_TRAINING', -- Formation obligatoire juniors
    'EXCEPTIONAL', -- Activité exceptionnelle collectivité
    'FEDERATION' -- Activité planifiée par la fédération
    );

CREATE TYPE bank_name AS ENUM (
    'BRED',
    'MCB',
    'BMOI',
    'BOA',
    'BGFI',
    'AFG',
    'ACCES_BANQUE',
    'BAOBAB',
    'SIPEM'
    );

CREATE TYPE mobile_money_service AS ENUM (
    'ORANGE_MONEY',
    'MVOLA',
    'AIRTEL_MONEY'
    );

-- ============================================================
-- TABLE : member
-- Un membre de la fédération
-- ============================================================
CREATE TABLE member
(
    id            VARCHAR(36) PRIMARY KEY    DEFAULT uuid_generate_v4(),
    first_name    VARCHAR(100)      NOT NULL,
    last_name     VARCHAR(100)      NOT NULL,
    birth_date    DATE              NOT NULL,
    gender        gender            NOT NULL,
    address       VARCHAR(255)      NOT NULL,
    profession    VARCHAR(100)      NOT NULL,
    phone_number  VARCHAR(20)       NOT NULL,
    email         VARCHAR(150)      NOT NULL UNIQUE,
    adhesion_date DATE              NOT NULL DEFAULT CURRENT_DATE,
    occupation    member_occupation NOT NULL DEFAULT 'JUNIOR',
    created_at    TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE : collectivity
-- Une collectivité agricole
-- number et name sont NULL à la création,
-- attribués ensuite par la fédération (fonctionnalité J)
-- ============================================================
CREATE TABLE collectivity
(
    id                  VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    number              VARCHAR(50) UNIQUE,  -- attribué par la fédération, immuable
    name                VARCHAR(255) UNIQUE, -- attribué par la fédération, immuable
    location            VARCHAR(150) NOT NULL,
    specialty           VARCHAR(150),
    creation_date       DATE         NOT NULL   DEFAULT CURRENT_DATE,
    federation_approval BOOLEAN      NOT NULL   DEFAULT FALSE,
    created_at          TIMESTAMP    NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL   DEFAULT CURRENT_TIMESTAMP
);

-- Index pour vérification rapide d'unicité (fonctionnalité J)
CREATE UNIQUE INDEX idx_collectivity_number ON collectivity (number) WHERE number IS NOT NULL;
CREATE UNIQUE INDEX idx_collectivity_name ON collectivity (name) WHERE name IS NOT NULL;

-- ============================================================
-- TABLE : collectivity_member
-- Appartenance d'un membre à une collectivité
-- Un membre peut changer de collectivité (left_date non NULL = parti)
-- ============================================================
CREATE TABLE collectivity_member
(
    id                 VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    collectivity_id    VARCHAR(36) NOT NULL REFERENCES collectivity (id) ON DELETE RESTRICT,
    member_id          VARCHAR(36) NOT NULL REFERENCES member (id) ON DELETE RESTRICT,
    joined_date        DATE        NOT NULL    DEFAULT CURRENT_DATE,
    left_date          DATE,         -- NULL = membre actif, sinon date de départ
    resignation_reason VARCHAR(500), -- motif de démission si applicable
    CONSTRAINT uq_active_membership UNIQUE (collectivity_id, member_id, left_date)
);

CREATE INDEX idx_collectivity_member_collectivity ON collectivity_member (collectivity_id);
CREATE INDEX idx_collectivity_member_member ON collectivity_member (member_id);

-- ============================================================
-- TABLE : member_referee
-- Parrainage — B-2 : au moins 2 membres confirmés
-- avec la nature de la relation (famille, amis, collègues...)
-- ============================================================
CREATE TABLE member_referee
(
    member_id    VARCHAR(36)  NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    referee_id   VARCHAR(36)  NOT NULL REFERENCES member (id) ON DELETE RESTRICT,
    relationship VARCHAR(100) NOT NULL, -- ex: famille, amis, collègues
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id, referee_id),
    CONSTRAINT chk_no_self_referee CHECK (member_id <> referee_id)
);

-- ============================================================
-- TABLE : mandate
-- Mandat d'un membre pour un poste spécifique
-- dans une collectivité (1 an) ou à la fédération (2 ans)
-- Postes : PRESIDENT, VICE_PRESIDENT, TREASURER, SECRETARY
-- Max 2 mandats par membre par poste par collectivité
-- ============================================================
CREATE TABLE mandate
(
    id                    VARCHAR(36) PRIMARY KEY    DEFAULT uuid_generate_v4(),
    member_id             VARCHAR(36)       NOT NULL REFERENCES member (id) ON DELETE RESTRICT,
    collectivity_id       VARCHAR(36) REFERENCES collectivity (id) ON DELETE RESTRICT, -- NULL si mandat fédération
    occupation            member_occupation NOT NULL,
    start_date            DATE              NOT NULL,
    end_date              DATE              NOT NULL,
    is_federation_mandate BOOLEAN           NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_mandate_dates CHECK (end_date > start_date),
    CONSTRAINT chk_mandate_occupation CHECK (occupation IN ('PRESIDENT', 'VICE_PRESIDENT', 'TREASURER', 'SECRETARY')),
    -- Un seul titulaire par poste par collectivité à une période donnée
    CONSTRAINT uq_one_holder_per_post UNIQUE (collectivity_id, occupation, start_date)
);

CREATE INDEX idx_mandate_member ON mandate (member_id);
CREATE INDEX idx_mandate_collectivity ON mandate (collectivity_id);

-- ============================================================
-- TABLE : account
-- Comptes financiers d'une collectivité ou de la fédération
-- Types : CAISSE (unique par entité), BANK, MOBILE_MONEY
-- ============================================================
CREATE TABLE account
(
    id              VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    collectivity_id VARCHAR(36) REFERENCES collectivity (id) ON DELETE RESTRICT, -- NULL si compte fédération
    account_type    account_type   NOT NULL,
    balance         NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    currency        VARCHAR(10)    NOT NULL DEFAULT 'MGA',
    is_federation   BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Une seule caisse par collectivité ou par fédération
    CONSTRAINT uq_one_caisse_per_collectivity UNIQUE (collectivity_id, account_type)
        DEFERRABLE INITIALLY DEFERRED
);

-- Contrainte applicative : une seule caisse fédération
-- (gérée en service car collectivity_id = NULL pour la fédération)
CREATE INDEX idx_account_collectivity ON account (collectivity_id);

-- ============================================================
-- TABLE : bank_account_detail
-- Détails d'un compte bancaire (format RIB malgache)
-- BBBBBGGGGGCCCCCCCCCCCKKK = 23 chiffres
-- ============================================================
CREATE TABLE bank_account_detail
(
    account_id     VARCHAR(36) PRIMARY KEY REFERENCES account (id) ON DELETE CASCADE,
    holder_name    VARCHAR(150) NOT NULL,
    bank_name      bank_name    NOT NULL,
    bank_code      CHAR(5)      NOT NULL, -- BBBBB
    branch_code    CHAR(5)      NOT NULL, -- GGGGG
    account_number CHAR(11)     NOT NULL, -- CCCCCCCCCCC
    rib_key        CHAR(2)      NOT NULL, -- KK
    CONSTRAINT chk_bank_code_digits CHECK (bank_code ~ '^\d{5}$'),
    CONSTRAINT chk_branch_code_digits CHECK (branch_code ~ '^\d{5}$'),
    CONSTRAINT chk_account_num_digits CHECK (account_number ~ '^\d{11}$'),
    CONSTRAINT chk_rib_key_digits CHECK (rib_key ~ '^\d{2}$')
);

-- ============================================================
-- TABLE : mobile_money_account_detail
-- Détails d'un compte mobile money
-- ============================================================
CREATE TABLE mobile_money_account_detail
(
    account_id   VARCHAR(36) PRIMARY KEY REFERENCES account (id) ON DELETE CASCADE,
    holder_name  VARCHAR(150)         NOT NULL,
    service_name mobile_money_service NOT NULL,
    phone_number VARCHAR(20)          NOT NULL UNIQUE
);

-- ============================================================
-- TABLE : contribution
-- Cotisations versées par un membre à une collectivité
-- Le trésorier garde la trace : montant, date, mode de paiement
-- ============================================================
CREATE TABLE contribution
(
    id                VARCHAR(36) PRIMARY KEY    DEFAULT uuid_generate_v4(),
    collectivity_id   VARCHAR(36)       NOT NULL REFERENCES collectivity (id) ON DELETE RESTRICT,
    member_id         VARCHAR(36)       NOT NULL REFERENCES member (id) ON DELETE RESTRICT,
    amount            NUMERIC(15, 2)    NOT NULL,
    payment_date      DATE              NOT NULL DEFAULT CURRENT_DATE,
    payment_mode      payment_mode      NOT NULL,
    contribution_type contribution_type NOT NULL,
    description       VARCHAR(500),                        -- précision pour cotisations exceptionnelles
    account_id        VARCHAR(36) REFERENCES account (id), -- compte crédité
    created_at        TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_contribution_amount CHECK (amount > 0)
);

CREATE INDEX idx_contribution_collectivity ON contribution (collectivity_id);
CREATE INDEX idx_contribution_member ON contribution (member_id);
CREATE INDEX idx_contribution_date ON contribution (payment_date);

-- ============================================================
-- TABLE : activity
-- Activités planifiées par une collectivité ou la fédération
-- ============================================================
CREATE TABLE activity
(
    id                     VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    collectivity_id        VARCHAR(36) REFERENCES collectivity (id) ON DELETE RESTRICT, -- NULL si fédération
    activity_type          activity_type NOT NULL,
    title                  VARCHAR(255)  NOT NULL,
    activity_date          DATE          NOT NULL,
    is_mandatory           BOOLEAN       NOT NULL  DEFAULT FALSE,
    is_federation_activity BOOLEAN       NOT NULL  DEFAULT FALSE,
    description            VARCHAR(1000),
    created_at             TIMESTAMP     NOT NULL  DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_activity_collectivity ON activity (collectivity_id);
CREATE INDEX idx_activity_date ON activity (activity_date);

-- ============================================================
-- TABLE : activity_target
-- Membres ciblés par une activité (présence obligatoire)
-- Si vide = tous les membres de la collectivité sont concernés
-- ============================================================
CREATE TABLE activity_target
(
    activity_id VARCHAR(36) NOT NULL REFERENCES activity (id) ON DELETE CASCADE,
    member_id   VARCHAR(36) NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    PRIMARY KEY (activity_id, member_id)
);

-- ============================================================
-- TABLE : attendance
-- Fiche de présence à une activité (gérée par le secrétaire)
-- Un membre extérieur peut assister mais sa présence
-- n'est pas comptabilisée dans son taux d'assiduité
-- ============================================================
CREATE TABLE attendance
(
    id                 VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    activity_id        VARCHAR(36) NOT NULL REFERENCES activity (id) ON DELETE RESTRICT,
    member_id          VARCHAR(36) NOT NULL REFERENCES member (id) ON DELETE RESTRICT,
    is_present         BOOLEAN     NOT NULL    DEFAULT FALSE,
    is_excused         BOOLEAN     NOT NULL    DEFAULT FALSE,
    excuse_reason      VARCHAR(500),
    is_external_member BOOLEAN     NOT NULL    DEFAULT FALSE, -- membre d'une autre collectivité
    recorded_at        TIMESTAMP   NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_attendance_per_activity UNIQUE (activity_id, member_id),
    CONSTRAINT chk_excuse_requires_reason CHECK (
        is_excused = FALSE OR excuse_reason IS NOT NULL
        )
);

CREATE INDEX idx_attendance_activity ON attendance (activity_id);
CREATE INDEX idx_attendance_member ON attendance (member_id);

-- ============================================================
-- VUE : v_active_memberships
-- Membres actifs (non démissionnaires) par collectivité
-- ============================================================
CREATE VIEW v_active_memberships AS
SELECT cm.collectivity_id,
       cm.member_id,
       cm.joined_date,
       m.first_name,
       m.last_name,
       m.occupation,
       m.adhesion_date
FROM collectivity_member cm
         JOIN member m ON m.id = cm.member_id
WHERE cm.left_date IS NULL;

-- ============================================================
-- VUE : v_collectivity_seniority
-- Ancienneté des membres dans la fédération (en jours)
-- Utile pour les règles A (6 mois) et B (90 jours parrain)
-- ============================================================
CREATE VIEW v_member_seniority AS
SELECT m.id                           AS member_id,
       m.first_name,
       m.last_name,
       m.occupation,
       m.adhesion_date,
       CURRENT_DATE - m.adhesion_date AS seniority_days
FROM member m;

-- ============================================================
-- VUE : v_mandate_count
-- Nombre de mandats par membre par poste par collectivité
-- (max 2 autorisés, règle du sujet)
-- ============================================================
CREATE VIEW v_mandate_count AS
SELECT member_id,
       collectivity_id,
       occupation,
       COUNT(*) AS mandate_count
FROM mandate
GROUP BY member_id, collectivity_id, occupation;