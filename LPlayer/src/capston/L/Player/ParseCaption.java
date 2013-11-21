package capston.L.Player;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ParseCaption {
	private String mPath;	
	private FileInputStream fis;
	private BufferedReader br;
	private Handler mHandler;
	private ArrayList<CaptionData> parsedCaption;
	
	public ParseCaption(Handler h){
		this.mHandler = h;
		parsedCaption = new ArrayList<CaptionData>();
	}
	public void setPath(String path){
		
		Log.d("smihas", "setpath in :"+path);
		this.mPath = path;
	}
	public void clearCaption(){
		Log.d("caption", "caption");
		parsedCaption.clear();
	}
	public void startParsing(){
		clearCaption();
		ParsingThread Thread = new ParsingThread();
		Thread.setDaemon(true);
		Thread.start();
	}
	public ArrayList<CaptionData> getParsedCaption(){
		return parsedCaption;		
	}
	class ParsingThread extends Thread{
		public void run(){
			try {
				fis = new FileInputStream(mPath);
				br = new BufferedReader(new InputStreamReader(fis, "euc-kr"));
							
				parseData();
				
				fis.close();
				br.close();			
				
				Message msg = Message.obtain(mHandler, 0);
				mHandler.sendMessage(msg);
				
			} catch (FileNotFoundException e) {			
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();		
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
		//
		public void parseData(){
			String str = null;	//읽어온 스트림 저장하는 임시변수
			String text = null;
			long time = -1;
			boolean bStartParsing = false;
			
			try {
				while( (str=br.readLine()) != null){
					if(str.contains("<SYNC")){
						bStartParsing = true;
						if(time != -1 && text != ""){							
							parsedCaption.add(new CaptionData(time, text));
						}
						time = Integer.parseInt(str.substring(str.indexOf("=") + 1, str.indexOf(">")));						
						text = "";
						//text = str.substring(str.indexOf(">") + 1, str.length());
						//text = text.substring(text.indexOf(">") + 1, text.length());
						//text = text.replaceAll("&nbsp;", "");
					}else if(str.contains("</BODY")){
						bStartParsing = false;
						break;
					}
					else{
						if(bStartParsing == true){
							text += str.replaceAll("<br>", " ");							
						}
					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
	//
	class CaptionData{
	   	 private long time;
	   	 private String text;
	   	 
	   	 CaptionData(long time, String text){
	   		 this.time = time;
	   		 this.text = text;
	   	 }
	   	 public long getTime(){
	   		 return time;
	   	 }
	   	 public String getText(){
	   		 return text;
	   	 }	   	 
	}
	
}


