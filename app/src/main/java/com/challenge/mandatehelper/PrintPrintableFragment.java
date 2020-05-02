package com.challenge.mandatehelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.navigation.fragment.NavHostFragment;

import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.ptouch.sdk.PrinterStatus;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class PrintPrintableFragment extends Fragment {
    private static final int REQUEST_WRITE_IMAGE = 1337;
    Bitmap printable;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.print_printable_fragment, container, false);
    }

    private void createUI(View view) {
        view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(PrintPrintableFragment.this)
                        .navigate(R.id.action_CancelPrint);
            }
        });

        view.findViewById(R.id.edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(PrintPrintableFragment.this)
                        .navigate(R.id.action_EditPrint);
            }
        });

        final EditText print_count = view.findViewById(R.id.print_count_text);
        print_count.setEnabled(false);

        if (PrinterManager.getPrinter() == null) {
            TextView preview = view.findViewById(R.id.print_preview_text);
            preview.setText(R.string.use_settings_to_add_printer_text);

            Button print =  view.findViewById(R.id.print_button);
            print.setText(R.string.no_printer_button);
            print.setEnabled(false);
        } else if (PrinterManager.getMode() == null) {
            TextView preview = view.findViewById(R.id.print_preview_text);
            preview.setText(R.string.use_settings_to_select_roll_text);

            Button print =  view.findViewById(R.id.print_button);
            print.setText(R.string.no_roll_button);
            print.setEnabled(false);
        }

        new Thread() {
            @Override
            public void run() {
                PrintableGenerator pr = new PrintableGenerator(getContext());
                printable = pr.buildOutput(PrintableItems.getSelected(), getContext());

                getActivity().findViewById(R.id.print_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Thread() {
                            @Override
                            public void run() {
                                PrinterManager.setWorkingDirectory(getContext());
                                Printer temp = PrinterManager.getPrinter();

                                int printed_count = 1;
                                try {
                                    printed_count = Integer.parseInt("" + print_count.getText());
                                } catch (NumberFormatException e) {
                                    System.out.println("Not a number");
                                }

                                temp.startCommunication();
                                for (int i = 1; i <= printed_count; i++) {
                                    Bitmap current =  Bitmap.createBitmap(printable);
                                    PrinterStatus result = temp.printImage(current);
                                    if (result.errorCode != PrinterInfo.ErrorCode.ERROR_NONE) {
                                        System.out.println("Error: " + result.errorCode);
                                    }
                                }
                                temp.endCommunication();
                            }
                        }.start();
                    }
                });

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout temp = getActivity().findViewById(R.id.print_preview_layout);
                        temp.removeAllViews();

                        ImageView preview = new ImageView(getContext());
                        preview.setImageBitmap(printable);
                        preview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                PrintableItem item = PrintableItems.getSelected();
                                if (item != null) {
                                    String[] printables = item.getPrintables();
                                    String name = printables[2].replaceAll("[^a-zA-Z0-9]", "_");

                                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                                    intent.setType("image/png");
                                    intent.putExtra(Intent.EXTRA_TITLE, name);
                                    startActivityForResult(intent, REQUEST_WRITE_IMAGE);
                                }
                            }
                        });
                        temp.addView(preview, 0);
                        print_count.setEnabled(true);

                        if (PrinterManager.getPrinter() != null && PrinterManager.getMode() != null) {
                            Button print =  getActivity().findViewById(R.id.print_button);
                            print.setText(R.string.print_button);
                            print.setEnabled(true);
                        }
                    }
                });
            }
        }.start();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        savePrintables();
        createUI(view);
    }

    public void onResume() {
        super.onResume();
        savePrintables();
        createUI(getView());
    }

    protected void savePrintables() {
        Gson gson = new Gson();
        SharedPreferences prefs = getContext()
                .getSharedPreferences("printable_items", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();


        ArrayList<PrintableItem> items =  PrintableItems.getOptions();
        editor.putInt("printable_item_count", items.size());
        for (int i = 0; i < items.size(); i++) {
            PrintableItem item = items.get(i);
            editor.putString("printable_item["+i+"]", gson.toJson(item));
        }

        editor.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WRITE_IMAGE) {
            if (resultCode == RESULT_OK) {
                PrintableGenerator pr = new PrintableGenerator(getContext());
                Bitmap image = pr.buildOutput(PrintableItems.getSelected(), getContext());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();
                image.recycle();

                try {
                    OutputStream out = getContext().getContentResolver().openOutputStream(data.getData());
                    out.write(bytes);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
