package id.indosw.testkt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.indosw.gdrivedebugview.GDriveDebugViewActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val openActivity = Intent(applicationContext, GDriveDebugViewActivity::class.java)
        startActivity(openActivity)
        finish()
    }
}