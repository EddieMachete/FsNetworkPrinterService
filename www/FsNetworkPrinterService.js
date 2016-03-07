(function () {
    'use strict';
    
    if (!require) {
        console.log('FsNetworkPrinterService :: cordova/require is not available');
        return;
    }

    // -- PRIVATE VARIABLES ----------
    var _exec = require('cordova/exec');
    var _status = {
        isConnected: false
    };

    // -- PRIVATE METHODS ----------
    function initialize(success, error) {
        _exec(success, error, "FsNetworkPrinterService", "initialize", []);
    }
    
    function echo(message, success, error) {
            _exec(success, error, "FsNetworkPrinterService", "echo", [message]);
        }
        
    function getPrinters(success, error) {
        _exec(success, error, "FsNetworkPrinterService", "getPrinters", []);
    }
    
    function connectToHoinPrinter(printerIp, success, error) {
        _exec(
            function (data) {
                _status.isConnected = data.status === 'hoin_printer_connected';
                
                if (success && typeof success === 'function')
                    success(data);
            },
            error,
            "FsNetworkPrinterService", "connectToHoinPrinter", [printerIp]);
    }

    function hoinPrint(header, document, success, error) {
        _exec(success, error, "FsNetworkPrinterService", "hoinPrint", [header, document]);
    }
    
    function getHoinPrinterStatus() {
        return _status;
    }

    // -- BEGIN CORDOVA PLUGIN REGISTRATION ---------
    var fsNetworkPrinterService = {
        initialize: initialize,
        echo: echo,
        getPrinters: getPrinters,
        connectToHoinPrinter: connectToHoinPrinter,
        hoinPrint: hoinPrint
    };
    
    module.exports = fsNetworkPrinterService;
    // -- END OF CORDOVA PLUGIN REGISTRATION ---------------
})();