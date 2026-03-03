package com.incredilearn.incredilearn.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.incredilearn.incredilearn.dto.AuthResponse;
import com.incredilearn.incredilearn.dto.LoginRequest;
import com.incredilearn.incredilearn.dto.RegisterRequest;
import com.incredilearn.incredilearn.model.Role;
import com.incredilearn.incredilearn.model.User;
import com.incredilearn.incredilearn.repository.UserRepository;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            AuthResponse response = new AuthResponse();
            response.setMessage("Un compte avec cet email existe déjà");
            return response;
        }
        
        // Créer un nouvel utilisateur
        User user = new User();
        user.setNom(request.getNom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Définir le rôle
        boolean isTeacher = "enseignant".equalsIgnoreCase(request.getRole());
        if (isTeacher) {
            user.setRole(Role.ENSEIGNANT);
            user.setDomain(request.getDomain());
            user.setVerified(false); // Les enseignants doivent être vérifiés par l'admin
        } else {
            user.setRole(Role.ETUDIANT);
            user.setVerified(true); // Les étudiants sont automatiquement vérifiés
        }
        
        user = userRepository.save(user);
        
        // Créer la réponse
        AuthResponse response = new AuthResponse();
        response.setNom(user.getNom());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name().toLowerCase());
        response.setDomain(user.getDomain());
        response.setVerified(user.isVerified());

        if (isTeacher) {
            // Inclure l'id pour l'upload de documents, mais pending=true empêche la connexion
            response.setId(user.getId());
            response.setPending(true);
            response.setMessage("Votre demande a été envoyée. Un administrateur validera votre compte prochainement.");
        } else {
            response.setId(user.getId());
            response.setMessage("Inscription réussie");
        }
        
        return response;
    }
    
    public AuthResponse login(LoginRequest request) {
        // Rechercher l'utilisateur par email
        User user = userRepository.findByEmail(request.getEmail())
            .orElse(null);
        
        if (user == null) {
            AuthResponse response = new AuthResponse();
            response.setMessage("Email ou mot de passe incorrect");
            return response;
        }
        
        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            AuthResponse response = new AuthResponse();
            response.setMessage("Email ou mot de passe incorrect");
            return response;
        }

        // Bloquer les utilisateurs bloqués par l'admin
        if (user.isBlocked()) {
            AuthResponse response = new AuthResponse();
            response.setBlocked(true);
            response.setMessage("Votre compte a été bloqué pour mauvais comportement. Contactez l'administrateur.");
            return response;
        }

        // Bloquer les enseignants non vérifiés
        if (user.getRole() == Role.ENSEIGNANT && !user.isVerified()) {
            AuthResponse response = new AuthResponse();
            response.setPending(true);
            response.setMessage("Votre compte est en attente de validation par un administrateur.");
            return response;
        }
        
        // Créer la réponse
        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setNom(user.getNom());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name().toLowerCase());
        response.setDomain(user.getDomain());
        response.setVerified(user.isVerified());
        response.setMessage("Connexion réussie");
        
        return response;
    }
}
