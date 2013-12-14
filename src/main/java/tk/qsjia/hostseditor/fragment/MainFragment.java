package tk.qsjia.hostseditor.fragment;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tk.qsjia.hostseditor.R;
import tk.qsjia.hostseditor.util.DBHelper;
import tk.qsjia.hostseditor.util.HostsUtils;
import tk.qsjia.hostseditor.util.NetworkUtils;
import tk.qsjia.hostseditor.util.RootUtils;

public class MainFragment extends Fragment {
	private Button enableBtn;
	private Button disableBtn;
	private DBHelper helper;
	private Cursor cursor;
	private ProgressDialog progressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		helper = new DBHelper(getActivity());

		View view = inflater.inflate(R.layout.main_frag, null);
		enableBtn = (Button) view.findViewById(R.id.enable_btn);
		enableBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("enable");
				new HostsAsyncTask().execute(new String[]{"enable"});
			}
		});
		disableBtn = (Button) view.findViewById(R.id.disable_btn);
		disableBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("disable");
				new HostsAsyncTask().execute(new String[]{"disable"});
			}
		});
		return view;
	}

	class HostsAsyncTask extends AsyncTask<String, Integer, Long> {

		@Override
		protected Long doInBackground(String... params) {
			List<Map<String, String>> hosts = new ArrayList<Map<String, String>>();
			File cacheDir = getActivity().getExternalCacheDir();
			if (cacheDir == null) {
				return -1l;
			}
			//启用自定义hosts
			if ("enable".equals(params[0])) {
				int flag = RootUtils.mountSystemAsRW();
				if (flag == 0) {
					//获取远程hosts文件
					cursor = helper.getReadableDatabase().query(
							DBHelper.REMOTE_TABLE_NAME,
							new String[]{"_id", "url", "local_file", "local_date", "remote_date",
									"used"}, "used=?", new String[]{"1"}, null, null, null);
					for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
						String localFileName = cursor.getString(cursor.getColumnIndex("local_file"));
						File localFile = null;
						if (localFileName != null) {
							localFile = new File(localFileName);
						}
						if (cursor.getLong(cursor.getColumnIndex("remote_date")) > cursor.getLong(cursor.getColumnIndex("local_date")) ||
								(localFile != null && !localFile.exists())) {
							File file = NetworkUtils.downloadHostsFile(cursor.getString(cursor.getColumnIndex("url")),
									cacheDir.getAbsolutePath());
							if (file != null) {
								hosts.addAll(HostsUtils.analysisHostsFile(file));
								ContentValues values = new ContentValues();
								values.put("local_file", file.getAbsolutePath());
								helper.getWritableDatabase().update(DBHelper.REMOTE_TABLE_NAME, values, "_id = ?", new String[]{cursor.getString(cursor.getColumnIndex("_id"))});
							}
						} else {
							hosts.addAll(HostsUtils.analysisHostsFile(localFile));
						}
					}
					cursor.close();
					System.out.println("remote count = " + cursor.getCount());

					//自定义的hosts条目
					cursor = helper.getReadableDatabase().query(
							DBHelper.CUSTOM_TABLE_NAME,
							new String[]{"_id", "ip", "host", "used"}, "used=?", new String[]{"1"}, null, null, null);
					System.out.println("custom count = " + cursor.getCount());
					for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
						Map<String, String> hostItem = new HashMap<String, String>();
						hostItem.put("ip", cursor.getString(cursor.getColumnIndex("ip")));
						hostItem.put("host", cursor.getString(cursor.getColumnIndex("host")));
						hosts.add(hostItem);
					}
					cursor.close();

					File newHostsFile = HostsUtils.makeHostsFile(hosts, cacheDir.getAbsolutePath());
					if (newHostsFile != null) {
						String hostsFileName = newHostsFile.getAbsolutePath();
						RootUtils.runCommand(new String[]{
								"cat " + hostsFileName + " > /system/etc/hosts",
								"chmod 0644 /system/etc/hosts",
								"rm -f " + hostsFileName}, true);
					}
					RootUtils.mountSystemAsRO();
				}
			} else if ("disable".equals(params[0])) {//禁用自定义hosts,还原默认hosts文件
				int flag = RootUtils.mountSystemAsRW();
				if (flag == 0) {
					RootUtils.runCommand(new String[]{
							"echo \"127.0.0.1 localhost\" > /system/etc/hosts",
							"chmod 0644 /system/etc/hosts"}, true);
					RootUtils.mountSystemAsRO();
				}
			}
			return 0l;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(getActivity(), "应用Hosts文件", "请稍候。。。");
		}

		@Override
		protected void onPostExecute(Long result) {
			if (result == -1) {
				Toast.makeText(getActivity(), "存储卡不可用！", Toast.LENGTH_SHORT).show();
			}
			progressDialog.dismiss();
		}
	}
}
