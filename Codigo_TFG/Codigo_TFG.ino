#include <DHT.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <WebServer.h>
#include <ArduinoJson.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <vector>

// =================================
// === CONFIGURACIÓN DE LOS LOGS ===
// =================================
enum LogLevel { ERROR_LEVEL, INFO_LEVEL, DEBUG_LEVEL };
LogLevel currentLogLevel = INFO_LEVEL;
#define LOG_ERROR(x) if (loggingEnabled && currentLogLevel >= ERROR_LEVEL) { Serial.print("[ERROR] "); Serial.println(x); }
#define LOG_INFO(x)  if (loggingEnabled && currentLogLevel >= INFO_LEVEL) { Serial.print("[INFO]  "); Serial.println(x); }
#define LOG_DEBUG(x) if (loggingEnabled && currentLogLevel >= DEBUG_LEVEL) { Serial.print("[DEBUG] "); Serial.println(x); }
bool loggingEnabled = true;  

// ===========================
// === CONFIGURACIÓN DEL HW ==
// ===========================
#define DHT_SENSOR_PIN  4
#define DHT_SENSOR_TYPE DHT11
#define AO_PIN 34
#define SIGNAL_PIN 35
#define HUMEDAD_PIN 33
#define MOTOR_PIN 25
#define AIRE_PIN 26
#define BOMBILLA_PIN 27

// ==============================
// === CONFIGURACIÓN DEL WiFi ===
// ==============================
const char* ssid = "WICOM-POZITO";
const char* password = "11102006";

// URLs para la lectura de los elementos de FIWARE
const char* fiware_sensor_url = "http://192.168.1.113:1026/v2/entities/SensorData1/attrs";
const char* fiware_motor_url = "http://192.168.1.113:1026/v2/entities/MotorData1/attrs";
const char* fiware_limites_url = "http://192.168.1.113:1026/v2/entities/Limites:1";
const char* fiware_aire = "http://192.168.1.113:1026/v2/entities/AireAcondicionado/attrs";
const char* fiware_luces = "http://192.168.1.113:1026/v2/entities/Luces/attrs";

// ========================
// === VARIABLES GLOBALES =
// ========================
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org");
DHT dht_sensor(DHT_SENSOR_PIN, DHT_SENSOR_TYPE);
WebServer server(80);

struct Limites {
  int consumo_agua_max;
  std::vector<int> horas_riego_horas;
  std::vector<int> horas_riego_minutos;
  float humedad_amb_min;
  float humedad_amb_max;
  int humedad_suelo_min;
  int humedad_suelo_max;
  int luz_min;
  int luz_max;
  float temp_min;
  float temp_max;
  int volumen_agua_min;
};

Limites limites;
bool last_motor_state = LOW, current_motor_state = LOW, signal_fiware_control_motor = false;
bool signal_fiware_control_aire = false, signal_fiware_control_luces = false, last_aire_state = LOW, last_luces_state = LOW;
unsigned long manualControl_start_time = 0, signal_fiware_control_aire_start = 0, signal_fiware_control_luces_start = 0;
const unsigned long manualControl_duration = 60000;
int last_nivel = -1;
int last_luz = -1;
int last_humedad_suelo = -1;
float last_humedad_ambiental = -1.0;
float last_temp_C = -1.0;
// ==============================
// === FUNCIONES PARA LOS LOGS ==
// ==============================
void handleSerialCommands() {
  if (Serial.available()) {
    String command = Serial.readStringUntil('\n');
    command.trim();
    command.toUpperCase();

    if (command == "ENABLE_LOGS") {
      loggingEnabled = true;
      Serial.println("Logs habilitados");
    } 
    else if (command == "DISABLE_LOGS") {
      loggingEnabled = false;
      Serial.println("Logs deshabilitados");
    }
    else if (command == "LOG_LEVEL=ERROR") {
      currentLogLevel = ERROR_LEVEL;
      Serial.println("Nivel de logs: ERROR");
    }
    else if (command == "LOG_LEVEL=INFO") {
      currentLogLevel = INFO_LEVEL;
      Serial.println("Nivel de logs: INFO");
    }
    else if (command == "LOG_LEVEL=DEBUG") {
      currentLogLevel = DEBUG_LEVEL;
      Serial.println("Nivel de logs: DEBUG");
    }
  }
}

