# ChriOnline — Application E-Commerce Java

Application e-commerce client-serveur native en Java (sockets TCP/UDP) avec interface JavaFX et base de données PostgreSQL.

## Structure
- `chrionline-common` — classes partagées (Message, Protocol, AppConstants)
- `chrionline-server` — serveur multi-clients TCP, logique métier, accès BDD
- `chrionline-client` — interface JavaFX, communication TCP avec le serveur

## Lancer le projet
```bash
# 1. Compiler
mvn clean install

# 2. Démarrer le serveur
java -jar chrionline-server/target/chrionline-server-1.0-SNAPSHOT.jar

# 3. Démarrer le client
java -jar chrionline-client/target/chrionline-client-1.0-SNAPSHOT.jar
```

## Configuration BDD
Modifier `chrionline-server/src/main/resources/db.properties` avec vos identifiants PostgreSQL.

## Technologies
- Java 17 · Sockets TCP/UDP · JavaFX · PostgreSQL · JDBC · Maven
