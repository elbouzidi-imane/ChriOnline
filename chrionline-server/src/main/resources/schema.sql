-- ============================================================
--  BASE DE DONNÉES PostgreSQL — ChriOnline
-- ============================================================

-- Utilisateur
CREATE TABLE utilisateur (
                             id               SERIAL PRIMARY KEY,
                             nom              VARCHAR(100) NOT NULL,
                             prenom           VARCHAR(100) NOT NULL,
                             email            VARCHAR(150) UNIQUE NOT NULL,
                             mot_de_passe     VARCHAR(255) NOT NULL,
                             telephone        VARCHAR(20),
                             adresse          TEXT,
                             role             VARCHAR(20) NOT NULL DEFAULT 'CLIENT'
                                 CHECK (role IN ('CLIENT', 'ADMIN')),
                             statut           VARCHAR(20) NOT NULL DEFAULT 'ACTIF'
                                 CHECK (statut IN ('ACTIF', 'SUSPENDU')),
                             date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             date_naissance   DATE,
                             notifications_activees BOOLEAN NOT NULL DEFAULT FALSE
);

-- Categorie
CREATE TABLE categorie (
                           id          SERIAL PRIMARY KEY,
                           nom         VARCHAR(100) UNIQUE NOT NULL,
                           description TEXT
);

-- Produit
CREATE TABLE produit (
                         id               SERIAL PRIMARY KEY,
                         categorie_id     INT NOT NULL REFERENCES categorie(id),
                         nom              VARCHAR(200) NOT NULL,
                         description      TEXT,
                         matiere          VARCHAR(100),
                         couleur          VARCHAR(50),
                         prix_original    NUMERIC(10, 2) NOT NULL,
                         prix_reduit      NUMERIC(10, 2),
                         stock            INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
                         statut           VARCHAR(20) NOT NULL DEFAULT 'ACTIF'
                             CHECK (statut IN ('ACTIF', 'INACTIF')),
                         nombre_ventes    INT NOT NULL DEFAULT 0,
                         date_debut_vente DATE,
                         image_url        VARCHAR(500)
);

-- Taille produit
CREATE TABLE taille_produit (
                                id         SERIAL PRIMARY KEY,
                                produit_id INT NOT NULL REFERENCES produit(id) ON DELETE CASCADE,
                                valeur     VARCHAR(10) NOT NULL,
                                stock      INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
                                UNIQUE (produit_id, valeur)
);

-- Trigger : sync stock produit
CREATE OR REPLACE FUNCTION sync_stock_produit()
RETURNS TRIGGER AS $$
BEGIN
UPDATE produit
SET stock = (
    SELECT COALESCE(SUM(stock), 0)
    FROM taille_produit
    WHERE produit_id = NEW.produit_id
)
WHERE id = NEW.produit_id;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_sync_stock
    AFTER INSERT OR UPDATE OF stock ON taille_produit
    FOR EACH ROW EXECUTE FUNCTION sync_stock_produit();

-- Guide de taille
CREATE TABLE guide_taille (
                              id         SERIAL PRIMARY KEY,
                              produit_id INT NOT NULL REFERENCES produit(id) ON DELETE CASCADE,
                              taille     VARCHAR(10) NOT NULL,
                              poitrine   VARCHAR(20),
                              taille_cm  VARCHAR(20),
                              hanches    VARCHAR(20),
                              UNIQUE (produit_id, taille)
);

