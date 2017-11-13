package com.zxingdemo.decode;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.zxingdemo.R;
import com.zxingdemo.camera.CameraManager;


/**
 * Author: Vondear
 * 描述: 扫描消息转发
 */
public final class CaptureActivityHandler extends Handler {

	DecodeThread decodeThread = null;
	Activity activity = null;
	private State state;

	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	public CaptureActivityHandler(Activity activity) {
		this.activity = activity;

		//CameraManager.get().startPreview();
		//restartPreviewAndDecode();
	}
	public void startDecode(CaptureActivityHandler handler){
		decodeThread = new DecodeThread(activity,handler);
		decodeThread.start();
		state = State.SUCCESS;
		CameraManager.get().startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {

		if (message.what == R.id.auto_focus) {
			if (state == State.PREVIEW) {
				CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
			}

		} else if (message.what == R.id.restart_preview) {
			restartPreviewAndDecode();

		} else if (message.what == R.id.decode_succeeded) {
			state = State.SUCCESS;
			//activity.handleDecode((String) message.obj);// 解析成功，回调
			Intent i=new Intent();
			i.setAction("QRCODE");
			i.putExtra("result",(String) message.obj);
			activity.sendBroadcast(i);

		} else if (message.what == R.id.decode_failed) {
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					R.id.decode);

		}

	}

	public void quitSynchronously() {
		state = State.DONE;
		CameraManager.get().stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
		quit.sendToTarget();
		try {
			// Wait at most half a second; should be enough time, and onPause() will timeout quickly
			decodeThread.join(500L);
		} catch (InterruptedException e) {
			// continue
		}
		removeMessages(R.id.decode_succeeded);
		removeMessages(R.id.decode_failed);
		removeMessages(R.id.decode);
		removeMessages(R.id.auto_focus);
	}

	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					R.id.decode);
			CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
		}
	}

}
