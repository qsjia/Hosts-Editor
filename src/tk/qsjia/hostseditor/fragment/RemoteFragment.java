package tk.qsjia.hostseditor.fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.widget.*;
import tk.qsjia.hostseditor.R;
import tk.qsjia.hostseditor.util.DBHelper;
import tk.qsjia.hostseditor.util.NetworkUtils;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView.OnQueryTextListener;

public class RemoteFragment extends ListFragment implements OnQueryTextListener {
	@Override
	public void onDestroy() {
		super.onDestroy();
		helper.close();
	}

	private DBHelper helper;
	private SimpleCursorAdapter adapter;
	private Cursor cursor;
	private List<String> selectedIds = new ArrayList<String>();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.setHasOptionsMenu(true);
		helper = new DBHelper(getActivity());
		// LoaderManager
		cursor = helper.getReadableDatabase().query(
				DBHelper.REMOTE_TABLE_NAME,
				new String[] { "_id", "url", "local_date", "remote_date",
						"used" }, null, null, null, null, null);
		adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.url_list_item, cursor, new String[] { "url",
						"local_date", "remote_date" }, new int[] {
						R.id.item_url, R.id.item_local_date,
						R.id.item_remote_date },
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				if ("url".equals(cursor.getColumnName(columnIndex))) {
					CheckBox checkBox = (CheckBox) view;
					checkBox.setText(cursor.getString(columnIndex));
					if (selectedIds.contains(cursor.getString(0))) {
						checkBox.setChecked(true);
					} else {
						checkBox.setChecked(false);
					}
					return true;
				}
				return false;
			}
		});
		adapter.setFilterQueryProvider(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence constraint) {
				return helper.getReadableDatabase().query(
						DBHelper.REMOTE_TABLE_NAME,
						new String[] { "_id", "url", "local_date", "remote_date",
								"used" }, "url like ?", new String[]{"%"+constraint+"%"}, null, null, null);
			}
		});
		setListAdapter(adapter);
		ListView listView = this.getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				final long[] ids = getListView().getCheckedItemIds();
				switch (item.getItemId()) {
					case R.id.menu_delete:
						StringBuffer whereStr = new StringBuffer();
						whereStr.append("_id in (");
						for (long id : ids) {
							whereStr.append(id + ",");
						}
						helper.getWritableDatabase().delete(
								DBHelper.REMOTE_TABLE_NAME,
								whereStr.toString().substring(0,
										whereStr.length() - 1)
										+ ")", null);
						refreshData();
						return true;
					case R.id.menu_edit:
						final EditText urlEditText = new EditText(getActivity());
						urlEditText.setHint("远程hosts文件地址");
						urlEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
						SparseBooleanArray sba = getListView().getCheckedItemPositions();
						int position = sba.keyAt((sba.indexOfValue(true)));
						Cursor c = (Cursor)getListView().getItemAtPosition(position);
						urlEditText.setText(c.getString(c.getColumnIndex("url")));
						new AlertDialog.Builder(getActivity())
								.setTitle("修改")
								.setView(urlEditText)
								.setPositiveButton("确定",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog,
													int which) {
												String url = urlEditText.getText()
														.toString();
												if (!TextUtils.isEmpty(url)) {
													new NetWorkAsyncTask().execute(url, ids[0]+"");
												}
											}
										}).setNegativeButton("取消", null).show();
						return true;
					default:
						return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.edit, menu);
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub

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
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		CheckBox checkBox = (CheckBox) v.findViewById(R.id.item_url);
		if (checkBox.isChecked()) {
			checkBox.setChecked(false);
			selectedIds.remove("" + id);
		} else {
			checkBox.setChecked(true);
			selectedIds.add("" + id);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.remote, menu);
		SearchManager searchManager = (SearchManager) getActivity()
				.getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(
				R.id.menu_remote_search).getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getActivity().getComponentName()));
		searchView.setOnQueryTextListener(this);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_remote_add:
				final EditText urlEditText = new EditText(getActivity());
				urlEditText.setText("http://");
				urlEditText.setSelection(7);
				urlEditText.setHint("远程hosts文件地址");
				urlEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
				new AlertDialog.Builder(getActivity())
						.setTitle("添加")
						.setView(urlEditText)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										String url = urlEditText.getText()
												.toString();
										if (!TextUtils.isEmpty(url)) {
											new NetWorkAsyncTask().execute(url);
										}
									}
								}).setNegativeButton("取消", null).show();
				return true;
			default:
				return false;
		}
	}

	void refreshData(){
		cursor = helper.getReadableDatabase().query(
				DBHelper.REMOTE_TABLE_NAME,
				new String[] { "_id", "url", "local_date",
						"remote_date", "used" }, null, null,
				null, null, null);
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
		// TODO Auto-generated method stub
		return false;
	}

	class NetWorkAsyncTask extends AsyncTask<String, Void, Long> {
		String url = null;
		String id = null;

		@Override
		protected Long doInBackground(String... params) {
			this.url = params[0];
			if(params.length == 2){
				id = params[1];
			}
			return NetworkUtils.getModifyDate(url);
		}

		@Override
		protected void onPostExecute(Long result) {
			if (result == -1) {
				new AlertDialog.Builder(getActivity()).setTitle("URL格式错误！")
						.setPositiveButton("确定", null).show();
			} else if (result == -2) {
				new AlertDialog.Builder(getActivity()).setTitle("网络连接错误！")
						.setPositiveButton("确定", null).show();
			} else {
				DateFormat fmt = SimpleDateFormat.getDateTimeInstance();
				ContentValues values = new ContentValues();
				values.put("url", url);
				values.put("remote_date", fmt.format(new Date(result)));
				if(id == null){
					values.put("used", 0);
					helper.getWritableDatabase().insert(DBHelper.REMOTE_TABLE_NAME, null, values);
				}else{
					helper.getWritableDatabase().update(DBHelper.REMOTE_TABLE_NAME, values, "_id = ?", new String[]{id});
				}
				refreshData();
			}
		}

	}

}
