package com.portman.panel;

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

public final class Airspeed extends View {

	private static final String TAG = Airspeed.class.getSimpleName();

	// drawing tools
	private RectF rimRect;
	private Paint rimPaint;
	private Paint rimCirclePaint;
	
	private RectF faceRect;
	private Paint facePaint;
	
	private Paint scalePaint;
	private Paint scaleRedPaint;
	private RectF scaleRect;
	
	private Paint titlePaint;	
	
	private Paint handPaint;
	
	private Paint backgroundPaint; 
	// end drawing tools
	
	private Bitmap background; // holds the cached static part
	
	// scale configuration
	private static final int totalNicks1 = 2;
	private static final float degreesPerNick1 = 30f / totalNicks1;
	private static final float minValue1 = 0f;
	private static final float maxValue1 = 20f;	
	
	private static final int totalNicks2 = 4;
	private static final float degreesPerNick2 = 75f / totalNicks2;
	private static final float minValue2 = 20f;
	private static final float maxValue2 = 40f;
		
	private static final int totalNicks3 = 22;
	private static final float degreesPerNick3 = 235f / totalNicks3;
	private static final float minValue3 = 40f;
	private static final float maxValue3 = 150;
	
	// hand dynamics
	private float handPosition = 0f;
		
	public Airspeed(Context context) {
		super(context);
		init();
	}

	public Airspeed(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Airspeed(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);
		
		handPosition = bundle.getFloat("handPosition");
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		state.putFloat("handPosition", handPosition);
		return state;
	}

	private void init() {
		initDrawingTools();
	}

	private String getTitle() {
		return "KNOTS";
	}

	private void initDrawingTools() {
		rimRect = new RectF(1f, 1f, 99f, 99f);

		rimPaint = new Paint();
		rimPaint.setAntiAlias(true);
		rimPaint.setColor(Color.LTGRAY);

		rimCirclePaint = new Paint();
		rimCirclePaint.setAntiAlias(true);
		rimCirclePaint.setStyle(Paint.Style.STROKE);
		rimCirclePaint.setColor(Color.GRAY);
		rimCirclePaint.setStrokeWidth(1f);

		float rimSize = 2f;
		faceRect = new RectF();
		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, 
			     rimRect.right - rimSize, rimRect.bottom - rimSize);
		
		facePaint = new Paint();
		facePaint.setStyle(Paint.Style.FILL);
		facePaint.setColor(Color.BLACK);

		scalePaint = new Paint();
		scalePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		scalePaint.setColor(Color.WHITE);
		scalePaint.setStrokeWidth(0.5f);
		scalePaint.setAntiAlias(true);
		
		scalePaint.setTextSize(8f);
		scalePaint.setTypeface(Typeface.SANS_SERIF);
		scalePaint.setTextAlign(Paint.Align.CENTER);
		
		scaleRedPaint = new Paint();
		scaleRedPaint.setStyle(Paint.Style.STROKE);
		scaleRedPaint.setColor(Color.RED);
		scaleRedPaint.setStrokeWidth(2f);
		scaleRedPaint.setAntiAlias(true);
		
