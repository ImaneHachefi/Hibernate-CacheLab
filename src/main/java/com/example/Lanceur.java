package com.example;

import com.example.service.BenchmarkService;
import com.example.service.JeuDonneesService;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Lanceur {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("cache-lab-pu");

        try {
            new JeuDonneesService(emf).peuplerBase(10, 5);

            BenchmarkService bench = new BenchmarkService(emf);
            bench.scenarioNPlusUn();
            bench.scenarioJoinFetch();
            bench.scenarioEntityGraph();
            bench.scenarioCacheSecondNiveau();
        } finally {
            emf.close();
        }
    }
}
