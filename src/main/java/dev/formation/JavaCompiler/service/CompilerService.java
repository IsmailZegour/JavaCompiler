package dev.formation.JavaCompiler.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Service
public class CompilerService {

    private static final int MAX_LINES = 200; // Limite du nombre de lignes
    private static final int MAX_BYTES = 10_000; // Limite en octets (~10 Ko)
    private static final String[] FORBIDDEN_CLASSES = {"Runtime", "ProcessBuilder", "System"};
    private static final String[] FORBIDDEN_IMPORTS = {"java.lang.Runtime", "java.nio.file.Files","sun.misc"};
    private static final String[] FORBIDDEN_PATTERNS = {"Runtime\\.", "ProcessBuilder", "System\\.exit", "exec\\(", "loadLibrary\\("};

    public String compileAndRun(String code, String language) throws IOException, InterruptedException {
        if(language.equalsIgnoreCase("java")){
            validateJava(code); // Validation avant traitement
            code = formatJava(code);
        }

        Process process = getProcess(code, language);

        // Lire la sortie du conteneur
        StringBuilder output = new StringBuilder();
        Thread stdoutReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading output from process.", e);
            }
        });

        stdoutReader.start();

        // Attendre la fin du processus ou un timeout
        boolean finished = process.waitFor(10, TimeUnit.SECONDS); // Timeout de 10 secondes
        if (!finished) {
            process.destroy();
            throw new RuntimeException("Execution timeout: process took too long.");
        }

        int exitCode = process.waitFor();
        stdoutReader.join(); // Assurez-vous que le thread se termine avant de continuer

        if (exitCode != 0) {
            throw new RuntimeException("Docker process failed with exit code " + exitCode + ": " + output);
        }

        return output.toString();
    }

    private static String formatJava(String input) {
        return input.replace("\"", "\"\"\"");// Transforme les " en """ pour passer le code correctement
    }

    private static void validateJava(String code) {
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

        // Vérifier les classes interdites
        for (String forbiddenClass : FORBIDDEN_CLASSES) {
            if (forbiddenClass.equals("System")) {
                // Vérifier si System est utilisé sans être suivi de System.out.print ou System.out.println
                if (code.contains("System")) {
                    boolean hasValidUsage = code.contains("System.out.print") || code.contains("System.out.println");
                    boolean hasInvalidUsage = code.matches("(?s).*System\\.(?!out\\.print|out\\.println).*");
                    if (!hasValidUsage || hasInvalidUsage) {
                        throw new IllegalArgumentException("Code contains forbidden class: " + forbiddenClass);
                    }
                }
            } else if (code.contains(forbiddenClass)) {
                throw new IllegalArgumentException("Code contains forbidden class: " + forbiddenClass);
            }
        }



        // Vérifier les imports interdits
        for (String forbiddenImport : FORBIDDEN_IMPORTS) {
            if (code.contains("import " + forbiddenImport)) {
                throw new IllegalArgumentException("Code contains forbidden import: " + forbiddenImport);
            }
        }

        // Vérifier les injections malveillantes
        for (String pattern : FORBIDDEN_PATTERNS) {
            if (code.matches("(?s).*" + pattern + ".*")) {
                throw new IllegalArgumentException("Code contains forbidden pattern: " + pattern);
            }
        }
    }

    private static Process getProcess(String code, String language) throws IOException {
        String imageName = switch (language.toLowerCase()) {
            case "java" -> "java-compiler-worker";
            case "python" -> "python-compiler-worker";
            case "c" -> "c-compiler-worker";
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };

        // Construire et exécuter la commande Docker
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run", "--rm", "--memory=256m", "--cpus=0.5", imageName, code
        );
        processBuilder.redirectErrorStream(true);

        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new IOException("Failed to execute Docker command. Ensure Docker is installed and running.", e);
        }
    }
}
