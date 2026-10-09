import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefBeforeDownloadCallback;
import org.cef.callback.CefDownloadItem;
import org.cef.callback.CefDownloadItemCallback;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefDownloadHandler;
import org.cef.handler.CefLifeSpanHandlerAdapter;

import me.friwi.jcefmaven.CefAppBuilder;

public class MainBrowser extends JFrame {

    private static CefApp cefApp_;
    private static CefClient client_;
    
    public JTabbedPane tabbedPane;
    public GingerbreadTextField urlField;
    public static JLabel statusBarLabel;
    private boolean isCreatingTab = false;

    public MainBrowser() {
        setTitle("GingerbreadWeb");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            File iconFile = new File("pngs/web_logo.png");
            if (iconFile.exists()) setIconImage(ImageIO.read(iconFile));
        } catch (Exception e) {
            System.out.println("[ICON] Logo loaded silently");
        }

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(40, 40, 40)); 
        topPanel.setLayout(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        leftButtonsPanel.setOpaque(false);

        GingerbreadButton backButton = new GingerbreadButton("Back", "pngs/btn_default_normal.9.png", "pngs/btn_default_pressed.9.png", "pngs/btn_default_selected.9.png", 6);
        backButton.setPreferredSize(new Dimension(75, 35));
        backButton.addActionListener(e -> {
            CefBrowser activeBrowser = getActiveBrowser();
            if (activeBrowser != null && activeBrowser.canGoBack()) activeBrowser.goBack();
        });

        GingerbreadButton nextButton = new GingerbreadButton("Next", "pngs/btn_default_normal.9.png", "pngs/btn_default_pressed.9.png", "pngs/btn_default_selected.9.png", 6);
        nextButton.setPreferredSize(new Dimension(75, 35));
        nextButton.addActionListener(e -> {
            CefBrowser activeBrowser = getActiveBrowser();
            if (activeBrowser != null && activeBrowser.canGoForward()) activeBrowser.goForward();
        });

        GingerbreadButton arrowButton = new GingerbreadButton("...", "pngs/btn_default_normal.9.png", "pngs/btn_default_pressed.9.png", "pngs/btn_default_selected.9.png", 6);
        arrowButton.setPreferredSize(new Dimension(40, 35));

        leftButtonsPanel.add(backButton);
        leftButtonsPanel.add(nextButton);
        leftButtonsPanel.add(arrowButton);

        urlField = new GingerbreadTextField("https://google.com", "pngs/textfield_default.9.png", "pngs/textfield_selected.9.png", 6);
        urlField.setFont(new Font("Roboto", Font.PLAIN, 14));
        urlField.addActionListener(e -> {
            CefBrowser activeBrowser = getActiveBrowser();
            if (activeBrowser != null) {
                String url = urlField.getText();
                if (!url.startsWith("http://") && !url.startsWith("https://")) url = "https://" + url;
                activeBrowser.loadURL(url);
            }
        });

        GingerbreadButton refreshButton = new GingerbreadButton("Refresh", "pngs/btn_default_normal.9.png", "pngs/btn_default_pressed.9.png", "pngs/btn_default_selected.9.png", 6);
        refreshButton.setPreferredSize(new Dimension(85, 35));
        refreshButton.addActionListener(e -> {
            CefBrowser activeBrowser = getActiveBrowser();
            if (activeBrowser != null) activeBrowser.reload();
        });

        topPanel.add(leftButtonsPanel, BorderLayout.WEST);
        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(refreshButton, BorderLayout.EAST);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(new Color(30, 30, 30)); 
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(20, 20, 20)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        statusBarLabel = new JLabel("Done");
        statusBarLabel.setFont(new Font("Roboto", Font.PLAIN, 12));
        statusBarLabel.setForeground(Color.LIGHT_GRAY);
        bottomBar.add(statusBarLabel, BorderLayout.WEST);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Roboto", Font.PLAIN, 12));
        tabbedPane.setUI(new ChromeTabUI());
        tabbedPane.setBackground(Color.BLACK);
        tabbedPane.setOpaque(true);
        
        tabbedPane.addChangeListener(e -> {
            if (isCreatingTab) return;
            int selectedIndex = tabbedPane.getSelectedIndex();
            int totalTabs = tabbedPane.getTabCount();
            
            if (selectedIndex >= 0 && totalTabs > 1) {
                if (selectedIndex == totalTabs - 1) {
                    createNewTab("https://google.com");
                } else {
                    CefBrowser activeBrowser = getActiveBrowser();
                    if (activeBrowser != null && !urlField.hasFocus()) {
                        urlField.setText(activeBrowser.getURL());
                    }
                }
            }
        });


        createNewTab("https://google.com");
        tabbedPane.addTab("     ", new JPanel());

        JPopupMenu settingsMenu = new JPopupMenu();
        settingsMenu.setBackground(new Color(30, 30, 30));
        
        JMenuItem camPermission = new JMenuItem("Website Site Permissions");
        camPermission.setFont(new Font("Roboto", Font.PLAIN, 12));
        camPermission.setForeground(Color.WHITE); camPermission.setBackground(new Color(40, 40, 40));
        camPermission.addActionListener(click -> {
            CefBrowser activeBrowser = getActiveBrowser();
            JOptionPane.showMessageDialog(this, "Permissions for: " + (activeBrowser != null ? activeBrowser.getURL() : "Unknown") + "\n\n📷 Camera: ALLOWED\n🎙️ Mic: ALLOWED", "Permissions", JOptionPane.INFORMATION_MESSAGE);
        });

        JMenuItem ariaSettings = new JMenuItem("Aria2 Engine Info");
        ariaSettings.setFont(new Font("Roboto", Font.PLAIN, 12));
        ariaSettings.setForeground(Color.WHITE); ariaSettings.setBackground(new Color(40, 40, 40));
        ariaSettings.addActionListener(click -> {
            JOptionPane.showMessageDialog(this, 
                "GingerbreadWeb Core Download Subsystem:\n\n" +
                "• Engine: Aria2 Standalone Worker\n" +
                "• Parallel Streams: 16 accelerated connections\n" +
                "• Secure DNS Fallback: 8.8.8.8, 1.1.1.1\n" +
                "• Split Size Threshold: 1M per block\n\n" +
                "Status: ACTIVE & MONITORING", 
                "Aria2 Engine Config", JOptionPane.INFORMATION_MESSAGE);
        });

        settingsMenu.add(camPermission);
        settingsMenu.add(ariaSettings);
        arrowButton.addActionListener(e -> settingsMenu.show(arrowButton, 0, arrowButton.getHeight()));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(bottomBar, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                CefApp.getInstance().dispose();
                System.exit(0);
            }
        });
    }

    public void createNewTab(String startUrl) {
        isCreatingTab = true;

        CefBrowser tabBrowser = client_.createBrowser(startUrl, false, false);
        Component tabUI = tabBrowser.getUIComponent();

        JPanel tabContentPanel = new JPanel(new BorderLayout());
        tabContentPanel.setBackground(Color.BLACK);
        tabContentPanel.add(tabUI, BorderLayout.CENTER);
        tabContentPanel.putClientProperty("browserInstance", tabBrowser);

        int totalTabs = tabbedPane.getTabCount();
        int targetIndex = (totalTabs == 0) ? 0 : totalTabs - 1;

        if (totalTabs == 0) {
            tabbedPane.addTab("", tabContentPanel); 
        } else {
            tabbedPane.insertTab("", null, tabContentPanel, null, targetIndex);
        }

        JPanel tabTitlePanel = new JPanel(new BorderLayout(5, 0));
        tabTitlePanel.setOpaque(false);
        tabTitlePanel.setPreferredSize(new Dimension(100, 24)); 
        
        JLabel titleLabel = new JLabel("Loading...");
        titleLabel.setFont(new Font("Roboto", Font.PLAIN, 12));
        tabTitlePanel.add(titleLabel, BorderLayout.CENTER); 
        
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Roboto", Font.BOLD, 14));
        closeButton.setBorder(null);
        closeButton.setContentAreaFilled(false); closeButton.setFocusable(false);
        closeButton.setPreferredSize(new Dimension(14, 14)); closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        closeButton.addActionListener(e -> {
            isCreatingTab = true; 
            int index = tabbedPane.indexOfComponent(tabContentPanel);
            if (index >= 0 && tabbedPane.getTabCount() > 2) {
    // Yeah bro i'm tired of pressing tab and space, i found a way to save sum time, maybe sometimes will use tab, ok, just don't mind me, i mean the code will look worse,  ok? I hope you don't mind
int nextSelectIndex = (tabbedPane.getSelectedIndex() == index) ? index - 1 : tabbedPane.getSelectedIndex();
if (nextSelectIndex < 0) nextSelectIndex = 0;
tabBrowser.close(true);
tabbedPane.remove(index);
tabbedPane.setSelectedIndex(nextSelectIndex);
CefBrowser activeBrowser = getActiveBrowser();
if (activeBrowser != null) urlField.setText(activeBrowser.getURL());
} else {
JOptionPane.showMessageDialog(this, "Cannot close the last remaining tab!", "GingerbreadWeb", JOptionPane.WARNING_MESSAGE);
}
isCreatingTab = false;
});
tabTitlePanel.add(closeButton, BorderLayout.EAST);
tabbedPane.setTabComponentAt(targetIndex, tabTitlePanel);
tabbedPane.setSelectedIndex(targetIndex);
isCreatingTab = false;
}
public CefBrowser getActiveBrowser() {
int selectedIndex = tabbedPane.getSelectedIndex();
if (selectedIndex >= 0 && selectedIndex < tabbedPane.getTabCount() - 1) {
Component selectedComponent = tabbedPane.getComponentAt(selectedIndex);
if (selectedComponent instanceof JPanel) {
return (CefBrowser) ((JPanel) selectedComponent).getClientProperty("browserInstance");
}
}
return null;
}
private static void copyImageToClipboard(String imageUrl) {
new Thread(() -> {
try {
java.net.URL url = new java.net.URL(imageUrl);
Image image = ImageIO.read(url);
if (image != null) {
java.awt.datatransfer.Transferable selection = new java.awt.datatransfer.Transferable() {
@Override public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() { return new java.awt.datatransfer.DataFlavor[]{java.awt.datatransfer.DataFlavor.imageFlavor}; }
@Override public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor flavor) { return java.awt.datatransfer.DataFlavor.imageFlavor.equals(flavor); }
@Override public Object getTransferData(java.awt.datatransfer.DataFlavor flavor) throws java.awt.datatransfer.UnsupportedFlavorException {
if (!isDataFlavorSupported(flavor)) throw new java.awt.datatransfer.UnsupportedFlavorException(flavor);
return image;
}
};
Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
}
} catch (Exception e) {
System.err.println("[CLIPBOARD ERROR]: " + e.getMessage());
}
}).start();
}
public static void main(String[] args) {
new Thread(() -> {
try {
CefAppBuilder builder = new CefAppBuilder();
builder.setInstallDir(new File("jcef_bundle"));
builder.getCefSettings().windowless_rendering_enabled = false;
cefApp_ = builder.build();
client_ = cefApp_.createClient();
client_.addDisplayHandler(new CefDisplayHandlerAdapter() {
@Override
public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
SwingUtilities.invokeLater(() -> {
MainBrowser mainFrame = (MainBrowser) JFrame.getFrames()[0];
CefBrowser active = mainFrame.getActiveBrowser();
if (active == browser && frame.isMain() && !mainFrame.urlField.hasFocus()) {
mainFrame.urlField.setText(url);
}
});
}
@Override
public void onTitleChange(CefBrowser browser, String title) {
SwingUtilities.invokeLater(() -> {
MainBrowser mainFrame = (MainBrowser) JFrame.getFrames()[0];
for (int i = 0; i < mainFrame.tabbedPane.getTabCount() - 1; i++) {
JPanel panel = (JPanel) mainFrame.tabbedPane.getComponentAt(i);
if (panel.getClientProperty("browserInstance") == browser) {
JPanel titlePanel = (JPanel) mainFrame.tabbedPane.getTabComponentAt(i);
if (titlePanel != null) {
JLabel label = (JLabel) titlePanel.getComponent(0);
label.setText(title.length() > 14 ? title.substring(0, 11) + "..." : title);
}
if (mainFrame.tabbedPane.getSelectedIndex() == i) {
mainFrame.setTitle(title + " - GingerbreadWeb");
mainFrame.statusBarLabel.setText("Done");
}
break;
}
}
});
}
@Override
public void onStatusMessage(CefBrowser browser, String value) {
SwingUtilities.invokeLater(() -> {
MainBrowser mainFrame = (MainBrowser) JFrame.getFrames()[0];
CefBrowser active = mainFrame.getActiveBrowser();
if (active == browser) {
statusBarLabel.setText(value != null && !value.isEmpty() ? "Link: " + value : "Done");
}
});
}
});
client_.addContextMenuHandler(new org.cef.handler.CefContextMenuHandlerAdapter() {
@Override
public void onBeforeContextMenu(CefBrowser browser, org.cef.browser.CefFrame frame, org.cef.callback.CefContextMenuParams params, org.cef.callback.CefMenuModel model) {
model.clear();
if (params.getLinkUrl() != null && !params.getLinkUrl().isEmpty()) {
model.addItem(101, "Open Link in New Tab");
model.addItem(102, "Copy Link Address");
}
if (params.getSourceUrl() != null && !params.getSourceUrl().isEmpty()) {
model.addItem(201, "Copy Image");
model.addItem(202, "Save Image via Aria2");
}
}
@Override
public boolean onContextMenuCommand(CefBrowser browser, org.cef.browser.CefFrame frame, org.cef.callback.CefContextMenuParams params, int commandId, int eventFlags) {
MainBrowser mainFrame = (MainBrowser) JFrame.getFrames()[0];
if (commandId == 101) {
SwingUtilities.invokeLater(() -> mainFrame.createNewTab(params.getLinkUrl()));
return true;
}
if (commandId == 102) {
SwingUtilities.invokeLater(() -> {
java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(params.getLinkUrl());
Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
});
return true;
}
if (commandId == 201) {
copyImageToClipboard(params.getSourceUrl());
return true;
}
if (commandId == 202) {
String imgUrl = params.getSourceUrl();
String suggestedName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1);
if (suggestedName.contains("?")) suggestedName = suggestedName.substring(0, suggestedName.indexOf('?'));
final String finalName = suggestedName.isEmpty() ? "image.png" : suggestedName;
SwingUtilities.invokeLater(() -> {
JFileChooser fileChooser = new JFileChooser(); fileChooser.setSelectedFile(new File(finalName));
if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
triggerAria2Download(imgUrl, fileChooser.getSelectedFile().getParent(), fileChooser.getSelectedFile().getName());
}
});
return true;
}
return false;
}
});
client_.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
@Override
public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String target_url, String target_frame_name) {
SwingUtilities.invokeLater(() -> {
MainBrowser mainFrame = (MainBrowser) JFrame.getFrames()[0];
mainFrame.createNewTab(target_url);
});
return true;
}
});
                client_.addDownloadHandler(new CefDownloadHandler() {
                    @Override
                    public boolean onBeforeDownload(CefBrowser browser, CefDownloadItem downloadItem, String suggestedName, CefBeforeDownloadCallback callback) {
                        SwingUtilities.invokeLater(() -> {
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setSelectedFile(new File(suggestedName));
                            fileChooser.setDialogTitle("Save File - GingerbreadWeb");
                            
                            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                                File fileToSave = fileChooser.getSelectedFile();
                                callback.Continue(fileToSave.getAbsolutePath(), true);
                                System.out.println("[DOWNLOAD] Native transfer started: " + fileToSave.getName());
                            } else {
                                callback.Continue("", false);
                            }
                        });
                        return true;
                    }

                    @Override
                    public void onDownloadUpdated(CefBrowser browser, CefDownloadItem downloadItem, CefDownloadItemCallback callback) {
                        if (downloadItem.isInProgress()) {
                            SwingUtilities.invokeLater(() -> 
                                statusBarLabel.setText("Downloading: " + downloadItem.getPercentComplete() + "%")
                            );
                        } else if (downloadItem.isComplete()) {
                            SwingUtilities.invokeLater(() -> statusBarLabel.setText("Download Complete"));
                        }
                    }
                });
                SwingUtilities.invokeLater(() -> {
                    MainBrowser mainFrame = new MainBrowser();
                    mainFrame.setVisible(true);
                });

} catch (Exception ex) {
System.err.println("Critical Error: " + ex.getMessage());
}
}).start();
}
private static void triggerAria2Download(String url, String folder, String fileName) {
try {
ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "start", "cmd.exe", "/k", "aria2c", "--async-dns-server=8.8.8.8,1.1.1.1", "-s", "16", "-x", "16", "-j", "16", "--min-split-size=1M", "-d", folder, "-o", fileName, url);
pb.start();
} catch (Exception e) { System.err.println("[ARIA2 ERROR]: " + e.getMessage()); }
}
}