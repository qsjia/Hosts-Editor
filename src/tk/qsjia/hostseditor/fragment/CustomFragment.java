package tk.qsjia.hostseditor.fragment;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.*;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import tk.qsjia.hostseditor.R;
import tk.qsjia.hostseditor.util.DBHelper;
import tk.qsjia.hostseditor.util.HostsUtils;
import tk.qsjia.hostseditor.util.QRCodeUtils;
import tk.qsjia.hostseditor.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
				new String[]{"_id", "ip", "host", "used"}, null, null, null, null, null);
		adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.hosts_list_item, cursor, new String[]{"ip",
				"host"}, new int[]{
				R.id.item_ip, R.id.item_host},
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
										int columnIndex) {
				if ("ip".equals(cursor.getColumnName(columnIndex))) {
					CheckBox checkBox = (CheckBox) view;
					checkBox.setText(cursor.getString(columnIndex));
					if (cursor.getLong(cursor.getColumnIndex("used")) == 1) {
						checkBox.setChecked(true);
					} else {
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
						new String[]{"%" + constraint + "%", "%" + constraint + "%"}, null, null, null);
			}
		});
		setListAdapter(adapter);

		ListView listView = getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				final long[] ids = getListView().getCheckedItemIds();
				if (ids.length > 0) {
					switch (item.getItemId()) {
						case R.id.menu_edit:
							SparseBooleanArray selectedPositions = getListView().getCheckedItemPositions();
							Cursor selectedItem = null;
							for (int i = 0; i < selectedPositions.size(); i++) {
								if (selectedPositions.valueAt(i)) {
									selectedItem = (Cursor) getListView().getItemAtPosition(selectedPositions.keyAt(i));
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
													String ip = ((EditText) view
															.findViewById(R.id.custom_ip))
															.getText().toString();
													String host = ((EditText) view
															.findViewById(R.id.custom_host))
															.getText().toString();
													Field field;
													try {
														field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
														field.setAccessible(true);
														if (!StringUtils.isIP(ip)) {
															Toast.makeText(getActivity(), "IP格式错误！", Toast.LENGTH_SHORT).show();
															field.set(dialog, false);
														} else if (!StringUtils.isHost(host)) {
															Toast.makeText(getActivity(), "Host格式错误！", Toast.LENGTH_SHORT).show();
															field.set(dialog, false);
														} else {
															ContentValues values = new ContentValues();
															values.put("ip", ip);
															values.put("host", host);
															helper.getWritableDatabase().update(DBHelper.CUSTOM_TABLE_NAME, values, "_id = ?", new String[]{"" + ids[0]});
															refreshData();
														}
													} catch (NoSuchFieldException e) {
														e.printStackTrace();
													} catch (IllegalAccessException e) {
														e.printStackTrace();
													}
												}
											})
									.setNegativeButton("取消", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											try {
												Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
												field.setAccessible(true);
												field.set(dialog, true);
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									})
									.show();
							mode.finish();
							return true;
						case R.id.menu_delete:
							StringBuffer whereStr = new StringBuffer();
							whereStr.append("_id in (");
							for (long id : ids) {
								whereStr.append(id + ",");
							}
							helper.getWritableDatabase().delete(
									DBHelper.CUSTOM_TABLE_NAME,
									whereStr.toString().substring(0,
											whereStr.length() - 1)
											+ ")", null);
							refreshData();
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
				System.out.println("----" + Arrays.toString(getListView().getCheckedItemIds()) + "----");
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
												  int position, long id, boolean checked) {
				if (getListView().getCheckedItemIds().length > 1) {
					mode.getMenu().getItem(0).setVisible(false);
				} else {
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
				final CheckBox useSequenceCheckbox = (CheckBox) view.findViewById(R.id.use_sequence);
				useSequenceCheckbox.setVisibility(View.VISIBLE);
				final LinearLayout sequenceLayout = (LinearLayout) view.findViewById(R.id.sequence_layout);
				useSequenceCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (isChecked) {
							sequenceLayout.setVisibility(View.VISIBLE);
						} else {
							sequenceLayout.setVisibility(View.GONE);
						}
					}
				});
				new AlertDialog.Builder(getActivity())
						.setView(view)
						.setTitle("添加自定义Host")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
														int which) {
										String ip = ((EditText) view
												.findViewById(R.id.custom_ip))
												.getText().toString().trim();
										String host = ((EditText) view
												.findViewById(R.id.custom_host))
												.getText().toString().trim();
										String tmpHost = new String(host);
										String sequenceStartStr;
										String sequenceEndStr;
										String sequenceType = "";
										int sequenceStartNum = 0;
										int sequenceEndNum = 0;
										char sequenceStartChar = 'a';
										char sequenceEndChar = 'a';
										Field field;
										try {
											field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
											field.setAccessible(true);
											field.set(dialog, true);
											if (useSequenceCheckbox.isChecked()) {
												sequenceStartStr = ((EditText) sequenceLayout.findViewById(R.id.sequence_start)).getText().toString().trim();
												sequenceEndStr = ((EditText) sequenceLayout.findViewById(R.id.sequence_end)).getText().toString().trim();
												if (StringUtils.isInteger(sequenceStartStr) && StringUtils.isInteger(sequenceEndStr) && Integer.parseInt(sequenceEndStr) > Integer.parseInt(sequenceStartStr)) {
													sequenceType = "int";
													sequenceStartNum = Integer.parseInt(sequenceStartStr);
													sequenceEndNum = Integer.parseInt(sequenceEndStr);
													host = host.replaceAll("%%", sequenceStartNum + "");
												} else if (StringUtils.isAlphabet(sequenceStartStr) && StringUtils.isAlphabet(sequenceEndStr) && sequenceEndStr.compareToIgnoreCase(sequenceStartStr) > 0) {
													sequenceType = "char";
													sequenceStartChar = sequenceStartStr.toLowerCase().charAt(0);
													sequenceEndChar = sequenceEndStr.toLowerCase().charAt(0);
													host = host.replaceAll("%%", sequenceStartChar + "");
												} else {
													Toast.makeText(getActivity(), "数字或字母序列不符合要求！", Toast.LENGTH_SHORT).show();
													field.set(dialog, false);
													return;
												}
											}
											if (!StringUtils.isIP(ip)) {
												Toast.makeText(getActivity(), "IP格式错误！", Toast.LENGTH_SHORT).show();
												field.set(dialog, false);
											} else if (!StringUtils.isHost(host)) {
												Toast.makeText(getActivity(), "Host格式错误！", Toast.LENGTH_SHORT).show();
												field.set(dialog, false);
											} else {
												helper.getWritableDatabase().beginTransaction();
												if ("int".equals(sequenceType)) {
													for (int i = sequenceStartNum; i <= sequenceEndNum; i++) {
														ContentValues values = new ContentValues();
														values.put("ip", ip);
														values.put("host", tmpHost.replaceAll("%%", i + ""));
														values.put("used", "2");
														helper.getWritableDatabase().insert(DBHelper.CUSTOM_TABLE_NAME, null, values);
													}
												} else if ("char".equals(sequenceType)) {
													for (int i = (int) sequenceStartChar; i <= (int) sequenceEndChar; i++) {
														ContentValues values = new ContentValues();
														values.put("ip", ip);
														values.put("host", tmpHost.replaceAll("%%", ((char) i) + ""));
														values.put("used", "2");
														helper.getWritableDatabase().insert(DBHelper.CUSTOM_TABLE_NAME, null, values);
													}
												} else {
													ContentValues values = new ContentValues();
													values.put("ip", ip);
													values.put("host", host);
													values.put("used", "2");
													helper.getWritableDatabase().insert(DBHelper.CUSTOM_TABLE_NAME, null, values);
												}
												helper.getWritableDatabase().setTransactionSuccessful();
												helper.getWritableDatabase().endTransaction();
												refreshData();
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								})
						.setNegativeButton("取消", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
									field.setAccessible(true);
									field.set(dialog, true);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						})
						.show();
				return true;
			case R.id.menu_add_items_via_qrcode:
				QRCodeUtils.scanQrCode(this);
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null && scanResult.getContents()!=null) {
			String text = scanResult.getContents();
			List<Map<String, String>> items = HostsUtils.analysisHostsFromText(text);
			helper.getWritableDatabase().beginTransaction();
			for(Map<String, String> item : items){
				String ip = item.get("ip");
				String host = item.get("host");
				if(StringUtils.isIP(ip) && StringUtils.isHost(host)){
					ContentValues values = new ContentValues();
					values.put("ip", ip);
					values.put("host", host);
					values.put("used", "2");
					helper.getWritableDatabase().insert(DBHelper.CUSTOM_TABLE_NAME, null, values);
				}
			}
			helper.getWritableDatabase().setTransactionSuccessful();
			helper.getWritableDatabase().endTransaction();
			refreshData();
		}else{
			Toast.makeText(getActivity(), "抱歉，无法读取二维码数据！", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		CheckBox checkBox = (CheckBox) v.findViewById(R.id.item_ip);
		ContentValues values = new ContentValues();
		if (checkBox.isChecked()) {
//			checkBox.setChecked(false);
			values.put("used", 2);
			helper.getWritableDatabase().update(DBHelper.CUSTOM_TABLE_NAME, values, "_id = ?", new String[]{id + ""});
		} else {
//			checkBox.setChecked(true);
			values.put("used", 1);
			helper.getWritableDatabase().update(DBHelper.CUSTOM_TABLE_NAME, values, "_id = ?", new String[]{id + ""});
		}
		refreshData();
	}

	void refreshData() {
		cursor = helper.getReadableDatabase().query(
				DBHelper.CUSTOM_TABLE_NAME,
				new String[]{"_id", "ip", "host", "used"},
				null, null, null, null, null);
		adapter.changeCursor(cursor);
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (getListAdapter() != null) {
			((CursorAdapter) getListAdapter()).getFilter().filter(newText);
		}
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

}
