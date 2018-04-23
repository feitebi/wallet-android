package com.wallet.crypto.ftb.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chanven.lib.cptr.PtrClassicFrameLayout;
import com.chanven.lib.cptr.PtrDefaultHandler;
import com.chanven.lib.cptr.PtrFrameLayout;
import com.wallet.crypto.ftb.R;
import com.wallet.crypto.ftb.net.AESUtil;
import com.wallet.crypto.ftb.publicapi.ApiPublic;
import com.wallet.crypto.ftb.publicapi.BaseResponsCallBack;
import com.wallet.crypto.ftb.publicapi.publicbean.AirDropListBean;
import com.wallet.crypto.ftb.publicapi.publicbean.CandyBean;
import com.wallet.crypto.ftb.publicapi.publicbean.TokenListBean;
import com.wallet.crypto.ftb.publicapi.publicbean.UserDetailBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CandyActivity extends AppCompatActivity {

    private ListView mListView;
    // 声明数组链表，其装载的类型是ListItem(封装了一个Drawable和一个String的类)
    private ArrayList<ListItem> mList = new ArrayList<ListItem>();
    private TextView amount;
    private MainListViewAdapter adapter;
    private HorizontalScrollView raisedLayout;
    private HorizontalScrollView receivedLayout;
    private Button rulerBtn;
    private String toAddr = "";
    private String imei = "";
    private PtrClassicFrameLayout mPtrFrame;

    private ProgressDialog progressDialog;

    private int loadingCount = 0;


    public static ProgressDialog initProgressDialog(Activity activity) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(false);//循环滚动
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("加载中...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    /**
     * Acitivity的入口方法
     */
    @SuppressLint({"WrongViewCast", "MissingPermission"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.candy_activity_main);

        ImageButton refreshBtn = (ImageButton) findViewById(R.id.candy_close_btn);
        refreshBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            imei = tm.getDeviceId();
        } catch (Exception ex) {
            imei = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
        }

        progressDialog = initProgressDialog(CandyActivity.this);

        toAddr = getIntent().getStringExtra("wallet_addr");

        // 通过findviewByID获取到ListView对象
        mListView = findViewById(R.id.listView1);
        View headView = View.inflate(this, R.layout.candy_header_top, null);
        amount = headView.findViewById(R.id.amount);
        rulerBtn = headView.findViewById(R.id.ruler);
        mListView.addHeaderView(headView, null, false);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(CandyActivity.this, CandyDetailActivity.class);
                intent.putExtra("tokenId", mList.get(position - 1).getTokenId());
                intent.putExtra("symbol", mList.get(position - 1).getTitle());
                intent.putExtra("IM", imei);
                intent.putExtra("toAddr", toAddr);
                startActivity(intent);

            }
        });
        rulerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        mPtrFrame = findViewById(R.id.chanven);
        //下拉刷新支持时间
        mPtrFrame.setLastUpdateTimeRelateObject(this);
        //下拉刷新一些设置 详情参考文档
        mPtrFrame.setResistance(1.7f);
        mPtrFrame.setRatioOfHeaderHeightToRefresh(1.2f);
        mPtrFrame.setDurationToClose(100);
        mPtrFrame.setDurationToCloseHeader(500);
        // default is false
        mPtrFrame.setPullToRefresh(false);
        // default is true
        mPtrFrame.setKeepHeaderWhenRefresh(true);


        //下拉刷新
        mPtrFrame.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {//检查是否能够刷新
                return super.checkCanDoRefresh(frame, content, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mPtrFrame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refresh();//刷新数据并更新UI,可换成数据处理的相关操作
                        mPtrFrame.refreshComplete();//发出刷新操作完成的信号
                    }

                    private void refresh() {
                        try {
                            loadingCount = 0;
                            if (!progressDialog.isShowing()) {
                                progressDialog.show();
                            }

                            userDate(toAddr);
                            tokenList(toAddr, "0", "100");
                            airDropList(toAddr);

                        } catch (Exception ex) {
                            if (progressDialog.isShowing()) {
                                progressDialog.hide();
                                loadingCount = 0;
                            }
                        }
                    }
                }, 50);

            }
        });

        raisedLayout = findViewById(R.id.receivedLayout);

        receivedLayout = findViewById(R.id.raisedLayout);

        // 获取MainListAdapter对象
        adapter = new MainListViewAdapter(mList);
        mListView.setAdapter(adapter);

        try {
            loadingCount = 0;
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
            userDate(toAddr);
            tokenList(toAddr, "0", "100");
            airDropList(toAddr);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (progressDialog.isShowing()) {
                progressDialog.hide();
                loadingCount = 0;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.get) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.candy_activity_main_menu, menu);
        return true;
    }

    /**
     * 定义ListView适配器MainListViewAdapter
     */
    class MainListViewAdapter extends BaseAdapter {
        private ArrayList<ListItem> adapterList;

        public MainListViewAdapter(ArrayList<ListItem> mList) {
            this.adapterList = mList;
        }

        public void setAdapterList(ArrayList<ListItem> mList) {
            this.adapterList = mList;
        }

        public List<ListItem> getAdapterList() {
            return this.adapterList;
        }

        @Override
        public int getCount() {
            return adapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return adapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ListItemView listItemView;

            // 初始化item view
            if (convertView == null) {
                // 通过LayoutInflater将xml中定义的视图实例化到一个View中
                convertView = LayoutInflater.from(CandyActivity.this).inflate(
                        R.layout.candy_items, null);

                // 实例化一个封装类ListItemView，并实例化它的两个域
                listItemView = new ListItemView();
                listItemView.imageView = convertView
                        .findViewById(R.id.image);
                listItemView.title = convertView
                        .findViewById(R.id.title);

                listItemView.count = convertView
                        .findViewById(R.id.data);
                listItemView.getBtn = convertView
                        .findViewById(R.id.get);
                // 将ListItemView对象传递给convertView
                convertView.setTag(listItemView);
            } else {
                // 从converView中获取ListItemView对象
                listItemView = (ListItemView) convertView.getTag();
            }

            String url = "http://feitebi.cn/icons/" + adapterList.get(position).getTokenId() + ".png";
            Glide.with(getBaseContext()).
                    load(url).
                    asBitmap(). //强制处理为bitmap
                    into(listItemView.imageView);

            // 获取到mList中指定索引位置的资源
            String title = adapterList.get(position).getTitle();
            String data = adapterList.get(position).getCount();

            // 将资源传递给ListItemView的两个域对象
            listItemView.title.setText(title);
            listItemView.count.setText(data);

            String drawFlag = adapterList.get(position).getDrawFlag();
            if (Integer.parseInt(drawFlag) > 0) {
                listItemView.getBtn.setEnabled(true);
                listItemView.getBtn.setBackgroundResource(R.color.colorPrimary);
            } else {
                listItemView.getBtn.setEnabled(false);
                listItemView.getBtn.setBackgroundColor(Color.LTGRAY);
            }

            listItemView.getBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    String symbol = adapterList.get(position).getTitle();
                    String tokenId = adapterList.get(position).getTokenId();
                    String timestamp = "" + System.currentTimeMillis();

                    getCandy(toAddr, symbol, tokenId, imei, timestamp, position);
                }
            });

            // 返回convertView对象
            return convertView;
        }
    }

    /**
     * 封装两个视图组件的类
     */
    class ListItemView {
        ImageView imageView;
        TextView title;
        TextView introduce;
        TextView count;
        Button getBtn;
    }

    /**
     * 封装了两个资源的类
     */
    class ListItem {
        private String tokenId;
        private String title;
        private String count;
        private String drawFlag;

        public String getDrawFlag() {
            return drawFlag;
        }

        public void setDrawFlag(String drawFlag) {
            this.drawFlag = drawFlag;
        }

        public String getTokenId() {
            return tokenId;
        }

        public void setTokenId(String tokenId) {
            this.tokenId = tokenId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String data) {
            this.count = data;
        }
    }


    public View createImage(String tokenId, String tokenAmount) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.setPadding(10, 5, 10, 5);
        linearLayout.setGravity(Gravity.CENTER);

        ImageView circleImageView = new ImageView(this);
        circleImageView.setLayoutParams(new LinearLayout.LayoutParams(96, 96));

        String url = "http://feitebi.cn/icons/" + tokenId + ".png";
        Glide.with(getBaseContext()).
                load(url).
                asBitmap(). //强制处理为bitmap
                into(circleImageView);
        linearLayout.addView(circleImageView);
        TextView textView = new TextView(this);
        textView = new TextView(this);

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);

        textView.setText(tokenAmount);
        textView.setGravity(Gravity.CENTER);
        linearLayout.setTag(textView.getText());
        linearLayout.addView(textView);

        return linearLayout;
    }

