
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'estado_motor' AND column_name = 'consumo_agua') THEN
        ALTER TABLE estado_motor ADD COLUMN consumo_agua DOUBLE PRECISION DEFAULT 0.0;
        COMMENT ON COLUMN estado_motor.consumo_agua IS 'Cantidad de agua consumida en litros durante este período';
    END IF;
END $$;


DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'eventos_motor' AND column_name = 'consumo_agua') THEN
        ALTER TABLE eventos_motor ADD COLUMN consumo_agua DOUBLE PRECISION DEFAULT 0.0;
        COMMENT ON COLUMN eventos_motor.consumo_agua IS 'Agua consumida durante este evento en litros';
    END IF;
END $$;


DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'limites' AND column_name = 'consumo_agua_max') THEN
        ALTER TABLE limites ADD COLUMN consumo_agua_max DOUBLE PRECISION NOT NULL DEFAULT 10.0;
        COMMENT ON COLUMN limites.consumo_agua_max IS 'Consumo máximo de agua permitido por riego (litros)';
        

        UPDATE limites SET consumo_agua_max = 10.0;
    END IF;
END $$;


COMMENT ON COLUMN estado_motor.estado IS 'TRUE=encendido, FALSE=apagado';
COMMENT ON COLUMN estado_motor.duracion_segundos IS 'Duración en segundos del estado actual';
COMMENT ON COLUMN estado_motor.motivo IS 'automatico, manual, fiware';
COMMENT ON COLUMN estado_motor.humedad_suelo_actual IS 'Humedad del suelo al cambiar estado';
COMMENT ON COLUMN estado_motor.temperatura_actual IS 'Temperatura ambiente al cambiar estado';

COMMENT ON COLUMN eventos_motor.accion IS 'ENCENDIDO o APAGADO';
COMMENT ON COLUMN eventos_motor.motivo IS 'Razón del cambio de estado';