// =============================
// === FUNCIONES PRINCIPALES ===
// =============================
void updateAireStateInFIWARE(bool state) {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  http.begin(fiware_aire);
  http.addHeader("Content-Type", "application/json");

  char payload[128];
  snprintf(payload, sizeof(payload), 
           "{\"estado\":{\"value\":%s,\"metadata\":{\"origen\":{\"value\":\"Arduino\"}}}}",
           state ? "true" : "false");
  int code = http.PATCH(payload);

  if (code > 0) {
    LOG_INFO("Estado del aire actualizado en FIWARE");
  } else {
    LOG_ERROR("Error actualizando aire: " + String(http.errorToString(code)));
  }
  http.end();
}

void updateLucesStateInFIWARE(bool state) {
  if (WiFi.status() != WL_CONNECTED) return;
  HTTPClient http;
  http.begin(fiware_luces);
  http.addHeader("Content-Type", "application/json");

  char payload[128];
  snprintf(payload, sizeof(payload), 
           "{\"estado\":{\"value\":%s,\"metadata\":{\"origen\":{\"value\":\"Arduino\"}}}}",
           state ? "true" : "false");
  int code = http.PATCH(payload);

  if (code > 0) {
    LOG_INFO("Estado de las luces actualizado en FIWARE");
  } else {
    LOG_ERROR("Error actualizando luces: " + String(http.errorToString(code)));
  }
  http.end();
}

void obtenerEstadoAireDesdeFiware() {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  http.begin(fiware_aire + String("/estado"));
  int httpCode = http.GET();

  if (httpCode == HTTP_CODE_OK) {
    String payload = http.getString();
    StaticJsonDocument<128> doc;
    DeserializationError error = deserializeJson(doc, payload);

    if (!error && doc.containsKey("value")) {
      bool estado = doc["value"].as<bool>();
      digitalWrite(AIRE_PIN, estado ? HIGH : LOW);
      LOG_INFO("Aire actualizado desde FIWARE: " + String(estado ? "ENCENDIDO" : "APAGADO"));
    }
  } else {
    LOG_ERROR("Error obteniendo estado del aire: " + String(httpCode));
  }
  http.end();
}

void obtenerEstadoLucesDesdeFiware() {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  http.begin(fiware_luces + String("/estado"));
  int httpCode = http.GET();

  if (httpCode == HTTP_CODE_OK) {
    String payload = http.getString();
    StaticJsonDocument<128> doc;
    DeserializationError error = deserializeJson(doc, payload);

    if (!error && doc.containsKey("value")) {
      bool estado = doc["value"].as<bool>();
      digitalWrite(BOMBILLA_PIN, estado ? HIGH : LOW);
      LOG_INFO("Luces actualizadas desde FIWARE: " + String(estado ? "ENCENDIDO" : "APAGADO"));
    }
  } else {
    LOG_ERROR("Error obteniendo estado de luces: " + String(httpCode));
  }
  http.end();
}

void obtenerEstadoMotorDesdeFiware() {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  http.begin(fiware_motor_url);
  int httpCode = http.GET();

  if (httpCode == HTTP_CODE_OK) {
    String payload = http.getString();
    StaticJsonDocument<512> doc;
    DeserializationError error = deserializeJson(doc, payload);

    if (!error && doc.containsKey("estado")) {
      bool estado = doc["estado"]["value"].as<bool>();
      if (estado != current_motor_state) {
        digitalWrite(MOTOR_PIN, estado ? HIGH : LOW);
        current_motor_state = estado;
        signal_fiware_control_motor = true;
        manualControl_start_time = millis();
        LOG_INFO("Motor actualizado desde FIWARE: " + String(estado ? "ENCENDIDO" : "APAGADO"));
      }
    }
  } else {
    LOG_ERROR("Error obteniendo estado del motor: " + String(httpCode));
  }
  http.end();
}

