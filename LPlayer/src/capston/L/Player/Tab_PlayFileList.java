package capston.L.Player;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.StringTokenizer;



import com.example.lplayer.R;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.util.Log;

public class Tab_PlayFileList extends Activity {
	static String[] str_fileList;
	String smiHave;
	
	ListView lv_list;
	DataApp dApp = DataApp.Instance();
	static FileListAdapter adapter;
    
	public Tab_PlayFileList()
	{
		dApp._pfileList = this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filelisttab);
		Log.d("test", "count");
		adapter = new FileListAdapter(this);
		lv_list = (ListView)findViewById(R.id.filelist);
		lv_list.setOnItemClickListener(list_click);
		lv_list.setOnItemLongClickListener(list_longclick);
		//showDialog(PROGRESS_DIALOG);
		
	}
	public void setFileList(){
		adapter.clearFile();
		fileFind();
		
		lv_list.setAdapter(adapter);
	}
	OnItemClickListener list_click = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
			// TODO Auto-generated method stub
			dApp._position = position;
			dApp._lplayer.LPlayer();
		}
	};
	OnItemLongClickListener list_longclick = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,final int position, long arg3) {
			// TODO Auto-generated method stub
			
			CharSequence[] items = {"����", "���"};
			AlertDialog.Builder builder = new AlertDialog.Builder(Tab_PlayFileList.this);
			builder.setTitle("������ �����Ͻðڽ��ϱ�?");
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch(item){
						case 0:
							String FileName = dApp._pfileList.adapter.filePath(position);
							File dFile = new File(FileName);
							dFile.delete();
							FileName = FileName.replace(".mp3", ".smi");
							File dFile2 = new File(FileName);
							
							dFile2.delete();
							setFileList();
							break;
						case 1:
							break;
					}
					}
				});
			AlertDialog alert = builder.create();
			alert.show();			

			return false;
		}	
	};


	public void fileFind(){
		String _filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File files = new File(_filePath+"/LPlayer");
        // ������ �������� ���� ��� ������ ����  
        if (!files.exists()) {  
        	files.mkdir();  
        }
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".mp3");
			}	
		};
		str_fileList = files.list(filter);
		for(int i=0;i<str_fileList.length;i++) {
			String smiPath = _filePath+"/LPlayer/" + str_fileList[i];
			smiPath= smiPath.replace(".mp3", ".smi");
			File smiFiles = new File(smiPath);
			if(!smiFiles.exists())
			{
				smiHave="�ڸ�����";
			}
			else
			{
				smiHave="�ڸ�����";
			}
			adapter.addFile(new FileList(str_fileList[i],_filePath+"/LPlayer/" + str_fileList[i],smiHave));
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

}
