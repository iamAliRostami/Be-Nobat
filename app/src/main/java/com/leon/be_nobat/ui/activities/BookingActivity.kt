package com.leon.be_nobat.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.leon.be_nobat.R
import com.leon.be_nobat.databinding.ItemServiceSelectableBinding
import com.leon.be_nobat.domain.model.Service
import com.leon.be_nobat.domain.model.Slot
import com.leon.be_nobat.helpers.BaseActivity
import com.leon.be_nobat.ui.view_models.BookingUiState
import com.leon.be_nobat.ui.view_models.BookingViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingActivity : BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_booking

    private val viewModel: BookingViewModel by viewModel()

    private var servicesAdapter: ServicesAdapter? = null

    override fun setupViews() {
        setToolbarTitle(getString(R.string.book_now))
        setMenuVisibility(false)

        viewModel.setBusinessId(intent.getStringExtra("businessId") ?: "")

        servicesAdapter = ServicesAdapter(
            selectedIds = { viewModel.state.value.selectedServiceIds },
            onToggle = { viewModel.toggleService(it) },
        )
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.servicesList).apply {
            layoutManager = LinearLayoutManager(this@BookingActivity)
            adapter = servicesAdapter
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPickDate).setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d)
                val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPickDate).text = iso
                viewModel.setDate(iso)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnConfirm).setOnClickListener {
            viewModel.setPhone(findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPhone).text.toString())
            viewModel.setNote(findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNote).text.toString())
            viewModel.setTerms(findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cbTerms).isChecked)
            viewModel.book()
        }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun render(state: BookingUiState) {
        // Branch dropdown
        val branchTv = findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.dropdownBranch)
        if (branchTv.adapter == null || branchTv.adapter.count != state.branches.size) {
            branchTv.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, state.branches.map { it.name }))
        }
        branchTv.setOnItemClickListener { _, _, pos, _ ->
            viewModel.selectBranch(state.branches[pos].id)
        }
        if (state.branchId.isNotBlank() && branchTv.text.isNullOrBlank()) {
            branchTv.setText(state.branches.firstOrNull { it.id == state.branchId }?.name ?: "", false)
        }

        // Services
        servicesAdapter?.submitList(state.services)

        // Provider dropdown
        val providerTv = findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.dropdownProvider)
        val providerOptions = listOf(getString(R.string.any_provider)) + state.providers.map { it.name }
        providerTv.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, providerOptions))
        providerTv.setOnItemClickListener { _, _, pos, _ ->
            val id = if (pos == 0) null else state.providers[pos - 1].id
            viewModel.selectProvider(id)
        }

        // Slots chips
        val chipGroup = findViewById<com.google.android.material.chip.ChipGroup>(R.id.slotChips)
        chipGroup.removeAllViews()
        val slots = state.slots?.slots ?: emptyList()
        if (slots.isNotEmpty()) {
            slots.forEach { slot ->
                val chip = Chip(this).apply {
                    text = formatSlotTime(slot)
                    isCheckable = true
                    setOnClickListener { viewModel.selectSlot(slot) }
                    if (state.selectedSlot == slot) isChecked = true
                }
                chipGroup.addView(chip)
            }
        } else if (state.slots != null) {
            // Slots loaded but empty
            val emptyChip = Chip(this).apply {
                text = getString(R.string.no_slots_available)
                isEnabled = false
            }
            chipGroup.addView(emptyChip)
        }

        // Confirm button state
        val btnConfirm = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnConfirm)
        btnConfirm.text = if (state.booking) getString(R.string.booking_in_progress) else getString(R.string.confirm_booking)
        btnConfirm.isEnabled = !state.booking &&
            state.selectedSlot != null &&
            state.selectedServices.isNotEmpty() &&
            state.date.isNotBlank()

        findViewById<android.widget.ProgressBar>(R.id.loadingProgress).isVisible = state.booking

        state.bookedTrackingCode?.let { code ->
            findViewById<android.widget.TextView>(R.id.tvResult).apply {
                visibility = View.VISIBLE
                text = "${getString(R.string.booking_success)}\n${getString(R.string.booking_tracking_code, code)}"
            }
        }
    }

    private fun formatSlotTime(slot: Slot): String {
        val iso = slot.startsAt.replace("T", " ").substringBefore('.')
        iso.let { return it.replace(Regex("(\\+|\\-).*"), "").trim() }
    }

    private class ServicesAdapter(
        private val selectedIds: () -> List<String>,
        private val onToggle: (Service) -> Unit,
    ) : androidx.recyclerview.widget.ListAdapter<Service, ServicesAdapter.VH>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Service>() {
            override fun areItemsTheSame(a: Service, b: Service) = a.id == b.id
            override fun areContentsTheSame(a: Service, b: Service) = a == b
        }
    ) {

        inner class VH(val binding: ItemServiceSelectableBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
            fun bind(item: Service) {
                binding.apply {
                    tvName.text = item.name
                    tvDescription.text = item.description
                    tvDurationPrice.text = "${item.durationMinutes} دقیقه — ${item.price.toInt()} ${item.currency}"
                    cbSelected.setOnCheckedChangeListener(null)
                    cbSelected.isChecked = selectedIds().contains(item.id)
                    cbSelected.setOnCheckedChangeListener { _, _ -> onToggle(item) }
                    root.setOnClickListener {
                        cbSelected.isChecked = !cbSelected.isChecked
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val inflater = android.view.LayoutInflater.from(parent.context)
            return VH(ItemServiceSelectableBinding.inflate(inflater, parent, false))
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(getItem(position))
        }
    }
}
