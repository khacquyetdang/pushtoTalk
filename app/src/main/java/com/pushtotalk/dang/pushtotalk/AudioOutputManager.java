package com.pushtotalk.dang.pushtotalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AudioOutputManager extends BroadcastReceiver {
	

	@Override
	public void onReceive(Context context, Intent intent) {
		
		
		// TODO Auto-generated method stub 
		
		if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
			
			
			/*
			 * state - 0 for unplugged, 1 for plugged.
			 * microphone - 1 if headset has a microphone, 0 otherwise
			 */
			int microphone = intent.getIntExtra("microphone", -1);
			  int state = intent.getIntExtra("state", -1);
	            switch (state) {
	            case 0:
	            	MainActivity.audiomanager.setSpeakerphoneOn(true); 
	                break;
	            case 1: 
	            	MainActivity.audiomanager.setSpeakerphoneOn(false); 
	                break;
	            default:
	            	MainActivity.audiomanager.setSpeakerphoneOn(true); 
	            }
	            
	            
	            
	            switch (microphone ){
	            case 0: 
	                break;
	            case 1:  
	            	//headset has a microphone
	                break;
	            default: 
	            }
		}
		
		
		
		
		
	}

}
