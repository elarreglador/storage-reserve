package eu.elarreglador.storage.reserve.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import kotlin.OptIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import eu.elarreglador.storage.reserve.R
import eu.elarreglador.storage.reserve.data.ReserveManager
import eu.elarreglador.storage.reserve.data.ReserveState
import eu.elarreglador.storage.reserve.ui.theme.SuccessColor
import eu.elarreglador.storage.reserve.ui.theme.WarningColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    reserveManager: ReserveManager,
    reserveState: ReserveState,
    onReserveStateChange: (Boolean, Long) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var freeSpace by remember { mutableStateOf(reserveManager.getFreeSpaceBytes()) }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val colors = MaterialTheme.colorScheme
    val successColor = SuccessColor()
    val warningColor = WarningColor()

    LaunchedEffect(reserveState.isReserved, reserveState.reservedSizeBytes) {
        while (true) {
            freeSpace = reserveManager.getFreeSpaceBytes()
            delay(2000)
        }
    }

    LaunchedEffect(reserveState.isReserved) {
        if (reserveState.isReserved && !reserveManager.isReserveExists()) {
            isLoading = true
            progress = 0f
            coroutineScope.launch(Dispatchers.IO) {
                val result = reserveManager.createReserve(reserveState.reservedSizeBytes) { p ->
                    progress = p
                }
                isLoading = false
                if (result.isSuccess) {
                    onReserveStateChange(true, reserveState.reservedSizeBytes)
                } else {
                    errorMessage = result.exceptionOrNull()?.message
                    onReserveStateChange(false, reserveState.reservedSizeBytes)
                }
            }
        }
    }

    val isProtected = reserveState.isReserved && reserveManager.isReserveExists()
    val buttonText = if (isProtected) {
        stringResource(R.string.release_button, formatSize(reserveState.reservedSizeBytes))
    } else {
        stringResource(R.string.reserve_button)
    }
    val canReserve = reserveManager.getFreeSpaceBytes() >= reserveState.reservedSizeBytes

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = { Text(stringResource(R.string.app_name)) },
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.surfaceContainer
            )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isProtected) successColor.copy(alpha = 0.1f) else warningColor.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = if (isProtected) stringResource(R.string.protected_status) else stringResource(R.string.no_reserve_status),
                    modifier = Modifier.size(64.dp),
                    tint = if (isProtected) successColor else warningColor
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))

                Text(
                    text = if (isProtected) stringResource(R.string.protected_status) else stringResource(R.string.no_reserve_status),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isProtected) successColor else warningColor,
                    textAlign = TextAlign.Center
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))

Text(
                     text = stringResource(R.string.free_space_label, formatSize(freeSpace)),
                     fontSize = 18.sp,
                     color = colors.onSurfaceVariant,
                     textAlign = TextAlign.Center
                 )

                 androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))

                 Text(
                     text = stringResource(R.string.app_description),
                     fontSize = 14.sp,
                     color = colors.onSurfaceVariant,
                     textAlign = TextAlign.Center
                 )

                 if (isLoading) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                    CircularProgressIndicator(progress = progress)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = stringResource(R.string.creating_reserve),
                        fontSize = 14.sp,
                        color = colors.onSurfaceVariant
                    )
                }

                errorMessage?.let { msg ->
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                    Text(
                        text = msg,
                        fontSize = 14.sp,
                        color = colors.error,
                        textAlign = TextAlign.Center
                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(24.dp))

                Button(
                    onClick = {
                        errorMessage = null
                        isLoading = true
                        progress = 0f
                        if (isProtected) {
                            coroutineScope.launch(Dispatchers.IO) {
                                val result = reserveManager.deleteReserve()
                                isLoading = false
                                if (result.isSuccess) {
                                    onReserveStateChange(false, reserveState.reservedSizeBytes)
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message
                                }
                            }
                        } else if (canReserve) {
                            coroutineScope.launch(Dispatchers.IO) {
                                val result = reserveManager.createReserve(reserveState.reservedSizeBytes) { p ->
                                    progress = p
                                }
                                isLoading = false
                                if (result.isSuccess) {
                                    onReserveStateChange(true, reserveState.reservedSizeBytes)
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && (isProtected || canReserve),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (isProtected) colors.errorContainer else colors.primaryContainer,
                        contentColor = if (isProtected) colors.onErrorContainer else colors.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!isProtected && !canReserve && !isLoading) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = stringResource(R.string.insufficient_space, formatSize(reserveManager.getFreeSpaceBytes())),
                        fontSize = 14.sp,
                        color = colors.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824L -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576L -> String.format("%.0f MB", bytes / 1_048_576.0)
        else -> String.format("%.0f KB", bytes / 1024.0)
    }
}