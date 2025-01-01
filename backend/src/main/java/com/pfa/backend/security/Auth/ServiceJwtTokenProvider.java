package com.pfa.backend.security.Auth;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

@Service
public class ServiceJwtTokenProvider {

    // Clé secrète pour signer les JWT (remplacez-la par une clé plus sécurisée dans une vraie application)
    private final String secretKey = "KiaIAUPHVeKqNwJmDrZ28pIukFzoabAvOcrVJi6flKmM+CoPFLLWRvFzWxXB4ulX";

    // Validation du token JWT
    public boolean validateToken(String token) {
        try {
            // Validation du token en utilisant la clé secrète
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expiré : " + e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Validation du token échouée : " + e.getMessage());
            return false;
        }
    }
}