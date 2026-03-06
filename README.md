🛍️ ChriOnline
Application E-Commerce Java
Architecture Client-Serveur TCP/UDP
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Module : SSI-AT  |  UAE-ENSATé  |  2026
Deadline : 27 Mars 2026
1. Description du Projet

ChriOnline est une application e-commerce  développée entièrement en Java. Elle repose sur une architecture client-serveur native utilisant les sockets TCP/UDP, permettant une communication réseau robuste et en temps réel.

L'application permet aux utilisateurs de parcourir un catalogue de produits, gérer leur panier, valider des commandes et effectuer des paiements simulés, le tout depuis une interface graphique moderne développée avec JavaFX.

Objectifs du projet
▸  Développer une application e-commerce complète de bout en bout
▸  Mettre en pratique la programmation réseau avec les sockets Java
▸  Implémenter une architecture client-serveur multi-clients avec threads
▸  Persister les données dans une base de données MySQL via JDBC
▸  Concevoir une interface graphique ergonomique avec JavaFX

Niveaux d'implémentation
▸  Fonctionnalités de base sans interface graphique avancéeNiveau Minimum : 
▸  Interface graphique complète, BDD, UML, gestion avancéeNiveau Avancé : 

2. Fonctionnalités

  👤 Espace Utilisateur

Fonctionnalité	Description	Niveau
Inscription	Créer un compte avec email et mot de passe	Minimum
Connexion	Authentification sécurisée avec token de session	Minimum
Catalogue produits	Consulter la liste des produits disponibles	Minimum
Détail produit	Voir nom, prix, description, stock d'un produit	Minimum
Panier	Ajouter, modifier, supprimer des produits	Minimum
Calcul total	Calcul automatique du total du panier	Minimum
Commande	Valider une commande avec adresse de livraison	Minimum
Paiement simulé	Simulation d'un paiement par carte bancaire	Minimum
Historique	Consulter l'historique des commandes passées	Avancé
Profil	Modifier ses informations personnelles	Avancé
Notifications	Recevoir des alertes sur l'état des commandes (UDP)	Avancé
Catégories	Filtrer les produits par catégorie	Avancé

  👑 Espace Administrateur

Fonctionnalité	Description
Gestion produits	Ajouter, modifier, supprimer des produits
Gestion stock	Mise à jour automatique du stock après achat
Gestion commandes	Voir et changer le statut des commandes
Gestion utilisateurs	Lister et gérer les comptes utilisateurs
Gestion catégories	Créer et organiser les catégories de produits
Tableau de bord	Interface admin complète et ergonomique

  📦 Statuts de Commande

▸   → Commande créée, en attente de validationEN_ATTENTE
▸   → Commande confirmée et paiement acceptéVALIDEE
▸   → Commande en cours de livraisonEXPEDIEE
▸   → Commande reçue par le clientLIVREE

3. Technologies Utilisées

Technologie	Version	Usage dans le projet
Java	17+	Langage principal — Client et Serveur
JavaFX	17+	Interface graphique desktop côté client
Scene Builder	Latest	Design visuel des fichiers FXML
MySQL	8+	Base de données relationnelle
JDBC	Inclus Java	Connexion Java ↔ MySQL côté serveur
TCP Sockets	Java.net	Communication principale Client ↔ Serveur
UDP Sockets	Java.net	Envoi de notifications en temps réel
GSON	2.10+	Sérialisation / désérialisation JSON
BCrypt	Latest	Hashage sécurisé des mots de passe
Git / GitHub	Latest	Gestion de versions et collaboration
Maven / Gradle	Latest	Gestion des dépendances Java

4. Architecture du Projet

Le projet suit une architecture MVC (Model-View-Controller) combinée au pattern DAO (Data Access Object), répartie entre deux parties indépendantes : le Serveur et le Client.

  🌐 Schéma Global

