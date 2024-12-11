import cv2
import pytesseract
import numpy as np
from flask import Flask, request, jsonify

app = Flask(__name__)

# Fonction pour extraire un champ spécifique à partir du texte OCR
def extract_field(text, field_name, line_offset=0, reverse=False):
    lines = text.split('\n')
    for i, line in enumerate(lines):
        if field_name in line:
            return lines[i + line_offset] if not reverse else lines[i - 1]
    return ""

@app.route('/process_image', methods=['POST'])
def process_image():
    try:
        # Récupérer l'image envoyée
        file = request.files['image']
        image = np.frombuffer(file.read(), np.uint8)
        image = cv2.imdecode(image, cv2.IMREAD_COLOR)

        # Étape 1 : Prétraitement (conversion en gris, amélioration du contraste)
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        gray = clahe.apply(gray)
        thresh = cv2.adaptiveThreshold(gray, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
                                       cv2.THRESH_BINARY, 11, 2)

        # Étape 2 : Utilisation de pytesseract pour extraire le texte
        text = pytesseract.image_to_string(thresh, lang="fra")  # Utilisez 'fra' pour français
        print("Texte extrait:", text)  # Debug

        # Étape 3 : Extraction des champs spécifiques à partir du texte OCR
        results = {
            "nom": extract_field(text, "Nom", line_offset=0),
            "prenom": extract_field(text, "Nom", line_offset=1),
            "date_naissance": extract_field(text, "Né le"),
            "adresse": extract_field(text, "à"),
            "numero_cin": extract_field(text, "Valable jusqu’au", reverse=True),
        }

        print("Données extraites:", results)  # Debug

        return jsonify(results)

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, port=5000)
