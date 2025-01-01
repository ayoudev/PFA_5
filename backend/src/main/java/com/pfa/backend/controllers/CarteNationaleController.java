package com.pfa.backend.controllers;

import com.pfa.backend.entities.CarteNationale;
import com.pfa.backend.repositories.CarteNationaleRepository;
import com.pfa.backend.services.OCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:4200", "http://192.168.1.2:8080"}, allowCredentials = "true")
@RestController
@RequestMapping("/api")
public class CarteNationaleController {

    @Autowired
    private OCRService ocrService;

    @Autowired
    private CarteNationaleRepository repository;
    @PostMapping("/process")
    public CarteNationale processImage(@RequestParam("file") MultipartFile file) {
        // Appeler le service pour extraire les données
        Map<String, String> data = ocrService.processImage(file);

        // Créer une entité CarteNationale à partir des données extraites
        CarteNationale carte = new CarteNationale();
        carte.setNom(data.getOrDefault("nom", null));
        carte.setPrenom(data.getOrDefault("prenom", null));
        carte.setNumeroCIN(data.getOrDefault("numero_cin", null));
        carte.setAdresse(data.getOrDefault("adresse", null));
        carte.setDateNaissance(data.getOrDefault("date_naissance", null));
        // Sauvegarder dans la base de données
        return repository.save(carte);
    }



    @GetMapping("/all")
    public List<CarteNationale> getAllCarteNationale() {
        return repository.findAll();
    }
}
