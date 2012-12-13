package tk.qsjia.hostseditor.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tk.qsjia.hostseditor.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class SettingFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
		for (int i = 0; i < 10; i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("name", "name" + i);
			item.put("value", "value" + i);
			items.add(item);
		}
		ListView listView = (ListView) inflater.inflate(R.layout.remote, null).findViewById(R.id.remoteList);
		ListAdapter adapter = new SimpleAdapter(this.getActivity(), items,
				android.R.layout.simple_list_item_2, new String[] {
						"name", "value" }, new int[] {
						android.R.id.text1, android.R.id.text2 });
		listView.setAdapter(adapter);
		return listView;
	}

}
