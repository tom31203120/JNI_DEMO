package com.music.lyricsync;

import java.io.FileDescriptor;
import java.io.IOException;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity {
	/** Called when the activity is first created. */
	private LyricView lyricView;
	private MediaPlayer mediaPlayer;
	private Button button;
	private SeekBar seekBar;
	private String mp3Path;
	private int INTERVAL = 45;// 歌词每行的间隔
	private ScoreLineView mv;
	ObjectAnimator anim1; // 唱歌部分的动画
	ObjectAnimator anim2; // 结束最后的动画
	private String mFileName = null;
	private long playtime = 0;
	private int scrollXEndPostion = 0;
	private boolean isRecord = false;

	public void record(View view) {
		RecordManager.getInstance(this).creatAudioRecord();
		RecordManager.getInstance(this).startRecord();
	}

	public void replay(View view) {
		mv.scrollTo(0, 0);
		initLineAnimation();
		ResetMusic(mp3Path);
		lyricView.SetTextSize();
		lyricView.setOffsetY(200);
		mediaPlayer.start();
		anim1.start();
	}

	private float arrX[] = { 0f, 0.58f, 0.792f, 1.004f, 1.216f, 1.428f, 1.64f,
			5.074f, 8.508f, 11.942f, 15.376f, 18.81f, 19.938f, 21.066f,
			22.194f, 23.322f, 24.45f, 25.578f, 26.706f, 27.834f, 28.962f,
			30.09f, 31.206f, 32.322f, 33.438f, 34.554f, 35.67f, 36.558f,
			37.446f, 38.334f, 39.222f, 40.11f, 40.334f, 40.558f, 40.782f,
			41.006f, 41.23f, 42.344f, 43.458f, 44.572f, 45.686f, 46.8f, 47.9f,
			49.0f, 50.1f, 51.2f, 52.3f, 53.448f, 54.596f, 55.744f, 56.892f,
			58.04f, 58.76f, 59.48f, 60.2f, 60.92f, 61.64f, 62.02f, 62.4f,
			62.78f, 63.16f, 63.54f, 64.634f, 65.728f, 66.822f, 67.916f, 69.01f,
			70.128f, 71.246f, 72.364f, 73.482f, 74.6f, 75.704f, 76.808f,
			77.912f, 79.016f, 80.12f, 81.074f, 82.028f, 82.982f, 83.936f,
			84.89f, 85.086f, 85.282f, 85.478f, 85.674f, 85.87f, 86.984f,
			88.098f, 89.212f, 90.326f, 91.44f, 92.526f, 93.612f, 94.698f,
			95.784f, 96.87f, 97.994f, 99.118f, 100.242f, 101.366f, 102.49f,
			104.214f, 105.938f, 107.662f, 109.386f, 111.11f, 116.078f,
			121.046f, 126.014f, 130.982f, 135.95f, 137.07f, 138.19f, 139.31f,
			140.43f, 141.55f, 142.664f, 143.778f, 144.892f, 146.006f, 147.12f,
			148.232f, 149.344f, 150.456f, 151.568f, 152.68f, 153.602f,
			154.524f, 155.446f, 156.368f, 157.29f, 157.488f, 157.686f,
			157.884f, 158.082f, 158.28f, 159.392f, 160.504f, 161.616f,
			162.728f, 163.84f, 164.936f, 166.032f, 167.128f, 168.224f, 169.32f,
			170.454f, 171.588f, 172.722f, 173.856f, 174.99f, 175.94f, 176.89f,
			177.84f, 178.79f, 179.74f, 179.918f, 180.096f, 180.274f, 180.452f,
			180.63f, 181.726f, 182.822f, 183.918f, 185.014f, 186.11f, 187.24f,
			188.37f, 189.5f, 190.63f, 191.76f, 192.856f, 193.952f, 195.048f,
			196.144f, 197.24f, 198.718f, 200.196f, 201.674f, 203.152f, 204.63f,
			204.842f, 205.054f, 205.266f, 205.478f, 205.69f, 206.784f,
			207.878f, 208.972f, 210.066f, 211.16f, 212.284f, 213.408f,
			214.532f, 215.656f, 216.78f, 217.882f, 218.984f, 220.086f,
			221.188f, 222.29f, 223.262f, 224.234f, 225.206f, 226.178f, 227.15f,
			227.314f, 227.478f, 227.642f, 227.806f, 227.97f, 229.064f,
			230.158f, 231.252f, 232.346f, 233.44f, 234.552f, 235.664f,
			236.776f, 237.888f, 239.0f, 240.122f, 241.244f, 242.366f, 243.488f,
			244.61f, 247.02f, 249.43f, 251.84f, 254.25f, 256.66f, };
	private float arrY[] = { 1, 2, 3, 2, 2, 3, 3, 3, 5, 5, 3, 4, 4, 4, 4, 3, 4,
			3, 3, 3, 3, 5, 5, 4, 4, 4, 3, 4, 4, 4, 4, 4, 4, 3, 3, 3, 5, 5, 5,
			5, 4, 5, 4, 3, 5, 5, 6, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 4, 4, 5,
			9, 6, 6, 6, 6, 7, 7, 6, 7, 6, 8, 6, 6, 7, 6, 6, 10, 7, 7, 10, 9, 8,
			9, 9, 10, 10, 7, 6, 6, 6, 6, 6, 6, 7, 6, 7, 6, 6, 7, 6, 9, 7, 6, 9,
			7, 6, 7, 7, 7, 6, 8, 7, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 6, 6, 6, 6,
			6, 6, 7, 8, 7, 6, 7, 6, 7, 10, 9, 8, 7, 7, 8, 7, 6, 8, 7, 9, 8, 8,
			7, 7, 8, 9, 8, 9, 9, 8, 9, 8, 8, 8, 10, 9, 8, 8, 7, 7, 6, 8, 9, 7,
			9, 8, 6, 7, 7, 9, 9, 6, 9, 10, 8, 9, 8, 6, 8, 9, 8, 8, 7, 7, 8, 8,
			7, 8, 7, 8, 7, 7, 7, 8, 8, 9, 9, 10, 9, 10, 9, 9, 10, 9, 10, 8, 8,
			9, 9, 8, 8, 9, 9, 7, 8, 8, 7, 8, 8, 11, 6, 6, 4, 4 };

	public void stopRecord(View view) {
		//
		isRecord=false;
		RecordManager.getInstance(this).stopRecord();
	}

	private void stopRecording() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);
		mv = (ScoreLineView) findViewById(R.id.svLine);
		mv.setArrX(arrX);
		mv.setArrY(arrY);
		initLineAnimation();
		mp3Path = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/LyricSync/7013785.wav";
		lyricView = (LyricView) findViewById(R.id.mylrc);
		mediaPlayer = new MediaPlayer();
		// this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		ResetMusic(mp3Path);
		SerchLrc();
		lyricView.SetTextSize();
		button = (Button) findViewById(R.id.button);
		button.setText("播放");

		seekBar = (SeekBar) findViewById(R.id.seekbarmusic);

		seekBar.setOnSeekBarChangeListener(seekbarListener);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mediaPlayer.isPlaying()) {
					button.setText("播放");
					mediaPlayer.pause();
//					RecordManager.getInstance(MainActivity.this).creatAudioRecord();
//					RecordManager.getInstance(MainActivity.this).startRecord();
					stopRecord(null);
					anim1.cancel();
				} else {
					button.setText("暂停");
					mediaPlayer.start();
					record(null);
					lyricView.setOffsetY(220
							- lyricView.SelectIndex(mediaPlayer
									.getCurrentPosition())
							* (lyricView.getSIZEWORD() + INTERVAL - 1));
					anim1.start();
				}
			}
		});

		mediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						ResetMusic(mp3Path);
						lyricView.SetTextSize();
						lyricView.setOffsetY(200);
						mediaPlayer.start();
					}
				});
		seekBar.setMax(mediaPlayer.getDuration());
		new Thread(new runable()).start();
	}

	private void initLineAnimation() {
		playtime = (long) (1000 * arrX[arrX.length - 1]);
		scrollXEndPostion = (int) arrX[arrX.length - 1] * 100;
		if (anim1 != null) {
			anim1.cancel();
			anim1.removeAllListeners();
			anim1.removeAllUpdateListeners();
			anim1 = null;
		}
		anim1 = ObjectAnimator.ofInt(mv, "scrollX", 0, scrollXEndPostion)
				.setDuration(playtime);
		// anim1 = ObjectAnimator
		// .ofInt(mv, "scrollX", 0, scrollXEndPostion).setDuration(1000);
		anim1.setInterpolator(new LinearInterpolator());
		anim1.addUpdateListener(updateListener);
		anim1.addListener(animatorListener);
	}

	public void SerchLrc() {
		String lrc = mp3Path;
		lrc = lrc.substring(0, lrc.length() - 4).trim() + ".lrc".trim();
		LyricView.read(lrc);
		lyricView.SetTextSize();
		lyricView.setOffsetY(350);
	}

	/**
	 * 从新设置监听位置
	 * 
	 * @param listener
	 */
	private void resetScoreLine(Animator.AnimatorListener listener, int scrollX) {
		anim1.removeListener(listener);
		anim1.removeAllUpdateListeners();
		long curentTime = anim1.getCurrentPlayTime();
		anim1 = null;
		anim1 = ObjectAnimator.ofInt(mv, "scrollX", scrollX, scrollXEndPostion)
				.setDuration(playtime - curentTime);
		anim1.setInterpolator(new LinearInterpolator());
		anim1.addUpdateListener(MainActivity.this.updateListener);
		anim1.addListener(listener);
	}

	public void ResetMusic(String path) {

		mediaPlayer.reset();
		try {

			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			if (fromUser) {
				mediaPlayer.seekTo(progress);
				lyricView.setOffsetY(220 - lyricView.SelectIndex(progress)
						* (lyricView.getSIZEWORD() + INTERVAL - 1));
			}
		}
	};
	AnimatorListener animatorListener = new AnimatorListener() {

		@Override
		public void onAnimationStart(Animator animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationEnd(Animator animation) {
			anim2 = ObjectAnimator.ofInt(mv, "scrollX", mv.getScrollX(),
					mv.getScrollX() + 560).setDuration(2000);
			anim2.setInterpolator(new LinearInterpolator());
			anim2.addUpdateListener(updateListener);
			anim2.start();
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			resetScoreLine(this, mv.getScrollX());
		}
	};
	AnimatorUpdateListener updateListener = new AnimatorUpdateListener() {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			// TODO Auto-generated method stub
			int scrollX = (Integer) animation.getAnimatedValue("scrollX");
			mv.scrollTo(scrollX, 0);
		}
	};

	class runable implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				try {
					Thread.sleep(100);
					if (mediaPlayer.isPlaying()) {
						lyricView.setOffsetY(lyricView.getOffsetY()
								- lyricView.SpeedLrc());
						lyricView.SelectIndex(mediaPlayer.getCurrentPosition());
						seekBar.setProgress(mediaPlayer.getCurrentPosition());
						mHandler.post(mUpdateResults);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	Handler mHandler = new Handler();
	Runnable mUpdateResults = new Runnable() {
		public void run() {
			lyricView.invalidate(); // 更新视图
		}
	};
}
