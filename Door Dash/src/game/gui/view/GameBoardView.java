package game.gui.view;

import game.engine.Role;
import game.engine.cells.Cell;
import game.engine.cells.*;
import game.engine.monsters.*;
import game.gui.model.GameModel;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * GameBoardView - 10x10 board that fills its parent.
 * Each cell shows:  index (1-based), a type label, and an image when applicable.
 * Exhausted door cells turn BLACK.
 * Monster tokens are larger and show the monster portrait.
 */
public class GameBoardView extends Region {

    private static final int ROWS = 10;
    private static final int COLS = 10;
    private static final int GAP  = 3;
    private static final int PAD  = 6;

    private final StackPane[][] cellPanes = new StackPane[ROWS][COLS];
    private final GameModel model;

    private final Map<String, Image> imgCache = new HashMap<>();

    // ---- Consistent cell colours ----
    private static final String C_SCARER_DOOR   = "#4A1080";   // deep purple
    private static final String C_LAUGHER_DOOR  = "#0A5C28";   // deep green
    private static final String C_DOOR_EXHAUSTED = "#000000";  // BLACK when used
    private static final String C_MONSTER        = "#0A2A5C";  // navy blue
    private static final String C_CARD           = "#7A0000";  // dark red
    private static final String C_BELT           = "#006080";  // deep teal (distinct from door greens)
    private static final String C_SOCK           = "#C04000";  // burnt orange
    private static final String C_NORMAL         = "#1A1A30";  // dark navy
    private static final String C_START          = "#003830";  // dark teal
    private static final String C_WIN            = "#6B4A00";  // dark gold

    // Image keys
    private static final String IMG_BOO      = "boo";
    private static final String IMG_CARD     = "card";
    private static final String IMG_CONVEYOR = "conveyor";
    private static final String IMG_SOCK     = "sock";
    // Door images rotate through Door1-Door6
    private static final String IMG_SULLEY     = "sullivan";
    private static final String IMG_MIKE       = "mike";
    private static final String IMG_RANDALL    = "randall";
    private static final String IMG_CELIA      = "celia";
    private static final String IMG_ROZ        = "roz";
    private static final String IMG_FUNGUS     = "fungus";
    private static final String IMG_YETI       = "yeti";
    private static final String IMG_WATERNOOSE = "spider";
    // Card images
    private static final String IMG_SWAPPER   = "swapper";
    private static final String IMG_STARTOVER = "startover";
    private static final String IMG_ESTEAL    = "energysteal";
    private static final String IMG_SHIELD    = "shield";
    private static final String IMG_CONFUSION = "confusion";

