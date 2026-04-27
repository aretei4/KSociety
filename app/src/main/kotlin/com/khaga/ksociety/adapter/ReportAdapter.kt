package com.khaga.ksociety.adapter

import android.content.Intent
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khaga.ksociety.R
import com.khaga.ksociety.databinding.ItemReportBinding
import com.khaga.ksociety.model.MonthlyReport
import com.khaga.ksociety.util.CurrencyUtil

class ReportAdapter(
    private val currentMonth: String
) : ListAdapter<MonthlyReport, ReportAdapter.ReportViewHolder>(DIFF) {

    private var expandedPosition = 0
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<MonthlyReport>() {
            override fun areItemsTheSame(o: MonthlyReport, n: MonthlyReport) = o.id == n.id
            override fun areContentsTheSame(o: MonthlyReport, n: MonthlyReport) = o == n
        }
    }

    fun setExpandedPosition(pos: Int) {
        // Always run notify on main thread
        mainHandler.post {
            val prev = expandedPosition
            expandedPosition = pos
            notifyItemChanged(prev)
            notifyItemChanged(pos)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ReportViewHolder(
            ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position), position, expandedPosition == position)
    }

    inner class ReportViewHolder(val b: ItemReportBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(r: MonthlyReport, pos: Int, isExpanded: Boolean) {
            val isCurrent = r.month == currentMonth

            b.tvMonth.text = r.month
            b.tvPool.text  = "Pool: ${CurrencyUtil.format(r.totalFund)}"

            b.tvCurrentBadge.visibility  = if (isCurrent) View.VISIBLE else View.GONE
            b.tvDefaultBadge.visibility  = if (r.defaults > 0) View.VISIBLE else View.GONE
            b.tvChevron.text             = if (isExpanded) "\u25B2" else "\u25BC"
            b.layoutDetails.visibility   = if (isExpanded) View.VISIBLE else View.GONE

            b.layoutHeader.setOnClickListener {
                val prev = expandedPosition
                expandedPosition = if (expandedPosition == pos) -1 else pos
                // Notify on main thread — we are already on main thread here
                // but use post to avoid calling notifyItemChanged inside onBindViewHolder
                mainHandler.post {
                    notifyItemChanged(prev)
                    if (prev != pos) notifyItemChanged(pos)
                }
            }

            b.btnExport.setOnClickListener { shareReport(r) }

            if (isExpanded) populateRows(r)
        }

        private fun populateRows(r: MonthlyReport) {
            b.layoutReportRows.removeAllViews()
            val ctx = b.root.context
            val dp  = ctx.resources.displayMetrics.density
            val dp1 = maxOf(1, dp.toInt())
            val dp8 = (8 * dp).toInt()

            val rows = listOf(
                Triple("Monthly Collection", CurrencyUtil.format(r.collected),  R.color.color_green),
                Triple("Interest Received",  CurrencyUtil.format(r.interestIn), R.color.color_blue),
                Triple("Member Fees",        CurrencyUtil.format(r.fees),       R.color.color_orange),
                Triple("Penalties",          CurrencyUtil.format(r.penalties),
                    if (r.penalties > 0) R.color.color_red else R.color.text_muted),
                Triple("Defaults", "${r.defaults} member(s)",
                    if (r.defaults > 0) R.color.color_red else R.color.color_green),
                Triple("Total Revenue", CurrencyUtil.format(r.totalRevenue),    R.color.text_primary),
            )

            rows.forEach { (label, value, colorRes) ->
                // Divider
                b.layoutReportRows.addView(View(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp1)
                    setBackgroundColor(ctx.getColor(R.color.divider_color))
                })
                // Row
                b.layoutReportRows.addView(LinearLayout(ctx).apply {
                    orientation  = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                    setPadding(0, dp8, 0, dp8)
                    addView(TextView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                        text      = label
                        textSize  = 12f
                        setTextColor(ctx.getColor(R.color.text_muted))
                    })
                    addView(TextView(ctx).apply {
                        text      = value
                        textSize  = 13f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(ctx.getColor(colorRes))
                    })
                })
            }
        }

        private fun shareReport(r: MonthlyReport) {
            val text = buildString {
                appendLine("KSociety — ${r.month} Report")
                appendLine("Monthly Collection : ${CurrencyUtil.format(r.collected)}")
                appendLine("Interest Received  : ${CurrencyUtil.format(r.interestIn)}")
                appendLine("Member Fees        : ${CurrencyUtil.format(r.fees)}")
                appendLine("Penalties          : ${CurrencyUtil.format(r.penalties)}")
                appendLine("Defaults           : ${r.defaults} member(s)")
                appendLine("Total Revenue      : ${CurrencyUtil.format(r.totalRevenue)}")
                appendLine("Fund Pool          : ${CurrencyUtil.format(r.totalFund)}")
                append("Generated by KSociety App")
            }
            b.root.context.startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }, "Share Report"
                )
            )
        }
    }
}
