
# **Java Online Compiler - Backend**

## **Description**

Java Online Compiler Backend est une application Spring Boot conçue pour compiler et exécuter du code Java envoyé par le frontend via une API REST sécurisée. Ce backend traite la compilation et retourne les résultats au frontend, offrant une expérience utilisateur fluide pour les développeurs.

---

## **Fonctionnalités**

- 🔄 **Compilation dynamique de code Java** : Compile et exécute du code Java à la volée.
- 🔌 **API REST** : Interface permettant la communication avec le frontend Angular.
- 🔒 **Sécurité intégrée** : Gestion des autorisations CORS et sandboxing de l'exécution.

---

## **Prérequis**

Pour exécuter ce projet localement, assurez-vous d'avoir les éléments suivants installés :

- **Java 17** ou supérieur
- **Maven 3.8** ou supérieur

---

## **Installation et Configuration**

1. **Clonez le dépôt** :
   ```bash
   git clone https://github.com/votre-utilisateur/java-online-compiler-backend.git
   cd java-online-compiler-backend
   ```

2. **Compilez et lancez l'application** :
   ```bash
   mvn spring-boot:run
   ```

3. **Accédez à l'API** :
   L'API sera disponible par défaut à l'adresse suivante :
   ```
   http://localhost:8080
   ```

---

## **API**

Le backend expose une API REST pour le frontend. Voici un exemple d'utilisation :

### **POST /compile**

- **Description** : Compile et exécute un code Java fourni.
- **Request Body** :
  ```json
  {
    "code": "public class Main { public static void main(String[] args) { System.out.println(\"Hello, World!\"); }}"
  }
  ```
- **Response** :
  ```json
  {
    "output": "Hello, World!"
  }
  ```

---

## **Structure du Projet**

```
src/
├── main/
│   ├── java/
│   │   ├── com.example.javaonlinecompiler/   # Paquet principal de l'application
│   │   ├── controller/                      # Contrôleurs REST
│   │   ├── service/                         # Services métiers
│   └── resources/
│       ├── application.properties           # Configuration Spring Boot
```

---

## **Technologies Utilisées**

- **Spring Boot** : Framework pour construire le backend.
- **Java Compiler API** : Pour la compilation dynamique de code.
- **Spring Web** : Gestion des requêtes HTTP.
- **Spring CORS** : Configuration pour autoriser les requêtes cross-origin.

---

## **Problèmes Connus**

- **CORS** : Assurez-vous que les origines autorisées dans le backend correspondent à l'URL de votre frontend (par défaut : `http://localhost:4200`).

---

## **Contributions**

Les contributions sont les bienvenues ! Si vous souhaitez contribuer :

1. **Forkez le dépôt**.
2. **Créez une branche pour votre fonctionnalité**.
3. **Soumettez une pull request** avec des modifications détaillées.

---

## **Licence**

Ce projet est sous licence **MIT License**. Consultez le fichier LICENSE pour plus de détails.

---

🚀 Fournissez une expérience de compilation Java fluide avec **Java Online Compiler Backend** dès aujourd'hui !
