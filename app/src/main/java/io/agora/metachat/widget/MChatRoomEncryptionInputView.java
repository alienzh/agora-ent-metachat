package io.agora.metachat.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatEditText;

import java.util.ArrayList;
import java.util.List;

import io.agora.metachat.R;

public class MChatRoomEncryptionInputView extends AppCompatEditText {
    private Paint sidePaint, backPaint, textPaint;
    private final Context mC;
    private String mText;
    private List<RectF> rectFS;
    private int strokeWidth, spaceX, spaceY, textSize;
    private int checkedColor, defaultColor, backColor, textColor, waitInputColor;
    private int textLength;
    private int circle, round;
    private boolean isPwd, isWaitInput, isStart;
    private Paint l;

    public MChatRoomEncryptionInputView(Context context) {
        this(context, null);
    }

    public MChatRoomEncryptionInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MChatRoomEncryptionInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mC = context;
        if (attrs != null) setAttrs(attrs);
        init();
    }

    private void setAttrs(AttributeSet attrs) {
        TypedArray t = mC.obtainStyledAttributes(attrs, R.styleable.mchat_encryption_input_style);
        if (t != null) {
            textLength = t.getInt(R.styleable.mchat_encryption_input_style_mchat_textLength, 6);
            spaceX = t.getDimensionPixelSize(R.styleable.mchat_encryption_input_style_mchat_space, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
            spaceY = t.getDimensionPixelSize(R.styleable.mchat_encryption_input_style_mchat_space, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()));
            strokeWidth = t.getDimensionPixelSize(R.styleable.mchat_encryption_input_style_mchat_strokeWidth, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
            round = t.getDimensionPixelSize(R.styleable.mchat_encryption_input_style_mchat_round, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()));
            circle = t.getDimensionPixelSize(R.styleable.mchat_encryption_input_style_mchat_circle, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
            textSize = t.getDimensionPixelSize(R.styleable.mchat_encryption_input_style_mchat_textSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            checkedColor = t.getColor(R.styleable.mchat_encryption_input_style_mchat_checkedColor, 0xff44ce61);
            defaultColor = t.getColor(R.styleable.mchat_encryption_input_style_mchat_defaultColor, 0xffd0d0d0);
            backColor = t.getColor(R.styleable.mchat_encryption_input_style_mchat_backColor, 0xfff1f1f1);
            textColor = t.getColor(R.styleable.mchat_encryption_input_style_mchat_textColor, 0xFF444444);
            waitInputColor = t.getColor(R.styleable.mchat_encryption_input_style_mchat_waitInputColor, 0xFF444444);
            isPwd = t.getBoolean(R.styleable.mchat_encryption_input_style_mchat_isPwd, true);
            isWaitInput = t.getBoolean(R.styleable.mchat_encryption_input_style_mchat_isWaitInput, true);
            t.recycle();
        }
    }

    private void init() {
        setTextColor(0X00ffffff); //???????????????????????????????????????
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        sidePaint = new Paint();
        backPaint = new Paint();
        textPaint = new Paint();

        rectFS = new ArrayList<>();
        mText = "";

        this.setBackgroundDrawable(null);
        setLongClickable(false);
        setTextIsSelectable(false);
        setCursorVisible(false);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
//        if (mText == null) {
//            return;
//        }
        //????????????????????????????????????????????????????????????????????????mText???
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (text.toString().length() <= textLength) {
            mText = text.toString();
        } else {
            setText(mText);
            if (getText()!=null){
                setSelection(getText().toString().length());  //?????????????????????
            }
            //??????setText(mText)???????????????????????????????????????????????????????????????
            setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        }
        if (OnTextChangeListener != null) OnTextChangeListener.onTextChange(mText);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                heightSize = MeasureSpec.getSize(heightMeasureSpec);
                break;
            case MeasureSpec.AT_MOST:
                heightSize = widthSize / textLength;
                break;
            default:
                break;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //????????????
        sidePaint.setAntiAlias(true);//????????????
        sidePaint.setStrokeWidth(strokeWidth);//?????????????????????
        sidePaint.setStyle(Paint.Style.STROKE);//??????????????????
        sidePaint.setColor(defaultColor);
        //???????????????
        backPaint.setStyle(Paint.Style.FILL);
        backPaint.setColor(backColor);
        //???????????????
        textPaint.setTextSize(textSize);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(textColor);

        int Wide = getMeasuredWidth() / textLength;
        RectF rect = null;
        for (int i = 0; i < textLength; i++) {
            //??????????????????????????????????????????
            if (mText.length() >= i) {
                sidePaint.setColor(checkedColor);
            } else {
                sidePaint.setColor(defaultColor);
            }
            rect = new RectF(i * Wide + spaceX, spaceY, i * Wide + Wide - spaceX, getMeasuredHeight() - spaceY); //????????????????????????4?????????????????????????????????
            canvas.drawRoundRect(rect, round, round, backPaint); //???????????????
            canvas.drawRoundRect(rect, round, round, sidePaint); //???????????????
            rectFS.add(rect);

            if (isWaitInput && i == mText.length()) {  //?????????????????????
                l = new Paint();
                l.setStrokeWidth(3);
                l.setStyle(Paint.Style.FILL);
                l.setColor(waitInputColor);
                canvas.drawLine(i * Wide + Wide / 2, getMeasuredHeight() / 2 - getMeasuredHeight() / 5, i * Wide + Wide / 2, getMeasuredHeight() / 2 + getMeasuredHeight() / 5, l);
            }
        }
        //???????????????
        for (int j = 0; j < mText.length(); j++) {
            if (isPwd) {
                canvas.drawCircle(rectFS.get(j).centerX(), rectFS.get(j).centerY(), circle, textPaint);
            } else {
                canvas.drawText(mText.substring(j, j + 1), rectFS.get(j).centerX() - (textSize - spaceX) / 2, rectFS.get(j).centerY() + (textSize - spaceY) / 2, textPaint);
            }
        }
    }

    // Cursor blink animation
    private Runnable cursorAnimation = new Runnable() {
        public void run() {

//         // Switch the cursor visibility and set it
//         Log.e("cursorAnimation","getAlpha1: " + l.getAlpha());
//         int newAlpha = (l.getAlpha() == 0) ? 255 : 0;
//         l.setAlpha(newAlpha);
//         Log.e("cursorAnimation","getAlpha2: " + l.getAlpha());
//         // Call onDraw() to draw the cursor with the new Paint
//         invalidate();
//         // Wait 500 milliseconds before calling self again
//         postDelayed(cursorAnimation, 2000);
        }
    };

    private int dp2px(float dpValue) {
        float scale = mC.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * ????????????
     */
    public interface OnTextChangeListener {
        void onTextChange(String pwd);
    }

    private OnTextChangeListener OnTextChangeListener;

    public void setOnTextChangeListener(OnTextChangeListener OnTextChangeListener) {
        this.OnTextChangeListener = OnTextChangeListener;
    }

    /**
     * ???????????????????????????
     */
    public void clearText() {
        setText("");
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
    }

    /**
     * ?????????????????????
     */
    public void setXSpace(int space) {
        spaceX = space;
    }

    /**
     * ?????????????????????
     */
    public void setYSpace(int space) {
        spaceY = space;
    }

    /**
     * ?????????????????????
     */
    public void setTextLength(int textLength) {
        this.textLength = textLength;
    }

    /**
     * ?????????????????????
     */
    public int getTextLength() {
        return this.textLength;
    }

    /**
     * ??????????????????????????????
     */
    public void setCheckedColorColor(int checkedColor) {
        this.checkedColor = checkedColor;
    }

    /**
     * ??????????????????????????????
     */
    public void setDefaultColorColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }

    /**
     * ????????????????????????
     */
    public void setBackColor(int backColor) {
        this.backColor = backColor;
    }

    /**
     * ???????????????????????????
     */
    public void setPwdTextColor(int textColor) {
        this.textColor = textColor;
    }

    /**
     * ??????????????? ???????????????
     */
    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }

    /**
     * ?????????????????????
     */
    public void setCircle(int Circle) {
        this.circle = Circle;
    }

    /**
     * ???????????????????????????
     */
    public void setRound(int Round) {
        this.round = Round;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public int getSpaceX() {
        return spaceX;
    }

    public int getSpaceY() {
        return spaceY;
    }

    public int getCheckedColor() {
        return checkedColor;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public int getBackColor() {
        return backColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getCircle() {
        return circle;
    }

    public int getRound() {
        return round;
    }

    public int getextSize() {
        return textSize;
    }

    public void settextSize(int textSize) {
        this.textSize = textSize;
    }

    public boolean isPwd() {
        return isPwd;
    }

    /**
     * ??????????????????
     *
     * @param pwd
     */
    public void setPwd(boolean pwd) {
        isPwd = pwd;
    }

    public int getWaitInputColor() {
        return waitInputColor;
    }

    /**
     * \
     * ?????????????????????
     *
     * @param waitInputColor
     */
    public void setWaitInputColor(int waitInputColor) {
        this.waitInputColor = waitInputColor;
    }

    public boolean isWaitInput() {
        return isWaitInput;
    }

    /**
     * ???????????????????????????
     *
     * @param waitInput
     */
    public void setWaitInput(boolean waitInput) {
        isWaitInput = waitInput;

    }

    public void rest() {
        removeCallbacks(cursorAnimation);
    }
}
