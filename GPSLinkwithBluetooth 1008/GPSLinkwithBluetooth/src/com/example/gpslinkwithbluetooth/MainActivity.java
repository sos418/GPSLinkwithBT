package com.example.gpslinkwithbluetooth;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

public class MainActivity extends AppCompatActivity {
	//Layout Variables
	private TextView msgText, btconnectstatue, stationConnectStatue, recordTime;
	private TextView btConnectDevice, btRSbyte, ConnectStationAddr, ConnectStationRSbyte,BuildInGPSStatus;
	private TextView LongtitudeText, LatitudeText, PointCount, LongtitudeHead, LatitudeHead, LocateStatusText;
	private TextView LabelAxisX, LabelAxisY, GPSTimeText, btScanConnectStatus, btMacConnectStatus;
	private EditText ip_et, port_et;
	private EditText mac1, mac2, mac3, mac4, mac5, mac6;
	private Button BTScan, BTScanConnect, btMacConnect, StationConnect, StationCancel;
	private RadioButton rBtn_WGS84, rBtn_TWD97;
	private RadioGroup rGroup_Cordinate;
	private ToggleButton BtnSave;
	private LineChartView lineChart;
	private LineChartData lineChartdata;
	private ColumnChartView chart;
	private WebView webview;
	private ScrollView mscrollView;
	private Axis axisX, axisY;
	private CheckBox AutoScale, AutoScroll;
	private Switch BTSwitch, StationSwitch, BuiltInGpsSwitch;
	private ImageView Btnclear, BtnDrawMap, BtnClearPlot;
	private Spinner spinner1;
	private SimpleAdapter adapter1;
	public ViewPager viewPager;
	public ViewPager BTDialogViewPager;
	private AlertDialog editDialog = null, ConnectStationDialog = null;
	private TabLayout tabLayout, BTDialogTab;
	private LinearLayout llo, satloc;
	private Button RtcmDecodeButton;
	private ProgressDialog progressDialod;

	//boolean
	private boolean firstplot = true, initial = true;
	private boolean fromFloatingToKinematic = true,GPSInvalidToFix=true, SendData2ServerComplete = false;
	private boolean sppConnected = false, isconnectserver = false, built_in_gps_connect = false;
	private boolean isStartBTConnect = false, isBTBdRegister = false, isStartStationConnect = false, StationShutdown = false,BTShutdown=false;
	private boolean isNMEAEnd = false, isContinueCollectNMEA = false, NMEAComplete = false;
	private boolean NMEAcheck = true, plotLocus = false;

	private String devAddress = null;
	private String devNameAddress = null;
	private String LocateStatus = null;
	private String StationConectErrorMsg = "連線主站失敗";
	private String btScanConnectStatusStr = "未連線", btMacConnectStatusStr = "未連線", btConnectStatusStr = "未連線";
	private String BuildinGPSStatusStr="未開啟";
	private String btConnectDeviceStr = "Device|Mac Address", btRSbyteStr = "0.000 KB / 0.000 KB";
	private String ConnectStationStatusStr = "未連線至主站", ConnectStationAddrStr = "IP:Port", ConnectStationRSbyteStr = "0.000 KB / 0.000 KB";
	private String provider;
	private String posBTFileName = null, posBTFilepath = null;
	private String GPSTime = "0", Latitude = "000.000000000", Longitude = "00.000000000", Height;
	private String data = "", msg = "", BuildInGPSNMEA = "";
	private static final String MAP_URL = "file:///android_asset/GoogleMapV3.html";

	private List<PointValue> values = new ArrayList<PointValue>();
	private ArrayList<Map<String, String>> devices = new ArrayList<Map<String, String>>();
	private ArrayList<SatData> SatData = new ArrayList<SatData>();
	private ArrayList<LocationData> LocationDataList = new ArrayList<LocationData>();
	private ArrayList<Map<String, String>> maps = new ArrayList<Map<String, String>>();

	private int pointCount = 0;
	private int viewTop, viewBottom, viewLeft, viewRight, scale;
	private int pos1 = 0, pos2 = 0,p1 = 0, p2 = 0;
	private int btConnectScanStatusColor = Color.RED, btConnectMacStatusColor = Color.RED, btConnectStatusColor = Color.RED;
	private int StationConnectStatusColor = Color.RED,BuildinGPSStatusColor=Color.RED;
	private int nowtap, msgCount = 0, InputStreamEmptyCount = 0;
	private final static int REQUEST_ENABLE_BT = 1;

	private long NMEA_TimeStamp = 0;
	private long recordtime = 172800, PosRecordTime = 172800;

	private float tempTop = 80;
	private float tempBottom = 0;
	private float tempLeft = 0;
	private float tempRight = 60;
	private double btReceiveByte = 0.000, btSendByte = 0.000, ConnetStationReceiveByte = 0.000, ConnetStationSendByte = 0.000;


	static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	static final UUID uuid = UUID.fromString(SPP_UUID);
	static final String TAG = "BTSPP";

	private BufferedOutputStream bufFileOut = null;
	private BufferedWriter posbufFileOut = null;
	private BluetoothAdapter mBluetoothAdapter;
	private SharedPreferences BTmacAddr;
	private DataOutputStream dataOutStream;
	private DataInputStream dataInStream;
	private ConnectServer m_connectserver;
	private Socket SSocket;
	private SendData2Server m_SendData2Server;
	private SppConnect sppConnect;
	private OutputStream BTOutputStream;
	private Timer timer;
	private WifiManager wifi;
	private ListView WiFiLV;
	private LocationManager locationManager;
	private IntentFilter intentfilter;

	static {
		System.loadLibrary("native-lib");
	}

