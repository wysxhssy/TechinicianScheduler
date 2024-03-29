package com.example.mario.techinicianscheduler.Technician;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mario.techinicianscheduler.R;
import com.example.mario.techinicianscheduler.ResideMenu.ResideMenu;
import com.example.mario.techinicianscheduler.ResideMenu.ResideMenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Another main activity for this project. The greedy search and 2-opt are implemented here.
 * The interface is just a google map, all the information will be shown on the map. When the user enter this
 * activity, the system will calculate his visiting sequence first, then use the Google map to show the result,
 * which is a general shape of the route.Then the user could click button to get the suggested path between two place,
 * and this is generated by Google Map Api.
 */
public class TechnicianRoute extends FragmentActivity implements OnMapReadyCallback,DirectionFinderListener, View.OnClickListener {

    private GoogleMap mMap;
    private ArrayList<LatLng> recordPos;
    private LatLng startEnd=new LatLng(37.331629,-121.8923151);
    private TextView orderedListShow;
    private float minimumDistance=100000000;

    private ImageButton route;
    private ImageButton menu;
    private ArrayList<LatLng> orderedList;
    private ProgressDialog progressDialog;
    private List<Polyline> polylinePaths = new ArrayList<>();
    private int routeCount=0;
    private Bundle techInfo;

