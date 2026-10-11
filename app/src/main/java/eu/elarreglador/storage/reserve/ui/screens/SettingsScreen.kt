package eu.elarreglador.storage.reserve.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import kotlin.OptIn
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import eu.elarreglador.storage.reserve.R
import eu.elarreglador.storage.reserve.data.ReservePresets
import eu.elarreglador.storage.reserve.data.ReserveState
import eu.elarreglador.storage.reserve.ui.theme.SuccessColor
import eu.elarreglador.storage.reserve.ui.theme.WarningColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    reserveState: ReserveState,
    onReserveSizeChange: (Long) -> Unit,
    onRecreateReserve: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedSize by remember { mutableStateOf(reserveState.reservedSizeBytes) }
    var customSizeText by remember { mutableStateOf("") }
    var customUnit by remember { mutableStateOf("GB") }
    var showRecreateDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val colors = MaterialTheme.colorScheme
    val successColor = SuccessColor()
    val warningColor = WarningColor()

    val presets = ReservePresets.PRESETS

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = { Text(stringResource(R.string.reserve_size)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.surfaceContainer
            )
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.reserve_size),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))

                presets.forEach { size ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatSize(size),
                            fontSize = 16.sp,
                            color = colors.onSurface
                        )
                        RadioButton(
                            selected = selectedSize == size,
                            onClick = {
                                selectedSize = size
                                customSizeText = ""
                                errorMessage = null
                            },
                            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                selectedColor = colors.primary,
                                unselectedColor = colors.outline
                            )
                        )
                    }
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.custom_size),
                        fontSize = 16.sp,
                        color = colors.onSurface
                    )
                    RadioButton(
                        selected = selectedSize !in presets,
                        onClick = {
                            if (customSizeText.isEmpty()) {
                                customSizeText = "1"
                                customUnit = "GB"
                            }
                            selectedSize = parseCustomSize(customSizeText, customUnit)
                        },
                        colors = androidx.compose.material3.RadioButtonDefaults.colors(
                            selectedColor = colors.primary,
                            unselectedColor = colors.outline
                        )
                    )
                }

                if (selectedSize !in presets) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = customSizeText,
                            onValueChange = { text ->
                                customSizeText = text
                                selectedSize = parseCustomSize(text, customUnit)
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text(stringResource(R.string.custom_size_hint)) }
                        )
                        DropdownMenu(
                            expanded = true,
                            onDismissRequest = {},
                            modifier = Modifier.width(100.dp)
                        ) {
                            listOf("MB", "GB").forEach { unit ->
                                DropdownMenuItem(
                                    onClick = { customUnit = unit },
                                    text = { Text(unit) }
                                )
                            }
                        }
                    }
                    errorMessage?.let { msg ->
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = colors.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "El cambio de tamaño se aplicará al pulsar \"Aplicar\". Si hay una reserva activa y cambias el tamaño, podrás recrearla con el nuevo tamaño.",
                    fontSize = 14.sp,
                    color = colors.onPrimaryContainer,
                    textAlign = TextAlign.Start
                )
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (selectedSize <= 0) {
                    errorMessage = "El tamaño debe ser mayor que 0"
                    return@Button
                }
                val maxSize = 100L * 1024 * 1024 * 1024 // 100 GB límite razonable
                if (selectedSize > maxSize) {
                    errorMessage = "Tamaño máximo: 100 GB"
                    return@Button
                }
                onReserveSizeChange(selectedSize)
                if (reserveState.isReserved && selectedSize != reserveState.reservedSizeBytes) {
                    showRecreateDialog = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 56.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Text(
                text = stringResource(R.string.apply_button),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (showRecreateDialog) {
            AlertDialog(
                onDismissRequest = { showRecreateDialog = false },
                title = { Text("Recrear reserva") },
                text = { Text("¿Quieres recrear la reserva con el nuevo tamaño (${formatSize(selectedSize)}) ahora?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showRecreateDialog = false
                            onRecreateReserve()
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        )
                    ) {
                        Text(stringResource(R.string.recreate_now))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showRecreateDialog = false }
                    ) {
                        Text("Luego")
                    }
                }
            )
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

private fun parseCustomSize(text: String, unit: String): Long {
    return try {
        val value = text.toDoubleOrNull() ?: 0.0
        when (unit) {
            "GB" -> (value * 1_073_741_824).toLong()
            "MB" -> (value * 1_048_576).toLong()
            else -> 0L
        }
    } catch (e: Exception) {
        0L
    }
}