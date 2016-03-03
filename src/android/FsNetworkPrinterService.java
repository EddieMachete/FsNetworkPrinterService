package cordova.plugin.fsnetworkprinterservice;


import android.app.Activity;
import android.graphics.Color;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

// HOIN PRINTER
import com.hoin.wfsdk.PrintPic;
import com.hoin.wfsdk.WifiCommunication;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FsNetworkPrinterService extends CordovaPlugin {
    private static final String TAG = "FsNetworkPrinterService";
    private static String _toastMessage = "Hello";
    private NsdManager.DiscoveryListener _discoveryListener;
    private String _status = "not initialized";
    private String _devices = "";
	
	// HOIN PRINTER
	private static final int WFPRINTER_REVMSG = 0x06;
	private int _hoinConnectionFlag = 0;
	private String _lastStatus = "";
	private WifiCommunication _hoinWifi = null;
	private HoinMessageThread _hoinMessageThread = null;
	private CallbackContext _connectionCallbackContext = null;

	@Override
	public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
		//Log.v(TAG, "initialization");
		super.initialize(cordova, webView);
        //initializeDiscoveryListener();
        _status = "initialized";
	}

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false otherwise.
     */
    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.v(TAG, "Executing action: " + action);

        if ("initialize".equals(action))
        {
            //cordova.getThreadPool().execute(new Runnable() {
            //        public void run() {
            //            
            //        }
            //    });
            
            callbackContext.success(_status + " :: " + _devices);
                
            return true;
        }
    
        if ("echo".equals(action)) {
            return this.echo(args.getString(0), callbackContext);
        }
        
        if ("getPrinters".equals(action)) {
            return this.getPrinters(callbackContext);
        }
		
		if ("connectToHoinPrinter".equals(action)) {
			return this.connectToHoinPrinter(args.getString(0), callbackContext);
		}
		

		callbackContext.success("did not match");
		return true;

        //return false;
		//IntentFilter wifiFilter = new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		//cordova.getActivity().registerReceiver(wifiBroadcastReceiver, wifiFilter);
		//this.callbackContext = callbackId;
		//PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
		//result.setKeepCallback(true);
		//this.callbackContext.sendPluginResult(result);
		//return true;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		closeHoinPrinterSocket();
	}
    
    private boolean echo(final String message, final CallbackContext callbackContext) {
        if (message != null && message.length() > 0) { 
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
		
		return true;
    }
    
    private void toast(String message) {
        _toastMessage = message;
        
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(cordova.getActivity().getApplicationContext(), _toastMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
	
	private void sendUpdate(String status) {
        toast(status);
        
		if (_connectionCallbackContext == null) {
			return;
		}
        
		// Only report status if the status actually changed
		if (_lastStatus.equals(status)) {
			return;
		}
		
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("status", status);
            PluginResult result = new PluginResult(PluginResult.Status.OK, parameters);
            result.setKeepCallback(true);
            _connectionCallbackContext.sendPluginResult(result);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
	}
	
	// ----- HOIN METHODS ----------------------------------
	
	private WifiCommunication getHoinWifi() {
		if (_hoinWifi == null) {
			_hoinMessageThread = new HoinMessageThread();
			_hoinWifi = new WifiCommunication(_hoinWifiHandler);
		}
		
		return _hoinWifi;
	}
	
	private void closeHoinPrinterSocket() {
		if (_hoinWifi == null)
			return;
			
		_hoinWifi.close();
		_hoinWifi = null;
		_hoinMessageThread.interrupt();
		_hoinMessageThread = null;
	}
	
	private boolean connectToHoinPrinter(final String printerIp, final CallbackContext callbackContext) {
		if (_hoinConnectionFlag != 0) {
			callbackContext.error("The system is currently processing a print request.");
			return false;
		}
        
		_hoinConnectionFlag = 1;
		_connectionCallbackContext = callbackContext;
		getHoinWifi().initSocket(printerIp, 9100);
        _hoinMessageThread.start();
		
		return true;
	}
    
    private boolean hoinPrint(final String text, final CallbackContext callbackContext) {
        if (_hoinConnectionFlag != 2) {
            callbackContext.error("The HOIN printer is not connected.");
            return false;
        }
        
        String msg = "";
        byte[] tcmd = null;
        WifiCommunication hoinWifi = getHoinWifi();
        
        //byte[] cmd = new byte[3];
        //cmd[0] = 0x1b;
        //cmd[1] = 0x21;
        //cmd[2] |= 0x10; // Dougle height
        //hoinWifi.sndByte(cmd);          //set double height and double width mode
        hoinWifi.sendMsg(text, "GBK");
        //cmd[2] &= 0xEF;
        //wfComm.sndByte(cmd);          //cancel double height and double width mode
        
        //try {
        //    Thread.sleep(50);                   //ÿ��һ����ʱ5����
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
        
        //msg = "  You have sucessfully created communications between your device and our WIFI printer.\n\n"
        //        +"  Our company is a high-tech enterprise which specializes" +
        //        " in R&D,manufacturing,marketing of thermal printers and barcode scanners.\n\n";
        //wfComm.sendMsg(msg, "GBK");
        
        return true;
    }
	
	private boolean disconnectFromHoinPrinter(final CallbackContext callbackContext) {
		return true;
	}
	
	private class HoinMessageThread extends Thread {	
		@Override
		public void run() {            
			try {
				Message message = new Message();
				int revData;
		
				while (true) {
					revData = getHoinWifi().revByte();
					
					if(revData != -1) {
						message = _hoinWifiHandler.obtainMessage(WFPRINTER_REVMSG);
						message.obj = revData;
						_hoinWifiHandler.sendMessage(message);
					}
					
					Thread.sleep(20);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				Log.v(TAG, "Hoin printer error");
			}
		}
	}
	
	private final Handler _hoinWifiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// What is an integer
			switch (msg.what) {
				case WifiCommunication.WFPRINTER_CONNECTED:
					//connFlag = 0;
					sendUpdate("hoin_printer_connected");
					break;
				case WifiCommunication.WFPRINTER_DISCONNECTED:
					sendUpdate("hoin_printer_disconnected");
					_hoinMessageThread.interrupt();
					break;
				case WifiCommunication.SEND_FAILED:
					sendUpdate("hoin_printer_send_failed");
					//connFlag = 0;
					//Toast.makeText(getActivity(), "Send Data Failed,please reconnect",
					//		Toast.LENGTH_SHORT).show();
					//revThred.interrupt();
					break;
				case WifiCommunication.WFPRINTER_CONNECTEDERR:
					sendUpdate("hoin_printer_connected_err");
					//connFlag = 0;
					//Toast.makeText(getActivity(), "Connect the WIFI-printer error",
					//		Toast.LENGTH_SHORT).show();
					break;
				case WFPRINTER_REVMSG:
					byte revData = (byte)Integer.parseInt(msg.obj.toString());
					
					if (((revData >> 6) & 0x01) == 0x01) {
						//Toast.makeText(getActivity(), "The printer has no paper",Toast.LENGTH_SHORT).show(); 
						sendUpdate("hoin_printer_check_paper");
					}
					break;
				default:
					break;
			}
		}
	};
	
	// ----- END OF HOIN METHODS ----------------------------------
    
	// EE 2016-03-01: This method is meant to return a list of available printers.
	// We won't be using it as our hoin printers have to be specified by the restaurant and are non-discoverable.
	// However, this method may be required donw the road to support other network printers.
    private boolean getPrinters(final CallbackContext callbackContext) {
        JSONObject printer1 = new JSONObject();
        printer1.optString("name", "Printer 1");
		
		JSONObject printer2 = new JSONObject();
        printer2.optString("name", "Printer 2");
		
		JSONArray printers = new JSONArray();
		printers.put(printer1);
		printers.put(printer2);

		//String jsonString = JSONValue.toJSONString(printers);
        
        //Boolean supported   = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        //PluginResult result = new PluginResult(PluginResult.Status.OK, supported);
        //command.sendPluginResult(result);
        
        callbackContext.success(printers);
		
		return true;
    }
    
    
    
    // EE 2016-03-01: This listener looks for available network devices.
	// I was using this before we got our hoin printers, which are not discoverable.
	// I'm keeping this code in case we need to move to a different printer.
    private void initializeDiscoveryListener()
    {
        // Instantiate a new DiscoveryListener
        _discoveryListener = new NsdManager.DiscoveryListener() {
            
            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                _devices += service.getServiceType();
                //Log.d(TAG, "Service discovery success" + service);
                //if (!service.getServiceType().equals(SERVICE_TYPE)) {
                //    // Service type is the string containing the protocol and
                //    // transport layer for this service.
                //    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                //} else if (service.getServiceName().equals(mServiceName)) {
                //    // The name of the service tells the user what they'd be
                //    // connecting to. It could be "Bob's Chat App".
                //    Log.d(TAG, "Same machine: " + mServiceName);
                //} else if (service.getServiceName().contains("NsdChat")){
                //    mNsdManager.resolveService(service, mResolveListener);
                //}
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                //mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                //mNsdManager.stopServiceDiscovery(this);
            }
        };
    
    
    
        /*
import com.hoin.wfsdk.PrintPic;
import com.hoin.wfsdk.WifiCommunication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WifiFragment extends Fragment {
	Button btnConn = null;
	Button btnPrint = null;
	Button qrCodeSend = null;
	Button btn_test = null;
	Button btnClose = null;
//	Button btn_opencasher = null;
	EditText edtContext = null;
	WifiCommunication wfComm = null;
	EditText txt_ip = null;
	int  connFlag = 0;
	revMsgThread revThred = null;
	//checkPrintThread cheThread = null;
	private static final int WFPRINTER_REVMSG = 0x06;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.wifi_fragment, null);
		btnConn = (Button)view.findViewById(R.id.btn_conn); 
		btnConn.setOnClickListener(new ClickEvent());
		btnPrint = (Button)view.findViewById(R.id.btnSend);
		btnPrint.setOnClickListener(new ClickEvent());
		qrCodeSend = (Button)view.findViewById(R.id.qr_code_Send);
		qrCodeSend.setOnClickListener(new ClickEvent());
		btn_test = (Button)view.findViewById(R.id.btn_test);
		btn_test.setOnClickListener(new ClickEvent());
		btnClose = (Button)view.findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new ClickEvent());
		edtContext = (EditText) view.findViewById(R.id.txt_content);
		txt_ip = (EditText)view.findViewById(R.id.txt_ip);
		wfComm = new WifiCommunication(mHandler);
//		btn_opencasher = (Button)this.findViewById(R.id.btn_opencasher);
//		btn_opencasher.setOnClickListener(new ClickEvent());

		btnConn.setEnabled(true);
		btnPrint.setEnabled(false);
		qrCodeSend.setEnabled(false);
		btn_test.setEnabled(false);
		btnClose.setEnabled(false);
//		btn_opencasher.setEnabled(false);
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		wfComm.close();
		wfComm = null;
	}
	
	class ClickEvent implements View.OnClickListener {
		public void onClick(View v) {

			String msg = "";
			byte[] tcmd = null;
			switch (v.getId()) {
			case R.id.btn_conn:
				if( connFlag == 0 ){   //�������������˰�ť�������������߳�
					connFlag = 1;
					Log.d("wifi����","����\"����\"");
					String strAddressIp = txt_ip.getText().toString();
					wfComm.initSocket(strAddressIp,9100);
				}
				break;
			case R.id.btnSend:
				msg = edtContext.getText().toString();
				if( msg.length() > 0 ){
					wfComm.sendMsg(msg, "GBK");
				}
				break;
			case R.id.qr_code_Send:
				tcmd = new byte[7];
				tcmd[0] = 0x1B;
				tcmd[1] = 0x5A;
				tcmd[2] = 0x00;
				tcmd[3] = 0x02;
				tcmd[4] = 0x06;
				tcmd[5] = 0x17;
				tcmd[6] = 0x00;
				String content = getResources().getString(R.string.wifi_qr_code_Send_string);
				wfComm.sndByte(tcmd);
				wfComm.sendMsg(content, "GBK");
				break;
			case R.id.btn_test:
				tcmd = new byte[3];
				tcmd[0] = 0x10;
				tcmd[1] = 0x04;
				tcmd[2] = 0x04;     
				wfComm.sndByte(tcmd);   //�����Ƿ���ָֽ��

				String lang = getString(R.string.wifi_strLang);
				printImage();           //��ӡͼƬ
				byte[] cmd = new byte[3];        
				cmd[0] = 0x1b;
				cmd[1] = 0x21;
				if((lang.compareTo("en")) == 0){	
					cmd[2] |= 0x10;
					wfComm.sndByte(cmd);          //set double height and double width mode
					wfComm.sendMsg("Congratulations! \n\n", "GBK");
					cmd[2] &= 0xEF;        
					wfComm.sndByte(cmd);          //cancel double height and double width mode
					try {
						Thread.sleep(50);                   //ÿ��һ����ʱ5����
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
					msg = "  You have sucessfully created communications between your device and our WIFI printer.\n\n"
							+"  Our company is a high-tech enterprise which specializes" +
							" in R&D,manufacturing,marketing of thermal printers and barcode scanners.\n\n";
					wfComm.sendMsg(msg, "GBK");
				}else if((lang.compareTo("ch")) == 0){
					cmd[2] |= 0x10;
					wfComm.sndByte(cmd);             //set double height and double width mode
					wfComm.sendMsg("��ϲ��! \n\n", "GBK");  //send data to the printer By gbk encoding
					cmd[2] &= 0xEF;                 
					wfComm.sndByte(cmd);            //cancel double height and double width mode
					try {
						Thread.sleep(50);                   //ÿ��һ����ʱ5����
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
					msg = "  ���Ѿ��ɹ��������������ǵ�WIFI��ӡ����\n\n"
							+ "  ���ǹ�˾��һ��רҵ�����з�����������������Ʊ�ݴ�ӡ��������ɨ���豸��һ���ĸ߿Ƽ���ҵ.\n\n";
					wfComm.sendMsg(msg, "GBK");
				}
				break;
			case R.id.btnClose:
				wfComm.close();
				break;
//			case R.id.btn_opencasher:
//				tcmd = new byte[5];
//				tcmd[0] = 0x1B;
//				tcmd[1] = 0x70;
//				tcmd[2] = 0x00;     
//				tcmd[3] = 0x40;   
//				tcmd[4] = 0x50;   
//				wfComm.sndByte(tcmd);
//				break;
			}
		}
	}  

	private final  Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WifiCommunication.WFPRINTER_CONNECTED:
				connFlag = 0;
				Toast.makeText(getActivity(), "Connect the WIFI-printer successful",
						Toast.LENGTH_SHORT).show();
				btnPrint.setEnabled(true);
				qrCodeSend.setEnabled(true);
				btn_test.setEnabled(true);
				btnClose.setEnabled(true);
//				btn_opencasher.setEnabled(true);
				btnConn.setEnabled(false);

				revThred = new revMsgThread();
				revThred.start();
				break;
			case WifiCommunication.WFPRINTER_DISCONNECTED:
				Toast.makeText(getActivity(), "Disconnect the WIFI-printer successful",
						Toast.LENGTH_SHORT).show();
				btnConn.setEnabled(true);
				btnPrint.setEnabled(false);
				qrCodeSend.setEnabled(false);
				btn_test.setEnabled(false);
				btnClose.setEnabled(false);
//				btn_opencasher.setEnabled(false);
				revThred.interrupt();
				break;
			case WifiCommunication.SEND_FAILED:
				connFlag = 0;
				Toast.makeText(getActivity(), "Send Data Failed,please reconnect",
						Toast.LENGTH_SHORT).show();
				btnConn.setEnabled(true);
				btnPrint.setEnabled(false);
				qrCodeSend.setEnabled(false);
				btn_test.setEnabled(false);
				btnClose.setEnabled(false);
//				btn_opencasher.setEnabled(false);
				revThred.interrupt();
				break;
			case WifiCommunication.WFPRINTER_CONNECTEDERR:
				connFlag = 0;
				Toast.makeText(getActivity(), "Connect the WIFI-printer error",
						Toast.LENGTH_SHORT).show();
				break;
			case WFPRINTER_REVMSG:
				byte revData = (byte)Integer.parseInt(msg.obj.toString());
				if(((revData >> 6) & 0x01) == 0x01)
					Toast.makeText(getActivity(), "The printer has no paper",Toast.LENGTH_SHORT).show();    
				break;
			default:
				break;
			}
		}
	};

	class checkPrintThread extends Thread {
		@Override
		public void run() {
			byte[] tcmd = new byte[3];
			tcmd[0] = 0x10;
			tcmd[1] = 0x04;
			tcmd[2] = 0x04;
			try {
				while(true){
					wfComm.sndByte(tcmd);
					Thread.sleep(15);
					Log.d("wifi����","����һ�ε�������");
				}
			}catch (InterruptedException e){
				e.printStackTrace();
				Log.d("wifi����","�˳��߳�");
			}
		}
	}

	//��ӡ���̣߳������ϴ�ӡ��ʱ�������رմ�ӡ��ʱ�˳�
	class revMsgThread extends Thread {	
		@Override
		public void run() {            
			try {
				Message msg = new Message();
				int revData;
				while(true)
				{
					revData = wfComm.revByte();               //�����������ֽڽ������ݣ������ĳɷ����������ַ������ο��ֲ�
					if(revData != -1){

						msg = mHandler.obtainMessage(WFPRINTER_REVMSG);
						msg.obj = revData;
						mHandler.sendMessage(msg);
					}    
					Thread.sleep(20);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				Log.d("wifi����","�˳��߳�");
			}
		}
	}


	//��ӡͼ��
	private void printImage() {
		byte[] sendData = null;
		PrintPic pg = new PrintPic();
		int i = 0,s = 0,j = 0,index = 0,lines = 0;
		pg.initCanvas(384);     
		pg.initPaint();
		pg.drawImage(0, 0, "/mnt/sdcard/icon.bmp");
		sendData = pg.printDraw();
		byte[] temp = new byte[(pg.getWidth() / 8)*5];
		byte[] dHeader = new byte[8];
		if(pg.getLength()!=0){
			dHeader[0] = 0x1D;
			dHeader[1] = 0x76;
			dHeader[2] = 0x30;
			dHeader[3] = 0x00;
			dHeader[4] = (byte)(pg.getWidth()/8);
			dHeader[5] = 0x00;
			dHeader[6] = (byte)(pg.getLength()%256);
			dHeader[7] = (byte)(pg.getLength()/256);
			wfComm.sndByte(dHeader); 	
			for( i = 0 ; i < (pg.getLength()/5)+1 ; i++ ){         //ÿ��5�з���һ��ͼƬ����
				s = 0;
				if( i < pg.getLength()/5 ){
					lines = 5;
				}else{
					lines = pg.getLength()%5;
				}
				for( j = 0 ; j < lines*(pg.getWidth() / 8) ; j++ ){
					temp[s++] = sendData[index++];
				}
				wfComm.sndByte(temp); 
				try {
					Thread.sleep(60);                              //ÿ��һ����ʱ60����
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				for(j = 0 ; j <(pg.getWidth()/8)*5 ; j++ ){         //����������
					temp[j] = 0;
				}
			}
		}
	}	
}
        */
        /*//Save init callback
        initCallbackContext = callbackContext;

        if (bluetoothAdapter != null)
        {
            JSONObject returnObj = new JSONObject();
            PluginResult pluginResult;

            if (bluetoothAdapter.isEnabled())
            {
            addProperty(returnObj, keyStatus, statusEnabled);

            pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
            pluginResult.setKeepCallback(true);
            initCallbackContext.sendPluginResult(pluginResult);
            }
            else
            {
            addProperty(returnObj, keyStatus, statusDisabled);
            addProperty(returnObj, keyMessage, logNotEnabled);

            pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
            pluginResult.setKeepCallback(true);
            initCallbackContext.sendPluginResult(pluginResult);
            }

            return;
        }

        Activity activity = cordova.getActivity();

        JSONObject obj = getArgsObject(args);

        if (obj != null && getStatusReceiver(obj))
        {
            //Add a receiver to pick up when Bluetooth state changes
            activity.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            isReceiverRegistered = true;
        }

        //Get Bluetooth adapter via Bluetooth Manager
        BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        connections = new HashMap<Object, HashMap<Object,Object>>();

        JSONObject returnObj = new JSONObject();

        //If it's already enabled,
        if (bluetoothAdapter.isEnabled())
        {
            addProperty(returnObj, keyStatus, statusEnabled);
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
            pluginResult.setKeepCallback(true);
            initCallbackContext.sendPluginResult(pluginResult);
            return;
        }

        boolean request = false;
        if (obj != null)
        {
            request = getRequest(obj);
        }

        //Request user to enable Bluetooth
        if (request)
        {
            //Request Bluetooth to be enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            cordova.startActivityForResult(this, enableBtIntent, REQUEST_BT_ENABLE);
        }
        //No request, so send back not enabled
        else
        {
            addProperty(returnObj, keyStatus, statusDisabled);
            addProperty(returnObj, keyMessage, logNotEnabled);
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
            pluginResult.setKeepCallback(true);
            initCallbackContext.sendPluginResult(pluginResult);
        }*/
    }
}