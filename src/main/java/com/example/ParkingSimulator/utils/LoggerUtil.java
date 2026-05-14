package com.example.ParkingSimulator.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static PrintWriter writer;

    static {
        try {
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            File logFile = new File("logs/parking.log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            writer = new PrintWriter(new FileWriter(logFile, true));
            System.out.println("[LoggerUtil] Prêt → " + logFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("[LoggerUtil] Erreur : " + e.getMessage());
        }
    }

    public static synchronized void log(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String ligne = "[" + timestamp + "] " + message;

        System.out.println(ligne);

        if (writer != null) {
            writer.println(ligne);
            writer.flush();
        }
    }

    public static void close() {
        if (writer != null) {
            writer.close();
        }
    }

    public static void logArrivee(int voitureId) {
        log("Voiture-" + voitureId + " | ARRIVEE   | Arrive au parking");
    }

    public static void logGare(int voitureId, int place, long attenteMs) {
        log("Voiture-" + voitureId + " | GAREE     | Place " + place + " | Attente : " + attenteMs + " ms");
    }

    public static void logSortie(int voitureId, int place) {
        log("Voiture-" + voitureId + " | SORTIE    | Quitte la place " + place);
    }

    public static void logMode(String mode, int nbrPlaces, int nbrVoitures) {
        log("SIMULATION | Mode : " + mode + " | Places : " + nbrPlaces + " | Voitures : " + nbrVoitures);
        log("------------------------------------------------------------");
    }
}