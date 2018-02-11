package com.gueg.rasp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


public class ControlView extends View {

    private Paint textPaint = new Paint();
    private String title = "MV";
    private boolean showTitle = true;

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
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);


    }
}
