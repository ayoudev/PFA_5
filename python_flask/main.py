import cv2
import pytesseract
import numpy as np
import re
from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/process_image', methods=['POST'])
def process_image():
    try:
        # Récupérer l'image envoyée
        file = request.files['image']
        image = np.frombuffer(file.read(), np.uint8)
        image = cv2.imdecode(image, cv2.IMREAD_COLOR)

        # Étape 1 : Prétraitement
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        gray = cv2.resize(gray, None, fx=2, fy=2, interpolation=cv2.INTER_CUBIC)  # Augmenter la résolution
        blurred = cv2.GaussianBlur(gray, (5, 5), 0)  # Réduction du bruit
        _, binary = cv2.threshold(blurred, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)

        # Détection des contours pour isoler les blocs de texte
        contours, _ = cv2.findContours(binary, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        mask = np.zeros_like(binary)

        # Filtrer les contours par taille pour capturer les zones de texte pertinentes
        for cnt in contours:
            x, y, w, h = cv2.boundingRect(cnt)
            if 50 < w < 1000 and 10 < h < 200:  # Seuils à ajuster selon les dimensions du texte
                mask[y:y + h, x:x + w] = 255

        filtered = cv2.bitwise_and(gray, gray, mask=mask)

        # Étape 2 : OCR
        custom_config = r'--oem 3 --psm 6'
        text = pytesseract.image_to_string(filtered, lang="fra", config=custom_config)

        # Sauvegarde pour debug
        with open("extracted_text_debug.txt", "w", encoding="utf-8") as f:
            f.write(text)
        print("Texte extrait par OCR :", text)  # Debug

        # Étape 3 : Extraction des données avec regex
        cin_pattern = re.search(r'[A-Z]{2}\d{6}', text)  # Format CIN
        date_pattern = re.search(r'\d{2}.\d{2}.\d{4}', text)  # Format des dates
        nom_prenom_pattern = re.findall(r'\b[A-Z]{2,}\b', text)  # Noms et prénoms en majuscules

        # Structuration des résultats
        results = {
            "nom": nom_prenom_pattern[1] if len(nom_prenom_pattern) > 1 else "",
            "prenom": nom_prenom_pattern[0] if len(nom_prenom_pattern) > 0 else "",
            "date_naissance": date_pattern.group(0) if date_pattern else "",
            "numero_cin": cin_pattern.group(0) if cin_pattern else "",
            "adresse": " ".join(text.split("à")[1:]).split("\n")[0] if "à" in text else ""
        }

        print("Données extraites :", results)  # Debug

        return jsonify(results)

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, port=5000)
