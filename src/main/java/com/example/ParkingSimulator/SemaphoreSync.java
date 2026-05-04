package com.example.ParkingSimulator;

import java.util.concurrent.Semaphore;

public class SemaphoreSync implements ISyncStrategy {

    private final Semaphore semaphore;

    public SemaphoreSync(int nbrPlaces) {
        this.semaphore = new Semaphore(nbrPlaces);
    }

    @Override
    public void acquérir(int voitureId) throws InterruptedException {
        System.out.println("[SEMAPHORE] Voiture-" + voitureId
                + " attend... (places dispo: " + semaphore.availablePermits() + ")");
        semaphore.acquire();
    }

    @Override
    public void libérer(int voitureId) {
        semaphore.release();
        System.out.println("[SEMAPHORE] Voiture-" + voitureId
                + " sorti → places dispo: " + semaphore.availablePermits());
    }

    @Override
    public String getNom() { return "SEMAPHORE"; }
}