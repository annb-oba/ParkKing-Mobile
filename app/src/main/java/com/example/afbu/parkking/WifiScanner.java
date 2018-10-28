package com.example.afbu.parkking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.afbu.parkking.FloorMapView.FloorMapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class WifiScanner {
    private WifiManager wifi;
    private Context mContext;
    private HandlerThread scannerHandlerThread, receiverHandlerThread;
    private Looper scannerLooper, receiverLooper;
    private Handler scannerHandler, receiverHandler;
    private Double routerDistance1, routerDistance2, routerDistance3;
    private ArrayList<Double> router1Signals, router2Signals, router3Signals;
    private Double userPositionX, userPositionY;
    private Router router1, router2, router3;
    private FloorMapView floorMapView;
    private Boolean floorFound;

    private WifiRangeScannerRunnable wifiRangeScannerRunnable;
    private BroadcastReceiver wifiScanReceiver;
    private IntentFilter broadcastReceiverIntentFilter;

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    public static final String CURRENT_FLOOR_ID = "currentFloorId";
    public static final String BROADCAST_RECEIVED ="bradcastReceived";


    private ArrayList<String> floorIdArray;
    private ArrayList<String> floorTitleArray;

    private ArrayList<Router> router1Array, router2Array, router3Array;

    class WifiRangeScannerRunnable implements Runnable {
        private volatile boolean running = true;
        private volatile boolean paused = false;
        private final Object pauseLock = new Object();

        @Override
        public void run() {
            while (running) {
                synchronized (pauseLock) {
                    if (!running) {
                        Log.w("LOG", "STOPPING SCAN THREAD");
                        return;
                    }
                    if (paused) {
                        try {
                            Log.w("LOG", "PAUSING SCAN THREAD");
                            pauseLock.wait();

                        } catch (InterruptedException ex) {
                            break;
                        }
                        if (!running) {
                            Log.w("LOG", "STOPPING SCAN THREAD");
                            break;
                        }
                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                wifi.startScan();
                pause();
            }
            Log.w("LOG", "Outside of while loop");
            return;
            //scannerHandler.postDelayed(this, 5000);
        }

        public void stop() {
            running = false;
            // you might also want to interrupt() the Thread that is
            // running this Runnable, too, or perhaps call:
            // resume();
            // to unblock
        }

        public void pause() {
            // you may want to throw an IllegalStateException if !running
            paused = true;
        }

        public void resume() {
            synchronized (pauseLock) {

                Log.w("LOG", "Resuming...");
                paused = false;
                pauseLock.notifyAll(); // Unblocks thread
            }
        }

        public void start() {
            running = true;
            resume();
        }

    }

    class WifiScannerRunnable implements Runnable {
        private volatile boolean running = true;
        private volatile boolean paused = false;
        private final Object pauseLock = new Object();
        Handler handler = new Handler();


        private int scanCount;

        @Override
        public void run() {

            if (Looper.myLooper() == Looper.getMainLooper()) {
                // Log.w("LOG", "RUN UI THREAD");
            } else {
                // Log.w("LOG", "RUN BACKGROUND THREAD");
            }
            // Log.w("LOG", "Batch Scan Starting");
            router1Signals = new ArrayList<Double>();
            router2Signals = new ArrayList<Double>();
            router3Signals = new ArrayList<Double>();
            start();
            while (running) {
                synchronized (pauseLock) {
                    if (!running) { // may have changed while waiting to
                        // synchronize on pauseLock
                        // Log.w("LOG", "Not Running");
                        break;
                    }
                    if (paused) {
                        try {
                            // Log.w("LOG", "Pause Wait");
                            pauseLock.wait();

                        } catch (InterruptedException ex) {
                            // Log.w("LOG", "Error");
                            break;
                        }
                        if (!running) { // running might have changed since we paused
                            // Log.w("LOG", "Not running while paused");
                            break;
                        }
                    }

                }
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    // Log.w("LOG", "WHILE UI THREAD");
                } else {
                    // Log.w("LOG", "WHILE BACKGROUND THREAD");
                }
                if (scanCount > 1) {
                    // Log.w("LOG", "Ending Batch Scan");
                    stop();
                    break;
                } else {
                    wifi.startScan();
                    // Log.w("LOG", "Starting Scan "+ Integer.toString(scanCount));
                    pause();
                    // Log.w("LOG", "Thread Running: "+ Boolean.toString(running));
                    // Log.w("LOG", "Paused: "+ Boolean.toString(paused));
                    scanCount++;
                }
                // Log.w("LOG", "Loop End");

                try {
                    sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Log.w("LOG", "Batch Scan Finished");
            for (int i = 0; i < router1Signals.size(); i++) {
                // Log.w("LOG", "Scan "+Integer.toString(i+1)+": "+Double.toString(router1Signals.get(i)));
            }
            routerDistance1 = averageSignals(router1Signals);
            routerDistance2 = averageSignals(router2Signals);
            routerDistance3 = averageSignals(router3Signals);
            // Log.w("LOG", "Router 1 Average: "+Double.toString(routerDistance1));
            // Log.w("LOG", "Router 2 Average: "+Double.toString(routerDistance2));
            // Log.w("LOG", "Router 3 Average: "+Double.toString(routerDistance3));

            setUserPosition(routerDistance1, routerDistance2, routerDistance3);

            // Log.w("LOG", "X: "+Double.toString(userPositionX));
            // Log.w("LOG", "Y: "+Double.toString(userPositionY));
            scannerHandler.postDelayed(this, 5000);
        }

        public void stop() {
            running = false;
            // you might also want to interrupt() the Thread that is
            // running this Runnable, too, or perhaps call:
            // resume();
            // to unblock
        }

        public void pause() {
            // you may want to throw an IllegalStateException if !running
            paused = true;
        }

        public void resume() {
            synchronized (pauseLock) {
                // Log.w("LOG", "Resuming...");
                paused = false;
                pauseLock.notifyAll(); // Unblocks thread
            }
        }

        public void start() {
            scanCount = 0;
            running = true;
            resume();
        }
    }

    public WifiRangeScannerRunnable getWifiRangeScannerRunnable() {
        return this.wifiRangeScannerRunnable;
    }

    public WifiScanner(Context mContext, FloorMapView floorMapView) {
        router1 = new Router(0d, 3.5d, 5d);
        router2 = new Router(5d, 3.5d, 5d);
        router3 = new Router(2.5d, 0d);
        this.mContext = mContext;
        this.floorMapView = floorMapView;


        wifi = (WifiManager) this.mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false) {
            Toast.makeText(this.mContext, "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            this.wifi.setWifiEnabled(true);
        }
        wifiScanStart();
    }

    public WifiScanner(Context mContext, ArrayList<Router> router1Array, ArrayList<Router> router2Array, ArrayList<Router> router3Array, ArrayList<String> floorIdArray, ArrayList<String> floorTitleArray) {

        editor = mContext.getSharedPreferences(this.CURRENT_FLOOR_ID, mContext.MODE_PRIVATE).edit();
        editor.putString("currentFloorID","");
        editor.commit();

        this.mContext = mContext;
        this.floorFound = false;
        this.router1Array = router1Array;
        this.router2Array = router2Array;
        this.router3Array = router3Array;
        this.floorIdArray = floorIdArray;
        this.floorTitleArray = floorTitleArray;
        wifi = (WifiManager) this.mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false) {
            Toast.makeText(this.mContext, "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            this.wifi.setWifiEnabled(true);
        }
        wifiRouterRangeScanStart();

        Log.w("LOG", "Wifi Scanner called Scan");

    }

    public void wifiRouterRangeScanStart() {
        wifiRangeScannerRunnable = new WifiRangeScannerRunnable();
        new Thread(wifiRangeScannerRunnable).start();
        receiverHandlerThread = new HandlerThread("ht");
        receiverHandlerThread.start();
        receiverLooper = receiverHandlerThread.getLooper();
        receiverHandler = new Handler(receiverLooper);
        scannerHandlerThread = new HandlerThread("scannerHandler");
        scannerHandlerThread.start();
        scannerLooper = scannerHandlerThread.getLooper();
        scannerHandler = new Handler(scannerLooper);
        final WifiManager finalWifi = wifi;


        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                Log.w("LOG", "Broadcast Received");
                List<ScanResult> results = finalWifi.getScanResults();
                int size = results.size();
                getRouterRangeSignal(size, results);
                wifiRangeScannerRunnable.resume();
            }
        };

        broadcastReceiverIntentFilter = new IntentFilter();
        broadcastReceiverIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(wifiScanReceiver, broadcastReceiverIntentFilter, null, receiverHandler);

//        mContext.registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context c, Intent intent) {
//                Log.w("LOG", "Broadcast Received");
//                List<ScanResult> results = finalWifi.getScanResults();
//                int size = results.size();
//                if(!floorFound){
//                    getRouterRangeSignal(size, results);
//                }
//                wifiRangeScannerRunnable.resume();
//            }
//        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION), null, receiverHandler);
    }

    public void getRouterRangeSignal(int size, List<ScanResult> results) {
        Boolean floorDetected=false;
        Log.w("LOG", "CHECKING IF IN RANGE OF FLOOR: ");
        String tempSSID;
        Double distance1=null,distance2=null,distance3=null;
        ArrayList<String> ssidResultsArray = new ArrayList<String>();
        for (int i = 0; i < results.size(); i++) {
            ssidResultsArray.add(results.get(i).SSID);

        }
        for (int i = 0; i < floorIdArray.size(); i++) {
            Log.w("LOG", "FloorID Array Size : " + Integer.toString(i + 1));
            if (ssidResultsArray.contains(router1Array.get(i).getSSID()) && ssidResultsArray.contains(router2Array.get(i).getSSID()) && ssidResultsArray.contains(router3Array.get(i).getSSID())) {
                Log.w("LOG", "A FLOOR'S ROUTERS WERE DETECTED : ");
                for (int j = 0; j < results.size(); j++) {
                    if (router1Array.get(i).getSSID().equals(results.get(j).SSID)) {
                         distance1 = calculateDistance(results.get(j).frequency, results.get(j).level);
                        Log.w("LOG", "DISTANCE FROM " + router1Array.get(i).getSSID() + ": " + Double.toString(distance1));
                    }
                    else if (router2Array.get(i).getSSID().equals(results.get(j).SSID)) {
                         distance2 = calculateDistance(results.get(j).frequency, results.get(j).level);
                        Log.w("LOG", "DISTANCE FROM " + router2Array.get(i).getSSID() + ": " + Double.toString(distance2));
                    }
                    else if (router3Array.get(i).getSSID().equals(results.get(j).SSID)) {
                         distance3 = calculateDistance(results.get(j).frequency, results.get(j).level);
                        Log.w("LOG", "DISTANCE FROM " + router3Array.get(i).getSSID() + ": " + Double.toString(distance3));
                    }
                }
                if(distance1!=null && distance2!=null && distance3!=null){
                    if(distance1<15d || distance2<15d || distance3<15d){
                        Log.w("LOG", "FLOOR FOUND: ");
                        sharedPreferences = mContext.getSharedPreferences(CURRENT_FLOOR_ID, mContext.MODE_PRIVATE);
                        String currentFloorID = sharedPreferences.getString("currentFloorID", "-1");
                        if (currentFloorID == "" || currentFloorID != floorIdArray.get(i)) {
                            Log.w("LOG","NEW FLOOR");
                            floorDetected=true;
                            editor.putString("currentFloorID", floorIdArray.get(i));
                            editor.commit();
                            Intent intent = new Intent("com.parkking.floor.id.broadcast");
                            intent.putExtra("floor_id", floorIdArray.get(i));
                            Log.w("LOG",floorIdArray.get(i));
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        }
                        else{
                            Log.w("LOG","SAME FLOOR");
                            floorDetected=true;
                            sharedPreferences = mContext.getSharedPreferences(BROADCAST_RECEIVED, mContext.MODE_PRIVATE);
                            String broadcastReceived = sharedPreferences.getString("broadcastReceived", "");
                            if(broadcastReceived.equals("")){
                                editor.putString("currentFloorID", "-1");
                                editor.commit();
                            }
                        }
                    }
                }
            }
        }

        if(floorDetected==true){
            //do nothing
        }
        else{
            editor.putString("currentFloorID", "-1");
            editor.commit();
        }
    }

    public void onDestroy() {
        mContext.unregisterReceiver(wifiScanReceiver);
    }

    public void wifiScanStart() {
        final WifiScannerRunnable wifiScannerRunnable = new WifiScannerRunnable();
        new Thread(wifiScannerRunnable).start();

        receiverHandlerThread = new HandlerThread("ht");
        receiverHandlerThread.start();
        receiverLooper = receiverHandlerThread.getLooper();
        receiverHandler = new Handler(receiverLooper);


        scannerHandlerThread = new HandlerThread("scannerHandler");
        scannerHandlerThread.start();
        scannerLooper = scannerHandlerThread.getLooper();
        scannerHandler = new Handler(scannerLooper);

        final WifiManager finalWifi = wifi;
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    // Log.w("LOG", "BROADCAST UI THREAD");
                } else {
                    // Log.w("LOG", "BROADCAST BACKGROUND THREAD");
                }
                // Log.w("LOG", "Broadcast Received");
                List<ScanResult> results = finalWifi.getScanResults();
                int size = results.size();
                // Log.w("LOG", "Results: "+ Integer.toString(results.size()));
                getSignals(size, results);
                wifiScannerRunnable.resume();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION), null, receiverHandler);
    }

    public void setUserPosition(double router1Distance, double router2Distance, double router3Distance) {
        Double userX, userY;
        Double distanceOfParallelRouters = this.router1.getParallelRouterDistance();
        Double initalX, initialY;   //X AND Y FROM THE PARALLEL ROUTERS
        Double router2AngleToUser;
        if ((router1Distance + router2Distance) > distanceOfParallelRouters) {
            router2AngleToUser = Math.toDegrees(Math.acos((router2Distance * router2Distance + distanceOfParallelRouters * distanceOfParallelRouters - router1Distance * router1Distance) / (2 * router2Distance * distanceOfParallelRouters)));  //COSINE LAW TO GET ANGLE TO USER
            initialY = router2Distance * Math.sin(Math.toRadians(router2AngleToUser));                                                             //Sin * H Ypostion to user
            initalX = router2Distance * Math.cos(Math.toRadians(router2AngleToUser));                                                             //cos * H Xpostion to user
            //NOTE X AND Y HERE ARE RELATIVE TO THE PARALLEL ROUTER'S INTERSECTIONS
            Double xFromRouter3 = router3.getxPosition() - initalX;
            Double yFromRouter3 = router3.getyPosition() - initialY;
            Double angleToRouter3 = Math.toDegrees(Math.atan(yFromRouter3 / xFromRouter3));
            Double distanceFromRouter3 = yFromRouter3 / (Math.sin(Math.toRadians(angleToRouter3)));
            Double distanceToRouter3Signal = Math.abs(router3Distance - distanceFromRouter3);
            userY = (distanceToRouter3Signal / 2) * Math.sin(Math.toRadians(angleToRouter3));
            userX = userY / Math.tan(Math.toRadians(angleToRouter3));


        } else {
            userX = ((distanceOfParallelRouters - (router1Distance + router2Distance)) / 2) + router2Distance;           //GETTING THE DISTANCE BETWEEN THE RADIUSES OF THE TWO ROUTERS AND ADDING IT TO ROUTER2 XCOOR TO GET CENTER
            if (router3Distance > router3.getyPosition()) {
                userY = 0d;
            } else {
                userY = router3Distance;
            }

        }
        userPositionX = userX;
        userPositionY = userY;
        updateUserPosition();

    }

    public void getSignals(int size, List<ScanResult> results) {
        // Log.w("LOG", "Getting Signals");
        // Log.w("LOG", "Size: "+Integer.toString(size));
        for (int i = 0; i < size; i++) {

            if (results.get(i).SSID.equals("TP-LINK_POCKET_3020_79B902")) {
                routerDistance1 = calculateDistance(results.get(i).frequency, results.get(i).level);
                router1Signals.add(routerDistance1);
                Log.w("JKLASD", "Router 1: " + Double.toString(routerDistance1));
            } else if (results.get(i).SSID.equals("TP-LINK_POCKET_3020_79BABA")) {
                routerDistance2 = calculateDistance(results.get(i).frequency, results.get(i).level);
                router2Signals.add(routerDistance2);
                Log.w("JKLASD", "Router 2: " + Double.toString(routerDistance2));
            } else if (results.get(i).SSID.equals("TP-LINK_43C122")) {
                routerDistance3 = calculateDistance(results.get(i).frequency, results.get(i).level);
                router3Signals.add(routerDistance3);
                Log.w("JKLASD", "Router 3: " + Double.toString(routerDistance3));
            }
        }
    }

    public static List<Double> getOutliers(List<Double> input) {
        // Log.w("LOG", "GETTING OUTLIERS");
        List<Double> output = new ArrayList<Double>();
        List<Double> data1 = new ArrayList<Double>();
        List<Double> data2 = new ArrayList<Double>();
        if (input.size() % 2 == 0) {
            data1 = input.subList(0, input.size() / 2);
            data2 = input.subList(input.size() / 2, input.size());
        } else {
            data1 = input.subList(0, input.size() / 2);
            data2 = input.subList(input.size() / 2 + 1, input.size());
        }
        double q1 = getMedian(data1);
        double q3 = getMedian(data2);
        double iqr = q3 - q1;
        double lowerFence = q1 - 1.5 * iqr;
        double upperFence = q3 + 1.5 * iqr;
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i) < lowerFence || input.get(i) > upperFence)
                output.add(input.get(i));
        }
        return output;
    }

    public static double getMedian(List<Double> data) {
        if (data.size() % 2 == 0)
            return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
        else
            return data.get(data.size() / 2);
    }

    public double calculateDistance(int frequency, int level) {
        double DISTANCE_MHZ_M = 27.55;
        return Math.pow(10.0, (DISTANCE_MHZ_M - (20 * Math.log10(frequency)) + Math.abs(level)) / 20.0);
    }

    public Double averageSignals(List<Double> signals) {
        Double average = 0.00;
        for (int i = 0; i < signals.size(); i++) {
            average += signals.get(i);
        }
        if (average != 0) {
            average /= signals.size();
        }
        return average;
    }

    public Boolean isInParkingLotRange(Double xRange, Double yRange) {
        return true;
    }

    public void updateUserPosition() {
        floorMapView.repositionUserBitmap(userPositionX, userPositionY);
        //Toast.makeText(mContext,"Y: "+Double.toString(userPositionY),Toast.LENGTH_SHORT).show();
    }
}
