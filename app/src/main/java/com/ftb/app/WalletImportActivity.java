package com.ftb.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ftb.app.util.BitcoinWalletUtil;
import com.ftb.app.util.SystemUtil;

public class WalletImportActivity extends AppCompatActivity {

    private EditText seedOrPrivateKeyText;

    private Button seedBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_import);

        seedOrPrivateKeyText = (EditText) findViewById(R.id.import_seed_text);

        ImageButton backup_close_btn = (ImageButton) findViewById(R.id.import_close_btn);

        backup_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        seedOrPrivateKeyText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String txt = editable.toString();
                if (txt.trim().length() == 51 || txt.trim().length() == 52 || txt.trim().split(" ").length == 12) {
                    seedBtn.setClickable(true);
                    seedBtn.setEnabled(true);
                    seedBtn.setBackgroundResource(R.color.colorButton);
                    seedBtn.setTextColor(Color.WHITE);
                } else {
                    seedBtn.setClickable(false);
                    seedBtn.setEnabled(false);
                    seedBtn.setBackgroundResource(R.color.light_gray_bg);
                    seedBtn.setTextColor(Color.GRAY);
                }
            }
        });

        seedOrPrivateKeyText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                String txt = seedOrPrivateKeyText.getText().toString();
                if (txt.trim().length() == 51 || txt.trim().length() == 52 || txt.trim().split(" ").length == 12) {
                    showLoginDialog();
                    return true;
                } else {
                    Toast.makeText(WalletImportActivity.this, "长度或格式不正确，请检查。", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });

        seedBtn = (Button) findViewById(R.id.import_by_btn);
        seedBtn.setClickable(false);
        seedBtn.setEnabled(false);
        seedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt = seedOrPrivateKeyText.getText().toString();
                if (txt.trim().length() == 51 || txt.trim().length() == 52 || txt.trim().split(" ").length == 12) {
                    showLoginDialog();
                }
            }
        });
    }

    private boolean startImport(String pwd) {

        String seedOrPrivateKey = seedOrPrivateKeyText.getText().toString();
        if ((seedOrPrivateKey.trim().length() == 51 || seedOrPrivateKey.trim().length() == 52 || seedOrPrivateKey.trim().split(" ").length == 12) && pwd.length() >= 6) {
            try {
                String wallet = BitcoinWalletUtil.imprtWalletBy(seedOrPrivateKey.trim(), pwd);

                SystemUtil.storeWallet(wallet);

                Toast.makeText(WalletImportActivity.this, "钱包导入成功！", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(WalletImportActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(WalletImportActivity.this, "导入失败，请检查密钥或种子密语是否正确，然后再试。", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(WalletImportActivity.this, "导入失败，请检查密钥或种子密语是否正确，然后再试。", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void showLoginDialog() {

        seedBtn.setClickable(false);
        seedBtn.setEnabled(false);
        seedBtn.setBackgroundResource(R.color.light_gray_bg);
        seedBtn.setTextColor(Color.GRAY);

        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(WalletImportActivity.this);
        final View dialogView = LayoutInflater.from(WalletImportActivity.this)
                .inflate(R.layout.login_dialog_layout, null);

        final EditText pwdTxt = (EditText) dialogView.findViewById(R.id.login_pwd_txt);

        normalDialog.setTitle("钱包加密");
        normalDialog.setView(dialogView);
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (pwdTxt.getText().length() >= 6) {
                            dialog.dismiss();
                            startImport(pwdTxt.getText().toString());
                        } else {
                            Toast.makeText(WalletImportActivity.this, "钱包密码不能为空，且至少6位！", Toast.LENGTH_LONG).show();
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

}
