package tk.qsjia.hostseditor.util;

import java.io.*;
import java.net.*;

public class NetworkUtils {
	public static long getModifyDate(String urlStr) {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				return connection.getLastModified();
			} else {
				return -3;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -2;
		}
	}

	public static File downloadHostsFile(String urlStr, String dirName) {
		File file = null;
		InputStream is = null;
		BufferedReader reader = null;
		FileWriter writer = null;
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK && "text/plain".equals(connection.getContentType())) {
				is = connection.getInputStream();
			}
			if (is != null) {
				file = File.createTempFile("hosts", "", new File(dirName));
				reader = new BufferedReader(new InputStreamReader(is));
				writer = new FileWriter(file);
				char[] buffer = new char[512];
				int size = 0;
				while (size >= 0) {
					writer.write(buffer, 0, size);
					size = reader.read(buffer, 0, 512);
				}
				writer.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) is.close();
				if (reader != null) reader.close();
				if (writer != null) writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	public static void main(String[] args) {
		NetworkUtils.downloadHostsFile("https://smarthosts.googlecode.com/svn/trunk/hosts", "E:/hosts");
	}
}
