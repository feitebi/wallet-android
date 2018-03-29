package com.ftb.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ftb.app.layout.FlowLayout;
import com.ftb.app.util.SystemUtil;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by mangoo on 2017/9/3.
 */

public class WalletValidActivity extends AppCompatActivity {

    FlowLayout flowLayout;

    int length;

    private TextView wordsList;

    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.validate_wallet_layout);

        flowLayout = (FlowLayout) findViewById(R.id.flowlayout);

        wordsList = (TextView) findViewById(R.id.words_list_tx);

        startBtn = (Button) findViewById(R.id.wallet_backup_validate_btn);
        startBtn.setClickable(false);
        startBtn.setEnabled(false);
        startBtn.setBackgroundResource(R.color.light_gray_bg);
        startBtn.setTextColor(Color.DKGRAY);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WalletValidActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ImageButton backup_close_btn = (ImageButton) findViewById(R.id.backup_valid_close_btn);

        backup_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        final String wallet_info = getIntent().getStringExtra("wallet_info");
        final String walletSeed;
        final JSONObject wallet;
        if (SystemUtil.isNotBlank(wallet_info)) {
            try {
                wallet = new JSONObject(wallet_info);
                walletSeed = wallet.getString("seed");
            } catch (Exception ex) {
                finish();
                Toast.makeText(WalletValidActivity.this, "钱包地址创建失败，请重试", Toast.LENGTH_LONG).show();
                return;
            }

            List<String> seedList = Arrays.asList(walletSeed.split(" "));
            Collections.shuffle(seedList);
            length = seedList.size();
            wordsList.setText("");

            for (int i = 0; i < seedList.size(); i++) {
                int ranHeight = dip2px(this, 30);
                ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ranHeight);
                lp.setMargins(dip2px(this, 10), 0, dip2px(this, 10), 15);
                TextView tv = new TextView(this);
                tv.setPadding(dip2px(this, 15), 0, dip2px(this, 15), 0);
                tv.setTextColor(Color.parseColor("#FF3030"));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                tv.setText(seedList.get(i));
                tv.setGravity(Gravity.CENTER_VERTICAL);
                tv.setLines(3);
                tv.setBackgroundResource(R.drawable.bg_tag);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        wordsList.append(((TextView) view).getText() + " ");
                        flowLayout.removeView(view);
                        flowLayout.relayoutToCompress();
                        if (flowLayout.getChildCount() <= 0) {
                            TextView textView = new TextView(view.getContext());
                            textView.setTextSize(16);
                            textView.setGravity(Gravity.CENTER_HORIZONTAL);
                            textView.setWidth(flowLayout.getWidth());
                            flowLayout.addView(textView);

                            if (wordsList.getText().toString().trim().equals(walletSeed)) {
                                textView.setText("密语备份正确！\n\n系统不会保存您的种子密语，且仅本次显示。请妥善保管！");
                                try {
                                    SystemUtil.storeWallet(wallet_info);

                                    startBtn.setClickable(true);
                                    startBtn.setEnabled(true);
                                    startBtn.setBackgroundResource(R.color.colorButton);
                                    startBtn.setTextColor(Color.WHITE);

                                } catch (Exception ex) {
                                    Toast.makeText(WalletValidActivity.this, "保存钱包失败，请确保手机有剩余空间并有写入权限，再操作一次", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                textView.setText("很遗憾，密语顺序不正确，\n请返回重新抄写，并重新按顺序点击。");
                            }
                        }
                    }
                });
                flowLayout.addView(tv, lp);
            }
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