┌─────────────────────────────────────────────────────────┐ │                    SERVEUR (Membre 1)                    │ │                                                          │ │   TCPServer :8080          UDPNotifier :9090             │ │        │                        │                        │ │   ClientHandler (Thread)         │                        │ │        │                        │                        │ │   AuthService  ProduitService  PanierService             │ │        │                                                  │ │   UserDAO  ProduitDAO  PanierDAO  CommandeDAO            │ │        │                                                  │ │            MySQL Database                                 │ └─────────────────────────────────────────────────────────┘          ▲  TCP :8080              UDP :9090  ▼              ┌─────────────────────────────────────────────────────────┐ │                    CLIENT (Membre 2)                     │ │                                                          │ │   ServerConnection (TCP)    UDPListener (UDP)            │ │        │                        │                        │ │   LoginController  ProduitsController  PanierController  │ │        │                                                  │ │   login.fxml  produits.fxml  panier.fxml  admin.fxml     │ │                                                          │ │                Interface JavaFX                          │ └─────────────────────────────────────────────────────────┘

  📁 Structure des Dossiers

🖥️ server/ — Membre 1 (Backend)

server/ ├── Server.java                  ← Point d'entrée du serveur ├── network/ │   ├── TCPServer.java           ← ServerSocket, écoute port 8080 │   ├── ClientHandler.java       ← Thread dédié à chaque client │   └── UDPNotifier.java         ← Envoi notifications UDP ├── model/ │   ├── User.java               ← id, nom, email, password, role │   ├── Produit.java            ← id, nom, prix, stock, categorie │   ├── Categorie.java          ← id, nom │   ├── Panier.java             ← id, userId, liste items │   ├── PanierItem.java         ← produit + quantite │   ├── Commande.java           ← id, userId, total, statut │   ├── CommandeItem.java       ← produit, quantite, prix │   └── Paiement.java           ← id, commandeId, method ├── dao/ │   ├── UserDAO.java            ← INSERT, SELECT, UPDATE users │   ├── ProduitDAO.java         ← SELECT, UPDATE produits │   ├── CategorieDAO.java       ← SELECT categories │   ├── PanierDAO.java          ← INSERT, DELETE panier │   ├── CommandeDAO.java        ← INSERT, SELECT commandes │   └── PaiementDAO.java        ← INSERT paiements ├── service/ │   ├── AuthService.java        ← Login, Register, Token │   ├── ProduitService.java     ← Logique produits et stock │   ├── PanierService.java      ← Logique panier │   ├── CommandeService.java    ← Logique commandes │   └── AdminService.java       ← Logique administration ├── database/ │   └── DatabaseConnection.java ← Connexion MySQL via JDBC └── util/     ├── JsonUtil.java           ← Parser JSON avec GSON     ├── PasswordUtil.java       ← Hashage BCrypt     └── TokenUtil.java          ← Génération tokens session

💻 client/ — Membre 2 (Frontend)

client/ ├── Main.java                        ← Point d'entrée JavaFX ├── network/ │   ├── ServerConnection.java        ← Socket TCP vers serveur │   └── UDPListener.java             ← Réception notifications ├── model/ │   ├── User.java                    ← Modèle utilisateur │   ├── Produit.java                 ← Modèle produit │   ├── PanierItem.java              ← Modèle item panier │   └── Commande.java                ← Modèle commande ├── controller/ │   ├── LoginController.java         ← Logique écran connexion │   ├── RegisterController.java      ← Logique inscription │   ├── ProduitsController.java      ← Logique catalogue │   ├── ProduitDetailController.java ← Logique détail produit │   ├── PanierController.java        ← Logique panier │   ├── CommandeController.java      ← Logique commande │   ├── PaiementController.java      ← Logique paiement │   ├── ProfilController.java        ← Logique profil │   ├── HistoriqueController.java    ← Logique historique │   └── AdminController.java         ← Logique dashboard admin ├── view/ │   ├── login.fxml                   ← Écran connexion │   ├── register.fxml                ← Écran inscription │   ├── produits.fxml                ← Catalogue produits │   ├── produit_detail.fxml          ← Détail d'un produit │   ├── panier.fxml                  ← Écran panier │   ├── commande.fxml                ← Validation commande │   ├── paiement.fxml                ← Écran paiement │   ├── profil.fxml                  ← Profil utilisateur │   ├── historique.fxml              ← Historique commandes │   └── admin.fxml                   ← Dashboard admin └── util/     └── JsonUtil.java                ← Parser JSON avec GSON

🔗 common/ — Partagé par les deux membres

common/ ├── Actions.java    ← Constantes des actions JSON (LOGIN, REGISTER...) └── Config.java     ← Configuration (IP serveur, ports TCP/UDP)

🗄️ database/ — Script MySQL

