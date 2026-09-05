package com.omer.expensetracker.widget

import android.content.Context
import android.content.Intent
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.omer.expensetracker.MainActivity
import com.omer.expensetracker.R
import com.omer.expensetracker.presentation.util.formatAsCurrency
import kotlinx.coroutines.flow.first

/** Net split-with-friends balance across everyone — "you'll get" vs "you owe", the same
 * numbers as the Friends tab's hero card, glanceable from the home screen.
 *
 * Sits on the shared dark gradient (`res/drawable/widget_gradient_background.xml`) so it
 * carries the same violet-to-blue identity as the in-app AuroraBackground, with translucent
 * "glass" chips and cards (see [WidgetTheme]) layered on top instead of flat widget colors. */
class BalanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = context.widgetEntryPoint()
        val friends = entryPoint.friendRepository().observeFriends().first().filter { !it.isYou }
        val balances = entryPoint.balanceRepository().observeAllOverallBalances().first()
        val balanceByFriendId = balances.associateBy { it.friendId }

        val netByFriend = friends.map { balanceByFriendId[it.id]?.netMinor ?: 0L }
        val owedToYou = netByFriend.filter { it > 0 }.sum()
        val youOwe = netByFriend.filter { it < 0 }.sum().let { -it }
        val netMinor = owedToYou - youOwe
        val hasFriends = friends.isNotEmpty()
        val settledUp = hasFriends && owedToYou == 0L && youOwe == 0L

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(R.drawable.widget_gradient_background))
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
                    .padding(18.dp)
            ) {
                BalanceHeader(onRefresh = actionRunCallback<RefreshBalanceAction>())
                Spacer(modifier = GlanceModifier.height(16.dp))

                when {
                    !hasFriends -> EmptyState(
                        icon = "+",
                        title = "No friends yet",
                        subtitle = "Add friends to start splitting"
                    )

                    settledUp -> EmptyState(
                        icon = "✓",
                        title = "All settled up",
                        subtitle = "Nothing outstanding"
                    )

                    else -> {
                        val netColor = when {
                            netMinor > 0 -> WidgetColors.Income
                            netMinor < 0 -> WidgetColors.Expense
                            else -> WidgetTheme.TextPrimary
                        }
                        val netText = if (netMinor > 0) "+${netMinor.formatAsCurrency()}" else netMinor.formatAsCurrency()
                        val netCaption = when {
                            netMinor > 0 -> "OVERALL, YOU'RE OWED"
                            netMinor < 0 -> "OVERALL, YOU OWE"
                            else -> "OVERALL, YOU'RE SQUARED UP"
                        }

                        Text(
                            text = netCaption,
                            style = TextStyle(color = WidgetTheme.TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium),
                            maxLines = 1
                        )
                        Text(
                            text = netText,
                            style = TextStyle(color = netColor, fontSize = 28.sp, fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            modifier = GlanceModifier.padding(top = 2.dp, bottom = 14.dp)
                        )

                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            BalanceStat(
                                label = "You'll get",
                                amountMinor = owedToYou,
                                symbol = "↑",
                                color = WidgetColors.Income,
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Spacer(modifier = GlanceModifier.width(10.dp))
                            BalanceStat(
                                label = "You owe",
                                amountMinor = youOwe,
                                symbol = "↓",
                                color = WidgetColors.Expense,
                                modifier = GlanceModifier.defaultWeight()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceHeader(onRefresh: Action) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = GlanceModifier.size(30.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.logo),
                contentDescription = null,
                modifier = GlanceModifier.size(30.dp).cornerRadius(5.dp)
            )
        }
        Spacer(modifier = GlanceModifier.width(10.dp))
        Text(
            "SPLIT WITH FRIENDS",
            style = TextStyle(color = WidgetTheme.TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )
        Spacer(modifier = GlanceModifier.width(10.dp))
        Box(
            modifier = GlanceModifier
                .size(26.dp)
                .background(WidgetTheme.ChipBackground)
                .cornerRadius(13.dp)
                .clickable(onRefresh),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.reload_svgrepo_com),
                contentDescription = "Refresh",
                modifier = GlanceModifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun BalanceStat(
    label: String,
    amountMinor: Long,
    symbol: String,
    color: ColorProvider,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .background(WidgetTheme.CardBackground)
            .cornerRadius(16.dp)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = GlanceModifier.size(20.dp).background(WidgetTheme.ChipBackground).cornerRadius(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(symbol, style = TextStyle(color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                label,
                style = TextStyle(color = WidgetTheme.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium),
                maxLines = 1
            )
        }
        Text(
            amountMinor.formatAsCurrency(),
            style = TextStyle(color = color, fontSize = 17.sp, fontWeight = FontWeight.Bold),
            maxLines = 1,
            modifier = GlanceModifier.padding(top = 6.dp)
        )
    }
}

/** Manually refreshes the widget's data without waiting for the system's update cycle. */
class RefreshBalanceAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        BalanceWidget().update(context, glanceId)
    }
}

class BalanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BalanceWidget()
}