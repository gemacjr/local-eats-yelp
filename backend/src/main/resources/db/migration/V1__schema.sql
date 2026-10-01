CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    display_name  VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE businesses (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(200)     NOT NULL,
    description  TEXT,
    category     VARCHAR(50)      NOT NULL,
    address      VARCHAR(300)     NOT NULL,
    phone        VARCHAR(30),
    website      VARCHAR(300),
    latitude     DOUBLE PRECISION NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude    DOUBLE PRECISION NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    -- Denormalized rating aggregates (deep dive #1): read on every search, updated on each review.
    avg_rating   DOUBLE PRECISION NOT NULL DEFAULT 0,
    num_ratings  INTEGER          NOT NULL DEFAULT 0,
    -- Optimistic-locking version used when applying a new rating concurrently.
    version      BIGINT           NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ      NOT NULL DEFAULT now(),
    -- Derived columns maintained by Postgres, never written by the app.
    geog          GEOGRAPHY(POINT, 4326)
        GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography) STORED,
    search_vector TSVECTOR
        GENERATED ALWAYS AS (
            setweight(to_tsvector('english', coalesce(name, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(category, '')), 'B') ||
            setweight(to_tsvector('english', coalesce(description, '')), 'C')
        ) STORED
);

CREATE INDEX idx_businesses_geog     ON businesses USING GIST (geog);
CREATE INDEX idx_businesses_search   ON businesses USING GIN (search_vector);
CREATE INDEX idx_businesses_name_trgm ON businesses USING GIN (name gin_trgm_ops);
CREATE INDEX idx_businesses_category ON businesses (category);
CREATE INDEX idx_businesses_rating   ON businesses (avg_rating DESC);

CREATE TABLE reviews (
    id          BIGSERIAL PRIMARY KEY,
    business_id BIGINT      NOT NULL REFERENCES businesses (id) ON DELETE CASCADE,
    user_id     BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    rating      SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    text        TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Deep dive #2: one review per user per business, enforced by the database.
    CONSTRAINT uq_reviews_user_business UNIQUE (user_id, business_id)
);

CREATE INDEX idx_reviews_business_created ON reviews (business_id, created_at DESC);

-- Deep dive #4: named areas (cities, neighborhoods) as polygons.
CREATE TABLE locations (
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    slug     VARCHAR(100) NOT NULL UNIQUE,
    type     VARCHAR(20)  NOT NULL CHECK (type IN ('CITY', 'NEIGHBORHOOD')),
    boundary GEOGRAPHY(POLYGON, 4326) NOT NULL
);

CREATE INDEX idx_locations_boundary ON locations USING GIST (boundary);

-- Precomputed "business is inside location" at write time, so name-based search is an index lookup.
CREATE TABLE business_locations (
    business_id BIGINT NOT NULL REFERENCES businesses (id) ON DELETE CASCADE,
    location_id BIGINT NOT NULL REFERENCES locations (id) ON DELETE CASCADE,
    PRIMARY KEY (location_id, business_id)
);

CREATE TABLE business_photos (
    id          BIGSERIAL PRIMARY KEY,
    business_id BIGINT       NOT NULL REFERENCES businesses (id) ON DELETE CASCADE,
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    s3_key      VARCHAR(500) NOT NULL UNIQUE,
    caption     VARCHAR(300),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_photos_business ON business_photos (business_id, created_at DESC);
