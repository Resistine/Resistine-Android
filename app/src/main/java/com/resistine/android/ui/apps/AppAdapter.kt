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
import androidx.core.content.ContextCompat
import com.resistine.android.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AppAdapter(
    private val packageManager: PackageManager,
    private val onManagePermissionsClick: (AppEntry) -> Unit = {}
) : ListAdapter<AppEntry, AppAdapter.AppViewHolder>(DiffCallback()) {

    init {
        setHasStableIds(true)
    }

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view as MaterialCardView
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val name: TextView = view.findViewById(R.id.app_name)
        val packageName: TextView = view.findViewById(R.id.package_name)
        val version: TextView = view.findViewById(R.id.app_version)
        val risk: TextView = view.findViewById(R.id.app_risk)
        val badges: ChipGroup = view.findViewById(R.id.badge_group)
        val managePermissionsButton: MaterialButton = view.findViewById(R.id.button_manage_permissions)
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
        holder.managePermissionsButton.setOnClickListener(null)

        val scan = entry.scanResult
        if (scan == null) {
            holder.risk.text = holder.itemView.context.getString(R.string.scan_not_run)
            holder.risk.setBackgroundResource(R.drawable.bg_wifi_chip_neutral)
            holder.risk.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.rs_text_secondary))
            holder.card.strokeWidth = 0
            holder.badges.removeAllViews()
            holder.badges.visibility = View.GONE
            holder.managePermissionsButton.visibility = View.GONE
        } else {
            val verdictLabel = when (scan.verdict) {
                RiskVerdict.NO_CONCERN -> holder.itemView.context.getString(R.string.app_verdict_no_concern)
                RiskVerdict.REVIEW -> holder.itemView.context.getString(R.string.app_verdict_review)
                RiskVerdict.URGENT_REVIEW -> holder.itemView.context.getString(R.string.app_verdict_urgent_review)
                RiskVerdict.UNKNOWN -> holder.itemView.context.getString(R.string.app_verdict_unknown)
            }
            holder.risk.text = holder.itemView.context.getString(
                R.string.scan_risk_label,
                scan.score,
                verdictLabel
            )
            val (backgroundRes, textColorRes, strokeColorRes) = when (scan.verdict) {
                RiskVerdict.NO_CONCERN -> Triple(
                    R.drawable.bg_wifi_risk_safe,
                    R.color.wifi_risk_safe_text,
                    R.color.rs_status_safe
                )
                RiskVerdict.REVIEW -> Triple(
                    R.drawable.bg_wifi_risk_warning,
                    R.color.wifi_risk_warning_text,
                    R.color.rs_status_warning
                )
                RiskVerdict.URGENT_REVIEW -> Triple(
                    R.drawable.bg_wifi_risk_danger,
                    R.color.wifi_risk_danger_text,
                    R.color.rs_status_danger
                )
                RiskVerdict.UNKNOWN -> Triple(
                    R.drawable.bg_wifi_chip_neutral,
                    R.color.rs_text_secondary,
                    R.color.rs_outline
                )
            }
            holder.risk.setBackgroundResource(backgroundRes)
            holder.risk.setTextColor(ContextCompat.getColor(holder.itemView.context, textColorRes))
            holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, strokeColorRes)
            holder.card.strokeWidth = if (scan.verdict == RiskVerdict.URGENT_REVIEW) {
                dpToPx(holder.itemView.context, 2)
            } else {
                dpToPx(holder.itemView.context, 1)
            }
            holder.badges.removeAllViews()
            if (scan.badges.isEmpty()) {
                holder.badges.visibility = View.GONE
            } else {
                holder.badges.visibility = View.VISIBLE
                for (badge in scan.badges.take(MAX_VISIBLE_BADGES)) {
                    val chip = Chip(holder.itemView.context)
                    chip.text = badge.label
                    chip.isClickable = true
                    chip.isCheckable = false
                    chip.minHeight = dpToPx(holder.itemView.context, 32)
                    chip.textSize = 12f
                    val description = badge.description ?: badgeDescription(holder.itemView.context, badge.type)
                    chip.setOnLongClickListener {
                        showBadgeDialog(holder, badge.label, description)
                        true
                    }
                    chip.setOnClickListener {
                        showBadgeDialog(holder, badge.label, description)
                    }
                    holder.badges.addView(chip)
                }
                val hiddenBadgeCount = scan.badges.size - MAX_VISIBLE_BADGES
                if (hiddenBadgeCount > 0) {
                    holder.badges.addView(Chip(holder.itemView.context).apply {
                        text = holder.itemView.context.getString(R.string.app_badges_more, hiddenBadgeCount)
                        isClickable = true
                        isCheckable = false
                        minHeight = dpToPx(holder.itemView.context, 32)
                        textSize = 12f
                        contentDescription = holder.itemView.context.getString(
                            R.string.app_badges_more_description,
                            hiddenBadgeCount
                        )
                        setOnClickListener {
                            showFindingsDialog(holder, entry, openSettingsAction = false)
                        }
                    })
                }
            }

            if (scan.verdict == RiskVerdict.REVIEW || scan.verdict == RiskVerdict.URGENT_REVIEW) {
                holder.managePermissionsButton.visibility = View.VISIBLE
                holder.managePermissionsButton.text = holder.itemView.context.getString(
                    R.string.review_findings_button
                )
                holder.managePermissionsButton.setOnClickListener {
                    showFindingsDialog(holder, entry, openSettingsAction = true)
                }
            } else {
                holder.managePermissionsButton.visibility = View.GONE
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
            BadgeType.UNKNOWN_SOURCE -> R.string.badge_desc_unknown_source
            BadgeType.LOCAL_INSTALL -> R.string.badge_desc_local_install
            BadgeType.DEBUGGABLE -> R.string.badge_desc_debuggable
            BadgeType.OLD_TARGET_SDK -> R.string.badge_desc_old_target_sdk
            BadgeType.SENSITIVE_PERMISSION -> R.string.badge_desc_high_risk_permission
            BadgeType.ACCESSIBILITY_ENABLED -> R.string.badge_desc_accessibility_enabled
            BadgeType.DEVICE_ADMIN -> R.string.badge_desc_device_admin
            BadgeType.NOTIFICATION_ACCESS -> R.string.badge_desc_notification_access
            BadgeType.INSTALLER_CAPABILITY -> R.string.badge_desc_installer_capability
            BadgeType.OVERLAY_CAPABILITY -> R.string.badge_desc_overlay_capability
            BadgeType.OVERLAY_ENABLED -> R.string.badge_desc_overlay_enabled
            BadgeType.USAGE_ACCESS_ENABLED -> R.string.badge_desc_usage_access_enabled
            BadgeType.BATTERY_OPTIMIZATION_EXEMPT -> R.string.badge_desc_battery_optimization_exempt
            BadgeType.VPN_CAPABILITY -> R.string.badge_desc_vpn_capability
        }
        return context.getString(resId)
    }

    private fun showBadgeDialog(holder: AppViewHolder, title: CharSequence, description: CharSequence) {
        MaterialAlertDialogBuilder(holder.itemView.context)
            .setTitle(title)
            .setMessage(description)
            .setPositiveButton(R.string.dialog_got_it, null)
            .show()
    }

    private fun showFindingsDialog(
        holder: AppViewHolder,
        entry: AppEntry,
        openSettingsAction: Boolean
    ) {
        val context = holder.itemView.context
        val scan = entry.scanResult ?: return
        val message = scan.badges.joinToString(separator = "\n\n") { badge ->
            val description = badge.description ?: badgeDescription(context, badge.type)
            "${badge.label}\n$description"
        }
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.review_findings_title, entry.label))
            .setMessage(message)
            .setNegativeButton(R.string.review_findings_close, null)
        if (openSettingsAction) {
            builder.setPositiveButton(R.string.review_findings_open_settings) { _, _ ->
                onManagePermissionsClick(entry)
            }
        } else {
            builder.setPositiveButton(R.string.dialog_got_it, null)
        }
        builder.show()
    }

    private fun dpToPx(context: android.content.Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    private companion object {
        const val MAX_VISIBLE_BADGES = 2
    }
}
