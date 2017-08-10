package com.study.offduty.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.study.offduty.R;

import java.util.Random;

/**
 * Description:  爱心泡泡
 * User:         Wan jy
 * Date:         2017-08-08  10:37
 */

public class FavorLayout extends RelativeLayout {
    private Context mContext;
    private Drawable[] mFavorDrawables;
    private Interpolator[] mInterpolator;
    private int mWidth;
    private int mHeight;
    private Random mRandom = new Random();//用于实现随机功能

    public FavorLayout(Context context) {
        super(context);
        mContext = context;
        init(context);
    }

    public FavorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context);
    }

    public FavorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    //重写onMeasure 获取控件宽高
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //注意!!  获取本身的宽高 需要在测量之后才有宽高
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        mContext = context;
        //设置背景颜色
        setBackgroundColor(ContextCompat.getColor(context, R.color.noColor));
        //系统的差值器
        mInterpolator = new Interpolator[4];
        mInterpolator[0] = new DecelerateInterpolator();//减速;
        mInterpolator[1] = new AccelerateInterpolator();//加速
        mInterpolator[2] = new LinearInterpolator();//线性
        mInterpolator[3] = new AccelerateDecelerateInterpolator();//先加速后减速
        //爱心集合
        mFavorDrawables = new Drawable[4];
        mFavorDrawables[0] = ContextCompat.getDrawable(mContext, R.mipmap.icon_little_fish);
        mFavorDrawables[1] = ContextCompat.getDrawable(mContext, R.mipmap.icon_yellow_fish);
        mFavorDrawables[2] = ContextCompat.getDrawable(mContext, R.mipmap.icon_blue_fish);
        mFavorDrawables[3] = ContextCompat.getDrawable(mContext, R.mipmap.icon_big_fish);
    }

    /**
     * 添加爱心
     */
    public void addFavor() {

        ImageView imageView = new ImageView(getContext());
        //随机选一个
        int random = mRandom.nextInt(mFavorDrawables.length);
        LayoutParams layoutParams = new LayoutParams(mFavorDrawables[random].getIntrinsicWidth()
                , mFavorDrawables[random].getIntrinsicHeight());
        imageView.setImageDrawable(mFavorDrawables[random]);
        imageView.setLayoutParams(layoutParams);

        addView(imageView);

        Animator set = getAnimator(imageView);
        set.addListener(new AnimEndListener(imageView));
        set.start();

    }

    /**
     * 动画结束监听(清除view 避免内存溢出)
     */
    private class AnimEndListener extends AnimatorListenerAdapter {
        private View target;

        AnimEndListener(View target) {
            this.target = target;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            removeView((target));
        }
    }

    /**
     * 贝塞尔曲线动画(爱心)
     */
    private ValueAnimator getBezierValueAnimator(View target) {

        //初始化一个BezierEvaluator
        BezierEvaluator evaluator = new BezierEvaluator(getPointF(2), getPointF(1));

        //这里最好画个图 理解一下 传入了起点 和 终点(从屏幕右边进入)
        ValueAnimator animator = ValueAnimator.ofObject(
                evaluator
                , new PointF(mWidth + 100, mHeight - mFavorDrawables[0].getIntrinsicHeight())
                , new PointF(0, mRandom.nextInt(mHeight / 2) + (mHeight / 2)));
        animator.addUpdateListener(new BezierListener(target));
        animator.setTarget(target);
        animator.setDuration(5000);
        return animator;
    }

    /**
     * 动画监听
     */
    private class BezierListener implements ValueAnimator.AnimatorUpdateListener {

        private View target;

        BezierListener(View target) {
            this.target = target;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            //这里获取到贝塞尔曲线计算出来的的x y值 赋值给view 这样就能让爱心随着曲线走啦
            PointF pointF = (PointF) animation.getAnimatedValue();
            target.setX(pointF.x);
            target.setY(pointF.y);
            target.setAlpha(1 - animation.getAnimatedFraction());
        }
    }

    /**
     * 获取中间的两个点(随机生成)
     */
    private PointF getPointF(int scale) {

        PointF pointF = new PointF();
        pointF.x = mRandom.nextInt(mWidth);
        //scale 为了控制第二点始终位于第一个点的上方
        pointF.y = mRandom.nextInt(mHeight / 2) / scale + (mHeight / 2);
        return pointF;
    }

    /**
     * 最终动画
     */
    private Animator getAnimator(View target) {
        ValueAnimator bezierValueAnimator = getBezierValueAnimator(target);

        AnimatorSet finalSet = new AnimatorSet();
        finalSet.playSequentially(bezierValueAnimator);
        finalSet.setInterpolator(mInterpolator[mRandom.nextInt(mInterpolator.length)]);//实现随机变速
        finalSet.setTarget(target);
        return finalSet;
    }

}