void obtenerLimites() {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  http.begin(fiware_limites_url);
  int httpCode = http.GET();
  
  if (httpCode == HTTP_CODE_OK) {
    String payload = http.getString();
    DynamicJsonDocument doc(2048);
    deserializeJson(doc, payload);

    limites.horas_riego_horas.clear();
    limites.horas_riego_minutos.clear();
    
    limites.consumo_agua_max = doc["consumo_agua_max"]["value"].as<int>();
    
    JsonArray horasArray = doc["horas_riego"]["value"];
    for (JsonArray horaPair : horasArray) {
      limites.horas_riego_horas.push_back(horaPair[0].as<int>());
      limites.horas_riego_minutos.push_back(horaPair[1].as<int>());
    }
    
    limites.humedad_amb_min = doc["humedad_amb_min"]["value"].as<float>();
    limites.humedad_amb_max = doc["humedad_amb_max"]["value"].as<float>();
    limites.humedad_suelo_min = doc["humedad_suelo_min"]["value"].as<int>();
    limites.humedad_suelo_max = doc["humedad_suelo_max"]["value"].as<int>();
    limites.luz_min = doc["luz_min"]["value"].as<int>();
    limites.luz_max = doc["luz_max"]["value"].as<int>();
    limites.temp_min = doc["temp_min"]["value"].as<float>();
    limites.temp_max = doc["temp_max"]["value"].as<float>();
    limites.volumen_agua_min = doc["volumen_agua_min"]["value"].as<int>();

    LOG_DEBUG("Límites actualizados desde FIWARE");
  } else {
    LOG_ERROR("Error obteniendo límites: " + String(httpCode));
  }
  http.end();
}

bool esHoraDeRiego() {
  timeClient.update();
  int currentHour = timeClient.getHours();
  int currentMinute = timeClient.getMinutes();
  bool es = false;

  for (size_t i = 0; i < limites.horas_riego_horas.size(); ++i) {
    if (currentHour == limites.horas_riego_horas[i]) {
      es = true;
      break;
    }
  }
  return es;
}

