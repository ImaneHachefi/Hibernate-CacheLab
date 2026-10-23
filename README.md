# TP – Hibernate / JPA : problème N+1 et cache de second niveau

## Objectif
Mesurer l'impact de différentes stratégies de lecture sur le nombre de requêtes SQL et le temps d'exécution :
1. Requête naïve (problème N+1)
2. `JOIN FETCH`
3. Entity Graph
4. Cache de second niveau (Ehcache 3 via JCache) + cache de requêtes

## Technologies
- Java 17, Maven
- Hibernate 5.4.32.Final (JPA 2.2) + `hibernate-jcache`
- Ehcache 3.8.1
- PostgreSQL 16 (Docker)
- DBeaver pour consulter la base

## Structure du projet
```
hibernate-cache-lab
├── docker-compose.yml
├── pom.xml
└── src/main
    ├── java/com/example
    │   ├── Lanceur.java
    │   ├── model/Ecrivain.java
    │   ├── model/Ouvrage.java
    │   └── service/
    │       ├── JeuDonneesService.java
    │       └── BenchmarkService.java
    └── resources
        ├── ehcache.xml
        └── META-INF/persistence.xml
```

## Modèle
Relation bidirectionnelle `Ecrivain` 1 ── N `Ouvrage` (`@OneToMany` / `@ManyToOne(LAZY)`).
Jeu de données : 10 écrivains × 5 ouvrages = 50 ouvrages.

## Lancement

### 1. Démarrer la base PostgreSQL
```bash
docker compose up -d
docker ps
```
<img width="1915" height="563" alt="Capture d&#39;écran 2026-10-23 112732" src="https://github.com/user-attachments/assets/953915b7-e2ef-4937-899d-d0529b260474" />

### 2. Connexion avec DBeaver
| Paramètre | Valeur |
|---|---|
| Host | localhost |
| Port | 5432 |
| Database | cachelab |
| Username | lab |
| Password | lab |

<img width="661" height="793" alt="Capture d&#39;écran 2026-10-23 113643" src="https://github.com/user-attachments/assets/9b5ad998-d8ed-4c6b-a6c6-24af3deffb28" />

### 3. Exécuter l'application
```bash
mvn clean compile exec:java
```
(ou lancer `Lanceur.main()` depuis l'IDE)

<img width="1781" height="893" alt="image" src="https://github.com/user-attachments/assets/b8c02464-0cc0-4d77-bcdc-706e5d44a35d" />

### 4. Vérifier les données dans DBeaver
```sql
SELECT * FROM ecrivains;
SELECT * FROM ouvrages;
```
<img width="1194" height="803" alt="Capture d&#39;écran 2026-10-23 114513" src="https://github.com/user-attachments/assets/22238e31-31a0-4a0d-be05-e5afa9783f16" />
<img width="1598" height="1079" alt="Capture d&#39;écran 2026-10-23 114527" src="https://github.com/user-attachments/assets/f73a45c3-12fc-41fa-a58f-c136aad239a9" />

## Configuration du cache
- `persistence.xml` : `hibernate.cache.use_second_level_cache=true`, `use_query_cache=true`, `JCacheRegionFactory`, `EhcachePro­vider`.
- `ehcache.xml` : régions `Ecrivain`, `Ouvrage`, `Ecrivain.ouvrages` (collection), `default-query-results-region`, `default-update-timestamps-region`.
- Entités et collection annotées `@Cache(usage = READ_WRITE)`.

## Résultats

<img width="847" height="715" alt="Capture d&#39;écran 2026-10-23 113741" src="https://github.com/user-attachments/assets/09789389-9542-40d6-9378-2dbe5e116cfa" />
<img width="799" height="737" alt="Capture d&#39;écran 2026-10-23 113807" src="https://github.com/user-attachments/assets/e01ac27e-e12e-4625-90a1-f868e12b63d2" />
<img width="1051" height="685" alt="Capture d&#39;écran 2026-10-23 113837" src="https://github.com/user-attachments/assets/8f559b06-02ac-4024-bd05-51863b035394" />
<img width="628" height="279" alt="Capture d&#39;écran 2026-10-23 113855" src="https://github.com/user-attachments/assets/bbff81a9-b213-4f27-aa4b-76d2529055d5" />
<img width="665" height="466" alt="Capture d&#39;écran 2026-10-23 113905" src="https://github.com/user-attachments/assets/a02fa303-eb55-4bd3-a20e-174bae6cc772" />
<img width="659" height="361" alt="Capture d&#39;écran 2026-10-23 113915" src="https://github.com/user-attachments/assets/b7c42c61-e65f-4450-8586-fe84e2b846c6" />
<img width="698" height="574" alt="Capture d&#39;écran 2026-10-23 113936" src="https://github.com/user-attachments/assets/38aa1efc-fc66-4287-9857-e60a69cdd721" />
<img width="803" height="348" alt="Capture d&#39;écran 2026-10-23 113950" src="https://github.com/user-attachments/assets/ded8fe3b-9419-41d5-b478-b310f8153fd0" />

## Analyse
- **N+1** : 1 requête pour les écrivains + 10 requêtes (une par écrivain) pour charger leurs ouvrages, soit 11 requêtes.
- **JOIN FETCH / Entity Graph** : une seule requête SQL avec jointure. `DISTINCT` évite les doublons d'écrivains.
- **Cache de second niveau** : à la 1ʳᵉ lecture le cache se remplit (miss + puts). À la 2ᵉ lecture, la requête, les entités et la collection viennent du cache : 0 requête SQL attendue.
- Le cache de collections doit être activé explicitement (`@Cache` sur le champ `ouvrages` + région dédiée dans `ehcache.xml`).

## Arrêt
```bash
docker compose down        # garde les données
docker compose down -v     # supprime aussi le volume
```
