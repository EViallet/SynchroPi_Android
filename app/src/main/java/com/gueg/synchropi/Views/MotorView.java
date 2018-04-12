package com.gueg.synchropi.views;

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
public class MotorView extends View implements View.OnTouchListener {

    private Paint paint = new Paint();
    private Paint whitePaint = new Paint();
    private Paint textPaint = new Paint();

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

    private boolean fullCircle = false;

    //private int colors[] = new int[] {0xff33cc33, 0xff99ff33, 0xffffff00, 0xffff9900, 0xffff0000, 0xffcc0000};
    //private int colors[] = new int[] {0xffe0e0e0, 0xffbdbdbd, 0xff9e9e9e, 0xff757575, 0xff616161, 0xff424242
    //  ,0xff616161, 0xff757575, 0xff9e9e9e, 0xffbdbdbd,0xffe0e0e0};
    private int colors[] = new int[]{0xffbdbdbd,  0xff546e7a, 0xff0d597c, 0xff546e7a, 0xffbdbdbd};
    private int colors2[] = new int[]{
            0xffbdbdbd,  0xff546e7a, 0xff0d597c, 0xff546e7a, 0xffbdbdbd,
            0xffbdbdbd,  0xff546e7a, 0xff0d597c, 0xff546e7a, 0xffbdbdbd,
            0xffbdbdbd,  0xff546e7a, 0xff0d597c, 0xff546e7a, 0xffbdbdbd,
            0xffbdbdbd,  0xff546e7a, 0xff0d597c, 0xff546e7a, 0xffbdbdbd
    };

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

        whitePaint.setColor(0xFFFAFAFA);

        triangle.setFillType(Path.FillType.EVEN_ODD);
        trianglePaint.setColor(0xff3a8936);
        trianglePaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(40f);

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

                if(!fullCircle) {
                    if (angle >= DEFAULT_ANGLE_R && lastAngle >= 0 && lastAngle <= DEFAULT_ANGLE_R)
                        angle = DEFAULT_ANGLE_R;
                    else if (angle >= 0 && angle <= 225 && lastAngle >= 225)
                        angle = 225;
                    lastAngle = angle;

                    float normalizedAngle = angle;
                    if (-90 <= angle && angle <= 0)
                        normalizedAngle += MAX_ANGLE;

                    if (normalizedAngle >= 225)
                        value = Math.round(((normalizedAngle - DEFAULT_ANGLE_L) / DEFAULT_ANGLE_L)*100*50/60);
                    else
                        value = 100 - (int)(((DEFAULT_ANGLE_R - normalizedAngle) / DEFAULT_ANGLE_R)*50);
                } else {
                    float normalizedAngle = angle;
                    if (-90 <= angle && angle <= 0)
                        normalizedAngle += MAX_ANGLE;

                    value = (int)normalizedAngle;

                    value*=1024;
                    value/=360;

                }

                if (listener != null)
                    listener.onValueChanged(this,value);
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
        if(!fullCircle)
            paint.setShader(new SweepGradient(canvas.getWidth()/2, canvas.getHeight()/2, colors, null));
        else
            paint.setShader(new SweepGradient(canvas.getWidth()/2, canvas.getHeight()/2, colors2, null));

        RectF rectf = new RectF(canvas.getClipBounds());
        if(!fullCircle) {
            canvas.rotate(DEFAULT_ANGLE_R, (float) canvas.getWidth() / 2f, (float) canvas.getHeight() / 2f);
            canvas.drawArc(rectf, 0, 2 * DEFAULT_ANGLE_R, true, paint);
            canvas.rotate(-DEFAULT_ANGLE_R, (float) canvas.getWidth() / 2f, (float) canvas.getHeight() / 2f);
        } else {
            canvas.drawArc(rectf, 0, MAX_ANGLE, true, paint);
        }
        if(circleRadius==-1f)
            circleRadius = (float)canvas.getWidth()/2f;
        canvas.drawCircle(circleRadius, (float)canvas.getHeight()/2f, (float)canvas.getHeight()/3f, whitePaint);

        /* Drawing triangle */
        if(triangleSize==-1f)
            triangleSize = (rectf.height()-circleRadius)/3f;
        triangle.reset();
        triangle.lineTo(triangleSize, 0);
        triangle.lineTo(triangleSize/2, triangleSize);
        triangle.close();

        if(triangleOffset ==-1f)
            triangleOffset = (float) canvas.getWidth() / 2f - triangleSize / 2;

        triangle.offset(triangleOffset, 0);

        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(angle, (float)canvas.getWidth()/2f, (float)canvas.getHeight()/2f);
        triangle.transform(rotationMatrix);

        canvas.drawPath(triangle, trianglePaint);

        if(fullCircle)
            canvas.drawText(Integer.toString(value)+"°", center.x, center.y, textPaint);
        else
            canvas.drawText(Integer.toString(value)+"%", center.x, center.y, textPaint);
    }


    public void setValue(int v) {
        value = v;
        invalidate();
    }

    public int getValue() {
        return value;
    }


    public void setFullCircle(boolean fc) {
        fullCircle = fc;
        angle = 0;
        invalidate();
    }


    public void setOnValueChangedListener(OnValueChanged listener) {
        this.listener = listener;
    }

    public interface OnValueChanged {
        void onValueChanged(MotorView v, int value);
    }

}
