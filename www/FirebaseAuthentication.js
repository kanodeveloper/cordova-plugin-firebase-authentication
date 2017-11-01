var exec = require("cordova/exec");
var PLUGIN_NAME = "FirebaseAuthentication";

module.exports = {
    getIdToken: function(forceRefresh, success, error) {
        exec(success, error, PLUGIN_NAME, "getIdToken", [forceRefresh]);
    },
    signInWithEmailAndPassword: function(email, password, success, error) {
        exec(success, error, PLUGIN_NAME, "signInWithEmailAndPassword", [email, password]);
    },
    verifyPhoneNumber: function(phoneNumber, timeout, success, error) {
        if (typeof timeout === "function") {
            error = success;
            success = timeout;
        }

        exec(success, error, PLUGIN_NAME, "verifyPhoneNumber", [phoneNumber, timeout]);
    },
    signInWithVerificationId: function(verificationId, code, success, error) {
        exec(success, error, PLUGIN_NAME, "signInWithVerificationId", [verificationId, code]);
    },
    signOut: function() {
        exec(success, error, PLUGIN_NAME, "signOut", []);
    },
    signInWithGoogle: function(success, error) {
        exec(success, error, PLUGIN_NAME, "signInWithGoogle", []);
    }
};