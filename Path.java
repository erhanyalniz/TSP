// Erhan Yalniz   -   150117905

import java.util.ArrayList;

// Used to describe a tour around cities.
public class Path {
	// Total sum of distance.
	long dist;
	// Cities travelled in order.
	ArrayList<City> cities;
	
	// Default constructor to initialize everything to empty.
	public Path() {
		dist = 0;
		cities = new ArrayList<City>();
	}

	// Constructor to initialize everything to empty.
	public Path(long totalDistance) {
		dist = totalDistance;
		cities = new ArrayList<City>();
	}
	
	// Constructor to initialize a path with starting city.	
	public Path(City startingCity) {
		dist = 0;
		cities = new ArrayList<City>();
		cities.add(startingCity);
	}
	
	// Constructor to initialize a new path with a existing one by copying it.
	public Path(Path p) {
		dist = p.dist;
		cities = new ArrayList<City>(p.cities);
	}
	
	// Add a new city to path. Delta distance is the distance needed to reach destination city.
	public void add(City c) {
		dist+=d(cities.get(cities.size()-1), c);
		cities.add(c);
	}
	
	// Remove last city from path. Delta distance is the distance needed to reach city from new last city.
	public void removeLast() {
		int size = cities.size();
		dist-= d(cities.get(size-2), cities.get(size-1));
		cities.remove(cities.size()-1);
	}
	
	// Merge two paths add passed path as a trail to this path.
	public void concat(Path p) {
		if(cities.isEmpty()) {
			dist+=p.dist;
		} else {
			dist+=d(cities.get(cities.size()-1), p.cities.get(0)) + p.dist;
		}
		cities.addAll(p.cities);
	}
	
	public void returnStart() {
		dist+=d(cities.get(cities.size()-1),cities.get(0));
	}
	
	long d(City c1, City c2) {
		// Given function to calculate distance between two cities.
		return Math.round(Math.sqrt((c1.x-c2.x)*(c1.x-c2.x)+(c1.y-c2.y)*(c1.y-c2.y)));
	}
}
