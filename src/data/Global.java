package data;

public class Global {
	
	private int days;
	private int[][] distanceMatrix;

	public Global(int days, int[][] distanceMatrix) {
		this.days = days;
		this.distanceMatrix = distanceMatrix;
	}
	
	public int computeDistance(Location loc1, Location loc2)
	{
		int distance = 0;
		if(distanceMatrix[0] == null)
		{
			distance = (int) Math.sqrt(Math.pow(loc1.getX()-loc2.getX(),2) + Math.pow(loc1.getY()-loc2.getY(),2));
		}
		else
		{
			distance = distanceMatrix[loc1.getId()][loc2.getId()];
		}
		return distance;
	}


}
