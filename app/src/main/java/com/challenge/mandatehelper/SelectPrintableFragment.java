package com.challenge.mandatehelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.Gson;

import java.util.ArrayList;

public class SelectPrintableFragment extends Fragment {
    protected void loadPrintables() {
        PrintableItems.reset();
        Gson gson = new Gson();
        SharedPreferences prefs = getContext()
                .getSharedPreferences("printable_items", Context.MODE_PRIVATE);
        int item_count = prefs.getInt("printable_item_count", 0);
        for (int i = item_count - 1; i >= 0; i--) {
            String item = prefs.getString("printable_item["+i+"]","");
            PrintableItem printable = gson.fromJson(item, PrintableItem.class);
            PrintableItems.add(printable);;
        }
    }

    private Button size(Button in) {
        Button out = in;
        out.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        out.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        return out;
    }

    public void createFragmentUI() {
        loadPrintables();

        ViewGroup radioParent = getActivity().findViewById(R.id.printable_options);
        Button custom = new Button(getContext());
        custom.setText(getResources().getString(R.string.custom_message_button_text));
        custom = size(custom);
        radioParent.addView(custom); // Need code to allow user to get a custom item added to the list.

        PrintableItems.setSelected(-1); // Reset the selected item... always.
        custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(SelectPrintableFragment.this)
                        .navigate(R.id.action_CreateItem);
            }
        });


        ArrayList<PrintableItem> options = PrintableItems.getOptions(getResources());
        for (int i = 0; i < options.size(); i++) {
            String[] printables = options.get(i).getPrintables();
            Button item = new Button(getContext());
            item.setText(printables[0]);
            item.setId(i);
            item = size(item); // set up the button.

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PrintableItems.setSelected(v.getId());

                    NavHostFragment.findNavController(SelectPrintableFragment.this)
                            .navigate(R.id.action_PrintItem);
                }
            }); // attach a click handler.

            radioParent.addView(item); // attach it to the view.
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.select_printable_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createFragmentUI();
    }
}
