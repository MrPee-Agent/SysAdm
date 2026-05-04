package com.example.ParkingSimulator.core;

import com.example.ParkingSimulator.strategy.DefaultStrategy;
import com.example.ParkingSimulator.sync.SemaphoreSync;

public class TestMain {
    public static void main(String[] args) throws InterruptedException {
        // Create parking with 3 spots, DefaultStrategy, Semaphore
        Parking parking = new Parking(3, new DefaultStrategy(), new SemaphoreSync(3));

        // Create 5 cars (more than spots to test waiting)
        for (int i = 1; i <= 5; i++) {
            Car car = new Car(i, parking);
            car.start();
            Thread.sleep(500); // Stagger arrivals
        }
    }
}