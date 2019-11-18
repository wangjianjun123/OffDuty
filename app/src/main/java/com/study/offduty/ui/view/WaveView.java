package com.study.offduty.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.study.offduty.R;

/**
 * Description:  水波纹控件
 * User:         Wan jy
 * Date:         2017-08-10  09:38
 */

public class WaveView extends View {

    private Context mContext;
    //波浪画笔
    private Paint mPaint;
    //波浪Path类
    private Path mPath;
    //一个波浪长度
    private int mWaveLength = 600;
    //波纹个数
    private int mWaveCount;
    //平移偏移量
    private int mOffset;
    //波纹的中间轴
    private int mCenterY;
    //波纹的浪高
    private int mWaveRadius;
    //屏幕高度
    private int mScreenHeight;
    //屏幕宽度
    private int mScreenWidth;
    private Bitmap mBitmapBoat;

    public WaveView(Context context) {
        super(context);
        init(context);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(ContextCompat.getColor(context, R.color.colorBlue200));
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mWaveRadius = 100;
        mBitmapBoat = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.icon_boat);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        mScreenHeight = h;
        mScreenWidth = w;
        //加1.5：至少保证波纹有2个，至少2个才能实现平移效果
        mWaveCount = (int) Math.round(mScreenWidth / mWaveLength + 1.5);
        mCenterY = mScreenHeight / 3;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPath.reset();
        //移到屏幕外最左边
        mPath.moveTo(-mWaveLength + mOffset, mCenterY);
        for (int i = 0; i < mWaveCount; i++) {
            //正弦曲线
            mPath.quadTo((-mWaveLength * 3 / 4) + (i * mWaveLength) + mOffset, mCenterY + mWaveRadius
                    , (-mWaveLength / 2) + (i * mWaveLength) + mOffset, mCenterY);
            mPath.quadTo((-mWaveLength / 4) + (i * mWaveLength) + mOffset
                    , mCenterY - mWaveRadius, i * mWaveLength + mOffset, mCenterY);
        }
        //填充矩形
        mPath.lineTo(mScreenWidth, mScreenHeight);
        mPath.lineTo(0, mScreenHeight);
        mPath.close();
        canvas.drawPath(mPath, mPaint);
        //小船
        drawBoat(canvas);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        openWaveAnimator();
    }

    /**
     * 绘制小船
     */
    private void drawBoat(Canvas canvas) {
        float x = mScreenWidth * 2 / 3;
        Region region = new Region();
        Region clip = new Region((int) (x - 0.1), 0, (int) x, mScreenHeight);//宽度为0.1的一根直线
        region.setPath(mPath, clip);
        Rect rect = region.getBounds();
        canvas.drawBitmap(mBitmapBoat, rect.right - (mBitmapBoat.getWidth() / 2), rect.top - (mBitmapBoat.getHeight() / 2), mPaint);
    }

    /**
     * 波纹平移动画
     */
    private void openWaveAnimator() {
        ValueAnimator animator = ValueAnimator.ofInt(0, mWaveLength);
        animator.setDuration(1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOffset = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        animator.start();
    }

}
