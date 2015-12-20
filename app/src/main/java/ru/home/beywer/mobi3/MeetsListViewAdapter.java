package ru.home.beywer.mobi3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class MeetsListViewAdapter extends BaseAdapter {

    private final static String TAG = "MEET_ADAPTER";

    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Object> meets;

    MeetsListViewAdapter(Context context, ArrayList<Object> meets) {
        ctx = context;
        this.meets = meets;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
//        return meets.size();
        return 20;
    }

    @Override
    public Object getItem(int position) {
        return meets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.meet_item, parent, false);
        }

        ((TextView) view.findViewById(R.id.meetName)).setText("Name " + position);
        ((TextView) view.findViewById(R.id.meetDescription)).setText("meetDescription " + position);

        CheckBox cbBuy = (CheckBox) view.findViewById(R.id.participateCheckBox);
//         пишем позицию
//        cbBuy.setTag(position);
        // заполняем данными из товаров: в корзине или нет
        cbBuy.setChecked(false);
        return view;
    }
}
