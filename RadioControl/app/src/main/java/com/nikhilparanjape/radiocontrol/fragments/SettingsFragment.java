package com.nikhilparanjape.radiocontrol.fragments;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.borax12.materialdaterangepicker.time.RadialPickerLayout;
import com.borax12.materialdaterangepicker.time.TimePickerDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.receivers.WifiReceiver;
import com.nikhilparanjape.radiocontrol.rootUtils.RootAccess;
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;
import com.nikhilparanjape.radiocontrol.services.PersistenceService;

import java.io.File;
import java.util.Calendar;

/**
 * Created by Nikhil on 4/5/2016.
 */
public class SettingsFragment extends PreferenceFragment implements TimePickerDialog.OnTimeSetListener, FolderChooserDialog.FolderCallback {

    Context c;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        if (android.os.Build.VERSION.SDK_INT >= 24){
            getPreferenceScreen().findPreference("workMode").setEnabled(true);
            editor.putBoolean("workMode",true);
            editor.apply();
        }


        c = getActivity();
        final Utilities util = new Utilities();

        if(Utilities.isWifiOn(c)){
            getPreferenceScreen().findPreference("ssid").setEnabled(true);
        }
        else{
            getPreferenceScreen().findPreference("ssid").setEnabled(false);
        }


        Preference ssidListPref = findPreference("ssid");
        ssidListPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return false;
            }
        });

        Preference clearPref = findPreference("clear-ssid");
        clearPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ssidClearButton();
                return false;
            }
        });
        Preference airplaneResetPref = findPreference("reset-airplane");
        airplaneResetPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                new MaterialDialog.Builder(getActivity())
                        .iconRes(R.mipmap.ic_launcher)
                        .limitIconToDefaultSize()
                        .title("Please disable WiFi and make sure Airplane mode is off before pressing OK")
                        .positiveText("OK")
                        .negativeText("Cancel")
                        .backgroundColorRes(R.color.material_drawer_dark_background)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                String[] airOffCmd2 = {"su", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\""};
                                RootAccess.runCommands(airOffCmd2);
                                Toast.makeText(getActivity(),
                                        "Airplane mode reset", Toast.LENGTH_LONG).show();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            }
                        })
                        .checkBoxPromptRes(R.string.dont_ask_again, false, null)
                        .show();


                return false;
            }
        });

        final Preference logDirPref = findPreference("logDir");
        logDirPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                logDirectoryButton();
                return false;
            }
        });
        final Preference logDelPref = findPreference("logDel");
        logDelPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                logDeleteButton();
                return false;
            }
        });

        final CheckBoxPreference batteryOptimizePref = (CheckBoxPreference) getPreferenceManager().findPreference("isBatteryOn");
        final CheckBoxPreference workModePref = (CheckBoxPreference) getPreferenceManager().findPreference("workMode");
        workModePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
                if(newValue.toString().equals("true")) {
                    getPreferenceScreen().findPreference("altRootCommand").setEnabled(false);
                    if(batteryOptimizePref.isChecked()){
                        if (pref.getBoolean("workMode", true)) {
                            getActivity().startService(new Intent(getActivity(), PersistenceService.class));
                        } else {
                            registerForBroadcasts(c);
                        }
                    } else{
                        new MaterialDialog.Builder(getActivity())
                                .iconRes(R.mipmap.ic_launcher)
                                .limitIconToDefaultSize()
                                .title(getString(R.string.permissionIntelligent))
                                .positiveText("Allow")
                                .negativeText("Deny")
                                .backgroundColorRes(R.color.material_drawer_dark_background)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        batteryOptimizePref.setChecked(true);
                                        if (pref.getBoolean("workMode", true)) {
                                            getActivity().startService(new Intent(getActivity(), PersistenceService.class));
                                        } else {
                                            registerForBroadcasts(c);
                                        }
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        if (pref.getBoolean("workMode", true)) {
                                            getActivity().startService(new Intent(getActivity(), PersistenceService.class));
                                        } else {
                                            registerForBroadcasts(c);
                                        }
                                    }
                                })
                                .checkBoxPromptRes(R.string.dont_ask_again, false, null)
                                .show();
                    }

                }else{
                    getPreferenceScreen().findPreference("altRootCommand").setEnabled(true);
                    getActivity().stopService(new Intent(getActivity(), PersistenceService.class));
                }
                Log.i("RadioControl","workMode");

                return true;
            }
        });

        final CheckBoxPreference checkboxPref = (CheckBoxPreference) getPreferenceManager().findPreference("enableLogs");
        checkboxPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
                SharedPreferences.Editor editor = preferences.edit();
                if(newValue.toString().equals("true")){
                    //Request storage permissions if on MM or greater
                    if (Build.VERSION.SDK_INT >= 23) {
                        String[] perms = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

                        int permsRequestCode = 200;

                        requestPermissions(perms, permsRequestCode);
                        editor.putBoolean("enableLogs", true);
                        Log.d("RadioControl", "Logging enabled");

                    }
                    else{
                        editor.putBoolean("enableLogs", true);
                        Log.d("RadioControl", "Logging enabled");
                    }
                }
                else{
                    checkboxPref.setChecked(false);
                    editor.putBoolean("enableLogs", false);
                    Log.d("RadioControl", "Logging disabled");
                    File log = new File("radiocontrol.log");
                    if (log.exists()) {
                        log.delete();
                    }
                }
                editor.apply();
                return true;
            }
        });
        final CheckBoxPreference altRootCommand = (CheckBoxPreference) getPreferenceManager().findPreference("altRootCommand");
        altRootCommand.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
                } else {
                    altRootCommand.setChecked(false);
                }

                return true;
            }

        });



        final CheckBoxPreference dozeSetting = (CheckBoxPreference) getPreferenceManager().findPreference("isDozeOff");
        dozeSetting.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue.toString().equals("true")){
                    if (Build.VERSION.SDK_INT >= 23) {
                        Intent intent = new Intent();
                        String packageName = c.getPackageName();
                        PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
                        if (pm.isIgnoringBatteryOptimizations(packageName))
                            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        else {
                            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + packageName));
                            c.startActivity(intent);
                            return false;
                        }
                        dozeSetting.setChecked(true);
                        c.startActivity(intent);
                    }
                }
                else{
                    if (Build.VERSION.SDK_INT >= 23) {
                        Intent intent = new Intent();
                        String packageName = c.getPackageName();
                        PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + packageName));
                        c.startActivity(intent);
                        return false;
                    }
                }

                return true;
            }

        });

        if(altRootCommand.isChecked() || batteryOptimizePref.isChecked()){
            final SharedPreferences pref = c.getSharedPreferences("batteryOptimizePref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor2 = pref.edit();
            editor2.clear();
            editor2.apply();
            getPreferenceScreen().findPreference("altRootCommand").setEnabled(false);
        }
        else if(!batteryOptimizePref.isChecked()){
            getPreferenceScreen().findPreference("altRootCommand").setEnabled(true);
            getActivity().stopService(new Intent(getActivity(), PersistenceService.class));
        }

        final CheckBoxPreference eulaShow = (CheckBoxPreference) getPreferenceManager().findPreference("eulaShow");
        eulaShow.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue.toString().equals("true")){
                    new MaterialDialog.Builder(getActivity())
                            .iconRes(R.mipmap.ic_launcher)
                            .limitIconToDefaultSize()
                            .title(Html.fromHtml(getString(R.string.permissionSampleFirebase, getString(R.string.app_name))))
                            .positiveText("Allow")
                            .negativeText("Deny")
                            .backgroundColorRes(R.color.material_drawer_dark_background)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    FirebaseAnalytics.getInstance(c).setAnalyticsCollectionEnabled(true);
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    eulaShow.setChecked(false);
                                    FirebaseAnalytics.getInstance(c).setAnalyticsCollectionEnabled(false);
                                }
                            })
                            .checkBoxPromptRes(R.string.dont_ask_again, false, null)
                            .show();
                }
                else{
                    FirebaseAnalytics.getInstance(c).setAnalyticsCollectionEnabled(false);
                    eulaShow.setChecked(false);
                }

                return true;
            }
        });

        final CheckBoxPreference fabricCrashlyticsPref = (CheckBoxPreference) getPreferenceManager().findPreference("fabricCrashlytics");
        fabricCrashlyticsPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
                SharedPreferences.Editor editor = preferences.edit();

                if(newValue.toString().equals("true")){
                    new MaterialDialog.Builder(getActivity())
                            .iconRes(R.mipmap.ic_launcher)
                            .limitIconToDefaultSize()
                            .title(Html.fromHtml(getString(R.string.permissionSampleFabric, getString(R.string.app_name))))
                            .positiveText("Allow")
                            .negativeText("Deny")
                            .backgroundColorRes(R.color.material_drawer_dark_background)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    editor.putBoolean("allowFabric", true);
                                    editor.apply();
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    fabricCrashlyticsPref.setChecked(false);
                                    editor.putBoolean("allowFabric", false);
                                    editor.apply();

                            }
                            })
                            .checkBoxPromptRes(R.string.dont_ask_again, false, null)
                            .show();

                }
                else{
                    editor.putBoolean("allowFabric", false);
                    editor.apply();
                }


                return true;
            }

        });

        final CheckBoxPreference callingCheck = (CheckBoxPreference) getPreferenceManager().findPreference("isPhoneStateCheck");
        callingCheck.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, 200);

                } else {
                   callingCheck.setChecked(false);
                }

                return true;
            }

        });

        final CheckBoxPreference serviceCheckbox = (CheckBoxPreference) getPreferenceManager().findPreference("isAirplaneService");
        serviceCheckbox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
                SharedPreferences.Editor editor = preferences.edit();

                if(newValue.toString().equals("true")){
                    editor.putBoolean("isAirplaneService", true);
                    editor.apply();

                    String intervalTimeString = preferences.getString("interval_prefs","60");
                    int intervalTime = Integer.parseInt(intervalTimeString);
                    boolean airplaneService = preferences.getBoolean("isAirplaneService", false);

                    if(intervalTime != 0 && airplaneService){
                        Log.d("RadioControl", "Alarm Scheduled");
                        util.scheduleAlarm(c);
                    }
                }
                else{
                    Log.d("RadioControl", "Alarm Cancelled");
                    util.cancelAlarm(c);
                }


                return true;
            }
        });


        if(!getPreferenceScreen().findPreference("isAirplaneService").isEnabled()){
            getPreferenceScreen().findPreference("interval_prefs").setEnabled(false);
        }
        else{
            getPreferenceScreen().findPreference("interval_prefs").setEnabled(true);
        }

        //Initialize time picker
        Calendar now = Calendar.getInstance();
        final TimePickerDialog tpd = TimePickerDialog.newInstance(
                SettingsFragment.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        );
        tpd.setThemeDark(true);
        tpd.setAccentColor(R.color.mdtp_accent_color);

        Preference night_mode = findPreference("night-mode-service");
        night_mode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                tpd.show(getFragmentManager(), "Timepickerdialog");
                return false;
            }
        });



    }
    private void showAndroidOptimizationDialog(Context context){
        if (Build.VERSION.SDK_INT >= 23) {
            Intent intent = new Intent();
            String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm.isIgnoringBatteryOptimizations(packageName))
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            else {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
            }
            context.startActivity(intent);
        }

    }


    public void registerForBroadcasts(Context context) {
        ComponentName component = new ComponentName(context, WifiReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {
        final Utilities util = new Utilities();
        Context c = getActivity();
        String hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
        String minuteString = minute < 10 ? "0"+minute : ""+minute;
        String hourStringEnd = hourOfDayEnd < 10 ? "0"+hourOfDayEnd : ""+hourOfDayEnd;
        String minuteStringEnd = minuteEnd < 10 ? "0"+minuteEnd : ""+minuteEnd;
        String time = "You picked the following time: From - "+hourString+"h"+minuteString+" To - "+hourStringEnd+"h"+minuteStringEnd;

        util.cancelNightAlarm(c,hourOfDay, minute);

        util.scheduleNightWakeupAlarm(c, hourOfDayEnd, minuteEnd);
        Log.d("RadioControl", "Night Mode: " + time);
        Toast.makeText(getActivity(), "Night mode set from " + hourOfDay + ":" + minuteString + " to " + hourOfDayEnd + ":" + minuteStringEnd, Toast.LENGTH_LONG).show();
    }

    //Method for the ssid list clear button
    public void ssidClearButton(){
        Context c = getActivity();
        final SharedPreferences pref = c.getSharedPreferences("disabled-networks", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
        Toast.makeText(getActivity(),
                R.string.reset_ssid, Toast.LENGTH_LONG).show();
    }
    public void logDirectoryButton(){
        Toast.makeText(getActivity(),
                "Coming Soon!", Toast.LENGTH_LONG).show();
    }
    public void logDeleteButton(){
        File log = new File(c.getFilesDir(), "radiocontrol.log");
        boolean deleted = log.delete();
        Toast.makeText(getActivity(),
                "Log Deleted", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        // TODO
        final String tag = dialog.getTag(); // gets tag set from Builder, if you use multiple dialogs
        Toast.makeText(getActivity(),
                tag, Toast.LENGTH_LONG).show();
    }
}