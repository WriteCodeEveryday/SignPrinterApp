package com.challenge.mandatehelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.google.gson.Gson;

import java.util.ArrayList;

public class PrintableItems {
    private static boolean resetDisabled = false;
    private static String defaultHeader = null;
    private static PrintableItem selected = null;
    private static ArrayList<PrintableItem> options = new ArrayList<PrintableItem>();

    private PrintableItems() {
        // No action.
    }

    public static ArrayList<PrintableItem> getOptions() {
        return options;
    }

    public static void loadDefaults(Resources resources) {
        defaultHeader = resources.getString(R.string.default_header_printable_text);
        options.add(new PrintableItem(defaultHeader,
                        resources.getString(R.string.social_distancing_warning_text),
                        resources.getString(R.string.social_distancing_button_text)));
        options.add(new PrintableItem(defaultHeader,
                        resources.getString(R.string.customer_limits_warning_text),
                        resources.getString(R.string.customer_limits_button_text)));
        options.add(new PrintableItem(defaultHeader,
                        resources.getString(R.string.wash_hands_warning_text),
                        resources.getString(R.string.wash_hands_button_text)));
        options.add(new PrintableItem(defaultHeader,
                        resources.getString(R.string.takeout_service_warning_text),
                        resources.getString(R.string.takeout_service_button_text)));
        options.add(new PrintableItem(defaultHeader,
                        resources.getString(R.string.staff_only_warning_text),
                        resources.getString(R.string.staff_only_button_text)));
        resetDisabled = true;
    }

    public static void reset() { // Reset the printables.
        if (!resetDisabled) {
            options = new ArrayList<PrintableItem>();
        } else {
            resetDisabled = !resetDisabled;
        }
    }

    public static PrintableItem get(int index) {
        return options.get(index);
    }

    public static void setSelected(int index) {
        if (index < 0 || index > options.size() - 1) {
            selected = null;
        } else {
            selected = options.get(index);
        }
    }

    public static PrintableItem getSelected() { return  selected; }

    public static void add(PrintableItem item) {
        if (!options.contains(item)) {
            options.add(0, item);
        }
    }

    public static void replace(PrintableItem current, PrintableItem replaced) {
        if (current == null) {
            add(replaced); // If we can't find it, push it.
        }

        int index = options.indexOf(current);
        if (index == -1) {
            add(replaced); // If we can't find it, push it.
        } else {
            options.set(index, replaced);
            setSelected(index);
        }
    }
}
