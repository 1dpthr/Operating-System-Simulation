
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class IpcTheme {

    public static final String BG = "#C8C4B0";
    public static final String PANEL = "#D4D0C0";
    public static final String CARD = "#E4E0D4";
    public static final String BTN = "#5A6642";
    public static final String BTN_HOVER = "#4A5632";
    public static final String TEXT = "#2A2622";
    public static final String TEXT_MUTED = "#5F5850";
    public static final String BORDER = "#A29B8A";

    private IpcTheme() {
    }

    public static void stylePrimary(Button btn) {
        btn.setStyle("-fx-background-color: " + BTN + "; -fx-text-fill: black; -fx-font-weight: bold;"
                + " -fx-background-radius: 6; -fx-padding: 8 16 8 16;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + BTN_HOVER
                + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 16 8 16;"));
        btn.setOnMouseExited(e -> stylePrimary(btn));
    }

    public static void styleSecondary(Button btn) {
        btn.setStyle("-fx-background-color: " + CARD + "; -fx-text-fill: " + TEXT + "; -fx-font-weight: bold;"
                + " -fx-border-color: " + BORDER + "; -fx-border-radius: 6; -fx-background-radius: 6;"
                + " -fx-padding: 8 16 8 16;");
    }

    public static void styleSidebar(Button btn, boolean active) {
        if (active) {
            btn.setStyle("-fx-background-color: " + BTN + "; -fx-text-fill: black; -fx-font-weight: bold;"
                    + " -fx-background-radius: 6; -fx-alignment: center-left; -fx-padding: 10 14 10 14;");
        } else {
            btn.setStyle("-fx-background-color: " + CARD + "; -fx-text-fill: " + TEXT + ";"
                    + " -fx-border-color: " + BORDER + "; -fx-border-radius: 6; -fx-background-radius: 6;"
                    + " -fx-alignment: center-left; -fx-padding: 10 14 10 14;");
        }
    }

    public static Label heading(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        lbl.setTextFill(javafx.scene.paint.Color.web(TEXT));
        return lbl;
    }

    public static Label subheading(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lbl.setTextFill(javafx.scene.paint.Color.web(TEXT_MUTED));
        lbl.setWrapText(true);
        return lbl;
    }

    public static Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("System", FontWeight.NORMAL, 13));
        lbl.setTextFill(javafx.scene.paint.Color.web(TEXT));
        lbl.setMinWidth(110);
        return lbl;
    }

    public static HBox formRow(String label, Region field) {
        HBox row = new HBox(10, fieldLabel(label), field);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    public static VBox card() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        return box;
    }
}
