package com.example.ParkingSimulator;
import java.util.List;

public interface IStrategy {
	public int trouverPlace(List<Long> tempPlace, List<Boolean> placeOccupe, int nbrPlaces);
}
