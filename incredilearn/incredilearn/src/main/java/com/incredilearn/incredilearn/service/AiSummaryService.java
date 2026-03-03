package com.incredilearn.incredilearn.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.incredilearn.incredilearn.model.CourseContent;
import com.incredilearn.incredilearn.repository.CourseContentRepository;

@Service
public class AiSummaryService {

    private final CourseContentRepository courseContentRepository;
    private final RestClient restClient;
    private final Path uploadDir;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:llama3.2}")
    private String ollamaModel;

    public AiSummaryService(CourseContentRepository courseContentRepository) {
        this.courseContentRepository = courseContentRepository;
        this.uploadDir = Paths.get("uploads/course-content").toAbsolutePath().normalize();
        this.restClient = RestClient.create();
    }

    public String generateSummary(List<Long> contentIds, List<String> lessonDescriptions) {
        List<String> contextParts = new ArrayList<>();

        if (contentIds != null && !contentIds.isEmpty()) {
            for (Long id : contentIds) {
                CourseContent content = courseContentRepository.findById(id).orElse(null);
                if (content == null) continue;

                String extracted = extractContent(content);
                if (extracted != null && !extracted.isBlank()) {
                    contextParts.add("--- " + content.getFileName() + " ---\n" + extracted.trim());
                }
            }
        }

        // Supplement with lesson descriptions if provided
        if (lessonDescriptions != null && !lessonDescriptions.isEmpty()) {
            contextParts.add("=== Descriptions des sections ===");
            for (String desc : lessonDescriptions) {
                contextParts.add("- " + desc);
            }
        }

        if (contextParts.isEmpty()) {
            return "Aucun contenu extractible trouvé. Vérifiez que les fichiers sont des PDF, DOCX, TXT ou des vidéos (nécessite FFmpeg + whisper installés).";
        }

        String context = String.join("\n\n", contextParts);
        // Limit context to ~10000 characters to stay within model limits
        if (context.length() > 10000) {
            context = context.substring(0, 10000) + "\n[... contenu tronqué ...]";
        }

        String prompt = """
                Tu es un assistant pédagogique expert spécialisé dans la synthèse de documents académiques.

                À partir du contenu de cours ci-dessous, produis un résumé structuré en français qui dégage les **grandes lignes** du document.

                Le résumé doit impérativement contenir :

                1. **Thème principal** : une phrase d'accroche résumant le sujet global du document.
                2. **Points clés** : une liste à puces des idées essentielles et concepts fondamentaux abordés, dans l'ordre logique du document. Chaque point doit être concis (1-2 phrases max).
                3. **Notions importantes** : les définitions, formules, théorèmes ou termes techniques majeurs à retenir, présentés clairement.
                4. **Articulation** : explique brièvement comment les différentes parties du document s'enchaînent et se complètent.
                5. **À retenir** : 3 à 5 points essentiels que l'étudiant doit absolument maîtriser après la lecture.

                Consignes de style :
                - Utilise un langage clair, accessible et pédagogique.
                - Structure le résumé avec des titres en gras pour chaque section.
                - Privilégie la précision : ne reformule pas de manière vague, reste fidèle au contenu original.
                - Longueur cible : entre 250 et 500 mots.
                - Réponds uniquement en français.

                Contenu du cours :
                %s

                Résumé structuré :
                """.formatted(context);

        return callOllama(prompt);
    }

    // ── Dispatch extraction by file type ───────────────────────────────────────
    private String extractContent(CourseContent content) {
        String mime = content.getMimeType() != null ? content.getMimeType().toLowerCase() : "";
        String name = content.getFileName() != null ? content.getFileName().toLowerCase() : "";
        Path filePath = uploadDir.resolve(content.getStoredFileName()).normalize();

        if (!Files.exists(filePath)) return null;

        if (mime.contains("pdf") || name.endsWith(".pdf")) {
            return extractPdf(filePath);
        } else if (mime.contains("wordprocessingml") || mime.contains("msword") || name.endsWith(".docx") || name.endsWith(".doc")) {
            return extractDocx(filePath);
        } else if (mime.startsWith("text/") || name.endsWith(".txt") || name.endsWith(".md")) {
            return extractText(filePath);
        } else if (mime.startsWith("video/") || isVideoExtension(name)) {
            return transcribeVideo(filePath, content.getFileName());
        }
        return null;
    }

    private boolean isVideoExtension(String name) {
        return name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mov")
            || name.endsWith(".mkv") || name.endsWith(".webm") || name.endsWith(".flv");
    }

    // ── PDF extraction ─────────────────────────────────────────────────────────
    private String extractPdf(Path filePath) {
        try (PDDocument doc = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        } catch (IOException e) {
            return "[Erreur lecture PDF : " + e.getMessage() + "]";
        }
    }

    // ── DOCX extraction ────────────────────────────────────────────────────────
    private String extractDocx(Path filePath) {
        try (InputStream is = Files.newInputStream(filePath);
             XWPFDocument doc = new XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                String text = p.getText();
                if (text != null && !text.isBlank()) {
                    sb.append(text).append("\n");
                }
            }
            return sb.toString();
        } catch (IOException e) {
            return "[Erreur lecture DOCX : " + e.getMessage() + "]";
        }
    }

    // ── Plain text extraction ──────────────────────────────────────────────────
    private String extractText(Path filePath) {
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            try {
                return Files.readString(filePath, StandardCharsets.ISO_8859_1);
            } catch (IOException ex) {
                return "[Erreur lecture texte : " + ex.getMessage() + "]";
            }
        }
    }

    // ── Video transcription via FFmpeg + Whisper ───────────────────────────────
    private String transcribeVideo(Path videoPath, String originalName) {
        Path tempDir = null;
        Path audioPath = null;
        try {
            tempDir = Files.createTempDirectory("incredilearn_whisper_");
            audioPath = tempDir.resolve(UUID.randomUUID() + ".wav");

            // Step 1: extract audio with FFmpeg
            ProcessBuilder ffmpegPb = new ProcessBuilder(
                "ffmpeg", "-y", "-i", videoPath.toString(),
                "-ar", "16000", "-ac", "1", "-c:a", "pcm_s16le",
                audioPath.toString()
            );
            ffmpegPb.redirectErrorStream(true);
            Process ffmpegProc = ffmpegPb.start();
            boolean ffmpegDone = ffmpegProc.waitFor(120, TimeUnit.SECONDS);
            if (!ffmpegDone || ffmpegProc.exitValue() != 0) {
                return "[Impossible d'extraire l'audio de la vidéo. Assurez-vous que FFmpeg est installé (https://ffmpeg.org)]";
            }

            // Step 2: transcribe with Whisper (Python)
            String audioStem = audioPath.getFileName().toString().replace(".wav", "");
            ProcessBuilder whisperPb = new ProcessBuilder(
                "python", "-m", "whisper",
                audioPath.toString(),
                "--model", "tiny",
                "--task", "transcribe",
                "--output_format", "txt",
                "--output_dir", tempDir.toString()
            );
            whisperPb.redirectErrorStream(true);
            Process whisperProc = whisperPb.start();
            boolean whisperDone = whisperProc.waitFor(300, TimeUnit.SECONDS);

            if (!whisperDone || whisperProc.exitValue() != 0) {
                // Try reading stderr for useful message
                String errMsg = new String(whisperProc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                if (errMsg.contains("No module named")) {
                    return "[Whisper n'est pas installé. Exécutez : pip install openai-whisper]";
                }
                return "[Erreur transcription Whisper : " + errMsg.substring(0, Math.min(200, errMsg.length())) + "]";
            }

            // Step 3: read transcript file
            Path transcriptFile = tempDir.resolve(audioStem + ".txt");
            if (Files.exists(transcriptFile)) {
                return "=== Transcription vidéo : " + originalName + " ===\n"
                    + Files.readString(transcriptFile, StandardCharsets.UTF_8);
            }
            return "[Fichier de transcription introuvable]";

        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("No such file")) {
                return "[FFmpeg introuvable. Installez FFmpeg et ajoutez-le au PATH (https://ffmpeg.org)]";
            }
            return "[Erreur : " + e.getMessage() + "]";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "[Transcription interrompue]";
        } finally {
            // Cleanup temp files
            try {
                if (audioPath != null) Files.deleteIfExists(audioPath);
                if (tempDir != null) {
                    Files.walk(tempDir)
                        .sorted(java.util.Comparator.reverseOrder())
                        .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
                }
            } catch (IOException ignored) {}
        }
    }

    // ── Ollama call ────────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private String callOllama(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                "model", ollamaModel,
                "prompt", prompt,
                "stream", false
            );

            Map<?, ?> response = restClient.post()
                .uri(ollamaBaseUrl + "/api/generate")
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);

            if (response != null && response.get("response") instanceof String text) {
                return text.trim();
            }
            return "Le modèle n'a pas retourné de réponse.";
        } catch (Exception e) {
            return "Erreur lors de la communication avec Ollama : " + e.getMessage()
                + "\nAssurez-vous qu'Ollama est lancé et que le modèle '" + ollamaModel + "' est disponible.";
        }
    }
}

