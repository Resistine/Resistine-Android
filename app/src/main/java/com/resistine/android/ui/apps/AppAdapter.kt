package com.resistine.android.ui.apps

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import androidx.appcompat.widget.TooltipCompat
import com.resistine.android.R

class AppAdapter(
    private val packageManager: PackageManager
) : ListAdapter<AppEntry, AppAdapter.AppViewHolder>(DiffCallback()) {

    init {
        setHasStableIds(true)
    }

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val name: TextView = view.findViewById(R.id.app_name)
        val packageName: TextView = view.findViewById(R.id.package_name)
        val version: TextView = view.findViewById(R.id.app_version)
        val risk: TextView = view.findViewById(R.id.app_risk)
        val badges: ChipGroup = view.findViewById(R.id.badge_group)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val entry = getItem(position)
        val info = entry.packageInfo
        holder.name.text = entry.label
        holder.icon.setImageDrawable(info.applicationInfo?.loadIcon(packageManager))
        holder.packageName.text = info.packageName
        holder.version.text = holder.itemView.context.getString(R.string.version_label, info.versionName ?: "?")

        val scan = entry.scanResult
        if (scan == null) {
            holder.risk.text = holder.itemView.context.getString(R.string.scan_not_run)
            holder.badges.removeAllViews()
            holder.badges.visibility = View.GONE
        } else {
            val verdictLabel = scan.verdict.name.lowercase().replaceFirstChar { it.uppercase() }
            holder.risk.text = holder.itemView.context.getString(
                R.string.scan_risk_label,
                scan.score,
                verdictLabel
            )
            holder.badges.removeAllViews()
            if (scan.badges.isEmpty()) {
                holder.badges.visibility = View.GONE
            } else {
                holder.badges.visibility = View.VISIBLE
                for (badge in scan.badges) {
                    val chip = Chip(holder.itemView.context)
                    chip.text = badge.label
                    chip.isClickable = false
                    chip.isCheckable = false
                    TooltipCompat.setTooltipText(chip, badgeDescription(holder.itemView.context, badge.type))
                    holder.badges.addView(chip)
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).packageInfo.packageName.hashCode().toLong()
    }

    private class DiffCallback : DiffUtil.ItemCallback<AppEntry>() {
        override fun areItemsTheSame(oldItem: AppEntry, newItem: AppEntry): Boolean {
            return oldItem.packageInfo.packageName == newItem.packageInfo.packageName
        }

        override fun areContentsTheSame(oldItem: AppEntry, newItem: AppEntry): Boolean {
            return oldItem == newItem
        }
    }

    private fun badgeDescription(context: android.content.Context, type: BadgeType): CharSequence {
        val resId = when (type) {
            BadgeType.SIGNATURE_MISMATCH -> R.string.badge_desc_signature_mismatch
            BadgeType.LOOKALIKE_NAME -> R.string.badge_desc_lookalike_name
            BadgeType.OUTDATED -> R.string.badge_desc_outdated
            BadgeType.SIDELOADED -> R.string.badge_desc_sideloaded
            BadgeType.DEBUGGABLE -> R.string.badge_desc_debuggable
            BadgeType.OLD_TARGET_SDK -> R.string.badge_desc_old_target_sdk
            BadgeType.HIGH_RISK_PERMISSION -> R.string.badge_desc_high_risk_permission
        }
        return context.getString(resId)
    }
}
