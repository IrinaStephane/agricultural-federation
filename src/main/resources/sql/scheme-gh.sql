-- Enum types
CREATE TYPE collectivity_occupation AS ENUM ('PRESIDENT','VICE_PRESIDENT','TREASURER','SECRETARY','SENIOR','JUNIOR');
CREATE TYPE activity_type AS ENUM ('MEETING','TRAINING','OTHER');
CREATE TYPE attendance_status AS ENUM ('MISSING','ATTENDED','UNDEFINED');
CREATE TYPE week_day AS ENUM ('MO','TU','WE','TH','FR','SA','SU');
CREATE TYPE federation_occupation AS ENUM ('PRESIDENT','VICE_PRESIDENT','TREASURER','SECRETARY');
CREATE TYPE gender AS ENUM ('MALE','FEMALE');
CREATE TYPE cotisation_frequency AS ENUM ('WEEKLY','MONTHLY','ANNUALLY','PUNCTUALLY');
CREATE TYPE payment_mode AS ENUM ('CASH','MOBILE_BANKING','BANK_TRANSFER');
CREATE TYPE bank_name AS ENUM ('BRED','MCB','BMOI','BOA','BGFI','AFG','ACCES_BANQUE','BAOBAB','SIPEM');
CREATE TYPE mobile_money_service AS ENUM ('ORANGE_MONEY','MVOLA','AIRTEL_MONEY');
CREATE TYPE transaction_type AS ENUM ('IN','OUT');

-- Core tables
CREATE TABLE IF NOT EXISTS "public"."member"
(
    "id"             varchar   NOT NULL,
    "first_name"     varchar   NOT NULL,
    "last_name"      varchar   NOT NULL,
    "birth_date"     date      NOT NULL,
    "enrolment_date" timestamp NOT NULL,
    "address"        text      NOT NULL,
    "email"          varchar   NOT NULL UNIQUE,
    "phone_number"   varchar   NOT NULL UNIQUE,
    "profession"     varchar   NOT NULL,
    "gender"         gender    NOT NULL,
    PRIMARY KEY ("id")
    );

CREATE TABLE IF NOT EXISTS "public"."federation"
(
    "id"                    varchar       NOT NULL,
    "cotisation_percentage" numeric(5, 2) NOT NULL DEFAULT 10.00,
    PRIMARY KEY ("id")
    );

CREATE TABLE IF NOT EXISTS "public"."collectivity"
(
    "id"                  varchar   NOT NULL,
    "number"              varchar            UNIQUE,
    "name"                varchar            UNIQUE,
    "speciality"          varchar,
    "creation_datetime"   timestamp NOT NULL,
    "federation_approval" boolean            DEFAULT FALSE,
    "authorization_date"  timestamp,
    "id_federation"       varchar   NOT NULL,
    "location"            varchar   NOT NULL,
    PRIMARY KEY ("id")
    );

CREATE TABLE IF NOT EXISTS "public"."member_collectivity"
(
    "id"              varchar                 NOT NULL,
    "id_member"       varchar                 NOT NULL,
    "id_collectivity" varchar                 NOT NULL,
    "occupation"      collectivity_occupation NOT NULL,
    "start_date"      timestamp               NOT NULL,
    "end_date"        timestamp,
    PRIMARY KEY ("id")
    );

CREATE TABLE IF NOT EXISTS "public"."member_referee"
(
    "id"              varchar   NOT NULL,
    "id_candidate"    varchar   NOT NULL,
    "id_referee"      varchar   NOT NULL,
    "id_collectivity" varchar   NOT NULL,
    "relationship"    varchar   NOT NULL,
    "created_at"      timestamp NOT NULL DEFAULT NOW(),
    PRIMARY KEY ("id"),
    UNIQUE ("id_candidate", "id_referee")
    );

CREATE TABLE IF NOT EXISTS "public"."mandate_federation"
(
    "id"            varchar               NOT NULL,
    "id_member"     varchar               NOT NULL,
    "id_federation" varchar               NOT NULL,
    "occupation"    federation_occupation NOT NULL,
    "start_date"    timestamp             NOT NULL,
    "end_date"      timestamp,
    PRIMARY KEY ("id")
    );

-- Membership fee

CREATE TABLE IF NOT EXISTS "public"."membership_fee"
(
    "id"              varchar              NOT NULL,
    "id_collectivity" varchar              NOT NULL,
    "label"           varchar              NOT NULL,
    "frequency"       cotisation_frequency NOT NULL,
    "amount"          numeric(15, 2)       NOT NULL,
    "eligible_from"   date,
    "is_active"       boolean              NOT NULL DEFAULT true,
    PRIMARY KEY ("id")
    );

