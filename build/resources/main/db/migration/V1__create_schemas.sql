-- Enable PostGIS extension for spatial data
CREATE EXTENSION IF NOT EXISTS postgis;

-- Create users table for authentication
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       full_name VARCHAR(100) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create regions table
CREATE TABLE regions (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         name VARCHAR(100) NOT NULL,
                         province VARCHAR(100) NOT NULL,
                         district VARCHAR(100) NOT NULL,
                         boundary GEOMETRY(Polygon, 4326),
                         center_latitude DOUBLE PRECISION,
                         center_longitude DOUBLE PRECISION,
                         natural_region VARCHAR(100),
                         avg_annual_rainfall_mm DOUBLE PRECISION,
                         predominant_soil_type VARCHAR(100),
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create agricultural extension officers table
CREATE TABLE agricultural_extension_officers (
                                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                 name VARCHAR(100) NOT NULL,
                                                 contact_number VARCHAR(20) NOT NULL,
                                                 email VARCHAR(100) NOT NULL UNIQUE,
                                                 employee_id VARCHAR(50) NOT NULL UNIQUE,
                                                 qualification VARCHAR(200),
                                                 years_of_experience INTEGER NOT NULL DEFAULT 0,
                                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- AEO assigned regions (many-to-many relationship)
CREATE TABLE aeo_assigned_regions (
                                      aeo_id UUID REFERENCES agricultural_extension_officers(id),
                                      region_id UUID REFERENCES regions(id),
                                      PRIMARY KEY (aeo_id, region_id)
);

-- Create farmers table
CREATE TABLE farmers (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         name VARCHAR(100) NOT NULL,
                         age INTEGER NOT NULL,
                         gender VARCHAR(10) NOT NULL,
                         contact_number VARCHAR(20),
                         region_id UUID NOT NULL REFERENCES regions(id),
                         province VARCHAR(100) NOT NULL,
                         ward VARCHAR(100) NOT NULL,
                         natural_region VARCHAR(100),
                         soil_type VARCHAR(100),
                         uses_fertilizer BOOLEAN NOT NULL DEFAULT FALSE,
                         fertilizer_type VARCHAR(100),
                         manure_availability BOOLEAN NOT NULL DEFAULT FALSE,
                         uses_ploughing BOOLEAN NOT NULL DEFAULT FALSE,
                         uses_pfumvudza BOOLEAN NOT NULL DEFAULT FALSE,
                         access_to_credit BOOLEAN NOT NULL DEFAULT FALSE,
                         land_ownership_type VARCHAR(20) NOT NULL,
                         keeps_farm_records BOOLEAN NOT NULL DEFAULT FALSE,
                         farm_size_hectares DOUBLE PRECISION NOT NULL,
                         previous_planted_crop VARCHAR(100),
                         previous_season_yield_kg DOUBLE PRECISION,
                         average_yield_per_season_kg DOUBLE PRECISION,
                         compliance_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
                         land_preparation_type VARCHAR(50),
                         soil_testing_done BOOLEAN NOT NULL DEFAULT FALSE,
                         planting_date TIMESTAMP,
                         observed_off_types BOOLEAN NOT NULL DEFAULT FALSE,
                         herbicides_used VARCHAR(200),
                         aeo_visit_frequency VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
                         has_crop_insurance BOOLEAN NOT NULL DEFAULT FALSE,
                         receives_govt_subsidies BOOLEAN NOT NULL DEFAULT FALSE,
                         uses_agroforestry BOOLEAN NOT NULL DEFAULT FALSE,
                         input_cost_per_season DOUBLE PRECISION,
                         main_source_of_inputs VARCHAR(100),
                         social_vulnerability VARCHAR(20) NOT NULL DEFAULT 'LOW',
                         education_level VARCHAR(20) NOT NULL DEFAULT 'PRIMARY',
                         household_size INTEGER NOT NULL DEFAULT 1,
                         num_dependents INTEGER NOT NULL DEFAULT 0,
                         marital_status VARCHAR(20) NOT NULL DEFAULT 'SINGLE',
                         aeo_id UUID REFERENCES agricultural_extension_officers(id),
                         needs_support BOOLEAN NOT NULL DEFAULT FALSE,
                         support_reason TEXT,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         last_updated_by VARCHAR(100)
);

-- Create farmer farming practices (one-to-many)
CREATE TABLE farmer_farming_practices (
                                          farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
                                          farming_practice VARCHAR(100) NOT NULL,
                                          PRIMARY KEY (farmer_id, farming_practice)
);

-- Create farmer conservation practices (one-to-many)
CREATE TABLE farmer_conservation_practices (
                                               farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
                                               conservation_practice VARCHAR(100) NOT NULL,
                                               PRIMARY KEY (farmer_id, conservation_practice)
);

-- Create farmer agronomic practices (one-to-many)
CREATE TABLE farmer_agronomic_practices (
                                            farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
                                            agronomic_practice VARCHAR(100) NOT NULL,
                                            PRIMARY KEY (farmer_id, agronomic_practice)
);

-- Create farmer problematic pests (one-to-many)
CREATE TABLE farmer_problematic_pests (
                                          farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
                                          pest_name VARCHAR(100) NOT NULL,
                                          PRIMARY KEY (farmer_id, pest_name)
);

-- Create farmer challenges (one-to-many)
CREATE TABLE farmer_challenges (
                                   farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
                                   challenge VARCHAR(200) NOT NULL,
                                   PRIMARY KEY (farmer_id, challenge)
);

-- Create contracts table
CREATE TABLE contracts (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           farmer_id UUID NOT NULL REFERENCES farmers(id),
                           contract_number VARCHAR(50) NOT NULL UNIQUE,
                           start_date DATE NOT NULL,
                           end_date DATE NOT NULL,
                           type VARCHAR(20) NOT NULL,
                           expected_delivery_kg DOUBLE PRECISION NOT NULL,
                           price_per_kg DOUBLE PRECISION,
                           advance_payment DOUBLE PRECISION,
                           input_support_value DOUBLE PRECISION,
                           signing_bonus DOUBLE PRECISION,
                           repayment_status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
                           total_repaid_amount DOUBLE PRECISION NOT NULL DEFAULT 0,
                           total_owed_amount DOUBLE PRECISION NOT NULL DEFAULT 0,
                           challenges_meeting_terms TEXT,
                           has_loan_component BOOLEAN NOT NULL DEFAULT FALSE,
                           active BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create deliveries table
CREATE TABLE deliveries (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            contract_id UUID NOT NULL REFERENCES contracts(id),
                            delivery_date TIMESTAMP NOT NULL,
                            quantity_kg DOUBLE PRECISION NOT NULL,
                            quality_grade VARCHAR(20) NOT NULL,
                            moisture_content DOUBLE PRECISION,
                            price_paid_per_kg DOUBLE PRECISION,
                            total_amount_paid DOUBLE PRECISION,
                            deduction_amount DOUBLE PRECISION NOT NULL DEFAULT 0,
                            deduction_reason TEXT,
                            receipt_number VARCHAR(50),
                            verified_by VARCHAR(100),
                            verified_at TIMESTAMP,
                            notes TEXT,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_farmers_region ON farmers(region_id);
CREATE INDEX idx_farmers_aeo ON farmers(aeo_id);
CREATE INDEX idx_farmers_needs_support ON farmers(needs_support);
CREATE INDEX idx_contracts_farmer ON contracts(farmer_id);
CREATE INDEX idx_contracts_active ON contracts(active);
CREATE INDEX idx_contracts_repayment_status ON contracts(repayment_status);
CREATE INDEX idx_deliveries_contract ON deliveries(contract_id);
CREATE INDEX idx_regions_province ON regions(province);
CREATE INDEX idx_regions_district ON regions(district);
CREATE INDEX idx_aeo_email ON agricultural_extension_officers(email);
CREATE INDEX idx_aeo_employee_id ON agricultural_extension_officers(employee_id);

-- Create spatial index on region boundary
CREATE INDEX idx_regions_boundary ON regions USING GIST(boundary);