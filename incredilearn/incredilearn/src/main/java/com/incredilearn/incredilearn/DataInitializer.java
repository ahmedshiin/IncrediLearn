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
            // Créer l'admin si inexistant (toujours vérifier)
            if (!userRepository.existsByEmail("admin@incredilearn.com")) {
                User admin = new User();
                admin.setNom("Admin User");
                admin.setEmail("admin@incredilearn.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setVerified(true);
                userRepository.save(admin);
                System.out.println("✓ Compte admin créé : admin@incredilearn.com / admin123");
            }

            // Reseed si moins de 24 cours
            if (courseRepository.count() >= 24) return;
            courseRepository.deleteAll();

            // ── Enseignants ──────────────────────────────────────────
            User marie = getOrCreateUser(userRepository, passwordEncoder,
                "Dr. Marie Dupont", "marie.dupont@incredilearn.com", "teacher123", "Informatique");
            User sophie = getOrCreateUser(userRepository, passwordEncoder,
                "Sophie Martin", "sophie.martin@incredilearn.com", "teacher123", "Design");
            User thomas = getOrCreateUser(userRepository, passwordEncoder,
                "Thomas Bernard", "thomas.bernard@incredilearn.com", "teacher123", "Marketing");
            User marc = getOrCreateUser(userRepository, passwordEncoder,
                "Marc Dubois", "marc.dubois@incredilearn.com", "teacher123", "Bureautique");
            User jean = getOrCreateUser(userRepository, passwordEncoder,
                "Dr. Jean Leclerc", "jean.leclerc@incredilearn.com", "teacher123", "Intelligence Artificielle");
            User claire = getOrCreateUser(userRepository, passwordEncoder,
                "Claire Rousseau", "claire.rousseau@incredilearn.com", "teacher123", "Développement Web");
            User emilie = getOrCreateUser(userRepository, passwordEncoder,
                "Émilie Laurent", "emilie.laurent@incredilearn.com", "teacher123", "Photographie");
            User antoine = getOrCreateUser(userRepository, passwordEncoder,
                "Dr. Antoine Moreau", "antoine.moreau@incredilearn.com", "teacher123", "Cybersécurité");
            User lucas = getOrCreateUser(userRepository, passwordEncoder,
                "Lucas Petit", "lucas.petit@incredilearn.com", "teacher123", "Développement Web");
            User camille = getOrCreateUser(userRepository, passwordEncoder,
                "Camille Fontaine", "camille.fontaine@incredilearn.com", "teacher123", "Business");

            // ── Cours ─────────────────────────────────────────────────
            saveCourse(courseRepository, "Introduction à la programmation Python",
                "Apprenez les bases de Python, le langage de programmation le plus demandé.",
                "Informatique & Programmation", "Débutant", 12, marie);

            saveCourse(courseRepository, "Design UI/UX Moderne",
                "Maîtrisez les principes du design d'interface utilisateur moderne.",
                "Design", "Intermédiaire", 8, sophie);

            saveCourse(courseRepository, "Marketing Digital Avancé",
                "Stratégies avancées pour le marketing en ligne et les réseaux sociaux.",
                "Marketing", "Avancé", 15, thomas);

            saveCourse(courseRepository, "Excel pour Professionnels",
                "Maîtrisez Excel avec les formules avancées et les tableaux croisés dynamiques.",
                "Bureautique", "Intermédiaire", 6, marc);

            saveCourse(courseRepository, "Intelligence Artificielle avec TensorFlow",
                "Créez des modèles de machine learning avec TensorFlow et Keras.",
                "Intelligence Artificielle", "Avancé", 20, jean);

            saveCourse(courseRepository, "Développement Web React",
                "Construisez des applications web modernes avec React et TypeScript.",
                "Informatique & Programmation", "Intermédiaire", 18, claire);

            saveCourse(courseRepository, "Photographie Créative",
                "Techniques de composition et d'éclairage pour des photos exceptionnelles.",
                "Photographie", "Débutant", 10, emilie);

            saveCourse(courseRepository, "Cybersécurité Fondamentale",
                "Protégez vos systèmes contre les menaces cybernétiques modernes.",
                "Cybersécurité", "Intermédiaire", 14, antoine);

            // ── 16 cours supplémentaires ────────────────────────────
            saveCourse(courseRepository, "HTML & CSS pour Débutants",
                "Créez vos premières pages web avec HTML5 et CSS3 modernes.",
                "Informatique & Programmation", "Débutant", 8, claire);
            saveCourse(courseRepository, "JavaScript Avancé et ES2024",
                "Closures, async/await, modules et les dernières fonctionnalités ES2024.",
                "Informatique & Programmation", "Avancé", 16, marie);
            saveCourse(courseRepository, "Node.js et APIs REST",
                "Développez des APIs RESTful robustes avec Node.js et Express.",
                "Informatique & Programmation", "Intermédiaire", 20, lucas);
            saveCourse(courseRepository, "Vue.js 3 et Composition API",
                "Maîtrisez Vue.js 3 avec Composition API, Pinia et les meilleures pratiques.",
                "Informatique & Programmation", "Avancé", 22, marie);
            saveCourse(courseRepository, "Spring Boot et Microservices",
                "Créez des applications d'entreprise robustes avec Spring Boot.",
                "Informatique & Programmation", "Avancé", 30, marie);
            saveCourse(courseRepository, "Git, GitHub et DevOps",
                "Maîtrisez le versioning, les branches, pull requests et les pipelines CI/CD.",
                "Informatique & Programmation", "Débutant", 6, antoine);
            saveCourse(courseRepository, "Docker et Kubernetes",
                "Conteneurisez et orchestrez vos applications en production.",
                "Informatique & Programmation", "Avancé", 18, antoine);
            saveCourse(courseRepository, "SQL et Bases de Données Relationnelles",
                "De SELECT à l'optimisation de requêtes complexes avec MySQL et PostgreSQL.",
                "Informatique & Programmation", "Débutant", 12, marc);
            saveCourse(courseRepository, "SEO et Référencement Naturel",
                "Positionnez votre site en première page de Google avec des techniques SEO éprouvées.",
                "Marketing", "Intermédiaire", 10, thomas);
            saveCourse(courseRepository, "Gestion de Projet Agile et Scrum",
                "Pilotez vos projets avec la méthode Agile, sprints et cérémonies Scrum.",
                "Business", "Intermédiaire", 12, camille);
            saveCourse(courseRepository, "Montage Vidéo avec Premiere Pro",
                "Créez des vidéos professionnelles avec Adobe Premiere Pro.",
                "Design", "Intermédiaire", 14, emilie);
            saveCourse(courseRepository, "Photoshop pour Designers",
                "Retouche photo, compositions et effets avec Adobe Photoshop.",
                "Design", "Intermédiaire", 10, sophie);
            saveCourse(courseRepository, "Figma et Design System",
                "Créez des composants réutilisables et des design systems scalables avec Figma.",
                "Design", "Avancé", 12, sophie);
            saveCourse(courseRepository, "Machine Learning avec Scikit-learn",
                "Algorithmes de classification, régression et clustering avec Python.",
                "Intelligence Artificielle", "Avancé", 24, jean);
            saveCourse(courseRepository, "Développement Mobile React Native",
                "Créez des applications iOS et Android avec React Native.",
                "Informatique & Programmation", "Avancé", 25, lucas);
            saveCourse(courseRepository, "Finance Personnelle et Investissement",
                "Gérez votre budget, épargnez et investissez intelligemment.",
                "Business", "Débutant", 8, camille);

            System.out.println("✓ Base de données initialisée avec 24 cours de démonstration");
        };
    }
    private User getOrCreateUser(UserRepository repo, PasswordEncoder enc,
            String nom, String email, String pass, String domain) {
        return repo.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setNom(nom); u.setEmail(email);
            u.setPassword(enc.encode(pass));
            u.setRole(Role.ENSEIGNANT);
            u.setDomain(domain);
            u.setVerified(true);
            return repo.save(u);
        });
    }

    private void saveCourse(CourseRepository repo, String titre, String desc,
            String domain, String niveau, int duree, User enseignant) {
        Course c = new Course();
        c.setTitre(titre); c.setDescription(desc);
        c.setDomain(domain); c.setNiveau(niveau);
        c.setDureeHeures(duree); c.setEnseignant(enseignant);
        repo.save(c);
    }}
