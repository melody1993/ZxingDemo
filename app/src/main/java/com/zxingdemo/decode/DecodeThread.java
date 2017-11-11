package com.zxingdemo.decode;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;

/**
 *
 * 描述: 解码线程
 */
final class DecodeThread extends Thread {

	Activity activity;
	private Handler handler;
	private final CountDownLatch handlerInitLatch;
	CaptureActivityHandler handlers;
	DecodeThread(Activity activity,CaptureActivityHandler handler) {
		this.activity = activity;
		handlerInitLatch = new CountDownLatch(1);
		this.handlers=handler;
	}

	Handler getHandler() {
		try {
			handlerInitLatch.await();
		} catch (InterruptedException ie) {
			// continue?
		}
		return handler;
	}

	@Override
	public void run() {
		Looper.prepare();
		handler = new DecodeHandler(activity,handlers);
		handlerInitLatch.countDown();
		Looper.loop();
	}

}
