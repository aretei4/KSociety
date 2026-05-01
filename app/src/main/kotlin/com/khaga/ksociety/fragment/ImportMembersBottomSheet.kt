package com.khaga.ksociety.fragment

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.khaga.ksociety.R
import com.khaga.ksociety.model.Member
import com.khaga.ksociety.util.CsvImportUtil
import com.khaga.ksociety.viewmodel.FundDetailViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

class ImportMembersBottomSheet : BottomSheetDialogFragment() {

    var fundId       = -1L
    var onImportDone : (() -> Unit)? = null

    private lateinit var viewModel: FundDetailViewModel

    // ── File picker ───────────────────────────────────────────────────────────
    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult
            runCatching {
                val stream = requireContext().contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Cannot open file")
                val text = BufferedReader(InputStreamReader(stream)).use { it.readText() }
                stream.close()
                showPreview(text)
            }.onFailure {
                Toast.makeText(requireContext(), "Failed to read file: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

    companion object {
        fun newInstance(fundId: Long) = ImportMembersBottomSheet().apply {
            this.fundId = fundId
        }
    }

    override fun onStart() {
        super.onStart()
        val d = dialog as? BottomSheetDialog ?: return
        val sheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        BottomSheetBehavior.from(sheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(requireParentFragment())[FundDetailViewModel::class.java]
        return buildMainView()
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Main screen
    // ══════════════════════════════════════════════════════════════════════════
    private fun buildMainView(): View {
        val ctx = requireContext()
        val dp  = ctx.resources.displayMetrics.density

        val root = ScrollView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((20*dp).toInt(), (20*dp).toInt(), (20*dp).toInt(), (32*dp).toInt())
        }

        // ── Header ─────────────────────────────────────────────────────────
        container.addView(LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (20*dp).toInt() }

            addView(TextView(ctx).apply {
                text = "📥  Import Members from CSV"
                textSize = 17f; setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(ctx).apply {
                text = "✕"; textSize = 20f; gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(ctx, R.color.text_muted))
                setPadding((8*dp).toInt(), 0, 0, 0)
                setOnClickListener { dismiss() }
            })
        })

        // ── Format card ────────────────────────────────────────────────────
        container.addView(buildFormatCard(ctx, dp))

        // ── Copy template button ───────────────────────────────────────────
        container.addView(buildButton(ctx, dp,
            label  = "📋  Copy Template to Clipboard",
            bgRes  = R.drawable.bg_button_secondary,
            color  = R.color.header_green,
            margin = (12*dp).toInt()
        ) {
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(
                ClipData.newPlainText("members_template", CsvImportUtil.templateText())
            )
            Toast.makeText(ctx, "Template copied — paste into a text file and save as .csv", Toast.LENGTH_LONG).show()
        })

        // ── Pick file button ───────────────────────────────────────────────
        container.addView(buildButton(ctx, dp,
            label  = "📂  Pick CSV File from Device",
            bgRes  = R.drawable.bg_button_primary,
            color  = R.color.white,
            margin = (4*dp).toInt()
        ) {
            pickFileLauncher.launch("*/*")
        })

        root.addView(container)
        return root
    }

    // ── Format reference card ──────────────────────────────────────────────
    private fun buildFormatCard(ctx: Context, dp: Float): View {
        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background  = ContextCompat.getDrawable(ctx, R.drawable.bg_info_box)
            setPadding((14*dp).toInt(), (14*dp).toInt(), (14*dp).toInt(), (14*dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (16*dp).toInt() }
        }

        card.addView(TextView(ctx).apply {
            text = "📄  CSV Column Format"
            textSize = 13f; setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(ctx, R.color.color_blue))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (10*dp).toInt() }
        })

        // Header row
        card.addView(monoText(ctx, dp,
            "name, phone, contribution, amt_borrowed, fees, join_date",
            bold = true
        ))

