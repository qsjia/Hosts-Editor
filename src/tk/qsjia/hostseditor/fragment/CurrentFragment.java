package tk.qsjia.hostseditor.fragment;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import tk.qsjia.hostseditor.R;
import tk.qsjia.hostseditor.util.HostsUtils;

import java.util.*;

public class CurrentFragment extends ListFragment implements SearchView.OnQueryTextListener {

	private hostsListAdapter adapter = null;
	private String preQueryString = "";
	private List<Map<String, String>> items = null;
	private List<Map<String, String>> savedItems = null;
	private List<String> selectedIds = new ArrayList<String>();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.setHasOptionsMenu(true);
		new LoadDataTask().execute();
		ListView listView = getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener(){

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				long[] ids = getListView().getCheckedItemIds();
				switch(item.getItemId()){
					case R.id.menu_edit:
						final View view = LayoutInflater.from(getActivity()).inflate(
								R.layout.custom_add_dialog, null);
						((EditText) view.findViewById(R.id.custom_ip)).setText(items.get((int)ids[0]).get("ip"));
						((EditText) view.findViewById(R.id.custom_host)).setText(items.get((int)ids[0]).get("host"));
						new AlertDialog.Builder(getActivity())
								.setView(view)
								.setTitle("编辑Host")
								.setPositiveButton("确定",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog,
													int which) {
												System.out.println(((EditText) view
														.findViewById(R.id.custom_ip))
														.getText());
												System.out.println(((EditText) view
														.findViewById(R.id.custom_host))
														.getText());
											}
										})
								.setNegativeButton("取消", null)
								.show();
						mode.finish();
						return true;
					case R.id.menu_delete:
						return true;
				}
				return true;
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.edit, menu);
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				System.out.println("----"+Arrays.toString(getListView().getCheckedItemIds())+"----");
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
				if(getListView().getCheckedItemIds().length>1){
					mode.getMenu().getItem(0).setVisible(false);
				}else{
					mode.getMenu().getItem(0).setVisible(true);
				}
			}
			
		});
	}
	
	class hostsListAdapter extends BaseAdapter implements Filterable{
		private Context context;
		private Filter mFilter;

		public hostsListAdapter(Context context) {
			super();
			this.context = context;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return Long.parseLong(items.get(position).get("index"));
		}
		
		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(R.layout.hosts_list_item, null);
				holder.itemIp = (CheckBox)convertView.findViewById(R.id.item_ip);
				holder.itemHost = (TextView)convertView.findViewById(R.id.item_host);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.itemIp.setText(items.get(position).get("ip"));
			holder.itemHost.setText(items.get(position).get("host"));
			holder.itemIp.setChecked(selectedIds.contains(items.get(position).get("index")));
			convertView.setTag(holder);
			return convertView;
		}

		@Override
		public Filter getFilter() {
			if(mFilter == null){
				mFilter = new MyFilter();
			}
			return mFilter;
		}
		
		private class MyFilter extends Filter{

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				if(TextUtils.isEmpty(constraint)){
                    results.values = savedItems;  
                    results.count = savedItems.size();
				}else{
					String filterStr = constraint.toString().toLowerCase(Locale.US);
					List<Map<String,String>> values = new ArrayList<Map<String,String>>();
					if(filterStr.length()>preQueryString.length()){
						for(Map<String,String> item : items){
							if(item.get("ip").indexOf(filterStr)>=0||item.get("host").indexOf(filterStr)>=0){
								values.add(item);
							}
						}
					}else{
						for(Map<String,String> item : savedItems){
							if(item.get("ip").indexOf(filterStr)>=0||item.get("host").indexOf(filterStr)>=0){
								values.add(item);
							}
						}
					}
					results.count = values.size();
					results.values = values;
				}
				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				preQueryString = constraint.toString();
				items = (List<Map<String,String>>)results.values;
				notifyDataSetChanged();
			}
			
		}
	}

	class ViewHolder{
		CheckBox itemIp;
		TextView itemHost;
	}

	class LoadDataTask extends AsyncTask<Void, Void, Void>{
	
		@Override
		protected void onPostExecute(Void v) {
			adapter = new hostsListAdapter(getActivity());
			setListAdapter(adapter);
		}
	
		@Override
		protected Void doInBackground(Void... params) {
			items = HostsUtils.analysisHostsFile(Environment.getRootDirectory().getPath()+"/etc/hosts");
			savedItems = new ArrayList<Map<String,String>>(items);
			return null;
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.custom, menu);
		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_custom_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
		searchView.setOnQueryTextListener(this);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		System.out.println("custom" + item.getItemId());
		switch (item.getItemId()) {
			case R.id.menu_custom_add:
				final View view = LayoutInflater.from(getActivity()).inflate(
						R.layout.custom_add_dialog, null);
				new AlertDialog.Builder(getActivity())
						.setView(view)
						.setTitle("添加自定义Host")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										System.out.println(((EditText) view
												.findViewById(R.id.custom_ip))
												.getText());
										System.out.println(((EditText) view
												.findViewById(R.id.custom_host))
												.getText());
									}
								})
						.setNegativeButton("取消", null)
						.show();
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		CheckBox checkBox = (CheckBox)v.findViewById(R.id.item_ip);
		if(checkBox.isChecked()){
			checkBox.setChecked(false);
			selectedIds.remove(items.get(position).get("index"));
		}else{
			checkBox.setChecked(true);
			selectedIds.add(items.get(position).get("index"));
		}
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if(getListAdapter()!=null){
	        ((hostsListAdapter)getListAdapter()).getFilter().filter(newText);
		}
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

}
