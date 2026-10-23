package com.example.service;

import com.example.model.Ecrivain;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import javax.persistence.*;
import java.util.List;
import java.util.function.Consumer;


public class BenchmarkService {

    private final EntityManagerFactory emf;

    public BenchmarkService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void scenarioNPlusUn() {
        mesurer("1. Requête naïve (problème N+1), cache L2 contourné", true, em -> {
            List<Ecrivain> liste = em
                    .createQuery("SELECT e FROM Ecrivain e", Ecrivain.class)
                    .getResultList();
            compterOuvrages(liste);
        });
    }

    public void scenarioJoinFetch() {
        mesurer("2. JOIN FETCH, cache L2 contourné", true, em -> {
            // DISTINCT évite les doublons d'écrivains générés par la jointure
            List<Ecrivain> liste = em
                    .createQuery("SELECT DISTINCT e FROM Ecrivain e JOIN FETCH e.ouvrages", Ecrivain.class)
                    .getResultList();
            compterOuvrages(liste);
        });
    }

    public void scenarioEntityGraph() {
        mesurer("3. Entity Graph, cache L2 contourné", true, em -> {
            EntityGraph<?> graphe = em.getEntityGraph("Ecrivain.ouvrages");
            List<Ecrivain> liste = em
                    .createQuery("SELECT DISTINCT e FROM Ecrivain e", Ecrivain.class)
                    .setHint("javax.persistence.fetchgraph", graphe)
                    .getResultList();
            compterOuvrages(liste);
        });
    }

    public void scenarioCacheSecondNiveau() {
        // On vide tout pour partir d'un cache froid
        emf.unwrap(SessionFactory.class).getCache().evictAllRegions();

        Consumer<EntityManager> lecture = em -> {
            List<Ecrivain> liste = em
                    .createQuery("SELECT e FROM Ecrivain e", Ecrivain.class)
                    .setHint("org.hibernate.cacheable", true) // cache de requêtes
                    .getResultList();
            compterOuvrages(liste);
        };

        mesurer("4a. Cache L2 - lecture 1 (cache froid, remplissage)", false, lecture);
        mesurer("4b. Cache L2 - lecture 2 (cache chaud)", false, lecture);
    }

    private void compterOuvrages(List<Ecrivain> liste) {
        int total = 0;
        for (Ecrivain e : liste) {
            total += e.getOuvrages().size(); // déclenche le chargement LAZY
        }
        System.out.println("    -> " + liste.size() + " écrivains, " + total + " ouvrages lus");
    }

    private void mesurer(String titre, boolean contournerCache, Consumer<EntityManager> action) {
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        System.out.println("\n==================================================");
        System.out.println(titre);
        System.out.println("==================================================");

        EntityManager em = emf.createEntityManager();
        if (contournerCache) {
            em.setProperty("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
            em.setProperty("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);
        }

        long debut = System.nanoTime();
        try {
            action.accept(em);
        } finally {
            em.close();
        }
        long dureeMs = (System.nanoTime() - debut) / 1_000_000;

        System.out.println("--- Résultat : " + titre);
        System.out.println("    Durée                  : " + dureeMs + " ms");
        System.out.println("    Requêtes SQL           : " + stats.getPrepareStatementCount());
        System.out.println("    Cache L2 - hits        : " + stats.getSecondLevelCacheHitCount());
        System.out.println("    Cache L2 - miss        : " + stats.getSecondLevelCacheMissCount());
        System.out.println("    Cache L2 - puts        : " + stats.getSecondLevelCachePutCount());
        System.out.println("    Cache requêtes - hits  : " + stats.getQueryCacheHitCount());
        System.out.println("    Cache requêtes - miss  : " + stats.getQueryCacheMissCount());
    }
}
