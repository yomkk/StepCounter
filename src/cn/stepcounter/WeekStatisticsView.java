package cn.stepcounter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class WeekStatisticsView extends View {

	private Paint xLinePaint;// ������ ���� ���ʣ�
	private Paint hLinePaint;// ������ˮƽ�ڲ� ���߻���
	private Paint titlePaint;// �����ı��Ļ���
	private Paint paint;// ���λ��� ��״ͼ����ʽ��Ϣ
	private int[] aniProgress;// ʵ�ֶ�����ֵ
	private Bitmap bitmap;
	int max;

	public WeekStatisticsView(Context context) {
		super(context);
		init();
	}

	public WeekStatisticsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {

		aniProgress = new int[] { 0, 0, 0, 0, 0, 0, 0 };

		xLinePaint = new Paint();
		hLinePaint = new Paint();
		titlePaint = new Paint();
		paint = new Paint();

		// ������������ɫ
		xLinePaint.setColor(Color.DKGRAY);
		hLinePaint.setColor(Color.LTGRAY);
		titlePaint.setColor(Color.BLACK);

		// ���ػ�ͼ
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rect_bg);
	}

	void setnumber(int num[]) {
		aniProgress = num;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int width = getWidth();
		int height = getHeight() - dp2px(50);
		// ���Ƶײ�������
		canvas.drawLine(dp2px(0), height + dp2px(3), width - dp2px(0), height + dp2px(3), xLinePaint);
		int leftHeight = height - dp2px(5);// ������ܵ� ��Ҫ���ֵĸ߶ȣ�

		titlePaint.setTextAlign(Align.RIGHT);
		titlePaint.setTextSize(sp2px(12));
		titlePaint.setAntiAlias(true);
		titlePaint.setStyle(Paint.Style.FILL);
		// ���� X ������
		int xAxisLength = width - dp2px(30);
		int step = xAxisLength / 6;

		// ���õײ�������
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -6);
		for (int i = 0; i < 7; i++) {
			// text, baseX, baseY, textPaint
			String yesterday = new SimpleDateFormat("MM-dd").format(calendar.getTime());
			canvas.drawText(yesterday, step * i + dp2px(31), height + dp2px(20), titlePaint);
			calendar.add(Calendar.DATE, 1);
		}
		max = getMAX(aniProgress);
		// ���ƾ���
		if (aniProgress != null && aniProgress.length > 0) {
			for (int i = 0; i < aniProgress.length; i++) {// ѭ��������7����״ͼ�λ�����
				int value = aniProgress[i];
				paint.setAntiAlias(true);// �����Ч��
				paint.setStyle(Paint.Style.FILL);
				paint.setTextSize(sp2px(15));// �����С
				paint.setColor(Color.parseColor("#6DCAEC"));// ������ɫ
				Rect rect = new Rect();// ��״ͼ����״

				rect.left = step * i;
				rect.right = dp2px(30) + step * i;
				int rh = (int) (leftHeight - leftHeight * (value / (max / 0.9)));
				rect.top = rh + dp2px(10);
				rect.bottom = height;

				canvas.drawBitmap(bitmap, null, rect, paint);
				// �Ƿ���ʾ��״ͼ�Ϸ�������
				canvas.drawText(value + "", (step * i + dp2px(2)), rh + dp2px(5), paint);

			}
		}

	}

	int getMAX(int num[]) {
		int a, i;
		a = num[0];
		for (i = 1; i < 7; i++) {
			if (a < num[i])
				a = num[i];
		}
		return a;

	}

	private int dp2px(int value) {
		float v = getContext().getResources().getDisplayMetrics().density;
		return (int) (v * value + 0.5f);
	}

	private int sp2px(int value) {
		float v = getContext().getResources().getDisplayMetrics().scaledDensity;
		return (int) (v * value + 0.5f);
	}

}