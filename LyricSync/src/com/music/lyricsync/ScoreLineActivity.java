package com.music.lyricsync;

import java.util.ArrayList;
import java.util.List;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;

public class ScoreLineActivity extends Activity {

	private MyView mv;
	private int palytime=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mv = new MyView(this);
		setContentView(R.layout.animation_custom_evaluator);
		LinearLayout container = (LinearLayout) findViewById(R.id.container);
		container.addView(mv);
		final ObjectAnimator anim1 = ObjectAnimator
				.ofInt(mv, "scrollX", 0, 1400).setDuration(10000);
		anim1.setInterpolator(new LinearInterpolator());
		anim1.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				mv.scrollTo((Integer) animation.getAnimatedValue("scrollX"), 0);
			}
		});
		Button starter = (Button) findViewById(R.id.startButton);
		starter.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(anim1.isRunning()){
					palytime = mv.getScrollX();
					anim1.cancel();
					
				}else{
					mv.scrollTo(palytime, 0);
					anim1.start();
				}
			}
		});
	}

	

	@Override
	protected void onResume() {
		super.onResume();
	}

	private class MyView extends View {
		private List<MyPoint> plotList; //
		private float arrX[] = { 0.58f, 0.79f, 1.004f, 1.22f, 1.43f, 1.64f,
				1.98f, 2.98f, 3.12f, 3.5f, 3.9f, 4.5f, 4.98f, 5.12f, 5.5f,
				5.9f, 6.5f, 6.79f, 6.904f, 10.22f };
		private float arrY[] = { 1.0f, 2.0f, 3.0f, 2.0f, 7.0f, 4.0f, 2.0f,
				7.0f, 4.0f, 2.0f, 3.0f, 10f, 5.0f, 2.0f, 3.0f, 9f, 4.0f, 7.0f,
				8.0f };
		private Paint paint;
		private float AVG_X = 10f;
		private final int MAX_LEVER = 11;
		private final int MIN_LEVER = 1;
		private float AVG_PIX;

		public MyView(Context context) {
			super(context);
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
			setMeasuredDimension(6000, 400);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			AVG_PIX = getHeight() / MAX_LEVER;
			canvas.drawColor(Color.GRAY);
			for (int i = 0; i < plotList.size(); i++) {
				canvas.save();
				if (i % 2 == 0) {
					canvas.drawLine(plotList.get(i).x, plotList.get(i).y,
							plotList.get(i + 1).x, plotList.get(i + 1).y, paint);
				}
				canvas.restore();
			}
		}

		private void pointX() {
			for (int i = 0; i < arrX.length; i++) {
				arrX[i] = arrX[i] * 100;
			}
		}

		private void pointY() {
			for (int i = 0; i < arrY.length; i++) {
				arrY[i] = (MAX_LEVER - arrY[i]) * 20;// getHeight() / MAX_LEVER;
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
			pointX();
			pointY();
			ArrayList<MyPoint> list = new ArrayList<MyPoint>();
			MyPoint p = null;
			for (int i = 0; i < arrY.length; i++) {
				p = new MyPoint(arrX[i], arrY[i]);
				list.add(p);
				int j = i + 1;
				p = new MyPoint(arrX[j], arrY[i]);
				list.add(p);
			}
			return list;
		}
	}
}
