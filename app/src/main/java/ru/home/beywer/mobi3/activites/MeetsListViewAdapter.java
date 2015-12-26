package ru.home.beywer.mobi3.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.beywer.home.mobi3.lib.Meet;
import ru.beywer.home.mobi3.lib.User;
import ru.home.beywer.mobi3.R;
import ru.home.beywer.mobi3.tasks.HttpUtil;

public class MeetsListViewAdapter extends BaseAdapter {

    private final static String TAG = "MEET_ADAPTER";

    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Meet> meets;
    private SharedPreferences mPref;

    MeetsListViewAdapter(Context context, ArrayList<Meet> meets) {
        ctx = context;
        this.meets = meets;
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return meets.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.meet_item, parent, false);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, MeetActivity.class);
                intent.putExtra("id", meets.get(position).getId());
                ctx.startActivity(intent);
            }
        });

        final String meetId = meets.get(position).getId();

        ((TextView) view.findViewById(R.id.meetName)).setText(meets.get(position).getName());
        String description = meets.get(position).getDescription();
        String shortDescription;
        if(description != null) {
            if (description.length() < 47) {
                shortDescription = description;
            } else {
                shortDescription = description.substring(0, 47) + "...";
            }
            ((TextView) view.findViewById(R.id.meetDescription)).setText(shortDescription);
        }

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


        CheckBox cbBuy = (CheckBox) view.findViewById(R.id.participateCheckBox);
        cbBuy.setChecked(false);
        Log.d(TAG, "parse meet " + meets.get(position));
        for(User user : meets.get(position).getParticipants()){
            Log.d(TAG, "I is " + mPref.getString("login", "") + "     check for " + user);
            if(user.getLogin().equals(mPref.getString("login", ""))){
                cbBuy.setChecked(true);
                break;
            }
        }
        cbBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("login", mPref.getString("login", ""));
                if(((CheckBox) v).isChecked()){
                    jsonObject.addProperty("type","add");
                }else{
                    jsonObject.addProperty("type","remove");
                }
                HttpUtil.updateTask(jsonObject, meetId, ctx);
                List<User> pat = meets.get(position).getParticipants();
                for(User user : pat){
                    if(user.getLogin().equals(mPref.getString("login", ""))){
                        pat.remove(user);
                        break;
                    }
                }
            }
        });

        Button delete = (Button)view.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUtil.deleteTask(meetId, ctx);
                meets.remove(position);
                MeetsListViewAdapter.this.notifyDataSetChanged();
            }
        });
        if(!meets.get(position).getOwner().getLogin().equals(mPref.getString("login", ""))){
            delete.setVisibility(View.INVISIBLE);
        }


        return view;
    }
}
