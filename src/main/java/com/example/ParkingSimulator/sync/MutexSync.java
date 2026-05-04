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
        this.placeDispo = dispo;
    }

    @Override
    public String getNom() { return "MUTEX"; }
}