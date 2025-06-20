# Aplicación de Detección de Emociones para Android

## Descripción General

Esta aplicación móvil para Android permite detectar emociones en rostros humanos mediante análisis de imágenes y videos. Utiliza una API externa que procesa el contenido multimedia y devuelve información detallada sobre las emociones detectadas.

## Características Principales

- **Captura de imágenes** desde la cámara del dispositivo
- **Grabación de videos** con audio
- **Selección de archivos** desde la galería del dispositivo
- **Análisis de emociones** en tiempo real
- **Interfaz intuitiva** con Material Design 3
- **Visualización detallada** de resultados con gráficos de barras
- **Soporte para múltiples personas** en una sola imagen

## Emociones Detectadas

La aplicación puede identificar las siguientes emociones:
- 😊 **Felicidad (Happy)**
- 😢 **Tristeza (Sad)**
- 😠 **Enojo (Anger)**
- 😨 **Miedo (Fear)**
- 😲 **Sorpresa (Surprise)**
- 🤢 **Disgusto (Disgust)**
- 😐 **Neutral (Neutral)**

## Arquitectura Técnica

### Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal de desarrollo
- **Jetpack Compose** - Framework de UI declarativo
- **Retrofit** - Cliente HTTP para llamadas a la API
- **Gson** - Serialización/deserialización JSON
- **Material Design 3** - Sistema de diseño de Google
- **ViewModel & StateFlow** - Gestión de estado y arquitectura MVVM
- **OkHttp** - Cliente HTTP con interceptores de logging

### Estructura del Proyecto

```
com.example.finalmovilesprueba/
├── MainActivity.kt                 # Actividad principal
├── model/
│   └── EmotionModels.kt           # Modelos de datos
├── network/
│   ├── ApiService.kt              # Interfaz de la API
│   └── NetworkModule.kt           # Configuración de red
├── viewmodel/
│   └── EmotionViewModel.kt        # ViewModel principal
└── ui/theme/
    └── Theme.kt                   # Tema de la aplicación
```

## Componentes Principales

### 1. MainActivity
Actividad principal que contiene la interfaz de usuario construida con Jetpack Compose.

**Funciones principales:**
- Gestión de permisos de cámara y audio
- Launchers para captura de imágenes y videos
- Launchers para selección de archivos desde galería
- Interfaz de usuario con botones de acción

### 2. EmotionViewModel
ViewModel que maneja la lógica de negocio y el estado de la aplicación.

**Funciones principales:**
- `uploadFile()` - Subida de archivos a la API
- `createFileFromUri()` - Conversión de URI a File
- `clearResults()` - Limpieza de resultados

**Estado manejado:**
```kotlin
data class EmotionUiState(
    val isLoading: Boolean = false,
    val emotionResponse: EmotionResponse? = null,
    val error: String? = null
)
```

### 3. Models (EmotionModels.kt)
Modelos de datos que representan la respuesta de la API.

**Estructuras principales:**
- `EmotionResponse` - Respuesta principal de la API
- `Resultado` - Información de cada persona detectada
- `Coordenadas` - Posición del rostro en la imagen
- `Emociones` - Valores de probabilidad para cada emoción

**Deserializador personalizado:**
```kotlin
class EmotionResponseDeserializer : JsonDeserializer<EmotionResponse>
```
Maneja dos formatos diferentes:
- **Imágenes**: Lista de resultados con coordenadas
- **Videos**: Objeto de emociones directo sin coordenadas

### 4. Network Layer
Configuración de red usando Retrofit y OkHttp.

**ApiService.kt:**
```kotlin
@Multipart
@POST("/")
suspend fun detectEmotions(@Part file: MultipartBody.Part): Response<EmotionResponse>
```

**NetworkModule.kt:**
- Configuración de timeouts (30 segundos)
- Interceptor de logging para debugging
- URL base de la API

## Funcionalidades de la Interfaz

### Botones de Acción
1. **Seleccionar Imagen** - Abre la galería para seleccionar una imagen
2. **Seleccionar Video** - Abre la galería para seleccionar un video
3. **Tomar Foto** - Abre la cámara para capturar una imagen
4. **Grabar Video** - Abre la cámara para grabar un video
5. **Limpiar Resultados** - Elimina los resultados actuales

### Visualización de Resultados

#### Para Imágenes:
- **Tipo de análisis** (imagen)
- **Número de personas detectadas**
- **Para cada persona:**
  - Emoción principal destacada
  - Coordenadas del rostro (X, Y, Ancho, Alto)
  - Barra de progreso para cada emoción con porcentajes

#### Para Videos:
- **Tipo de análisis** (video)
- **Emociones generales del video**
- **Emoción principal**
- **Barras de progreso** para todas las emociones

### Estados de la Aplicación
- **Cargando** - Indicador circular mientras se procesa
- **Error** - Mensaje de error en caso de fallos
- **Resultados** - Visualización detallada de las emociones detectadas

## Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Configuración FileProvider

Para el manejo de archivos temporales:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API Integration

### Endpoint
```
POST https://detectar-emociones-4koqhixykq-uc.a.run.app/
```

### Formato de Respuesta

**Para Imágenes:**
```json
{
    "tipo": "imagen",
    "resultados": [
        {
            "coordenadas": {
                "x": 1756,
                "y": 547,
                "w": 2388,
                "h": 2388
            },
            "emociones": {
                "Anger": 0.142,
                "Disgust": 0.145,
                "Fear": 0.150,
                "Happy": 0.142,
                "Surprise": 0.148,
                "Sad": 0.139,
                "Neutral": 0.134
            },
            "emocion_principal": "Fear"
        }
    ]
}
```

**Para Videos:**
```json
{
    "tipo": "video",
    "resultados": {
        "Anger": 0,
        "Disgust": 0,
        "Fear": 16,
        "Happy": 14,
        "Surprise": 0,
        "Sad": 0,
        "Neutral": 0
    }
}
```

## Características Técnicas Avanzadas

### Gestión de Archivos Temporales
- Creación automática de archivos temporales en cache
- Limpieza automática después del procesamiento
- Naming con timestamp para evitar conflictos

### Manejo de Estados Asíncronos
- Use de Kotlin Coroutines
- StateFlow para reactive UI
- Manejo proper de loading states

### UI Responsiva
- Diseño adaptativo para diferentes tamaños de pantalla
- Animaciones suaves con Material Design
- Colores temáticos para cada emoción

### Error Handling
- Manejo de errores de red
- Validación de permisos
- Mensajes de error informativos para el usuario

## Instalación y Configuración

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Configurar la URL de la API** en `NetworkModule.kt`
4. **Compilar y ejecutar** en dispositivo o emulador

## Dependencias Principales

```gradle
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'
implementation 'com.squareup.okhttp3:logging-interceptor'
```

## Consideraciones de Rendimiento

- **Timeouts configurados** para evitar bloqueos
- **Archivos temporales** eliminados automáticamente
- **Estados de carga** para mejor UX
- **Compresión automática** de archivos antes del envío

## Posibles Mejoras Futuras

- Soporte para análisis en tiempo real
- Guardado de historial de análisis
- Exportación de resultados
- Análisis batch de múltiples archivos
- Integración con cámara en tiempo real
- Soporte para más formatos de archivo

# Aplicación de Detección de Emociones para Android

## Descripción General

Esta aplicación móvil para Android permite detectar emociones en rostros humanos mediante análisis de imágenes y videos. Utiliza una API externa que procesa el contenido multimedia y devuelve información detallada sobre las emociones detectadas.

## Características Principales

- **Captura de imágenes** desde la cámara del dispositivo
- **Grabación de videos** con audio
- **Selección de archivos** desde la galería del dispositivo
- **Análisis de emociones** en tiempo real
- **Interfaz intuitiva** con Material Design 3
- **Visualización detallada** de resultados con gráficos de barras
- **Soporte para múltiples personas** en una sola imagen

## Emociones Detectadas

La aplicación puede identificar las siguientes emociones:
- 😊 **Felicidad (Happy)**
- 😢 **Tristeza (Sad)**
- 😠 **Enojo (Anger)**
- 😨 **Miedo (Fear)**
- 😲 **Sorpresa (Surprise)**
- 🤢 **Disgusto (Disgust)**
- 😐 **Neutral (Neutral)**

## Arquitectura Técnica

### Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal de desarrollo
- **Jetpack Compose** - Framework de UI declarativo
- **Retrofit** - Cliente HTTP para llamadas a la API
- **Gson** - Serialización/deserialización JSON
- **Material Design 3** - Sistema de diseño de Google
- **ViewModel & StateFlow** - Gestión de estado y arquitectura MVVM
- **OkHttp** - Cliente HTTP con interceptores de logging

### Estructura del Proyecto

```
com.example.finalmovilesprueba/
├── MainActivity.kt                 # Actividad principal
├── model/
│   └── EmotionModels.kt           # Modelos de datos
├── network/
│   ├── ApiService.kt              # Interfaz de la API
│   └── NetworkModule.kt           # Configuración de red
├── viewmodel/
│   └── EmotionViewModel.kt        # ViewModel principal
└── ui/theme/
    └── Theme.kt                   # Tema de la aplicación
```

## Componentes Principales

### 1. MainActivity
Actividad principal que contiene la interfaz de usuario construida con Jetpack Compose.

**Funciones principales:**
- Gestión de permisos de cámara y audio
- Launchers para captura de imágenes y videos
- Launchers para selección de archivos desde galería
- Interfaz de usuario con botones de acción

### 2. EmotionViewModel
ViewModel que maneja la lógica de negocio y el estado de la aplicación.

**Funciones principales:**
- `uploadFile()` - Subida de archivos a la API
- `createFileFromUri()` - Conversión de URI a File
- `clearResults()` - Limpieza de resultados

**Estado manejado:**
```kotlin
data class EmotionUiState(
    val isLoading: Boolean = false,
    val emotionResponse: EmotionResponse? = null,
    val error: String? = null
)
```

### 3. Models (EmotionModels.kt)
Modelos de datos que representan la respuesta de la API.

**Estructuras principales:**
- `EmotionResponse` - Respuesta principal de la API
- `Resultado` - Información de cada persona detectada
- `Coordenadas` - Posición del rostro en la imagen
- `Emociones` - Valores de probabilidad para cada emoción

**Deserializador personalizado:**
```kotlin
class EmotionResponseDeserializer : JsonDeserializer<EmotionResponse>
```
Maneja dos formatos diferentes:
- **Imágenes**: Lista de resultados con coordenadas
- **Videos**: Objeto de emociones directo sin coordenadas

### 4. Network Layer
Configuración de red usando Retrofit y OkHttp.

**ApiService.kt:**
```kotlin
@Multipart
@POST("/")
suspend fun detectEmotions(@Part file: MultipartBody.Part): Response<EmotionResponse>
```

**NetworkModule.kt:**
- Configuración de timeouts (30 segundos)
- Interceptor de logging para debugging
- URL base de la API

## Funcionalidades de la Interfaz

### Botones de Acción
1. **Seleccionar Imagen** - Abre la galería para seleccionar una imagen
2. **Seleccionar Video** - Abre la galería para seleccionar un video
3. **Tomar Foto** - Abre la cámara para capturar una imagen
4. **Grabar Video** - Abre la cámara para grabar un video
5. **Limpiar Resultados** - Elimina los resultados actuales

### Visualización de Resultados

#### Para Imágenes:
- **Tipo de análisis** (imagen)
- **Número de personas detectadas**
- **Para cada persona:**
  - Emoción principal destacada
  - Coordenadas del rostro (X, Y, Ancho, Alto)
  - Barra de progreso para cada emoción con porcentajes

