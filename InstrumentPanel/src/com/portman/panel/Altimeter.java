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

public final class Altimeter extends View {

	private static final String TAG = Altimeter.class.getSimpleName();

	// drawing tools
	private RectF rimRect;
	private Paint rimPaint;
	private Paint rimCirclePaint;
	
	private RectF faceRect;
	private Paint facePaint;
	
	private Paint scalePaint;
	private RectF scaleRect;
	
	private Paint titlePaint;	
	
	private Paint hand10000Paint;
	private Paint hand1000Paint;
	private Paint hand100Paint;
	
	private Paint backgroundPaint; 
	// end drawing tools
	
	private Bitmap background; // holds the cached static part
	
	// scale configuration
	private static final int totalNicks = 50;
	private static final float degreesPerNick = 360.0f / totalNicks;	
	private static final int minValue = 0;
	private static final int maxValue = 10;
	
	// hand dynamics
	private float hand10000Position = 0f;
	private float hand1000Position = 0f;
	private float hand100Position = 0f;
	
	public Altimeter(Context context) {
		super(context);
		init();
	}

	public Altimeter(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Altimeter(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);
		
		hand10000Position = bundle.getFloat("hand10000Position");
		hand1000Position = bundle.getFloat("hand1000Position");
		hand100Position = bundle.getFloat("hand100Position");
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		state.putFloat("hand10000Position", hand10000Position);
		state.putFloat("hand1000Position", hand1000Position);
		state.putFloat("hand100Position", hand100Position);
		return state;
	}

	private void init() {
		initDrawingTools();
	}

	private String getTitle() {
		return "ALTITUDE";
	}

	private void initDrawingTools() {
		rimRect = new RectF(1f, 1f, 99f, 99f);

		rimPaint = new Paint();
		rimPaint.setAntiAlias(true);
		rimPaint.setColor(Color.BLACK);

		rimCirclePaint = new Paint();
		rimCirclePaint.setAntiAlias(true);
		rimCirclePaint.setStyle(Paint.Style.STROKE);
		rimCirclePaint.setColor(Color.GRAY);
		rimCirclePaint.setStrokeWidth(0.5f);

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

		hand10000Paint = new Paint();
		hand10000Paint.setAntiAlias(true);
		hand10000Paint.setColor(Color.WHITE);
		hand10000Paint.setStrokeWidth(1f);
		hand10000Paint.setStyle(Paint.Style.FILL_AND_STROKE);	
		
		hand1000Paint = new Paint();
		hand1000Paint.setAntiAlias(true);
		hand1000Paint.setColor(Color.WHITE);
		hand1000Paint.setStrokeWidth(3f);
		hand1000Paint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		hand100Paint = new Paint();
		hand100Paint.setAntiAlias(true);
		hand100Paint.setColor(Color.WHITE);
		hand100Paint.setStrokeWidth(2f);
		hand100Paint.setStyle(Paint.Style.FILL_AND_STROKE);
		
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
		// now the outer rim circle
		canvas.drawOval(rimRect, rimCirclePaint);
	}
	
	private void drawFace(Canvas canvas) {		
		canvas.drawOval(faceRect, facePaint);
		// draw the inner rim circle
		canvas.drawOval(faceRect, rimCirclePaint);
		// draw the rim shadow inside the face
		//canvas.drawOval(faceRect, rimShadowPaint);
	}

	private void drawScale(Canvas canvas) {
		//canvas.drawOval(scaleRect, scalePaint);

		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		for (int i = 0; i < totalNicks; ++i) {
			float y1 = scaleRect.top;
			float y2 = y1 + 3f;
			
			canvas.drawLine(50f, y1, 50f, y2, scalePaint);
			
			if (i % 5 == 0) { // every 5
				canvas.drawLine(50f, y1, 50f, y2 + 1f, scalePaint);
				
				int value = nickToValue(i);				
				if (value >= minValue && value <= maxValue) {
					String valueString = Integer.toString(value);
					
					// draw vertical text
					canvas.save(Canvas.MATRIX_SAVE_FLAG);
					canvas.rotate(-degreesPerNick * i, 50f, y2 + 8f);
					canvas.drawText(valueString, 50f, y2 + 10f, scalePaint);
					canvas.restore();
				}
			}
			
			canvas.rotate(degreesPerNick, 50f, 50f);
		}
		canvas.restore();		
	}
	
	private int nickToValue(int nick) {
		int rawValue = minValue + nick * (maxValue - minValue) / totalNicks;
		return rawValue;
	}
	
	private float valueToAngle(float value) {
		float valuePerNick = (float) (maxValue - minValue) / totalNicks;
		return degreesPerNick * (value - minValue) / valuePerNick;
	}
	
	private void drawTitle(Canvas canvas) {
		String title = getTitle();
		canvas.drawText(title, 50f, 40f, titlePaint);
	}
	

	private void drawHand(Canvas canvas) {
		float hand10000Angle = valueToAngle(hand10000Position);
		float hand1000Angle = valueToAngle(hand1000Position);
		float hand100Angle = valueToAngle(hand100Position);
		
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate(hand10000Angle, 50f, 50f);
		canvas.drawLine(50f, 50f, 50f, 35f, hand10000Paint);
		canvas.restore();
		
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate(hand1000Angle, 50f, 50f);
		canvas.drawLine(50f, 50f, 50f, 25f, hand1000Paint);
		canvas.restore();
		
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate(hand100Angle, 50f, 50f);
		canvas.drawLine(50f, 50f, 50f, 15f, hand100Paint);
		
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
		backgroundCanvas.scale(scale/100f, scale/100f);
		
		drawRim(backgroundCanvas);
		drawFace(backgroundCanvas);
		drawScale(backgroundCanvas);
		drawTitle(backgroundCanvas);		
	}
		
	public void setAltimeter(float value10000, float value1000, float value100) {
		if (value10000 < minValue) {
			value10000 = minValue;
		} else if (value10000 > maxValue) {
			value10000 = maxValue;
		}
		hand10000Position = value10000;
		
		if (value1000 < minValue) {
			value1000 = minValue;
		} else if (value1000 > maxValue) {
			value1000 = maxValue;
		}
		hand1000Position = value1000;

		if (value100 < minValue) {
			value100 = minValue;
		} else if (value100 > maxValue) {
			value100 = maxValue;
		}
		hand100Position = value100;

		invalidate();
	}
}
