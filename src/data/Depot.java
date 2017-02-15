package data;

import java.util.HashMap;

public class Depot {

	private Location location;
	private HashMap<Tool, Integer> toolAvailable;

	public Depot(Location location, HashMap<Tool, Integer> toolAvailable) {
			this.location=location;
			this.toolAvailable=toolAvailable;
			
					
	}

	public Location getLocation() {
		return location;
	}

	public HashMap<Tool, Integer> getToolAvailable() {
		return toolAvailable;
	}
}
