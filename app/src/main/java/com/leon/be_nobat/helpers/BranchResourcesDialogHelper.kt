package com.leon.be_nobat.helpers

import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.leon.be_nobat.R
import com.leon.be_nobat.domain.model.ManagementBranch
import com.leon.be_nobat.domain.model.ManagementResource
import com.leon.be_nobat.domain.model.ManagementResourceInput
import com.leon.be_nobat.domain.model.ManagementResourceServicesInput
import com.leon.be_nobat.domain.repository.ManagementRepository
import kotlinx.coroutines.launch

/**
 * Shared dialog helper for managing a branch's resources (staff, room, equipment)
 * and assigning branch services to each resource.
 */
object BranchResourcesDialogHelper {

    fun show(
        activity: ComponentActivity,
        branch: ManagementBranch,
        managementRepository: ManagementRepository,
    ) {
        val dp = activity.resources.displayMetrics.density
        val pad = (16 * dp).toInt()

        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, (12 * dp).toInt(), pad, pad)
        }

        val btnAdd = MaterialButton(activity, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "+ افزودن منبع جدید"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        }
        root.addView(btnAdd)

        val progress = ProgressBar(activity).apply {
            isVisible = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                topMargin = (12 * dp).toInt()
            }
        }
        root.addView(progress)

        val tvEmpty = TextView(activity).apply {
            text = "هنوز منبعی برای این شعبه ثبت نشده است."
            isVisible = false
            setPadding(0, (12 * dp).toInt(), 0, (12 * dp).toInt())
        }
        root.addView(tvEmpty)

        val scroll = android.widget.ScrollView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (360 * dp).toInt(),
            ).apply {
                topMargin = (8 * dp).toInt()
            }
        }
        val listContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(listContainer)
        root.addView(scroll)

        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle("تنظیم منابع · ${branch.name}")
            .setView(root)
            .setPositiveButton("بستن", null)
            .create()

        fun reloadResources() {
            progress.isVisible = true
            tvEmpty.isVisible = false
            listContainer.removeAllViews()
            activity.lifecycleScope.launch {
                managementRepository.listResources(
                    branchId = branch.id,
                    includeInactive = true,
                    page = 1,
                    pageSize = 100,
                ).onSuccess { page ->
                    progress.isVisible = false
                    val items = page.items
                    tvEmpty.isVisible = items.isEmpty()
                    scroll.isVisible = items.isNotEmpty()
                    items.forEach { resource ->
                        listContainer.addView(
                            buildResourceCard(
                                activity = activity,
                                branch = branch,
                                resource = resource,
                                managementRepository = managementRepository,
                                onChanged = { reloadResources() },
                            ),
                        )
                    }
                }.onFailure { err ->
                    progress.isVisible = false
                    tvEmpty.isVisible = true
                    tvEmpty.text = err.message ?: "خطا در دریافت منابع شعبه"
                }
            }
        }

        btnAdd.setOnClickListener {
            showCreateResourceDialog(activity, branch, managementRepository) {
                reloadResources()
            }
        }

        dialog.show()
        reloadResources()
    }

    private fun buildResourceCard(
        activity: ComponentActivity,
        branch: ManagementBranch,
        resource: ManagementResource,
        managementRepository: ManagementRepository,
        onChanged: () -> Unit,
    ): View {
        val dp = activity.resources.displayMetrics.density
        val card = MaterialCardView(activity).apply {
            radius = 12 * dp
            cardElevation = 0f
            strokeWidth = (1 * dp).toInt()
            strokeColor = activity.getColor(R.color.colorOutline)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                bottomMargin = (8 * dp).toInt()
            }
        }

        val inner = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            val p = (12 * dp).toInt()
            setPadding(p, p, p, p)
        }

        val kindLabel = when (resource.kind.lowercase()) {
            "staff" -> "عضو تیم"
            "room" -> "اتاق"
            "equipment" -> "تجهیزات"
            else -> resource.kind
        }
        val statusLabel = if (resource.active) "فعال" else "غیرفعال"

        val tvTitle = TextView(activity).apply {
            text = resource.name
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        val tvMeta = TextView(activity).apply {
            text = "$kindLabel · $statusLabel"
            textSize = 12f
            setTextColor(activity.getColor(R.color.colorOnSurfaceVariant))
        }
        inner.addView(tvTitle)
        inner.addView(tvMeta)

        val actions = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = (8 * dp).toInt()
            }
        }

        val btnServices = MaterialButton(activity, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "خدمات"
            textSize = 11f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = (4 * dp).toInt()
            }
            setOnClickListener {
                showAssignServicesDialog(activity, branch, resource, managementRepository)
            }
        }

        val btnToggle = MaterialButton(activity, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = if (resource.active) "غیرفعال" else "فعال"
            textSize = 11f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = (4 * dp).toInt()
            }
            setOnClickListener {
                activity.lifecycleScope.launch {
                    managementRepository.setResourceActive(resource.id, !resource.active)
                        .onSuccess {
                            Toast.makeText(activity, "وضعیت منبع به‌روزرسانی شد.", Toast.LENGTH_SHORT).show()
                            onChanged()
                        }
                        .onFailure { err ->
                            Toast.makeText(activity, err.message ?: "خطا در تغییر وضعیت", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        val btnDelete = MaterialButton(activity, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "حذف"
            textSize = 11f
            setTextColor(activity.getColor(R.color.error_red))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                MaterialAlertDialogBuilder(activity)
                    .setTitle("حذف منبع")
                    .setMessage("آیا از حذف «${resource.name}» اطمینان دارید؟")
                    .setPositiveButton("حذف") { _, _ ->
                        activity.lifecycleScope.launch {
                            managementRepository.deleteResource(resource.id)
                                .onSuccess {
                                    Toast.makeText(activity, "منبع حذف شد.", Toast.LENGTH_SHORT).show()
                                    onChanged()
                                }
                                .onFailure { err ->
                                    Toast.makeText(activity, err.message ?: "خطا در حذف منبع", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .setNegativeButton("انصراف", null)
                    .show()
            }
        }

        actions.addView(btnServices)
        actions.addView(btnToggle)
        actions.addView(btnDelete)
        inner.addView(actions)
        card.addView(inner)
        return card
    }

    private fun showCreateResourceDialog(
        activity: ComponentActivity,
        branch: ManagementBranch,
        managementRepository: ManagementRepository,
        onCreated: () -> Unit,
    ) {
        val dp = activity.resources.displayMetrics.density
        val pad = (16 * dp).toInt()
        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, (12 * dp).toInt(), pad, 0)
        }

        val tvKind = TextView(activity).apply {
            text = "نوع منبع"
            textSize = 12f
        }
        val kinds = listOf("staff" to "عضو تیم", "room" to "اتاق", "equipment" to "تجهیزات")
        val spKind = Spinner(activity).apply {
            adapter = ArrayAdapter(
                activity,
                android.R.layout.simple_spinner_item,
                kinds.map { it.second },
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }

        val memberContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = (10 * dp).toInt()
            }
        }
        val tvMember = TextView(activity).apply {
            text = "عضو تیم (اعضای شعبه)"
            textSize = 12f
        }
        val spMember = Spinner(activity)
        val tvNoMembers = TextView(activity).apply {
            text = "هنوز عضو تیمی برای این شعبه ثبت نشده است."
            textSize = 11f
            setTextColor(activity.getColor(R.color.colorOnSurfaceVariant))
            isVisible = false
        }
        memberContainer.addView(tvMember)
        memberContainer.addView(spMember)
        memberContainer.addView(tvNoMembers)

        val tilName = TextInputLayout(activity).apply {
            hint = "نام نمایشی منبع"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = (12 * dp).toInt()
            }
        }
        val etName = TextInputEditText(activity)
        tilName.addView(etName)

        container.addView(tvKind)
        container.addView(spKind)
        container.addView(memberContainer)
        container.addView(tilName)

        var membersList: List<Pair<String, String>> = emptyList() // userId -> displayName
        activity.lifecycleScope.launch {
            val memberships = managementRepository.listMemberships(branchId = branch.id, page = 1, pageSize = 100)
                .getOrNull()?.items.orEmpty()
            membersList = if (memberships.isNotEmpty()) {
                memberships.map { m ->
                    val label = m.displayName.ifBlank { m.email ?: m.userId }
                    m.userId to label
                }
            } else {
                managementRepository.listTeam(branchId = branch.id, page = 1, pageSize = 100)
                    .getOrNull()?.items.orEmpty()
                    .map { p -> p.id to p.displayName.ifBlank { p.email ?: p.phoneNumber ?: p.id } }
            }

            if (membersList.isEmpty()) {
                tvNoMembers.isVisible = true
                spMember.isVisible = false
            } else {
                tvNoMembers.isVisible = false
                spMember.isVisible = true
                val options = listOf("— انتخاب از لیست اعضای تیم —") + membersList.map { it.second }
                spMember.adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, options).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spMember.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                        if (pos > 0 && etName.text.isNullOrBlank()) {
                            etName.setText(membersList[pos - 1].second)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }
            }
        }

        spKind.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                memberContainer.isVisible = kinds.getOrNull(pos)?.first == "staff"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        val dlg = MaterialAlertDialogBuilder(activity)
            .setTitle("منبع جدید برای ${branch.name}")
            .setView(container)
            .setPositiveButton("ذخیره", null)
            .setNegativeButton("انصراف", null)
            .create()

        dlg.setOnShowListener {
            val btnSave = dlg.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
            btnSave.setOnClickListener {
                val kind = kinds.getOrNull(spKind.selectedItemPosition)?.first ?: "staff"
                val name = etName.text?.toString()?.trim().orEmpty()
                if (name.isBlank()) {
                    tilName.error = "نام منبع الزامی است."
                    return@setOnClickListener
                }
                val userId = if (kind == "staff" && spMember.isVisible && spMember.selectedItemPosition > 0) {
                    membersList.getOrNull(spMember.selectedItemPosition - 1)?.first
                } else null

                btnSave.isEnabled = false
                activity.lifecycleScope.launch {
                    managementRepository.createResource(
                        ManagementResourceInput(
                            branchId = branch.id,
                            name = name,
                            kind = kind,
                            userId = userId,
                        ),
                    ).onSuccess {
                        Toast.makeText(activity, "منبع جدید ثبت شد.", Toast.LENGTH_SHORT).show()
                        dlg.dismiss()
                        onCreated()
                    }.onFailure { err ->
                        btnSave.isEnabled = true
                        Toast.makeText(activity, err.message ?: "خطا در ثبت منبع", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        dlg.show()
    }

    private fun showAssignServicesDialog(
        activity: ComponentActivity,
        branch: ManagementBranch,
        resource: ManagementResource,
        managementRepository: ManagementRepository,
    ) {
        activity.lifecycleScope.launch {
            val servicesResult = managementRepository.listServices(
                businessId = branch.businessId,
                branchId = branch.id,
                includeInactive = false,
                page = 1,
                pageSize = 100,
            )
            val assignedResult = managementRepository.listResourceServices(resource.id)

            val services = servicesResult.getOrNull()?.items.orEmpty()
            if (services.isEmpty()) {
                Toast.makeText(activity, "خدمتی برای این شعبه ثبت نشده است.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val assignedIds = assignedResult.getOrNull()?.serviceIds?.toMutableSet() ?: mutableSetOf()
            val names = services.map { it.name }.toTypedArray()
            val checked = BooleanArray(services.size) { idx -> services[idx].id in assignedIds }

            MaterialAlertDialogBuilder(activity)
                .setTitle("خدمات قابل ارائه توسط ${resource.name}")
                .setMultiChoiceItems(names, checked) { _, which, isChecked ->
                    val id = services[which].id
                    if (isChecked) assignedIds.add(id) else assignedIds.remove(id)
                }
                .setPositiveButton("ذخیره") { _, _ ->
                    activity.lifecycleScope.launch {
                        managementRepository.setResourceServices(
                            resource.id,
                            ManagementResourceServicesInput(assignedIds.toList()),
                        ).onSuccess {
                            Toast.makeText(activity, "خدمات منبع ذخیره شد.", Toast.LENGTH_SHORT).show()
                        }.onFailure { err ->
                            Toast.makeText(activity, err.message ?: "خطا در ذخیره خدمات منبع", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("انصراف", null)
                .show()
        }
    }
}
