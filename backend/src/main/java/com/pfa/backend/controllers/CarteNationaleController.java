package com.pfa.backend.controllers;

import com.pfa.backend.entities.CarteNationale;
import com.pfa.backend.repositories.CarteNationaleRepository;
import com.pfa.backend.services.OCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CarteNationaleController {

    @Autowired
    private OCRService ocrService;

    @Autowired
    private CarteNationaleRepository repository;

//    @PostMapping("/colab/process")
//    public CarteNationale processColabImage(@RequestBody Map<String, String> data) {
//        // Créer une entité CarteNationale à partir des données extraites
//        CarteNationale carte = new CarteNationale();
//        carte.setNom(data.get("nom"));
//        carte.setPrenom(data.get("prenom"));
//        carte.setNumeroCIN(data.get("numero_cin"));
//        carte.setAdresse(data.get("adresse"));
//        carte.setDateNaissance(data.get("date_naissance"));
//
//        // Sauvegarder dans la base de données
//        return repository.save(carte);
//    }

    @PostMapping("/process")
    public CarteNationale processImage(@RequestParam("file") MultipartFile file) {
        // Appeler le service pour extraire les données
        Map<String, String> data = ocrService.processImage(file);

        // Créer une entité CarteNationale à partir des données extraites
        CarteNationale carte = new CarteNationale();
        carte.setNom(data.get("nom"));
        carte.setPrenom(data.get("prenom"));
        carte.setNumeroCIN(data.get("numero_cin"));
        carte.setAdresse(data.get("adresse"));
        carte.setDateNaissance(data.get("date_naissance"));

        // Sauvegarder dans la base de données
        return repository.save(carte);
    }



    @GetMapping("/all")
    public List<CarteNationale> getAllCarteNationale() {
        return repository.findAll();
    }
}
