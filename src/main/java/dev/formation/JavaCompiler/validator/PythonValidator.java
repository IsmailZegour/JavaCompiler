package dev.formation.JavaCompiler.validator;

import org.springframework.stereotype.Component;

@Component("python")
public class PythonValidator implements LanguageValidator {

    private static final int MAX_LINES = 200; // Limite du nombre de lignes
    private static final int MAX_BYTES = 10_000; // Limite en octets (~10 Ko)
    private static final String[] FORBIDDEN_PATTERNS = {
            "import os",
            "import sys",
            "subprocess",
            "__import__",
            "eval\\(",
            "exec\\(",
            "open\\(",
            "socket\\.",
            "os\\.",
            "sys\\.",
            "shutil\\.",
            "importlib\\."
    };

    @Override
    public void validate(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be null or empty.");
        }

        // Vérifier le nombre de lignes
        long lineCount = code.lines().count();
        if (lineCount > MAX_LINES) {
            throw new IllegalArgumentException("Code exceeds the maximum allowed number of lines (" + MAX_LINES + ").");
        }

        // Vérifier la taille en octets
        int byteSize = code.getBytes().length;
        if (byteSize > MAX_BYTES) {
            throw new IllegalArgumentException("Code exceeds the maximum allowed size in bytes (" + MAX_BYTES + ").");
        }

        // Vérifier les motifs interdits
        for (String pattern : FORBIDDEN_PATTERNS) {
            if (code.matches("(?s).*" + pattern + ".*")) {
                throw new IllegalArgumentException("Code contains forbidden pattern: " + pattern);
            }
        }
    }

    @Override
    public String injectMeasurements(String code) {
        String before = """
                import os, psutil
                import time
                process = psutil.Process(os.getpid())
                memory_before = process.memory_info().rss / 1024
                start_time = time.time()
                """;

        String after = """
                end_time = time.time()
                execution_time = end_time - start_time  # Temps en secondes
                print("{ExecutionTime}:", f"{execution_time:.3f}")
                memory_after = process.memory_info().rss / 1024
                print("{MemoryUsage}:", memory_after - memory_before)
                """;

        // Ajouter les mesures au début et à la fin du script
        return before + "\n" + code + "\n" + after;
    }

    @Override
    public String getImageName() {
        return "pcw"; // Nom de l'image Docker pour Python
    }
}
