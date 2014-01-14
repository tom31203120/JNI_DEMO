package com.music.lyricsync;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LevelLineView extends View {

	private Paint paint;
	private int lineColor = 0xff2b34b6;
	private final int MAX_LEVEL = 11;
	private float AVG_LEVER_PIX;

	public LevelLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LevelLineView(Context context) {
		super(context);
		init();
	}

	public void init() {
		paint = new Paint();
		paint.setColor(lineColor);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		AVG_LEVER_PIX = getHeight() / MAX_LEVEL;
		canvas.save();
		canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight(), paint);
		canvas.restore();
		for (int i = 0; i < MAX_LEVEL; i++) {
			canvas.save();
			canvas.drawLine(0, i * AVG_LEVER_PIX, getWidth(),
					i * AVG_LEVER_PIX, paint);
			canvas.restore();
		}

	}
}
