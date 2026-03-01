package com.incredilearn.incredilearn;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.incredilearn.incredilearn.model.Course;
import com.incredilearn.incredilearn.model.Role;
import com.incredilearn.incredilearn.model.User;
import com.incredilearn.incredilearn.repository.CourseRepository;
import com.incredilearn.incredilearn.repository.UserRepository;

@Configuration
public class DataInitializer {
    
    @Bean
    CommandLineRunner initDatabase(
        UserRepository userRepository, 
        CourseRepository courseRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Vérifier si des données existent déjà
            if (courseRepository.count() > 0) {
                return; // Les données sont déjà initialisées
            }
            
            // Créer un enseignant
            User enseignant = new User();
            enseignant.setNom("Dr. Marie Dupont");
            enseignant.setEmail("marie.dupont@incredilearn.com");
            enseignant.setPassword(passwordEncoder.encode("teacher123"));
            enseignant.setRole(Role.ENSEIGNANT);
            enseignant.setDomain("informatique");
            enseignant.setVerified(true);
            enseignant = userRepository.save(enseignant);
            
            // Créer quelques cours
            Course cours1 = new Course();
            cours1.setTitre("Introduction à JavaScript");
            cours1.setDescription("Apprenez les bases de JavaScript, le langage de programmation le plus populaire pour le développement web.");
            cours1.setDomain("Informatique & Programmation");
            cours1.setNiveau("Débutant");
            cours1.setDureeHeures(20);
            cours1.setEnseignant(enseignant);
            courseRepository.save(cours1);
            
            Course cours2 = new Course();
            cours2.setTitre("Vue.js Avancé");
            cours2.setDescription("Maîtrisez Vue.js 3 avec Composition API, Pinia et les meilleures pratiques.");
            cours2.setDomain("Informatique & Programmation");
            cours2.setNiveau("Avancé");
            cours2.setDureeHeures(30);
            cours2.setEnseignant(enseignant);
            courseRepository.save(cours2);
            
            Course cours3 = new Course();
            cours3.setTitre("Spring Boot et Microservices");
            cours3.setDescription("Créez des applications d'entreprise robustes avec Spring Boot et l'architecture microservices.");
            cours3.setDomain("Informatique & Programmation");
            cours3.setNiveau("Intermédiaire");
            cours3.setDureeHeures(40);
            cours3.setEnseignant(enseignant);
            courseRepository.save(cours3);
            
            Course cours4 = new Course();
            cours4.setTitre("Design Patterns en Java");
            cours4.setDescription("Découvrez les modèles de conception essentiels pour écrire du code maintenable et évolutif.");
            cours4.setDomain("Informatique & Programmation");
            cours4.setNiveau("Intermédiaire");
            cours4.setDureeHeures(25);
            cours4.setEnseignant(enseignant);
            courseRepository.save(cours4);
            
            System.out.println("✓ Base de données initialisée avec des données de démonstration");
        };
    }
}
