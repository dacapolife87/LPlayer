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
	
	
	// file ����
	private static ArrayList<String> mFileList;
	private static String path;
	
	private ServerSocket serverSocket = null;
	
	//���ϸ�� ���� ����
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
				
				// Ŭ���̾�Ʈ ip�ּ�
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
		// Ŭ���̾�Ʈ�� ���� ��Ʈ�� �迭
		mFileList = new ArrayList<String>();
		path = "C:/servertest";
		//��������
		File dirFile=new File(path);
		// ���� ������ ���ϸ���Ʈ
		File[] fileList=dirFile.listFiles();
		//�ߺ� �˻� �÷��� ��
		Boolean fileExist = false;
		try{
			for(File tempFile : fileList) {
				  if(tempFile.isFile()) {  
				    //String tempPath=tempFile.getParent();
				    String tempFileName=tempFile.getName();	// �����̸�
				    //System.out.println("Path="+tempPath);
				    // ���ڿ� �ڸ���...
				    StringTokenizer token = new StringTokenizer(tempFileName,".");
				    tempFileName = token.nextToken();
				    
				    fileExist = false;
				    //�ߺ� �˻�
				    for(int i=0; i<mFileList.size();i++){
				    	// tempfilename �� ���� �迭�� ���� ���� ���� ��� flag���� true��..
				    	if(tempFileName.equals(mFileList.get(i)))
				    		fileExist = true;
				    }
				    
				    // flag�� false��.. �迭�� ���ٴ� ��..
				    if(!fileExist){
				    	System.out.println("FileName="+tempFileName);// �����̸� ���
				    	mFileList.add(tempFileName);// arr�� �߰�
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

	// ���� ������
	private static final int DEFAULT_BUFFER_SIZE = 1024;
	
	// Ŭ���̾�Ʈ ����
	private Socket client;
	private String ip;
	private int port;
	
	// ���� ���
	private String filePath;
	
	// Ŭ���̾�Ʈ ���� ����
	public SendFile(Socket client, String ip, int port,String filePath){
		this.client = client;
		this.ip = ip;
		this.port = port;
		this.filePath = filePath;
	}
	
	public void run(){
		// ���� ��Ʈ�� mp3,smi 2��
		FileInputStream fis = null;
		FileInputStream fis2 = null;
		// ���� ���� ��Ʈ��
		OutputStream os = null;
		DataOutputStream dos = null;
		// ���ϸ� ���� ��Ʈ��
		InputStream is = null;
		BufferedReader in = null;
		
		// ���� �ð�
		long startTime = System.currentTimeMillis();
		try {
						
			os = client.getOutputStream();
			dos = new DataOutputStream(client.getOutputStream());
			is = client.getInputStream();
			// ���ϸ� �������� from client
			 in = new BufferedReader(new InputStreamReader(is));
			String filenameToSend = in.readLine();
			//���� ��� ���� , ���ϸ��� Ŭ���̾�Ʈ�� ���� ���
			if(filenameToSend != null){
				System.out.println("ready");
				// ���� ���� �غ�
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
				
				//file size ����
				dos.writeLong(fileSize);
				dos.flush();
				//���� ����
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
        				// file size ����
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


