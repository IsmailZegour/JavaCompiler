package dev.formation.JavaCompiler.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class CompilerService {

    public String compileAndRun(String code, String language) throws IOException, InterruptedException {
        Process process = getProcess(code, language);

        // Lire la sortie du conteneur
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // Attendre la fin du processus
        int exitCode = process.waitFor();

        return output.toString();
    }

    private static Process getProcess(String code, String language) throws IOException {
        String imageName;

        // Associer le langage à l'image Docker correspondante
        switch (language.toLowerCase()) {
            case "java":
                imageName = "java-compiler-worker";
                break;
            case "python":
                imageName = "python-compiler-worker";
                break;
            case "c":
                imageName = "c-compiler-worker";
                break;
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }

        // Construire et exécuter la commande Docker
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run", "--rm", imageName, code
        );
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }
}
