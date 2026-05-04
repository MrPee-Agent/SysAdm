package com.example.ParkingSimulator;
import java.util.List;

public class DefaultStrategy implements IStrategy {
	@Override
	public int trouverPlace(List<Long> tempPlace, List<Boolean> placeOccupe, int nbrPlaces) {
		// Calculer le nombre de colonnes basé sur le nombre de places
		int numColumns = (int) Math.ceil(Math.sqrt(nbrPlaces));
		// Calculer le nombre de lignes basé sur le nombre de colonnes et le nombre de places
		int numRows = (int) Math.ceil((double) nbrPlaces / numColumns);

		// Parcourir chaque colonne
		for (int col = 0; col < numColumns; col++) {
			// Parcourir chaque ligne
			for (int row = 0; row < numRows; row++) {
				// Calculer l'index de la place actuelle
				int place = col + row * numColumns;
				// Vérifier si l'index est valide et si la place n'est pas occupée
				if (place < nbrPlaces && !placeOccupe.get(place)) {
					return place; // Retourner l'index de la première place trouvée non occupée
				}
			}
		}
		return -1; // Retourner -1 si aucune place n'est trouvée
	}
