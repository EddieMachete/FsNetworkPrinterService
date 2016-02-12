var exec = require('cordova/exec');

//exports.coolMethod = function (arg0, success, error) {
//    exec(success, error, "FsNetworkPrinterService", "getNetworkPrinters", [arg0]);
//    exec(success, error, "FsNetworkPrinterService", "echo", [arg0]);
//    //exec(success, error, "FsNetworkPrinterService", "getNetworkPrinters");
//};

var fsNetworkPrintersService = {
    // The JavaScript portion of a plugin always uses the cordova.exec method as follows:
    // exec(<successFunction>, <failFunction>, <service>, <action>, [<args>]);
    echo: function (message, success, error) {
        exec(success, error, "FsNetworkPrinterService", "echo", [message]);
    }
};

module.exports = fsNetworkPrintersService;