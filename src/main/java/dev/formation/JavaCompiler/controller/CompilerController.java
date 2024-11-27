package dev.formation.JavaCompiler.controller;

import dev.formation.JavaCompiler.dto.CodeRequest;
import dev.formation.JavaCompiler.dto.CodeResponse;
import dev.formation.JavaCompiler.service.ICompilerService;
import dev.formation.JavaCompiler.validator.LanguageValidator;
import dev.formation.JavaCompiler.validator.ValidatorFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
//@CrossOrigin(origins = "${frontend.url}") // Autorise uniquement Angular
@CrossOrigin(origins = "*") // Autorise tout (test)
public class CompilerController {

    private final ICompilerService compilerService;

    private final ValidatorFactory validatorFactory;

    public CompilerController(ICompilerService compilerService, ValidatorFactory validatorFactory) {
        this.compilerService = compilerService;
        this.validatorFactory = validatorFactory;
    }

    @PostMapping("/compile")
    public ResponseEntity<CodeResponse> compileAndRun(@RequestBody CodeRequest codeRequest) {
        try {
            System.out.println("Requête reçue par le front");
            String language = codeRequest.getLanguage();
            String code = codeRequest.getCode();
            LanguageValidator validator = validatorFactory.getValidator(language);
            validator.validate(code); // Validation avant traitement

            String imageName = validator.getImageName();
            String instrumentedCode = validator.injectMeasurements(code);

            return ResponseEntity.ok(compilerService.startContainer(instrumentedCode, imageName));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new CodeResponse(e.getMessage(), null,null));
        } catch (Exception e) {
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
