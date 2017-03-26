package de.jandrotek.android.arobot.tab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class TiltView extends View implements TiltListener {
//	public class TiltView extends View {

    private final Paint mCirclePaint = new Paint();
    private final Paint mLinePaint = new Paint();
    private final Path mCircle = new Path();
    private final Path mLine = new Path();
    private final Paint mBarPaint = new Paint();
    private Path mBar = new Path();
    private double mRotation = 0f;

    public TiltView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public TiltView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public TiltView(Context context) {
        super(context);
        initialize();
    }

    private void initialize() {
        mBar = new Path();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(Color.WHITE);
        mCirclePaint.setStrokeWidth(5);
        mCirclePaint.setStyle(Paint.Style.STROKE);

        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStrokeWidth(5);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mBarPaint.setAntiAlias(true);
        mBarPaint.setColor(Color.BLUE);
        mBarPaint.setStrokeWidth(5);
        mBarPaint.setStyle(Paint.Style.STROKE);


        // circle with horizontal bar
        mCircle.addCircle(0, 0, 75, Path.Direction.CCW);
        mLine.addRect(-150, 0, 150, 1, Path.Direction.CCW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int cx = canvas.getWidth() / 2;
        int cy = canvas.getHeight() / 2;
        canvas.translate(cx, cy);
        canvas.rotate((float) Math.toDegrees(mRotation));
        canvas.drawPath(mCircle, mCirclePaint);
        canvas.drawPath(mLine, mLinePaint);
        canvas.drawPath(mBar, mBarPaint);
    }

    //@Override
    public void setTilt(double roll, double pitch) {
        mRotation = pitch;
//        mRotation = pitch + (Math.PI/2);
//        mBar = new Path();
//        mBar.reset();
        mBar.rewind();
        double fHeight = (roll);
        float height = -(float) (fHeight * 2);
        //mBar.addRect(height, height, -height, -height, Path.Direction.CCW);
        mBar.moveTo(0f, 0f);
        mBar.lineTo(0f, height); // next end point
        mBar.lineTo(0f, 0f); // next end point
        mBar.close();
        invalidate();
    }

}
