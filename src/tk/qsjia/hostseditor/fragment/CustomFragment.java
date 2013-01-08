package tk.qsjia.hostseditor.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.widget.*;
import tk.qsjia.hostseditor.R;
import tk.qsjia.hostseditor.util.DBHelper;
import tk.qsjia.hostseditor.util.HostsUtils;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class CustomFragment extends ListFragment implements SearchView.OnQueryTextListener {

	private String preQueryString = "";
	private DBHelper helper;
	private SimpleCursorAdapter adapter;
	private Cursor cursor;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.setHasOptionsMenu(true);
		helper = new DBHelper(getActivity());
		cursor = helper.getReadableDatabase().query(
				DBHelper.CUSTOM_TABLE_NAME,
				new String[] { "_id", "ip", "host", "used" }, null, null, null, null, null);
		adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.hosts_list_item, cursor, new String[] { "ip",
				"host" }, new int[] {
				R.id.item_ip, R.id.item_host },
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
										int columnIndex) {
				if ("ip".equals(cursor.getColumnName(columnIndex))) {
					CheckBox checkBox = (CheckBox) view;
					checkBox.setText(cursor.getString(columnIndex));
					if(cursor.getLong(cursor.getColumnIndex("used")) == 1){
						checkBox.setChecked(true);
					}else{
						checkBox.setChecked(false);
					}
					return true;
				}
				return false;
			}
		});
		//设置过滤
		adapter.setFilterQueryProvider(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence constraint) {
				return helper.getReadableDatabase().query(
						DBHelper.CUSTOM_TABLE_NAME,
						new String[]{"_id", "ip", "host", "used"}, "ip like ? or host like ?",
						new String[]{"%"+constraint+"%", "%"+constraint+"%"}, null, null, null);
			}
		});
		setListAdapter(adapter);

		ListView listView = getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener(){

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				final long[] ids = getListView().getCheckedItemIds();
				if(ids.length>0){
					switch(item.getItemId()){
						case R.id.menu_edit:
							SparseBooleanArray selectedPositions = getListView().getCheckedItemPositions();
							Cursor selectedItem = null;
							for(int i = 0;i < selectedPositions.size();i++){
								if(selectedPositions.valueAt(i)){
									selectedItem = (Cursor)getListView().getItemAtPosition(selectedPositions.keyAt(i));
								}
							}
							final View view = LayoutInflater.from(getActivity()).inflate(
									R.layout.custom_add_dialog, null);
							((EditText) view.findViewById(R.id.custom_ip)).setText(selectedItem.getString(1));
							((EditText) view.findViewById(R.id.custom_host)).setText(selectedItem.getString(2));
							new AlertDialog.Builder(getActivity())
									.setView(view)
									.setTitle("编辑Host")
									.setPositiveButton("确定",
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(DialogInterface dialog,
																	int which) {
													ContentValues values = new ContentValues();
													values.put("ip", ((EditText) view
															.findViewById(R.id.custom_ip))
															.getText().toString());
													values.put("host", ((EditText) view
															.findViewById(R.id.custom_host))
															.getText().toString());
													helper.getWritableDatabase().update(DBHelper.CUSTOM_TABLE_NAME, values, "_id = ?", new String[]{""+ids[0]});
													refreshData();
												}
											})
									.setNegativeButton("取消", null)
									.show();
							mode.finish();
							return true;
						case R.id.menu_delete:
							return true;
					}
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
				// TODO Auto-generated method stub
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
										ContentValues values = new ContentValues();
										values.put("ip", ((EditText) view
												.findViewById(R.id.custom_ip))
												.getText().toString());
										values.put("host", ((EditText) view
												.findViewById(R.id.custom_host))
												.getText().toString());
										values.put("used", "2");
										helper.getWritableDatabase().insert(DBHelper.CUSTOM_TABLE_NAME, null, values);
										refreshData();
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
		ContentValues values = new ContentValues();
		if (checkBox.isChecked()) {
			checkBox.setChecked(false);
			values.put("used",2);
			helper.getWritableDatabase().update(DBHelper.CUSTOM_TABLE_NAME, values, "_id = ?", new String[]{id+""});
		} else {
			checkBox.setChecked(true);
			values.put("used",1);
			helper.getWritableDatabase().update(DBHelper.CUSTOM_TABLE_NAME, values, "_id = ?", new String[]{id+""});
		}
		refreshData();
	}

	void refreshData(){
		cursor = helper.getReadableDatabase().query(
				DBHelper.CUSTOM_TABLE_NAME,
				new String[] { "_id", "ip", "host", "used" },
				null, null, null, null, null);
		adapter.changeCursor(cursor);
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if(getListAdapter()!=null){
	        ((CursorAdapter)getListAdapter()).getFilter().filter(newText);
		}
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

}
