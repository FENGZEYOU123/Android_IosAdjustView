package com.yfz.main;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 作者：游丰泽
 * 简介：仿ios风格的亮度控制view
 * CSDN: https://blog.csdn.net/ruiruiddd
 * GITHUB: https://github.com/FENGZEYOU123
 */
public class IosColumnBrightnessView extends View {
    private Context mContext;
    //日志TAG
    private static final String TAG = IosColumnBrightnessView.class.getName();
    //系统亮度监听
    private BrightnessObserver mBrightnessObserver = null;
    //标记-是否是当前自己在调整亮度大小
    private boolean isMeAdjustVolume = true;
    //当前圆心半径
    private double mCircleRadius = 0;
    //当前圆心边长
    private double mCircleMaxWidth = 0;
    //当前UI高度与view高度的比例
    private double mCurrentDrawLoudRate = 0;
    //当前真实亮度与总亮度大小比例
    private double mCurrentRealLoudRate = 0;
    //系统最大亮度index-默认255
    private final int mMaxBrightness = 255;
    //记录按压时手指相对于组件view的高度
    private float mDownY;
    //手指移动的距离，视为亮度调整
    private float mMoveDistance;
    //系统audio管理
    private AudioManager mAudioManager;
    //当前亮度文字数字
    private String mTextLoud ="";
    //画笔
    private Paint mPaint;
    //位置
    private RectF mRectF;
    //当前Canvas LayerId
    private int layerId = 0;
    //亮度图标圆弧位置
    private RectF mRectVolumeArc =new RectF();
    //亮度图标静音位置
    private Rect mRectVolumeDrawable=new Rect();
    //亮度图标margin
    private int mRectVolumeDrawableMargin=10;
    //亮度图标粗细
    private final static int mRectVolumeDrawableWidth=4;
    //亮度图标开始角度
    private final static int mRectVolumeDrawableStartAngle=315;
    //亮度图标终止角度
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
     * 设置当前亮度颜色-xml-iosColumnAudioView_setColorLoud
     */
    private int mColorLoud = Color.GRAY;
    /**
     * 设置组件背景颜色-xml-iosColumnAudioView_setColorBackground
     */
    private int mColorBackground = Color.DKGRAY;

    /**
     * 设置是否画亮度文字-iosColumnAudioView_setIsDrawTextVolume
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
     * 设置是否画亮度图标-iosColumnAudioView_setIsDrawDrawableVolume
     * @param context
     */
    private boolean mIsDrawDrawableVolume = true;
    /**
     * 设置亮度圆弧颜色-xml-iosColumnAudioView_setColorVolume
     */
    private int mColorVolume = Color.DKGRAY;
    /**
     * 设置亮度静音图标drawable-xml-iosColumnAudioView_setColorVolumeDrawable
     */
    private Drawable mColorDrawable = null;
    //固定组件高度长度，这里不做适配，可自行修改
    private int mViewHeight = 150, mViewWeight=50;

    public IosColumnBrightnessView(Context context) {
        super(context);
        initial(context);
    }

