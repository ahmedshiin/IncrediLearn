package com.incredilearn.incredilearn.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.incredilearn.incredilearn.dto.CreateQuizRequest;
import com.incredilearn.incredilearn.dto.QuizDTO;
import com.incredilearn.incredilearn.service.QuizService;

@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /** Liste des quiz d'un cours */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<QuizDTO>> getQuizzesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(quizService.getQuizzesByCourse(courseId));
    }

    /** Détail d'un quiz pour un étudiant (sans les réponses correctes) */
    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDTO> getQuiz(@PathVariable Long quizId) {
        QuizDTO dto = quizService.getQuizForStudent(quizId);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    /** Détail d'un quiz pour l'enseignant (avec les réponses correctes) */
    @GetMapping("/{quizId}/teacher")
    public ResponseEntity<QuizDTO> getQuizForTeacher(@PathVariable Long quizId) {
        QuizDTO dto = quizService.getQuizForTeacher(quizId);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    /** Soumettre les réponses d'un quiz */
    @PostMapping("/{quizId}/submit/{userId}")
    public ResponseEntity<QuizService.QuizResultDTO> submitQuiz(
            @PathVariable Long quizId,
            @PathVariable Long userId,
            @RequestBody List<QuizService.AnswerDTO> answers) {
        // Vérifier si l'étudiant a déjà répondu
        if (quizService.hasAttempted(quizId, userId)) {
            return ResponseEntity.status(409).build(); // Conflict
        }
        QuizService.QuizResultDTO result = quizService.checkAnswers(quizId, userId, answers);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    /** Vérifier si un étudiant a déjà répondu à un quiz */
    @GetMapping("/{quizId}/attempt/{userId}")
    public ResponseEntity<QuizService.AttemptDTO> getUserAttempt(
            @PathVariable Long quizId, @PathVariable Long userId) {
        QuizService.AttemptDTO attempt = quizService.getUserAttempt(quizId, userId);
        if (attempt == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(attempt);
    }

    /** Récupérer toutes les tentatives d'un quiz (pour l'enseignant) */
    @GetMapping("/{quizId}/attempts")
    public ResponseEntity<List<QuizService.AttemptDTO>> getAttemptsByQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getAttemptsByQuiz(quizId));
    }

    /** Revoir un quiz déjà répondu (quiz + corrections + réponses étudiants) */
    @GetMapping("/{quizId}/review/{userId}")
    public ResponseEntity<QuizService.QuizReviewDTO> getQuizReview(
            @PathVariable Long quizId, @PathVariable Long userId) {
        QuizService.QuizReviewDTO review = quizService.getQuizReview(quizId, userId);
        if (review == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(review);
    }

    /** Créer un quiz */
    @PostMapping
    public ResponseEntity<QuizDTO> createQuiz(@RequestBody CreateQuizRequest request) {
        QuizDTO dto = quizService.createQuiz(request);
        if (dto == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(dto);
    }

    /** Modifier un quiz */
    @PutMapping("/{quizId}")
    public ResponseEntity<QuizDTO> updateQuiz(@PathVariable Long quizId, @RequestBody CreateQuizRequest request) {
        QuizDTO dto = quizService.updateQuiz(quizId, request);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    /** Supprimer un quiz */
    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        boolean deleted = quizService.deleteQuiz(quizId);
        if (!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().build();
    }
}
