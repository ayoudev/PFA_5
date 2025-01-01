package com.pfa.backend.security.Auth;



import com.pfa.backend.security.config.JwtService;
import com.pfa.backend.security.entity.Role;
import com.pfa.backend.security.entity.User;
import com.pfa.backend.security.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // Constructeur avec tous les paramètres nécessaires
    public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }
    // Méthode pour récupérer tous les utilisateurs
    public List<User> findAll() {
        return repository.findAll();
    }

    // Méthode pour supprimer un utilisateur par ID
    public void deleteById(Integer id) {
        if (!repository.existsById(id)) {
            throw new UsernameNotFoundException("Utilisateur avec l'ID " + id + " non trouvé");
        }
        repository.deleteById(id);
    }

    // Autres méthodes existantes
    public AuthenticationResponse register(RegisterRequest request) {
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("L'email est déjà utilisé, Veuillez choisir une autre adresse mail!");
        }
        User user = new User(
                request.getFirstname(),
                request.getLastname(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER
        );
        repository.save(user);
        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken, user.getRole().name());
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Authentification de l'utilisateur
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Récupération de l'utilisateur depuis la base de données
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Génération du token JWT
        var jwtToken = jwtService.generateToken(user);

        // Création de l'objet AuthenticationResponse
        AuthenticationResponse response = new AuthenticationResponse();
        response.setToken(jwtToken);
        response.setRole(user.getRole().name());

        return response;
    }


    public void registerAdmin() {
        if (repository.findByEmail("admin@gmail.com").isEmpty()) {
            // Création manuelle de l'utilisateur admin
            User admin = new User();
            admin.setFirstname("admin");
            admin.setLastname("admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(Role.ADMIN);

            // Sauvegarde de l'administrateur dans le repository
            repository.save(admin);
        }
    }


    public User findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
    }
}
