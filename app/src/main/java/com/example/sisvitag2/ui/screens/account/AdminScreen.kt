package com.example.sisvitag2.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sisvitag2.data.model.UserProfileData
import com.example.sisvitag2.ui.vm.AdminViewModel
import com.example.sisvitag2.ui.vm.AdminUiState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    adminViewModel: AdminViewModel = koinViewModel()
) {
    val uiState by adminViewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserProfileData?>(null) }
    var newRole by remember { mutableStateOf(1) }
    var newStatus by remember { mutableStateOf("pendiente") }
    var userToDelete by remember { mutableStateOf<UserProfileData?>(null) }

    LaunchedEffect(Unit) {
        adminViewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Administración de Usuarios") })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState) {
                is AdminUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AdminUiState.Error -> {
                    Text((uiState as AdminUiState.Error).message, color = MaterialTheme.colorScheme.error)
                }
                is AdminUiState.UsersLoaded -> {
                    val users = (uiState as AdminUiState.UsersLoaded).users
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(users) { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = user.displayName ?: "(Sin nombre)", style = MaterialTheme.typography.titleMedium)
                                        Text(text = "Email: ${user.email ?: "-"}", style = MaterialTheme.typography.bodySmall)
                                        Text(text = "Rol: ${rolToString(user.legacyTipoUsuarioId)} | Estado: ${user.estado ?: "-"}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(onClick = {
                                        userToEdit = user
                                        newRole = user.legacyTipoUsuarioId ?: 1
                                        newStatus = user.estado ?: "pendiente"
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar rol/estado")
                                    }
                                    IconButton(onClick = {
                                        userToDelete = user
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar usuario")
                                    }
                                }
                            }
                        }
                    }
                }
                is AdminUiState.Success -> {
                    Text((uiState as AdminUiState.Success).message, color = MaterialTheme.colorScheme.primary)
                }
                else -> {}
            }

            // Diálogo para editar rol y estado
            if (showDialog && userToEdit != null) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Editar rol y estado") },
                    text = {
                        Column {
                            Text("Usuario: ${userToEdit?.displayName}")
                            Spacer(modifier = Modifier.height(8.dp))
                            // Selector de rol
                            Text("Rol:")
                            Row {
                                RadioButton(
                                    selected = newRole == 1,
                                    onClick = { newRole = 1 }
                                )
                                Text("Persona", modifier = Modifier.padding(end = 8.dp))
                                RadioButton(
                                    selected = newRole == 2,
                                    onClick = { newRole = 2 }
                                )
                                Text("Especialista")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // Selector de estado
                            Text("Estado:")
                            Row {
                                RadioButton(
                                    selected = newStatus == "pendiente",
                                    onClick = { newStatus = "pendiente" }
                                )
                                Text("Pendiente", modifier = Modifier.padding(end = 8.dp))
                                RadioButton(
                                    selected = newStatus == "aprobado",
                                    onClick = { newStatus = "aprobado" }
                                )
                                Text("Aprobado")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            userToEdit?.let { adminViewModel.updateUserRoleAndStatus(it.uid, newRole, newStatus) }
                            showDialog = false
                        }) { Text("Guardar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
                    }
                )
            }

            // Diálogo para confirmar eliminación
            if (userToDelete != null) {
                AlertDialog(
                    onDismissRequest = { userToDelete = null },
                    title = { Text("Eliminar usuario") },
                    text = { Text("¿Estás seguro de que deseas eliminar a ${userToDelete?.displayName}? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        TextButton(onClick = {
                            userToDelete?.let { adminViewModel.deleteUser(it.uid) }
                            userToDelete = null
                        }) { Text("Eliminar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { userToDelete = null }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}

private fun rolToString(rol: Int?): String = when (rol) {
    1 -> "Persona"
    2 -> "Especialista"
    else -> "Desconocido"
} 