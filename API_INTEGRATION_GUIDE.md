# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 

# Guía de Integración - API de Detección de Emociones

## Resumen

Se ha integrado exitosamente la API de detección de emociones `https://detect-emotions-3uyocih3lq-uc.a.run.app` en el proyecto SisvitaG2. Esta integración permite analizar videos grabados directamente desde la cámara de la aplicación y obtener resultados de emociones en tiempo real.

## Características Implementadas

### 1. **Análisis Directo de Video**
- Grabación de video usando CameraX
- Análisis inmediato usando la API externa
- Visualización de resultados en tiempo real
- Manejo de errores robusto

### 2. **Formato de Respuesta**
La API devuelve resultados en el siguiente formato:
```json
{
    "angry": 4,
    "disgust": 0,
    "fear": 5,
    "happy": 75,
    "sad": 1,
    "surprise": 0,
    "neutral": 11
}
```

### 3. **Visualización de Resultados**
- Emoción dominante destacada
- Barras de progreso para cada emoción
- Porcentajes calculados automáticamente
- Interfaz moderna con Material Design 3

## Archivos Modificados/Creados

### Nuevos Archivos
1. **`EmotionalAnalysisApiService.kt`**
   - Servicio para comunicarse con la API externa
   - Manejo de peticiones HTTP con OkHttp
   - Parsing de respuestas JSON
   - Timeouts configurados (60 segundos)

2. **`API_INTEGRATION_GUIDE.md`** (este archivo)
   - Documentación de la integración

### Archivos Modificados
1. **`EmotionalAnalysisResponse.kt`**
   - Actualizado para el nuevo formato de respuesta
   - Métodos utilitarios para análisis de datos
   - Soporte para serialización JSON

2. **`EmotionalAnalysisRepository.kt`**
   - Agregado método `analyzeVideoDirectly()`
   - Integración con el nuevo servicio de API
   - Mantiene compatibilidad con Firebase

3. **`CameraScreenViewModel.kt`**
   - Estados para análisis en tiempo real
   - Método `analyzeRecordedVideo()`
   - Manejo de errores y resultados

4. **`CameraScreen.kt`**
   - UI para mostrar resultados del análisis
   - Indicadores de carga
   - Overlays para resultados y errores

5. **`EmotionalAnalysisModule.kt`**
   - Configuración de Koin para el nuevo servicio
   - Inyección de dependencias actualizada

6. **`build.gradle.kts`**
   - Agregada dependencia de OkHttp 4.12.0

## Cómo Usar la Funcionalidad

### 1. **Grabar Video**
1. Abre la aplicación y navega a la pantalla de cámara
2. Presiona el botón de grabación (círculo rojo)
3. Graba tu video (máximo 60 segundos recomendado)
4. Presiona el botón de detener

### 2. **Análisis Automático**
- El análisis comienza automáticamente después de detener la grabación
- Se muestra un indicador de carga durante el proceso
- Los resultados aparecen en una overlay modal

### 3. **Interpretar Resultados**
- **Emoción Dominante**: La emoción con mayor puntuación
- **Barras de Progreso**: Muestran la distribución de todas las emociones
- **Porcentajes**: Calculados automáticamente del total

## Configuración Técnica

### Dependencias Agregadas
```kotlin
// OkHttp para llamadas a API
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Configuración de Koin
```kotlin
// Servicio de API
single { EmotionalAnalysisApiService() }

// Repositorio actualizado
single { EmotionalAnalysisRepository(get(), get(), get(), get()) }

// ViewModel de cámara
viewModel { CameraScreenViewModel(get()) }
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Error de Red**: Problemas de conectividad
2. **Error de API**: Respuestas no exitosas del servidor
3. **Error de Archivo**: Problemas con el video grabado
4. **Error de Permisos**: Cámara no disponible

### Mensajes de Error
- Errores se muestran en overlay modal
- Botón para cerrar y reintentar
- Logs detallados para debugging

## Optimizaciones Implementadas

### 1. **Performance**
- Análisis asíncrono en background
- UI no bloqueada durante el proceso
- Timeouts configurados para evitar esperas infinitas

### 2. **UX/UI**
- Indicadores de carga claros
- Resultados visuales atractivos
- Navegación intuitiva

### 3. **Robustez**
- Manejo de errores completo
- Fallbacks para casos edge
- Logging detallado para debugging

## Próximos Pasos Sugeridos

### 1. **Mejoras de UX**
- Animaciones más suaves
- Sonidos de feedback
- Tutorial integrado

### 2. **Funcionalidades Adicionales**
- Guardar resultados en Firebase
- Historial de análisis
- Comparación entre sesiones

### 3. **Optimizaciones Técnicas**
- Cache de resultados
- Compresión de video antes del envío
- Análisis en lotes

## Troubleshooting

### Problemas Comunes

1. **"Error en el análisis: Network error"**
   - Verificar conexión a internet
   - Revisar logs para detalles específicos

2. **"No hay video grabado para analizar"**
   - Asegurar que la grabación se completó correctamente
   - Verificar permisos de cámara

3. **"Error de configuración: repositorio no disponible"**
   - Verificar configuración de Koin
   - Revisar inyección de dependencias

### Logs Útiles
```bash
# Buscar logs de análisis
adb logcat | grep "EmotionalAnalysisApi"

# Buscar logs de cámara
adb logcat | grep "CameraScreenViewModel"
```

## Conclusión

La integración de la API de detección de emociones está completa y funcional. La aplicación ahora puede:

- Grabar videos desde la cámara
- Analizar emociones en tiempo real
- Mostrar resultados de forma visual y clara
- Manejar errores de forma robusta

La implementación mantiene la arquitectura existente del proyecto y agrega funcionalidad nueva de manera modular y escalable. 