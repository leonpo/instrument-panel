package com.portman.panel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public final class TurnIndicator extends View {

	private static final String TAG = TurnIndicator.class.getSimpleName();

	// drawing tools
	private RectF rimRect;
	private Paint rimPaint;
	private Paint rimCirclePaint;
	
	private RectF faceRect;
	private Paint facePaint;
		
	private Paint turnNeedlePaint;
	private Paint slipballPaint;
	
	private Paint backgroundPaint; 
	// end drawing tools
	
	private Bitmap background; // holds the cached static part
	
	// scale configuration
	private static final float minTurnNeedleValue = -0.0523f;	
	private static final float maxTurnNeedleValue = 0.0523f;
	private static final float minSlipballValue = -1.0f;	
	private static final float maxSlipballValue = 1.0f;	
	
	//hands
	private boolean turnNeedleInitialized = false;
	private float turnNeedlePosition = 0;
	private boolean slipballInitialized = false;
	private float slipballPosition = 0;
	
	public TurnIndicator(Context context) {
		super(context);
		init();
	}

	public TurnIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TurnIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);
		
		turnNeedleInitialized = bundle.getBoolean("turnNeedleInitialized");
		turnNeedlePosition = bundle.getFloat("turnNeedlePosition");
		slipballInitialized = bundle.getBoolean("slipballInitialized");
		slipballPosition = bundle.getFloat("slipballPosition");
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		state.putBoolean("turnNeedleInitialized", turnNeedleInitialized);
		state.putFloat("turnNeedlePosition", turnNeedlePosition);
		state.putBoolean("slipballInitialized", slipballInitialized);
		state.putFloat("slipballPosition", slipballPosition);
		return state;
	}

	private void init() {
		initDrawingTools();
	}

	private void initDrawingTools() {
		rimRect = new RectF(0.01f, 0.01f, 0.99f, 0.99f);

		rimPaint = new Paint();
		rimPaint.setColor(Color.BLACK);
		rimPaint.setAntiAlias(true);

		rimCirclePaint = new Paint();
		rimCirclePaint.setStyle(Paint.Style.STROKE);
		rimCirclePaint.setColor(Color.GRAY);
		rimCirclePaint.setStrokeWidth(0.005f);
		rimCirclePaint.setAntiAlias(true);

		float rimSize = 0.02f;
		faceRect = new RectF();
		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, 
			     rimRect.right - rimSize, rimRect.bottom - rimSize);
		
		facePaint = new Paint();
		facePaint.setStyle(Paint.Style.FILL);
		facePaint.setColor(Color.BLACK);
		
		turnNeedlePaint = new Paint();
		turnNeedlePaint.setColor(Color.WHITE);
		turnNeedlePaint.setStrokeWidth(0.02f);
		turnNeedlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		turnNeedlePaint.setAntiAlias(true);
		
		slipballPaint = new Paint();
		slipballPaint.setColor(Color.WHITE);
		slipballPaint.setStrokeWidth(0.02f);
		slipballPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		slipballPaint.setAntiAlias(true);
		
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
	}
	
	private void drawTurnNeedle(Canvas canvas) {
		if (turnNeedleInitialized) {
			float turnNeedleAngle = (float) Math.toDegrees(turnNeedlePosition)*5;
			canvas.save(Canvas.MATRIX_SAVE_FLAG);
			canvas.rotate(turnNeedleAngle, 0.5f, 0.9f);
			canvas.drawLine(0.5f, 0.9f, 0.5f, 0.2f, turnNeedlePaint);
			canvas.restore();
		}
	}
	
	private void drawSlipball(Canvas canvas) {
		if (slipballInitialized) {
			float slipballTranslate = slipballPosition*0.3f;
			canvas.save(Canvas.MATRIX_SAVE_FLAG);
			
			// draw gate
			canvas.drawLine(0.4f, 0.6f, 0.4f, 0.8f, slipballPaint);
			canvas.drawLine(0.6f, 0.6f, 0.6f, 0.8f, slipballPaint);
			
			// draw slipball
			canvas.translate(slipballTranslate, 0.0f);
			canvas.scale(0.01f, 0.01f); // for drawing circle, does not work with scale 1.0
			canvas.drawCircle(50.0f, 70.0f, 10f, slipballPaint);
			
			canvas.restore();
		}
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
		canvas.scale(scale, scale);

		drawSlipball(canvas);
		drawTurnNeedle(canvas);
		
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
		backgroundCanvas.scale(scale, scale);
		
		drawRim(backgroundCanvas);
		drawFace(backgroundCanvas);		
	}
		
	public void setTurnNeedlePosition(float value) {
		if (value < minTurnNeedleValue) {
			value = minTurnNeedleValue;
		} else if (value > maxTurnNeedleValue) {
			value = maxTurnNeedleValue;
		}
		turnNeedlePosition = value;
		turnNeedleInitialized = true;
		invalidate();
	}
	
	public void setSlipballPosition(float value) {
		if (value < minSlipballValue) {
			value = minSlipballValue;
		} else if (value > maxSlipballValue) {
			value = maxSlipballValue;
		}
		slipballPosition = value;
		slipballInitialized = true;
		invalidate();
	}
}
