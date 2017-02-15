package data;

public class Location {
	
	private int x;
	private int y;
	private int id;
	
	public Location(int x, int y, int id){
		this.x=x;
		this.y=y;
		this.id=id;
		
	}
	
	public final int getId() {
		return id;
	}

	public final int getX() {
		return x;
	}

	public final int getY() {
		return y;
	}

	
}
