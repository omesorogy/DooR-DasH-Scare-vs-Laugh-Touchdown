package game.gui.view;

import game.engine.monsters.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;

import java.io.InputStream;

/**
 * Side panel showing all required monster info:
 *  - Portrait image (if available), name, type, original + current role
 *  - Confusion visual indicator (banner + red border)
 *  - Energy value + progress bar
 *  - Current board position (1-based)
 *  - All active status effects with durations:
 *    FROZEN, CONFUSED(Nt), SHIELD, MOMENTUM RUSH(Nt), FOCUS MODE(Nt)
 */
public class MonsterInfoPanel extends VBox {

    private final boolean isPlayer;

    private Label nameLabel;
    private Label typeLabel;
    private Label origRoleLabel;
    private Label currRoleLabel;
    private Label confusionBanner;
    private Label energyLabel;
    private ProgressBar energyBar;
    private Label posLabel;
    private Label statusLabel;
    private ImageView portrait;

    public MonsterInfoPanel(boolean isPlayer) {
        this.isPlayer = isPlayer;
        build();
    }

    // ---- Construction ----

    private void build() {
        setSpacing(4);
        setPadding(new Insets(9));
        setMaxWidth(Double.MAX_VALUE);

        String borderColor = isPlayer ? "#00cc44" : "#ff6633";
        String bgColor     = isPlayer ? "#091809"  : "#190808";
        setStyle(
            "-fx-background-color:" + bgColor + ";" +
            "-fx-border-color:" + borderColor + ";" +
            "-fx-border-width:2;" +
            "-fx-border-radius:10;" +
            "-fx-background-radius:10;");

        // Header
        Label header = new Label(isPlayer ? "YOU" : "OPPONENT");
        header.setFont(Font.font("Impact", 14));
        header.setTextFill(Color.web(borderColor));

        // Portrait circle + fallback letter
        Circle avatarCircle = new Circle(22);
        avatarCircle.setFill(Color.web(isPlayer ? "#003311" : "#330011"));
        avatarCircle.setStroke(Color.web(borderColor));
        avatarCircle.setStrokeWidth(2);

        portrait = new ImageView();
        portrait.setFitWidth(38); portrait.setFitHeight(38);
        portrait.setPreserveRatio(true); portrait.setSmooth(true);
        // Circular clip
        Circle clip = new Circle(19, 19, 19);
        portrait.setClip(clip);

        Label avatarLbl = new Label(isPlayer ? "[P]" : "[O]");
        avatarLbl.setFont(Font.font("Impact", 10));
        avatarLbl.setTextFill(Color.WHITE);

        StackPane avatarStack = new StackPane(avatarCircle, avatarLbl, portrait);
        avatarStack.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(avatarStack, Pos.CENTER_LEFT);

        // Name + type
        nameLabel = styled("", 14, Color.WHITE, true);
        nameLabel.setWrapText(true); nameLabel.setMaxWidth(Double.MAX_VALUE);
        typeLabel = styled("", 11, Color.web("#AAAACC"), false);

        // Roles row
        HBox roleRow = new HBox(4);
        roleRow.setAlignment(Pos.CENTER_LEFT);
        Label origHdr = styled("Role: ", 11, Color.web("#999999"), false);
        origRoleLabel = styled("", 11, Color.web("#FFDD00"), false);
        Label currHdr = styled("  Now: ", 11, Color.web("#999999"), false);
        currRoleLabel = styled("", 11, Color.web("#00FFAA"), false);
        currRoleLabel.setWrapText(true);
        roleRow.getChildren().addAll(origHdr, origRoleLabel, currHdr, currRoleLabel);

        // Confusion banner
        confusionBanner = new Label("!! ROLE CONFUSED !!");
        confusionBanner.setMaxWidth(Double.MAX_VALUE);
        confusionBanner.setAlignment(Pos.CENTER);
        confusionBanner.setTextAlignment(TextAlignment.CENTER);
        confusionBanner.setFont(Font.font("Impact", 12));
        confusionBanner.setTextFill(Color.web("#FF2222"));
        confusionBanner.setStyle(
            "-fx-background-color:#3a0000;" +
            "-fx-border-color:#FF2222;" +
            "-fx-border-width:1;" +
            "-fx-border-radius:6;" +
            "-fx-background-radius:6;" +
            "-fx-padding:3 8 3 8;");
        confusionBanner.setVisible(false);
        confusionBanner.setManaged(false);

        // Energy
        energyLabel = styled("Energy: 0 / 1000", 12, Color.web("#FFD700"), true);
        energyBar = new ProgressBar(0);
        energyBar.setMaxWidth(Double.MAX_VALUE);
        energyBar.setPrefHeight(11);
        energyBar.setStyle("-fx-accent:" + (isPlayer ? "#00cc44" : "#ff4400") + ";");

        // Position
        posLabel = styled("Cell: 1", 11, Color.web("#88AAFF"), false);

        // Status effects
        statusLabel = styled("Status: None", 11, Color.web("#FF8844"), false);
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:" + borderColor + ";");

        getChildren().addAll(
            header, avatarStack, nameLabel, typeLabel,
            sep, roleRow, confusionBanner,
            energyLabel, energyBar,
            posLabel, statusLabel
        );
    }

