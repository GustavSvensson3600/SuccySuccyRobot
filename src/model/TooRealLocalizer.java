package model;

import control.EstimatorInterface;

public class TooRealLocalizer implements EstimatorInterface {
	
	private int rows, cols, head;
	private Sensor sensor;
	
	private Bot bot;
	private Grid grid;
	private State est;

	public TooRealLocalizer( int rows, int cols, int head) {		
		this.rows = rows;
		this.cols = cols;
		this.head = head;
		
		this.sensor = new Sensor(cols, rows);
		this.grid = new Grid(cols, rows);
		this.bot = new Bot(new State(grid, 4, 4, 0));
		this.est = bot.getState();
	}
	
	public int getNumRows() {
		return rows;
	}
	
	public int getNumCols() {
		return cols;
	}
	
	public int getNumHead() {
		return head;
	}
	
	public double getTProb( int y, int x, int h, int nY, int nX, int nH) {
		State origin = new State(grid, x, y, h);
		State target = new State(grid, nX, nY, nH);
		return origin.transition(target);
	}

	public double getOrXY( int rX, int rY, int x, int y) {
		return 0.2;
	}


	public int[] getCurrentTruePosition() {
		return bot.getState().position();
	}

	public int[] getCurrentReading() {
		return est.position();
	}


	public double getCurrentProb( int y, int x) {
		State s = bot.getState();
		return s.transition(new State(grid, x, y, State.NORTH)) +
				s.transition(new State(grid, x, y, State.EAST)) +
				s.transition(new State(grid, x, y, State.SOUTH)) +
				s.transition(new State(grid, x, y, State.WEST));
	}
	
	public State sample() {
		State s = bot.getState();
		// Only considers states with same direction
		double[][] E = s.emission();
		double r = Math.random();
		
		int nx = -1, ny = -1;
		for (int y = 0; y < E.length; y++) {
			for (int x = 0; x < E[y].length; x++) {
				r -= E[y][x];
				if (r <= 0) {
					nx = x; ny = y;
					break;
				}
			}
		}
		
		if (nx == -1 && ny == -1) {
			// "nothing"
			return null;
		}
		
		return s.offset(nx - 2, ny - 2, s.heading());
	}
	
	
	public void update() {
		// Update real world
		bot.update();
		
		// Update model
		State obs = sample();
		est = obs == null ? est : obs;
	}
}