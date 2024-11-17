FROM openjdk:17-jdk-slim

# Créer un répertoire de travail
WORKDIR /workspace

# Installer timeout (inclus dans coreutils)
RUN apt-get update && apt-get install -y coreutils && apt-get clean

# Copier le script d'exécution
COPY compile.sh /workspace/compile.sh
RUN chmod +x /workspace/compile.sh

# Entrée par défaut du conteneur
ENTRYPOINT ["/workspace/compile.sh"]
