package com.teltech.teaspoon_tester;


import com.teltech.teaspoon.Teaspoon;
import com.teltech.teaspoon.TeaspoonHandler;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;



public class MainActivity extends ActionBarActivity {

	public Teaspoon teaspoon;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		teaspoon = new Teaspoon(this.getApplicationContext(), "23.22.245.68", 8090);
		teaspoon.setHandler(new TeaspoonHandler(){

			@Override
			public void onConnect() {
				Log.v("DEBUG", "Handler onConnect");
			}

			@Override
			public void onConnectionError(Exception e) {
				Log.v("DEBUG", "Handler onConnectionError: " + e.getLocalizedMessage());
				
			}
			
			@Override
			public void onDisconnect() {
				Log.v("DEBUG", "Handler onDisconnect");
				
			}
		});
		Log.v("DEBUG", "TESTER: Connecting");
		teaspoon.connect(2);
		Log.v("DEBUG", "TESTER: Done Connecting");
	}

	 @Override
	   protected void onStop() {
		super.onStop();
		teaspoon.disconnect();
	   }
	 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
