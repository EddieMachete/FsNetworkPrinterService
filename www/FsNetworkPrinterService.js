var exec = require('cordova/exec');

//exports.coolMethod = function (arg0, success, error) {
//    exec(success, error, "FsNetworkPrinterService", "getNetworkPrinters", [arg0]);
//    exec(success, error, "FsNetworkPrinterService", "echo", [arg0]);
//    //exec(success, error, "FsNetworkPrinterService", "getNetworkPrinters");
//};

var fsNetworkPrinterService = {
    // The JavaScript portion of a plugin always uses the cordova.exec method as follows:
    // exec(<successFunction>, <failFunction>, <service>, <action>, [<args>]);
    initialize: function (success, error) {
        exec(success, error, "FsNetworkPrinterService", "initialize", []);
    },
    echo: function (message, success, error) {
        exec(success, error, "FsNetworkPrinterService", "echo", [message]);
    },
    getPrinters: function (success, error) {
        exec(success, error, "FsNetworkPrinterService", "getPrinters", []);
    },
    connectToHoinPrinter: function (printerIp, success, error) {
        exec(success, error, "FsNetworkPrinterService", "getPrinters", [printerIp]);
    }
};

module.exports = fsNetworkPrinterService;