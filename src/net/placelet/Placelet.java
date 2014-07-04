package net.placelet;

import android.app.Application;

import com.pushbots.push.Pushbots;

public class Placelet extends Application {

	private static final String SENDER_ID = "649565121667";
	private static final String PUSHBOTS_APPLICATION_ID = "539986981d0ab1d0048b45f6";

	@Override
	public void onCreate() {
		super.onCreate();
		Pushbots.init(this, SENDER_ID, PUSHBOTS_APPLICATION_ID);
		Pushbots.getInstance().setMsgReceiver(CustomPushReceiver.class);

	}

}