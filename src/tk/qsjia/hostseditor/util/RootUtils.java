package tk.qsjia.hostseditor.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 12-6-12
 * Time: 上午10:05
 * To change this template use File | Settings | File Templates.
 */
public class RootUtils {
	public static void runCommand(String[] commands, boolean requireRoot){
		Process process = null;
		BufferedReader errorReader = null;
		BufferedReader reader = null;
		PrintWriter writer = null;
		try{
			if(requireRoot){
				process = Runtime.getRuntime().exec("su");
			}else{
				process = Runtime.getRuntime().exec("sh");
			}
			errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())));

			if(commands!=null){
				for(String command : commands){
					writer.println(command);
					writer.flush();
				}
				writer.println("exit");
				writer.flush();
			}
			String tmp = null;
			while((tmp = errorReader.readLine())!= null){
				System.out.println(new String(tmp.getBytes("")));
			}
			System.out.println("-----------------------------------------------------");
			while((tmp = reader.readLine())!= null){
				System.out.println(tmp);
			}
			process.waitFor();

			errorReader.close();
			reader.close();
			writer.close();
		}catch (IOException e){
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				if(errorReader != null){
					errorReader.close();
				}
				if(reader != null){
					reader.close();
				}
				if(writer != null){
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args){
		runCommand(new String[]{"ls","ls -al"},false);
	}
}
