package dev.formation.JavaCompiler.controller;

import dev.formation.JavaCompiler.dto.CodeRequest;
import dev.formation.JavaCompiler.dto.CodeResponse;
import dev.formation.JavaCompiler.service.CompilerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/compile")
@CrossOrigin(origins = "${frontend.url}") // Autorise uniquement Angular
public class CompilerController {

    private final CompilerService compilerService;

    public CompilerController(CompilerService compilerService) {
        this.compilerService = compilerService;
    }

    @PostMapping
    public ResponseEntity<CodeResponse> compileAndRun(@RequestBody CodeRequest codeRequest) {
        try {

            CodeResponse response = compilerService.compileAndRun(codeRequest);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Gère les erreurs de validation (mauvais code, etc.)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new CodeResponse(e.getMessage(), null,null));
        } catch (Exception e) {
            // Gère les erreurs internes ou inattendues
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CodeResponse(e.getMessage(),null,null));
        }
    }
    @GetMapping("/test")
    public String getHelloWorld(){
        return "Hello, World!";
    }
}
