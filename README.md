# APKQR - Lector QR con Análisis en VirusTotal

**Proyecto de Trabajo de Fin de Grado (TFG) - Técnico Superior en Administración de Sistemas Informáticos en Red (ASIR)**

Aplicación Android que permite escanear códigos QR y analizar automáticamente las URLs detectadas mediante **VirusTotal**, ayudando a identificar posibles enlaces maliciosos.

---

## 🎯 Objetivo del Proyecto

Desarrollar una aplicación móvil segura que combine funcionalidad útil (lector de QR) con mecanismos de ciberseguridad, permitiendo al usuario verificar la reputación de enlaces antes de acceder a ellos.

---

## ✨ Características Principales

- **Escaneo rápido de códigos QR** usando la cámara del dispositivo
- **Análisis automático** de URLs mediante la API de VirusTotal
- **Interfaz moderna** con Jetpack Compose
- **Detección de enlaces peligrosos** con alertas visuales
- **Historial de escaneos** realizados
- **Diseño limpio y responsive**

---

## 🛠 Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose
- **Cámara**: CameraX
- **API**: VirusTotal API v3
- **Arquitectura**: MVVM
- **IDE**: Android Studio

---

## 📱 Capturas de Pantalla

<img width="172" height="378" alt="Imagen1" src="https://github.com/user-attachments/assets/75b0a6c5-093a-429f-a6db-535af5cb9c51" />
<img width="173" height="378" alt="Imagen2" src="https://github.com/user-attachments/assets/ba889d49-8e84-4e3e-be05-83dcd6faa736" />
<img width="174" height="378" alt="Imagen3" src="https://github.com/user-attachments/assets/f3ebaba1-3e80-4962-9951-09336495abd8" />


---

## 🚀 Cómo Ejecutar el Proyecto

1. Clona el repositorio:
   ```bash
   git clone https://github.com/ZedWiz/APKQR-TFG-Cibersecurity.git
2. Abre el proyecto en Android Studio
3. Configura tu propia API Key de VirusTotal en el archivo local.properties:
   ```bash
   propertiesVIRUSTOTAL_API_KEY=tu_api_key_aquí   
6. Ejecuta la aplicación en un emulador o dispositivo físico (Android 8.0+)
