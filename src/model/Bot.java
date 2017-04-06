package model;

public class Bot {
	private State current;
		
	public Bot(State s) {
		current = s;
	}
	
	public State simulate() {
		double[][] T = current.transition();
		
		double r = Math.random();
		int nx = -1, ny = -1, nh = -1;
		
		for (int y = 0; y < 3 && r > 0; y++) {
			for (int x = 0; x < 3 && r > 0; x++) {
				for (int h = 0; h < 4 && r > 0; h++) {
					r -= T[y][x * 4 + h];
					if (r <= 0) {
						nx = x; ny = y; nh = h;
					}
				}
			}
		}
		
		if (nx == -1 && ny == -1 && nh == -1) {
			throw new RuntimeException("Shit: " + r);
		}
		
		// Convert to world coords: [0..2, 0..2] to [-1..1, -1..1] 
		return current.offset(nx - 1, ny - 1, nh);
	}
	
	public void update() {
		current = simulate();
	}
	
	public State getState() { return current; }
}
