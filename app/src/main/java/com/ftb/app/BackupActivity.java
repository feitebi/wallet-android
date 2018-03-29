package com.ftb.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ftb.app.util.KeyCrypto;
import com.ftb.app.util.SystemUtil;

import org.json.JSONObject;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;

public class BackupActivity extends AppCompatActivity {

    private AlertDialog backupDialog;

    private TextView backup_seed_txt;

    private TextView privateKey_txt;

    private JSONObject tempWallet = null;

    private Button reg_start_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        backup_seed_txt = (TextView) findViewById(R.id.backup_seed_txt);

        Button backup_private_key_btn = (Button) findViewById(R.id.backup_private_key_btn);

        backup_private_key_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });

        ImageButton backup_close_btn = (ImageButton) findViewById(R.id.backup_close_btn);

        backup_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        try {
            final JSONObject jsonObject;
            String wallet_info = getIntent().getStringExtra("wallet_info");

            boolean isFirstTime = false;

            if (SystemUtil.isNotBlank(wallet_info)) {
                tempWallet = new JSONObject(wallet_info);
                isFirstTime = true;
            } else {
                tempWallet = SystemUtil.getWallet();
                isFirstTime = false;
            }

            if (tempWallet == null) {
                Toast.makeText(BackupActivity.this, "钱包读取失败，请稍后！", Toast.LENGTH_SHORT).show();
            } else {
                TextView seedTxt = (TextView) findViewById(R.id.backup_seed_txt);
                reg_start_btn = (Button) findViewById(R.id.backup_next_btn);
                if (isFirstTime) {
                    seedTxt.setText(tempWallet.getString("seed"));
                    reg_start_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(BackupActivity.this, WalletValidActivity.class);
                            try {
                                intent.putExtra("wallet_info", tempWallet.toString());
                                startActivity(intent);
                            } catch (Exception ex) {
                                Toast.makeText(BackupActivity.this, "不能启动验证，请重启。", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    reg_start_btn.setBackgroundResource(R.color.colorButton);
                    reg_start_btn.setTextColor(Color.WHITE);
                    reg_start_btn.setEnabled(true);
                } else {
                    seedTxt.setText("您已备份种子密语，或者通过导入的钱包密语不显示。");
                    seedTxt.setClickable(false);
                    reg_start_btn.setBackgroundColor(Color.RED);
                    reg_start_btn.setTextColor(Color.WHITE);
                    reg_start_btn.setEnabled(true);
                    reg_start_btn.setText("删除本钱包");
                    reg_start_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            reg_start_btn.setEnabled(false);
                            reg_start_btn.setTextColor(Color.DKGRAY);
                            reg_start_btn.setBackgroundColor(Color.LTGRAY);
                            showDeleteConfirmDialog();
                        }
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(BackupActivity.this, "创建钱包失败，请稍后！", Toast.LENGTH_SHORT).show();
        }
    }

    private void showWarningDialog() {

        final AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(BackupActivity.this);

        customizeDialog.setTitle("重要提示");
        customizeDialog.setMessage("钱包删除仅清除APP的地址与交易记录，并不会清除您的币，您仍可以用密语或者私钥恢复钱包。");
        customizeDialog.setPositiveButton("立即删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SecuredPreferenceStore.getSharedInstance().edit().clear().commit();

                Toast.makeText(BackupActivity.this, "钱包已删除，请重新开始！", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(BackupActivity.this, RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }, 2500);
            }
        });
        customizeDialog.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        customizeDialog.show();
    }

    //eager chief live duty minute loud horse series paddle crush flee reduce
    //cQTENvH4xYzUhtG3bqo3dr6jdfpURAj3S43G31WRACiC5JVhzUJd

    AlertDialog dlg;

    private void showCustomizeDialog(String privateKey, String pwd) {
        final AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(BackupActivity.this);
        final View dialogView = LayoutInflater.from(BackupActivity.this)
                .inflate(R.layout.backup_dialog_layout, null);
        customizeDialog.setTitle("私钥备份");
        customizeDialog.setView(dialogView);

        if (tempWallet != null) {
            privateKey_txt = (TextView) dialogView.findViewById(R.id.dlg_private_key_txt);
            privateKey_txt.setText(privateKey);
            privateKey_txt.setClickable(true);
            privateKey_txt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dlg.dismiss();
                    onClickCopyPrivate(view);
                }
            });
            dlg = customizeDialog.show();
            Button copyBtn = dialogView.findViewById(R.id.copy_private_key_btn);
            copyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dlg.dismiss();
                    onClickCopyPrivate(view);
                }
            });
        } else {
            Toast.makeText(BackupActivity.this, "钱包读取失败，请稍后！", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmDialog() {

        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(BackupActivity.this);
        final View dialogView = LayoutInflater.from(BackupActivity.this)
                .inflate(R.layout.login_dialog_layout, null);

        final EditText pwdTxt = (EditText) dialogView.findViewById(R.id.login_pwd_txt);

        normalDialog.setTitle("钱包密码");
        normalDialog.setView(dialogView);
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (pwdTxt.getText().length() > 0) {
                            dialog.dismiss();
                            try {
                                String privateKey = tempWallet.getString("privateKey");
                                privateKey = KeyCrypto.decryptPrivatekey(privateKey, pwdTxt.getText().toString());

                                showWarningDialog();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                reg_start_btn.setEnabled(true);
                                reg_start_btn.setTextColor(Color.WHITE);
                                reg_start_btn.setBackgroundColor(Color.RED);
                                Toast.makeText(BackupActivity.this, "私钥解密失败，请确定密码正确。", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(BackupActivity.this, "钱包密码不能为空！", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        reg_start_btn.setEnabled(true);
                        reg_start_btn.setTextColor(Color.WHITE);
                        reg_start_btn.setBackgroundColor(Color.RED);
                    }
                });
        normalDialog.show();
    }

    private void showLoginDialog() {

        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(BackupActivity.this);
        final View dialogView = LayoutInflater.from(BackupActivity.this)
                .inflate(R.layout.login_dialog_layout, null);

        final EditText pwdTxt = (EditText) dialogView.findViewById(R.id.login_pwd_txt);

        normalDialog.setTitle("钱包密码");
        normalDialog.setView(dialogView);
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (pwdTxt.getText().length() > 0) {
                            dialog.dismiss();
                            try {
                                String privateKey = tempWallet.getString("privateKey");

                                privateKey = KeyCrypto.decryptPrivatekey(privateKey, pwdTxt.getText().toString());

                                showCustomizeDialog(privateKey, pwdTxt.getText().toString());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Toast.makeText(BackupActivity.this, "私钥解密失败，请确定密码正确。", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(BackupActivity.this, "钱包密码不能为空！", Toast.LENGTH_LONG).show();
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


    public void onClickCopy(View v) {
        if (backup_seed_txt.getText().length() > 0) {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText(null, backup_seed_txt.getText().toString()));
            Toast.makeText(this, "密语复制成功！", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "密语为空，复制失败！", Toast.LENGTH_LONG).show();
        }
    }

    public void onClickCopyPrivate(View v) {
        if (privateKey_txt.getText().length() > 0) {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText(null, privateKey_txt.getText().toString()));
            Toast.makeText(this, "私钥复制成功！", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "私钥为空，复制失败！", Toast.LENGTH_LONG).show();
        }
    }
}
