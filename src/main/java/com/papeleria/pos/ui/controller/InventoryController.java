package com.papeleria.pos.ui.controller;

import com.papeleria.pos.model.Product;
import com.papeleria.pos.service.InventoryService;
import com.papeleria.pos.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for the Inventory view.
 */
public class InventoryController {
    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @FXML private TextField searchField;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, BigDecimal> priceColumn;

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
        
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("salePrice"));

        productList = FXCollections.observableArrayList();
        productTable.setItems(productList);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchProducts(newValue);
        });

        loadProducts();
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
            List<Product> products = productService.getAll();
            productList.setAll(products);
        }
    }

    private void searchProducts(String query) {
        if (productService != null && query != null && !query.trim().isEmpty()) {
            List<Product> results = productService.search(query);
            productList.setAll(results);
        } else {
            loadProducts();
        }
    }

    @FXML
    private void handleNewProduct() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Producto");
        dialog.setHeaderText("Registrar un nuevo producto en inventario");

        ButtonType btnSaveType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));

        TextField barcodeField = new TextField();
        barcodeField.setPromptText("Ej. 750123456789");
        TextField nameField = new TextField();
        nameField.setPromptText("Nombre del producto");
        TextField purchasePriceField = new TextField("0.00");
        TextField salePriceField = new TextField("0.00");
        TextField stockField = new TextField("0");
        TextField minStockField = new TextField("5");

        grid.add(new Label("Código de Barras:"), 0, 0);
        grid.add(barcodeField, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Precio Compra:"), 0, 2);
        grid.add(purchasePriceField, 1, 2);
        grid.add(new Label("Precio Venta:"), 0, 3);
        grid.add(salePriceField, 1, 3);
        grid.add(new Label("Stock Inicial:"), 0, 4);
        grid.add(stockField, 1, 4);
        grid.add(new Label("Stock Mínimo:"), 0, 5);
        grid.add(minStockField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSaveType) {
                try {
                    Product p = new Product();
                    p.setBarcode(barcodeField.getText().trim());
                    p.setName(nameField.getText().trim());
                    p.setPurchasePrice(new BigDecimal(purchasePriceField.getText().trim()));
                    p.setSalePrice(new BigDecimal(salePriceField.getText().trim()));
                    p.setStock(Integer.parseInt(stockField.getText().trim()));
                    p.setMinStock(Integer.parseInt(minStockField.getText().trim()));
                    p.setUnit("pieza");
                    p.setActive(true);
                    return p;
                } catch (Exception e) {
                    showAlert("Datos inválidos", "Verifique que los precios y existencias sean numéricos.");
                    return null;
                }
            }
            return null;
        });

        java.util.Optional<Product> result = dialog.showAndWait();
        result.ifPresent(p -> {
            if (p.getName().isEmpty()) {
                showAlert("Error", "El nombre del producto no puede estar vacío.");
                return;
            }
            try {
                productService.createProduct(p);
                loadProducts();
                showAlert("Éxito", "Producto registrado correctamente.");
            } catch (Exception e) {
                showAlert("Error al registrar", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione un producto", "Por favor seleccione un producto para editar.");
            return;
        }

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Editar Producto");
        dialog.setHeaderText("Modificar producto: " + selected.getName());

        ButtonType btnSaveType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));

        TextField nameField = new TextField(selected.getName());
        TextField barcodeField = new TextField(selected.getBarcode() != null ? selected.getBarcode() : "");
        TextField purchasePriceField = new TextField(selected.getPurchasePrice() != null ? selected.getPurchasePrice().toString() : "0.00");
        TextField salePriceField = new TextField(selected.getSalePrice() != null ? selected.getSalePrice().toString() : "0.00");
        TextField minStockField = new TextField(String.valueOf(selected.getMinStock()));

        grid.add(new Label("Código de Barras:"), 0, 0);
        grid.add(barcodeField, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Precio Compra:"), 0, 2);
        grid.add(purchasePriceField, 1, 2);
        grid.add(new Label("Precio Venta:"), 0, 3);
        grid.add(salePriceField, 1, 3);
        grid.add(new Label("Stock Mínimo:"), 0, 4);
        grid.add(minStockField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSaveType) {
                try {
                    selected.setName(nameField.getText().trim());
                    selected.setBarcode(barcodeField.getText().trim());
                    selected.setPurchasePrice(new BigDecimal(purchasePriceField.getText().trim()));
                    selected.setSalePrice(new BigDecimal(salePriceField.getText().trim()));
                    selected.setMinStock(Integer.parseInt(minStockField.getText().trim()));
                    return selected;
                } catch (Exception e) {
                    showAlert("Datos inválidos", "Verifique que los precios sean valores numéricos.");
                    return null;
                }
            }
            return null;
        });

        java.util.Optional<Product> result = dialog.showAndWait();
        result.ifPresent(p -> {
            try {
                productService.updateProduct(p);
                loadProducts();
                showAlert("Éxito", "Producto actualizado correctamente.");
            } catch (Exception e) {
                showAlert("Error", "No se pudo actualizar el producto: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleAdjustInventory() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione un producto", "Por favor seleccione un producto para ajustar su inventario.");
            return;
        }

        ChoiceDialog<String> typeDialog = new ChoiceDialog<>("Entrada (Compra)", "Entrada (Compra)", "Merma/Pérdida (Salida)", "Ajuste de inventario");
        typeDialog.setTitle("Ajustar Inventario");
        typeDialog.setHeaderText("Producto: " + selected.getName() + " (Stock actual: " + selected.getStock() + ")");
        typeDialog.setContentText("Tipo de ajuste:");

        java.util.Optional<String> typeResult = typeDialog.showAndWait();
        if (typeResult.isEmpty()) return;

        TextInputDialog qtyDialog = new TextInputDialog("1");
        qtyDialog.setTitle("Cantidad");
        qtyDialog.setHeaderText("Ingrese la cantidad:");
        qtyDialog.setContentText("Cantidad:");
        java.util.Optional<String> qtyResult = qtyDialog.showAndWait();
        if (qtyResult.isEmpty()) return;

        try {
            int qty = Integer.parseInt(qtyResult.get().trim());
            if (qty <= 0) {
                showAlert("Error", "La cantidad debe ser mayor a 0.");
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
            showAlert("Éxito", "Inventario ajustado correctamente.");
        } catch (NumberFormatException e) {
            showAlert("Error", "La cantidad debe ser un número entero.");
        } catch (Exception e) {
            showAlert("Error", "No se pudo ajustar el inventario: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