		float scalePosition = 3f;
		scaleRect = new RectF();
		scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
					  faceRect.right - scalePosition, faceRect.bottom - scalePosition);

		titlePaint = new Paint();
		titlePaint.setColor(Color.WHITE);
		titlePaint.setAntiAlias(true);
		titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
		titlePaint.setTextAlign(Paint.Align.CENTER);
		titlePaint.setTextSize(8f);

		handPaint = new Paint();
		handPaint.setAntiAlias(true);
		handPaint.setColor(Color.WHITE);
		handPaint.setStrokeWidth(2f);
		handPaint.setStyle(Paint.Style.FILL_AND_STROKE);	
		
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
		for (int i = 0; i < totalNicks1; ++i) {
			float y1 = scaleRect.top;
			float y2 = y1 + 3f;
			
			canvas.drawLine(50f, y1, 50f, y2, scalePaint);
			
			if (i % 2 == 0) { // every 1
				canvas.drawLine(50f, y1, 50f, y2 + 3f, scalePaint);
				
				float value = nickToValue1(i);
				String valueString = Integer.toString((int)value);
				
				// draw vertical text
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
				canvas.rotate(-degreesPerNick1 * i, 50f, y2 + 10f);
				canvas.drawText(valueString, 50f, y2 + 10f, scalePaint);
				canvas.restore();
			}
			canvas.rotate(degreesPerNick1, 50f, 50f);
		}
		
		for (int i = 0; i < totalNicks2; ++i) {
			float y1 = scaleRect.top;
			float y2 = y1 + 3f;
			
			canvas.drawLine(50f, y1, 50f, y2, scalePaint);
			
			if (i % 2 == 0) { // every 5
				canvas.drawLine(50f, y1, 50f, y2 + 3f, scalePaint);
				
				if (i % 4 == 0) {
					float value = nickToValue2(i);
					String valueString = Integer.toString((int)value);
					
					// draw vertical text
					canvas.save(Canvas.MATRIX_SAVE_FLAG);
					canvas.rotate(-degreesPerNick2 * i - 30f , 50f, y2 + 10f);
					canvas.drawText(valueString, 50f, y2 + 10f, scalePaint);
					canvas.restore();
				}
			}			
			canvas.rotate(degreesPerNick2, 50f, 50f);
		}
		
		for (int i = 0; i <= totalNicks3; ++i) {
			float y1 = scaleRect.top;
			float y2 = y1 + 3f;
			
			canvas.drawLine(50f, y1, 50f, y2, scalePaint);
			
			if (i % 2 == 0) { // every 2
				canvas.drawLine(50f, y1, 50f, y2 + 3f, scalePaint);
				
				if (i % 4 == 0) {
					float value = nickToValue3(i);
					String valueString = Integer.toString((int)value);
				
					// draw vertical text
					canvas.save(Canvas.MATRIX_SAVE_FLAG);
					canvas.rotate(-degreesPerNick3 * i - 105f, 50f, y2 + 10f);
					canvas.drawText(valueString, 50f, y2 + 10f, scalePaint);
					canvas.restore();
				}
			}
			
			// draw red line at 124 knots
			if (i == 17) {
				canvas.drawLine(50f, y1, 50f, y2 + 5f, scaleRedPaint);
			}
			
			canvas.rotate(degreesPerNick3, 50f, 50f);
		}
		canvas.restore();		
	}
	
	private float nickToValue1(int nick) {
		float rawValue = minValue1 + nick * (maxValue1 - minValue1) / totalNicks1;
		return rawValue;
	}
	
	private float nickToValue2(int nick) {
		float rawValue = minValue2 + nick * (maxValue2 - minValue2) / totalNicks2;
		return rawValue;
	}
	
	private float nickToValue3(int nick) {
		float rawValue = minValue3 + nick * (maxValue3 - minValue3) / totalNicks3;
		return rawValue;
	}
	
	private float valueToAngle(float value) {
		float angle = 0f;
		if (value < maxValue1) {
			float valuePerNick = (float)(maxValue1 - minValue1) / totalNicks1;
			angle =  degreesPerNick1 * (value - minValue1) / valuePerNick;
		} else if (value < maxValue2) {
			float valuePerNick = (float)(maxValue2 - minValue2) / totalNicks2;
			angle =  degreesPerNick2 * (value - minValue2) / valuePerNick + 30f;
		} else {
			float valuePerNick = (float)(maxValue3 - minValue3) / totalNicks3;
			angle =  degreesPerNick3 * (value - minValue3) / valuePerNick + 105f;
		}
		
		return angle;
	}
	
	private void drawTitle(Canvas canvas) {
		String title = getTitle();
		canvas.drawText(title, 50f, 60f, titlePaint);
	}
	

	private void drawHand(Canvas canvas) {
		float handAngle = valueToAngle(handPosition);
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate(handAngle, 50f, 50f);
		canvas.drawLine(50f, 50f, 50f, 10f, handPaint);
		canvas.restore();
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
		canvas.scale(scale/100f, scale/100f);

		drawHand(canvas);
		
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
		backgroundCanvas.scale(scale / 100f, scale / 100f);
		
		drawRim(backgroundCanvas);
		drawFace(backgroundCanvas);
		drawScale(backgroundCanvas);
		drawTitle(backgroundCanvas);		
	}
		
	public void setAirspeed(float value) {
		if (value < minValue1) {
			value = minValue1;
		} else if (value > maxValue3) {
			value = maxValue3;
		}
		handPosition = value;
		invalidate();
	}
}
