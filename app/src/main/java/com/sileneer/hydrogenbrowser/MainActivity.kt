package com.sileneer.hydrogenbrowser

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.sileneer.hydrogenbrowser.ui.navigation.HydrogenNavGraph
import com.sileneer.hydrogenbrowser.ui.theme.HydrogenBrowserTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            HydrogenBrowserTheme {
                HydrogenNavGraph()
            }
        }
    }
}
