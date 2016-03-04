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
		
		if ("hoinPrint".equals(action)) {
			return this.hoinPrint(args.getString(0), args.getString(1), callbackContext);
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
    
    private boolean hoinPrint(final String header, final String document, final CallbackContext callbackContext) {
        if (_hoinConnectionFlag != 2) {
            toast("The HOIN printer is not connected. " + _hoinConnectionFlag);
            callbackContext.error("The HOIN printer is not connected.");
            return false;
        }
        
        WifiCommunication hoinWifi = getHoinWifi();
        
        byte[] cmd = new byte[3];
        cmd[0] = 0x1b;
        cmd[1] = 0x21;
        cmd[2] |= 0x10; // Dougle height
        hoinWifi.sendMsg(header, "GBK");
        cmd[2] &= 0xEF;
        hoinWifi.sndByte(cmd);          //cancel double height and double width mode
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        hoinWifi.sendMsg(document, "GBK");
        callbackContext.success("Document sent to printer");
        
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
					_hoinConnectionFlag = 2;
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
    }
}