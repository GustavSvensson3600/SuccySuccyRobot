package model;

public class Grid {
	private int w, h;
	
	public Grid(int w, int h) {
		this.w = w;
		this.h = h;
	}
	
	public boolean inside(int x, int y) {
		return x >= 0 && x < w && y >= 0 && y < h;
	}
}