void handleUpdate() {
  if (server.method() == HTTP_POST) {
    String body = server.arg("plain");
    LOG_DEBUG("Notificación recibida: " + body);
    
    StaticJsonDocument<1024> doc;
    DeserializationError error = deserializeJson(doc, body);
    
    if (error) {
      LOG_ERROR("Error parsing JSON: " + String(error.c_str()));
      server.send(400, "text/plain", "Bad Request");
      return;
    }

    if (!doc.containsKey("data") || doc["data"].size() == 0) {
      LOG_ERROR("Formato de notificación inválido");
      server.send(400, "text/plain", "Invalid format");
      return;
    }

    for (JsonObject notification : doc["data"].as<JsonArray>()) {
      String id = notification["id"];
      String type = notification["type"];
      
      //Funcion para verificar si el origen es arduino mediante metadatos
      auto isFromArduino = [](JsonObject notification, const String& attr) {
        if (notification.containsKey(attr) && 
            notification[attr].containsKey("metadata") && 
            notification[attr]["metadata"].containsKey("origen")) {
          String origen = notification[attr]["metadata"]["origen"]["value"].as<String>();
          return origen == "Arduino";
        }
        return false;
      };

      //Verificamos si es notificacion del motor y por quien
      if (id == "MotorData1" && type == "Motor" && 
          notification.containsKey("estado") && 
          isFromArduino(notification, "estado")) {
        LOG_DEBUG("Ignorando notificación de cambio propio (Arduino)");
        continue;
      }
      
      //Verificamos si es notificacion del aire acondicionado y por quien
      if (id == "AireAcondicionado" && type == "AireAcondicionado" && 
          notification.containsKey("estado") && 
          isFromArduino(notification, "estado")) {
        LOG_DEBUG("Ignorando notificación de cambio propio (Arduino)");
        continue;
      }
      
      //Verificamos si es notificacion de las luces y por quien
      if (id == "Luces" && type == "Luces" && 
          notification.containsKey("estado") && 
          isFromArduino(notification, "estado")) {
        LOG_DEBUG("Ignorando notificación de cambio propio (Arduino)");
        continue;
      }

      if (id == "AireAcondicionado" && type == "AireAcondicionado") {
        if (notification.containsKey("estado")) {
          bool estado = notification["estado"]["value"];
          digitalWrite(AIRE_PIN, estado ? HIGH : LOW);
          last_aire_state = estado;
          signal_fiware_control_aire = true;
          signal_fiware_control_aire_start = millis();
          LOG_INFO("Control Manual del aire activado: " + String(estado ? "ENCENDIDO" : "APAGADO"));
        }
      } 
      else if (id == "Luces" && type == "Luces") {
        if (notification.containsKey("estado")) {
          bool estado = notification["estado"]["value"];
          digitalWrite(BOMBILLA_PIN, estado ? HIGH : LOW);
          last_luces_state = estado;
          signal_fiware_control_luces = true;
          signal_fiware_control_luces_start = millis();
          LOG_INFO("Control Manual de luces activado: " + String(estado ? "ENCENDIDO" : "APAGADO"));
        }
      }
      else if (id == "MotorData1" && type == "Motor") {
        if (notification.containsKey("estado")) {
          bool estado = notification["estado"]["value"];
          digitalWrite(MOTOR_PIN, estado ? HIGH : LOW);
          current_motor_state = estado;
          signal_fiware_control_motor = true;
          manualControl_start_time = millis();
          LOG_INFO("Motor actualizado desde notificación: " + String(estado ? "ENCENDIDO" : "APAGADO"));
        }
      }
      else if (type == "Limites") {
        LOG_INFO("Notificación de límites recibida");
        
        limites.horas_riego_horas.clear();
        limites.horas_riego_minutos.clear();
        
        if (notification.containsKey("temp_min")) limites.temp_min = notification["temp_min"]["value"];
        if (notification.containsKey("temp_max")) limites.temp_max = notification["temp_max"]["value"];
        if (notification.containsKey("humedad_amb_min")) limites.humedad_amb_min = notification["humedad_amb_min"]["value"];
        if (notification.containsKey("humedad_amb_max")) limites.humedad_amb_max = notification["humedad_amb_max"]["value"];
        if (notification.containsKey("humedad_suelo_min")) limites.humedad_suelo_min = notification["humedad_suelo_min"]["value"];
        if (notification.containsKey("humedad_suelo_max")) limites.humedad_suelo_max = notification["humedad_suelo_max"]["value"];
        if (notification.containsKey("luz_min")) limites.luz_min = notification["luz_min"]["value"];
        if (notification.containsKey("luz_max")) limites.luz_max = notification["luz_max"]["value"];
        if (notification.containsKey("consumo_agua_max")) limites.consumo_agua_max = notification["consumo_agua_max"]["value"];
        if (notification.containsKey("volumen_agua_min")) limites.volumen_agua_min = notification["volumen_agua_min"]["value"];
        if (notification.containsKey("horas_riego")) {
          JsonArray horasArray = notification["horas_riego"]["value"];
          for (JsonArray horaPair : horasArray) {
            limites.horas_riego_horas.push_back(horaPair[0].as<int>());
            limites.horas_riego_minutos.push_back(horaPair[1].as<int>());
          }
        }
        LOG_INFO("Límites actualizados desde notificación");
      }
    }
    server.send(200, "text/plain", "OK");
  } else {
    server.send(405, "text/plain", "Method Not Allowed");
  }
}

void updateMotorStateInFIWARE(bool state) {
  if (WiFi.status() != WL_CONNECTED) return;
  HTTPClient http;
  http.begin(fiware_motor_url);
  http.addHeader("Content-Type", "application/json");

  char payload[128];
  snprintf(payload, sizeof(payload), 
           "{\"estado\":{\"value\":%s,\"metadata\":{\"origen\":{\"value\":\"Arduino\"}}}}",
           state ? "true" : "false");

  int code = http.PATCH(payload);

  if (code > 0) {
    LOG_INFO("Estado del motor confirmado en FIWARE");
  } else {
    LOG_ERROR("Error confirmando estado del motor: " + String(http.errorToString(code)));
  }
  http.end();
  last_motor_state = state;
}

