package defpackage;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.*;

/* JADX INFO: loaded from: Dolinablox.jar:Dolinablox.class */
public class Dolinablox extends JPanel implements Runnable, KeyListener, MouseListener, MouseWheelListener, MouseMotionListener {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean keyW;
    private boolean keyA;
    private boolean keyS;
    private boolean keyD;
    private boolean keySpace;
    private JTextField authNameField;
    private JPasswordField authPassField;
    private JLabel authStatusLabel;
    private JTextField nameField;
    private JTextField newWorldField;
    private JTextField searchField;
    private JList<String> worldList;
    private JButton joinBtn;
    private JButton createBtn;
    private JButton refreshBtn;
    private JButton editorBtn;
    private JButton headBtn;
    private JButton bodyBtn;
    private JButton shirtBtn;
    private JButton faceBtn;
    private State currentState = State.AUTH;
    private String username = "";
    private String serverIP = "26.189.202.2";
    private String currentWorldName = "";
    private String currentWorldOwner = "Unknown";
    private final ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();
    private DefaultListModel<String> worldListModel = new DefaultListModel<>();
    private List<String> rawWorlds = new ArrayList();
    private String screenMessage = "";
    private long screenMessageUntil = 0;
    private String currentChat = "";
    private boolean isChatting = false;
    private boolean isOffline = false;
    private boolean hideNames = false;
    private final float GRAVITY = 0.8f;
    private final float JUMP_FORCE_DEFAULT = -15.0f;
    private final List<Component> authComps = new ArrayList();
    private final List<Component> menuComps = new ArrayList();
    private String pendingAuthUsername = "";
    private String pendingAuthPassword = "";
    private String savedUsername = "";
    private String savedPassword = "";
    private final File gameDataDir = new File("GameData");
    private final File cacheDir = new File(this.gameDataDir, "cache");
    private final File sessionFile = new File(this.gameDataDir, "session.properties");
    private final File skinFile = new File(this.gameDataDir, "skin.properties");
    private Color headColor = new Color(245, 205, 48);
    private Color bodyColor = new Color(163, 162, 165);
    private String shirtUrl = "none";
    private String faceUrl = "none";
    private String skyUrl = "none";
    private String musicUrl = "none";
    private Clip musicClip = null;
    private String currentMusicUrl = "";
    private final ConcurrentHashMap<String, Image> cachedImages = new ConcurrentHashMap<>();
    private final Set<String> loadingUrls = Collections.newSetFromMap(new ConcurrentHashMap());
    private final ExecutorService loaderService = Executors.newFixedThreadPool(2);
    private List<MapObject> editorObjects = new CopyOnWriteArrayList();
    private MapObject selectedObject = null;
    private Color selectedColor = Color.RED;
    private String selectedShape = "CUBE";
    private String selectedTexture = "none";
    private boolean isStatic = true;
    private boolean hasCollider = true;
    private boolean isKill = false;
    private String selectedNpcType = "none";
    private float camX = 0.0f;
    private float camY = 0.0f;
    private float zoom = 1.0f;
    private int mouseX = 0;
    private int mouseY = 0;
    private boolean showDebug = false;
    private int fps = 0;
    private int frames = 0;
    private long lastFpsTime = 0;
    private long ping = 0;
    private long lastPingSend = 0;
    private int totalPlayersGlobal = 0;
    private volatile boolean running = true;
    private boolean mousePaintLeft = false;
    private boolean mousePaintRight = false;
    private boolean fixedCamera = false;
    private float fixedCamX = 0.0f;
    private float fixedCamY = 0.0f;
    private final ConcurrentHashMap<String, Double> scriptVars = new ConcurrentHashMap<>();
    private boolean lightingEnabled = false;
    private List<Light> lights = new CopyOnWriteArrayList();
    private float globalAmbient = 0.5f;
    private String heldItem = "none";
    private final ConcurrentHashMap<String, Item> items = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> playerItems = new ConcurrentHashMap<>();
    private boolean isAfk = false;
    private boolean hideHead = false;
    private boolean commandsDisabled = false;
    private boolean gravityDisabled = false;
    private Player localPlayer = new Player("", 400.0f, 300.0f);

    /* JADX INFO: loaded from: Dolinablox.jar:Dolinablox$State.class */
    private enum State {
        AUTH,
        MENU,
        GAME,
        EDITOR
    }

    public Dolinablox() {
        setPreferredSize(new Dimension(1000, 700));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        setLayout(null);
        ensureGameDataDir();
        loadSkinData();
        loadSessionData();
        initItems();
        initAuth();
        initMenu();
        ArrayList<Component> arrayList = new ArrayList();
        arrayList.addAll(this.authComps);
        arrayList.addAll(this.menuComps);
        for (Component component : arrayList) {
            if (!(component instanceof JButton))
                continue;
            ((JButton)component).addActionListener(paramActionEvent -> playSound("button.wav"));
        }
        showState(State.AUTH);
        customCursor();
        new Thread(this).start();
        connectToMaster();
    }

    private void initItems() {
        this.items.put("pruzina", new Item("pruzina", "/sprites/pruzina.png", 1.5f, 0.0f));
    }

    private void customCursor() {
        try {
            InputStream resourceAsStream = getClass().getResourceAsStream("/sprites/cursor.png");
            if (resourceAsStream != null) {
                setCursor(Toolkit.getDefaultToolkit().createCustomCursor(ImageIO.read(resourceAsStream), new Point(0, 0), "customCursor"));
            }
        } catch (Exception e) {
            System.out.println("cursor doesn't downloaded because " + e.getMessage());
        }
    }

