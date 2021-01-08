package org.rfcx.guardian.classify.activity;

import org.rfcx.guardian.classify.utils.AudioConverter;
import org.rfcx.guardian.classify.R;
import org.rfcx.guardian.classify.RfcxGuardian;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		Button sbdMsgSend = findViewById(R.id.sendButton);
		sbdMsgSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		//		((RfcxGuardian) getApplication()).apiUpdateRequestUtils.attemptToTriggerUpdateRequest(true, true);
			}
		});

		// example how model running process called
		RfcxGuardian app = (RfcxGuardian) this.getApplicationContext();
		int sampleRate = 12000; //app.rfcxPrefs.getPrefAsInt("audio_capture_sample_rate");
		float stepSize = app.rfcxPrefs.getPrefAsFloat("prediction_step_size");
		float windowSize = app.rfcxPrefs.getPrefAsFloat("prediction_window_size");

		app.audioClassifyUtils.initClassifierAttributes(sampleRate, windowSize, stepSize);
		app.audioClassifyUtils.classifyAudio(Environment.getExternalStorageDirectory().getAbsolutePath() + "/chainsaw12000.wav");
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		((RfcxGuardian) getApplication()).appPause();
	}
	
}
