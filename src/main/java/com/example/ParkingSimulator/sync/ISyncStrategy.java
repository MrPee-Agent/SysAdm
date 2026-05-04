package com.example.ParkingSimulator.sync;

public interface ISyncStrategy {

    void acquérir(int voitureId) throws InterruptedException;

    void libérer(int voitureId);

    String getNom();
}