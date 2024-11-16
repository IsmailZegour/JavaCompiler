package dev.formation.JavaCompiler.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CompilerService {

    public String compileAndRun(String code) throws IOException, InterruptedException {
        // Extraire le nom de la classe publique
        String className = extractClassName(code);

        if (className == null) {
            return "Error: Could not find a public class declaration.";
        }

        // Créer un fichier temporaire avec le bon nom
        Path tempDir = Files.createTempDirectory("java-code");
        Path javaFile = tempDir.resolve(className + ".java");
        Files.write(javaFile, code.getBytes());

        // Compiler le fichier
        Process compileProcess = new ProcessBuilder("javac", javaFile.toString()).start();
        compileProcess.waitFor();

        // Vérifier les erreurs de compilation
        if (compileProcess.exitValue() != 0) {
            return new String(compileProcess.getErrorStream().readAllBytes());
        }

        // Exécuter le fichier compilé
        Process runProcess = new ProcessBuilder("java", "-cp", tempDir.toString(), className).start();
        runProcess.waitFor();

        // Récupérer la sortie ou les erreurs
        if (runProcess.exitValue() == 0) {
            return new String(runProcess.getInputStream().readAllBytes()).replace("\r\n", "\n");
        } else {
            return new String(runProcess.getErrorStream().readAllBytes());
        }
    }

    private String extractClassName(String code) {
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);
        return matcher.find() ? matcher.group(1) : null;
    }

}

