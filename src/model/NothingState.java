package model;

public class NothingState extends State {

	public NothingState(Grid grid, int x, int y, int heading) {
		super(grid, x, y, heading);
	}
	
	@Override
	public int[] position() { return null; }

	public double emission(State other) {
		return 1.0 / (grid.rows() * grid.cols() * State.HEADINGS);
	}
}
