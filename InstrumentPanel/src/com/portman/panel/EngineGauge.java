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

public final class EngineGauge extends View {

	private static final String TAG = EngineGauge.class.getSimpleName();

	// drawing tools
	private RectF rimRect;
	private Paint rimPaint;
	private Paint rimCirclePaint;
	
	private RectF faceRect;
	private Paint facePaint;
	
	private Paint scalePaint;
	private Paint scaleGreenPaint;
	private Paint scaleRedPaint;	
	private RectF scaleOilTemperatureRect, scaleOilPressureRect, scaleFuelPressureRect;
	
	private Paint titlePaint;	
	
	private Paint handPaint;
	
	private Paint backgroundPaint; 
	// end drawing tools
	
	private Bitmap background; // holds the cached static part
	
	// scale configuration
	private static final int totalFuelPressureNicks = 25;
	private static final float degreesFuelPressurePerNick = 180.0f / totalFuelPressureNicks;	
	private static final int minFuelPressureValue = 0;
	private static final int maxFuelPressureValue = 25;
	
	private static final int totalOilPressureNicks = 20;
	private static final float degreesOilPressurePerNick = 180.0f / totalOilPressureNicks;	
	private static final int minOilPressureValue = 0;
	private static final int maxOilPressureValue = 200;
	
	private static final int totalOilTemperatureNicks = 20;
	private static final float degreesOilTemperaturePerNick = 180.0f / totalOilTemperatureNicks;	
	private static final int minOilTemperatureValue = 0;
	private static final int maxOilTemperatureValue = 100;
	
	// hand dynamics
	private float fuelPressure = 0f;
	private float oilTemperature = 0f;
	private float oilPressure = 0f;
	
	public EngineGauge(Context context) {
		super(context);
		init();
	}

	public EngineGauge(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public EngineGauge(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);
		
		fuelPressure = bundle.getFloat("fuelPressure");
		oilTemperature = bundle.getFloat("oilTemperature");
		oilPressure = bundle.getFloat("oilPressure");		
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		state.putFloat("fuelPressure", fuelPressure);
		state.putFloat("oilTemperature", oilTemperature);
		state.putFloat("oilPressure", oilPressure);
		
		return state;
	}

