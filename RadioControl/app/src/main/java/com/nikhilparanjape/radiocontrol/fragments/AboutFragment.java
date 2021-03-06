package com.nikhilparanjape.radiocontrol.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;


import com.nikhilparanjape.radiocontrol.BuildConfig;
import com.nikhilparanjape.radiocontrol.activities.ChangeLogActivity;
import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.activities.TutorialActivity;
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Nikhil on 4/5/2016.
 */
public class AboutFragment extends PreferenceFragment {
    String versionName = BuildConfig.VERSION_NAME;
    private static final String PRIVATE_PREF = "prefs";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);


        final Context c = getActivity();
        SimpleChromeCustomTabs.initialize(c);

        if (Utilities.isConnected(c)) {
            getPreferenceScreen().findPreference("source").setEnabled(true);
        } else {
            getPreferenceScreen().findPreference("source").setEnabled(false);
        }

        Preference versionPref = findPreference("version");
        CharSequence cs = versionName;
        versionPref.setSummary("v" + cs);
        versionPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            int z = 0;

            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences sp = c.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE); //Initializes prefs.xml
                SharedPreferences.Editor editor = sp.edit();//Initializes xml editor
                z++;
                Log.d("RadioControl", (7 - z) + " steps away from easter egg");
                //Toast.makeText(getActivity(), (7 - z) + " steps away from easter egg", Toast.LENGTH_SHORT).show();
                if (z >= 7) {
                    if (!sp.getBoolean("isDeveloper", false)) {
                        Toast.makeText(getActivity(), R.string.dev_activated, Toast.LENGTH_LONG).show();
                        z = 0;
                        Log.d("RadioControl", "Developer features activated");


                        editor.putBoolean("isDeveloper", true); //Puts the boolean into prefs.xml
                        editor.apply(); //Ends writing to prefs file
                    } else if (sp.getBoolean("isDeveloper", false)) {
                        Toast.makeText(getActivity(), R.string.dev_deactivated, Toast.LENGTH_LONG).show();
                        z = 0;
                        Log.d("RadioControl", c.getString(R.string.dev_deactivated));


                        editor.putBoolean("isDeveloper", false); //Puts the boolean into prefs.xml
                        editor.apply(); //Ends writing to prefs file
                    }

                }
                return false;
            }

        });

        Preference myPref = findPreference("changelog");
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                changelog(c);
                return false;
            }
        });

        Preference tutorialPref = findPreference("tutorial");
        tutorialPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                tutorial(c);
                return false;
            }
        });

        Preference openSource = findPreference("source");
        openSource.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                displayLicensesAlertDialog(c);
                return false;
            }
        });


    }
    public static void getUpdate(){
        Document doc;
        try{
            URL xmlURL = new URL("http://nikhilp.org/radiocontrol/backend/update_check.xml");
            InputStream xml = xmlURL.openStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(xml);
            xml.close();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }


    }
    private void displayLicensesAlertDialog(Context c) {
        if(Utilities.isConnected(c)){
            String url = "https://nikhilp.org/radiocontrol/opensource";
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(424242);

            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
        } else{
            Snackbar.make(getView(), "No internet connection found", Snackbar.LENGTH_LONG)
                    .show();
        }

    }

    //whats new dialog
    private void changelog(Context c) {
        Intent i = new Intent(c, ChangeLogActivity.class);
        startActivity(i);
    }
    //whats new dialog
    private void tutorial(Context c) {
        Intent i = new Intent(c, TutorialActivity.class);
        startActivity(i);
    }


}
