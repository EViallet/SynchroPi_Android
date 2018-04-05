package com.gueg.synchropi.Views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.gueg.synchropi.R;

public class LoadingView extends View {

    private Paint whitePaint = new Paint();
    private Paint emptyCirclePaint = new Paint();
    private Paint fullCirclePaint = new Paint();

    private float radius = -1f;

    private static final float MAX_ROTATION = 360f;

    private static final long FRAME_TIME = (long)(1000f/60f);
    private float lastAngle = 0f;
    private float turningStartingAngle = 0f;

    public LoadingView(Context context) {
        this(context, null);
    }
    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        whitePaint.setColor(Color.WHITE);
        //fullCirclePaint.setColor(0xff009900);
        fullCirclePaint.setColor(getResources().getColor(R.color.colorLoadingFull));
        emptyCirclePaint.setColor(getResources().getColor(R.color.colorLoadingEmpty));
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        RectF rectf = new RectF(canvas.getClipBounds());

        if(lastAngle>=2*MAX_ROTATION)
            lastAngle=0;
        if(turningStartingAngle>=MAX_ROTATION)
            turningStartingAngle=0;

        canvas.rotate(turningStartingAngle, (float) canvas.getWidth() / 2f, (float) canvas.getHeight() / 2f);
        if(lastAngle<=MAX_ROTATION) {
            canvas.drawArc(rectf, 0, MAX_ROTATION, true, emptyCirclePaint);
            canvas.drawArc(rectf, 0, lastAngle, true, fullCirclePaint);
        } else {
            canvas.drawArc(rectf, 0, MAX_ROTATION, true, fullCirclePaint);
            canvas.drawArc(rectf, 0, lastAngle-MAX_ROTATION, true, emptyCirclePaint);
        }
        lastAngle+=3;
        turningStartingAngle-=2;
        canvas.rotate(-turningStartingAngle, (float) canvas.getWidth() / 2f, (float) canvas.getHeight() / 2f);

        if (radius == -1f)
            radius = (float) canvas.getWidth() / 2f;
        canvas.drawCircle(radius, (float)canvas.getHeight()/2f, (float)canvas.getHeight()/3f, whitePaint);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        },FRAME_TIME);
    }

}
