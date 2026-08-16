package com.android.calendar.year

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import ws.xsoh.etar.R
import java.util.Calendar

/**
 * A small, static, non-scrolling calendar grid for one month — the building
 * block of the year view. Drawn with plain widgets (not canvas) to keep
 * things simple and safe.
 */
class MiniMonthView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    init {
        orientation = VERTICAL
    }

    /** Rebuilds the grid to show [month] (0-based, like Calendar.MONTH) of [year]. */
    fun setMonth(year: Int, month: Int, firstDayOfWeek: Int, onDayClick: ((Calendar) -> Unit)?) {
        removeAllViews()

        val today = Calendar.getInstance()
        val isCurrentMonth = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

        val density = resources.displayMetrics.density

        val title = TextView(context).apply {
            text = android.text.format.DateUtils.formatDateTime(
                context,
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, 1)
                }.timeInMillis,
                android.text.format.DateUtils.FORMAT_SHOW_DATE or android.text.format.DateUtils.FORMAT_NO_MONTH_DAY
                    or android.text.format.DateUtils.FORMAT_NO_YEAR
            ).uppercase()
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, (4 * density).toInt(), 0, (6 * density).toInt())
            setTextColor(ContextCompat.getColor(context, R.color.oneui_primary))
        }
        addView(title)

        val grid = GridLayout(context).apply {
            columnCount = 7
            rowCount = 7
        }

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        // Convert Calendar.DAY_OF_WEEK (1=Sunday..7=Saturday) to a 0-based
        // offset from firstDayOfWeek (also 1=Sunday..7=Saturday).
        val firstDow = cal.get(Calendar.DAY_OF_WEEK)
        val leadingBlanks = ((firstDow - firstDayOfWeek) + 7) % 7

        val weekdayLabels = arrayOf("S", "M", "T", "W", "T", "F", "S")
        for (i in 0 until 7) {
            val dow = ((firstDayOfWeek - 1) + i) % 7 // 0=Sunday
            val label = TextView(context).apply {
                text = weekdayLabels[dow]
                textSize = 9f
                gravity = Gravity.CENTER
                setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (dow == 0) R.color.oneui_weekend_text else R.color.month_day_names_color
                    )
                )
                layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(0, 1, 1f), GridLayout.spec(i, 1, 1f)
                ).apply { width = 0; height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT }
            }
            grid.addView(label)
        }

        var row = 1
        var col = leadingBlanks
        for (day in 1..daysInMonth) {
            val isToday = isCurrentMonth && day == todayDay
            val dow = (leadingBlanks + day - 1) % 7
            val cell = TextView(context).apply {
                text = day.toString()
                textSize = 10f
                gravity = Gravity.CENTER
                setPadding(0, (2 * density).toInt(), 0, (2 * density).toInt())
                val baseColor = when {
                    isToday -> ContextCompat.getColor(context, android.R.color.white)
                    dow == 0 -> ContextCompat.getColor(context, R.color.oneui_weekend_text)
                    else -> resolvePrimaryTextColor(context)
                }
                setTextColor(baseColor)
                if (isToday) {
                    setTypeface(typeface, Typeface.BOLD)
                    background = ContextCompat.getDrawable(context, R.drawable.year_view_today_badge)
                }
                layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(row, 1, 1f), GridLayout.spec(col, 1, 1f)
                ).apply { width = 0; height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT }
                setOnClickListener {
                    onDayClick?.invoke(Calendar.getInstance().apply {
                        set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, day)
                    })
                }
            }
            grid.addView(cell)
            col++
            if (col == 7) {
                col = 0
                row++
            }
        }

        addView(grid)
    }

    private fun resolvePrimaryTextColor(context: Context): Int {
        val value = android.util.TypedValue()
        context.theme.resolveAttribute(android.R.attr.textColorPrimary, value, true)
        return if (value.resourceId != 0) {
            ContextCompat.getColor(context, value.resourceId)
        } else {
            value.data
        }
    }
}
