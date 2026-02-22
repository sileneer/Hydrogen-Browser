package com.sileneer.hydrogenbrowser.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sileneer.hydrogenbrowser.R
import com.sileneer.hydrogenbrowser.common.base.BaseActivity
import com.sileneer.hydrogenbrowser.databinding.ActivityLanguageBinding
import com.sileneer.hydrogenbrowser.databinding.LanguageItemBinding

class LanguageActivity : BaseActivity() {

    private lateinit var binding: ActivityLanguageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.title.setTitleText(getString(R.string.language))

        val options = listOf(
            LanguageOption(getString(R.string.language_follow_system), ""),
            LanguageOption(getString(R.string.language_english), "en"),
            LanguageOption(getString(R.string.language_chinese), "zh-Hans")
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = LanguageAdapter(options)
    }

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, LanguageActivity::class.java)
            context.startActivity(intent)
        }
    }
}

private data class LanguageOption(val label: String, val tag: String)

private class LanguageAdapter(
    private val options: List<LanguageOption>
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    class ViewHolder(val binding: LanguageItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LanguageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.binding.languageText.text = option.label

        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val currentTag = if (currentLocales.isEmpty) "" else currentLocales.toLanguageTags()
        holder.binding.languageRadio.isChecked = option.tag == currentTag

        holder.itemView.setOnClickListener {
            val locales = if (option.tag.isEmpty()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(option.tag)
            }
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }

    override fun getItemCount(): Int = options.size
}
