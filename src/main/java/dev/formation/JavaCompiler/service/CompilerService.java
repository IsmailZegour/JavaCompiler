package dev.formation.JavaCompiler.service;

import dev.formation.JavaCompiler.dto.CodeRequest;
import dev.formation.JavaCompiler.dto.CodeResponse;
import dev.formation.JavaCompiler.validator.LanguageValidator;
import dev.formation.JavaCompiler.validator.ValidatorFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class CompilerService {

//    private final ValidatorFactory validatorFactory;
//
//    public CompilerService(ValidatorFactory validatorFactory) {
//        this.validatorFactory = validatorFactory;
//    }

//    public CodeResponse compileAndRun(CodeRequest codeRequest) throws IOException, InterruptedException {
//        String language = codeRequest.getLanguage();
//        String code = codeRequest.getCode();
//
//        LanguageValidator validator = validatorFactory.getValidator(language);
//        validator.validate(code); // Validation avant traitement
//
//        String imageName = validator.getImageName();
//        String instrumentedCode = validator.injectMeasurements(code);
//
//        CodeResponse response = startContainer(instrumentedCode, imageName);
//
//        return response;
//    }

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
