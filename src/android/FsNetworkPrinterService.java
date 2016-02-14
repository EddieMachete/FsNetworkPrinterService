package cordova.plugin.fsnetworkprinterservice;


import android.app.Activity;
import android.graphics.Color;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import android.os.Build;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

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
    private NsdManager.DiscoveryListener _discoveryListener;
    private String _status = "not initialized";
    private String _devices = "";

	@Override
	public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
		Log.v(TAG, "initialization");
		super.initialize(cordova, webView);
        initializeDiscoveryListener();
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
            this.echo(args.getString(0), callbackContext);
            return true;
        }
        
        if ("getPrinters".equals(action)) {
            this.getPrinters(callbackContext);
            return true;
        }

		callbackContext.success("did not match");
		return true;

        //return false;
    }
    
    private void echo(final String message, final CallbackContext callbackContext) {
        if (message != null && message.length() > 0) { 
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    
    private void getPrinters(final CallbackContext callbackContext) {
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
    }
    
    
    
    
    private void initializeDiscoveryListener()
    {
        Log.v(TAG, "+++++ Initializing");
        
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