        card.addView(TextView(ctx).apply {
            text = "Example rows:"
            textSize = 11f
            setTextColor(ContextCompat.getColor(ctx, R.color.text_muted))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = (10*dp).toInt(); it.bottomMargin = (4*dp).toInt() }
        })
        card.addView(monoText(ctx, dp, "Ravi Kumar, 9876543210, 2000, 10000, 500, Jan 2025"))
        card.addView(monoText(ctx, dp, "Sunita Devi, , 2000, 0, 500, Jan 2025"))
        card.addView(monoText(ctx, dp, "Mohan Rao, , 2000, , ,"))

        card.addView(TextView(ctx).apply {
            text = "• Only name is required  •  Leave other columns blank to use defaults\n" +
                   "• amt_borrowed = current loan in ₹  •  join_date e.g. Jan 2025"
            textSize = 10f; lineSpacingMultiplier = 1.4f
            setTextColor(ContextCompat.getColor(ctx, R.color.text_muted))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = (10*dp).toInt() }
        })

        return card
    }

    private fun monoText(ctx: Context, dp: Float, text: String, bold: Boolean = false): TextView =
        TextView(ctx).apply {
            this.text = text
            textSize  = 11f
            typeface  = Typeface.MONOSPACE
            if (bold) setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
            setBackgroundColor(0x10000000)
            setPadding((8*dp).toInt(), (4*dp).toInt(), (8*dp).toInt(), (4*dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (4*dp).toInt() }
        }

    // ══════════════════════════════════════════════════════════════════════════
    // Preview screen — shown after file is parsed
    // ══════════════════════════════════════════════════════════════════════════
    private fun showPreview(csvText: String) {
        val result = CsvImportUtil.parse(csvText, fundId)

        if (result.members.isEmpty()) {
            val msg = buildString {
                appendLine("No valid members found in the file.")
                if (result.skipped.isNotEmpty()) {
                    appendLine()
                    appendLine("Issues:")
                    result.skipped.forEach { appendLine("• $it") }
                }
            }
            AlertDialog.Builder(requireContext())
                .setTitle("Nothing to Import")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val ctx = requireContext()
        val dp  = ctx.resources.displayMetrics.density

        val root = ScrollView(ctx)
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((20*dp).toInt(), (20*dp).toInt(), (20*dp).toInt(), (32*dp).toInt())
        }

        // Header
        container.addView(TextView(ctx).apply {
            text = "✅  Preview — ${result.members.size} member(s) ready to import"
            textSize = 15f; setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (16*dp).toInt() }
        })

        // Skipped rows warning
        if (result.skipped.isNotEmpty()) {
            container.addView(LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                background  = ContextCompat.getDrawable(ctx, R.drawable.bg_alert_red)
                setPadding((12*dp).toInt(), (10*dp).toInt(), (12*dp).toInt(), (10*dp).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = (12*dp).toInt() }
                addView(TextView(ctx).apply {
                    text = "⚠  ${result.skipped.size} row(s) skipped:"
                    textSize = 12f; setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(ctx, R.color.color_red))
                })
                result.skipped.forEach { msg ->
                    addView(TextView(ctx).apply {
                        text = "• $msg"; textSize = 11f
                        setTextColor(ContextCompat.getColor(ctx, R.color.color_red))
                    })
                }
            })
        }

        // Member rows
        result.members.forEach { m -> container.addView(buildMemberPreviewRow(ctx, dp, m)) }

        // Buttons
        container.addView(buildButton(ctx, dp,
            label  = "✅  Import ${result.members.size} Member(s)",
            bgRes  = R.drawable.bg_button_primary,
            color  = R.color.white,
            margin = (16*dp).toInt()
        ) {
            doImport(result.members)
        })
        container.addView(buildButton(ctx, dp,
            label  = "← Back",
            bgRes  = R.drawable.bg_button_secondary,
            color  = R.color.header_green,
            margin = (4*dp).toInt()
        ) {
            // Swap back to main view
            val bs = (dialog?.window?.decorView as? ViewGroup)
                ?.let { findBottomSheet(it) }
            bs?.removeAllViews()
            bs?.addView(buildMainView())
        })

        root.addView(container)

        val bs = (dialog?.window?.decorView as? ViewGroup)?.let { findBottomSheet(it) }
        bs?.removeAllViews()
        bs?.addView(root)
    }

    private fun buildMemberPreviewRow(ctx: Context, dp: Float, m: Member): View =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
            background  = ContextCompat.getDrawable(ctx, R.drawable.bg_card)
            setPadding((12*dp).toInt(), (10*dp).toInt(), (12*dp).toInt(), (10*dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (8*dp).toInt() }

            // Avatar chip
            addView(TextView(ctx).apply {
                text = m.avatar; textSize = 13f; gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(ctx, R.color.header_green))
                background = ContextCompat.getDrawable(ctx, R.drawable.bg_stat_card_green)
                layoutParams = LinearLayout.LayoutParams(
                    (40*dp).toInt(), (40*dp).toInt()
                ).also { it.marginEnd = (12*dp).toInt() }
            })

            // Name + phone
            addView(LinearLayout(ctx).apply {
                orientation  = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                addView(TextView(ctx).apply {
                    text = m.name; textSize = 14f; setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
                })
                addView(TextView(ctx).apply {
                    text = buildString {
                        if (m.phone.isNotBlank()) append("${m.phone}  ")
                        append("Joined ${m.joinDate}")
                    }
                    textSize = 11f
                    setTextColor(ContextCompat.getColor(ctx, R.color.text_muted))
                })
            })

            // Amounts
            addView(LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                gravity     = Gravity.END
                addView(TextView(ctx).apply {
                    text = if (m.contribution > 0) "₹${m.contribution}/mo" else "—"
                    textSize = 12f; setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(ctx, R.color.header_green))
                })
                if (m.amtBorrowed > 0) {
                    addView(TextView(ctx).apply {
                        text = "Loan ₹${m.amtBorrowed}"; textSize = 10f
                        setTextColor(ContextCompat.getColor(ctx, R.color.color_orange))
                    })
                }
            })
        }

    // ══════════════════════════════════════════════════════════════════════════
    // Import
    // ══════════════════════════════════════════════════════════════════════════
    private fun doImport(members: List<Member>) {
        viewModel.bulkInsertMembers(members) { count ->
            activity?.runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    "✅  $count member(s) imported successfully!",
                    Toast.LENGTH_LONG
                ).show()
                onImportDone?.invoke()
                dismiss()
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun buildButton(
        ctx: Context, dp: Float,
        label: String, bgRes: Int, color: Int,
        margin: Int,
        onClick: () -> Unit
    ): Button = Button(ctx).apply {
        text = label
        setBackgroundResource(bgRes)
        setTextColor(ContextCompat.getColor(ctx, color))
        textSize = 14f
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (52*dp).toInt()
        ).also { it.topMargin = margin }
        setOnClickListener { onClick() }
    }

    private fun findBottomSheet(vg: ViewGroup): ViewGroup? {
        for (i in 0 until vg.childCount) {
            val child = vg.getChildAt(i)
            if (child is ViewGroup) {
                if (child.id == com.google.android.material.R.id.design_bottom_sheet) return child
                findBottomSheet(child)?.let { return it }
            }
        }
        return null
    }
}
