package org.openobservatory.ooniprobe.fragment;

import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.XmlRes;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import org.openobservatory.ooniprobe.BuildConfig;
import org.openobservatory.ooniprobe.R;
import org.openobservatory.ooniprobe.activity.MainActivity;
import org.openobservatory.ooniprobe.activity.PreferenceActivity;
import org.openobservatory.ooniprobe.common.Application;
import org.openobservatory.ooniprobe.common.alarm.AlarmService;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Arrays;

import localhost.toolkit.app.fragment.ConfirmDialogFragment;
import localhost.toolkit.app.fragment.MessageDialogFragment;
import localhost.toolkit.preference.ExtendedPreferenceFragment;

public class PreferenceFragment extends ExtendedPreferenceFragment<PreferenceFragment> implements SharedPreferences.OnSharedPreferenceChangeListener, ConfirmDialogFragment.OnConfirmedListener {
    public static final String ARG_PREFERENCES_RES_ID = "org.openobservatory.ooniprobe.fragment.PreferenceFragment.PREF_RES_ID";
    private static final String ARG_CONTAINER_RES_ID = "org.openobservatory.ooniprobe.fragment.PreferenceFragment.CONTAINER_VIEW_ID";
    private String rootKey;

    public static PreferenceFragment newInstance(@XmlRes int preferencesResId, @IdRes int preferencesContainerResId, String rootKey) {
        PreferenceFragment fragment = new PreferenceFragment();
        fragment.setArguments(new Bundle());
        fragment.getArguments().putInt(ARG_PREFERENCES_RES_ID, preferencesResId);
        fragment.getArguments().putInt(ARG_CONTAINER_RES_ID, preferencesContainerResId);
        fragment.getArguments().putString(ARG_PREFERENCE_ROOT, rootKey);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        this.rootKey = rootKey;
    }

