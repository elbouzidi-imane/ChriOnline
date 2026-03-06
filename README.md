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


