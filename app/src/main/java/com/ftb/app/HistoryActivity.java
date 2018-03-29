package com.ftb.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ftb.app.net.JSONRequest;
import com.ftb.app.util.SystemUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeLayout;

    /*
    * 监听器SwipeRefreshLayout.OnRefreshListener中的方法，当下拉刷新后触发
    */
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 停止刷新
                mSwipeLayout.setRefreshing(false);
                attemptLoad();
            }
        }, 2000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_wallet);

        ImageButton backup_close_btn = (ImageButton) findViewById(R.id.history_close_btn);

        backup_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        progressDialog = SystemUtil.initProgressDialog(HistoryActivity.this);

        progressDialog.show();

        listView = (ListView) findViewById(R.id.trans_list);

        totalFTB_txt = (TextView) findViewById(R.id.totalFTB_txt);

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container_history);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light);
        mSwipeLayout.setDistanceToTriggerSync(400);
        mSwipeLayout.setProgressBackgroundColor(R.color.colorButton);
        mSwipeLayout.setSize(SwipeRefreshLayout.DEFAULT);

        attemptLoad();

    }

    private View rootView;//缓存Fragment view

    private WalletLoadTask mLoadTask = null;

    private ListView listView = null;

    public Context context; // 存储上下文对象
    public Activity activity; // 存储上下文对象

    private TextView totalFTB_txt;

    private ProgressDialog progressDialog;

    private void attemptLoad() {
        if (mLoadTask != null) {
            return;
        }

        String walletAddr = SystemUtil.getWalletAddr();

        if (SystemUtil.isNotBlank(walletAddr)) {
            mLoadTask = new WalletLoadTask(walletAddr);
            mLoadTask.execute((Void) null);
        }
    }


    /**
     */
    public class WalletLoadTask extends AsyncTask<Void, Void, JSONArray> {

        private final String mWalletAddr;

        WalletLoadTask(String walletAddr) {
            mWalletAddr = walletAddr;
        }

        @Override
        protected JSONArray doInBackground(Void... params) {
            try {
                Map<String, String> pparams = new HashMap<String, String>();
                pparams.put("address", mWalletAddr);
                String results = JSONRequest.post("/api/address/transactions", pparams);

                if (SystemUtil.isNotBlank(results)) {
                    JSONObject jsonObject = new JSONObject(results);
                    JSONArray transactions = jsonObject.getJSONArray("transactions");

                    return transactions;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }

        //arrange critic umbrella spare puppy office bicycle camp brave trophy dune payment

        @Override
        protected void onPostExecute(final JSONArray results) {
            mLoadTask = null;

            BigDecimal totalFTB = BigDecimal.ZERO.setScale(4, RoundingMode.CEILING);

            //获取到集合数据
            List<Map<String, String>> data = new ArrayList<Map<String, String>>();

            if (results != null) {
                for (int i = 0; i < results.length(); i++) {
                    try {
                        JSONObject tran = results.getJSONObject(i);

                        if (tran.getBoolean("colored") && tran.getJSONArray("vout") != null) {
                            JSONArray vouts = tran.getJSONArray("vout");
                            JSONObject vout0 = vouts.getJSONObject(0);
                            JSONArray assets = vout0.getJSONArray("assets");
                            Log.d("FTB-INFO", assets.toString());

                            if (vout0 != null && assets.length() > 0 && assets.getJSONObject(0).getDouble("amount") > 0) {
                                double amount = assets.getJSONObject(0).getDouble("amount") / 10000;

                                String addrN = vout0.getJSONObject("scriptPubKey").getJSONArray("addresses").get(0).toString();

                                Map<String, String> item = new HashMap<String, String>();
                                if (addrN.equalsIgnoreCase(mWalletAddr)) {
                                    item.put("type", "接收");
                                    totalFTB = totalFTB.add(new BigDecimal(amount).setScale(4, RoundingMode.CEILING));
                                } else {
                                    item.put("type", "发送");
                                    totalFTB = totalFTB.subtract(new BigDecimal(amount).setScale(4, RoundingMode.CEILING));
                                }
                                item.put("name", addrN);
                                item.put("amount", amount + "");
                                data.add(item);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(rootView.getContext(), "网络延迟，请稍后再试。", Toast.LENGTH_LONG).show();
            }

            totalFTB_txt.setText(totalFTB.toPlainString() + " FTB");

            //创建SimpleAdapter适配器将数据绑定到item显示控件上
            SimpleAdapter adapter = new setColorAdapter(HistoryActivity.this, data, R.layout.list_item,
                    new String[]{"type", "name", "amount"}, new int[]{R.id.transType, R.id.wallet_addr, R.id.trans_qty});

            //实现列表的显示
            listView.setAdapter(adapter);

            progressDialog.hide();
        }

        @Override
        protected void onCancelled() {
            mLoadTask = null;
        }
    }

    public class setColorAdapter extends SimpleAdapter {
        List<? extends Map<String, ?>> mdata;

        public setColorAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from,
                               int[] to) {
            super(context, data, resource, from, to);
            this.mdata = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LinearLayout.inflate(getBaseContext(), R.layout.list_item, null);
            }
            //这个TextView是R.layout.list_item里面的，修改这个字体的颜色
            TextView textView = (TextView) convertView.findViewById(R.id.transType);

            //获取每次进来时 mData里面存的值  若果相同则变颜色
            //根据Key值取出装入的数据，然后进行比较

            String ss = (String) mdata.get(position).get("type");
            if (ss.equals("发送")) {
                textView.setTextColor(Color.RED);
            } else if (ss.equals("接收")) {
                textView.setTextColor(Color.parseColor("#228B22"));
            }
            //Log.i("TAG", Integer.toString(position));
            //Log.i("TAG", (String) mData.get(position).get("text"));
            return super.getView(position, convertView, parent);
        }
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1))
                + 15;
        listView.setLayoutParams(params);
    }

    class transaction {

        public String addr;
        public String amount;

        public String getAddr() {
            return addr;
        }

        public String getAmount() {
            return amount;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }
    }
}