	/*List<ScanResult> wiFiResults;
	ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
	SimpleAdapter adapter;*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int MonitorWidthfactor=metrics.widthPixels/90;
		//toolbar setting
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setOnMenuItemClickListener(onMenuItemClick);
		toolbar.setLogo(resize(getResources().getDrawable(R.drawable.gpprd02_appbar), (int)(MonitorWidthfactor*10.67), MonitorWidthfactor*8));
		//128 96
		//tablayout+viewpager
		viewPager = (ViewPager) findViewById(R.id.pager);
		tabLayout = (TabLayout) findViewById(R.id.pager_tabs);
		if (viewPager != null) {
			viewPager.setAdapter(new HomeViewPagerAdapter(
					getSupportFragmentManager()));
			tabLayout.setupWithViewPager(viewPager);
			viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
			tabLayout.setOnTabSelectedListener(
					new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
						@Override
						public void onTabSelected(TabLayout.Tab tab) {
							super.onTabSelected(tab);
							nowtap = tab.getPosition();
						}
					});
			setupTabIcons(MonitorWidthfactor);
			viewPager.setCurrentItem(2);
		}

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 初始化藍芽
		intentfilter = new IntentFilter();
		intentfilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentfilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intentfilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		intentfilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

		//ask permission
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.ACCESS_COARSE_LOCATION)) {
			} else {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
						2);
			}
		}
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			} else {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						3);
			}
		}

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available !", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	private Drawable resize(Drawable image, int dstWidth, int dstHeight) {
		Bitmap b = ((BitmapDrawable) image).getBitmap();
		Bitmap bitmapResized = Bitmap.createScaledBitmap(b, dstWidth, dstHeight, false);
		return new BitmapDrawable(getResources(), bitmapResized);
	}

	private void setupTabIcons(int m_MonitorWidthfactor) {

		TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
		tabOne.setText("連線設定");
		tabOne.setCompoundDrawablesWithIntrinsicBounds(null, resize(getResources().getDrawable(R.drawable.ic_settings_white_48dp), m_MonitorWidthfactor*6, m_MonitorWidthfactor*6), null, null);
		tabLayout.getTabAt(0).setCustomView(tabOne);

		TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
		tabTwo.setText("NMEA");
		tabTwo.setCompoundDrawablesWithIntrinsicBounds(null, resize(getResources().getDrawable(R.drawable.ic_speaker_notes_white_48dp), m_MonitorWidthfactor*6, m_MonitorWidthfactor*6), null, null);
		tabLayout.getTabAt(1).setCustomView(tabTwo);

		TextView tabThree = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
		tabThree.setText("即時位置");
		tabThree.setCompoundDrawablesWithIntrinsicBounds(null, resize(getResources().getDrawable(R.drawable.ic_location_on_white_48dp), m_MonitorWidthfactor*6, m_MonitorWidthfactor*6), null, null);
		tabLayout.getTabAt(2).setCustomView(tabThree);

		TextView tabFour = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
		tabFour.setText("衛星狀態");
		tabFour.setCompoundDrawablesWithIntrinsicBounds(null, resize(getResources().getDrawable(R.drawable.satellite2), m_MonitorWidthfactor*6, m_MonitorWidthfactor*6), null, null);
		tabLayout.getTabAt(3).setCustomView(tabFour);
	}


	private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem menuItem) {

			switch (menuItem.getItemId()) {
				case R.id.action_exit:
					Process.killProcess(Process.myPid());
					sppConnected = false;
					if (isBTBdRegister) {
						getApplicationContext().unregisterReceiver(BluetoothBroadcastReveiver);
						isBTBdRegister = false;
					}
					finish();
					break;
			}
			return true;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 2: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				} else {
				}
				return;
			}
			case 3: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				} else {
				}
				return;
			}

		}
	}

	// 畫長條圖
	private void setcolumnchartData() {
		int numSubcolumns = 1;
		int numColumns = SatData.size();

		List<Column> columns = new ArrayList<Column>();
		List<AxisValue> axisValues = new ArrayList<AxisValue>();

		List<SubcolumnValue> values;
		for (int i = 0; i < numColumns; i++) {

			values = new ArrayList<>();
			for (int j = 0; j < numSubcolumns; ++j) {
				values.add(new SubcolumnValue(SatData.get(i).getsnr(), ChartUtils.COLOR_GREEN));

			}
			axisValues.add(new AxisValue(i).setLabel(String.valueOf(SatData.get(i).getnumber())));
			Column column = new Column(values);
			columns.add(column);
		}

		ColumnChartData data = new ColumnChartData(columns);

		Axis axisY = new Axis().setHasLines(true);
		Axis axisX = new Axis(axisValues);
		axisY.setTextColor(Color.parseColor("#000000"));
		axisY.setLineColor(Color.parseColor("#000000"));
		axisX.setTextColor(Color.parseColor("#000000"));
		axisY.setName("dB");
		axisX.setName("Satellite Number");
		axisX.setMaxLabelChars(1);
		data.setAxisYLeft(axisY);
		data.setAxisXBottom(axisX);
		chart.setColumnChartData(data);

		final Viewport v = new Viewport(chart.getMaximumViewport());
		v.bottom = 0;
		v.top = 55;
		chart.setMaximumViewport(v);
		chart.setCurrentViewport(v);
	}


	private String getBTFileName() {
		//先行定義時間格式
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HHmmss");

		//取得現在時間
		Date dt = new Date();

		//透過SimpleDateFormat的format方法將Date轉為字串
		String dts = sdf.format(dt);
		return dts;
	}

	private void writeToSDcard(byte[] data) {
		try {
			bufFileOut.write(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private final BroadcastReceiver BluetoothBroadcastReveiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			Bundle bundle = intent.getExtras();
			Object[] listName = bundle.keySet().toArray();

			// 顯示所有收到的資訊及細節
			for (int i = 0; i < listName.length; i++) {
				String keyName = listName[i].toString();
				Log.e(TAG,
						"+ BroadcastReceiver KeyNAme : "
								+ String.valueOf(bundle.get(keyName)));
			}
			BluetoothDevice device = null;
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Map<String, String> datum = new HashMap<String, String>(2);
				datum.put("BTName", device.getName());
				datum.put("BTMac", device.getAddress());
				devices.add(datum);
				adapter1.notifyDataSetChanged(); // 通知adapter1 devices有更新
			} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				switch (device.getBondState()) {
					case BluetoothDevice.BOND_BONDING:
						Log.d(TAG, "正在配對...");
						break;
					case BluetoothDevice.BOND_BONDED:
						Log.d(TAG, "完成配對");
						break;
					case BluetoothDevice.BOND_NONE:
						Log.d(TAG, "取消配對");
						break;
					default:
						break;
				}
			}
		}
	};


	private class ConnectServer extends Thread {
		public void run() {
			try {
				try {
					//Log.i(TAG, "isconnectserver" + "conectservering");
					InetSocketAddress sockaddr = new InetSocketAddress(ip_et
							.getText().toString(), Integer.parseInt(port_et
							.getText().toString()));
					SSocket = new Socket();
					SppConnecthandler.sendEmptyMessage(3);
					SSocket.connect(sockaddr, 4000);


					if (SSocket.isConnected()) {
						SppConnecthandler.sendEmptyMessage(1);
						dataOutStream = new DataOutputStream(SSocket.getOutputStream());
						dataInStream = new DataInputStream(SSocket.getInputStream());
						dataOutStream.flush();
						isconnectserver = true;
						//Log.e("debug", "connectserver thread start complete");
					}


					//加入SppReceicer(Socket)
					timer = new Timer(true);
					timer.schedule(new SppReceiver(SSocket), 1000, 500);

				} catch (UnknownHostException e) {
					StationConectErrorMsg = "連線主站失敗";
					SppConnecthandler.sendEmptyMessage(2);
					Log.i(TAG, "isconnectserver" + e.toString());
				}

			} catch (Exception e) {
				StationConectErrorMsg = "連線主站失敗，主站端未開啟" + "\n" + "或IP/Port輸入錯誤";
				SppConnecthandler.sendEmptyMessage(2);
				Log.i(TAG, "isconnectserver" + e.toString());
			}

		}

		public void cancel() {
			isconnectserver = false;
			//Log.e("debug", "connectserver thread cancel 1");
			if (m_SendData2Server != null) {
				SendData2ServerComplete = true;
				if (!m_SendData2Server.isInterrupted())
					m_SendData2Server.interrupt();
				m_SendData2Server = null;
			}
			try {
				dataOutStream.close();
				dataInStream.close();
				SSocket.close();
				SSocket = null;
				dataOutStream = null;
				dataInStream = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Log.e("debug", "connectserver thread cancel 2");
		}
	}

	//public void SendData2Server (){{
	public class SendData2Server extends Thread {
		public byte[] tempmsg;
		public int size;

		public void run() {

			if (SSocket.isConnected()) {
				if (tempmsg != null) {
					Log.e(TAG, "Server " + tempmsg);
					try {
						ConnetStationSendByte += (double) size / 1000;
						dataOutStream.write(tempmsg);
					} catch (IOException e) {
						e.printStackTrace();
						m_connectserver.cancel();
						m_connectserver.interrupt();
						m_connectserver = null;
						StationConectErrorMsg = "連線主站失敗，主站端已取消連結\n或手機網路訊號不佳";
						SppConnecthandler.sendEmptyMessage(2);
						return;
					}
//					if (NMEAComplete)
					//將此改成true執行從伺服器接收資訊
					if (true)
						ReceivefromServer();
				}
			} else {
				Log.e("debug", "SendData2Server SSocket.isnotConnected");
				m_connectserver.cancel();
				m_connectserver.interrupt();
				m_connectserver = null;
				StationConectErrorMsg = "連線主站失敗";
				SppConnecthandler.sendEmptyMessage(2);
				return;
			}
			SendData2ServerComplete = true;
		}

		private void ReceivefromServer() {
			byte[] buffer = new byte[4096];
			int read = 0;
			try {
				if (dataInStream.available() > 0) {
					read = dataInStream.read(buffer, 0, 4096);
					ConnetStationReceiveByte += (double) read / 1000;

					if (read != -1) {
						Log.e("debug", "SendData2Server ReceivefromServer read");
						byte[] tempdata = new byte[read];
						System.arraycopy(buffer, 0, tempdata, 0, read);
						String situation = new String(tempdata);
						Log.e("debug", situation);
						//將situation寫入到txt

						String[] temps = situation.split(",");
						//Log.i("situation", situation);

						if (temps.length > 3) {
							Log.i("lat", temps[1]);
							Log.i("lon", temps[2]);
							GPSTime = temps[0];
							Latitude = temps[1].split("\\s+")[1];
							Longitude = temps[2].split("\\s+")[1];
							Height = temps[3];
							LocateStatus = temps[4].substring(1, 2);
							SavePos();
							NMEAfilter(/*temps[0],*/ temps[1], temps[2]);
							try {
								BTOutputStream.write(data.getBytes());
							}
							catch (IOException e){
								e.printStackTrace();
							}
							btSendByte += (double) data.getBytes().length / 1000;
							msg += "\n" + data + "\n";
							mBluetoothHandler.sendEmptyMessage(0);
							charthandler.sendEmptyMessage(0);
						}
						InputStreamEmptyCount = 0;
					}
				}
				//先將此註解，略過沒抓到資料時，會斷線