    private void showDateDialog(Preference timePreference) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                Locale en_locale = new Locale("en","EN");
                NumberFormat nf = NumberFormat.getInstance(en_locale);
                int hour = Integer.parseInt(nf.format(selectedHour));
                int minute = Integer.parseInt(nf.format(selectedMinute));
                String time = String.format(en_locale, "%02d", hour) + ":" + String.format(en_locale, "%02d", minute);
                SharedPreferences.Editor editor = getPreferenceScreen().getSharedPreferences().edit();
                editor.putString(getActivity().getString(R.string.automated_testing_time), time);
                editor.apply();
                timePreference.setSummary(String.format("%02d", selectedHour) + ":" + String.format("%02d", selectedMinute));
                AlarmService.setRecurringAlarm(getActivity().getApplication());
            }
        }, hour, minute, true);
        mTimePicker.show();
    }

    @Override
    public void onResume() {
        assert getArguments() != null;
        super.onResume();
        setPreferencesFromResource(getArguments().getInt(ARG_PREFERENCES_RES_ID), getArguments().getInt(ARG_CONTAINER_RES_ID), getArguments().getString(ARG_PREFERENCE_ROOT));
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        getActivity().setTitle(getPreferenceScreen().getTitle());

        Preference timePreference = findPreference(getActivity().getString(R.string.automated_testing_time));
        if (timePreference != null){
            String time = ((Application) getActivity().getApplication()).getPreferenceManager().getAutomatedTestingTime();
            timePreference.setSummary(time);
            timePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showDateDialog(timePreference);
                    return false;
                }
            });
        }

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            if (getPreferenceScreen().getPreference(i) instanceof EditTextPreference) {
                EditTextPreference editTextPreference = (EditTextPreference) getPreferenceScreen().getPreference(i);
                editTextPreference.setSummary(editTextPreference.getText());
            }
            if (getString(R.string.Settings_Websites_Categories_Label).equals(getPreferenceScreen().getPreference(i).getKey())) {
                String count = ((Application) getActivity().getApplication()).getPreferenceManager().countEnabledCategory().toString();
                getPreferenceScreen().getPreference(i).setSummary(getString(R.string.Settings_Websites_Categories_Description, count));
            }
        }
        Preference pref = findPreference(getString(R.string.send_email));
        if (pref != null)
            pref.setOnPreferenceClickListener(preference -> {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(getString(R.string.shareEmailTo)));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareSubject, BuildConfig.VERSION_NAME));
                emailIntent.putExtra(Intent.EXTRA_TEXT, "\nMANUFACTURER: " + Build.MANUFACTURER + "\nMODEL: " + Build.MODEL + "\nBOARD: " + Build.BOARD + "\nTIME: " + Build.TIME);
                try {
                    startActivity(Intent.createChooser(emailIntent, getString(R.string.Settings_SendEmail_Label)));
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.Settings_SendEmail_Error, Toast.LENGTH_SHORT).show();
                }
                return true;
            });
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        if (getActivity() instanceof MainActivity) {
            startActivity(PreferenceActivity.newIntent(getActivity(), getArguments().getInt(ARG_PREFERENCES_RES_ID), preferenceScreen.getKey()));
            return true;
        } else
            return super.onPreferenceStartScreen(preferenceFragmentCompat, preferenceScreen);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (key.equals(getString(R.string.include_cc)) && !sharedPreferences.getBoolean(key, true))
            new ConfirmDialogFragment.Builder()
                    .withExtra(key)
                    .withTitle(getString(R.string.Settings_Sharing_IncludeCountryCode))
                    .withMessage(getString(R.string.Settings_Sharing_IncludeCountryCode_PopUp))
                    .build().show(getChildFragmentManager(), null);
        else if (key.equals(getString(R.string.automated_testing_enabled))){
            if(sharedPreferences.getBoolean(key, false))
                //Enabling the alarm every time the user enabled automated_testing
                AlarmService.setRecurringAlarm(getActivity().getApplication());
            else
                AlarmService.cancelRecurringAlarm(getActivity().getApplication());
        }
        else if (preference instanceof EditTextPreference) {
            String value = sharedPreferences.getString(key, null);
            preference.setSummary(value);
            if (key.equals(getString(R.string.max_runtime)) && value != null && !TextUtils.isDigitsOnly(value)) {
                new MessageDialogFragment.Builder()
                        .withTitle(getString(R.string.Modal_Error))
                        .withMessage(getString(R.string.Modal_OnlyDigits))
                        .build().show(getFragmentManager(), null);
                sharedPreferences.edit().remove(key).apply();
                EditTextPreference p = (EditTextPreference) findPreference(key);
                if (p != null)
                    p.setText("");
            }
        } else if (preference instanceof SwitchPreferenceCompat) {
            //Call this code only in case of category or tests
            if (Arrays.asList(getActivity().getResources().getStringArray(R.array.CategoryCodes)).contains(key) ||
                    Arrays.asList(getActivity().getResources().getStringArray(R.array.preferenceTestsNames)).contains(key))
                checkAtLeastOneEnabled(sharedPreferences, key);
        }
    }

    private void checkAtLeastOneEnabled(SharedPreferences sharedPreferences, String key){
        boolean found = false;
        //cycle all preferences in the page and return true if at least one is enabled
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++)
            if (getPreferenceScreen().getPreference(i) instanceof SwitchPreferenceCompat && !getPreferenceScreen().getPreference(i).getKey().equals(getString(R.string.test_whatsapp_extensive)))
                found = found || sharedPreferences.getBoolean(getPreferenceScreen().getPreference(i).getKey(), true);
        if (!found) {
            new MessageDialogFragment.Builder()
                    .withMessage(getString(R.string.Modal_EnableAtLeastOneTest))
                    .build().show(getFragmentManager(), null);
            sharedPreferences.edit().remove(key).apply();
            SwitchPreferenceCompat p = (SwitchPreferenceCompat) findPreference(key);
            if (p != null)
                p.setChecked(true);
        }
    }

    @Override
    protected PreferenceFragment newConcreteInstance(String rootKey) {
        return PreferenceFragment.newInstance(getArguments().getInt(ARG_PREFERENCES_RES_ID), getArguments().getInt(ARG_CONTAINER_RES_ID), rootKey);
    }

    @Override
    public void onConfirmation(Serializable serializable, int i) {
        if (i == DialogInterface.BUTTON_NEGATIVE && serializable.equals(getString(R.string.include_cc))) {
            getPreferenceScreen().getSharedPreferences().edit().remove((String) serializable).apply();
            getFragmentManager().beginTransaction().replace(android.R.id.content, newConcreteInstance(rootKey)).commit();
        }
    }
}
