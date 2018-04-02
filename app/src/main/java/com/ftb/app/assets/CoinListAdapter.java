package com.wallet.crypto.ftb.assets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wallet.crypto.ftb.R;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by mangoo on 2017/9/15.
 */

public class CoinListAdapter extends BaseAdapter {


    private Context context = null;

    private LayoutInflater inflater = null;
    private List<BeanList> lists;


    public CoinListAdapter(List list, Context context) {
        this.lists = list;
        this.context = context;
        // 布局装载器对象
        inflater = LayoutInflater.from(context);
    }



    // 适配器中数据集中数据的个数
    @Override
    public int getCount() {
        return lists.size();
    }

    // 获取数据集中与指定索引对应的数据项
    @Override
    public Object getItem(int position) {
        return lists.get(position);

    }

    // 获取指定行对应的ID
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 获取每一个Item显示的内容
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BeanList beanList=lists.get(position);
        View view = inflater.inflate(R.layout.coin_list, null);

        ImageView symbolImg = view.findViewById(R.id.symbolImg);
        TextView symbol = view.findViewById(R.id.symbol);
        TextView price = view.findViewById(R.id.price);
        TextView count = view.findViewById(R.id.count);
        TextView total = view.findViewById(R.id.total);
        TextView radio = view.findViewById(R.id.ratio);
        String Img = beanList.getSymbel().toLowerCase();
        String symbolText = beanList.getSymbel().toUpperCase();
        String url = "http://feitebi.cn/icons/" +Img+ ".png";
        Glide.with(context).
                load(url).
                asBitmap(). //强制处理为bitmap
                into(symbolImg);

        float price01;
        if (beanList.getPrice().equals("")) {
            price01=0;
        }else{
            price01 = Float.parseFloat(beanList.getPrice());
        }
        BigDecimal BD  =   new BigDecimal(price01);
        String coinPrice = String.valueOf(BD.setScale(4,BigDecimal.ROUND_HALF_UP).floatValue());


        float count01 = Float.parseFloat(beanList.getCount());
        String total01 = String.valueOf(count01*price01);

        symbol.setText(symbolText);



        count.setText(initText(beanList.getCount()));

        if(beanList.getPrice().equals("")) {
            price.setText("-");
        }else{
            price.setText("￥"+coinPrice);
        }
        total.setText("￥"+total01);
        if(beanList.getRadio().equals("") ||beanList.getRadio().equals("0")) {
            radio.setText("("+"-"+")");
        }else{
            radio.setText("("+beanList.getRadio()+"%"+")");
        }
        return view;
    }

    public List<BeanList> getLists() {
        return lists;
    }

    public String  initText(String text){
        String str[] =text.split("\\.");
        System.out.println(text+"-----------------------+  str "+str.length);

        if(str.length==2){
            if(str[1].length()==1){
                text+="0";
            }
            if(str[1].length()>2){
               text=str[0]+"."+ removeZero(str[1]);
            }

        }else {
            text+=".00";
        }

        return text;

    }

    public String removeZero(String string){
        //      如果字符串尾部不为0，返回字符串
        if(!string.substring(string.length() -1).equals("0")){
            return string;
        }else{
//          否则将字符串尾部删除一位再进行递归
            return removeZero(string.substring(0, string.length() -1 ));
        }
    }
}
