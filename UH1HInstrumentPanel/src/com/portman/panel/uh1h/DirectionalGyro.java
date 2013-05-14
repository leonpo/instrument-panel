package com.portman.panel.uh1h;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public final class DirectionalGyro extends View {

	private static final String TAG = DirectionalGyro.class.getSimpleName();

	// drawing tools
	private RectF rimRect;
	private Paint rimPaint;
	private Paint rimCirclePaint;
	
	private RectF faceRect;
	private Paint facePaint;
	
	private Paint scalePaint;
	private Paint scaleLargePaint;
	
	private Paint backgroundPaint; 
	// end drawing tools
	
	private Bitmap background; 			// holds the cached static part
	
	// scale configuration	
	private static final float minGyroHeading = 0.0f;
	private static final float maxGyroHeading = 2.0f * (float) Math.PI;
	
	// hand dynamics
	private float gyroHeading = (float) Math.PI * 0f;
	
	public DirectionalGyro(Context context) {
		super(context);
		init();
	}

	public DirectionalGyro(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DirectionalGyro(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);
		
		gyroHeading = bundle.getFloat("gyroHeading");
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		state.putFloat("gyroHeading", gyroHeading);		
		return state;
	}

	private void init() {
		initDrawingTools();
	}

	private void initDrawingTools() {
		rimRect = new RectF(100f, 100f, 900f, 900f);

		rimPaint = new Paint();
		rimPaint.setAntiAlias(true);
		rimPaint.setColor(Color.LTGRAY);

		rimCirclePaint = new Paint();
		rimCirclePaint.setAntiAlias(true);
		rimCirclePaint.setStyle(Paint.Style.STROKE);
		rimCirclePaint.setColor(Color.GRAY);
		rimCirclePaint.setStrokeWidth(10f);

		float rimSize = 20f;
		faceRect = new RectF();
		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, 
			     rimRect.right - rimSize, rimRect.bottom - rimSize);
		
		facePaint = new Paint();
		facePaint.setStyle(Paint.Style.FILL);
		facePaint.setColor(Color.BLACK);

		scalePaint = new Paint();
		scalePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		scalePaint.setColor(Color.WHITE);
		scalePaint.setStrokeWidth(3f);
		scalePaint.setAntiAlias(true);	
		
		scalePaint.setTextSize(50f);
		scalePaint.setTypeface(Typeface.SANS_SERIF);
		scalePaint.setTextAlign(Paint.Align.CENTER);
		
		scaleLargePaint = new Paint();
		scaleLargePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		scaleLargePaint.setColor(Color.WHITE);
		scaleLargePaint.setStrokeWidth(5f);
		scaleLargePaint.setAntiAlias(true);	
		
		scaleLargePaint.setTextSize(80f);
		scaleLargePaint.setTypeface(Typeface.SANS_SERIF);
		scaleLargePaint.setTextAlign(Paint.Align.CENTER);
		
		backgroundPaint = new Paint();
		backgroundPaint.setFilterBitmap(true);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d(TAG, "Width spec: " + MeasureSpec.toString(widthMeasureSpec));
		Log.d(TAG, "Height spec: " + MeasureSpec.toString(heightMeasureSpec));
		
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);
		
		int chosenDimension = Math.min(chosenWidth, chosenHeight);
		
		setMeasuredDimension(chosenDimension, chosenDimension);
	}
	
	private int chooseDimension(int mode, int size) {
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
			return size;
		} else { // (mode == MeasureSpec.UNSPECIFIED)
			return getPreferredSize();
		} 
	}
	
	// in case there is no size specified
	private int getPreferredSize() {
		return 300;
	}

	private void drawRim(Canvas canvas) {
		// first, draw the metallic body
		canvas.drawOval(rimRect, rimPaint);
	}
	
	private void drawFace(Canvas canvas) {		
		canvas.drawOval(faceRect, facePaint);
		// draw the inner rim circle
		canvas.drawOval(faceRect, rimCirclePaint);
	}

	private void drawScale(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.restore();	
	}
		
	private void drawNeedle(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		
		canvas.clipRect(250f, 200f, 750f, 550f);
		
		int shift =  (int) Math.toDegrees(gyroHeading) * 50 / 5 - 3400;
		canvas.translate(shift, 0.0f);
		
		for (int i = 78; i > -12; --i) {
			String value;
			if (i < 0)
				value = Integer.toString(36 + i/2);
			else if (i > 72)
				value = Integer.toString(i/2 - 36);
			else
				value = Integer.toString(i/2);
			if (value.contentEquals("36"))
				value = "0";
			
			if (i % 6 == 0) { // large tick every 30 degrees
				canvas.drawLine(0f, 380f, 0f, 500f, scalePaint);
				canvas.drawText(value, 0f, 370f, scaleLargePaint);
			} else if (i % 2 == 0) { // large tick every 10 degrees
				canvas.drawLine(0f, 380f, 0f, 500f, scalePaint);
				canvas.drawText(value, 0f, 370f, scalePaint);
			}
			else { //small tick
				canvas.drawLine(0f, 430f, 0f, 500f, scalePaint);
			}
			
			canvas.translate(50f, 0.0f);
		}
		
		canvas.restore();
		
		canvas.drawLine(500f, 300, 500f, 500f, scalePaint);
	}

	private void drawBackground(Canvas canvas) {
		if (background == null) {
			Log.w(TAG, "Background not created");
		} else {
			canvas.drawBitmap(background, 0, 0, backgroundPaint);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		drawBackground(canvas);

		float scale = (float) getWidth();		
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(scale/1000f, scale/1000f);

		drawNeedle(canvas);
		
		canvas.restore();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.d(TAG, "Size changed to " + w + "x" + h);
		
		regenerateBackground();
	}
	
	private void regenerateBackground() {
		// free the old bitmap
		if (background != null) {
			background.recycle();
		}
		
		background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas backgroundCanvas = new Canvas(background);
		float scale = (float) getWidth();		
		backgroundCanvas.scale(scale/1000f, scale/1000f);
		
		drawRim(backgroundCanvas);
		drawFace(backgroundCanvas);
		drawScale(backgroundCanvas);
	}
		
	public void setGyroHeading(float value) {
		if (gyroHeading < minGyroHeading) {
			value = minGyroHeading;
		} else if (value > maxGyroHeading) {
			value = maxGyroHeading;
		}
		this.gyroHeading = value;
		
		invalidate();
	}
}