    public IosColumnBrightnessView(Context context, @Nullable AttributeSet attrs) {
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
        mColorVolume = typedArray.getColor(R.styleable.IosColumnAudioView_iosColumnAudioView_setVolumeColor, mColorVolume);
        mColorDrawable = typedArray.getDrawable(R.styleable.IosColumnAudioView_iosColumnAudioView_setVolumeDrawable);
        initial(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRectVolumeDrawableMargin = MeasureSpec.getSize(widthMeasureSpec)/10;
        mCircleMaxWidth = MeasureSpec.getSize(widthMeasureSpec)/2;
        //固定组件高度长度，这里不做适配，可自行修改
        setMeasuredDimension(dp2px(mContext,mViewWeight),dp2px(mContext,mViewHeight));
    }

    private void initial(Context context){
        mContext=context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mCurrentDrawLoudRate = getCalculateLoudRate();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mRectF = new RectF();
        setWillNotDraw(false);
        setBackgroundColor(Color.TRANSPARENT);
        mPaint.setTextSize(mTextSize);
        getPermission();
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
     * 更新所有内容-ui-系统亮度
     */
    private void refreshAll(){
        setSystemBrightness((int)(mCurrentDrawLoudRate * mMaxBrightness));
        refreshUI();
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
        onDrawFront(canvas); //画当前亮度前景表示当前多大亮度
        onDrawText(canvas); //画文字
        onDrawSunCircle(canvas);//画底部图标圆心
        canvas.restoreToCount(layerId);
    }
    /**
     * 计算手指移动后亮度UI占比大小，视其为亮度大小
     */
    private void calculateLoudRate(){
        mCurrentDrawLoudRate = ( getHeight() * mCurrentDrawLoudRate + mMoveDistance) /  getHeight();
        if(mCurrentDrawLoudRate >=1){
            mCurrentDrawLoudRate =1;
        }
        if(mCurrentDrawLoudRate <=0){
            mCurrentDrawLoudRate =0;
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
     * 画亮度背景-方形-随手势上下滑动而变化用来显示亮度大小
     * @param canvas
     */
    private void onDrawFront(Canvas canvas){
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        mPaint.setColor(mColorLoud);
        mRectF.left=0;
        mRectF.top=(canvas.getHeight()-(int)(canvas.getHeight() * mCurrentDrawLoudRate));
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
            mTextLoud = "" + (int) (mCurrentDrawLoudRate * 100);
            mPaint.setColor(mTextColor);
            canvas.drawText(mTextLoud, (canvas.getWidth() / 2 - mPaint.measureText(mTextLoud) / 2), mTextHeight >= 0 ? mTextHeight : getHeight() / 6, mPaint);
        }
    }
    /**
     * 画亮度图标
     */
    private void onDrawSunCircle(Canvas canvas){
        if(mIsDrawDrawableVolume){ //如果开启了则开始绘制
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeWidth(mRectVolumeDrawableWidth);
            mPaint.setColor(mColorVolume);
            mCircleRadius=(1-mCurrentDrawLoudRate ) *2;
            Log.d(TAG, "onDrawSunCircle: "+mCurrentDrawLoudRate + "   "+mCircleRadius);

            mRectF.left= (float)(( mCircleMaxWidth-mRectVolumeDrawableMargin) * mCircleRadius );
            mRectF.right=(float)( canvas.getWidth()-( mCircleMaxWidth-mRectVolumeDrawableMargin) * mCircleRadius) ;
//            mCircleWidth=
            mRectF.bottom=(float)((canvas.getHeight()*0.9)-( mCircleMaxWidth-mRectVolumeDrawableMargin) * mCircleRadius);
            mRectF.top=(float)( (mRectF.bottom-canvas.getWidth()+( mCircleMaxWidth-mRectVolumeDrawableMargin) *2  * mCircleRadius));
//            canvas.drawCircle(canvas.getWidth()/2,(float) (canvas.getHeight()*0.9-mRectVolumeDrawableMargin),3,mPaint);
            canvas.drawArc(mRectF,0f,360f,false,mPaint);
        }
    }

    /**
     * 画亮度图片-静音drawable
     */
    private void onDrawVolumeMutedDrawable(Canvas canvas){

        mRectVolumeDrawable.left=mRectVolumeDrawableMargin * 3;
        mRectVolumeDrawable.right=canvas.getWidth()-mRectVolumeDrawableMargin * 3;
        mRectVolumeDrawable.bottom=(int)(canvas.getHeight()*0.9)-mRectVolumeDrawableMargin * 3;
        mRectVolumeDrawable.top= (int)(mRectVolumeDrawable.bottom-canvas.getWidth()+mRectVolumeDrawableMargin *2 *3);
        mColorDrawable.setBounds(mRectVolumeDrawable);
        mColorDrawable.draw(canvas);
    }
    /**
     * 画亮度图标-圆弧-计算多少个，这里是展示4个
     */
    private void onDrawVolumeDrawableArc(Canvas canvas){
        for(int i = 0; i<=(int)(mCurrentDrawLoudRate /0.33); i++){
                onDrawVolumeDrawableArc(canvas,(int)(mCurrentDrawLoudRate /0.33)-i); //画圆弧
            }
    }

    /**
     * 画亮度图标-圆弧
     */
    private void onDrawVolumeDrawableArc(Canvas canvas, int index){
        index=4-index;
        mRectVolumeArc.left=mRectVolumeDrawableMargin * index;
        mRectVolumeArc.right=canvas.getWidth()-mRectVolumeDrawableMargin * index;
        mRectVolumeArc.bottom=(int)(canvas.getHeight()*0.9)-mRectVolumeDrawableMargin * index;
        mRectVolumeArc.top= mRectVolumeArc.bottom-canvas.getWidth()+mRectVolumeDrawableMargin *2 *index;
        //画板偏移
        canvas.translate(-mRectVolumeDrawableMargin,0);
        //开始角度向 360度收缩，结束角度向0度收缩，这样就可以造成弧度随着亮度扩大而扩大，缩小而缩小的感觉
        canvas.drawArc(
                mRectVolumeArc,
                (float) (mRectVolumeDrawableStartAngle + ( (360 - mRectVolumeDrawableStartAngle) * (1- mCurrentDrawLoudRate))),
                (float) (mRectVolumeDrawableEndAngle - ( (mRectVolumeDrawableEndAngle-0) * (1- mCurrentDrawLoudRate))),
                false,
                mPaint);

    }

    /**
     * 监听view视图在window里时，监听系统亮度
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(mContext != null) {
            mBrightnessObserver = new BrightnessObserver(mContext, new Handler(Looper.getMainLooper()));
        }

    }
    /**
     * 监听view视图从window里抽离的时，取消广播的注册
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(null != mBrightnessObserver) {
            mBrightnessObserver.unregister();
        }
    }

    /**
     * 监听亮度改变
     */
    private class BrightnessObserver extends ContentObserver {

        private Context mContext;

        public BrightnessObserver(Context context, Handler handler) {
            super(handler);
            this.mContext =context;
            register();
        }

        public void register(){
            Uri brightnessUri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
            mContext.getContentResolver().registerContentObserver(brightnessUri, true,this);
        }
        public void unregister(){
            mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d(TAG, "onChange: "+getSystemBrightness());
        }
    }

    /**
     * 改变当前系统亮度
     * @return
     */
    public void setSystemBrightness(int brightness) {
        if(null != mContext) {
            if(brightness>=mMaxBrightness){
                brightness=mMaxBrightness;
            }
            if(brightness<=0){
                brightness=0;
            }
                Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,brightness);
        }
    }
    /**
     * 获取当前系统亮度
     * @return
     */
    public int getSystemBrightness(){
        if(null != mContext) {
            try {
              return Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

        }
        return 0;
    }
    /**
     * 计算亮度比例
     */
    private double getCalculateLoudRate(){
        return (double) mAudioManager.getStreamVolume(mAudioManagerStreamType)/ mMaxBrightness;
    }
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
    /**
     *
     */
    private void getPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(mContext)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } else {
                // 申请权限后做的操作
            }
        }
    }
}
