package com.yfz.main.View;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

import com.yfz.main.R;

/**
 * 作者：游丰泽
 * 简介：仿ios风格的音量控制view
 * CSDN: https://blog.csdn.net/ruiruiddd
 * GITHUB: https://github.com/FENGZEYOU123
 */
public class IosColumnAudioView extends View {
    private Context mContext;
    //日志TAG
    private static final String TAG = IosColumnAudioView.class.getName();
    //系统声音广播名
    private static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    //系统声音广播接收器
    private MyVolumeReceiver mVolumeReceiver = null;
    //标记-是否是当前自己在调整音量大小
    private boolean isMeAdjustVolume = true;
    //当前UI高度与view高度的比例
    private double mCurrentLoudRate = 0.5;
    //系统最大声音index
    private int mMaxLoud=0;
    //记录按压时手指相对于组件view的高度
    private float mDownY;
    //手指移动的距离，视为音量调整
    private float mMoveDistance;
    //系统audio管理
    private AudioManager mAudioManager;
    //当前音量文字数字
    private String mTextLoud ="";
    //画笔
    private Paint mPaint;
    //位置
    private RectF mRectF;
    //当前Canvas LayerId
    private int layerId = 0;
    //音量图标位置
    private RectF mRectVolumeDrawable=new RectF();
    //音量图标margin
    private final static int mRectVolumeDrawableMargin=10;
    //音量图标粗细
    private final static int mRectVolumeDrawableWidth=4;
    //音量图标开始角度
    private final static int mRectVolumeDrawableStartAngle=315;
    //音量图标终止角度
    private final static int mRectVolumeDrawableEndAngle=90;
    /**
     * 设置声音流类型-默认音乐-iosColumnAudioView_setAudioStreamType
     */
    private int mAudioManagerStreamType = AudioManager.STREAM_MUSIC;
    /**
     * 设置圆弧度数-xml-iosColumnAudioView_setRadiusXY
     */
    private float mRXY=40;
    /**
     * 设置当前音量颜色-xml-iosColumnAudioView_setColorLoud
     */
    private int mColorLoud = Color.GRAY;
    /**
     * 设置组件背景颜色-xml-iosColumnAudioView_setColorBackground
     */
    private int mColorBackground = Color.DKGRAY;

    /**
     * 设置是否画音量文字-iosColumnAudioView_setIsDrawTextVolume
     * @param context
     */
    private boolean mIsDrawTextVolume = true;
    /**
     * 设置文字大小-xml-iosColumnAudioView_setTextSize
     */
    private float mTextSize = 15;
    /**
     * 设置文字颜色-xml-iosColumnAudioView_setTextColor
     */
    private int mTextColor = Color.BLACK;
    /**
     * 设置文字高度-xml-iosColumnAudioView_setTextHeight
     * @param context
     */
    private int mTextHeight = -1;
    /**
     * 设置是否画音量图标-iosColumnAudioView_setIsDrawDrawableVolume
     * @param context
     */
    private boolean mIsDrawDrawableVolume = true;
    /**
     * 设置音量图标颜色-xml-iosColumnAudioView_setColorVolumeDrawable
     */
    private int mColorVolumeDrawable = Color.DKGRAY;


    public IosColumnAudioView(Context context) {
        super(context);
        initial(context);
    }

