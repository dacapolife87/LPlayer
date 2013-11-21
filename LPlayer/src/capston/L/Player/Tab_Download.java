package capston.L.Player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.example.lplayer.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Tab_Download extends Activity   {
	private static final String DEFAULT_SERVER_IP = "117.17.198.41";
	private static final int DEFAULT_PORT = 6633;
	private static final int DEFAULT_BUFFER_SIZE = 1024;
	
	private ListView mListView;
	String File_Name = "asdf.txt";// Ȯ���ڸ� ������ ���ϸ�   
	String File_extend = "txt";// Ȯ����   
	
	// ip, port ����
	//private int port = 369;
   // private String ip = "dacapolife87.iptime.org";
	
    // ���� �ٿ�ε� ���
    String Save_Path = null;   
    String Save_folder = "/LPlayer";

    DownloadThread dThread = null;
    // ���� ��ü����  (���α׷����ٿ��� ����ϱ�����)
    long totalfilesize;
    // ���� ���� ����
    int downfilesize;

    static Socket socket=null;
    ObjectInputStream ois=null;
    InputStream is= null;
    private ArrayList<String> mFilelist;
    
    /////////////////
    ProgressDialog progressDialog;
    ProgressThread progressThread;
    static final int PROGRESS_DIALOG =0;
    /////////////////
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.downloadtab);
		
		
		if(android.os.Build.VERSION.SDK_INT >9){
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	        StrictMode.setThreadPolicy(policy);
		}
		
		//�� ����
		initView();
		//mHandler = new Handler();
		
		try{
			setSocket(DEFAULT_SERVER_IP,DEFAULT_PORT);
			
			ois = new ObjectInputStream(socket.getInputStream());
			// ���ϸ�� �޾ƿ���
			mFilelist =(ArrayList)ois.readObject();
			 //���ϸ���Ʈ ����Ʈ �信 �Է� 
			setFileList();
		}
		catch(IOException e1){
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
            

        // ����Ʈ Ŭ�� �̺�Ʈ ���� �ٿ�ε�.
        mListView.setOnItemClickListener(new OnItemClickListener(){
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
            	
            	// ���õ� ���ϸ� �Է�
            	File_Name = mFilelist.get(index);
            	
            	File dir = new File(Save_Path);  
                // ������ �������� ���� ��� ������ ����  
                if (!dir.exists()) {  
                    dir.mkdir();  
                }
                String downFile = File_Name+".mp3";
                downFile= Save_Path+"/"+downFile;
                File downfileex = new File(downFile);
                Log.d("test", "down"+downfileex);
                if(!downfileex.exists()){
                	probar();
                	dThread = new DownloadThread();  
                    dThread.start();
                }
                else{
                	showCompleteMsg("������ �����մϴ�");
                }
                
                Log.d("test", "down"+File_Name);
    			
                
                
            }
        });
        // �ٿ�ε� ��θ� ����޸� ����� ���� ������ ��.  
        String ext = Environment.getExternalStorageState();  
        if (ext.equals(Environment.MEDIA_MOUNTED)) {  
            Save_Path = Environment.getExternalStorageDirectory()  
                    .getAbsolutePath() + Save_folder;  
        }  
	}
	public void probar(){
		 progressThread = null;
         progressDialog = null;
         
         Log.e("test","test2");
         progressDialog = new ProgressDialog(Tab_Download.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("Loading...");
			progressDialog.setCancelable(false);
			progressDialog.show();
         //showDialog(PROGRESS_DIALOG);
         progressThread = new ProgressThread(mhandler);
         progressThread.start();
	}
	
	private void initView(){
		mListView = (ListView)findViewById(R.id.Find_ListView);
	}
	
	private void setFileList(){
		ArrayAdapter<String> filelist = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mFilelist);
		mListView.setAdapter(filelist);
	}
	
	protected Dialog onCreateDialog(int id) {
		switch(id){
		case PROGRESS_DIALOG:
			progressDialog = new ProgressDialog(Tab_Download.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("Loading...");
			progressDialog.setCancelable(false);
//			progressThread = new ProgressThread(mhandler);
//			progressThread.start();
			return progressDialog;
		default:
			return null;
		}
	}
    
    final Handler mhandler = new Handler(){
		public void handleMessage(Message msg){
			//������κ��� ���� �޽��� : ���� �������� ���� ���� 
    		int present = msg.getData().getInt("downfilesize");
    		// ������� ȯ��
    		present = (int) ((present*100)/(totalfilesize));
    		// �� ���α׷����� ��ġ
    		//downfilesize = (int) ((downfilesize*100)/(totalfilesize));
    		progressDialog.setProgress(present);
    		Log.e("test","msgd"+present);
    		if(present>=100){
    			progressDialog.setProgress(0);
    			Log.e("test", "msgdfdf");
//    			dismissDialog(PROGRESS_DIALOG);
    			Log.e("test", "msgdfdf");
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
    			b.putInt("downfilesize", downfilesize);
    			msg.setData(b);
    			
    			Log.e("test","test12");
    			mHandler.sendMessage(msg);
    			//total++;
    		}
    	}
    	public void setState(int state){
    		mState = state;
    		Log.e("test","test");
    	}
    	
    }

	// �ٿ�ε� ������� ����..  
	class DownloadThread extends Thread {  
          
	  
        DownloadThread() {  
              
        }  
  
        @Override  
        public void run() {  
          
        	FileOutputStream fos = null;
            FileOutputStream fos2 = null;
        	InputStream is = null;
        	
        	BufferedWriter networkWriter=null;
        	BufferedReader br=null;
        	OutputStream os = null;
        	DataInputStream dis = null;
            // �ٿ�ε� ������ ������ ���ϸ��� �����ϴ��� Ȯ���ؼ�  
            // ������ �ٿ�ް� ������ �ش� ���� �����Ŵ.
            File mp3File = new File(Save_Path + "/" + File_Name+".mp3");
            File smiFile = new File(Save_Path + "/" + File_Name+".smi");
            
            if (mp3File.exists() == false) {  
               // loadingBar.setVisibility(View.VISIBLE);  
            	downfilesize=0;
       
            	try {
            		if(socket.isClosed()){
            			setSocket(DEFAULT_SERVER_IP, DEFAULT_PORT);
            			os = socket.getOutputStream(); // ��� ��Ʈ��
                		is = socket.getInputStream();// �Է� ��Ʈ��
                		dis = new DataInputStream(is);
                		ois = new ObjectInputStream(socket.getInputStream());
            			// ���ϸ�� �޾ƿ���
						ois.readObject();
            		}
            		else{
                		os = socket.getOutputStream(); // ��� ��Ʈ��
                		is = socket.getInputStream();// �Է� ��Ʈ��
                		dis = new DataInputStream(is);
            		}
            		//������ �ٿ���� ���ϸ� ����
            		networkWriter = new BufferedWriter(new OutputStreamWriter(os)); 
            		br = new BufferedReader(new InputStreamReader(is));
            		PrintWriter out = new PrintWriter(networkWriter, true);
            		out.println(File_Name);
            		Log.e("dfdf",File_Name);
            		
            		// ���� ��Ʈ��
            		fos = new FileOutputStream(mp3File);
            		fos2 = new FileOutputStream(smiFile);
            		
            		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                	int readBytes;
					totalfilesize = dis.readLong();
					
                	while ((readBytes = is.read(buffer)) > 0) {
                		downfilesize+=readBytes;
                		
						fos.write(buffer, 0, readBytes);
						Log.d("test","enco"+readBytes);
//						Log.e("asdf","asdfcc"+String.valueOf(readBytes));
//						Log.e("asdf","asdfaa"+String.valueOf(totalfilesize));
//						Log.e("asdf","asdfbb"+String.valueOf(mp3File.length()));
						if(totalfilesize == mp3File.length()){
							out.println("re");
							break;
						}
					}
                	//downfilesize=0;
                	totalfilesize = dis.readLong();
                	
                	while ((readBytes = is.read(buffer)) > 0) {
                		//downfilesize+=readBytes;
						fos2.write(buffer, 0, readBytes);
						if(totalfilesize == smiFile.length())
							break;
					}
                	
                	Log.e("asdf","asdf11" + String.valueOf(totalfilesize));
					
				}  catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
            	catch (Exception e){
            		e.printStackTrace();
            	}
            	finally{
            		Log.e("asdf",socket.toString());
            		try {
						fos.close();
						fos2.close();
						
						socket.close();
						Log.e("asdf","close socket");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e){
						e.printStackTrace();
					}
            		
            	}
            	
                
            } else {  
                //showDownloadFile();
            	//showCompleteMsg();
            }
            mAfterDown.sendEmptyMessage(0);  
        }  
    }  
  
    Handler mAfterDown = new Handler() {  
        @Override  
        public void handleMessage(Message msg) {  
            // TODO Auto-generated method stub  
            // ���� �ٿ�ε� ������ �佺Ʈ �޽���
            showCompleteMsg(File_Name+"Download Complete");
            // ���� �ٿ�ε� ���� �� �ٿ���� ������ �����Ų��.  
            //showDownloadFile(); 
        }  
  
    };  
    
    // �佺Ʈ �޽��� ���
    private void showCompleteMsg(String Tmsg){
    	Toast toast = Toast.makeText(this, Tmsg,Toast.LENGTH_SHORT); 
    	toast.show();
    	
    }
    
    // �ٿ� ���� ���� ����.
    private void showDownloadFile() {  
        Intent intent = new Intent();  
        intent.setAction(android.content.Intent.ACTION_VIEW);  
        File file = new File(Save_Path + "/" + File_Name);  
  
        // ���� Ȯ���� ���� mime type ������ �ش�.  
        if (File_extend.equals("mp3")) {  
            intent.setDataAndType(Uri.fromFile(file), "audio/*");  
        } else if (File_extend.equals("mp4")) {  
            intent.setDataAndType(Uri.fromFile(file), "vidio/*");  
        } else if (File_extend.equals("jpg") || File_extend.equals("jpeg")  
                || File_extend.equals("JPG") || File_extend.equals("gif")  
                || File_extend.equals("png") || File_extend.equals("bmp")) {  
            intent.setDataAndType(Uri.fromFile(file), "image/*");  
        } else if (File_extend.equals("txt")) {  
            intent.setDataAndType(Uri.fromFile(file), "text/*");  
        } else if (File_extend.equals("doc") || File_extend.equals("docx")) {  
            intent.setDataAndType(Uri.fromFile(file), "application/msword");  
        } else if (File_extend.equals("xls") || File_extend.equals("xlsx")) {  
            intent.setDataAndType(Uri.fromFile(file),  
                    "application/vnd.ms-excel");  
        } else if (File_extend.equals("ppt") || File_extend.equals("pptx")) {  
            intent.setDataAndType(Uri.fromFile(file),  
                    "application/vnd.ms-powerpoint");  
        } else if (File_extend.equals("pdf")) {  
            intent.setDataAndType(Uri.fromFile(file), "application/pdf");  
        }  
        startActivity(intent);  
    }  
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	protected void onStop(){
		super.onStop();
		try{
			socket.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	// ���� ����
	public void setSocket(String ip, int port)throws IOException{
		try{
			
			socket = new Socket(ip,port);
			socket.setKeepAlive(true);
		}
		catch(Exception e){
			Log.e("dfef",ip+"dfef2");
			System.out.println(e);
			e.printStackTrace();
		}
	}
	


}