package com.example.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(name = "ouvrages")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Ouvrage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String intitule;

    // LAZY : indispensable pour observer le problème N+1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecrivain_id")
    private Ecrivain ecrivain;

    public Ouvrage() {
    }

    public Ouvrage(String intitule) {
        this.intitule = intitule;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }

    public Ecrivain getEcrivain() { return ecrivain; }
    public void setEcrivain(Ecrivain ecrivain) { this.ecrivain = ecrivain; }
}
