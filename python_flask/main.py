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

        for cnt in contours:
            x, y, w, h = cv2.boundingRect(cnt)
            if 50 < w < 1000 and 10 < h < 200:  # Ajuster selon les dimensions du texte
                mask[y:y + h, x:x + w] = 255

        filtered = cv2.bitwise_and(gray, gray, mask=mask)

        # Étape 2 : OCR avec suppression du bruit
        custom_config = r'--oem 3 --psm 6'
        raw_text = pytesseract.image_to_string(filtered, lang="fra", config=custom_config)

        # Liste de mots-clés à ignorer
        keywords_to_ignore = ["ROYAUME", "MAROC", "CARTE", "NATIONALE", "IDENTITE", "DE", "BEI", "PE"]

        # Nettoyer le texte pour exclure le bruit
        cleaned_text = "\n".join([line for line in raw_text.split("\n") if not any(keyword in line for keyword in keywords_to_ignore)])

        # Sauvegarde pour debug
        with open("cleaned_text_debug.txt", "w", encoding="utf-8") as f:
            f.write(cleaned_text)
        print("Texte nettoyé :", cleaned_text)  # Debug

        # Étape 3 : Extraction des données avec regex
        cin_pattern = re.search(r'[A-Z]{2}\d{6}', cleaned_text)  # Format CIN
        date_pattern = re.search(r'\d{2}[./-]\d{2}[./-]\d{4}', cleaned_text)  # Format des dates
        nom_prenom_pattern = [word for word in re.findall(r'\b[A-Z]+\b', cleaned_text) if word not in keywords_to_ignore]

        # Structuration des résultats
        results = {
            "nom": nom_prenom_pattern[1] if len(nom_prenom_pattern) > 1 else "",
            "prenom": nom_prenom_pattern[0] if len(nom_prenom_pattern) > 0 else "",
            "date_naissance": date_pattern.group(0) if date_pattern else "",
            "numero_cin": cin_pattern.group(0) if cin_pattern else "",
            "adresse": " ".join(cleaned_text.split("à")[1:]).split("\n")[0] if "à" in cleaned_text else ""
        }

        print("Données extraites :", results)  # Debug

        return jsonify(results)

    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(debug=True, port=5000)