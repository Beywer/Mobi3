package ru.home.beywer.mobi3.activites;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.util.Locale;

import ru.beywer.home.mobi3.lib.Meet;
import ru.beywer.home.mobi3.lib.User;
import ru.home.beywer.mobi3.R;
import ru.home.beywer.mobi3.tasks.HttpUtil;

public class MeetActivity extends AppCompatActivity {

    private static final String TAG = "MEET_ACTIVITY";
    private SharedPreferences mPref;
    private String id;

    private TextView name;
    private TextView descr;
    private TextView from;
    private TextView to;
    private CheckBox cbBuy;
    private Button delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        id = getIntent().getStringExtra("id");

        name = (TextView) findViewById(R.id.name);
        descr = (TextView) findViewById(R.id.descr);
        from = (TextView) findViewById(R.id.interval_start);
        to = (TextView) findViewById(R.id.interval_end);
        cbBuy = (CheckBox) MeetActivity.this.findViewById(R.id.participateCheckBox);
        delete = (Button)MeetActivity.this.findViewById(R.id.delete);

        loadMeet();
    }

    private void loadMeet(){
        //Тред, в котором все будет грузиться
        new Thread(new Runnable() {
            @Override
            public void run() {

                final Meet meet = HttpUtil.get(id, MeetActivity.this);

                Log.d(TAG, "Loaded meet = "+ meet);

                //Тред, в котором все будет отображаться
                MeetActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        name.setText(meet.getName());
                        descr.setText(meet.getDescription());

                        DateFormat formatter = DateFormat.getDateTimeInstance(
                                DateFormat.SHORT,
                                DateFormat.SHORT,
                                new Locale("ru"));
                        from.setText(formatter.format(meet.getStart()));
                        to.setText(formatter.format(meet.getEnd()));

                        cbBuy.setChecked(false);
                        Log.d(TAG, "parse meet " + meet);
                        for(User user : meet.getParticipants()){
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
                                if (((CheckBox) v).isChecked()) {
                                    jsonObject.addProperty("type", "add");
                                } else {
                                    jsonObject.addProperty("type", "remove");
                                }
                                HttpUtil.updateTask(jsonObject, meet.getId(), MeetActivity.this);
                                loadMeet();
                            }
                        });

                        delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                HttpUtil.deleteTask(meet.getId(), MeetActivity.this);
                                finish();
                            }
                        });
                        Log.d(TAG, "Login and del butt   " +  meet.getOwner().getLogin() + "  " + mPref.getString("login", ""));
                        if(!meet.getOwner().getLogin().equals(mPref.getString("login", ""))){
                            delete.setVisibility(View.INVISIBLE);
                        }

                        ListView lv = (ListView) MeetActivity.this.findViewById(R.id.participants);
                        ParticipateListViewAdapter adapter = new ParticipateListViewAdapter(MeetActivity.this, meet.getParticipants());
                        lv.setAdapter(adapter);

                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_meet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
