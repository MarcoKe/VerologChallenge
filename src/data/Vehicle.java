package data;

public class Vehicle {
	private int capacity;
	private int maxDistance;
	private int costPerVehicle;
	private int costPerDay;
	private int costPerDistance;
	private int id;
	
	public Vehicle(int capacity, int maxDistance, int costPerVehicle, int costPerDay, int costPerDistance, int id) {
		this.capacity=capacity;
		this.maxDistance=maxDistance;
		this.costPerVehicle=costPerVehicle;
		this.costPerDay=costPerDay;
		this.costPerDistance=costPerDistance;
		this.id=id;
		
	
	}

	public final int getCapacity() {
		return capacity;
	}

	public final int getMaxDistance() {
		return maxDistance;
	}

	public final int getCostPerVehicle() {
		return costPerVehicle;
	}

	public final int getCostPerDay() {
		return costPerDay;
	}

	public final int getCostPerDistance() {
		return costPerDistance;
	}

	public final int getId() {
		return id;
	} 
}
