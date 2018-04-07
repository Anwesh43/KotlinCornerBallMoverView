package ui.anwesome.com.kotlincornerballmoverveiw

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import ui.anwesome.com.cornerballmoverview.CornerBallMoverView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CornerBallMoverView.create(this)
    }
}
