package dev.formation.JavaCompiler.samples;

public class JavaSamples {

    // === Exemples de code Java correct ===

    public static String getSimpleHelloWorld() {
        return """
                // Un exemple basique de code Java valide
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }
                """;
    }

    public static String getSimpleForLoop() {
        return """
                // Exemple d'une boucle for simple en Java
                public class Main {
                    public static void main(String[] args) {
                        for (int i = 0; i < 5; i++) {
                            System.out.println("Iteration: " + i);
                        }
                    }
                }
                """;
    }

    public static String getMemoryMeasurement() {
        return """
                // Exemple de mesure d'utilisation mémoire en Java
                public class Main {
                    public static void main(String[] args) {
                        Runtime runtime = Runtime.getRuntime();
                        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
                        System.out.println("Memory used before: " + usedMemoryBefore + " bytes");
                        
                        // Simulate memory usage
                        int[] array = new int[100000];
                        
                        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
                        System.out.println("Memory used after: " + usedMemoryAfter + " bytes");
                    }
                }
                """;
    }

    // === Exemples de code Java incorrect ===

    public static String getMissingMainMethod() {
        return """
                // Exemple de code Java incorrect (manque de méthode main)
                public class Main {
                    public void helloWorld() {
                        System.out.println("Hello, World!");
                    }
                }
                """;
    }

    public static String getForbiddenClassUsage() {
        return """
                // Exemple de code Java incorrect (utilisation interdite de Runtime)
                public class Main {
                    public static void main(String[] args) {
                        Runtime runtime = Runtime.getRuntime();
                        runtime.exec("notepad.exe");
                    }
                }
                """;
    }

    public static String getSyntaxError() {
        return """
                // Exemple de code Java incorrect (erreur de syntaxe)
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!"
                    }
                }
                """;
    }
}
