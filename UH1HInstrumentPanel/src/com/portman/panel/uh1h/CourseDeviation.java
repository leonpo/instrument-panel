package com.portman.panel.uh1h;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public final class CourseDeviation extends View {

	private static final String TAG = CourseDeviation.class.getSimpleName();

	// drawing tools
	private RectF rimRect;
	private Paint rimPaint;
	private Paint rimCirclePaint;

	private RectF faceRect;
	private Paint facePaint;

	private Paint scalePaint;
	private RectF scaleRect;

	private Paint handPaint;

	private Paint backgroundPaint;
	// end drawing tools

	private Bitmap background; // holds the cached static part

	// scale configuration
	private static final int totalNicks = 72;
	private static final float degreesPerNick = 360.0f / totalNicks;
	private static final float minValue = 0f;
	private static final float maxValue = 360f;

	// pointers
    private float verticalBar = 0f;
    private float horisontalBar = 0f;
    private int toMarker = 0;
    private int fromMarker = 0;
    private float RotCourseCard = 0f;

    public CourseDeviation(Context context) {
		super(context);
		init();
	}

	public CourseDeviation(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CourseDeviation(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);

        verticalBar = bundle.getFloat("verticalBar");
        horisontalBar = bundle.getFloat("horisontalBar");
        toMarker = bundle.getInt("toMarker");
        fromMarker = bundle.getInt("fromMarker");
        RotCourseCard = bundle.getFloat("RotCourseCard");
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		state.putFloat("horisontalBar", horisontalBar);
		state.putInt("toMarker", toMarker);
		state.putInt("fromMarker", fromMarker);
        state.putFloat("RotCourseCard", RotCourseCard);
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
		scalePaint.setStyle(Paint.Style.STROKE);
		scalePaint.setColor(Color.WHITE);
		scalePaint.setStrokeWidth(0.5f);
		scalePaint.setAntiAlias(true);
		
		scalePaint.setTextSize(6f);
		scalePaint.setTypeface(Typeface.SANS_SERIF);
		scalePaint.setTextAlign(Paint.Align.CENTER);
		
		float scalePosition = 3f;
		scaleRect = new RectF();
		scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
					  faceRect.right - scalePosition, faceRect.bottom - scalePosition);

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
		
		canvas.rotate(-RotCourseCard, 50f, 50f);
		
		for (int i = 0; i < totalNicks; ++i) {
			float y1 = scaleRect.top;
			float y2 = y1 + 6f;
			
			canvas.drawLine(50f, y1 + 4f, 50f, y2, scalePaint);
			
			if (i % 2 == 0) { // every 2
				canvas.drawLine(50f, y1 + 2f, 50f, y2, scalePaint);
				
				if (i % 6 == 0) { // every 6
					float value = nickToValue(i);
					String valueString = Integer.toString((int) value/10);
				
					// draw vertical text
					canvas.save(Canvas.MATRIX_SAVE_FLAG);
					canvas.drawText(valueString, 50f, y2 + 6f, scalePaint);
					canvas.restore();
				}
			}
			
			canvas.rotate(degreesPerNick, 50f, 50f);
		}
		canvas.restore();
		
		// draw course pointer
		canvas.drawLine(50f, 10f, 47f, 5f, scalePaint);
		canvas.drawLine(50f, 10f, 53f, 5f, scalePaint);
		canvas.drawLine(47f, 5f, 53f, 5f, scalePaint);

        // draw reciprocal pointer
        canvas.drawLine(50f, 80f, 48f, 76f, scalePaint);
        canvas.drawLine(50f, 80f, 52f, 76f, scalePaint);
        canvas.drawLine(48f, 76f, 52f, 76f, scalePaint);

        // draw center circle
        canvas.drawCircle(50f, 50f, 5f, scalePaint);
	}
	
	private float nickToValue(int nick) {
		return  minValue + nick * (maxValue - minValue) / totalNicks;
	}
	
	private float valueToAngle(float value) {
		float valuePerNick = (float)(maxValue - minValue) / totalNicks;
		return degreesPerNick * (value - minValue) / valuePerNick;
	}

	private void drawPointers(Canvas canvas) {
		float handAngle = verticalBar * 30f;
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate(handAngle, 50f, 20f);
		canvas.drawLine(50f, 20f, 50f, 70f, handPaint);
        canvas.restore();
		
		handAngle = horisontalBar * 30f;
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate(handAngle, 20f, 50f);
        canvas.drawLine(20f, 50f, 80f, 50f, handPaint);
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

		drawScale(canvas);
		drawPointers(canvas);
		
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
	}
		
	public void setDeviation(float verticalBar, float horisontalBar, int toMarker, int fromMarker, float RotCourseCard) {
		this.verticalBar = verticalBar;
        this.horisontalBar = horisontalBar;
		this.toMarker = toMarker;
        this.fromMarker = fromMarker;
		this.RotCourseCard = RotCourseCard * 180f/(float) Math.PI;
		invalidate();
	}
}
