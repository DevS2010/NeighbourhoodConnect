-- NeighbourConnect PostgreSQL 16 + PostGIS Production Schema
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

CREATE TYPE user_role AS ENUM ('CUSTOMER', 'PROVIDER', 'ADMIN');
CREATE TYPE subscription_tier AS ENUM ('FREE', 'PRO', 'FEATURED');
CREATE TYPE booking_status AS ENUM (
    'REQUESTED',
    'QUOTE_SUBMITTED',
    'ESCROW_FUNDED',
    'PROVIDER_EN_ROUTE',
    'IN_PROGRESS',
    'COMPLETED',
    'CANCELLED'
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    role user_role NOT NULL DEFAULT 'CUSTOMER',
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'en',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE provider_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id VARCHAR(64) NOT NULL,
    custom_profession_title VARCHAR(120),
    bio TEXT NOT NULL,
    experience_years INT NOT NULL DEFAULT 1,
    neighborhood VARCHAR(120) NOT NULL,
    city VARCHAR(80) NOT NULL,
    location GEOMETRY(Point, 4326) NOT NULL,
    is_online BOOLEAN NOT NULL DEFAULT TRUE,
    accepts_emergency BOOLEAN NOT NULL DEFAULT FALSE,
    aadhaar_kyc_verified BOOLEAN NOT NULL DEFAULT FALSE,
    trade_license_verified BOOLEAN NOT NULL DEFAULT FALSE,
    government_id_masked VARCHAR(24) NOT NULL,
    kyc_points INT NOT NULL DEFAULT 25,
    completion_points INT NOT NULL DEFAULT 30,
    review_points INT NOT NULL DEFAULT 20,
    punctuality_points INT NOT NULL DEFAULT 15,
    trust_score INT GENERATED ALWAYS AS (kyc_points + completion_points + review_points + punctuality_points) STORED,
    ai_profile_quality_score INT NOT NULL DEFAULT 90,
    tier subscription_tier NOT NULL DEFAULT 'FREE',
    base_visiting_fee_inr INT NOT NULL DEFAULT 199,
    upi_id VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_provider_profiles_location_gist ON provider_profiles USING GIST (location);
CREATE INDEX idx_provider_profiles_category_trust ON provider_profiles (category_id, trust_score DESC);

CREATE TABLE bookings (
    id VARCHAR(32) PRIMARY KEY,
    customer_id UUID REFERENCES users(id),
    provider_id UUID NOT NULL REFERENCES provider_profiles(id),
    customer_address TEXT NOT NULL,
    issue_summary TEXT NOT NULL,
    scheduled_slot VARCHAR(120) NOT NULL,
    is_emergency_sos BOOLEAN NOT NULL DEFAULT FALSE,
    status booking_status NOT NULL DEFAULT 'REQUESTED',
    labor_cost_inr INT NOT NULL,
    parts_cost_inr INT NOT NULL DEFAULT 0,
    gst_amount_inr INT NOT NULL,
    platform_commission_inr INT NOT NULL,
    provider_net_payout_inr INT NOT NULL,
    completion_otp CHAR(4) NOT NULL,
    razorpay_order_id VARCHAR(64),
    invoice_number VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
