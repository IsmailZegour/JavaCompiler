package dev.formation.JavaCompiler.validator;

import org.springframework.stereotype.Component;

@Component("java")
public class JavaValidator implements LanguageValidator {
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


    @Override
    public String injectMeasurements(String code) {
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

    @Override
    public String getImageName() {
        return "jcw";
    }
    @Override
    public void validate(String code) {
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
