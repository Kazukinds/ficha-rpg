package com.fichaeclipse.widgets;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#09090B"));
        root.setGravity(Gravity.CENTER);
        int pad = (int)(24*getResources().getDisplayMetrics().density);
        root.setPadding(pad,pad,pad,pad);

        TextView t = new TextView(this);
        t.setText("Ficha Eclipse — Widgets");
        t.setTextColor(Color.parseColor("#C8F542"));
        t.setTextSize(20);
        root.addView(t);

        TextView s = new TextView(this);
        s.setText("Long-press na home screen → Widgets → Ficha Eclipse → Dados\n\nArrasta pra tela inicial.");
        s.setTextColor(Color.parseColor("#A0A0B0"));
        s.setTextSize(14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = pad;
        s.setLayoutParams(lp);
        s.setGravity(Gravity.CENTER);
        root.addView(s);

        setContentView(root);
    }
}
