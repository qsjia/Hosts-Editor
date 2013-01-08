package tk.qsjia.hostseditor.util;

import java.io.*;
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
	public static List<Map<String, String>> analysisHostsFile(File file) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		try {
			if (file != null && file.exists()) {
				list = analysisHostsFromStream(new FileInputStream(file));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			return list;
		}
	}

	public static List<Map<String, String>> analysisHostsFile(String file) {
		return analysisHostsFile(new File(file));
	}

	public static List<Map<String, String>> analysisHostsFromStream(InputStream in) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String tmp;
			int index = 0;
			while ((tmp = reader.readLine()) != null) {
				if (!tmp.startsWith("#")) {
					String[] datas = tmp.split("\\s+");
					int j = datas.length;
					if (j >= 2) {
						for (int i = 1; i < datas.length; i++) {
							Map<String, String> map = new HashMap<String, String>();
							map.put("ip", datas[0]);
							map.put("host", datas[i].toLowerCase(Locale.US));
							map.put("index", index + "");
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

	public static File makeHostsFile(List<Map<String, String>> data, String dirName) {
		File file = null;
		try {
			file = File.createTempFile("hosts", "", new File(dirName));
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for (int i = 0, j = data.size(); i < j; i++) {
				writer.print(data.get(i).get("ip") + "\t");
				writer.println(data.get(i).get("host"));
			}
			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			return file;
		}
	}

	public static void main(String[] args) {
		makeHostsFile(analysisHostsFile(Environment.getExternalStorageDirectory().getPath() + "/tk.qsjia.hostseditor/hosts"), "");
		RootUtils.runCommand(new String[]{"mv /etc/hosts /etc/hosts.bak", "mv d:\\hosts.tmp.txt /etc/hosts"}, true);
	}
}
