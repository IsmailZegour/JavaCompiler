# Étape 1 : Construire les dépendances Maven
FROM maven:3.9.8-eclipse-temurin-21 AS builder

# Définir le répertoire de travail
WORKDIR /app

# Étape 1.1 : Copier les fichiers nécessaires pour les dépendances
COPY pom.xml .

# Étape 1.2 : Résoudre les dépendances Maven
RUN mvn dependency:go-offline -B

# Étape 1.3 : Copier le reste du code source
COPY src ./src

# Étape 1.4 : Construire l'application
RUN mvn clean package -DskipTests

# Étape 2 : Image d'exécution avec Docker installé
FROM eclipse-temurin:21-jre AS runtime

# Définir le répertoire de travail
WORKDIR /app

# Copier l'artefact généré depuis la phase de build
COPY --from=builder /app/target/*.jar app.jar

# Installer Docker
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    docker.io && \
    rm -rf /var/lib/apt/lists/*

# Exposer le port utilisé par l'application
EXPOSE 8080

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