    private ResideMenu resideMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_route);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);//start map service

        initialize();

        techInfo=getIntent().getExtras();
        recordPos=techInfo.getParcelableArrayList("recordPos");
        orderedList=new ArrayList<LatLng>();

        if(recordPos.size()!=0){
            findShortestOrder();
            routePlan();

            minimumDistance=calculateDistance(orderedList);
        }

        route.setOnClickListener(this);
        menu.setOnClickListener(this);

        handleResideMenu();

    }

    private void improve(){
        routeCount=0;
        if(orderedList.size()==0){
            return;
        }
        kopt(orderedList);

//      mMap.clear();
        mMap.addMarker(new MarkerOptions().position(startEnd).title("Home").icon(BitmapDescriptorFactory.fromResource(R.drawable.start))).showInfoWindow();
        for(int i=1;i<orderedList.size()-1;i++){
            LatLng latLng=orderedList.get(i);
            mMap.addMarker(new MarkerOptions().position(latLng).title("place"+i));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13.0f));
        }

        mMap.addPolyline(new PolylineOptions().add(startEnd,orderedList.get(1)).width(5).color(Color.BLUE));
        for(int i=1;i<orderedList.size()-2;i++){
            LatLng latLng1=orderedList.get(i);
            LatLng latLng2=orderedList.get(i+1);
            mMap.addPolyline(new PolylineOptions().add(latLng1,latLng2).width(5).color(Color.RED));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1,13.0f));
        }
        mMap.addPolyline(new PolylineOptions().add(orderedList.get(orderedList.size()-2),startEnd).width(5).color(Color.BLUE));


    }


    /**
     * Initial the components.
     */
    private void initialize(){
        //orderedListShow=(TextView)findViewById(R.id.orderedList);
        route=(ImageButton)findViewById(R.id.googleRoute);
        menu=(ImageButton)findViewById(R.id.routeMenu);
    }


    private void handleResideMenu(){
        resideMenu=new ResideMenu(this);
        resideMenu.setShadowVisible(true);
        resideMenu.attachToActivity(this);
        resideMenu.setScaleValue(0.6f);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT);

        String titles[]={"Home","Work Arrangement","Route","Settings","Log out"};
        int icon[]={R.drawable.home,R.drawable.schedule,R.drawable.route,R.drawable.settings,R.drawable.logout};

        for(int i=0;i<titles.length;i++){
            ResideMenuItem item=new ResideMenuItem(this,icon[i],titles[i]);
            item.setId(i);
            item.setOnClickListener(this);
            resideMenu.addMenuItem(item,ResideMenu.DIRECTION_LEFT);
        }

        resideMenu.setMenuListener(menuListener);
    }

    private ResideMenu.OnMenuListener menuListener=new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
            resideMenu.setBackground(R.drawable.bg);
        }
        @Override
        public void closeMenu() {
            resideMenu.setBackground(R.drawable.white);
        }
    };

    public boolean dispatchTouchEvent(MotionEvent ev){
        return resideMenu.dispatchTouchEvent(ev);
    }


    /**
     * Use 2-opt heuristic to improve the initial solution made by greedy search.
     */
    private void kopt(ArrayList<LatLng> list) {
        float bestDistance=calculateDistance(list);
        for(int i=1;i<list.size()-1;i++){
            for(int j=i+1;j<list.size()-1;j++){
                ArrayList<LatLng> newList=new ArrayList<LatLng>();
                newList.addAll(list);
                swap(i,j,newList);
                //try to swap the location to get smaller distance for traveling all the position.
                float newDist=calculateDistance(newList);
                if(newDist<bestDistance){
                    list.clear();
                    list.addAll(newList);
                }
            }
        }
        float improveDistance=calculateDistance(list);
        if(improveDistance<bestDistance){
            //if have found the visiting sequence that have smaller distance than the best known solution,record it.
            bestDistance=improveDistance;
            kopt(list);
        }
    }

    /**
    * Swap two positions.
    */
    private void swap(int i,int j,ArrayList<LatLng> list){
        LatLng latlng1=list.get(i);
        LatLng latlng2=list.get(j);

        list.set(i,latlng2);
        list.set(j,latlng1);
    }

    /**
     * Use google map to plan the route for technician.
     */
    private void routePlan() {
    }

    /**
     * Use greedy search  to find the shortest route order.
     */
    private void findShortestOrder() {
        orderedList.add(startEnd); //startEnd means the position of the company.
        ArrayList<LatLng> waitList=new ArrayList<LatLng>();
        waitList.addAll(recordPos);
        greedySearch(waitList,startEnd);
    }

    private void greedySearch(ArrayList<LatLng> waitList,LatLng base) {

        float minDistance=10000000;
        int minDistId=0;

        if(waitList.size()==0){
            return;
        }
        //calculate the distance between start position to each other position
        for(int i=0;i<waitList.size();i++){
            LatLng latlng=waitList.get(i);
            float[] distBetweemTwoNodes=new float[1];
            Location.distanceBetween(base.latitude,base.longitude,latlng.latitude,latlng.longitude,distBetweemTwoNodes);
            if(distBetweemTwoNodes[0]<minDistance){
                minDistance=distBetweemTwoNodes[0];
                //find the position with minimum distance
                minDistId=i;  //use minDistId record the id of position in waitlist.
            }
        }
        if(waitList.size()>1){
            LatLng newBase=waitList.get(minDistId);   //add the found position which has min distance into orderedList.
            orderedList.add(waitList.get(minDistId));
            waitList.remove(minDistId);               //remove the found position.
            greedySearch(waitList,newBase);
        }else {
            orderedList.add(waitList.get(minDistId));  //if the waitList only have 1 location left, then the greedySearch ends.
            waitList.remove(minDistId);
            orderedList.add(startEnd);
        }

    }

    private float calculateDistance(ArrayList<LatLng> list){
        float total=0;
        for(int i=0;i<list.size()-1;i++){
            float[] distBetweemTwoNodes=new float[1];
            Location.distanceBetween(list.get(i).latitude,list.get(i).longitude,list.get(i+1).latitude,list.get(i+1).longitude,distBetweemTwoNodes);
            total+=distBetweemTwoNodes[0];
        }

        return total;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        improve();

    }


    /**
     * Handle the progress Dialog while find the direction.
     */
    @Override
    public void onDirectionFinderStart() {
        progressDialog=ProgressDialog.show(this,"Please wait.","Finding direction..",true);
        if(polylinePaths!=null){
            for(Polyline polyline:polylinePaths){
                polyline.remove();
            }
        }
    }

    /**
     * Handle the situation when successfully find the direction.
     * @param routes
     */
    @Override
    public void onDirectionFinderSuccess(List<DirectionFinder.Route> routes) {
        progressDialog.dismiss();
        polylinePaths=new ArrayList<>();

        for(DirectionFinder.Route route:routes){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation,16.0f));
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.start)).position(route.startLocation));
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.end)).position(route.endLocation));

            PolylineOptions polylineOptions=new PolylineOptions().
                    geodesic(true).color(Color.BLUE).width(15);

            for (int i=0;i<route.points.size();i++){
                polylineOptions.add(route.points.get(i));
            }

            polylinePaths.add(mMap.addPolyline(polylineOptions));

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case 0:
                Intent intent0=new Intent(TechnicianRoute.this,TechnicianDashboard.class);
                intent0.putExtras(techInfo);
                startActivity(intent0);
                break;
            case 1:
                Intent intent=new Intent(TechnicianRoute.this,TechnicianTasks.class);
                intent.putExtras(techInfo);
                startActivity(intent);
                break;
            case 2:
                Intent intent1=new Intent(TechnicianRoute.this,TechnicianRoute.class);
                intent1.putExtras(techInfo);
                startActivity(intent1);
                finish();
                break;
            case 3:
                Intent intent2=new Intent(TechnicianRoute.this,TechnicianSetting.class);
                intent2.putExtras(techInfo);
                startActivity(intent2);
                break;
            case 4:
                Intent intent3=new Intent(TechnicianRoute.this, TechnicianLogin.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent3);
                break;
            case R.id.routeMenu:
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                break;
            case R.id.googleRoute:
                mMap.clear();
                if(routeCount<orderedList.size()-1){
                    try {
                        new DirectionFinder(TechnicianRoute.this,orderedList.get(routeCount).latitude+","+orderedList.get(routeCount).longitude,orderedList.get(routeCount+1).latitude+","+orderedList.get(routeCount+1).longitude).execute();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    routeCount++;
                }else {
                    Toast.makeText(TechnicianRoute.this,"Congratulations! You have finished today's task",Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }
}
