package com.challenge.mandatehelper;

import android.support.annotation.Nullable;

public class PrintableItem {
    String header = "Missing header";
    String text = "Missing Text";
    String button = "Missing Button";

    public PrintableItem(String h, String t, String b) {
        header = h;
        text = t;
        button = b;
    }

    public String[] getPrintables() {
        return new String[]{ button, header, text };
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        PrintableItem incoming = (PrintableItem) obj;
        return incoming.header.equals(this.header) &&
                incoming.text.equals(this.text) &&
                incoming.button.equals(this.button);
    }
}
