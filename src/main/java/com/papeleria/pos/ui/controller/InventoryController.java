package com.papeleria.pos.ui.controller;

import com.papeleria.pos.model.Product;
import com.papeleria.pos.service.InventoryService;
import com.papeleria.pos.service.ProductService;
import com.papeleria.pos.ui.component.AlertHelper;
import com.papeleria.pos.util.MoneyUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Inventory view with full editing and deletion support.
 */
public class InventoryController {
    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @FXML private TextField searchField;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, BigDecimal> purchasePriceColumn;
    @FXML private TableColumn<Product, BigDecimal> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, Integer> minStockColumn;
    @FXML private TableColumn<Product, String> unitColumn;
    @FXML private Label statusLabel;

    private ProductService productService;
    private InventoryService inventoryService;
    private ObservableList<Product> productList;

    public InventoryController() {
        this.productService = new ProductService();
        this.inventoryService = new InventoryService();
    }

    @FXML
    public void initialize() {
        logger.info("Initializing InventoryController");

        // Cell value factories
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        minStockColumn.setCellValueFactory(new PropertyValueFactory<>("minStock"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        purchasePriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("salePrice"));

        // Currency formatters
        purchasePriceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : MoneyUtil.format(item));
            }
        });

        priceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : MoneyUtil.format(item));
            }
        });

        // Stock alert cell styling
        stockColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(item.toString());
                    Product p = getTableRow() != null ? getTableRow().getItem() : null;
                    if (p != null && item <= p.getMinStock()) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        productList = FXCollections.observableArrayList();
        productTable.setItems(productList);

        // Real-time search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchProducts(newValue);
        });

        // Context Menu on table rows
        setupContextMenu();

        // Row double-click listener to open edit dialog
        productTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    handleEditProduct();
                }
            });
            return row;
        });

        // Keyboard shortcuts on TableView
        productTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                handleDeleteProduct();
            } else if (event.getCode() == KeyCode.ENTER) {
                handleEditProduct();
            }
        });

        loadProducts();
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem editItem = new MenuItem("✏️ Editar Producto");
        editItem.setOnAction(e -> handleEditProduct());

        MenuItem adjustItem = new MenuItem("📦 Ajustar Stock");
        adjustItem.setOnAction(e -> handleAdjustInventory());

        MenuItem deleteItem = new MenuItem("🗑️ Eliminar Producto");
        deleteItem.setOnAction(e -> handleDeleteProduct());

        contextMenu.getItems().addAll(editItem, adjustItem, new SeparatorMenuItem(), deleteItem);
        productTable.setContextMenu(contextMenu);
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
        loadProducts();
    }

    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    private void loadProducts() {
        if (productService != null) {
            try {
                List<Product> products = productService.getAll();
                productList.setAll(products);
                updateStatusLabel(products.size(), false);
            } catch (Exception e) {
                logger.error("Error loading products", e);
                AlertHelper.showError("Error al cargar productos", e.getMessage());
            }
        }
    }

    private void searchProducts(String query) {
        if (productService != null && query != null && !query.trim().isEmpty()) {
            try {
                List<Product> results = productService.search(query.trim());
                productList.setAll(results);
                updateStatusLabel(results.size(), true);
            } catch (Exception e) {
                logger.error("Error searching products", e);
            }
        } else {
            loadProducts();
        }
    }

    private void updateStatusLabel(int count, boolean isFiltered) {
        if (statusLabel != null) {
            if (isFiltered) {
                statusLabel.setText(count + " producto(s) encontrado(s)");
            } else {
                statusLabel.setText("Total: " + count + " artículo(s) en inventario activo");
            }
        }
    }

    @FXML
    private void handleNewProduct() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Producto");
        dialog.setHeaderText("Registrar un nuevo producto en inventario");

        ButtonType btnSaveType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField barcodeField = new TextField();
        barcodeField.setPromptText("Opcional (Ej. 750123456789)");
        TextField nameField = new TextField();
        nameField.setPromptText("Nombre del producto (Requerido)");
        TextField purchasePriceField = new TextField("0.00");
        TextField salePriceField = new TextField("0.00");
        TextField stockField = new TextField("0");
        TextField minStockField = new TextField("5");
        
        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll("pieza", "paquete", "caja", "metro", "docena", "rollo", "bolsa");
        unitCombo.setValue("pieza");
        unitCombo.setEditable(true);

        grid.add(new Label("Código de Barras:"), 0, 0);
        grid.add(barcodeField, 1, 0);
        grid.add(new Label("Nombre:*"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Precio Compra ($):"), 0, 2);
        grid.add(purchasePriceField, 1, 2);
        grid.add(new Label("Precio Venta ($):*"), 0, 3);
        grid.add(salePriceField, 1, 3);
        grid.add(new Label("Stock Inicial:"), 0, 4);
        grid.add(stockField, 1, 4);
        grid.add(new Label("Stock Mínimo:"), 0, 5);
        grid.add(minStockField, 1, 5);
        grid.add(new Label("Unidad de Medida:"), 0, 6);
        grid.add(unitCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSaveType) {
                try {
                    String name = nameField.getText() != null ? nameField.getText().trim() : "";
                    if (name.isEmpty()) {
                        AlertHelper.showWarning("Nombre requerido", "El nombre del producto no puede estar vacío.");
                        return null;
                    }

                    BigDecimal purchasePrice = new BigDecimal(purchasePriceField.getText().trim());
                    BigDecimal salePrice = new BigDecimal(salePriceField.getText().trim());
                    if (salePrice.compareTo(BigDecimal.ZERO) <= 0) {
                        AlertHelper.showWarning("Precio inválido", "El precio de venta debe ser mayor a 0.");
                        return null;
                    }
                    if (purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
                        AlertHelper.showWarning("Precio inválido", "El precio de compra no puede ser negativo.");
                        return null;
                    }

                    int stock = Integer.parseInt(stockField.getText().trim());
                    int minStock = Integer.parseInt(minStockField.getText().trim());
                    if (stock < 0 || minStock < 0) {
                        AlertHelper.showWarning("Stock inválido", "El stock y el stock mínimo no pueden ser negativos.");
                        return null;
                    }

                    Product p = new Product();
                    String barcode = barcodeField.getText() != null ? barcodeField.getText().trim() : "";
                    p.setBarcode(barcode.isEmpty() ? null : barcode);
                    p.setName(name);
                    p.setPurchasePrice(purchasePrice);
                    p.setSalePrice(salePrice);
                    p.setStock(stock);
                    p.setMinStock(minStock);
                    p.setUnit(unitCombo.getValue() != null && !unitCombo.getValue().trim().isEmpty() ? unitCombo.getValue().trim() : "pieza");
                    p.setActive(true);
                    return p;
                } catch (NumberFormatException e) {
                    AlertHelper.showError("Datos inválidos", "Verifique que los precios y existencias sean valores numéricos válidos.");
                    return null;
                }
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(p -> {
            try {
                productService.createProduct(p);
                loadProducts();
                AlertHelper.showInfo("Éxito", "El producto \"" + p.getName() + "\" ha sido registrado correctamente.");
            } catch (Exception e) {
                AlertHelper.showError("Error al registrar", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Seleccione un producto", "Por favor seleccione un producto de la tabla para editar.");
            return;
        }

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Editar Producto");
        dialog.setHeaderText("Modificar datos del producto: " + selected.getName());

        ButtonType btnSaveType = new ButtonType("Guardar Cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField barcodeField = new TextField(selected.getBarcode() != null ? selected.getBarcode() : "");
        barcodeField.setPromptText("Opcional (Ej. 750123456789)");

        TextField nameField = new TextField(selected.getName());
        nameField.setPromptText("Nombre del producto (Requerido)");

        TextField purchasePriceField = new TextField(selected.getPurchasePrice() != null ? selected.getPurchasePrice().toString() : "0.00");
        TextField salePriceField = new TextField(selected.getSalePrice() != null ? selected.getSalePrice().toString() : "0.00");
        TextField stockField = new TextField(String.valueOf(selected.getStock()));
        TextField minStockField = new TextField(String.valueOf(selected.getMinStock()));

        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.getItems().addAll("pieza", "paquete", "caja", "metro", "docena", "rollo", "bolsa");
        unitCombo.setValue(selected.getUnit() != null ? selected.getUnit() : "pieza");
        unitCombo.setEditable(true);

        grid.add(new Label("Código de Barras:"), 0, 0);
        grid.add(barcodeField, 1, 0);
        grid.add(new Label("Nombre:*"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Precio Compra ($):"), 0, 2);
        grid.add(purchasePriceField, 1, 2);
        grid.add(new Label("Precio Venta ($):*"), 0, 3);
        grid.add(salePriceField, 1, 3);
        grid.add(new Label("Existencia Actual (Stock):"), 0, 4);
        grid.add(stockField, 1, 4);
        grid.add(new Label("Stock Mínimo:"), 0, 5);
        grid.add(minStockField, 1, 5);
        grid.add(new Label("Unidad de Medida:"), 0, 6);
        grid.add(unitCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSaveType) {
                try {
                    String name = nameField.getText() != null ? nameField.getText().trim() : "";
                    if (name.isEmpty()) {
                        AlertHelper.showWarning("Nombre requerido", "El nombre del producto no puede estar vacío.");
                        return null;
                    }

                    BigDecimal purchasePrice = new BigDecimal(purchasePriceField.getText().trim());
                    BigDecimal salePrice = new BigDecimal(salePriceField.getText().trim());
                    if (salePrice.compareTo(BigDecimal.ZERO) <= 0) {
                        AlertHelper.showWarning("Precio inválido", "El precio de venta debe ser mayor a 0.");
                        return null;
                    }
                    if (purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
                        AlertHelper.showWarning("Precio inválido", "El precio de compra no puede ser negativo.");
                        return null;
                    }

                    int newStock = Integer.parseInt(stockField.getText().trim());
                    int minStock = Integer.parseInt(minStockField.getText().trim());
                    if (newStock < 0 || minStock < 0) {
                        AlertHelper.showWarning("Stock inválido", "El stock y el stock mínimo no pueden ser negativos.");
                        return null;
                    }

                    String barcode = barcodeField.getText() != null ? barcodeField.getText().trim() : "";
                    selected.setBarcode(barcode.isEmpty() ? null : barcode);
                    selected.setName(name);
                    selected.setPurchasePrice(purchasePrice);
                    selected.setSalePrice(salePrice);
                    selected.setStock(newStock);
                    selected.setMinStock(minStock);
                    selected.setUnit(unitCombo.getValue() != null && !unitCombo.getValue().trim().isEmpty() ? unitCombo.getValue().trim() : "pieza");
                    return selected;
                } catch (NumberFormatException e) {
                    AlertHelper.showError("Datos inválidos", "Verifique que los precios y existencias sean valores numéricos válidos.");
                    return null;
                }
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(p -> {
            try {
                productService.updateProduct(p);
                loadProducts();
                AlertHelper.showInfo("Éxito", "El producto \"" + p.getName() + "\" ha sido actualizado correctamente.");
            } catch (Exception e) {
                logger.error("Error updating product", e);
                AlertHelper.showError("Error al actualizar", e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Seleccione un producto", "Por favor seleccione el producto que desea eliminar de la tabla.");
            return;
        }

        String codeInfo = selected.getBarcode() != null && !selected.getBarcode().isEmpty() 
                ? " (Código: " + selected.getBarcode() + ")" 
                : "";
        String confirmMsg = "¿Está seguro de que desea eliminar el producto \"" + selected.getName() + "\"" + codeInfo + "?\n\n"
                + "Stock actual: " + selected.getStock() + " " + selected.getUnit() + "(s).\n"
                + "Esta acción removerá el producto del inventario activo.";

        boolean confirmed = AlertHelper.showConfirmation("Confirmar Eliminación", confirmMsg);
        if (!confirmed) {
            return;
        }

        try {
            boolean permanentlyDeleted = productService.deleteProduct(selected.getId());
            loadProducts();
            if (permanentlyDeleted) {
                AlertHelper.showInfo("Producto Eliminado", "El producto \"" + selected.getName() + "\" ha sido eliminado exitosamente del catálogo.");
            } else {
                AlertHelper.showInfo("Producto Archivado", "El producto \"" + selected.getName() + "\" cuenta con historial de ventas o movimientos contables, por lo que fue desactivado y archivado del catálogo activo.");
            }
        } catch (Exception e) {
            logger.error("Error deleting product", e);
            AlertHelper.showError("Error al eliminar", "No se pudo eliminar el producto: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdjustInventory() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Seleccione un producto", "Por favor seleccione un producto para ajustar su inventario.");
            return;
        }

        ChoiceDialog<String> typeDialog = new ChoiceDialog<>("Entrada (Compra)", "Entrada (Compra)", "Merma/Pérdida (Salida)", "Ajuste de inventario");
        typeDialog.setTitle("Ajustar Inventario");
        typeDialog.setHeaderText("Producto: " + selected.getName() + " (Stock actual: " + selected.getStock() + " " + selected.getUnit() + ")");
        typeDialog.setContentText("Tipo de ajuste:");

        Optional<String> typeResult = typeDialog.showAndWait();
        if (typeResult.isEmpty()) return;

        TextInputDialog qtyDialog = new TextInputDialog("1");
        qtyDialog.setTitle("Cantidad");
        qtyDialog.setHeaderText("Ingrese la cantidad:");
        qtyDialog.setContentText("Cantidad:");
        Optional<String> qtyResult = qtyDialog.showAndWait();
        if (qtyResult.isEmpty()) return;

        try {
            int qty = Integer.parseInt(qtyResult.get().trim());
            if (qty <= 0) {
                AlertHelper.showWarning("Error", "La cantidad debe ser mayor a 0.");
                return;
            }

            String type = typeResult.get();
            if (type.startsWith("Entrada")) {
                inventoryService.updateStock(selected.getId(), qty, "Entrada manual de inventario");
            } else if (type.startsWith("Merma")) {
                inventoryService.removeStock(selected.getId(), qty, "Merma o daño", "LOSS");
            } else {
                inventoryService.removeStock(selected.getId(), qty, "Ajuste manual", "ADJUSTMENT");
            }
            loadProducts();
            AlertHelper.showInfo("Éxito", "Inventario ajustado correctamente para \"" + selected.getName() + "\".");
        } catch (NumberFormatException e) {
            AlertHelper.showError("Error", "La cantidad debe ser un número entero válido.");
        } catch (Exception e) {
            logger.error("Error adjusting inventory", e);
            AlertHelper.showError("Error", "No se pudo ajustar el inventario: " + e.getMessage());
        }
    }
}
