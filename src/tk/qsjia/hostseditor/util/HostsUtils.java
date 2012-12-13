package tk.qsjia.hostseditor.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.os.Environment;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 12-6-12
 * Time: 上午10:14
 * To change this template use File | Settings | File Templates.
 */
public class HostsUtils {
	public static List<Map<String,String>> analysisHostsFile(String file){
		List<Map<String,String>> list = new ArrayList<Map<String, String>>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String tmp = null;
			int index = 0;
			while((tmp = reader.readLine()) != null){
				if(!tmp.startsWith("#")){
					String[] datas = tmp.split("\\s+");
					int j = datas.length;
					if(j>=2){
						for(int i = 1;i < datas.length;i++){
							Map<String,String> map = new HashMap<String, String>();
							map.put("ip",datas[0]);
							map.put("host",datas[i].toLowerCase(Locale.US));
							map.put("index", index+"");
							list.add(map);
							index++;
						}
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static List<Map<String,String>> analysisHostsFromStream(InputStream in){
		List<Map<String,String>> list = new ArrayList<Map<String, String>>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String tmp = null;
			int index = 0;
			while((tmp = reader.readLine()) != null){
				if(!tmp.startsWith("#")){
					String[] datas = tmp.split("\\s+");
					int j = datas.length;
					if(j>=2){
						for(int i = 1;i < datas.length;i++){
							Map<String,String> map = new HashMap<String, String>();
							map.put("ip",datas[0]);
							map.put("host",datas[i].toLowerCase(Locale.US));
							map.put("index", index+"");
							list.add(map);
							index++;
						}
					}
				}
			}
			in.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static void makeHostsFile(List<Map<String,String>> data){
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("d:\\hosts.tmp.txt")));
			for(int i=0,j=data.size();i<j;i++){
				writer.print(data.get(i).get("ip")+"\t");
				writer.println(data.get(i).get("host"));
			}
			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args){
		makeHostsFile(analysisHostsFile(Environment.getExternalStorageDirectory().getPath()+"/tk.qsjia.hostseditor/hosts"));
		RootUtils.runCommand(new String[]{"mv /etc/hosts /etc/hosts.bak","mv d:\\hosts.tmp.txt /etc/hosts"},true);
	}
}
