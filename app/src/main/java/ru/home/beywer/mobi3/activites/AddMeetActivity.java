package ru.home.beywer.mobi3.activites;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

import ru.beywer.home.mobi3.lib.MeetPriority;
import ru.home.beywer.mobi3.DatePickerFragment;
import ru.home.beywer.mobi3.R;
import ru.home.beywer.mobi3.TimePickerFragment;
import ru.home.beywer.mobi3.tasks.AddMeetTask;

public class AddMeetActivity extends AppCompatActivity {

    private static final String TAG = "ADD_MEET_ACTIVITY";

    private EditText nameElem;
    private EditText descriptionElem;

    private DatePickerFragment dateFragment;
    private TimePickerFragment startFragment;
    private TimePickerFragment endFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivity.shown = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meet);

        nameElem = (EditText) findViewById(R.id.new_meet_name);
        descriptionElem = (EditText) findViewById(R.id.new_meet_descr);

        TextView dateElem = (TextView) findViewById(R.id.new_meet_date);
        TextView startElem = (TextView) findViewById(R.id.new_meet_start);
        TextView endElem = (TextView) findViewById(R.id.new_meet_end);

        dateElem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dateFragment == null) {
                    dateFragment = new DatePickerFragment();
                    dateFragment.setTextView((TextView) v);
                }
                dateFragment.show(getSupportFragmentManager(), "dateElemDatePicker");
            }
        });
        startElem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startFragment == null){
                    startFragment = new TimePickerFragment();
                    startFragment.setTextView((TextView)v);
                }
                startFragment.show(getSupportFragmentManager(), "startElemTimePicker");
            }
        });
        endElem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(endFragment == null) {
                    endFragment = new TimePickerFragment();
                    endFragment.setTextView((TextView)v);
                }
                endFragment.show(getSupportFragmentManager(), "startElemTimePicker");
            }
        });


        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddMeetActivity.this.finish();
            }
        });
        Button create = (Button) findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar startDate = Calendar.getInstance();
                startDate.setTimeInMillis(0);
                startDate.set(dateFragment.getYear(), dateFragment.getMonth()-1, dateFragment.getDay(),
                        startFragment.getHour(), startFragment.getMinute(), 0);
                Calendar endDate = Calendar.getInstance();
                endDate.setTimeInMillis(0);
                endDate.set(dateFragment.getYear(), dateFragment.getMonth()-1, dateFragment.getDay(),
                        endFragment.getHour(), endFragment.getMinute(), 0);
                AddMeetTask adder = new AddMeetTask(AddMeetActivity.this, nameElem.getText().toString(),
                        descriptionElem.getText().toString(),startDate, endDate, MeetPriority.LOW);
                adder.execute();
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        MainActivity.shown = false;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_meet, menu);
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
