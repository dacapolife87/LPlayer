package capston.L.Player;

public class ForJNI {
	static{
		System.loadLibrary("ff");
	}
	public native int open(String a, String b);
	public native int Init();
}
