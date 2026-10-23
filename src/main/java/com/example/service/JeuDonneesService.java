package com.example.service;

import com.example.model.Ecrivain;
import com.example.model.Ouvrage;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Insère un jeu de données de test (écrivains + ouvrages).
 */
public class JeuDonneesService {

    private final EntityManagerFactory emf;

    public JeuDonneesService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void peuplerBase(int nbEcrivains, int ouvragesParEcrivain) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            for (int i = 1; i <= nbEcrivains; i++) {
                Ecrivain ecrivain = new Ecrivain("Écrivain " + i);

                for (int j = 1; j <= ouvragesParEcrivain; j++) {
                    ecrivain.attacherOuvrage(new Ouvrage("Ouvrage " + j + " de l'écrivain " + i));
                }
                em.persist(ecrivain);
            }

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
        System.out.println(">>> " + nbEcrivains + " écrivains et "
                + (nbEcrivains * ouvragesParEcrivain) + " ouvrages insérés.");
    }
}
