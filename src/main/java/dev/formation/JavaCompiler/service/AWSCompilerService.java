package dev.formation.JavaCompiler.service;

import dev.formation.JavaCompiler.dto.CodeResponse;

import java.io.IOException;

public class AWSCompilerService implements ICompilerService {
    @Override
    public CodeResponse startContainer(String instrumentedCode, String imageName) throws IOException, InterruptedException {
        return new CodeResponse("Requête bien reçu sur le AWSCompilerService",null,null);
    }
}
