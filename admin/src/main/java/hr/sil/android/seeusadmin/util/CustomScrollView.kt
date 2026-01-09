package hr.sil.android.seeusadmin.util

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class CustomScrollView(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs) {

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val action = ev!!.action
        when (action) {
            MotionEvent.ACTION_DOWN ->             //Log.i("CustomScrollView", "onInterceptTouchEvent: DOWN super false" );
                super.onTouchEvent(ev)
            MotionEvent.ACTION_MOVE -> return false // redirect MotionEvents to ourself
            MotionEvent.ACTION_CANCEL ->             // Log.i("CustomScrollView", "onInterceptTouchEvent: CANCEL super false" );
                super.onTouchEvent(ev)
            MotionEvent.ACTION_UP ->             //Log.i("CustomScrollView", "onInterceptTouchEvent: UP super false" );
                return false
            else -> {
            }
        }

        return false
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        super.onTouchEvent(ev);
        //Log.i("CustomScrollView", "onTouchEvent. action: " + ev.getAction() );
        return true;
    }

}