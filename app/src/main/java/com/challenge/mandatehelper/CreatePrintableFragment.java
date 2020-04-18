package com.challenge.mandatehelper;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.navigation.fragment.NavHostFragment;

import java.util.Timer;


public class CreatePrintableFragment extends Fragment {
    private void enable(int id) {
        getActivity().findViewById(id).setEnabled(true);
    }
    private void hide(int id) {
        getActivity().findViewById(id).setVisibility(View.GONE);
    }
    private void show(int id) {
        getActivity().findViewById(id).setVisibility(View.VISIBLE);
    }
    private void setText(int id, String text) {
        ((TextView) getActivity().findViewById(id)).setText(text);
    }

    private String getTextById(int id) {
        EditText temp = getActivity().findViewById(id);
        return "" + temp.getText();
    }

    private void add() {
        System.out.println("Creating new printable " + getPrintable().getPrintables());
        PrintableItems.add(getPrintable());
        PrintableItems.setSelected(0); //Always gets added at the front.
    }

    private void replace() {
        System.out.println("Replacing printable " + PrintableItems.getSelected() + " " + getPrintable().getPrintables());
        PrintableItem replaced = getPrintable();
        PrintableItems.replace(PrintableItems.getSelected(), replaced);
    }

    private PrintableItem getPrintable() {
        String button = getTextById(R.id.buttonText);
        String header = getTextById(R.id.headerText);
        String text = getTextById(R.id.warningText);
        return new PrintableItem(header, text, button);
    }

    private void showPreview() {
        new Thread() {
            @Override
            public void run() {
                final LinearLayout temp = getActivity().findViewById(R.id.create_print_preview_layout);

                if (PrinterManager.getPrinter() != null && PrinterManager.getMode() != null) {
                    PrintableGenerator pr = new PrintableGenerator(PrinterManager.getModel(), PrinterManager.getMode());

                    PrintableItem item = getPrintable();

                    final TextView tapToRefresh = new TextView(getContext());
                    tapToRefresh.setText(getResources().getText(R.string.tap_to_preview_text));
                    tapToRefresh.setTextSize(14);
                    tapToRefresh.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showPreview();
                        }
                    });
                    tapToRefresh.setGravity(Gravity.CENTER);

                    final Button buttonPreview = new Button(getContext());
                    buttonPreview.setText(item.getPrintables()[0]);
                    buttonPreview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showPreview();
                        }
                    });

                    final ImageView preview = new ImageView(getContext());
                    preview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showPreview();
                        }
                    });
                    final Bitmap output = pr.buildOutput(item, getContext());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            preview.setImageBitmap(output);
                            temp.removeAllViews();
                            temp.addView(buttonPreview);
                            temp.addView(tapToRefresh);
                            temp.addView(preview);
                        }
                    });
                }
            }
        }.start();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.create_printable_fragment, container, false);
    }

    private void createUI(View view) {
        view.findViewById(R.id.save_and_print_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrintableItem selected = PrintableItems.getSelected();
                if (selected != null) {
                    replace();
                } else {
                    add(); //Needs to be here.
                }
                NavHostFragment.findNavController(CreatePrintableFragment.this)
                        .navigate(R.id.action_PrintItem);
            }
        });

        view.findViewById(R.id.cancel_create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(CreatePrintableFragment.this)
                        .navigate(R.id.action_CancelPrint);
            }
        });

        int [] inputs = new int[] { R.id.buttonText, R.id.headerText, R.id.warningText };
        final int [] outputs = new int[] { R.id.headerText, R.id.warningText, R.id.save_and_print_button };

        showPreview();
        PrintableItem selected = PrintableItems.getSelected();
        if (selected != null) {
            // Show all inputs right away.
            String[] printables = selected.getPrintables();
            for (int i = 0; i < inputs.length; i++) {
                enable(outputs[i]);
                show(outputs[i]);
                setText(inputs[i], printables[i]);
            }
        } else {
            for (int i = 0; i < inputs.length; i++) {
                final int j = i;
                hide(outputs[j]);
                view.findViewById(inputs[i]).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        enable(outputs[j]);
                        show(outputs[j]);
                    }
                });
            }
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
