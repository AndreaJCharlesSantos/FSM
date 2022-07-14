/*
 * Author: Meta @ vidasconcurrentes
 * Related to: http://vidasconcurrentes.blogspot.com/2011/06/detectando-drag-drop-en-un-canvas-de.html
 * Edited by Andrea Charles, Eduardo Uriegas
 */

package com.upv.pm_2022.iti_27849_u2_equipo_04;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.inputmethod.InputMethodManager;

import com.upv.pm_2022.iti_27849_u2_equipo_04.DeleteDialog;

public class DragAndDropView extends SurfaceView implements SurfaceHolder.Callback,
		GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

	private DragAndDropThread thread;
	DeleteDialog deleteDialog;
	private ArrayList<Figure> figures;
	private int currentIndex;
	int id = 0;
	private final GestureDetector gestureDetector;
	private static final String TAG = "FSM_canvas";
	private final Paint p;

	public DragAndDropView(Context context) {
		super(context);
		getHolder().addCallback(this);
		gestureDetector = new GestureDetector(context, this);
		setFocusable(true);
		setFocusableInTouchMode(true);
		this.p = new Paint();
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// nothing here
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// Initialize data
		figures = new ArrayList<>();
		figures.add(new State(id++,500,500));
		currentIndex = -1;
		//Initialize paint
		p.setAntiAlias(true);
		// Initialize thread
		thread = new DragAndDropThread(getHolder(), this);
		thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Draws all the states aka circles
	 * @param canvas canvas to draw into
	 */
	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		if(figures != null)
			for(Figure figure : figures)
				 figure.draw(canvas);
	}

	// TODO: Move functionality from onTouchEvent to Gestures
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		int x = (int) event.getX(); int y = (int) event.getY();
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				getCurrentFigure(x,y);
				break;
			case MotionEvent.ACTION_MOVE:
				if(currentIndex == -2) {
					Arrow arrow = new Arrow(id++, x, y);
					figures.add(arrow);
					currentIndex = arrow.onDown(x,y);
				}
				else if(currentIndex != -1)
					if(figures.get(currentIndex) instanceof Arrow &&
					   ((Arrow)figures.get(currentIndex)).isLocked)
						break;
					figures.get(currentIndex).onMove(x, y);
				break;
			case MotionEvent.ACTION_UP:
				if(currentIndex != -1 && figures.get(currentIndex) instanceof Arrow)
					((Arrow)figures.get(currentIndex)).isLocked = true;
				currentIndex = -1;
				break;
		}
		return true;
	}

	@Override
	public boolean onDown(MotionEvent motionEvent) {
		Log.d(TAG, "On down: called");
		return false;
	}

	@Override
	public void onShowPress(MotionEvent motionEvent) {
		Log.d(TAG, "On show press: called");
	}

	@Override
	public boolean onSingleTapUp(MotionEvent motionEvent) {
		Log.d(TAG, "On single tap up press: called");
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
		Log.d(TAG, "On scroll: called");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent motionEvent) {
		Log.d(TAG, "On long press: called");
		deleteDialog.show();
//		arrows.add(new Arrow(id++, (int) motionEvent.getX(), (int) motionEvent.getY()));
	}

	/**
	 * When an arrow is created it will be locked, thus it can not be redrawn
	 * @param motionEvent event
	 * @param motionEvent1 new event
	 * @param v position v
	 * @param v1 position v1
	 * @return true if handled
	 */
	@Override
	public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
		Log.d(TAG, "On fling: called");
		// Andrea, este metodo se ejecuta cuando el usuario suelta el dedo despues de hacer un
		// `drag` (arrastrar el dedo).
		// Aqui obten los valores (x,y) y crea checa si esos valores estan dentro de un circulo
		// si esta fuera del circulo borra el Arrow.
		// HINT: itera sobre solo las figuras que son State (ignora las variables de tipo Arrow)
		int x = (int) motionEvent.getX(); int y = (int) motionEvent.getY();
		getCurrentFigure(x,y);
		if(currentIndex != -1 && figures.get(currentIndex) instanceof Arrow)
			((Arrow)figures.get(currentIndex)).isLocked = true;
		return true;
	}

	/**
	 * On single confirmed click, change name of the state
	 * @param motionEvent touch event
	 * @return true if handled
	 */
	@Override
	public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
		Log.d(TAG, "On single tap confirmed: called");
		int x = (int) motionEvent.getX(); int y = (int) motionEvent.getY();
		getCurrentFigure(x,y);

		if(currentIndex != -1 && figures.get(currentIndex) instanceof State) {
			// Open Keyboard
			requestFocus();
			((State)figures.get(currentIndex)).isEdited(true);
			InputMethodManager imm = (InputMethodManager) getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "Key " + (char) event.getUnicodeChar() + " pressed");
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.d(TAG, "Key Up " + (char) event.getUnicodeChar() + " pressed");
		//TODO: It makes sense having this code @onKeyDown() since that supports hardware keyboards
		if(currentIndex != -1 && figures.get(currentIndex) instanceof State) {
			State state = (State)figures.get(currentIndex);
			if (keyCode==KeyEvent.KEYCODE_DEL && state.name.length()>0)
				 state.name = state.name.substring(0, state.name.length()-1);
			else if(keyCode == KeyEvent.KEYCODE_ENTER) {
				clearFocus();
				state.isEdited(false);
				InputMethodManager imm = (InputMethodManager) getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindowToken(), 0);
			}
			else
				((State)figures.get(currentIndex)).name += (char) event.getUnicodeChar();
			Log.d(TAG, "State name changed to " + state.name);
		}
		return false;
	}

	/**
	 * On double click, set final or not final to the state,
	 * if clicked outside of a circle create a new circle
	 * @param motionEvent touch event
	 * @return true if handled
	 */
	@Override
	public boolean onDoubleTap(MotionEvent motionEvent) {
		Log.d(TAG, "On double tap: called");
		int x = (int) motionEvent.getX(); int y = (int) motionEvent.getY();
		getCurrentFigure(x,y);
		if(currentIndex != -1)
			figures.get(currentIndex).setFlag();
		else
			figures.add(new State(id++, x, y));
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent motionEvent) {
		Log.d(TAG, "On double tap event: called");
		return false;
	}

	/**
	 * Get the current State, that is the clicked Circle.
	 * <p>
	 * This method sets the attribute currentIndex, there is no need to set it outside
	 * <p>
	 * TODO: Handle case of clicking an arrow
	 * @param x position in the x-axis
	 * @param y position in the y-axis
	 * @return id of the current State
	 */
	private int getCurrentFigure(int x, int y) {
		for(Figure figure : figures)
			if(currentIndex == -1)
				currentIndex = figure.onDown(x, y);
		return currentIndex;
	}

	/**
	 * Get all figures in the canvas
	 * @return a list of figures
	 */
	public ArrayList<Figure> getAllFigures() {
		return this.figures;
	}
}