#### Para Videos:
- **Tipo de análisis** (video)
- **Emociones generales del video**
- **Emoción principal**
- **Barras de progreso** para todas las emociones

### Estados de la Aplicación
- **Cargando** - Indicador circular mientras se procesa
- **Error** - Mensaje de error en caso de fallos
- **Resultados** - Visualización detallada de las emociones detectadas

## Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Configuración FileProvider

Para el manejo de archivos temporales:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API Integration

### Endpoint
```
POST https://detectar-emociones-4koqhixykq-uc.a.run.app/
```

### Formato de Respuesta

**Para Imágenes:**
```json
{
    "tipo": "imagen",
    "resultados": [
        {
            "coordenadas": {
                "x": 1756,
                "y": 547,
                "w": 2388,
                "h": 2388
            },
            "emociones": {
                "Anger": 0.142,
                "Disgust": 0.145,
                "Fear": 0.150,
                "Happy": 0.142,
                "Surprise": 0.148,
                "Sad": 0.139,
                "Neutral": 0.134
            },
            "emocion_principal": "Fear"
        }
    ]
}
```

**Para Videos:**
```json
{
    "tipo": "video",
    "resultados": {
        "Anger": 0,
        "Disgust": 0,
        "Fear": 16,
        "Happy": 14,
        "Surprise": 0,
        "Sad": 0,
        "Neutral": 0
    }
}
```

## Características Técnicas Avanzadas

### Gestión de Archivos Temporales
- Creación automática de archivos temporales en cache
- Limpieza automática después del procesamiento
- Naming con timestamp para evitar conflictos

### Manejo de Estados Asíncronos
- Use de Kotlin Coroutines
- StateFlow para reactive UI
- Manejo proper de loading states

### UI Responsiva
- Diseño adaptativo para diferentes tamaños de pantalla
- Animaciones suaves con Material Design
- Colores temáticos para cada emoción

### Error Handling
- Manejo de errores de red
- Validación de permisos
- Mensajes de error informativos para el usuario

## Instalación y Configuración

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Configurar la URL de la API** en `NetworkModule.kt`
4. **Compilar y ejecutar** en dispositivo o emulador

## Dependencias Principales

```gradle
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'
implementation 'com.squareup.okhttp3:logging-interceptor'
```

## Consideraciones de Rendimiento

- **Timeouts configurados** para evitar bloqueos
- **Archivos temporales** eliminados automáticamente
- **Estados de carga** para mejor UX
- **Compresión automática** de archivos antes del envío

## Posibles Mejoras Futuras

- Soporte para análisis en tiempo real
- Guardado de historial de análisis
- Exportación de resultados
- Análisis batch de múltiples archivos
- Integración con cámara en tiempo real
- Soporte para más formatos de archivo


# Aplicación de Detección de Emociones para Android

## Descripción General

Esta aplicación móvil para Android permite detectar emociones en rostros humanos mediante análisis de imágenes y videos. Utiliza una API externa que procesa el contenido multimedia y devuelve información detallada sobre las emociones detectadas.

## Características Principales

- **Captura de imágenes** desde la cámara del dispositivo
- **Grabación de videos** con audio
- **Selección de archivos** desde la galería del dispositivo
- **Análisis de emociones** en tiempo real
- **Interfaz intuitiva** con Material Design 3
- **Visualización detallada** de resultados con gráficos de barras
- **Soporte para múltiples personas** en una sola imagen

## Emociones Detectadas

La aplicación puede identificar las siguientes emociones:
- 😊 **Felicidad (Happy)**
- 😢 **Tristeza (Sad)**
- 😠 **Enojo (Anger)**
- 😨 **Miedo (Fear)**
- 😲 **Sorpresa (Surprise)**
- 🤢 **Disgusto (Disgust)**
- 😐 **Neutral (Neutral)**

## Arquitectura Técnica

### Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal de desarrollo
- **Jetpack Compose** - Framework de UI declarativo
- **Retrofit** - Cliente HTTP para llamadas a la API
- **Gson** - Serialización/deserialización JSON
- **Material Design 3** - Sistema de diseño de Google
- **ViewModel & StateFlow** - Gestión de estado y arquitectura MVVM
- **OkHttp** - Cliente HTTP con interceptores de logging

### Estructura del Proyecto

```
com.example.finalmovilesprueba/
├── MainActivity.kt                 # Actividad principal
├── model/
│   └── EmotionModels.kt           # Modelos de datos
├── network/
│   ├── ApiService.kt              # Interfaz de la API
│   └── NetworkModule.kt           # Configuración de red
├── viewmodel/
│   └── EmotionViewModel.kt        # ViewModel principal
└── ui/theme/
    └── Theme.kt                   # Tema de la aplicación
```

## Componentes Principales

### 1. MainActivity
Actividad principal que contiene la interfaz de usuario construida con Jetpack Compose.

**Funciones principales:**
- Gestión de permisos de cámara y audio
- Launchers para captura de imágenes y videos
- Launchers para selección de archivos desde galería
- Interfaz de usuario con botones de acción

### 2. EmotionViewModel
ViewModel que maneja la lógica de negocio y el estado de la aplicación.

**Funciones principales:**
- `uploadFile()` - Subida de archivos a la API
- `createFileFromUri()` - Conversión de URI a File
- `clearResults()` - Limpieza de resultados

**Estado manejado:**
```kotlin
data class EmotionUiState(
    val isLoading: Boolean = false,
    val emotionResponse: EmotionResponse? = null,
    val error: String? = null
)
```

### 3. Models (EmotionModels.kt)
Modelos de datos que representan la respuesta de la API.

**Estructuras principales:**
- `EmotionResponse` - Respuesta principal de la API
- `Resultado` - Información de cada persona detectada
- `Coordenadas` - Posición del rostro en la imagen
- `Emociones` - Valores de probabilidad para cada emoción

**Deserializador personalizado:**
```kotlin
class EmotionResponseDeserializer : JsonDeserializer<EmotionResponse>
```
Maneja dos formatos diferentes:
- **Imágenes**: Lista de resultados con coordenadas
- **Videos**: Objeto de emociones directo sin coordenadas

### 4. Network Layer
Configuración de red usando Retrofit y OkHttp.

**ApiService.kt:**
```kotlin
@Multipart
@POST("/")
suspend fun detectEmotions(@Part file: MultipartBody.Part): Response<EmotionResponse>
```

**NetworkModule.kt:**
- Configuración de timeouts (30 segundos)
- Interceptor de logging para debugging
- URL base de la API

## Funcionalidades de la Interfaz

### Botones de Acción
1. **Seleccionar Imagen** - Abre la galería para seleccionar una imagen
2. **Seleccionar Video** - Abre la galería para seleccionar un video
3. **Tomar Foto** - Abre la cámara para capturar una imagen
4. **Grabar Video** - Abre la cámara para grabar un video
5. **Limpiar Resultados** - Elimina los resultados actuales

### Visualización de Resultados

#### Para Imágenes:
- **Tipo de análisis** (imagen)
- **Número de personas detectadas**
- **Para cada persona:**
  - Emoción principal destacada
  - Coordenadas del rostro (X, Y, Ancho, Alto)
  - Barra de progreso para cada emoción con porcentajes

#### Para Videos:
- **Tipo de análisis** (video)
- **Emociones generales del video**
- **Emoción principal**
- **Barras de progreso** para todas las emociones

### Estados de la Aplicación
- **Cargando** - Indicador circular mientras se procesa
- **Error** - Mensaje de error en caso de fallos
- **Resultados** - Visualización detallada de las emociones detectadas

## Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Configuración FileProvider

Para el manejo de archivos temporales:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API Integration

### Endpoint
```
POST https://detectar-emociones-4koqhixykq-uc.a.run.app/
```

### Formato de Respuesta

**Para Imágenes:**
```json
{
    "tipo": "imagen",
    "resultados": [
        {
            "coordenadas": {
                "x": 1756,
                "y": 547,
                "w": 2388,
                "h": 2388
            },
            "emociones": {
                "Anger": 0.142,
                "Disgust": 0.145,
                "Fear": 0.150,
                "Happy": 0.142,
                "Surprise": 0.148,
                "Sad": 0.139,
                "Neutral": 0.134
            },
            "emocion_principal": "Fear"
        }
    ]
}
```

**Para Videos:**
```json
{
    "tipo": "video",
    "resultados": {
        "Anger": 0,
        "Disgust": 0,
        "Fear": 16,
        "Happy": 14,
        "Surprise": 0,
        "Sad": 0,
        "Neutral": 0
    }
}
```

## Características Técnicas Avanzadas

### Gestión de Archivos Temporales
- Creación automática de archivos temporales en cache
- Limpieza automática después del procesamiento
- Naming con timestamp para evitar conflictos

### Manejo de Estados Asíncronos
- Use de Kotlin Coroutines
- StateFlow para reactive UI
- Manejo proper de loading states

### UI Responsiva
- Diseño adaptativo para diferentes tamaños de pantalla
- Animaciones suaves con Material Design
- Colores temáticos para cada emoción

### Error Handling
- Manejo de errores de red
- Validación de permisos
- Mensajes de error informativos para el usuario

## Instalación y Configuración

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Configurar la URL de la API** en `NetworkModule.kt`
4. **Compilar y ejecutar** en dispositivo o emulador

## Dependencias Principales

```gradle
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'
implementation 'com.squareup.okhttp3:logging-interceptor'
```

## Consideraciones de Rendimiento

- **Timeouts configurados** para evitar bloqueos
- **Archivos temporales** eliminados automáticamente
- **Estados de carga** para mejor UX
- **Compresión automática** de archivos antes del envío

## Posibles Mejoras Futuras

- Soporte para análisis en tiempo real
- Guardado de historial de análisis
- Exportación de resultados
- Análisis batch de múltiples archivos
- Integración con cámara en tiempo real
- Soporte para más formatos de archivo


# Aplicación de Detección de Emociones para Android

## Descripción General

Esta aplicación móvil para Android permite detectar emociones en rostros humanos mediante análisis de imágenes y videos. Utiliza una API externa que procesa el contenido multimedia y devuelve información detallada sobre las emociones detectadas.

## Características Principales

- **Captura de imágenes** desde la cámara del dispositivo
- **Grabación de videos** con audio
- **Selección de archivos** desde la galería del dispositivo
- **Análisis de emociones** en tiempo real
- **Interfaz intuitiva** con Material Design 3
- **Visualización detallada** de resultados con gráficos de barras
- **Soporte para múltiples personas** en una sola imagen

## Emociones Detectadas

La aplicación puede identificar las siguientes emociones:
- 😊 **Felicidad (Happy)**
- 😢 **Tristeza (Sad)**
- 😠 **Enojo (Anger)**
- 😨 **Miedo (Fear)**
- 😲 **Sorpresa (Surprise)**
- 🤢 **Disgusto (Disgust)**
- 😐 **Neutral (Neutral)**

## Arquitectura Técnica

### Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal de desarrollo
- **Jetpack Compose** - Framework de UI declarativo
- **Retrofit** - Cliente HTTP para llamadas a la API
- **Gson** - Serialización/deserialización JSON
- **Material Design 3** - Sistema de diseño de Google
- **ViewModel & StateFlow** - Gestión de estado y arquitectura MVVM
- **OkHttp** - Cliente HTTP con interceptores de logging

### Estructura del Proyecto

```
com.example.finalmovilesprueba/
├── MainActivity.kt                 # Actividad principal
├── model/
│   └── EmotionModels.kt           # Modelos de datos
├── network/
│   ├── ApiService.kt              # Interfaz de la API
│   └── NetworkModule.kt           # Configuración de red
├── viewmodel/
│   └── EmotionViewModel.kt        # ViewModel principal
└── ui/theme/
    └── Theme.kt                   # Tema de la aplicación
```

