package com.challenge.mandatehelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {
    protected void loadPrinterPreferences() {
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("printer_settings", Context.MODE_PRIVATE);
        String printer = prefs.getString("printer", null);
        String raw_connection = prefs.getString("connection", null);
        PrinterManager.CONNECTION connection = null;
        if (raw_connection != null && !raw_connection.equals("null")) {
            connection = PrinterManager.CONNECTION.valueOf(raw_connection);
        }
        String mode = prefs.getString("mode", null);

        if(printer != null) {
            PrinterManager.setModel(printer);
        }

        if (connection != null) {
            PrinterManager.setConnection(connection);
        }

        if (printer != null && connection != null) {
            PrinterManager.findPrinter(printer, connection);
        }

        if (mode != null) {
            switch(mode) {
                case "label":
                    PrinterManager.loadLabel();
                    break;
                case "roll":
                    PrinterManager.loadRoll();
                    break;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        loadPrinterPreferences();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SetupPrinterActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
