package com.example.ParkingSimulator.sync;

public class MutexSync implements ISyncStrategy {

    private final Object verrou;
    private volatile boolean placeDispo;

    public MutexSync(Object verrou, boolean placeDispo) {
        this.verrou     = verrou;
        this.placeDispo = placeDispo;
    }

    @Override
    public void acquérir(int voitureId) throws InterruptedException {
        synchronized (verrou) {
            while (!placeDispo) {
                System.out.println("[MUTEX] Voiture-" + voitureId + " attend une place...");
                verrou.wait();
            }
            // Une fois réveillé, on consomme la place disponible
            placeDispo = false;
            System.out.println("[MUTEX] Voiture-" + voitureId + " a obtenu l'accès");
        }
    }

    @Override
    public void libérer(int voitureId) {
        synchronized (verrou) {
            System.out.println("[MUTEX] Voiture-" + voitureId + " libère → notifyAll()");
            verrou.notifyAll();
        }
    }

    public void setPlaceDispo(boolean dispo) {
        synchronized (verrou) {
            this.placeDispo = dispo;
            if (dispo) {
                verrou.notifyAll();
            }
        }
    }

    @Override
    public String getNom() { return "MUTEX"; }
}