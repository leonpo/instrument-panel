package com.portman.mfd;

import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public final class VSD extends View {

	private static final String TAG = VSD.class.getSimpleName();

	// drawing tools
	private RectF faceRect;
	private Paint facePaint;
	
	private Paint scalePaint;
	private RectF scaleRect;
	
	private Paint symbolTextPaint;	
	private Paint symbolPaint;	

	private Paint backgroundPaint; 
	// end drawing tools
	
	private Bitmap background; // holds the cached static part
	
	private JSONObject jsonMFD = null;
	
	public VSD(Context context) {
		super(context);
		init();
	}

	public VSD(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VSD(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		return state;
	}

	private void init() {
		initDrawingTools();
		
		// init test targets
		try {
			jsonMFD = (JSONObject) new JSONTokener("{ 'scale_distance':40.0, 'pos_azimuth': 0.0, 'pos_elevation':0.0, 'size_azimuth': 0.0, 'size_elevation':0.0, 'coverage_H_min': 0.0, 'coverage_H_max':30.0, 'TDC_x':0.0, 'TDC_y':0.0, 'PRF':'HI', 'Targets':[{ 'ID':'1', 'velocity':100.0, 'distance':2000, 'convergence_velocity':1000, 'mach':0.5, 'delta_psi':0.2, 'fim':0.5, 'fin':0.3, 'course':1.2, 'isjamming':'false', 'jammer_burned':'false', 'country':'RUS', 'Type':'mig-29c' }}], 'LockedTargets':[{}] }").nextValue();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // test threats
	}

	private void initDrawingTools() {
		faceRect = new RectF(10f, 10f, 990f, 990f);
		
		facePaint = new Paint();
		facePaint.setStyle(Paint.Style.FILL);
		facePaint.setColor(Color.BLACK);

		scalePaint = new Paint();
		scalePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		scalePaint.setColor(Color.GREEN);
		scalePaint.setStrokeWidth(10f);
		scalePaint.setAntiAlias(true);
		
		float scalePosition = 30f;
		scaleRect = new RectF();
		scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
					  faceRect.right - scalePosition, faceRect.bottom - scalePosition);

		symbolTextPaint = new Paint();
		symbolTextPaint.setColor(Color.GREEN);
		symbolTextPaint.setAntiAlias(true);
		symbolTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		symbolTextPaint.setTextAlign(Paint.Align.CENTER);
		symbolTextPaint.setTextSize(40f);

		symbolPaint = new Paint();
		symbolPaint.setAntiAlias(true);
		symbolPaint.setColor(Color.GREEN);
		symbolPaint.setStrokeWidth(5f);
		symbolPaint.setStyle(Paint.Style.STROKE);	
		
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
	
	private void drawFace(Canvas canvas) {		
		canvas.drawOval(faceRect, facePaint);
	}

	private void drawScale(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		
		// draw center mark
		canvas.drawLine(460f, 500f, 540f, 500f, scalePaint);
		canvas.drawLine(500f, 460f, 500f, 540f, scalePaint);
		
		canvas.restore();		
	}
	
	private void drawTargets(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);

		// iterate all targets
		JSONArray targets;
		
		try {
			targets = jsonMFD.getJSONArray("Targets");
			
			for (int i = 0; i < targets.length(); i++) {
			    JSONObject target = targets.getJSONObject(i);
			    float azimuth = (float) (target.getDouble("Azimuth") * 180f/ Math.PI);
			    float power = (float) (target.getDouble("Power")); 			// 0 - 1 (> 0,5 inner circle)
			    String type = target.getString("Type"); 					// mig-29c, osa, etc...
			    			    
				// draw target ID
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
				float x = 500f;
				float y = 500f;
				canvas.translate(0f, -100 - (1f - power) * 300f);
				canvas.rotate(-azimuth, x, y + 15f);
				
				canvas.drawText(type, x, y + 15f, symbolTextPaint);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		canvas.scale(scale/1000f, scale/1000f);

		drawTargets(canvas);
		
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
		backgroundCanvas.scale(scale / 1000f, scale / 1000f);
		
		drawFace(backgroundCanvas);
		drawScale(backgroundCanvas);		
	}
	
	public void showTargets(JSONObject object) {
		jsonMFD = object;
		invalidate();
	}
}
