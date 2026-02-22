package com.sileneer.hydrogenbrowser.common.utils

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.sileneer.hydrogenbrowser.R

class TitleLayout(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val titleText: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.title, this)
        findViewById<ImageView>(R.id.title_back).setOnClickListener {
            (getContext() as Activity).finish()
        }
        titleText = findViewById(R.id.title_text)
    }

    fun setTitleText(str: String) {
        titleText.text = str
    }
}