/*
    private class TokenListTask extends AsyncTask<Void, Void, JSONObject> {

        private String toAddr;
        private String start;
        private String limit;

        TokenListTask(String toAddr, String start, String limit) {
            this.toAddr = toAddr;
            this.start = start;
            this.limit = limit;
        }


        @Override
        protected JSONObject doInBackground(Void... voids) {

            String url = "http://feitebi.cn/rest/token/open_list";
            try {
                Map<String, String> map = new HashMap<String, String>();
                map.put("toAddr", toAddr);
                map.put("start", start);
                map.put("limit", limit);
                String json = JSONRequest.doPost(map, url);
                return new JSONObject(json);
            } catch (Exception ex) {
            }
            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            ListItem item;
            ArrayList<ListItem> OpenList = new ArrayList<ListItem>();
            JSONArray result = null;
            try {
                result = json.getJSONObject("entity").getJSONArray("dataList");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (result != null) {
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject jsonObject = (JSONObject) result.get(i);
                        item = new ListItem();
                        item.setTokenId(jsonObject.optString("id"));
                        item.setTitle(jsonObject.optString("symbol"));
                        item.setDrawFlag(jsonObject.optString("drawFlag"));
                        item.setCount("满" + jsonObject.optString("withdrawStart") + "自动提现");
                        OpenList.add(item);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            //adapter = new MainListViewAdapter(OpenList);
            //mListView.setAdapter(adapter);

            adapter.setAdapterList(OpenList);
            adapter.notifyDataSetChanged();

            mList.clear();
            mList.addAll(OpenList);

            loadingCount++;

            if (progressDialog.isShowing() && loadingCount >= 2) {
                progressDialog.hide();
                loadingCount = 0;
            }
        }
    }


    private class UserDetailTask extends AsyncTask<Void, Void, JSONObject> {

        private String toAddr;

        UserDetailTask(String toAddr) {
            this.toAddr = toAddr;
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {

            String url = "http://feitebi.cn/rest/token/user/summary";
            try {
                Map<String, String> map = new HashMap<String, String>();
                map.put("toAddr", toAddr);
                String json = JSONRequest.doPost(map, url);
                return new JSONObject(json);
            } catch (Exception ex) {
            }
            return new JSONObject();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(JSONObject json) {
            JSONObject result = null;
            try {
                result = json.getJSONObject("entity");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String count = result.optString("airdropedSum", "0");
            amount.setText(count);

            loadingCount++;

            if (progressDialog.isShowing() && loadingCount >= 2) {
                progressDialog.hide();
                loadingCount = 0;
            }
        }
    }

    private class AirDropListTask extends AsyncTask<Void, Void, JSONObject> {

        private String toAddr;

        AirDropListTask(String toAddr) {
            this.toAddr = toAddr;
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            String url = "http://feitebi.cn/rest/token/airdrop_list";
            try {
                Map<String, String> map = new HashMap<String, String>();
                map.put("toAddr", toAddr);
                String json = JSONRequest.doPost(map, url);
                return new JSONObject(json);
            } catch (Exception ex) {
            }
            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            JSONArray waitingAirdropList = null;
            JSONArray withdrawAirdropList = null;
            try {
                waitingAirdropList = json.getJSONObject("entity").getJSONArray("waitingAirdropList");
                withdrawAirdropList = json.getJSONObject("entity").getJSONArray("withdrawAirdropList");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            LinearLayout withdrawLayout = new LinearLayout(getBaseContext());
            withdrawLayout.setOrientation(LinearLayout.HORIZONTAL);

            if (withdrawAirdropList.length() <= 0) {
                TextView defaultText = new TextView(getBaseContext());
                defaultText.setText("暂无已提现糖果");
                defaultText.setTextColor(Color.LTGRAY);
                defaultText.setTextSize(14);
                defaultText.setPadding(16,8,0,0);
                withdrawLayout.addView(defaultText);
            } else {
                for (int i = 0; i < withdrawAirdropList.length(); i++) {
                    try {
                        withdrawLayout.addView(createImage((JSONObject) withdrawAirdropList.get(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            receivedLayout.removeAllViews();
            receivedLayout.addView(withdrawLayout);

            LinearLayout waitingLayout = new LinearLayout(getBaseContext());
            waitingLayout.setOrientation(LinearLayout.HORIZONTAL);

            if (waitingAirdropList.length() <= 0) {
                TextView defaultText = new TextView(getBaseContext());
                defaultText.setText("暂无已领取糖果");
                defaultText.setTextColor(Color.LTGRAY);
                defaultText.setTextSize(14);
                defaultText.setPadding(16,8,0,0);
                waitingLayout.addView(defaultText);
            } else {
                for (int i = 0; i < waitingAirdropList.length(); i++) {
                    try {
                        waitingLayout.addView(createImage((JSONObject) waitingAirdropList.get(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            raisedLayout.removeAllViews();
            raisedLayout.addView(waitingLayout);

            loadingCount++;

            if (progressDialog.isShowing() && loadingCount >= 2) {
                progressDialog.hide();
                loadingCount = 0;
            }
        }
    }


    private class GetCandyTask extends AsyncTask<Void, Void, JSONObject> {
        private String toAddr;
        private String imei;
        private String symbol;
        private String tokenId;
        private String timestamp;

        private int position;

        public GetCandyTask(String toAddr, String imei, String symbol, String tokenId, String timestamp, int position) {
            this.toAddr = toAddr;
            this.imei = imei;
            this.symbol = symbol;
            this.tokenId = tokenId;
            this.timestamp = timestamp;
            this.position = position;
        }

        @SuppressLint({"HardwareIds", "MissingPermission"})
        @Override
        protected JSONObject doInBackground(Void... voids) {

            String url = "http://feitebi.cn/rest/token/get";
            try {

                String enParams = "imei" + imei + "symbol" + symbol + "timestamp" + timestamp + "toAddr" + toAddr
                        + "tokenId" + tokenId;

                String sign = AESUtil.encrypt(enParams);

                Map<String, String> map = new HashMap<String, String>();
                map.put("toAddr", toAddr);
                map.put("symbol", symbol);
                map.put("tokenId", tokenId);
                map.put("imei", imei);
                map.put("timestamp", timestamp);
                map.put("sign", sign);
                String json = JSONRequest.doPost(map, url);

                return new JSONObject(json);
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(getApplicationContext(), "领取失败，无FTB或者已领取。", Toast.LENGTH_LONG).show();
            }
            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            JSONObject result = null;
            try {
                result = (JSONObject) json.get("entity");
                String amt = result.optString("otherCode", "0");
                String flag = result.optString("flag", "0");
                if (Integer.parseInt(flag) > 0) {
                    Toast.makeText(getApplicationContext(), "成功领取 " + amt + " " + symbol, Toast.LENGTH_LONG).show();

                    List<ListItem> oldList = adapter.getAdapterList();
                    if (position >= 0 && oldList.size() > position) {
                        ListItem item = oldList.get(position);
                        item.setDrawFlag("-1");
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "领取失败，无FTB或者已领取。", Toast.LENGTH_LONG).show();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(getApplicationContext(), "领取失败，无FTB或者已领取。", Toast.LENGTH_LONG).show();
            }
        }
    }*/

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("糖果领取协议：\n" +
                "\n" +
                "本糖果中心的糖果为第三方用户免费投放，本平台不对糖果的投资、" +
                "价格、技术以及政策做任何担保或承诺，用户领取皆为自由行为。\n" +
                "\n" +
                "本平台同时对糖果的发放、下架等规则保留最终解释权。\n\n一旦领取表示您已接受本协议！\n");
        builder.setPositiveButton("知晓并接受",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    //下载用户

    public void userDate(String toAddr) {
        ApiPublic.getDefault(1).userDate(toAddr).enqueue(new BaseResponsCallBack<UserDetailBean>() {
            @Override
            public void onResponse(UserDetailBean result) {
                amount.setText(result.getAirdropedSum());

                loadingCount++;

                if (progressDialog.isShowing() && loadingCount >= 2) {
                    progressDialog.hide();
                    loadingCount = 0;
                }
            }
        });
    }

    public void tokenList(String toAddr, String start, String limit) {
        ApiPublic.getDefault(1).tokenList(toAddr, start, limit).enqueue(new BaseResponsCallBack<TokenListBean>() {
            @Override
            public void onResponse(TokenListBean result) {
                ListItem item;
                ArrayList<ListItem> OpenList = new ArrayList<ListItem>();
                ArrayList<TokenListBean.DataList> datalist = (ArrayList<TokenListBean.DataList>) result.getDataList();
                if (datalist != null) {
                    for (int i = 0; i < datalist.size(); i++) {
                        TokenListBean.DataList itemBean = datalist.get(i);
                        item = new ListItem();
                        item.setTokenId(itemBean.getId());
                        item.setTitle(itemBean.getSymbol());
                        item.setDrawFlag(itemBean.getDrawFlag());
                        item.setCount("满" + itemBean.getWithdrawStart() + "自动提现");
                        OpenList.add(item);
                    }
                }

                //adapter = new MainListViewAdapter(OpenList);
                //mListView.setAdapter(adapter);

                adapter.setAdapterList(OpenList);
                adapter.notifyDataSetChanged();

                mList.clear();
                mList.addAll(OpenList);

                loadingCount++;

                if (progressDialog.isShowing() && loadingCount >= 2) {
                    progressDialog.hide();
                    loadingCount = 0;
                }
            }
        });
    }

    public void getCandy(String toAddr, String symbol, String tokenId, String imei, String timestamp, int position) {

        String enParams = "imei" + imei + "symbol" + symbol + "timestamp" + timestamp + "toAddr" + toAddr
                + "tokenId" + tokenId;

        String sign = AESUtil.encrypt(enParams);

        ApiPublic.getDefault(1).candy(toAddr, symbol, tokenId, imei, timestamp, sign).enqueue(new BaseResponsCallBack<CandyBean>() {
            @Override
            public void onResponse(CandyBean result) {

                if (result != null) {
                    String amt = result.getOtherCode();
                    String flag = result.getFlag();
                    if (Integer.parseInt(flag) > 0) {
                        Toast.makeText(getApplicationContext(), "成功领取 " + amt + " " + symbol, Toast.LENGTH_LONG).show();

                        List<ListItem> oldList = adapter.getAdapterList();
                        if (position >= 0 && oldList.size() > position) {
                            ListItem item = oldList.get(position);
                            item.setDrawFlag("-1");
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "领取失败，无FTB或者已领取。", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "领取失败，无FTB或者已领取。", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void airDropList(String toAddr) {

        ApiPublic.getDefault(1).airDroplist(toAddr).enqueue(new BaseResponsCallBack<AirDropListBean>() {
            @Override
            public void onResponse(AirDropListBean result) {
                ArrayList<AirDropListBean.WaitingAirdropList> waitingAirdropList = (ArrayList<AirDropListBean.WaitingAirdropList>) result.getWaitingAirdropList();
                ArrayList<AirDropListBean.WithdrawAirdropList> withdrawAirdropList = (ArrayList<AirDropListBean.WithdrawAirdropList>) result.getWithdrawAirdropList();


                LinearLayout withdrawLayout = new LinearLayout(getBaseContext());
                withdrawLayout.setOrientation(LinearLayout.HORIZONTAL);

                if (withdrawAirdropList == null || withdrawAirdropList.size() <= 0) {
                    TextView defaultText = new TextView(getBaseContext());
                    defaultText.setText("暂无已提现糖果");
                    defaultText.setTextColor(Color.LTGRAY);
                    defaultText.setTextSize(14);
                    defaultText.setPadding(16, 8, 0, 0);
                    withdrawLayout.addView(defaultText);
                } else {
                    for (int i = 0; i < withdrawAirdropList.size(); i++) {
                        withdrawLayout.addView(createImage(withdrawAirdropList.get(i).getTokenId(), withdrawAirdropList.get(i).getTokenAmount()));
                    }
                }
                receivedLayout.removeAllViews();
                receivedLayout.addView(withdrawLayout);

                LinearLayout waitingLayout = new LinearLayout(getBaseContext());
                waitingLayout.setOrientation(LinearLayout.HORIZONTAL);

                if (waitingAirdropList == null || waitingAirdropList.size() <= 0) {
                    TextView defaultText = new TextView(getBaseContext());
                    defaultText.setText("暂无已领取糖果");
                    defaultText.setTextColor(Color.LTGRAY);
                    defaultText.setTextSize(14);
                    defaultText.setPadding(16, 8, 0, 0);
                    waitingLayout.addView(defaultText);
                } else {
                    for (int i = 0; i < waitingAirdropList.size(); i++) {
                        waitingLayout.addView(createImage(waitingAirdropList.get(i).getTokenId(), waitingAirdropList.get(i).getTokenAmount()));
                    }
                }

                raisedLayout.removeAllViews();
                raisedLayout.addView(waitingLayout);

                loadingCount++;

                if (progressDialog.isShowing() && loadingCount >= 2) {
                    progressDialog.hide();
                    loadingCount = 0;
                }
            }
        });

    }
}

