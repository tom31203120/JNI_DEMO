package com.music.lyricsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.thunderstone.ktv.KtvApi;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

public class RecordManager {
	// 音频获取源
	private int audioSource = MediaRecorder.AudioSource.VOICE_DOWNLINK;
	// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
	private static int sampleRateInHz = 44100;
	// 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
	// Notice：录音时双声道有问题
	private static int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	// 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
	private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	// 缓冲区字节大小
	private int bufferSizeInBytes = 0;

	private AudioRecord audioRecord;
	private boolean isRecord = false;// 设置正在录制的状态
	// AudioName裸音频数据文件
	private static final String AudioName = "/sdcard/love_.raw";
	// NewAudioName可播放的音频文件
	private static final String NewAudioName = "/sdcard/new_.wav";
	private static final String API_JSON="/sdcard/api.json";

	private double buttferTime = 0d;
	private static RecordManager manager;
	private Context mContext;
	private KtvApi api;
	public static RecordManager getInstance(Context context){
		if(manager==null){
			manager = new RecordManager();
			manager.initRecordManager(context);
		}
		return manager;
	}
	
	private RecordManager(){
		
	}
	
	public void initRecordManager(Context context) {
		api = new KtvApi();
		mContext=context;
		// 音是调节
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		int currentVolume = audioManager
				.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
		int maxVolume = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
		Toast.makeText(context, "on Ready_AB_NORMAL", Toast.LENGTH_SHORT)
				.show();
		Log.i("out", "onReady.....current -> " + currentVolume + "    max -> "
				+ maxVolume);
	}

	public void creatAudioRecord() {
		// 获得缓冲区字节大小
		bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
				channelConfig, audioFormat);
		/**
		 * 
		 *  一个buffer 的时间 
		 *  每个样本是2个字节；  bufferSizeInBytes/2d ==一共采了多少个样本
		 *   在根据样本数/每秒钟采样的多少，算出buffer时间
		 */
		buttferTime = 1d * bufferSizeInBytes / 2d / sampleRateInHz;
		Log.i("out", "bufferSizeInByte: " + bufferSizeInBytes + " xxx: "
				+ buttferTime);
		// 创建AudioRecord对象
		audioRecord = new AudioRecord(audioSource, sampleRateInHz,
				channelConfig, audioFormat, bufferSizeInBytes);
		Log.i("out", "createAudioRecord object");
	}

	public void startRecord() {
		audioRecord.startRecording();
		// 让录制状态为true
		isRecord = true;
		// 开启音频文件写入线程
		Toast.makeText(mContext, "开始录音", Toast.LENGTH_SHORT).show();
		new Thread(new AudioRecordThread()).start();
	}

	class AudioRecordThread implements Runnable {
		@Override
		public void run() {
			writeDateTOFile();// 往文件中写入裸数据
			copyWaveFile(AudioName, NewAudioName);// 给裸数据加上头文件
		}
	}

	/**
	 * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频，
	 * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理
	 */
  private double start_time = 0d;
	private void writeDateTOFile() {
		// new一个byte数组用来存一些字节数据，大小为缓冲区大小
		byte[] audiodata = new byte[bufferSizeInBytes];
		// byte[] audiodata = new byte[640];
		FileOutputStream fos = null;
		int readsize = 0;
		try {
			File file = new File(AudioName);
			if (file.exists()) {
				file.delete();
			}
			fos = new FileOutputStream(file);// 建立一个可存取字节的文件
			api.NativeCalcInit(API_JSON);
			while (isRecord == true) {
				readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
				if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
					buttferTime = 1d * readsize / 2d / sampleRateInHz;
					start_time += buttferTime;
					Log.i("out", "start_time: " + start_time+ " -------");
					int score = api.NativeCalcScore(audiodata, start_time);
					fos.write(audiodata);
					Log.i("out", "score: " + score + " -------" + audiodata.toString());
				}
			}
		} catch (Exception e) {
			Log.i("out", e.toString());
		} finally {
			try {
				fos.close();// 关闭写入流
			} catch (IOException e) {
				Log.i("out", e.toString());
			}
		}
		Log.i("out", "录音结束");
	}

	// 这里得到可播放的音频文件
	private void copyWaveFile(String inFilename, String outFilename) {
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = sampleRateInHz;
		int channels = 1;
		long byteRate = 16 * sampleRateInHz * channels / 8;
		byte[] data = new byte[bufferSizeInBytes];
		try {
			File outFile = new File(outFilename);
			if (outFile.exists()) {
				outFile.delete();
			}
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			while (in.read(data) != -1) {
				out.write(data);
			}
		} catch (Exception e) {
			Log.i("out", e.toString());
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				Log.i("out", e.toString());
			}
		}
		Log.i("out", "得到可播放的音频文件结束");
	}
	
	/**
	 * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
	 * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
	 * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有
	 * 自己特有的头文件。
	 */
	private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {
		byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.write(header, 0, 44);
	}
	
	public void stopRecord() {
		Toast.makeText(mContext, "结束录音", Toast.LENGTH_SHORT).show();
		close();
	}

	private void close() {
		if (audioRecord != null) {
			System.out.println("stopRecord");
			isRecord = false;//停止文件写入
			audioRecord.stop();
			audioRecord.release();//释放资源
			audioRecord = null;
		}
	}
}
