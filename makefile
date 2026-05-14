# =============================================================================
#  Makefile — ParkingSimulator
#  Auteur  : Houssam (Phase 5)
#  Projet  : Simulation du Problème de Parking (Mini Projet SE)
#
#  Ce Makefile s'appuie sur Maven (mvn) pour compiler et packager le projet.
#  JavaFX est géré via le pom.xml (dépendances + plugin javafx-maven-plugin).
#
#  Commandes disponibles :
#    make          → compile le projet
#    make run      → lance la simulation (interface graphique)
#    make test     → exécute les tests unitaires
#    make clean    → supprime les fichiers compilés
#    make package  → crée le JAR exécutable
#    make log      → affiche le fichier parking.log
#    make stats    → affiche uniquement les statistiques du dernier run
#    make help     → liste toutes les commandes disponibles
# =============================================================================

# --- Variables ---
MVN       := mvn
JAVA      := java
LOG_FILE  := logs/parking.log
JAR_DIR   := target
MAIN_JAR  := $(JAR_DIR)/ParkingSimulator-1.0-SNAPSHOT.jar

# Couleurs terminal
GREEN  := \033[0;32m
YELLOW := \033[1;33m
CYAN   := \033[0;36m
RESET  := \033[0m

# --- Cible par défaut ---
.DEFAULT_GOAL := help

# =============================================================================
#  COMPILATION
# =============================================================================

## compile : Compile le projet avec Maven
.PHONY: compile
compile:
	@echo "$(CYAN)► Compilation du projet...$(RESET)"
	$(MVN) compile -q
	@echo "$(GREEN)✔ Compilation réussie.$(RESET)"

## all : Alias de compile (cible par défaut classique)
.PHONY: all
all: compile

# =============================================================================
#  EXÉCUTION
# =============================================================================

## run : Lance le simulateur (interface JavaFX)
.PHONY: run
run:
	@echo "$(CYAN)► Lancement du simulateur de parking...$(RESET)"
	@mkdir -p logs
	$(MVN) javafx:run -q
	@echo "$(GREEN)✔ Simulation terminée. Consultez $(LOG_FILE) pour les détails.$(RESET)"

# =============================================================================
#  TESTS
# =============================================================================

## test : Exécute les tests unitaires JUnit
.PHONY: test
test:
	@echo "$(CYAN)► Exécution des tests...$(RESET)"
	$(MVN) test
	@echo "$(GREEN)✔ Tests terminés.$(RESET)"

# =============================================================================
#  PACKAGING
# =============================================================================

## package : Crée le JAR exécutable dans target/
.PHONY: package
package:
	@echo "$(CYAN)► Création du JAR...$(RESET)"
	$(MVN) package -DskipTests -q
	@echo "$(GREEN)✔ JAR créé : $(MAIN_JAR)$(RESET)"

# =============================================================================
#  NETTOYAGE
# =============================================================================

## clean : Supprime les fichiers compilés (target/)
.PHONY: clean
clean:
	@echo "$(YELLOW)► Nettoyage des fichiers compilés...$(RESET)"
	$(MVN) clean -q
	@echo "$(GREEN)✔ Nettoyage terminé.$(RESET)"

## clean-logs : Supprime le fichier de log
.PHONY: clean-logs
clean-logs:
	@echo "$(YELLOW)► Suppression du fichier log...$(RESET)"
	@rm -f $(LOG_FILE)
	@echo "$(GREEN)✔ Logs supprimés.$(RESET)"

## clean-all : Nettoyage complet (binaires + logs)
.PHONY: clean-all
clean-all: clean clean-logs
	@echo "$(GREEN)✔ Nettoyage complet effectué.$(RESET)"

# =============================================================================
#  JOURNALISATION & STATISTIQUES
# =============================================================================

## log : Affiche le fichier parking.log complet
.PHONY: log
log:
	@if [ -f $(LOG_FILE) ]; then \
		echo "$(CYAN)► Contenu de $(LOG_FILE) :$(RESET)"; \
		cat $(LOG_FILE); \
	else \
		echo "$(YELLOW)⚠  Aucun fichier log trouvé. Lancez d'abord 'make run'.$(RESET)"; \
	fi

## stats : Affiche uniquement le rapport statistique du dernier run
.PHONY: stats
stats:
	@if [ -f $(LOG_FILE) ]; then \
		echo "$(CYAN)► Rapport statistique :$(RESET)"; \
		grep -A 20 "RAPPORT D'ANALYSE STATISTIQUE" $(LOG_FILE) || \
			echo "$(YELLOW)⚠  Aucun rapport trouvé dans le log.$(RESET)"; \
	else \
		echo "$(YELLOW)⚠  Aucun fichier log trouvé. Lancez d'abord 'make run'.$(RESET)"; \
	fi

## log-tail : Affiche les 30 dernières lignes du log (utile pendant une simulation)
.PHONY: log-tail
log-tail:
	@if [ -f $(LOG_FILE) ]; then \
		tail -30 $(LOG_FILE); \
	else \
		echo "$(YELLOW)⚠  Aucun fichier log trouvé.$(RESET)"; \
	fi

# =============================================================================
#  AIDE
# =============================================================================

## help : Affiche cette aide
.PHONY: help
help:
	@echo ""
	@echo "$(CYAN)╔══════════════════════════════════════════════════════════╗$(RESET)"
	@echo "$(CYAN)║         ParkingSimulator — Commandes disponibles         ║$(RESET)"
	@echo "$(CYAN)╚══════════════════════════════════════════════════════════╝$(RESET)"
	@echo ""
	@grep -E '^## ' $(MAKEFILE_LIST) | sed 's/## //' | \
		awk -F ':' '{printf "  $(GREEN)%-18s$(RESET) %s\n", $$1, $$2}'
	@echo ""
	@echo "  Exemples :"
	@echo "    make run          → Lance la simulation"
	@echo "    make stats        → Affiche les statistiques"
	@echo "    make clean-all    → Nettoie tout"
	@echo ""