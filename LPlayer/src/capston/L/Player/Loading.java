package capston.L.Player;

import com.example.lplayer.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class Loading extends Activity   {

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		
		
		Handler h = new Handler(){
			public void handleMessage(Message msg){
				finish();
			}
		};
		h.sendEmptyMessageDelayed(0,3000);
	}

}