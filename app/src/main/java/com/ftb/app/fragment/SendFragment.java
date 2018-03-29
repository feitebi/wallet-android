package com.ftb.app.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ftb.app.R;
import com.ftb.app.net.JSONRequest;
import com.ftb.app.util.KeyCrypto;
import com.ftb.app.util.SystemUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * create an instance of this fragment.
 */
public class SendFragment extends Fragment {

    private View rootView;//缓存Fragment view

    private EditText toAddressTxt;

    private EditText toAmountTxt;

    private SeekBar feeTxt;

    private Button sendBtn;

    private PostTask mAuthTask = null;

    private ProgressDialog progressDialog;

    private Activity activity;

    private TextView fee_tips_txt;

    @Override
    public void onAttach(Activity act) {
        // TODO Auto-generated method stub
        super.onAttach(act);
        activity = act;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_send, null);
        }

        progressDialog = SystemUtil.initProgressDialog(activity);

        fee_tips_txt = (TextView) rootView.findViewById(R.id.fee_tips_txt);

        toAddressTxt = (EditText) rootView.findViewById(R.id.send_to_address_text);
        toAmountTxt = (EditText) rootView.findViewById(R.id.send_to_amount_txt);
        feeTxt = (SeekBar) rootView.findViewById((R.id.send_seek_bar));

        feeTxt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
               // BigDecimal bd = new BigDecimal(seekBar.getProgress()).multiply(new BigDecimal(0.0021)).divide(new BigDecimal(8)).add(new BigDecimal(0.0021));
               // fee_tips_txt.setText("矿工费:" + bd.setScale(6, RoundingMode.HALF_UP) + "ETH");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sendBtn = (Button) rootView.findViewById(R.id.send_coin_btn);
        sendBtn.setClickable(false);
        sendBtn.setEnabled(false);


        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }


    private void showLoginDialog() {

        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(rootView.getContext());
        final View dialogView = LayoutInflater.from(rootView.getContext())
                .inflate(R.layout.login_dialog_layout, null);

        final EditText pwdTxt = (EditText) dialogView.findViewById(R.id.login_pwd_txt);

        normalDialog.setTitle("安全确认");
        normalDialog.setView(dialogView);
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (pwdTxt.getText().length() > 0) {
                            dialog.dismiss();

                            String toAddr = toAddressTxt.getText().toString();
                            String toAmount = toAmountTxt.getText().toString();
                            String fee = feeTxt.getProgress() + "";
                            if (toAddr.length() <= 0 || toAmount.length() <= 0) {
                                Toast.makeText(rootView.getContext(), "请输入转币地址以及数量！", Toast.LENGTH_LONG).show();
                                return;
                            }

                            mAuthTask = new PostTask(toAddr, toAmount, fee, pwdTxt.getText().toString());
                            mAuthTask.execute((Void) null);
                        } else {
                            Toast.makeText(rootView.getContext(), "钱包密码至少6位，请重新输入！", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        normalDialog.show();
    }


    private boolean checkParameters() {
        return false;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class PostTask extends AsyncTask<Void, Void, String> {
        private String addr;
        private String amt;
        private String fee;
        private String pwd;

        PostTask(String toAddr, String sendAmount, String cfee, String cpwd) {
            addr = toAddr;
            amt = sendAmount;
            fee = cfee;
            pwd = cpwd;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                JSONObject wallet = SystemUtil.getWallet();

                String privateKey = KeyCrypto.decryptPrivatekey(wallet.getString("privateKey"), pwd);

                Map<String, String> pparams = new HashMap<String, String>();
                pparams.put("address", wallet.getString("address"));
                pparams.put("privateKey", privateKey);
                pparams.put("toAddress", addr);
                pparams.put("sendAmount", amt);
                pparams.put("fee", fee);

                return JSONRequest.post("/api/assets/transfer", pparams);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String results) {
            mAuthTask = null;

            if (results == null || results.indexOf("error") >= 0) {
                Toast.makeText(rootView.getContext(), "发送失败，请检查密码和其他参数，再试！", Toast.LENGTH_LONG).show();
            } else if (results.indexOf("txHex") >= 0) {
                Toast.makeText(rootView.getContext(), "发送成功！", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            progressDialog.hide();
        }
    }
}

