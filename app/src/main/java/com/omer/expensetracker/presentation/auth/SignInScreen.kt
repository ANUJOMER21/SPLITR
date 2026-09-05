package com.omer.expensetracker.presentation.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.omer.expensetracker.R
import com.omer.expensetracker.presentation.components.pressScale
import com.omer.expensetracker.ui.theme.AccentBlue
import com.omer.expensetracker.ui.theme.AccentBlueDeep
import com.omer.expensetracker.ui.theme.AccentCyan
import com.omer.expensetracker.ui.theme.BorderGlass
import com.omer.expensetracker.ui.theme.SurfaceGlass
import kotlinx.coroutines.launch

private data class SignInHighlight(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String, val subtitle: String)

private val highlights = listOf(
    SignInHighlight(Icons.Filled.Groups, "Split with friends", "Shared expenses and balances stay in sync on both ends"),
    SignInHighlight(Icons.Filled.CloudSync, "Always up to date", "Add an expense on one device, see it everywhere else"),
    SignInHighlight(Icons.Filled.Shield, "Your data, your account", "Nothing is shared until a friend joins with the same email")
)

@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var credentialError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.signedIn) { if (state.signedIn) onSignedIn() }

    fun launchGoogleSignIn() {
        scope.launch {
            credentialError = null
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.signInWithGoogle(googleIdTokenCredential.idToken)
                } else {
                    Log.w("SignInScreen", "Unexpected credential type: ${credential.type}")
                }
            } catch (e: GetCredentialException) {
                Log.w("SignInScreen", "Google sign-in failed", e)
                credentialError = "No Google account available on this device — add one in Settings first"
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 28.dp)
        ) {
            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Brush.linearGradient(listOf(AccentBlueDeep, AccentBlue, AccentCyan)), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            }

            Text(
                "Split expenses.\nStay in sync.",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 38.sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                "Sign in with Google to link your account — everything else keeps working offline either way.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp, bottom = 36.dp)
            )

            highlights.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SurfaceGlass, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.padding(start = 14.dp)) {
                        Text(item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                GoogleSignInButton(onClick = ::launchGoogleSignIn)
                (state.error ?: credentialError)?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                Text(
                    "By continuing you agree this device may sync shared expense data to your Google account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp, bottom = 28.dp)
                )
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .pressScale(interactionSource)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoogleG(modifier = Modifier.size(22.dp))
            Text(
                "Continue with Google",
                color = Color(0xFF1F1F1F),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

/** A plain-shapes stand-in for Google's "G" mark — no bundled brand asset in this project, so
 * this is deliberately simple rather than an inaccurate imitation of the real logo. */
@Composable
private fun GoogleG(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Brush.sweepGradient(listOf(Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC05), Color(0xFFEA4335), Color(0xFF4285F4))), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(12.dp).background(Color.White, CircleShape))
    }
}
