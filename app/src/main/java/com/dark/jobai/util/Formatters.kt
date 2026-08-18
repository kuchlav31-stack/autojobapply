package com.dark.jobai.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatTimeAgo(timestamp: Long): String {
        if (timestamp == 0L) return "Just now"

        val diff = System.currentTimeMillis() - timestamp
        val seconds = diff / 1000
        val minutes = diff / (60 * 1000)
        val hours = diff / (60 * 60 * 1000)
        val days = diff / (24 * 60 * 60 * 1000)
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            weeks < 4 -> "${weeks}w ago"
            months < 12 -> "${months}mo ago"
            else -> "${years}y ago"
        }
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "N/A"
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return format.format(date)
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp == 0L) return "N/A"
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return format.format(date)
    }

    fun formatSalary(salary: String): String {
        if (salary == "Not Disclosed" || salary.isBlank()) return "Not Disclosed"
        return salary
    }
}