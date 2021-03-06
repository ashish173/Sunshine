package com.example.comrade.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    /* Define the adapter which will take data and populate the list view */
    private ArrayAdapter<String> mForecastAdapter;

    public static String unit = null;
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

    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        // Fetch default value of location from shared preferences.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        /* Bound in the code in SettingsActivity
         * bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
         * Same is used in code below to get the value from SPs.
         */
        String location = prefs.getString(
            getString(R.string.pref_location_key),     // if value is set
            getString(R.string.pref_location_default)  // default value
        );
        unit = prefs.getString(
          getString(R.string.pref_unit_key),
          getString(R.string.pref_unit_default)
        );
        weatherTask.execute(location);
    }
    /* action on item selected in menu */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // when action is refresh call AsyncTask background job.
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart(){
        super.onStart();
        // call updateWeather to update the weather when we see the first landing screen
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        // Initialize with no data in list.

        /* Feed the adapter */
        mForecastAdapter =
                new ArrayAdapter<String>(
                    // the current context, fragments parent activity
                    getActivity(),
                    // Id of item list layout
                    R.layout.list_item_forecast,
                    // Id of item list textview to populate
                    R.id.list_item_forecast_textview,
                    // dummy data
                    new ArrayList<String>());

        // Find id of listview
        ListView listView = (ListView) rootView.findViewById(
                R.id.listview_forecast);
        // Binding the adapter to listView
        listView.setAdapter(mForecastAdapter);
        // set click listener on the listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /* Context of the application can be fetched using the current activity
                 * but since we are in a fragment we have to first fetch its parent activity then
                 * it context.
                 * Another approach can be to define a static var(type: Context) in application class onCreate method
                 * that way we can access it from anywhere in the application.
                 */
                Context context = getActivity().getApplicationContext();
                String forecast = mForecastAdapter.getItem(position);
              /* Now we use and intent to launch the detailActivity
               * int duration = Toast.LENGTH_SHORT;
               * // We can also chain makeText and show methods.
               * Toast toast = Toast.makeText(context, text, duration);
               * toast.show();
               */
                // Intent constructor takes in application context since in fragment here
                // so call to getActivity() to get the current context.
                // we also provide the name of the class which should be called with
                // this intent
                Intent detailIntent = new Intent(
                        getActivity().getApplicationContext(),
                        DetailActivity.class)
                        // Intent.EXTRA_TEXT is a key which is used to read the data in the
                        // receiving activity its doesn't matter much as long as we use the
                        // same key to read the values there.
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                // Starting the activity
                startActivity(detailIntent);

            }
        });
        return rootView;
    }

    /* This is subclass to ForecastFragment.java it handles fetching data from the api
     * openweathermaps api. run AsyncTask worker.
     */
        public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
            // return type is String[] as we expect doInBackground method to return
            // array of string containing weather data.
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            /* onPostExecute method will be called after the the execution of background job
             * is completed.
             */
            @Override
            protected void onPostExecute(String[] result) {
                // super.onPostExecute(strings);
                if(result != null) {
                    mForecastAdapter.clear();
                    // Populating data to adapter manually
                    for(String dayForecastStr : result){
                        mForecastAdapter.add(dayForecastStr);
                    }
                }
            }

            /**
            * Prepare the weather high/lows for presentation.
            */
            private String formatHighLows(double high, double low) {
                // For presentation, assume the user doesn't care about tenths of a degree.
                long roundedHigh, roundedLow;
                if(unit.equals("cel")) {
                    roundedHigh = Math.round(high);
                    roundedLow = Math.round(low);
                }else if(unit.equals("fah")){
                    roundedHigh = Math.round((high * 1.8) + 32);
                    roundedLow = Math.round((low * 1.8) + 32);
                }else{
                    // this case is not a possibility but we get an
                    // uninitialized variable error since using if and else if
                    // above.
                    roundedHigh = 0;
                    roundedLow = 0;
                }

                String highLowStr = roundedHigh + "/" + roundedLow;
                return highLowStr;
            }

            /**
            * Take the String representing the complete forecast in JSON Format and
            * pull out the data we need to construct the Strings needed for the wireframes.
            *
            * Fortunately parsing is easy:  constructor takes the JSON string and converts it
            * into an Object hierarchy for us.
            */
            private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                    throws JSONException {

                // These are the names of the JSON objects that need to be extracted.
                final String OWM_LIST = "list";
                final String OWM_WEATHER = "weather";
                final String OWM_TEMPERATURE = "temp";
                final String OWM_MAX = "max";
                final String OWM_MIN = "min";
                final String OWM_DESCRIPTION = "main";

                JSONObject forecastJson = new JSONObject(forecastJsonStr);
                JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

                String[] resultStrs = new String[numDays];
                for(int i = 0; i < weatherArray.length(); i++) {
                    // For now, using the format "Day, description, hi/low"
                    String day;
                    String description;
                    String highAndLow;

                    // Get the JSON object representing the day
                    JSONObject dayForecast = weatherArray.getJSONObject(i);

                    // this returns unix timestamp.
                    long dt = dayForecast.getLong("dt");
                    // multiply by 1000 as date constructor expects milliseconds
                    Date date = new Date((long)dt*1000);
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    day = df.format(date);
                    // description is in a child array called "weather", which is 1 element long.
                    JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                    description = weatherObject.getString(OWM_DESCRIPTION);

                    // Temperatures are in a child object called "temp".  Try not to name variables
                    // "temp" when working with temperature.  It confuses everybody.
                    JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                    double high = temperatureObject.getDouble(OWM_MAX);
                    double low = temperatureObject.getDouble(OWM_MIN);

                    highAndLow = formatHighLows(high, low);
                    resultStrs[i] = day + " - " + description + " - " + highAndLow;
                }
                return resultStrs;
            }

            // return type of this method should match the return type in the class that inherits from
            // AsyncTask<String, Void, String[]>
            @Override
            protected String[] doInBackground(String... params) {
                String format = "json";
                String units = "metric";
                int numDays = 7;
                try {
                    // Construct the URL for the OpenWeatherMap query
                    // Possible parameters are avaiable at OWM's forecast API page, at
                    // http://openweathermap.org/API#forecast
                    final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                    final String QUERY_PARAM = "q";
                    final String FORMAT_PARAM = "mode";
                    final String UNITS_PARAM = "units";
                    final String DAYS_PARAM = "cnt";

                    Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                            .appendQueryParameter(QUERY_PARAM, params[0])
                            .appendQueryParameter(FORMAT_PARAM, format)
                            .appendQueryParameter(UNITS_PARAM, units)
                            .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                            .build();

                    URL url = new URL(builtUri.toString());
                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    // Defensive programming(DP)
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

                    // DP
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
                try{
                    return getWeatherDataFromJson(forecastJsonStr, numDays);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
}