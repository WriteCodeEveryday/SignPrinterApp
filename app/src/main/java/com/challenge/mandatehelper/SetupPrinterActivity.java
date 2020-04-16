package com.challenge.mandatehelper;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.brother.ptouch.sdk.LabelInfo;
import com.brother.ptouch.sdk.Printer;



public class SetupPrinterActivity extends Activity {
    private void setUpPrinterOptions() {
        String currentModel = PrinterManager.getModel();
        PrinterManager.CONNECTION currentConnection = PrinterManager.getConnection();
        String currentMode = PrinterManager.getMode();

        final String[] supportedModels = PrinterManager.getSupportedModels();
        final PrinterManager.CONNECTION[] supportedConnections = PrinterManager.getSupportedConnections();

        RadioGroup connectors = this.findViewById(R.id.connection_selection_group);
        RadioGroup printers = this.findViewById(R.id.printer_selection_group);


        connectors.removeAllViews();
        for (int i = 0; i < supportedConnections.length; i++) {
            RadioButton button = new RadioButton(this);
            if (currentConnection != null) {
                button.setChecked(supportedConnections[i].compareTo(currentConnection) == 0);
            }
            button.setText(supportedConnections[i].toString());
            button.setId(i);
            final int j = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!v.isSelected()) {
                        PrinterManager.setConnection(supportedConnections[j]);
                        resetStatus();
                    }
                }
            });
            connectors.addView(button);
        }

        printers.removeAllViews();
        for (int i = 0; i < supportedModels.length; i++) {
            RadioButton button = new RadioButton(this);
            if (currentModel != null) {
                button.setChecked(supportedModels[i].equals(currentModel));
            }
            button.setText(supportedModels[i]);
            button.setId(i);
            final int j = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!v.isSelected()) {
                        PrinterManager.setModel(supportedModels[j]);
                        resetStatus();
                    }
                }
            });
            printers.addView(button);
        }

        if (currentMode != null) {
            RadioButton label = this.findViewById(R.id.radio_option_label);
            RadioButton roll = this.findViewById(R.id.radio_option_roll);

            switch (currentMode) {
                case "roll":
                    roll.setChecked(true);
                    break;
                case "label":
                    label.setChecked(true);
                    break;
            }
        }
    }

    private void resetStatus() {
        TextView status = this.findViewById(R.id.printer_status_text);
        status.setText(R.string.printer_status_text);

        RadioButton label = this.findViewById(R.id.radio_option_label);
        RadioButton roll = this.findViewById(R.id.radio_option_roll);
        label.setVisibility(View.GONE);
        roll.setVisibility(View.GONE);
    }

    private void updateStatus() {
        if (PrinterManager.getPrinter() != null) {
            final RadioButton label = this.findViewById(R.id.radio_option_label);
            final RadioButton roll = this.findViewById(R.id.radio_option_roll);

            final String[] options = PrinterManager.getLabelRoll();
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setUpPrinterOptions();
                    if (options.length == 2){
                        String newText = "" + label.getText();
                        newText = newText.split("\\(",-2)[0];
                        newText += "(" + options[0] + ")";
                        label.setText(newText);

                        newText = "" + roll.getText();
                        newText = newText.split("\\(",-2)[0];
                        newText += "(" + options[1] + ")";
                        roll.setText(newText);

                        label.setVisibility(View.VISIBLE);
                        roll.setVisibility(View.VISIBLE);
                        label.setEnabled(true);
                        roll.setEnabled(true);
                    }
                }
            });

            PrinterManager.CONNECTION conn = PrinterManager.getConnection();
            String model = PrinterManager.getModel();
            String mode = PrinterManager.getMode();
            String output = "";

            if (conn == null || model == null) {
                // Do nothing.
            }
            else if (mode == null) {
                output = getResources().getString(R.string.no_roll_button);
            } else {
                output = getResources().getString(R.string.printer_cancel_button);
            }

            final TextView status = this.findViewById(R.id.printer_status_text);
            final String finalOutput = output;
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    status.setText(finalOutput);
                }
            });
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_printer_activity);

        updateStatus();
        this.findViewById(R.id.radio_option_label).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new Thread() {
                    @Override
                    public void run() {
                        PrinterManager.loadLabel();
                        updateStatus();
                    }
                }.start();
            }
        });

        this.findViewById(R.id.radio_option_roll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new Thread() {
                    @Override
                    public void run() {
                        PrinterManager.loadRoll();
                        updateStatus();
                    }
                }.start();
            }
        });

        this.findViewById(R.id.printer_connect_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new Thread() {
                    @Override
                    public void run() {
                        String currentModel = PrinterManager.getModel();
                        PrinterManager.CONNECTION currentConnection = PrinterManager.getConnection();

                        if (currentConnection == null || currentModel == null) {
                            return;
                        }

                        if (PrinterManager.getConnection().equals(PrinterManager.CONNECTION.BLUETOOTH)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                            }

                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                                    .getDefaultAdapter();
                            if (bluetoothAdapter != null) {
                                if (!bluetoothAdapter.isEnabled()) {
                                    Intent enableBtIntent = new Intent(
                                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(enableBtIntent);
                                }
                            }
                        }

                        PrinterManager.findPrinter(PrinterManager.getModel(), PrinterManager.getConnection());
                        updateStatus();
                    }
                }.start();
            }
        });

        this.findViewById(R.id.printer_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setUpPrinterOptions();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
