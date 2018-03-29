package com.ftb.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.ftb.app.util.UpdateManager;


public class MainActivity extends FragmentActivity implements OnTabChangeListener {

    private FragmentTabHost tabHost;

    private UpdateManager updateManager;

    private String downloadURI = "https://feitebi.com/token/downloads.ad.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabHost = (FragmentTabHost) super.findViewById(android.R.id.tabhost);
        tabHost.setup(this, super.getSupportFragmentManager()
                , R.id.contentLayout);
        tabHost.getTabWidget().setDividerDrawable(null);
        tabHost.setOnTabChangedListener(this);

        initTab();

        updateManager = new UpdateManager(MainActivity.this);

        mAuthTask = new PostTask();
        mAuthTask.execute((Void) null);
    }

    private void initTab() {
        String tabs[] = TabDataList.getTabsTxt();
        for (int i = 0; i < tabs.length; i++) {
            TabSpec tabSpec = tabHost.newTabSpec(tabs[i]).setIndicator(getTabView(i));
            tabHost.addTab(tabSpec, TabDataList.getFragments()[i], null);
            tabHost.setTag(i);
        }
    }

    private View getTabView(int idx) {
        View view = LayoutInflater.from(this).inflate(R.layout.footer_tabs, null);
        ((TextView) view.findViewById(R.id.tvTab)).setText(TabDataList.getTabsTxt()[idx]);
        if (idx == 0) {
            ((TextView) view.findViewById(R.id.tvTab)).setTextColor(getResources().getColor(R.color.colorButton));
            ((ImageView) view.findViewById(R.id.ivImg)).setImageResource(TabDataList.getTabsImgLight()[idx]);
        } else {
            ((ImageView) view.findViewById(R.id.ivImg)).setImageResource(TabDataList.getTabsImg()[idx]);
        }
        return view;
    }

    @Override
    public void onTabChanged(String tabId) {
        updateTab();

    }

    private void updateTab() {
        TabWidget tabw = tabHost.getTabWidget();
        for (int i = 0; i < tabw.getChildCount(); i++) {
            View view = tabw.getChildAt(i);
            ImageView iv = (ImageView) view.findViewById(R.id.ivImg);
            if (i == tabHost.getCurrentTab()) {
                ((TextView) view.findViewById(R.id.tvTab)).setTextColor(getResources().getColor(R.color.colorButton));
                iv.setImageResource(TabDataList.getTabsImgLight()[i]);
            } else {
                ((TextView) view.findViewById(R.id.tvTab)).setTextColor(Color.DKGRAY);
                iv.setImageResource(TabDataList.getTabsImg()[i]);
            }
        }
    }

    private PostTask mAuthTask = null;

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class PostTask extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Void... params) {
            try {

                return updateManager.isUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean isUpdate) {
            mAuthTask = null;

            try {
                if (isUpdate) {
                    showNoticeDialog();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }


    /**
     * 显示软件更新对话框
     */
    public void showNoticeDialog() {

        // 构造对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.soft_update_title);
        builder.setMessage(R.string.soft_update_info);
        // 更新
        builder.setPositiveButton(R.string.soft_update_updatebtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // 显示下载对话框
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(downloadURI);
                intent.setData(content_url);
                startActivity(intent);
            }
        });
        // 稍后更新
        builder.setNegativeButton(R.string.soft_update_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }
}
