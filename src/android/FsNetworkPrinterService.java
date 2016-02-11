package cordova.plugin.FsNetworkPrinterService;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;

/**
 * This class echoes a string called from JavaScript.
 */
public class FsNetworkPrinterService extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getNetworkPrinters")) {
            //String message = args.getString(0);
            //this.getNetworkPrinters(message, callbackContext);
            this.getNetworkPrinters(callbackContext);
            return true;
        }

        return false;
    }

    private void getNetworkPrinters(CallbackContext callbackContext) {
        //if (message != null && message.length() > 0) {
        //    callbackContext.success(message);
        //} else {
        //    callbackContext.error("Expected one non-empty string argument.");
        //}

		JSONObject printer1 = new JSONObject();
        returnObject.put("name", "Printer 1");
		
		JSONObject printer2 = new JSONObject();
        returnObject.put("name", "Printer 2");
		
		JSONArray printers = new JSONArray();
		printers.put(printer1);
		printers.put(printer2);

		//String jsonString = JSONValue.toJSONString(printers);
		//callbackContext.success(jsonString);
		callbackContext.success(printers);
    }
}