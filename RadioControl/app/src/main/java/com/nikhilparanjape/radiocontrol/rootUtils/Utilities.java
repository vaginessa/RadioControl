package com.nikhilparanjape.radiocontrol.rootUtils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.receivers.ActionReceiver;
import com.nikhilparanjape.radiocontrol.receivers.NightModeReceiver;
import com.nikhilparanjape.radiocontrol.receivers.RootServiceReceiver;
import com.nikhilparanjape.radiocontrol.receivers.TimedAlarmReceiver;
import com.nikhilparanjape.radiocontrol.receivers.WakeupReceiver;
import com.nikhilparanjape.radiocontrol.services.CellRadioService;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static android.app.AlarmManager.RTC;
import static android.app.AlarmManager.RTC_WAKEUP;


/**
 * Created by Nikhil on 2/3/2016.
 */
public class Utilities {

    private static final String PRIVATE_PREF = "prefs";
    private String[] airCmd = {"su", "settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true"};


    /**
     * gets network ssid
     * @param context
     * @return
     */
    public static String getCurrentSsid(Context context) {
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            ssid = connectionInfo.getSSID();
            ssid = ssid.substring(1, ssid.length()-1);
        }
        else if(!networkInfo.isConnected()){
            ssid = "Not Connected";
        }
        return ssid;
    }

    public static int getCellStatus(Context c){
        int z = 0;
        try {
            TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
            List<CellInfo> cellInfoList = tm.getAllCellInfo();
            //This means cell is off
            if (cellInfoList.isEmpty()) {
                z = 1;
            }
            return z;
        } catch(SecurityException e){
            Log.e("RadioControl","Unable to get Location Permission",e);
        } catch(NullPointerException e){
            Log.e("RadioControl","NullPointer ",e);
        }
        return z;
    }

    /**
     * Checks link speed
     * @param c
     * @return
     */
    public static int linkSpeed(Context c){
        WifiManager wifiManager = (WifiManager)c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int linkSpeed = wifiManager.getConnectionInfo().getLinkSpeed();
        Log.d("RadioControl", "Link speed = " + linkSpeed + "Mbps");
        return linkSpeed;
    }
    public void scheduleWakeupAlarm(Context c, int hour) {
        Calendar cal = Calendar.getInstance();
        // start 30 seconds after boot completed
        cal.add(Calendar.HOUR, hour);
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(c, WakeupReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, ActionReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(RTC_WAKEUP, firstMillis,
                cal.getTimeInMillis(), pIntent);

    }
    public void scheduleNightWakeupAlarm(Context c, int hourofDay, int minute) {
        Calendar cal = Calendar.getInstance();
        // start 30 seconds after boot completed
        cal.add(Calendar.HOUR_OF_DAY, hourofDay);
        cal.add(Calendar.MINUTE, minute);
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(c, TimedAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(RTC_WAKEUP, firstMillis,
                cal.getTimeInMillis(), pIntent);

    }

    // Setup a recurring alarm every 15,30,60 minutes
    public void scheduleAlarm(Context c) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        String intervalTimeString = preferences.getString("interval_prefs","10");
        int intervalTime = Integer.parseInt(intervalTimeString);
        Calendar cal = Calendar.getInstance();
        // start 30 seconds after boot completed
        cal.add(Calendar.MINUTE, intervalTime);
        Log.d("RadioControl","Interval: " + intervalTime + cal.getTime());

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(c, TimedAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        Log.d("RadioControl","Time: " + (cal.getTimeInMillis()-firstMillis));
        alarm.setInexactRepeating(RTC, firstMillis,
                cal.getTimeInMillis(), pIntent);

    }

    public void scheduleRootAlarm(Context c) {
        Calendar cal = Calendar.getInstance();
        // start 10 seconds
        cal.add(Calendar.SECOND, 30);

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(c, RootServiceReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, RootServiceReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every X seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY

        alarm.setInexactRepeating(RTC_WAKEUP, firstMillis,
                cal.getTimeInMillis(), pIntent);
        Log.d("RadioControl","RootClock enabled for " + cal.getTime());

    }
    public void cancelRootAlarm(Context c) {
        Intent intent = new Intent(c, RootServiceReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, RootServiceReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        Log.d("RadioControl","RootClock cancelled");
    }
    public void cancelAlarm(Context c) {
        Intent intent = new Intent(c, TimedAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
    public void cancelNightAlarm(Context c, int hourofDay, int minute) {
        Calendar cal = Calendar.getInstance();
        // start 30 seconds after boot completed
        cal.add(Calendar.HOUR_OF_DAY, hourofDay);
        cal.add(Calendar.MINUTE, minute);
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(c, NightModeReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, NightModeReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(RTC_WAKEUP, firstMillis,
                cal.getTimeInMillis(), pIntent);
    }

    public void cancelWakeupAlarm(Context c) {
        Intent intent = new Intent(c, WakeupReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, WakeupReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
    public static int frequency(Context c){
        WifiManager wifiManager = (WifiManager)c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int freq = wifiManager.getConnectionInfo().getFrequency();
        int GHz = freq/1000;
        if(GHz == 2){
            Log.d("RadioControl", "Frequency = " + freq + "MHz");
            return 2;
        }
        else if(GHz == 5){
            Log.d("RadioControl", "Frequency = " + freq + "MHz");
            return 5;
        }
        else
            return 0;

    }
    public static void getSecurity(Context c){
        WifiManager wifiManager = (WifiManager)c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration netConfig = new WifiConfiguration();

    }
    public static String getPingStats(String s) {
        try{
            String status;
            if (s.contains("0% packet loss")) {
                int start = s.indexOf("/mdev = ");
                int end = s.indexOf(" ms\n", start);
                s = s.substring(start + 8, end);
                String stats[] = s.split("/");
                return stats[2];
            } else if (s.contains("100% packet loss")) {
                status = "100% packet loss";
                return status;
            } else if (s.contains("% packet loss")) {
                status = "partial packet loss";
                return s;
            } else if (s.contains("unknown host")) {
                status = "unknown host";
                return status;
            } else {
                status = "unknown error in getPingStats";
                return status;
            }
        }catch(StringIndexOutOfBoundsException e){
            return "An error occured";
        }

    }




    public static String getFrequency(Context c){
        WifiManager wifiManager = (WifiManager)c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int freq = wifiManager.getConnectionInfo().getFrequency();
        int GHz = freq/1000;
        if(GHz == 2){
            Log.d("RadioControl", "Frequency = " + freq + "MHz");
            return "2.4 GHz";
        }
        else if(GHz == 5){
            Log.d("RadioControl", "Frequency = " + freq + "MHz");
            return "5 GHz";
        }
        else
            return null;
    }

    /**
     * Makes a network alert
     * @param context
     * @return
     */
    public static void sendNote(Context context, String mes, boolean vibrate, boolean sound, boolean heads){
        int priority;
        if(!heads){
            priority = 0;
        }
        else if(heads){
            priority = 1;
        }
        else{
            priority = 1;
        }
        PendingIntent pi = PendingIntent.getActivity(context, 1, new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK), 0);
        //Resources r = getResources();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_warning_black_48dp)
                .setContentTitle("RadioControl Network Alert")
                .setContentIntent(pi)
                .setContentText(mes)
                .setPriority(priority)
                .setAutoCancel(true)
                .build();

        if(vibrate){
            notification.defaults|= Notification.DEFAULT_VIBRATE;
        }
        if(sound){
            notification.defaults|= Notification.DEFAULT_SOUND;
        }


        notificationManager.notify(1, notification);

    }

    /**
     * Get the network info
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Enable WiFi without root
     *@param context
     */
    public void enableWifi(Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

    }

    /**
     * Disable WiFi without root
     *@param context
     */
    public void disableWifi(Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);

    }

    /**
     * Check if there is any connectivity to a Wifi network
     * @param context
     * @return
     */
    public static boolean isConnectedWifi(Context context){
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnectedOrConnecting() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }
    public static boolean isWifiOn(Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Check if there is any connectivity
     * @param context
     * @return
     */
    public static boolean isConnected(Context context){
        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isConnectedOrConnecting();
    }

    /**
     * Check if there is any connectivity to a mobile network
     * @param context
     * @return
     */
    public static boolean isConnectedMobile(Context context){
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnectedOrConnecting() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is any active call
     * @param context
     * @return
     */
    public boolean isCallActive(Context context){ AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(manager.getMode()== AudioManager.MODE_IN_CALL){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Check if there is fast connectivity
     * @param context
     * @return
     */
    public static boolean isConnectedFast(Context context){
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnectedOrConnecting() && isConnectionFast(info.getType(), info.getSubtype()));
    }

    public static boolean isAirplaneMode(Context context){
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    public static String getNetworkType(Context context){
        try {
            NetworkInfo info = getNetworkInfo(context);
            int type = info.getType();
            int subType = info.getSubtype();

            if (type == ConnectivityManager.TYPE_WIFI) {
                return "WIFI";
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                switch (subType) {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        return "1xRTT"; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        return "CDMA"; // ~ 14-64 kbps
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        return "EDGE"; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        return "EVDO_0"; // ~ 400-1000 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        return "EVDO_A"; // ~ 600-1400 kbps
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        return "GPRS"; // ~ 100 kbps
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        return "HSDPA"; // ~ 2-14 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        return "HSPA"; // ~ 700-1700 kbps
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        return "HSUPA"; // ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        return "UMTS"; // ~ 400-7000 kbps
            /*
             * Above API level 7, make sure to set android:targetSdkVersion
             * to appropriate level to use these
             */
                    case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                        return "EHRPD"; // ~ 1-2 Mbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                        return "EVDO_B"; // ~ 5 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                        return "HSPAP"; // ~ 10-20 Mbps
                    case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                        return "IDEN"; // ~25 kbps
                    case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                        return "LTE"; // ~ 10+ Mbps
                    // Unknown
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    default:
                        return "UNKNOWN";
                }
            } else {
                return "UNKNOWN";
            }
        }
        catch(NullPointerException e){
            return "CELL";
        }

    }

    /**
     * Check if the connection is fast
     * @param type
     * @param subType
     * @return
     */
    public static boolean isConnectionFast(int type, int subType){
        if(type==ConnectivityManager.TYPE_WIFI){
            return true;
        }else if(type == ConnectivityManager.TYPE_MOBILE){
            switch(subType){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
            /*
             * Above API level 7, make sure to set android:targetSdkVersion
             * to appropriate level to use these
             */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        }else{
            return false;
        }
    }

    public void pingTask(Context context){
        Runtime runtime = Runtime.getRuntime();
        try {
            //Wait for network to be connected fully
            while(!Utilities.isConnected(context)){
                Thread.sleep(1000);
            }
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");//Send 1 packet to google and check if it came back
            int exitValue = ipProcess.waitFor();
            Log.d("RadioControl", "Ping test returned " + exitValue);

            SharedPreferences sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            boolean alertPriority = prefs.getBoolean("networkPriority", false);//Setting for network notifier
            boolean alertSounds = prefs.getBoolean("networkSound",false);
            boolean alertVibrate = prefs.getBoolean("networkVibrate",false);


            if(sp.getInt("isActive",0) == 0){
                //If the connection can't reach Google
                if(exitValue != 0){
                    Utilities.sendNote(context, context.getString(R.string.not_connected_alert),alertVibrate,alertSounds,alertPriority);
                }
            }
            else if(sp.getInt("isActive",0) == 1){
                //If the connection can't reach Google
                if(exitValue != 0){
                    Utilities.sendNote(context, context.getString(R.string.not_connected_alert),alertVibrate,alertSounds,alertPriority);
                }
                else{
                    //Runs the alternate root command
                    if(prefs.getBoolean("altRootCommand", false)){
                        Intent cellIntent = new Intent(context, CellRadioService.class);
                        context.startService(cellIntent);
                        scheduleRootAlarm(context);
                        Log.d("RadioControl", "Cell Radio has been turned off");
                    }
                    else if(!prefs.getBoolean("altRootCommand", false)){
                        RootAccess.runCommands(airCmd);
                        Log.d("RadioControl", "Airplane mode has been turned on");
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    private class AsyncBackgroundTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Runtime runtime = Runtime.getRuntime();
            try {
                Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
                int exitValue = ipProcess.waitFor();
                Log.d("RadioControl", "Ping test returned " + exitValue);
                return (exitValue == 0);
            }
            catch (IOException | InterruptedException e){ e.printStackTrace(); }

            return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {

        }
    }
}
