
DROP DATABASE IF EXISTS registro_invernadero;


CREATE DATABASE registro_invernadero;


GRANT ALL PRIVILEGES ON DATABASE registro_invernadero TO dit;


ALTER DATABASE registro_invernadero OWNER TO dit;

DROP TABLE IF EXISTS horas_riego CASCADE;
DROP TABLE IF EXISTS limites CASCADE;
DROP TABLE IF EXISTS limites_horas_riego CASCADE;
DROP TABLE IF EXISTS estado_motor CASCADE;
DROP TABLE IF EXISTS eventos_motor CASCADE;
DROP TABLE IF EXISTS lecturas_sensores CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;


DROP INDEX IF EXISTS idx_limites_actualizacion;
DROP INDEX IF EXISTS idx_usuarios_nombre;
DROP INDEX IF EXISTS idx_lecturas_fecha;
DROP INDEX IF EXISTS idx_motor_estado;
DROP INDEX IF EXISTS idx_eventos_accion;
DROP INDEX IF EXISTS idx_horas_riego_limite;


ALTER TABLE IF EXISTS usuarios DROP CONSTRAINT IF EXISTS uk_email;
-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    nombre_usuario TEXT UNIQUE NOT NULL,
    contraseña TEXT NOT NULL,
    email VARCHAR(100) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP,
    nivel_acceso INTEGER DEFAULT 1
);

ALTER TABLE usuarios 
ADD CONSTRAINT uk_email UNIQUE (email);

-- Tabla para el registro de las lecturas de los sensores
CREATE TABLE IF NOT EXISTS lecturas_sensores (
    id SERIAL PRIMARY KEY,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nivel_agua INTEGER NOT NULL,
    luz_ambiente INTEGER NOT NULL,
    humedad_suelo INTEGER NOT NULL,
    humedad_ambiental DOUBLE PRECISION NOT NULL,
    temperatura_c DOUBLE PRECISION NOT NULL,
    temperatura_f DOUBLE PRECISION NOT NULL
);

-- Tabla para el registro del estado del motor
CREATE TABLE IF NOT EXISTS estado_motor (
    id SERIAL PRIMARY KEY,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado BOOLEAN NOT NULL,
    duracion_segundos INTEGER,
    consumo_agua DOUBLE PRECISION DEFAULT 0.0,
    motivo VARCHAR(20) NOT NULL
);

-- Tabla para el regitro de los eventos del motor
CREATE TABLE IF NOT EXISTS eventos_motor (
    id SERIAL PRIMARY KEY,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accion VARCHAR(9) CHECK (accion IN ('ENCENDIDO', 'APAGADO')) NOT NULL,
    motivo VARCHAR(20) NOT NULL,
    duracion_segundos INTEGER,
    consumo_agua DOUBLE PRECISION DEFAULT 0.0
);

-- Tabla Límites
CREATE TABLE IF NOT EXISTS limites (
    id SERIAL PRIMARY KEY,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    temp_min DOUBLE PRECISION NOT NULL,
    temp_max DOUBLE PRECISION NOT NULL,
    humedad_amb_min DOUBLE PRECISION NOT NULL,
    humedad_amb_max DOUBLE PRECISION NOT NULL,
    humedad_suelo_min INTEGER NOT NULL,
    humedad_suelo_max INTEGER NOT NULL,
    luz_min INTEGER NOT NULL,
    luz_max INTEGER NOT NULL,
    consumo_agua_max DOUBLE PRECISION NOT NULL DEFAULT 10.0,
    volumen_agua_min DOUBLE PRECISION NOT NULL DEFAULT 20.0,
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    usuario_actualizacion TEXT NOT NULL
);

-- Tabla secundaria para las Horas de riego
CREATE TABLE IF NOT EXISTS limites_horas_riego (
    id SERIAL PRIMARY KEY,
    limite_id INTEGER NOT NULL REFERENCES limites(id) ON DELETE CASCADE,
    hora_riego TIME NOT NULL
);


CREATE INDEX idx_lecturas_fecha ON lecturas_sensores(fecha_creacion);
CREATE INDEX idx_motor_estado ON estado_motor(estado);
CREATE INDEX idx_eventos_accion ON eventos_motor(accion);
CREATE INDEX idx_limites_actualizacion ON limites(fecha_actualizacion);
CREATE INDEX idx_usuarios_nombre ON usuarios(nombre_usuario);
CREATE INDEX idx_horas_riego_limite ON limites_horas_riego(limite_id);


INSERT INTO usuarios (nombre_usuario, contraseña, email, nivel_acceso)
VALUES ('admin', 'admin','joseantoniopozo03@gmail.com', 3);

INSERT INTO limites (
    temp_min, temp_max,
    humedad_amb_min, humedad_amb_max,
    humedad_suelo_min, humedad_suelo_max,
    luz_min, luz_max,
    consumo_agua_max,
    volumen_agua_min,
    usuario_id,
    usuario_actualizacion
) VALUES (
    18.0, 28.0,
    40.0, 70.0,
    300, 700,
    100, 900,
    15.0,
    20.0,
    (SELECT id FROM usuarios WHERE nombre_usuario = 'admin'),
    'admin'
);


INSERT INTO limites_horas_riego (limite_id, hora_riego)
VALUES 
    (1, '08:00:00'::TIME),
    (1, '20:00:00'::TIME);

