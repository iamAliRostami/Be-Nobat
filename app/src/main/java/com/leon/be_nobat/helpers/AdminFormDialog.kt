package com.leon.be_nobat.helpers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.leon.be_nobat.R

/**
 * Reusable builder for "create entity" dialogs used across admin/platform
 * screens. The caller declares a list of fields (label + optional default +
 * optional input type) and gets back the entered values when the user taps
 * "submit". Empty fields are returned as empty strings.
 *
 * Example:
 * ```
 * AdminFormDialog.Builder(this)
 *   .setTitle("ایجاد شعبه")
 *   .field("نام", "شعبه مرکزی")
 *   .field("آدرس")
 *   .field("کد", "1")
 *   .show { values -> viewModel.createBranch(name = values[0], ...) }
 * ```
 */
class AdminFormDialog private constructor(
    private val context: Context,
    private val title: String,
    private val fields: List<Field>,
    private val onSubmit: (List<String>) -> Unit,
) {

    data class Field(
        val label: String,
        val default: String? = null,
        val inputType: Int? = null,
        val helperText: String? = null,
    )

    class Builder(private val context: Context) {
        private var title: String = ""
        private val fields = mutableListOf<Field>()
        private var onSubmit: ((List<String>) -> Unit)? = null

        fun setTitle(title: String) = apply { this.title = title }

        fun field(label: String, default: String? = null, inputType: Int? = null, helperText: String? = null) =
            apply { fields.add(Field(label, default, inputType, helperText)) }

        /** Convenience for a numeric field. */
        fun numberField(label: String, default: String? = null) = apply {
            fields.add(Field(label, default, android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL))
        }

        fun onSubmit(block: (List<String>) -> Unit) = apply { onSubmit = block }

        fun show() {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_admin_form, null)
            val titleView = view.findViewById<TextView>(R.id.tvFormTitle)
            titleView.text = title

            // We have 5 slots in the template (field0Layout..field4Layout).
            val layouts = listOf(
                R.id.field0Layout to R.id.field0,
                R.id.field1Layout to R.id.field1,
                R.id.field2Layout to R.id.field2,
                R.id.field3Layout to R.id.field3,
                R.id.field4Layout to R.id.field4,
            )

            fields.forEachIndexed { index, fieldDef ->
                if (index >= layouts.size) return@forEachIndexed
                val (layoutId, editId) = layouts[index]
                val layout = view.findViewById<TextInputLayout>(layoutId)
                val edit = view.findViewById<TextInputEditText>(editId)
                layout.hint = fieldDef.label
                fieldDef.helperText?.let { layout.helperText = it }
                fieldDef.default?.let { edit.setText(it) }
                fieldDef.inputType?.let { edit.inputType = it }
                layout.visibility = View.VISIBLE
            }

            // Hide unused slots
            for (i in fields.size until layouts.size) {
                view.findViewById<TextInputLayout>(layouts[i].first).visibility = View.GONE
            }

            MaterialAlertDialogBuilder(context)
                .setView(view)
                .setPositiveButton(R.string.save) { _, _ ->
                    val values = fields.indices.map { idx ->
                        val editId = layouts[idx].second
                        view.findViewById<TextInputEditText>(editId).text?.toString()?.trim() ?: ""
                    }
                    onSubmit?.invoke(values)
                }
                .setNegativeButton(R.string.action_close, null)
                .show()
        }
    }
}
