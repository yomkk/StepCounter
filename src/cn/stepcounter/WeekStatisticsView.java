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

	private Paint xLinePaint;// 坐标轴 轴线 画笔：
	private Paint hLinePaint;// 坐标轴水平内部 虚线画笔
	private Paint titlePaint;// 绘制文本的画笔
	private Paint paint;// 矩形画笔 柱状图的样式信息
	private int[] aniProgress;// 实现动画的值
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

		// 给画笔设置颜色
		xLinePaint.setColor(Color.DKGRAY);
		hLinePaint.setColor(Color.LTGRAY);
		titlePaint.setColor(Color.BLACK);

		// 加载画图
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
		// 绘制底部的线条
		canvas.drawLine(dp2px(0), height + dp2px(3), width - dp2px(0), height + dp2px(3), xLinePaint);
		int leftHeight = height - dp2px(5);// 左侧外周的 需要划分的高度：

		titlePaint.setTextAlign(Align.RIGHT);
		titlePaint.setTextSize(sp2px(12));
		titlePaint.setAntiAlias(true);
		titlePaint.setStyle(Paint.Style.FILL);
		// 绘制 X 做坐标
		int xAxisLength = width - dp2px(30);
		int step = xAxisLength / 6;

		// 设置底部的数字
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -6);
		for (int i = 0; i < 7; i++) {
			// text, baseX, baseY, textPaint
			String yesterday = new SimpleDateFormat("MM-dd").format(calendar.getTime());
			canvas.drawText(yesterday, step * i + dp2px(31), height + dp2px(20), titlePaint);
			calendar.add(Calendar.DATE, 1);
		}
		max = getMAX(aniProgress);
		// 绘制矩形
		if (aniProgress != null && aniProgress.length > 0) {
			for (int i = 0; i < aniProgress.length; i++) {// 循环遍历将7条柱状图形画出来
				int value = aniProgress[i];
				paint.setAntiAlias(true);// 抗锯齿效果
				paint.setStyle(Paint.Style.FILL);
				paint.setTextSize(sp2px(15));// 字体大小
				paint.setColor(Color.parseColor("#6DCAEC"));// 字体颜色
				Rect rect = new Rect();// 柱状图的形状

				rect.left = step * i;
				rect.right = dp2px(30) + step * i;
				int rh = (int) (leftHeight - leftHeight * (value / (max / 0.9)));
				rect.top = rh + dp2px(10);
				rect.bottom = height;

				canvas.drawBitmap(bitmap, null, rect, paint);
				// 是否显示柱状图上方的数字
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