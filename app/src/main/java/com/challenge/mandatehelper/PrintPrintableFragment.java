package com.challenge.mandatehelper;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputFilter;
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

public class PrintPrintableFragment extends Fragment {
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
        } else {
            new Thread() {
                @Override
                public void run() {
                    PrintableGenerator pr = new PrintableGenerator(PrinterManager.getModel(), PrinterManager.getMode());
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
                            ImageView preview = new ImageView(getContext());
                            preview.setImageBitmap(printable);
                            temp.addView(preview, 0);
                            print_count.setEnabled(true);

                            Button print =  getActivity().findViewById(R.id.print_button);
                            print.setText(R.string.print_button);
                            print.setEnabled(true);
                        }
                    });
                }
            }.start();
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createUI(view);
    }

    public void onResume() {
        super.onResume();
        createUI(getView());
    }
}
