package nz.co.ijw.aviation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CgEnvelopeView extends View {
	
	public CgEnvelopeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	Station[] Limits;
	Station Position;
	
	public void setLimits(Station[] limits) { Limits = limits; invalidate(); }
	public void setPosition(Station position) { Position = position; invalidate(); }
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(0);
		
		Paint envelopePaint = new Paint(); envelopePaint.setColor(0xffffffff); envelopePaint.setStrokeWidth(3);
		Paint gridPaint = new Paint(); gridPaint.setColor(0x33333333);
		Paint gridPaint2 = new Paint(); gridPaint2.setColor(0x77777777);
		Paint positionPaint = new Paint(); positionPaint.setColor(0xffff0000); envelopePaint.setStrokeWidth(3);

		if (Limits == null || Position == null) return;
		
		// TODO: autoscale these
		double minCg = 30.0;
		double maxCg = 37.0;
		double minWeight = 1000;
		double maxWeight = 1800;
		
		for (double cg = minCg; cg < maxCg; cg += 0.2 ) {
			double x = getWidth() * (cg - minCg) / (maxCg - minCg);
			canvas.drawLine((float)x, 0, (float)x, getHeight(), gridPaint);
		}
		for (double weight = minWeight; weight < maxWeight; weight += 20.0 ) {
			double y = getHeight() * (maxWeight - weight) / (maxWeight - minWeight);
			canvas.drawLine(0, (float)y, getWidth(), (float)y, gridPaint);
		}
		
		for (double cg = minCg; cg < maxCg; cg += 1 ) {
			double x = getWidth() * (cg - minCg) / (maxCg - minCg);
			canvas.drawLine((float)x, 0, (float)x, getHeight(), gridPaint2);
		}
		for (double weight = minWeight; weight < maxWeight; weight += 100.0 ) {
			double y = getHeight() * (maxWeight - weight) / (maxWeight - minWeight);
			canvas.drawLine(0, (float)y, getWidth(), (float)y, gridPaint2);
		}
		
		Station t = Limits[Limits.length - 1];
		double lastX = getWidth() * (t.Arm - minCg) / (maxCg - minCg);
		double lastY = getHeight() * (maxWeight - t.getWeightInPounds()) / (maxWeight - minWeight);
		
		for (Station s: Limits) {
			double thisX = getWidth() * (s.Arm - minCg) / (maxCg - minCg);
			double thisY = getHeight() * (maxWeight - s.getWeightInPounds()) / (maxWeight - minWeight);
			
			canvas.drawLine((float)lastX, (float)lastY, (float)thisX, (float)thisY, envelopePaint);
			
			lastX = thisX;
			lastY = thisY;
		}
		
		double x = getWidth() * (Position.Arm - minCg) / (maxCg - minCg);
		double y = getHeight() * (maxWeight - Position.getWeightInPounds()) / (maxWeight - minWeight);
		
		canvas.drawCircle((float)x, (float)y, 5, positionPaint);
		canvas.drawLine(0, (float)y, getWidth(), (float)y, positionPaint);
		canvas.drawLine((float)x, 0, (float)x, getHeight(), positionPaint);
	}
}
