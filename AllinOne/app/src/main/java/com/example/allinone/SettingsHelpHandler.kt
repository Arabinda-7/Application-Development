package com.example.allinone

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SettingsHelpHandler(private val context: Context) {
    fun showMasterGuideDetail(article: HelpArticle) {
        val dialog = Dialog(context); dialog.setContentView(R.layout.dialog_help_detail)
        dialog.window?.let { it.setBackgroundDrawableResource(android.R.color.transparent); if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) it.attributes.blurBehindRadius = 20; it.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND) }
        dialog.findViewById<TextView>(R.id.tv_help_title).text = article.title.uppercase()
        dialog.findViewById<TextView>(R.id.tv_help_content).text = article.content
        dialog.findViewById<View>(R.id.btn_close_help).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    fun showHelpDetail(section: String) {
        val features = HelpData.getGuideForSection(section)
        if (features.isEmpty()) { android.widget.Toast.makeText(context, "Guide coming soon", android.widget.Toast.LENGTH_SHORT).show(); return }
        val dialog = Dialog(context); dialog.setContentView(R.layout.dialog_help_guide)
        dialog.window?.let { it.setBackgroundDrawableResource(android.R.color.transparent); if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) it.attributes.blurBehindRadius = 20; it.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND) }
        val viewPager = dialog.findViewById<ViewPager2>(R.id.vp_help_features)
        dialog.findViewById<TextView>(R.id.tv_help_title).text = "${section.uppercase()} GUIDE"
        viewPager.adapter = HelpGuideAdapter(features)
        TabLayoutMediator(dialog.findViewById(R.id.tl_indicator), viewPager) { _, _ -> }.attach()
        dialog.findViewById<View>(R.id.btn_got_it).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btn_close_help).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    class HelpGuideAdapter(private val features: List<HelpFeature>) : RecyclerView.Adapter<HelpGuideAdapter.ViewHolder>() {
        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tv_feature_title)
            val desc: TextView = v.findViewById(R.id.tv_feature_description)
            val img: ImageView = v.findViewById(R.id.iv_feature_screenshot)
        }
        override fun onCreateViewHolder(p: ViewGroup, t: Int) = ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_help_feature, p, false))
        override fun onBindViewHolder(h: ViewHolder, pos: Int) {
            val f = features[pos]; h.title.text = f.title; h.desc.text = f.description
            if (f.imageFileName != null) {
                val file = java.io.File(h.itemView.context.filesDir, f.imageFileName)
                if (file.exists()) {
                    val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) { h.img.setPadding(0, 0, 0, 0); h.img.setImageBitmap(bitmap); h.img.scaleType = ImageView.ScaleType.CENTER_CROP }
                    else { h.img.setPadding(24, 24, 24, 24); h.img.setImageResource(f.imageRes); h.img.scaleType = ImageView.ScaleType.CENTER_INSIDE }
                } else { h.img.setPadding(24, 24, 24, 24); h.img.setImageResource(f.imageRes); h.img.scaleType = ImageView.ScaleType.CENTER_INSIDE }
            } else { h.img.setPadding(24, 24, 24, 24); h.img.setImageResource(f.imageRes); h.img.scaleType = ImageView.ScaleType.CENTER_INSIDE }
        }
        override fun getItemCount() = features.size
    }
}