    public IosColumnAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mTextSize = sp2px(context,mTextSize);
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.IosColumnAudioView);
        mColorBackground = typedArray.getColor(R.styleable.IosColumnAudioView_iosColumnAudioView_setColorBackground,mColorBackground);
        mColorLoud = typedArray.getColor(R.styleable.IosColumnAudioView_iosColumnAudioView_setColorLoud,mColorLoud);
        mAudioManagerStreamType =typedArray.getInteger(R.styleable.IosColumnAudioView_iosColumnAudioView_setAudioStreamType, mAudioManagerStreamType);
        mRXY = typedArray.getDimension(R.styleable.IosColumnAudioView_iosColumnAudioView_setRadiusXY, mRXY);
        mTextSize = typedArray.getDimension(R.styleable.IosColumnAudioView_iosColumnAudioView_setTextSize,mTextSize);
        mTextColor = typedArray.getColor(R.styleable.IosColumnAudioView_iosColumnAudioView_setTextColor,mTextColor);
        mTextHeight = typedArray.getInt(R.styleable.IosColumnAudioView_iosColumnAudioView_setTextHeight,mTextHeight);
        mIsDrawTextVolume = typedArray.getBoolean(R.styleable.IosColumnAudioView_iosColumnAudioView_setIsDrawTextVolume,mIsDrawTextVolume);
        mIsDrawDrawableVolume = typedArray.getBoolean(R.styleable.IosColumnAudioView_iosColumnAudioView_setIsDrawDrawableVolume,mIsDrawDrawableVolume);
        mColorVolumeDrawable = typedArray.getColor(R.styleable.IosColumnAudioView_iosColumnAudioView_setColorVolumeDrawable,mColorVolumeDrawable);
        initial(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void initial(Context context){
        mContext=context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMaxLoud = mAudioManager.getStreamMaxVolume(mAudioManagerStreamType);
        mCurrentLoudRate = getCalculateLoudRate();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mRectF = new RectF();
        setWillNotDraw(false);
        setBackgroundColor(Color.TRANSPARENT);
        mPaint.setTextSize(mTextSize);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDownY=event.getY();
                isMeAdjustVolume=true;
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveDistance = mDownY - event.getY();
                calculateLoudRate();
                mDownY=event.getY();
                break;
            case MotionEvent.ACTION_UP:
                isMeAdjustVolume=false;
                break;
            default:
                break;
        }
        refreshAll();
        return true;
    }

    /**
     * 更新所有内容-ui-音乐大小
     */
    private void refreshAll(){
        refreshStreamVolume(((int)(mCurrentLoudRate * mMaxLoud))<= mMaxLoud? (int)(mCurrentLoudRate * mMaxLoud) : mMaxLoud);
        refreshUI();
    }
    /**
     * 设置音量大小
     * @param currentVolume
     */
    public void refreshStreamVolume(int currentVolume){
        mAudioManager.setStreamVolume(mAudioManagerStreamType,currentVolume, 0);
    }
    /**
     * 刷新UI
     */
    public void refreshUI(){
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        layerId = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        onDrawBackground(canvas); //画背景
        onDrawLoud(canvas); //画当前音量前景表示当前多大音量
        onDrawText(canvas); //画文字
        onDrawVolumeDrawable(canvas);//画底部音量图标
        canvas.restoreToCount(layerId);
    }
    /**
     * 计算手指移动后音量UI占比大小，视其为音量大小
     */
    private void calculateLoudRate(){
        mCurrentLoudRate = ( getHeight() * mCurrentLoudRate + mMoveDistance) /  getHeight();
        if(mCurrentLoudRate >=1){
            mCurrentLoudRate =1;
        }
        if(mCurrentLoudRate <=0){
            mCurrentLoudRate =0;
        }
    }
    /**
     * 画圆弧背景
     * @param canvas
     */
    private void onDrawBackground(Canvas canvas){
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mColorBackground );
        mRectF.left=0;
        mRectF.top=0;
        mRectF.right=canvas.getWidth();
        mRectF.bottom=canvas.getHeight();
        canvas.drawRoundRect(mRectF,mRXY,mRXY,mPaint);
    }
    /**
     * 画音量背景-方形-随手势上下滑动而变化用来显示音量大小
     * @param canvas
     */
    private void onDrawLoud(Canvas canvas){
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        mPaint.setColor(mColorLoud);
        mRectF.left=0;
        mRectF.top=(canvas.getHeight()-(int)(canvas.getHeight() * mCurrentLoudRate));
        mRectF.right=canvas.getWidth();
        mRectF.bottom=canvas.getHeight();
        canvas.drawRect(mRectF,mPaint);
        mPaint.setXfermode(null);
    }
    /**
     * 画文字-展示当前语音大小
     * @param canvas
     */
    private void onDrawText(Canvas canvas){
        if(mIsDrawTextVolume) { //如果开启了则开始绘制
            mPaint.setStyle(Paint.Style.FILL);
            mTextLoud = "" + (int) (mCurrentLoudRate * 100);
            mPaint.setColor(mTextColor);
            canvas.drawText(mTextLoud, (canvas.getWidth() / 2 - mPaint.measureText(mTextLoud) / 2), mTextHeight >= 0 ? mTextHeight : getHeight() / 6, mPaint);
        }
    }
    /**
     * 画音量图标
     */
    private void onDrawVolumeDrawable(Canvas canvas){
        if(mIsDrawDrawableVolume){ //如果开启了则开始绘制
            mRectVolumeDrawable.left=mRectVolumeDrawableMargin;
            mRectVolumeDrawable.right=canvas.getWidth()-mRectVolumeDrawableMargin;
            mRectVolumeDrawable.bottom=(int)(canvas.getHeight()*0.9)-mRectVolumeDrawableMargin;
            mRectVolumeDrawable.top=mRectVolumeDrawable.bottom-canvas.getWidth()+mRectVolumeDrawableMargin*2;
            mPaint.setColor(mColorVolumeDrawable);
            onDrawVolumeDrawableArc(canvas); //画圆弧
        }
    }
    /**
     * 画音量图标-圆弧
     */
    private void onDrawVolumeDrawableArc(Canvas canvas){
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mRectVolumeDrawableWidth);
        //开始角度向 360度收缩，结束角度向0度收缩，这样就可以造成弧度随着音量扩大而缩小的感觉
        canvas.drawArc(
                mRectVolumeDrawable,
                (float) (mRectVolumeDrawableStartAngle + ( (360 - mRectVolumeDrawableStartAngle) * (1-mCurrentLoudRate))),
                (float) (mRectVolumeDrawableEndAngle - ( (mRectVolumeDrawableEndAngle-0) * (1-mCurrentLoudRate) )),
                false,mPaint);

    }
    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
    /**
     * 监听view视图在window里时，监听系统声音并注册广播
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        myRegisterReceiver(mContext);
    }
    /**
     * 监听view视图从window里抽离的时，取消广播的注册
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unRegisterReceiver();//取消注册系统声音监听
    }
    /**
     * 注册音量广播-监听音量改变事件
     */
    private void myRegisterReceiver(Context context) {
        if(null != context) {
            mVolumeReceiver = new MyVolumeReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(VOLUME_CHANGED_ACTION);
            context.registerReceiver(mVolumeReceiver, intentFilter);
        }
    }
    /**
     * 取消广播
     */
    private void unRegisterReceiver(){
        if(mVolumeReceiver!=null) {
            mContext.unregisterReceiver(mVolumeReceiver);
        }
    }
    /**
     * 继承广播接受者
     */
    private class MyVolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
         if (intent.getAction().equals(VOLUME_CHANGED_ACTION)){
             if(intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1) == mAudioManagerStreamType){
                 if(!isMeAdjustVolume) {
                     mCurrentLoudRate = getCalculateLoudRate();
                     refreshUI();
                 }
             }
           }
        }
    }
    /**
     * 计算音量比例
     */
    private double getCalculateLoudRate(){
        return (double) mAudioManager.getStreamVolume(mAudioManagerStreamType)/ mMaxLoud;
    }

}
