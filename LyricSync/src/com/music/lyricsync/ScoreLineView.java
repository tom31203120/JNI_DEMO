package com.music.lyricsync;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ScoreLineView extends View {
	private List<MyPoint> plotList; //
	private float arrX[];
	private float arrY[];
	private Paint paint;
	private float AVG_X = 10f;
	private final int MAX_LEVEL = 11;
	private final int MIN_LEVEL = 1;
	private float AVG_LEVER_PIX;
	private float AVG_PER_X = 100;
	private float xOffset;

	public ScoreLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ScoreLineView(Context context) {
		super(context);
		init();
	}
	
	public void setArrX(float arrX[]){
		this.arrX = arrX;
		plotList = mapPoint();
	}
	public void setArrY(float arrY[]){
		this.arrY = arrY;
		plotList = mapPoint();
	}
	
	private void init() {
		if (paint == null) {
			paint = new Paint();
			paint.setColor(Color.RED);
			paint.setAntiAlias(true);
			paint.setStrokeWidth(8);
		}
		plotList = mapPoint();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// setMeasuredDimension(6000, 400);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		AVG_LEVER_PIX = getHeight() / MAX_LEVEL;
		xOffset = getWidth()/2;
		canvas.drawColor(0x00000000);
		for (int i = 0; i < plotList.size(); i++) {
			canvas.save();
			if (i % 2 == 0 && plotList.get(i).x >=0.f ) {
				if (plotList.get(i+1).x<=18.81){
					paint.setStrokeWidth(2);
				}else{
					paint.setStrokeWidth(8);
				}
				paint.setColor(Color.GREEN);
				canvas.drawPoint(plotList.get(i).x * AVG_PER_X + xOffset, plotList.get(i).y * AVG_LEVER_PIX, paint);
				paint.setColor(Color.RED);
				canvas.drawLine(plotList.get(i).x * AVG_PER_X + xOffset, plotList.get(i).y * AVG_LEVER_PIX,
						plotList.get(i + 1).x * AVG_PER_X + xOffset , plotList.get(i + 1).y * AVG_LEVER_PIX, paint);
			}
			canvas.restore();
		}
	}

	private void pointX() {
		for (int i = 0; arrX !=null && i < arrX.length; i++) {
			arrX[i] = arrX[i] * 100;
		}
	}

	private void pointY() {
		for (int i = 0; arrY !=null && i < arrY.length; i++) {
			arrY[i] = (MAX_LEVEL - arrY[i]);// getHeight() / MAX_LEVER;
		}
	}

	private class MyPoint {
		float x;
		float y;

		@Override
		public String toString() {
			return "MyPoint [x=" + x + ", y=" + y + "]";
		}

		public MyPoint(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	private List<MyPoint> mapPoint() {
		//pointX();
		pointY();
		ArrayList<MyPoint> list = new ArrayList<MyPoint>();
		MyPoint p = null;
		for (int i = 0; arrY!=null && i < arrY.length; i++) {
			p = new MyPoint(arrX[i], arrY[i]);
			list.add(p);
			int j = i + 1;
			p = new MyPoint(arrX[j], arrY[i]);
			list.add(p);
		}
		return list;
	}
}
