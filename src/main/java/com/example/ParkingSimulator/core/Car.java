package com.example.ParkingSimulator.core;

import java.util.Random;

public class Car extends Thread {
    private final int id;
    private final Parking parking;
    private int place;
    private long tempsAttente;  // For Phase 5 (statistics)

    public Car(int id, Parking parking) {
        this.id = id;
        this.parking = parking;
    }

    public int getCarId() {
        return id;
    }

    public int getPlace() {
        return place;
    }

    public long getTempsAttente() {
        return tempsAttente;
    }

    @Override
    public void run() {
        try {
            // Record arrival time
            long arrivee = System.currentTimeMillis();

            // Try to park (blocks if parking is full)
            place = parking.entrer(id);

            // Calculate waiting time
            tempsAttente = System.currentTimeMillis() - arrivee;

            // Simulate parking duration (2 to 5 seconds)
            Random random = new Random();
            Thread.sleep(5000 + random.nextInt(5000)); // Ralenti : 5 à 10 secondes

            // Leave the parking
            parking.sortir(id, place);

        } catch (InterruptedException e) {
            System.out.println("[Car-" + id + "] Interrupted: " + e.getMessage());
        }
    }
}