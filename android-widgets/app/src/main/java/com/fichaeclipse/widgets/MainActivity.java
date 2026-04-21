package com.fichaeclipse.widgets;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private float d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        d = getResources().getDisplayMetrics().density;

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#09090B"));
        scroll.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, dp(32), pad, dp(32));
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Hero
        TextView brand = new TextView(this);
        brand.setText("FICHA ECLIPSE");
        brand.setTextColor(Color.parseColor("#C8F542"));
        brand.setTextSize(12);
        brand.setLetterSpacing(0.3f);
        brand.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
        root.addView(brand);

        TextView title = new TextView(this);
        title.setText("Widgets");
        title.setTextColor(Color.parseColor("#F0F0F5"));
        title.setTextSize(34);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.topMargin = dp(4);
        title.setLayoutParams(tlp);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Acesso rápido pra tua ficha direto da home screen.");
        sub.setTextColor(Color.parseColor("#A0A0B0"));
        sub.setTextSize(14);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = dp(8);
        sub.setLayoutParams(slp);
        root.addView(sub);

        // Instructions card
        LinearLayout card = card();
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.topMargin = dp(24);
        card.setLayoutParams(clp);

        TextView ch = new TextView(this);
        ch.setText("Como instalar");
        ch.setTextColor(Color.parseColor("#C8F542"));
        ch.setTextSize(13);
        ch.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
        ch.setLetterSpacing(0.2f);
        card.addView(ch);

        card.addView(step("1", "Long-press na home screen"));
        card.addView(step("2", "Toca em \"Widgets\""));
        card.addView(step("3", "Procura \"Ficha Eclipse\""));
        card.addView(step("4", "Arrasta o widget pra tela"));

        root.addView(card);

        // Available widgets
        TextView avail = new TextView(this);
        avail.setText("DISPONÍVEIS");
        avail.setTextColor(Color.parseColor("#5A5A6E"));
        avail.setTextSize(11);
        avail.setLetterSpacing(0.3f);
        avail.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alp.topMargin = dp(28);
        alp.leftMargin = dp(4);
        avail.setLayoutParams(alp);
        root.addView(avail);

        root.addView(widgetRow("Dados", "Rolar d4–d100", "#C8F542"));

        // Footer
        TextView foot = new TextView(this);
        foot.setText("v1.0 · fichaeclipse");
        foot.setTextColor(Color.parseColor("#5A5A6E"));
        foot.setTextSize(11);
        foot.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL);
        foot.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flp.topMargin = dp(32);
        foot.setLayoutParams(flp);
        root.addView(foot);

        scroll.addView(root);
        setContentView(scroll);
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#131316"));
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), Color.parseColor("#2A2A32"));
        c.setBackground(bg);
        int p = dp(20);
        c.setPadding(p, p, p, p);
        return c;
    }

    private View step(String n, String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = dp(14);
        row.setLayoutParams(rlp);

        TextView num = new TextView(this);
        num.setText(n);
        num.setTextColor(Color.parseColor("#C8F542"));
        num.setTextSize(13);
        num.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
        num.setGravity(Gravity.CENTER);
        GradientDrawable nb = new GradientDrawable();
        nb.setShape(GradientDrawable.OVAL);
        nb.setColor(Color.parseColor("#1F2610"));
        nb.setStroke(dp(1), Color.parseColor("#3A4A1A"));
        num.setBackground(nb);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(dp(28), dp(28));
        nlp.rightMargin = dp(12);
        num.setLayoutParams(nlp);
        row.addView(num);

        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(Color.parseColor("#D0D0D8"));
        t.setTextSize(15);
        row.addView(t);

        return row;
    }

    private View widgetRow(String name, String desc, String accent) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#131316"));
        bg.setCornerRadius(dp(14));
        bg.setStroke(dp(1), Color.parseColor("#2A2A32"));
        row.setBackground(bg);
        int p = dp(16);
        row.setPadding(p, p, p, p);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = dp(10);
        row.setLayoutParams(rlp);

        View dot = new View(this);
        GradientDrawable db = new GradientDrawable();
        db.setShape(GradientDrawable.OVAL);
        db.setColor(Color.parseColor(accent));
        dot.setBackground(db);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(dp(10), dp(10));
        dlp.rightMargin = dp(14);
        dot.setLayoutParams(dlp);
        row.addView(dot);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        col.setLayoutParams(clp);

        TextView n = new TextView(this);
        n.setText(name);
        n.setTextColor(Color.parseColor("#F0F0F5"));
        n.setTextSize(16);
        n.setTypeface(null, android.graphics.Typeface.BOLD);
        col.addView(n);

        TextView dv = new TextView(this);
        dv.setText(desc);
        dv.setTextColor(Color.parseColor("#A0A0B0"));
        dv.setTextSize(13);
        col.addView(dv);

        row.addView(col);

        TextView arrow = new TextView(this);
        arrow.setText("→");
        arrow.setTextColor(Color.parseColor("#5A5A6E"));
        arrow.setTextSize(18);
        row.addView(arrow);

        return row;
    }

    private int dp(int v) {
        return (int) (v * d);
    }
}
