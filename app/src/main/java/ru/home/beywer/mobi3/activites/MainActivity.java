package ru.home.beywer.mobi3.activites;

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
import java.util.concurrent.ExecutionException;

import ru.beywer.home.mobi3.lib.Meet;
import ru.home.beywer.mobi3.DownloadService;
import ru.home.beywer.mobi3.R;
import ru.home.beywer.mobi3.tasks.LoadMeetsTask;


public class MainActivity extends AppCompatActivity {

    public static final String REQ_TYPE = "reqType";

    private static final String TAG = "MAIN_ACTIVITY";
    public static final String PARAM_PINTENT = "PINTENT";
    public static final String BROADCAST_ACTION = "ru.home.beywer.mobi3.broadcast";

    ArrayList<Meet> meets;
    MeetsListViewAdapter meetAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getOverflowMenu();

        meets = new ArrayList<>();
        meetAdapter = new MeetsListViewAdapter(this, meets);
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.meets_swipe_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "refreshing");
                loadAll();
                //Остановить анимацию
                refreshLayout.setRefreshing(false);
            }
        });

        // настраиваем список
        ListView lvMain = (ListView) findViewById(R.id.meet_view);
        lvMain.setAdapter(meetAdapter);

        loadAll();

        BroadcastReceiver br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                int task = intent.getIntExtra("AZA", 0);
                int status = intent.getIntExtra("AZA2", 0);
                Log.d(TAG, "onReceive: AZA = " + task + ", AZA2 = " + status);
            }
        };
        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(br, intFilt);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAll();
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

    private void loadAll(){
        LoadMeetsTask task = new LoadMeetsTask(MainActivity.this);
        task.execute();
        try {
            ArrayList<Meet> loadedMeets = task.get();
            meets.clear();
            meets.addAll(loadedMeets);
            meetAdapter.notifyDataSetChanged();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

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
                Intent intent2 = new Intent(this, DownloadService.class);
                intent2.putExtra(REQ_TYPE,"load");
                startService(intent2);
                Log.d(TAG, "Service sended comma");
                return true;
            case R.id.add_meet:
                Intent intent = new Intent(this, AddMeetActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
