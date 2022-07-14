/*
 * Author: Meta @ vidasconcurrentes
 * Related to: http://vidasconcurrentes.blogspot.com/2011/06/detectando-drag-drop-en-un-canvas-de.html
 * Eduardo Uriegas - Added getX() and getY() methods
 */

package com.upv.pm_2022.iti_27849_u2_equipo_04;

import android.graphics.Canvas;

public abstract class Figure {
	protected int id;
	protected int x;
	protected int y;
	protected boolean flag;

	// Variables for toLatex implementations
	protected final static String DRAW_COMMAND	= "\\draw";
	protected final static String FILL_COMMAND 	= "\\fill";
	protected final static String COLOR       	= "[black]";
	protected final static int CONVERSION_RATIO 	= 27;

	public void draw(Canvas canvas){ }
	public int onDown(int touchX, int touchY){
		return this.id;
	}
	public void onMove(int touchX, int touchY){ }
	public void setFlag() { this.flag = !this.flag; }
	public int getX(){return this.x;}
	public int getY(){return this.y;}
	public abstract String toLatex(float resize_factor);
}
