package dev.formation.JavaCompiler.controller;

import dev.formation.JavaCompiler.dto.CodeRequest;
import dev.formation.JavaCompiler.dto.CodeResponse;
import dev.formation.JavaCompiler.dto.FileRequest;
import dev.formation.JavaCompiler.service.CompilerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/compile")
@CrossOrigin(origins = "http://localhost:4200") // Autorise uniquement les requêtes venant de Angular
public class CompilerController {

    private final CompilerService compilerService;

    public CompilerController(CompilerService compilerService) {
        this.compilerService = compilerService;
    }

    @PostMapping
    public ResponseEntity<?> compileAndRun(@RequestBody CodeRequest codeRequest) {
        try {
            String output = compilerService.compileAndRun(codeRequest.getCode());
            return ResponseEntity.ok(new CodeResponse(output));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CodeResponse(e.getMessage()));
        }
    }

    @PostMapping("/from-file")
    public ResponseEntity<CodeResponse> compileFromFile(@RequestBody FileRequest fileRequest) {
        try {
            // Lire le contenu du fichier
            Path path = Paths.get(fileRequest.getFilePath());
            String code = Files.readString(path);

            // Compiler et exécuter le code
            String output = compilerService.compileAndRun(code);
            return ResponseEntity.ok(new CodeResponse(output));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CodeResponse("Error reading file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CodeResponse("Compilation or execution error: " + e.getMessage()));
        }
    }
}

