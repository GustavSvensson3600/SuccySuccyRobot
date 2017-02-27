package model;

import java.util.ArrayList;
import java.util.Random;

import control.EstimatorInterface;

public class TooRealLocalizer implements EstimatorInterface {
	
	private enum Heading {
		NORTH, EAST, WEST, SOUTH
	}

	private int rows, cols, head;
	private int[] truePos, estPos;
	private Heading currentH;
	private Sensor sensor;

	public TooRealLocalizer( int rows, int cols, int head) {		
		this.rows = rows;
		this.cols = cols;
		this.head = head;
		
		this.sensor = new Sensor(cols, rows);
		// this.hmm
		initialState();
	}

	private void initialState() {
		truePos = new int[2];
		truePos[0] = rows/2;
		truePos[1] = cols/2;
		
		estPos = new int[2];
		estPos[0] = truePos[0];
		estPos[1] = truePos[1];
		
		currentH = Heading.NORTH;
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
	
	public double getTProb( int x, int y, int h, int nX, int nY, int nH) {
		return 0.0;
	}

	public double getOrXY( int rX, int rY, int x, int y) {
		return 0.2;
	}


	public int[] getCurrentTruePosition() {
		return truePos;
	}

	public int[] getCurrentReading() {
		return estPos;
	}


	public double getCurrentProb( int x, int y) {
		double ret = 0.1;
		return ret;
	}
	
	private void move() {
		int dx = 0, dy = 0;
		switch (currentH) {
		case WEST: dx = -1; break;
		case EAST: dx = 1; break;
		case NORTH: dy = -1; break;
		case SOUTH: dy = 1; break;
		}
		
		truePos[0] = Math.min(Math.max(truePos[0] + dy, 0), rows-1);
		truePos[1] = Math.min(Math.max(truePos[1] + dx, 0), cols-1);
	}
	
	/* ?? */
	private Heading getHead(int y, int x, Heading old) {
		ArrayList<Heading> available = new ArrayList<Heading>();
		if (x > 0) available.add(Heading.WEST);
		if (x < cols - 1) available.add(Heading.EAST);
		if (y > 0) available.add(Heading.NORTH);
		if (y < rows - 1) available.add(Heading.SOUTH);
		available.remove(old);
		Random rng = new Random();
		return available.get(rng.nextInt(available.size()));
	}
	
	
	// [0, 0, 0.05, 0, 0.05, 0, 0, 0.05, ] = L_s1
	// .... = L_s1
	public void update() {
		Random rng = new Random();
		
		
		/* 
		 * 	P( h_t+1 = h_t | not encountering a wall) = 0.7
			P( h_t+1 != h_t | not encountering a wall) = 0.3
			P( h_t+1 = h_t | encountering a wall) = 0.0
			P( h_t+1 != h_t | encountering a wall) = 1.0
		*/
		
		int[] stimulai = sensor.sample(truePos);
		boolean found_wall = stimulai == null;
		//P(wall | s_t och s_t-1)
		
		if (found_wall) {
			// problem
		} else {
			if (rng.nextFloat() >= 0.75) {
				// P( h_t+1 != h_t | not encountering a wall) = 0.3
				currentH = getHead(truePos[1], truePos[0], currentH);
			} else {
				// keep moving
			}
		}
		
		move();
		
		System.out.println("Nothing is happening, no model to go for...");
	}
	
	
}