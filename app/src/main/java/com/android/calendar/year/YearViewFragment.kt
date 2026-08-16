package com.android.calendar.year

import android.os.Bundle
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.android.calendar.CalendarController
import com.android.calendar.CalendarController.EventHandler
import com.android.calendar.CalendarController.EventInfo
import com.android.calendar.CalendarController.EventType
import com.android.calendar.CalendarController.ViewType
import com.android.calendar.Utils
import com.android.calendar.calendarcommon2.Time
import java.util.Calendar
import ws.xsoh.etar.R

/**
 * A static 12-month-at-a-glance year view, One UI Calendar-style. Hosted
 * inside AllInOneActivity's main pane like the other views (Month/Week/
 * Day/Agenda), not as a separate activity.
 */
class YearViewFragment() : Fragment(), EventHandler {

    private var year: Int = Calendar.getInstance().get(Calendar.YEAR)
    private lateinit var grid: GridLayout
    private lateinit var yearLabel: TextView

    constructor(timeMillis: Long) : this() {
        if (timeMillis != 0L) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timeMillis
            year = cal.get(Calendar.YEAR)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val density = resources.displayMetrics.density
        val root = NestedScrollView(context)
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val yearNavRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val vPad = (8 * density).toInt()
            setPadding(0, vPad, 0, vPad)
        }
        val prevButton = ImageButton(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_back))
            background = null
            setOnClickListener { year--; buildYear() }
        }
        yearLabel = TextView(context).apply {
            textSize = 22f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val nextButton = ImageButton(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_back))
            rotation = 180f
            background = null
            setOnClickListener { year++; buildYear() }
        }
        yearNavRow.addView(prevButton)
        yearNavRow.addView(yearLabel)
        yearNavRow.addView(nextButton)
        content.addView(yearNavRow)

        grid = GridLayout(context).apply {
            columnCount = 3
            val pad = (12 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }
        content.addView(grid)
        root.addView(content)

        // Swipe left/right to go to the next/previous year, like the real
        // app (in addition to the prev/next buttons).
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 100 && Math.abs(velocityX) > 200) {
                    if (dx < 0) year++ else year--
                    buildYear()
                    return true
                }
                return false
            }
        })
        root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }

        buildYear()
        return root
    }

    private fun buildYear() {
        val context = context ?: return
        grid.removeAllViews()
        yearLabel.text = year.toString()
        val firstDayOfWeek = Utils.getFirstDayOfWeekAsCalendar(context)
        for (month in 0 until 12) {
            val monthView = MiniMonthView(context)
            monthView.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1, 1f),
                GridLayout.spec(month % 3, 1, 1f)
            ).apply {
                width = 0
                val margin = (4 * resources.displayMetrics.density).toInt()
                setMargins(margin, margin, margin, margin)
            }
            monthView.setMonth(year, month, firstDayOfWeek) { calendar ->
                val time = Time(Utils.getTimeZone(context, null))
                time.set(calendar.timeInMillis)
                CalendarController.getInstance(context)
                    .sendEvent(this, EventType.GO_TO, time, time, -1L, ViewType.MONTH)
            }
            grid.addView(monthView)
        }
    }

    // EventHandler — Year view doesn't need controller-driven updates (it
    // manages its own displayed year from user taps), so this is a minimal
    // implementation that just satisfies the interface AllInOneActivity's
    // fragment-switching code requires of every main-pane fragment.
    override fun getSupportedEventTypes(): Long = 0L

    override fun handleEvent(event: EventInfo?) {}

    override fun eventsChanged() {}
}