    // ---- Update ----

    public void update(Monster m, boolean isCurrent) {
        if (m == null) return;

        // Load portrait image
        Image img = loadMonsterImage(m);
        if (img != null) {
            portrait.setImage(img);
            portrait.setVisible(true);
        } else {
            portrait.setVisible(false);
        }

        nameLabel.setText(m.getName());
        typeLabel.setText("Type: " + m.getClass().getSimpleName());

        String origRole = (m.getOriginalRole() != null)
            ? m.getOriginalRole().toString()
            : m.getRole().toString();
        String currRole = m.getRole().toString();
        origRoleLabel.setText(origRole);

        boolean confused = m.isConfused();
        if (confused) {
            currRoleLabel.setText(currRole + " [CONFUSED " + m.getConfusionTurns() + "t]");
            currRoleLabel.setTextFill(Color.web("#FF4444"));
            confusionBanner.setVisible(true);
            confusionBanner.setManaged(true);
            String bgColor = isPlayer ? "#091809" : "#190808";
            setStyle(
                "-fx-background-color:" + bgColor + ";" +
                "-fx-border-color:#FF2222;" +
                "-fx-border-width:2;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;");
        } else {
            currRoleLabel.setText(currRole);
            currRoleLabel.setTextFill(Color.web("#00FFAA"));
            confusionBanner.setVisible(false);
            confusionBanner.setManaged(false);
            String borderColor = isPlayer ? "#00cc44" : "#ff6633";
            String bgColor     = isPlayer ? "#091809" : "#190808";
            setStyle(
                "-fx-background-color:" + bgColor + ";" +
                "-fx-border-color:" + borderColor + ";" +
                "-fx-border-width:2;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;");
        }

        int energy = m.getEnergy();
        energyLabel.setText("Energy: " + energy + " / 1000");
        energyBar.setProgress(Math.min(1.0, Math.max(0.0, energy / 1000.0)));
        if      (energy >= 1000) energyBar.setStyle("-fx-accent:#FFD700;");
        else if (energy >= 500)  energyBar.setStyle("-fx-accent:" + (isPlayer ? "#00cc44" : "#ff4400") + ";");
        else                     energyBar.setStyle("-fx-accent:#cc2200;");

        posLabel.setText("Cell: " + (m.getPosition() + 1));

        // Status effects
        StringBuilder sb = new StringBuilder("Status: ");
        boolean any = false;
        if (m.isFrozen()) { sb.append("[FROZEN] "); any = true; }
        if (confused) {
            sb.append("[CONFUSED(").append(m.getConfusionTurns()).append("t)] "); any = true;
        }
        if (m.isShielded()) { sb.append("[SHIELD] "); any = true; }
        if (m instanceof Dasher) {
            Dasher d = (Dasher) m;
            if (d.getMomentumTurns() > 0) {
                sb.append("[MOMENTUM RUSH(").append(d.getMomentumTurns()).append("t)] "); any = true;
            }
        }
        if (m instanceof MultiTasker) {
            MultiTasker mt = (MultiTasker) m;
            if (mt.getNormalSpeedTurns() > 0) {
                sb.append("[FOCUS MODE(").append(mt.getNormalSpeedTurns()).append("t)] "); any = true;
            }
        }
        if (!any) sb.append("None");
        statusLabel.setText(sb.toString());

        // Active glow + border when it is this monster's turn
        String activeBorder = isCurrent
            ? (isPlayer ? "#00ff88" : "#ff6622")
            : (isPlayer ? "#00cc44" : "#ff6633");
        int bw = isCurrent ? 3 : 2;
        setEffect(isCurrent ? new DropShadow(16, Color.web(activeBorder)) : null);
        setBorder(new Border(new BorderStroke(
            Color.web(activeBorder),
            BorderStrokeStyle.SOLID,
            new CornerRadii(10),
            new BorderWidths(bw))));
    }

    // ---- Image loading ----

    private Image loadMonsterImage(Monster m) {
        String key = monsterKey(m);
        if (key == null) return null;
        String[] exts = {".png", ".jpg", ".jpeg"};
        for (String ext : exts) {
            String path = "/game/gui/resources/images/" + key + ext;
            try {
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    Image img = new Image(is);
                    if (!img.isError()) return img;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String monsterKey(Monster m) {
        if (m == null) return null;
        String n = m.getName().toLowerCase();
        if (n.contains("sullivan") || n.contains("sulley")) return "sulley";
        if (n.contains("wazowski") || n.contains("mike"))   return "mike";
        if (n.contains("randall"))                          return "randall";
        if (n.contains("celia"))                            return "celia";
        if (n.contains("roz"))                              return "roz";
        if (n.contains("fungus"))                           return "fungus";
        if (n.contains("waternoose"))                       return "spider";
        if (n.contains("yeti"))                             return "yeti";
        return null;
    }

    // ---- Helper ----

    private Label styled(String text, int size, Color color, boolean bold) {
        Label l = new Label(text);
        l.setFont(Font.font("Georgia", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        l.setTextFill(color);
        return l;
    }
}
