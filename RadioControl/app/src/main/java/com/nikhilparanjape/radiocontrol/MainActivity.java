package com.nikhilparanjape.radiocontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;

import static android.provider.Settings.Global.AIRPLANE_MODE_ON;


public class MainActivity extends Activity {
    private static final String PRIVATE_PREF = "radiocontrol-prefs";
    private static final String VERSION_KEY = "version_number";
    Model[] modelItems;
    public static ArrayList<String> ssidList = new ArrayList<String>();
    String ssidlist[];
    private static TextView airplane;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        String versionName = BuildConfig.VERSION_NAME;
        SharedPreferences pref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        airplane = (TextView) findViewById(R.id.airStatus);

        boolean isEnabled = Settings.System.getInt(this.getContentResolver(), AIRPLANE_MODE_ON, 0) == 1;
        if(isEnabled){
            airplane.setText("ON");
            airplane.setTextColor(Color.GREEN);
        }
        else{
            airplane.setText("OFF");
            airplane.setTextColor(Color.RED);
        }

        //Save button for the network list
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                String arrayString = pref.getString("disabled_networks", "1");

                final EditText field = (EditText) findViewById(R.id.editText);

                // get value in field
                String value = field.getText().toString();
                if (value.length() != 0) {
                    //Check if the list contains the entered SSID
                    if(!arrayString.contains(value)){
                        ssidList.add(value);
                        // pair the value in text field with the key
                        editor.putString("disabled_networks", ssidList.toString());
                        field.setText("");
                        Toast.makeText(MainActivity.this,
                                "SSID saved", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this,
                                "SSID already exists", Toast.LENGTH_LONG).show();
                        field.setText("");
                    }
                }
                editor.commit();
            }

        });

        //Clear button for the network list
        Button btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.remove("disabled_networks");
                Toast.makeText(MainActivity.this,
                        "Disabled SSID list cleared", Toast.LENGTH_LONG).show();
                editor.apply();
            }

        });
        if(getDeviceName().contains("Nexus 6P")){
            //Creates navigation drawer header
            AccountHeader headerResult = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(R.mipmap.header)
                    .addProfiles(
                            new ProfileDrawerItem().withName(getDeviceName()).withEmail(versionName).withIcon(getResources().getDrawable(R.mipmap.huawei))
                    )
                    .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                        @Override
                        public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                            return false;
                        }
                    })
                    .build();
            //Creates navigation drawer items
            PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Home").withIcon(GoogleMaterial.Icon.gmd_wifi);
            SecondaryDrawerItem item2 = new SecondaryDrawerItem().withName("Settings").withIcon(GoogleMaterial.Icon.gmd_settings);
            SecondaryDrawerItem item3 = new SecondaryDrawerItem().withName("About").withIcon(GoogleMaterial.Icon.gmd_info);

            //Create navigation drawer
            Drawer result = new DrawerBuilder()
                    .withAccountHeader(headerResult)
                    .withActivity(this)
                    .withTranslucentStatusBar(false)
                    .withActionBarDrawerToggle(false)
                    .addDrawerItems(
                            item1,
                            new DividerDrawerItem(),
                            item2,
                            item3
                    )

                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            Log.d("drawer", "The drawer is: " + drawerItem + " position is " + position);
                            //Settings button
                            if (position == 3) {
                                startSettingsActivity();
                                Log.d("drawer", "Started settings activity");
                            }
                            //About button
                            else if (position == 4) {
                                startAboutActivity();
                                Log.d("drawer", "Started about activity");
                            }
                            return false;
                        }
                    })
                    .build();
            result.setSelection(1);
        }
        else if(getDeviceName().contains("Motorola Nexus 6")){
            //Creates navigation drawer header
            AccountHeader headerResult = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(R.mipmap.header)
                    .addProfiles(
                            new ProfileDrawerItem().withName(getDeviceName()).withEmail(versionName).withIcon(getResources().getDrawable(R.mipmap.moto2))
                    )
                    .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                        @Override
                        public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                            return false;
                        }
                    })
                    .build();
            //Creates navigation drawer items
            PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Home").withIcon(GoogleMaterial.Icon.gmd_wifi);
            SecondaryDrawerItem item2 = new SecondaryDrawerItem().withName("Settings").withIcon(GoogleMaterial.Icon.gmd_settings);
            SecondaryDrawerItem item3 = new SecondaryDrawerItem().withName("About").withIcon(GoogleMaterial.Icon.gmd_info);

            //Create navigation drawer
            Drawer result = new DrawerBuilder()
                    .withAccountHeader(headerResult)
                    .withActivity(this)
                    .withTranslucentStatusBar(false)
                    .withActionBarDrawerToggle(false)
                    .addDrawerItems(
                            item1,
                            new DividerDrawerItem(),
                            item2,
                            item3
                    )

                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            Log.d("drawer", "The drawer is: " + drawerItem + " position is " + position);
                            //Settings button
                            if (position == 3) {
                                startSettingsActivity();
                                Log.d("drawer", "Started settings activity");
                            }
                            //About button
                            else if (position == 4) {
                                startAboutActivity();
                                Log.d("drawer", "Started about activity");
                            }
                            return false;
                        }
                    })
                    .build();
            result.setSelection(1);
        }
        else{
            //Creates navigation drawer header
            AccountHeader headerResult = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(R.mipmap.header)
                    .addProfiles(
                            new ProfileDrawerItem().withName(getDeviceName()).withEmail(versionName)
                    )
                    .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                        @Override
                        public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                            return false;
                        }
                    })
                    .build();
            //Creates navigation drawer items
            PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Home").withIcon(GoogleMaterial.Icon.gmd_wifi);
            SecondaryDrawerItem item2 = new SecondaryDrawerItem().withName("Settings").withIcon(GoogleMaterial.Icon.gmd_settings);
            SecondaryDrawerItem item3 = new SecondaryDrawerItem().withName("About").withIcon(GoogleMaterial.Icon.gmd_info);

            //Create navigation drawer
            Drawer result = new DrawerBuilder()
                    .withAccountHeader(headerResult)
                    .withActivity(this)
                    .withTranslucentStatusBar(false)
                    .withActionBarDrawerToggle(false)
                    .addDrawerItems(
                            item1,
                            new DividerDrawerItem(),
                            item2,
                            item3
                    )

                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            Log.d("drawer", "The drawer is: " + drawerItem + " position is " + position);
                            //Settings button
                            if (position == 3) {
                                startSettingsActivity();
                                Log.d("drawer", "Started settings activity");
                            }
                            //About button
                            else if (position == 4) {
                                startAboutActivity();
                                Log.d("drawer", "Started about activity");
                            }
                            return false;
                        }
                    })
                    .build();
            result.setSelection(1);
        }


    }

    //Init for the Whats new dialog
    private void init() {
        SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        int currentVersionNumber        = 0;

        int savedVersionNumber          = sharedPref.getInt(VERSION_KEY, 0);

        try {
            PackageInfo pi          = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionNumber    = pi.versionCode;
        } catch (Exception e) {}

        if (currentVersionNumber > savedVersionNumber) {
            showWhatsNewDialog();

            SharedPreferences.Editor editor   = sharedPref.edit();

            editor.putInt(VERSION_KEY, currentVersionNumber);
            editor.commit();
        }
    }
    //starts about activity
    public void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
    //starts settings activity
    public void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    //whats new dialog
    private void showWhatsNewDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);//Creates layout inflator for dialog
        View view = inflater.inflate(R.layout.dialog_whatsnew, null);//Initializes the view for whats new dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//creates alertdialog

        builder.setView(view).setTitle("                 Whats New - Alpha")//sets title
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
    //Grab device make and model for drawer
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    //Capitalizes names for devices. Used by getDeviceName()
    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
    public static void airStatus(boolean tr){

        if(tr){
            airplane.setText("ON");
            airplane.setTextColor(Color.GREEN);
        }
        else{
            airplane.setText("OFF");
            airplane.setTextColor(Color.RED);
        }
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