## Componentes Principales

### 1. MainActivity
Actividad principal que contiene la interfaz de usuario construida con Jetpack Compose.

**Funciones principales:**
- Gestión de permisos de cámara y audio
- Launchers para captura de imágenes y videos
- Launchers para selección de archivos desde galería
- Interfaz de usuario con botones de acción

### 2. EmotionViewModel
ViewModel que maneja la lógica de negocio y el estado de la aplicación.

**Funciones principales:**
- `uploadFile()` - Subida de archivos a la API
- `createFileFromUri()` - Conversión de URI a File
- `clearResults()` - Limpieza de resultados

**Estado manejado:**
```kotlin
data class EmotionUiState(
    val isLoading: Boolean = false,
    val emotionResponse: EmotionResponse? = null,
    val error: String? = null
)
```

### 3. Models (EmotionModels.kt)
Modelos de datos que representan la respuesta de la API.

**Estructuras principales:**
- `EmotionResponse` - Respuesta principal de la API
- `Resultado` - Información de cada persona detectada
- `Coordenadas` - Posición del rostro en la imagen
- `Emociones` - Valores de probabilidad para cada emoción

**Deserializador personalizado:**
```kotlin
class EmotionResponseDeserializer : JsonDeserializer<EmotionResponse>
```
Maneja dos formatos diferentes:
- **Imágenes**: Lista de resultados con coordenadas
- **Videos**: Objeto de emociones directo sin coordenadas

### 4. Network Layer
Configuración de red usando Retrofit y OkHttp.

**ApiService.kt:**
```kotlin
@Multipart
@POST("/")
suspend fun detectEmotions(@Part file: MultipartBody.Part): Response<EmotionResponse>
```

**NetworkModule.kt:**
- Configuración de timeouts (30 segundos)
- Interceptor de logging para debugging
- URL base de la API

## Funcionalidades de la Interfaz

### Botones de Acción
1. **Seleccionar Imagen** - Abre la galería para seleccionar una imagen
2. **Seleccionar Video** - Abre la galería para seleccionar un video
3. **Tomar Foto** - Abre la cámara para capturar una imagen
4. **Grabar Video** - Abre la cámara para grabar un video
5. **Limpiar Resultados** - Elimina los resultados actuales

### Visualización de Resultados

#### Para Imágenes:
- **Tipo de análisis** (imagen)
- **Número de personas detectadas**
- **Para cada persona:**
  - Emoción principal destacada
  - Coordenadas del rostro (X, Y, Ancho, Alto)
  - Barra de progreso para cada emoción con porcentajes

#### Para Videos:
- **Tipo de análisis** (video)
- **Emociones generales del video**
- **Emoción principal**
- **Barras de progreso** para todas las emociones

### Estados de la Aplicación
- **Cargando** - Indicador circular mientras se procesa
- **Error** - Mensaje de error en caso de fallos
- **Resultados** - Visualización detallada de las emociones detectadas

## Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Configuración FileProvider

Para el manejo de archivos temporales:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API Integration

### Endpoint
```
POST https://detectar-emociones-4koqhixykq-uc.a.run.app/
```

### Formato de Respuesta

**Para Imágenes:**
```json
{
    "tipo": "imagen",
    "resultados": [
        {
            "coordenadas": {
                "x": 1756,
                "y": 547,
                "w": 2388,
                "h": 2388
            },
            "emociones": {
                "Anger": 0.142,
                "Disgust": 0.145,
                "Fear": 0.150,
                "Happy": 0.142,
                "Surprise": 0.148,
                "Sad": 0.139,
                "Neutral": 0.134
            },
            "emocion_principal": "Fear"
        }
    ]
}
```

**Para Videos:**
```json
{
    "tipo": "video",
    "resultados": {
        "Anger": 0,
        "Disgust": 0,
        "Fear": 16,
        "Happy": 14,
        "Surprise": 0,
        "Sad": 0,
        "Neutral": 0
    }
}
```

## Características Técnicas Avanzadas

### Gestión de Archivos Temporales
- Creación automática de archivos temporales en cache
- Limpieza automática después del procesamiento
- Naming con timestamp para evitar conflictos

### Manejo de Estados Asíncronos
- Use de Kotlin Coroutines
- StateFlow para reactive UI
- Manejo proper de loading states

### UI Responsiva
- Diseño adaptativo para diferentes tamaños de pantalla
- Animaciones suaves con Material Design
- Colores temáticos para cada emoción

### Error Handling
- Manejo de errores de red
- Validación de permisos
- Mensajes de error informativos para el usuario

## Instalación y Configuración

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Configurar la URL de la API** en `NetworkModule.kt`
4. **Compilar y ejecutar** en dispositivo o emulador

## Dependencias Principales

```gradle
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'
implementation 'com.squareup.okhttp3:logging-interceptor'
```

## Consideraciones de Rendimiento

- **Timeouts configurados** para evitar bloqueos
- **Archivos temporales** eliminados automáticamente
- **Estados de carga** para mejor UX
- **Compresión automática** de archivos antes del envío

## Posibles Mejoras Futuras

- Soporte para análisis en tiempo real
- Guardado de historial de análisis
- Exportación de resultados
- Análisis batch de múltiples archivos
- Integración con cámara en tiempo real
- Soporte para más formatos de archivo


# Aplicación de Detección de Emociones para Android

## Descripción General

Esta aplicación móvil para Android permite detectar emociones en rostros humanos mediante análisis de imágenes y videos. Utiliza una API externa que procesa el contenido multimedia y devuelve información detallada sobre las emociones detectadas.

## Características Principales

- **Captura de imágenes** desde la cámara del dispositivo
- **Grabación de videos** con audio
- **Selección de archivos** desde la galería del dispositivo
- **Análisis de emociones** en tiempo real
- **Interfaz intuitiva** con Material Design 3
- **Visualización detallada** de resultados con gráficos de barras
- **Soporte para múltiples personas** en una sola imagen

## Emociones Detectadas

La aplicación puede identificar las siguientes emociones:
- 😊 **Felicidad (Happy)**
- 😢 **Tristeza (Sad)**
- 😠 **Enojo (Anger)**
- 😨 **Miedo (Fear)**
- 😲 **Sorpresa (Surprise)**
- 🤢 **Disgusto (Disgust)**
- 😐 **Neutral (Neutral)**

## Arquitectura Técnica

### Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal de desarrollo
- **Jetpack Compose** - Framework de UI declarativo
- **Retrofit** - Cliente HTTP para llamadas a la API
- **Gson** - Serialización/deserialización JSON
- **Material Design 3** - Sistema de diseño de Google
- **ViewModel & StateFlow** - Gestión de estado y arquitectura MVVM
- **OkHttp** - Cliente HTTP con interceptores de logging

### Estructura del Proyecto

```
com.example.finalmovilesprueba/
├── MainActivity.kt                 # Actividad principal
├── model/
│   └── EmotionModels.kt           # Modelos de datos
├── network/
│   ├── ApiService.kt              # Interfaz de la API
│   └── NetworkModule.kt           # Configuración de red
├── viewmodel/
│   └── EmotionViewModel.kt        # ViewModel principal
└── ui/theme/
    └── Theme.kt                   # Tema de la aplicación
```

## Componentes Principales

### 1. MainActivity
Actividad principal que contiene la interfaz de usuario construida con Jetpack Compose.

**Funciones principales:**
- Gestión de permisos de cámara y audio
- Launchers para captura de imágenes y videos
- Launchers para selección de archivos desde galería
- Interfaz de usuario con botones de acción

### 2. EmotionViewModel
ViewModel que maneja la lógica de negocio y el estado de la aplicación.

**Funciones principales:**
- `uploadFile()` - Subida de archivos a la API
- `createFileFromUri()` - Conversión de URI a File
- `clearResults()` - Limpieza de resultados

**Estado manejado:**
```kotlin
data class EmotionUiState(
    val isLoading: Boolean = false,
    val emotionResponse: EmotionResponse? = null,
    val error: String? = null
)
```

### 3. Models (EmotionModels.kt)
Modelos de datos que representan la respuesta de la API.

**Estructuras principales:**
- `EmotionResponse` - Respuesta principal de la API
- `Resultado` - Información de cada persona detectada
- `Coordenadas` - Posición del rostro en la imagen
- `Emociones` - Valores de probabilidad para cada emoción

**Deserializador personalizado:**
```kotlin
class EmotionResponseDeserializer : JsonDeserializer<EmotionResponse>
```
Maneja dos formatos diferentes:
- **Imágenes**: Lista de resultados con coordenadas
- **Videos**: Objeto de emociones directo sin coordenadas

### 4. Network Layer
Configuración de red usando Retrofit y OkHttp.

**ApiService.kt:**
```kotlin
@Multipart
@POST("/")
suspend fun detectEmotions(@Part file: MultipartBody.Part): Response<EmotionResponse>
```

**NetworkModule.kt:**
- Configuración de timeouts (30 segundos)
- Interceptor de logging para debugging
- URL base de la API

## Funcionalidades de la Interfaz

### Botones de Acción
1. **Seleccionar Imagen** - Abre la galería para seleccionar una imagen
2. **Seleccionar Video** - Abre la galería para seleccionar un video
3. **Tomar Foto** - Abre la cámara para capturar una imagen
4. **Grabar Video** - Abre la cámara para grabar un video
5. **Limpiar Resultados** - Elimina los resultados actuales

### Visualización de Resultados

#### Para Imágenes:
- **Tipo de análisis** (imagen)
- **Número de personas detectadas**
- **Para cada persona:**
  - Emoción principal destacada
  - Coordenadas del rostro (X, Y, Ancho, Alto)
  - Barra de progreso para cada emoción con porcentajes

#### Para Videos:
- **Tipo de análisis** (video)
- **Emociones generales del video**
- **Emoción principal**
- **Barras de progreso** para todas las emociones

### Estados de la Aplicación
- **Cargando** - Indicador circular mientras se procesa
- **Error** - Mensaje de error en caso de fallos
- **Resultados** - Visualización detallada de las emociones detectadas

## Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Configuración FileProvider

Para el manejo de archivos temporales:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API Integration

### Endpoint
```
POST https://detectar-emociones-4koqhixykq-uc.a.run.app/
```

### Formato de Respuesta

**Para Imágenes:**
```json
{
    "tipo": "imagen",
    "resultados": [
        {
            "coordenadas": {
                "x": 1756,
                "y": 547,
                "w": 2388,
                "h": 2388
            },
            "emociones": {
                "Anger": 0.142,
                "Disgust": 0.145,
                "Fear": 0.150,
                "Happy": 0.142,
                "Surprise": 0.148,
                "Sad": 0.139,
                "Neutral": 0.134
            },
            "emocion_principal": "Fear"
        }
    ]
}
```

**Para Videos:**
```json
{
    "tipo": "video",
    "resultados": {
        "Anger": 0,
        "Disgust": 0,
        "Fear": 16,
        "Happy": 14,
        "Surprise": 0,
        "Sad": 0,
        "Neutral": 0
    }
}
```

## Características Técnicas Avanzadas

### Gestión de Archivos Temporales
- Creación automática de archivos temporales en cache
- Limpieza automática después del procesamiento
- Naming con timestamp para evitar conflictos

### Manejo de Estados Asíncronos
- Use de Kotlin Coroutines
- StateFlow para reactive UI
- Manejo proper de loading states

### UI Responsiva
- Diseño adaptativo para diferentes tamaños de pantalla
- Animaciones suaves con Material Design
- Colores temáticos para cada emoción

