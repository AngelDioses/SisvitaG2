import os
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
from sklearn.metrics import classification_report, confusion_matrix
import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import (Conv2D, MaxPooling2D, Flatten,
                                     Dense, Dropout, BatchNormalization)
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint


# === CONFIGURACIÓN GENERAL ===
IMG_SIZE = 48
BATCH_SIZE = 64
EPOCHS = 50
NUM_CLASSES = 7

base_dir = "FER2013"
train_dir = os.path.join(base_dir, "train")
test_dir = os.path.join(base_dir, "test")

# === PREPROCESAMIENTO ===
train_datagen = ImageDataGenerator(
    rescale=1./255,
    rotation_range=20,
    zoom_range=0.2,
    width_shift_range=0.1,
    height_shift_range=0.1,
    horizontal_flip=True,
    validation_split=0.2
)

test_datagen = ImageDataGenerator(rescale=1./255)

train_generator = train_datagen.flow_from_directory(
    train_dir,
    target_size=(IMG_SIZE, IMG_SIZE),
    batch_size=BATCH_SIZE,
    class_mode='categorical',
    subset='training',
    shuffle=True
)

val_generator = train_datagen.flow_from_directory(
    train_dir,
    target_size=(IMG_SIZE, IMG_SIZE),
    batch_size=BATCH_SIZE,
    class_mode='categorical',
    subset='validation',
    shuffle=True
)

test_generator = test_datagen.flow_from_directory(
    test_dir,
    target_size=(IMG_SIZE, IMG_SIZE),
    batch_size=BATCH_SIZE,
    class_mode='categorical',
    shuffle=False
)

# === MODELO CNN ===
model = Sequential([
    Conv2D(64, (3, 3), activation='relu', input_shape=(IMG_SIZE, IMG_SIZE, 3)),
    BatchNormalization(),
    MaxPooling2D(2, 2),
    Dropout(0.25),

    Conv2D(128, (3, 3), activation='relu'),
    BatchNormalization(),
    MaxPooling2D(2, 2),
    Dropout(0.25),

    Conv2D(256, (3, 3), activation='relu'),
    BatchNormalization(),
    MaxPooling2D(2, 2),
    Dropout(0.25),

    Flatten(),
    Dense(256, activation='relu'),
    Dropout(0.5),
    Dense(NUM_CLASSES, activation='softmax')
])

model.compile(
    optimizer='adam',
    loss='categorical_crossentropy',
    metrics=['accuracy']
)

model.summary()

# === CALLBACKS ===
callbacks = [
    EarlyStopping(patience=8, restore_best_weights=True, monitor='val_loss'),
    ModelCheckpoint("modelo_emociones_fer2013.keras", save_best_only=True, monitor='val_loss')
]

# === ENTRENAMIENTO ===
history = model.fit(
    train_generator,
    epochs=EPOCHS,
    validation_data=val_generator,
    callbacks=callbacks
)

# === EVALUACIÓN FINAL ===
loss, acc = model.evaluate(test_generator)
print(f"Test Accuracy: {acc:.4f}")
print(f"Test Loss: {loss:.4f}")

# === PREDICCIONES ===
y_pred = model.predict(test_generator)
y_pred_classes = np.argmax(y_pred, axis=1)
y_true = test_generator.classes
labels = list(test_generator.class_indices.keys())

# === MATRIZ DE CONFUSIÓN ===
cm = confusion_matrix(y_true, y_pred_classes)
cm_df = pd.DataFrame(cm, index=labels, columns=labels)

plt.figure(figsize=(8, 6))
sns.heatmap(cm_df, annot=True, fmt='d', cmap='Blues')
plt.title("Matriz de Confusión")
plt.ylabel("Etiqueta Real")
plt.xlabel("Etiqueta Predicha")
plt.tight_layout()
plt.savefig("matriz_confusion.png")
plt.show()

# === GRÁFICAS DE ENTRENAMIENTO ===
def plot_metrics(history):
    plt.figure(figsize=(12, 5))

    # Precisión
    plt.subplot(1, 2, 1)
    plt.plot(history.history['accuracy'], label='Entrenamiento')
    plt.plot(history.history['val_accuracy'], label='Validación')
    plt.title('Precisión')
    plt.xlabel('Épocas')
    plt.ylabel('Precisión')
    plt.legend()

    # Pérdida
    plt.subplot(1, 2, 2)
    plt.plot(history.history['loss'], label='Entrenamiento')
    plt.plot(history.history['val_loss'], label='Validación')
    plt.title('Pérdida')
    plt.xlabel('Épocas')
    plt.ylabel('Pérdida')
    plt.legend()

    plt.tight_layout()
    plt.savefig("entrenamiento_curvas.png")
    plt.show()

plot_metrics(history)

# === GUARDAR INFORME EN TXT ===
with open("informe_entrenamiento.txt", "w") as f:
    f.write("=== INFORME DE ENTRENAMIENTO FER2013 ===\n\n")
    f.write(f"Precisión en test: {acc:.4f}\n")
    f.write(f"Pérdida en test: {loss:.4f}\n\n")

    f.write("=== Hiperparámetros ===\n")
    f.write(f"Épocas máximas: {EPOCHS}\n")
    f.write(f"Batch size: {BATCH_SIZE}\n")
    f.write(f"Tamaño de imagen: {IMG_SIZE}x{IMG_SIZE}\n")
    f.write(f"Número de clases: {NUM_CLASSES}\n\n")

    f.write("=== Clases ===\n")
    f.write(', '.join(labels) + "\n\n")

    f.write("=== Classification Report ===\n")
    report = classification_report(y_true, y_pred_classes, target_names=labels)
    f.write(report)
