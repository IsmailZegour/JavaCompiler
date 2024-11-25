package dev.formation.JavaCompiler.service;

import dev.formation.JavaCompiler.dto.CodeResponse;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class LocalCompilerService implements ICompilerService {

    public CodeResponse startContainer(String instrumentedCode, String imageName) throws IOException, InterruptedException {

        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run",
                "--rm", // Supprime automatiquement le conteneur après exécution
                "--memory=256m", // Limiter la mémoire
                "--cpus=0.5", // Limiter les CPU
                imageName, // Nom de l'image
                instrumentedCode // Code en argument
        );
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        String executionTime="";
        String memoryUsage="";

        int exitCode = process.waitFor();

        // Capturer la sortie du conteneur
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.contains("{ExecutionTime}")){
                    executionTime= line.split(":")[1];
                } else if (line.contains("{MemoryUsage}")) {
                    memoryUsage= line.split(":")[1];
                }else{
                    output.append(line).append("\n");
                }
            }
        }

        if (exitCode != 0) {
            throw new RuntimeException(output.toString());
        }

        return new CodeResponse(output.toString(),executionTime,memoryUsage);
    }

}
