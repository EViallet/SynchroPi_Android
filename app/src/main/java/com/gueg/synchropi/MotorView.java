package com.gueg.synchropi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

@SuppressWarnings("unused")
@SuppressLint("DrawAllocation")
public class MotorView extends ControlView implements View.OnTouchListener {

    private Paint paint = new Paint();
    private Paint whitePaint = new Paint();

    private Path triangle = new Path();
    private Paint trianglePaint = new Paint();
    private float triangleSize = -1f;

    private static final float MAX_ANGLE = 360f;
    private static final float DEFAULT_ANGLE_L = 225f;
    private static final float DEFAULT_ANGLE_R = 135f;
    private float circleRadius = -1f;
    private float triangleOffset = -1f;
    private float angle = -DEFAULT_ANGLE_R;
    private float lastAngle = angle;
    private Point center;

    private int value = 0;
    private OnValueChanged listener;

    private int colors[] = new int[] {0xff33cc33, 0xff99ff33, 0xffffff00, 0xffff9900, 0xffff0000, 0xffcc0000};

    public MotorView(Context context) {
        this(context, null);
    }
    public MotorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public MotorView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public MotorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        whitePaint.setColor(Color.WHITE);

        triangle.setFillType(Path.FillType.EVEN_ODD);
        trianglePaint.setColor(Color.GRAY);
        trianglePaint.setStyle(Paint.Style.FILL);

        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        switch(e.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                double relativeX = e.getX() - center.x,
                        relativeY = e.getY() - center.y;
                angle = (float) Math.toDegrees(Math.atan2(relativeY, relativeX) + Math.PI / 2d);

                if (angle >= DEFAULT_ANGLE_R && lastAngle >= 0 && lastAngle <= DEFAULT_ANGLE_R)
                    angle = DEFAULT_ANGLE_R;
                else if (angle >= 0 && angle <= 225 && lastAngle >= 225)
                    angle = 225;
                lastAngle = angle;

                if (allowClick) {
                    float normalizedAngle = angle;
                    if (-90 <= angle && angle <= 0)
                        normalizedAngle += MAX_ANGLE;

                    if (normalizedAngle >= 225)
                        value = Math.round(((normalizedAngle - DEFAULT_ANGLE_L) / DEFAULT_ANGLE_L)*100*50/60);
                    else
                        value = 100 - (int)(((DEFAULT_ANGLE_R - normalizedAngle) / DEFAULT_ANGLE_R)*50);

                    if (listener != null)
                        listener.onValueChanged(this,value);
                }
                invalidate();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /* Required for angles calculations */
        if(center == null)
            center = new Point(canvas.getWidth()/2, canvas.getHeight()/2);

        /* Drawing gradient arc */
        paint.setShader(new SweepGradient(canvas.getWidth()/2, canvas.getHeight()/2, colors, null));

        RectF rectf = new RectF(canvas.getClipBounds());
        canvas.rotate(DEFAULT_ANGLE_R, (float)canvas.getWidth()/2f, (float)canvas.getHeight()/2f);
        canvas.drawArc(rectf, 0, 2* DEFAULT_ANGLE_R, true, paint);
        canvas.rotate(-DEFAULT_ANGLE_R, (float)canvas.getWidth()/2f, (float)canvas.getHeight()/2f);
        if(circleRadius==-1f)
            circleRadius = (float)canvas.getWidth()/2f;
        canvas.drawCircle(circleRadius, (float)canvas.getHeight()/2f, (float)canvas.getHeight()/3f, whitePaint);

        /* Drawing text */
        if(showTitle) {
            if (textPaint.getTextSize() != canvas.getWidth() / 10)
                textPaint.setTextSize(canvas.getWidth() / 10);
            canvas.drawText(title, (float) canvas.getWidth() / 2f, (float) canvas.getHeight() - 20f, textPaint);
            textPaint.setFakeBoldText(true);
            textPaint.setTextSize(80f);
            canvas.drawText(Integer.toString(value), (float) canvas.getWidth() / 2f, (float) canvas.getHeight() * 3f / 4f, textPaint);
            textPaint.setFakeBoldText(false);
            textPaint.setTextSize(60f);
        }

        /* Drawing triangle */
        if(triangleSize==-1f)
            triangleSize = (rectf.height()-circleRadius)/3f;
        triangle.reset();
        triangle.lineTo(triangleSize       , 0);
        triangle.lineTo(triangleSize/2     , triangleSize);
        triangle.close();

        if(triangleOffset ==-1f)
            triangleOffset = (float) canvas.getWidth() / 2f - triangleSize / 2;

        triangle.offset(triangleOffset, 0);

        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(angle, (float)canvas.getWidth()/2f, (float)canvas.getHeight()/2f);
        triangle.transform(rotationMatrix);

        canvas.drawPath(triangle, trianglePaint);
    }


    public void setValue(int v) {
        value = v;
        invalidate();
    }

    public int getValue() {
        return value;
    }




    public void setOnValueChangedListener(OnValueChanged listener) {
        this.listener = listener;
    }

    public interface OnValueChanged {
        void onValueChanged(MotorView v, int value);
    }

}
