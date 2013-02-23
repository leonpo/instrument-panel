package com.portman.mfd;

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
			jsonMFD = (JSONObject) new JSONTokener("{ 'scale_distance':74080.000000, 'pos_azimuth': -0.246114, 'pos_elevation':0.057716, 'size_azimuth': 1.047197, 'size_elevation':0.164061, 'coverage_h_min': 2439.425537, 'coverage_h_max':6323.372070, 'TDC_x':-0.235022, 'TDC_y':-0.362870, 'PRF':HI,  'Targets':[{ 'ID':'16777472', 'distance':30264.613281, 'convergence_velocity':449.575378, 'mach':0.877256, 'delta_psi':2.494845, 'fim':0.092773, 'fin':0.045996, 'course':3.700646, 'isjamming':'1', 'jammer_burned':'false', 'country':'1', 'Type':'NA' }, { 'ID':'16777984', 'distance':27322.126953, 'convergence_velocity':518.025024, 'mach':1.090608, 'delta_psi':2.498843, 'fim':0.159505, 'fin':0.039069, 'course':3.704644, 'isjamming':'0', 'jammer_burned':'true', 'country':'1', 'Type':'NA' }] } ").nextValue();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // test threats
	}

	private void initDrawingTools() {
		faceRect = new RectF(0f, 0f, 1000f, 1000f);
		
		facePaint = new Paint();
		facePaint.setStyle(Paint.Style.FILL);
		facePaint.setColor(Color.BLACK);

		scalePaint = new Paint();
		scalePaint.setStyle(Paint.Style.STROKE);
		scalePaint.setColor(Color.GREEN);
		scalePaint.setStrokeWidth(5f);
		scalePaint.setAntiAlias(true);
		
		float scalePosition = 50f;
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
		symbolPaint.setStrokeWidth(10f);
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
		canvas.drawRect(faceRect, facePaint);
	}

	private void drawScale(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		// draw VSD grid
		canvas.drawRect(scaleRect, scalePaint);
		
		// grid  horizontal lines
		canvas.drawLine(50f, 275f, 950f, 275f, scalePaint);
		canvas.drawLine(50f, 500f, 385f, 500f, scalePaint);
		canvas.drawLine(615f, 500f, 950f, 500f, scalePaint);
		canvas.drawLine(50f, 725f, 950f, 725f, scalePaint);
		
		// grid vertial lines
		canvas.drawLine(275f, 50f, 275f, 950f, scalePaint);
		canvas.drawLine(500f, 50f, 500f, 385f, scalePaint);
		canvas.drawLine(500f, 615f, 500f, 950f, scalePaint);
		canvas.drawLine(725f, 50f, 725f, 950f, scalePaint);
		
		// draw horizontal ticks
		canvas.drawLine(30f, 275f, 50f, 275f, scalePaint);
		canvas.drawLine(30f, 350f, 50f, 350f, scalePaint);
		canvas.drawLine(30f, 425f, 50f, 425f, scalePaint);
		canvas.drawLine(30f, 500f, 50f, 500f, scalePaint);
		canvas.drawLine(30f, 575f, 50f, 575f, scalePaint);
		canvas.drawLine(30f, 650f, 50f, 650f, scalePaint);
		
		// draw vertical ticks
		canvas.drawLine(350f, 920f, 350f, 950f, scalePaint);
		canvas.drawLine(425f, 920f, 425f, 950f, scalePaint);
		canvas.drawLine(575f, 920f, 575f, 950f, scalePaint);
		canvas.drawLine(650f, 920f, 650f, 950f, scalePaint);
		
		canvas.restore();		
	}

	
	private void drawTDC(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		
		try {
			
		    float x = (float) (jsonMFD.getDouble("TDC_x"));
		    float y = (float) (jsonMFD.getDouble("TDC_y")); 
			    			    
			canvas.translate(x * 450f, - y * 450f);

			canvas.drawLine(490f, 480f, 490f, 520f, symbolPaint);
			canvas.drawLine(510f, 480f, 510f, 520f, symbolPaint);				
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		canvas.restore();
	}
	
	private void drawTargets(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);

		// iterate all targets
		JSONArray targets;
		
		try {
			targets = jsonMFD.getJSONArray("Targets");
			float range = (float) jsonMFD.getDouble("scale_distance");
			
			for (int i = 0; i < targets.length(); i++) {
			    JSONObject target = targets.getJSONObject(i);
			    float fim = (float) (target.getDouble("fim") * 180f/ Math.PI);
			    float distance = (float) (target.getDouble("distance")); // meters 
			    			    
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
				
				canvas.translate(450f * fim/60f, - 950f * distance / range);
				
				canvas.drawLine(485f, 950f, 515f, 950f, symbolPaint);
				
				canvas.restore();
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

		
		drawTDC(canvas);
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
