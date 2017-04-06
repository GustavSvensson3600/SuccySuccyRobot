package model;

import java.util.Random;

import control.EstimatorInterface;

public class TooRealLocalizer implements EstimatorInterface {
	
	private int rows, cols, head;
	private int num_states;

	private double[] F;
	private Bot bot;
	private Grid grid;
	private State sensor;
	
	private double[][] Tt;
	
	private long totalManhattanError = 0, totalHits = 0, timestep = 0;
	
	private State int2state(int i) {
		//    s 1-3   ...     sN
		//    x0      x1      x2
		// y0 h/h/h/h h/h/h/h h/h/h/h
		// y1 h/h/h/h h/h/h/h h/h/h/h
		int states_per_row = head * cols;
		int y = i / states_per_row;
		int x = (i % states_per_row) / head;
		int h = (i % states_per_row) % head;
		return new State(grid, x, y, h);
	}

	public TooRealLocalizer( int rows, int cols, int head) {		
		this.rows = rows;
		this.cols = cols;
		this.head = head;
		
		this.grid = new Grid(cols, rows);
		this.bot = new Bot(new State(grid, 4, 4, 0));
		this.sensor = new NothingState(grid, 0, 0, 0);
		
		// precompute the transition matrix: Tij = P(Sj | Si)
		num_states = rows * cols * head; // 256x256
		this.Tt = new double[num_states][num_states];
		for (int i = 0; i < num_states; i++) {
			State from = int2state(i);
			for (int j = 0; j < num_states; j++) {
				State to = int2state(j);
				// Shitty computer science hack to transpose
				Tt[j][i] = from.transition(to);
			}
		}
		
		F = new double[num_states];
		for (int i = 0; i < num_states; i++){
			F[i] = 1.0 / num_states;
		}
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
		int i = y * cols * head + x * 4 + h;
		int j = nY * cols * head + nX * 4 + nH; 
		return Tt[j][i];
	}

	public double getOrXY( int rY, int rX, int y, int x) {
		State origin;
		
		if (rX == -1 || rY == -1) {
			origin = new NothingState(grid, rX, rY, 0);
		} else {
			origin = new State(grid, rX, rY, 0);
		}
		
		State other = new State(grid, x, y, 0);
		return origin.emission(other) * 4;
	}


	public int[] getCurrentTruePosition() {
		return bot.getState().position();
	}

	public int[] getCurrentReading() {
		return sensor.position();
	}

	public double getCurrentProb( int y, int x) {
		int north = y * cols * head + x * head + State.NORTH;
		int east = y * cols * head + x * head + State.EAST;
		int south = y * cols * head + x * head + State.SOUTH;
		int west = y * cols * head + x * head + State.WEST;
		return F[north] + F[east] + F[south] + F[west]; 
	}
	
	public double[][] sample() {
		State s = bot.getState();
		
		// Only considers states with same direction
		double[][] E = s.emission();
		
		double r = Math.random();
		sensor = new NothingState(grid, 0, 0, 0);
		for (int y = 0; y < 5 && r > 0; y++) {
			for (int x = 0; x < 5 && r > 0; x++) {
				r -= E[y][x];
				if (r <= 0)
					sensor = s.offset(x - 2, y - 2, s.heading());
			}
		}
		
		double[][] O = new double[num_states][num_states];
		for (int i = 0; i < num_states; i++) {
			State Xi = int2state(i);
			O[i][i] = sensor.emission(Xi);
		}
		
		return O;
	}
	
	
	public void update() {
		timestep++;
		
		// Update real world
		bot.update();
		
		// Update model
		double[][] O = sample();
		
		// F_t+1 = a * O * Tt * F_t
		
		// X = O * Tt ( O(n*n*n) :) )
		double[][] X = new double[num_states][num_states];

		// O is a diagonal matrix = zeroes except for diagonal
		for (int i = 0; i < num_states; i++) {
			for (int j = 0; j < num_states; j++) {
				X[i][j] = O[j][j] * Tt[i][j];
			}
		}
		
		double sum = 0.0;
		double[] F_t = new double[num_states];
		for (int i = 0; i < num_states; i++) {
			for (int j = 0; j < num_states; j++) {
				F_t[i] = F_t[i] + X[i][j] * F[j];
			}
			sum += F_t[i];
		}
		
		// F = NORMALIZE(F_t)
		double a = 1.0 / sum;
		int prediction = 0;
		double max_pred = 0;
		for (int i = 0; i < num_states; i++) {
			F[i] = F_t[i] * a;
			if (F[i] > max_pred) {
				max_pred = F[i];
				prediction = i;
			}
		}
		
		int[] pos = getCurrentTruePosition();
		//int[] pos = bot.simulate().position();
		//int[] guess = int2state(prediction).position();
		//int[] guess = getCurrentReading();
		int[] guess = int2state((new Random()).nextInt(num_states)).position();
		if (guess == null) {
			guess = new int[]{getNumRows() / 2, getNumCols() / 2};
		}
		
		totalManhattanError += Math.abs(pos[0] - guess[0]);
		totalManhattanError += Math.abs(pos[1] - guess[1]);
		
		if (pos[0] == guess[0] && pos[1] == guess[1]) {
			totalHits++;
		}
		System.out.println("t=" + timestep + ", ame=" + ((totalManhattanError+0.0)/timestep) + ", hitrate=" + ((totalHits+0.0)/timestep));
	}
}