package com.example.ParkingSimulator.utils;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * LoggerUtil — Utilitaire de journalisation et d'analyse statistique.
 *
 * Phase 5 (Houssam) — ParkingSimulator
 *
 * Responsabilités :
 *  - Enregistrer chaque événement du simulateur dans un fichier parking.log
 *  - Calculer les statistiques d'attente et d'utilisation en fin de simulation
 *  - Fournir la méthode analyze() pour afficher les résultats
 */
public class LoggerUtil {

    // -----------------------------------------------------------------------
    //  Constantes
    // -----------------------------------------------------------------------

    private static final String LOG_DIR  = "logs";
    private static final String LOG_FILE = LOG_DIR + "/parking.log";

    /** Format horodatage : 2025-01-15 14:32:05.123 */
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // -----------------------------------------------------------------------
    //  Types d'événements
    // -----------------------------------------------------------------------

    public enum EventType {
        SIMULATION_START,
        SIMULATION_END,
        CAR_ARRIVAL,
        CAR_WAITING,
        CAR_PARKED,
        CAR_DEPARTED,
        PARKING_FULL,
        PARKING_FREE,
        STRATEGY_CHANGE,
        STAT_REPORT
    }

    // -----------------------------------------------------------------------
    //  État interne (thread-safe)
    // -----------------------------------------------------------------------

    /** Instance singleton */
    private static volatile LoggerUtil instance;
    private static final ReentrantLock instanceLock = new ReentrantLock();

    private final ReentrantLock writeLock = new ReentrantLock();
    private PrintWriter writer;

    /** File d'attente en mémoire pour l'analyse en fin de simulation */
    private final ConcurrentLinkedQueue<LogEntry> entries = new ConcurrentLinkedQueue<>();

    // -----------------------------------------------------------------------
    //  Singleton
    // -----------------------------------------------------------------------

    private LoggerUtil() {
        initLogFile();
    }

    /**
     * Retourne l'instance unique (double-checked locking).
     */
    public static LoggerUtil getInstance() {
        if (instance == null) {
            instanceLock.lock();
            try {
                if (instance == null) {
                    instance = new LoggerUtil();
                }
            } finally {
                instanceLock.unlock();
            }
        }
        return instance;
    }

    // -----------------------------------------------------------------------
    //  Initialisation du fichier
    // -----------------------------------------------------------------------

