package data;

public class Global {
	
	private int days;
	private int[][] distanceMatrix;
	private String dataSet;
	private String name;

	public Global(String dataSet, String name, int days, int[][] distanceMatrix) {
		this.dataSet = dataSet;
		this.name = name;
		this.days = days;
		this.distanceMatrix = distanceMatrix;
	}
	
	public final int computeDistance(Location loc1, Location loc2)
	{
		int distance = 0;
		if(distanceMatrix == null)
		{
			distance = (int) Math.sqrt(Math.pow(loc1.getX()-loc2.getX(),2) + Math.pow(loc1.getY()-loc2.getY(),2));
		}
		else
		{
			distance = distanceMatrix[loc1.getId()][loc2.getId()];
		}
		return distance;
	}

	
	public final int getDays() {
		return days;
	}

	public final String getDataSet() {
		return dataSet;
	}

	public final String getName() {
		return name;
	}


}
