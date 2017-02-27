package model;

import java.util.Random;

public class Sensor {
	
	int cols, rows;
	
	public Sensor(int cols, int rows) {
		this.cols = cols;
		this.rows = rows;
	}
	
	// square centered in 0,0 with bounds [-size, size]
	private boolean onEdge(int x, int y, int size) {
		return x == size || y == size || x == -size || y == -size;
	}
	
	private int[] pointOnSquareEdge(int outer) {
		Random rng = new Random();
		int half = outer / 2;
		
		// 5 => 5/2 = 2, 3 => 3/2 = 1
		int x = rng.nextInt(outer) - half;
		int y = rng.nextInt(outer) - half;
		
		while (!onEdge(x, y, half)) {
			x = rng.nextInt(outer) - half;
			y = rng.nextInt(outer) - half;
		}
		
		return new int[]{y, x};
	}
	
	private boolean outsideWorld(int y, int x) {
		return x < 0 || x >= cols || y < 0 || y >= rows;
	}
	
	public int[] sample(int[] truePos) {
		Random rng = new Random();
		float f = rng.nextFloat();
		
		// Nothing
		if (f <= 0.6)
			return null;
		
		// actual, hue hue hue
		int x = truePos[1];
		int y = truePos[0];
		
		if (f < 0.6 + 0.15) {
			int []delta = pointOnSquareEdge(3);
			x += delta[1];
			y += delta[0];
		} 
		
		if (f < 0.6 + 0.15 + 0.15){
			int []delta = pointOnSquareEdge(5);
			x += delta[1];
			y += delta[0];
		}
		
		if (outsideWorld(y, x))
			return null;
		
		return new int[]{y, x};
	}

}
