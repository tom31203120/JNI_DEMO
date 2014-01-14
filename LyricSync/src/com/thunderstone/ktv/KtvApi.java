package com.thunderstone.ktv;

public class KtvApi {
	static {
		System.loadLibrary("calc_score");
	}
	public native void NativeCalcInit(String filename);

	public native void NativeCalcUnInit();

	public native int NativeCalcScore(byte[] data, double start_time);
}
