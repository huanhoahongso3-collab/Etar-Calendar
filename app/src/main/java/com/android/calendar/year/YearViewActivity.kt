package com.android.calendar.year

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.android.calendar.AllInOneActivity
import com.android.calendar.Utils
import com.android.calendar.theme.applyTheme
import java.util.Calendar
import ws.xsoh.etar.R

/** A static 12-month-at-a-glance year view, One UI Calendar-style. */
class YearViewActivity : AppCompatActivity() {

    private var year: Int = Calendar.getInstance().get(Calendar.YEAR)
    private lateinit var grid: GridLayout
    private lateinit var yearLabel: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        val root = NestedScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val toolbarHeight = TypedValue().let {
            theme.resolveAttribute(android.R.attr.actionBarSize, it, true)
            TypedValue.complexToDimensionPixelSize(it.data, resources.displayMetrics)
        }
        val toolbar = Toolbar(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, toolbarHeight)
            navigationIcon = ContextCompat.getDrawable(this@YearViewActivity, R.drawable.ic_arrow_back)
            setNavigationOnClickListener { finish() }
        }
        setSupportActionBar(toolbar)
        content.addView(toolbar)

        // Prev/next year navigation row — the real One UI year view lets you
        // swipe between years; buttons are the lower-risk equivalent here.
        val yearNavRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val vPad = (8 * density).toInt()
            setPadding(0, vPad, 0, vPad)
        }
        val prevButton = ImageButton(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@YearViewActivity, R.drawable.ic_arrow_back))
            background = null
            setOnClickListener { year--; buildYear() }
        }
        yearLabel = android.widget.TextView(this).apply {
            textSize = 22f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val nextButton = ImageButton(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@YearViewActivity, R.drawable.ic_arrow_back))
            rotation = 180f
            background = null
            setOnClickListener { year++; buildYear() }
        }
        yearNavRow.addView(prevButton)
        yearNavRow.addView(yearLabel)
        yearNavRow.addView(nextButton)
        content.addView(yearNavRow)

        grid = GridLayout(this).apply {
            columnCount = 3
            val pad = (12 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }
        content.addView(grid)
        root.addView(content)
        setContentView(root)

        buildYear()
    }

    private fun buildYear() {
        grid.removeAllViews()
        yearLabel.text = year.toString()
        val firstDayOfWeek = Utils.getFirstDayOfWeekAsCalendar(this)
        for (month in 0 until 12) {
            val monthView = MiniMonthView(this)
            monthView.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1, 1f),
                GridLayout.spec(month % 3, 1, 1f)
            ).apply {
                width = 0
                val margin = (4 * resources.displayMetrics.density).toInt()
                setMargins(margin, margin, margin, margin)
            }
            monthView.setMonth(year, month, firstDayOfWeek) {
                startActivity(Intent(this, AllInOneActivity::class.java))
                finish()
            }
            grid.addView(monthView)
        }
    }
}