    private void playSound(String str) {
        new Thread(() -> {
            AudioInputStream audioInputStream;
            try {
                InputStream resourceAsStream = getClass().getResourceAsStream("/sounds/" + str);
                if (resourceAsStream != null) {
                    audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(resourceAsStream));
                } else {
                    File file = new File("sounds/" + str);
                    if (!file.exists()) {
                        return;
                    } else {
                        audioInputStream = AudioSystem.getAudioInputStream(file);
                    }
                }
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
                clip.addLineListener(lineEvent -> {
                    if (lineEvent.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {
                System.out.println("Error (" + str + "): " + e.getMessage());
            }
        }).start();
    }

    private void playMusicFromUrl(String str) {
        if (str == null || str.equals("none") || str.isEmpty()) {
            stopMusic();
            return;
        }
        if (str.equals(this.currentMusicUrl) && this.musicClip != null && this.musicClip.isRunning()) {
            return;
        }
        stopMusic();
        this.currentMusicUrl = str;
        new Thread(() -> {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(new URL(str).openStream()));
                AudioFormat format = audioInputStream.getFormat();
                AudioInputStream audioInputStream2 = AudioSystem.getAudioInputStream(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), 16, format.getChannels(), format.getChannels() * 2, format.getSampleRate(), false), audioInputStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream2);
                clip.loop(-1);
                clip.start();
                this.musicClip = clip;
            } catch (Exception e) {
                System.out.println("Music error (" + str + "): " + e.getMessage());
            }
        }).start();
    }

    private void stopMusic() {
        if (this.musicClip != null) {
            try {
                this.musicClip.stop();
                this.musicClip.close();
            } catch (Exception e) {
            }
            this.musicClip = null;
        }
        this.currentMusicUrl = "";
    }

    private void ensureGameDataDir() {
        if (!this.gameDataDir.exists()) {
            this.gameDataDir.mkdirs();
        }
        if (!this.cacheDir.exists()) {
            this.cacheDir.mkdirs();
        }
    }

    private int parseIntSafe(String str, int i) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return i;
        }
    }

    private Color parseColorSafe(String str, Color color) {
        try {
            return new Color(Integer.parseInt(str), true);
        } catch (Exception e) {
            return color;
        }
    }

    private String encodePassword(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    private String decodePassword(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        try {
            return new String(Base64.getDecoder().decode(str), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private void loadSessionData() {
        Properties properties = new Properties();
        if (this.sessionFile.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(this.sessionFile);
                Throwable th = null;
                try {
                    try {
                        properties.load(fileInputStream);
                        if (fileInputStream != null) {
                            if (0 != 0) {
                                try {
                                    fileInputStream.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            } else {
                                fileInputStream.close();
                            }
                        }
                    } finally {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th3;
                }
            } catch (Exception e) {
            }
        }
        this.savedUsername = properties.getProperty("username", "");
        this.savedPassword = decodePassword(properties.getProperty("password", ""));
        this.username = this.savedUsername;
        this.pendingAuthUsername = this.savedUsername;
        if (this.localPlayer != null) {
            this.localPlayer.name = this.username;
        }
    }

    private void saveSessionData(String str, String str2) {
        ensureGameDataDir();
        Properties properties = new Properties();
        properties.setProperty("username", str == null ? "" : str);
        properties.setProperty("password", encodePassword(str2));
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(this.sessionFile);
            Throwable th = null;
            try {
                try {
                    properties.store(fileOutputStream, (String) null);
                    if (fileOutputStream != null) {
                        if (0 != 0) {
                            try {
                                fileOutputStream.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        } else {
                            fileOutputStream.close();
                        }
                    }
                } finally {
                }
            } finally {
            }
        } catch (Exception e) {
        }
        this.savedUsername = str == null ? "" : str;
        this.savedPassword = str2 == null ? "" : str2;
    }

    private void loadSkinData() {
        Properties properties = new Properties();
        if (this.skinFile.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(this.skinFile);
                Throwable th = null;
                try {
                    try {
                        properties.load(fileInputStream);
                        if (fileInputStream != null) {
                            if (0 != 0) {
                                try {
                                    fileInputStream.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            } else {
                                fileInputStream.close();
                            }
                        }
                    } finally {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th3;
                }
            } catch (Exception e) {
            }
        }
        this.headColor = parseColorSafe(properties.getProperty("headRGB", String.valueOf(this.headColor.getRGB())), this.headColor);
        this.bodyColor = parseColorSafe(properties.getProperty("bodyRGB", String.valueOf(this.bodyColor.getRGB())), this.bodyColor);
        this.shirtUrl = properties.getProperty("shirtUrl", this.shirtUrl);
        this.faceUrl = properties.getProperty("faceUrl", this.faceUrl);
        this.skyUrl = properties.getProperty("skyUrl", this.skyUrl);
        if (this.localPlayer != null) {
            this.localPlayer.hRGB = this.headColor.getRGB();
            this.localPlayer.bRGB = this.bodyColor.getRGB();
            this.localPlayer.sUrl = this.shirtUrl;
            this.localPlayer.fUrl = this.faceUrl;
        }
    }

    private void saveSkinData() {
        ensureGameDataDir();
        Properties properties = new Properties();
        properties.setProperty("headRGB", String.valueOf(this.headColor.getRGB()));
        properties.setProperty("bodyRGB", String.valueOf(this.bodyColor.getRGB()));
        properties.setProperty("shirtUrl", this.shirtUrl == null ? "none" : this.shirtUrl);
        properties.setProperty("faceUrl", this.faceUrl == null ? "none" : this.faceUrl);
        properties.setProperty("skyUrl", this.skyUrl == null ? "none" : this.skyUrl);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(this.skinFile);
            Throwable th = null;
            try {
                properties.store(fileOutputStream, (String) null);
                if (fileOutputStream != null) {
                    if (0 != 0) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    } else {
                        fileOutputStream.close();
                    }
                }
            } finally {
            }
        } catch (Exception e) {
        }
    }

    private void tryAutoLogin() {
        if (this.out == null || this.savedUsername == null || this.savedUsername.isEmpty() || this.savedPassword == null || this.savedPassword.isEmpty()) {
            return;
        }
        this.pendingAuthUsername = this.savedUsername;
        this.pendingAuthPassword = this.savedPassword;
        SwingUtilities.invokeLater(() -> {
            if (this.authNameField != null) {
                this.authNameField.setText(this.savedUsername);
            }
            if (this.authPassField != null) {
                this.authPassField.setText(this.savedPassword);
            }
            if (this.authStatusLabel != null) {
                this.authStatusLabel.setText("Autologin...");
            }
        });
        this.out.println("LOGIN " + this.savedUsername + " " + this.savedPassword);
    }

    private void addAuth(Component component) {
        this.authComps.add(component);
        add(component);
        component.setVisible(false);
    }

    private void addMenu(Component component) {
        this.menuComps.add(component);
        add(component);
        component.setVisible(false);
    }

    private void showState(State state) {
        this.currentState = state;
        Iterator<Component> it = this.authComps.iterator();
        while (it.hasNext()) {
            it.next().setVisible(state == State.AUTH);
        }
        Iterator<Component> it2 = this.menuComps.iterator();
        while (it2.hasNext()) {
            it2.next().setVisible(state == State.MENU);
        }
    }

    private void showScreenMessage(String str, int i) {
        this.screenMessage = str;
        this.screenMessageUntil = System.currentTimeMillis() + ((long) i);
    }

    private void initAuth() {
        JLabel jLabel = new JLabel("Log in / Sign up", 0);
        jLabel.setBounds(270, 240, 460, 25);
        jLabel.setFont(new Font("Tahoma", 1, 16));
        jLabel.setForeground(Color.WHITE);
        JLabel jLabel2 = new JLabel("Username:");
        jLabel2.setBounds(285, 287, 100, 25);
        jLabel2.setForeground(Color.WHITE);
        this.authNameField = new JTextField();
        this.authNameField.setBounds(385, 284, 245, 28);
        JLabel jLabel3 = new JLabel("Password:");
        jLabel3.setBounds(285, 325, 100, 25);
        jLabel3.setForeground(Color.WHITE);
        this.authPassField = new JPasswordField();
        this.authPassField.setBounds(385, 322, 245, 28);
        JButton jButton = new JButton("Log in");
        jButton.setBounds(385, 364, 112, 30);
        jButton.addActionListener(actionEvent -> {
            doLogin();
        });
        JButton jButton2 = new JButton("Sign up");
        jButton2.setBounds(510, 364, 120, 30);
        jButton2.addActionListener(actionEvent2 -> {
            doRegister();
        });
        this.authStatusLabel = new JLabel("", 0);
        this.authStatusLabel.setBounds(270, 408, 460, 25);
        this.authStatusLabel.setForeground(new Color(255, 220, 100));
        addAuth(jLabel);
        addAuth(jLabel2);
        addAuth(this.authNameField);
        addAuth(jLabel3);
        addAuth(this.authPassField);
        addAuth(jButton);
        addAuth(jButton2);
        addAuth(this.authStatusLabel);
        if (!this.username.isEmpty()) {
            this.authNameField.setText(this.username);
        }
        if (!this.savedPassword.isEmpty()) {
            this.authPassField.setText(this.savedPassword);
        }
    }

    private void doLogin() {
        String strTrim = this.authNameField.getText().trim();
        String str = new String(this.authPassField.getPassword());
        if (strTrim.isEmpty() || str.isEmpty()) {
            this.authStatusLabel.setText("Type your username and password.");
            return;
        }
        this.pendingAuthUsername = strTrim;
        this.pendingAuthPassword = str;
        if (this.out != null) {
            this.out.println("LOGIN " + strTrim + " " + str);
            this.authStatusLabel.setText("Connecting...");
        } else {
            this.authStatusLabel.setText("Not connected to server.");
        }
    }

    private void doRegister() {
        String strTrim = this.authNameField.getText().trim();
        String str = new String(this.authPassField.getPassword());
        if (strTrim.isEmpty() || str.isEmpty()) {
            this.authStatusLabel.setText("Type your username and password.");
            return;
        }
        if (strTrim.length() < 3 || strTrim.length() > 16) {
            this.authStatusLabel.setText("Username must be between 3 and 15 characters long.");
            return;
        }
        this.pendingAuthUsername = strTrim;
        this.pendingAuthPassword = str;
        if (this.out != null) {
            this.out.println("REGISTER " + strTrim + " " + str);
            this.authStatusLabel.setText("Signing up...");
        } else {
            this.authStatusLabel.setText("Not connected to server.");
        }
    }

    private void initMenu() {
        this.nameField = new JTextField(this.username);
        this.nameField.setBounds(420, 100, 150, 25);
        this.headBtn = new JButton("Head");
        this.headBtn.setBounds(420, 140, 150, 25);
        this.headBtn.addActionListener(actionEvent -> {
            Color colorShowDialog = JColorChooser.showDialog(this, "Head", this.headColor);
            if (colorShowDialog != null) {
                this.headColor = colorShowDialog;
                this.localPlayer.hRGB = colorShowDialog.getRGB();
                saveSkinData();
            }
        });
        this.bodyBtn = new JButton("Body");
        this.bodyBtn.setBounds(420, 175, 150, 25);
        this.bodyBtn.addActionListener(actionEvent2 -> {
            Color colorShowDialog = JColorChooser.showDialog(this, "Body", this.bodyColor);
            if (colorShowDialog != null) {
                this.bodyColor = colorShowDialog;
                this.localPlayer.bRGB = colorShowDialog.getRGB();
                saveSkinData();
            }
        });
        this.faceBtn = new JButton("Head (URL)");
        this.faceBtn.setBounds(420, 210, 150, 25);
        this.faceBtn.addActionListener(actionEvent4 -> {
            String strShowInputDialog = JOptionPane.showInputDialog(this, "Head URL (png/jpg):", this.faceUrl);
            if (strShowInputDialog != null) {
                this.faceUrl = strShowInputDialog;
                this.localPlayer.fUrl = strShowInputDialog;
                saveSkinData();
            }
        });
        this.shirtBtn = new JButton("Body (URL)");
        this.shirtBtn.setBounds(420, 245, 150, 25);
        this.shirtBtn.addActionListener(actionEvent3 -> {
            String strShowInputDialog = JOptionPane.showInputDialog(this, "Body URL (png/jpg):", this.shirtUrl);
            if (strShowInputDialog != null) {
                this.shirtUrl = strShowInputDialog;
                this.localPlayer.sUrl = strShowInputDialog;
                saveSkinData();
            }
        });
        this.searchField = new JTextField();
        this.searchField.setBounds(100, 230, 300, 25);
        this.searchField.addKeyListener(new KeyAdapter() { // from class: Dolinablox.1
            public void keyReleased(KeyEvent keyEvent) {
                Dolinablox.this.updateWorldListUI();
            }
        });
        this.worldList = new JList<>(this.worldListModel);
        JScrollPane jScrollPane = new JScrollPane(this.worldList);
        jScrollPane.setBounds(100, 280, 400, 220);
        this.joinBtn = new JButton("Join Server");
        this.joinBtn.setBounds(100, 510, 120, 30);
        this.joinBtn.addActionListener(actionEvent5 -> {
            String str = (String) this.worldList.getSelectedValue();
            if (str != null) {
                String strSubstring = str;
                if (str.contains(" (")) {
                    strSubstring = str.substring(0, str.lastIndexOf(" ("));
                }
                this.username = this.nameField.getText();
                if (this.username.length() > 12) {
                    this.username = this.username.substring(0, 12);
                }
                this.localPlayer.name = this.username;
                this.currentWorldName = strSubstring;
                joinWorld(strSubstring, State.GAME);
            }
        });
        this.newWorldField = new JTextField("My World");
        this.newWorldField.setBounds(600, 280, 150, 25);
        this.createBtn = new JButton("Create Server");
        this.createBtn.setBounds(600, 310, 150, 30);
        this.createBtn.addActionListener(actionEvent6 -> {
            if (this.out != null) {
                this.out.println("CREATE " + this.newWorldField.getText());
                this.out.println("GET_WORLDS");
            }
        });
        this.refreshBtn = new JButton("Refresh");
        this.refreshBtn.setBounds(230, 510, 100, 30);
        this.refreshBtn.addActionListener(actionEvent7 -> {
            if (this.out != null) {
                this.out.println("GET_WORLDS");
            } else {
                refreshCacheList();
            }
        });
        this.editorBtn = new JButton("Map Editor");
        this.editorBtn.setBounds(600, 350, 150, 30);
        this.editorBtn.addActionListener(actionEvent8 -> {
            String str = (String) this.worldList.getSelectedValue();
            if (str != null) {
                String strSubstring = str;
                if (str.contains(" (")) {
                    strSubstring = str.substring(0, str.lastIndexOf(" ("));
                }
                this.currentWorldName = strSubstring;
                this.username = this.nameField.getText();
                if (this.username.length() > 12) {
                    this.username = this.username.substring(0, 12);
                }
                joinWorld(strSubstring, State.EDITOR);
                return;
            }
            JOptionPane.showMessageDialog(this, "Choose server.");
        });
        addMenu(this.searchField);
        addMenu(this.nameField);
        addMenu(this.headBtn);
        addMenu(this.bodyBtn);
        addMenu(this.shirtBtn);
        addMenu(this.faceBtn);
        addMenu(jScrollPane);
        addMenu(this.joinBtn);
        addMenu(this.newWorldField);
        addMenu(this.createBtn);
        addMenu(this.refreshBtn);
        addMenu(this.editorBtn);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateWorldListUI() {
        SwingUtilities.invokeLater(() -> {
            this.worldListModel.clear();
            String lowerCase = this.searchField != null ? this.searchField.getText().toLowerCase() : "";
            this.rawWorlds.stream().filter(str -> {
                return (str.contains(":") ? str.split(":")[0] : str).toLowerCase().contains(lowerCase);
            }).sorted((str2, str3) -> {
                int intSafe = 0;
                int intSafe2 = 0;
                if (str2.contains(":")) {
                    intSafe = parseIntSafe(str2.split(":")[1], 0);
                }
                if (str3.contains(":")) {
                    intSafe2 = parseIntSafe(str3.split(":")[1], 0);
                }
                return Integer.compare(intSafe2, intSafe);
            }).forEach(str4 -> {
                if (str4.contains(":")) {
                    String[] strArrSplit = str4.split(":");
                    this.worldListModel.addElement(strArrSplit[0] + " (" + strArrSplit[1] + ")");
                } else {
                    this.worldListModel.addElement(str4 + " (0)");
                }
            });
        });
    }

    private void refreshCacheList() {
        this.rawWorlds.clear();
        File[] fileArrListFiles = this.cacheDir.listFiles((file, str) -> {
            return str.endsWith(".map");
        });
        if (fileArrListFiles != null) {
            for (File file2 : fileArrListFiles) {
                this.rawWorlds.add(file2.getName().replace(".map", ""));
            }
        }
        updateWorldListUI();
    }

    private void connectToMaster() {
        new Thread(() -> {
            String line;
            try {
                this.socket = new Socket();
                this.socket.connect(new InetSocketAddress(this.serverIP, 25565), 3000);
                this.out = new PrintWriter((Writer) new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8), true);
                this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
                this.isOffline = false;
                SwingUtilities.invokeLater(this::tryAutoLogin);
                while (this.running && (line = this.in.readLine()) != null) {
                    processNetworkData(line);
                }
            } catch (Exception e) {
                this.isOffline = true;
                Player player = this.localPlayer;
                String str = this.savedUsername.isEmpty() ? "Guest" + new Random().nextInt(1000) : this.savedUsername;
                this.username = str;
                player.name = str;
                SwingUtilities.invokeLater(() -> {
                    showState(State.MENU);
                    refreshCacheList();
                    showScreenMessage("You are guest and you can't play online.", 3000);
                });
            }
        }).start();
    }

    private void saveWorldToCache(String str, String str2) {
        try {
            PrintWriter printWriter = new PrintWriter(new File(this.cacheDir, str + ".map"), "UTF-8");
            Throwable th = null;
            try {
                try {
                    printWriter.print(str2);
                    if (printWriter != null) {
                        if (0 != 0) {
                            try {
                                printWriter.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        } else {
                            printWriter.close();
                        }
                    }
                } finally {
                }
            } catch (Throwable th3) {
                th = th3;
                throw th3;
            }
        } catch (Exception e) {
        }
    }

    private java.lang.String loadWorldFromCache(java.lang.String r9) {
        throw new UnsupportedOperationException("Method not decompiled: defpackage.Dolinablox.loadWorldFromCache(java.lang.String):java.lang.String");
    }

    private void processNetworkData(String str) {
        String[] strArrSplit;

        if (str.startsWith("WORLDS ")) {

            this.rawWorlds.clear();

            String data = str.substring(7); // "WORLDS ".length()

            if (!data.isEmpty()) {

                for (String str8 : data.split(",")) {
                    if (!str8.trim().isEmpty()) {
                        this.rawWorlds.add(str8.trim());
                    }
                }
            }

            updateWorldListUI();
            return;
        } else {
            strArrSplit = str.split(" ", 3);
        }

        String str2 = strArrSplit[0];

        if (str2.equals("REGISTER_OK")) {
            if (!this.pendingAuthUsername.isEmpty() && !this.pendingAuthPassword.isEmpty()) {
                saveSessionData(this.pendingAuthUsername, this.pendingAuthPassword);
            }
            SwingUtilities.invokeLater(() -> {
                this.authStatusLabel.setText("Sign up success! Now log in.");
            });
            return;
        }
        if (str2.equals("REGISTER_FAIL")) {
            String str3 = strArrSplit.length > 1 ? strArrSplit[1] : "";
            String str4 = str3.equals("already_exists") ? "Nickname already busy." : str3.equals("invalid_name") ? "Long or short nickname." : "Sign up error.";
            SwingUtilities.invokeLater(() -> {
                this.authStatusLabel.setText(str4);
            });
            return;
        }
        if (str2.equals("LOGIN_OK")) {
            Player player = this.localPlayer;
            String str5 = this.pendingAuthUsername;
            this.username = str5;
            player.name = str5;
            saveSessionData(this.username, this.pendingAuthPassword);
            saveSkinData();
            SwingUtilities.invokeLater(() -> {
                this.nameField.setText(this.username);
                showState(State.MENU);
                if (this.out != null) {
                    this.out.println("GET_WORLDS");
                }
            });
            return;
        }
        if (str2.equals("LOGIN_FAIL")) {
            String str6 = strArrSplit.length > 1 ? strArrSplit[1] : "";
            String str7 = str6.equals("not_found") ? "User doesn't exist." : str6.equals("wrong_password") ? "Password is incorrect." : "Log in error.";
            SwingUtilities.invokeLater(() -> {
                this.authStatusLabel.setText(str7);
            });
            return;
        }
        if (str2.equals("WORLDS ")) {
            this.rawWorlds.clear();
            if (strArrSplit.length > 1) {
                for (String str8 : strArrSplit[1].split(",")) {
                    if (!str8.isEmpty()) {
                        this.rawWorlds.add(str8);
                    }
                }
            }
            updateWorldListUI();
            return;
        }
        if (str2.equals("PONG")) {
            this.ping = System.currentTimeMillis() - this.lastPingSend;
            return;
        }
        if (str2.equals("GLOBAL_PLAYERS")) {
            this.totalPlayersGlobal = Integer.parseInt(strArrSplit[1]);
            return;
        }
        if (str2.equals("MOVE")) {
            String[] strArrSplit2 = strArrSplit[2].split(" ");
            Player playerComputeIfAbsent = this.players.computeIfAbsent(strArrSplit[1], str9 -> {
                return new Player(strArrSplit[1], 0.0f, 0.0f);
            });
            playerComputeIfAbsent.x = Float.parseFloat(strArrSplit2[0]);
            playerComputeIfAbsent.y = Float.parseFloat(strArrSplit2[1]);
            if (strArrSplit2.length >= 4) {
                playerComputeIfAbsent.hRGB = Integer.parseInt(strArrSplit2[2]);
                playerComputeIfAbsent.bRGB = Integer.parseInt(strArrSplit2[3]);
            }
            if (strArrSplit2.length >= 5) {
                playerComputeIfAbsent.sUrl = strArrSplit2[4];
            }
            if (strArrSplit2.length >= 6) {
                playerComputeIfAbsent.fUrl = strArrSplit2[5];
            }
            if (strArrSplit2.length >= 7) {
                playerComputeIfAbsent.danceUntil = Long.parseLong(strArrSplit2[6]);
            }
            if (strArrSplit2.length >= 8) {
                playerComputeIfAbsent.isAfk = Boolean.parseBoolean(strArrSplit2[7]);
            }
            if (strArrSplit2.length >= 9) {
                playerComputeIfAbsent.hideHead = Boolean.parseBoolean(strArrSplit2[8]);
                return;
            }
            return;
        }
        if (str2.equals("MAP_DATA")) {
            saveWorldToCache(this.currentWorldName, strArrSplit[1]);
            loadMapFromData(strArrSplit[1]);
            return;
        }
        if (str2.equals("CHAT")) {
            if (strArrSplit.length > 2 && strArrSplit[2].equals("/hide") && strArrSplit[1].equals(this.currentWorldOwner)) {
                this.hideNames = !this.hideNames;
                return;
            }
            Player playerComputeIfAbsent2 = this.players.computeIfAbsent(strArrSplit[1], str10 -> {
                return new Player(strArrSplit[1], 400.0f, 300.0f);
            });
            playerComputeIfAbsent2.chatBubble = strArrSplit[2];
            playerComputeIfAbsent2.chatTime = System.currentTimeMillis();
            if (strArrSplit[1].equals(this.username)) {
                this.localPlayer.chatBubble = strArrSplit[2];
                this.localPlayer.chatTime = playerComputeIfAbsent2.chatTime;
                return;
            }
            return;
        }
        if (str2.equals("LEAVE")) {
            this.players.remove(strArrSplit[1]);
        }
    }

    private Color applyLighting(Color color, float f, float f2) {
        if (!this.lightingEnabled) {
            return color;
        }
        float brightness = this.globalAmbient;
        Iterator<Light> it = this.lights.iterator();
        while (it.hasNext()) {
            brightness += it.next().getBrightness(f, f2);
        }
        float fMin = Math.min(1.0f, brightness);
        return new Color(Math.max(0, Math.min(255, (int) (color.getRed() * fMin))), Math.max(0, Math.min(255, (int) (color.getGreen() * fMin))), Math.max(0, Math.min(255, (int) (color.getBlue() * fMin))), color.getAlpha());
    }

    private void drawLighting(Graphics2D graphics2D) {
        if (this.lightingEnabled) {
            for (Light light : this.lights) {
                graphics2D.setColor(new Color(light.color.getRed(), light.color.getGreen(), light.color.getBlue(), (int) (light.intensity * 60.0f)));
                graphics2D.fillOval(light.x - light.radius, light.y - light.radius, light.radius * 2, light.radius * 2);
            }
        }
    }

    private void saveLights(StringBuilder sb) {
        if (this.lightingEnabled || !this.lights.isEmpty()) {
            sb.append("|LIGHTS:");
            for (Light light : this.lights) {
                sb.append(light.x).append(",").append(light.y).append(",").append(light.radius).append(",");
                sb.append(light.intensity).append(",").append(light.color.getRGB()).append(";");
            }
        }
    }

    private void loadLights(String str) {
        this.lights.clear();
        if (str == null || !str.contains("LIGHTS:")) {
            return;
        }
        String strSubstring = str.substring(str.indexOf("LIGHTS:") + 7);
        if (strSubstring.isEmpty()) {
            return;
        }
        for (String str2 : strSubstring.split(";")) {
            if (!str2.isEmpty()) {
                try {
                    String[] strArrSplit = str2.split(",");
                    if (strArrSplit.length >= 5) {
                        this.lights.add(new Light(Integer.parseInt(strArrSplit[0]), Integer.parseInt(strArrSplit[1]), Integer.parseInt(strArrSplit[2]), Float.parseFloat(strArrSplit[3]), new Color(Integer.parseInt(strArrSplit[4]))));
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    private void loadMapFromData(String str) {
        this.editorObjects.clear();
        this.selectedObject = null;
        if (str == null || str.trim().isEmpty()) {
            return;
        }
        String[] strArrSplit = str.split("\\|");
        if (strArrSplit.length > 1) {
            this.skyUrl = strArrSplit[1];
        }
        if (strArrSplit.length > 2) {
            this.currentWorldOwner = strArrSplit[2];
        }
        if (strArrSplit.length > 3) {
            this.musicUrl = strArrSplit[3];
            playMusicFromUrl(this.musicUrl);
        } else {
            this.musicUrl = "none";
            stopMusic();
        }
        this.commandsDisabled = false;
        this.gravityDisabled = false;
        for (String str2 : strArrSplit) {
            if (str2.startsWith("SETTINGS:")) {
                for (String str3 : str2.substring(9).split(",")) {
                    if (str3.equals("nocmds")) {
                        this.commandsDisabled = true;
                    }
                    if (str3.equals("nograv")) {
                        this.gravityDisabled = true;
                    }
                }
            }
        }
        this.lights.clear();
        this.lightingEnabled = false;
        int length = strArrSplit.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String str4 = strArrSplit[i];
            if (!str4.startsWith("LIGHTS:")) {
                i++;
            } else {
                loadLights(str4);
                this.lightingEnabled = !this.lights.isEmpty();
            }
        }
        for (String str5 : strArrSplit[0].split(";")) {
            if (!str5.trim().isEmpty()) {
                String[] strArrSplit2 = str5.split(",");
                if (strArrSplit2.length >= 5) {
                    MapObject mapObject = new MapObject(Integer.parseInt(strArrSplit2[0]), Integer.parseInt(strArrSplit2[1]), new Color(Integer.parseInt(strArrSplit2[2])), strArrSplit2[3], Boolean.parseBoolean(strArrSplit2[4]), strArrSplit2.length >= 6 ? strArrSplit2[5] : "none", strArrSplit2.length >= 7 ? Boolean.parseBoolean(strArrSplit2[6]) : true, strArrSplit2.length >= 8 ? Boolean.parseBoolean(strArrSplit2[7]) : false, strArrSplit2.length >= 9 ? new String(Base64.getDecoder().decode(strArrSplit2[8]), StandardCharsets.UTF_8) : "");
                    if (strArrSplit2.length >= 10) {
                        mapObject.size = Integer.parseInt(strArrSplit2[9]);
                    }
                    if (strArrSplit2.length >= 11) {
                        mapObject.npcType = strArrSplit2[10];
                    }
                    if (strArrSplit2.length >= 12) {
                        mapObject.searchRadius = Integer.parseInt(strArrSplit2[11]);
                    }
                    if (strArrSplit2.length >= 13) {
                        mapObject.isDynamite = Boolean.parseBoolean(strArrSplit2[12]);
                    }
                    this.editorObjects.add(mapObject);
                    if (mapObject.shape.equals("SPAWN") && this.currentState == State.GAME) {
                        this.localPlayer.x = mapObject.x;
                        this.localPlayer.y = mapObject.y;
                    }
                }
            }
        }
    }

    private void joinWorld(String str, State state) {
        this.players.clear();
        this.editorObjects.clear();
        this.selectedObject = null;
        this.skyUrl = "none";
        this.currentWorldOwner = "Unknown";
        this.hideNames = false;
        this.localPlayer.speed = 5.0f;
        this.localPlayer.jumpForce = -15.0f;
        this.localPlayer.size = 40;
        this.localPlayer.danceUntil = 0L;
        this.musicUrl = "none";
        stopMusic();
        this.fixedCamera = false;
        this.lights.clear();
        this.heldItem = "none";
        this.isAfk = false;
        this.hideHead = false;
        this.commandsDisabled = false;
        this.gravityDisabled = false;
        if (!this.isOffline && this.out != null) {
            this.out.println("LEAVE_WORLD");
            this.out.println("JOIN " + this.username + " " + str);
            this.out.println("GET_MAP " + str);
        } else {
            loadMapFromData(loadWorldFromCache(str));
        }
        showState(state);
        requestFocus();
        this.camX = 0.0f;
        this.camY = 0.0f;
        this.zoom = 1.0f;
    }

    private void toggleMenu(boolean z) {
        if (z) {
            stopMusic();
            showState(State.MENU);
            if (this.out != null) {
                this.out.println("LEAVE_WORLD");
                return;
            }
            return;
        }
        Iterator<Component> it = this.menuComps.iterator();
        while (it.hasNext()) {
            it.next().setVisible(false);
        }
    }

    private void restartClient() {
        this.running = false;
        stopMusic();
        try {
            if (this.out != null) {
                this.out.println("LEAVE_WORLD");
            }
        } catch (Exception e) {
        }
        try {
            if (this.socket != null && !this.socket.isClosed()) {
                this.socket.close();
            }
        } catch (Exception e2) {
        }
        this.loaderService.shutdownNow();
        Window windowAncestor = SwingUtilities.getWindowAncestor(this);
        if (windowAncestor != null) {
            windowAncestor.dispose();
        }
        SwingUtilities.invokeLater(() -> {
            main(new String[0]);
        });
    }

    @Override // java.lang.Runnable
    public void run() {
        while (this.running) {
            this.frames++;
            if (System.currentTimeMillis() - this.lastFpsTime >= 1000) {
                this.fps = this.frames;
                this.frames = 0;
                this.lastFpsTime = System.currentTimeMillis();
                if (this.out != null) {
                    this.lastPingSend = System.currentTimeMillis();
                    this.out.println("PING");
                    this.out.println("GET_GLOBAL_PLAYERS");
                }
            }
            if (this.currentState == State.GAME) {
                updatePhysics();
                updateMovement();
                if (!this.fixedCamera) {
                    this.camX = ((getWidth() / 2) - this.localPlayer.x) - (this.localPlayer.size / 2);
                    this.camY = ((getHeight() / 2) - this.localPlayer.y) - (this.localPlayer.size / 2);
                } else {
                    this.camX = ((getWidth() / 2) - this.fixedCamX) - 20.0f;
                    this.camY = ((getHeight() / 2) - this.fixedCamY) - 20.0f;
                }
            }
            repaint();
            try {
                Thread.sleep(16L);
            } catch (Exception e) {
            }
        }
    }

    private String resolveVar(String str) {
        if (str.startsWith("$")) {
            Double d = this.scriptVars.get(str.substring(1));
            return d != null ? d.doubleValue() == ((double) d.longValue()) ? String.valueOf(d.longValue()) : String.valueOf(d) : "0";
        }
        return str;
    }

    private String resolveInline(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = Pattern.compile("\\$([A-Za-z_][A-Za-z0-9_]*)").matcher(str);
        while (matcher.find()) {
            Double d = this.scriptVars.get(matcher.group(1));
            matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(d != null ? d.doubleValue() == ((double) d.longValue()) ? String.valueOf(d.longValue()) : String.valueOf(d) : "0"));
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    private double resolveNum(String str) {
        try {
            return Double.parseDouble(resolveVar(str));
        } catch (Exception e) {
            return 0.0d;
        }
    }

    private void executeScript(MapObject mapObject, String str) {
        if (mapObject.script == null || mapObject.script.isEmpty() || System.currentTimeMillis() < mapObject.waitUntil) {
            return;
        }
        boolean z = false;
        boolean z2 = false;
        int i = 0;
        for (String str2 : mapObject.script.split("\n")) {
            String strTrim = str2.trim();
            if (strTrim.startsWith("@")) {
                if (strTrim.equalsIgnoreCase(str)) {
                    z = true;
                    z2 = false;
                    i = 0;
                } else if (strTrim.toLowerCase().startsWith("@on_signal")) {
                    String[] strArrSplit = strTrim.split(" ");
                    z = str.startsWith("@ON_SIGNAL") && strArrSplit.length > 1 && str.endsWith(strArrSplit[1]);
                    if (z) {
                        z2 = false;
                        i = 0;
                    }
                } else {
                    z = false;
                }
            } else if (z && !strTrim.isEmpty()) {
                String[] strArrSplit2 = strTrim.split(" ", 2);
                String lowerCase = strArrSplit2[0].toLowerCase();
                if (lowerCase.equals("if")) {
                    i++;
                    if (!z2) {
                        try {
                            String[] strArrSplit3 = strArrSplit2[1].trim().split("\\s+");
                            double dResolveNum = resolveNum(strArrSplit3[0]);
                            String str3 = strArrSplit3[1];
                            double dResolveNum2 = resolveNum(strArrSplit3[2]);
                            boolean z3 = str3.equals("==") ? dResolveNum == dResolveNum2 : str3.equals("!=") ? dResolveNum != dResolveNum2 : str3.equals(">") ? dResolveNum > dResolveNum2 : str3.equals("<") ? dResolveNum < dResolveNum2 : str3.equals(">=") ? dResolveNum >= dResolveNum2 : str3.equals("<=") && dResolveNum <= dResolveNum2;
                            if (!z3) {
                                z2 = true;
                            }
                        } catch (Exception e) {
                            z2 = true;
                        }
                    }
                } else if (lowerCase.equals("endif")) {
                    if (i > 0) {
                        i--;
                    }
                    if (i == 0) {
                        z2 = false;
                    }
                } else if (!z2) {
                    try {
                        if (lowerCase.equals("set_var")) {
                            String[] strArrSplit4 = strArrSplit2[1].trim().split("\\s+");
                            ConcurrentHashMap<String, Double> concurrentHashMap = this.scriptVars;
                            String str4 = strArrSplit4[0];
                            String str5 = strArrSplit4[1];
                            concurrentHashMap.put(str4, Double.valueOf(resolveNum(str5)));
                        }
                        if (lowerCase.equals("add_var")) {
                            String[] strArrSplit5 = strArrSplit2[1].trim().split("\\s+");
                            ConcurrentHashMap<String, Double> concurrentHashMap2 = this.scriptVars;
                            String str6 = strArrSplit5[0];
                            Double dValueOf = Double.valueOf(resolveNum(strArrSplit5[1]));
                            BiFunction<? super Double, ? super Double, ? extends Double> biFunction = (v0, v1) -> {
                                return Double.sum(v0, v1);
                            };
                            concurrentHashMap2.merge(str6, dValueOf, biFunction);
                        }
                        if (lowerCase.equals("sub_var")) {
                            String[] strArrSplit6 = strArrSplit2[1].trim().split("\\s+");
                            ConcurrentHashMap<String, Double> concurrentHashMap3 = this.scriptVars;
                            String str7 = strArrSplit6[0];
                            Double dValueOf2 = Double.valueOf(resolveNum(strArrSplit6[1]));
                            BiFunction<? super Double, ? super Double, ? extends Double> biFunction2 = (d, d2) -> {
                                return Double.valueOf(d.doubleValue() - d2.doubleValue());
                            };
                            concurrentHashMap3.merge(str7, dValueOf2, biFunction2);
                        }
                        if (lowerCase.equals("mul_var")) {
                            String[] strArrSplit7 = strArrSplit2[1].trim().split("\\s+");
                            ConcurrentHashMap<String, Double> concurrentHashMap4 = this.scriptVars;
                            String str8 = strArrSplit7[0];
                            Double dValueOf3 = Double.valueOf(resolveNum(strArrSplit7[1]));
                            BiFunction<? super Double, ? super Double, ? extends Double> biFunction3 = (d3, d4) -> {
                                return Double.valueOf(d3.doubleValue() * d4.doubleValue());
                            };
                            concurrentHashMap4.merge(str8, dValueOf3, biFunction3);
                        }
                        if (lowerCase.equals("div_var")) {
                            String[] strArrSplit8 = strArrSplit2[1].trim().split("\\s+");
                            double dResolveNum3 = resolveNum(strArrSplit8[1]);
                            if (dResolveNum3 != 0.0d) {
                                ConcurrentHashMap<String, Double> concurrentHashMap5 = this.scriptVars;
                                String str9 = strArrSplit8[0];
                                Double dValueOf4 = Double.valueOf(dResolveNum3);
                                BiFunction<? super Double, ? super Double, ? extends Double> biFunction4 = (d5, d6) -> {
                                    return Double.valueOf(d5.doubleValue() / d6.doubleValue());
                                };
                                concurrentHashMap5.merge(str9, dValueOf4, biFunction4);
                            }
                        }
                        if (lowerCase.equals("message")) {
                            showScreenMessage(resolveInline(strArrSplit2[1].trim()), 2000);
                        }
                        if (lowerCase.equals("set_id")) {
                            mapObject.id = strArrSplit2[1];
                        }
                        if (lowerCase.equals("send_signal")) {
                            for (MapObject mapObject2 : this.editorObjects) {
                                StringBuilder sbAppend = new StringBuilder().append("@ON_SIGNAL ");
                                String str10 = strArrSplit2[1];
                                executeScript(mapObject2, sbAppend.append(str10).toString());
                            }
                        }
                        if (lowerCase.equals("wait")) {
                            mapObject.waitUntil = System.currentTimeMillis() + ((long) (resolveNum(strArrSplit2[1]) * 1000.0d));
                        }
                        if (lowerCase.equals("set_pos")) {
                            String[] strArrSplit9 = strArrSplit2[1].split(" ");
                            mapObject.x = (int) resolveNum(strArrSplit9[0]);
                            mapObject.y = (int) resolveNum(strArrSplit9[1]);
                        }
                        if (lowerCase.equals("move")) {
                            String[] strArrSplit10 = strArrSplit2[1].split(" ");
                            mapObject.targetX = (int) resolveNum(strArrSplit10[0]);
                            mapObject.targetY = (int) resolveNum(strArrSplit10[1]);
                        }
                        if (lowerCase.equals("stop_move")) {
                            mapObject.targetX = -1;
                            mapObject.targetY = -1;
                        }
                        if (lowerCase.equals("set_texture")) {
                            mapObject.textureUrl = strArrSplit2[1];
                        }
                        if (lowerCase.equals("set_sky")) {
                            this.skyUrl = strArrSplit2[1];
                        }
                        if (lowerCase.equals("set_music")) {
                            this.musicUrl = strArrSplit2[1];
                            playMusicFromUrl(this.musicUrl);
                        }
                        if (lowerCase.equals("stop_music")) {
                            this.musicUrl = "none";
                            stopMusic();
                        }
                        if (lowerCase.equals("move_x")) {
                            int i2 = mapObject.x;
                            String str11 = strArrSplit2[1];
                            mapObject.x = i2 + ((int) resolveNum(str11));
                        }
                        if (lowerCase.equals("move_y")) {
                            int i3 = mapObject.y;
                            String str12 = strArrSplit2[1];
                            mapObject.y = i3 + ((int) resolveNum(str12));
                        }
                        if (lowerCase.equals("set_vel_y")) {
                            mapObject.velY = (float) resolveNum(strArrSplit2[1]);
                        }
                        if (lowerCase.equals("kill_player")) {
                            respawn();
                        }
                        if (lowerCase.equals("teleport_player")) {
                            String[] strArrSplit11 = strArrSplit2[1].split(" ");
                            this.localPlayer.x = (float) resolveNum(strArrSplit11[0]);
                            this.localPlayer.y = (float) resolveNum(strArrSplit11[1]);
                            this.localPlayer.velY = 0.0f;
                        }
                        if (lowerCase.equals("set_color")) {
                            String[] strArrSplit12 = strArrSplit2[1].split(" ");
                            int iResolveNum = (int) resolveNum(strArrSplit12[0]);
                            mapObject.color = new Color(iResolveNum, (int) resolveNum(strArrSplit12[1]), (int) resolveNum(strArrSplit12[2]), mapObject.color.getAlpha());
                        }
                        if (lowerCase.equals("set_alpha")) {
                            int iResolveNum2 = (int) resolveNum(strArrSplit2[1]);
                            Color color = mapObject.color;
                            int red = color.getRed();
                            mapObject.color = new Color(red, color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, iResolveNum2)));
                        }
                        if (lowerCase.equals("set_size")) {
                            mapObject.size = (int) resolveNum(strArrSplit2[1]);
                        }
                        if (lowerCase.equals("set_player_speed")) {
                            this.localPlayer.speed = (float) resolveNum(strArrSplit2[1]);
                        }
                        if (lowerCase.equals("set_player_jump")) {
                            this.localPlayer.jumpForce = -((float) Math.abs(resolveNum(strArrSplit2[1])));
                        }
                        if (lowerCase.equals("set_player_size")) {
                            this.localPlayer.size = (int) resolveNum(strArrSplit2[1]);
                        }
                        if (lowerCase.equals("destroy")) {
                            this.editorObjects.remove(mapObject);
                        }
                        if (lowerCase.equals("toggle_collider")) {
                            mapObject.hasCollider = !mapObject.hasCollider;
                        }
                        if (lowerCase.equals("add_camera")) {
                            String[] strArrSplit13 = strArrSplit2[1].split(" ");
                            this.fixedCamera = true;
                            this.fixedCamX = (float) resolveNum(strArrSplit13[0]);
                            this.fixedCamY = (float) resolveNum(strArrSplit13[1]);
                        }
                        if (lowerCase.equals("remove_camera")) {
                            this.fixedCamera = false;
                        }
                    } catch (Exception e2) {
                    }
                }
            }
        }
    }

    private void updatePhysics() {
        for (MapObject mapObject : this.editorObjects) {
            executeScript(mapObject, "@ON_TICK");
            if (mapObject.isDynamite && mapObject.explodeTime == 0) {
                double d = this.localPlayer.x - mapObject.x;
                double d2 = this.localPlayer.y - mapObject.y;
                if (Math.sqrt((d * d) + (d2 * d2)) < mapObject.size + this.localPlayer.size) {
                    mapObject.explodeTime = System.currentTimeMillis() + 500;
                }
                Iterator<Player> it = this.players.values().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Player next = it.next();
                    if (!next.name.equals(this.username)) {
                        double d3 = next.x - mapObject.x;
                        double d4 = next.y - mapObject.y;
                        if (Math.sqrt((d3 * d3) + (d4 * d4)) < mapObject.size + next.size) {
                            mapObject.explodeTime = System.currentTimeMillis() + 500;
                            break;
                        }
                    }
                }
            }
            if (mapObject.isDynamite && mapObject.explodeTime > 0 && System.currentTimeMillis() >= mapObject.explodeTime) {
                double d5 = this.localPlayer.x - mapObject.x;
                double d6 = this.localPlayer.y - mapObject.y;
                if (Math.sqrt((d5 * d5) + (d6 * d6)) < mapObject.explosionRadius) {
                    respawn();
                }
                for (Player player : this.players.values()) {
                    if (!player.name.equals(this.username)) {
                        double d7 = player.x - mapObject.x;
                        double d8 = player.y - mapObject.y;
                        if (Math.sqrt((d7 * d7) + (d8 * d8)) < mapObject.explosionRadius) {
                            playSound("button.wav");
                        }
                    }
                }
                this.editorObjects.removeIf(mapObject2 -> {
                    double d9 = mapObject2.x - mapObject.x;
                    double d10 = mapObject2.y - mapObject.y;
                    if (Math.sqrt((d9 * d9) + (d10 * d10)) < mapObject.explosionRadius && !mapObject2.equals(mapObject)) {
                        if (mapObject2.isDynamite) {
                            mapObject2.explodeTime = System.currentTimeMillis() + 300;
                        }
                        return !mapObject2.isDynamite;
                    }
                    return false;
                });
                this.editorObjects.remove(mapObject);
            } else {
                if (mapObject.npcType.equals("ENEMY")) {
                    Player player2 = null;
                    double d9 = mapObject.searchRadius;
                    double d10 = this.localPlayer.x - mapObject.x;
                    double d11 = this.localPlayer.y - mapObject.y;
                    double dSqrt = Math.sqrt((d10 * d10) + (d11 * d11));
                    if (dSqrt < d9) {
                        d9 = dSqrt;
                        player2 = this.localPlayer;
                    }
                    for (Player player3 : this.players.values()) {
                        if (!player3.name.equals(this.username)) {
                            double d12 = player3.x - mapObject.x;
                            double d13 = player3.y - mapObject.y;
                            double dSqrt2 = Math.sqrt((d12 * d12) + (d13 * d13));
                            if (dSqrt2 < d9) {
                                d9 = dSqrt2;
                                player2 = player3;
                            }
                        }
                    }
                    if (player2 != null) {
                        mapObject.targetPlayerName = player2.name;
                        if (player2.x > mapObject.x) {
                            mapObject.x += 2;
                        } else if (player2.x < mapObject.x) {
                            mapObject.x -= 2;
                        }
                        if (player2.y > mapObject.y) {
                            mapObject.y++;
                        } else if (player2.y < mapObject.y) {
                            mapObject.y--;
                        }
                    } else {
                        mapObject.targetPlayerName = "";
                    }
                }
                if (mapObject.targetX != -1 || mapObject.targetY != -1) {
                    boolean z = true;
                    if (mapObject.targetX != -1) {
                        if (Math.abs(mapObject.x - mapObject.targetX) > 2) {
                            mapObject.x += mapObject.x < mapObject.targetX ? 2 : -2;
                            z = false;
                        } else {
                            mapObject.x = mapObject.targetX;
                        }
                    }
                    if (mapObject.targetY != -1) {
                        if (Math.abs(mapObject.y - mapObject.targetY) > 2) {
                            mapObject.y += mapObject.y < mapObject.targetY ? 2 : -2;
                            z = false;
                        } else {
                            mapObject.y = mapObject.targetY;
                        }
                    }
                    if (z) {
                        mapObject.targetX = -1;
                        mapObject.targetY = -1;
                        executeScript(mapObject, "@ON_DOSHOL");
                    }
                }
            }
        }
    }

    private void updateMovement() {
        Item item;
        if (this.isChatting) {
            return;
        }
        float f = this.localPlayer.x;
        float f2 = this.localPlayer.y;
        if (this.keyA) {
            this.localPlayer.x -= this.localPlayer.speed;
        }
        if (this.keyD) {
            this.localPlayer.x += this.localPlayer.speed;
        }
        for (MapObject mapObject : this.editorObjects) {
            if (!mapObject.shape.equals("SPAWN") && checkCollision(this.localPlayer.x, this.localPlayer.y, mapObject)) {
                if (mapObject.isKill) {
                    respawn();
                    return;
                } else {
                    if (mapObject.hasCollider) {
                        this.localPlayer.x = f;
                    }
                    executeScript(mapObject, "@ON_TOUCH");
                }
            }
        }
        if (!this.isOffline) {
            Iterator<Player> it = this.players.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Player next = it.next();
                if (!next.name.equals(this.username) && checkPlayerCollision(this.localPlayer.x, this.localPlayer.y, next)) {
                    this.localPlayer.x = f;
                    break;
                }
            }
        }
        if (this.gravityDisabled) {
            if (this.keySpace) {
                this.localPlayer.velY = -this.localPlayer.speed;
            } else if (this.keyS) {
                this.localPlayer.velY = this.localPlayer.speed;
            } else {
                this.localPlayer.velY = 0.0f;
            }
        } else {
            if (this.keySpace && this.localPlayer.isGrounded) {
                float f3 = this.localPlayer.jumpForce;
                if (!this.heldItem.equals("none") && (item = this.items.get(this.heldItem)) != null) {
                    f3 *= item.jumpMultiplier;
                }
                this.localPlayer.velY = f3;
                this.localPlayer.isGrounded = false;
            }
            this.localPlayer.velY += 0.8f;
        }
        this.localPlayer.y += this.localPlayer.velY;
        this.localPlayer.isGrounded = false;
        for (MapObject mapObject2 : this.editorObjects) {
            if (!mapObject2.shape.equals("SPAWN") && checkCollision(this.localPlayer.x, this.localPlayer.y, mapObject2)) {
                if (mapObject2.isKill) {
                    respawn();
                    return;
                }
                if (mapObject2.hasCollider) {
                    if (this.localPlayer.velY > 0.0f) {
                        this.localPlayer.y = mapObject2.y - this.localPlayer.size;
                        this.localPlayer.velY = 0.0f;
                        this.localPlayer.isGrounded = true;
                    } else if (this.localPlayer.velY < 0.0f) {
                        this.localPlayer.y = mapObject2.y + mapObject2.size;
                        this.localPlayer.velY = 0.0f;
                    }
                }
                executeScript(mapObject2, "@ON_TOUCH");
            }
        }
        for (MapObject mapObject3 : this.editorObjects) {
            if (mapObject3.npcType.equals("ENEMY") && checkCollision(this.localPlayer.x, this.localPlayer.y, mapObject3)) {
                respawn();
                return;
            }
        }
        if (!this.isOffline) {
            for (Player player : this.players.values()) {
                if (!player.name.equals(this.username) && checkPlayerCollision(this.localPlayer.x, this.localPlayer.y, player)) {
                    if (this.localPlayer.velY > 0.0f) {
                        this.localPlayer.y = player.y - this.localPlayer.size;
                        this.localPlayer.velY = 0.0f;
                        this.localPlayer.isGrounded = true;
                    } else if (this.localPlayer.velY < 0.0f) {
                        this.localPlayer.y = player.y + player.size;
                        this.localPlayer.velY = 0.0f;
                    }
                }
            }
            if (this.out != null) {
                this.out.println("MOVE " + this.username + " " + this.localPlayer.x + " " + this.localPlayer.y + " " + this.headColor.getRGB() + " " + this.bodyColor.getRGB() + " " + this.shirtUrl + " " + this.faceUrl + " " + this.localPlayer.danceUntil + " " + this.isAfk + " " + this.hideHead);
            }
        }
        if (!this.gravityDisabled && this.localPlayer.y > 5000.0f) {
            respawn();
        }
    }

    private void respawn() {
        boolean z = false;
        for (MapObject mapObject : this.editorObjects) {
            if (!mapObject.shape.equals("SPAWN"))
                continue;
            this.localPlayer.x = mapObject.x;
            this.localPlayer.y = mapObject.y;
            z = true;
        }
        if (!z) {
            this.localPlayer.x = 400.0f;
            this.localPlayer.y = 300.0f;
        }
        this.localPlayer.velY = 0.0f;
    }

    private boolean checkCollision(float f, float f2, MapObject mapObject) {
        return f < ((float) (mapObject.x + mapObject.size)) && f + ((float) this.localPlayer.size) > ((float) mapObject.x) && f2 < ((float) (mapObject.y + mapObject.size)) && f2 + ((float) this.localPlayer.size) > ((float) mapObject.y);
    }

    private boolean checkPlayerCollision(float f, float f2, Player player) {
        return f < player.x + ((float) player.size) && f + ((float) this.localPlayer.size) > player.x && f2 < player.y + ((float) player.size) && f2 + ((float) this.localPlayer.size) > player.y;
    }

    private Image getImage(String str) {
        if (str == null || str.equals("none") || str.isEmpty()) {
            return null;
        }
        Image image = this.cachedImages.get(str);
        if (image != null) {
            return image;
        }
        if (!this.loadingUrls.contains(str)) {
            this.loadingUrls.add(str);
            this.loaderService.submit(() -> {
                try {
                    BufferedImage image2 = null;
                    if (str.startsWith("http://") || str.startsWith("https://")) {
                        image2 = ImageIO.read(new URL(str));
                    } else {
                        InputStream resourceAsStream = getClass().getResourceAsStream(str);
                        if (resourceAsStream != null) {
                            image2 = ImageIO.read(resourceAsStream);
                            resourceAsStream.close();
                        } else {
                            System.out.println("IMAGE NOT FOUND: " + str);
                        }
                    }
                    if (image2 != null && image2.getWidth((ImageObserver) null) > 0) {
                        this.cachedImages.put(str, image2);
                    } else if (image2 == null) {
                        System.out.println("Error: " + str);
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + str);
                    e.printStackTrace();
                }
                this.loadingUrls.remove(str);
            });
            return null;
        }
        return null;
    }

    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (this.currentState == State.AUTH) {
            drawAuth(graphics2D);
        } else if (this.currentState == State.MENU) {
            drawMenu(graphics2D);
        } else if (this.currentState == State.GAME) {
            drawGame(graphics2D);
        } else if (this.currentState == State.EDITOR) {
            drawEditor(graphics2D);
        }
        if (this.showDebug && this.currentState != State.MENU && this.currentState != State.AUTH) {
            graphics2D.setFont(new Font("Monospaced", 1, 12));
            if (this.currentState == State.EDITOR) {
                graphics2D.setColor(new Color(0, 0, 0, 100));
                graphics2D.fillRect(5, 5, 200, 30);
                graphics2D.setColor(Color.WHITE);
                graphics2D.drawString("X: " + ((int) ((this.mouseX - this.camX) / this.zoom)) + " Y: " + ((int) ((this.mouseY - this.camY) / this.zoom)), 10, 22);
            } else {
                graphics2D.setColor(new Color(0, 0, 0, 100));
                graphics2D.fillRect(5, 5, 200, 85);
                graphics2D.setColor(Color.WHITE);
                graphics2D.drawString("FPS: " + this.fps, 10, 20);
                int i = 20 + 15;
                graphics2D.drawString("Ping: " + (this.isOffline ? "OFFLINE" : this.ping + "ms"), 10, i);
                int i2 = i + 15;
                graphics2D.drawString("X: " + ((int) this.localPlayer.x) + " Y: " + ((int) this.localPlayer.y), 10, i2);
                int i3 = i2 + 15;
                graphics2D.drawString("Global Players: " + this.totalPlayersGlobal, 10, i3);
                graphics2D.drawString("World: " + this.currentWorldName, 10, i3 + 15);
            }
        }
        if (!this.screenMessage.isEmpty() && System.currentTimeMillis() < this.screenMessageUntil) {
            graphics2D.setColor(Color.YELLOW);
            graphics2D.setFont(new Font("Tahoma", 1, 18));
            graphics2D.drawString(this.screenMessage, (getWidth() - graphics2D.getFontMetrics().stringWidth(this.screenMessage)) / 2, 30);
        }
    }

    private void drawAuth(Graphics2D graphics2D) {
        graphics2D.setColor(new Color(220, 230, 250));
        graphics2D.fillRect(0, 0, getWidth(), getHeight());
        graphics2D.setColor(new Color(140, 28, 19));
        graphics2D.fillRect(0, 0, getWidth(), 60);
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(new Font("Tahoma", 1, 28));
        graphics2D.drawString("PukiBlox", 20, 42);
        graphics2D.setFont(new Font("Tahoma", 0, 13));
        graphics2D.setColor(new Color(255, 220, 180));
        graphics2D.drawString("v1.1", 20, 58);
        graphics2D.setColor(new Color(45, 65, 110, 215));
        graphics2D.fillRoundRect(265, 222, 470, 230, 22, 22);
        graphics2D.setColor(new Color(80, 110, 180));
        graphics2D.setStroke(new BasicStroke(2.0f));
        graphics2D.drawRoundRect(265, 222, 470, 230, 22, 22);
    }

    private void drawMenu(Graphics2D graphics2D) {
        graphics2D.setColor(new Color(230, 240, 255));
        graphics2D.fillRect(0, 0, getWidth(), getHeight());
        graphics2D.setColor(new Color(140, 28, 19));
        graphics2D.fillRect(0, 0, getWidth(), 60);
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(new Font("Tahoma", 1, 28));
        graphics2D.drawString("PukiBlox", 20, 42);
        graphics2D.setFont(new Font("Tahoma", 0, 13));
        graphics2D.drawString(this.username, 20, 58);
        graphics2D.setColor(this.bodyColor);
        graphics2D.fillRect(650, 110, 60, 60);
        Image image = getImage(this.shirtUrl);
        if (image != null) {
            graphics2D.drawImage(image, 650, 110, 60, 60, (ImageObserver) null);
        }
        graphics2D.setColor(this.headColor);
        graphics2D.fillRoundRect(665, 85, 30, 30, 8, 8);
        Image image2 = getImage(this.faceUrl);
        if (image2 != null) {
            graphics2D.drawImage(image2, 665, 85, 30, 30, (ImageObserver) null);
        }
        graphics2D.setColor(new Color(80, 80, 80));
        graphics2D.setFont(new Font("Tahoma", 1, 14));
        graphics2D.drawString("Nickname:", 330, 117);
        graphics2D.drawString("Serveri:", 100, 240);
    }

    private void drawObject(Graphics2D graphics2D, MapObject mapObject) {
        graphics2D.setColor(applyLighting(mapObject.color, mapObject.x + (mapObject.size / 2), mapObject.y + (mapObject.size / 2)));
        if (mapObject.shape.equals("CUBE") || mapObject.shape.equals("SPAWN")) {
            graphics2D.fillRect(mapObject.x, mapObject.y, mapObject.size, mapObject.size);
            Image image = getImage(mapObject.textureUrl);
            if (image != null) {
                graphics2D.drawImage(image, mapObject.x, mapObject.y, mapObject.size, mapObject.size, (ImageObserver) null);
                return;
            }
            return;
        }
        if (mapObject.shape.equals("SPHERE")) {
            graphics2D.fillOval(mapObject.x, mapObject.y, mapObject.size, mapObject.size);
            Image image2 = getImage(mapObject.textureUrl);
            if (image2 != null) {
                Shape clip = graphics2D.getClip();
                graphics2D.setClip(new Ellipse2D.Float(mapObject.x, mapObject.y, mapObject.size, mapObject.size));
                graphics2D.drawImage(image2, mapObject.x, mapObject.y, mapObject.size, mapObject.size, (ImageObserver) null);
                graphics2D.setClip(clip);
                return;
            }
            return;
        }
        if (mapObject.shape.equals("TRIANGLE")) {
            int[] iArr = {mapObject.x, mapObject.x + (mapObject.size / 2), mapObject.x + mapObject.size};
            int[] iArr2 = {mapObject.y + mapObject.size, mapObject.y, mapObject.y + mapObject.size};
            graphics2D.fillPolygon(iArr, iArr2, 3);
            Image image3 = getImage(mapObject.textureUrl);
            if (image3 != null) {
                Shape clip2 = graphics2D.getClip();
                graphics2D.setClip(new Polygon(iArr, iArr2, 3));
                graphics2D.drawImage(image3, mapObject.x, mapObject.y, mapObject.size, mapObject.size, (ImageObserver) null);
                graphics2D.setClip(clip2);
            }
        }
    }

    private void drawGame(Graphics2D graphics2D) {
        Image image;
        Image image2 = getImage(this.skyUrl);
        if (image2 != null) {
            graphics2D.drawImage(image2, 0, 0, getWidth(), getHeight(), (ImageObserver) null);
        } else {
            graphics2D.setColor(new Color(135, 206, 235));
            graphics2D.fillRect(0, 0, getWidth(), getHeight());
        }
        graphics2D.translate(this.camX, this.camY);
        for (MapObject mapObject : this.editorObjects) {
            if (!mapObject.shape.equals("SPAWN")) {
                if (mapObject.npcType.equals("ENEMY")) {
                    graphics2D.setColor(new Color(255, 50, 50));
                }
                if (mapObject.isDynamite && mapObject.explodeTime > 0) {
                    graphics2D.setColor(new Color(255, 100, 0));
                }
                drawObject(graphics2D, mapObject);
                if (mapObject.isDynamite && mapObject.explodeTime > 0 && mapObject.explodeTime - System.currentTimeMillis() > 0) {
                    graphics2D.setColor(new Color(255, 200, 0, 100 + ((int) (Math.abs(Math.sin(System.currentTimeMillis() * 0.01d)) * 155.0d))));
                    graphics2D.fillOval(mapObject.x - mapObject.explosionRadius, mapObject.y - mapObject.explosionRadius, mapObject.explosionRadius * 2, mapObject.explosionRadius * 2);
                }
            }
        }
        this.players.put(this.username, this.localPlayer);
        Iterator<Player> it = this.players.values().iterator();
        while (it.hasNext()) {
            Player next = it.next();
            float f = next.size / 40.0f;
            float fSin = 0.0f;
            float fAbs = 0.0f;
            if (System.currentTimeMillis() < next.danceUntil) {
                long jCurrentTimeMillis = System.currentTimeMillis();
                fSin = ((float) Math.sin(jCurrentTimeMillis * 0.015d)) * 12.0f;
                fAbs = ((float) Math.abs(Math.sin(jCurrentTimeMillis * 0.02d))) * (-10.0f);
            }
            if (next != this.localPlayer ? !next.hideHead : !this.hideHead) {
                graphics2D.setColor(applyLighting(new Color(next.hRGB), next.x + fSin + (10.0f * f), (next.y + fAbs) - (20.0f * f)));
                graphics2D.fillRoundRect((int) (next.x + fSin + (10.0f * f)), (int) ((next.y + fAbs) - (20.0f * f)), (int) (20.0f * f), (int) (20.0f * f), (int) (5.0f * f), (int) (5.0f * f));
                Image image3 = getImage(next.fUrl);
                if (image3 != null) {
                    graphics2D.drawImage(image3, (int) (next.x + fSin + (10.0f * f)), (int) ((next.y + fAbs) - (20.0f * f)), (int) (20.0f * f), (int) (20.0f * f), (ImageObserver) null);
                }
            }
            graphics2D.setColor(applyLighting(new Color(next.bRGB), next.x + fSin, next.y + fAbs));
            graphics2D.fillRect((int) (next.x + fSin), (int) (next.y + fAbs), next.size, next.size);
            Image image4 = getImage(next.sUrl);
            if (image4 != null) {
                graphics2D.drawImage(image4, (int) (next.x + fSin), (int) (next.y + fAbs), next.size, next.size, (ImageObserver) null);
            }
            if (!this.heldItem.equals("none") && (image = getImage(this.items.get(this.heldItem).iconUrl)) != null && next == this.localPlayer) {
                graphics2D.drawImage(image, (int) (next.x + fSin + (25.0f * f)), (int) ((next.y + fAbs) - (15.0f * f)), (int) (15.0f * f), (int) (15.0f * f), (ImageObserver) null);
            }
            if (!this.hideNames) {
                boolean z = next == this.localPlayer ? this.isAfk : next.isAfk;
                String str = z ? "[AFK] " + next.name : next.name;
                graphics2D.setColor(z ? new Color(170, 170, 170) : Color.BLACK);
                graphics2D.drawString(str, (int) (next.x + fSin), (int) ((next.y + fAbs) - (25.0f * f)));
            }
            if (System.currentTimeMillis() - next.chatTime < 5000 && !next.chatBubble.isEmpty()) {
                graphics2D.setColor(new Color(255, 255, 255, 200));
                graphics2D.fillRoundRect((int) ((next.x + fSin) - 10.0f), (int) ((next.y + fAbs) - (65.0f * f)), 120, 30, 10, 10);
                graphics2D.setColor(Color.BLACK);
                graphics2D.drawString(next.chatBubble, (int) (next.x + fSin), (int) ((next.y + fAbs) - (45.0f * f)));
            }
        }
        for (MapObject mapObject2 : this.editorObjects) {
            if (mapObject2.npcType.equals("ENEMY")) {
                graphics2D.setColor(Color.RED);
                graphics2D.drawString("ENEMY", mapObject2.x, mapObject2.y - 10);
            }
        }
        if (this.lightingEnabled) {
            drawLighting(graphics2D);
        }
        graphics2D.setTransform(new AffineTransform());
        for (Player player : this.players.values()) {
            if (this.lightingEnabled && player == this.localPlayer) {
                graphics2D.setColor(new Color(0, 0, 0, 80));
                graphics2D.fillOval(((int) ((player.x + this.camX) + 15.0f)) - 15, (int) (((player.y + this.camY) + player.size) - 5.0f), 30, 8);
            }
        }
        if (this.gravityDisabled) {
            graphics2D.setColor(new Color(100, 200, 255, 200));
            graphics2D.setFont(new Font("Tahoma", 1, 13));
            graphics2D.drawString("[fly]", getWidth() - 50, 20);
        }
        if (this.isAfk) {
            graphics2D.setColor(new Color(200, 200, 200, 200));
            graphics2D.setFont(new Font("Tahoma", 1, 13));
            graphics2D.drawString("[AFK]", getWidth() - 55, this.gravityDisabled ? 38 : 20);
        }
        if (this.isChatting) {
            graphics2D.setColor(new Color(0, 0, 0, 150));
            graphics2D.fillRect(0, getHeight() - 40, getWidth(), 40);
            graphics2D.setColor(Color.WHITE);
            graphics2D.drawString("> " + this.currentChat + "_", 20, getHeight() - 15);
        }
    }

    private void drawEditor(Graphics2D graphics2D) {
        graphics2D.setColor(new Color(40, 40, 40));
        graphics2D.fillRect(0, 0, getWidth(), getHeight());
        graphics2D.translate(this.camX, this.camY);
        graphics2D.scale(this.zoom, this.zoom);
        graphics2D.setColor(new Color(60, 60, 60));
        for (int i = -2000; i < 4000; i += 40) {
            graphics2D.drawLine(i, -2000, i, 4000);
        }
        for (int i2 = -2000; i2 < 4000; i2 += 40) {
            graphics2D.drawLine(-2000, i2, 4000, i2);
        }
        for (MapObject mapObject : this.editorObjects) {
            drawObject(graphics2D, mapObject);
            if (mapObject.shape.equals("SPAWN")) {
                graphics2D.setColor(Color.BLACK);
                graphics2D.drawString("SPAWN", mapObject.x, mapObject.y + 25);
            }
            if (mapObject.npcType.equals("ENEMY")) {
                graphics2D.setColor(Color.RED);
                graphics2D.drawString("NPC", mapObject.x, mapObject.y - 10);
            }
            if (mapObject.isDynamite) {
                graphics2D.setColor(new Color(255, 100, 0));
                graphics2D.drawString("dinamit", mapObject.x, mapObject.y - 25);
            }
        }
        if (this.lightingEnabled) {
            for (Light light : this.lights) {
                graphics2D.setColor(new Color(light.color.getRed(), light.color.getGreen(), light.color.getBlue(), 100));
                graphics2D.setStroke(new BasicStroke(2.0f));
                graphics2D.drawOval(light.x - light.radius, light.y - light.radius, light.radius * 2, light.radius * 2);
                graphics2D.fillOval(light.x - 5, light.y - 5, 10, 10);
            }
        }
        if (this.selectedObject != null) {
            graphics2D.setColor(Color.YELLOW);
            graphics2D.setStroke(new BasicStroke(3.0f));
            graphics2D.drawRect(this.selectedObject.x, this.selectedObject.y, this.selectedObject.size, this.selectedObject.size);
            graphics2D.setStroke(new BasicStroke(1.0f));
        }
        graphics2D.setTransform(new AffineTransform());
        graphics2D.setColor(new Color(50, 50, 50, 220));
        graphics2D.fillRect(0, getHeight() - 185, getWidth(), 185);
        graphics2D.setColor(Color.WHITE);
        graphics2D.drawString("WASD: cam | Wheel: zoom | LMB: place/select | RMB: del | P: spawn | V: shape | G: texture", 20, getHeight() - 160);
        graphics2D.drawString("K: kill [" + this.isKill + "] | H: collider [" + this.hasCollider + "] | N: NPC [" + this.selectedNpcType + "] | J: vzrivopasnoe | L: skybox | M: music | E: script", 20, getHeight() - 135);
        graphics2D.drawString("I: lighting [" + (this.lightingEnabled ? "ON" : "OFF") + "] | Shift+U: new light | U: remove light", 20, getHeight() - 110);
        graphics2D.drawString("Q: gravity [" + (this.gravityDisabled ? "OFF" : "ON") + "] | X: commands [" + (this.commandsDisabled ? "OFF" : "ON") + "]", 20, getHeight() - 85);
        graphics2D.drawString("Type: " + this.selectedShape + " | Sky: " + this.skyUrl + " | Owner: " + this.currentWorldOwner, 20, getHeight() - 60);
        graphics2D.drawString("Enter: Save and Exit", 20, getHeight() - 35);
        if (!this.currentWorldOwner.equals("Unknown") && !this.currentWorldOwner.equals(this.username)) {
            graphics2D.setColor(Color.RED);
            graphics2D.drawString("CANNOT SAVE: You are not the owner!", 20, getHeight() - 10);
        }
    }

    private void openScriptEditor(MapObject mapObject) {
        JDialog jDialog = new JDialog((JFrame)SwingUtilities.getWindowAncestor(this), "Script Editor", true);
        jDialog.setSize(500, 400);
        jDialog.setLayout(new BorderLayout());
        JTextArea jTextArea = new JTextArea(mapObject.script);
        jTextArea.setFont(new Font("Monospaced", 0, 14));
        JButton jButton = new JButton("Save & Close");
        jButton.addActionListener(actionEvent -> {
            mapObject.script = jTextArea.getText();
            mapObject.parseInitialId();
            jDialog.dispose();
            requestFocus();
        });
        jDialog.add(new JScrollPane(jTextArea), "Center");
        jDialog.add(jButton, "South");
        jDialog.setLocationRelativeTo(this);
        jDialog.setVisible(true);
    }

    public void mouseMoved(MouseEvent mouseEvent) {
        this.mouseX = mouseEvent.getX();
        this.mouseY = mouseEvent.getY();
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        this.mouseX = mouseEvent.getX();
        this.mouseY = mouseEvent.getY();
        if (this.currentState == State.EDITOR) {
            int x = (((int) ((mouseEvent.getX() - this.camX) / this.zoom)) / 40) * 40;
            int y = (((int) ((mouseEvent.getY() - this.camY) / this.zoom)) / 40) * 40;
            if (!this.mousePaintLeft) {
                if (this.mousePaintRight) {
                    this.editorObjects.removeIf(mapObject -> {
                        return mapObject.x / 40 == x / 40 && mapObject.y / 40 == y / 40;
                    });
                    this.selectedObject = null;
                    return;
                }
                return;
            }
            boolean z = false;
            Iterator<MapObject> it = this.editorObjects.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                MapObject next = it.next();
                if (next.x / 40 == x / 40 && next.y / 40 == y / 40) {
                    z = true;
                    break;
                }
            }
            if (!z) {
                if (this.selectedShape.equals("SPAWN")) {
                    this.editorObjects.removeIf(mapObject2 -> {
                        return mapObject2.shape.equals("SPAWN");
                    });
                }
                MapObject mapObject3 = new MapObject(x, y, this.selectedColor, this.selectedShape, this.isStatic, this.selectedTexture, this.hasCollider, this.isKill, "");
                mapObject3.npcType = this.selectedNpcType;
                mapObject3.isDynamite = false;
                this.editorObjects.add(mapObject3);
            }
        }
    }

    public void mousePressed(MouseEvent mouseEvent) {
        playSound("click.wav");
        if (this.currentState != State.EDITOR) {
            return;
        }
        if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
            this.mousePaintLeft = true;
        }
        if (SwingUtilities.isRightMouseButton(mouseEvent)) {
            this.mousePaintRight = true;
        }
        int x = (int) ((mouseEvent.getX() - this.camX) / this.zoom);
        int y = (int) ((mouseEvent.getY() - this.camY) / this.zoom);
        int i = (x / 40) * 40;
        int i2 = (y / 40) * 40;
        if (!SwingUtilities.isLeftMouseButton(mouseEvent)) {
            if (SwingUtilities.isRightMouseButton(mouseEvent)) {
                this.editorObjects.removeIf(mapObject -> {
                    return mapObject.x / 40 == i / 40 && mapObject.y / 40 == i2 / 40;
                });
                this.selectedObject = null;
                return;
            }
            return;
        }
        MapObject mapObject2 = null;
        int size = this.editorObjects.size() - 1;
        while (true) {
            if (size < 0) {
                break;
            }
            MapObject mapObject3 = this.editorObjects.get(size);
            if (x < mapObject3.x || x > mapObject3.x + mapObject3.size || y < mapObject3.y || y > mapObject3.y + mapObject3.size) {
                size--;
            } else {
                mapObject2 = mapObject3;
                break;
            }
        }
        if (mapObject2 != null) {
            this.selectedObject = mapObject2;
            this.selectedShape = mapObject2.shape;
            this.selectedColor = mapObject2.color;
            this.selectedTexture = mapObject2.textureUrl;
            this.hasCollider = mapObject2.hasCollider;
            this.isKill = mapObject2.isKill;
            this.isStatic = mapObject2.isStatic;
            this.selectedNpcType = mapObject2.npcType;
            return;
        }
        if (this.selectedShape.equals("SPAWN")) {
            this.editorObjects.removeIf(mapObject4 -> {
                return mapObject4.shape.equals("SPAWN");
            });
        }
        MapObject mapObject5 = new MapObject(i, i2, this.selectedColor, this.selectedShape, this.isStatic, this.selectedTexture, this.hasCollider, this.isKill, "");
        mapObject5.npcType = this.selectedNpcType;
        this.editorObjects.add(mapObject5);
        this.selectedObject = mapObject5;
    }

    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        float f;
        if (this.currentState == State.EDITOR) {
            if (mouseWheelEvent.getWheelRotation() < 0) {
                float f2 = this.zoom * 1.1f;
                f = f2;
                this.zoom = f2;
            } else {
                float f3 = this.zoom / 1.1f;
                f = f3;
                this.zoom = f3;
            }
            this.zoom = f;
            this.zoom = Math.max(0.2f, Math.min(this.zoom, 3.0f));
        }
    }

    public void keyPressed(KeyEvent keyEvent) {
        String strShowInputDialog;
        String strShowInputDialog2;
        String strShowInputDialog3;
        Color colorShowDialog;
        if (keyEvent.getKeyCode() == 114) {
            this.showDebug = !this.showDebug;
            return;
        }
        if (this.currentState == State.EDITOR) {
            if (keyEvent.getKeyCode() == 69) {
                int i = (int) ((this.mouseX - this.camX) / this.zoom);
                int i2 = (int) ((this.mouseY - this.camY) / this.zoom);
                Iterator<MapObject> it = this.editorObjects.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    MapObject next = it.next();
                    if (i >= next.x && i <= next.x + next.size && i2 >= next.y && i2 <= next.y + next.size) {
                        openScriptEditor(next);
                        break;
                    }
                }
            }
            if (keyEvent.getKeyCode() == 87) {
                this.camY += 40.0f;
            }
            if (keyEvent.getKeyCode() == 83) {
                this.camY -= 40.0f;
            }
            if (keyEvent.getKeyCode() == 65) {
                this.camX += 40.0f;
            }
            if (keyEvent.getKeyCode() == 68) {
                this.camX -= 40.0f;
            }
            if (keyEvent.getKeyCode() == 67 && (colorShowDialog = JColorChooser.showDialog(this, "Color", this.selectedColor)) != null) {
                this.selectedColor = colorShowDialog;
                if (this.selectedObject != null) {
                    this.selectedObject.color = colorShowDialog;
                }
            }
            if (keyEvent.getKeyCode() == 71 && (strShowInputDialog3 = JOptionPane.showInputDialog(this, "Texture URL (png/jpg) or 'none':", this.selectedTexture)) != null) {
                this.selectedTexture = strShowInputDialog3;
                if (this.selectedObject != null) {
                    this.selectedObject.textureUrl = strShowInputDialog3;
                }
            }
            if (keyEvent.getKeyCode() == 76 && (strShowInputDialog2 = JOptionPane.showInputDialog(this, "Skybox URL (png/jpg) or 'none':", this.skyUrl)) != null) {
                this.skyUrl = strShowInputDialog2;
                saveSkinData();
            }
            if (keyEvent.getKeyCode() == 77 && (strShowInputDialog = JOptionPane.showInputDialog(this, "Music URL (mp3/wav) or 'none':", this.musicUrl)) != null) {
                this.musicUrl = strShowInputDialog;
                playMusicFromUrl(this.musicUrl);
            }
            if (keyEvent.getKeyCode() == 86) {
                this.selectedShape = this.selectedShape.equals("CUBE") ? "SPHERE" : this.selectedShape.equals("SPHERE") ? "TRIANGLE" : "CUBE";
                if (this.selectedObject != null) {
                    this.selectedObject.shape = this.selectedShape;
                }
            }
            if (keyEvent.getKeyCode() == 70) {
                this.isStatic = !this.isStatic;
                if (this.selectedObject != null) {
                    this.selectedObject.isStatic = this.isStatic;
                }
            }
            if (keyEvent.getKeyCode() == 75) {
                this.isKill = !this.isKill;
                if (this.selectedObject != null) {
                    this.selectedObject.isKill = this.isKill;
                }
            }
            if (keyEvent.getKeyCode() == 72) {
                this.hasCollider = !this.hasCollider;
                if (this.selectedObject != null) {
                    this.selectedObject.hasCollider = this.hasCollider;
                }
            }
            if (keyEvent.getKeyCode() == 78) {
                this.selectedNpcType = this.selectedNpcType.equals("ENEMY") ? "none" : "ENEMY";
                if (this.selectedObject != null) {
                    this.selectedObject.npcType = this.selectedNpcType;
                }
            }
            if (keyEvent.getKeyCode() == 74 && this.selectedObject != null) {
                this.selectedObject.isDynamite = !this.selectedObject.isDynamite;
            }
            if (keyEvent.getKeyCode() == 73) {
                this.lightingEnabled = !this.lightingEnabled;
            }
            if (keyEvent.getKeyCode() == 81) {
                this.gravityDisabled = !this.gravityDisabled;
            }
            if (keyEvent.getKeyCode() == 88) {
                this.commandsDisabled = !this.commandsDisabled;
            }
            if (keyEvent.getKeyCode() == 85 && (keyEvent.getModifiersEx() & 64) != 0) {
                int i3 = (int) ((this.mouseX - this.camX) / this.zoom);
                int i4 = (int) ((this.mouseY - this.camY) / this.zoom);
                String strShowInputDialog4 = JOptionPane.showInputDialog(this, "Radius, Strength (0-1) [def: 200,0.8]:", "200,0.8");
                if (strShowInputDialog4 != null) {
                    try {
                        String[] strArrSplit = strShowInputDialog4.split(",");
                        int i5 = Integer.parseInt(strArrSplit[0]);
                        float f = Float.parseFloat(strArrSplit[1]);
                        Color colorShowDialog2 = JColorChooser.showDialog(this, "Light Color", Color.YELLOW);
                        if (colorShowDialog2 != null) {
                            this.lights.add(new Light(i3, i4, i5, f, colorShowDialog2));
                        }
                    } catch (Exception e) {
                    }
                }
            }
            if (keyEvent.getKeyCode() == 85 && (keyEvent.getModifiersEx() & 64) == 0) {
                int i6 = (int) ((this.mouseX - this.camX) / this.zoom);
                int i7 = (int) ((this.mouseY - this.camY) / this.zoom);
                Iterator<Light> it2 = this.lights.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    Light next2 = it2.next();
                    if (((float) Math.sqrt(Math.pow(next2.x - i6, 2.0d) + Math.pow(next2.y - i7, 2.0d))) < 40.0f) {
                        this.lights.remove(next2);
                        break;
                    }
                }
            }
            if (keyEvent.getKeyCode() == 80) {
                this.selectedShape = "SPAWN";
                this.selectedColor = Color.GREEN;
                this.selectedTexture = "none";
            }
            if (keyEvent.getKeyCode() == 10) {
                StringBuilder sb = new StringBuilder();
                Iterator<MapObject> it3 = this.editorObjects.iterator();
                while (it3.hasNext()) {
                    sb.append(it3.next().toString()).append(";");
                }
                sb.append("|").append(this.skyUrl).append("|").append(this.isOffline ? this.username : this.currentWorldOwner).append("|").append(this.musicUrl);
                saveLights(sb);
                if (this.gravityDisabled || this.commandsDisabled) {
                    sb.append("|SETTINGS:");
                    if (this.gravityDisabled) {
                        sb.append("nograv,");
                    }
                    if (this.commandsDisabled) {
                        sb.append("nocmds,");
                    }
                }
                String string = sb.toString();
                saveWorldToCache(this.currentWorldName, string);
                if (!this.isOffline && this.out != null && (this.currentWorldOwner.equals("Unknown") || this.currentWorldOwner.equals(this.username))) {
                    this.out.println("SAVE_MAP " + this.currentWorldName + " " + string);
                }
                this.currentState = State.MENU;
                toggleMenu(true);
                return;
            }
            return;
        }
        if (!this.isChatting) {
            if (keyEvent.getKeyCode() == 84) {
                this.isChatting = true;
                return;
            }
            if (keyEvent.getKeyCode() == 89) {
                this.heldItem = this.heldItem.equals("pruzina") ? "none" : "pruzina";
                return;
            }
            if (keyEvent.getKeyCode() == 87) {
                this.keyW = true;
            }
            if (keyEvent.getKeyCode() == 65) {
                this.keyA = true;
            }
            if (keyEvent.getKeyCode() == 83) {
                this.keyS = true;
            }
            if (keyEvent.getKeyCode() == 68) {
                this.keyD = true;
            }
            if (keyEvent.getKeyCode() == 32) {
                this.keySpace = true;
            }
            if (keyEvent.getKeyCode() == 27) {
                restartClient();
                return;
            }
            return;
        }
        if (keyEvent.getKeyCode() == 10) {
            if (!this.currentChat.isEmpty()) {
                if (this.currentChat.startsWith("/")) {
                    if (this.commandsDisabled && !this.username.equals(this.currentWorldOwner)) {
                        this.localPlayer.chatBubble = "Commands disabled.";
                        this.localPlayer.chatTime = System.currentTimeMillis();
                        this.currentChat = "";
                        this.isChatting = false;
                        return;
                    }
                    if (this.currentChat.equalsIgnoreCase("/kill")) {
                        respawn();
                        this.localPlayer.chatBubble = "* suicide *";
                        this.localPlayer.chatTime = System.currentTimeMillis();
                    } else if (this.currentChat.equalsIgnoreCase("/dance")) {
                        this.localPlayer.danceUntil = System.currentTimeMillis() + 3000;
                        this.localPlayer.chatBubble = "* dance *";
                        this.localPlayer.chatTime = System.currentTimeMillis();
                    } else if (this.currentChat.equalsIgnoreCase("/getpos")) {
                        this.localPlayer.chatBubble = "Pos: " + ((int) this.localPlayer.x) + " " + ((int) this.localPlayer.y);
                        this.localPlayer.chatTime = System.currentTimeMillis();
                    } else if (this.currentChat.equalsIgnoreCase("/list")) {
                        StringBuilder sb2 = new StringBuilder("Players: ");
                        Iterator<Player> it4 = this.players.values().iterator();
                        while (it4.hasNext()) {
                            sb2.append(it4.next().name).append(", ");
                        }
                        this.localPlayer.chatBubble = sb2.toString();
                        this.localPlayer.chatTime = System.currentTimeMillis();
                    } else if (this.currentChat.equalsIgnoreCase("/spawn")) {
                        respawn();
                        this.localPlayer.chatBubble = "Teleported to spawn.";
                        this.localPlayer.chatTime = System.currentTimeMillis();
                    } else if (this.currentChat.equalsIgnoreCase("/afk")) {
                        this.isAfk = !this.isAfk;
                        this.localPlayer.chatBubble = this.isAfk ? "* AFK *" : "* Returned *";
                        this.localPlayer.chatTime = System.currentTimeMillis();
                    } else if (this.currentChat.equalsIgnoreCase("/hidehead")) {
                        this.hideHead = !this.hideHead;
                        this.localPlayer.chatBubble = this.hideHead ? " *head hid* " : "* head showed *";
                        this.localPlayer.chatTime = System.currentTimeMillis();
                    } else if (this.currentChat.toLowerCase().startsWith("/tp ")) {
                        String strTrim = this.currentChat.substring(4).trim();
                        boolean z = false;
                        Iterator<Player> it5 = this.players.values().iterator();
                        while (true) {
                            if (!it5.hasNext()) {
                                break;
                            }
                            Player next3 = it5.next();
                            if (next3.name.equalsIgnoreCase(strTrim) && !next3.name.equals(this.username)) {
                                this.localPlayer.x = next3.x;
                                this.localPlayer.y = next3.y;
                                this.localPlayer.velY = 0.0f;
                                this.localPlayer.chatBubble = "-> " + next3.name;
                                this.localPlayer.chatTime = System.currentTimeMillis();
                                z = true;
                                break;
                            }
                        }
                        if (!z) {
                            this.localPlayer.chatBubble = "not found: " + strTrim;
                            this.localPlayer.chatTime = System.currentTimeMillis();
                        }
                    } else if (this.currentChat.equalsIgnoreCase("/hide")) {
                        if (this.username.equals(this.currentWorldOwner)) {
                            if (!this.isOffline && this.out != null) {
                                this.out.println("CHAT " + this.username + " /hide");
                            }
                            this.hideNames = !this.hideNames;
                            this.localPlayer.chatBubble = "* nicknames " + (this.hideNames ? "hid*" : "showed *");
                        } else {
                            this.localPlayer.chatBubble = "You aren't owner.";
                        }
                        this.localPlayer.chatTime = System.currentTimeMillis();
                    }
                } else {
                    String strSubstring = this.currentChat;
                    if (strSubstring.length() > 20) {
                        strSubstring = strSubstring.substring(0, 20);
                    }
                    if (!this.isOffline && this.out != null) {
                        this.out.println("CHAT " + this.username + " " + strSubstring);
                    }
                    this.localPlayer.chatBubble = strSubstring;
                    this.localPlayer.chatTime = System.currentTimeMillis();
                }
            }
            this.currentChat = "";
            this.isChatting = false;
            return;
        }
        if (keyEvent.getKeyCode() == 8 && this.currentChat.length() > 0) {
            this.currentChat = this.currentChat.substring(0, this.currentChat.length() - 1);
            return;
        }
        char keyChar = keyEvent.getKeyChar();
        if (keyChar != 65535 && !Character.isISOControl(keyChar) && this.currentChat.length() < 20) {
            this.currentChat += keyChar;
        }
    }

    public void keyReleased(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == 87) {
            this.keyW = false;
        }
        if (keyEvent.getKeyCode() == 65) {
            this.keyA = false;
        }
        if (keyEvent.getKeyCode() == 83) {
            this.keyS = false;
        }
        if (keyEvent.getKeyCode() == 68) {
            this.keyD = false;
        }
        if (keyEvent.getKeyCode() == 32) {
            this.keySpace = false;
        }
    }

    public void keyTyped(KeyEvent keyEvent) {
    }

    public void mouseClicked(MouseEvent mouseEvent) {
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
            this.mousePaintLeft = false;
        }
        if (SwingUtilities.isRightMouseButton(mouseEvent)) {
            this.mousePaintRight = false;
        }
    }

    public void mouseEntered(MouseEvent mouseEvent) {
    }

    public void mouseExited(MouseEvent mouseEvent) {
    }

    public static void main(String[] strArr) {
        JFrame jFrame = new JFrame("PukiBlox");
        jFrame.setDefaultCloseOperation(3);
        jFrame.add(new Dolinablox());
        jFrame.pack();
        jFrame.setLocationRelativeTo((Component) null);
        jFrame.setVisible(true);
    }

    class Player {
        float x;
        float y;
        float velY;
        String name;
        int hRGB;
        int bRGB;
        float speed = 5.0f;
        float jumpForce = -15.0f;
        int size = 40;
        String chatBubble = "";
        String sUrl = "none";
        String fUrl = "none";
        long chatTime = 0;
        boolean isGrounded = false;
        long danceUntil = 0;
        boolean isAfk = false;
        boolean hideHead = false;

        Player(String str, float f, float f2) {
            this.name = str;
            this.x = f;
            this.y = f2;
            this.hRGB = Dolinablox.this.headColor.getRGB();
            this.bRGB = Dolinablox.this.bodyColor.getRGB();
        }
    }

    class MapObject {
        int x;
        int y;
        Color color;
        String shape;
        boolean isStatic;
        boolean hasCollider;
        boolean isKill;
        String textureUrl;
        String script;
        int size = 40;
        float velY = 0.0f;
        String id = "";
        int targetX = -1;
        int targetY = -1;
        long waitUntil = 0;
        String npcType = "none";
        String targetPlayerName = "";
        int searchRadius = 300;
        boolean isDynamite = false;
        long explodeTime = 0;
        int explosionRadius = 150;

        MapObject(int i, int i2, Color color, String str, boolean z, String str2, boolean z2, boolean z3, String str3) {
            this.textureUrl = "none";
            this.script = "";
            this.x = i;
            this.y = i2;
            this.color = color;
            this.shape = str;
            this.isStatic = z;
            this.textureUrl = str2;
            this.hasCollider = z2;
            this.isKill = z3;
            this.script = str3;
            parseInitialId();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void parseInitialId() {
            if (this.script == null || this.script.isEmpty()) {
                return;
            }
            for (String str : this.script.split("\n")) {
                if (str.trim().startsWith("set_id")) {
                    String[] strArrSplit = str.trim().split(" ");
                    if (strArrSplit.length > 1) {
                        this.id = strArrSplit[1];
                        return;
                    }
                    return;
                }
            }
        }

        public String toString() {
            return String.format("%d,%d,%d,%s,%b,%s,%b,%b,%s,%d,%s,%d,%b", Integer.valueOf(this.x), Integer.valueOf(this.y), Integer.valueOf(this.color.getRGB()), this.shape, Boolean.valueOf(this.isStatic), this.textureUrl, Boolean.valueOf(this.hasCollider), Boolean.valueOf(this.isKill), Base64.getEncoder().encodeToString(this.script.getBytes(StandardCharsets.UTF_8)), Integer.valueOf(this.size), this.npcType, Integer.valueOf(this.searchRadius), Boolean.valueOf(this.isDynamite));
        }
    }

    /* JADX INFO: loaded from: Dolinablox.jar:Dolinablox$Light.class */
    class Light {
        int x;
        int y;
        int radius;
        float intensity;
        Color color;

        Light(int i, int i2, int i3, float f, Color color) {
            this.x = i;
            this.y = i2;
            this.radius = i3;
            this.intensity = Math.max(0.0f, Math.min(1.0f, f));
            this.color = color;
        }

        float getBrightness(float f, float f2) {
            float f3 = f - this.x;
            float f4 = f2 - this.y;
            float fSqrt = (float) Math.sqrt((f3 * f3) + (f4 * f4));
            if (fSqrt > this.radius) {
                return 0.0f;
            }
            return this.intensity * (1.0f - (fSqrt / this.radius));
        }
    }

    /* JADX INFO: loaded from: Dolinablox.jar:Dolinablox$Item.class */
    class Item {
        String name;
        String iconUrl;
        float jumpMultiplier;
        float speedMultiplier;

        Item(String str, String str2, float f, float f2) {
            this.name = str;
            this.iconUrl = str2;
            this.jumpMultiplier = f;
            this.speedMultiplier = f2;
        }
    }
}