-- Accounts

CREATE TABLE IF NOT EXISTS "public"."account"
(
    "id"              varchar        NOT NULL,
    "id_collectivity" varchar,
    "id_federation"   varchar,
    "balance"         numeric(15, 2) NOT NULL DEFAULT 0,
    PRIMARY KEY ("id"),
    CONSTRAINT "chk_account_owner" CHECK (
("id_collectivity" IS NOT NULL AND "id_federation" IS NULL) OR
("id_collectivity" IS NULL  AND "id_federation" IS NOT NULL)
    )
    );

CREATE TABLE IF NOT EXISTS "public"."cash_account"
(
    "id"         varchar NOT NULL,
    "id_account" varchar NOT NULL UNIQUE,
    PRIMARY KEY ("id"),
    CONSTRAINT "fk_cash_account"
    FOREIGN KEY ("id_account") REFERENCES "public"."account" ("id") ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS "public"."bank_account"
(
    "id"             varchar   NOT NULL,
    "id_account"     varchar   NOT NULL UNIQUE,
    "holder_name"    varchar   NOT NULL,
    "bank_name"      bank_name NOT NULL,
    "bank_code"      char(5)   NOT NULL,
    "branch_code"    char(5)   NOT NULL,
    "account_number" char(11)  NOT NULL,
    "rib_key"        char(2)   NOT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "fk_bank_account"
    FOREIGN KEY ("id_account") REFERENCES "public"."account" ("id") ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS "public"."mobile_money_account"
(
    "id"           varchar              NOT NULL,
    "id_account"   varchar              NOT NULL UNIQUE,
    "holder_name"  varchar              NOT NULL,
    "service_name" mobile_money_service NOT NULL,
    "phone_number" varchar              NOT NULL UNIQUE,
    PRIMARY KEY ("id"),
    CONSTRAINT "fk_mobile_account"
    FOREIGN KEY ("id_account") REFERENCES "public"."account" ("id") ON DELETE CASCADE
    );

-- Transactions

CREATE TABLE IF NOT EXISTS "public"."transaction"
(
    "id"                 varchar          NOT NULL,
    "id_member"          varchar          NOT NULL,
    "id_collectivity"    varchar          NOT NULL,
    "id_membership_fee"  varchar,
    "id_account"         varchar          NOT NULL,
    "transaction_type"   transaction_type NOT NULL DEFAULT 'IN',
    "amount"             numeric(15, 2)   NOT NULL,
    "transaction_date"   timestamp        NOT NULL DEFAULT NOW(),
    "payment_mode"       payment_mode,
    "description"        text,
    PRIMARY KEY ("id")
    );

-- Foreign keys

ALTER TABLE "public"."collectivity"
DROP CONSTRAINT IF EXISTS fk_collectivity_federation,
    ADD CONSTRAINT "fk_collectivity_federation"
        FOREIGN KEY ("id_federation") REFERENCES "public"."federation" ("id");

ALTER TABLE "public"."member_collectivity"
DROP CONSTRAINT IF EXISTS fk_member_collectivity_member,
    DROP CONSTRAINT IF EXISTS fk_member_collectivity_collectivity,
    ADD CONSTRAINT "fk_member_collectivity_member"
        FOREIGN KEY ("id_member") REFERENCES "public"."member" ("id"),
    ADD CONSTRAINT "fk_member_collectivity_collectivity"
        FOREIGN KEY ("id_collectivity") REFERENCES "public"."collectivity" ("id");

ALTER TABLE "public"."member_referee"
DROP CONSTRAINT IF EXISTS fk_member_referee_candidate,
    DROP CONSTRAINT IF EXISTS fk_member_referee_referee,
    DROP CONSTRAINT IF EXISTS fk_member_referee_collectivity,
    ADD CONSTRAINT "fk_member_referee_candidate"
        FOREIGN KEY ("id_candidate") REFERENCES "public"."member" ("id"),
    ADD CONSTRAINT "fk_member_referee_referee"
        FOREIGN KEY ("id_referee") REFERENCES "public"."member" ("id"),
    ADD CONSTRAINT "fk_member_referee_collectivity"
        FOREIGN KEY ("id_collectivity") REFERENCES "public"."collectivity" ("id");

ALTER TABLE "public"."mandate_federation"
DROP CONSTRAINT IF EXISTS fk_mandate_federation_member,
    DROP CONSTRAINT IF EXISTS fk_mandate_federation_federation,
    ADD CONSTRAINT "fk_mandate_federation_member"
        FOREIGN KEY ("id_member") REFERENCES "public"."member" ("id"),
    ADD CONSTRAINT "fk_mandate_federation_federation"
        FOREIGN KEY ("id_federation") REFERENCES "public"."federation" ("id");

ALTER TABLE "public"."membership_fee"
DROP CONSTRAINT IF EXISTS fk_membership_fee_collectivity,
    ADD CONSTRAINT "fk_membership_fee_collectivity"
        FOREIGN KEY ("id_collectivity") REFERENCES "public"."collectivity" ("id");

ALTER TABLE "public"."transaction"
DROP CONSTRAINT IF EXISTS fk_transaction_member,
    DROP CONSTRAINT IF EXISTS fk_transaction_collectivity,
    DROP CONSTRAINT IF EXISTS fk_transaction_membership_fee,
    DROP CONSTRAINT IF EXISTS fk_transaction_account,
    ADD CONSTRAINT "fk_transaction_member"
        FOREIGN KEY ("id_member") REFERENCES "public"."member" ("id"),
    ADD CONSTRAINT "fk_transaction_collectivity"
        FOREIGN KEY ("id_collectivity") REFERENCES "public"."collectivity" ("id"),
    ADD CONSTRAINT "fk_transaction_membership_fee"
        FOREIGN KEY ("id_membership_fee") REFERENCES "public"."membership_fee" ("id"),
    ADD CONSTRAINT "fk_transaction_account"
        FOREIGN KEY ("id_account") REFERENCES "public"."account" ("id");

ALTER TABLE "public"."account"
DROP CONSTRAINT IF EXISTS fk_account_collectivity,
    DROP CONSTRAINT IF EXISTS fk_account_federation,
    ADD CONSTRAINT "fk_account_collectivity"
        FOREIGN KEY ("id_collectivity") REFERENCES "public"."collectivity" ("id"),
    ADD CONSTRAINT "fk_account_federation"
        FOREIGN KEY ("id_federation") REFERENCES "public"."federation" ("id");
-- ─── Activities (Feature E) ────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS "public"."collectivity_activity"
(
    "id"                       varchar       NOT NULL,
    "id_collectivity"          varchar       NOT NULL,
    "label"                    varchar       NOT NULL,
    "activity_type"            activity_type NOT NULL,
    "executive_date"           date,
    "recurrence_week_ordinal"  integer,
    "recurrence_day_of_week"   week_day,
    PRIMARY KEY ("id"),
    CONSTRAINT "chk_activity_date_or_recurrence" CHECK (
("executive_date" IS NOT NULL AND "recurrence_week_ordinal" IS NULL AND "recurrence_day_of_week" IS NULL)
    OR ("executive_date" IS NULL)
    )
    );

CREATE TABLE IF NOT EXISTS "public"."activity_occupation_concerned"
(
    "id_activity"  varchar                 NOT NULL,
    "occupation"   collectivity_occupation NOT NULL,
    PRIMARY KEY ("id_activity", "occupation")
    );

-- ─── Attendance (Feature F) ────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS "public"."activity_attendance"
(
    "id"                varchar           NOT NULL,
    "id_activity"       varchar           NOT NULL,
    "id_member"         varchar           NOT NULL,
    "attendance_status" attendance_status NOT NULL DEFAULT 'UNDEFINED',
    PRIMARY KEY ("id"),
    UNIQUE ("id_activity", "id_member")
    );

-- Foreign keys for new tables
ALTER TABLE "public"."collectivity_activity"
DROP CONSTRAINT IF EXISTS fk_activity_collectivity,
    ADD CONSTRAINT "fk_activity_collectivity"
        FOREIGN KEY ("id_collectivity") REFERENCES "public"."collectivity" ("id");

ALTER TABLE "public"."activity_occupation_concerned"
DROP CONSTRAINT IF EXISTS fk_aoc_activity,
    ADD CONSTRAINT "fk_aoc_activity"
        FOREIGN KEY ("id_activity") REFERENCES "public"."collectivity_activity" ("id");

ALTER TABLE "public"."activity_attendance"
DROP CONSTRAINT IF EXISTS fk_attendance_activity,
    DROP CONSTRAINT IF EXISTS fk_attendance_member,
    ADD CONSTRAINT "fk_attendance_activity"
        FOREIGN KEY ("id_activity") REFERENCES "public"."collectivity_activity" ("id"),
    ADD CONSTRAINT "fk_attendance_member"
        FOREIGN KEY ("id_member") REFERENCES "public"."member" ("id");