//				else if (InputStreamEmptyCount > 20) {
//					m_connectserver.cancel();
//					m_connectserver.interrupt();
//					m_connectserver = null;
//					InputStreamEmptyCount=0;
//					StationConectErrorMsg = "連線主站失敗";
//					SppConnecthandler.sendEmptyMessage(2);
//					Log.e("debug", StationConectErrorMsg);
//				}
				else
					InputStreamEmptyCount++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void SavePos() {
		if (PosRecordTime >= 172800) {
			posBTFileName = "Pos-" + getBTFileName() + ".txt";
			posBTFilepath = Environment.getExternalStorageDirectory().getPath();    //建立自己的目錄
			File dir = new File(posBTFilepath + "/GPSData");
			if (!dir.exists()) {
				dir.mkdir();
			}
			PosRecordTime = 0;
		}

		try {
			FileWriter fw = new FileWriter(posBTFilepath + "/GPSData/" + posBTFileName, true);
			posbufFileOut = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
			if (PosRecordTime == 0) {
				posbufFileOut.write("DataNum,Type,Longitude,Latitude,Height");
				posbufFileOut.newLine();
				posbufFileOut.write("(Type definition: Floating=2 ; Kinematic=3)");
				posbufFileOut.newLine();
				posbufFileOut.newLine();
			}

			int status = Integer.parseInt(LocateStatus);
			String tempStr = String.valueOf(PosRecordTime) + "," + String.valueOf(status) + ",";
			tempStr += " " + Longitude + "," + " " + Latitude + "," + Height;
			posbufFileOut.append(tempStr);
			posbufFileOut.newLine();
			posbufFileOut.close();
			posbufFileOut = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		PosRecordTime++;
	}

	public void connected(BluetoothSocket BTSocketIn) throws IOException {
		// TODO Auto-generated method stub
		sppConnected = true;
		Log.e(TAG, "++ connected() : BTSocketIn = " + BTSocketIn);
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		BTOutputStream = BTSocketIn.getOutputStream();
		//原本的BT connect裡的SppReceiver先刪掉
//		timer = new Timer(true);
//		timer.schedule(new SppReceiver(BTSocketIn), 1000, 500);
		Log.e(TAG, "++ connected() : sppReceiver.start();");
		Log.e(TAG, "++ connected() 成功，連線中");
		SppConnecthandler.sendEmptyMessage(4);
	}


	private class SppConnect extends Thread {

		private final BluetoothSocket mBluetoothSocket;

		public SppConnect() {
			BluetoothSocket tmpBluetoothSocket = null;

			try {
				tmpBluetoothSocket = mBluetoothAdapter.getRemoteDevice(
						devAddress).createRfcommSocketToServiceRecord(uuid);
				Log.d(TAG, "SppConnect(): createRfcommSocketToServiceRecord ");
			} catch (Exception e) {
				// TODO: handle exception
			}
			mBluetoothSocket = tmpBluetoothSocket;
		}

		public void run() {
			mBluetoothAdapter.cancelDiscovery();
			Log.d(TAG, "SppConnect(): mBluetoothAdapter.cancelDiscovery(); ");

			try {
				mBluetoothSocket.connect();
				Log.d(TAG, "SppConnect():mBluetoothSocket.connect(); ");
				synchronized (MainActivity.this) {
					if (sppConnected) {
						return;
					}
					connected(mBluetoothSocket);
				}
			} catch (IOException e) {
				// TODO: handle exception
				Log.d(TAG,
						"--- SppConnect():mBluetoothSocket.connect(); Failed!!! ");
				try {
					mBluetoothSocket.close();
					Log.d(TAG, "--- SppConnect():mBluetoothSocket.close() ");
				} catch (IOException e2) {
					// TODO: handle exception
					Log.d(TAG, "mBluetoothSocket.close() failed!");
				}
				connectionFailed(0);

			}
		}

		public void cancel() {
			try {
				mBluetoothSocket.close();
			} catch (IOException e) {
				// TODO: handle exception
			}
		}
	}

	Handler SppConnecthandler = new Handler() {
		public void handleMessage(Message message) {
			switch (message.what) {
				case 0:
					btConnectStatusStr = "連線失敗";
					btConnectStatusColor = Color.RED;
					btconnectstatue.setText(btConnectStatusStr);
					btconnectstatue.setTextColor(btConnectStatusColor);

					btScanConnectStatusStr = "連線失敗";
					btConnectScanStatusColor = Color.RED;
					btScanConnectStatus.setText(btScanConnectStatusStr);
					btScanConnectStatus.setTextColor(btConnectScanStatusColor);

					btMacConnectStatusStr = "連線失敗";
					btConnectMacStatusColor = Color.RED;
					btMacConnectStatus.setText(btScanConnectStatusStr);
					btMacConnectStatus.setTextColor(btConnectScanStatusColor);

					spinner1.setEnabled(true);
					BuiltInGpsSwitch.setEnabled(true);
					if (!editDialog.isShowing())
						BTSwitch.setChecked(false);
					isStartBTConnect = false;
					break;
				case 1:
					ConnectStationStatusStr = "主站連接成功";
					StationConnectStatusColor = Color.BLUE;
					stationConnectStatue.setTextColor(StationConnectStatusColor);
					stationConnectStatue.setText(ConnectStationStatusStr);
					SharedPreferences set = getSharedPreferences("StationSetting", 0);
					ConnectStationAddrStr = set.getString("ip", "") + ":" + set.getString("port", "");
					ConnectStationAddr.setText(ConnectStationAddrStr);
					resetLineChart();
					break;
				case 2:
					ConnectStationStatusStr = StationConectErrorMsg;
					StationConnectStatusColor = Color.RED;
					stationConnectStatue.setTextColor(Color.RED);
					stationConnectStatue.setText(ConnectStationStatusStr);
					isStartStationConnect = false;
					ConnectStationAddrStr = "IP:Port";
					ConnectStationRSbyteStr = "0.000 KB / 0.000 KB";
					ConnectStationRSbyte.setText(ConnectStationRSbyteStr);
					ConnectStationAddr.setText(ConnectStationAddrStr);
					if (nowtap != 0) {
						StationShutdown = true;
						viewPager.setCurrentItem(0);
					} else
						StationSwitch.setChecked(false);
					LatitudeText.setText("00.00000000");
					LongtitudeText.setText("000.00000000");
					GPSTimeText.setText("000000");
					LocateStatusText.setText("No Data");
					break;
				case 3:
					ConnectStationStatusStr = "主站連線中";
					StationConnectStatusColor = Color.BLUE;
					stationConnectStatue.setTextColor(StationConnectStatusColor);
					stationConnectStatue.setText(ConnectStationStatusStr);
					break;
				case 4:
					btConnectStatusStr = "已連線";
					btConnectStatusColor = Color.BLUE;
					btconnectstatue.setText(btConnectStatusStr);
					btconnectstatue.setTextColor(btConnectStatusColor);

					btScanConnectStatusStr = "已連線";
					btConnectScanStatusColor = Color.BLUE;
					btScanConnectStatus.setText(btScanConnectStatusStr);
					btScanConnectStatus.setTextColor(btConnectScanStatusColor);

					btMacConnectStatusStr = "已連線";
					btConnectMacStatusColor = Color.BLUE;
					btMacConnectStatus.setText(btMacConnectStatusStr);
					btMacConnectStatus.setTextColor(btConnectMacStatusColor);

					String tempStr;
					try {
						tempStr = devNameAddress.split("\\s+")[0];
					} catch (ArrayIndexOutOfBoundsException e) {
						tempStr = devNameAddress;
					}
					btConnectDeviceStr = tempStr + "|" + devAddress;
					btConnectDevice.setText(btConnectDeviceStr);
					editDialog.cancel();
					StationSwitch.setEnabled(true);
					BuiltInGpsSwitch.setEnabled(false);
					resetNmeaMsg();
					break;
				case 5:
					btConnectStatusStr = "連線失敗";
					btConnectStatusColor = Color.RED;
					btconnectstatue.setText(btConnectStatusStr);
					btconnectstatue.setTextColor(btConnectStatusColor);

					btScanConnectStatusStr = "連線失敗";
					btConnectScanStatusColor = Color.RED;
					btScanConnectStatus.setText(btScanConnectStatusStr);
					btScanConnectStatus.setTextColor(btConnectScanStatusColor);

					btMacConnectStatusStr = "連線失敗";
					btConnectMacStatusColor = Color.RED;
					btMacConnectStatus.setText(btScanConnectStatusStr);
					btMacConnectStatus.setTextColor(btConnectScanStatusColor);

					isStartBTConnect = false;

					if (nowtap != 0) {
						BTShutdown = true;
						viewPager.setCurrentItem(0);
					} else
						BTSwitch.setChecked(false);
					break;
			}
		}

	};


	private class SppReceiver extends TimerTask {

		private final InputStream input;
		//先將BTSocket刪掉
//		private final BluetoothSocket mBluetoothSocket;

//		public SppReceiver(BluetoothSocket socketIn) {
//
//			recordtime = 172800;
//			PosRecordTime = 172800;
//			mBluetoothSocket = socketIn;
//			InputStream tmpIn = null;
//			try {
//				tmpIn = socketIn.getInputStream();
//
//			} catch (IOException e) {
//				// TODO: handle exception
//				Log.i(TAG, "SppReceiver : tmpIn is empty");
//			}
//			input = tmpIn;
//		}

		//將建構子SppReceiver(BT)改成SppReceiver(Socket SSocket)
		public SppReceiver(Socket SSocket) {

			recordtime = 172800;
			PosRecordTime = 172800;
			InputStream tmpIn = null;
			try {
				tmpIn = SSocket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
				Log.i(TAG, "SppReceiver : tmpIn is empty");
			}

			input = tmpIn;
		}

		@Override
		public void run() {

			datafilter m_datafilter = new datafilter();

			if (input == null) {
				Log.d(TAG, "-- SppReceiver : InputStream NULL");
				return;
			}
			if (true) {

				byte[] buffer = new byte[4096];
				int read;
				try {
					read = input.read(buffer);
					Log.i(TAG, "READDDD!!!!!!!!BYTES:" + String.valueOf(read));
					if (read != -1) {
						byte[] tempdata = new byte[read];
						System.arraycopy(buffer, 0, tempdata, 0, read);
						checkOpenNewFile();
						writeToSDcard(tempdata);
						recordtime++;
						btReceiveByte += (double) read / 1000;

						String decodeds = new String(tempdata);
						NMEAComplete = NMEASubstring(decodeds);
						if (NMEAComplete) {
							isContinueCollectNMEA = false;
							isNMEAEnd = false;
							if (!isconnectserver) {
								BTOutputStream.write(data.getBytes());
								btSendByte += (double) data.getBytes().length / 1000;
								msg += "\n" + data + "\n";
							}
							m_datafilter.filter(data);
							//lat=m_datafilter.getlat();
							//lon=m_datafilter.getlon();
							SatData = m_datafilter.getsatdata();
							Log.e("debug", "update skyplot"+SatData.size());
						}

						if (isconnectserver) {
							if (m_SendData2Server != null) {
								while (!SendData2ServerComplete) {
									try {
										Thread.sleep(10);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								SendData2ServerComplete = false;
								if(m_SendData2Server!=null)
								{
									if (!m_SendData2Server.isInterrupted())
										m_SendData2Server.interrupt();
								}
								m_SendData2Server = null;
							}
							m_SendData2Server = new SendData2Server();
							Log.e(TAG, "Server BT " + tempdata);
							m_SendData2Server.tempmsg = tempdata;
							m_SendData2Server.size = read;
							m_SendData2Server.start();
						}
						mBluetoothHandler.sendEmptyMessage(1);
					}
				} catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
					Log.e(TAG, "SppReceiver :　read Data FAILED , SppReveiver disconnect!");
					connectionFailed(5);
				}
			}
		}
	}

	private void checkOpenNewFile()
	{
		if(recordtime>=172800)

		{
			if (bufFileOut != null) {
				try {
					bufFileOut.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			bufFileOut = null;
			String BTFileName = "Binary-" + getBTFileName() + ".txt";
			String path = Environment.getExternalStorageDirectory().getPath();    //建立自己的目錄
			File dir = new File(path + "/GPSData");
			if (!dir.exists()) {
				dir.mkdir();
			}
			File file = new File(path + "/GPSData/" + BTFileName);
			FileOutputStream fout;
			try {

				fout = new FileOutputStream(file, true);
				bufFileOut = new BufferedOutputStream(fout);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			msg += "\n" + "Data storage Start!! filename:" + BTFileName + "\n\n";
			recordtime = 0;
		}
	}
	/**
	 * Handler: 將訊息顯示在TextView中，並將捲軸捲至最底
	 */
	Handler mBluetoothHandler = new Handler() {

		public void handleMessage(Message message) {
			switch (nowtap) {
				case 0:
					if (message.what == 1) {
						btRSbyteStr = String.format("%.3f KB / %.3f KB", btReceiveByte, btSendByte);
						btRSbyte.setText(btRSbyteStr);
						btReceiveByte = 0.000;
						btSendByte = 0.000;
						if (isconnectserver) {
							ConnectStationRSbyteStr = String.format("%.3f KB / %.3f KB", ConnetStationReceiveByte, ConnetStationSendByte);
							ConnectStationRSbyte.setText(ConnectStationRSbyteStr);
							ConnetStationReceiveByte = 0.000;
							ConnetStationSendByte = 0.000;
						}
					}
					break;
				case 3:
					setcolumnchartData();
					if (llo != null) {
						llo.removeViewAt(0);
						llo.addView(chart);
					}
					final DrawView view = new DrawView(MainActivity.this,
							satloc.getWidth(), satloc.getHeight(), SatData);
					view.invalidate();
					if (satloc != null) {
						satloc.removeAllViews();
						satloc.addView(view);
					}
					break;
			}
			if (recordtime % 2 == 0)
				recordTime.setText("Record Time: " + String.valueOf(recordtime / 2 / 3600) + ":" + String.valueOf(recordtime / 2 / 60 % 60) + ":" + String.valueOf(recordtime / 2 % 60));
			if (msg != null) {
				if (msgCount > 60) {
					resetNmeaMsg();
				}
				msgText.setText(msg);
				msgCount++;
			}

			if (AutoScroll.isChecked())
				mscrollView.fullScroll(ScrollView.FOCUS_DOWN); // 卷軸自動捲至最底
			Log.i(TAG, "mbluetooth handler complete");
		}
	};

	private void resetNmeaMsg()
	{
		msgText.setText(null);
		msgCount = 0;
		msg = "";
	}

	private Handler charthandler = new Handler() {
		public void handleMessage(Message message) {
			int tempx=0;
			int tempy=0;
			int status = Integer.parseInt(LocateStatus);
			switch (status) {
				case 2://RTK floating
					LocateStatusText.setText("Floating");
					fromFloatingToKinematic = true;
					break;
				case 3://RTK Kinematic
					LocateStatusText.setText("Kinematic");
					if (fromFloatingToKinematic) {
						resetLineChart();
						fromFloatingToKinematic = false;
					}
					break;
				case 4://GPS Invalid
					LocateStatusText.setText("Invalid");
					GPSInvalidToFix=true;
					break;
				case 5://GPS fix
					LocateStatusText.setText("GPS fix");
					if (GPSInvalidToFix) {
						resetLineChart();
						GPSInvalidToFix = false;
					}
					break;
				case 6://DGPS fix
					LocateStatusText.setText("DGPS fix");
					if (GPSInvalidToFix) {
						resetLineChart();
						GPSInvalidToFix = false;
					}
					break;
			}
			GPSTimeText.setText(GPSTime);
			if (rBtn_TWD97.isChecked()) {
				GeoCoor tempGeo = new GeoCoor(Double.parseDouble(Longitude), Double.parseDouble(Latitude), 0);
				GausCoor tempGaus = new GausCoor();
				BLtoXY(121, tempGeo, tempGaus);
				String E = String.valueOf(tempGaus.getX()), N = String.valueOf(tempGaus.getY());
				LongtitudeText.setText(E.substring(0, 11));
				LatitudeText.setText(N.substring(0, 12));
				LongtitudeHead.setText(E.substring(0, 4));
				LatitudeHead.setText(N.substring(0, 4));
			} else if (rBtn_WGS84.isChecked()) {
				LongtitudeText.setText(Longitude);
				LatitudeText.setText(Latitude);
				LongtitudeHead.setText(Longitude.substring(0, 7));
				LatitudeHead.setText(Latitude.substring(0, 5));
			}
			String displayX=Longitude.split("\\.")[1];
			int displayXLength=displayX.length();
			String displayY=Latitude.split("\\.")[1];
			int displayYLength=displayY.length();
			if(displayXLength>9)
				displayX=displayX.substring(0,9);
			else if(displayXLength<9) {
				for(int i=0;i<9-displayXLength;i++)
					displayX+="0";
			}
			if(displayYLength>9)
				displayY=displayY.substring(0,9);
			else if(displayYLength<9) {
				for(int i=0;i<9-displayYLength;i++)
					displayY+="0";
			}
			tempx = Integer.parseInt(displayX) % 10000000;
			tempy = Integer.parseInt(displayY) % 10000000;
			if (firstplot) {
				firstplot = false;
				tempTop = tempy + 5;
				tempBottom = tempy - 5;
				tempRight = tempx + 5;
				tempLeft = tempx - 5;
				setViewport();
			} else if (AutoScale.isChecked()) {
				if (tempx > viewRight) {
					tempRight = tempx + 5;
					setViewport();
				} else if (tempx < viewLeft) {
					tempLeft = tempx - 5;
					setViewport();
				} else if (tempx > tempRight)
					tempRight = tempx + 5;
				else if (tempx < tempLeft)
					tempLeft = tempx - 5;

				if (tempy > viewTop) {
					tempTop = tempy + 5;
					setViewport();
				} else if (tempy < viewBottom) {
					tempBottom = tempy - 5;
					setViewport();
				} else if (tempy > tempTop)
					tempTop = tempy + 5;
				else if (tempy < tempBottom)
					tempBottom = tempy - 5;
			}
			values.add(new PointValue((float) tempx, (float) tempy));
			lineChart.startDataAnimation();
			PointCount.setText(String.valueOf(pointCount));
			if (pointCount >= 1800)
				values.remove(0);
			else
				pointCount++;
			Log.i(TAG, "chart handler complete");
		}
	};
	/**
	 * Handler: 顯示Toast，表示完成解碼
	 */
	Handler rtcmDecodeHandler =new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what){
				case 1:
					Toast finishDecodeToast = Toast.makeText(MainActivity.this,"完成RTCM解碼",Toast.LENGTH_SHORT);
					finishDecodeToast.show();
					break;
			}

		}
	};


	public void connectionFailed(int type) {
		// TODO Auto-generated method stub
		if(BTSwitch.isChecked()) {
			Log.d("TAG", "++ connectionFailed()");
			SppConnecthandler.sendEmptyMessage(type);
			sppConnected = false;
			if (sppConnect != null) {
				sppConnect.cancel();
				sppConnect = null;
			}
			if (timer != null) {
				timer.cancel();

				timer = null;
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (isBTBdRegister) {
			this.unregisterReceiver(BluetoothBroadcastReveiver);
			isBTBdRegister = false;
		}
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder CheckLeaveDialog = new AlertDialog.Builder(MainActivity.this); //創建訊息方塊

			CheckLeaveDialog.setMessage("確定要離開程式?");

			CheckLeaveDialog.setPositiveButton("是", new DialogInterface.OnClickListener() { //按"是",則退出應用程式

				public void onClick(DialogInterface dialog, int i) {
					Process.killProcess(Process.myPid());
					sppConnected = false;
					if (isBTBdRegister) {
						getApplicationContext().unregisterReceiver(BluetoothBroadcastReveiver);
						isBTBdRegister = false;
					}
					finish();
				}

			});

			CheckLeaveDialog.setNeutralButton("讓程式在背景執行", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int i) {
					moveTaskToBack(true);
				}

			});
			CheckLeaveDialog.setNegativeButton("否", new DialogInterface.OnClickListener() { //按"否",則不執行任何操作
				public void onClick(DialogInterface dialog, int i) {
				}
			});

			CheckLeaveDialog.show();//顯示訊息視窗
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public class HomeViewPagerAdapter extends FragmentPagerAdapter {
		private String[] mFragmentTitleList;

		public HomeViewPagerAdapter(FragmentManager fm) {

			super(fm);
			mFragmentTitleList = getResources().getStringArray(R.array.pager_array);
		}

		public Fragment getItem(int iPosition) {


			switch (iPosition) {

				case 0:
					return new MyFirstFragment(0);
				case 1:

					return new MyFirstFragment(1);
				case 2:

					return new MyFirstFragment(2);
				case 3:

					return new MyFirstFragment(3);

			}

			return null;

		}

		public int getCount() {

			return 4;

		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mFragmentTitleList[position];
		}
	}

	public class MyFirstFragment extends Fragment {
		int num;
		View wv = null;

		MyFirstFragment(int a) {
			num = a;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View v = null;
			switch (num) {

				case 0:
					v = inflater.inflate(R.layout.setting, container, false);
					BTSwitch = (Switch) v.findViewById(R.id.switch_BTConnect);
					btconnectstatue = (TextView) v.findViewById(R.id.btconnectstatue);
					btConnectDevice = (TextView) v.findViewById(R.id.txt_btConnectDevice);
					btRSbyte = (TextView) v.findViewById(R.id.txt_btRSbyte);

					btRSbyte.setText(btRSbyteStr);
					btConnectDevice.setText(btConnectDeviceStr);
					btconnectstatue.setText(btConnectStatusStr);
					btconnectstatue.setTextColor(btConnectStatusColor);
					BTSwitch.setEnabled(!built_in_gps_connect);
					BTSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if (isChecked && !sppConnected && !BTShutdown) {
								LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
								View tempView = inflater.inflate(R.layout.bt_connect_dialog, null);
								BTDialogTab = (TabLayout) tempView.findViewById(R.id.bt_pager_tabs);
								BTDialogViewPager = (ViewPager) tempView.findViewById(R.id.bt_pager);
								if (BTDialogViewPager != null) {
									BTDialogViewPager.setAdapter(new BT_HomeViewPagerAdapter(MainActivity.this));
									BTDialogViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(BTDialogTab));
									BTDialogTab.setupWithViewPager(BTDialogViewPager);
								}
								editDialog = new AlertDialog.Builder(MainActivity.this).create();
								editDialog.setTitle("藍芽連線");
								editDialog.setView(tempView);
								editDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
									@Override
									public void onDismiss(DialogInterface dialog) {
										if (!isStartBTConnect)
											BTSwitch.setChecked(false);
									}
								});
								editDialog.show();
							}
							else if (!isChecked&&sppConnected) {
								sppConnected=false;
								spinner1.setEnabled(true);
								BuiltInGpsSwitch.setEnabled(true);
								StationSwitch.setChecked(false);
								StationSwitch.setEnabled(false);
								isStartBTConnect = false;
								if (m_connectserver != null) {
									m_connectserver.cancel();
									m_connectserver.interrupt();
									m_connectserver = null;
								}

								if (!Objects.equals(btScanConnectStatusStr, "連線失敗")) {
									btConnectStatusStr = "未連線";
									btConnectStatusColor = Color.RED;
									btconnectstatue.setText(btConnectStatusStr);
									btconnectstatue.setTextColor(btConnectStatusColor);
								}
								btConnectDeviceStr = "Device|Mac Address";
								btConnectDevice.setText(btConnectDeviceStr);
								btRSbyteStr = "0.000 KB / 0.000 KB";
								btRSbyte.setText(btRSbyteStr);

								btScanConnectStatusStr = "未連線";
								btConnectScanStatusColor = Color.RED;
								btScanConnectStatus.setText(btScanConnectStatusStr);
								btScanConnectStatus.setTextColor(btConnectScanStatusColor);

								btMacConnectStatusStr = "未連線";
								btConnectMacStatusColor = Color.RED;
								btMacConnectStatus.setText(btMacConnectStatusStr);
								btMacConnectStatus.setTextColor(btConnectMacStatusColor);

								BTScanConnect.setEnabled(false);
								devices.clear();
								adapter1.notifyDataSetChanged(); // 通知adapter1 devices有更新

								ConnectStationStatusStr = "未連線至主站";
								StationConnectStatusColor = Color.RED;
								stationConnectStatue.setText(ConnectStationStatusStr);
								stationConnectStatue.setTextColor(StationConnectStatusColor);

								if (sppConnect != null) {
									sppConnect.cancel();
									sppConnect = null;
								}
								if (timer != null) {
									timer.cancel();
									timer = null;
								}
								if (isBTBdRegister) {
									unregisterReceiver(BluetoothBroadcastReveiver);
									isBTBdRegister = false;
								}
							}
							else if (isChecked && !sppConnected && BTShutdown) {
								BTShutdown = false;
								BTSwitch.setChecked(false);
							}
						}
					});


					StationSwitch = (Switch) v.findViewById(R.id.switch_StationConnect);
					stationConnectStatue = (TextView) v.findViewById(R.id.txt_stationConnectStatue);
					ConnectStationAddr = (TextView) v.findViewById(R.id.txt_ConnectStationAddr);
					ConnectStationRSbyte = (TextView) v.findViewById(R.id.txt_ConnectStationRSbyte);

					//StationSwitch.setEnabled(Sppconnected);
					//改成true
					StationSwitch.setEnabled(true);
					ConnectStationRSbyte.setText(ConnectStationRSbyteStr);
					ConnectStationAddr.setText(ConnectStationAddrStr);
					stationConnectStatue.setText(ConnectStationStatusStr);
					stationConnectStatue.setTextColor(StationConnectStatusColor);


					StationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if (StationSwitch.isChecked() && !isconnectserver && !StationShutdown) {
								SharedPreferences set = getSharedPreferences("StationSetting", 0);
								View v2 = null;
								LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
								v2 = inflater.inflate(R.layout.station, null);

								ip_et = (EditText) v2.findViewById(R.id.ip_et);
								port_et = (EditText) v2.findViewById(R.id.port_et);
								StationConnect = (Button) v2.findViewById(R.id.btn_StationConnect);
								StationCancel = (Button) v2.findViewById(R.id.btn_StationCancel);
								ip_et.setText(set.getString("ip", ""));
								port_et.setText(set.getString("port", ""));

								StationConnect.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										isStartStationConnect = true;
										SharedPreferences set = getSharedPreferences(
												"StationSetting", 0);
										set.edit()
												.putString("ip",
														ip_et.getText().toString())
												.putString("port",
														port_et.getText().toString())
												.apply();
										m_connectserver = new ConnectServer();
										m_connectserver.start();
										Log.e("debug", "m_connectserver.start");
										ConnectStationDialog.cancel();
									}
								});

								StationCancel.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										isconnectserver = false;
										ConnectStationDialog.cancel();
									}
								});

								ConnectStationDialog = new AlertDialog.Builder(MainActivity.this).create();
								ConnectStationDialog.setTitle("伺服器連線");
								ConnectStationDialog.setView(v2);
								ConnectStationDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
									@Override
									public void onDismiss(DialogInterface dialog) {
										if (!isStartStationConnect)
											StationSwitch.setChecked(false);
									}
								});
								ConnectStationDialog.show();
							} else if (!StationSwitch.isChecked() && isconnectserver) {
								isStartStationConnect = false;
								m_connectserver.cancel();
								m_connectserver.interrupt();
								m_connectserver = null;
								StationConnectStatusColor = Color.RED;
								stationConnectStatue.setTextColor(StationConnectStatusColor);
								ConnectStationStatusStr = "未連線至主站";
								stationConnectStatue.setText(ConnectStationStatusStr);
								ConnectStationAddrStr = "IP:Port";
								ConnectStationRSbyteStr = "0.000 KB / 0.000 KB";
								ConnectStationRSbyte.setText(ConnectStationRSbyteStr);
								ConnectStationAddr.setText(ConnectStationAddrStr);
							} else if (StationSwitch.isChecked() && !isconnectserver && StationShutdown) {
								StationShutdown = false;
								StationSwitch.setChecked(false);
							}
						}
					});

					BuiltInGpsSwitch = (Switch) v.findViewById(R.id.switch_buildinGPS);
					BuildInGPSStatus = (TextView) v.findViewById(R.id.txt_BuildinGPSStatus);

					BuildInGPSStatus.setText(BuildinGPSStatusStr);
					BuildInGPSStatus.setTextColor(BuildinGPSStatusColor);
					BuiltInGpsSwitch.setEnabled(!sppConnected);
					BuiltInGpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

							if (isChecked && !built_in_gps_connect) {
								//provider = LocationManager.GPS_PROVIDER;
								if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
									return;
								}

								locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
								if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
									new AlertDialog.Builder(MainActivity.this).setTitle("地圖工具").setMessage("您尚未開啟定位服務，要前往設定頁面啟動定位服務嗎？")
											.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
										}
									}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											Toast.makeText(MainActivity.this, "未開啟定位服務，無法使用本工具!!", Toast.LENGTH_SHORT).show();
											BuiltInGpsSwitch.setChecked(false);
											return;
										}
									}).show();
								}
								locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
								locationManager.addNmeaListener(NmeaListener);
								built_in_gps_connect=true;
								resetLineChart();
								resetNmeaMsg();

								BuildinGPSStatusStr="已開啟";
								BuildinGPSStatusColor=Color.BLUE;
								BuildInGPSStatus.setText(BuildinGPSStatusStr);
								BuildInGPSStatus.setTextColor(BuildinGPSStatusColor);
								BTSwitch.setEnabled(false);
							}
							else if(!isChecked && built_in_gps_connect)
							{
								locationManager.removeNmeaListener(NmeaListener);
								locationManager.removeUpdates(locationListener);
								built_in_gps_connect=false;

								BuildinGPSStatusStr="未開啟";
								BuildinGPSStatusColor=Color.RED;
								BuildInGPSStatus.setText(BuildinGPSStatusStr);
								BuildInGPSStatus.setTextColor(BuildinGPSStatusColor);
								BTSwitch.setEnabled(true);
							}

						}

					});

					RtcmDecodeButton = (Button)v.findViewById(R.id.btn_rtcmdecode);
					RtcmDecodeButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							progressDialod = ProgressDialog.show(MainActivity.this,"解碼中","請稍候...",true);
							Thread thread = new Thread(){
							@Override
								public void run(){
								Message msg = new Message();

								stringFromJNI();

								msg.what = 1;
								rtcmDecodeHandler.sendMessage(msg);
								progressDialod.dismiss();
							}
							};
							thread.start();

						}
					});
					break;
			case 1:

				v = inflater.inflate(R.layout.fragmentab1, container, false);
				mscrollView = (ScrollView) v.findViewById(R.id.mscrollView);
				msgText = (TextView) v.findViewById(R.id.msgText);
				Btnclear = (ImageView) v.findViewById(R.id.btnclear);
				BtnSave = (ToggleButton) v.findViewById(R.id.Btn_Save);
				AutoScroll =(CheckBox) v.findViewById(R.id.cbox_autoscroll);
				recordTime=(TextView)v.findViewById(R.id.txt_recordtime);
				msgText.setText(msg);

				break;

			case 2:
				v = inflater.inflate(R.layout.fragment3_1, container, false);
				lineChart=(LineChartView)v.findViewById(R.id.chartLayout);
				lineChart.setOnValueTouchListener(new ValueTouchListener());
				lineChart.setViewportCalculationEnabled(false);
				LatitudeText=(TextView)v.findViewById(R.id.txt_latitude);
				LongtitudeText=(TextView)v.findViewById(R.id.txt_longtitude);
				LongtitudeHead=(TextView)v.findViewById(R.id.txt_longtitudeHead);
				LocateStatusText=(TextView)v.findViewById(R.id.txt_locateStatus);
				LatitudeHead=(TextView)v.findViewById(R.id.txt_latitudeHead);
				GPSTimeText=(TextView)v.findViewById(R.id.txt_GPStime);
				PointCount=(TextView)v.findViewById(R.id.txt_pointcnt);
				AutoScale=(CheckBox)v.findViewById(R.id.cbox_autoscale);
				BtnDrawMap = (ImageView) v.findViewById(R.id.drawLine);
				BtnClearPlot=(ImageView)v.findViewById(R.id.btn_ClearPlot);
				rBtn_WGS84=(RadioButton)v.findViewById(R.id.rbtn_wgs84);
				rBtn_TWD97=(RadioButton)v.findViewById(R.id.rbtn_twd97);
				LabelAxisX=(TextView)v.findViewById(R.id.txt_LabelAxisX);
				LabelAxisY=(TextView)v.findViewById(R.id.txt_LabelAxisY);
				rGroup_Cordinate=(RadioGroup)v.findViewById(R.id.radioGroup_Cordinate);
				PointCount.setText(String.valueOf(pointCount));

				rGroup_Cordinate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						List<AxisValue> axisXs = new ArrayList<>();
						List<AxisValue> axisYs = new ArrayList<>();
						switch(checkedId) {
							case R.id.rbtn_wgs84:
									if(isconnectserver) {
										LongtitudeText.setText(Longitude);
										LatitudeText.setText(Latitude);
									}
									LabelAxisX.setText("經度");
									LabelAxisY.setText("緯度");
									axisX.setName("Longtitude");
									axisY.setName("Latitude");
									LongtitudeHead.setText(Longitude.substring(0, 7));
									LatitudeHead.setText(Latitude.substring(0, 5));
									for (int i = viewLeft; i <= viewRight; i += scale) {
										axisXs.add(new AxisValue(i).setLabel(String.format(Locale.TAIWAN,"%06d", i % 1000000)));
									}
									for (int i = viewBottom; i <= viewTop; i += scale) {
										axisYs.add(new AxisValue(i).setLabel(String.format(Locale.TAIWAN,"%07d", i)));
									}
									break;
							case R.id.rbtn_twd97:
									LabelAxisX.setText("E");
									LabelAxisY.setText("N");
									axisX.setName("E");
									axisY.setName("N");
									GeoCoor tempGeo=new GeoCoor(Double.parseDouble(Longitude),Double.parseDouble(Latitude),0);
									GausCoor tempGaus=new GausCoor();
									BLtoXY(121,tempGeo,tempGaus);
									String E=String.valueOf(tempGaus.getX()),N=String.valueOf(tempGaus.getY());
									if(isconnectserver)
									{
										LongtitudeText.setText(E.substring(0,11));
										LatitudeText.setText(N.substring(0,12));
									}
									if(N.length()>5) {
										LongtitudeHead.setText(E.substring(0, 4));
										LatitudeHead.setText(N.substring(0, 4));
									}
									double LongtitudeHeadDouble=Double.parseDouble(Longitude.substring(0, 6));
									double LatitudeHeadDouble=Double.parseDouble(Latitude.substring(0, 5));
									for (int i = 0; i < 5; i++) {
										GeoCoor tempGeo1 = new GeoCoor(LongtitudeHeadDouble + (viewLeft + i * scale) * 1E-9, LatitudeHeadDouble + (viewBottom + i * scale) * 1E-9, 0);
										GausCoor tempGaus1 = new GausCoor();
										BLtoXY(121, tempGeo1, tempGaus1);
										axisXs.add(new AxisValue(viewLeft + i * scale).setLabel(String.valueOf(tempGaus1.getX()).substring(4, 11)));
										axisYs.add(new AxisValue(viewBottom + i * scale).setLabel(String.valueOf(tempGaus1.getY()).substring(4, 12)));
									}
									break;
						}
						axisX.setValues(axisXs);
						axisY.setValues(axisYs);
						final Viewport v = new Viewport(lineChart.getMaximumViewport());
						v.bottom = viewBottom;
						v.top = viewTop;
						v.left = viewLeft;
						v.right = viewRight;
						lineChart.resetViewports();
						lineChart.setMaximumViewport(v);
						lineChart.setCurrentViewport(v);
					}
				});
				BtnClearPlot.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						resetLineChart();
					}
				});
				LineChartInit();
				setViewport();
				if(initial)
				{
					viewPager.setCurrentItem(0);
					initial=false;
				}
				break;

				case 3://Skyplot
					v = inflater.inflate(R.layout.fragmenttab4, container, false);
					satloc = (LinearLayout) v.findViewById(R.id.satloc);
					llo = (LinearLayout) v.findViewById(R.id.snrlo);
					chart = new ColumnChartView(getApplicationContext());
					setcolumnchartData();
					llo.addView(chart);
					break;
			}
			return v;

		}
	}


	public class BT_HomeViewPagerAdapter extends PagerAdapter {
		private String[] mFragmentTitleList;
		private Context mContext;

		public BT_HomeViewPagerAdapter(Context context) {
			mContext = context;
			mFragmentTitleList=getResources().getStringArray(R.array.BT_Connect_Mode);
		}

		@Override
		public Object instantiateItem(ViewGroup collection, int position) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			ViewGroup v = null;
			switch (position) {
				case 0:
					v = (ViewGroup)inflater.inflate(R.layout.bt_connect_by_scan, collection, false);
					BTScan=(Button)v.findViewById(R.id.scan);
					BTScanConnect=(Button)v.findViewById(R.id.ScanConnect);
					btScanConnectStatus=(TextView)v.findViewById(R.id.txt_dialogScanConnectStatus);
					spinner1 = (Spinner) v.findViewById(R.id.ScanDeviceSpinner);
					btScanConnectStatus.setText(btScanConnectStatusStr);
					btScanConnectStatus.setTextColor(btConnectScanStatusColor);
					adapter1 = new SimpleAdapter(MainActivity.this, devices, R.layout.spinner_list_item_2,
							new String[]{"BTName", "BTMac"},
							new int[]{android.R.id.text1,
									android.R.id.text2});

					spinner1.setAdapter(adapter1);
					spinner1.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> parent,
												   View view, int position, long id) {
							BTScanConnect.setEnabled(true);
							btScanConnectStatusStr="已搜尋到藍芽裝置";
							btConnectScanStatusColor=Color.BLUE;
							btScanConnectStatus.setText(btScanConnectStatusStr);
							btScanConnectStatus.setTextColor(btConnectScanStatusColor);

							devNameAddress = devices.get(position).get("BTName");
							devAddress = devices.get(position).get("BTMac");
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) {
							// TODO Auto-generated method stub
						}
					});

					BTScan.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							BTScanConnect.setEnabled(false);
							devices.clear();
							adapter1.notifyDataSetChanged(); // 通知adapter1 devices有更新
							registerReceiver(BluetoothBroadcastReveiver, intentfilter);
							isBTBdRegister=true;
							mBluetoothAdapter.cancelDiscovery(); // 搜尋裝置前先確認藍芽裝置不是處於搜尋中
							mBluetoothAdapter.startDiscovery();
							btScanConnectStatusStr="搜尋中";
							btConnectScanStatusColor=Color.BLUE;
							btScanConnectStatus.setText(btScanConnectStatusStr);
							btScanConnectStatus.setTextColor(btConnectScanStatusColor);
						}
					});

					BTScanConnect.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							btScanConnectStatusStr="連線中";
							btConnectStatusStr="連線中";
							btConnectScanStatusColor=Color.BLUE;
							btConnectStatusColor=Color.BLUE;
							btScanConnectStatus.setText(btScanConnectStatusStr);
							btScanConnectStatus.setTextColor(btConnectScanStatusColor);
							btconnectstatue.setText(btConnectStatusStr);
							btconnectstatue.setTextColor(btConnectStatusColor);
							spinner1.setEnabled(false);
							BuiltInGpsSwitch.setEnabled(false);

							if (sppConnected || devAddress == null) {
								Log.d(TAG, "NULL sppConnected = " + sppConnected
										+ " ;devAddress= " + devAddress);
								return;
							}
							if (sppConnect != null) {
								sppConnect.cancel();
								sppConnect = null;
							}
							if (timer != null) {
								timer.cancel();
								timer = null;
							}
							isStartBTConnect=true;
							sppConnect = new SppConnect();
							sppConnect.start();
						}
					});
					break;
				case 1:
					v = (ViewGroup)inflater.inflate(R.layout.btconnectdialog, collection, false);
					BTmacAddr = getSharedPreferences("BTmacAddr", 0);
					mac1 = (EditText) v.findViewById(R.id.etxt_mac1);
					mac2 = (EditText) v.findViewById(R.id.etxt_mac2);
					mac3 = (EditText) v.findViewById(R.id.etxt_mac3);
					mac4 = (EditText) v.findViewById(R.id.etxt_mac4);
					mac5 = (EditText) v.findViewById(R.id.etxt_mac5);
					mac6 = (EditText) v.findViewById(R.id.etxt_mac6);
					btMacConnectStatus=(TextView) v.findViewById(R.id.txt_btMacConnectStatus);
					btMacConnect=(Button) v.findViewById(R.id.btn_btMacConnect);

					btMacConnectStatus.setText(btMacConnectStatusStr);
					btMacConnectStatus.setTextColor(btConnectMacStatusColor);

					if(!Objects.equals(BTmacAddr.getString("btmac", ""), "")) {
						String[] btMacArr = BTmacAddr.getString("btmac", "").split(":");
						mac1.setText(btMacArr[0]);
						mac2.setText(btMacArr[1]);
						mac3.setText(btMacArr[2]);
						mac4.setText(btMacArr[3]);
						mac5.setText(btMacArr[4]);
						mac6.setText(btMacArr[5]);
					}
					btMacConnect.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (mac1.getText().length() == 2 && mac2.getText().length() == 2 && mac3.getText().length() == 2 && mac4.getText().length() == 2 && mac5.getText().length() == 2 && mac6.getText().length() == 2)
							{
								BTmacAddr.edit()
										.putString("btmac",
												mac1.getText().toString().toUpperCase() + ":" + mac2.getText().toString().toUpperCase() + ":" + mac3.getText().toString().toUpperCase() + ":" + mac4.getText().toString().toUpperCase() + ":" + mac5.getText().toString().toUpperCase() + ":" + mac6.getText().toString().toUpperCase())
										.apply();
							}
							else
							{
								btMacConnectStatusStr="藍芽裝置Mac Address輸入不完全";
								btConnectMacStatusColor=Color.RED;
								btMacConnectStatus.setText(btMacConnectStatusStr);
								btMacConnectStatus.setTextColor(btConnectMacStatusColor);
								return;
							}

							devNameAddress="  ";
							devAddress = BTmacAddr.getString("btmac", "");
							registerReceiver(BluetoothBroadcastReveiver, intentfilter);
							isBTBdRegister=true;

							msgText.setText(null);
							btMacConnectStatusStr="連線中";
							btConnectStatusStr="連線中";
							btConnectMacStatusColor=Color.BLUE;
							btConnectStatusColor=Color.BLUE;
							btMacConnectStatus.setText(btMacConnectStatusStr);
							btMacConnectStatus.setTextColor(btConnectMacStatusColor);
							btconnectstatue.setText(btConnectStatusStr);
							btconnectstatue.setTextColor(btConnectStatusColor);


							if (sppConnect != null) {
								sppConnect.cancel();
								sppConnect = null;
							}
							if (timer != null) {
								timer.cancel();
								timer = null;
							}
							if (sppConnected || devAddress == null) {
								Log.d(TAG, "NULL sppConnected = " + sppConnected
										+ " ;devAddress= " + devAddress);
							} else {
								sppConnect = new SppConnect();
								sppConnect.start();
								isStartBTConnect=true;
							}
						}
					});
					break;
			}
			collection.addView(v);
			return v;


		}

		@Override
		public void destroyItem(ViewGroup collection, int position, Object view) {
			collection.removeView((View) view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}


		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mFragmentTitleList[position];
		}
	}



	private class ValueTouchListener implements LineChartOnValueSelectListener{

		@Override
		public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
			//Toast.makeText(MainActivity.this, "Selected: " + value, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onValueDeselected() {
			// TODO Auto-generated method stub

		}

	}

	private void setViewport() {
		// Reset viewport height range to (0,100)
		int xscale=(int)((tempTop-tempBottom)/4)+1;
		int yscale=(int)((tempRight-tempLeft)/4)+1;
		int centerY=(int)(tempTop+tempBottom)/2;
		int centerX=(int)(tempRight+tempLeft)/2;

		if(xscale>yscale)
			scale=xscale;
		else
			scale=yscale;
		//Log.i("plot","scale:"+String.valueOf(scale));
		viewRight=centerX+2*scale;
		viewLeft=centerX-2*scale;
		viewTop=centerY+2*scale;
		viewBottom=centerY-2*scale;
		final Viewport v = new Viewport(lineChart.getMaximumViewport());
		List<AxisValue> axisXs = new ArrayList<>();
		List<AxisValue> axisYs = new ArrayList<>();
		if(rBtn_TWD97.isChecked())
		{
			double LongtitudeHeadDouble=Double.parseDouble(Longitude.substring(0, 6));
			double LatitudeHeadDouble=Double.parseDouble(Latitude.substring(0, 5));

			for (int i = 0; i <5; i++) {
				GeoCoor tempGeo = new GeoCoor(LongtitudeHeadDouble + (viewLeft + i * scale) * 1E-9, LatitudeHeadDouble + (viewBottom + i * scale) * 1E-9, 0);
				GausCoor tempGaus = new GausCoor();
				BLtoXY(121, tempGeo, tempGaus);
				axisXs.add(new AxisValue(viewLeft + i * scale).setLabel(String.valueOf(tempGaus.getX()).substring(4,11)));
				axisYs.add(new AxisValue(viewBottom + i * scale).setLabel(String.valueOf(tempGaus.getY()).substring(4,12)));
			}
		}
		else if(rBtn_WGS84.isChecked()){
			for (int i = viewLeft; i <=viewRight; i+=scale)
			{
				axisXs.add(new AxisValue(i).setLabel(String.format(Locale.TAIWAN,"%06d", i % 1000000)));
			}
			for (int i =viewBottom; i <= viewTop; i+=scale)
			{
				axisYs.add(new AxisValue(i).setLabel(String.format(Locale.TAIWAN,"%07d", i)));
			}
		}
		v.bottom = viewBottom;
		v.top = viewTop;
		v.left = viewLeft;
		v.right = viewRight;

		axisX.setValues(axisXs);
		axisY.setValues(axisYs);

		lineChart.setMaximumViewport(v);
		lineChart.setCurrentViewport(v);

	}

	private void LineChartInit() {

		List<Line> lines = new ArrayList<Line>();

		Line line = new Line(values);
		line.setColor(ChartUtils.COLORS[0]);
		line.setShape(ValueShape.CIRCLE);
		line.setCubic(false);
		line.setFilled(false);
		line.setHasLabels(false);
		line.setHasLabelsOnlyForSelected(false);
		line.setHasLines(true);
		line.setHasPoints(true);
		line.setPointColor(ChartUtils.COLORS[4]);
		lines.add(line);

		lineChartdata = new LineChartData(lines);

		axisX = new Axis().setHasLines(true);
		axisY = new Axis().setHasLines(true);

		axisX.setName("Longtitude");
		axisY.setName("Latitude");
		axisX.setTextSize(14);
		axisY.setTextSize(14);
		axisX.setTextColor(Color.parseColor("#000000"));
		axisY.setTextColor(Color.parseColor("#000000"));
		axisX.setLineColor(Color.parseColor("#000000"));
		axisY.setLineColor(Color.parseColor("#000000"));
		axisY.setMaxLabelChars(8);

		lineChartdata.setAxisXBottom(axisX);
		lineChartdata.setAxisYLeft(axisY);
		lineChartdata.setBaseValue(Float.POSITIVE_INFINITY);
		lineChart.setZoomEnabled(true);
		lineChart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
		lineChart.setScrollEnabled(true);
		lineChart.setLineChartData(lineChartdata);
	}

	private void resetLineChart() {
		firstplot=true;
		pointCount=0;
		PointCount.setText("0");
		values.clear();
		lineChart.startDataAnimation();
	}
	public boolean NMEASubstring(String allData) {
		p1 = 0;
		p2 = 0;

		if(isContinueCollectNMEA) {
			p2 = allData.indexOf("*", p1);
			if (p2 < allData.length())
			{
				data += allData.substring(0, p2 + 3) + "\n";
				if (isNMEAEnd)
				{
					return true;
				}
			}
		}
		p1 = allData.indexOf("$GNRMC", p2);

		if(p1>0) {
			p2 = allData.indexOf("*", p1);
			if (p1 > 0 && p2 > p1)
			{
				p2 += 3;
				if (p2 < allData.length()) {
					data = null;
					data = allData.substring(p1, p2) + "\n";
				} else {
					data = null;
					data = allData.substring(p1, allData.length() - 1);
					isContinueCollectNMEA=true;
					return false;
				}
			}

			 else {
				return false;
			}
		}

		GetNMEA("$GNGGA", allData);
		GetNMEA("$GNGSA", allData);
		GetNMEA("$GPGSV", allData);
		GetNMEA("$GPGSV", allData);
		GetNMEA("$GPGSV", allData);
		GetNMEA("$GPGSV", allData);
		GetNMEA("$GPGSV", allData);

		p1 = allData.indexOf("$GNGLL", p2);
		if (p1 > 0) {
			p2 = allData.indexOf("*", p1);
			if (p1 > 0 && p2 > p1) {
				p2 += 3;
				if (p2 < allData.length()) {
					data += allData.substring(p1, p2)+"\n";
					return true;
				} else {
					data += allData.substring(p1, allData.length() - 1);
					isNMEAEnd=true;
					isContinueCollectNMEA=true;
					return false;
				}
			} else {
				return false;
			}
		}
		return false;
	}

	public void GetNMEA(String startWord,String allData) {
		p1 = allData.indexOf(startWord, p2);
		if (p1 > 0) {
			p2 = allData.indexOf("*", p1);
			if (p1 > 0 && p2 > p1) {
				p2 += 3;
				if (p2 < allData.length()) {
					data += allData.substring(p1, p2) + "\n";
				} else {
					data += allData.substring(p1, allData.length() - 1);
					isContinueCollectNMEA=true;
				}
			}
		}
	}
	public void NMEAfilter(String lat,String lon) {
		pos1=0;
		pos1 = data.indexOf("GNRMC", pos1);
		pos2 = data.indexOf("*", pos1) + 3;

		if(pos1 > 0 && pos2 > pos1) {
			String temps = data.substring(pos1, pos2);

			String[] temps2 = temps.split(",");
			String checksum1 = temps2[temps2.length - 1].substring(temps2[temps2.length - 1].length() - 3, temps2[temps2.length - 1].length());
			if (temps2[3].equals("") || temps2[5].equals("")) {
				NMEAcheck = false;
			}
			if (NMEAcheck) {
				data = data.replaceAll(temps2[3], lat);
				data = data.replaceAll(temps2[5], lon);
				pos2 = data.indexOf("*", pos1) + 3;
				temps = data.substring(pos1, pos2);
				String newchecksum1 = "*" + CalCheckSum(temps);
				data = data.replace(checksum1, newchecksum1);



				pos1 = data.indexOf("GNGGA", pos1);
				pos2 = data.indexOf("*", pos1) + 3;
				if(pos1 > 0 && pos2 > pos1) {
					temps = data.substring(pos1, pos2);
					String[] GNGGA_par=temps.split(",");
					switch(Integer.parseInt(LocateStatus))
					{
						case 2:
							data = data.replaceFirst(","+GNGGA_par[6]+",",",5,");
							break;
						case 3:
							data = data.replaceFirst(","+GNGGA_par[6]+",",",4,");
							break;
					}
					newchecksum1 = "*" + CalCheckSum(temps);
					temps2 = temps.split(",");
					checksum1 = temps2[temps2.length - 1].substring(temps2[temps2.length - 1].length() - 3, temps2[temps2.length - 1].length());

					data = data.replace(checksum1, newchecksum1);
				}
			}
		}
    }
    public String CalCheckSum(String s) {
        char[] cArray = s.toCharArray();
        int checksum = 0;
        for (int l = 0; l < (cArray.length - 3); l++)
        {
            checksum = cArray[l] ^ checksum;
        }

		String sChecksum = Integer.toHexString(checksum).toUpperCase();
		if (sChecksum.length() == 1)
			sChecksum = "0" + sChecksum;

		return sChecksum;
	}

    private class GeoCoor {
		private double B;
		private double L;
		private double H;
		public GeoCoor(double longitude,double latitude,double height)
		{
			L=longitude;
			B=latitude;
			H=height;
		}

		public double getB() {return B;}
		public double getL() {return L;}
		public double getH() {return H;}
	}

	private class GausCoor {
		private double x;
		private double y;

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		public void setX(double x) {
			this.x = x;
		}

		public void setY(double y) {
			this.y = y;
		}
	}
	void BLtoXY(double L0, GeoCoor GeoPoint, GausCoor GausPoint) {

		double t, Ita2, N, m0, l,a0,a2,a4,a6,a8,s;
		double Temp1, Temp2, Temp3 ,Temp4, Temp5 ,Temp6 ,Temp7 ,Temp8;
		double PAI=3.1415926535898;
		double a=6378137.0;
		double f_c=1.0/298.257222101;
		double n=f_c/(2.0-f_c);
		a0=1.0+Math.pow(n,2)/4.0+Math.pow(n,4)/64.0;
		a2=3.0*(n-Math.pow(n,3)/8.0)/2.0;
		a4=15.0*(Math.pow(n,2)-Math.pow(n,4)/4.0)/16.0;
		a6=35.0*Math.pow(n,3)/48.0;
		a8=315.0*Math.pow(n,4)/512.0;

		//double a=6378245.0;
		//double e2=0.00669342162297;
		double e2=2.0*f_c-f_c*f_c;
		//double e12=0.00673852541468;
		double e12=e2/(1.0-e2);
		double p2=3600.0*180.0/PAI;
		double P0=PAI/180.0;
		/*54座標系常數*/
		double C0=6367558.49686;
		double C1=32005.79642;
		double C2=133.86115;
		double C3=0.7031;
		double temp1, temp2, temp3 ,temp4;
		/*高斯變換*/
		l = (GeoPoint.getL()-L0)*3600;
		t=Math.tan((GeoPoint.getB())*P0);
		temp1=t * t;
		Ita2=e12*Math.cos((GeoPoint.getB())*P0)*Math.cos((GeoPoint.getB())*P0);
		temp2= Ita2* Ita2;
		N=a/Math.sqrt(1-e2 * Math.sin((GeoPoint.getB())* P0)* Math.sin((GeoPoint.getB())*P0));
		m0=l* Math.cos((GeoPoint.getB())*P0)/p2;
		temp3=m0*m0;
		temp4= temp3* temp3;
		Temp1=N*m0;
		Temp2=C0*(GeoPoint.getB())*P0;
		Temp5= Math.sin(GeoPoint.getB()*P0)* Math.sin(GeoPoint.getB()*P0);

		Temp3= Math.cos(GeoPoint.getB()*P0)* Math.sin(GeoPoint.getB()*P0)*(C1+C2*Temp5+C3*Temp5*
				Temp5);
		Temp4=1.0/2.0*N*t*temp3;
		Temp5=1/24.0*(5.0-(temp1* temp1)+9.0* Ita2+4.0*(temp2* temp2))*N*t* temp4;
		Temp6=1/720*(61.0-58.0* temp1+(temp1* temp1)) *N*t*(temp3* temp4);
		Temp7=1/6.0*(1.0- temp1+ Ita2)* N*(m0*temp3);
		Temp8=1/120.0*(5.0-18.0* temp1+(temp1* temp1)+14.0* temp2-58.0* Ita2* temp1)*N*(m0*
				temp4);

		s=a*(a0*(GeoPoint.getB())*P0-a2*Math.sin(2.0*GeoPoint.getB()*P0)+a4*Math.sin(4.0*GeoPoint.getB()*P0)
				-a6*Math.sin(6.0*GeoPoint.getB()*P0)+a8*Math.sin(8.0*GeoPoint.getB()*P0))/(1.0+n);
		//GausPoint->x= Temp2-Temp3+Temp4+Temp5+Temp6;
		GausPoint.setY((s+Temp4+Temp5+Temp6)*0.9999);
		GausPoint.setX(250000+(Temp1+ Temp7+ Temp8)*0.9999);
	}


	public void putMaker() {
		/*String commadStr = LocationManager.GPS_PROVIDER;
		locationManager.requestLocationUpdates(commadStr, 0, 0, locationListener);
		Location location = locationManager.getLastKnownLocation(commadStr);
		centerURL = "javascript:centerAt(" + location.getLatitude() + "," + location.getLongitude() + ",0)";*/
		String centerURL = "javascript:centerAt(" + Latitude + "," + Longitude + ",0)";
		webview.loadUrl(centerURL);
	}


	LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location loc) {
			// TODO Auto-generated method stub
			//定位資料更新時會回呼
			/*if (plotLocus) {
				centerURL = "javascript:centerAt(" + loc.getLatitude() + "," + loc.getLongitude() + ")";
				webview.loadUrl(centerURL);
			}*/
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			//GPS狀態提供，這只有提供者為gps時才會動作
			switch (status) {
				case LocationProvider.OUT_OF_SERVICE:
					Log.d("GPS-NMEA", "OUT_OF_SERVICE");
					break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					Log.d("GPS-NMEA", " TEMPORARILY_UNAVAILABLE");
					break;
				case LocationProvider.AVAILABLE:
					Log.d("GPS-NMEA", "" + provider + "");
					break;
			}

		}

    };
    GpsStatus.NmeaListener NmeaListener = new GpsStatus.NmeaListener() {

		@Override
		public void onNmeaReceived(long timestamp, String nmea) {
			datafilter m_datafilter=new datafilter();
			if (timestamp-NMEA_TimeStamp<500)
				BuildInGPSNMEA += nmea+"\n";
			else {
				//msg +="---------------------------\nTime Stamp: "+String.valueOf(NMEA_TimeStamp)+"\n\n";
				NMEA_TimeStamp=timestamp;
				m_datafilter.filter(BuildInGPSNMEA);

				Latitude = m_datafilter.getlat();
				Longitude = m_datafilter.getlon();
				GPSTime = m_datafilter.getUTC();
				LocateStatus= m_datafilter.getGPSStatus();
				if(Latitude!=null && Longitude!=null && GPSTime!=null) {
					charthandler.sendEmptyMessage(0);
				}
				SatData = m_datafilter.getsatdata();
				if (nowtap == 3) {
					setcolumnchartData();
					if (llo != null) {
						llo.removeViewAt(0);
						llo.addView(chart);
					}
					final DrawView view = new DrawView(MainActivity.this,
							satloc.getWidth(), satloc.getHeight(), SatData);
					view.invalidate();
					if (satloc != null) {
						satloc.removeAllViews();
						satloc.addView(view);
					}
				}
				if (msgCount > 60) {
					resetNmeaMsg();
				}
				msg += BuildInGPSNMEA + "\n\n";
				msgCount++;
				msgText.setText(msg);
				BuildInGPSNMEA="";
				BuildInGPSNMEA += nmea+ "\n";

				if (AutoScroll.isChecked())
					mscrollView.fullScroll(ScrollView.FOCUS_DOWN); // 卷軸自動捲至最底

			}
		}
	};


	public native String stringFromJNI();
}
