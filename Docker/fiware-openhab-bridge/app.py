from flask import Flask, request
import requests

app = Flask(__name__)

# Dirección de OpenHAB (ajusta si no está en localhost dentro del contenedor)
OPENHAB_URL = "http://host.docker.internal:8080/rest/items"

MAPA_ITEMS = {
    "consumo_agua_max": "LimiteConsumoAguaMax",
    "horas_riego": "LimiteHorasRiego",
    "humedad_amb_min": "LimiteHumedadAmbMin",
    "humedad_amb_max": "LimiteHumedadAmbMax",
    "humedad_suelo_min": "LimiteHumedadSueloMin",
    "humedad_suelo_max": "LimiteHumedadSueloMax",
    "luz_min": "LimiteLuzMin",
    "luz_max": "LimiteLuzMax",
    "temp_min": "LimiteTempMin",
    "temp_max": "LimiteTempMax",
    "volumen_agua_min": "LimiteVolumenAguaMin"
}

@app.route('/notify', methods=['POST'])
def recibir_notificacion():
    data = request.json
    if not data or 'data' not in data:
        return "Formato incorrecto", 400

    entidad = data['data'][0]
    print("🔔 Recibido:", entidad)
    for attr, item_name in MAPA_ITEMS.items():
         if attr in entidad:
            valor = entidad[attr]['value']
            if isinstance(valor, list):
                valor = str(valor)
            url = f"{OPENHAB_URL}/{item_name}"
            response = requests.post(url, data=str(valor), headers={"Content-Type": "text/plain"})
            print(f"{item_name} ← {valor} → {response.status_code}")
         else:
            print(f"⚠️ Atributo '{attr}' no presente en la entidad")

    return "OK", 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
