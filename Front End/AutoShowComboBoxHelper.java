package sample;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import  javafx.scene.input.KeyCode;
import  javafx.scene.input.KeyEvent;
import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import  javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.util.Callback;


public class AutoShowComboBoxHelper {
    public AutoShowComboBoxHelper(final ComboBox<String> comboBox, final Callback<String, String> textBuilder) {
        final ObservableList<String> items = FXCollections.observableArrayList(comboBox.getItems());

        comboBox.getEditor().textProperty().addListener((ov, o, n) -> {
            if (n.equals(comboBox.getSelectionModel().getSelectedItem())) {
                return;
            }

            comboBox.hide();
            final FilteredList<String> filtered = items.filtered(s -> textBuilder.call(s).toLowerCase().contains(n.toLowerCase()));
            if (filtered.isEmpty()) {
                comboBox.getItems().setAll(items);
            } else {
                comboBox.getItems().setAll(filtered);
                comboBox.show();
            }
        });
    }
}
