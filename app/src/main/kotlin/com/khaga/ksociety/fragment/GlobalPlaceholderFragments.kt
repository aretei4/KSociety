package com.khaga.ksociety.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.khaga.ksociety.R

/**
 * Shown when the user taps Members / Payments / Reports in the BottomNav
 * without first selecting a fund. Prompts them to go to Home and pick a fund.
 */
class MembersGlobalFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = buildPromptView(
        emoji    = "\uD83D\uDC64",
        title    = "Select a Fund First",
        subtitle = "Go to Home, tap on a fund, then open the Members tab."
    )
}

class PaymentsGlobalFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = buildPromptView(
        emoji    = "\uD83D\uDCB3",
        title    = "Select a Fund First",
        subtitle = "Go to Home, tap on a fund, then open the Payments tab."
    )
}

class ReportsGlobalFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = buildPromptView(
        emoji    = "\uD83D\uDCCA",
        title    = "Select a Fund First",
        subtitle = "Go to Home, tap on a fund, then open the Reports tab."
    )
}

// ── Shared helper ─────────────────────────────────────────────────────────

private fun Fragment.buildPromptView(
    emoji: String,
    title: String,
    subtitle: String
): View {
    val ctx     = requireContext()
    val density = ctx.resources.displayMetrics.density
    val pad48   = (48 * density).toInt()
    val pad24   = (24 * density).toInt()
    val pad8    = (8  * density).toInt()

    return LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        gravity     = Gravity.CENTER
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(ctx.getColor(R.color.bg_main))
        setPadding(pad48, pad48, pad48, pad48)

        // Emoji
        addView(TextView(ctx).apply {
            text     = emoji
            textSize = 56f
            gravity  = Gravity.CENTER
        })

        // Title
        addView(TextView(ctx).apply {
            text     = title
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ctx.getColor(R.color.text_primary))
            gravity  = Gravity.CENTER
            setPadding(0, pad24, 0, pad8)
        })

        // Subtitle  ← use setLineSpacing() NOT lineSpacingMultiplier (val, read-only)
        addView(TextView(ctx).apply {
            text     = subtitle
            textSize = 13f
            setTextColor(ctx.getColor(R.color.text_muted))
            gravity  = Gravity.CENTER
            setLineSpacing(0f, 1.4f)   // (add, multiplier) — correct API
        })
    }
}
