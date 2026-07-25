package org.mtier.timetracker.ui.login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.mtier.timetracker.R
import org.mtier.timetracker.ui.theme.NcBlue
import org.mtier.timetracker.ui.theme.NcBlueButtonDark

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.WaitingForBrowserLogin) {
            CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(uiState.browserLoginUrl))
        }
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                // Matches the Nextcloud Files app's own login screen: a
                // fixed brand-blue background regardless of system
                // dark/light mode, same as the Files app's own login screen
                // (a pre-auth branding screen, not themed content — no
                // per-server theming color is available yet anyway, since
                // that's only fetched after a session exists, see
                // ThemeRepository).
                .background(NcBlue)
                .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )

        Column(modifier = Modifier.padding(top = 32.dp)) {
            when (val state = uiState) {
                is LoginUiState.EnteringServerUrl -> ServerUrlForm(viewModel)
                is LoginUiState.Connecting -> LoadingIndicator(stringResource(R.string.login_continue))
                is LoginUiState.WaitingForBrowserLogin ->
                    LoadingIndicator(stringResource(R.string.login_waiting_for_browser))
                is LoginUiState.Success ->
                    LoadingIndicator(stringResource(R.string.login_waiting_for_browser))
                is LoginUiState.Error -> ErrorState(state.message, onRetry = viewModel::retry)
            }
        }
    }
}

@Composable
private fun ServerUrlForm(viewModel: LoginViewModel) {
    val context = LocalContext.current
    // Only ever true on-device with the Nextcloud Files app installed —
    // otherwise this stays hidden and the standalone Login Flow v2 form
    // below is the only path, exactly as it was before Files-app SSO
    // (PLAN.md §3/§9: a v1.x convenience, not a replacement). Computed
    // asynchronously by the ViewModel (blocking PackageManager IPC doesn't
    // belong on the composition/main thread), so it's null until known.

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (viewModel.filesAppAvailable == true) {
            Button(
                onClick = { context.findActivity()?.let(viewModel::startSsoLogin) },
                colors = ButtonDefaults.buttonColors(containerColor = NcBlueButtonDark, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.login_sso_button))
            }
            Text(
                text = stringResource(R.string.login_sso_or_divider),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        }
        OutlinedTextField(
            value = viewModel.serverUrlInput,
            onValueChange = viewModel::onServerUrlChanged,
            label = { Text(stringResource(R.string.login_server_url_label)) },
            placeholder = { Text(stringResource(R.string.login_server_url_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = viewModel::startLogin,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = NcBlue),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
        ) {
            Text(stringResource(R.string.login_continue))
        }
    }
}

/** LocalContext.current isn't guaranteed to be an Activity directly — a
 *  wrapping composable (theming, a dialog host, a preview harness) can
 *  interpose a ContextWrapper. Currently a plain `as? Activity` cast always
 *  succeeds here since nothing wraps it, but that's fragile: this unwraps
 *  any ContextWrapper chain instead of silently no-op-ing the SSO button
 *  the moment one gets introduced. */
private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

@Composable
private fun LoadingIndicator(label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = Color.White)
        Text(
            text = label,
            color = Color.White,
            modifier = Modifier.padding(top = 16.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = message, color = Color.White, textAlign = TextAlign.Center)
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = NcBlue),
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(stringResource(R.string.common_retry))
        }
    }
}
