# Étape 1 : Image d'exécution
FROM eclipse-temurin:21-jdk

# Définir le répertoire de travail
WORKDIR /app

# Copier le JAR pré-construit depuis le build local
# Assurez-vous que le JAR est généré localement dans le dossier `target`
COPY target/*.jar app.jar

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
