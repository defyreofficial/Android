package com.lecodesoft.safepal.app.report;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lecodesoft.safepal.R;
import com.lecodesoft.safepal.app.datetimepickers.DatePickerFragment;
import com.lecodesoft.safepal.app.datetimepickers.TimePickerFragment;
import com.lecodesoft.safepal.app.jsonreport.JSONParser;
import com.lecodesoft.safepal.app.picklocation.PlaceDetailsJSONParser;
import com.lecodesoft.safepal.app.picklocation.PlaceJSONParser;
import com.lecodesoft.safepal.app.reportforpersmissiondialog.PermissionDialog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class YourselfReportActivity extends AppCompatActivity {
    // Progress Dialog
    private ProgressDialog pDialog;
    private EditText rfyEtPhonenumber, rfyEtIncidentDescription;
    private Button  rfyButtonDate, rfyButtonTime;
    private AppCompatSpinner rfyspinnerAge,rfyspinnerGender, rfyspinnerIncidentType, rfyspinnerActionTaken;
    private AutoCompleteTextView rfyIncidentLocation;

    DownloadTask placesDownloadTask;
    DownloadTask placeDetailsDownloadTask;
    ParserTask placesParserTask;
    ParserTask placeDetailsParserTask;

    GoogleMap googleMap;

    final int PLACES=0;
    final int PLACES_DETAILS=1;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    //php add a comment script
    //testing on Emulator:
    private static final String POST_COMMENT_URL = "http://www.thinkitlimited.com/safepal/api/addreport.php";
    //ids
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_yourself);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_rfy);
        setSupportActionBar(toolbar);

        //backwards arrow in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        rfyEtPhonenumber = (EditText)findViewById(R.id.rfy_et_phonenumber);
        rfyEtIncidentDescription=(EditText)findViewById(R.id.rfy_et_incident_description);

        rfyButtonDate=(Button)findViewById(R.id.rfy_button_date);
        rfyButtonTime=(Button)findViewById(R.id.rfy_button_time);

        rfyIncidentLocation = (AutoCompleteTextView)findViewById(R.id.rfy_cactv_location);
        rfyIncidentLocation.setThreshold(1);


