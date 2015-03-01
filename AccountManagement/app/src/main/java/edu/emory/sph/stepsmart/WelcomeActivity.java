package edu.emory.sph.stepsmart;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.swhittier.accountmanagement.R;

import edu.emory.sph.stepsmart.authentication.AccountHelper;

public class WelcomeActivity extends Activity {


    private Context _context = null;
    private Account _account = null;
    private GetAuthTokenTask _getAuthTokenTask = null;


    static final String CLASS_TAG = "WelcomeActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        _context = getApplicationContext();

        setContentView(R.layout.activity_welcome);

        if(AccountHelper.getInstance(this).accountExists()) {

            _account = AccountHelper.getInstance(_context).getAccount();

            // Uncomment this line to test invalidated token.
            // AccountHelper.getInstance(_context).invalidateAuthToken(AccountHelper.ACCOUNT_TYPE, AccountHelper.AUTH_TOKEN);

            startUserLoginActivity();

        }
        else
            startUserRegistrationActivity();
    }


    @Override
    protected void onResume() {

        super.onResume();


        if(AccountHelper.getInstance(this).accountExists()) {

            _account = AccountHelper.getInstance(_context).getAccount();

            // Uncomment this line to test invalidated token.
            // AccountHelper.getInstance(_context).invalidateAuthToken(AccountHelper.ACCOUNT_TYPE, AccountHelper.AUTH_TOKEN);

            // Get Auth Token.  This will either get a token if the current token is not invalidated
            // or present the UserLogin Activity to re-authenticate the user and set a new Auth Token.
            _getAuthTokenTask = new GetAuthTokenTask(this);
            _getAuthTokenTask.execute();

        }

    }


    /**
     * Starts the UserRegistrationActivity Activity.
     */
    private void startUserRegistrationActivity() {


        Intent myIntent = new Intent(this, UserRegistrationActivity.class);
        this.startActivityForResult(myIntent, REGISTRATION_ACTIVITY);
    }

    /**
     * Starts the UserLogin Activity.
     */
    static final int LOGIN_ACTIVITY = 1;
    static final int REGISTRATION_ACTIVITY = 2;
    private void startUserLoginActivity() {

        Intent myIntent = new Intent(this, UserLoginActivity.class);
        this.startActivityForResult(myIntent, LOGIN_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == LOGIN_ACTIVITY) {

            if (resultCode == RESULT_OK) {
                Log.i("### WelcomeActivity ###", "Login Activity returned okay");
            } else {
                Log.i("### WelcomeActivity ###", "Login Activity returned error");
                startUserLoginActivity();
            }
        } else if (requestCode == REGISTRATION_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                Log.i("### WelcomeActivity ###", "Registration Activity returned okay");
            } else {
                Log.i("### WelcomeActivity ###", "Registration Activity returned error");
                startUserRegistrationActivity();
            }
        }
    }


    /**
     * Callback for onPostExecute() of GetAuthTokenTask
     * @param result The result of the call to get the Auth Token.
     */
    public void onGetAuthTokenResult(Bundle result) {

        String METHOD_TAG;
        METHOD_TAG = CLASS_TAG + ".onGetAuthTokenResult()";

        Log.d(METHOD_TAG, "Get Auth Token task is complete.");

        // Our task is complete, so clear it out.
        _getAuthTokenTask = null;

        // Check the bundle for the token.  If one is found, go to app home.
        if(result != null && result.getString(AccountManager.KEY_AUTHTOKEN) != null ) {

            Log.d(METHOD_TAG, "Retrieval of Auth Token was successful. Starting ApplicationHome Activity.");

        }
        else {

            // TODO: remove this when the UserLogin Activity can be started by the system in getAuthToken in the Authenticator.
            Log.d(METHOD_TAG, "Retrieval of Auth Token was unsuccessful. Starting UserLogin Activity.");

            startUserLoginActivity();
        }

    }

    /**
     * Callback for onCancelled() of GetAuthTokenTask
     */
    public void onGetAuthTokenCancel() {

        String METHOD_TAG;
        METHOD_TAG = CLASS_TAG + ".onGetAuthTokenCancel()";

        Log.d(METHOD_TAG, "Get Auth Token task was cancelled.");

        // Our task is complete, so clear it out.
        _getAuthTokenTask = null;

    }

    /**
     * Class Name: GetAuthTokenTask
     * Description: Represents an asynchronous task used to get an auth token.
     */
    private class GetAuthTokenTask extends AsyncTask<Void, Void, Bundle> {


        private Activity _activity = null;

        static final String CLASS_TAG = "GetAuthTokenTask";


        public GetAuthTokenTask(Activity a) {

            _activity = a;
        }

        @Override
        protected Bundle doInBackground(Void... params) {

            String METHOD_TAG;
            METHOD_TAG = CLASS_TAG + ".doInBackground()";

            // Get Auth Token using the AccountHelper class.
            try {

                return AccountHelper.getInstance(_context).getAuthToken(_account, AccountHelper.AUTHTOKEN_TYPE_FULL_ACCESS, null, _activity, null, null);

            } catch (Exception e) {

                Log.e(METHOD_TAG, "Failed to get Auth Token.");
                Log.e(METHOD_TAG, e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bundle result) {

            String METHOD_TAG;
            METHOD_TAG = CLASS_TAG + ".onPostExecute()";

            Log.d(METHOD_TAG, "Call to get Auth Token successful. Returning result to UI thread.");

            // Return the get Auth Token result to the Activity.
            onGetAuthTokenResult(result);
        }

        @Override
        protected void onCancelled() {

            // If the action was canceled , then call back into the
            // Activity to let it know.
            onGetAuthTokenCancel();
        }

    }

}
