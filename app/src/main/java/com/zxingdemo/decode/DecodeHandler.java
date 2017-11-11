package com.zxingdemo.decode;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.zxingdemo.R;
import com.zxingdemo.camera.CameraManager;


/**
 * 描述: 接受消息后解码
 */
final class DecodeHandler extends Handler {

	Activity activity = null;
	CaptureActivityHandler handler;
	private final MultiFormatReader multiFormatReader;
	DecodeHandler(Activity activity,CaptureActivityHandler handler) {
		this.activity = activity;
		this.handler=handler;
		multiFormatReader = new MultiFormatReader();
		multiFormatReader.setHints(QRCodeDecoder.HINTS);
	}

	@Override
	public void handleMessage(Message message) {
		if (message.what == R.id.decode) {
			decode((byte[]) message.obj, message.arg1, message.arg2);
		} else if (message.what == R.id.quit) {
			Looper.myLooper().quit();

		}
	}

	private void decode(byte[] data, int width, int height) {
		byte[] rotatedData = new byte[data.length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++)
				rotatedData[x * height + height - y - 1] = data[x + y * width];
		}
        String result=processData(rotatedData,width,height);
		if (result != null) {
			if(null != handler){
				Message msg = new Message();
				msg.obj = ""+result;
				msg.what = R.id.decode_succeeded;
				handler.sendMessage(msg);
			}
		} else {
			handler.sendEmptyMessage(R.id.decode_failed);
		}
	}
	/*解析二维码*/
	public String processData(byte[] data, int width, int height) {
		String result = null;
		Result rawResult = null;
		PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(data, width, height);//对方框里图像进行剪切得到对象
		if (source != null) {
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));//所得成像转化成bitmap
			try {
				rawResult = multiFormatReader.decodeWithState(bitmap);//得到解析结果
			} catch (ReaderException re) {
				// continue
			} finally {
				multiFormatReader.reset();
			}
		}




		if (rawResult != null) {
			result = rawResult.getText();
		}
		return result;
	}

}
