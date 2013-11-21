package capston.L.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.example.lplayer.R;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;


public class Tab_Extract extends Activity{
	private Button smiButton;
	private Button openfilebtn;
	private Button btnExtractAudio;
	private TextView fileNameTv;
	
	String filePath;
	String fileName;
	String fileDPName;
	int numAudioTr=0;
	int nowIdx;
	Boolean bfinish=false;
	
	long totalfilesize;
	ProgressDialog progressDialog;
    ProgressThread progressThread;
    static final int PROGRESS_DIALOG =0;
    private ExtractorThread extThread = null;
	Intent intent = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.extracttab);
		
		openfilebtn = (Button)findViewById(R.id.openbtn);
		btnExtractAudio = (Button)findViewById(R.id.btnExtract);
		
		fileNameTv = (TextView)findViewById(R.id.fileNameView);
		openfilebtn.setOnClickListener(btn_openfile);
		
		btnExtractAudio.setEnabled(false);
		
		btnExtractAudio.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				///
				bfinish=false;
				progressThread = null;
                progressDialog = null;
                copySmi(filePath, fileDPName);
                Log.e("test","test2");
                progressDialog = new ProgressDialog(Tab_Extract.this);
    			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    			progressDialog.setMessage("Extracting...");
    			progressDialog.setCancelable(false);
    			progressDialog.setIcon(0);
    			progressDialog.show();
                progressThread = new ProgressThread(mhandler);
    			progressThread.start();
				///
				
				if(fileName != null){
					extThread = new ExtractorThread(filePath,AnalyzeAVI());
					extThread.start();
					//startExtract(filePath,AnalyzeAVI());
				}
				else{
					Log.e("test","No Search file");
				}
			}
		});
	}

	final Handler mhandler = new Handler(){
		public void handleMessage(Message msg){
			//스레드로부터 받은 메시지 : 현재 진행중인 파일 길이 
    		int present = msg.getData().getInt("nowidx");
    		// 백분율로 환산
    		//present = (int) ((present*100)/(totallength));
    		// 셋 프로그레스바 위치
    		//nowIdx = (int) ((nowIdx*100)/(numTracks));
    		progressDialog.setProgress(present);
    		Log.e("test","enco pre :"+present);
    		Log.e("test","enco numtr :"+numAudioTr);
    		Log.e("test","enco nowidx : "+nowIdx);
    		//if(present>=100){
    		if(bfinish){
    			progressDialog.setProgress(0);

//    			dismissDialog(PROGRESS_DIALOG);
    			progressThread.setState(progressThread.STATE_DONE);
    			progressDialog.dismiss();
    			//progressThread.stop();
    		}
    	}
    };
    private class ProgressThread extends Thread{
    	Handler mHandler;
    	final int STATE_DONE =0;
    	final static int STATE_RUNNING =1;
    	int mState;
    	//int total;
    	
    	ProgressThread(Handler h){
    		mHandler =h;
    	}
    	public void run(){
    		mState = STATE_RUNNING;
    		//total = 0;
    		while(mState == STATE_RUNNING){
    			//mHandler.sendEmptyMessageDelayed(0, 100);
    			try {
					Thread.sleep(100);
				} catch (Exception e) {
					// TODO: handle exception
				}
    			Message msg = mHandler.obtainMessage();
    			Bundle b = new Bundle();
    			b.putInt("nowidx", nowIdx);
    			msg.setData(b);
    			mHandler.sendMessage(msg);
    			//total++;
    		}
    	}
    	public void setState(int state){
    		mState = state;
    		Log.e("test","test");
    	}
    }
	
	Button.OnClickListener btn_openfile = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			intent = new Intent(getBaseContext() , extractfileOpen.class);
			startActivityForResult(intent,0);
		}
	};

	private MediaExtractor AnalyzeAVI(){
		
		MediaFormat format;
		MediaExtractor extractor = new MediaExtractor();
		String mime = null ;
		//fos = new FileOutputStream(mp3File);
		Log.e("test",filePath);
		
		try {
			extractor.setDataSource(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int numTracks = extractor.getTrackCount();
		for (int i = 0; i < numTracks; ++i) {
			format = extractor.getTrackFormat(i);
			mime= format.getString(MediaFormat.KEY_MIME);
			Log.e("test","media type : "+mime);
			if (mime.contains("audio")){
				extractor.selectTrack(i);
				totalfilesize = format.getLong(MediaFormat.KEY_DURATION)*format.getInteger(MediaFormat.KEY_SAMPLE_RATE)*4;
				Log.d("test", "asdf"+totalfilesize);
				break;
			}
		}
		return extractor;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//getParent().onActivityResult(requestCode, resultCode,data);
		if(resultCode == RESULT_OK){
			if(data!= null){
				fileName = data.getStringExtra("name");
				filePath = data.getStringExtra("path");
				fileDPName = data.getStringExtra("dpname");
				fileNameTv.setText(fileName);
				btnExtractAudio.setEnabled(true);
			}
		}
	};
	
	public void copySmi(String _filepath,String fileDPName){
		String smifilepath = filePath;
		smifilepath = _filepath.substring(0, _filepath.length()-4)+".smi";
		File smiFile = new File(smifilepath);
		String filesmiName=fileDPName;
		filesmiName = filesmiName.substring(0, filesmiName.length()-4);
        String _filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/LPlayer/"+filesmiName+".smi";
		File copysmiFile = new File(_filePath);
		
		if(smiFile.exists())
		{
			try{
				FileInputStream fis = new FileInputStream(smiFile);
				FileOutputStream fos = new FileOutputStream(copysmiFile);
								
				byte[] buf = new byte[1024];
		        int i = 0;
		        while ((i = fis.read(buf)) != -1) {
		            fos.write(buf, 0, i);
		        }

				fis.close();
				fos.close();
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		}else
		{
			showfileopenMsg();
		}
	}
	private void showfileopenMsg(){
    	Toast toast = Toast.makeText(this,"자막파일이없습니다.",Toast.LENGTH_SHORT); 
    	toast.show();
    }
	private void startExtract(String src,MediaExtractor extractor){
		int buffersize;
		ByteBuffer inputBuffer = ByteBuffer.allocate(1024);
		String dst = Environment.getExternalStorageDirectory().getAbsolutePath();
		
		String dstfileName = fileDPName;
		
		dstfileName = dstfileName.substring(0, dstfileName.length()-4);
		dst += "/LPlayer/"+dstfileName+".mp3";
		
		try {
			File mp3File = new File(dst);
			FileOutputStream fos = new FileOutputStream(mp3File);
			while ((buffersize = extractor.readSampleData(inputBuffer,0)) >= 0) {
				nowIdx++;
				//int trackIndex = extractor.getSampleTrackIndex();
				//long presentationTimeUs = extractor.getSampleTime();
				byte[] buffer = new byte[buffersize];
				buffer = inputBuffer.array();
				fos.write(buffer,0,buffersize);
				inputBuffer.clear();
				extractor.advance();
			}
			fos.close();
			bfinish=true;
			//progressThread.setState(progressThread.STATE_DONE);
			//progressDialog.dismiss();
			extractor.release();
			extractor = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	class ExtractorThread extends Thread{
		private String src;
		private MediaExtractor extractor;
		ExtractorThread(String src,MediaExtractor extractor){
			this.src = src;
			this.extractor = extractor;
		}
		
		public void run(){
			startExtract(src, extractor);
		}
		
	}
}
