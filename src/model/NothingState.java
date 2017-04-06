package model;

public class NothingState extends State {

	public NothingState(Grid grid, int x, int y, int heading) {
		super(grid, x, y, heading);
	}
	
	protected int first(State other) {
		int count = 0;
		int origin[] = other.position();
		
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {				
				if (x == 1 && y == 1) continue;
				
				int ox = x - 1;
				int oy = y - 1;
				
				if (grid.inside(origin[1] + ox, origin[0] + oy))
					count++;
			}
		}
		return count;
	}
	
	protected int second(State other) {
		int count = 0;
		int origin[] = other.position();
		
		for (int y = 0; y < 5; y++) {
			for (int x = 0; x < 5; x++) {				
				if (x >= 1 && x <= 3 && y >= 1 && y <= 3) continue;
				
				int ox = x - 2;
				int oy = y - 2;
				
				if (grid.inside(origin[1] + ox, origin[0] + oy))
					count++;
			}
		}
		return count;
	}
	
	@Override
	public int[] position() { return null; }

	public double emission(State other) {
		int a = first(other);
		int b = second(other);
		return (1.0 - 0.1 - a * 0.05 - b * 0.025) / 4;
	}
}
