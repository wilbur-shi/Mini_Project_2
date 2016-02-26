package com.example.wilbur.mini_project_2;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static final String TEXT_KEY = "textKey";
    public SharedPreferences sharedPreferences;
    private Button disappearingButton;
    private TextView appearingTextView;
    private EditText disappearingEditText;
    // Boolean checks for whether the onStop() method of the activity lifecycle is called
    // on the whole app closing or just going to another activity
    public boolean activityIsClosing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        disappearingButton = (Button) findViewById(R.id.disButton);
        appearingTextView = (TextView) findViewById(R.id.appearTextView);
        disappearingEditText = (EditText) findViewById(R.id.disEditText);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        disappearingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editTextString = disappearingEditText.getText().toString();
                if (editTextString.length() <= 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                            .setMessage("Error: there is no input text")
                            .setTitle("Error")
                            .setNegativeButton("OK", null)
                            .show();
                } else {
                    saveValues(editTextString);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                // the method onStop() will be called but the app itself is not closing.
                activityIsClosing = false;
                return true;
            case R.id.reset:
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Reset values")
                        .setMessage("Are you sure you want to reset values?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reset();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Reloads the page by displaying the string in the text view and also bolds the text
    private void reload() {
        String displayText = sharedPreferences.getString(TEXT_KEY, null);
        if(displayText != null) {
            appearingTextView.setText(displayText);
        }
        boolean bold = sharedPreferences.getBoolean("bold", false);
        if(bold) {
            appearingTextView.setTypeface(null, Typeface.BOLD);
            disappearingEditText.setTypeface(null, Typeface.BOLD);
        }
        else {
            appearingTextView.setTypeface(null);
            disappearingEditText.setTypeface(null);
        }
    }

    // Makes the button, edit text, and text view appear and disappear then removes
    // the saved value.
    private void reset() {
        disappearingButton.setVisibility(View.VISIBLE);
        disappearingEditText.setVisibility(View.VISIBLE);
        appearingTextView.setText(null);
        disappearingEditText.setText(null);
        appearingTextView.setVisibility(View.GONE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TEXT_KEY);
        editor.apply();
    }

    // Saves the user input text into the SharedPreferences
    private void saveValues(String text) {
        disappearingButton.setVisibility(View.GONE);
        disappearingEditText.setVisibility(View.GONE);
        appearingTextView.setVisibility(View.VISIBLE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TEXT_KEY, text);
        editor.apply();
        reload();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String txt = sharedPreferences.getString(TEXT_KEY, null);
        saveValues(txt);
        activityIsClosing = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(activityIsClosing) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(android.R.drawable.ic_notification_overlay)
                            .setContentText(sharedPreferences.getString(TEXT_KEY, ""))
                            .setAutoCancel(true);
            Intent resultIntent = new Intent(this, MainActivity.class);
            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            // Sets an ID for the notification
            int mNotificationId = 001;
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }
    }

}
