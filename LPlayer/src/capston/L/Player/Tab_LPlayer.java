package capston.L.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;



import com.example.lplayer.R;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.util.Log;

public class Tab_LPlayer extends Activity {
	Button btn_play;
	Button btn_stop;
	Button btn_partRepeat;
	Button btn_smipartRepeat;
	TextView tv_playName;
	TextView nohaveSmi;
	SeekBar sb_progress;
	String FilePath;
	
	MediaPlayer mp_player;
	boolean b_Playing;
	boolean b_hasSmi=false;
	boolean fileopen=false;
	int idxNum;
	int repeatStatus=0;
	int repeatStartTime;
	int repeatEndTime;
	int smirepeatTime;
	boolean repeatSignal=false;
	boolean smirepeat=false;
	
	Tab_MainActivity ma_MainActivity;
	Tab_PlayFileList cl_PlayFileList;
	
	/////////////////////  app test
	DataApp dApp = DataApp.Instance();
	/////////////

	///////////////////////////////// N add	
	ListView mCaption;
	String CaptionFilePath;	
	
	ArrayAdapter<String> CaptionAdapter;
	ArrayList<String> UsedInAdapter;
	
	ParseCaption mParseCaption;
	/////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playertab);
		
		//ma_MainActivity = new Tab_MainActivity();
		cl_PlayFileList = new Tab_PlayFileList();
		mp_player = new MediaPlayer();
		
		btn_play = (Button)findViewById(R.id.playBtn);
		btn_stop = (Button)findViewById(R.id.stopBtn);
		btn_partRepeat = (Button)findViewById(R.id.repeat);
		btn_smipartRepeat = (Button)findViewById(R.id.smirepeat);
		sb_progress =(SeekBar)findViewById(R.id.progress);
		tv_playName = (TextView)findViewById(R.id.playName);
		nohaveSmi = (TextView)findViewById(R.id.nohaveSmi);
		btn_play.setOnClickListener(btn_clickPlay);
		btn_stop.setOnClickListener(btn_clickStop);
		btn_partRepeat.setOnClickListener(btn_clickrepeat);
		btn_smipartRepeat.setOnClickListener(btn_clicksmirepeat);
		sb_progress.setOnSeekBarChangeListener(_onSeek);
		mp_player.setOnSeekCompleteListener(_onSeekComplete);
		mp_player.setOnCompletionListener(_onComplete);
		ph_progressHandler.sendEmptyMessageDelayed(0,200);
		
