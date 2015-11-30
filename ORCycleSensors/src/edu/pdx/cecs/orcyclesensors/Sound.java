package edu.pdx.cecs.orcyclesensors;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class Sound {

	private final SoundPool soundPool;
	private final int sound;
	
	public Sound(Context context, int resId) {
		soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
		sound = soundPool.load(context, resId, 1);
	}

	public void play() {
		soundPool.play(sound, 1.0f, 1.0f, 1, 0, 1.0f);
	}
}
