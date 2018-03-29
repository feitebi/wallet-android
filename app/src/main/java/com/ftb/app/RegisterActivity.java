package com.ftb.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ftb.app.util.BitcoinWalletUtil;
import com.ftb.app.util.SystemUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers login via phone/password.
 */
public class RegisterActivity extends AppCompatActivity {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    // UI references.
    private EditText mPhoneView;
    private EditText mPasswordView;
    private PostTask mAuthTask = null;
    private Button startBtn;
    private Button importBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        String json = SystemUtil.getPreferValueBy(SystemUtil.USER_WALLET_LIST);

        if (SystemUtil.isNotBlank(json)) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        mPasswordView = (EditText) findViewById(R.id.reg_new_pwd_text);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.reg_new_pwd_text || id == EditorInfo.IME_NULL) {
                    register();
                    return true;
                }
                return false;
            }
        });

        mPasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String txt = editable.toString();
                if (txt.trim().length() >= 6) {
                    startBtn.setClickable(true);
                    startBtn.setEnabled(true);
                    startBtn.setBackgroundResource(R.color.colorButton);
                    startBtn.setTextColor(Color.WHITE);
                }else{
                    startBtn.setClickable(false);
                    startBtn.setEnabled(false);
                    startBtn.setBackgroundResource(R.color.light_gray_bg);
                    startBtn.setTextColor(Color.GRAY);
                }
            }
        });


        importBtn = (Button) findViewById(R.id.import_btn);

        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, WalletImportActivity.class);
                startActivity(intent);
            }
        });

        startBtn = (Button) findViewById(R.id.reg_start_btn);
        startBtn.setClickable(false);
        startBtn.setEnabled(false);
        startBtn.setBackgroundResource(R.color.light_gray_bg);
        startBtn.setTextColor(Color.GRAY);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid phone, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void register() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }

        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            Toast.makeText(RegisterActivity.this, "密码请至少输入6位", Toast.LENGTH_SHORT).show();
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new PostTask(password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class PostTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mPassword;

        PostTask(String password) {
            mPassword = password;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {

                String results = BitcoinWalletUtil.createWalletByPwd(mPassword);

                return new JSONObject(results);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final JSONObject wallet) {
            mAuthTask = null;

            try {
                if (wallet != null && SystemUtil.isNotBlank(wallet.getString("address"))) {
                    finish();

                    Intent intent = new Intent(RegisterActivity.this, BackupActivity.class);
                    intent.putExtra("wallet_info", wallet.toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(RegisterActivity.this, "创建钱包失败，请稍后！", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(RegisterActivity.this, "创建钱包失败，请稍后！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}

