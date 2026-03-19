\# ChriOnline — Application E-Commerce Java



Application e-commerce client-serveur native en Java (sockets TCP/UDP) avec interface JavaFX et base de données PostgreSQL.



\---



\## Structure



\- `chrionline-common` — classes partagées (Message, Protocol, AppConstants)

\- `chrionline-server` — serveur multi-clients TCP, logique métier, accès BDD

\- `chrionline-client` — interface JavaFX, communication TCP avec le serveur



\---



\## Lancer le projet



```bash

\# 1. Compiler

mvn clean install



\# 2. Démarrer le serveur

java -jar chrionline-server/target/chrionline-server-1.0-SNAPSHOT.jar



\# 3. Démarrer le client

java -jar chrionline-client/target/chrionline-client-1.0-SNAPSHOT.jar

```



\---



\## Configuration BDD



Modifier `chrionline-server/src/main/resources/db.properties` avec vos identifiants PostgreSQL.



```properties

db.url=jdbc:postgresql://localhost:5432/chrionline

db.user=postgres

db.password=votre\_mot\_de\_passe

```



> ⚠️ Ce fichier est ignoré par Git — ne jamais le commiter.



\---



\## Configuration Email (OTP)



Modifier `chrionline-server/src/main/resources/mail.properties` :



```properties

mail.from=votre.email@gmail.com

mail.password=votre\_app\_password\_gmail

mail.host=smtp.gmail.com

mail.port=587

```



> ⚠️ Utiliser un \*\*App Password Gmail\*\* (pas le mot de passe du compte).

> ⚠️ Ce fichier est ignoré par Git — ne jamais le commiter.



\---



\## Migrations Base de Données



Les scripts suivants doivent être exécutés \*\*dans l'ordre\*\* sur votre base PostgreSQL.



\### Migration 1 — Schéma initial



Exécuter le fichier :

```

chrionline-server/src/main/resources/schema.sql

```



\### Migration 2 — Ajouts AuthHandler (OTP + statuts)



```sql

\-- 1. Ajouter la colonne date\_naissance

ALTER TABLE utilisateur

ADD COLUMN date\_naissance DATE;



\-- 2. Créer la table des codes OTP

CREATE TABLE otp\_code (

&#x20;   id         SERIAL PRIMARY KEY,

&#x20;   email      VARCHAR(150) NOT NULL,

&#x20;   code       VARCHAR(6)   NOT NULL,

&#x20;   type       VARCHAR(30)  NOT NULL, -- INSCRIPTION ou MOT\_DE\_PASSE

&#x20;   expire\_at  TIMESTAMP    NOT NULL,

&#x20;   utilise    BOOLEAN      DEFAULT FALSE,

&#x20;   created\_at TIMESTAMP    DEFAULT CURRENT\_TIMESTAMP

);



\-- 3. Mettre à jour la contrainte de statut utilisateur

ALTER TABLE utilisateur

DROP CONSTRAINT utilisateur\_statut\_check;



ALTER TABLE utilisateur

ADD CONSTRAINT utilisateur\_statut\_check

CHECK (statut IN ('ACTIF', 'SUSPENDU', 'INACTIF', 'EN\_ATTENTE'));

```



\---



\## Fonctionnalités implémentées



\### ✅ Authentification (AuthHandler)

\- Inscription avec vérification email (OTP)

\- Connexion / Déconnexion

\- Mot de passe oublié (OTP par email)

\- Réinitialisation du mot de passe

\- Mise à jour du profil

\- Désactivation / Suppression de compte



\### 🔄 En cours

\- Gestion des produits (ProductHandler)

\- Gestion du panier (CartHandler)

\- Gestion des commandes (OrderHandler)

\- Interface admin (AdminHandler)



\---



\## Technologies



\- Java 17 · Sockets TCP/UDP · JavaFX · PostgreSQL · JDBC · Maven

\- BCrypt (hashage mots de passe) · Gson (JSON) · JavaMail (emails)

