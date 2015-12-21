package ru.home.beywer.mobi3;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import ru.beywer.home.mobi3.lib.Meet;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN_ACTIVITY";
    private static final String ADDRESS = "http://192.168.56.1:8080/api/meets/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<Meet> meets = new ArrayList<>();
        final MeetsListViewAdapter meetAdapter = new MeetsListViewAdapter(this, meets);
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.meets_swipe_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "refreshing");

                LoadMeetsTask task = new LoadMeetsTask();
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

                //Остановить анимацию
                refreshLayout.setRefreshing(false);
            }
        });

        // настраиваем список
        ListView lvMain = (ListView) findViewById(R.id.meet_view);
        lvMain.setAdapter(meetAdapter);

    }

//    private void readStream(BufferedInputStream in) {
//        try {
//            int available = in.available();
//            Log.d(TAG, "available " + available);
//            byte[] buffer = new byte[5000];
//            int readed = in.read(buffer);
//            Log.d(TAG, "readed " + readed);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
