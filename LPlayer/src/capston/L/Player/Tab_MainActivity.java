package capston.L.Player;

import java.io.File;
import java.io.IOException;

import com.example.lplayer.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;


@SuppressWarnings("deprecation")
public class Tab_MainActivity extends TabActivity {
    /** Called when the activity is first created. */
	public static TabHost tabs;
	DataApp _dApp = DataApp.Instance();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabmain); 
        startActivity(new Intent(this, Loading.class));
        
        
        String _filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/LPlayer";
		File files = new File(_filePath);
		
        // 폴더가 존재하지 않을 경우 폴더를 만듦  
        if (!files.exists()) {  
        	files.mkdirs();  
        }
        // setup tab widget
        setupTabs();
    }
    //MediaPlayer : android.media.MediaPlayer@42581940
    /**
     * setup tab widget
     */
    private void setupTabs() {
    	tabs = getTabHost();
    	
 	    // TAB 01 
 	    TabHost.TabSpec spec = null;
 	    Intent intent = null;
        
 	    spec = tabs.newTabSpec("tab01");
 	    intent = new Intent(this, Tab_LPlayer.class);
 	    intent.putExtra("mode", "new");
	   	intent.putExtra("initialize", true);
	   	intent.putExtra("request", true);
	   	intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
   	
 	    spec.setContent(intent);
 	   
 	    spec.setIndicator("LPlayer");
 	    tabs.addTab(spec);
 	   
 	    // TAB 02 
 	    spec = tabs.newTabSpec("tab02");
 	    intent = new Intent(this, Tab_PlayFileList.class);
 	    intent.putExtra("mode", "new");
	   	intent.putExtra("initialize", true);
	   	intent.putExtra("request", true);
	   	intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 	    spec.setContent(intent);

 	    spec.setIndicator("FileList");
 	    tabs.addTab(spec);
 	    
 	    // TAB 03 
 	    spec = tabs.newTabSpec("tab03");
 	    intent = new Intent(this, Tab_Download.class);
 	    intent.putExtra("mode", "new");
	   	intent.putExtra("initialize", true);
	   	intent.putExtra("request", true);
	   	intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 	    spec.setContent(intent);

 	    spec.setIndicator("Down");
 	    tabs.addTab(spec);
 	    
 	    // TAB 04
 	    spec = tabs.newTabSpec("tab04");
 	    intent = new Intent(this, Tab_Extract.class);
 	    //intent = new Intent(this, extractfileOpen.class);
 	    intent.putExtra("mode", "new");
	   	intent.putExtra("initialize", true);
	   	intent.putExtra("request", true);
	   	intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 	    spec.setContent(intent);

 	    spec.setIndicator("Extract");
 	    tabs.addTab(spec);
 	    
 	    // set current tab
 	    tabs.setCurrentTab(0);
 	   
 	    tabs.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
				
				if(tabId == "tab02")
				{
					_dApp._pfileList.setFileList();
				}
			}
		});
 	   
    }
    
}