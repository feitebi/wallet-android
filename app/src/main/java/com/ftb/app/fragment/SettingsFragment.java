package com.ftb.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ftb.app.BackupActivity;
import com.ftb.app.BlockNodeActivity;
import com.ftb.app.HelpActivity;
import com.ftb.app.R;
import com.ftb.app.ReceiveActivity;

import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    private View rootView;//缓存Fragment view

    private ListView listView = null;

    String[] titles = new String[]{"钱包地址", "钱包备份", "区块节点", "帮助中心", "公司网站"};
    int[] imgs = {R.drawable.arrow, R.drawable.arrow, R.drawable.arrow, R.drawable.arrow, R.drawable.arrow};

    Class[] items = {ReceiveActivity.class, BackupActivity.class, BlockNodeActivity.class, HelpActivity.class, URL.class};

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_settings, null);
        }

        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }

        listView = (ListView) rootView.findViewById(R.id.settings_list);

        //创建SimpleAdapter适配器将数据绑定到item显示控件上
        CustomList cl = new CustomList(titles, imgs);

        //实现列表的显示
        listView.setAdapter(cl);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i < items.length - 1) {
                    Intent intent = new Intent(getActivity(), items[i]);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse("https://feitebi.com");
                    intent.setData(content_url);
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    class CustomList extends BaseAdapter {

        String[] Title;
        int[] imge;

        CustomList() {
            Title = null;
            imge = null;
        }


        public CustomList(String[] text, int[] images) {
            Title = text;
            imge = images;

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
            View row = inflater.inflate(R.layout.setting_item_layout, parent, false);

            TextView title, detail;
            ImageView i1;
            title = (TextView) row.findViewById(R.id.settings_item_text);
            i1 = (ImageView) row.findViewById(R.id.settings_item_img);
            title.setText(Title[position]);
            i1.setImageResource(imge[position]);

            return (row);
        }
    }
}
