package hr.sil.android.seeusadmin.fonts

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView

/**
 * @author mfatiga
 */
internal class TextViewBoldFont : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val tf = Typeface.createFromAsset(context.assets, "fonts/Opensans-Bold.ttf")
        setTypeface(tf)
    }
}