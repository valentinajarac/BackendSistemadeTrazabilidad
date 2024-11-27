-- Eliminar tipos y tablas existentes si existen
DROP TYPE IF EXISTS role_type CASCADE;
DROP TYPE IF EXISTS user_status CASCADE;
DROP TYPE IF EXISTS product_type CASCADE;
DROP TYPE IF EXISTS crop_status CASCADE;
DROP TYPE IF EXISTS certification_type CASCADE;

DROP TABLE IF EXISTS remissions CASCADE;
DROP TABLE IF EXISTS crops CASCADE;
DROP TABLE IF EXISTS farms CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS user_certifications CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Crear tipos ENUM actualizados
CREATE TYPE role_type AS ENUM ('ADMIN', 'PRODUCER');
CREATE TYPE user_status AS ENUM ('ACTIVO', 'INACTIVO');
CREATE TYPE product_type AS ENUM ('UCHUVA', 'GULUPA');
CREATE TYPE crop_status AS ENUM ('PRODUCCION', 'VEGETACION');
CREATE TYPE certification_type AS ENUM ('FAIRTRADE_USA', 'GLOBAL_GAP', 'ICA', 'NINGUNA');


-- Crear tabla de usuarios
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    cedula VARCHAR(20) NOT NULL UNIQUE,
    nombre_completo VARCHAR(100) NOT NULL,
    codigo_trazabilidad VARCHAR(20) NOT NULL UNIQUE,
    municipio VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role role_type NOT NULL,
    user_status user_status NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de certificaciones de usuarios
CREATE TABLE user_certifications (
    user_id BIGINT NOT NULL,
    certification certification_type NOT NULL,
    CONSTRAINT pk_user_certifications PRIMARY KEY (user_id, certification),
    CONSTRAINT fk_user_certifications_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- Crear tabla de clientes
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    nit VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    floid VARCHAR(4) NOT NULL UNIQUE,
    direccion TEXT NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de fincas
CREATE TABLE farms (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    hectareas DECIMAL(10,2) NOT NULL,
    municipio VARCHAR(100) NOT NULL,
    vereda VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_farms_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_farms_nombre_user UNIQUE (nombre, user_id),
    CONSTRAINT chk_farms_hectareas CHECK (hectareas > 0)
);

-- Crear tabla de cultivos
CREATE TABLE crops (
    id BIGSERIAL PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    numero_plants INTEGER NOT NULL,
    hectareas DECIMAL(10,2) NOT NULL,
    fecha_siembra DATE NOT NULL,
    producto product_type NOT NULL,
    estado crop_status NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_crops_farm FOREIGN KEY (farm_id)
        REFERENCES farms(id) ON DELETE CASCADE,
    CONSTRAINT fk_crops_user FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT chk_crops_numero_plants CHECK (numero_plants > 0),
    CONSTRAINT chk_crops_hectareas CHECK (hectareas > 0)
);

-- Crear tabla de remisiones
CREATE TABLE remissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    farm_id BIGINT NOT NULL,
    crop_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    fecha_despacho DATE NOT NULL,
    canastillas_enviadas INTEGER NOT NULL,
    kilos_promedio DECIMAL(10,2) NOT NULL,
    total_kilos DECIMAL(10,2) NOT NULL,
    producto product_type NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_remissions_user FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT fk_remissions_farm FOREIGN KEY (farm_id)
        REFERENCES farms(id),
    CONSTRAINT fk_remissions_crop FOREIGN KEY (crop_id)
        REFERENCES crops(id),
    CONSTRAINT fk_remissions_client FOREIGN KEY (client_id)
        REFERENCES clients(id),
    CONSTRAINT chk_remissions_canastillas CHECK (canastillas_enviadas > 0),
    CONSTRAINT chk_remissions_kilos CHECK (kilos_promedio > 0)
);

-- Crear función para actualizar timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Crear triggers para actualizar timestamps
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_clients_updated_at
    BEFORE UPDATE ON clients
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_farms_updated_at
    BEFORE UPDATE ON farms
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_crops_updated_at
    BEFORE UPDATE ON crops
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_remissions_updated_at
    BEFORE UPDATE ON remissions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Crear función para actualizar estado del cultivo
