package com.jinshu.xuzhi.learnpinyin;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by xuzhi on 2016/8/18.
 */
public class AdapterAlphabets extends BaseAdapter {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private Context mContext ;
    private String[] mAlphabetsArray;

    public AdapterAlphabets(Context context,String [] alphabetsArray) {
        super();
        mContext = context;
        mAlphabetsArray = alphabetsArray;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            textView = new TextView(mContext); // 实例化ImageView的对象
            //imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // 设置缩放方式
            //textView.setPadding(0, 3, 0, 3); // 设置ImageView的内边距
        } else {
            textView = (TextView) convertView;
        }

        textView.setText(mAlphabetsArray[position + 1]);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        textView.setTypeface(null, Typeface.BOLD);
        switch (mAlphabetsArray[position + 1]) {
            case "a":
            case "o":
            case "e":
            case "i":
            case "u":
            case "v":
                textView.setTextColor(mContext.getResources().getColor(R.color.violet));
                break;
            case "b":
            case "h":
            case "r":
            case "y":
            case "z":
                textView.setTextColor(mContext.getResources().getColor(R.color.deepskyblue));
                break;
            case "c":
            case "p":
            case "w":
            case "n":
                textView.setTextColor(mContext.getResources().getColor(R.color.lime));
                break;
            case "d":
            case "l":
            case "x":
            case "t":
                textView.setTextColor(mContext.getResources().getColor(R.color.yellow));
                break;
            case "m":
            case "s":
            case "q":
            case "f":
                textView.setTextColor(mContext.getResources().getColor(R.color.orange));
                break;
            case "g":
            case "j":
            case "k":
                textView.setTextColor(mContext.getResources().getColor(R.color.white));
                break;

            default:
                textView.setTextColor(mContext.getResources().getColor(R.color.white));
        }

        textView.setBackgroundResource(R.drawable.grid_item_border);
        textView.setGravity(0x11);
        return textView;
    }
    /*
     * 功能：获得当前选项的ID
     *
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        //System.out.println("getItemId = " + position);
        return position;
    }

    /*
     * 功能：获得当前选项
     *
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return position;
    }

    /*
     * 获得数量
     *
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {

        return 30;/*26字母 + 4 声调*/
    }

}