database/ └── chrionline.sql  ← Script de création de toutes les tables MySQL

5. Base de Données MySQL

Table	Colonnes principales	Description
users	id, nom, email, password, telephone, role	Comptes utilisateurs
categories	id, nom	Catégories de produits
produits	id, nom, description, prix, stock, categorie_id	Catalogue produits
paniers	id, user_id, date_creation	Paniers utilisateurs
panier_items	id, panier_id, produit_id, quantite	Produits dans panier
commandes	id, user_id, total, statut, adresse, date	Commandes validées
commande_items	id, commande_id, produit_id, quantite, prix	Détail commandes
paiements	id, commande_id, method, statut, date	Paiements simulés
sessions	id, user_id, token, date_expiration	Tokens de session

6. Protocole de Communication JSON

Toute communication entre le Client et le Serveur utilise le format JSON via TCP. C'est le contrat entre les deux membres de l'équipe.

Format général
// Requête : Client → Serveur { "action": "NOM_ACTION", "token": "abc123", "data": { ... } }  // Réponse : Serveur → Client { "status": "OK" | "ERROR", "message": "...", "data": { ... } }

Liste complète des actions

Action	Description	Token requis
LOGIN	Authentification utilisateur	Non
REGISTER	Création d'un compte	Non
LOGOUT	Déconnexion	Oui
GET_PRODUCTS	Liste des produits	Oui
GET_PRODUCT	Détail d'un produit	Oui
GET_CATEGORIES	Liste des catégories	Oui
ADD_TO_CART	Ajouter au panier	Oui
GET_CART	Voir le panier	Oui
UPDATE_CART	Modifier quantité	Oui
REMOVE_FROM_CART	Supprimer du panier	Oui
CLEAR_CART	Vider le panier	Oui
PLACE_ORDER	Valider commande	Oui
PAY	Payer la commande	Oui
GET_ORDERS	Historique commandes	Oui
GET_ORDER_DETAIL	Détail d'une commande	Oui
GET_PROFILE	Voir profil	Oui
UPDATE_PROFILE	Modifier profil	Oui
ADMIN_ADD_PRODUCT	Ajouter produit (admin)	Oui
ADMIN_UPDATE_PRODUCT	Modifier produit (admin)	Oui
ADMIN_DELETE_PRODUCT	Supprimer produit (admin)	Oui
ADMIN_UPDATE_ORDER	Changer statut commande	Oui
ADMIN_GET_USERS	Liste utilisateurs (admin)	Oui

7. Répartition des Tâches

	Membre 1 — Serveur	Membre 2 — Client
Dossier	server/	client/
Technologie principale	Java Sockets, JDBC, MySQL	JavaFX, FXML, Sockets
Base de données	✅ Crée et gère MySQL	❌ Pas d'accès direct
Interface graphique	❌ Aucune	✅ Tous les écrans FXML
TCP	Reçoit et traite requêtes	Envoie requêtes, affiche réponses
UDP	Envoie notifications	Reçoit et affiche notifications
Logique métier	✅ Services complets	❌ Délégué au serveur
Branche Git	server	client

8. Planning de Développement

Semaine	Dates	Membre 1 (Serveur)	Membre 2 (Client)
S1	→ 13 mars	BDD MySQL + Auth + TCPServer + DAOs	Projet JavaFX + Login + Register
S2	→ 20 mars	Produits + Panier + Commandes Services	Écrans Produits + Panier + Commande
S3	→ 25 mars	Admin + UDP + Tests Serveur	Paiement + Profil + Admin + UDP
S4	27 mars	Intégration + Correction bugs	Intégration + Présentation

9. Organisation Git

main     ← Version finale stable (merge final) ├── server  ← Membre 1 développe et push ici └── client  ← Membre 2 développe et push ici

Workflow quotidien
# Récupérer les mises à jour git pull origin main  # Après avoir codé (Membre 1) git add server/ git commit -m "Description de la tâche" git push origin server  # Après avoir codé (Membre 2) git add client/ git commit -m "Description de la tâche" git push origin client

10. Configuration Réseau

Paramètre	Valeur	Description
Adresse serveur	localhost	En développement local
Port TCP	8080	Communication principale
Port UDP	9090	Notifications temps réel
BDD Host	localhost:3306	MySQL local
BDD Name	chrionline	Nom de la base de données


UAE-ENSA
