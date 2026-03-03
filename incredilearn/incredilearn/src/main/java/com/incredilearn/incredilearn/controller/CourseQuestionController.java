package com.incredilearn.incredilearn.controller;

import java.util.ArrayList;
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

import com.incredilearn.incredilearn.model.Course;
import com.incredilearn.incredilearn.model.CourseQuestion;
import com.incredilearn.incredilearn.model.CourseQuestionReply;
import com.incredilearn.incredilearn.model.User;
import com.incredilearn.incredilearn.repository.CourseQuestionReplyRepository;
import com.incredilearn.incredilearn.repository.CourseQuestionRepository;
import com.incredilearn.incredilearn.repository.CourseRepository;
import com.incredilearn.incredilearn.repository.UserRepository;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:*")
public class CourseQuestionController {

    private final CourseQuestionRepository questionRepository;
    private final CourseQuestionReplyRepository replyRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseQuestionController(CourseQuestionRepository questionRepository,
                                    CourseQuestionReplyRepository replyRepository,
                                    CourseRepository courseRepository,
                                    UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.replyRepository = replyRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    // ── DTOs ─────────────────────────────────────────────

    public static class QuestionDTO {
        public Long id;
        public Long userId;
        public String userName;
        public String role;
        public String text;
        public String timestamp;
        public boolean edited;
        public List<ReplyDTO> replies = new ArrayList<>();
    }

    public static class ReplyDTO {
        public Long id;
        public Long userId;
        public String userName;
        public String role;
        public String text;
        public String timestamp;
        public boolean edited;
    }

    public static class CreateQuestionRequest {
        public Long userId;
        public String text;
    }

    public static class CreateReplyRequest {
        public Long userId;
        public String text;
    }

    public static class UpdateTextRequest {
        public String text;
    }

    // ── Endpoints ────────────────────────────────────────

    /** GET /api/questions/course/{courseId} — list all questions for a course */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<QuestionDTO>> getQuestions(@PathVariable Long courseId) {
        List<CourseQuestion> questions = questionRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
        List<QuestionDTO> dtos = new ArrayList<>();
        for (CourseQuestion q : questions) {
            dtos.add(toDTO(q));
        }
        return ResponseEntity.ok(dtos);
    }

    /** POST /api/questions/course/{courseId} — create a new question */
    @PostMapping("/course/{courseId}")
    public ResponseEntity<QuestionDTO> createQuestion(@PathVariable Long courseId,
                                                       @RequestBody CreateQuestionRequest req) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();
        User user = userRepository.findById(req.userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        CourseQuestion q = new CourseQuestion();
        q.setCourse(course);
        q.setUser(user);
        q.setText(req.text);
        q = questionRepository.save(q);
        return ResponseEntity.ok(toDTO(q));
    }

    /** PUT /api/questions/{questionId} — edit a question */
    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionDTO> updateQuestion(@PathVariable Long questionId,
                                                       @RequestBody UpdateTextRequest req) {
        CourseQuestion q = questionRepository.findById(questionId).orElse(null);
        if (q == null) return ResponseEntity.notFound().build();
        q.setText(req.text);
        q.setEdited(true);
        q = questionRepository.save(q);
        return ResponseEntity.ok(toDTO(q));
    }

    /** DELETE /api/questions/{questionId} — delete a question */
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        if (!questionRepository.existsById(questionId)) return ResponseEntity.notFound().build();
        questionRepository.deleteById(questionId);
        return ResponseEntity.ok().build();
    }

    /** POST /api/questions/{questionId}/replies — add a reply */
    @PostMapping("/{questionId}/replies")
    public ResponseEntity<ReplyDTO> createReply(@PathVariable Long questionId,
                                                 @RequestBody CreateReplyRequest req) {
        CourseQuestion q = questionRepository.findById(questionId).orElse(null);
        if (q == null) return ResponseEntity.notFound().build();
        User user = userRepository.findById(req.userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        CourseQuestionReply reply = new CourseQuestionReply();
        reply.setQuestion(q);
        reply.setUser(user);
        reply.setText(req.text);
        reply = replyRepository.save(reply);
        return ResponseEntity.ok(toReplyDTO(reply));
    }

    /** PUT /api/questions/replies/{replyId} — edit a reply */
    @PutMapping("/replies/{replyId}")
    public ResponseEntity<ReplyDTO> updateReply(@PathVariable Long replyId,
                                                 @RequestBody UpdateTextRequest req) {
        CourseQuestionReply reply = replyRepository.findById(replyId).orElse(null);
        if (reply == null) return ResponseEntity.notFound().build();
        reply.setText(req.text);
        reply.setEdited(true);
        reply = replyRepository.save(reply);
        return ResponseEntity.ok(toReplyDTO(reply));
    }

    /** DELETE /api/questions/replies/{replyId} — delete a reply */
    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable Long replyId) {
        if (!replyRepository.existsById(replyId)) return ResponseEntity.notFound().build();
        replyRepository.deleteById(replyId);
        return ResponseEntity.ok().build();
    }

    // ── Converters ───────────────────────────────────────

    private QuestionDTO toDTO(CourseQuestion q) {
        QuestionDTO dto = new QuestionDTO();
        dto.id = q.getId();
        dto.userId = q.getUser().getId();
        dto.userName = q.getUser().getNom();
        dto.role = q.getUser().getRole() != null ? q.getUser().getRole().name().toLowerCase() : "etudiant";
        dto.text = q.getText();
        dto.timestamp = q.getCreatedAt() != null ? q.getCreatedAt().toString() : null;
        dto.edited = q.isEdited();
        for (CourseQuestionReply r : q.getReplies()) {
            dto.replies.add(toReplyDTO(r));
        }
        return dto;
    }

    private ReplyDTO toReplyDTO(CourseQuestionReply r) {
        ReplyDTO dto = new ReplyDTO();
        dto.id = r.getId();
        dto.userId = r.getUser().getId();
        dto.userName = r.getUser().getNom();
        dto.role = r.getUser().getRole() != null ? r.getUser().getRole().name().toLowerCase() : "etudiant";
        dto.text = r.getText();
        dto.timestamp = r.getCreatedAt() != null ? r.getCreatedAt().toString() : null;
        dto.edited = r.isEdited();
        return dto;
    }
}
