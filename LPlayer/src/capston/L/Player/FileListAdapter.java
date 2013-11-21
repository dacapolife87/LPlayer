package capston.L.Player;

import java.util.ArrayList;
import java.util.List;

import com.example.lplayer.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FileListAdapter extends BaseAdapter {
/**/
	private Context m_Context;
	private List<FileList> m_files = new ArrayList<FileList>();
	private LayoutInflater m_Inflater;
	DataApp dApp = DataApp.Instance();
	
	public FileListAdapter(Context context) {
		m_Context = context;
		this.m_Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public String filePath(int position)
	{
		return m_files.get(position).getData(1);
	}
	public void clearFile(){
		m_files.clear();
	}
	public String fileName(int position)
	{
		return m_files.get(position).getData(0);
	}
	public String smiHaveCheck(int position)
	{
		return m_files.get(position).getData(2);
	}
	
	public void addFile(FileList it) {
		m_files.add(it);
	}

	public int getCount() {
		return m_files.size();
	}

	public Object getItem(int position) {
		return m_files.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		FileViewHolder viewHolder;
		
		if (convertView == null) {
			convertView = m_Inflater.inflate(R.layout.filelist,parent,false);
			viewHolder = new FileViewHolder();
			viewHolder.tv_FileName = (TextView)convertView.findViewById(R.id.dataItem01);
			viewHolder.tv_smiHave = (TextView)convertView.findViewById(R.id.smicheck);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (FileViewHolder) convertView.getTag();
		}
		viewHolder.tv_FileName.setText(fileName(position));
		viewHolder.tv_smiHave.setText(smiHaveCheck(position));

		return convertView;
	}
}
class FileViewHolder {
	public TextView tv_FileName;
	public TextView tv_smiHave;
}

