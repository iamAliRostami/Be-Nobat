package com.leon.be_nobat.ui.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.leon.be_nobat.R
import com.leon.be_nobat.data.local.TokenManager
import com.leon.be_nobat.databinding.ItemServiceSelectableBinding
import com.leon.be_nobat.domain.model.Service
import com.leon.be_nobat.domain.model.Slot
import com.leon.be_nobat.helpers.BaseActivity
import com.leon.be_nobat.helpers.JalaliDateHelper
import com.leon.be_nobat.helpers.PersianUtils
import com.leon.be_nobat.helpers.toPersianDigits
import com.leon.be_nobat.ui.view_models.BookingUiState
import com.leon.be_nobat.ui.view_models.BookingViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingActivity : BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_booking

    private val viewModel: BookingViewModel by viewModel()
    private val tokenManager: TokenManager by inject()

    private var servicesAdapter: ServicesAdapter? = null

    override fun setupViews() {
        setToolbarTitle(getString(R.string.book_now))
        setMenuVisibility(false)

        val bizId = intent.getStringExtra("businessId") ?: ""
        viewModel.setBusinessId(bizId)

        servicesAdapter = ServicesAdapter(
            selectedIds = { viewModel.state.value.selectedServiceIds },
            onToggle = { viewModel.toggleService(it) },
        )
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.servicesList).apply {
            layoutManager = LinearLayoutManager(this@BookingActivity)
            adapter = servicesAdapter
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPickDate).apply {
            text = JalaliDateHelper.formatIsoToJalaliDisplay(viewModel.state.value.date)
            setOnClickListener { showDatePickerOptions() }
        }

        lifecycleScope.launch {
            val phone = tokenManager.currentSession()?.user?.phoneNumber
            if (!phone.isNullOrBlank()) {
                findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPhone).setText(phone)
            }
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnConfirm).setOnClickListener {
            lifecycleScope.launch {
                if (tokenManager.currentSession() == null) {
                    MaterialAlertDialogBuilder(this@BookingActivity)
                        .setTitle(R.string.login_required_title)
                        .setMessage(R.string.login_required_desc)
                        .setPositiveButton(R.string.login) { _, _ ->
                            val intent = Intent(this@BookingActivity, LoginActivity::class.java).apply {
                                putExtra(LoginActivity.EXTRA_RETURN_TARGET, BookingActivity::class.java.name)
                                putExtra(LoginActivity.EXTRA_BUSINESS_ID, viewModel.state.value.businessId)
                            }
                            startActivity(intent)
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    return@launch
                }

                viewModel.setPhone(findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPhone).text.toString())
                viewModel.setNote(findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNote).text.toString())
                viewModel.setTerms(findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cbTerms).isChecked)
                viewModel.book()
            }
        }
    }

    private fun showDatePickerOptions() {
        val cal = Calendar.getInstance()
        val daysOptions = mutableListOf<Pair<String, String>>()
        for (i in 0..6) {
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, i) }
            val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
            val label = when (i) {
                0 -> "امروز (${JalaliDateHelper.formatIsoToJalaliDisplay(iso)})"
                1 -> "فردا (${JalaliDateHelper.formatIsoToJalaliDisplay(iso)})"
                else -> JalaliDateHelper.formatIsoToJalaliDisplay(iso)
            }
            daysOptions.add(Pair(label, iso))
        }

        val displayLabels = daysOptions.map { it.first }.toTypedArray() + "انتخاب تاریخ دیگر از تقویم…"

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.booking_select_date)
            .setItems(displayLabels) { _, which ->
                if (which < daysOptions.size) {
                    val chosenIso = daysOptions[which].second
                    viewModel.setDate(chosenIso)
                    findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPickDate).text =
                        JalaliDateHelper.formatIsoToJalaliDisplay(chosenIso)
                } else {
                    showStandardCalendar()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showStandardCalendar() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d)
            val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
            viewModel.setDate(iso)
            findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPickDate).text =
                JalaliDateHelper.formatIsoToJalaliDisplay(iso)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
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
        if (state.branches.isNotEmpty()) {
            val branchNames = state.branches.map { it.name }
            if (branchTv.adapter == null || branchTv.adapter.count != branchNames.size) {
                branchTv.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, branchNames))
            }
            branchTv.setOnItemClickListener { _, _, pos, _ ->
                val selectedName = branchTv.text.toString()
                val chosen = state.branches.firstOrNull { it.name == selectedName } ?: state.branches.getOrNull(pos)
                chosen?.let { viewModel.selectBranch(it.id) }
            }
            val currentBranch = state.branches.firstOrNull { it.id == state.branchId }
            if (currentBranch != null && branchTv.text.toString() != currentBranch.name) {
                branchTv.setText(currentBranch.name, false)
            }
        }

        // Services
        servicesAdapter?.submitList(state.services)

        // Provider dropdown
        val providerTv = findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.dropdownProvider)
        val providerOptions = listOf(getString(R.string.any_provider)) + state.providers.map { it.name }
        if (providerTv.adapter == null || providerTv.adapter.count != providerOptions.size) {
            providerTv.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, providerOptions))
        }
        providerTv.setOnItemClickListener { _, _, pos, _ ->
            val selectedText = providerTv.text.toString()
            val id = if (selectedText == getString(R.string.any_provider)) null
            else state.providers.firstOrNull { it.name == selectedText }?.id ?: state.providers.getOrNull(pos - 1)?.id
            viewModel.selectProvider(id)
        }
        val currentProvider = state.providers.firstOrNull { it.id == state.selectedProviderId }
        val expectedProviderText = currentProvider?.name ?: getString(R.string.any_provider)
        if (providerTv.text.toString() != expectedProviderText && state.providers.isNotEmpty()) {
            providerTv.setText(expectedProviderText, false)
        }

        // Date Button Text
        if (state.date.isNotBlank()) {
            findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPickDate).text =
                JalaliDateHelper.formatIsoToJalaliDisplay(state.date)
        }

        // Slots chips
        val chipGroup = findViewById<com.google.android.material.chip.ChipGroup>(R.id.slotChips)
        chipGroup.removeAllViews()
        val slots = state.slots?.slots ?: emptyList()
        if (slots.isNotEmpty()) {
            slots.forEach { slot ->
                val chip = Chip(this).apply {
                    text = JalaliDateHelper.formatIsoTime(slot.startsAt)
                    isCheckable = true
                    setOnClickListener { viewModel.selectSlot(slot) }
                    if (state.selectedSlot?.startsAt == slot.startsAt) isChecked = true
                }
                chipGroup.addView(chip)
            }
        } else if (state.slots != null && !state.loadingSlots) {
            val emptyChip = Chip(this).apply {
                text = getString(R.string.no_slots_available)
                isEnabled = false
            }
            chipGroup.addView(emptyChip)
        } else if (state.loadingSlots) {
            val loadingChip = Chip(this).apply {
                text = "در حال دریافت زمان‌های خالی…"
                isEnabled = false
            }
            chipGroup.addView(loadingChip)
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
                text = "${getString(R.string.booking_success)}\n${getString(R.string.booking_tracking_code, PersianUtils.toPersianDigits(code))}"
            }
        }
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
                    tvDurationPrice.text = "${item.durationMinutes.toPersianDigits()} دقیقه — ${PersianUtils.formatPrice(item.price, item.currency)}"
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
