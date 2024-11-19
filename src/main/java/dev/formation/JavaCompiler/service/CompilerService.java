package dev.formation.JavaCompiler.service;


import dev.formation.JavaCompiler.dto.CodeRequest;
import dev.formation.JavaCompiler.dto.CodeResponse;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.concurrent.TimeUnit;

//TODO Rajouter les exitcodes pour les erreurs de compilations / execution
//TODO Stats : modifier le compile.sh du java-compiler-worker => faire noter les stats dans un fichier toute les X ms
//TODO Récupérer ce fichier dans le backend puis parser

@Service
public class CompilerService {

    private static final int MAX_LINES = 200; // Limite du nombre de lignes
    private static final int MAX_BYTES = 10_000; // Limite en octets (~10 Ko)
    private static final int TIMEOUT_SECONDS = 10; // Timeout en secondes
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
//            code = formatJava(code);
        }

        String containerId = startContainer(code, language);

        long startTime = System.nanoTime(); // Début du chronométrage

        String output = captureContainerOutput(containerId);
        long executionTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime); // Temps écoulé en ms

//        String stats = getContainerStatsCURL(containerId);
        String stats = "zebi";

        stopContainer(containerId);
        stats = String.valueOf(executionTime);
        return new CodeResponse(output, stats);
    }

    private static String startContainer(String code, String language) throws IOException {
        String imageName = switch (language.toLowerCase()) {
            case "java" -> "java-compiler-worker";
            case "python" -> "python-compiler-worker";
            case "c" -> "c-compiler-worker";
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };

        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run", "-d", "--memory=256m", "--cpus=0.5", imageName, code
        );
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.readLine(); // Retourne le container_id
        }
    }

    private static void waitForContainer(String containerId) throws IOException, InterruptedException {
        new ProcessBuilder("docker", "wait", containerId)
                .redirectErrorStream(true)
                .start()
                .waitFor();
    }

    private static String captureContainerOutput(String containerId) throws IOException, InterruptedException {

        // Attendre que le conteneur se termine
        waitForContainer(containerId);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "logs", containerId
        );
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();


    }

    public static String getContainerStatsCURL(String containerId) throws Exception {
        String dockerApiUrl = "http://localhost:2375/containers/" + containerId + "/stats?stream=false";
        URL url = new URL(dockerApiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString(); // JSON contenant les stats
    }

//    public static String getContainerStats(String containerId) throws Exception {
//        int a=0;
////        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
//        int b=0;

//        DefaultDockerClientConfig.Builder config
//                = DefaultDockerClientConfig.createDefaultConfigBuilder();

//        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
//                .dockerHost(config.getDockerHost())
//                .sslConfig(config.getSSLConfig())
//                .maxConnections(100)
//                .connectionTimeout(Duration.ofSeconds(30))
//                .responseTimeout(Duration.ofSeconds(45))
//                .build();



//        DockerClient dockerClient = DockerClientBuilder
//                .getInstance(config)
//                .build();

//        DockerClientConfig standard = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
//        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
//        dockerClient.pingCmd().exec();

//        StringBuilder statsResponse = new StringBuilder();
//        List<Container> containers = dockerClient.listContainersCmd().exec();
//        System.out.print(containers);
//        dockerClient.statsCmd(containerId).exec(new ResultCallback.Adapter<>() {
//            @Override
//            public void onNext(Statistics stats) {
//                statsResponse.append("Memory Usage: ").append(stats.getMemoryStats().getUsage()).append(" bytes\n");
//                statsResponse.append("Memory Max Usage: ").append(stats.getMemoryStats().getMaxUsage()).append(" bytes\n");
//                statsResponse.append("CPU Total Usage: ").append(stats.getCpuStats().getCpuUsage().getTotalUsage()).append(" nanoseconds\n");
//            }
//        }).awaitCompletion(); // Attendre que les stats soient collectées

//        return statsResponse.toString();
//    }

    private static String captureMemoryUsage(String containerId) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "stats", "--no-stream", "--format", "{{.MemUsage}}", containerId
        );
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.readLine(); // Mémoire utilisée
        }
    }

    private static void stopContainer(String containerId) throws IOException, InterruptedException {
        new ProcessBuilder("docker", "rm", "-f", containerId)
                .redirectErrorStream(true)
                .start()
                .waitFor();
    }

    private static String formatJava(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Code cannot be null.");
        }
        return input.replace("\"", "\"\"\""); // Échappe les guillemets pour éviter les erreurs
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
