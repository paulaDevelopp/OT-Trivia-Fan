import os
import cloudinary
import cloudinary.uploader
import firebase_admin
from firebase_admin import credentials, firestore
from dotenv import load_dotenv

# Cargar variables del entorno
load_dotenv()

# Configurar Cloudinary
cloudinary.config(
    cloud_name=os.getenv("CLOUDINARY_CLOUD_NAME"),
    api_key=os.getenv("CLOUDINARY_API_KEY"),
    api_secret=os.getenv("CLOUDINARY_API_SECRET")
)

# Inicializar Firebase
cred = credentials.Certificate("../firebase_uploader/firebase-service-account.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# Ruta de las carpetas por dificultad
BASE_DIR = "output"
COLLECTION = "wallpapers"  # colección Firestore

def ya_esta_subida(nombre_archivo: str) -> bool:
    """Verifica si ya existe una imagen con ese nombre."""
    docs = db.collection(COLLECTION).where("filename", "==", nombre_archivo).stream()
    return any(True for _ in docs)

def subir_imagen(path: str, dificultad: str):
    filename = os.path.basename(path)

    if ya_esta_subida(filename):
        print(f"Ya existe: {filename}")
        return

    print(f"Subiendo: {filename} ({dificultad})...")
    result = cloudinary.uploader.upload(
        path,
        folder=f"wallpapers/{dificultad}",
        use_filename=True,
        unique_filename=False,
        overwrite=False
    )

    image_url = result["secure_url"]

    # Guardar en Firestore
    db.collection(COLLECTION).add({
        "filename": filename,
        "url": image_url,
        "difficulty": dificultad
    })

    print(f"Subido: {filename}")

def main():
    for dificultad in os.listdir(BASE_DIR):
        dir_path = os.path.join(BASE_DIR, dificultad)
        if not os.path.isdir(dir_path):
            continue

        for file in os.listdir(dir_path):
            if file.lower().endswith((".png", ".jpg", ".jpeg")):
                subir_imagen(os.path.join(dir_path, file), dificultad)

if __name__ == "__main__":
    main()
