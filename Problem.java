// Erhan Yalniz   -   150117905

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Scanner;

public class Problem {
	
	// This will hold the cities as a list read from file with x and y positions on 2D grid.
	static ArrayList<City> cities = new ArrayList<City>();
	static Path p = new Path();
	
	public static void main(String[] args) {
		try {
			// Try to parse file. 
			parseFile("inputs/test-input-1.txt");
			// Solve TSP problem with given cities. Divide the whole problem to TSP problems with at most 1000 cities.
			// Limit queue iteration on each part by 10 and limit the call (recursion) by 2 times of part size.
			p = tspTourCalculate(1000, 10, 2);
			// Print the resulting solution path to a file.
			printPath(p, "outputs/test1.txt");
		} catch(FileNotFoundException e) {
			// If any error happens show it and exit the program.
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	// Parses file to get cities and their positions on 2D grid.
	static void parseFile(String filename) throws FileNotFoundException {
		Scanner fs = new Scanner(new File(filename));
		int index;
		long x, y;
		// Read whole file.
		while(fs.hasNext()) {
			index = fs.nextInt();
			x = fs.nextLong();
			y = fs.nextLong();
			// Append to cities list.
			cities.add(new City(index, x, y));
		}
	}
	
	// Calculate the TSP problem, nearest neighbor algorithm approach.
	// Part size here will be used to divide problem to more little problems to make it easier and faster to solve.
	// Other two integers are used to optimize each and every step of calculating parts of whole TSP problem.
	// The effects of them are explained in more detail below.
	static Path tspTourCalculate(int partSize, int queueLimit, int callLimitMultiplier) {
		// Get the starting time to calculate how much time trying each tour takes.
		Instant start = Instant.now();
		
		// Divide and Conquer Method

		// trySolve function already calculates small number of tours with up to 1000 cities.
		// But to solve TSP problems with bigger sets of cities, there is another way to approach it:
		// Divide whole problem to TSP problems with more manageable sized sets of cities.
		// So divide them to be TSP problems with 1000 or less cities.
		
		int numberOfParts = (int) Math.ceil((double)cities.size()/partSize);
		
		// Tour should contain exactly the same number of elements to visit every vertex once.
		// Then with return to starting element we get one more element. 
		Path path = new Path();
		
		for(int i = 0; i < numberOfParts; i++) {
			
			// This queue will represent all the cities that are needed to be visited.
			ArrayList<Integer> queue = new ArrayList<Integer>();
			
			// Add cities that need to be visited to queue.
			int last = (i+1)*partSize < cities.size() ? (i+1)*partSize : cities.size();
			int j = i*partSize + 1;
			for(; j < last; j++) {
				queue.add(cities.get(j).id);
			}
			
			// Select next starting city to calculate TSP on.
			City nextCity = cities.get(i*partSize);
			
			// Calculate every and each path using a recursive function and get the best path.
			// 5th parameter here actually allows for optimization of algorithm by only allowing complex combination of cities
			// after the remaining number of cities are less than this integer.
			// In testing this algorithm performed best under optimization parameter of 10 or less.
			// The waiting time exponentially grow after 12.
			// Number of cities do effect algorithm too but not too much on lower level optimization parameters.
			// Negative queue limit will disable queue limiting. (So more different branches on each step but not optimized)
			// The last parameter is also for optimization it is the maximum number of times function can call itself.
			// Just like queue limit this "callLimit" also has a non-optimize option if it is not positive.
			Path subresult = tryPath(queue, nextCity.id, new Path(nextCity), new Path(Long.MAX_VALUE), queueLimit, partSize*callLimitMultiplier);
			path.concat(subresult);
		}
		
		// Get the ending time to calculate how much time trying each tour takes.
		// This is used to get a information about how good optimization variable can be.
		Instant end = Instant.now();
		Duration timeElapsed = Duration.between(start, end);
		System.out.println("TSP tour combinations took time: " 
		+ timeElapsed.toMillis()/60/1000 + " minutes " 
		+ timeElapsed.toMillis()/1000%60 + " seconds " 
		+ timeElapsed.toMillis()%1000 +  " milliseconds");
		
		// Return the best path calculated.
		return path;
	}
	
	static long d(City c1, City c2) {
		// Given function to calculate distance between two cities.
		return Math.round(Math.sqrt((c1.x-c2.x)*(c1.x-c2.x)+(c1.y-c2.y)*(c1.y-c2.y)));
	}
	
	static ArrayList<Integer> getSortedQueue(ArrayList<Integer> queue, int index) {
		City current = cities.get(index);
		
		// Bubble-sort the queue according to minimal distance with current city.
		for(int i = 0; i < queue.size() - 1; i++) {
			for(int j = 0; j < queue.size() - i - 1; j++) {
				City c1 = cities.get(queue.get(j));
				City c2 = cities.get(queue.get(j+1));
				if(d(current, c2) < d(current, c1)) {
					int temp = queue.get(j);
					queue.set(j, queue.get(j+1));
					queue.set(j+1, temp);
				}
			}
		}
		
		// Return the new queue.
		return queue;
	}
	
	// Recursive function to calculate tours starting from nearest neighbor path.
	// Queue represents all the cities that are not yet visited. (just id's of them)
	// Last City Id is the city algorithm is currently on which will make decisions to further choose a next city to travel.
	// Path Taken is the tour we are creating on this iteration of algorithm.
	// Best Path is a complete tour of all cities with known minimum distance. (Known best solution for TSP)
	// Queue Limit is optimizing this algorithm by just allowing branching combinations when there is less cities than this limit to choose from queue.
	static Path tryPath(ArrayList<Integer> queue, int lastCityId, Path pathTaken, Path bestPath, int queueLimit, int callLimit) {
		
		// When all cities visited return the resulting travel path and distance.
		if(queue.size() == 0) {
			return pathTaken;
		}
		// Abort the path if path is too long.
		if(pathTaken.dist > bestPath.dist) {
			// Assume path is very long. So return maximum possible distance as result.
			pathTaken.dist = Long.MAX_VALUE;
			return pathTaken;
		}
		
		// abort if call limit is reached.
		if(callLimit == 0) {
			return bestPath;
		}
		
		// Sort queue by minimal distances to current city algorithm is on.
		queue = getSortedQueue(queue, lastCityId);
		
		Path temp = new Path(Long.MAX_VALUE);
		
		// Get the number of remaining cities to visit and because of queue size is big:
		// There is ton of combinations so limit the number of possible next cities to visit by "queueLimit".
		int j = queue.size();
		if(queueLimit > 0) {
			j = j > queueLimit ? 1 : j;
		}
		
		for(int i = 0; i < j; i++) {
			// Get the next selected city to visit.
			City nextCity = cities.get(queue.get(i));
			int nextCityId = nextCity.id;
			
			// This is the new queue for next sub-path.
			ArrayList<Integer> newQueue = new ArrayList<Integer>(queue);
			
			// Remove the element selected for next city to be visited.
			newQueue.remove(i);
			
			// Create path for next city by copying old path and adding next city to it.
			Path newPath = new Path(pathTaken);
			newPath.add(nextCity);
			
			// Get a minimal tour distance of each combination of sub-path that could be choosen from queue.
			temp = tryPath(newQueue, nextCityId, newPath, bestPath, queueLimit, callLimit-1);
			
			// Decrease call limit by each time the function was recursed.
			// This can be calculated by difference between number of items between the path that was calculated and
			// path that algorithm is currently have taken.
			// This optimization should only be applied if "callLimit" is positive.
			if(callLimit > 0) {
				int timesCalled = temp.cities.size()-pathTaken.cities.size();
				callLimit = (callLimit - timesCalled) > 0 ? (callLimit - timesCalled) : 0;	
			}
			
			// Check if current calculated sub-path is the new minimum sub-path from current city. 
			// If a new minimum distance is found update it
			if(temp.dist < bestPath.dist) {
				bestPath = temp;
			}
			
			if(callLimit == 0) {
				break;
			}
		}
		
		// Return the minimum known complete tour.
		return bestPath;
	}
	
	// Given a complete tour "p"; print all info about path to file. 
	static void printPath(Path p, String filename) throws FileNotFoundException {
		Formatter f = new Formatter(filename);
		
		// Don't forget to calculate and add distance to starting city in total distance.
		p.returnStart();
		
		// Print to file.
		f.format("%d\n", p.dist);
		for(City c:p.cities) {
			f.format("%d\n", c.id);
		}
		
		f.close();
	}
}
