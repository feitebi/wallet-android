package com.ftb.app.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ftb.app.HistoryActivity;
import com.ftb.app.R;
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

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeLayout;

    private View rootView;//缓存Fragment view

    private WalletLoadTask mLoadTask = null;

    private ListView listView = null;

    public Context context; // 存储上下文对象
    public Activity activity; // 存储上下文对象

    private TextView walletAddrTxt;

    private TextView totalFTB_txt;

    private ProgressDialog progressDialog;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
        this.activity = activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_wallet_sum, null);
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }

        progressDialog = SystemUtil.initProgressDialog(getActivity());

        listView = (ListView) rootView.findViewById(R.id.coin_list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(activity, HistoryActivity.class);
                startActivity(intent);
            }
        });

        walletAddrTxt = rootView.findViewById(R.id.wallet_addr_text);

        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container_home);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light);
        mSwipeLayout.setDistanceToTriggerSync(400);
        mSwipeLayout.setProgressBackgroundColor(R.color.colorButton);
        mSwipeLayout.setSize(SwipeRefreshLayout.DEFAULT);

        attemptLoad();

        return rootView;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    private void attemptLoad() {
        if (mLoadTask != null) {
            return;
        }

        String walletAddr = SystemUtil.getWalletAddr();
        if (SystemUtil.isNotBlank(walletAddr)) {
            walletAddrTxt.setText(walletAddr.substring(0, 13) + "..." + walletAddr.substring(19, walletAddr.length()));
        } else {
            mLoadTask = null;
            Toast.makeText(rootView.getContext(), "钱包地址获取失败，请重新导入。", Toast.LENGTH_LONG).show();
            return;
        }

        if (SystemUtil.isNotBlank(walletAddr)) {
            progressDialog.show();
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

                String results = JSONRequest.post("/api/assets/addressinfo", pparams);

                if (SystemUtil.isNotBlank(results)) {
                    JSONObject jsonObject = new JSONObject(results);
                    JSONArray transactions = jsonObject.getJSONArray("utxos");

                    return transactions;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(final JSONArray results) {
            mLoadTask = null;

            BigDecimal totalFTB = BigDecimal.ZERO.setScale(4, RoundingMode.CEILING);

            if (results != null) {

                //获取到集合数据
                List<Map<String, String>> data = new ArrayList<Map<String, String>>();
                for (int i = 0; i < results.length(); i++) {
                    try {
                        JSONObject tran = results.getJSONObject(i);
                        JSONArray assets = tran.getJSONArray("assets");
                        Log.d("FTB-INFO", assets.toString());

                        if (assets != null && assets.length() > 0 && assets.getJSONObject(0).getDouble("amount") > 0) {
                            double amount = assets.getJSONObject(0).getDouble("amount") / 10000;

                            totalFTB = totalFTB.add(new BigDecimal(amount).setScale(4, RoundingMode.CEILING));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(rootView.getContext(), "网络延迟，请稍后再试。", Toast.LENGTH_LONG).show();
            }

            //创建SimpleAdapter适配器将数据绑定到item显示控件上
            CustomList adapter = new CustomList(new String[]{"FTB"}, new int[]{R.drawable.arrow}, new String[]{totalFTB.toPlainString()});

            //实现列表的显示
            listView.setAdapter(adapter);

            progressDialog.hide();
        }

        @Override
        protected void onCancelled() {
            mLoadTask = null;
            progressDialog.hide();
        }
    }

    class CustomList extends BaseAdapter {

        String[] Title;
        String[] amounts;
        int[] imge;

        CustomList() {
            Title = null;
            imge = null;
        }


        public CustomList(String[] text, int[] images, String[] amount) {
            Title = text;
            imge = images;
            amounts = amount;
        }


        @Override
        public int getCount() {
            return Title.length;
        }


        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View row = inflater.inflate(R.layout.sum_item_layout, parent, false);

            TextView title, amount;
            ImageView i1;
            amount = (TextView) row.findViewById(R.id.amount_txt);
            i1 = (ImageView) row.findViewById(R.id.right_arrow_img);

            amount.setText(amounts[position]);
            i1.setImageResource(imge[position]);

            return (row);
        }
    }
}