### Error Handling
- Manejo de errores de red
- Validación de permisos
- Mensajes de error informativos para el usuario

## Instalación y Configuración

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Configurar la URL de la API** en `NetworkModule.kt`
4. **Compilar y ejecutar** en dispositivo o emulador

## Dependencias Principales

```gradle
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'
implementation 'com.squareup.okhttp3:logging-interceptor'
```

## Consideraciones de Rendimiento

- **Timeouts configurados** para evitar bloqueos
- **Archivos temporales** eliminados automáticamente
- **Estados de carga** para mejor UX
- **Compresión automática** de archivos antes del envío

## Posibles Mejoras Futuras

- Soporte para análisis en tiempo real
- Guardado de historial de análisis
- Exportación de resultados
- Análisis batch de múltiples archivos
- Integración con cámara en tiempo real
- Soporte para más formatos de archivo


# Aplicación de Detección de Emociones para Android

## Descripción General

Esta aplicación móvil para Android permite detectar emociones en rostros humanos mediante análisis de imágenes y videos. Utiliza una API externa que procesa el contenido multimedia y devuelve información detallada sobre las emociones detectadas.

## Características Principales

- **Captura de imágenes** desde la cámara del dispositivo
- **Grabación de videos** con audio
- **Selección de archivos** desde la galería del dispositivo
- **Análisis de emociones** en tiempo real
- **Interfaz intuitiva** con Material Design 3
- **Visualización detallada** de resultados con gráficos de barras
- **Soporte para múltiples personas** en una sola imagen

## Emociones Detectadas

La aplicación puede identificar las siguientes emociones:
- 😊 **Felicidad (Happy)**
- 😢 **Tristeza (Sad)**
- 😠 **Enojo (Anger)**
- 😨 **Miedo (Fear)**
- 😲 **Sorpresa (Surprise)**
- 🤢 **Disgusto (Disgust)**
- 😐 **Neutral (Neutral)**

## Arquitectura Técnica

### Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal de desarrollo
- **Jetpack Compose** - Framework de UI declarativo
- **Retrofit** - Cliente HTTP para llamadas a la API
- **Gson** - Serialización/deserialización JSON
- **Material Design 3** - Sistema de diseño de Google
- **ViewModel & StateFlow** - Gestión de estado y arquitectura MVVM
- **OkHttp** - Cliente HTTP con interceptores de logging

### Estructura del Proyecto

```
com.example.finalmovilesprueba/
├── MainActivity.kt                 # Actividad principal
├── model/
│   └── EmotionModels.kt           # Modelos de datos
├── network/
│   ├── ApiService.kt              # Interfaz de la API
│   └── NetworkModule.kt           # Configuración de red
├── viewmodel/
│   └── EmotionViewModel.kt        # ViewModel principal
└── ui/theme/
    └── Theme.kt                   # Tema de la aplicación
```

## Componentes Principales

### 1. MainActivity
Actividad principal que contiene la interfaz de usuario construida con Jetpack Compose.

**Funciones principales:**
- Gestión de permisos de cámara y audio
- Launchers para captura de imágenes y videos
- Launchers para selección de archivos desde galería
- Interfaz de usuario con botones de acción

### 2. EmotionViewModel
ViewModel que maneja la lógica de negocio y el estado de la aplicación.

**Funciones principales:**
- `uploadFile()` - Subida de archivos a la API
- `createFileFromUri()` - Conversión de URI a File
- `clearResults()` - Limpieza de resultados

**Estado manejado:**
```kotlin
data class EmotionUiState(
    val isLoading: Boolean = false,
    val emotionResponse: EmotionResponse? = null,
    val error: String? = null
)
```

### 3. Models (EmotionModels.kt)
Modelos de datos que representan la respuesta de la API.

**Estructuras principales:**
- `EmotionResponse` - Respuesta principal de la API
- `Resultado` - Información de cada persona detectada
- `Coordenadas` - Posición del rostro en la imagen
- `Emociones` - Valores de probabilidad para cada emoción

**Deserializador personalizado:**
```kotlin
class EmotionResponseDeserializer : JsonDeserializer<EmotionResponse>
```
Maneja dos formatos diferentes:
- **Imágenes**: Lista de resultados con coordenadas
- **Videos**: Objeto de emociones directo sin coordenadas

### 4. Network Layer
Configuración de red usando Retrofit y OkHttp.

**ApiService.kt:**
```kotlin
@Multipart
@POST("/")
suspend fun detectEmotions(@Part file: MultipartBody.Part): Response<EmotionResponse>
```

**NetworkModule.kt:**
- Configuración de timeouts (30 segundos)
- Interceptor de logging para debugging
- URL base de la API

## Funcionalidades de la Interfaz

### Botones de Acción
1. **Seleccionar Imagen** - Abre la galería para seleccionar una imagen
2. **Seleccionar Video** - Abre la galería para seleccionar un video
3. **Tomar Foto** - Abre la cámara para capturar una imagen
4. **Grabar Video** - Abre la cámara para grabar un video
5. **Limpiar Resultados** - Elimina los resultados actuales

### Visualización de Resultados

#### Para Imágenes:
- **Tipo de análisis** (imagen)
- **Número de personas detectadas**
- **Para cada persona:**
  - Emoción principal destacada
  - Coordenadas del rostro (X, Y, Ancho, Alto)
  - Barra de progreso para cada emoción con porcentajes

#### Para Videos:
- **Tipo de análisis** (video)
- **Emociones generales del video**
- **Emoción principal**
- **Barras de progreso** para todas las emociones

### Estados de la Aplicación
- **Cargando** - Indicador circular mientras se procesa
- **Error** - Mensaje de error en caso de fallos
- **Resultados** - Visualización detallada de las emociones detectadas

## Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Configuración FileProvider

Para el manejo de archivos temporales:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API Integration

### Endpoint
```
POST https://detectar-emociones-4koqhixykq-uc.a.run.app/
```

### Formato de Respuesta

**Para Imágenes:**
```json
{
    "tipo": "imagen",
    "resultados": [
        {
            "coordenadas": {
                "x": 1756,
                "y": 547,
                "w": 2388,
                "h": 2388
            },
            "emociones": {
                "Anger": 0.142,
                "Disgust": 0.145,
                "Fear": 0.150,
                "Happy": 0.142,
                "Surprise": 0.148,
                "Sad": 0.139,
                "Neutral": 0.134
            },
            "emocion_principal": "Fear"
        }
    ]
}
```

**Para Videos:**
```json
{
    "tipo": "video",
    "resultados": {
        "Anger": 0,
        "Disgust": 0,
        "Fear": 16,
        "Happy": 14,
        "Surprise": 0,
        "Sad": 0,
        "Neutral": 0
    }
}
```

## Características Técnicas Avanzadas

### Gestión de Archivos Temporales
- Creación automática de archivos temporales en cache
- Limpieza automática después del procesamiento
- Naming con timestamp para evitar conflictos

### Manejo de Estados Asíncronos
- Use de Kotlin Coroutines
- StateFlow para reactive UI
- Manejo proper de loading states

### UI Responsiva
- Diseño adaptativo para diferentes tamaños de pantalla
- Animaciones suaves con Material Design
- Colores temáticos para cada emoción

### Error Handling
- Manejo de errores de red
- Validación de permisos
- Mensajes de error informativos para el usuario

## Instalación y Configuración

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Configurar la URL de la API** en `NetworkModule.kt`
4. **Compilar y ejecutar** en dispositivo o emulador

## Dependencias Principales

```gradle
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'
implementation 'com.squareup.okhttp3:logging-interceptor'
```

## Consideraciones de Rendimiento

- **Timeouts configurados** para evitar bloqueos
- **Archivos temporales** eliminados automáticamente
- **Estados de carga** para mejor UX
- **Compresión automática** de archivos antes del envío

## Posibles Mejoras Futuras

- Soporte para análisis en tiempo real
- Guardado de historial de análisis
- Exportación de resultados
- Análisis batch de múltiples archivos
- Integración con cámara en tiempo real
- Soporte para más formatos de archivo
# Aplicación de Detección de Emociones para Android

## Descripción General

Esta aplicación móvil para Android permite detectar emociones en rostros humanos mediante análisis de imágenes y videos. Utiliza una API externa que procesa el contenido multimedia y devuelve información detallada sobre las emociones detectadas.

## Características Principales

- **Captura de imágenes** desde la cámara del dispositivo
- **Grabación de videos** con audio
- **Selección de archivos** desde la galería del dispositivo
- **Análisis de emociones** en tiempo real
- **Interfaz intuitiva** con Material Design 3
- **Visualización detallada** de resultados con gráficos de barras
- **Soporte para múltiples personas** en una sola imagen

## Emociones Detectadas

La aplicación puede identificar las siguientes emociones:
- 😊 **Felicidad (Happy)**
- 😢 **Tristeza (Sad)**
- 😠 **Enojo (Anger)**
- 😨 **Miedo (Fear)**
- 😲 **Sorpresa (Surprise)**
- 🤢 **Disgusto (Disgust)**
- 😐 **Neutral (Neutral)**

## Arquitectura Técnica

### Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal de desarrollo
- **Jetpack Compose** - Framework de UI declarativo
- **Retrofit** - Cliente HTTP para llamadas a la API
- **Gson** - Serialización/deserialización JSON
- **Material Design 3** - Sistema de diseño de Google
- **ViewModel & StateFlow** - Gestión de estado y arquitectura MVVM
- **OkHttp** - Cliente HTTP con interceptores de logging

### Estructura del Proyecto

```
com.example.finalmovilesprueba/
├── MainActivity.kt                 # Actividad principal
├── model/
│   └── EmotionModels.kt           # Modelos de datos
├── network/
│   ├── ApiService.kt              # Interfaz de la API
│   └── NetworkModule.kt           # Configuración de red
├── viewmodel/
│   └── EmotionViewModel.kt        # ViewModel principal
└── ui/theme/
    └── Theme.kt                   # Tema de la aplicación
```

## Componentes Principales

### 1. MainActivity
Actividad principal que contiene la interfaz de usuario construida con Jetpack Compose.

**Funciones principales:**
- Gestión de permisos de cámara y audio
- Launchers para captura de imágenes y videos
- Launchers para selección de archivos desde galería
- Interfaz de usuario con botones de acción

### 2. EmotionViewModel
ViewModel que maneja la lógica de negocio y el estado de la aplicación.

**Funciones principales:**
- `uploadFile()` - Subida de archivos a la API
- `createFileFromUri()` - Conversión de URI a File
- `clearResults()` - Limpieza de resultados

**Estado manejado:**
```kotlin
data class EmotionUiState(
    val isLoading: Boolean = false,
    val emotionResponse: EmotionResponse? = null,
    val error: String? = null
)
```

### 3. Models (EmotionModels.kt)
Modelos de datos que representan la respuesta de la API.

**Estructuras principales:**
- `EmotionResponse` - Respuesta principal de la API
- `Resultado` - Información de cada persona detectada
- `Coordenadas` - Posición del rostro en la imagen
- `Emociones` - Valores de probabilidad para cada emoción

**Deserializador personalizado:**
```kotlin
class EmotionResponseDeserializer : JsonDeserializer<EmotionResponse>
```
Maneja dos formatos diferentes:
- **Imágenes**: Lista de resultados con coordenadas
- **Videos**: Objeto de emociones directo sin coordenadas

### 4. Network Layer
Configuración de red usando Retrofit y OkHttp.

**ApiService.kt:**
```kotlin
@Multipart
@POST("/")
suspend fun detectEmotions(@Part file: MultipartBody.Part): Response<EmotionResponse>
```