	private void init() {
		initDrawingTools();
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
		
		scalePaint.setTextSize(5f);
		scalePaint.setTypeface(Typeface.SANS_SERIF);
		scalePaint.setTextAlign(Paint.Align.CENTER);
		
		scaleGreenPaint = new Paint();
		scaleGreenPaint.setStyle(Paint.Style.STROKE);
		scaleGreenPaint.setColor(Color.GREEN);
		scaleGreenPaint.setStrokeWidth(3f);
		scaleGreenPaint.setAntiAlias(true);
		
		scaleRedPaint = new Paint();
		scaleRedPaint.setStyle(Paint.Style.STROKE);
		scaleRedPaint.setColor(Color.RED);
		scaleRedPaint.setStrokeWidth(1f);
		scaleRedPaint.setAntiAlias(true);
		
		float scaleOilTemperaturePosition = 3f;
		scaleOilTemperatureRect = new RectF();
		scaleOilTemperatureRect.set(faceRect.left + scaleOilTemperaturePosition, faceRect.top + scaleOilTemperaturePosition,
					  faceRect.right - scaleOilTemperaturePosition, faceRect.bottom - scaleOilTemperaturePosition);
		
		float scaleOilPressurePosition = 22f;
		scaleOilPressureRect = new RectF();
		scaleOilPressureRect.set(faceRect.left + scaleOilPressurePosition, faceRect.top + scaleOilPressurePosition,
					  faceRect.right - scaleOilPressurePosition, faceRect.bottom - scaleOilPressurePosition);
		
		float scaleFuelPressurePosition = 22f;
		scaleFuelPressureRect = new RectF();
		scaleFuelPressureRect.set(faceRect.left + scaleFuelPressurePosition, faceRect.top + scaleFuelPressurePosition,
					  faceRect.right - scaleFuelPressurePosition, faceRect.bottom - scaleFuelPressurePosition);
		
		titlePaint = new Paint();
		titlePaint.setColor(Color.WHITE);
		titlePaint.setAntiAlias(true);
		titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
		titlePaint.setTextAlign(Paint.Align.CENTER);
		titlePaint.setTextSize(6f);

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

	private void drawOilTemperatureScale(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.drawText("TEMP C", 50f, 30f, scalePaint);
		
		// draw green range 70-80
		canvas.drawArc(scaleOilTemperatureRect, -valueOilTemperatureToAngle(70),  - valueOilTemperatureToAngle(80) + valueOilTemperatureToAngle(70), false, scaleGreenPaint);
		
		canvas.rotate(-90, 50f, 50f);
		for (int i = 0; i <= totalOilTemperatureNicks; ++i) {
			float y1 = scaleOilTemperatureRect.top;
			float y2 = y1 + 3f;
			
			canvas.drawLine(50f, y1, 50f, y2, scalePaint);
			
			if ( i % 2 == 0 ) { // every 2
				canvas.drawLine(50f, y1, 50f, y2 + 1f, scalePaint);
				
				float value = nickOilTemperatureToValue(i);
				if ( i % 4 == 0 ) {
					String valueString = Integer.toString(Math.abs((int)value));
					
					// draw vertical text
					canvas.save(Canvas.MATRIX_SAVE_FLAG);
					canvas.rotate(- degreesOilTemperaturePerNick * i + 90, 50f, y2 + 8f);
					canvas.drawText(valueString, 50f, y2 + 10f, scalePaint);
					canvas.restore();
				}
			}
			
			// draw red line at 90C
			if (i == 18)
				canvas.drawLine(50f, y1, 50f, y2 + 5f, scaleRedPaint);
			
			canvas.rotate(degreesOilTemperaturePerNick, 50f, 50f);
		}
		canvas.restore();		
	}
	
	private float nickOilTemperatureToValue(int nick) {
		return  minOilTemperatureValue + nick * (maxOilTemperatureValue - minOilTemperatureValue) / totalOilTemperatureNicks;
	}
	
	private float valueOilTemperatureToAngle(float value) {
		float valuePerNick = (float)(maxOilTemperatureValue - minOilTemperatureValue) / totalOilTemperatureNicks;
		return degreesOilTemperaturePerNick * (value - minOilTemperatureValue) / valuePerNick - 90;
	}
	
	private void drawOilTemperatureHand(Canvas canvas) {
		float handAngle = valueOilTemperatureToAngle(oilTemperature);
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate(handAngle, 50f, 50f);
		canvas.drawLine(50f, 50f, 50f, 10f, handPaint);
		canvas.restore();
	}
	
	private void drawOilPressureScale(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.translate(-10f,15f);
		
		canvas.drawText("OIL", 50f, 45f, scalePaint);
		canvas.drawText("LBS", 60f, 60f, scalePaint);
		
		// draw green range 70-80 PSI
		canvas.drawArc(scaleOilPressureRect, valueOilPressureToAngle(70) - 90, valueOilPressureToAngle(80) - valueOilPressureToAngle(70), false, scaleGreenPaint);
		
		canvas.rotate(-180, 50f, 50f);
		for (int i = 0; i <= totalOilPressureNicks; ++i) {
			float y1 = scaleOilPressureRect.top;
			float y2 = y1 + 3f;
			
			canvas.drawLine(50f, y1, 50f, y2, scalePaint);
			
			if ( i % 5 == 0 ) { // every 2
				canvas.drawLine(50f, y1, 50f, y2 + 1f, scalePaint);
				
				float value = nickOilPressureToValue(i);
				String valueString = Integer.toString(Math.abs((int)value));
				
				// draw vertical text
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
				canvas.rotate(- degreesOilPressurePerNick * i + 180, 50f, y2 + 8f);
				canvas.drawText(valueString, 50f, y2 + 10f, scalePaint);
				canvas.restore();
			}
			
			// draw red line at 50 PSI
			if (i == 5)
				canvas.drawLine(50f, y1-2, 50f, y2 + 3f, scaleRedPaint);
			
			// draw red line at 90 PSI
			if (i == 9)
				canvas.drawLine(50f, y1-2, 50f, y2 + 3f, scaleRedPaint);
			
			canvas.rotate(degreesOilPressurePerNick, 50f, 50f);
		}
		canvas.restore();		
	}
	
	private float nickOilPressureToValue(int nick) {
		return  minOilPressureValue + nick * (maxOilPressureValue - minOilPressureValue) / totalOilPressureNicks;
	}
	
	private float valueOilPressureToAngle(float value) {
		float valuePerNick = (float)(maxOilPressureValue - minOilPressureValue) / totalOilPressureNicks;
		return degreesOilPressurePerNick * (value - minOilPressureValue) / valuePerNick - 180;
	}
	
	private void drawOilPressureHand(Canvas canvas) {
		float handAngle = valueOilPressureToAngle(oilPressure);
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.translate(-10f,15f);
		canvas.rotate(handAngle, 50f, 50f);
		canvas.drawLine(50f, 50f, 50f, 30f, handPaint);
		canvas.restore();
	}
	
	private void drawFuelPressureScale(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.translate(10f,15f);
		
		canvas.drawText("FUEL", 50f, 45f, scalePaint);
		
		// draw green range 12-16 PSI
		canvas.drawArc(scaleFuelPressureRect, valueFuelPressureToAngle(12) - 90, valueFuelPressureToAngle(16) - valueFuelPressureToAngle(12), false, scaleGreenPaint);
		
		canvas.rotate(-180, 50f, 50f);
		for (int i = 0; i <= totalFuelPressureNicks; ++i) {
			float y1 = scaleFuelPressureRect.top;
			float y2 = y1 + 3f;
			
			canvas.drawLine(50f, y1, 50f, y2, scalePaint);
			
			if ( i % 5 == 0 ) { // every 2
				canvas.drawLine(50f, y1, 50f, y2 + 1f, scalePaint);
				
				float value = nickFuelPressureToValue(i);
				String valueString = Integer.toString(Math.abs((int)value));
				
				// draw vertical text
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
				canvas.rotate(degreesFuelPressurePerNick * i + 180, 50f, y2 + 8f);
				canvas.drawText(valueString, 50f, y2 + 10f, scalePaint);
				canvas.restore();
			}
			
			// draw red line at 12 PSI
			if (i == 12)
				canvas.drawLine(50f, y1-2, 50f, y2 + 3f, scaleRedPaint);
			
			// draw red line at 90C
			if (i == 19)
				canvas.drawLine(50f, y1-2, 50f, y2 + 3f, scaleRedPaint);
			
			canvas.rotate(-degreesFuelPressurePerNick, 50f, 50f);
		}
		canvas.restore();		
	}
	
	private float nickFuelPressureToValue(int nick) {
		return  minFuelPressureValue + nick * (maxFuelPressureValue - minFuelPressureValue) / totalFuelPressureNicks;
	}
	
	private float valueFuelPressureToAngle(float value) {
		float valuePerNick = (float)(maxFuelPressureValue - minFuelPressureValue) / totalFuelPressureNicks;
		return -degreesFuelPressurePerNick * (value - minFuelPressureValue) / valuePerNick - 180;
	}
	
	private void drawFuelPressureHand(Canvas canvas) {
		float handAngle = valueFuelPressureToAngle(fuelPressure);
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.translate(10f,15f);
		canvas.rotate(handAngle, 50f, 50f);
		canvas.drawLine(50f, 50f, 50f, 30f, handPaint);
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
		canvas.scale(scale / 100f, scale / 100f);

		drawOilTemperatureHand(canvas);
		drawOilPressureHand(canvas);
		drawFuelPressureHand(canvas);
		
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
		
		drawOilTemperatureScale(backgroundCanvas);
		drawOilPressureScale(backgroundCanvas);
		drawFuelPressureScale(backgroundCanvas);
	}

	public void setValues(float oilTemperature, float oilPressure, float fuelPressure) {
		this.oilTemperature = oilTemperature;
		this.oilPressure = oilPressure;
		this.fuelPressure = fuelPressure;
		invalidate();
	}
}
