package com.yfz.main.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
    private final int MAX_LOUD=100;
    private double mCurrentLoudRate = 0;
    private Drawable mDrawable_outside = null;
    private Drawable mDrawable_inside = null;
    private float mDownX,mDownY;
    private float mDistanceX,mDistanceY;


    public IosColumnAudioView(Context context) {
        super(context);
        initial(context);
    }
    public IosColumnAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.IosColumnAudioView);
        mDrawable_outside=typedArray.getDrawable(R.styleable.IosColumnAudioView_IosColumnAudioView_setBackgroundOutSide);
        mDrawable_inside=typedArray.getDrawable(R.styleable.IosColumnAudioView_IosColumnAudioView_setBackgroundInSide);
        initial(context);
    }
    private void initial(Context context){
        mContext=context;
        if(null == mDrawable_outside){
            setBackgroundColor(Color.GRAY);
        }else {
            setBackground(mDrawable_outside);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDownX=event.getX();
                mDownY=event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mDistanceX=event.getX()-mDownX;
                mDistanceY=event.getY()-mDownY;
                Log.d("TAG", "onTouchEvent距离: "+mDistanceX+"  "+mDistanceY);
                mCurrentLoudRate = getHeight() / ( getHeight()* mCurrentLoudRate + mDistanceY) ;
                Log.d("TAG", "onTouchEvent: "+ mCurrentLoudRate);
                if(mCurrentLoudRate >=1){
                    mCurrentLoudRate =1;
                }
                if(mCurrentLoudRate <=0){
                    mCurrentLoudRate =0;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        refresh();
        return true;
    }

    private void refresh(){
        postInvalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(null != mDrawable_inside) {
            mDrawable_inside.setBounds(0,(int)(getHeight()-getHeight() * mCurrentLoudRate),getWidth(),getHeight());
            mDrawable_inside.draw(canvas);
        }
    }
}
