var exec = require("cordova/exec");
var PLUGIN_NAME = "FirebaseAuthentication";

module.exports = {
    signInAnonymously: function(success, error) {
        exec(success, error, PLUGIN_NAME, "signInAnonymously", []);
    },
    getIdToken: function(forceRefresh, success, error) {
        exec(success, error, PLUGIN_NAME, "getIdToken", [forceRefresh]);
    }
};