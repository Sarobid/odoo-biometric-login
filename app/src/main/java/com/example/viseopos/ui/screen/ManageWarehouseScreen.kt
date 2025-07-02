package com.example.viseopos.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.viseopos.data.models.Warehouse
import com.example.viseopos.ui.navigation.AppDestinations
import com.example.viseopos.viewmodel.ManageWarehouseViewModel
import com.example.viseopos.viewmodel.ManageWarehouseViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageWarehouseScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    manageWarehouseViewModel: ManageWarehouseViewModel = viewModel(
        factory = ManageWarehouseViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    var showAddWarehouseDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf<Warehouse?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val availableWarehousesForSelection by manageWarehouseViewModel.availableWarehousesForSelection.collectAsState()
    val isLoading by manageWarehouseViewModel.isLoading.collectAsState()
    val errorMessage by manageWarehouseViewModel.errorMessage.collectAsState()
    val warehouses = manageWarehouseViewModel.warehouses

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gérer les Dépôts") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(AppDestinations.HOME_SCREEN_ROUTE) {
                                popUpTo(AppDestinations.HOME_SCREEN_ROUTE) { inclusive = true }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { manageWarehouseViewModel.loadListWarehouses() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Rafraîchir la liste des dépôts"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (availableWarehousesForSelection.isEmpty() && !isLoading) {
                    manageWarehouseViewModel.loadListWarehouses()
                }
                showAddWarehouseDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter un dépôt existant")
            }
        }
    ) { paddingValues ->
        Box(modifier = modifier
            .padding(paddingValues)
            .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (warehouses.isEmpty()) {
                    EmptyStateView()
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(
                            items = warehouses,
                            key = { warehouse -> warehouse.id }
                        ) { warehouse ->
                            WarehouseItem(
                                warehouse = warehouse,
                                onDeleteClick = { showConfirmDeleteDialog = warehouse }
                            )
                        }
                    }
                }
            }
            if (isLoading && warehouses.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if (showAddWarehouseDialog) {
        SelectWarehouseDialogWithSearch(
            availableWarehouses = availableWarehousesForSelection,
            alreadyAddedWarehouses = warehouses.toList(),
            onDismiss = { showAddWarehouseDialog = false },
            isLoading = isLoading,
            onWarehouseSelected = { selectedWarehouse ->
                val added = manageWarehouseViewModel.addSelectedWarehouse(selectedWarehouse)
                showAddWarehouseDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Dépôt '${selectedWarehouse.name}' ajouté.")
                }
            }
        )
    }

    showConfirmDeleteDialog?.let { warehouseToDelete ->
        ConfirmDeleteDialog(
            warehouseName = warehouseToDelete.name,
            onConfirm = {
                manageWarehouseViewModel.removeWarehouse(warehouseToDelete)
                scope.launch {
                    snackbarHostState.showSnackbar("Dépôt '${warehouseToDelete.name}' supprimé.")
                }
                showConfirmDeleteDialog = null
            },
            onDismiss = { showConfirmDeleteDialog = null }
        )
    }
}


@Composable
fun EmptyStateView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(
                imageVector = Icons.Filled.Warehouse,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Aucun dépôt",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Cliquez sur le bouton '+' pour ajouter un dépôt à partir de la liste, ou sur rafraîchir pour charger les dépôts disponibles.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WarehouseItem(warehouse: Warehouse, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warehouse,
                contentDescription = "Icône dépôt",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = warehouse.name, style = MaterialTheme.typography.titleMedium)
           //     Text(text = "ID: ${warehouse.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Supprimer le dépôt ${warehouse.name}", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectWarehouseDialogWithSearch(
    availableWarehouses: List<Warehouse>,
    alreadyAddedWarehouses: List<Warehouse>,
    onDismiss: () -> Unit,
    onWarehouseSelected: (Warehouse) -> Unit,
    isLoading: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedWarehouseForConfirmation by remember { mutableStateOf<Warehouse?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val filteredAndSelectableWarehouses = remember(searchQuery, availableWarehouses, alreadyAddedWarehouses) {
        val selectable = availableWarehouses.filter { avail ->
            alreadyAddedWarehouses.none { added -> added.id == avail.id }
        }
        if (searchQuery.isBlank()) {
            selectable
        } else {
            selectable.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.id.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 16.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier
                    .padding(all = 24.dp)
            ) {
                Text(
                    text = "Sélectionner un Dépôt",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        selectedWarehouseForConfirmation = null
                    },
                    label = { Text("Rechercher un dépôt...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Rechercher") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                selectedWarehouseForConfirmation = null
                            }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Effacer la recherche")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    isLoading && availableWarehouses.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Chargement des dépôts...")
                            }
                        }
                    }
                    !isLoading && availableWarehouses.isEmpty() -> {
                        Text(
                            text = "Aucun dépôt n'est disponible pour la sélection.",
                            modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally),
                            textAlign = TextAlign.Center
                        )
                    }
                    searchQuery.isNotBlank() && filteredAndSelectableWarehouses.isEmpty() && !isLoading -> {
                        Text(
                            text = "Aucun dépôt ne correspond à votre recherche.",
                            modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally),
                            textAlign = TextAlign.Center
                        )
                    }
                    filteredAndSelectableWarehouses.isEmpty() && !isLoading && searchQuery.isBlank() -> {
                        Text(
                            text = if (availableWarehouses.any { wh -> alreadyAddedWarehouses.none { it.name == wh.name }})
                                "Aucun dépôt ne correspond aux critères."
                            else
                                "Tous les dépôts disponibles ont déjà été ajoutés ou aucun n'est disponible.",
                            modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally),
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                        ) {
                            items(
                                items = filteredAndSelectableWarehouses,
                                key = { warehouse -> warehouse.id }
                            ) { warehouse ->
                                WarehouseSearchResultItem(
                                    warehouse = warehouse,
                                    onClick = {
                                        selectedWarehouseForConfirmation = warehouse
                                        searchQuery = warehouse.name
                                        keyboardController?.hide()
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (selectedWarehouseForConfirmation != null) {
                    Text(
                        "Sélectionné : ${selectedWarehouseForConfirmation!!.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
                    )
                } else {
                    Spacer(modifier = Modifier.height(MaterialTheme.typography.bodyMedium.lineHeight.value.dp + 8.dp))
                }
                Spacer(modifier = Modifier.weight(1f, fill = false))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedWarehouseForConfirmation?.let {
                                onWarehouseSelected(it)
                            }
                        },
                        enabled = selectedWarehouseForConfirmation != null && !isLoading
                    ) {
                        Text("Ajouter")
                    }
                }
            }
        }
    }
}

@Composable
fun WarehouseSearchResultItem(
    warehouse: Warehouse,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Warehouse,
            contentDescription = "Icône dépôt",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = warehouse.name, style = MaterialTheme.typography.titleSmall)
          //  Text(text = "ID: ${warehouse.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


@Composable
fun ConfirmDeleteDialog(
    warehouseName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer le Dépôt ?") },
        text = { Text("Êtes-vous sûr de vouloir supprimer le dépôt \"$warehouseName\" ?") },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Supprimer", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}