package model;

public class Bot {
	private State current;
		
	public Bot(State s) {
		current = s;
	}
	
	public void update() {
		double[][] T = current.transition();
		
		double r = Math.random();
		double a = 0;
		int nx = -1, ny = -1, nh = -1;
		
		for (int y = 0; y < 3 && r > 0; y++) {
			for (int x = 0; x < 3 && r > 0; x++) {
				double c = 0;
				for (int h = 0; h < 4 && r > 0; h++) {
					r -= T[y][x * 4 + h];
					a += T[y][x * 4 + h];
					c += T[y][x * 4 + h];
					if (r <= 0) {
						nx = x; ny = y; nh = h;
					}
				}
				System.out.print(c + " ");
			}
			System.out.println();
		}
		
		if (nx == -1 && ny == -1 && nh == -1) {
			throw new RuntimeException("Shit: " + r + ", " + a);
		}
		
		// Convert to world coords: [0..2, 0..2] to [-1..1, -1..1] 
		current = current.offset(nx - 1, ny - 1, nh);
	}
	
	public State getState() { return current; }
}
