package com.papeleria.pos.ui.controller;

import com.papeleria.pos.model.Product;
import com.papeleria.pos.model.Sale;
import com.papeleria.pos.model.SaleItem;
import com.papeleria.pos.service.ProductService;
import com.papeleria.pos.service.SaleService;
import com.papeleria.pos.service.CashService;
import com.papeleria.pos.ui.component.AlertHelper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for the Point of Sale view.
 */
public class PosController {
    private static final Logger logger = LoggerFactory.getLogger(PosController.class);

    @FXML private TextField searchField;
    @FXML private TableView<SaleItem> cartTable;
    @FXML private TableColumn<SaleItem, String> colProductName;
    @FXML private TableColumn<SaleItem, BigDecimal> colPrice;
    @FXML private TableColumn<SaleItem, Integer> colQuantity;
    @FXML private TableColumn<SaleItem, BigDecimal> colSubtotal;
    
    @FXML private Label subtotalLabel;
    @FXML private Label totalLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;

    private final ProductService productService;
    private final SaleService saleService;
    private final CashService cashService;
    
    private final ObservableList<SaleItem> cartItems = FXCollections.observableArrayList();

    public PosController() {
        this.productService = new ProductService();
        this.saleService = new SaleService();
        this.cashService = new CashService();
    }

    @FXML
    public void initialize() {
        setupTable();
        paymentMethodComboBox.getItems().setAll("EFECTIVO", "TARJETA", "TRANSFERENCIA", "OTRO");
        paymentMethodComboBox.setValue("EFECTIVO");
        updateTotals();
    }

    private void setupTable() {
        cartTable.setItems(cartItems);
        cartTable.setEditable(true);

        colProductName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductNameSnapshot()));
        colPrice.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getUnitPriceSnapshot()));
        
        colQuantity.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getQuantity()));
        colQuantity.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colQuantity.setOnEditCommit(event -> {
            SaleItem item = event.getRowValue();
            item.setQuantity(event.getNewValue());
            item.setSubtotal(item.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())));
            cartTable.refresh();
            updateTotals();
        });

        colSubtotal.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getSubtotal()));
    }

    @FXML
    private void onSearchEnter(ActionEvent event) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        try {
            Product product = productService.getByBarcode(query);
            if (product == null) {
                java.util.List<Product> matches = productService.search(query);
                if (matches.size() == 1) {
                    product = matches.get(0);
                } else if (matches.size() > 1) {
                    ChoiceDialog<Product> choice = new ChoiceDialog<>(matches.get(0), matches);
                    choice.setTitle("Varios productos encontrados");
                    choice.setHeaderText("Seleccione el producto a agregar:");
                    choice.setContentText("Producto:");
                    Optional<Product> chosen = choice.showAndWait();
                    if (chosen.isPresent()) {
                        product = chosen.get();
                    } else {
                        return;
                    }
                }
            }
            if (product == null) {
                AlertHelper.showWarning("Producto no encontrado", "No se encontró ningún producto con: " + query);
                return;
            }
            addProductToCart(product);
            searchField.clear();
        } catch (Exception e) {
            logger.error("Error searching product: {}", query, e);
            AlertHelper.showError("Error de búsqueda", "Ocurrió un error al buscar el producto.");
        }
    }

    private void addProductToCart(Product product) {
        for (SaleItem item : cartItems) {
            if (item.getProductId() == product.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                item.setSubtotal(item.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())));
                cartTable.refresh();
                updateTotals();
                return;
            }
        }
        SaleItem newItem = new SaleItem();
        newItem.setProductId(product.getId());
        newItem.setProductNameSnapshot(product.getName());
        newItem.setProductBarcodeSnapshot(product.getBarcode());
        newItem.setUnitPriceSnapshot(product.getSalePrice());
        newItem.setQuantity(1);
        newItem.setDiscount(BigDecimal.ZERO);
        newItem.setSubtotal(product.getSalePrice());
        cartItems.add(newItem);
        updateTotals();
    }

    private void updateTotals() {
        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem item : cartItems) {
            total = total.add(item.getSubtotal());
        }
        subtotalLabel.setText(String.format("$%.2f", total));
        totalLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    private void onConfirmSale(ActionEvent event) {
        if (cartItems.isEmpty()) {
            AlertHelper.showWarning("Carrito vacío", "Agregue productos antes de confirmar.");
            return;
        }
        if (!cashService.isSessionOpen()) {
            AlertHelper.showError("Caja Cerrada", "Debe abrir la caja antes de registrar ventas.");
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem item : cartItems) {
            total = total.add(item.getSubtotal());
        }

        String method = paymentMethodComboBox.getValue();
        BigDecimal amountPaid = total;
        BigDecimal change = BigDecimal.ZERO;

        if ("EFECTIVO".equals(method)) {
            TextInputDialog dialog = new TextInputDialog(total.toString());
            dialog.setTitle("Pago en Efectivo");
            dialog.setHeaderText("Total a pagar: $" + total);
            dialog.setContentText("Efectivo recibido:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    amountPaid = new BigDecimal(result.get());
                    if (amountPaid.compareTo(total) < 0) {
                        AlertHelper.showError("Pago insuficiente", "El efectivo recibido es menor al total.");
                        return;
                    }
                    change = amountPaid.subtract(total);
                    AlertHelper.showInfo("Cambio", "Cambio a entregar: $" + change);
                } catch (NumberFormatException e) {
                    AlertHelper.showError("Monto inválido", "Ingrese un número válido.");
                    return;
                }
            } else {
                return; // User cancelled
            }
        }

        try {
            Sale sale = new Sale();
            sale.setSaleNumber("VNT-" + System.currentTimeMillis());
            sale.setIdempotencyToken(UUID.randomUUID().toString());
            sale.setCashSessionId(cashService.getActiveSession().getId());
            sale.setSubtotal(total);
            sale.setDiscount(BigDecimal.ZERO);
            sale.setTotal(total);
            sale.setPaymentMethod(method);
            sale.setAmountPaid(amountPaid);
            sale.setChange(change);
            sale.setStatus("COMPLETED");
            sale.setCreatedAt(LocalDateTime.now());
            sale.setItems(new ArrayList<>(cartItems));

            saleService.completeSale(sale);
            AlertHelper.showInfo("Venta Exitosa", "La venta se ha registrado correctamente.");
            onCancelSale(null); // Clear cart
        } catch (Exception e) {
            logger.error("Error completing sale", e);
            AlertHelper.showError("Error al confirmar", "Ocurrió un error al procesar la venta: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelSale(ActionEvent event) {
        cartItems.clear();
        updateTotals();
        searchField.clear();
        searchField.requestFocus();
    }
}
