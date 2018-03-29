package com.ftb.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ftb.app.util.SystemUtil;

public class ReceiveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_recieve);

        ImageButton backup_btn = (ImageButton) findViewById(R.id.receive_close_btn);
        backup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final TextView walletAddrTxt = (TextView) findViewById(R.id.wallet_addr);

        ImageButton share_btn = (ImageButton) findViewById(R.id.receive_share_btn);
        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent textIntent = new Intent(Intent.ACTION_SEND);
                textIntent.setType("text/plain");
                textIntent.putExtra(Intent.EXTRA_TEXT, walletAddrTxt.getText());
                startActivity(Intent.createChooser(textIntent, "分享FTB接收地址"));
            }
        });

        String addr = SystemUtil.getWalletAddr();
        if (SystemUtil.isNotBlank(addr)) {
            walletAddrTxt.setText(addr);
        } else {
            share_btn.setClickable(false);
            share_btn.setEnabled(false);
            walletAddrTxt.setClickable(false);
            Toast.makeText(this, "钱包地址获取失败，请重新导入。", Toast.LENGTH_LONG).show();
        }
    }

    public void onClickCopy(View v) {

        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText(null, SystemUtil.getWalletAddr()));

        Toast.makeText(this, "钱包收币地址复制成功！", Toast.LENGTH_LONG).show();
    }
}
