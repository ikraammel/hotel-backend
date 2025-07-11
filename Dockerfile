# Étape 1 : build avec Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app                    # <-- Ajoute cette ligne
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : image d'exécution
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app                   # <-- Ajoute aussi ici pour la cohérence
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
