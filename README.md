
# Mini SGBDR – Projet BDDA  

> **TP Bases de Données Avancées** – Implémentation d’un Mini-SGBDR (Système de Gestion de Bases de Données Relationnelles) simplifié.

---

## Objectif du projet  

L’objectif est de développer pas à pas un **Mini-SGBDR** en console :  
- **Mono-utilisateur** (pas de concurrence, pas de transactions).  
- **Langage simplifié** proche du SQL (insertion, sélection, jointure, etc.).  
- **Commandes de debug** pour explorer les données.  
- **Application console uniquement** (une GUI peut être ajoutée, mais non évaluée).  

---

## Structure du projet  

```
PROJET_BDDA/
├─ config/
│  └─ dbconfig.properties           # contient les paramètres modulables de la base de données
├─ scripts/
│  ├─ build_run.bat                 # compile + lance Main (Windows)
│  ├─ build_run.sh                  # compile + lance Main (Linux/Mac)
│  ├─ test.bat                      # compile + lance les tests (Windows)
│  └─ test.sh                       # compile + lance les tests (Linux/Mac)
├─ src/
│  └─ bdda/
│     ├─ config/
│     |  ├─ BufferManager.java      # Gestion du Buffer Manager
│     |  ├─ Condition.java          # Gestion des conditions des commandes SELECT, DELETE et UPDATE
│     |  ├─ DBConfig.java           # Gestion de la configuration de la BD
│     |  ├─ DBManager.java          # Gestion de la BD
│     |  ├─ DiskManager.java        # Gestion du Disk Manager
│     |  ├─ InfoColonne.java        # Gestion des colonnes des relations
│     |  ├─ IRecordIterator.java    # Iterator pour parcourir les tuples
│     |  ├─ PageID.java             # Identifiant des pages
│     |  ├─ ProjectOperator.java    # Opérateur logique pour projeter les tuples
│     |  ├─ Record.java             # Gestion des tuples
│     |  ├─ RecordID.java           # Identifiant des tuples
│     |  ├─ RecordPrinter.java      # Affichage des tuples
│     |  ├─ Relation.java           # Gestion des relations
│     │  ├─ RelationScanner.java    # Parcours des relations
│     │  ├─ SelectOperator.java     # Opérateur logique pour sélectionner les tuples
│     │  └─ SGBD.java               # Gestion du SGBD
│     └─ test/
│        ├─ BufferManagerTests.java # Tests simples sur le Buffer Manager
│        ├─ DiskManagerTests.java   # Tests simples sur le Disk Manager
│        ├─ RelationTest.java       # Tests simples sur la gestion des relations
│        ├─ SGBDTests.java          # Tests simples sur le SGBD
│        └─ TestDBConfig.java       # Tests simples sur la configuration de la base de données
├─ .gitignore
└─ README.md
```

---

## Fonctionnalités actuelles

Création d'un mini-SGBDR avec les commandes intégrés :
- **CREATE TABLE** : Crée une relation
- **DESCRIBE TABLE** : Affiche une relation
- **DESCRIBE TABLES** : Affiche toutes les relations
- **DROP TABLE** : Supprime une relation
- **DROP TABLES** : Supprimes toutes les relations

- **INSERT INTO** : Ajoute un tuple à une relation
- **APPEND INTO** : Ajoute les tuples d'un fichier .csv à une relation
- **SELECT FROM WHERE** : Affiche les tupples correspondants aux conditions cités (optionnel) d'une relation
- **DELETE FROM WHERE** : Supprime les tupples correspondants aux conditions cités (optionnel) d'une relation
- **UPDATE FROM WHERE** : Actualise les tuples correspondants aux conditions cités (optionnel) d'une relation

- **EXIT** : Quitte le programme

## ▶Compilation & exécution

### 🔹 Linux / MacOS
```bash
./scripts/build_run.sh
./scripts/build_test.sh
```

### 🔹 Windows
```bat
scripts\build_run.bat
scripts\build_tests.bat
```

---

## Tests inclus

Lors de l’exécution du script de test, les tests de `BufferManagerTests`, `DiskManagerTests`, `RelationTests` ,`SGBDTests` et `TestDBConfig` s’affichent automatiquement. 


## Technologies utilisées

- **Langage** : Java (≥ 11)  
- **Gestion de version** : Git + GitHub  
- **Organisation** : Branches par fonctionnalité + Pull Requests  

---

## Équipe

- **Rayan DEFOOR**  
- **Ronan LALLOUET**  
- **Anne-Louis VOJINOVIC**
- **Rayan MOUAKKILI**
- **Jordan CRISOTO**
---

