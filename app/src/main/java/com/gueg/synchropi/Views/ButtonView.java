package com.gueg.synchropi.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class ButtonView extends ControlView implements View.OnClickListener {

    private Paint circlePaint = new Paint();
    private Paint outerCirclePaint = new Paint();
    private Paint wavePaint = new Paint();

    private static final float WAVE_MAX_RADIUS = 20;
    private static final long WAVE_DURATION = 500;
    private static final long WAVE_FRAMES = 60;
    private Handler handler = new Handler();
    private boolean drawWave = false;
    private float radiusExtension;

    private float radius = -1f;
    private Point center;

    private OnAction listener;
    private String cmd;

    private int circleColor = 0xffff9900;
    private int outerCircleColor = 0xffff6600;
    private int waveColor = 0xffcc3300;


    public ButtonView(Context context) {
        this(context, null);
    }
    public ButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public ButtonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        textPaint.setColor(Color.WHITE);
        textPaint.setFakeBoldText(true);

        setOnClickListener(this);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        circlePaint.setColor(circleColor);
        outerCirclePaint.setColor(outerCircleColor);
        wavePaint.setColor(waveColor);

        if(center==null)
            center = new Point(canvas.getWidth()/2, canvas.getHeight()/2);

        /* Drawing button */
        if(radius == -1f)
            radius = canvas.getWidth()>canvas.getHeight()?(float)canvas.getHeight()/3f:(float)canvas.getWidth()/3f;
        canvas.drawCircle(center.x, center.y, (int)(radius*1.1), outerCirclePaint);

        if(drawWave) {
            radiusExtension++;
            canvas.drawCircle(center.x, center.y, radius+ radiusExtension, wavePaint);
        }

        canvas.drawCircle(center.x, center.y, radius, circlePaint);

        /* Drawing text */
        if(showTitle) {
            if (textPaint.getTextSize() != (canvas.getWidth()>canvas.getHeight()?(float)canvas.getHeight()/5f:(float)canvas.getWidth()/5f))
                textPaint.setTextSize(canvas.getWidth()>canvas.getHeight()?(float)canvas.getHeight()/5f:(float)canvas.getWidth()/5f);
            canvas.drawText(title, (float) canvas.getWidth() / 2f, (float) canvas.getHeight() /2f + textPaint.getTextSize()/3f, textPaint);
        }

        if(drawWave&& radiusExtension >=WAVE_MAX_RADIUS) {
            drawWave = false;
            invalidate();
        }
        if(drawWave)
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            },WAVE_DURATION/WAVE_FRAMES);
    }


    @Override
    public void onClick(View v) {
        if(listener!=null)
            listener.clicked(this,cmd,macs);
        handler.removeCallbacks(null);
        radiusExtension = 0;
        drawWave = true;
        invalidate();
    }

    public ButtonView setCommand(String cmd) {
        this.cmd = cmd;
        return this;
    }

    public ButtonView setActionlistener(OnAction listener) {
        this.listener = listener;
        return this;
    }

    public void setCircleColor(int c) {
        circleColor = c;
        invalidate();
    }

    public void setOuterCircleColor(int c) {
        outerCircleColor = c;
        invalidate();
    }

    public void setWaveColor(int c) {
        waveColor = c;
        invalidate();
    }

    public interface OnAction {
        void clicked(ButtonView v, String cmd, ArrayList<Integer> macs);
    }


}
