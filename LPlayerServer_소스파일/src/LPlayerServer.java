import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class LPlayerServer {
	public static final int DEFAULT_PORT = 6633;

	
	//private static final String CONFIGURATION_FILENAME = "conf/file_sending_service.properties";
	//private static final String KEY_FILENAME_TO_SEND = "FILENAME_TO_SEND";

	private final int port;
	
	
	// file 관련
	private static ArrayList<String> mFileList;
	private static String path;
	
	private ServerSocket serverSocket = null;
	
	//파일목록 전송 관련
	ObjectOutputStream soos = null;
	OutputStream osfilelist = null;
	private boolean running = false;

	public LPlayerServer() {
		this(DEFAULT_PORT);
	}

	public LPlayerServer(int port) {
		this.port = port;
	}

	public void start() {
		running = true;
		
		getFileList();
		
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("This server is listening... (Port: " + port + ")");			
			while (running) {
				//
				Socket client = serverSocket.accept();
				
				osfilelist = client.getOutputStream();
				soos = new ObjectOutputStream(osfilelist);
				soos.reset();
				soos.writeObject(mFileList);
				soos.flush();
				
				System.out.println("Start time: " + new Date());
				
				//OutputStream os = client.getOutputStream();
				//DataOutputStream dos = new DataOutputStream(client.getOutputStream());
				//InputStream is = client.getInputStream();
				
				// 클라이언트 ip주소
				InetSocketAddress clientIsa = (InetSocketAddress) client.getRemoteSocketAddress();
				String clientIp = clientIsa.getAddress().getHostAddress();
				int clientPort = clientIsa.getPort();
				System.out.println("A client is connected. (" + clientIp+ ":" + clientPort + ")");
				
				SendFile sender = new SendFile(client,clientIp,clientPort,path);
				sender.start();
				}
			}
		catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (serverSocket != null) {
				try {
					System.out.println("serverSocket close");
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void stop() {
		running = false;
	}

	public static void main(String[] args) {
		
		LPlayerServer fileSendingService = new LPlayerServer();
		
		fileSendingService.start();
	}
	public static void getFileList(){
		// 클라이언트로 보낼 스트링 배열
		mFileList = new ArrayList<String>();
		path = "C:/servertest";
		//서버폴더
		File dirFile=new File(path);
		// 서버 폴더의 파일리스트
		File[] fileList=dirFile.listFiles();
		//중복 검사 플래그 값
		Boolean fileExist = false;
		try{
			for(File tempFile : fileList) {
				  if(tempFile.isFile()) {  
				    //String tempPath=tempFile.getParent();
				    String tempFileName=tempFile.getName();	// 파일이름
				    //System.out.println("Path="+tempPath);
				    // 문자열 자르기...
				    StringTokenizer token = new StringTokenizer(tempFileName,".");
				    tempFileName = token.nextToken();
				    
				    fileExist = false;
				    //중복 검사
				    for(int i=0; i<mFileList.size();i++){
				    	// tempfilename 의 값과 배열에 같은 값이 있을 경우 flag값을 true로..
				    	if(tempFileName.equals(mFileList.get(i)))
				    		fileExist = true;
				    }
				    
				    // flag가 false면.. 배열에 없다는 말..
				    if(!fileExist){
				    	System.out.println("FileName="+tempFileName);// 파일이름 출력
				    	mFileList.add(tempFileName);// arr에 추가
				    	fileExist = false;
				    }
				    
				    /*** Do something withd tempPath and temp FileName ^^; ***/
				  }
				
				}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}

}

class SendFile extends Thread{

	// 버퍼 사이즈
	private static final int DEFAULT_BUFFER_SIZE = 1024;
	
	// 클라이언트 정보
	private Socket client;
	private String ip;
	private int port;
	
	// 파일 경로
	private String filePath;
	
	// 클라이언트 소켓 정보
	public SendFile(Socket client, String ip, int port,String filePath){
		this.client = client;
		this.ip = ip;
		this.port = port;
		this.filePath = filePath;
	}
	
	public void run(){
		// 파일 스트림 mp3,smi 2개
		FileInputStream fis = null;
		FileInputStream fis2 = null;
		// 파일 전송 스트림
		OutputStream os = null;
		DataOutputStream dos = null;
		// 파일명 수신 스트림
		InputStream is = null;
		BufferedReader in = null;
		
		// 시작 시간
		long startTime = System.currentTimeMillis();
		try {
						
			os = client.getOutputStream();
			dos = new DataOutputStream(client.getOutputStream());
			is = client.getInputStream();
			// 파일명 가져오기 from client
			 in = new BufferedReader(new InputStreamReader(is));
			String filenameToSend = in.readLine();
			//파일 목록 전송 , 파일명을 클라이언트로 받은 경우
			if(filenameToSend != null){
				System.out.println("ready");
				// 파일 전송 준비
				//os = client.getOutputStream();
				System.out.println("Filename to send: " + filenameToSend);
				File fileToSend = new File(filePath+"/"+filenameToSend+".mp3");
				File fileToSend2 = new File(filePath+"/"+filenameToSend+".smi");
				
				long fileSize = fileToSend.length();
				long fileSize2 = fileToSend2.length();
				System.out.println("totalsize : "+fileSize);
				System.out.println("File size: " + (fileSize) + " Byte(s)");
				fis = new FileInputStream(fileToSend);
				fis2 = new FileInputStream(fileToSend2);
				
				//file size 전송
				dos.writeLong(fileSize);
				dos.flush();
				//파일 전송
				byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
				int totalReadBytes = 0;
				int readBytes;
				int count=0;
				while ((readBytes = fis.read(buffer)) != -1) {
					count++;
					os.write(buffer, 0, readBytes);
					os.flush();
					totalReadBytes += readBytes;
					if(count%100 ==0){
						System.out.println("In progress: " + totalReadBytes + "/" + fileSize + " Byte(s) (" + (totalReadBytes * 100 / fileSize) + " %)");
					}
				}
				//outstring = new PrintWriter(out, true);
        		//outstring.println("1");
        		
        		System.out.println("Sent mp3 file");
        		readBytes =0;
        		count=0;
        		while(true){
        			if(in.readLine()!=null){
        				// file size 전송
                		dos.writeLong(fileSize2);
                		dos.flush();
                		totalReadBytes =0;
                		while ((readBytes = fis2.read(buffer)) != -1) {
							os.write(buffer, 0, readBytes);
							totalReadBytes += readBytes;
							if(count%100 ==0)
								System.out.println("In progress: " + totalReadBytes + "/" + fileSize2 + " Byte(s) (" + (totalReadBytes * 100 / fileSize2) + " %)");
							os.flush();
							}
                		}
            		
        			if(readBytes ==-1)
        				break;
        		}
        		
        		System.out.println("Sent smi file");

				long endTime = System.currentTimeMillis();
				System.out.println("End time: " + new Date());

				long diffTime = endTime - startTime;
				long diffTimeInSeconds = diffTime / 1000;
				System.out.println("Elapsed time: " + diffTimeInSeconds + " second(s)");

				//System.out.println("Average transfer speed: " + (fileSize / diffTime) + " KB/s");
				filenameToSend = null;
				
				fileToSend = null;
				fileToSend2 = null;
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			try {
				fis.close();
				fis2.close();
				in.close();
				os.close();
				dos.close();
        		client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("ready2");
			
		}
		
	}
}


