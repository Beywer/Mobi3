package ru.home.beywer.mobi3.activites;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.util.ArrayList;

import ru.beywer.home.mobi3.lib.Meet;
import ru.home.beywer.mobi3.Constants;
import ru.home.beywer.mobi3.DownloadService;
import ru.home.beywer.mobi3.R;
import ru.home.beywer.mobi3.receivers.LoadReceiver;


public class MainActivity extends AppCompatActivity {

    public static final String REQ_TYPE = "reqType";
    public static final String NEED_NOTIF = "needLoad";
    public static final String MEETS = "meets";

    private static final String TAG = "MAIN_ACTIVITY";
    public static final String BROADCAST_ACTION = "ru.home.beywer.mobi3.broadcast";

    ArrayList<Meet> meets;
    MeetsListViewAdapter meetAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getOverflowMenu();

        Log.d(TAG, "Stared init complete");

        meets = new ArrayList<>();
        meetAdapter = new MeetsListViewAdapter(this, meets);
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.meets_swipe_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "refreshing");
                loadAll("refreshing");
            }
        });
        // настраиваем список
        ListView lvMain = (ListView) findViewById(R.id.meet_view);
        lvMain.setAdapter(meetAdapter);

        Log.d(TAG, "Meet list init complete");

        BroadcastReceiver br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "get new data  ");

                String type = intent.getStringExtra(REQ_TYPE);
                switch (type){
                    case "result":
                        ArrayList<Meet> newMeets = (ArrayList<Meet>) intent.getSerializableExtra(MEETS);
                        Log.d(TAG, "get new data  " + newMeets);
                        meets.clear();
                        meets.addAll(newMeets);
                        meetAdapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);//Остановит онимацию, если был свайп.
                        break;
                }

            }
        };
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(br, intFilt);

        Log.d(TAG, "BroadcastReceiver init complete");

        loadAll("onCreate");
        Log.d(TAG, "loadAll called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAll("onResume");
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAll(final String from){
        Intent intent = new Intent(MainActivity.this, DownloadService.class);
        intent.putExtra(REQ_TYPE, "load");
        intent.putExtra(NEED_NOTIF, false);
        startService(intent);
        Log.d(TAG, "sended query for update from  " + from);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                Intent intent2 = new Intent(MainActivity.this, LoadReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, 0, intent2, 0);
                AlarmManager am =(AlarmManager)MainActivity.this.getSystemService(Context.ALARM_SERVICE);
                am.cancel(pi);
                am.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 2000, Constants.UPDATE_INTERVAL, pi);
                Log.d(TAG, "Created timer");
                return true;
            case R.id.add_meet:
                Intent intent = new Intent(this, AddMeetActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