**NetworkModule.kt:**
- Configuración de timeouts (30 segundos)
- Interceptor de logging para debugging
- URL base de la API

## Funcionalidades de la Interfaz

### Botones de Acción
1. **Seleccionar Imagen** - Abre la galería para seleccionar una imagen
2. **Seleccionar Video** - Abre la galería para seleccionar un video
3. **Tomar Foto** - Abre la cámara para capturar una imagen
4. **Grabar Video** - Abre la cámara para grabar un video
5. **Limpiar Resultados** - Elimina los resultados actuales

### Visualización de Resultados

#### Para Imágenes:
- **Tipo de análisis** (imagen)
- **Número de personas detectadas**
- **Para cada persona:**
  - Emoción principal destacada
  - Coordenadas del rostro (X, Y, Ancho, Alto)
  - Barra de progreso para cada emoción con porcentajes

#### Para Videos:
- **Tipo de análisis** (video)
- **Emociones generales del video**
- **Emoción principal**
- **Barras de progreso** para todas las emociones

### Estados de la Aplicación
- **Cargando** - Indicador circular mientras se procesa
- **Error** - Mensaje de error en caso de fallos
- **Resultados** - Visualización detallada de las emociones detectadas

## Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Configuración FileProvider

Para el manejo de archivos temporales:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API Integration

### Endpoint
```
POST https://detectar-emociones-4koqhixykq-uc.a.run.app/
```

### Formato de Respuesta

**Para Imágenes:**
```json
{
    "tipo": "imagen",
    "resultados": [
        {
            "coordenadas": {
                "x": 1756,
                "y": 547,
                "w": 2388,
                "h": 2388
            },
            "emociones": {
                "Anger": 0.142,
                "Disgust": 0.145,
                "Fear": 0.150,
                "Happy": 0.142,
                "Surprise": 0.148,
                "Sad": 0.139,
                "Neutral": 0.134
            },
            "emocion_principal": "Fear"
        }
    ]
}
```

**Para Videos:**
```json
{
    "tipo": "video",
    "resultados": {
        "Anger": 0,
        "Disgust": 0,
        "Fear": 16,
        "Happy": 14,
        "Surprise": 0,
        "Sad": 0,
        "Neutral": 0
    }
}
```

## Características Técnicas Avanzadas

### Gestión de Archivos Temporales
- Creación automática de archivos temporales en cache
- Limpieza automática después del procesamiento
- Naming con timestamp para evitar conflictos

### Manejo de Estados Asíncronos
- Use de Kotlin Coroutines
- StateFlow para reactive UI
- Manejo proper de loading states

### UI Responsiva
- Diseño adaptativo para diferentes tamaños de pantalla
- Animaciones suaves con Material Design
- Colores temáticos para cada emoción

### Error Handling
- Manejo de errores de red
- Validación de permisos
- Mensajes de error informativos para el usuario

## Instalación y Configuración

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Configurar la URL de la API** en `NetworkModule.kt`
4. **Compilar y ejecutar** en dispositivo o emulador

## Dependencias Principales

```gradle
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'
implementation 'com.squareup.okhttp3:logging-interceptor'
```

## Consideraciones de Rendimiento

- **Timeouts configurados** para evitar bloqueos
- **Archivos temporales** eliminados automáticamente
- **Estados de carga** para mejor UX
- **Compresión automática** de archivos antes del envío

## Posibles Mejoras Futuras

- Soporte para análisis en tiempo real
- Guardado de historial de análisis
- Exportación de resultados
- Análisis batch de múltiples archivos
- Integración con cámara en tiempo real
- Soporte para más formatos de archivo

# Aplicación de Detección de Emociones para Android

## Descripción General

Esta aplicación móvil para Android permite detectar emociones en rostros humanos mediante análisis de imágenes y videos. Utiliza una API externa que procesa el contenido multimedia y devuelve información detallada sobre las emociones detectadas.

## Características Principales

- **Captura de imágenes** desde la cámara del dispositivo
- **Grabación de videos** con audio
- **Selección de archivos** desde la galería del dispositivo
- **Análisis de emociones** en tiempo real
- **Interfaz intuitiva** con Material Design 3
- **Visualización detallada** de resultados con gráficos de barras
- **Soporte para múltiples personas** en una sola imagen

## Emociones Detectadas

La aplicación puede identificar las siguientes emociones:
- 😊 **Felicidad (Happy)**
- 😢 **Tristeza (Sad)**
- 😠 **Enojo (Anger)**
- 😨 **Miedo (Fear)**
- 😲 **Sorpresa (Surprise)**
- 🤢 **Disgusto (Disgust)**
- 😐 **Neutral (Neutral)**

## Arquitectura Técnica

### Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal de desarrollo
- **Jetpack Compose** - Framework de UI declarativo
- **Retrofit** - Cliente HTTP para llamadas a la API
- **Gson** - Serialización/deserialización JSON
- **Material Design 3** - Sistema de diseño de Google
- **ViewModel & StateFlow** - Gestión de estado y arquitectura MVVM
- **OkHttp** - Cliente HTTP con interceptores de logging

### Estructura del Proyecto

```
com.example.finalmovilesprueba/
├── MainActivity.kt                 # Actividad principal
├── model/
│   └── EmotionModels.kt           # Modelos de datos
├── network/
│   ├── ApiService.kt              # Interfaz de la API
│   └── NetworkModule.kt           # Configuración de red
├── viewmodel/
│   └── EmotionViewModel.kt        # ViewModel principal
└── ui/theme/
    └── Theme.kt                   # Tema de la aplicación
```

## Componentes Principales

### 1. MainActivity
Actividad principal que contiene la interfaz de usuario construida con Jetpack Compose.

**Funciones principales:**
- Gestión de permisos de cámara y audio
- Launchers para captura de imágenes y videos
- Launchers para selección de archivos desde galería
- Interfaz de usuario con botones de acción

### 2. EmotionViewModel
ViewModel que maneja la lógica de negocio y el estado de la aplicación.

**Funciones principales:**
- `uploadFile()` - Subida de archivos a la API
- `createFileFromUri()` - Conversión de URI a File
- `clearResults()` - Limpieza de resultados

**Estado manejado:**
```kotlin
data class EmotionUiState(
    val isLoading: Boolean = false,
    val emotionResponse: EmotionResponse? = null,
    val error: String? = null
)
```

### 3. Models (EmotionModels.kt)
Modelos de datos que representan la respuesta de la API.

**Estructuras principales:**
- `EmotionResponse` - Respuesta principal de la API
- `Resultado` - Información de cada persona detectada
- `Coordenadas` - Posición del rostro en la imagen
- `Emociones` - Valores de probabilidad para cada emoción

**Deserializador personalizado:**
```kotlin
class EmotionResponseDeserializer : JsonDeserializer<EmotionResponse>
```
Maneja dos formatos diferentes:
- **Imágenes**: Lista de resultados con coordenadas
- **Videos**: Objeto de emociones directo sin coordenadas

### 4. Network Layer
Configuración de red usando Retrofit y OkHttp.

**ApiService.kt:**
```kotlin
@Multipart
@POST("/")
suspend fun detectEmotions(@Part file: MultipartBody.Part): Response<EmotionResponse>
```

**NetworkModule.kt:**
- Configuración de timeouts (30 segundos)
- Interceptor de logging para debugging
- URL base de la API

## Funcionalidades de la Interfaz

### Botones de Acción
1. **Seleccionar Imagen** - Abre la galería para seleccionar una imagen
2. **Seleccionar Video** - Abre la galería para seleccionar un video
3. **Tomar Foto** - Abre la cámara para capturar una imagen
4. **Grabar Video** - Abre la cámara para grabar un video
5. **Limpiar Resultados** - Elimina los resultados actuales

### Visualización de Resultados

#### Para Imágenes:
- **Tipo de análisis** (imagen)
- **Número de personas detectadas**
- **Para cada persona:**
  - Emoción principal destacada
  - Coordenadas del rostro (X, Y, Ancho, Alto)
  - Barra de progreso para cada emoción con porcentajes

#### Para Videos:
- **Tipo de análisis** (video)
- **Emociones generales del video**
- **Emoción principal**
- **Barras de progreso** para todas las emociones

### Estados de la Aplicación
- **Cargando** - Indicador circular mientras se procesa
- **Error** - Mensaje de error en caso de fallos
- **Resultados** - Visualización detallada de las emociones detectadas

## Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Configuración FileProvider

Para el manejo de archivos temporales:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API Integration

### Endpoint
```
POST https://detectar-emociones-4koqhixykq-uc.a.run.app/
```

### Formato de Respuesta

**Para Imágenes:**
```json
{
    "tipo": "imagen",
    "resultados": [
        {
            "coordenadas": {
                "x": 1756,
                "y": 547,
                "w": 2388,
                "h": 2388
            },
            "emociones": {
                "Anger": 0.142,
                "Disgust": 0.145,
                "Fear": 0.150,
                "Happy": 0.142,
                "Surprise": 0.148,
                "Sad": 0.139,
                "Neutral": 0.134
            },
            "emocion_principal": "Fear"
        }
    ]
}
```

**Para Videos:**
```json
{
    "tipo": "video",
    "resultados": {
        "Anger": 0,
        "Disgust": 0,
        "Fear": 16,
        "Happy": 14,
        "Surprise": 0,
        "Sad": 0,
        "Neutral": 0
    }
}
```

## Características Técnicas Avanzadas

### Gestión de Archivos Temporales
- Creación automática de archivos temporales en cache
- Limpieza automática después del procesamiento
- Naming con timestamp para evitar conflictos

### Manejo de Estados Asíncronos
- Use de Kotlin Coroutines
- StateFlow para reactive UI
- Manejo proper de loading states

### UI Responsiva
- Diseño adaptativo para diferentes tamaños de pantalla
- Animaciones suaves con Material Design
- Colores temáticos para cada emoción

### Error Handling
- Manejo de errores de red
- Validación de permisos
- Mensajes de error informativos para el usuario

## Instalación y Configuración

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Configurar la URL de la API** en `NetworkModule.kt`
4. **Compilar y ejecutar** en dispositivo o emulador

## Dependencias Principales

```gradle
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'
implementation 'com.squareup.okhttp3:logging-interceptor'
```

## Consideraciones de Rendimiento

- **Timeouts configurados** para evitar bloqueos
- **Archivos temporales** eliminados automáticamente
- **Estados de carga** para mejor UX
- **Compresión automática** de archivos antes del envío

## Posibles Mejoras Futuras

- Soporte para análisis en tiempo real
- Guardado de historial de análisis
- Exportación de resultados
- Análisis batch de múltiples archivos
- Integración con cámara en tiempo real
- Soporte para más formatos de archivo

# Aplicación de Detección de Emociones para Android

## Descripción General

Esta aplicación móvil para Android permite detectar emociones en rostros humanos mediante análisis de imágenes y videos. Utiliza una API externa que procesa el contenido multimedia y devuelve información detallada sobre las emociones detectadas.

## Características Principales

- **Captura de imágenes** desde la cámara del dispositivo
- **Grabación de videos** con audio
- **Selección de archivos** desde la galería del dispositivo
- **Análisis de emociones** en tiempo real
- **Interfaz intuitiva** con Material Design 3
- **Visualización detallada** de resultados con gráficos de barras
- **Soporte para múltiples personas** en una sola imagen

## Emociones Detectadas

La aplicación puede identificar las siguientes emociones:
- 😊 **Felicidad (Happy)**
- 😢 **Tristeza (Sad)**
- 😠 **Enojo (Anger)**
- 😨 **Miedo (Fear)**
- 😲 **Sorpresa (Surprise)**
- 🤢 **Disgusto (Disgust)**
- 😐 **Neutral (Neutral)**