    private void initLogFile() {
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
            // Append = false : nouveau fichier à chaque simulation
            writer = new PrintWriter(new BufferedWriter(new FileWriter(LOG_FILE, false)));
            writeHeader();
        } catch (IOException e) {
            System.err.println("[LoggerUtil] Impossible de créer le fichier log : " + e.getMessage());
        }
    }

    private void writeHeader() {
        String sep = "=".repeat(70);
        writeLine(sep);
        writeLine("  PARKING SIMULATOR — Journal d'exécution");
        writeLine("  Démarré le : " + LocalDateTime.now().format(TIMESTAMP_FMT));
        writeLine(sep);
        writeLine(String.format("%-26s %-20s %-6s %s",
                "HORODATAGE", "ÉVÉNEMENT", "ID", "MESSAGE"));
        writeLine("-".repeat(70));
        flush();
    }

    // -----------------------------------------------------------------------
    //  API publique — Journalisation
    // -----------------------------------------------------------------------

    /**
     * Logue le démarrage de la simulation.
     *
     * @param nbPlaces   Nombre total de places du parking
     * @param nbVoitures Nombre de voitures simulées
     * @param strategy   Stratégie de synchronisation utilisée
     */
    public void logSimulationStart(int nbPlaces, int nbVoitures, String strategy) {
        String msg = String.format(
                "Démarrage — %d places | %d voitures | Stratégie : %s",
                nbPlaces, nbVoitures, strategy);
        log(EventType.SIMULATION_START, -1, msg);
    }

    /**
     * Logue la fin de la simulation.
     */
    public void logSimulationEnd() {
        log(EventType.SIMULATION_END, -1, "Simulation terminée.");
    }

    /**
     * Logue l'arrivée d'une voiture.
     *
     * @param carId Identifiant de la voiture
     */
    public void logArrival(int carId) {
        log(EventType.CAR_ARRIVAL, carId, "Voiture " + carId + " arrive au parking.");
    }

    /**
     * Logue qu'une voiture commence à attendre.
     *
     * @param carId         Identifiant de la voiture
     * @param waitStartMs   Timestamp (System.currentTimeMillis()) du début de l'attente
     */
    public void logWaiting(int carId, long waitStartMs) {
        LogEntry entry = findOrCreate(carId);
        entry.waitStartMs = waitStartMs;
        log(EventType.CAR_WAITING, carId,
                "Voiture " + carId + " attend — parking complet.");
    }

    /**
     * Logue qu'une voiture s'est garée avec succès.
     *
     * @param carId       Identifiant de la voiture
     * @param placeIndex  Numéro de la place attribuée
     * @param waitEndMs   Timestamp de fin d'attente (= moment du stationnement)
     */
    public void logParked(int carId, int placeIndex, long waitEndMs) {
        LogEntry entry = findOrCreate(carId);
        if (entry.waitStartMs > 0) {
            entry.waitDurationMs = waitEndMs - entry.waitStartMs;
        }
        entry.parked = true;
        log(EventType.CAR_PARKED, carId,
                "Voiture " + carId + " garée — place #" + placeIndex
                + "  (attente : " + entry.waitDurationMs + " ms)");
    }

    /**
     * Logue le départ d'une voiture.
     *
     * @param carId        Identifiant de la voiture
     * @param placeIndex   Numéro de la place libérée
     * @param parkDurationMs Durée totale de stationnement en ms
     */
    public void logDeparture(int carId, int placeIndex, long parkDurationMs) {
        LogEntry entry = findOrCreate(carId);
        entry.parkDurationMs = parkDurationMs;
        log(EventType.CAR_DEPARTED, carId,
                "Voiture " + carId + " quitte la place #" + placeIndex
                + "  (durée stationnement : " + parkDurationMs + " ms)");
    }

    /**
     * Logue que le parking est plein.
     *
     * @param occupiedPlaces Nombre de places occupées
     * @param totalPlaces    Capacité totale
     */
    public void logParkingFull(int occupiedPlaces, int totalPlaces) {
        log(EventType.PARKING_FULL, -1,
                "PARKING COMPLET (" + occupiedPlaces + "/" + totalPlaces + ")");
    }

    /**
     * Logue qu'une place vient de se libérer.
     *
     * @param placeIndex   Numéro de la place libérée
     * @param remaining    Places restantes disponibles
     */
    public void logPlaceFree(int placeIndex, int remaining) {
        log(EventType.PARKING_FREE, -1,
                "Place #" + placeIndex + " libérée — disponibles : " + remaining);
    }

    /**
     * Logue un changement de stratégie de synchronisation en temps réel.
     *
     * @param oldStrategy Ancienne stratégie
     * @param newStrategy Nouvelle stratégie
     */
    public void logStrategyChange(String oldStrategy, String newStrategy) {
        log(EventType.STRATEGY_CHANGE, -1,
                "Stratégie modifiée : " + oldStrategy + " → " + newStrategy);
    }

    // -----------------------------------------------------------------------
    //  Méthode Analyze — Analyse statistique finale
    // -----------------------------------------------------------------------

    /**
     * Calcule et affiche les statistiques de la simulation dans la console
     * et les inscrit dans le fichier log.
     *
     * Statistiques fournies :
     *  - Nombre de voitures ayant attendu
     *  - Temps d'attente moyen, min et max
     *  - Taux d'utilisation du parking
     *  - Durée moyenne de stationnement
     */
    public void analyze() {
        List<LogEntry> all = new ArrayList<>(entries);

        long totalCars     = all.size();
        long waitingCars   = all.stream().filter(e -> e.waitDurationMs > 0).count();
        long parkedCars    = all.stream().filter(e -> e.parked).count();

        OptionalDouble avgWait = all.stream()
                .filter(e -> e.waitDurationMs > 0)
                .mapToLong(e -> e.waitDurationMs)
                .average();

        long minWait = all.stream()
                .filter(e -> e.waitDurationMs > 0)
                .mapToLong(e -> e.waitDurationMs)
                .min().orElse(0);

        long maxWait = all.stream()
                .filter(e -> e.waitDurationMs > 0)
                .mapToLong(e -> e.waitDurationMs)
                .max().orElse(0);

        OptionalDouble avgPark = all.stream()
                .filter(e -> e.parkDurationMs > 0)
                .mapToLong(e -> e.parkDurationMs)
                .average();

        double tauxUtilisation = totalCars > 0
                ? (double) parkedCars / totalCars * 100.0
                : 0.0;

        // --- Construction du rapport ---
        String sep = "=".repeat(70);
        String[] lines = {
            sep,
            "  RAPPORT D'ANALYSE STATISTIQUE",
            sep,
            String.format("  Voitures simulées         : %d", totalCars),
            String.format("  Voitures garées           : %d (%.1f%%)", parkedCars, tauxUtilisation),
            String.format("  Voitures ayant attendu    : %d", waitingCars),
            "-".repeat(70),
            String.format("  Attente moyenne           : %.1f ms",
                    avgWait.isPresent() ? avgWait.getAsDouble() : 0.0),
            String.format("  Attente minimale          : %d ms", minWait),
            String.format("  Attente maximale          : %d ms", maxWait),
            "-".repeat(70),
            String.format("  Durée stationnement moy.  : %.1f ms",
                    avgPark.isPresent() ? avgPark.getAsDouble() : 0.0),
            String.format("  Taux d'utilisation        : %.2f%%", tauxUtilisation),
            sep
        };

        // Affichage console
        Arrays.stream(lines).forEach(System.out::println);

        // Écriture dans le log
        writeLock.lock();
        try {
            Arrays.stream(lines).forEach(this::writeLine);
            flush();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Retourne les statistiques sous forme de Map (pour utilisation dans l'UI JavaFX).
     *
     * @return Map clé → valeur des statistiques principales
     */
    public Map<String, Object> getStats() {
        List<LogEntry> all = new ArrayList<>(entries);
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalCars   = all.size();
        long waitingCars = all.stream().filter(e -> e.waitDurationMs > 0).count();
        long parkedCars  = all.stream().filter(e -> e.parked).count();

        double avgWait = all.stream()
                .filter(e -> e.waitDurationMs > 0)
                .mapToLong(e -> e.waitDurationMs)
                .average().orElse(0.0);

        long minWait = all.stream()
                .filter(e -> e.waitDurationMs > 0)
                .mapToLong(e -> e.waitDurationMs).min().orElse(0);

        long maxWait = all.stream()
                .filter(e -> e.waitDurationMs > 0)
                .mapToLong(e -> e.waitDurationMs).max().orElse(0);

        double avgPark = all.stream()
                .filter(e -> e.parkDurationMs > 0)
                .mapToLong(e -> e.parkDurationMs)
                .average().orElse(0.0);

        double tauxUtil = totalCars > 0 ? (double) parkedCars / totalCars * 100.0 : 0.0;

        stats.put("totalCars",          totalCars);
        stats.put("parkedCars",         parkedCars);
        stats.put("waitingCars",        waitingCars);
        stats.put("avgWaitMs",          Math.round(avgWait));
        stats.put("minWaitMs",          minWait);
        stats.put("maxWaitMs",          maxWait);
        stats.put("avgParkDurationMs",  Math.round(avgPark));
        stats.put("utilizationRate",    Math.round(tauxUtil * 100.0) / 100.0);

        return stats;
    }

    /**
     * Réinitialise les données pour une nouvelle simulation.
     */
    public void reset() {
        entries.clear();
        initLogFile();
    }

    // -----------------------------------------------------------------------
    //  Méthodes internes
    // -----------------------------------------------------------------------

    /** Écrit une ligne formatée dans le log (thread-safe). */
    private void log(EventType type, int carId, String message) {
        String ts     = LocalDateTime.now().format(TIMESTAMP_FMT);
        String idStr  = carId >= 0 ? String.valueOf(carId) : "-";
        String line   = String.format("%-26s %-20s %-6s %s",
                ts, type.name(), idStr, message);

        writeLock.lock();
        try {
            writeLine(line);
            flush();
        } finally {
            writeLock.unlock();
        }

        System.out.println("[LOG] " + line);
    }

    private void writeLine(String line) {
        if (writer != null) {
            writer.println(line);
        }
    }

    private void flush() {
        if (writer != null) writer.flush();
    }

    /** Cherche ou crée un LogEntry pour une voiture donnée. */
    private LogEntry findOrCreate(int carId) {
        return entries.stream()
                .filter(e -> e.carId == carId)
                .findFirst()
                .orElseGet(() -> {
                    LogEntry e = new LogEntry(carId);
                    entries.add(e);
                    return e;
                });
    }

    // -----------------------------------------------------------------------
    //  Classe interne — Entrée de log (données par voiture)
    // -----------------------------------------------------------------------

    private static class LogEntry {
        final int carId;
        long waitStartMs    = 0;
        long waitDurationMs = 0;
        long parkDurationMs = 0;
        boolean parked      = false;

        LogEntry(int carId) {
            this.carId = carId;
        }
    }
}