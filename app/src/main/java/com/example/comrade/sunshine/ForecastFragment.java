package com.example.comrade.sunshine;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    // Constuctor
    public ForecastFragment() {

    }

    /*This method runs before onCreateView
     * setting here options menu
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // this line enables callback for menu item methods.
        setHasOptionsMenu(true);
    }

    /* Set the layout for the option menu
     * on create of option menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // super.onCreateOptionsMenu(menu, inflater);
        // menu.clear();
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    /* action on item selected in menu */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // when action is refresh call AsyncTask background job.
        if (id == R.id.action_refresh) {
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

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
        public class FetchWeatherTask extends AsyncTask<URL, Integer, Long> {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            @Override
            protected Long doInBackground(URL... params) {
                try {
                    // Construct the URL for the OpenWeatherMap query
                    // Possible parameters are avaiable at OWM's forecast API page, at
                    // http://openweathermap.org/API#forecast
                    URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    forecastJsonStr = buffer.toString();
                } catch (IOException e) {
                    Log.e("PlaceholderFragment", "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attemping
                    // to parse it.
                    return null;
                } finally{
                    // close the connection
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    // close the reader
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("PlaceholderFragment", "Error closing stream", e);
                        }
                    }
                }
                return null;
            }
        }
}