-- Panier
CREATE TABLE panier (
                        id             SERIAL PRIMARY KEY,
                        utilisateur_id INT UNIQUE NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
                        date_creation  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ligne panier
CREATE TABLE ligne_panier (
                              id                SERIAL PRIMARY KEY,
                              panier_id         INT NOT NULL REFERENCES panier(id) ON DELETE CASCADE,
                              produit_id        INT NOT NULL REFERENCES produit(id) ON DELETE CASCADE,
                              taille_produit_id INT NOT NULL REFERENCES taille_produit(id) ON DELETE CASCADE,
                              quantite          INT NOT NULL DEFAULT 1 CHECK (quantite > 0),
                              prix_unitaire     NUMERIC(10, 2) NOT NULL,
                              UNIQUE (panier_id, taille_produit_id)
);

-- Commande
CREATE TABLE commande (
                          id                SERIAL PRIMARY KEY,
                          reference         VARCHAR(50) UNIQUE NOT NULL,
                          utilisateur_id    INT NOT NULL REFERENCES utilisateur(id),
                          date_commande     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          statut            VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
                              CHECK (statut IN ('EN_ATTENTE','VALIDEE','EXPEDIEE','LIVREE','ANNULEE')),
                          montant_total     NUMERIC(10, 2) NOT NULL,
                          adresse_livraison TEXT NOT NULL
);

-- Ligne commande
CREATE TABLE ligne_commande (
                                id                SERIAL PRIMARY KEY,
                                commande_id       INT NOT NULL REFERENCES commande(id) ON DELETE CASCADE,
                                produit_id        INT NOT NULL REFERENCES produit(id),
                                taille_produit_id INT NOT NULL REFERENCES taille_produit(id),
                                quantite          INT NOT NULL CHECK (quantite > 0),
                                prix_unitaire     NUMERIC(10, 2) NOT NULL
);

-- Paiement
CREATE TABLE paiement (
                          id            SERIAL PRIMARY KEY,
                          commande_id   INT UNIQUE NOT NULL REFERENCES commande(id) ON DELETE CASCADE,
                          montant       NUMERIC(10, 2) NOT NULL,
                          mode_paiement VARCHAR(50) NOT NULL
                              CHECK (mode_paiement IN ('CARTE_BANCAIRE','PAYPAL','VIREMENT','FICTIF')),
                          statut        VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
                              CHECK (statut IN ('EN_ATTENTE','VALIDE','REFUSE','REMBOURSE')),
                          date_paiement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          reference     VARCHAR(100) UNIQUE
);

-- Livraison
CREATE TABLE livraison (
                           id             SERIAL PRIMARY KEY,
                           commande_id    INT UNIQUE NOT NULL REFERENCES commande(id) ON DELETE CASCADE,
                           mode_livraison VARCHAR(50) NOT NULL
                               CHECK (mode_livraison IN ('STANDARD','EXPRESS','POINT_RELAIS')),
                           statut         VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
                               CHECK (statut IN ('EN_ATTENTE','EN_COURS','EXPEDIEE','LIVREE')),
                           date_estimee   DATE,
                           date_effective DATE,
                           suivi_actif    BOOLEAN DEFAULT FALSE
);

-- ============================================================
--  DONNÉES DE TEST
-- ============================================================

INSERT INTO categorie (nom, description) VALUES
                                             ('Femmes',  'Vêtements pour femmes'),
                                             ('Hommes',  'Vêtements pour hommes'),
                                             ('Enfants', 'Vêtements pour enfants'),
                                             ('Sport',   'Tenues de sport');

INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, telephone, role) VALUES
    ('Admin', 'ChriOnline', 'admin@chrionline.com', 'hashed_password_here', '0600000000', 'ADMIN');

INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, telephone, adresse) VALUES
    ('Dupont', 'Jean', 'jean@email.com', 'hashed_password_here', '0611111111', '12 Rue de Paris, 75001 Paris');

INSERT INTO produit (categorie_id, nom, description, matiere, couleur, prix_original, prix_reduit, date_debut_vente) VALUES
    (2, 'T-Shirt Classic', 'T-shirt coupe droite', 'Coton 100%', 'Blanc', 29.99, 19.99, CURRENT_DATE);

INSERT INTO taille_produit (produit_id, valeur, stock) VALUES
                                                           (1, 'S',  15),
                                                           (1, 'M',  30),
                                                           (1, 'L',  25),
                                                           (1, 'XL', 10);

INSERT INTO guide_taille (produit_id, taille, poitrine, taille_cm, hanches) VALUES
                                                                                (1, 'S',  '82-86 cm',   '62-66 cm', '88-92 cm'),
                                                                                (1, 'M',  '90-94 cm',   '70-74 cm', '96-100 cm'),
                                                                                (1, 'L',  '98-102 cm',  '78-82 cm', '104-108 cm'),
                                                                                (1, 'XL', '106-110 cm', '86-90 cm', '112-116 cm');
