package tk.qsjia.hostseditor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetworkUtils {

	public static long getModifyDate(String urlStr) {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(3000);
			connection.connect();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				return connection.getLastModified() == 0 ? connection.getDate() : connection.getLastModified();
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
			connection.setConnectTimeout(3000);
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK && connection.getContentType().contains("text/plain")) {
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
