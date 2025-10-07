
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
├─ src/
│  └─ bdda/
│     ├─ Main.java                  # Point d'entrée console (prompt, EXIT)
│     ├─ config/
│     │  ├─ DBConfig.java           # Lecture config (dbpath) depuis .properties
│     │  ├─ DiskManager.java        # Gère toutes les opérations sur le disque pour le SGBD
│     │  └─ PageID.java             # Représente l’identifiant unique d’une page dans le SGBD
│     ├─ test/
│     │  ├─ DiskManagerTests.java   # Tests simples sans framework
│     │  └─ TestDBConfig.java       # Tests simples sans framework
│     └─ util/
│        └─ IO.java                 # Placeholder utilitaires
├─ config/
│  └─ dbconfig.properties           # dbpath=./DB
├─ DB/
│  └─ .gitkeep                      # pour versionner le dossier vide
├─ scripts/
│  ├─ build_run.sh                  # compile + lance Main (Linux/Mac)
│  ├─ build_run.bat                 # compile + lance Main (Windows)
│  ├─ test.sh                       # compile + lance les tests (Linux/Mac)
│  └─ test.bat                      # compile + lance les tests (Windows)
├─ .gitignore
└─ README.md
```

---

## Fonctionnalités actuelles (TP1)

Gestion d’une configuration de base via la classe `DBConfig` :  
- Stocke le chemin `dbpath` vers le dossier de données.  
- Peut être construite **directement** ou via un **fichier texte** (`.properties`).  
- Tests simples inclus (`TestDBConfig`).  

Exemple de fichier `config/dbconfig.properties` :  
```properties
dbpath=../DB
```

---

## ▶Compilation & exécution

### 🔹 Linux / MacOS
```bash
./scripts/build_run.sh
```

### 🔹 Windows
```bat
scripts\build_run.bat
```

---

## Tests inclus

Lors de l’exécution, les tests de `TestDBConfig` s’affichent automatiquement :  

```
[OK] Constructeur direct : DBConfig{dbpath='../DB'}
[OK] LoadDBConfig : DBConfig{dbpath='../DB'}
[OK] Rejet dbpath vide
[OK] Rejet fichier inexistant
```

---

## Technologies utilisées

- **Langage** : Java (≥ 11)  
- **Gestion de version** : Git + GitHub  
- **Organisation** : Branches par fonctionnalité + Pull Requests  

---

## Équipe

- **Rayan**  
- **Ronan**  
- **Anne-Louis**
- **Rayan Mouakkili**
- **Jordan**
---

