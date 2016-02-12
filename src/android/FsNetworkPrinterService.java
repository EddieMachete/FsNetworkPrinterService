package cordova.plugin.fsnetworkprinterservice;


import android.app.Activity;
import android.graphics.Color;
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
//import org.json.JSONArray;
import org.json.JSONException;
//import org.json.JSONObject;

public class FsNetworkPrinterService extends CordovaPlugin {
    private static final String TAG = "FsNetworkPrinterService";

	@Override
	public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
		Log.v(TAG, "initialization");
		super.initialize(cordova, webView);
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
    public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        Log.v(TAG, "Executing action: " + action);

        if ("echo".equals(action)) {
            this.echo("message for echo", callbackContext);
            return true;
        }

		callbackContext.success("did not match");
		return true;

        //return false;
    }
    
    private void echo(final String message, final CallbackContext callbackContext) {
        /*if (message != null && message.length() > 0) { 
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }*/

		callbackContext.success("abcde");
    }
}