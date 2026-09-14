package com.example.mywork.ui.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}

fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "No Date"
    val sdf = SimpleDateFormat("MMM dd, yyyy, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDateShort(timestamp: Long): String {
    if (timestamp == 0L) return "N/A"
    val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
