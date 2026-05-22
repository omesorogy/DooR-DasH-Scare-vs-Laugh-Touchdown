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
import java.util.ArrayList;
import java.util.List;

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
    private static final int BOTTOM_PAD = 22;

    private final StackPane[][] cellPanes = new StackPane[ROWS][COLS];
    private final GameModel model;

    // During smooth movement, hide the engine-updated board token so only the animated token is visible.
    private Monster animatingMonster = null;
    private Monster heldMonster = null;
    private int heldMonsterIndex = -1;

    // While a turn is being animated, keep visual cell state exactly as it was
    // before the engine applied the move. This prevents doors from showing USED,
    // energy labels changing, etc. before the token actually reaches the cell.
    private boolean cellVisualsFrozen = false;
    private final boolean[][] frozenDoorActivated = new boolean[ROWS][COLS];

    // Keep track of animations started by this view so they do not accumulate
    // after repeated refreshes, card effects, or confusion role changes.
    private final List<Animation> activeTokenAnimations = new ArrayList<Animation>();
    private Animation activeMoveAnimation = null;
    private StackPane activeMovingToken = null;

    private static final Map<String, Image> imgCache = new HashMap<>();

    // ---- Hover / conveyor / ambient animation state ----
    private final Map<StackPane, Animation> hoverAnimations = new HashMap<>();
    private final List<Animation> conveyorAnimations = new ArrayList<>();
    private final List<Animation> ambientAnimations  = new ArrayList<>();
    private Timeline screenShakeTimeline = null;
    private boolean hoverSoundCooldown = false;

    // ---- Consistent cell colours ----
    private static final String C_SCARER_DOOR          = "#4A1080";  // deep purple      - scarer doors
    private static final String C_LAUGHER_DOOR         = "#0A5C28";  // deep green        - laugher doors
    private static final String C_DOOR_EXHAUSTED       = "#000000";  // black             - used doors
    private static final String C_SCARER_MONSTER_CELL  = "#7A3B00";  // burnt amber       - scarer monster cells
    private static final String C_LAUGHER_MONSTER_CELL = "#004A5C";  // dark cyan         - laugher monster cells
    private static final String C_CARD                 = "#7A0000";  // dark red          - card cells
    private static final String C_BELT                 = "#003A7A";  // deep steel blue   - conveyor belt (distinct from all greens)
    private static final String C_SOCK                 = "#C04000";  // burnt orange      - contamination sock
    private static final String C_NORMAL               = "#1A1A30";  // dark navy         - normal cells
    private static final String C_START                = "#003830";  // dark teal         - start cell
    private static final String C_WIN                  = "#6B4A00";  // dark gold         - win cell

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

        for (String k : keys) {
            if (!imgCache.containsKey(k)) {
                imgCache.put(k, loadImage(k));
            }
        }
    }

    /** Public accessor so other views (WinScreenView) can share the cache. */
    public static Image getCachedImage(String key) {
        return imgCache.get(key);
    }

    private Image loadImage(String key) {
        String[] exts = {".png", ".jpg", ".jpeg"};

        for (String ext : exts) {
            try (InputStream is = getClass().getResourceAsStream(
                    "/game/gui/resources/images/" + key + ext)) {

                if (is != null) {
                    Image img = new Image(is);
                    if (!img.isError())
                        return img;
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

    private int lastCellSize = -1;

    @Override
    protected void layoutChildren() {
        double w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        double cellW = (w - 2.0*PAD - (COLS-1)*GAP) / COLS;
        double cellH = (h - PAD - BOTTOM_PAD - (ROWS-1)*GAP) / ROWS;
        double cell  = Math.max(30, Math.min(cellW, cellH));
        double gx    = (w - (COLS*cell + (COLS-1)*GAP)) / 2.0;
        double gy    = Math.max(PAD, (h - BOTTOM_PAD - (ROWS*cell + (ROWS-1)*GAP)) / 2.0);
        for (int row = 0; row < ROWS; row++)
            for (int col = 0; col < COLS; col++)
                cellPanes[row][col].resizeRelocate(
                    gx + col*(cell+GAP),
                    gy + (ROWS-1-row)*(cell+GAP),
                    cell, cell);
        int cs = (int) cell;
        // Full cell rebuild only when cell size changed (window resize, first layout).
        // Token-only refresh when state changes during gameplay (much cheaper).
        if (cs != lastCellSize) {
            lastCellSize = cs;
            refreshCells(cs);
        } else {
            refreshTokensOnly(cs);
        }
    }

    public void refresh() { requestLayout(); }

    /** Forces a full cell rebuild regardless of size — call when cell content changes. */
    public void refreshFull() {
        lastCellSize = -1;
        requestLayout();
    }

    // ---- Cell rendering ----

    /**
     * Fast path: only replaces the monster token nodes in each cell.
     * All background/image/overlay nodes are left intact.
     * Called on every refresh() when the cell size has not changed.
     */
    private void refreshTokensOnly(int cs) {
        stopTokenPulseAnimations();
        Monster player   = model.getPlayer();
        Monster opponent = model.getOpponent();
        int tokR = Math.max(10, cs / 5);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                StackPane pane = cellPanes[row][col];
                int idx = model.cellIndexAt(row, col);

                // Remove any existing token nodes (they are always the LAST children)
                // Token nodes are StackPanes; bg, imageview and Group are not StackPane
                pane.getChildren().removeIf(n -> n instanceof StackPane);

                int playerDisplayPosition = (player == heldMonster && heldMonsterIndex >= 0)
                    ? heldMonsterIndex : player.getPosition();
                int opponentDisplayPosition = (opponent == heldMonster && heldMonsterIndex >= 0)
                    ? heldMonsterIndex : opponent.getPosition();

                boolean playerHere   = (player   != animatingMonster && playerDisplayPosition   == idx);
                boolean opponentHere = (opponent != animatingMonster && opponentDisplayPosition == idx);
                if (playerHere || opponentHere) {
                    addTokens(pane, player, opponent, playerHere, opponentHere, tokR);
                }
            }
        }
    }

    private void refreshCells(int cs) {
        stopTokenPulseAnimations();
        stopConveyorAnimations();
        stopAmbientAnimations();

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
                boolean exhausted = (cell instanceof DoorCell) &&
                    (cellVisualsFrozen ? frozenDoorActivated[row][col] : ((DoorCell)cell).isActivated());
                Rectangle bg = new Rectangle(cs - 2, cs - 2);
                bg.setArcWidth(7); bg.setArcHeight(7);
                bg.setFill(cellColor(cell, idx, exhausted));
                bg.setStroke(Color.web(exhausted ? "#444444" : "#3A3A60"));
                bg.setStrokeWidth(1.5);
                pane.getChildren().add(bg);

                // Hover scale + glow + sound
                attachHoverEffect(pane, bg, cell instanceof ConveyorBelt);

                // Conveyor belt opacity pulse
                if (cell instanceof ConveyorBelt) {
                    animateConveyorCell(pane);
                }

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
                    Monster stationed = mc.getCellMonster();
                    if (stationed != null) {
                        // Top-right: role tag (SCARER / LAUGHER) same design as door cells
                        String typeStr = (stationed.getRole() == Role.SCARER) ? "SCR" : "LGH";
                        Color typeCol  = (stationed.getRole() == Role.SCARER)
                            ? Color.web("#CC88FF") : Color.web("#88FFCC");
                        Text typeT = new Text(typeStr);
                        typeT.setFont(Font.font("Impact", fSml));
                        typeT.setFill(typeCol);
                        double tW = typeT.getBoundsInLocal().getWidth();
                        double tH = typeT.getBoundsInLocal().getHeight();
                        Rectangle typeBg = new Rectangle(tW + 6, tH + 2, Color.color(0, 0, 0, 0.85));
                        typeBg.setArcWidth(4); typeBg.setArcHeight(4);
                        typeBg.setX(cs - tW - 8); typeBg.setY(2);
                        typeT.setX(cs - tW - 5); typeT.setY(2 + tH - 2);
                        overlay.getChildren().addAll(typeBg, typeT);

                        // Bottom-centre: monster energy (same design as door energy)
                        String eStr = "E:" + stationed.getEnergy();
                        Color eCol = Color.web("#FFD700");
                        Text eT = new Text(eStr);
                        eT.setFont(Font.font("Impact", FontWeight.BOLD, fSml + 2));
                        eT.setFill(eCol);
                        double eW = eT.getBoundsInLocal().getWidth();
                        double eH = eT.getBoundsInLocal().getHeight();
                        double eX = (cs - eW) / 2.0;
                        double eY = cs - 5;
                        Rectangle eBg = new Rectangle(eW + 8, eH + 2, Color.color(0, 0, 0, 0.90));
                        eBg.setArcWidth(4); eBg.setArcHeight(4);
                        eBg.setX(eX - 4); eBg.setY(eY - eH);
                        eT.setX(eX); eT.setY(eY);
                        overlay.getChildren().addAll(eBg, eT);

                        // Monster first name - small text above energy
                        String nm = stationed.getName().split(" ")[0];
                        Text nmT = new Text(nm);
                        nmT.setFont(Font.font("Impact", fSml - 1));
                        nmT.setFill(Color.web("#CCEEFF"));
                        double nmW = nmT.getBoundsInLocal().getWidth();
                        double nmH = nmT.getBoundsInLocal().getHeight();
                        double nmX = (cs - nmW) / 2.0;
                        double nmY = eY - eH - 2;
                        Rectangle nmBg = new Rectangle(nmW + 6, nmH + 2, Color.color(0, 0, 0, 0.80));
                        nmBg.setArcWidth(3); nmBg.setArcHeight(3);
                        nmBg.setX(nmX - 3); nmBg.setY(nmY - nmH);
                        nmT.setX(nmX); nmT.setY(nmY);
                        overlay.getChildren().addAll(nmBg, nmT);
                    }
                } else if (idx == 0) {
                    addCentreText(overlay, "START", fSml, Color.web("#FFD700"), cs);
                } else if (idx == 99) {
                    addCentreText(overlay, "BOO", fSml, Color.web("#FFD700"), cs);
                }

                pane.getChildren().add(overlay);

                // 4. Monster token on top
                // If a smooth movement animation is running, skip drawing that same monster
                // from the refreshed board state. This prevents duplicate icons when cards,
                // conveyor belts, or socks change the final position before the animation ends.
                int playerDisplayPosition = (player == heldMonster && heldMonsterIndex >= 0)
                    ? heldMonsterIndex : player.getPosition();
                int opponentDisplayPosition = (opponent == heldMonster && heldMonsterIndex >= 0)
                    ? heldMonsterIndex : opponent.getPosition();

                boolean playerHere   = (player != animatingMonster && playerDisplayPosition == idx);
                boolean opponentHere = (opponent != animatingMonster && opponentDisplayPosition == idx);
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

        // Ambient door breathe animation
        if (cell instanceof DoorCell && !exhausted) {
            attachAmbientDoorAnimation(view, idx);
        }
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

    private void stopTokenPulseAnimations() {
        for (Animation a : activeTokenAnimations) {
            if (a != null) a.stop();
        }
        activeTokenAnimations.clear();
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
            // Circular portrait token. Use the image as the fill of a circle instead
            // of clipping an ImageView. This keeps the portrait fully contained in
            // the glowing ring during both normal display and smooth movement.
            double ringRadius = tokR + 2;
            double portraitRadius = tokR * 0.86;

            Circle portraitCircle = new Circle(portraitRadius);
            portraitCircle.setFill(new ImagePattern(portrait));
            portraitCircle.setMouseTransparent(true);

            Circle background = new Circle(ringRadius);
            background.setFill(Color.web("#050505"));
            background.setOpacity(0.65);

            Circle ring = new Circle(ringRadius);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(borderColor);
            ring.setStrokeWidth(3);
            ring.setEffect(new DropShadow(10, borderColor));

            tokenStack = new StackPane(background, portraitCircle, ring);
            StackPane.setAlignment(background, Pos.CENTER);
            StackPane.setAlignment(portraitCircle, Pos.CENTER);
            StackPane.setAlignment(ring, Pos.CENTER);
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
            activeTokenAnimations.add(st);
            st.play();
        }

        // Monster personality idle animations
        attachMonsterPersonality(tokenStack, m);

        double preferredSize = (tokR + 4) * 2.0;
        tokenStack.setMinSize(preferredSize, preferredSize);
        tokenStack.setPrefSize(preferredSize, preferredSize);
        tokenStack.setMaxSize(preferredSize, preferredSize);
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
        if (cell instanceof MonsterCell) {
            MonsterCell mc = (MonsterCell) cell;
            if (mc.getCellMonster() != null && mc.getCellMonster().getRole() == Role.SCARER)
                return Color.web(C_SCARER_MONSTER_CELL);
            return Color.web(C_LAUGHER_MONSTER_CELL);
        }
        if (cell instanceof CardCell)          return Color.web(C_CARD);
        if (cell instanceof ConveyorBelt)      return Color.web(C_BELT);
        if (cell instanceof ContaminationSock) return Color.web(C_SOCK);
        return Color.web(C_NORMAL);
    }

    /** Returns a short type label for a stationed monster. */
    private String monsterTypeLetter(Monster m) {
        if (m instanceof Dasher)      return "DSH";
        if (m instanceof Dynamo)      return "DYN";
        if (m instanceof MultiTasker) return "MLT";
        if (m instanceof Schemer)     return "SCH";
        return "MON";
    }



    /**
     * Freezes cell visual state before the engine applies a move.
     * Call this before playTurn(), then unfreeze only after smooth movement finishes.
     */
    public void freezeCellVisualState() {
        Cell[][] board = model.getBoardCells();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Cell cell = board[row][col];
                frozenDoorActivated[row][col] =
                    (cell instanceof DoorCell) && ((DoorCell) cell).isActivated();
            }
        }
        cellVisualsFrozen = true;
    }

    public void unfreezeCellVisualState() {
        cellVisualsFrozen = false;
    }

    /**
     * Temporarily keeps a monster visually at its old cell while dice animation is playing.
     * The engine may already have updated the real position, but the board should not reveal
     * that destination before the smooth movement begins.
     */
    public void holdMonsterAt(Monster monster, int index) {
        heldMonster = monster;
        heldMonsterIndex = Math.max(0, Math.min(99, index));
        refresh();
    }

    public void clearHeldMonster() {
        heldMonster = null;
        heldMonsterIndex = -1;
        refresh();
    }

    /**
     * Animates a monster token smoothly from one cell to another, then refreshes the board.
     * This is GUI-only; it does not change any engine state.
     */
    public void animateMonsterMove(Monster monster, int fromIndex, int toIndex, int lastRoll, Runnable onFinished) {
        if (monster == heldMonster) {
            heldMonster = null;
            heldMonsterIndex = -1;
        }

        // Never allow two movement animations to run on top of each other.
        if (activeMoveAnimation != null) {
            activeMoveAnimation.stop();
            activeMoveAnimation = null;
        }
        if (activeMovingToken != null) {
            getChildren().remove(activeMovingToken);
            activeMovingToken = null;
        }
        animatingMonster = null;

        if (monster == null || fromIndex == toIndex || getWidth() <= 0 || getHeight() <= 0) {
            refresh();
            if (onFinished != null) onFinished.run();
            return;
        }

        animatingMonster = monster;
        applyCss();
        layout();

        // Detect if the dice-roll landing cell is a ConveyorBelt.
        // The engine already applied the belt's effect so toIndex is the post-belt position.
        // beltLandIndex is where the dice roll actually lands (pre-belt), beltEffect is the
        // number of extra cells the belt carries the monster.
        int beltLandIndex = -1;
        int beltEffect    = 0;
        {
            // Find the belt cell: it is at fromIndex + actualSteps (before belt carry).
            // We use actualSteps because MultiTaskers move half the dice roll.
            Cell[][] board = model.getBoardCells();
            int diceTarget = Math.max(0, Math.min(99, fromIndex + lastRoll));
            int row = model.indexToRow(diceTarget);
            int col = model.indexToCol(diceTarget);
            if (row >= 0 && col >= 0) {
                Cell landCell = board[row][col];
                if (landCell instanceof ConveyorBelt && diceTarget != toIndex) {
                    beltLandIndex = diceTarget;
                    beltEffect    = ((ConveyorBelt) landCell).getEffect();
                }
            }
        }

        Point2D start = cellCenter(fromIndex);
        if (start == null) {
            animatingMonster = null;
            refresh();
            if (onFinished != null) onFinished.run();
            return;
        }

        StackPane movingToken = buildToken(monster, monster == model.getPlayer(), Math.max(12, (int)(cellPanes[0][0].getWidth() / 5.0)));
        double tokenSize = Math.max(26, cellPanes[0][0].getWidth() * 0.55);
        movingToken.setManaged(false);
        movingToken.resizeRelocate(start.getX() - tokenSize / 2.0, start.getY() - tokenSize / 2.0, tokenSize, tokenSize);
        movingToken.setMouseTransparent(true);
        getChildren().add(movingToken);
        activeMovingToken = movingToken;

        // --- Build the two-leg or single-leg path ---
        SequentialTransition sequence = new SequentialTransition();

        if (beltLandIndex >= 0 && beltLandIndex != toIndex) {
            // Leg 1: dice roll movement to the belt cell
            java.util.List<Integer> leg1 = buildMovementPath(fromIndex, beltLandIndex, lastRoll);
            for (int i = 1; i < leg1.size(); i++) {
                Point2D target = cellCenter(leg1.get(i));
                if (target == null) continue;
                TranslateTransition step = new TranslateTransition(Duration.millis(95), movingToken);
                step.setToX(target.getX() - start.getX());
                step.setToY(target.getY() - start.getY());
                step.setInterpolator(Interpolator.EASE_BOTH);
                sequence.getChildren().add(step);
            }

            // Brief pause on the belt so the player can see it activate
            sequence.getChildren().add(new PauseTransition(Duration.millis(220)));

            // Leg 2: belt carries the monster to the final cell
            java.util.List<Integer> leg2 = buildMovementPath(beltLandIndex, toIndex, Math.abs(beltEffect));
            for (int i = 1; i < leg2.size(); i++) {
                Point2D target = cellCenter(leg2.get(i));
                if (target == null) continue;
                // Slightly faster steps for the belt carry to feel distinct
                TranslateTransition step = new TranslateTransition(Duration.millis(75), movingToken);
                step.setToX(target.getX() - start.getX());
                step.setToY(target.getY() - start.getY());
                step.setInterpolator(Interpolator.LINEAR);
                sequence.getChildren().add(step);
            }
        } else {
            // Normal single-leg movement (no conveyor, or belt already at toIndex)
            java.util.List<Integer> path = buildMovementPath(fromIndex, toIndex, lastRoll);
            for (int i = 1; i < path.size(); i++) {
                Point2D target = cellCenter(path.get(i));
                if (target == null) continue;
                TranslateTransition step = new TranslateTransition(Duration.millis(95), movingToken);
                step.setToX(target.getX() - start.getX());
                step.setToY(target.getY() - start.getY());
                step.setInterpolator(Interpolator.EASE_BOTH);
                sequence.getChildren().add(step);
            }
        }

        sequence.setOnFinished(e -> {
            getChildren().remove(movingToken);
            if (activeMovingToken == movingToken) activeMovingToken = null;
            if (activeMoveAnimation == sequence) activeMoveAnimation = null;
            animatingMonster = null;
            refresh();
            if (onFinished != null) onFinished.run();
        });
        if (sequence.getChildren().isEmpty()) {
            getChildren().remove(movingToken);
            if (activeMovingToken == movingToken) activeMovingToken = null;
            animatingMonster = null;
            refresh();
            if (onFinished != null) onFinished.run();
            return;
        }
        activeMoveAnimation = sequence;
        sequence.play();
    }

    private java.util.List<Integer> buildMovementPath(int fromIndex, int toIndex, int actualSteps) {
        java.util.ArrayList<Integer> path = new java.util.ArrayList<Integer>();
        fromIndex = Math.max(0, Math.min(99, fromIndex));
        toIndex   = Math.max(0, Math.min(99, toIndex));
        path.add(fromIndex);
        if (fromIndex == toIndex) return path;

        // Determine direction from the actual steps and positions.
        // Forward: toIndex > fromIndex, OR it's a wrap (toIndex < fromIndex but steps > gap).
        // Backward: toIndex < fromIndex AND steps <= gap (contamination sock / start-over).
        boolean isForward;
        if (toIndex > fromIndex) {
            isForward = true;
        } else if (toIndex < fromIndex) {
            // Distinguish wrap-forward from backward movement:
            // If going forward and wrapping: distance going forward = (100-from)+to
            // If going backward: distance = from-to
            // Use whichever matches actualSteps more closely.
            int forwardDist  = (100 - fromIndex) + toIndex;
            int backwardDist = fromIndex - toIndex;
            isForward = (Math.abs(actualSteps - forwardDist) <= Math.abs(actualSteps - backwardDist));
        } else {
            return path; // same cell
        }

        int idx = fromIndex;
        int guard = 0;
        while (idx != toIndex && guard < 110) {
            if (isForward) {
                idx = (idx + 1) % 100;
            } else {
                idx = idx - 1;
                if (idx < 0) idx = 0;
            }
            path.add(idx);
            guard++;
        }
        return path;
    }

    private Point2D cellCenter(int index) {
        int row = model.indexToRow(index);
        int col = model.indexToCol(index);
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return null;
        StackPane cell = cellPanes[row][col];
        return new Point2D(cell.getLayoutX() + cell.getWidth() / 2.0,
                           cell.getLayoutY() + cell.getHeight() / 2.0);
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

    // =========================================================================
    //  SCREEN SHAKE
    // =========================================================================

    /**
     * Shakes the entire board briefly.  Call from GameController when the monster
     * takes a hit, loses energy, or a contamination sock activates.
     */
    public void screenShake() {
        if (screenShakeTimeline != null) {
            screenShakeTimeline.stop();
            setTranslateX(0);
            setTranslateY(0);
        }
        double[] xs = {  6, -6,  5, -5,  3, -3,  1, -1, 0 };
        double[] ys = { -4,  4, -3,  3, -2,  2, -1,  1, 0 };
        screenShakeTimeline = new Timeline();
        for (int i = 0; i < xs.length; i++) {
            final double tx = xs[i], ty = ys[i];
            screenShakeTimeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * 45), e -> {
                    setTranslateX(tx);
                    setTranslateY(ty);
                }));
        }
        screenShakeTimeline.setOnFinished(e -> { setTranslateX(0); setTranslateY(0); });
        screenShakeTimeline.play();
    }

    // =========================================================================
    //  PARTICLE EFFECTS
    // =========================================================================

    /**
     * Spawns small coloured circles that fly outward from the given cell index,
     * fade out, then remove themselves.  Works great for teleport, shield-glow, etc.
     */
    public void spawnParticles(int cellIndex, Color color, int count) {
        int row = model.indexToRow(cellIndex);
        int col = model.indexToCol(cellIndex);
        if (row < 0 || col < 0) return;
        StackPane cell = cellPanes[row][col];
        Point2D centre = new Point2D(cell.getLayoutX() + cell.getWidth() / 2.0,
                                     cell.getLayoutY() + cell.getHeight() / 2.0);
        java.util.Random rng = new java.util.Random();
        for (int i = 0; i < count; i++) {
            Circle p = new Circle(3 + rng.nextDouble() * 3, color);
            p.setManaged(false);
            p.setOpacity(0.9);
            double startX = centre.getX() - 4 + rng.nextDouble() * 8;
            double startY = centre.getY() - 4 + rng.nextDouble() * 8;
            p.relocate(startX, startY);
            getChildren().add(p);

            double dx = (rng.nextDouble() - 0.5) * 60;
            double dy = (rng.nextDouble() - 0.5) * 60;
            double delay = i * 30;

            TranslateTransition move = new TranslateTransition(Duration.millis(550), p);
            move.setByX(dx); move.setByY(dy);
            move.setDelay(Duration.millis(delay));

            FadeTransition fade = new FadeTransition(Duration.millis(550), p);
            fade.setToValue(0);
            fade.setDelay(Duration.millis(delay));

            ParallelTransition pt = new ParallelTransition(move, fade);
            pt.setOnFinished(e -> getChildren().remove(p));
            pt.play();
        }
    }

    // =========================================================================
    //  HOVER ANIMATIONS (called in refreshCells)
    // =========================================================================

    private void attachHoverEffect(StackPane pane, Rectangle bg, boolean isConveyor) {
        // Store original stroke so we can restore it
        String normalStroke  = "#3A3A60";
        String hoverStroke   = isConveyor ? "#00DDFF" : "#FFD700";
        double normalWidth   = 1.5;
        double hoverWidth    = 2.5;

        pane.setOnMouseEntered(e -> {
            pane.setScaleX(1.08);
            pane.setScaleY(1.08);
            bg.setStroke(Color.web(hoverStroke));
            bg.setStrokeWidth(hoverWidth);
            bg.setEffect(new DropShadow(12, Color.web(hoverStroke, 0.7)));

            // Throttled hover sound
            if (!hoverSoundCooldown) {
                game.gui.SoundManager.get().playCellHover();
                hoverSoundCooldown = true;
                PauseTransition cooldown = new PauseTransition(Duration.millis(180));
                cooldown.setOnFinished(ev -> hoverSoundCooldown = false);
                cooldown.play();
            }
        });
        pane.setOnMouseExited(e -> {
            pane.setScaleX(1.0);
            pane.setScaleY(1.0);
            bg.setStroke(Color.web(normalStroke));
            bg.setStrokeWidth(normalWidth);
            bg.setEffect(null);
        });
    }

    // =========================================================================
    //  CONVEYOR BELT PULSE ANIMATION
    // =========================================================================

    private void animateConveyorCell(StackPane pane) {
        FadeTransition ft = new FadeTransition(Duration.millis(900), pane);
        ft.setFromValue(0.80);
        ft.setToValue(1.0);
        ft.setAutoReverse(true);
        ft.setCycleCount(Animation.INDEFINITE);
        conveyorAnimations.add(ft);
        ft.play();
    }

    private void stopConveyorAnimations() {
        for (Animation a : conveyorAnimations) a.stop();
        conveyorAnimations.clear();
    }

    // =========================================================================
    //  AMBIENT DOOR MOTION
    // =========================================================================

    /**
     * Gives door-cell images a gentle opacity breathe so the board looks alive.
     */
    private void attachAmbientDoorAnimation(ImageView iv, int seed) {
        // Only animate every third door to reduce the number of simultaneous animations.
        if (seed % 3 != 0) return;
        double delay = (seed % 6) * 150;
        FadeTransition ft = new FadeTransition(Duration.millis(1600 + seed * 60), iv);
        ft.setFromValue(0.35);
        ft.setToValue(0.52);
        ft.setAutoReverse(true);
        ft.setCycleCount(Animation.INDEFINITE);
        ft.setDelay(Duration.millis(delay));
        ambientAnimations.add(ft);
        ft.play();
    }

    private void stopAmbientAnimations() {
        for (Animation a : ambientAnimations) a.stop();
        ambientAnimations.clear();
    }

    // =========================================================================
    //  MONSTER PERSONALITY IDLE ANIMATIONS
    // =========================================================================

    /**
     * Adds a subtle idle animation to a monster token based on which monster it is.
     * Boo bounces, Randall fades, Roz barely moves (blink), Yeti stomps.
     */
    private void attachMonsterPersonality(StackPane token, Monster m) {
        // Personality animations disabled during active gameplay to keep framerate smooth.
        // They are re-enabled only for the active (current-turn) token which already
        // has the pulse animation. Adding more INDEFINITE animations per token causes
        // heavy CPU usage with two monsters and 6 stationed monsters all running at once.
    }
}
