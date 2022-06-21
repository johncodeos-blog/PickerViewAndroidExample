package com.example.pickerviewexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class MainActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter("custom-event-name"))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val openPickerViewButton = findViewById<Button>(R.id.open_picker_view_button)
        openPickerViewButton.setOnClickListener {
            val intent = Intent(this, PickerViewActivity()::class.java)
            startActivity(intent)
        }

    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            // Get extra data included in the Intent
            val selectedItem = intent.getStringExtra("selectedItem")
            Log.d("receiver", "Got message: $selectedItem")
            val pickerViewResultsTextView = findViewById<TextView>(R.id.picker_view_results_textview)
            pickerViewResultsTextView.text = selectedItem
        }
    }

}