## Arquitectura Técnica

### Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal de desarrollo
- **Jetpack Compose** - Framework de UI declarativo
- **Retrofit** - Cliente HTTP para llamadas a la API
- **Gson** - Serialización/deserialización JSON
- **Material Design 3** - Sistema de diseño de Google
- **ViewModel & StateFlow** - Gestión de estado y arquitectura MVVM
- **OkHttp** - Cliente HTTP con interceptores de logging

### Estructura del Proyecto

```
com.example.finalmovilesprueba/
├── MainActivity.kt                 # Actividad principal
├── model/
│   └── EmotionModels.kt           # Modelos de datos
├── network/
│   ├── ApiService.kt              # Interfaz de la API
│   └── NetworkModule.kt           # Configuración de red
├── viewmodel/
│   └── EmotionViewModel.kt        # ViewModel principal
└── ui/theme/
    └── Theme.kt                   # Tema de la aplicación
```

## Componentes Principales

### 1. MainActivity
Actividad principal que contiene la interfaz de usuario construida con Jetpack Compose.

**Funciones principales:**
- Gestión de permisos de cámara y audio
- Launchers para captura de imágenes y videos
- Launchers para selección de archivos desde galería
- Interfaz de usuario con botones de acción

### 2. EmotionViewModel
ViewModel que maneja la lógica de negocio y el estado de la aplicación.

**Funciones principales:**
- `uploadFile()` - Subida de archivos a la API
- `createFileFromUri()` - Conversión de URI a File
- `clearResults()` - Limpieza de resultados

**Estado manejado:**
```kotlin
data class EmotionUiState(
    val isLoading: Boolean = false,
    val emotionResponse: EmotionResponse? = null,
    val error: String? = null
)
```

### 3. Models (EmotionModels.kt)
Modelos de datos que representan la respuesta de la API.

**Estructuras principales:**
- `EmotionResponse` - Respuesta principal de la API
- `Resultado` - Información de cada persona detectada
- `Coordenadas` - Posición del rostro en la imagen
- `Emociones` - Valores de probabilidad para cada emoción

**Deserializador personalizado:**
```kotlin
class EmotionResponseDeserializer : JsonDeserializer<EmotionResponse>
```
Maneja dos formatos diferentes:
- **Imágenes**: Lista de resultados con coordenadas
- **Videos**: Objeto de emociones directo sin coordenadas

### 4. Network Layer
Configuración de red usando Retrofit y OkHttp.

**ApiService.kt:**
```kotlin
@Multipart
@POST("/")
suspend fun detectEmotions(@Part file: MultipartBody.Part): Response<EmotionResponse>
```

**NetworkModule.kt:**
- Configuración de timeouts (30 segundos)
- Interceptor de logging para debugging
- URL base de la API

## Funcionalidades de la Interfaz

### Botones de Acción
1. **Seleccionar Imagen** - Abre la galería para seleccionar una imagen
2. **Seleccionar Video** - Abre la galería para seleccionar un video
3. **Tomar Foto** - Abre la cámara para capturar una imagen
4. **Grabar Video** - Abre la cámara para grabar un video
5. **Limpiar Resultados** - Elimina los resultados actuales

### Visualización de Resultados

#### Para Imágenes:
- **Tipo de análisis** (imagen)
- **Número de personas detectadas**
- **Para cada persona:**
  - Emoción principal destacada
  - Coordenadas del rostro (X, Y, Ancho, Alto)
  - Barra de progreso para cada emoción con porcentajes

#### Para Videos:
- **Tipo de análisis** (video)
- **Emociones generales del video**
- **Emoción principal**
- **Barras de progreso** para todas las emociones

### Estados de la Aplicación
- **Cargando** - Indicador circular mientras se procesa
- **Error** - Mensaje de error en caso de fallos
- **Resultados** - Visualización detallada de las emociones detectadas

## Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Configuración FileProvider

Para el manejo de archivos temporales:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API Integration

### Endpoint
```
POST https://detectar-emociones-4koqhixykq-uc.a.run.app/
```

### Formato de Respuesta

**Para Imágenes:**
```json
{
    "tipo": "imagen",
    "resultados": [
        {
            "coordenadas": {
                "x": 1756,
                "y": 547,
                "w": 2388,
                "h": 2388
            },
            "emociones": {
                "Anger": 0.142,
                "Disgust": 0.145,
                "Fear": 0.150,
                "Happy": 0.142,
                "Surprise": 0.148,
                "Sad": 0.139,
                "Neutral": 0.134
            },
            "emocion_principal": "Fear"
        }
    ]
}
```

**Para Videos:**
```json
{
    "tipo": "video",
    "resultados": {
        "Anger": 0,
        "Disgust": 0,
        "Fear": 16,
        "Happy": 14,
        "Surprise": 0,
        "Sad": 0,
        "Neutral": 0
    }
}
```

## Características Técnicas Avanzadas

### Gestión de Archivos Temporales
- Creación automática de archivos temporales en cache
- Limpieza automática después del procesamiento
- Naming con timestamp para evitar conflictos

### Manejo de Estados Asíncronos
- Use de Kotlin Coroutines
- StateFlow para reactive UI
- Manejo proper de loading states

### UI Responsiva
- Diseño adaptativo para diferentes tamaños de pantalla
- Animaciones suaves con Material Design
- Colores temáticos para cada emoción

### Error Handling
- Manejo de errores de red
- Validación de permisos
- Mensajes de error informativos para el usuario

## Instalación y Configuración

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Configurar la URL de la API** en `NetworkModule.kt`
4. **Compilar y ejecutar** en dispositivo o emulador

## Dependencias Principales

```gradle
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'
implementation 'com.squareup.okhttp3:logging-interceptor'
```

## Consideraciones de Rendimiento

- **Timeouts configurados** para evitar bloqueos
- **Archivos temporales** eliminados automáticamente
- **Estados de carga** para mejor UX
- **Compresión automática** de archivos antes del envío

## Posibles Mejoras Futuras

- Soporte para análisis en tiempo real
- Guardado de historial de análisis
- Exportación de resultados
- Análisis batch de múltiples archivos
- Integración con cámara en tiempo real
- Soporte para más formatos de archivo

# Aplicación de Detección de Emociones para Android

## Descripción General

Esta aplicación móvil para Android permite detectar emociones en rostros humanos mediante análisis de imágenes y videos. Utiliza una API externa que procesa el contenido multimedia y devuelve información detallada sobre las emociones detectadas.

## Características Principales

- **Captura de imágenes** desde la cámara del dispositivo
- **Grabación de videos** con audio
- **Selección de archivos** desde la galería del dispositivo
- **Análisis de emociones** en tiempo real
- **Interfaz intuitiva** con Material Design 3
- **Visualización detallada** de resultados con gráficos de barras
- **Soporte para múltiples personas** en una sola imagen

## Emociones Detectadas

La aplicación puede identificar las siguientes emociones:
- 😊 **Felicidad (Happy)**
- 😢 **Tristeza (Sad)**
- 😠 **Enojo (Anger)**
- 😨 **Miedo (Fear)**
- 😲 **Sorpresa (Surprise)**
- 🤢 **Disgusto (Disgust)**
- 😐 **Neutral (Neutral)**

## Arquitectura Técnica

### Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal de desarrollo
- **Jetpack Compose** - Framework de UI declarativo
- **Retrofit** - Cliente HTTP para llamadas a la API
- **Gson** - Serialización/deserialización JSON
- **Material Design 3** - Sistema de diseño de Google
- **ViewModel & StateFlow** - Gestión de estado y arquitectura MVVM
- **OkHttp** - Cliente HTTP con interceptores de logging

### Estructura del Proyecto

```
com.example.finalmovilesprueba/
├── MainActivity.kt                 # Actividad principal
├── model/
│   └── EmotionModels.kt           # Modelos de datos
├── network/
│   ├── ApiService.kt              # Interfaz de la API
│   └── NetworkModule.kt           # Configuración de red
├── viewmodel/
│   └── EmotionViewModel.kt        # ViewModel principal
└── ui/theme/
    └── Theme.kt                   # Tema de la aplicación
```

## Componentes Principales

### 1. MainActivity
Actividad principal que contiene la interfaz de usuario construida con Jetpack Compose.

**Funciones principales:**
- Gestión de permisos de cámara y audio
- Launchers para captura de imágenes y videos
- Launchers para selección de archivos desde galería
- Interfaz de usuario con botones de acción

### 2. EmotionViewModel
ViewModel que maneja la lógica de negocio y el estado de la aplicación.

**Funciones principales:**
- `uploadFile()` - Subida de archivos a la API
- `createFileFromUri()` - Conversión de URI a File
- `clearResults()` - Limpieza de resultados

**Estado manejado:**
```kotlin
data class EmotionUiState(
    val isLoading: Boolean = false,
    val emotionResponse: EmotionResponse? = null,
    val error: String? = null
)
```

### 3. Models (EmotionModels.kt)
Modelos de datos que representan la respuesta de la API.

**Estructuras principales:**
- `EmotionResponse` - Respuesta principal de la API
- `Resultado` - Información de cada persona detectada
- `Coordenadas` - Posición del rostro en la imagen
- `Emociones` - Valores de probabilidad para cada emoción

**Deserializador personalizado:**
```kotlin
class EmotionResponseDeserializer : JsonDeserializer<EmotionResponse>
```
Maneja dos formatos diferentes:
- **Imágenes**: Lista de resultados con coordenadas
- **Videos**: Objeto de emociones directo sin coordenadas

### 4. Network Layer
Configuración de red usando Retrofit y OkHttp.

**ApiService.kt:**
```kotlin
@Multipart
@POST("/")
suspend fun detectEmotions(@Part file: MultipartBody.Part): Response<EmotionResponse>
```

**NetworkModule.kt:**
- Configuración de timeouts (30 segundos)
- Interceptor de logging para debugging
- URL base de la API

## Funcionalidades de la Interfaz

### Botones de Acción
1. **Seleccionar Imagen** - Abre la galería para seleccionar una imagen
2. **Seleccionar Video** - Abre la galería para seleccionar un video
3. **Tomar Foto** - Abre la cámara para capturar una imagen
4. **Grabar Video** - Abre la cámara para grabar un video
5. **Limpiar Resultados** - Elimina los resultados actuales

### Visualización de Resultados

#### Para Imágenes:
- **Tipo de análisis** (imagen)
- **Número de personas detectadas**
- **Para cada persona:**
  - Emoción principal destacada
  - Coordenadas del rostro (X, Y, Ancho, Alto)
  - Barra de progreso para cada emoción con porcentajes

#### Para Videos:
- **Tipo de análisis** (video)
- **Emociones generales del video**
- **Emoción principal**
- **Barras de progreso** para todas las emociones

### Estados de la Aplicación
- **Cargando** - Indicador circular mientras se procesa
- **Error** - Mensaje de error en caso de fallos
- **Resultados** - Visualización detallada de las emociones detectadas

## Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Configuración FileProvider

Para el manejo de archivos temporales:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API Integration

### Endpoint
```
POST https://detectar-emociones-4koqhixykq-uc.a.run.app/
```

### Formato de Respuesta

**Para Imágenes:**
```json
{
    "tipo": "imagen",
    "resultados": [
        {
            "coordenadas": {
                "x": 1756,
                "y": 547,
                "w": 2388,
                "h": 2388
            },
            "emociones": {
                "Anger": 0.142,
                "Disgust": 0.145,
                "Fear": 0.150,
                "Happy": 0.142,
                "Surprise": 0.148,
                "Sad": 0.139,
                "Neutral": 0.134
            },
            "emocion_principal": "Fear"
        }
    ]
}
```

