package com.android.calendar.month;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * Wraps the month grid (a vertically-scrolling ListView) and additionally
 * recognizes clearly-horizontal swipes, intercepting them before the child
 * ListView can start a vertical scroll, so a left/right swipe can be used to
 * jump to the next/previous month (One UI-style) without disturbing the
 * existing vertical week-scrolling behavior for anything that isn't a clear
 * horizontal gesture.
 */
public class HorizontalMonthSwipeLayout extends FrameLayout {

    /** Callback for a recognized horizontal swipe. direction: -1 = previous, +1 = next. */
    public interface OnMonthSwipeListener {
        void onMonthSwipe(int direction);

        /** Called continuously while dragging so the caller can translate the
         *  content live under the finger, like a real page swipe. */
        default void onMonthSwipeProgress(float dx) {}

        /** Called on release when no swipe threshold was reached, so the
         *  caller can animate the content back to rest. */
        default void onMonthSwipeCancelled() {}
    }

    private final int mTouchSlop;
    private float mDownX;
    private float mDownY;
    private boolean mIntercepting;
    private boolean mHandledSwipe;
    private OnMonthSwipeListener mListener;

    public HorizontalMonthSwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setOnMonthSwipeListener(OnMonthSwipeListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                mIntercepting = false;
                mHandledSwipe = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIntercepting) {
                    float dx = ev.getX() - mDownX;
                    float dy = ev.getY() - mDownY;
                    // Claim ANY drag past the touch slop, not just horizontal
                    // ones — One UI's month view doesn't scroll vertically
                    // at all, so a vertical drag should do nothing rather
                    // than fall through to the child ListView's own scroll.
                    if (Math.abs(dx) > mTouchSlop || Math.abs(dy) > mTouchSlop) {
                        mIntercepting = true;
                        return true;
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mIntercepting) return false;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                if (mListener != null) {
                    mListener.onMonthSwipeProgress(ev.getX() - mDownX);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!mHandledSwipe) {
                    float dx = ev.getX() - mDownX;
                    float dy = ev.getY() - mDownY;
                    // A generous threshold — this is a whole-month jump, not
                    // a fine-grained drag, so require a fairly deliberate,
                    // clearly-horizontal swipe. A vertical/diagonal drag is
                    // just absorbed (no scroll happens) rather than doing
                    // nothing useful.
                    if (Math.abs(dx) > mTouchSlop * 4 && Math.abs(dx) > Math.abs(dy)
                            && mListener != null) {
                        mListener.onMonthSwipe(dx < 0 ? 1 : -1);
                        mHandledSwipe = true;
                    } else if (mListener != null) {
                        mListener.onMonthSwipeCancelled();
                    }
                }
                mIntercepting = false;
                break;
            default:
                break;
        }
        return true;
    }
}
