package model;

import java.util.ArrayList;
import java.util.Arrays;

public class State {
	public final static double CORRECT = 0.1;
	public final static double SQUARE_1 = 0.05;
	public final static double SQUARE_2 = 0.025;
	
	public final static int HEADINGS = 4;
	public final static int NORTH = 0; 
	public final static int EAST = 1;
	public final static int SOUTH = 2;
	public final static int WEST = 3;
	
	private int x, y, h;
	protected Grid grid;
	private double[][] T, E;
	
	public State(Grid grid, int x, int y, int heading) {
		this.y = y;
		this.h = heading;
		this.x = x;
		this.grid = grid;
	}
	
	
	public State step(int h) {
		int []dir = relativePosition(h);
		return offset(dir[1], dir[0], h);
	}

	public State forward() {
		return step(this.h);
	}
	
	public State offset(int ox, int oy, int h) {
		return new State(grid, x + ox, y + oy, h);
	}
	
	/** 
	 * The transition matrix is static b/c all states are static?
	 * @return the 3x3*4 transition matrix for this state
	 */
	public double[][] transition() {
		if (T == null) {
			T = compute_t();
		}
		return T;
	}
	
	private double[][] compute_t() {
		/* Format: t = [y][x + h], 2<=y<=2, 2<=x<=2, 0<=h<=3
		 * 
		 * 0,0N 0,0E 0,0S 0,0W ... 0,2N 0,2E 0,2S 0,2W
		 * 1,0N 1,0E 1,0S 1,0W ... 1,2N 1,2E 1,2S 1,2W
		 * 2,0N 2,0E 2,0S 2,0W ... 2,2N 2,2E 2,2S 2,2W
		 * 
		 */
		double[][] t = new double[3][3 * HEADINGS];
				
		ArrayList<State> possible = next();
		
		// Can we continue in forward direction?
		double p_forward = 0.7;
		State forward = this.forward();
		if (possible.remove(forward)) {
			int x = forward.x - this.x + 1;
			int y = forward.y - this.y + 1;
			t[y][x * 4 + this.h] = p_forward;
		} else {
			p_forward = 0;
		}
		
		// Split over adjacent states
		double p_other = (1.0 - p_forward) / possible.size();
		for (State next : possible) {			
			int x = next.x - this.x + 1;
			int y = next.y - this.y + 1;
			t[y][x * 4 + next.heading()] = p_other;

		}
		return t;
	}
	
	/**
	 * Find all possible states this state can transition to in one step.
	 * @return array list of states
	 */
	protected ArrayList<State> next() {
		ArrayList<State> possible = new ArrayList<State>(); 
		for (int h = 0; h < HEADINGS; h++) {
			State next = this.step(h);
			if (next.isValid())
				possible.add(next);
		}
		return possible;
	}
	
	
	/**
	 * Convert direction into movement.	
	 * @param h the heading: {@link #NORTH}, {@link #SOUTH},  {@link #EAST},  {@link #WEST}
	 * @return [y,x]
	 */
	private int[] relativePosition(int h) {
		switch (h) {
			case NORTH: return new int[]{-1,0};
			case EAST: return new int[]{0,1};
			case SOUTH: return new int[]{1,0};
			case WEST: return new int[]{0,-1};
		}
		return null;
	}

	/**
	 * Is this a valid state?
	 * @return true if position is inside grid
	 */
	public boolean isValid() { return grid.inside(this.x, this.y); }
	// public boolean isValid() { return hmm.hasState(this); }
	
	/**
	 * Check if it's possible to transition to other from this state
	 * @param other target state
	 * @return true if it's possible to transition, false otherwise
	 */
	public boolean reachable(State other) {
		int dx = this.x - other.x;
		int dy = this.y - other.y;
		return Math.abs(dx) <= 1 && Math.abs(dy) <= 1;
	}
	
	/**
	 * The emission matrix is static b/c all states are static?
	 * @return the 5x5 emission matrix for this state
	 */
	public double[][] emission() {
		if (E == null) {
			E = compute_e();
		}
		return E;	}
	
	private double[][] compute_e() {
		double[][] mat = new double[5][5];
		int n_s1 = 0;
		int n_s2 = 0;
		
		// true position
		mat[2][2] = CORRECT;
		
		// false positions (L_s1, L_s2)
		for (int y = 0; y < 5; y++) {
			for (int x = 0; x < 5; x++) {
				// ignore center b/c it's not on any edge
				if (x == 2 && y == 2) 
					continue;
				
				boolean inside = grid.inside(this.x + x - 2, this.y + y - 2);
				if (inside) {
					if ((y >= 1 && y <= 3) && (x >= 1 && x <= 3)) {
						n_s1 += 1;
						mat[y][x] = SQUARE_1;
					} else {
						n_s2 += 1;
						mat[y][x] = SQUARE_2;
					}
				} else {
					mat[y][x] = 0;
				}
			}	
		}
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
		int x = (other.x - this.x) + 1;
		int y = (other.y - this.y) + 1;
		
		return transition()[y][x * 4 + other.h];
	}

	/**
	 * World position.
	 * @return [y,x]
	 */
	public int[] position() {
		return new int[]{y, x};
	}


	public int heading() {
		return h;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof State)) return false;
		State other = (State)obj;
		return other.x == x && other.y == y && other.h == h;
	}
	
	@Override
	public int hashCode() {
		return ("" + x + "" + y + "" + h).hashCode();
	}


	public double emission(State xi) {
		int dx = xi.x - this.x;
		int dy = xi.y - this.y;
		
		if (Math.abs(dx) > 2 || Math.abs(dy) > 2)
			return 0;

		return emission()[dy + 2][dx + 2] / 4;
	}

}