    public GameBoardView(GameModel model) {
        this.model = model;
        setStyle("-fx-background-color: #0A0A14;");
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        preloadImages();
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                StackPane p = new StackPane();
                cellPanes[r][c] = p;
                getChildren().add(p);
            }
    }

    // ---- Image loading ----

    private void preloadImages() {
        String[] keys = {
            IMG_BOO, IMG_CARD, IMG_CONVEYOR, IMG_SOCK,
            IMG_SULLEY, IMG_MIKE, IMG_RANDALL, IMG_CELIA,
            IMG_ROZ, IMG_FUNGUS, IMG_YETI, IMG_WATERNOOSE,
            IMG_SWAPPER, IMG_STARTOVER, IMG_ESTEAL, IMG_SHIELD, IMG_CONFUSION,
            "door1","door2","door3","door4","door5","door6"
        };
        for (String k : keys) imgCache.put(k, loadImage(k));
    }

    private Image loadImage(String key) {
        String[] exts = {".png", ".jpg", ".jpeg"};
        for (String ext : exts) {
            try {
                InputStream is = getClass().getResourceAsStream(
                    "/game/gui/resources/images/" + key + ext);
                if (is != null) {
                    Image img = new Image(is);
                    if (!img.isError()) return img;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private ImageView iv(String key, double w, double h) {
        Image img = imgCache.get(key);
        if (img == null) return null;
        ImageView view = new ImageView(img);
        view.setFitWidth(w); view.setFitHeight(h);
        view.setPreserveRatio(true); view.setSmooth(true);
        return view;
    }

    /** Returns the purple-background monster image key for a given monster. */
    private String monsterKey(Monster m) {
        if (m == null) return null;
        String n = m.getName().toLowerCase();
        if (n.contains("sullivan") || n.contains("sulley")) return IMG_SULLEY;
        if (n.contains("wazowski") || n.contains("mike"))   return IMG_MIKE;
        if (n.contains("randall"))                          return IMG_RANDALL;
        if (n.contains("celia"))                            return IMG_CELIA;
        if (n.contains("roz"))                              return IMG_ROZ;
        if (n.contains("fungus"))                           return IMG_FUNGUS;
        if (n.contains("waternoose"))                       return IMG_WATERNOOSE;
        if (n.contains("yeti"))                             return IMG_YETI;
        return null;
    }

    /** Door image key cycles through Door1..Door6 based on cell index. */
    private String doorImageKey(int idx) {
        return "door" + ((idx % 6) + 1);
    }

    // ---- Layout ----

    @Override protected double computePrefWidth(double h)  { return 700; }
    @Override protected double computePrefHeight(double w) { return 700; }

    @Override
    protected void layoutChildren() {
        double w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        double cellW = (w - 2.0*PAD - (COLS-1)*GAP) / COLS;
        double cellH = (h - 2.0*PAD - (ROWS-1)*GAP) / ROWS;
        double cell  = Math.max(30, Math.min(cellW, cellH));
        double gx    = (w - (COLS*cell + (COLS-1)*GAP)) / 2.0;
        double gy    = (h - (ROWS*cell + (ROWS-1)*GAP)) / 2.0;
        for (int row = 0; row < ROWS; row++)
            for (int col = 0; col < COLS; col++)
                cellPanes[row][col].resizeRelocate(
                    gx + col*(cell+GAP),
                    gy + (ROWS-1-row)*(cell+GAP),
                    cell, cell);
        refreshCells((int) cell);
    }

    public void refresh() { requestLayout(); }

    // ---- Cell rendering ----

    private void refreshCells(int cs) {
        Cell[][] board   = model.getBoardCells();
        Monster player   = model.getPlayer();
        Monster opponent = model.getOpponent();

        int fIdx  = Math.max(10, cs / 6);   // index font - always readable
        int fSml  = Math.max(9,  cs / 8);   // small label font
        int imgSz = Math.max(20, (int)(cs * 0.52));
        int tokR  = Math.max(10, cs / 5);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx   = model.cellIndexAt(row, col);
                Cell cell = board[row][col];
                StackPane pane = cellPanes[row][col];
                pane.getChildren().clear();
                pane.setClip(null);          // never clip children

                // 1. Coloured background rectangle
                boolean exhausted = (cell instanceof DoorCell) && ((DoorCell)cell).isActivated();
                Rectangle bg = new Rectangle(cs - 2, cs - 2);
                bg.setArcWidth(7); bg.setArcHeight(7);
                bg.setFill(cellColor(cell, idx, exhausted));
                bg.setStroke(Color.web(exhausted ? "#444444" : "#3A3A60"));
                bg.setStrokeWidth(1.5);
                pane.getChildren().add(bg);

                // 2. Cell image (non-door cells only, full opacity)
                addCellImage(pane, cell, idx, imgSz, exhausted);

                // 3. Overlay group - all text drawn with absolute pixel positions
                //    so layout manager can never interfere
                javafx.scene.Group overlay = new javafx.scene.Group();

                // --- INDEX: top-left pill ---
                String idxStr = String.valueOf(idx);
                Text idxT = new Text(idxStr);
                idxT.setFont(Font.font("Consolas", FontWeight.BOLD, fIdx));
                idxT.setFill(Color.WHITE);
                double idxW = idxT.getBoundsInLocal().getWidth();
                double idxH = idxT.getBoundsInLocal().getHeight();
                // pill background
                Rectangle idxBg = new Rectangle(idxW + 6, idxH + 2,
                    Color.color(0, 0, 0, 0.85));
                idxBg.setArcWidth(4); idxBg.setArcHeight(4);
                idxBg.setX(2); idxBg.setY(2);
                idxT.setX(5); idxT.setY(2 + idxH - 2); // baseline align
                overlay.getChildren().addAll(idxBg, idxT);

                // --- DOOR ENERGY: bottom-centre pill ---
                if (cell instanceof DoorCell) {
                    DoorCell dc = (DoorCell) cell;

                    // Role tag top-right
                    String roleStr = exhausted ? "USED"
                        : (dc.getRole() == Role.SCARER ? "SCR" : "LGH");
                    Color roleCol = exhausted ? Color.GRAY
                        : (dc.getRole() == Role.SCARER
                            ? Color.web("#CC88FF") : Color.web("#88FFCC"));
                    Text roleT = new Text(roleStr);
                    roleT.setFont(Font.font("Impact", fSml));
                    roleT.setFill(roleCol);
                    double rW = roleT.getBoundsInLocal().getWidth();
                    double rH = roleT.getBoundsInLocal().getHeight();
                    Rectangle roleBg = new Rectangle(rW + 6, rH + 2,
                        Color.color(0, 0, 0, 0.85));
                    roleBg.setArcWidth(4); roleBg.setArcHeight(4);
                    roleBg.setX(cs - rW - 8); roleBg.setY(2);
                    roleT.setX(cs - rW - 5); roleT.setY(2 + rH - 2);
                    overlay.getChildren().addAll(roleBg, roleT);

                    // Energy bottom-centre
                    String eStr = "E:" + dc.getEnergy();
                    Color eCol  = exhausted ? Color.GRAY : Color.web("#FFD700");
                    Text eT = new Text(eStr);
                    eT.setFont(Font.font("Impact", FontWeight.BOLD, fSml + 2));
                    eT.setFill(eCol);
                    double eW = eT.getBoundsInLocal().getWidth();
                    double eH = eT.getBoundsInLocal().getHeight();
                    double eX = (cs - eW) / 2.0;
                    double eY = cs - 5;
                    Rectangle eBg = new Rectangle(eW + 8, eH + 2,
                        Color.color(0, 0, 0, 0.90));
                    eBg.setArcWidth(4); eBg.setArcHeight(4);
                    eBg.setX(eX - 4); eBg.setY(eY - eH);
                    eT.setX(eX); eT.setY(eY);
                    overlay.getChildren().addAll(eBg, eT);

                } else if (cell instanceof ConveyorBelt) {
                    String s = "+" + ((ConveyorBelt) cell).getEffect();
                    addBottomText(overlay, s, fSml + 1, Color.web("#00DDFF"), cs);
                } else if (cell instanceof ContaminationSock) {
                    String s = String.valueOf(((ContaminationSock) cell).getEffect());
                    addBottomText(overlay, s, fSml + 1, Color.web("#FF5500"), cs);
                } else if (cell instanceof MonsterCell) {
                    MonsterCell mc = (MonsterCell) cell;
                    if (mc.getCellMonster() != null) {
                        String nm = mc.getCellMonster().getName().split(" ")[0];
                        addBottomText(overlay, nm, fSml, Color.web("#CCEEFF"), cs);
                    }
                } else if (idx == 0) {
                    addCentreText(overlay, "START", fSml, Color.web("#FFD700"), cs);
                } else if (idx == 99) {
                    addCentreText(overlay, "BOO", fSml, Color.web("#FFD700"), cs);
                }

                pane.getChildren().add(overlay);

                // 4. Monster token on top
                boolean playerHere   = (player.getPosition() == idx);
                boolean opponentHere = (opponent.getPosition() == idx);
                if (playerHere || opponentHere) {
                    addTokens(pane, player, opponent, playerHere, opponentHere, tokR);
                }
            }
        }
    }

    /** Draws text centred horizontally at the bottom of the cell. */
    private void addBottomText(javafx.scene.Group g, String s, int fs, Color col, int cs) {
        Text t = new Text(s);
        t.setFont(Font.font("Impact", FontWeight.BOLD, fs));
        t.setFill(col);
        double w = t.getBoundsInLocal().getWidth();
        double h = t.getBoundsInLocal().getHeight();
        double x = (cs - w) / 2.0;
        double y = cs - 5;
        Rectangle bg = new Rectangle(w + 8, h + 2, Color.color(0, 0, 0, 0.85));
        bg.setArcWidth(4); bg.setArcHeight(4);
        bg.setX(x - 4); bg.setY(y - h);
        t.setX(x); t.setY(y);
        g.getChildren().addAll(bg, t);
    }

    /** Draws text centred in the cell. */
    private void addCentreText(javafx.scene.Group g, String s, int fs, Color col, int cs) {
        Text t = new Text(s);
        t.setFont(Font.font("Impact", FontWeight.BOLD, fs));
        t.setFill(col);
        double w = t.getBoundsInLocal().getWidth();
        double h = t.getBoundsInLocal().getHeight();
        double x = (cs - w) / 2.0;
        double y = (cs + h) / 2.0 - 2;
        Rectangle bg = new Rectangle(w + 8, h + 4, Color.color(0, 0, 0, 0.75));
        bg.setArcWidth(4); bg.setArcHeight(4);
        bg.setX(x - 4); bg.setY(y - h);
        t.setX(x); t.setY(y);
        g.getChildren().addAll(bg, t);
    }

    // ---- Cell image layer ----

    private void addCellImage(StackPane pane, Cell cell, int idx, int imgSz, boolean exhausted) {
        String key = null;
        if      (idx == 99)                        key = IMG_BOO;
        else if (cell instanceof CardCell)          key = IMG_CARD;
        else if (cell instanceof ConveyorBelt)      key = IMG_CONVEYOR;
        else if (cell instanceof ContaminationSock) key = IMG_SOCK;
        else if (cell instanceof DoorCell && !exhausted) key = doorImageKey(idx);
        else if (cell instanceof MonsterCell)
            key = monsterKey(((MonsterCell)cell).getCellMonster());

        if (key == null) return;  // Normal cell or door cell: no image

        ImageView view = iv(key, imgSz, imgSz);
        if (view == null) return;

        // Door images: semi-transparent so background color + text overlays remain visible
        view.setOpacity((cell instanceof DoorCell) ? 0.45 : 1.0);
        if (exhausted) {
            ColorAdjust ca = new ColorAdjust();
            ca.setBrightness(-0.6);
            ca.setSaturation(-1.0);
            view.setEffect(ca);
        }
        StackPane.setAlignment(view, Pos.CENTER);
        pane.getChildren().add(view);
    }

    // ---- Monster tokens ----

    private void addTokens(StackPane pane, Monster player, Monster opponent,
                            boolean playerHere, boolean opponentHere, int tokR) {
        if (playerHere && opponentHere) {
            // Both on same cell - offset them
            StackPane pt = buildToken(player, true,  tokR);
            StackPane ot = buildToken(opponent, false, tokR);
            pt.setTranslateX(-tokR * 0.8);
            ot.setTranslateX( tokR * 0.8);
            pane.getChildren().addAll(pt, ot);
        } else if (playerHere) {
            pane.getChildren().add(buildToken(player, true, tokR));
        } else {
            pane.getChildren().add(buildToken(opponent, false, tokR));
        }
    }

    private StackPane buildToken(Monster m, boolean isPlayer, int tokR) {
        boolean frozen   = m.isFrozen();
        boolean confused = m.isConfused();
        boolean isCurrent = (m == model.getCurrent());

        Color borderColor = frozen   ? Color.web("#88CCFF")
                          : confused ? Color.web("#FFAA00")
                          : isPlayer ? Color.web("#00FF88")
                                     : Color.web("#FF6622");

        String imgKey = monsterKey(m);
        Image portrait = imgCache.get(imgKey);

        StackPane tokenStack;
        if (portrait != null) {
            // Larger circular portrait
            ImageView iv = new ImageView(portrait);
            iv.setFitWidth(tokR * 2);
            iv.setFitHeight(tokR * 2);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);
            Circle clip = new Circle(tokR, tokR, tokR);
            iv.setClip(clip);

            Circle ring = new Circle(tokR + 2);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(borderColor);
            ring.setStrokeWidth(3);
            ring.setEffect(new DropShadow(10, borderColor));

            tokenStack = new StackPane(iv, ring);
        } else {
            Circle tok = new Circle(tokR);
            tok.setFill(Color.web(isPlayer ? "#003322" : "#330A00"));
            tok.setStroke(borderColor);
            tok.setStrokeWidth(3);
            tok.setEffect(new DropShadow(8, borderColor));
            Label lbl = new Label(isPlayer ? "P" : "O");
            lbl.setFont(Font.font("Impact", FontWeight.BOLD, tokR));
            lbl.setTextFill(borderColor);
            tokenStack = new StackPane(tok, lbl);
        }

        // Status badges
        if (frozen)  addBadge(tokenStack, "F", Color.web("#88CCFF"), tokR);
        if (confused) addBadge(tokenStack, "?", Color.web("#FFAA00"), tokR);

        // Pulse for active monster
        if (isCurrent) {
            ScaleTransition st = new ScaleTransition(Duration.millis(700), tokenStack);
            st.setFromX(1.0); st.setToX(1.25);
            st.setFromY(1.0); st.setToY(1.25);
            st.setAutoReverse(true); st.setCycleCount(Animation.INDEFINITE);
            st.play();
        }

        StackPane.setAlignment(tokenStack, Pos.CENTER);
        return tokenStack;
    }

    private void addBadge(StackPane stack, String text, Color color, int tokR) {
        Circle badgeBg = new Circle(tokR * 0.4);
        badgeBg.setFill(Color.BLACK);
        badgeBg.setStroke(color);
        badgeBg.setStrokeWidth(1.5);
        Label badgeLbl = new Label(text);
        badgeLbl.setFont(Font.font("Impact", tokR * 0.5));
        badgeLbl.setTextFill(color);
        StackPane badge = new StackPane(badgeBg, badgeLbl);
        StackPane.setAlignment(badge, Pos.BOTTOM_RIGHT);
        stack.getChildren().add(badge);
    }

    // ---- Colour helpers ----

    private Paint cellColor(Cell cell, int idx, boolean exhausted) {
        if (idx == 0)  return Color.web(C_START);
        if (idx == 99) return Color.web(C_WIN);
        if (cell instanceof DoorCell) {
            DoorCell dc = (DoorCell) cell;
            if (exhausted) return Color.web(C_DOOR_EXHAUSTED);
            return Color.web(dc.getRole() == Role.SCARER ? C_SCARER_DOOR : C_LAUGHER_DOOR);
        }
        if (cell instanceof MonsterCell)      return Color.web(C_MONSTER);
        if (cell instanceof CardCell)          return Color.web(C_CARD);
        if (cell instanceof ConveyorBelt)      return Color.web(C_BELT);
        if (cell instanceof ContaminationSock) return Color.web(C_SOCK);
        return Color.web(C_NORMAL);
    }

    /** Returns the card image key given the card name. */
    public String cardImageKey(String cardName) {
        if (cardName == null) return null;
        String n = cardName.toLowerCase();
        if (n.contains("swap"))       return IMG_SWAPPER;
        if (n.contains("start") || n.contains("alert") || n.contains("2319") || n.contains("contamination")) return IMG_STARTOVER;
        if (n.contains("steal") || n.contains("snatch") || n.contains("drain") || n.contains("energy")) return IMG_ESTEAL;
        if (n.contains("shield"))     return IMG_SHIELD;
        if (n.contains("confus") || n.contains("scramble")) return IMG_CONFUSION;
        return IMG_CARD;
    }

    /** Load a card image for external use (e.g. popup). */
    public Image getCardImage(String cardName) {
        return imgCache.get(cardImageKey(cardName));
    }
}
