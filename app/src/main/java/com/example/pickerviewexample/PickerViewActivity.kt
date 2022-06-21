package com.example.pickerviewexample

import android.animation.*
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class PickerViewActivity : AppCompatActivity() {

    private lateinit var pickerViewTransparentBg: RelativeLayout
    private lateinit var pickerViewBg: LinearLayout
    private lateinit var barView: RelativeLayout
    private lateinit var cancelButton: Button
    private lateinit var doneButton: Button

    private var selectedItem = ""
    private var selectedItemPosition = 0

    private var statusBarDark = false

    private var yearList = mutableListOf(
        "2022",
        "2021",
        "2020",
        "2019",
        "2018",
        "2017",
        "2016",
        "2015",
        "2014",
        "2013",
        "2012",
        "2011",
        "2010"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Remove Open/Close Activity Animations
        overridePendingTransition(0, 0)
        setContentView(R.layout.activity_picker_view)

        pickerViewTransparentBg = findViewById(R.id.picker_view_transparent_bg)
        pickerViewBg = findViewById(R.id.picker_view_bg)
        barView = findViewById(R.id.bar_view)
        cancelButton = findViewById(R.id.cancel_button)
        doneButton = findViewById(R.id.done_button)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // Show dark status bar or not
        if (statusBarDark) {
            this.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        this.window.statusBarColor = Color.TRANSPARENT
        setWindowFlag(this, false)


        // Fade Animation for the Semi-Transparent Black Background
        // Get the size of the size
        val mDisplay = windowManager.defaultDisplay
        val mDisplaySize = Point()
        mDisplay.getSize(mDisplaySize)
        val maxX = mDisplaySize.x
        val maxY = mDisplaySize.y

        // Set the background same as Display height
        pickerViewBg.y = maxY.toFloat()
        val alpha = 85 // Set Between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.BLACK, alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), Color.TRANSPARENT, alphaColor)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator -> pickerViewTransparentBg.setBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.start()


        // PickerViewActivity Customization
        pickerViewBg.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        barView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        // Empty click listener helps to stop the view from closing when you tap 'outside'
        barView.setOnClickListener { }

        // WheelView Customization
        val wheelView = findViewById<WheelView>(R.id.wheel_view)
        wheelView.selectedItemColor = Color.WHITE
        wheelView.unselectedItemColor = Color.WHITE
        wheelView.linesColor = Color.WHITE
        wheelView.itemTextSize = 25f
        //val font = ResourcesCompat.getFont(this, R.font.roboto)
        //wheelView.itemFont = font

        // Amount of items above and below the selected item
        wheelView.offset = 1


        val defaultValue = "2015"

        // Check if default value exists, if not then show the first item of on the wheelView
        if (yearList.contains(defaultValue)) {
            val valueIndex = yearList.indexOf(defaultValue)
            wheelView.setSelection(valueIndex)
        } else {
            wheelView.setSelection(0)
        }

        // Add the list of items to the wheelView
        wheelView.setItems(yearList)

        // Set the value you see once you open the PickerView as selected item, this will change later with the onWheelViewListener
        selectedItem = wheelView.getSelectedItem

        // Set the position of the value you see once you open the PickerView as selected item position, this will change later with the onWheelViewListener
        selectedItemPosition = wheelView.getSelectedIndex

        // Returns the current selected item and position of the item after you scroll up/down
        wheelView.onWheelViewListener = object : WheelView.OnWheelViewListener() {
            override fun onSelected(selectedIndex: Int, item: String) {
                selectedItem = item
                selectedItemPosition = selectedIndex
            }
        }


        // PickerView Bar Customization
        cancelButton.setTextColor(Color.WHITE)

        // Go Back by pressing 'Cancel'
        cancelButton.setOnClickListener {
            onBackPressed()
        }

        doneButton.setTextColor(Color.WHITE)


        // Returns the final selected item and position after you pressed 'Done'
        doneButton.setOnClickListener {

            Log.d("Selected Item", selectedItem)

            Log.d("Selected Item Position", selectedItemPosition.toString())

            // Return results to the MainActivity
            returnResultsBack()

            // Close PickerView
            onBackPressed()
        }

        // Cancel PickerView when you press the background
        pickerViewTransparentBg.setOnClickListener {
            onBackPressed()
        }
    }

    private fun returnResultsBack() {
        val intent = Intent("custom-event-name")
        intent.putExtra("selectedItem", selectedItem)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun setWindowFlag(activity: Activity, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        } else {
            winParams.flags =
                winParams.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
        }
        win.attributes = winParams
    }

    override fun onBackPressed() {
        // Fade-Out Animation the Semi-Transparent Black Background
        // Close PickerView with slide down animation
        val alpha = 85 //between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.BLACK, alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), alphaColor, Color.TRANSPARENT)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator -> pickerViewTransparentBg.setBackgroundColor(animator.animatedValue as Int) }
        slideDown(pickerViewBg)
        colorAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                finish()
                overridePendingTransition(0, 0)
            }
        })
        colorAnimation.start()
    }


    // Slide the view from its current position to below itself
    private fun slideUp(view: View) {
        ObjectAnimator.ofFloat(view, "translationY", 0f).apply {
            duration = 600
            start()
        }
    }

    // Slide the view from below itself to the current position
    private fun slideDown(view: View) {
        val mDisplay = windowManager.defaultDisplay
        val mDisplaySize = Point()
        mDisplay.getSize(mDisplaySize)
        val maxY = mDisplaySize.y
        val animation = ObjectAnimator.ofFloat(view, "translationY", maxY.toFloat())
        animation.duration = 600
        animation.start()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        slideUp(pickerViewBg)
    }

}
