var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    //exec(success, error, "FsNetworkPrinterService", "getNetworkPrinters", [arg0]);
    exec(success, error, "FsNetworkPrinterService", "getNetworkPrinters");
};
