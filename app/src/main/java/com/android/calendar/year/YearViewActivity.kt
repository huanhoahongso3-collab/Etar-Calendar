package com.android.calendar.year

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.GridLayout
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

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)

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

        grid = GridLayout(this).apply {
            columnCount = 3
            val pad = (12 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }
        content.addView(grid)
        root.addView(content)
        setContentView(root)

        buildYear()
    }

    private fun buildYear() {
        grid.removeAllViews()
        title = year.toString()
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
