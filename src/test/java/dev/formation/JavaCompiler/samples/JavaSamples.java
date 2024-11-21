package dev.formation.JavaCompiler.samples;

public class JavaSamples {

    // === Exemple de code Java correct ===
    public static String getCorrectHelloWorld() {
        return "// Un exemple basique de code Java valide\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}";
    }

    // === Exemples de code Java incorrect ===
    public static String getIncorrectMissingMainMethod() {
        return "// Exemple de code Java incorrect (manque de méthode main)\n" +
                "public class Main {\n" +
                "    public void helloWorld() {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}";
    }

    public static String getIncorrectForbiddenClassUsage() {
        return "// Exemple de code Java incorrect (utilisation interdite de Runtime)\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        Runtime runtime = Runtime.getRuntime();\n" +
                "        runtime.exec(\"notepad.exe\");\n" +
                "    }\n" +
                "}";
    }

    public static String getIncorrectSyntaxError() {
        return "// Exemple de code Java incorrect (erreur de syntaxe)\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}";
    }
}