		///////////////////////////////// N add
		mCaption = (ListView)findViewById(R.id.smitext);		
		mCaption.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mCaption.setOnItemClickListener(mItemClickListener);		
		/////////////////////////////////	
		dApp._lplayer = this;
		///////////////////////////////// N add		
		UsedInAdapter = new ArrayList<String>();
		CaptionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, UsedInAdapter);
		mCaption.setAdapter(CaptionAdapter);
		mParseCaption = new ParseCaption(CaptionHandler);
		fileLoad(idxNum);
		disableIcon();
	}
	
    
	/*************************************
	 * LPlayer                            
	 * 파일리스트에서 클릭한 리스트의 Position값을 받아서  
	 * 파일리스트 배열에서 index값을 검색하여                  
	 * 파일 open 및 onPlay함수 호출                           
	 *************************************/
	public void LPlayer(){
		b_hasSmi =false;
		smirepeat=false;
		fileopen=true;
	
		
		btn_smipartRepeat.setText("자막반복 On ");
		if(b_Playing==true){
			mp_player.reset();
			b_Playing = false;
		}
		idxNum = dApp._position;
		ma_MainActivity.tabs.setCurrentTab(0);
		fileLoad(idxNum);
		onPlay(idxNum);
	}
	
	public void onPlay(int position)
	{		
		tv_playName.setText(dApp._pfileList.adapter.fileName(idxNum));
		mp_player.start();
		b_Playing=true;
		disableIcon();
		btn_play.setText("∥∥");
	}

	Button.OnClickListener btn_clickPlay = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(mp_player.isPlaying() == false){
				if(fileopen==false){
					showfileopenMsg();
				}
				else{
					onPlay(idxNum);
				}
			}
			else{
				mp_player.pause();
				btn_play.setText("▶");
			}
		}
	};
	Button.OnClickListener btn_clicksmirepeat = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(b_hasSmi==true){
				if(smirepeat==false){
					int index = getSync(mp_player.getCurrentPosition());
					smirepeatTime = index;
					smirepeat=true;
					btn_smipartRepeat.setText("자막반복 Off");
				}
				else{
					btn_smipartRepeat.setText("자막반복 On ");
					smirepeat=false;
				}
			}
		}
	};
	
	Button.OnClickListener btn_clickrepeat = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(repeatStatus==0)
			{
				// 음원파일 반복 시작부분 데이터 저장
				repeatStatus=1;
				btn_partRepeat.setText("A->□ Off");
				repeatStartTime = mp_player.getCurrentPosition();
			}
			else if(repeatStatus==1)
			{
				// 음원파일 부분반복시작
				repeatStatus=2;
				btn_partRepeat.setText("A->B On ");
				repeatEndTime = mp_player.getCurrentPosition();
				repeatSignal=true;
				mp_player.pause();
				mp_player.seekTo(repeatStartTime);
				mp_player.start();				
			}
			else if(repeatStatus==2)
			{
				repeatStatus=0;
				btn_partRepeat.setText("A->B Off");
				repeatSignal=false;
			}

		}
	};
	
	Button.OnClickListener btn_clickStop = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mp_player.stop();
			sb_progress.setProgress(0);
			b_Playing =false;
			btn_play.setText("▶");
			fileLoad(idxNum);
		}
	};
	
	SeekBar.OnSeekBarChangeListener _onSeek = new SeekBar.OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				mp_player.seekTo(progress);
			}
		}
		public void onStartTrackingTouch(SeekBar seekBar) {
			b_Playing = mp_player.isPlaying();
			if (b_Playing) {
				mp_player.pause();
			}
		}
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	};
	
	MediaPlayer.OnSeekCompleteListener _onSeekComplete = new MediaPlayer.OnSeekCompleteListener() {
		public void onSeekComplete(MediaPlayer mp) {
			if (b_Playing) {
				mp_player.start();
			}
		}
    };
    
    MediaPlayer.OnCompletionListener _onComplete = new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			idxNum = (idxNum == dApp._pfileList.adapter.getCount() -1 ? 0:idxNum + 1);
			mp_player.reset();	
			fileLoad(idxNum);
			onPlay(idxNum);
		}
	};
	
    
    Handler ph_progressHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (mp_player == null) return;
			if (mp_player.isPlaying()) {
				sb_progress.setProgress(mp_player.getCurrentPosition());

				if(b_hasSmi==true){
					if(smirepeat==true){
						if(smirepeatTime == (mParseCaption.getParsedCaption().size()-1))
						{
							if(sb_progress.getProgress() >= sb_progress.getMax()-250){
								mp_player.seekTo((int)mParseCaption.getParsedCaption().get(smirepeatTime).getTime());
							}
						}
						else if(sb_progress.getProgress() >= mParseCaption.getParsedCaption().get(smirepeatTime+1).getTime())
						{
							mp_player.seekTo((int)mParseCaption.getParsedCaption().get(smirepeatTime).getTime());
						}
					}
					else{
						int index = getSync(mp_player.getCurrentPosition());
						mCaption.setItemChecked(index, true);
					}
				}
			}
			
			if(repeatSignal==true){
				if(sb_progress.getProgress()>=repeatEndTime)
				{
					mp_player.seekTo(repeatStartTime);
				}
			}
			ph_progressHandler.sendEmptyMessageDelayed(0,200);
		}
	};
	///////////////////////////////// N add    
	/* ParsingThread의 Handler */
	Handler CaptionHandler = new Handler(){
		public void handleMessage(Message msg){
			for(int i = 0; i < mParseCaption.getParsedCaption().size();i++){
				UsedInAdapter.add(mParseCaption.getParsedCaption().get(i).getText());
			}
			CaptionAdapter.notifyDataSetChanged();    		 
		}
	};
		
	/* 재생시간으로 자막의 인덱스 계산*/
	public int getSync(long playTime){
		int  first = 0, m, h = mParseCaption.getParsedCaption().size()-1;
		while(first <= h){		
			m = (first + h) / 2;
			if( m+1 > h )
				return h;
			else if(mParseCaption.getParsedCaption().get(m).getTime() <= playTime && playTime < mParseCaption.getParsedCaption().get(m+1).getTime())
			{
				return m;
			}
			if(playTime > mParseCaption.getParsedCaption().get(m+1).getTime()){
				first = m + 1;
			}else{
				h = m - 1;
			}
		}
		return 0;
	}
		
	OnItemClickListener mItemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
			if(smirepeat==true){
				btn_smipartRepeat.setText("자막반복 On ");
				smirepeat=false;
			}
			int sec = (int)mParseCaption.getParsedCaption().get(position).getTime();
			mp_player.seekTo(sec);
		}
	};	
	/////////////////////////////////
	
	boolean fileLoad(int idx){
		mp_player.reset();
		try {
			mp_player.setDataSource(dApp._pfileList.adapter.filePath(idx));
			FilePath = dApp._pfileList.adapter.filePath(idx);
			FilePath=FilePath.replace(".mp3", ".smi");
			File smiFile = new File(FilePath);
			Log.d("test", "smiFile : "+FilePath);
			UsedInAdapter.clear();
			CaptionAdapter.notifyDataSetChanged();
			if(smiFile.exists())
			{
				nohaveSmi.setText("");
				b_hasSmi= true;
				mParseCaption.setPath(FilePath);
				mParseCaption.startParsing();			

			}
			else
			{
				nohaveSmi.setText("자막파일이 없습니다.\n\n\n\n\n\n\n\n\n\n\n\n\n");
				mParseCaption.clearCaption();
				b_hasSmi= false;
			}
		} catch (Exception e) {
			Log.d("FileOpenDebug","FO Fail"+e);
		}
		if (filePrepare() == false) {
			return false;
		}
		
		return true;
	}
	
	boolean filePrepare(){
		try {
			mp_player.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sb_progress.setMax(mp_player.getDuration());
		return true;
	}
	
	public void onDestroy() {
        super.onDestroy();
        if (mp_player != null) {
        	mp_player.stop();
        	mp_player.release();
        	mp_player = null;
        }
    }
	public void disableIcon(){
		if(b_Playing==false)
		{
			nohaveSmi.setText("듣는 보는 어학학습기 \n L-Player입니다.\n =사용방법=\n 1. FILELIST탭을 누루신후\n 2. 원하는 파일을 선택하세요 \n 3. 다운로드탭에서는 기본샘플이  제공됩니다\n 4. 어학파일 추출탭에서는 원하는 파일에서 음성을 추출하실수 있습니다. \n\n\n\n");
			btn_stop.setEnabled(false);
			btn_partRepeat.setEnabled(false);
			btn_smipartRepeat.setEnabled(false);
			sb_progress.setEnabled(false);
		}
		else
		{
			btn_stop.setEnabled(true);
			btn_partRepeat.setEnabled(true);
			btn_smipartRepeat.setEnabled(true);
		}	
	}
	private void showfileopenMsg(){
    	Toast toast = Toast.makeText(this,"파일을 먼저 열어주세요.",Toast.LENGTH_SHORT); 
    	toast.show();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
