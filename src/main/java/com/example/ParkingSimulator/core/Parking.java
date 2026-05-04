package com.example.ParkingSimulator.core;
import com.example.ParkingSimulator.strategy.IStrategy;
import com.example.ParkingSimulator.sync.ISyncStrategy;
import com.example.ParkingSimulator.sync.MutexSync;

import java.util.ArrayList;
import java.util.List;

public class Parking {

    private final int           nbrPlaces;
    private final List<Boolean> placeOccupe;
    private final List<Long>    tempPlace;
    private final IStrategy     strategy;
    private final ISyncStrategy syncStrategy;
    private final List<Long>    tempsAttente = new ArrayList<>();

    public Parking(int nbrPlaces, IStrategy strategy, ISyncStrategy syncStrategy) {
        this.nbrPlaces    = nbrPlaces;
        this.strategy     = strategy;
        this.syncStrategy = syncStrategy;
        this.placeOccupe  = new ArrayList<>(nbrPlaces);
        this.tempPlace    = new ArrayList<>(nbrPlaces);

        for (int i = 0; i < nbrPlaces; i++) {
            placeOccupe.add(false);
            tempPlace.add(0L);
        }

        System.out.println("[Parking] Créé → "
                + nbrPlaces + " places | Mode : " + syncStrategy.getNom());
    }

    public int entrer(int voitureId) throws InterruptedException {
        long debut = System.currentTimeMillis();

        if (syncStrategy instanceof MutexSync mutex) {
            synchronized (this) {
                boolean dispo = strategy.trouverPlace(tempPlace, placeOccupe, nbrPlaces) != -1;
                mutex.setPlaceDispo(dispo);
            }
        }

        syncStrategy.acquérir(voitureId);

        long attente = System.currentTimeMillis() - debut;
        tempsAttente.add(attente);

        synchronized (this) {
            int place = strategy.trouverPlace(tempPlace, placeOccupe, nbrPlaces);
            placeOccupe.set(place, true);
            tempPlace.set(place, System.currentTimeMillis());

            System.out.println("[Parking] Voiture-" + voitureId
                    + " → place " + place
                    + " (attente : " + attente + "ms)");
            return place;
        }
    }

    public void sortir(int voitureId, int place) {
        synchronized (this) {
            placeOccupe.set(place, false);
            tempPlace.set(place, 0L);

            if (syncStrategy instanceof MutexSync mutex) {
                mutex.setPlaceDispo(true);
            }
        }

        System.out.println("[Parking] Voiture-" + voitureId
                + " quitte la place " + place);

        syncStrategy.libérer(voitureId);
    }

    public int        getNbrPlaces()        { return nbrPlaces; }
    public String     getModeSync()         { return syncStrategy.getNom(); }
    public List<Long> getTempsAttente()     { return tempsAttente; }

    public synchronized boolean isOccupe(int i) {
        return placeOccupe.get(i);
    }

    public synchronized List<Boolean> getPlaceOccupe() {
        return new ArrayList<>(placeOccupe);
    }
}