import os
import json
import firebase_admin
from firebase_admin import credentials, firestore
from dotenv import load_dotenv

# Cargar variables del archivo .env
load_dotenv()

# Ruta al JSON de la cuenta de servicio
cred = credentials.Certificate("firebase-service-account.json")
firebase_admin.initialize_app(cred)

# Conexión a Firestore
db = firestore.client()

# Nombre de la colección donde se suben las preguntas
COLLECTION_NAME = os.getenv("COLLECTION_NAME", "questions_by_level")

# Carpeta con los archivos .json
JSON_FOLDER = os.getenv("JSON_FOLDER")


# Recorrer todos los archivos JSON de la carpeta
for filename in os.listdir(JSON_FOLDER):
    if filename.endswith(".json"):
        file_path = os.path.join(JSON_FOLDER, filename)
        
        # Leer el archivo JSON
        with open(file_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        # Extraer el nombre del documento (sin la extensión .json)
        doc_name = filename.replace(".json", "")

        # Subir los datos al documento correspondiente en la colección
        db.collection(COLLECTION_NAME).document(doc_name).set(data)

        print(f"Subido: {filename} → documento: {doc_name}")
