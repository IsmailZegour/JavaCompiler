# Étape 1 : Construire l'application
FROM maven:3.9.8-eclipse-temurin-21 AS builder

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers nécessaires pour la construction
COPY pom.xml .
COPY src ./src

# Construire l'application
RUN mvn clean package -DskipTests

# Étape 2 : Image d'exécution
FROM maven:3.9.8-eclipse-temurin-21 AS runtime

# Définir le répertoire de travail
WORKDIR /app

# Copier l'artefact généré depuis la phase de build
COPY --from=builder /app/target/*.jar app.jar

RUN apt-get update && apt-get install -y \
    docker.io \
    && rm -rf /var/lib/apt/lists/*

# Exposer le port utilisé par l'application
EXPOSE 8080

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
