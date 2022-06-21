package com.example.pickerviewexample

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 7/1/14.
 *
 * Edited by John Codeos
 */
class WheelView : ScrollView {

    private lateinit var mContext: Context

    private lateinit var views: LinearLayout

    private var items: MutableList<String>? = null

    var offset = OFF_SET_DEFAULT

    var linesColor: Int = Color.YELLOW

    var selectedItemColor: Int = Color.YELLOW

    var unselectedItemColor: Int = Color.YELLOW

    var itemFont: Typeface? = null

    var itemTextSize: Float = 25f

    private var displayItemCount: Int = 0

    private var selectedIndex = 1

    private var initialY: Int = 0

    private lateinit var scrollerTask: Runnable
    private var newCheck = 50

    private var itemHeight = 0

    private var selectedAreaBorder: IntArray? = null

    private var scrollDirection = -1

    internal var paint: Paint? = null
    internal var viewWidth: Int = 0

    val getSelectedItem: String
        get() = items!![selectedIndex]

    val getSelectedIndex: Int
        get() {
            return selectedIndex - offset
        }

    lateinit var onWheelViewListener: OnWheelViewListener

    open class OnWheelViewListener {
        open fun onSelected(selectedIndex: Int, item: String) {}
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context)
    }

    private fun getItems(): List<String>? {
        return items
    }

    fun setItems(list: List<String>) {
        if (null == items) {
            items = ArrayList()
        }
        items!!.clear()
        items!!.addAll(list)

        for (i in 0 until offset) {
            items!!.add(0, "")
            items!!.add("")
        }
        initData()
    }


    private fun init(context: Context) {
        mContext = context
        this.isVerticalScrollBarEnabled = false

        views = LinearLayout(context)
        views.orientation = LinearLayout.VERTICAL
        this.addView(views)

        scrollerTask = Runnable {
            val newY = scrollY
            if (initialY - newY == 0) { // stopped
                val remainder = initialY % itemHeight
                val divided = initialY / itemHeight
                if (remainder == 0) {
                    selectedIndex = divided + offset
                    onSelectedCallBack()
                } else {
                    if (remainder > itemHeight / 2) {
                        post {
                            smoothScrollTo(0, initialY - remainder + itemHeight)
                            selectedIndex = divided + offset + 1
                            onSelectedCallBack()
                        }
                    } else {
                        post {
                            smoothScrollTo(0, initialY - remainder)
                            selectedIndex = divided + offset
                            onSelectedCallBack()
                        }
                    }
                }
            } else {
                initialY = scrollY
                postDelayed(scrollerTask, newCheck.toLong())
            }
        }
    }

    private fun startScrollerTask() {
        initialY = scrollY
        postDelayed(scrollerTask, newCheck.toLong())
    }

    private fun initData() {
        displayItemCount = offset * 2 + 1

        for (item in items!!) {
            views.addView(createView(item))
        }

        refreshItemView(0)
    }

    private fun createView(item: String): TextView {
        val textView = TextView(context)
        textView.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textView.isSingleLine = true

        textView.typeface = itemFont
        textView.typeface = null
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemTextSize)
        textView.text = item
        textView.gravity = Gravity.CENTER
        val padding = dip2px(15f)
        textView.setPadding(padding, padding, padding, padding)
        if (0 == itemHeight) {
            itemHeight = getViewMeasuredHeight(textView)
            views.layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                itemHeight * displayItemCount
            )
            val lp = this.layoutParams as LinearLayout.LayoutParams
            layoutParams = LinearLayout.LayoutParams(lp.width, itemHeight * displayItemCount)
        }
        return textView
    }


    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        refreshItemView(t)

        scrollDirection = if (t > oldt) {
            SCROLL_DIRECTION_DOWN
        } else {
            SCROLL_DIRECTION_UP
        }
    }

    private fun refreshItemView(y: Int) {
        var position = y / itemHeight + offset
        val remainder = y % itemHeight
        val divided = y / itemHeight

        if (remainder == 0) {
            position = divided + offset
        } else {
            if (remainder > itemHeight / 2) {
                position = divided + offset + 1
            }
        }
        val childSize = views.childCount
        for (i in 0 until childSize) {
            val itemView = views.getChildAt(i) as TextView
            itemView.setTypeface(null, Typeface.BOLD)
            if (position == i) {
                itemView.setTextColor(selectedItemColor) // Selected Item Text Color
                itemView.alpha = 1f
            } else {
                itemView.setTextColor(unselectedItemColor) // Unselected Item Text Color
                itemView.alpha = 0.6f
            }
        }
    }

    private fun obtainSelectedAreaBorder(): IntArray {
        if (null == selectedAreaBorder) {
            selectedAreaBorder = IntArray(2)
            selectedAreaBorder!![0] = itemHeight * offset
            selectedAreaBorder!![1] = itemHeight * (offset + 1)
        }
        return selectedAreaBorder as IntArray
    }

    override fun setBackground(background: Drawable?) {
        if (viewWidth == 0) {
            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels
        }

        if (null == paint) {
            paint = Paint()
            paint?.color = linesColor //Lines Color
            paint?.strokeWidth = dip2px(1f).toFloat()
        }

        val drawable = object : Drawable() {
            override fun draw(canvas: Canvas) {
                canvas.drawLine(
                    (viewWidth * 1 / 6).toFloat(),
                    obtainSelectedAreaBorder()[0].toFloat(),
                    (viewWidth * 5 / 6).toFloat(),
                    obtainSelectedAreaBorder()[0].toFloat(),
                    paint!!
                )
                canvas.drawLine(
                    (viewWidth * 1 / 6).toFloat(),
                    obtainSelectedAreaBorder()[1].toFloat(),
                    (viewWidth * 5 / 6).toFloat(),
                    obtainSelectedAreaBorder()[1].toFloat(),
                    paint!!
                )
            }

            override fun setAlpha(alpha: Int) {}

            override fun setColorFilter(cf: ColorFilter?) {}

            @SuppressWarnings("deprecation")
            override fun getOpacity(): Int {
                return PixelFormat.UNKNOWN
            }
        }
        super.setBackground(drawable)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        background = null
    }


    private fun onSelectedCallBack() {
        onWheelViewListener.onSelected(selectedIndex, items!![selectedIndex])
    }

    fun setSelection(position: Int) {
        selectedIndex = position + offset
        this.post { this@WheelView.smoothScrollTo(0, position * itemHeight) }

    }


    override fun fling(velocityY: Int) {
        super.fling(velocityY / 3)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            startScrollerTask()
        }
        return super.onTouchEvent(ev)
    }

    private fun dip2px(dpValue: Float): Int {
        val scale = context?.resources?.displayMetrics?.density
        return (dpValue * scale!! + 0.5f).toInt()
    }

    private fun getViewMeasuredHeight(view: View): Int {
        val width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val expandSpec =
            MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        view.measure(width, expandSpec)
        return view.measuredHeight
    }

    companion object {
        const val OFF_SET_DEFAULT = 1
        private const val SCROLL_DIRECTION_UP = 0
        private const val SCROLL_DIRECTION_DOWN = 1
    }

}
