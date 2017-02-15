package data;

public class Tool {
	private int size;
	private int cost;
	private int id;
	
	public Tool(int size,int cost,int id) {
		this.size=size;
		this.cost=cost;
		this.id=id;
		
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

}
