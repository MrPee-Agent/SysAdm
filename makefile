# Parking Simulator - Makefile

MVN = mvn

all: compile

compile:
	$(MVN) clean compile

run:
	$(MVN) javafx:run

clean:
	$(MVN) clean

run-fresh: clean compile run

help:
	@echo "Commandes disponibles :"
	@echo "  make compile    - Compiler le projet"
	@echo "  make run        - Lancer l'application"
	@echo "  make clean      - Nettoyer les fichiers compilés"
	@echo "  make run-fresh  - Nettoyer, compiler et lancer"
	@echo "  make help       - Afficher cette aide"

.PHONY: all compile run clean run-fresh help