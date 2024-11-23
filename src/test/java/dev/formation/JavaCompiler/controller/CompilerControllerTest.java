package dev.formation.JavaCompiler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.formation.JavaCompiler.dto.CodeRequest;
import dev.formation.JavaCompiler.dto.CodeResponse;
import dev.formation.JavaCompiler.samples.JavaSamples;
import dev.formation.JavaCompiler.service.CompilerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompilerController.class)
//@ActiveProfiles("test")
class CompilerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompilerService compilerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment environment;

    @Value("${frontend.url}")
    private String frontendUrl; // URL de votre frontend injectée depuis les variables d'environnement


    @Test
    void checkActiveProfile() {
        System.out.println("Active Profiles: " + Arrays.toString(environment.getActiveProfiles()));
    }
    @Test
    void compileAndRun_Successful() throws Exception {
        // Mock de la réponse attendue
        CodeResponse mockResponse = new CodeResponse("Hello, World!", "", "");
        CodeRequest codeRequest = new CodeRequest("java", JavaSamples.getCorrectHelloWorld());
        Mockito.when(compilerService.compileAndRun(codeRequest)).thenReturn(mockResponse);

        // Requête JSON d'entrée
        String requestJson = objectMapper.writeValueAsString(codeRequest);

        // Effectuer la requête POST et vérifier la réponse
        mockMvc.perform(post("/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Origin",frontendUrl)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.output").value(mockResponse.getOutput())); // Vérifie uniquement le champ output
    }

    @Test
    void compileAndRun_BadRequest() throws Exception {
        // Mock pour lancer une exception IllegalArgumentException
        Mockito.when(compilerService.compileAndRun(any(CodeRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid code"));

        // Requête JSON d'entrée
        CodeRequest codeRequest = new CodeRequest("java", "");
        String requestJson = objectMapper.writeValueAsString(codeRequest);

        // Effectuer la requête POST et vérifier la réponse
        mockMvc.perform(post("/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"output\":\"Invalid code\",\"executionTime\":null,\"memoryUsage\":null}"));
    }

    @Test
    void compileAndRun_InternalServerError() throws Exception {
        // Mock pour lancer une exception générique
        Mockito.when(compilerService.compileAndRun(any(CodeRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Requête JSON d'entrée
        CodeRequest codeRequest = new CodeRequest("java", "System.out.println(\"Hello, World!\");");
        String requestJson = objectMapper.writeValueAsString(codeRequest);

        // Effectuer la requête POST et vérifier la réponse
        mockMvc.perform(post("/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"output\":\"Unexpected error\",\"executionTime\":null,\"memoryUsage\":null}"));
    }
}
