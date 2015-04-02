package com.example.comrade.sunshine;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.comrade.sunshine.R.*;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        // Constuctor
        public PlaceholderFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(layout.fragment_main, container, false);

            String[] forecastArray = {
                    "Today - Sunny - 88/63",
                    "Tomorrow - Foggy - 70/40",
                    "Weds - Cloudy - 75/35",
                    "Thursday - Rainy - 79/45",
                    "Friday - Stormy - 65/35",
                    "Saturday - Cool - 76/45",
                    "Sunday - Dark cloudy - 34/12",
                    "Fictional day - Not known - 56/24",
                    "Finctional day 2 - not known - 234/34"
            };

            List<String> weekForecast = new ArrayList<String>(
                    Arrays.asList(forecastArray));

            /* Define the adapter which will take data and populate the list view */
            ArrayAdapter<String> mForecastAdapter;
            /* Feed the adapter */
            mForecastAdapter = new ArrayAdapter<String>(
                    // the current context, fragments parent activity
                    getActivity(),
                    // Id of item list layout
                    R.layout.list_item_forecast,
                    // Id of item list textview to populate
                    R.id.list_item_forecast_textview,
                    // dummy data
                    weekForecast);
            // Find id of listview
            ListView listView = (ListView) rootView.findViewById(
                    R.id.listview_forecast);
            // Binding the adapter to listView
            listView.setAdapter(mForecastAdapter);

            return rootView;
        }
    }
}
