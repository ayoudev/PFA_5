package com.pfa.backend.security.Auth;

import com.pfa.backend.configuration_2fa.EmailService;
import com.pfa.backend.configuration_2fa.OtpGenerator;
import com.pfa.backend.security.config.JwtService;
import com.pfa.backend.security.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService service;
    private final JwtService jwtService;

    @Autowired
    private EmailService emailService;
    private Map<String, String> otpStore = new HashMap<>();

    @Autowired
    private ServiceJwtTokenProvider jwtTokenProvider;

    // Constructeur explicite
    public AuthenticationController(AuthenticationService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String email) {
        String otp = OtpGenerator.generateOtp(6);
        long timestamp = System.currentTimeMillis(); // Temps actuel en millisecondes
        otpStore.put(email, otp + ":" + timestamp); // Stocker OTP avec un horodatage
        emailService.sendOtpEmail(email, "Votre OTP", otp);
        return "OTP envoyé à l'adresse e-mail " + email;
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp) {
        if (otpStore.containsKey(email)) {
            String[] otpData = otpStore.get(email).split(":");
            String storedOtp = otpData[0];
            long timestamp = Long.parseLong(otpData[1]);

            // Vérifier si l'OTP a expiré (30 secondes)
            if (System.currentTimeMillis() - timestamp > 30 * 1000) {
                otpStore.remove(email);
                return "OTP expiré.";
            }

            // Vérifier si l'OTP est correct
            if (storedOtp.equals(otp)) {
                otpStore.remove(email);
                return "OTP validé avec succès.";
            } else {
                return "OTP invalide.";
            }
        } else {
            return "Aucun OTP trouvé pour cet e-mail.";
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            boolean isValid = jwtTokenProvider.validateToken(token);
            return isValid ? ResponseEntity.ok("Valid token") :
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token validation failed");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthenticationResponse response = service.register(request);
            return ResponseEntity.ok(response);
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            AuthenticationResponse response = service.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Accès refusé: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String email = jwtService.extractUsername(token);
            User user = service.findByEmail(email);

            if (user != null) {
                return ResponseEntity.ok(Map.of(
                        "firstname", user.getFirstname(),
                        "lastname", user.getLastname(),
                        "email", user.getEmail(),
                        "role", user.getRole().name()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide ou expiré");
        }
    }

    //  Afficher tous les utilisateurs
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = service.findAll();
        return ResponseEntity.ok(users);
    }

    //  Supprimer un utilisateur
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable Integer id) {
        try {
            service.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Utilisateur non trouvé ou suppression échouée"));
        }
    }
}
