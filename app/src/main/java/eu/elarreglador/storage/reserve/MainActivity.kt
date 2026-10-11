package eu.elarreglador.storage.reserve

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.elarreglador.storage.reserve.data.ReserveManager
import eu.elarreglador.storage.reserve.data.ReserveRepository
import eu.elarreglador.storage.reserve.data.ReserveState
import eu.elarreglador.storage.reserve.ui.screens.MainScreen
import eu.elarreglador.storage.reserve.ui.screens.SettingsScreen
import eu.elarreglador.storage.reserve.ui.theme.ReserveTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val reserveManager: ReserveManager by lazy { ReserveManager(this) }
    private val repository: ReserveRepository by lazy {
        (application as ReserveApplication).getReserveRepository()
    }

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(repository, reserveManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReserveTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val reserveState by viewModel.reserveState.collectAsStateWithLifecycle()

                    NavHost(navController, startDestination = "main") {
                        composable("main") {
                            MainDestination(
                                reserveManager = reserveManager,
                                reserveState = reserveState,
                                viewModel = viewModel,
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        composable("settings") {
                            SettingsDestination(
                                reserveState = reserveState,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDestination(
    reserveManager: ReserveManager,
    reserveState: ReserveState,
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit
) {
    MainScreen(
        reserveManager = reserveManager,
        reserveState = reserveState,
        onReserveStateChange = { isReserved, size ->
            viewModel.updateReserveState(isReserved, size)
        },
        onNavigateToSettings = onNavigateToSettings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDestination(
    reserveState: ReserveState,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    SettingsScreen(
        reserveState = reserveState,
        onReserveSizeChange = { size ->
            viewModel.updateReserveSize(size)
        },
        onRecreateReserve = {
            viewModel.recreateReserve()
        },
        onNavigateBack = onNavigateBack
    )
}

class MainViewModel(
    private val repository: ReserveRepository,
    private val reserveManager: ReserveManager
) : ViewModel() {

    private val _reserveState = MutableStateFlow(ReserveState())
    val reserveState = _reserveState.asStateFlow()

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            repository.reserveState
                .first()
                .also { state ->
                    _reserveState.value = state
                }
        }
    }

    fun updateReserveState(isReserved: Boolean, size: Long) {
        viewModelScope.launch {
            repository.updateState(isReserved, size).also { success ->
                if (success) {
                    _reserveState.value = ReserveState(isReserved, size)
                }
            }
        }
    }

    fun updateReserveSize(size: Long) {
        viewModelScope.launch {
            repository.setReservedSize(size).also { success ->
                if (success) {
                    _reserveState.value = _reserveState.value.copy(reservedSizeBytes = size)
                }
            }
        }
    }

    fun recreateReserve() {
        viewModelScope.launch {
            val currentState = _reserveState.value
            _reserveState.value = currentState.copy(isReserved = true)
        }
    }

    class Factory(
        private val repository: ReserveRepository,
        private val reserveManager: ReserveManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository, reserveManager) as T
        }
    }
}