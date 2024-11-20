package dev.formation.JavaCompiler.service;

import dev.formation.JavaCompiler.dto.CodeRequest;
import dev.formation.JavaCompiler.dto.CodeResponse;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class CompilerService {

    private static final int MAX_LINES = 200; // Limite du nombre de lignes
    private static final int MAX_BYTES = 10_000; // Limite en octets (~10 Ko)
    private static final String[] FORBIDDEN_CLASSES = {
            "Runtime",
            "ProcessBuilder",
            "System",
            "com.jcraft.jsch", // SSH (ex: JSch)
            "java.net.HttpURLConnection", // HttpURLConnection
            "org.apache.http.client.HttpClient", // Apache HttpClient
            "com.github.dockerjava", // Docker Java SDK
            "java.net.Socket", // Sockets
            "java.nio.channels.SocketChannel", // NIO Sockets
            "java.io.File", // File operations
            "java.nio.file.Paths" // NIO Path operations
    };

    private static final String[] FORBIDDEN_IMPORTS = {
            "java.lang.Runtime",
            "java.nio.file.Files",
            "sun.misc",
            "com.jcraft.jsch", // SSH (JSch)
            "java.net.HttpURLConnection", // HttpURLConnection
            "org.apache.http.client.HttpClient", // Apache HttpClient
            "com.github.dockerjava", // Docker Java SDK
            "java.net.Socket", // Sockets
            "java.nio.channels.SocketChannel", // NIO Sockets
            "java.io.File", // File operations
            "java.nio.file.Paths" // NIO Path operations
    };

    private static final String[] FORBIDDEN_PATTERNS = {
            "Runtime\\.",
            "ProcessBuilder",
            "System\\.exit",
            "exec\\(",
            "loadLibrary\\(",
            "com\\.jcraft\\.jsch", // SSH
            "java\\.net\\.HttpURLConnection", // HttpURLConnection
            "org\\.apache\\.http\\.client\\.HttpClient", // HttpClient
            "com\\.github\\.dockerjava", // Docker Java
            "java\\.net\\.Socket", // Sockets
            "java\\.nio\\.channels\\.SocketChannel", // NIO Sockets
            "java\\.io\\.File", // File operations
            "java\\.nio\\.file\\.Paths" // Path operations
    };

    public CodeResponse compileAndRun(CodeRequest codeRequest) throws Exception {
        String language = codeRequest.getLanguage();
        String code = codeRequest.getCode();

        if (language.equalsIgnoreCase("java")) {
            validateJava(code); // Validation avant traitement
        }

        CodeResponse response = startContainer(code, language);
        System.out.println("Output: " + response.getOutput());

        return response;
    }

    private static CodeResponse startContainer(String code, String language) throws IOException, InterruptedException {
        String imageName = switch (language.toLowerCase()) {
            case "java" -> "jcw";
            case "python" -> "python-compiler-worker";
            case "c" -> "c-compiler-worker";
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };

        String instrumentedCode = injectMemoryMeasurement(code);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run",
                "--rm", // Supprime automatiquement le conteneur après exécution
                "--memory=256m", // Limiter la mémoire
                "--cpus=0.5", // Limiter les CPU
                imageName, // Nom de l'image
                instrumentedCode // Argument à passer au conteneur
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

    private static String injectMemoryMeasurement(String code) {
        String memoryBefore = """
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
        """;

        String memoryAfter = """
        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("{MemoryUsage}:" + ((usedMemoryAfter-usedMemoryBefore)/ 1024));
        """;

        // Trouver le début de la méthode main
        int mainStartIndex = code.indexOf("public static void main(String[] args) {");
        if (mainStartIndex == -1) {
            throw new IllegalArgumentException("No main method found in the code.");
        }

        // Trouver l'index pour insérer le code après la déclaration de la méthode main
        int insertAfterMain = mainStartIndex + "public static void main(String[] args) {".length();

        // Séparer le code en deux parties
        String codeBeforeMain = code.substring(0, insertAfterMain);
        String codeAfterMain = code.substring(insertAfterMain);

        // Identifier la fin de la méthode main
        int openBraces = 1; // Une accolade ouverte pour "main"
        int mainEndIndex = -1;
        for (int i = 0; i < codeAfterMain.length(); i++) {
            char ch = codeAfterMain.charAt(i);
            if (ch == '{') {
                openBraces++;
            } else if (ch == '}') {
                openBraces--;
            }
            if (openBraces == 0) { // Trouvé la fermeture de "main"
                mainEndIndex = i;
                break;
            }
        }

        if (mainEndIndex == -1) {
            throw new IllegalArgumentException("Could not find the end of the main method.");
        }

        return codeBeforeMain
                + "\n" + memoryBefore + "\n"
                + codeAfterMain.substring(0, mainEndIndex)
                + "\n" + memoryAfter + "\n"
                + codeAfterMain.substring(mainEndIndex);
    }




    private static void validateJava(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be null or empty.");
        }

        // Vérifier le nombre de lignes
        long lineCount = code.lines().count();
        if (lineCount > MAX_LINES) {
            throw new IllegalArgumentException("Code exceeds the maximum allowed number of lines (" + MAX_LINES + ").");
        }

        // Vérifier la taille en octets
        int byteSize = code.getBytes().length;
        if (byteSize > MAX_BYTES) {
            throw new IllegalArgumentException("Code exceeds the maximum allowed size in bytes (" + MAX_BYTES + ").");
        }

        // Vérifier les classes interdites
        for (String forbiddenClass : FORBIDDEN_CLASSES) {
            if (forbiddenClass.equals("System")) {
                boolean hasValidUsage = code.contains("System.out.print") || code.contains("System.out.println");
                boolean hasInvalidUsage = code.matches("(?s).*System\\.(?!out\\.print|out\\.println).*");
                if (!hasValidUsage || hasInvalidUsage) {
                    throw new IllegalArgumentException("Code contains forbidden usage of class: " + forbiddenClass);
                }
            } else if (code.contains(forbiddenClass)) {
                throw new IllegalArgumentException("Code contains forbidden class: " + forbiddenClass);
            }
        }

        // Vérifier les imports interdits
        for (String forbiddenImport : FORBIDDEN_IMPORTS) {
            if (code.contains("import " + forbiddenImport)) {
                throw new IllegalArgumentException("Code contains forbidden import: " + forbiddenImport);
            }
        }

        // Vérifier les injections malveillantes
        for (String pattern : FORBIDDEN_PATTERNS) {
            if (code.matches("(?s).*" + pattern + ".*")) {
                throw new IllegalArgumentException("Code contains forbidden pattern: " + pattern);
            }
        }
    }
}
