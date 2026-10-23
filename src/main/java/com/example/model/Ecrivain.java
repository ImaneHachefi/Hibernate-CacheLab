package com.example.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ecrivains")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NamedEntityGraph(
        name = "Ecrivain.ouvrages",
        attributeNodes = @NamedAttributeNode("ouvrages")
)
public class Ecrivain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_complet")
    private String nomComplet;

    @OneToMany(mappedBy = "ecrivain", cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<Ouvrage> ouvrages = new ArrayList<>();

    public Ecrivain() {
    }

    public Ecrivain(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public void attacherOuvrage(Ouvrage ouvrage) {
        ouvrages.add(ouvrage);
        ouvrage.setEcrivain(this);
    }

    public void detacherOuvrage(Ouvrage ouvrage) {
        ouvrages.remove(ouvrage);
        ouvrage.setEcrivain(null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public List<Ouvrage> getOuvrages() { return ouvrages; }
    public void setOuvrages(List<Ouvrage> ouvrages) { this.ouvrages = ouvrages; }
}
