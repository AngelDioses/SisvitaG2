from tensorflow.keras.models import load_model

modelo_antiguo = "modelo_emociones_fer2013.h5"
modelo_nuevo = "modelo_emociones_fer2013_nuevo.keras"

print("Cargando modelo antiguo (formato .h5)...")
modelo = load_model(modelo_antiguo)

print("Guardando en nuevo formato .keras...")
modelo.save(modelo_nuevo, save_format="keras")

print("✅ Modelo reconvertido:", modelo_nuevo)
