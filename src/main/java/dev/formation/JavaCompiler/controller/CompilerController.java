package dev.formation.JavaCompiler.controller;

import dev.formation.JavaCompiler.dto.CodeRequest;
import dev.formation.JavaCompiler.dto.CodeResponse;
import dev.formation.JavaCompiler.service.CompilerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/compile")
@CrossOrigin(origins = "http://localhost:4200") // Autorise uniquement Angular
public class CompilerController {

    private final CompilerService compilerService;

    public CompilerController(CompilerService compilerService) {
        this.compilerService = compilerService;
    }

    @PostMapping
    public ResponseEntity<CodeResponse> compileAndRun(@RequestBody CodeRequest codeRequest) {
        try {
            // Le client envoie le code et le langage
            String language = codeRequest.getLanguage();
            String code = codeRequest.getCode();

            // Transformation des guillemets simples en triple guillemets
            if ("java".equalsIgnoreCase(language)) {
                code = code.replace("\"", "\"\"\"");
            }

            // Compiler et exécuter le code
            String output = compilerService.compileAndRun(code, language);
            return ResponseEntity.ok(new CodeResponse(output));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CodeResponse("Compilation error: "+e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CodeResponse("Compilation or execution error: "+e.getMessage()));
        }
    }
}
