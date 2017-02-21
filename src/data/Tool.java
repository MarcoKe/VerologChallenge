package data;

public class Tool {
	private int size;
	private int cost;
	private int id;
	private int maxAvailable;
	
	public Tool(int size,int cost,int id, int maxAvailable) {
		this.size=size;
		this.cost=cost;
		this.id=id;
		this.maxAvailable = maxAvailable;
		
	}

	public final int getSize() {
		return size;
	}

	public final int getCost() {
		return cost;
	}

	public final int getId() {
		return id;
	}
	
	public final int getMaxAvailable() {
		return maxAvailable;
	}

}
