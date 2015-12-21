package ru.home.beywer.mobi3;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ru.beywer.home.mobi3.lib.Meet;

public class MeetsListViewAdapter extends BaseAdapter {

    private final static String TAG = "MEET_ADAPTER";

    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Meet> meets;

    MeetsListViewAdapter(Context context, ArrayList<Meet> meets) {
        ctx = context;
        this.meets = meets;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return meets.size();
//        return 20;
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

        ((TextView) view.findViewById(R.id.meetName)).setText(meets.get(position).getName());
        String description = meets.get(position).getDescription();
        String shortDescription;
        if(description.length() < 47){
            shortDescription = description;
        } else {
            shortDescription = description.substring(0, 47) + "...";
        }
        ((TextView) view.findViewById(R.id.meetDescription)).setText(shortDescription);

        Date startDate = meets.get(position).getStart();
        Date endDate = meets.get(position).getEnd();
        DateFormat formatter = DateFormat.getDateTimeInstance(
                DateFormat.SHORT,
                DateFormat.SHORT,
                new Locale("ru"));
        if(startDate != null)
            ((TextView) view.findViewById(R.id.from)).setText(formatter.format(startDate));
        if(endDate != null)
            ((TextView) view.findViewById(R.id.to)).setText(formatter.format(endDate));


        //TODO проверить себя во встрече
        CheckBox cbBuy = (CheckBox) view.findViewById(R.id.participateCheckBox);
//         пишем позицию
//        cbBuy.setTag(position);
        // заполняем данными из товаров: в корзине или нет
        cbBuy.setChecked(false);
        return view;
    }
}
