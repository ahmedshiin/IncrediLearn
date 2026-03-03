package com.incredilearn.incredilearn.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.incredilearn.incredilearn.dto.CreateQuizRequest;
import com.incredilearn.incredilearn.dto.QuizDTO;
import com.incredilearn.incredilearn.dto.QuizOptionDTO;
import com.incredilearn.incredilearn.dto.QuizQuestionDTO;
import com.incredilearn.incredilearn.model.Course;
import com.incredilearn.incredilearn.model.Quiz;
import com.incredilearn.incredilearn.model.QuizAttempt;
import com.incredilearn.incredilearn.model.QuizAttemptAnswer;
import com.incredilearn.incredilearn.model.QuizOption;
import com.incredilearn.incredilearn.model.QuizQuestion;
import com.incredilearn.incredilearn.model.User;
import com.incredilearn.incredilearn.repository.CourseRepository;
import com.incredilearn.incredilearn.repository.QuizAttemptRepository;
import com.incredilearn.incredilearn.repository.QuizRepository;
import com.incredilearn.incredilearn.repository.UserRepository;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;

    public QuizService(QuizRepository quizRepository, CourseRepository courseRepository,
                       QuizAttemptRepository quizAttemptRepository, UserRepository userRepository) {
        this.quizRepository = quizRepository;
        this.courseRepository = courseRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.userRepository = userRepository;
    }

    /** Liste des quiz d'un cours (sans les réponses correctes pour les étudiants) */
    public List<QuizDTO> getQuizzesByCourse(Long courseId) {
        return quizRepository.findByCourseId(courseId).stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    /** Détails d'un quiz SANS les réponses correctes (pour l'étudiant qui passe le quiz) */
    public QuizDTO getQuizForStudent(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) return null;
        return convertToDTO(quiz, false);
    }

    /** Détails d'un quiz AVEC les réponses correctes (pour l'enseignant) */
    public QuizDTO getQuizForTeacher(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) return null;
        return convertToDTO(quiz, true);
    }

    /** Vérifier si un étudiant a déjà répondu à un quiz */
    public boolean hasAttempted(Long quizId, Long userId) {
        return quizAttemptRepository.existsByQuizIdAndUserId(quizId, userId);
    }

    /** Récupérer la tentative d'un étudiant pour un quiz */
    public AttemptDTO getUserAttempt(Long quizId, Long userId) {
        return quizAttemptRepository.findByQuizIdAndUserId(quizId, userId)
                .map(this::convertAttemptToDTO)
                .orElse(null);
    }

    /** Récupérer toutes les tentatives d'un quiz (pour l'enseignant) */
    public List<AttemptDTO> getAttemptsByQuiz(Long quizId) {
        return quizAttemptRepository.findByQuizId(quizId).stream()
                .map(this::convertAttemptToDTO)
                .collect(Collectors.toList());
    }

    /** Vérifier les réponses d'un étudiant et retourner le score */
    public QuizResultDTO checkAnswers(Long quizId, Long userId, List<AnswerDTO> answers) {
        // Vérifier si l'étudiant a déjà répondu
        if (quizAttemptRepository.existsByQuizIdAndUserId(quizId, userId)) {
            return null; // déjà répondu
        }

        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) return null;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        int total = quiz.getQuestions().size();
        int correct = 0;

        List<QuestionResultDTO> results = new java.util.ArrayList<>();

        for (QuizQuestion question : quiz.getQuestions()) {
            Long selectedOptionId = answers.stream()
                    .filter(a -> a.questionId.equals(question.getId()))
                    .map(a -> a.selectedOptionId)
                    .findFirst().orElse(null);

            boolean isCorrect = false;
            Long correctOptionId = null;

            for (QuizOption opt : question.getOptions()) {
                if (opt.isCorrect()) correctOptionId = opt.getId();
                if (opt.isCorrect() && opt.getId().equals(selectedOptionId)) {
                    isCorrect = true;
                }
            }

            if (isCorrect) correct++;

            QuestionResultDTO qr = new QuestionResultDTO();
            qr.questionId = question.getId();
            qr.correct = isCorrect;
            qr.correctOptionId = correctOptionId;
            qr.selectedOptionId = selectedOptionId;
            results.add(qr);
        }

        QuizResultDTO result = new QuizResultDTO();
        result.total = total;
        result.correct = correct;
        result.score = total > 0 ? Math.round((correct * 100.0) / total) : 0;
        result.results = results;

        // Sauvegarder la tentative avec les réponses
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setTotalQuestions(total);
        attempt.setCorrectAnswers(correct);
        attempt.setScore(result.score);

        for (AnswerDTO a : answers) {
            QuizAttemptAnswer answer = new QuizAttemptAnswer();
            answer.setAttempt(attempt);
            answer.setQuestionId(a.questionId);
            answer.setSelectedOptionId(a.selectedOptionId);
            attempt.getAnswers().add(answer);
        }

        quizAttemptRepository.save(attempt);

        return result;
    }

    /** Créer un quiz */
    public QuizDTO createQuiz(CreateQuizRequest request) {
        Course course = courseRepository.findById(request.getCourseId()).orElse(null);
        if (course == null) return null;

        Quiz quiz = new Quiz();
        quiz.setTitre(request.getTitre());
        quiz.setDescription(request.getDescription());
        quiz.setCourse(course);

        if (request.getQuestions() != null) {
            int order = 0;
            for (CreateQuizRequest.QuestionRequest qr : request.getQuestions()) {
                QuizQuestion question = new QuizQuestion();
                question.setTexte(qr.getTexte());
                question.setSortOrder(order++);
                question.setQuiz(quiz);

                if (qr.getOptions() != null) {
                    for (CreateQuizRequest.OptionRequest or : qr.getOptions()) {
                        QuizOption option = new QuizOption();
                        option.setTexte(or.getTexte());
                        option.setCorrect(or.isCorrect());
                        option.setQuestion(question);
                        question.getOptions().add(option);
                    }
                }

                quiz.getQuestions().add(question);
            }
        }

        quiz = quizRepository.save(quiz);
        return convertToDTO(quiz, true);
    }

    /** Modifier un quiz existant */
    @Transactional
    public QuizDTO updateQuiz(Long quizId, CreateQuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) return null;

        quiz.setTitre(request.getTitre());
        quiz.setDescription(request.getDescription());

        // Supprimer les anciennes questions
        quiz.getQuestions().clear();
        quizRepository.saveAndFlush(quiz);

        // Ajouter les nouvelles
        if (request.getQuestions() != null) {
            int order = 0;
            for (CreateQuizRequest.QuestionRequest qr : request.getQuestions()) {
                QuizQuestion question = new QuizQuestion();
                question.setTexte(qr.getTexte());
                question.setSortOrder(order++);
                question.setQuiz(quiz);

                if (qr.getOptions() != null) {
                    for (CreateQuizRequest.OptionRequest or : qr.getOptions()) {
                        QuizOption option = new QuizOption();
                        option.setTexte(or.getTexte());
                        option.setCorrect(or.isCorrect());
                        option.setQuestion(question);
                        question.getOptions().add(option);
                    }
                }
                quiz.getQuestions().add(question);
            }
        }

        quiz = quizRepository.save(quiz);
        return convertToDTO(quiz, true);
    }

    /** Revoir un quiz déjà répondu (quiz + réponses de l'étudiant + corrections) */
    @Transactional(readOnly = true)
    public QuizReviewDTO getQuizReview(Long quizId, Long userId) {
        QuizAttempt attempt = quizAttemptRepository.findByQuizIdAndUserId(quizId, userId).orElse(null);
        if (attempt == null) return null;

        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) return null;

        QuizReviewDTO review = new QuizReviewDTO();
        review.quiz = convertToDTO(quiz, true);
        review.attempt = convertAttemptToDTO(attempt);
        review.answers = new java.util.ArrayList<>();

        List<QuizAttemptAnswer> savedAnswers = attempt.getAnswers();

        if (savedAnswers != null && !savedAnswers.isEmpty()) {
            // Reconstituer à partir des réponses sauvegardées
            for (QuizAttemptAnswer ans : savedAnswers) {
                QuestionResultDTO qr = new QuestionResultDTO();
                qr.questionId = ans.getQuestionId();
                qr.selectedOptionId = ans.getSelectedOptionId();

                for (QuizQuestion q : quiz.getQuestions()) {
                    if (q.getId().equals(ans.getQuestionId())) {
                        for (QuizOption opt : q.getOptions()) {
                            if (opt.isCorrect()) qr.correctOptionId = opt.getId();
                        }
                        qr.correct = qr.correctOptionId != null && qr.correctOptionId.equals(ans.getSelectedOptionId());
                        break;
                    }
                }
                review.answers.add(qr);
            }
        } else {
            // Tentative ancienne sans réponses sauvegardées : montrer uniquement les bonnes réponses
            for (QuizQuestion q : quiz.getQuestions()) {
                QuestionResultDTO qr = new QuestionResultDTO();
                qr.questionId = q.getId();
                qr.selectedOptionId = null;
                qr.correct = false;
                for (QuizOption opt : q.getOptions()) {
                    if (opt.isCorrect()) qr.correctOptionId = opt.getId();
                }
                review.answers.add(qr);
            }
        }

        return review;
    }

    /** Supprimer un quiz */
    @Transactional
    public boolean deleteQuiz(Long quizId) {
        if (!quizRepository.existsById(quizId)) return false;
        quizAttemptRepository.deleteByQuizId(quizId);
        quizRepository.deleteById(quizId);
        return true;
    }

    // ── Conversion helpers ──

    private QuizDTO convertToListDTO(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitre(quiz.getTitre());
        dto.setDescription(quiz.getDescription());
        dto.setCourseId(quiz.getCourse().getId());
        dto.setNombreQuestions(quiz.getQuestions().size());
        dto.setCreatedAt(quiz.getCreatedAt() != null ? quiz.getCreatedAt().toString() : null);
        return dto;
    }

    private QuizDTO convertToDTO(Quiz quiz, boolean includeCorrect) {
        QuizDTO dto = convertToListDTO(quiz);
        dto.setQuestions(quiz.getQuestions().stream().map(q -> {
            QuizQuestionDTO qdto = new QuizQuestionDTO();
            qdto.setId(q.getId());
            qdto.setTexte(q.getTexte());
            qdto.setSortOrder(q.getSortOrder());
            qdto.setOptions(q.getOptions().stream().map(o -> {
                QuizOptionDTO odto = new QuizOptionDTO();
                odto.setId(o.getId());
                odto.setTexte(o.getTexte());
                if (includeCorrect) odto.setCorrect(o.isCorrect());
                return odto;
            }).collect(Collectors.toList()));
            return qdto;
        }).collect(Collectors.toList()));
        return dto;
    }

    // ── Inner DTOs for answers/results ──

    public static class AnswerDTO {
        public Long questionId;
        public Long selectedOptionId;
    }

    public static class QuestionResultDTO {
        public Long questionId;
        public boolean correct;
        public Long correctOptionId;
        public Long selectedOptionId;
    }

    public static class QuizResultDTO {
        public int total;
        public int correct;
        public double score;
        public List<QuestionResultDTO> results;
    }

    public static class AttemptDTO {
        public Long id;
        public Long quizId;
        public String quizTitre;
        public Long userId;
        public String userName;
        public int totalQuestions;
        public int correctAnswers;
        public double score;
        public String completedAt;
    }

    public static class QuizReviewDTO {
        public QuizDTO quiz;
        public AttemptDTO attempt;
        public List<QuestionResultDTO> answers;
    }

    private AttemptDTO convertAttemptToDTO(QuizAttempt attempt) {
        AttemptDTO dto = new AttemptDTO();
        dto.id = attempt.getId();
        dto.quizId = attempt.getQuiz().getId();
        dto.quizTitre = attempt.getQuiz().getTitre();
        dto.userId = attempt.getUser().getId();
        dto.userName = attempt.getUser().getNom();
        dto.totalQuestions = attempt.getTotalQuestions();
        dto.correctAnswers = attempt.getCorrectAnswers();
        dto.score = attempt.getScore();
        dto.completedAt = attempt.getCompletedAt() != null ? attempt.getCompletedAt().toString() : null;
        return dto;
    }
}
