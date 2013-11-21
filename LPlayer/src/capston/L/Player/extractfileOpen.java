package capston.L.Player;

import java.util.ArrayList;
import java.util.List;

import com.example.lplayer.R;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class extractfileOpen extends ListActivity{
	ContentResolver mCr;
	Cursor cursor;
	Intent intent = null;
	List<String> data = new ArrayList<String>();
	ArrayAdapter<String> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCr = getContentResolver();	
		getListView().setOnItemClickListener(file_click);
		dumpQuery();
		///////
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		filter.addDataScheme("file");
		registerReceiver(mScanReceiver, filter);
		
		adapter = new ArrayAdapter<String>(this,R.layout.openlist,data);
		this.setListAdapter(adapter);
	}

	BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			dumpQuery();
		}
	};
	void dumpQuery() {
		data.clear();
		//adapter.clear();
		Uri uri;		
		// 미디어 종류와 메모리 위치로부터 URI 결정
		uri = Video.Media.EXTERNAL_CONTENT_URI;
		cursor = mCr.query(uri, null, null, null, null);
		// 레코드 목록 출력
		int count = 0;
		Log.d("test", "dump in!");
		while (cursor.moveToNext()) {
			count++;
			Log.d("test", "dump!"+count);
			data.add(getColumeValue(cursor,MediaColumns.DISPLAY_NAME));
			if (count == cursor.getCount()) break;
		}
		cursor.close();
	}

	String getColumeValue(Cursor cursor, String cname) {
		String value = cursor.getString(cursor.getColumnIndex(cname));

		return value;
	}
	public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScanReceiver);
        
    }
	
	OnItemClickListener file_click = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
			// TODO Auto-generated method stub
			cursor = mCr.query(Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
			cursor.moveToPosition(position);
			intent = getIntent();
			Log.d("test","mdb : "+cursor.getString(cursor.getColumnIndex(MediaColumns.DATA)));
			Log.d("test","mdb : "+cursor.getString(cursor.getColumnIndex(MediaColumns.DISPLAY_NAME)));
			Log.d("test","mdb : "+cursor.getString(cursor.getColumnIndex(MediaColumns.TITLE)));
			intent.putExtra("path", cursor.getString(cursor.getColumnIndex(MediaColumns.DATA)));
			intent.putExtra("name", cursor.getString(cursor.getColumnIndex(MediaColumns.TITLE)));
			intent.putExtra("dpname", cursor.getString(cursor.getColumnIndex(MediaColumns.DISPLAY_NAME)));
			setResult(RESULT_OK,intent);

			finish();
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
