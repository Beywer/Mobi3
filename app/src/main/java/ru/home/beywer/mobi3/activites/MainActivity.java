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
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

import ru.beywer.home.mobi3.lib.Meet;
import ru.home.beywer.mobi3.DatePickerFragment;
import ru.home.beywer.mobi3.DownloadService;
import ru.home.beywer.mobi3.R;
import ru.home.beywer.mobi3.tasks.HttpUtil;


public class MainActivity extends AppCompatActivity {

    public static boolean shown = false;
    public static boolean needResult = true;
    public static final String REQ_TYPE = "reqType";
    public static final String MEETS = "meets";

    private RadioButton today;
    private RadioButton interval;
    private RadioButton search;
    private final DatePickerFragment startFragment = new DatePickerFragment();
    private final DatePickerFragment endFragment = new DatePickerFragment();
    private EditText searchEditText;

    private static final String TAG = "MAIN_ACTIVITY";
    public static final String BROADCAST_ACTION = "ru.home.beywer.mobi3.broadcast";

    ArrayList<Meet> meets;
    MeetsListViewAdapter meetAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        shown = true;
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
                if(today.isChecked()){
                    Log.d(TAG, "load from DownloadService");
                    loadAll("refreshing");
                }
                if(interval.isChecked()){
                    Log.d(TAG, "load interval");
                    Calendar start = Calendar.getInstance();
                    start.set(startFragment.getYear(), startFragment.getMonth()-1, startFragment.getDay());
                    Calendar end = Calendar.getInstance();
                    end.set(endFragment.getYear(), endFragment.getMonth()-1, endFragment.getDay());

                    if(start.getTime().getTime() <= end.getTime().getTime()) {
                        HttpUtil.getAll(start, end, MainActivity.this);
                        Log.d(TAG, "HttpUtil.getAll(start, end, MainActivity.this) called");
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Конец интервала меньше начала", Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                }
                if(search.isChecked()){
                    Log.d(TAG, "load search");
                    String str = searchEditText.getText().toString();
                    if(str.equals("")){
                        Toast.makeText(MainActivity.this, "Пустой запрос", Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    } else HttpUtil.getAll(str, MainActivity.this);
                }
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
                        refreshLayout.setRefreshing(false);//Остановит анимацию, если был свайп.
                        break;
                }

            }
        };
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(br, intFilt);

        Log.d(TAG, "BroadcastReceiver init complete");

        final View intervalPanel = findViewById(R.id.interval_panel);
        final View searchPanel = findViewById(R.id.search_panel);

        today = (RadioButton) findViewById(R.id.rb_today);
        today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needResult = true;
                intervalPanel.setVisibility(View.GONE);
                searchPanel.setVisibility(View.GONE);
            }
        });
        interval = (RadioButton) findViewById(R.id.rb_interval);
        interval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needResult = false;
                intervalPanel.setVisibility(View.VISIBLE);
                searchPanel.setVisibility(View.GONE);
            }
        });
        search = (RadioButton) findViewById(R.id.rb_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needResult = false;
                intervalPanel.setVisibility(View.GONE);
                searchPanel.setVisibility(View.VISIBLE);
            }
        });
        Log.d(TAG, "Radio buttons inited");

        TextView startDate = (TextView) findViewById(R.id.interval_start);
        startFragment.setTextView(startDate);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment.show(getSupportFragmentManager(), "dateElemDatePicker");
            }
        });
        TextView endDate = (TextView) findViewById(R.id.interval_end);
        endFragment.setTextView(endDate);
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endFragment.show(getSupportFragmentManager(), "dateElemDatePicker");
            }
        });
        Log.d(TAG, "Date pickers inited");

        searchEditText = (EditText) findViewById(R.id.search);
        Log.d(TAG, "Search inited");

        loadAll("onCreate");
        Log.d(TAG, "loadAll called");
    }

    @Override
    protected void onStop() {
        shown = false;
        super.onStop();
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
        intent.putExtra("needNotif", false);
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
                Intent settingsActivity = new Intent(this, Preferences.class);
                startActivity(settingsActivity);
                return true;
            case R.id.add_meet:
                Intent intent = new Intent(this, AddMeetActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