void controlAutomatico() {
  int nivel_value = analogRead(SIGNAL_PIN);
  int humedad_value = analogRead(HUMEDAD_PIN);
  float humi = dht_sensor.readHumidity();
  float temperature_C = dht_sensor.readTemperature();
  int light_value = analogRead(AO_PIN);

  light_value = ((4095.0 - light_value) / 4095.0) * 100.0;  
  humedad_value = ((4095.0 - humedad_value) / 4095.0) * 100.0;  
  nivel_value = (nivel_value / 4095.0) * 100.0; 

  LOG_DEBUG("=== Lecturas de Sensores ===");
  LOG_DEBUG("Nivel agua: " + String(nivel_value));
  LOG_DEBUG("Humedad suelo: " + String(humedad_value));
  LOG_DEBUG("Humedad ambiental: " + String(humi) + "%");
  LOG_DEBUG("Temperatura: " + String(temperature_C) + "°C");
  LOG_DEBUG("Luz: " + String(light_value) + "%");

  // Verificaciones del control del motor 1º nivel del agua, 2º hora de riego y 3ºsi corresponde regar
  if (nivel_value >= limites.volumen_agua_min) {
    if (esHoraDeRiego()) {
      bool should_be_on = (humedad_value < limites.humedad_suelo_min) && !signal_fiware_control_motor;
       if (signal_fiware_control_motor) {
        LOG_INFO("Control Manual activo. Se omite control automático del motor.");
      }else{
        if (current_motor_state != should_be_on) {
          digitalWrite(MOTOR_PIN, should_be_on ? HIGH : LOW);
          current_motor_state = should_be_on;
          updateMotorStateInFIWARE(current_motor_state);
          LOG_INFO("Motor " + String(should_be_on ? "ENCENDIDO" : "APAGADO") + " (control automático)");
        }
      }
    } else {
      digitalWrite(MOTOR_PIN, LOW);
      current_motor_state = LOW;
      LOG_DEBUG("No es hora de riego ");
      updateMotorStateInFIWARE(current_motor_state);

    }
  } else {
    digitalWrite(MOTOR_PIN, LOW);
    current_motor_state = LOW;
    LOG_ERROR("Nivel de agua insuficiente "+ String(nivel_value) +" limite "+ String(limites.volumen_agua_min));
    updateMotorStateInFIWARE(current_motor_state);

  }

  // Verificaciones del control del aire
  if (!signal_fiware_control_aire) {
    bool aire_should_be_on = (humi < limites.humedad_amb_min || humi > limites.humedad_amb_max || 
                            temperature_C < limites.temp_min || temperature_C > limites.temp_max);
    if (digitalRead(AIRE_PIN) != aire_should_be_on) {
      digitalWrite(AIRE_PIN, aire_should_be_on ? HIGH : LOW);
      updateAireStateInFIWARE(aire_should_be_on);
      LOG_INFO("Aire " + String(aire_should_be_on ? "ENCENDIDO" : "APAGADO") + " (control automático)");
    }
  } else if (millis() - signal_fiware_control_aire_start > manualControl_duration) {
    signal_fiware_control_aire = false;
    LOG_INFO("Control Manual del aire expirado");
  }

  // Verificaciones del control de las luces
  if (!signal_fiware_control_luces) {
    bool luces_should_be_on = (light_value < limites.luz_min || light_value > limites.luz_max);
    if (digitalRead(BOMBILLA_PIN) != luces_should_be_on) {
      digitalWrite(BOMBILLA_PIN, luces_should_be_on ? HIGH : LOW);
      updateLucesStateInFIWARE(luces_should_be_on);
      LOG_INFO("Luces " + String(luces_should_be_on ? "ENCENDIDAS" : "APAGADAS") + " (control automático)");
    }
  } else if (millis() - signal_fiware_control_luces_start > manualControl_duration) {
    signal_fiware_control_luces = false;
    LOG_INFO("Control Manual de luces expirado");
  }
}