/**        * gender spinner */
        rfyspinnerAge = (AppCompatSpinner) findViewById(R.id.rfy_spinner_age);
        rfyspinnerGender = (AppCompatSpinner) findViewById(R.id.rfy_spinner_gender);
        rfyspinnerIncidentType = (AppCompatSpinner) findViewById(R.id.rfy_spinner_incident_type);
        rfyspinnerActionTaken = (AppCompatSpinner) findViewById(R.id.rfy_spinner_actiontaken);

        //ageAdapter
        ArrayAdapter<CharSequence> adapterAge = ArrayAdapter.createFromResource(this,
                R.array.hint_age_array, R.layout.spinner_layout);
        //genderAdapter
        ArrayAdapter<CharSequence> adapterGender = ArrayAdapter.createFromResource(this,
                R.array.hint_gender_array, R.layout.spinner_layout);
        //genderAdapter
        ArrayAdapter<CharSequence> adapterIncidentType = ArrayAdapter.createFromResource(this,
                R.array.rfy_incident_type, R.layout.spinner_layout);
        //actionTakenAdapter
        ArrayAdapter<CharSequence> adapterActionTaken = ArrayAdapter.createFromResource(this,
                R.array.rfy_hint_actiontaken, R.layout.spinner_layout);

        /** Specify the layout to use when the list of choices appears*/
        adapterAge.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        /** Specify the layout to use when the list of choices appears*/
        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        /** Specify the layout to use when the list of choices appears*/
        adapterIncidentType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        /** Specify the layout to use when the list of choices appears*/
        adapterActionTaken.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        rfyspinnerAge.setAdapter(adapterAge);
        rfyspinnerGender.setAdapter(adapterGender);
        rfyspinnerIncidentType.setAdapter(adapterIncidentType);
        rfyspinnerActionTaken.setAdapter(adapterActionTaken);

        //picking location by auto completing the text
        rfyIncidentLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Creating a DownloadTask to download Google Places matching "s"
                placesDownloadTask = new DownloadTask(PLACES);

                // Getting url to the Google Places Autocomplete api
                String url = getAutoCompleteUrl(s.toString());

                // Start downloading Google Places
                // This causes to execute doInBackground() of DownloadTask class
                placesDownloadTask.execute(url);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // Setting an item click listener for the AutoCompleteTextView dropdown list
        rfyIncidentLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long id) {

                ListView lv = (ListView) arg0;
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();

                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);

                // Creating a DownloadTask to download Places details of the selected place
                placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);

                // Getting url to the Google Places details api
                String url = getPlaceDetailsUrl(hm.get("reference"));

                // Start downloading Google Place Details
                // This causes to execute doInBackground() of DownloadTask class
                placeDetailsDownloadTask.execute(url);

            }
        });

        //end of picking location by autocomplete text
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_rfy);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //check whether the gender is selected
                if(rfyspinnerGender.getSelectedItemPosition()!=0){
                //check whether the age is selected
                    if(rfyspinnerAge.getSelectedItemPosition()!=0){
                        //check whether the incident type is selected
                        if(rfyspinnerIncidentType.getSelectedItemPosition()!=0){
                            //check whether the incident type is selected
                            if(rfyspinnerActionTaken.getSelectedItemPosition()!=0){
                                if(rfyEtIncidentDescription.getText().toString().length()>10){
                                    permissionReportedFor();
                                    Snackbar.make(view, "You've successfully reported", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }else{
                                    Snackbar.make(view, "Your Story is too short. ", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            }else{
                                Snackbar.make(view, "Tell us the action u've taken so far.", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }

                        }else {
                            Snackbar.make(view, "Select the type of incident.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                        //feed back when age is not selected
                        else{
                        Snackbar.make(view, "Select your Age. ", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
                //feed back when gender is not selected
                else {
                    Snackbar.make(view, "What's your gender. ", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                /*
                new PostIncident(rfyEtPhonenumber.getText().toString(),String.valueOf(rfyspinnerGender.getSelectedItem()),
                        String.valueOf(rfyspinnerAge.getSelectedItem()),rfyButtonLocation.getText().toString(),rfyButtonDate.getText().toString(),
                        rfyButtonTime.getText().toString(),rfyEtIncidentDescription.getText().toString(),String.valueOf(rfyspinnerIncidentType.getSelectedItem()),
                        String.valueOf(rfyspinnerActionTaken.getSelectedItem())).execute();
                                */





            }
        });
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
    public void permissionReportedFor(){
        DialogFragment newFragment = new PermissionDialog();
        newFragment.show(getSupportFragmentManager(), getResources().getString(R.string.permission_message));

    }


    class PostIncident extends AsyncTask<String, String, String> {


        String post_ei_phoneNumber;
        String post_ei_gender;
        String post_ei_age ;
        String post_ti_location ;
        String post_ti_date  ;
        String post_ti_time ;
        String post_ti_description ;
        String post_ti_type ;
        String post_ti_action ;
        public PostIncident(String post_ei_phoneNumber,String post_ei_gender,   String post_ei_age,String post_ti_location , String post_ti_date,
                            String post_ti_time, String post_ti_description,String post_ti_type,
                            String post_ti_action){

            this.post_ei_phoneNumber = post_ei_phoneNumber;
            this.post_ei_gender = post_ei_gender;
            this.post_ei_age = post_ei_age;
            this.post_ti_location=post_ti_location;
            this.post_ti_date=post_ti_date;
            this.post_ti_time=post_ti_time;
            this.post_ti_description=post_ti_description;
            this.post_ti_type=post_ti_type;
            this.post_ti_action=post_ti_action;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(YourselfReportActivity.this);
            pDialog.setMessage("Posting Comment...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;


            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();

                params.add(new BasicNameValuePair("ei_phoneNumber", post_ei_phoneNumber));
                params.add(new BasicNameValuePair("ei_gender", post_ei_gender));
                params.add(new BasicNameValuePair("ei_age", post_ei_age));

                params.add(new BasicNameValuePair("ti_location", post_ti_location));
                params.add(new BasicNameValuePair("ti_date", post_ti_date));
                params.add(new BasicNameValuePair("ti_time", post_ti_time));
                params.add(new BasicNameValuePair("ti_description", post_ti_description));
                params.add(new BasicNameValuePair("ti_type", post_ti_type));
                params.add(new BasicNameValuePair("ti_action", post_ti_action));
                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        POST_COMMENT_URL, "POST", params);

                // full json response
                Log.d("Post Comment attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Comment Added!", json.toString());
                    finish();
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Comment Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if (file_url != null){
                Toast.makeText(YourselfReportActivity.this, file_url, Toast.LENGTH_LONG).show();

            }

        }

    }//end of class for sending data to safepal

    //methods for picking a place from a given location

    private String getAutoCompleteUrl(String place){

        // Obtain browser key from https://code.google.com/apis/console
        String key = "key=AIzaSyDBLBir0SwUpSm46u9scayjeVBh9e86p5U";

        // place to be be searched
        String input = "input="+place;

        // place type to be searched
        String types = "types=geocode";

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = input+"&"+types+"&"+sensor+"&"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

        return url;
    }


    private String getPlaceDetailsUrl(String ref){

        // Obtain browser key from https://code.google.com/apis/console
        String key = "key=YOUR_API_KEY";

        // reference of place
        String reference = "reference="+ref;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = reference+"&"+sensor+"&"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/details/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            //Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        private int downloadType=0;

        // Constructor
        public DownloadTask(int type){
            this.downloadType = type;
        }

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            switch(downloadType){
                case PLACES:
                    // Creating ParserTask for parsing Google Places
                    placesParserTask = new ParserTask(PLACES);

                    // Start parsing google places json data
                    // This causes to execute doInBackground() of ParserTask class
                    placesParserTask.execute(result);

                    break;

                case PLACES_DETAILS :
                    // Creating ParserTask for parsing Google Places
                    placeDetailsParserTask = new ParserTask(PLACES_DETAILS);

                    // Starting Parsing the JSON string
                    // This causes to execute doInBackground() of ParserTask class
                    placeDetailsParserTask.execute(result);
            }
        }
    }


    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        int parserType = 0;

        public ParserTask(int type){
            this.parserType = type;
        }

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<HashMap<String, String>> list = null;

            try{
                jObject = new JSONObject(jsonData[0]);

                switch(parserType){
                    case PLACES :
                        PlaceJSONParser placeJsonParser = new PlaceJSONParser();
                        // Getting the parsed data as a List construct
                        list = placeJsonParser.parse(jObject);
                        break;
                    case PLACES_DETAILS :
                        PlaceDetailsJSONParser placeDetailsJsonParser = new PlaceDetailsJSONParser();
                        // Getting the parsed data as a List construct
                        list = placeDetailsJsonParser.parse(jObject);
                }

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            switch(parserType){
                case PLACES :
                    String[] from = new String[] { "description"};
                    int[] to = new int[] { android.R.id.text1 };

                    // Creating a SimpleAdapter for the AutoCompleteTextView
                    SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

                    // Setting the adapter
                    rfyIncidentLocation.setAdapter(adapter);
                    break;
                case PLACES_DETAILS :
                    HashMap<String, String> hm = result.get(0);

                    // Getting latitude from the parsed data
                    double latitude = Double.parseDouble(hm.get("lat"));

                    // Getting longitude from the parsed data
                    double longitude = Double.parseDouble(hm.get("lng"));

                    // Getting reference to the SupportMapFragment of the activity_main.xml
                    SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.rfy_incident_map_location_fragment);

                    // Getting GoogleMap from SupportMapFragment
                    googleMap = fm.getMap();


                    LatLng point = new LatLng(latitude, longitude);

                    CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(point);
                    CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(5);

                    // Showing the user input location in the Google Map
                    googleMap.moveCamera(cameraPosition);
                    googleMap.animateCamera(cameraZoom);

                    MarkerOptions options = new MarkerOptions();
                    options.position(point);
                    options.title("Position");
                    options.snippet("Latitude:"+latitude+",Longitude:"+longitude);

                    // Adding the marker in the Google Map
                    googleMap.addMarker(options);

                    break;
            }
        }
    }
}