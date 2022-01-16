package com.example.printo

import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment


//https://www.freepik.com/vectors/business' Business vector created by catalyststuff - www.freepik.com
//parent="Theme.MaterialComponents.DayNight.DarkActionBar"
//parent="Theme.AppCompat.Light.NoActionBar


class MainActivity : AppCompatActivity() {
    private lateinit var serverButton: Button
    private lateinit var switch: SwitchCompat
    private val WRITE = 101
    private val READ = 101

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        serverButton = findViewById(R.id.button)
        switch = findViewById(R.id.switch_one)
        switch.setOnClickListener()
        {
            openHotspotSetting()
        }

        // checking for required permission
        serverButton.setOnClickListener()
        {
            //for android android 11 or above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    val intent = Intent(this, ServerActivity::class.java)
                    startActivity(intent)
                } else {
                    DialogAl().show(supportFragmentManager, DialogAl.TAG)
                }
            } else {
                val read = checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, READ)
                val write =
                    checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE)
                if (read && write) {
                    val intent = Intent(this, ServerActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }


    //code related to above onClick
    private fun checkPermission(permission: String, requestCode: Int): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            false
        } else {
            true
        }

    }

    //this opens up the tethering settings
    private fun openHotspotSetting() {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val cn = ComponentName(
            "com.android.settings",
            "com.android.settings.TetherSettings"
        )
        intent.component = cn
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    //code to be execute when permission denied
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this@MainActivity, "permission not granted", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (requestCode == WRITE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this@MainActivity, "permission not granted", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    //register broadcast receiver when activity started
    override fun onStart() {
        super.onStart()
        val iFilter = IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED")
        registerReceiver(mRec, iFilter)
    }

    //unregister broadcast receiver when activity no longer running
    override fun onStop() {
        super.onStop()
        unregisterReceiver(mRec)
    }

    //routine code for coordinating with btn and wifi event
    private val mRec: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val ac = intent?.action as String
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED" == ac) {
                val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
                if (state == 13) {
                    switch.isChecked = true
                    serverButton.isEnabled = true
                } else {
                    switch.isChecked = false
                    serverButton.isEnabled = false
                }
            }
        }
    }

}

//dialog for android 11 notice
class DialogAl : DialogFragment() {
    private lateinit var btn: Button

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.dialog, container, false)
        btn = root.findViewById(R.id.button2)
        btn.setOnClickListener()
        {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data =
                    Uri.parse(String.format("package:%s") + MainActivity().applicationContext.packageName.toString())
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(intent)
            }
        }
        return root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    companion object {
        const val TAG = "DialogAl"
    }
}



