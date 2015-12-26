package ru.home.beywer.mobi3.activites;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.beywer.home.mobi3.lib.User;
import ru.home.beywer.mobi3.R;

/**
 * Created by Beywer on 26.12.2015.
 */
public class ParticipateListViewAdapter extends BaseAdapter {

    private List<User> participants = new ArrayList<>();
    private Context context;
    private SharedPreferences mPref;
    LayoutInflater lInflater;

    ParticipateListViewAdapter(Context context, List<User> participants) {
        this.context = context;
        this.participants = participants;
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return participants.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.participate_item, parent, false);
        }
        ((TextView)view.findViewById(R.id.name)).setText(participants.get(position).getName());
        ((TextView)view.findViewById(R.id.sername)).setText(participants.get(position).getSurname());
        ((TextView)view.findViewById(R.id.fathername)).setText(participants.get(position).getFatherName());
        ((TextView)view.findViewById(R.id.login)).setText(participants.get(position).getLogin());

        return view;
    }
}
