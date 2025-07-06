# Script temporal para convertir el modelo
import tensorflow as tf
from tensorflow import keras

try:
    # Cargar el modelo con custom_objects si es necesario
    model = tf.keras.models.load_model('modelo_emociones_50.keras', compile=False)
    
    # Re-guardar el modelo con la versión actual
    model.save('modelo_emociones_50_fixed.keras')
    print("Modelo convertido exitosamente")
    
except Exception as e:
    print(f"Error: {e}")
    # Si falla, intenta cargar solo los pesos
    try:
        # Necesitarás recrear la arquitectura del modelo
        model = create_model()  # Tu función para crear el modelo
        model.load_weights('modelo_emociones_50.keras')
        model.save('modelo_emociones_50_fixed.keras')
        print("Modelo recreado con pesos exitosamente")
    except Exception as e2:
        print(f"Error al cargar pesos: {e2}")