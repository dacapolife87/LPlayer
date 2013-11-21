package capston.L.Player;

public class FileList {

	private String[] m_File;

	public FileList(String fileName, String filePath, String smiHave) {
		m_File = new String[3];
		m_File[0] = fileName;
		m_File[1] = filePath;
		m_File[2] = smiHave;
	}

	public String[] getData() {
		return m_File;
	}

	public String getData(int index) {
		if (m_File == null || index >= m_File.length) {
			return null;
		}		
		return m_File[index];
	}
}
