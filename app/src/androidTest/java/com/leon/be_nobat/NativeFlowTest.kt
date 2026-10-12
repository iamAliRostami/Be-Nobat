package com.leon.be_nobat

import android.view.View
import com.leon.be_nobat.data.objects
import com.leon.be_nobat.data.text
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.android.material.button.MaterialButton
import com.leon.be_nobat.data.SecureSession
import com.leon.be_nobat.ui.MainActivity
import com.leon.be_nobat.ui.Screen
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeFlowTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private fun prepare() {
        val context = instrumentation.targetContext
        SecureSession(context).clear()
        context.getSharedPreferences("settings", 0).edit()
            .putString("server_url", "http://127.0.0.1:8080").commit()
    }

    @Test fun guestAppointmentsRequireSignIn() {
        prepare()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                for (name in listOf("appointments", "person_appointments", "pending_evaluations")) {
                    activity.open(Screen(name, "untrusted-id"), false)
                    assertEquals("login", activity.screen.name)
                    assertFalse(activity.vm.signedIn)
                }
            }
        }
    }

    @Test fun signInAndOpenServerScopedManagementScreens() {
        val arguments = InstrumentationRegistry.getArguments()
        val email = arguments.getString("apiEmail")
        val password = arguments.getString("apiPassword")
        assumeTrue("Supply disposable API account arguments", email != null && password != null)
        prepare()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.open(Screen("login"), false) }
            onView(withTagValue(`is`("auth_email" as Any))).perform(replaceText(email!!), closeSoftKeyboard())
            onView(withTagValue(`is`("auth_password" as Any))).perform(replaceText(password!!), closeSoftKeyboard())
            onView(allOf(withText(R.string.login), isAssignableFrom(MaterialButton::class.java), isDisplayed()))
                .perform(scrollTo(), click())
            await(scenario) { it.vm.signedIn && it.screen.name == "home" && !it.vm.state.value.loading }
            var businessId = ""
            scenario.onActivity {
                assertNull(it.vm.state.value.error)
                businessId = it.vm.state.value.data.objects().firstOrNull()?.text("id").orEmpty()
            }
            assertTrue("Seed a disposable business fixture", businessId.isNotBlank())
            scenario.onActivity { it.open(Screen("business_reviews", businessId), false) }
            await(scenario) { !it.vm.state.value.loading }
            scenario.onActivity { assertNull(it.vm.state.value.error); it.open(Screen("manage"), false) }
            await(scenario) { !it.vm.state.value.loading }
            scenario.onActivity { assertNull(it.vm.state.value.error) }
            for (route in listOf("admin/branches", "admin/services", "admin/resources", "admin/memberships",
                "admin/availability", "admin/reviews", "admin/team", "admin/customers", "platform/businesses", "platform/catalog", "platform/categories", "platform/users")) {
                scenario.onActivity { it.open(Screen("manage_list", route), false) }
                await(scenario) { !it.vm.state.value.loading }
                scenario.onActivity { assertNull("Failed route: " + route, it.vm.state.value.error) }
                if (route in listOf("admin/team", "admin/customers")) {
                    onView(allOf(withText(R.string.delete), isAssignableFrom(MaterialButton::class.java))).check(doesNotExist())
                    onView(allOf(withText(R.string.add), isAssignableFrom(MaterialButton::class.java))).check(doesNotExist())
                }
                if (route == "admin/customers") {
                    var customerId = ""
                    scenario.onActivity { customerId = it.vm.state.value.data.objects().firstOrNull()?.text("id").orEmpty() }
                    assertTrue("Seed a customer with appointments", customerId.isNotBlank())
                    scenario.onActivity { it.open(Screen("person_appointments", customerId), false) }
                    await(scenario) { !it.vm.state.value.loading }
                    scenario.onActivity { assertNull(it.vm.state.value.error) }
                }
            }
            scenario.onActivity { it.open(Screen("pending_evaluations"), false) }
            await(scenario) { !it.vm.state.value.loading }
            scenario.onActivity { assertNull(it.vm.state.value.error) }
            scenario.onActivity { it.open(Screen("manage"), false) }
            await(scenario) { !it.vm.state.value.loading }
        }
    }

    private fun await(scenario: ActivityScenario<MainActivity>, condition: (MainActivity) -> Boolean) {
        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            var done = false
            scenario.onActivity { done = condition(it) }
            if (done) return
            Thread.sleep(100)
        }
        fail("UI operation did not finish within 30 seconds")
    }
}
