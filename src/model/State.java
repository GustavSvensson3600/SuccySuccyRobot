package model;

import java.util.ArrayList;

public class State {
	public final static int MATRIX_SIZE = 5;
	public final static int MATRIX_SIZE_HALF = 2;
	
	public final static double CORRECT = 0.1;
	public final static double SQUARE_1 = 0.05;
	public final static double SQUARE_2 = 0.025;
	
	private int x, y, h;
	
	public State(int x, int y, int heading) {
		this.y = y;
		this.h = heading;
		this.x = x;
	}
	
	private boolean onSquareEdge(int x, int y, int size) {
		size -= 1; // 0..size-1 inclusive
		return x == size || y == size || x == 0 || y == 0;
	}

	/** 
	 * The transition matrix is static b/c all states are static?
	 * @return the 5x5 transition matrix for this state
	 */
	public double[][] transition() {
		ArrayList<Integer> possibleH;
		double[][] t = new double[5][5];
		double pool = 1;
		if(crash(x,y,h)) {
			//Going to crash -> Change direction
			possibleH = getHeadings(h);
		} else {
			//No crash -> 0.6 on current direction
			// 0.4 -> divided equally on possible heading changes
			int [] relPos = relativePosition(h);
			pool -= 0.6;
			t[relPos[0]][relPos[1]] = 0.6;
			possibleH = getHeadings(h);
		}
		//Divide up rest of pool on possible headings
		for (int i = 0; i<possibleH.size(); i++) {
			int head = possibleH.get(i);
			int[] relPos = relativePosition(head);
			t[relPos[0]][relPos[1]] = pool / possibleH.size();
		}
		return t;
	}
	
	private ArrayList<Integer> getHeadings(int h) {
		ArrayList<Integer> possibleH = new ArrayList<Integer>(); 
		for (int i=1; i<=4; i++) {
			if(i != h) {
				if(!crash(x,y,i)) {
					possibleH.add(i);
				}
			}
		}
		return possibleH;
	}
	
	
	
	private int[] relativePosition(int h) {
		//North = 1, East = 2, West = 3, South = 4.
		switch (h) {
			case 1: break;
			case 2: break;
			case 3: break;
			case 4: break;
		}
			
		return null;
	}

	private boolean crash(int x, int y, int h) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean insideGrid(int worldx, int worldy) {
		return true;
	}
	
	/**
	 * Check if it's possible to transition to other from this state
	 * @param other target state
	 * @return true if it's possible to transition, false otherwise
	 */
	public boolean reachable(State other) {
		int dx = this.x - other.x;
		int dy = this.y - other.y;
		return Math.abs(dx) <= 2 && Math.abs(dy) <= 2;
	}
	
	/**
	 * The emission matrix is static b/c all states are static?
	 * @return the emission matrix for this state
	 */
	public double[][] emission() {
		double[][] mat = new double[MATRIX_SIZE][MATRIX_SIZE];
		int n_s1 = 0;
		int n_s2 = 0;
		
		// true position
		mat[MATRIX_SIZE_HALF][MATRIX_SIZE_HALF] = CORRECT;
		
		// false positions (L_s1, L_s2)
		for (int y = 0; y < MATRIX_SIZE; y++) {
			for (int x = 0; x < MATRIX_SIZE; x++) {
				// ignore center b/c it's not on any edge
				if (x == MATRIX_SIZE_HALF && y == MATRIX_SIZE_HALF) 
					continue;
				
				boolean inside = insideGrid(this.x + x - MATRIX_SIZE_HALF, this.y + y - MATRIX_SIZE_HALF);
				if (inside) {
					if (onSquareEdge(x - 1, y - 1, 3)) {
						n_s1 += 1;
						mat[y][x] = SQUARE_1;
					} else if (onSquareEdge(x, y, 5)) {
						n_s2 += 1;
						mat[y][x] = SQUARE_2;
					}
				} else {
					mat[y][x] = 0;
				}
			}	
		}
		double nothing = 1.0 - CORRECT - n_s1 * SQUARE_1 - n_s2 * SQUARE_2; 
		// TODO: somehow return nothing
		return mat;
	}
	
	/**
	 * 
	 * @param other state to transition to
	 * @return the probability to transition to other [0,1]
	 */
	public double transition(State other) {
		if (!reachable(other))
			return 0;
		
		// Important: dx,dy is vector from this to other
		// Adding half matrix size gives position in matrix
		int x = (other.x - this.x) + MATRIX_SIZE_HALF;
		int y = (other.y - this.y) + MATRIX_SIZE_HALF;
		
		return transition()[y][x];
	}
}
