package com.pfa.backend.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Service
public class OCRService {

    public Map<String, String> processImage(MultipartFile file) {
        try {
            String pythonApiUrl = "http://localhost:5000/process_image";  // URL de votre API Python
            RestTemplate restTemplate = new RestTemplate();

            // Créer une requête multipart pour envoyer l'image
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Envoyer la requête à l'API Python
            ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);

            System.out.println("Réponse API Python : " + response.getBody());  // Debugging
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Failed to process image: " + e.getMessage());
        }
    }
}

