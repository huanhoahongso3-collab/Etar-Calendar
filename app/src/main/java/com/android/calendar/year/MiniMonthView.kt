package com.android.calendar.year

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import ws.xsoh.etar.R
import java.util.Calendar

/**
 * A small, static, non-scrolling calendar grid for one month — the building
 * block of the year view. All 7 header cells and 42 (6x7) day cells are
 * created once and reused across setMonth() calls (only text/color/
 * visibility change), since rebuilding ~600 views from scratch on every
 * year navigation was the main cause of jank.
 */
class MiniMonthView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    companion object {
        private const val ROWS = 6
        private const val COLS = 7
    }

    private val title: TextView
    private val headerCells = arrayOfNulls<TextView>(COLS)
    private val dayCells = arrayOfNulls<TextView>(ROWS * COLS)

    private val weekendColor = ContextCompat.getColor(context, R.color.oneui_weekend_text)
    private val dayNameColor = ContextCompat.getColor(context, R.color.month_day_names_color)
    private val primaryColor = ContextCompat.getColor(context, R.color.oneui_primary)
    private val whiteColor = ContextCompat.getColor(context, android.R.color.white)
    private val primaryTextColor: Int = run {
        val value = TypedValue()
        context.theme.resolveAttribute(android.R.attr.textColorPrimary, value, true)
        if (value.resourceId != 0) ContextCompat.getColor(context, value.resourceId) else value.data
    }
    private val todayBadge = ContextCompat.getDrawable(context, R.drawable.year_view_today_badge)

    init {
        orientation = VERTICAL
        val density = resources.displayMetrics.density

        title = TextView(context).apply {
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, (4 * density).toInt(), 0, (6 * density).toInt())
            setTextColor(primaryColor)
        }
        addView(title)

        val grid = GridLayout(context).apply {
            columnCount = COLS
            rowCount = ROWS + 1
        }

        for (i in 0 until COLS) {
            val label = TextView(context).apply {
                textSize = 9f
                gravity = Gravity.CENTER
                layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(0, 1, 1f), GridLayout.spec(i, 1, 1f)
                ).apply { width = 0; height = ViewGroup.LayoutParams.WRAP_CONTENT }
            }
            headerCells[i] = label
            grid.addView(label)
        }

        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                val cell = TextView(context).apply {
                    textSize = 10f
                    gravity = Gravity.CENTER
                    setPadding(0, (2 * density).toInt(), 0, (2 * density).toInt())
                    layoutParams = GridLayout.LayoutParams(
                        GridLayout.spec(row + 1, 1, 1f), GridLayout.spec(col, 1, 1f)
                    ).apply { width = 0; height = ViewGroup.LayoutParams.WRAP_CONTENT }
                }
                dayCells[row * COLS + col] = cell
                grid.addView(cell)
            }
        }

        addView(grid)
    }

    /** Updates the grid to show [month] (0-based, like Calendar.MONTH) of [year]. */
    fun setMonth(year: Int, month: Int, firstDayOfWeek: Int, onDayClick: ((Calendar) -> Unit)?) {
        val today = Calendar.getInstance()
        val isCurrentMonth = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

        title.text = android.text.format.DateUtils.formatDateTime(
            context,
            Calendar.getInstance().apply {
                set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis,
            android.text.format.DateUtils.FORMAT_SHOW_DATE or android.text.format.DateUtils.FORMAT_NO_MONTH_DAY
                or android.text.format.DateUtils.FORMAT_NO_YEAR
        ).uppercase()

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        // Convert Calendar.DAY_OF_WEEK (1=Sunday..7=Saturday) to a 0-based
        // offset from firstDayOfWeek (also 1=Sunday..7=Saturday).
        val firstDow = cal.get(Calendar.DAY_OF_WEEK)
        val leadingBlanks = ((firstDow - firstDayOfWeek) + 7) % 7

        val weekdayLabels = arrayOf("S", "M", "T", "W", "T", "F", "S")
        for (i in 0 until COLS) {
            val dow = ((firstDayOfWeek - 1) + i) % 7 // 0=Sunday
            headerCells[i]?.apply {
                text = weekdayLabels[dow]
                setTextColor(if (dow == 0) weekendColor else dayNameColor)
            }
        }

        for (i in dayCells.indices) {
            val day = i - leadingBlanks + 1
            val cell = dayCells[i] ?: continue
            if (day < 1 || day > daysInMonth) {
                cell.text = ""
                cell.background = null
                cell.setOnClickListener(null)
                cell.isClickable = false
                continue
            }
            val isToday = isCurrentMonth && day == todayDay
            val dow = i % COLS
            cell.text = day.toString()
            cell.setTextColor(
                when {
                    isToday -> whiteColor
                    dow == 0 -> weekendColor
                    else -> primaryTextColor
                }
            )
            cell.setTypeface(cell.typeface, if (isToday) Typeface.BOLD else Typeface.NORMAL)
            cell.background = if (isToday) todayBadge else null
            cell.setOnClickListener {
                onDayClick?.invoke(Calendar.getInstance().apply {
                    set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, day)
                })
            }
            cell.isClickable = true
        }
    }
}
