package com.wallet.crypto.ftb.assets;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.wallet.crypto.ftb.R;

/**
 * Created by zhanghesong on 2018/3/18.
 */

public class PopuWindowAssets extends PopupWindow {
    private Context context;
    private EditText symbol1,count1;
    private Button button;
    private PopuWindowAssets popuWindowAssets;
    private Window window;
    public PopuWindowAssets(Context context,int w,int y){
        super(w,y);
        this.context =context;
        View viewcontent =View.inflate(context, R.layout.asset_popu_layout,null);
        viewcontent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.MATCH_PARENT));
        initview(viewcontent);
        setContentView(viewcontent);

        ImageButton closeBtn = viewcontent.findViewById(R.id.dlg_close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void initview(View viewcontent) {
        symbol1 = viewcontent.findViewById(R.id.edit_symbol);
        count1 = viewcontent.findViewById(R.id.edit_total);
        button = viewcontent.findViewById(R.id.bt_entrue);
    }

    public void setOnClickListener(View.OnClickListener onClickListener ){
        button.setOnClickListener(onClickListener);
    }

    public String getSymbolText(){
        return symbol1.getText().toString();
    }

    public String getCount(){
        return count1.getText().toString();
    }
    public void setSymbolTextHint(String s){
        symbol1.setHint(s.toUpperCase());
    }
    public void setCountTextHint(String s){
        count1.setText("");
        count1.setHint(s);
    }

    public void showPopu(View view,int gravity,int x,int y){
        showAtLocation(view,gravity,x,y);
        changerWindowAlph(false);

    }
    public void changerWindowAlph(boolean white){
        if(window==null){
            window = ((AppCompatActivity)context).getWindow();
        }
        WindowManager.LayoutParams wl = window.getAttributes();
        if(white){
            wl.alpha=1f;
        }else {
            wl.alpha=0.6f;   //这句就是设置窗口里崆件的透明度的．０.０全透明．１.０不透明．
        }
        window.setAttributes(wl);
    }
    public void setCancleEdit(boolean cancleEdit){
        symbol1.setFocusableInTouchMode(cancleEdit);
    }

    public void clear(){
        symbol1.setText("");
        count1.setText("");
        symbol1.setHint("请输入币种代号(ETH、BTC等)");
        count1.setHint("请输入数量(可为0)");
        symbol1.setFocusableInTouchMode(true);
    }
}