void setup() {
  Serial.begin(115200);
  LOG_INFO("Iniciando sistema...");

  // Configuración IP estática
  IPAddress local_IP(192, 168, 1, 100);
  IPAddress gateway(192, 168, 1, 1);
  IPAddress subnet(255, 255, 255, 0);
  IPAddress primaryDNS(8, 8, 8, 8);
  IPAddress secondaryDNS(8, 8, 4, 4);
  
  if (!WiFi.config(local_IP, gateway, subnet, primaryDNS, secondaryDNS)) {
    LOG_ERROR("Error al configurar IP estática");
  }

  // Nos conectamos al WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    LOG_INFO("Conectando a WiFi...");
  }
  LOG_INFO("Conectado a WiFi. IP: " + WiFi.localIP().toString());

  // Iniciamos NTP y configuramos la zona horaria. España 3600(invierno), 7200(verano)
  timeClient.begin();
  timeClient.setTimeOffset(7200);
  LOG_DEBUG("Cliente NTP iniciado");

  // Inicializamos los sensores
  dht_sensor.begin();
  pinMode(MOTOR_PIN, OUTPUT);
  pinMode(AIRE_PIN, OUTPUT);
  pinMode(BOMBILLA_PIN, OUTPUT);
  digitalWrite(MOTOR_PIN, LOW);
  digitalWrite(AIRE_PIN, LOW);
  digitalWrite(BOMBILLA_PIN, LOW);
  LOG_DEBUG("Sensores y actuadores inicializados");

  // Obtenemos de fiware la configuración inicial de los elementos
  obtenerLimites();
  obtenerEstadoMotorDesdeFiware();
  obtenerEstadoAireDesdeFiware();    
  obtenerEstadoLucesDesdeFiware();

  // Iniciamos el servidor web
  server.on("/update", handleUpdate);
  server.begin();
  LOG_INFO("Servidor HTTP iniciado");
}

void loop() {
  handleSerialCommands(); 
  server.handleClient();
  timeClient.update();

  if (signal_fiware_control_motor && millis() - manualControl_start_time > manualControl_duration) {
    signal_fiware_control_motor = false;
    LOG_INFO("Control Manual del motor expirado");
  }

  controlAutomatico();

  // Enviamos los datos a FIWARE cada 5 segundos
  static unsigned long lastSendTime = 0;
  if (millis() - lastSendTime >= 5000) {
    lastSendTime = millis();
    
    int nivel_value = analogRead(SIGNAL_PIN);
    int light_value = analogRead(AO_PIN);
    int humedad_value = analogRead(HUMEDAD_PIN);
    float humi = dht_sensor.readHumidity();
    float temperature_C = dht_sensor.readTemperature();

    light_value = ((4095.0 - light_value) / 4095.0) * 100.0;  
    humedad_value = ((4095.0 - humedad_value) / 4095.0) * 100.0;  
    nivel_value = (nivel_value / 4095.0) * 100.0; 
    bool cambio = false;
    if (abs(nivel_value - last_nivel) >= 2) cambio = true;
    if (abs(light_value - last_luz) >= 2) cambio = true;
    if (abs(humedad_value - last_humedad_suelo) >= 2) cambio = true;
    if (abs(humi - last_humedad_ambiental) >= 1.0) cambio = true;
    if (abs(temperature_C - last_temp_C) >= 1.0) cambio = true;
    if (cambio && WiFi.status() == WL_CONNECTED) {
      HTTPClient http;
      http.begin(fiware_sensor_url);
      http.addHeader("Content-Type", "application/json");

      String payload = "{";
      payload += "\"nivel\": {\"value\": " + String(nivel_value) + "},";
      payload += "\"luz\": {\"value\": " + String(light_value) + "},";
      payload += "\"humedad_suelo\": {\"value\": " + String(humedad_value) + "},";
      payload += "\"humedad_ambiental\": {\"value\": " + String(humi) + "},";
      payload += "\"temperatura_C\": {\"value\": " + String(temperature_C) + "}";
      payload += "}";

      int httpResponseCode = http.POST(payload);
      if (httpResponseCode > 0) {
        LOG_DEBUG("Datos enviados a FIWARE");
        last_nivel = nivel_value;
        last_luz = light_value;
        last_humedad_suelo = humedad_value;
        last_humedad_ambiental = humi;
        last_temp_C = temperature_C;
      } else {
        LOG_ERROR("Error enviando datos: " + String(httpResponseCode));
      }
      http.end();
    }
  }
  delay(1000);
}