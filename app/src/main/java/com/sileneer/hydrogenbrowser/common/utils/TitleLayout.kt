package com.sileneer.hydrogenbrowser.common.utils

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.sileneer.hydrogenbrowser.databinding.TitleBinding

class TitleLayout(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val binding: TitleBinding

    init {
        binding = TitleBinding.inflate(LayoutInflater.from(context), this, true)
        binding.titleBack.setOnClickListener {
            (getContext() as? Activity)?.finish()
        }
    }

    fun setTitleText(str: String) {
        binding.titleText.text = str
    }
}
