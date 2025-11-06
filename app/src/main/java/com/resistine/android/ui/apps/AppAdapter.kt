package com.resistine.android.ui.apps

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.resistine.android.R

class AppAdapter(
    private val apps: List<PackageInfo>,
    private val packageManager: PackageManager
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val name: TextView = view.findViewById(R.id.app_name)
        val packageName: TextView = view.findViewById(R.id.package_name)
        val version: TextView = view.findViewById(R.id.app_version)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val info = apps[position]
        holder.name.text = info.applicationInfo?.loadLabel(packageManager)
        holder.icon.setImageDrawable(info.applicationInfo?.loadIcon(packageManager))
        holder.packageName.text = info.packageName
        holder.version.text = holder.itemView.context.getString(R.string.version_label, info.versionName ?: "?")
    }

    override fun getItemCount(): Int = apps.size
}
