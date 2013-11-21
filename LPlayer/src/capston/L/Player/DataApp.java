package capston.L.Player;

import android.app.Application;

public class DataApp extends Application{
	private static DataApp _dataApp;
	
	public static DataApp Instance(){
		if(_dataApp == null){
			_dataApp = new DataApp();
		}
		return _dataApp;
	}
	
	public int _position;
	public Tab_LPlayer _lplayer;
	public Tab_PlayFileList _pfileList;
	public Tab_Download _download;

}