CREATE OR REPLACE FUNCTION update_crop_status()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.fecha_siembra IS NOT NULL THEN
        IF NEW.fecha_siembra <= CURRENT_DATE - INTERVAL '4 months' THEN
            NEW.estado = 'PRODUCCION';
        ELSE
            NEW.estado = 'VEGETACION';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Crear trigger para actualizar estado del cultivo
CREATE TRIGGER trigger_update_crop_status
    BEFORE INSERT OR UPDATE OF fecha_siembra ON crops
    FOR EACH ROW
    EXECUTE FUNCTION update_crop_status();

-- Crear función para validar hectáreas de finca
CREATE OR REPLACE FUNCTION validate_farm_hectareas()
RETURNS TRIGGER AS $$
DECLARE
    total_hectareas DECIMAL;
    farm_hectareas DECIMAL;
BEGIN
    SELECT hectareas INTO farm_hectareas
    FROM farms
    WHERE id = NEW.farm_id;

    SELECT COALESCE(SUM(hectareas), 0) INTO total_hectareas
    FROM crops
    WHERE farm_id = NEW.farm_id
    AND id != COALESCE(NEW.id, -1);

    total_hectareas := total_hectareas + NEW.hectareas;

    IF total_hectareas > farm_hectareas THEN
        RAISE EXCEPTION 'La suma de hectáreas de los cultivos (%) excede las hectáreas disponibles en la finca (%)',
            total_hectareas, farm_hectareas;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Crear trigger para validar hectáreas
CREATE TRIGGER trigger_validate_farm_hectareas
    BEFORE INSERT OR UPDATE ON crops
    FOR EACH ROW
    EXECUTE FUNCTION validate_farm_hectareas();

-- Crear función para calcular total de kilos
CREATE OR REPLACE FUNCTION calculate_total_kilos()
RETURNS TRIGGER AS $$
BEGIN
    NEW.total_kilos = NEW.canastillas_enviadas * NEW.kilos_promedio;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Crear trigger para calcular total de kilos
CREATE TRIGGER trigger_calculate_total_kilos
    BEFORE INSERT OR UPDATE ON remissions
    FOR EACH ROW
    EXECUTE FUNCTION calculate_total_kilos();

-- Crear índices
CREATE INDEX idx_users_usuario ON users(usuario);
CREATE INDEX idx_users_cedula ON users(cedula);
CREATE INDEX idx_users_codigo_trazabilidad ON users(codigo_trazabilidad);
CREATE INDEX idx_users_status ON users(user_status);
CREATE INDEX idx_users_role ON users(role);

CREATE INDEX idx_clients_nit ON clients(nit);
CREATE INDEX idx_clients_floid ON clients(floid);
CREATE INDEX idx_clients_email ON clients(email);

CREATE INDEX idx_farms_user_id ON farms(user_id);
CREATE INDEX idx_farms_municipio ON farms(municipio);

CREATE INDEX idx_crops_farm_id ON crops(farm_id);
CREATE INDEX idx_crops_user_id ON crops(user_id);
CREATE INDEX idx_crops_producto ON crops(producto);
CREATE INDEX idx_crops_estado ON crops(estado);
CREATE INDEX idx_crops_fecha_siembra ON crops(fecha_siembra);

CREATE INDEX idx_remissions_user_id ON remissions(user_id);
CREATE INDEX idx_remissions_farm_id ON remissions(farm_id);
CREATE INDEX idx_remissions_crop_id ON remissions(crop_id);
CREATE INDEX idx_remissions_client_id ON remissions(client_id);
CREATE INDEX idx_remissions_fecha_despacho ON remissions(fecha_despacho);
CREATE INDEX idx_remissions_producto ON remissions(producto);

-- Insertar usuario administrador inicial (actualizado para usar ENUM)
INSERT INTO users (
    cedula,
    nombre_completo,
    codigo_trazabilidad,
    municipio,
    telefono,
    usuario,
    password,
    role,
    user_status
) VALUES (
    '1234567890',
    'Administrador Sistema',
    'ADMIN001',
    'Bogotá',
    '3001234567',
    'admin',
    '$2a$10$DaWzKFk1h3Vk8rKHGxvCp.LZpUk2qkKxY5Jy9H4ZEqD4kKvqFLtcO',
    'ADMIN',
    'ACTIVO'
);