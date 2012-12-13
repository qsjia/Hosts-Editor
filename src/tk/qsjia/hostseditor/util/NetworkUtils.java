package tk.qsjia.hostseditor.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class NetworkUtils {
	public static long getModifyDate(String urlStr){
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
				return connection.getLastModified();
			}else{
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
	
	public static InputStream getInputStream(String urlStr){
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
				return connection.getInputStream();
			}else{
				return null;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
