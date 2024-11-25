package dev.formation.JavaCompiler.service;

import dev.formation.JavaCompiler.dto.CodeResponse;

import java.io.IOException;

public interface ICompilerService {
    CodeResponse startContainer(String instrumentedCode, String imageName) throws IOException, InterruptedException ;
}
