package com.jnu.dns.tiah.wemakebeauty.views;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jnu.dns.tiah.wemakebeauty.R;
import com.jnu.dns.tiah.wemakebeauty.items.EventItem;
import com.jnu.dns.tiah.wemakebeauty.others.DBAdapter;
import com.jnu.dns.tiah.wemakebeauty.others.NetworkHandler;
import com.jnu.dns.tiah.wemakebeauty.others.Work;

import java.util.ArrayList;
import java.util.Calendar;


public class Map extends ActionBarActivity { //fragment activity
//TODO


    private GoogleMap gMap;
    public int screenWidth, screenHeight;
    LatLng sPoint = null, ePoint;
    double dist;
    Work work;
    float zoomLevel;

    Context context;
    DBAdapter db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpMapIfNeeded();
        getScreenSize();
        context = this;
        db = new DBAdapter(context);
        db.open();
        work = new Work(screenWidth);
        makeIcon();

    }

    public void getScreenSize() {
        DisplayMetrics dis = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dis);
        screenHeight = dis.heightPixels;
        screenWidth = dis.widthPixels;
    }

    private void setUpMapIfNeeded() {

        if (gMap == null) {
            gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            gMap.getUiSettings().setZoomControlsEnabled(true);
            gMap.getUiSettings().setTiltGesturesEnabled(false);
            gMap.getUiSettings().setRotateGesturesEnabled(false);
            gMap.setMyLocationEnabled(true);
            CameraUpdate center =
                    CameraUpdateFactory.newLatLng(new LatLng(35.177314, 126.912658));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(13);

            gMap.moveCamera(center);
            gMap.animateCamera(zoom);

            gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    //마커를 터치했을 때 호출
                    return false;
                }
            });

            gMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {


                @Override
                public void onCameraChange(CameraPosition cameraPosition) {

                    if (sPoint == null) { //initialize at first time
                        sPoint = cameraPosition.target;


                    } else { //from second time calculate distance between start and end point
                        ePoint = cameraPosition.target;

                        dist += work.getDistDiff(sPoint, ePoint);

                        if (cameraPosition.zoom > 12) { // magnify more than 10 level
                            double pix = dist / work.getMeterPerPixelByZoomLevel(cameraPosition.zoom);

                            if (pix > screenWidth / 2) { // if camera moved more than half of screen size
                                sPoint = ePoint;
                                zoomLevel = cameraPosition.zoom;
                                int rad = work.getSearchRadius(work.getMeterPerPixelByZoomLevel(cameraPosition.zoom));

                                update(rad, ePoint);
                                dist = 0;
                            }
                        }

                    }


                }
            });


        }
    }

    public void log(String msg) {
        Log.d("tiah", "map." + msg);
    }

    public final String ARITAUM = "2";
    public final String IOPE = "1";

    public void update(int rad, LatLng point) {
        log("update");
        Toast.makeText(context, "updating!", Toast.LENGTH_SHORT).show();

        gMap.clear();
        Cursor c = db.get();
        if (c.moveToFirst())
            do {

                log("cursor");


                String name = c.getString(0);
                String due = work.getDateDifference(c.getLong(1));
                String memo = c.getString(2);

                String url = work.getURL(name, point.latitude, point.longitude, rad);
                Calendar cal = Calendar.getInstance();
                //cal.setTimeInMillis(_due);
                //String due = cal.getTime().toString();

                log(due);

                String[] params = {url, name, due, memo};

                request(params);
            } while (c.moveToNext());

        else
            log("update cursor empty");
    }


    public void request(String[] params) {

        log("request " + params[1]);
        new AsyncTask<String, Void, String>() {
            private String name
                    ,
                    memo
                    ,
                    due;


            @Override
            protected String doInBackground(String... params) {
                NetworkHandler networkHandler = new NetworkHandler();
                name = params[1];
                due = params[2];
                memo = params[3];
                return networkHandler.sendRequest(params[0]);


            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                log("result : s");

                ArrayList<EventItem> list = work.parsing(s, name, due, memo);
                for (EventItem e : list)
                    addMarker(e);


            }

        }.execute(params);

    }


    public void addMarker(EventItem e) {
        log("addMarker " + e.getBrand());
        LatLng latLng = new LatLng(e.getLat(), e.getLng());
        MarkerOptions mOptions = new MarkerOptions().position(latLng).snippet(e.getDue() + "\n" + e.getMemo()).title(e.getBrand()).icon(missha);

        //gMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        gMap.addMarker(mOptions);
    }

    BitmapDescriptor missha;

    public void makeIcon() {

        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.missha);
        Bitmap bitmap = drawable.getBitmap();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);

        //missha = BitmapDescriptorFactory.fromResource(R.drawable.missha);
        missha = BitmapDescriptorFactory.fromBitmap(scaledBitmap);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View inflater;

        MyInfoWindowAdapter() {
            inflater = getLayoutInflater()
                    .inflate(R.layout.map_infowindow, null);
        }

        public View getInfoWindow(Marker marker) {
            render(marker, inflater);

            return inflater;
        }

        public View getInfoContents(Marker marker) {

            return null;
        }

        private void render(Marker marker, View view) {

            ImageView photo = (ImageView) inflater.findViewById(R.id.info_window_photo);
            TextView tv = (TextView) inflater.findViewById(R.id.info_window_tv);
            if (marker.getTitle().equalsIgnoreCase("미샤"))
                photo.setImageDrawable(context.getResources().getDrawable(R.drawable.missha));
            else if (marker.getTitle().equalsIgnoreCase("아리따움"))
                photo.setImageDrawable(context.getResources().getDrawable(R.drawable.aritaum));

            log("market snippt " + marker.getSnippet());
            tv.setText(marker.getSnippet());


        }
    }
}