**Para Videos:**
```json
{
    "tipo": "video",
    "resultados": {
        "Anger": 0,
        "Disgust": 0,
        "Fear": 16,
        "Happy": 14,
        "Surprise": 0,
        "Sad": 0,
        "Neutral": 0
    }
}
```

## Características Técnicas Avanzadas

### Gestión de Archivos Temporales
- Creación automática de archivos temporales en cache
- Limpieza automática después del procesamiento
- Naming con timestamp para evitar conflictos

### Manejo de Estados Asíncronos
- Use de Kotlin Coroutines
- StateFlow para reactive UI
- Manejo proper de loading states

### UI Responsiva
- Diseño adaptativo para diferentes tamaños de pantalla
- Animaciones suaves con Material Design
- Colores temáticos para cada emoción

### Error Handling
- Manejo de errores de red
- Validación de permisos
- Mensajes de error informativos para el usuario

## Instalación y Configuración

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Configurar la URL de la API** en `NetworkModule.kt`
4. **Compilar y ejecutar** en dispositivo o emulador

## Dependencias Principales

```gradle
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'
implementation 'com.squareup.okhttp3:logging-interceptor'
```

## Consideraciones de Rendimiento

- **Timeouts configurados** para evitar bloqueos
- **Archivos temporales** eliminados automáticamente
- **Estados de carga** para mejor UX
- **Compresión automática** de archivos antes del envío

## Posibles Mejoras Futuras

- Soporte para análisis en tiempo real
- Guardado de historial de análisis
- Exportación de resultados
- Análisis batch de múltiples archivos
- Integración con cámara en tiempo real
- Soporte para más formatos de archivo
# Aplicación de Detección de Emociones para Android

## Descripción General

Esta aplicación móvil para Android permite detectar emociones en rostros humanos mediante análisis de imágenes y videos. Utiliza una API externa que procesa el contenido multimedia y devuelve información detallada sobre las emociones detectadas.

## Características Principales

- **Captura de imágenes** desde la cámara del dispositivo
- **Grabación de videos** con audio
- **Selección de archivos** desde la galería del dispositivo
- **Análisis de emociones** en tiempo real
- **Interfaz intuitiva** con Material Design 3
- **Visualización detallada** de resultados con gráficos de barras
- **Soporte para múltiples personas** en una sola imagen

## Emociones Detectadas

La aplicación puede identificar las siguientes emociones:
- 😊 **Felicidad (Happy)**
- 😢 **Tristeza (Sad)**
- 😠 **Enojo (Anger)**
- 😨 **Miedo (Fear)**
- 😲 **Sorpresa (Surprise)**
- 🤢 **Disgusto (Disgust)**
- 😐 **Neutral (Neutral)**

## Arquitectura Técnica

### Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal de desarrollo
- **Jetpack Compose** - Framework de UI declarativo
- **Retrofit** - Cliente HTTP para llamadas a la API
- **Gson** - Serialización/deserialización JSON
- **Material Design 3** - Sistema de diseño de Google
- **ViewModel & StateFlow** - Gestión de estado y arquitectura MVVM
- **OkHttp** - Cliente HTTP con interceptores de logging

### Estructura del Proyecto

```
com.example.finalmovilesprueba/
├── MainActivity.kt                 # Actividad principal
├── model/
│   └── EmotionModels.kt           # Modelos de datos
├── network/
│   ├── ApiService.kt              # Interfaz de la API
│   └── NetworkModule.kt           # Configuración de red
├── viewmodel/
│   └── EmotionViewModel.kt        # ViewModel principal
└── ui/theme/
    └── Theme.kt                   # Tema de la aplicación
```

## Componentes Principales

### 1. MainActivity
Actividad principal que contiene la interfaz de usuario construida con Jetpack Compose.

**Funciones principales:**
- Gestión de permisos de cámara y audio
- Launchers para captura de imágenes y videos
- Launchers para selección de archivos desde galería
- Interfaz de usuario con botones de acción

### 2. EmotionViewModel
ViewModel que maneja la lógica de negocio y el estado de la aplicación.

**Funciones principales:**
- `uploadFile()` - Subida de archivos a la API
- `createFileFromUri()` - Conversión de URI a File
- `clearResults()` - Limpieza de resultados

**Estado manejado:**
```kotlin
data class EmotionUiState(
    val isLoading: Boolean = false,
    val emotionResponse: EmotionResponse? = null,
    val error: String? = null
)
```

### 3. Models (EmotionModels.kt)
Modelos de datos que representan la respuesta de la API.

**Estructuras principales:**
- `EmotionResponse` - Respuesta principal de la API
- `Resultado` - Información de cada persona detectada
- `Coordenadas` - Posición del rostro en la imagen
- `Emociones` - Valores de probabilidad para cada emoción

**Deserializador personalizado:**
```kotlin
class EmotionResponseDeserializer : JsonDeserializer<EmotionResponse>
```
Maneja dos formatos diferentes:
- **Imágenes**: Lista de resultados con coordenadas
- **Videos**: Objeto de emociones directo sin coordenadas

### 4. Network Layer
Configuración de red usando Retrofit y OkHttp.

**ApiService.kt:**
```kotlin
@Multipart
@POST("/")
suspend fun detectEmotions(@Part file: MultipartBody.Part): Response<EmotionResponse>
```

**NetworkModule.kt:**
- Configuración de timeouts (30 segundos)
- Interceptor de logging para debugging
- URL base de la API

## Funcionalidades de la Interfaz

### Botones de Acción
1. **Seleccionar Imagen** - Abre la galería para seleccionar una imagen
2. **Seleccionar Video** - Abre la galería para seleccionar un video
3. **Tomar Foto** - Abre la cámara para capturar una imagen
4. **Grabar Video** - Abre la cámara para grabar un video
5. **Limpiar Resultados** - Elimina los resultados actuales

### Visualización de Resultados

#### Para Imágenes:
- **Tipo de análisis** (imagen)
- **Número de personas detectadas**
- **Para cada persona:**
  - Emoción principal destacada
  - Coordenadas del rostro (X, Y, Ancho, Alto)
  - Barra de progreso para cada emoción con porcentajes

#### Para Videos:
- **Tipo de análisis** (video)
- **Emociones generales del video**
- **Emoción principal**
- **Barras de progreso** para todas las emociones

### Estados de la Aplicación
- **Cargando** - Indicador circular mientras se procesa
- **Error** - Mensaje de error en caso de fallos
- **Resultados** - Visualización detallada de las emociones detectadas

## Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Configuración FileProvider

Para el manejo de archivos temporales:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API Integration

### Endpoint
```
POST https://detectar-emociones-4koqhixykq-uc.a.run.app/
```

### Formato de Respuesta

**Para Imágenes:**
```json
{
    "tipo": "imagen",
    "resultados": [
        {
            "coordenadas": {
                "x": 1756,
                "y": 547,
                "w": 2388,
                "h": 2388
            },
            "emociones": {
                "Anger": 0.142,
                "Disgust": 0.145,
                "Fear": 0.150,
                "Happy": 0.142,
                "Surprise": 0.148,
                "Sad": 0.139,
                "Neutral": 0.134
            },
            "emocion_principal": "Fear"
        }
    ]
}
```

**Para Videos:**
```json
{
    "tipo": "video",
    "resultados": {
        "Anger": 0,
        "Disgust": 0,
        "Fear": 16,
        "Happy": 14,
        "Surprise": 0,
        "Sad": 0,
        "Neutral": 0
    }
}
```

## Características Técnicas Avanzadas

### Gestión de Archivos Temporales
- Creación automática de archivos temporales en cache
- Limpieza automática después del procesamiento
- Naming con timestamp para evitar conflictos

### Manejo de Estados Asíncronos
- Use de Kotlin Coroutines
- StateFlow para reactive UI
- Manejo proper de loading states

### UI Responsiva
- Diseño adaptativo para diferentes tamaños de pantalla
- Animaciones suaves con Material Design
- Colores temáticos para cada emoción

### Error Handling
- Manejo de errores de red
- Validación de permisos
- Mensajes de error informativos para el usuario

## Instalación y Configuración

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Configurar la URL de la API** en `NetworkModule.kt`
4. **Compilar y ejecutar** en dispositivo o emulador

## Dependencias Principales

```gradle
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'
implementation 'com.squareup.okhttp3:logging-interceptor'
```

## Consideraciones de Rendimiento

- **Timeouts configurados** para evitar bloqueos
- **Archivos temporales** eliminados automáticamente
- **Estados de carga** para mejor UX
- **Compresión automática** de archivos antes del envío

## Posibles Mejoras Futuras

- Soporte para análisis en tiempo real
- Guardado de historial de análisis
- Exportación de resultados
- Análisis batch de múltiples archivos
- Integración con cámara en tiempo real
- Soporte para más formatos de archivo

# Aplicación de Detección de Emociones para Android

## Descripción General

Esta aplicación móvil para Android permite detectar emociones en rostros humanos mediante análisis de imágenes y videos. Utiliza una API externa que procesa el contenido multimedia y devuelve información detallada sobre las emociones detectadas.

## Características Principales

- **Captura de imágenes** desde la cámara del dispositivo
- **Grabación de videos** con audio
- **Selección de archivos** desde la galería del dispositivo
- **Análisis de emociones** en tiempo real
- **Interfaz intuitiva** con Material Design 3
- **Visualización detallada** de resultados con gráficos de barras
- **Soporte para múltiples personas** en una sola imagen

## Emociones Detectadas

La aplicación puede identificar las siguientes emociones:
- 😊 **Felicidad (Happy)**
- 😢 **Tristeza (Sad)**
- 😠 **Enojo (Anger)**
- 😨 **Miedo (Fear)**
- 😲 **Sorpresa (Surprise)**
- 🤢 **Disgusto (Disgust)**
- 😐 **Neutral (Neutral)**

## Arquitectura Técnica

### Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal de desarrollo
- **Jetpack Compose** - Framework de UI declarativo
- **Retrofit** - Cliente HTTP para llamadas a la API
- **Gson** - Serialización/deserialización JSON
- **Material Design 3** - Sistema de diseño de Google
- **ViewModel & StateFlow** - Gestión de estado y arquitectura MVVM
- **OkHttp** - Cliente HTTP con interceptores de logging

### Estructura del Proyecto

```
com.example.finalmovilesprueba/
├── MainActivity.kt                 # Actividad principal
├── model/
│   └── EmotionModels.kt           # Modelos de datos
├── network/
│   ├── ApiService.kt              # Interfaz de la API
│   └── NetworkModule.kt           # Configuración de red
├── viewmodel/
│   └── EmotionViewModel.kt        # ViewModel principal
└── ui/theme/
    └── Theme.kt                   # Tema de la aplicación
```

## Componentes Principales

### 1. MainActivity
Actividad principal que contiene la interfaz de usuario construida con Jetpack Compose.

**Funciones principales:**
- Gestión de permisos de cámara y audio
- Launchers para captura de imágenes y videos
- Launchers para selección de archivos desde galería
- Interfaz de usuario con botones de acción

### 2. EmotionViewModel
ViewModel que maneja la lógica de negocio y el estado de la aplicación.

**Funciones principales:**
- `uploadFile()` - Subida de archivos a la API
- `createFileFromUri()` - Conversión de URI a File
- `clearResults()` - Limpieza de resultados

**Estado manejado:**
```kotlin
data class EmotionUiState(
    val isLoading: Boolean = false,
    val emotionResponse: EmotionResponse? = null,
    val error: String? = null
)
```

### 3. Models (EmotionModels.kt)
Modelos de datos que representan la respuesta de la API.

