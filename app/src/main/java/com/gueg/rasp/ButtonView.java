package com.gueg.rasp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class ButtonView extends View implements View.OnTouchListener {

    private Paint circlePaint = new Paint();
    private Paint outerCirclePaint = new Paint();
    private Paint checkPaint = new Paint();
    private Paint textPaint = new Paint();

    private String title = "Switch";
    private boolean showTitle = true;
    private boolean switched = false;
    private boolean allowClick = true;
    private OnSwitch listener;

    private float radius = -1f;
    private Point center;
    private float[] check;
    private float[] cross;

    private int trueColor = Color.GREEN;
    private int falseColor = Color.RED;

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

        outerCirclePaint.setColor(Color.BLACK);
        checkPaint.setColor(Color.WHITE);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);

        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        if(!allowClick)
            return false;
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int relativeX = (int)e.getX() - center.x,
                        relativeY = (int)e.getY() - center.y;

                if(norme(relativeX,relativeY) <= radius) {
                    switched = !switched;

                    if (listener != null)
                        listener.onSwitch(this, switched);
                    invalidate();
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(center==null)
            center = new Point(canvas.getWidth()/2, canvas.getHeight()/2);

        /* Drawing button */
        if(radius == -1f)
            radius = (float)canvas.getWidth()/3f;
        canvas.drawCircle(center.x, center.y, (int)(radius*1.1), outerCirclePaint);

        /* Drawing check mark */
        if(checkPaint.getStrokeWidth()!=canvas.getWidth()/10)
            checkPaint.setStrokeWidth(canvas.getWidth()/10);
        if(switched) {
            circlePaint.setColor(trueColor);
            canvas.drawCircle(center.x, center.y, radius, circlePaint);
            if(check==null)
                check = new float[] {
                        center.x+radius*3/7,
                        center.y-radius*4/7,
                        center.x-radius*1/7,
                        center.y+radius*3/7,
                        center.x,
                        center.y+radius*2.75f/7,
                        center.x-radius*4/7,
                        center.y+radius*0.75f/7
                };
            canvas.drawLines(check, checkPaint);
        } else {
            circlePaint.setColor(falseColor);
            canvas.drawCircle(center.x, center.y, radius, circlePaint);
            if(cross==null)
                cross = new float[] {
                        center.x+radius*3/7,
                        center.y+radius*3/7,
                        center.x-radius*3/7,
                        center.y-radius*3/7,
                        center.x+radius*3/7,
                        center.y-radius*3/7,
                        center.x-radius*3/7,
                        center.y+radius*3/7
                };
            canvas.drawLines(cross, checkPaint);
        }

        /* Drawing text */
        if(showTitle) {
            if (textPaint.getTextSize() != canvas.getWidth() / 10)
                textPaint.setTextSize(canvas.getWidth() / 10);
            canvas.drawText(title, (float) canvas.getWidth() / 2f, (float) canvas.getHeight() - 20f, textPaint);
        }
    }

    public void setTitle(String t) {
        title = t;
        invalidate();
    }

    public void setShowTitle(boolean st) {
        showTitle = st;
    }

    public void setSwitched(boolean b) {
        switched = b;
        invalidate();
    }

    public void setCheckedColor(int c) {
        trueColor = c;
    }

    public void setUncheckedColor(int c) {
        falseColor = c;
    }

    public void setAllowClick(boolean as) {
        allowClick = as;
    }

    public boolean getSwitched() {
        return switched;
    }

    public String getTitle() {
        return title;
    }

    public void setOnSwitchListener(OnSwitch listener) {
        this.listener = listener;
    }

    private double norme(float x, float y) {
        return Math.sqrt(Math.pow(x,2)+Math.pow(y,2));
    }

    public interface OnSwitch {
        void onSwitch(ButtonView v, boolean isEnabled);
    }

}

