package net.osmand.plus.views.layers.geometry;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;

import net.osmand.AndroidUtils;
import net.osmand.plus.R;

import androidx.core.content.ContextCompat;

public class GpxGeometryWayContext extends MultiColoringGeometryWayContext {

	private final Paint circlePaint;
	private final Paint strokePaint;

	private final Bitmap specialArrowBitmap;

	public GpxGeometryWayContext(Context ctx, float density) {
		super(ctx, density);
		Paint paint = getPaintIcon();
		paint.setStrokeCap(Paint.Cap.ROUND);
		circlePaint = createCirclePaint();
		strokePaint = createStrokePaint();
		specialArrowBitmap = AndroidUtils.drawableToBitmap(ContextCompat.getDrawable(ctx, R.drawable.mm_special_arrow_up));
	}

	@Override
	public Paint getStrokePaint() {
		return strokePaint;
	}

	@Override
	protected int getArrowBitmapResId() {
		return R.drawable.ic_action_direction_arrow;
	}

	public Bitmap getSpecialArrowBitmap() {
		return specialArrowBitmap;
	}

	public Paint getCirclePaint() {
		return circlePaint;
	}

	private Paint createCirclePaint() {
		Paint circlePaint = new Paint();
		circlePaint.setDither(true);
		circlePaint.setAntiAlias(true);
		circlePaint.setStyle(Paint.Style.FILL);
		circlePaint.setColor(0x33000000);
		return circlePaint;
	}

	private Paint createStrokePaint() {
		Paint strokePaint = new Paint();
		strokePaint.setDither(true);
		strokePaint.setAntiAlias(true);
		strokePaint.setStyle(Style.STROKE);
		strokePaint.setStrokeCap(Cap.ROUND);
		strokePaint.setStrokeJoin(Join.ROUND);
		return strokePaint;
	}
}