package com.gueg.synchropi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


public class ControlView extends View {

    protected Paint textPaint = new Paint();
    protected String title = "MV";
    protected String cmdId = "id";
    protected boolean showTitle = true;
    protected boolean allowClick = true;

    public ControlView(Context context) {
        this(context, null);
    }
    public ControlView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public ControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public ControlView setTitle(String t) {
        title = t;
        invalidate();
        return this;
    }


    public ControlView setShowTitle(boolean st) {
        showTitle = st;
        invalidate();
        return this;
    }

    public ControlView setCmdId(String id) {
        cmdId = id;
        return this;
    }

    public String getCmdId() {
        return cmdId;
    }

    public String getTitle() {
        return title;
    }

}
