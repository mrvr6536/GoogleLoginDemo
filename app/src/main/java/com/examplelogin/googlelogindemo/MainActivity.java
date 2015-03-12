package com.examplelogin.googlelogindemo;

//TODO import these:
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;
//TODO end of imports.


import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


//TODO implement the GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener and View.OnClickListener interfaces
public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    //TODO declare/define the following:
    // App's tag
    final String TAG = "LoginDemo";
    // Google Api Client
    private GoogleApiClient mGoogleApiClient;
    // Request code to invoke sign in user interactions
    private static int REQUEST_CODE_SIGN_IN = 9001;
    // Request code to invoke the Google Play Services status
    private static int REQUEST_CODE_PLAY_SERVICES = 1001;
    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;
    // Has user clicked the sign-in button?
    private boolean mSignInClicked = false;
    //TODO end of declarations/definitions.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate():---");

        setContentView(R.layout.activity_main);

        //TODO Create the Google API Client to access the Play Game Services and add the listeners to the buttons
        // Create the Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
                .build();

        Log.d(TAG, "onCreate(): GoogleApiClient created");

        // Set up listener callbacks
        findViewById(R.id.button_sign_in).setOnClickListener(this);
        findViewById(R.id.button_sign_out).setOnClickListener(this);

        Log.d(TAG, "onCreate(): Listener attached");
        Log.d(TAG, "onCreate(): end ---");
    }


    //TODO implement the following methods to access the Games APIs
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart():---");

        super.onStart();
        if (!mResolvingConnectionFailure) {
            Log.d(TAG, "onStart(): connecting");
            mGoogleApiClient.connect();
        }
        Log.d(TAG, "onStart(): end ---");
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop():---");

        if (isUserSignedIn()) {

            Log.d(TAG, "onStop(): disconnecting");

            //mGoogleApiClient.disconnect();
            mGoogleApiClient.disconnect();
        }
        super.onStop();

        Log.d(TAG, "onStop(): end ---");
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume():---");
        super.onResume();
        if (checkPlayServices()) {
            Log.d(TAG, "onResume(): called checkePlayServices()");
            // Good to go
        }
        Log.d(TAG, "onResume(): end ---");
    }

    // Notify when sign-in is successful
    @Override
    public void onConnected (Bundle connectionHint) {
        Log.d(TAG, "onConnected(): connected to Google APIs ---");

        // The user is signed in. Show sign-out button, hide the sign-in button
        showSingOutBar();
        // Enable any UI component that dependes on Google APIs
        // Allow the user to proceed
        //TODO *** update UI, enable functionality that depends on sign in here

        Log.d(TAG, "onConnected(): end ---");
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed():---");

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed(): already resolving");
            // Already resolving
            return;
        }

        // Only resolve if the sign-in button was clicked
        if (mSignInClicked) {
            Log.d(TAG, "onConnectionFailed(): not resolving the connection failure, connectionResult == "
                    + connectionResult);
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            // Attempt to resolve the connection failure using BaseGameUtils.
            if(!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult,
                    REQUEST_CODE_SIGN_IN, getString(R.string.singin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }
        showSignInBar();

        Log.d(TAG, "onConnectionFailed(): end ---");
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): attempting to connect ---");

        // Disable any UI component that depend on Google APIs
        // Attempt to reconnect
        mGoogleApiClient.connect();

        Log.d(TAG, "onConnectionSuspended(): end ---");
    }


    // Check that the Google Play Services is correctly installed and configured on the device
    public boolean checkPlayServices() {
        Log.d(TAG, "checkPlayServices():---");
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if(status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                showErrorDialog(status);
            } else {
                Toast.makeText(this,R.string.device_not_supported,Toast.LENGTH_LONG).show();
            }
            Log.d(TAG, "checkPlayServices(): end --- Error was not recoverable");
            return false;
        }
        Log.d(TAG, "checkPlayServices(): end --- Play Services status == " + status);
        return true;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult(): ---");

        // Handle the result of the connection resolution
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                // The app is not connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    Log.d(TAG, "onActivityResult(): connecting requestCode== " + requestCode);
                    mGoogleApiClient.connect();
                }
            } else {
                // Bring up an error dialog to alert the user that sign-in failed.
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_failure);
            }
        }
        // Handle the result of the Google Play Services resolution
        if (requestCode == REQUEST_CODE_PLAY_SERVICES) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this,R.string.play_services_must_be_installed, Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode,intent);

        Log.d(TAG, "onActivityResult(): end ---" );
    }


    // Start the sign-in flow when user clicks sign in button
    public void onClick(View view) {
        if (view.getId() == R.id.button_sign_in) {

            Log.d(TAG, "onClick(): Sing-in button clicked");

            // Start the asynchronous sign in flow
            mSignInClicked = true;
            mGoogleApiClient.connect();
        } else if (view.getId() == R.id.button_sign_out) {

            Log.d(TAG, "onClick(): Sing-out button clicked");

            // Sign user out
            mSignInClicked = false;
            Games.signOut(mGoogleApiClient);
            if (isUserSignedIn()) {

                Log.d(TAG, "onClick(): disconnecting");

                mGoogleApiClient.disconnect();
            }
            // Show sign in button, hide sign out button
            showSignInBar();
        }
    }


    // Show the sing in bar & button
    private void showSignInBar() {
        Log.d(TAG, "showSignInBar(): ");
        findViewById(R.id.sign_in_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_out_bar).setVisibility(View.GONE);
        findViewById(R.id.mainTextView).setVisibility(View.VISIBLE);
    }


    // Show the sign out bar & button
    private void showSingOutBar() {
        Log.d(TAG, "showSignOutBar(): ");
        findViewById(R.id.sign_in_bar).setVisibility(View.GONE);
        findViewById(R.id.sign_out_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.mainTextView).setVisibility(View.GONE);
    }


    // Is user signed in?
    private boolean isUserSignedIn() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }


    //
    private void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this, REQUEST_CODE_PLAY_SERVICES).show();
    }
    //TODO end of methods implementation
}
