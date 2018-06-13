package by.chemerisuk.cordova.firebase;

import android.util.Log;

import android.support.annotation.NonNull;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FirebaseAuthenticationPlugin extends CordovaPlugin implements OnCompleteListener<AuthResult> {
    private static final String TAG = "FirebaseAuthentication";

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider phoneAuthProvider;
    private CallbackContext signinCallback;
    private GoogleApiClient googleApiClient;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Firebase Authentication plugin");

        this.firebaseAuth = FirebaseAuth.getInstance();
        this.phoneAuthProvider = PhoneAuthProvider.getInstance();

        Context context = this.cordova.getActivity().getApplicationContext();
        String defaultClientId = getDefaultClientId(context);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(defaultClientId)
                .requestEmail()
                .requestProfile()
                .build();

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleApiClient.connect();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("signInAnonymously")) {
            signInAnonymously(callbackContext);
            return true;
        }
        else if (action.equals("getIdToken")) {
            getIdToken(args.getBoolean(0), callbackContext);
            return true;
        }

        return false;
    }

    @Override
    public void onComplete(Task<AuthResult> task) {

        if (this.signinCallback == null) return;

        if (task.isSuccessful()) {
            FirebaseUser user = task.getResult().getUser();
            this.signinCallback.success(getProfileData(user));
        } else {
            this.signinCallback.error(task.getException().getMessage());
        }

        this.signinCallback = null;
    }

    private void signInAnonymously(CallbackContext callbackContext) throws JSONException {
        this.signinCallback = callbackContext;

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                firebaseAuth.signInAnonymously()
                    .addOnCompleteListener(cordova.getActivity(), FirebaseAuthenticationPlugin.this);
            }
        });
    }

    private void getIdToken(boolean forceRefresh, final CallbackContext callbackContext) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            callbackContext.error("User is not authorized");
        } else {
            user.getIdToken(forceRefresh)
                    .addOnCompleteListener(cordova.getActivity(), new OnCompleteListener<GetTokenResult>() {
                        @Override
                        public void onComplete(Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                callbackContext.success(task.getResult().getToken());
                            } else {
                                callbackContext.error(task.getException().getMessage());
                            }
                        }
                    });
        }
    }

    private void signOut(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                firebaseAuth.signOut();

                callbackContext.success();
            }
        });
    }

    private static JSONObject getProfileData(FirebaseUser user) {
        JSONObject result = new JSONObject();

        try {
            result.put("uid", user.getUid());
            result.put("displayName", user.getDisplayName());
            result.put("email", user.getEmail());
            result.put("phoneNumber", user.getPhoneNumber());
            result.put("photoURL", user.getPhotoUrl());
            result.put("providerId", user.getProviderId());
        } catch (JSONException e) {
            Log.e(TAG, "Fail to process getProfileData", e);
        }

        return result;
    }

    private String getDefaultClientId(Context context) {

        String packageName = context.getPackageName();
        int id = context.getResources().getIdentifier("default_web_client_id", "string", packageName);
        return context.getString(id);
    }
}
