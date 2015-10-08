package com.practice.noyet.giftest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by noyet on 2015/9/24.
 */
public class MyGiftView extends View {

    /** gift动态效果总时长，在未设置时长时默认为1秒 */
    private static final int DEFAULT_MOVIE_DURATION = 1000;
    /** gift图片资源ID */
    private int mGiftId;
    /** Movie实例，用来显示gift图片 */
    private Movie mMovie;
    /** 显示gift图片的动态效果的开始时间 */
    private long mMovieStart;
    /** 动态图当前显示第几帧 */
    private int mCurrentAnimationTime = 0;
    /** 图片离屏幕左边的距离 */
    private float mLeft;
    /** 图片离屏幕上边的距离 */
    private float mTop;
    /** 图片的缩放比例 */
    private float mScale;
    /** 图片在屏幕上显示的宽度 */
    private int mMeasuredMovieWidth;
    /** 图片在屏幕上显示的高度 */
    private int mMeasuredMovieHeight;
    /** 是否显示动画,为true表示显示，false表示不显示 */
    private boolean mVisible = true;
    /** 动画效果是否被暂停 */
    private volatile boolean mPaused = false;

    public MyGiftView(Context context) {
        this(context, null);
    }

    public MyGiftView(Context context, AttributeSet attrs) {
        this(context, attrs, R.styleable.CustomTheme_gifViewStyle);
    }

    public MyGiftView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setViewAttributes(context, attrs, defStyleAttr);
    }

    /**
     *
     * @param context 上下文
     * @param attrs 自定义属性
     * @param defStyle 默认风格
     */
    @SuppressLint("NewApi")
    private void setViewAttributes(Context context, AttributeSet attrs,
                                   int defStyle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        // 从描述文件中读出gif的值，创建出Movie实例
        final TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.MyGiftView, defStyle, R.style.Widget_MyGiftView);
        mGiftId = array.getResourceId(R.styleable.MyGiftView_gif, -1);
        mPaused = array.getBoolean(R.styleable.MyGiftView_paused, false);
        array.recycle();
        if (mGiftId != -1) {
            byte[] bytes = getGiftBytes();
            mMovie = Movie.decodeByteArray(bytes, 0, bytes.length);
        }
    }

    /**
     * 设置gif图资源
     * @param giftResId
     */
    public void setMovieResource(int giftResId) {
        this.mGiftId = giftResId;
        byte[] bytes = getGiftBytes();
        mMovie = Movie.decodeByteArray(bytes, 0, bytes.length);
        requestLayout();
    }

    /**
     * 手动设置 Movie对象
     * @param movie Movie
     */
    public void setMovie(Movie movie) {
        this.mMovie = movie;
        requestLayout();
    }

    /**
     * 得到Movie对象
     * @return Movie
     */
    public Movie getMovie() {
        return mMovie;
    }

    /**
     * 设置要显示第几帧动画
     * @param time
     */
    public void setMovieTime(int time) {
        mCurrentAnimationTime = time;
        invalidate();
    }

    /**
     * 设置暂停
     *
     * @param paused
     */
    public void setPaused(boolean paused) {
        this.mPaused = paused;
        if (!paused) {
            mMovieStart = android.os.SystemClock.uptimeMillis()
                    - mCurrentAnimationTime;
        }
        invalidate();
    }

    /**
     * 判断gif图是否停止了
     *
     * @return
     */
    public boolean isPaused() {
        return this.mPaused;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMovie != null) {
            int movieWidth = mMovie.width();
            int movieHeight = mMovie.height();
            int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
            float scaleW = (float) movieWidth / (float) maximumWidth;
            mScale = 1f / scaleW;
            mMeasuredMovieWidth = maximumWidth;
            mMeasuredMovieHeight = (int) (movieHeight * mScale);
            setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);
        } else {
            setMeasuredDimension(getSuggestedMinimumWidth(),
                    getSuggestedMinimumHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mLeft = (getWidth() - mMeasuredMovieWidth) / 2f;
        mTop = (getHeight() - mMeasuredMovieHeight) / 2f;
        mVisible = getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMovie != null) {
            if (!mPaused) {
                updateAnimationTime();
                drawMovieFrame(canvas);
                invalidateView();
            } else {
                drawMovieFrame(canvas);
            }
        }
    }

    /**
     * 重绘
     */
    @SuppressLint("NewApi")
    private void invalidateView() {
        if (mVisible) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                postInvalidateOnAnimation();
            } else {
                invalidate();
            }
        }
    }

    /**
     * 更新当前显示进度
     */
    private void updateAnimationTime() {
        long now = android.os.SystemClock.uptimeMillis();
        // 如果第一帧，记录起始时间
        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        // 取出动画的时长
        int dur = mMovie.duration();
        if (dur == 0) {
            dur = DEFAULT_MOVIE_DURATION;
        }
        // 算出需要显示第几帧
        mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
    }

    /**
     * 绘制图片
     * @param canvas 画布
     */
    private void drawMovieFrame(Canvas canvas) {
        // 设置要显示的帧，绘制即可
        mMovie.setTime(mCurrentAnimationTime);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScale, mScale);
        mMovie.draw(canvas, mLeft / mScale, mTop / mScale);
        canvas.restore();
    }

    @SuppressLint("NewApi")
    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        mVisible = screenState == SCREEN_STATE_ON;
        invalidateView();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }

    /**
     * 将gif图片转换成byte[]
     * @return byte[]
     */
    private byte[] getGiftBytes() {
//        mMovie = Movie.decodeStream(getResources().openRawResource(
//                mGiftId));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = getResources().openRawResource(mGiftId);
        byte[] b = new byte[1024];
        int len;
        try {
            while ((len = is.read(b, 0, 1024)) != -1) {
                baos.write(b, 0, len);
            }
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return baos.toByteArray();
    }
}
