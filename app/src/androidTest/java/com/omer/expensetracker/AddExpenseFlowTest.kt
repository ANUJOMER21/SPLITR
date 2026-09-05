package com.omer.expensetracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

/**
 * Core happy path: add an expense, see it appear in the entry list, see the dashboard
 * total update — with no manual refresh anywhere in between.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddExpenseFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun addingAnExpense_updatesListAndDashboardImmediately() {
        composeRule.onNodeWithText("Add Expense").performClick()

        composeRule.onNodeWithContentDescription("Amount input").performTextInput("45.50")
        composeRule.onNodeWithContentDescription("Food category").performClick()
        composeRule.onNode(hasText("Add Expense") and hasClickAction()).performClick()

        // Back on the dashboard: the new total is visible immediately, no manual refresh.
        composeRule.onAllNodesWithText("₹45.50", substring = true).onFirst().assertIsDisplayed()

        composeRule.onNodeWithContentDescription("View all entries").performClick()
        composeRule.onNodeWithText("Food").assertIsDisplayed()
    }
}