**Estructuras principales:**
- `EmotionResponse` - Respuesta principal de la API
- `Resultado` - Información de cada persona detectada
- `Coordenadas` - Posición del rostro en la imagen
- `Emociones` - Valores de probabilidad para cada emoción

**Deserializador personalizado:**
```kotlin
class EmotionResponseDeserializer : JsonDeserializer<EmotionResponse>
```
Maneja dos formatos diferentes:
- **Imágenes**: Lista de resultados con coordenadas
- **Videos**: Objeto de emociones directo sin coordenadas

### 4. Network Layer
Configuración de red usando Retrofit y OkHttp.

**ApiService.kt:**
```kotlin
@Multipart
@POST("/")
suspend fun detectEmotions(@Part file: MultipartBody.Part): Response<EmotionResponse>
```

**NetworkModule.kt:**
- Configuración de timeouts (30 segundos)
- Interceptor de logging para debugging
- URL base de la API

## Funcionalidades de la Interfaz

### Botones de Acción
1. **Seleccionar Imagen** - Abre la galería para seleccionar una imagen
2. **Seleccionar Video** - Abre la galería para seleccionar un video
3. **Tomar Foto** - Abre la cámara para capturar una imagen
4. **Grabar Video** - Abre la cámara para grabar un video
5. **Limpiar Resultados** - Elimina los resultados actuales

### Visualización de Resultados

#### Para Imágenes:
- **Tipo de análisis** (imagen)
- **Número de personas detectadas**
- **Para cada persona:**
  - Emoción principal destacada
  - Coordenadas del rostro (X, Y, Ancho, Alto)
  - Barra de progreso para cada emoción con porcentajes

#### Para Videos:
- **Tipo de análisis** (video)
- **Emociones generales del video**
- **Emoción principal**
- **Barras de progreso** para todas las emociones

### Estados de la Aplicación
- **Cargando** - Indicador circular mientras se procesa
- **Error** - Mensaje de error en caso de fallos
- **Resultados** - Visualización detallada de las emociones detectadas

## Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Configuración FileProvider

Para el manejo de archivos temporales:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API Integration

### Endpoint
```
POST https://detectar-emociones-4koqhixykq-uc.a.run.app/
```

### Formato de Respuesta

**Para Imágenes:**
```json
{
    "tipo": "imagen",
    "resultados": [
        {
            "coordenadas": {
                "x": 1756,
                "y": 547,
                "w": 2388,
                "h": 2388
            },
            "emociones": {
                "Anger": 0.142,
                "Disgust": 0.145,
                "Fear": 0.150,
                "Happy": 0.142,
                "Surprise": 0.148,
                "Sad": 0.139,
                "Neutral": 0.134
            },
            "emocion_principal": "Fear"
        }
    ]
}
```

**Para Videos:**
```json
{
    "tipo": "video",
    "resultados": {
        "Anger": 0,
        "Disgust": 0,
        "Fear": 16,
        "Happy": 14,
        "Surprise": 0,
        "Sad": 0,
        "Neutral": 0
    }
}
```

## Características Técnicas Avanzadas

### Gestión de Archivos Temporales
- Creación automática de archivos temporales en cache
- Limpieza automática después del procesamiento
- Naming con timestamp para evitar conflictos

### Manejo de Estados Asíncronos
- Use de Kotlin Coroutines
- StateFlow para reactive UI
- Manejo proper de loading states

### UI Responsiva
- Diseño adaptativo para diferentes tamaños de pantalla
- Animaciones suaves con Material Design
- Colores temáticos para cada emoción

### Error Handling
- Manejo de errores de red
- Validación de permisos
- Mensajes de error informativos para el usuario

## Instalación y Configuración

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Configurar la URL de la API** en `NetworkModule.kt`
4. **Compilar y ejecutar** en dispositivo o emulador

## Dependencias Principales

```gradle
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'
implementation 'com.squareup.okhttp3:logging-interceptor'
```

## Consideraciones de Rendimiento

- **Timeouts configurados** para evitar bloqueos
- **Archivos temporales** eliminados automáticamente
- **Estados de carga** para mejor UX
- **Compresión automática** de archivos antes del envío

## Posibles Mejoras Futuras

- Soporte para análisis en tiempo real
- Guardado de historial de análisis
- Exportación de resultados
- Análisis batch de múltiples archivos
- Integración con cámara en tiempo real
- Soporte para más formatos de archivo

# Aplicación de Detección de Emociones para Android

## Descripción General

Esta aplicación móvil para Android permite detectar emociones en rostros humanos mediante análisis de imágenes y videos. Utiliza una API externa que procesa el contenido multimedia y devuelve información detallada sobre las emociones detectadas.

## Características Principales

- **Captura de imágenes** desde la cámara del dispositivo
- **Grabación de videos** con audio
- **Selección de archivos** desde la galería del dispositivo
- **Análisis de emociones** en tiempo real
- **Interfaz intuitiva** con Material Design 3
- **Visualización detallada** de resultados con gráficos de barras
- **Soporte para múltiples personas** en una sola imagen

## Emociones Detectadas

La aplicación puede identificar las siguientes emociones:
- 😊 **Felicidad (Happy)**
- 😢 **Tristeza (Sad)**
- 😠 **Enojo (Anger)**
- 😨 **Miedo (Fear)**
- 😲 **Sorpresa (Surprise)**
- 🤢 **Disgusto (Disgust)**
- 😐 **Neutral (Neutral)**

## Arquitectura Técnica

### Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal de desarrollo
- **Jetpack Compose** - Framework de UI declarativo
- **Retrofit** - Cliente HTTP para llamadas a la API
- **Gson** - Serialización/deserialización JSON
- **Material Design 3** - Sistema de diseño de Google
- **ViewModel & StateFlow** - Gestión de estado y arquitectura MVVM
- **OkHttp** - Cliente HTTP con interceptores de logging

### Estructura del Proyecto

```
com.example.finalmovilesprueba/
├── MainActivity.kt                 # Actividad principal
├── model/
│   └── EmotionModels.kt           # Modelos de datos
├── network/
│   ├── ApiService.kt              # Interfaz de la API
│   └── NetworkModule.kt           # Configuración de red
├── viewmodel/
│   └── EmotionViewModel.kt        # ViewModel principal
└── ui/theme/
    └── Theme.kt                   # Tema de la aplicación
```

## Componentes Principales

### 1. MainActivity
Actividad principal que contiene la interfaz de usuario construida con Jetpack Compose.

**Funciones principales:**
- Gestión de permisos de cámara y audio
- Launchers para captura de imágenes y videos
- Launchers para selección de archivos desde galería
- Interfaz de usuario con botones de acción

### 2. EmotionViewModel
ViewModel que maneja la lógica de negocio y el estado de la aplicación.

**Funciones principales:**
- `uploadFile()` - Subida de archivos a la API
- `createFileFromUri()` - Conversión de URI a File
- `clearResults()` - Limpieza de resultados

**Estado manejado:**
```kotlin
data class EmotionUiState(
    val isLoading: Boolean = false,
    val emotionResponse: EmotionResponse? = null,
    val error: String? = null
)
```

### 3. Models (EmotionModels.kt)
Modelos de datos que representan la respuesta de la API.

**Estructuras principales:**
- `EmotionResponse` - Respuesta principal de la API
- `Resultado` - Información de cada persona detectada
- `Coordenadas` - Posición del rostro en la imagen
- `Emociones` - Valores de probabilidad para cada emoción

**Deserializador personalizado:**
```kotlin
class EmotionResponseDeserializer : JsonDeserializer<EmotionResponse>
```
Maneja dos formatos diferentes:
- **Imágenes**: Lista de resultados con coordenadas
- **Videos**: Objeto de emociones directo sin coordenadas

### 4. Network Layer
Configuración de red usando Retrofit y OkHttp.

**ApiService.kt:**
```kotlin
@Multipart
@POST("/")
suspend fun detectEmotions(@Part file: MultipartBody.Part): Response<EmotionResponse>
```

**NetworkModule.kt:**
- Configuración de timeouts (30 segundos)
- Interceptor de logging para debugging
- URL base de la API

## Funcionalidades de la Interfaz

### Botones de Acción
1. **Seleccionar Imagen** - Abre la galería para seleccionar una imagen
2. **Seleccionar Video** - Abre la galería para seleccionar un video
3. **Tomar Foto** - Abre la cámara para capturar una imagen
4. **Grabar Video** - Abre la cámara para grabar un video
5. **Limpiar Resultados** - Elimina los resultados actuales

### Visualización de Resultados

#### Para Imágenes:
- **Tipo de análisis** (imagen)
- **Número de personas detectadas**
- **Para cada persona:**
  - Emoción principal destacada
  - Coordenadas del rostro (X, Y, Ancho, Alto)
  - Barra de progreso para cada emoción con porcentajes

#### Para Videos:
- **Tipo de análisis** (video)
- **Emociones generales del video**
- **Emoción principal**
- **Barras de progreso** para todas las emociones

### Estados de la Aplicación
- **Cargando** - Indicador circular mientras se procesa
- **Error** - Mensaje de error en caso de fallos
- **Resultados** - Visualización detallada de las emociones detectadas

## Permisos Requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Configuración FileProvider

Para el manejo de archivos temporales:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API Integration

### Endpoint
```
POST https://detectar-emociones-4koqhixykq-uc.a.run.app/
```

### Formato de Respuesta

**Para Imágenes:**
```json
{
    "tipo": "imagen",
    "resultados": [
        {
            "coordenadas": {
                "x": 1756,
                "y": 547,
                "w": 2388,
                "h": 2388
            },
            "emociones": {
                "Anger": 0.142,
                "Disgust": 0.145,
                "Fear": 0.150,
                "Happy": 0.142,
                "Surprise": 0.148,
                "Sad": 0.139,
                "Neutral": 0.134
            },
            "emocion_principal": "Fear"
        }
    ]
}
```

**Para Videos:**
```json
{
    "tipo": "video",
    "resultados": {
        "Anger": 0,
        "Disgust": 0,
        "Fear": 16,
        "Happy": 14,
        "Surprise": 0,
        "Sad": 0,
        "Neutral": 0
    }
}
```

## Características Técnicas Avanzadas

### Gestión de Archivos Temporales
- Creación automática de archivos temporales en cache
- Limpieza automática después del procesamiento
- Naming con timestamp para evitar conflictos

### Manejo de Estados Asíncronos
- Use de Kotlin Coroutines
- StateFlow para reactive UI
- Manejo proper de loading states

### UI Responsiva
- Diseño adaptativo para diferentes tamaños de pantalla
- Animaciones suaves con Material Design
- Colores temáticos para cada emoción

### Error Handling
- Manejo de errores de red
- Validación de permisos
- Mensajes de error informativos para el usuario

## Instalación y Configuración

1. **Clonar el repositorio**
2. **Abrir en Android Studio**
3. **Configurar la URL de la API** en `NetworkModule.kt`
4. **Compilar y ejecutar** en dispositivo o emulador

## Dependencias Principales

```gradle
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'
implementation 'com.squareup.retrofit2:retrofit'
implementation 'com.squareup.retrofit2:converter-gson'
implementation 'com.squareup.okhttp3:logging-interceptor'
```

## Consideraciones de Rendimiento

- **Timeouts configurados** para evitar bloqueos
- **Archivos temporales** eliminados automáticamente
- **Estados de carga** para mejor UX
- **Compresión automática** de archivos antes del envío

## Posibles Mejoras Futuras

- Soporte para análisis en tiempo real
- Guardado de historial de análisis
- Exportación de resultados
- Análisis batch de múltiples archivos
- Integración con cámara en tiempo real
- Soporte para más formatos de archivo