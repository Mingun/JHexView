// See LICENSE.md for license information

package tv.porst.jhexview;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * The JHexView component is a Java component that can be used to display data
 * in hexadecimal format.
 *
 * @author Sebastian Porst (sp@porst.tv)
 * @author argent77
 * @author Mingun
 */
public final class JHexView extends JComponent
{
  //<editor-fold defaultstate="collapsed" desc="Fields">
  private static final long serialVersionUID = -2402458562501988128L;

  /**
   * Lookup table to convert byte values into printable strings.
   */
  private static final String[] HEX_BYTES = {
    "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F",
    "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
    "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F",
    "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F",
    "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F",
    "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5A", "5B", "5C", "5D", "5E", "5F",
    "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6A", "6B", "6C", "6D", "6E", "6F",
    "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7A", "7B", "7C", "7D", "7E", "7F",
    "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8A", "8B", "8C", "8D", "8E", "8F",
    "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D", "9E", "9F",
    "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF",
    "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF",
    "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB", "CC", "CD", "CE", "CF",
    "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF",
    "E0", "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF",
    "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF" };

  /** Lookup table for strings for draw byte values in ASCII view. */
  private static final String[] ASCII_VIEW_TABLE;

  private static final int PADDING_OFFSETVIEW = 20;

  /**
   * A stroke definition used for showing a hint box in the view that doesn't currently has
   * the input focus.
   */
  private static final Stroke DOTTED_STROKE =
      new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                      1.0f, new float[]{1.0f}, 0.0f);

  /**
   * List containing listeners of all supported types.
   */
  private final EventListenerList m_listeners = new EventListenerList();

  //<editor-fold defaultstate="collapsed" desc="Modified">
  /**
   * Stores offset values that have been modified by the user.
   * The value indicates how often the data at the position has been modified. (Important when
   * undoing actions).
   */
  private final TreeMap<Long, Integer> m_modifiedOffsets = new TreeMap<>();

  /**
   * Defines whether to show data that has been modified by the user in a separate color.
   */
  private boolean m_showModified = false;
  //</editor-fold>

  private final SelectionModel selectionModel = new SelectionModel();
  /**
   * Manages the undo/redo functionality
   */
  private final UndoManager m_undo = new UndoManager();

  /**
   * The data set that is displayed in the component.
   */
  private IDataProvider m_dataProvider;

  /**
   * Number of bytes shown per row.
   */
  private int m_bytesPerRow = 16;

  /**
   * Font used to draw the data.
   */
  private Font m_font = new Font(Font.MONOSPACED, Font.PLAIN, 12);

  /**
   * Determines the window where the caret is shown.
   */
  private Views m_activeView = Views.HEX_VIEW;

  /**
   * Width of the hex view in pixels.
   */
  private int m_hexViewWidth = 270;

  /**
   * Width of the space between columns in pixels.
   */
  private int m_columnSpacing = 4;

  /**
   * Number of bytes per column.
   */
  private int m_bytesPerColumn = 2;

  //<editor-fold defaultstate="collapsed" desc="Colors">
  /** Background color of the header view. */
  private Color m_bgColorHeader = Color.WHITE;
  /** Background color of the offset view. */
  private Color m_bgColorOffset = Color.GRAY;
  /** Background color of the hex view. */
  private Color m_bgColorHex = Color.WHITE;
  /** Background color of the ASCII view. */
  private Color m_bgColorAscii = Color.WHITE;

  /** Font color of the header view. */
  private Color m_fontColorHeader = new Color(0x0000BF);
  /** Font color of the offset view. */
  private Color m_fontColorOffsets = Color.WHITE;
  /** Font color of the hex view for even columns. */
  private Color m_fontColorHex1 = Color.BLUE;
  /** Font color of the hex view for odd columns. */
  private Color m_fontColorHex2 = new Color(0x3399FF);
  /** Font color of the ASCII view. */
  private Color m_fontColorAscii = new Color(0x339900);

  /** Color that is used to draw all text in disabled components. */
  private Color m_disabledColor = Color.GRAY;
  /** Color that is used to highlight data when the mouse cursor hovers of the data. */
  private Color m_colorHighlight = Color.LIGHT_GRAY;
  /** Color that is used to draw background of selected data. */
  private Color m_selectionColor = Color.YELLOW;
  /** Font color for data that has been modified by the user. */
  private Color m_fontColorModified = Color.RED;

  /** Manager that keeps track of specially colored byte ranges. */
  private final ColoredRangeManager[] m_coloredRanges = new ColoredRangeManager[10];

  /** Color map, used for assigning colors to bytes from its content and/or offsets. */
  private IColormap m_colormap;
  /** Determines whether to use an assigned color map to colorize data. */
  private boolean m_colorMapEnabled = true;
  //</editor-fold>

  /**
   * Used to store the height of a single row. This value is updated every time
   * the component is drawn.
   */
  private int m_rowHeight = 12;

  /**
   * Used to store the width of a single character. This value is updated every
   * time the component is drawn.
   */
  private int m_charWidth = 8;

  /**
   * Scrollbar that is used to scroll through the dataset. Scrolling performed in
   * row units, i.e. each scroll step shows new {@link #m_bytesPerRow} bytes of data.
   */
  private final JScrollBar m_scrollbar = new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 1);

  /**
   * Horizontal scrollbar that is used to scroll through the dataset.
   */
  private final JScrollBar m_horizontalScrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 1);

  /**
   * The first visible row.
   */
  private int m_firstRow = 0;

  /**
   * The first visible column.
   */
  private int m_firstColumn = 0;

  /**
   * Address of the first offset in the data set.
   */
  private long m_baseAddress = 0;

  /**
   * Last x-coordinate of the mouse cursor in the component.
   */
  private int m_lastMouseX = 0;

  /**
   * Last y-coordinate of the mouse cursor in the component.
   */
  private int m_lastMouseY = 0;

  /**
   * Flag that determines whether the component reacts to user input or not.
   */
  private boolean editable = false;

  /**
   * Blinking caret of the component.
   */
  private final Caret m_caret = new Caret();

  /**
   * Left-padding of the hex view in pixels.
   */
  private final int m_paddingHexLeft = 10;

  /**
   * Left-padding of the ASCII view in pixels.
   */
  private final int m_paddingAsciiLeft = 10;

  /**
   * Top-padding of all views in pixels.
   */
  private final int m_paddingTop = 16;

  /**
   * Height of a drawn character in the component.
   */
  private int m_charHeight = 8;

  /**
   * Maximum positive height (ascent) of a drawn character in the component.
   */
  private int m_charMaxAscent = 8;

  /**
   * Maximum negative height (descent) of a drawn character in the component.
   */
  private int m_charMaxDescent = 3;

  /**
   * Start with an undefined definition status.
   */
  private DefinitionStatus m_status = DefinitionStatus.UNDEFINED;

  /**
   * The menu creator is used to create popup menus when the user right-clicks
   * on the hex view control.
   */
  private IMenuCreator m_menuCreator;

  /**
   * Current addressing mode (32bit or 64bit)
   */
  private AddressMode m_addressMode = AddressMode.BIT32;

  /**
   * Width of the offset view part of the component.
   */
  private int m_offsetViewWidth;

  /**
   * Timer that is used to refresh the component if no data for the selected
   * range is available.
   */
  private Timer m_updateTimer;

  /**
   * Flag that indicates whether the component is being drawn for the first
   * time.
   */
  private boolean m_firstDraw = true;

  /**
   * Default internal listener that is used to handle various events.
   */
  private final InternalListener m_listener = new InternalListener();

  //<editor-fold defaultstate="collapsed" desc="Actions">
  /**
   * Action that's executed when the user presses the left arrow key.
   */
  private final ActionLeft m_leftAction = new ActionLeft(true);

  /**
   * Action that's executed when the user presses the shift+left arrow key.
   */
  private final ActionLeft m_shiftLeftAction = new ActionLeft(false);

  /**
   * Action that's executed when the user presses the right arrow key.
   */
  private final ActionRight m_rightAction = new ActionRight(true);

  /**
   * Action that's executed when the user presses the shift+right arrow key.
   */
  private final ActionRight m_shiftRightAction = new ActionRight(false);

  /**
   * Action that's executed when the user presses the up arrow key.
   */
  private final ActionUp m_upAction = new ActionUp();

  /**
   * Action that's executed when the user presses the down arrow key.
   */
  private final ActionDown m_downAction = new ActionDown();

  /**
   * Action that's executed when the user presses the page up key.
   */
  private final ActionPageUp m_pageUpAction = new ActionPageUp();

  /**
   * Action that's executed when the user presses the page down key.
   */
  private final ActionPageDown m_pageDownAction = new ActionPageDown();

  /**
   * Action that's executed when the user presses the home key.
   */
  private final ActionHome m_homeLineAction = new ActionHome(false);

  /**
   * Action that's executed when the user presses the ctrl+home key.
   */
  private final ActionHome m_homeDocAction = new ActionHome(true);

  /**
   * Action that's executed when the user presses the end key.
   */
  private final ActionEnd m_endLineAction = new ActionEnd(false);

  /**
   * Action that's executed when the user presses the ctrl+end key.
   */
  private final ActionEnd m_endDocAction = new ActionEnd(true);

  /**
   * Action that's executed when the user presses the tab key.
   */
  private final ActionTab m_tabAction = new ActionTab();

  /**
   * Action that's executed when the user presses the shortcut ctrl+A.
   */
  private final ActionShortcut m_SelectAllAction =
      new ActionShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                                                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

  /**
   * Action that's executed when the user presses the shortcut ctrl+V.
   */
  private final ActionShortcut m_PasteTextAction =
      new ActionShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

  /**
   * Action that's executed when the user presses the shortcut ctrl+C.
   */
  private final ActionShortcut m_CopyTextAction =
      new ActionShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

  /**
   * Action that's executed when the user presses the shortcut ctrl+Z.
   */
  private final ActionShortcut m_UndoAction =
      new ActionShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                                                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

  /**
   * Action that's executed when the user presses the shortcut ctrl+Y.
   */
  private final ActionShortcut m_RedoAction =
      new ActionShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                                                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
  //</editor-fold>

  /**
   * Determines whether to draw a title header.
   */
  private boolean m_headerVisible = true;

  /**
   * Defines the font style for the header text.
   */
  private int m_headerFontStyle = Font.PLAIN;

  /**
   * Determines whether to draw vertical lines between the individual views.
   */
  private boolean m_separatorsVisible = true;

  /**
   * Determines whether to highlight the byte under the mouse cursor.
   */
  private boolean m_mouseOverHighlighted = true;

  /**
   * Determines whether the bytes inside a column are flipped or not.
   */
  private boolean m_flipBytes = false;
  //</editor-fold>

  static {
    // Draw only ASCII symbols (< 0x80), that are not ISO control characters.
    // ISO Control characters (if assume that `byte` is unsigned):
    //   (b >= 0x00 && b <= 0x1F) ||
    //   (b >= 0x7F && b <= 0x9F);
    // Other bytes drawn as dot
    ASCII_VIEW_TABLE = new String[256];
    Arrays.fill(ASCII_VIEW_TABLE, ".");
    for (int i = 0x20; i < 0x7F; ++i) {
      ASCII_VIEW_TABLE[i] = String.valueOf((char)i);
    }
  }

  /**
   * Creates a new hex viewer.
   */
  public JHexView()
  {
    setDoubleBuffered(true);

    // Setting default colors for undefined areas
    setBackground(Color.WHITE);
    setForeground(Color.BLACK);

    for (int i = 0; i < m_coloredRanges.length; i++) {
      m_coloredRanges[i] = new ColoredRangeManager();
    }

    // Necessary to receive input
    setFocusable(true);

    setLayout(new BorderLayout());

    // Set the initial font
    setFont(m_font);

    initListeners();

    initHotkeys();

    initScrollbar();

    setTransferHandler(new HexTransferHandler());

    setScrollBarMaximum();

    updateOffsetViewWidth();
  }

  //<editor-fold defaultstate="collapsed" desc="Colors">
  /**
   * Returns the current background color of the header view.
   *
   * @return The current background color of the header view.
   */
  public Color getBackgroundColorHeader() { return m_bgColorHeader; }
  /**
   * Sets the current background color of the header view.
   *
   * @param color
   *          The new background color of the header view.
   *
   * @throws NullPointerException
   *           Thrown if the new color is null.
   */
  public void setBackgroundColorHeader(final Color color)
  {
    if (color == null) {
      throw new NullPointerException("Error: Color can't be null");
    }

    m_bgColorHeader = color;
    repaint();
  }

  /**
   * Returns the current background color of the offset view.
   *
   * @return The current background color of the offset view.
   */
  public Color getBackgroundColorOffsetView() { return m_bgColorOffset; }
  /**
   * Sets the current background color of the offset view.
   *
   * @param color
   *          The new background color of the offset view.
   *
   * @throws NullPointerException
   *           Thrown if the new color is null.
   */
  public void setBackgroundColorOffsetView(final Color color)
  {
    if (color == null) {
      throw new NullPointerException("Error: Color can't be null");
    }

    m_bgColorOffset = color;
    repaint();
  }

  /**
   * Returns the current background color of the hex view.
   *
   * @return The current background color of the hex view.
   */
  public Color getBackgroundColorHexView() { return m_bgColorHex; }
  /**
   * Sets the current background color of the hex view.
   *
   * @param color
   *          The new background color of the hex view.
   *
   * @throws NullPointerException
   *           Thrown if the new color is null.
   */
  public void setBackgroundColorHexView(final Color color)
  {
    if (color == null) {
      throw new NullPointerException("Error: Color can't be null");
    }

    m_bgColorHex = color;
    repaint();
  }

  /**
   * Returns the current background color of the ASCII view.
   *
   * @return The current background color of the ASCII view.
   */
  public Color getBackgroundColorAsciiView() { return m_bgColorAscii; }
  /**
   * Sets the current background color of the ASCII view.
   *
   * @param color
   *          The new background color of the ASCII view.
   *
   * @throws NullPointerException
   *           Thrown if the new color is null.
   */
  public void setBackgroundColorAsciiView(final Color color)
  {
    if (color == null) {
      throw new NullPointerException("Error: Color can't be null");
    }

    m_bgColorAscii = color;
    repaint();
  }


  /**
   * Returns the current font color of the header view.
   *
   * @return The current font color of the header view.
   */
  public Color getFontColorHeader() { return m_fontColorHeader; }
  /**
   * Sets the current font color of the header view.
   *
   * @param color
   *          The new font color of the header view.
   *
   * @throws NullPointerException
   *           Thrown if the new color is null.
   */
  public void setFontColorHeader(final Color color)
  {
    if (color == null) {
      throw new NullPointerException("Font color for header view can't be null");
    }

    m_fontColorHeader = color;
    repaint();
  }

  /**
   * Returns the current font color of the offset view.
   *
   * @return The current font color of the offset view.
   */
  public Color getFontColorOffsetView() { return m_fontColorOffsets; }
  /**
   * Sets the current font color of the offset view.
   *
   * @param color
   *          The new font color of the offset view.
   *
   * @throws NullPointerException
   *           Thrown if the new color is null.
   */
  public void setFontColorOffsetView(final Color color)
  {
    if (color == null) {
      throw new NullPointerException("Font color for offset view can't be null");
    }

    m_fontColorOffsets = color;
    repaint();
  }

  /**
   * Returns the current font color of even columns in the hex view.
   *
   * @return The current font color of even columns in the hex view.
   */
  public Color getFontColorHexView1() { return m_fontColorHex1; }
  /**
   * Sets the current font color of even columns in the hex view.
   *
   * @param color
   *          The new font color of even columns in the hex view.
   *
   * @throws NullPointerException
   *           Thrown if the new color is null.
   */
  public void setFontColorHexView1(final Color color)
  {
    if (color == null) {
      throw new NullPointerException("Font color for even columns can't be null");
    }

    m_fontColorHex1 = color;
    repaint();
  }

  /**
   * Returns the current font color of odd columns in the hex view.
   *
   * @return The current font color of odd columns in the hex view.
   */
  public Color getFontColorHexView2() { return m_fontColorHex2; }
  /**
   * Sets the current font color of odd columns in the hex view.
   *
   * @param color
   *          The new font color of odd columns in the hex view.
   *
   * @throws NullPointerException
   *           Thrown if the new color is null.
   */
  public void setFontColorHexView2(final Color color)
  {
    if (color == null) {
      throw new NullPointerException("Font color for odd columns can't be null");
    }

    m_fontColorHex2 = color;
    repaint();
  }

  /**
   * Returns the current font color of the ASCII view.
   *
   * @return The current font color of the ASCII view.
   */
  public Color getFontColorAsciiView() { return m_fontColorAscii; }
  /**
   * Sets the current font color of the ASCII view.
   *
   * @param color
   *          The new font color of the ASCII view.
   *
   * @throws NullPointerException
   *           Thrown if the new color is null.
   */
  public void setFontColorAsciiView(final Color color)
  {
    if (color == null) {
      throw new NullPointerException("Font color for ASCII view can't be null");
    }

    m_fontColorAscii = color;
    repaint();
  }


  /**
   * Returns the current selection background color.
   *
   * @return The current selection background color.
   */
  public Color getSelectionColor() { return m_selectionColor; }
  /**
   * Sets the current selection background color.
   *
   * @param color
   *          The new selection background color.
   *
   * @throws NullPointerException
   *           Thrown if the new color is null.
   */
  public void setSelectionColor(final Color color)
  {
    if (color == null) {
      throw new NullPointerException("Selection color can't be null");
    }

    m_selectionColor = color;
    repaint();
  }

  /**
   * Returns the current font color for modified data.
   *
   * @return The current font color for modified data.
   */
  public Color getFontColorModified() { return m_fontColorModified; }
  /**
   * Sets the font color for data that has been modified by the user.
   *
   * @param color
   *          The font color for data that has been modified by the user.
   *
   * @throws NullPointerException
   *           Thrown if the new color is null.
   */
  public void setFontColorModified(final Color color)
  {
    if (color == null) {
      throw new NullPointerException("Font color for modified data can't be null");
    }

    m_fontColorModified = color;
    repaint();
  }

  /**
   * Returns the currently assigned caret color.
   * @return
   */
  public Color getCaretColor() { return m_caret.getColor(); }
  /**
   * Assigns a new color to the caret.
   * @param color The new caret color.
   */
  public void setCaretColor(final Color color)
  {
    m_caret.setColor(color);
    repaint();
  }

  /**
   * Returns whether {@link #getColorMap() a color map} is used to colorize data.
   * This colorization doesn't affect currently {@link #getSelectionModel selected}
   * byte ranges. The color map is used only if the colorized byte does not contained
   * within any explicitly defined {@link #colorize color range}.
   *
   * @return {@code true}, if data will be colorized, {@code false}, otherwise.
   */
  public boolean isColorMapEnabled() { return m_colorMapEnabled; }
  /**
   * Specify whether to enable the currently assigned color map.
   * This colorization doesn't affect currently {@link #getSelectionModel selected}
   * byte ranges. The color map is used only if the colorized byte does not contained
   * within any explicitly defined {@link #colorize color range}.
   *
   * @param enabled If {@code true}, then colors of each byte determined by
   *        {@link #getColorMap() color map} if it defined and {@link IColormap#colorize(byte, long)}
   *        returns {@code true}
   */
  public void setColorMapEnabled(boolean enabled)
  {
    if (enabled != m_colorMapEnabled) {
      m_colorMapEnabled = enabled;
      repaint();
    }
  }
  /**
   * Returns the currently assigned color map, if any.
   * This colorization doesn't affect currently {@link #getSelectionModel selected}
   * byte ranges. The color map is used only if the colorized byte does not contained
   * within any explicitly defined {@link #colorize color range}.
   *
   * @return The currently assigned color map.
   */
  public IColormap getColorMap() { return m_colormap; }
  /**
   * Assigns a new color map.
   * This colorization doesn't affect currently {@link #getSelectionModel selected}
   * byte ranges. The color map is used only if the colorized byte does not contained
   * within any explicitly defined {@link #colorize color range}.
   *
   * @param colormap The new color map.
   */
  public void setColormap(final IColormap colormap)
  {
    m_colormap = colormap;
    repaint();
  }

  /**
   * Colorizes a range of bytes in special colors. To keep the default text or
   * background color, it is possible to pass null as these colors. This colorization
   * doesn't affect currently {@link #getSelectionModel selected} byte ranges.
   * This setting has priority under {@link #setColormap color maps}
   *
   * @param level Priority level at which specified color range must be added.
   *        Ranges with lowest level has priority
   * @param offset The start offset of the byte range. The meaningful values live
   *        in range {@code [0; getData().getDataLength())}, but you can use any
   *        positive values even outside data range. If range will grown, that
   *        values will be used
   * @param size The number of bytes in the range
   * @param color The text color that is used to color that range
   * @param bgcolor The background color that is used to color that range
   *
   * @throws IllegalArgumentException If {@code level} not in range {@code [0; 9]},
   *         {@code offset} is negative, {@code size} is not positive
   *
   * @see #uncolorize
   * @see #uncolorizeAll()
   * @see #uncolorizeAll(int)
   */
  public void colorize(int level, long offset, int size, Color color, Color bgcolor)
  {
    getColoredRange(level, offset, size).addRange(new ColoredRange(offset, size, color, bgcolor));
    repaint();
  }

  /**
   * Removes special colorization from a range of bytes.
   *
   * @param level The colored range that must be disabled
   * @param offset The start offset of the byte range. The meaningful values live
   *        in range {@code [0; getData().getDataLength())}, but you can use any
   *        positive values even outside data range. If range will grown, that
   *        values will be used
   * @param size The number of bytes in the byte range
   *
   * @throws IllegalArgumentException If {@code level} not in range {@code [0; 9]},
   *         {@code offset} is negative, {@code size} is not positive
   *
   * @see #colorize
   * @see #uncolorizeAll()
   * @see #uncolorizeAll(int)
   */
  public void uncolorize(int level, long offset, int size)
  {
    getColoredRange(level, offset, size).removeRange(offset, size);
    repaint();
  }

  /**
   * Removes special range colorizations for specified color range.
   *
   * @param level The colored range that must be completely removed
   *
   * @throws IllegalArgumentException If {@code level} not in range {@code [0; 9]}
   *
   * @see #colorize
   * @see #uncolorize
   * @see #uncolorizeAll()
   */
  public void uncolorizeAll(int level)
  {
    // Use fictive offset and size just to check `level` inside `getColoredRange`
    getColoredRange(level, 0, 1).clear();
    repaint();
  }

  /**
   * Removes all special range colorizations.
   *
   * @see #colorize
   * @see #uncolorize
   * @see #uncolorizeAll(int)
   */
  public void uncolorizeAll()
  {
    for (final ColoredRangeManager coloredRange : m_coloredRanges) {
      coloredRange.clear();
    }
    repaint();
  }
  private ColoredRangeManager getColoredRange(int level, long offset, int size)
  {
    if (level < 0 || level >= m_coloredRanges.length) {
      throw new IllegalArgumentException("Invalid level: " + level + ", must be in range [0 ;"
        + m_coloredRanges.length + ")");
    }
    if (offset < 0) {
      throw new IllegalArgumentException("Offset can't be negative: 0x" + Long.toHexString(offset));
    }
    if (size <= 0) {
      throw new IllegalArgumentException("Size must be positive: " + size);
    }
    return m_coloredRanges[level];
  }
  private ColoredRange findColoredRange(long currentOffset)
  {
    for (final ColoredRangeManager element : m_coloredRanges) {
      final ColoredRange range = element.findRangeWith(currentOffset);
      if (range != null) {
        return range;
      }
    }
    return null;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Undo/Redo">
  /** Returns whether an undo is possible. */
  public boolean canUndo() { return m_undo.canUndo(); }
  /** Performs an undo action if available. */
  public void undo() { if (canUndo()) { m_undo.undo(); } }
  /**
   * Returns the name of the last undoable action added to the list.
   *
   * @return Name of action or empty string, if nothing to undo
   */
  public String getUndoPresentationName()
  {
    return canUndo() ? m_undo.getUndoPresentationName() : "";
  }

  /** Returns whether a redo is possible. */
  public boolean canRedo() { return m_undo.canRedo(); }
  /** Performs a redo action if available. */
  public void redo() { if (canRedo()) { m_undo.redo(); } }
  /**
   * Returns the name of the last redoable action added to the list.
   *
   * @return Name of action or empty string, if nothing to redo
   */
  public String getRedoPresentationName()
  {
    return canRedo() ? m_undo.getRedoPresentationName() : "";
  }

  /** Removes all undoable edit actions from the list. */
  public void resetUndo() { m_undo.die(); }

  public void addUndoableEditListener(UndoableEditListener listener)
  {
    if (listener == null) {
      throw new NullPointerException("UndoableEditListener can't be null");
    }

    m_listeners.add(UndoableEditListener.class, listener);
  }
  public void removeUndoableEditListener(UndoableEditListener listener)
  {
    if (listener == null) {
      throw new NullPointerException("UndoableEditListener can't be null");
    }

    m_listeners.remove(UndoableEditListener.class, listener);
  }

  /**
   * Notifies all registered UndoableEditListeners that an undoable event has been triggered.
   */
  private void fireUndoableEditListener(UndoableEdit edit)
  {
    UndoableEditEvent event = null;
    Object[] l = m_listeners.getListenerList();
    for (int i = l.length - 2; i >= 0; i -= 2) {
      if (l[i] == UndoableEditListener.class) {
        if (event == null) {
          event = new UndoableEditEvent(this, edit);
        }
        ((UndoableEditListener)l[i+1]).undoableEditHappened(event);
      }
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Modified">
  /**
   * Returns whether to apply a separate color to modified data.
   *
   * @return {code true}, if {@link #getFontColorModified()} must be used to draw
   *         modified data
   */
  public boolean isShowModified() { return m_showModified; }
  /**
   * Enable whether to show modified data in a separate color.
   *
   * @param show If {code true}, {@link #getFontColorModified()} will be used to
   *        draw modified data
   */
  public void setShowModified(boolean show)
  {
    if (show != m_showModified) {
      m_showModified = show;

      repaint();
    }
  }

  /**
   * Returns whether data has been modified by the user.
   *
   * @return {@code true} if data has been modified by the user, {@code false} otherwise.
   */
  public boolean isModified() { return !m_modifiedOffsets.isEmpty(); }

  /**
   * Returns whether the value at the specified offset has been modified by the user.
   *
   * @param offset The data offset in range {@code [0; getData().getDataLength())}.
   *
   * @return {@code true} if the data at the specified {@code offset} has been modified
   *         by the user, {@code false} otherwise.
   */
  public boolean isModified(long offset) { return m_modifiedOffsets.containsKey(offset); }

  /**
   * Returns the number of modifications done to the data at the specified offset.
   *
   * @param offset The offset of the modified data in range {@code [0; getData().getDataLength())}.
   *
   * @return Number of modifications done to the data at the specified offset.
   */
  public int getModifiedCount(long offset)
  {
    final Integer value = m_modifiedOffsets.get(offset);
    return value != null ? value.intValue() : 0;
  }

  /**
   * Returns all offsets of data that has been modified by the user.
   *
   * @return An array of offsets of modified data. Each offset in range
   *         {@code [0; getData().getDataLength())}
   */
  public long[] getModifiedOffsets()
  {
    final long[] retVal = new long[m_modifiedOffsets.size()];
    if (!m_modifiedOffsets.isEmpty()) {
      int i = 0;
      Iterator<Long> iter = m_modifiedOffsets.keySet().iterator();
      while (iter.hasNext()) {
        retVal[i] = iter.next().longValue();
        i++;
      }
    }
    return retVal;
  }

  /** Clears all offsets that have been marked as modified. */
  public void clearModified()
  {
    if (!m_modifiedOffsets.isEmpty()) {
      m_modifiedOffsets.clear();
      if (isShowModified()) {
        repaint();
      }
    }
  }

  /**
   * Decrements the number of modifications to the data at the specified offset
   * or removes it completely.
   *
   * @param offset The position of data that has been modified by the user
   *        in range {@code [0; getData().getDataLength())}
   * @param forceRemove If {@code true}, remove the offset regardless of how many
   *        times it has been modified.
   *
   * @return {@code true} if the data at this offset had been modified, {@code false} otherwise.
   */
  private boolean clearModified(long offset, boolean forceRemove)
  {
    final Long key = Long.valueOf(offset);
    final Integer value = m_modifiedOffsets.get(key);
    if (value != null) {
      final int newCount = value.intValue() - 1;
      if (!forceRemove && newCount > 0) {
        m_modifiedOffsets.put(key, newCount);
      } else {
        m_modifiedOffsets.remove(key);
      }
      return true;
    }
    return false;
  }
  /**
   * Adds the specified offset of the modified data to the list or increment its
   * use if already existing.
   *
   * @param offset The position of data that has been modified by the user
   *        in range {@code [0; getData().getDataLength())}
   *
   * @return The number of modifications done to the data at the specified offset before.
   */
  private int setModified(long offset)
  {
    int retVal = 0;
    if (offset >= 0L) {
      Long key = Long.valueOf(offset);
      Integer value = m_modifiedOffsets.get(key);
      if (value != null) {
        retVal = value.intValue();
      }
      m_modifiedOffsets.put(key, retVal + 1);
    }
    return retVal;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Properties">
  /**
   * Returns the currently selected view.
   *
   * @return The currently selected view.
   */
  public Views getActiveView() { return m_activeView; }
  /**
   * Selects a new active view.
   *
   * @param view The view to select.
   *
   * @throws NullPointerException If the new view is {@code null}.
   */
  public void setActiveView(Views view)
  {
    if (view == null) {
      throw new NullPointerException("Active view can't be null");
    }
    if (view != m_activeView) {
      m_tabAction.actionPerformed(new ActionEvent(this, Event.ACTION_EVENT, ""));
    }
  }

  /**
   * Returns the currently used address mode.
   *
   * @return The currently used address mode.
   */
  public AddressMode getAddressMode() { return m_addressMode; }
  /**
   * Sets the currently used address mode.
   *
   * @param mode The new address mode.
   *
   * @throws NullPointerException If the new address mode is {@code null}.
   */
  public void setAddressMode(final AddressMode mode)
  {
    if (mode == null) {
      throw new NullPointerException("Address mode can't be null");
    }
    if (m_addressMode != mode) {
      m_addressMode = mode;

      updateOffsetViewWidth();
      updatePreferredSize();

      repaint();
    }
  }

  /**
   * Returns the number of bytes displayed per column.
   *
   * @return The number of bytes displayed per column.
   */
  public int getBytesPerColumn() { return m_bytesPerColumn; }
  /**
   * Sets the number of bytes displayed per column.
   *
   * @param bytes The new number of bytes per column.
   *
   * @throws IllegalArgumentException If the new number of bytes is smaller
   *         than 1 or bigger than the number of bytes per row.
   */
  public void setBytesPerColumn(final int bytes)
  {
    if (bytes <= 0 || bytes > m_bytesPerRow) {
      throw new IllegalArgumentException("Invalid number of bytes per column: " + bytes
        + ", must be in range [1; " + m_bytesPerRow + "]"
      );
    }
    if (m_bytesPerColumn != bytes) {
      m_bytesPerColumn = bytes;

      updateHexViewWidth();
      updatePreferredSize();
      repaint();
    }
  }

  /**
   * Returns the current number of bytes displayed per row.
   *
   * @return The current number of bytes displayed per row.
   */
  public int getBytesPerRow() { return m_bytesPerRow; }
  /**
   * Sets the current number of bytes displayed per row.
   *
   * @param value The new number of bytes displayed per row.
   *
   * @throws IllegalArgumentException If the new number is smaller than 1.
   */
  public void setBytesPerRow(final int value)
  {
    if (value <= 0) {
      throw new IllegalArgumentException("Bytes per row must be positive: " + value);
    }
    if (m_bytesPerRow != value) {
      m_bytesPerRow = value;
      repaint();
    }
  }

  /**
   * Returns the spacing between columns in pixels.
   *
   * @return The spacing between columns.
   */
  public int getColumnSpacing() { return m_columnSpacing; }
  /**
   * Sets the spacing between columns.
   *
   * @param spacing The spacing between columns in pixels.
   *
   * @throws IllegalArgumentException If the new spacing is smaller than 1.
   */
  public void setColumnSpacing(final int spacing)
  {
    if (spacing <= 0) {
      throw new IllegalArgumentException("Column spacing must be positive: " + spacing);
    }
    if (m_columnSpacing != spacing) {
      m_columnSpacing = spacing;
      repaint();
    }
  }

  /**
   * Returns the current definition status.
   *
   * @return The current definition status.
   */
  public DefinitionStatus getDefinitionStatus() { return m_status; }
  /**
   * Changes the definition status of the JHexView component. This flag
   * determines whether real data or {@code ??} are displayed.
   *
   * @param status The new definition status.
   *
   * @throws NullPointerException If the new definition status is {@code null}.
   */
  public void setDefinitionStatus(final DefinitionStatus status)
  {
    if (status == null) {
      throw new NullPointerException("Definition status can't be null");
    }

    if (m_status != status) {
      m_status = status;
      repaint();
    }
  }

  /** Returns whether vertical lines between the individual views are visible. */
  public boolean isSeparatorsVisible() { return m_separatorsVisible; }
  /**
   * Shows or hides vertical lines between the individual views.
   *
   * @param show The visibility state of vertical lines between the individual views.
   */
  public void setSeparatorsVisible(boolean show)
  {
    if (show != m_separatorsVisible) {
      m_separatorsVisible = show;

      repaint();
    }
  }

  /**
   * Returns the current width of the hex view.
   *
   * @return The current width of the hex view.
   */
  public int getHexViewWidth() { return m_hexViewWidth; }
  /**
   * Sets the width of the hex view.
   *
   * @param width The new width of the offset view.
   *
   * @throws IllegalArgumentException If the new width is smaller than 1.
   */
  public void setHexViewWidth(final int width)
  {
    if (width <= 0) {
      throw new IllegalArgumentException("Hex view width must be positive: " + width);
    }
    if (m_hexViewWidth != width) {
      m_hexViewWidth = width;

      repaint();
    }
  }

  /**
   * Returns whether the title header is visible.
   */
  public boolean isHeaderVisible() { return m_headerVisible; }
  /**
   * Set whether to draw a title header over the hex data.
   */
  public void setHeaderVisible(boolean visible)
  {
    if (m_headerVisible != visible) {
      m_headerVisible = visible;

      // The proportions of the hex window change significantly.
      // Just start over when the next repaint event comes.
      m_firstDraw = true;

      repaint();
    }
  }

  /**
   * Returns the font style used for header text.
   *
   * @return The font style used for header text.
   *
   * @see Font#deriveFont(int)
   */
  public int getHeaderFontStyle() { return m_headerFontStyle; }
  /**
   * Sets the font style used for header text.
   *
   * @param style Font style used for header text.
   *
   * @see Font#deriveFont(int)
   */
  public void setHeaderFontStyle(final int style)
  {
    if (style != m_headerFontStyle) {
      m_headerFontStyle = style;
      repaint();
    }
  }

  /**
   * Returns whether the byte under the mouse cursor will be highlighted.
   *
   * @return The highlighted state of bytes under the current mouse cursor position
   */
  public boolean isMouseOverHighlighted() { return m_mouseOverHighlighted; }
  /**
   * Enables or disables the highlights state of bytes under the mouse cursor.
   *
   * @param highlight The highlighted state of bytes under the mouse cursor.
   */
  public void setMouseOverHighlighted(boolean highlight)
  {
    if (highlight != m_mouseOverHighlighted) {
      m_mouseOverHighlighted = highlight;
      repaint();
    }
  }

  /**
   * Returns a flag that indicates whether the bytes inside a column are
   * flipped or not.
   *
   * @return {@code true}, if the bytes are flipped, {@code false}, otherwise.
   */
  public boolean isFlipBytes() { return m_flipBytes; }
  public void setFlipBytes(final boolean flip)
  {
    if (m_flipBytes != flip) {
      m_flipBytes = flip;

      repaint();
    }
  }

  @Override
  public Font getFont() { return m_font; }
  @Override
  public void setFont(Font font)
  {
    if (font != m_font) {
      if (font == null) {
        font = new Font(Font.MONOSPACED, Font.PLAIN, m_font.getSize());
      }
      m_font = font;
      super.setFont(m_font);

      // The proportions of the hex window change significantly.
      // Just start over when the next repaint event comes.
      m_firstDraw = true;

      repaint();
    }
  }

  /**
   * Returns the boolean indicating whether this component is editable or not.
   *
   * @return {@code true}, if contents of this component can be changed by user.
   *         {@code false}, otherwise.
   *
   * @see #setEditable
   */
  public boolean isEditable() { return editable; }
  /**
   * Sets the specified boolean to indicate whether or not this component should
   * be editable.
   * A PropertyChange event ("editable") is fired when the state is changed.
   *
   * @param editable {@code true}, to allow user change content of this element.
   *
   * @see #isEditable
   */
  public void setEditable(boolean editable)
  {
    if (editable != this.editable) {
      final boolean oldVal = this.editable;
      this.editable = editable;
      enableInputMethods(editable);
      firePropertyChange("editable", Boolean.valueOf(oldVal), Boolean.valueOf(editable));
      repaint();
    }
  }

  /**
   * Enables or disables the component.
   *
   * @param enabled {@code true} to enable the component, {@code false} to disable it.
   */
  @Override
  public void setEnabled(final boolean enabled)
  {
    if (enabled && !isEnabled()) {
      setScrollBarMaximum();
    }
    super.setEnabled(enabled);
  }

  /**
   * Sets the menu creator of the hex view control.
   *
   * @param creator The new menu creator. If this parameter is {@code null},
   *        no context menu is shown in the component.
   */
  public void setMenuCreator(final IMenuCreator creator)
  {
    m_menuCreator = creator;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Data">
  /**
   * Returns the currently used data provider.
   *
   * @return The currently used data provider.
   */
  public IDataProvider getData() { return m_dataProvider; }
  /**
   * Sets the current data provider.
   *
   * It is valid to pass null as the new data provider. This clears the display.
   *
   * @param data The new data provider.
   */
  public void setData(final IDataProvider data)
  {
    /**
     * Remove the data listener from the old data source.
     */
    if (m_dataProvider != null) {
      m_dataProvider.removeListener(m_listener);
    }

    m_dataProvider = data;

    /**
     * Add a data listener to the new data source so that the component can be
     * updated when the data changes.
     */
    if (data != null) {
      data.addListener(m_listener);
    }

    setCurrentPosition(0);
    setScrollBarMaximum();
    repaint();
  }

  /**
   * Returns the current base address.
   *
   * @return The current base address.
   */
  public long getBaseAddress() { return m_baseAddress; }
  /**
   * Sets the current base address.
   *
   * @param baseAddress The current base address.
   *
   * @throws IllegalArgumentException If the new base address is negative.
   */
  public void setBaseAddress(final long baseAddress)
  {
    if (baseAddress < 0) {
      throw new IllegalArgumentException("Base address can't be negative: 0x" + Long.toHexString(baseAddress));
    }
    if (m_baseAddress != baseAddress) {
      m_baseAddress = baseAddress;
      repaint();
    }
  }

  /**
   * Returns the first visible offset in bytes in range
   * {@code [getBaseAddress(); getBaseAddress() + getData().getDataLength())}.
   *
   * @return The first visible offset.
   */
  public long getFirstVisibleOffset() { return getBaseAddress() + getFirstVisibleByte(); }
  public int getVisibleBytes()
  {
    final int maxVisible = getMaximumVisibleBytes();
    final int visible = m_dataProvider.getDataLength() - (int)getFirstVisibleByte();

    return visible >= maxVisible ? maxVisible : visible;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Selection">
  /**
   * Returns the model that manages selection state of the component. Use model
   * methods {@link SelectionModel#addSelectionListener to subscribe} to selection
   * change {@link SelectionEvent events}.
   *
   * @return the model that manages selection state of the component
   *
   * @since 2.0
   */
  public SelectionModel getSelectionModel() { return selectionModel; }
  /**
   * Returns the offset at the current caret position in bytes in range
   * {@code [getBaseAddress(); getBaseAddress() + getData().getDataLength())}.
   *
   * @return The offset at the current caret position.
   */
  public long getCurrentOffset()
  {
    final long currentOffset = m_baseAddress + m_caret.getPosition() / 2;

    if (m_flipBytes) {
      return (currentOffset & -m_bytesPerColumn) + m_bytesPerColumn
           - (currentOffset %  m_bytesPerColumn) - 1;
    }
    return currentOffset;
  }
  /**
   * Sets the caret to a new offset. Do nothing, if {@link #getData() data provider}
   * doesn't set.
   *
   * @param offset The new offset in bytes in range
   *        {@code [getBaseAddress(); getBaseAddress() + getData().getDataLength())}
   */
  public void setCurrentOffset(final long offset)
  {
    if (m_dataProvider == null) {
      return;
    }

    final long end = m_baseAddress + m_dataProvider.getDataLength();
    if (offset < m_baseAddress || offset > end) {
      throw new IllegalArgumentException("Invalid offset 0x" + Long.toHexString(offset)
        + ", must be in range [0x" + Long.toHexString(m_baseAddress)
        + "; 0x" + Long.toHexString(end) + "]"
      );
    }

    setCurrentPosition(2 * (offset - m_baseAddress));
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Shortcuts">
  /**
   * Registers the specified shortcut for its predefined action.
   *
   * @param shortcut The shortcut to register.
   */
  public void registerShortcut(Shortcut shortcut)
  {
    final int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    final InputMap inputMap = this.getInputMap();

    switch (shortcut) {
      case CTRL_A:
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ctrl), "ctrl A");
        break;
      case CTRL_C:
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ctrl), "ctrl C");
        break;
      case CTRL_V:
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ctrl), "ctrl V");
        break;
      case CTRL_Y:
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ctrl), "ctrl Y");
        break;
      case CTRL_Z:
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ctrl), "ctrl Z");
        break;
    }
  }
  /**
   * Unregisters the specified shortcut. Associated action will not be executed
   * on shortcut afterwards.
   *
   * @param shortcut The shortcut to unregister.
   */
  public void unregisterShortcut(Shortcut shortcut)
  {
    final int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    final InputMap inputMap = this.getInputMap();

    switch (shortcut) {
      case CTRL_A:
        inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_A, ctrl));
        break;
      case CTRL_C:
        inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_C, ctrl));
        break;
      case CTRL_V:
        inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_V, ctrl));
        break;
      case CTRL_Y:
        inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ctrl));
        break;
      case CTRL_Z:
        inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ctrl));
        break;
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="HEX Listener">
  /**
   * Adds a new event listener to the list of event listeners.
   *
   * @param listener The new event listener
   *
   * @throws NullPointerException If the listener argument is {@code null}
   *
   * @see #removeHexListener
   */
  public void addHexListener(final IHexViewListener listener)
  {
    if (listener == null) {
      throw new NullPointerException("IHexViewListener can't be null");
    }

    m_listeners.add(IHexViewListener.class, listener);
  }
  /**
   * Remove event listener from list of event listeners.
   *
   * @param listener The listener to remove
   *
   * @throws NullPointerException If the listener argument is {@code null}
   *
   * @see #addHexListener
   */
  public void removeHexListener(final IHexViewListener listener)
  {
    if (listener == null) {
      throw new NullPointerException("IHexViewListener can't be null");
    }

    m_listeners.remove(IHexViewListener.class, listener);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Copy/Paste">
  /**
   * Transfers the currently selected range of data to the system clipboard.
   * Does nothing if no selection is available.
   */
  public void copy()
  {
    if (!selectionModel.isEmpty()) {
      m_CopyTextAction.actionPerformed(new ActionEvent(this, Event.ACTION_EVENT, ""));
    }
  }
  /**
   * Transfers the contents of the system clipboard into the hex viewer. Data starting at the current
   * cursor position will be overwritten.
   */
  public void paste()
  {
    m_PasteTextAction.actionPerformed(new ActionEvent(this, Event.ACTION_EVENT, ""));
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Search/Goto">
  /**
   * Attempts to find the next occurrence of keyword in the ascii view of the data,
   * starting at the specified offset.
   *
   * @param offset The start offset for the search in range {@code [0; getData().getDataLength())}
   * @param keyword The keyword to search
   * @param caseSensitive Indicates whether to search case sensitive
   *
   * @return The offset of the match in range {@code [0; getData().getDataLength())},
   *         or -1 if no match has been found
   *
   * @throws NullPointerException If {@code keyword} is {@code null}
   *
   * @deprecated Use {@link #findAscii(long, java.lang.String, boolean)}. Will be removed in 3.0
   */
  @Deprecated
  public int findAscii(int offset, String keyword, boolean caseSensitive)
  {
    return (int)findAscii((long)offset, keyword, caseSensitive);
  }
  /**
   * Attempts to find the next occurrence of keyword in the ascii view of the data,
   * starting at the specified offset.
   *
   * @param offset The start offset for the search in range {@code [0; getData().getDataLength())}
   * @param keyword The keyword to search
   * @param caseSensitive Indicates whether to search case sensitive
   *
   * @return The offset of the match in range {@code [0; getData().getDataLength())},
   *         or -1 if no match has been found
   *
   * @throws NullPointerException If {@code keyword} is {@code null}
   *
   * @since 2.1
   */
  public long findAscii(long offset, String keyword, boolean caseSensitive)
  {
    if (getDefinitionStatus() == DefinitionStatus.DEFINED) {
      if (keyword == null) {
        throw new NullPointerException("String for search must not be null");
      }
      // converting string into byte array
      final byte[] pattern = new byte[keyword.length()];
      for (int i = 0; i < pattern.length; i++) {
        pattern[i] = (byte)(keyword.charAt(i) & 0xFF);
      }

      final long len = getData().getDataLength() - offset;
      return findIndexOf(offset, len, pattern, caseSensitive);
    }
    return -1;
  }

  /**
   * Attempts to find the next occurrence of keyword in the hex view of the data,
   * starting at the specified offset.
   *
   * @param offset The start offset for the search in range {@code [0; getData().getDataLength())}
   * @param keyword The keyword to search
   *
   * @return The offset of the match in range {@code [0; getData().getDataLength())},
   *         or -1 if no match has been found
   *
   * @throws NullPointerException If {@code keyword} is {@code null}
   *
   * @deprecated Use {@link #findHex(long, byte[])}. Will be removed in 3.0
   */
  @Deprecated
  public int findHex(int offset, byte[] keyword)
  {
    return (int)findHex((long)offset, keyword);
  }
  /**
   * Attempts to find the next occurrence of keyword in the hex view of the data,
   * starting at the specified offset.
   *
   * @param offset The start offset for the search in range {@code [0; getData().getDataLength())}
   * @param keyword The keyword to search
   *
   * @return The offset of the match in range {@code [0; getData().getDataLength())},
   *         or -1 if no match has been found
   *
   * @throws NullPointerException If {@code keyword} is {@code null}
   *
   * @since 2.1
   */
  public long findHex(long offset, byte[] keyword)
  {
    if (getDefinitionStatus() == DefinitionStatus.DEFINED) {
      if (keyword == null) {
        throw new NullPointerException("Byte array for search must not be null");
      }
      final long len = getData().getDataLength() - offset;
      return findIndexOf(offset, len, keyword, false);
    }
    return -1;
  }

  /**
   * Scrolls to a given offset.
   *
   * @param offset The offset in bytes to scroll to in range
   *        {@code [getBaseAddress(); getBaseAddress() + getData().getDataLength())}
   *
   * @throws IllegalArgumentException If the offset is out of bounds.
   *
   * @deprecated Use {@link #setCurrentOffset}. Will be removed in 3.0
   */
  @Deprecated
  public void gotoOffset(final long offset)
  {
    if (m_dataProvider == null) {
      throw new IllegalStateException("No data provider active");
    }
    setCurrentOffset(offset);
  }
  //</editor-fold>

  @Override
  protected void paintComponent(final Graphics gx)
  {
    super.paintComponent(gx);

    // Make room for a new graphic
    resetBufferedGraphic(gx);

    // Calculate current sizes of characters and rows
    calculateSizes();

    updateOffsetViewWidth();

    if (m_firstDraw) {
      m_firstDraw = false;

      // The first time the component is drawn, its size must be set.
      updateHexViewWidth();
      updatePreferredSize();
    }

    // Draw the background of the hex panel
    drawBackground(gx);

    // Draw the offsets column
    drawOffsets(gx);

    if (isEnabled()) {
      // Only draw the cursor "shadow" if the component is enabled.
      drawMouseOverHighlighting(gx);
    }

    // If the component has defined data, it can be drawn.
    if (m_status == DefinitionStatus.DEFINED && m_dataProvider != null) {

      final int bytesToDraw = getBytesToDraw();

      if (bytesToDraw != 0 && !m_dataProvider.hasData(getFirstVisibleByte(), bytesToDraw)) {
        // At this point the component wants to draw data but the data
        // provider does not have the data yet. The hope is that the data
        // provider can reload the data. Until this happens, set the
        // component's status to UNDEFINED and create a timer that
        // periodically rechecks if the missing data is finally available.

        setDefinitionStatus(DefinitionStatus.UNDEFINED);
        setEnabled(false);

        if (m_updateTimer != null) {
          m_updateTimer.setRepeats(false);
          m_updateTimer.stop();
        }

        m_updateTimer = new Timer(1000, new ActionWaitingForData(getFirstVisibleByte(),
            bytesToDraw));
        m_updateTimer.setRepeats(true);
        m_updateTimer.start();

        return;
      }
    }

    if (isDataAvailable() || m_status == DefinitionStatus.UNDEFINED) {
      // Draw the hex data
      drawHexView(gx);

      // Draw the ASCII data
      drawAsciiPanel(gx);

      // Show the caret if necessary
      if (hasFocus()) {
        drawCaret((Graphics2D)gx);
      }
    }
  }

  //<editor-fold defaultstate="collapsed" desc="Private">
  //<editor-fold defaultstate="collapsed" desc="Draw">
  /**
   * Draws the background of the hex panel.
   *
   * @param g The graphics context of the hex panel.
   */
  private void drawBackground(final Graphics g)
  {
    final int H  = getHeight();
    final int W  = getWidth();
    final int HH = getHeaderHeight();

    // clearing whole component
    g.setColor(getBackground());
    g.fillRect(0, 0, W, H);

    final int x1 = -m_firstColumn * m_charWidth;// Start of offsets area
    final int x2 = x1 + m_offsetViewWidth;      // Start of HEX area
    final int x3 = x2 + m_hexViewWidth;         // Start of ASCII area

    // Draw the background of the header view
    if (m_headerVisible) {
      final int w = W - x1 - m_scrollbar.getWidth();
      g.setColor(m_bgColorHeader);
      g.fillRect(x1, 0, w, HH);
    }

    // Draw the background of the offset view
    g.setColor(m_bgColorOffset);
    g.fillRect(x1, HH, m_offsetViewWidth, H);

    // Draw the background of the hex view
    g.setColor(m_bgColorHex);
    g.fillRect(x2, HH, m_hexViewWidth, H);

    // Draw the background of the ASCII view
    final int w = m_bytesPerRow * m_charWidth + 2 * m_paddingAsciiLeft;
    g.setColor(m_bgColorAscii);
    g.fillRect(x3, HH, w, H);

    // Draw the lines that separate the individual views
    if (m_separatorsVisible) {
      g.setColor(Color.BLACK);
      g.drawLine(x2, HH, x2, H);
      g.drawLine(x3, HH, x3, H);
    }
  }
  /**
   * Draws the offsets in the offset view.
   *
   * @param g The graphics context of the hex panel.
   */
  private void drawOffsets(final Graphics g)
  {
    final int x = -m_firstColumn * m_charWidth + 10;

    // Drawing offset title
    if (m_headerVisible) {
      Font oldFont = getFont();
      g.setFont(oldFont.deriveFont(m_headerFontStyle));
      g.setColor(m_fontColorHeader);
      String title = getHeaderTitleOffset(m_addressMode);
      g.drawString(title, x, m_paddingTop);
      g.setFont(oldFont);
    }

    if (isEnabled()) {
      // Choose the right color for the offset text
      g.setColor(m_fontColorOffsets);
    } else {
      g.setColor(m_disabledColor != m_bgColorOffset ? m_disabledColor : Color.WHITE);
    }

    final int bytesToDraw;
    if (m_status == DefinitionStatus.DEFINED && m_dataProvider.getDataLength() > 0) {
      bytesToDraw = getBytesToDraw();
    } else {
      bytesToDraw = m_bytesPerRow;
    }

    final String formatString = getAddressModeFormat(m_addressMode);

    // Iterate over the data and print the offsets
    for (int i = 0; i < bytesToDraw; i += m_bytesPerRow) {
      final long address = m_baseAddress + m_firstRow * m_bytesPerRow + i;

      final String offsetString = String.format(formatString, address);
      final int currentRow = i / m_bytesPerRow;

      int y = m_paddingTop + getHeaderHeight() + currentRow * m_rowHeight;
      g.drawString(offsetString, x, y);
    }
  }

  //<editor-fold defaultstate="collapsed" desc="Mouse highlighting">
  /**
   * Draws highlighting of bytes when the mouse hovers over them.
   *
   * @param g The graphics context where the highlighting is drawn.
   */
  private void drawMouseOverHighlighting(final Graphics g)
  {
    if (m_mouseOverHighlighted) {
      final long nibble = getNibbleAtCoordinate(m_lastMouseX, m_lastMouseY);
      if (nibble == -1) {
        return;
      }
      final int relativeNibble = (int) (nibble - 2 * getFirstVisibleByte());
      if (relativeNibble >= 0 && relativeNibble <= 2 * getMaximumVisibleBytes()) {
        // Find out in which view the mouse currently resides.
        final Views lastHighlightedView = m_lastMouseX >= getAsciiViewLeft()
            ? Views.ASCII_VIEW
            : Views.HEX_VIEW;

        g.setColor(m_colorHighlight);
        if (lastHighlightedView == Views.HEX_VIEW) {
          // If the mouse is in the hex view just one nibble must be highlighted.
          drawNibbleBoundsHex(g, relativeNibble);
        } else
        if (lastHighlightedView == Views.ASCII_VIEW) {
          // If the mouse is in the ASCII view it is necessary
          // to highlight two nibbles.
          drawNibbleBoundsHex(g, relativeNibble);
          drawNibbleBoundsHex(g, relativeNibble + 1);
        }
        // Highlight the byte in the ASCII panel too.
        drawByteBoundsAscii(g, relativeNibble / 2);
      }
    }
  }
  /**
   * Draws the bounds of a nibble in the hex view.
   *
   * @param g The graphics context of the hex panel
   * @param position The index of the nibble
   */
  private void drawNibbleBoundsHex(Graphics g, final int position)
  {
    final int row    = position / (2 * m_bytesPerRow);
    final int column = position % (2 * m_bytesPerRow) / (2 * m_bytesPerColumn);
    final int nibble = position % (2 * m_bytesPerRow) % (2 * m_bytesPerColumn);

    final int x = getHexViewLeft() + m_paddingHexLeft + column * getColumnSize() + nibble * m_charWidth;
    final int y = m_paddingTop + getHeaderHeight() - m_charHeight + row * m_rowHeight;

    g.fillRect(x, y, m_charWidth, m_charMaxAscent + m_charMaxDescent);
  }
  /**
   * Draws the bounds of a byte in the ASCII view.
   *
   * @param g The graphics context of the ASCII panel
   * @param position The index of one of the nibbles that belong to the byte
   */
  private void drawByteBoundsAscii(Graphics g, final int position)
  {
    final int row = position / m_bytesPerRow;
    final int chr = position % m_bytesPerRow;

    final int x = getAsciiViewLeft() + m_paddingAsciiLeft + chr * m_charWidth;
    final int y = m_paddingTop + getHeaderHeight() - m_charHeight + row * m_rowHeight;

    g.fillRect(x, y, m_charWidth, m_charMaxAscent + m_charMaxDescent);
  }
  //</editor-fold>

  /**
   * Draws the content of the hex view.
   *
   * @param g The graphics context of the hex panel.
   */
  private void drawHexView(final Graphics g)
  {
    final int standardSize = 2 * m_charWidth;

    final int firstX = -m_firstColumn * m_charWidth + m_paddingHexLeft + m_offsetViewWidth;

    // drawing hex title
    if (m_headerVisible) {
      Font oldFont = getFont();
      g.setFont(oldFont.deriveFont(m_headerFontStyle));
      g.setColor(m_fontColorHeader);
      int x = firstX;
      for (int i = 0; i < m_bytesPerRow; i++) {
        if (i != 0) {
          if (i % m_bytesPerColumn == 0) {
            x += m_columnSpacing;
          }
        }
        g.drawString(HEX_BYTES[i & 0xFF], x, m_paddingTop);
        x += standardSize;
      }
      g.setFont(oldFont);
    }

    int x = firstX;
    int y = m_paddingTop + getHeaderHeight();

    boolean evenColumn = true;

    byte[] data = null;
    final int bytesToDraw;

    long dataOffset = getFirstVisibleByte();
    if (m_status == DefinitionStatus.DEFINED) {
      bytesToDraw = getBytesToDraw();
      data = m_dataProvider.getData(dataOffset, bytesToDraw);
    } else {
      bytesToDraw = getMaximumVisibleBytes();
    }

    // Iterate over all bytes in the data set and
    // print their hex value to the hex view.
    for (int i = 0; i < bytesToDraw; i++, dataOffset++) {
      if (i != 0) {
        if (i % m_bytesPerRow == 0) {
          // If the end of a row was reached, reset the x-coordinate
          // and set the y-coordinate to the next row.

          x = firstX;
          y += m_rowHeight;

          evenColumn = true;
        } else
        if (i % m_bytesPerColumn == 0) {
          // Add some spacing after each column.
          x += m_columnSpacing;

          evenColumn = !evenColumn;
        }
      }

      if (isEnabled()) {
        // determine whether to colorize additional horizontal space before or after the value
        int preSpaceX = 0, postSpaceX = 0;
        if (i % m_bytesPerColumn == 0) {
          preSpaceX = m_columnSpacing / 2;
        }
        if (i % m_bytesPerColumn == m_bytesPerColumn - 1) {
          postSpaceX = m_columnSpacing / 2;
        }

        if (selectionModel.isSelected(2 * dataOffset)) {
          g.setColor(m_selectionColor);
          g.fillRect(x - preSpaceX, y - m_charMaxAscent,
                     2 * m_charWidth + preSpaceX + postSpaceX, m_charMaxAscent + m_charMaxDescent);

          // Choose the right color for the hex view
          g.setColor(evenColumn ? m_fontColorHex1 : m_fontColorHex2);
        } else {
          final ColoredRange range = findColoredRange(dataOffset);
          if (range != null) {
            final Color bgColor = range.getBackgroundColor();

            if (bgColor != null) {
              g.setColor(bgColor);
            }

            g.fillRect(x - preSpaceX, y - m_charMaxAscent,
                       2 * m_charWidth + preSpaceX + postSpaceX, m_charMaxAscent + m_charMaxDescent);
            g.setColor(range.getColor());
          } else
          if (m_colorMapEnabled && m_colormap != null && m_colormap.colorize(data[i], dataOffset)) {
            final Color backgroundColor = m_colormap.getBackgroundColor(data[i], dataOffset);
            final Color foregroundColor = isShowModified() && isModified(dataOffset)
              ? m_fontColorModified
              : m_colormap.getForegroundColor(data[i], dataOffset);

            if (backgroundColor != null) {
              g.setColor(backgroundColor);
              g.fillRect(x - preSpaceX, y - m_charMaxAscent,
                         2 * m_charWidth + preSpaceX + postSpaceX, m_charMaxAscent + m_charMaxDescent);
            }

            if (foregroundColor != null) {
              g.setColor(foregroundColor);
            } else {
              g.setColor(evenColumn ? m_fontColorHex1 : m_fontColorHex2);
            }
          } else
          // Choose the right color for the hex view
          if (isShowModified() && isModified(dataOffset)) {
            g.setColor(m_fontColorModified);
          } else {
            g.setColor(evenColumn ? m_fontColorHex1 : m_fontColorHex2);
          }
        }
      } else {
        g.setColor(m_disabledColor != m_bgColorHex ? m_disabledColor : Color.WHITE);
      }

      if (m_status == DefinitionStatus.DEFINED) {
        // Number of bytes shown in the current column
        final int columnBytes = Math.min(m_dataProvider.getDataLength() - i, m_bytesPerColumn);

        final int dataPosition = m_flipBytes ? (i / m_bytesPerColumn) * m_bytesPerColumn
            + (columnBytes - (i % columnBytes) - 1) : i;

        // Print the data
        g.drawString(HEX_BYTES[data[dataPosition] & 0xFF], x, y);
      } else {
        g.drawString("??", x, y);
      }

      // Update the position of the x-coordinate
      x += standardSize;
    }
  }
  /**
   * Draws the content of the ASCII panel.
   *
   * @param g The graphics context of the hex panel.
   */
  private void drawAsciiPanel(final Graphics g)
  {
    final int initx = getAsciiViewLeft() + m_paddingAsciiLeft;

    int x = initx;
    int y = m_paddingTop + getHeaderHeight();

    // Drawing offset title
    if (m_headerVisible) {
      Font oldFont = getFont();
      g.setFont(oldFont.deriveFont(m_headerFontStyle));
      g.setColor(m_fontColorHeader);
      String title = getHeaderTitleAscii(m_addressMode);
      g.drawString(title, x, m_paddingTop);
      g.setFont(oldFont);
    }

    if (isEnabled()) {
      // Choose the right color for the ASCII view
      g.setColor(m_fontColorAscii);
    }
    else {
      g.setColor(m_disabledColor != m_bgColorAscii ? m_disabledColor : Color.WHITE);
    }

    byte[] data = null;
    int bytesToDraw;

    long dataOffset = getFirstVisibleByte();

    if (m_status == DefinitionStatus.DEFINED) {
      bytesToDraw = getBytesToDraw();
      data = m_dataProvider.getData(dataOffset, bytesToDraw);
    } else {
      bytesToDraw = getMaximumVisibleBytes();
    }

    for (int i = 0; i < bytesToDraw; i++, dataOffset++) {
      if (i != 0 && i % m_bytesPerRow == 0) {
        // If the end of a row is reached, reset the
        // x-coordinate and increase the y-coordinate.
        x = initx;
        y += m_rowHeight;
      }

      if (m_status == DefinitionStatus.DEFINED) {
        final byte b = data[i];

        if (isEnabled()) {
          if (selectionModel.isSelected(2 * dataOffset)) {
            g.setColor(m_selectionColor);
            g.fillRect(x, y - m_charMaxAscent, m_charWidth, m_charMaxAscent + m_charMaxDescent);

            // Choose the right color for the ASCII view
            if (isShowModified() && isModified(dataOffset)) {
              g.setColor(m_fontColorModified);
            } else {
              g.setColor(m_fontColorAscii);
            }
          } else {
            final ColoredRange range = findColoredRange(dataOffset);
            if (range != null && dataOffset + bytesToDraw >= range.getStart()) {
              final Color bgColor = range.getBackgroundColor();

              if (bgColor != null) {
                g.setColor(bgColor);
              }

              g.fillRect(x, y - m_charMaxAscent, m_charWidth, m_charMaxAscent + m_charMaxDescent);
              g.setColor(range.getColor());
            } else
            if (m_colorMapEnabled && m_colormap != null && m_colormap.colorize(b, dataOffset)) {
              final Color backgroundColor = m_colormap.getBackgroundColor(b, dataOffset);
              final Color foregroundColor = isShowModified() && isModified(dataOffset)
                ? m_fontColorModified
                : m_colormap.getForegroundColor(b, dataOffset);

              if (backgroundColor != null) {
                g.setColor(backgroundColor);
                g.fillRect(x, y - m_charMaxAscent, m_charWidth, m_charMaxAscent + m_charMaxDescent);
              }

              if (foregroundColor != null) {
                g.setColor(foregroundColor);
              } else {
                g.setColor(m_fontColorAscii);
              }
            } else
            // Choose the right color for the ASCII view
            if (isShowModified() && isModified(dataOffset)) {
              g.setColor(m_fontColorModified);
            } else {
              g.setColor(m_fontColorAscii);
            }
          }
        } else {
          g.setColor(m_disabledColor != m_bgColorAscii ? m_disabledColor : Color.WHITE);
        }

        g.drawString(ASCII_VIEW_TABLE[b & 0xFF], x, y);
      } else {
        g.drawString("?", x, y);
      }

      x += m_charWidth;
    }
  }

  //<editor-fold defaultstate="collapsed" desc="Caret">
  /**
   * Draws the caret.
   *
   * @param g The graphics context of the hex panel.
   */
  private void drawCaret(final Graphics2D g)
  {
    final long first = getFirstVisibleByte();
    if (m_caret.getPosition() < 2 * first
     || getCurrentColumn() > first + getMaximumVisibleBytes()
    ) {
      return;
    }

    final boolean isHex = m_activeView == Views.HEX_VIEW;
    drawCaretHexWindow(g, isHex);
    drawCaretAsciiWindow(g, !isHex);
  }
  /**
   * Draws the caret or outline in the hex window. Caret is drawn only if component
   * is editable, otherwise an outline is drawn.
   *
   * @param g The graphic context of the hex panel.
   * @param showCaret If {@code false}, show an outline instead of the caret.
   */
  private void drawCaretHexWindow(Graphics2D g, boolean showCaret)
  {
    final int currentRow = getCurrentRow() - m_firstRow;
    final int currentColumn = getCurrentColumn();

    // Calculate the position of the first character in the row.
    final int startLeft = 9 + m_offsetViewWidth;

    // Calculate the extra padding between columns.
    final int paddingColumns = currentColumn / (2 * m_bytesPerColumn) * m_columnSpacing;

    // Calculate the position of the character in the row.
    final int x = (currentColumn - m_firstColumn) * m_charWidth + startLeft + paddingColumns;

    // Calculate the position of the row.
    final int y = m_paddingTop + getHeaderHeight() - m_charHeight + m_rowHeight * currentRow;

    if (showCaret && isEditable()) {
      // Caret is blinking. When it in blink off state it is invisible
      if (m_caret.isVisible()) {
        m_caret.draw(g, x, y, m_rowHeight);
      }
    } else {
      final Stroke oldStroke = g.getStroke();
      g.setStroke(DOTTED_STROKE);
      // If caret in ASCII window, then outline byte, otherwise only one nibble
      g.drawRect(x, y, showCaret ? m_charWidth : m_charWidth*2+1, m_rowHeight);
      g.setStroke(oldStroke);
    }
  }
  /**
   * Draws the caret or outline in the ASCII window. Caret is drawn only if component
   * is editable, otherwise an outline is drawn.
   *
   * @param g The graphic context of the ASCII panel.
   * @param showCaret If {@code false}, show an outline instead of the caret.
   */
  private void drawCaretAsciiWindow(Graphics2D g, boolean showCaret)
  {
    final int currentRow = getCurrentRow() - m_firstRow;
    final int currentColumn = getCurrentColumn();
    final int currentCharacter = currentColumn / 2;

    // Calculate the position of the first character in the row
    final int startLeft = 9 + m_offsetViewWidth + m_hexViewWidth;

    // Calculate the position of the current character in the row
    final int x = (currentCharacter - m_firstColumn) * m_charWidth + startLeft;

    // Calculate the position of the row
    final int y = m_paddingTop + getHeaderHeight() - m_charHeight + m_rowHeight * currentRow;

    if (showCaret && isEditable()) {
      // Caret is blinking. When it in blink off state it is invisible
      if (m_caret.isVisible()) {
        m_caret.draw(g, x, y, m_rowHeight);
      }
    } else {
      final Stroke oldStroke = g.getStroke();
      g.setStroke(DOTTED_STROKE);
      g.drawRect(x, y, m_charWidth, m_rowHeight);
      g.setStroke(oldStroke);
    }
  }
  //</editor-fold>
  //</editor-fold>

  /**
   * Calculates current character and row sizes.
   */
  private void calculateSizes()
  {
    final Graphics g = getGraphics();
    if (g != null) {
      try {
        final FontMetrics m = g.getFontMetrics();
        m_rowHeight      = m.getHeight();
        m_charHeight     = m.getAscent();
        m_charMaxAscent  = m.getMaxAscent();
        m_charMaxDescent = m.getMaxDescent();
        m_charWidth      = (int)m.getStringBounds("0", g).getWidth();
      } finally {
        g.dispose();
      }
    }
  }

  private void changeBy(final ActionEvent event, final long length)
  {
    changeBy((event.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK, length);
  }
  /**
   * Changes or expands selection.
   *
   * @param expandSelection If {@code true}, expand or reduce selection, otherwise
   *        just select one nibble
   * @param length The number of nibbles by which the cursor moved
   */
  private void changeBy(boolean expandSelection, final long length)
  {
    final long oldPos = m_caret.getPosition();
    final long pos = oldPos + length;
    final long newPos;
    if (pos < 0) {
      newPos = 0;
    } else {
      final int nibbleCount = 2 * m_dataProvider.getDataLength();
      newPos = pos < nibbleCount ? pos : nibbleCount;
    }
    if (expandSelection) {
      final SelectionModel.Interval newSel = new SelectionModel.Interval(
        Math.min(oldPos, newPos),
        Math.max(oldPos, newPos)
      );

      if (selectionModel.isSelected(newPos)) {
        selectionModel.removeSelectionInterval(newSel);
      } else {
        selectionModel.addSelectionInterval(newSel);
      }
    } else {
      selectionModel.clearSelection();
    }
    m_caret.setPosition(newPos);

    if (newPos < 2 * getFirstVisibleByte()) {
      scrollToPosition(newPos);
    } else
    if (newPos >= 2 * (getFirstVisibleByte() + getMaximumVisibleBytes())) {
      scrollToPosition(newPos + 2 * (m_bytesPerRow - getMaximumVisibleBytes()));
    }

    m_caret.setVisible(true);
    repaint();
  }

  /**
   * Based on reference implementation from
   *   https://en.wikipedia.org/wiki/Boyer%E2%80%93Moore_string_search_algorithm
   *
   * Returns the start index of the first occurrence of the specified pattern.
   * If the pattern is not found, then -1 is returned.
   *
   * @param startPos The position within the data to start searching in range
   *        {@code [0; getData().getDataLength())}.
   * @param length The length of the data section to search.
   * @param pattern The pattern to search.
   * @param caseSensitive Indicates whether to compare case-sensitive or not.
   *
   * @return The start index of the first match, or -1 otherwise.
   */
  private long findIndexOf(long startPos, long length, byte[] pattern, boolean caseSensitive)
  {
    if (pattern.length == 0) {
      return startPos;
    }

    final IDataProvider data = getData();

    final int dataLength = data.getDataLength();
    if (startPos < 0) startPos = 0;
    if (length < 0) length = 0;
    if (startPos+length > dataLength) length = dataLength - startPos;
    if (length <= 0) {
      return -1;
    }

    // normalizing search string
    for (int i = 0; i < pattern.length; i++) {
      pattern[i] = normalizeByte(pattern[i], caseSensitive);
    }

    int[] byteTable = findMakeByteTable(pattern);
    int[] offsetTable = findMakeOffsetTable(pattern);
    int j;
    for (long off = startPos + pattern.length - 1; off < startPos+length;) {
      byte b;
      for (j = pattern.length - 1;
           pattern[j] == (b = normalizeByte(data.getData(off, 1)[0], caseSensitive));
           --off, --j) {
        if (j == 0) {
          return off;
        }
      }
      off += Math.max(offsetTable[pattern.length - 1 - j], byteTable[b & 0xFF]);
    }
    return -1;
  }

  /**
   * Converts the specified byte value into a lower-cased counterpart if caseSensitive is false.
   */
  private byte normalizeByte(byte value, boolean caseSensitive)
  {
    if (!caseSensitive) {
      final char ch = toChar(value);
      if (isPrintableCharacter(ch)) {
        final char chLo = Character.toLowerCase(ch);
        return (byte)(chLo < 0x80 ? chLo : '?');
      }
    }
    return value;
  }

  /**
   * Based on reference implementation from
   *   https://en.wikipedia.org/wiki/Boyer%E2%80%93Moore_string_search_algorithm
   *
   * Makes the jump table based on the mismatched byte information.
   */
  private int[] findMakeByteTable(byte[] pattern)
  {
    int[] table = new int[256];
    for (int i = 0; i < table.length; i++) {
      table[i] = pattern.length;
    }
    for (int i = 0; i < pattern.length - 1; i++) {
      table[pattern[i] & 255] = pattern.length - 1 - i;
    }
    return table;
  }

  /**
   * Based on reference implementation from
   *   https://en.wikipedia.org/wiki/Boyer%E2%80%93Moore_string_search_algorithm
   *
   * Makes the jump table based on the scan offset which mismatch occurs.
   */
  private int[] findMakeOffsetTable(byte[] pattern)
  {
    int[] table = new int[pattern.length];
    int lastPrefixPos = pattern.length;
    for (int i = pattern.length - 1; i >= 0; i--) {
      if (findIsPrefix(pattern, i + 1)) {
        lastPrefixPos = i + 1;
      }
      table[pattern.length - 1 - i] = lastPrefixPos - i + pattern.length - 1;
    }
    for (int i = 0; i < pattern.length - 1; i++) {
      int slen = findSuffixLength(pattern, i);
      table[slen] = pattern.length - 1 - i + slen;
    }
    return table;
  }

  /**
   * Based on reference implementation from
   *   https://en.wikipedia.org/wiki/Boyer%E2%80%93Moore_string_search_algorithm
   *
   * Is pattern[p:end] a prefix of pattern?
   */
  private boolean findIsPrefix(byte[] pattern, int p)
  {
    for (int i = p, j = 0; i < pattern.length; i++, j++) {
      if (pattern[i] != pattern[j]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Based on reference implementation from
   *   https://en.wikipedia.org/wiki/Boyer%E2%80%93Moore_string_search_algorithm
   *
   * Returns the maximum length of the subpattern ends at p and is a suffix.
   */
  private int findSuffixLength(byte[] pattern, int p)
  {
    int len = 0;
    for (int i = p, j = pattern.length - 1; i >= 0 && pattern[i] == pattern[j]; i--, j--) {
      len += 1;
    }
    return len;
  }

  /**
   * Notifies all registered HexListeners that the view has been changed.
   * @param view The new view.
   */
  private void fireHexListener(Views view)
  {
    HexViewEvent event = null;
    Object[] l = m_listeners.getListenerList();
    for (int i = l.length - 2; i >= 0; i -= 2) {
      if (l[i] == IHexViewListener.class) {
        if (event == null) {
          event = new HexViewEvent(this, view);
        }
        ((IHexViewListener)l[i+1]).stateChanged(event);
      }
    }
  }

  /**
   * Returns the number digits required to fully print an offset in the given address mode.
   * @param mode The address mode.
   * @return The number of digits.
   */
  private int getAddressDigits(AddressMode mode)
  {
    switch (mode) {
      case BIT8:  return 2;
      case BIT16: return 4;
      case BIT24: return 6;
      case BIT32: return 8;
      case BIT40: return 10;
      case BIT48: return 12;
      case BIT56: return 14;
      default:    return 16;
    }
  }

  /**
   * Returns a format string for displaying an offset in the given address mode.
   * @param mode The address mode.
   * @return Format string for given address mode.
   */
  private String getAddressModeFormat(AddressMode mode)
  {
    return String.format("%%0%1$dX", getAddressDigits(mode));
  }

  /**
   * Returns the left coordinate of the ASCII view.
   *
   * @return The left coordinate of the ASCII view.
   */
  private int getAsciiViewLeft()
  {
    return getHexViewLeft() + getHexViewWidth();
  }

  /**
   * Returns the number of bytes that need to be displayed.
   *
   * @return The number of bytes that need to be displayed.
   */
  private int getBytesToDraw()
  {
    final int firstVisibleByte = (int)getFirstVisibleByte();

    final int maxBytes = getMaximumVisibleBytes() + m_bytesPerRow;

    final int restBytes = m_dataProvider.getDataLength() - firstVisibleByte;

    return Math.min(maxBytes, restBytes);
  }

  /**
   * Returns the size of a hex view column in pixels (includes column spacing).
   *
   * @return The size of a hex view column in pixels.
   */
  private int getColumnSize()
  {
    return 2 * m_bytesPerColumn * m_charWidth + m_columnSpacing;
  }

  /**
   * Returns the column of the byte at the current position.
   *
   * @return The column of the byte at the current position.
   */
  private int getCurrentColumn()
  {
    return (int) m_caret.getPosition() % (2 * m_bytesPerRow);
  }

  /**
   * Returns the row of the byte at the current position.
   *
   * @return The row of the byte at the current position.
   */
  private int getCurrentRow()
  {
    return (int) m_caret.getPosition() / (2 * m_bytesPerRow);
  }

  /**
   * Returns the offset to first visible byte.
   *
   * @return Offset in range in range {@code [0; getData().getDataLength())}
   */
  private long getFirstVisibleByte()
  {
    return m_firstRow * m_bytesPerRow;
  }

  /**
   * Returns the height of the header panel.
   */
  private int getHeaderHeight()
  {
    if (m_headerVisible) {
      final Graphics g = getGraphics();
      if (g != null) {
        try {
          final FontMetrics m = g.getFontMetrics();
          return m.getMaxAscent() + m.getMaxDescent();
        } finally {
          g.dispose();
        }
      }
    }
    return 0;
  }

  /**
   * Returns the title for the offset column. Result depends on the actual width of the offset column.
   * @param mode Current address mode.
   * @return The String "Offset" in different lengths.
   */
  private String getHeaderTitleOffset(AddressMode mode)
  {
    final int length = getAddressDigits(mode);
    String retVal;
    if (length < 2) {
      retVal = "";
    } else if (length < 4) {
      retVal = "Of";
    } else if (length < 6) {
      retVal = "Ofs.";
    } else if (length < 9) {
      retVal = "Offset";
    } else {
      retVal = "Offset(h)";
    }
    return retVal;
  }

  /**
   * Returns the title for the ascii column. Result depends on the actual width of the offset column.
   * @param mode Current address mode.
   * @return The String "Ascii" in different lengths.
   */
  private String getHeaderTitleAscii(AddressMode mode)
  {
    final int length = m_bytesPerRow;
    String retVal;
    if (length < 5) {
      retVal = "";
    } else {
      retVal = "ASCII";
    }
    return retVal;
  }


  /**
   * Returns the left position of the hex view.
   *
   * @return The left position of the hex view.
   */
  private int getHexViewLeft()
  {
    return -m_firstColumn * m_charWidth + m_offsetViewWidth;
  }

  /**
   * Returns the maximum number of visible bytes.
   *
   * @return The maximum number of visible bytes.
   */
  private int getMaximumVisibleBytes()
  {
    return getNumberOfVisibleRows() * m_bytesPerRow;
  }

  /**
   * Returns the index of the nibble below given coordinates.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   *
   * @return The nibble index at the coordinates in range {@code [0; 2*getData().getDataLength())}
   *         or -1 if there is no nibble at the coordinates.
   */
  private long getNibbleAtCoordinate(final int x, final int y)
  {
    if (m_dataProvider != null && y >= m_paddingTop + getHeaderHeight() - m_font.getSize()) {
      final int left = getHexViewLeft();
      // ____________________________
      // |offsets|     hex    |ascii|
      // |       |<=left      |     |
      // '--------------------------'
      if (x >= left + getHexViewWidth()) {
        // Cursor is in ASCII view
        return getNibbleAtCoordinatesAscii(x, y);
      }
      if (x >= left + m_paddingHexLeft) {
        // Cursor is in hex view
        return getNibbleAtCoordinatesHex(x, y);
      }
    }

    return -1;
  }

  /**
   * Returns the index of the nibble below given coordinates in the ASCII view.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   *
   * @return The nibble index at the coordinates in range {@code [0; 2*getData().getDataLength())}
   *         or -1 if there is no nibble at the coordinates.
   */
  private long getNibbleAtCoordinatesAscii(final int x, final int y)
  {
    // Normalize the x coordinate to inside the ASCII view
    final int normalizedX = x - (getAsciiViewLeft() + m_paddingAsciiLeft);

    if (normalizedX < 0 || normalizedX / m_charWidth >= m_bytesPerRow) {
      return -1;
    }

    // Find the row at the coordinate
    final int row = (y - (m_paddingTop + getHeaderHeight() - m_charHeight)) / m_rowHeight;

    final long byteAtPos = getFirstVisibleByte()
                         + row * m_bytesPerRow
                         + normalizedX / m_charWidth;

    return byteAtPos >= m_dataProvider.getDataLength() ? -1 : 2 * byteAtPos;
  }

  /**
   * Returns the index of the nibble below given coordinates in the hex view.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   *
   * @return The nibble index at the coordinates in range {@code [0; 2*getData().getDataLength())}
   *         or -1 if there is no nibble at the coordinates.
   */
  private long getNibbleAtCoordinatesHex(final int x, final int y)
  {
    // Normalize the x coordinate to inside the hex view
    final int normalizedX = x - (getHexViewLeft() + m_paddingHexLeft);

    final int columnSize = getColumnSize();

    // Find the column at the specified coordinate.
    final int column = normalizedX / columnSize;

    // Return if the cursor is at the spacing at the end of a line.
    if (column >= m_bytesPerRow / m_bytesPerColumn) {
      return -1;
    }

    // Find the coordinate relative to the beginning of the column.
    final int xInColumn = normalizedX % columnSize;

    // Find the nibble inside the column.
    final int nibbleInColumn = xInColumn / m_charWidth;

    // Return if the cursor is in the spacing between columns.
    if (nibbleInColumn >= 2 * m_bytesPerColumn) {
      return -1;
    }

    // Find the row at the coordinate
    final int row = (y - (m_paddingTop + getHeaderHeight() - m_charHeight)) / m_rowHeight;

    final long byteAtPos = getFirstVisibleByte()
                         + row * m_bytesPerRow
                         + column * m_bytesPerColumn;
    final long position = 2 * byteAtPos + nibbleInColumn;

    return position >= 2 * m_dataProvider.getDataLength() ? -1 : position;
  }

  /**
   * Returns the number of visible rows for current component height.
   *
   * @return The number of visible rows.
   */
  private int getNumberOfVisibleRows()
  {
    final int rawHeight = getHeight() - m_paddingTop - getHeaderHeight() - m_horizontalScrollbar.getHeight();
    return rawHeight / m_rowHeight + (rawHeight % m_rowHeight == 0 ? 0 : 1);
  }

  /**
   * Initializes the keys that can be used by the user inside the component.
   */
  private void initHotkeys()
  {
    int none = 0;
    int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    int shift = Event.SHIFT_MASK;

    // Don't change focus on TAB
    setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, new HashSet<KeyStroke>());

    final InputMap inputMap = this.getInputMap();
    final ActionMap actionMap = this.getActionMap();

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, none), "LEFT");
    actionMap.put("LEFT", m_leftAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, shift), "shift LEFT");
    actionMap.put("shift LEFT", m_shiftLeftAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, none), "RIGHT");
    actionMap.put("RIGHT", m_rightAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, shift), "shift RIGHT");
    actionMap.put("shift RIGHT", m_shiftRightAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, none), "UP");
    actionMap.put("UP", m_upAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, shift), "shift UP");
    actionMap.put("shift UP", m_upAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, none), "DOWN");
    actionMap.put("DOWN", m_downAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, shift), "shift DOWN");
    actionMap.put("shift DOWN", m_downAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, none), "PAGE_DOWN");
    actionMap.put("PAGE_DOWN", m_pageDownAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, shift), "shift PAGE_DOWN");
    actionMap.put("shift PAGE_DOWN", m_pageDownAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, none), "PAGE_UP");
    actionMap.put("PAGE_UP", m_pageUpAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, shift), "shift PAGE_UP");
    actionMap.put("shift PAGE_UP", m_pageUpAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, none), "HOME");
    actionMap.put("HOME", m_homeLineAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, shift), "shift HOME");
    actionMap.put("shift HOME", m_homeLineAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, ctrl), "ctrl HOME");
    actionMap.put("ctrl HOME", m_homeDocAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, ctrl+shift), "ctrl shift HOME");
    actionMap.put("ctrl shift HOME", m_homeDocAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, none), "END");
    actionMap.put("END", m_endLineAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, shift), "shift END");
    actionMap.put("shift END", m_endLineAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, ctrl), "ctrl END");
    actionMap.put("ctrl END", m_endDocAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, ctrl+shift), "ctrl shift END");
    actionMap.put("ctrl shift END", m_endDocAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, none), "TAB");
    actionMap.put("TAB", m_tabAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ctrl), "ctrl A");
    actionMap.put("ctrl A", m_SelectAllAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ctrl), "ctrl V");
    actionMap.put("ctrl V", m_PasteTextAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ctrl), "ctrl C");
    actionMap.put("ctrl C", m_CopyTextAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ctrl), "ctrl Z");
    actionMap.put("ctrl Z", m_UndoAction);

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ctrl), "ctrl Y");
    actionMap.put("ctrl Y", m_RedoAction);
  }

  /**
   * Initializes all internal listeners.
   */
  private void initListeners()
  {
    // Add the input listeners
    addMouseListener(m_listener);
    addMouseMotionListener(m_listener);
    addMouseWheelListener(m_listener);
    addFocusListener(m_listener);
    addComponentListener(m_listener);
    addKeyListener(m_listener);
    addUndoableEditListener(m_listener);

    m_caret.addCaretListener(m_listener);
  }

  /**
   * Creates and initializes the scroll bar that is used to scroll through the
   * data.
   */
  private void initScrollbar()
  {
    m_scrollbar.addAdjustmentListener(m_listener);

    add(m_scrollbar, BorderLayout.EAST);

    m_horizontalScrollbar.addAdjustmentListener(m_listener);

    add(m_horizontalScrollbar, BorderLayout.SOUTH);
  }

  /**
   * Determines whether data to be displayed is available.
   *
   * @return True, if data is available. False, otherwise.
   */
  private boolean isDataAvailable()
  {
    return m_dataProvider != null;
  }

  private boolean isInsideAsciiView(final int x, final int y)
  {
    return y >= m_paddingTop + getHeaderHeight() - m_font.getSize() && x >= getAsciiViewLeft();
  }

  private boolean isInsideHexView(final int x, final int y)
  {
    return y >= m_paddingTop + getHeaderHeight() - m_font.getSize() && x >= getHexViewLeft()
        && x < getHexViewLeft() + getHexViewWidth();
  }

  /**
   * Determines whether a certain position is visible in the view.
   *
   * @param position Offset in nibbles in range {@code [0; 2*getData().getDataLength())}
   *
   * @return {@code true}, if the position is visible, {@code false}, otherwise.
   */
  private boolean isPositionVisible(final long position)
  {
    final long firstVisible = getFirstVisibleByte();
    final long lastVisible  = firstVisible + getMaximumVisibleBytes();

    return position >= 2 * firstVisible && position <= 2 * lastVisible;
  }

  /**
   * Resets the current graphic buffer and prepares it for another round of
   * drawing.
   */
  private void resetBufferedGraphic(Graphics g)
  {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setFont(m_font);
  }

  /**
   * Scrolls the scroll bar so that it matches the given position.
   *
   * @param position The position to scroll to in nibbles in range
   *        {@code [0; 2*getData().getDataLength())}
   */
  private void scrollToPosition(final long position)
  {
    m_scrollbar.setValue((int) position / (2 * m_bytesPerRow));
  }

  /**
   * Moves the current position of the caret and notifies the listeners about
   * the position change.
   *
   * @param newPosition The new position of the caret in nibbles in range
   *        {@code [0; 2*getData().getDataLength())}
   */
  private void setCurrentPosition(final long newPosition)
  {
    if (!isPositionVisible(newPosition)) {
      scrollToPosition(newPosition);
    }

    selectionModel.clearSelection();
    m_caret.setPosition(newPosition);
  }

  /**
   * Updates the maximum scroll range of the scroll bar depending on the number
   * of bytes in the current data set.
   */
  private void setScrollBarMaximum()
  {
    if (m_dataProvider == null) {
      m_scrollbar.setMaximum(1);
      m_horizontalScrollbar.setMaximum(1);
    }
    else {
      final int visibleRows = getNumberOfVisibleRows();

      final int totalRows = m_dataProvider.getDataLength() / m_bytesPerRow;
      // 2 - Count of empty rows that can be scrolled down
      int scrollRange = 2 + totalRows - visibleRows;

      // If all rows visible, disable vertical scrollbar
      if (scrollRange < 0) {
        scrollRange = 0;
        m_scrollbar.setEnabled(false);
      } else {
        m_scrollbar.setEnabled(true);
      }

      m_scrollbar.setValue(Math.min(m_scrollbar.getValue(), scrollRange));
      m_scrollbar.setMaximum(scrollRange + visibleRows);
      m_scrollbar.setVisibleAmount(visibleRows);
      m_scrollbar.setBlockIncrement(visibleRows);

      final int totalWidth = getAsciiViewLeft() + m_paddingAsciiLeft + m_charWidth * m_bytesPerRow;

      final int realWidth = getWidth() - m_scrollbar.getWidth();

      if (realWidth >= totalWidth) {
        m_horizontalScrollbar.setValue(0);
        m_horizontalScrollbar.setEnabled(false);
      }
      else {
        m_horizontalScrollbar.setMaximum((totalWidth - realWidth) / m_charWidth + 1);
        m_horizontalScrollbar.setEnabled(true);
      }
    }
  }

  private void updateHexViewWidth()
  {
    m_hexViewWidth = 15 + getColumnSize() * getBytesPerRow() / getBytesPerColumn();
  }

  /**
   * Calculates and sets the size of the offset view depending on the currently
   * selected address mode.
   */
  private void updateOffsetViewWidth()
  {
    final int addressBytes = getAddressDigits(m_addressMode);
    m_offsetViewWidth = PADDING_OFFSETVIEW + m_charWidth * addressBytes;
  }

  /**
   * Calculates and sets the preferred size of the component.
   */
  private void updatePreferredSize()
  {
    // TODO: Improve this
    final int width = m_offsetViewWidth + m_hexViewWidth + 18 * m_charWidth + m_scrollbar.getWidth();
    setPreferredSize(new Dimension(width, getHeight()));
    revalidate();
  }

  /**
   * Check if specified byte can be used to expand selection in ASCII view.
   *
   * @param value Byte for check
   *
   * @return {@code true}, if byte can be included in double-click selection expansion
   *         and {@code false} otherwise
   */
  private boolean needSkip(byte value)
  {
    final char ch = toChar(value);
    return ".,:;()?!-'/\"".indexOf(ch) >= 0 // stop-symbols
        || Character.isWhitespace(ch)       // whitespace
        || !getFont().canDisplay(ch);       // non-displayable characters
  }
  /**
   * Expands selection from specified byte position while byte represents
   * not-whitespace, not-special ({@code .,:;()?!-'/"}) symbols and symbols,
   * that {@link #getFont currect font} can display.
   *
   * @param offset Initial byte offset for selection in range
   *        {@code [0; getData().getDataLength())}
   */
  private void expandSelection(long offset)
  {
    // Starting from initial position, find word delimiter characters in both directions
    long start = offset;
    long end = offset;
    if (!needSkip(m_dataProvider.getData(offset, 1)[0])) {
      // find starting delimiter
      for (int i = 1; i < offset; i++) {
        if (needSkip(m_dataProvider.getData(offset-i, 1)[0])) {
          break;
        }
        start--;
      }

      // find ending delimiter
      final long maxLength = m_dataProvider.getDataLength() - offset;
      for (int i = 1; i < maxLength; i++) {
        if (needSkip(m_dataProvider.getData(offset+i, 1)[0])) {
          break;
        }
        end++;
      }
    }

    end = 2 * (end + 1);
    selectionModel.setSelectionInterval(2 * start, end);
    m_caret.setPosition(end);
  }
  //</editor-fold>

  public void dispose()
  {
    removeMouseListener(m_listener);
    removeMouseMotionListener(m_listener);
    removeMouseWheelListener(m_listener);
    removeFocusListener(m_listener);
    removeComponentListener(m_listener);
    removeKeyListener(m_listener);

    m_caret.removeListener(m_listener);

    m_caret.stop();
  }

  /**
   * Selects all data in the component.
   */
  public void selectAll()
  {
    m_SelectAllAction.actionPerformed(new ActionEvent(this, Event.ACTION_EVENT, ""));
  }

  /**
   * Tests whether a character is a valid character of a hexadecimal string.
   *
   * @param c The character to test.
   *
   * @return True, if the character is a hex character. False, otherwise.
   */
  private static boolean isHexCharacter(final char c)
  {
    return c >= '0' && c <= '9'
        || c >= 'a' && c <= 'f'
        || c >= 'A' && c <= 'F';
  }

  /**
   * Tests whether a character is a printable ASCII character.
   *
   * @param c The character to test.
   *
   * @return {@code true}, if the character is a printable ASCII character,
   *         {@code false} otherwise.
   */
  private static boolean isPrintableCharacter(final char c)
  {
    final Character.UnicodeBlock block = Character.UnicodeBlock.of(c);

    return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED
        && block != null && block != Character.UnicodeBlock.SPECIALS;
  }

  /**
   * Converts the byte value into a character representation.
   *
   * @param b The byte value to convert.
   *
   * @return The character representation of the byte value.
   */
  private static char toChar(byte b)
  {
    return b >= 0 ? (char)b : '\uFFFD';
  }

  //<editor-fold defaultstate="collapsed" desc="Internal classes">
  /** Abstract superclass for undoable edits in the JHexView component. */
  public abstract class AbstractEdit extends AbstractUndoableEdit
  {
    private final String name;

    public AbstractEdit(String name)
    {
      this.name = name;
    }

    @Override
    public String getPresentationName()
    {
      return (name != null) ? name : "";
    }

    @Override
    public String getRedoPresentationName()
    {
      return getPresentationName();
    }

    @Override
    public String getUndoPresentationName()
    {
      return getPresentationName();
    }

    @Override
    public String toString()
    {
      return name;
    }
  }

  private class ActionDown extends AbstractAction
  {
    private static final long serialVersionUID = -6501310447863685486L;

    @Override
    public void actionPerformed(final ActionEvent event)
    {
      changeBy(event, 2L * m_bytesPerRow);
    }
  }

  private class ActionEnd extends AbstractAction
  {
    private static final long serialVersionUID = 3857972387525998638L;

    private final boolean isCtrl;

    public ActionEnd(boolean isCtrl)
    {
      this.isCtrl = isCtrl;
    }

    @Override
    public void actionPerformed(final ActionEvent event)
    {
      final long change;
      if (isCtrl) {
        change = getData().getDataLength()*2 - m_caret.getPosition() - 2;
      } else {
        change = (m_bytesPerRow*2) - (m_caret.getPosition() % (m_bytesPerRow*2)) - 2;
      }
      changeBy(event, change);
    }
  }

  private class ActionHome extends AbstractAction
  {
    private static final long serialVersionUID = 3857972387525998637L;

    private final boolean isCtrl;

    public ActionHome(boolean isCtrl)
    {
      this.isCtrl = isCtrl;
    }

    @Override
    public void actionPerformed(final ActionEvent event)
    {
      final long change;
      if (isCtrl) {
        change = -m_caret.getPosition();
      } else {
        change = -(m_caret.getPosition() % (m_bytesPerRow*2));
      }
      changeBy(event, change);
    }
  }

  private class ActionLeft extends AbstractAction
  {
    private static final long serialVersionUID = -9032577023548944503L;

    private final boolean clearSelection;

    /** @param clearSelection If {@code true}, then selection will be cleared before move caret. */
    public ActionLeft(boolean clearSelection)
    {
      this.clearSelection = clearSelection;
    }

    @Override
    public void actionPerformed(final ActionEvent event)
    {
      if (clearSelection && !selectionModel.isEmpty()) {
        // Move caret to the first nibble of selected block
        // [           |]     }|___________{
        // |    caret -'|  =>  `- caret
        // `-selection--'         selection cleared
        // [|           ]     }|___________{
        // |`-- caret   |  =>  `- caret
        // `-selection--'         selection cleared
        final long cur = m_caret.getPosition();
        final SelectionModel.Interval range = selectionModel.findInterval(cur);
        // Round up selection and position to even nibbles when switch active view
        // to ASCII view. Constant ~1L clears last bit which effectively makes number even
        changeBy(event, range == null ? 0L : (range.getStart() & ~1L) - cur);
      } else {
        changeBy(event, m_activeView == Views.HEX_VIEW ? -1L : -2L);
      }
    }
  }

  private class ActionPageDown extends AbstractAction
  {
    private static final long serialVersionUID = 490837791577654025L;

    @Override
    public void actionPerformed(final ActionEvent event)
    {
      changeBy(event, 2L * getNumberOfVisibleRows() * m_bytesPerRow);
    }
  }

  private class ActionPageUp extends AbstractAction
  {
    private static final long serialVersionUID = -7424423002191015929L;

    @Override
    public void actionPerformed(final ActionEvent event)
    {
      changeBy(event, -2L * getNumberOfVisibleRows() * m_bytesPerRow);
    }
  }

  private class ActionRight extends AbstractAction
  {
    private static final long serialVersionUID = 3857972387525998636L;

    private final boolean clearSelection;

    /** @param clearSelection If {@code true}, then selection will be cleared before move caret. */
    public ActionRight(boolean clearSelection)
    {
      this.clearSelection = clearSelection;
    }

    @Override
    public void actionPerformed(final ActionEvent event)
    {
      if (clearSelection && !selectionModel.isEmpty()) {
        // Move caret to the last nibble of selected block
        // [           |]     }___________|{
        // |    caret -'|  =>     caret --'
        // `-selection--'         selection cleared
        // [|           ]     }___________|{
        // |`-- caret   |  =>     caret --'
        // `-selection--'         selection cleared
        final long cur = m_caret.getPosition();
        final SelectionModel.Interval range = selectionModel.findInterval(cur);
        // Round up selection and position to even nibbles when switch active view
        // to ASCII view. Constant ~1L clears last bit which effectively makes number even
        changeBy(event, range == null ? 0L : ((range.getEnd() + 1) & ~1L) - cur);
      } else {
        changeBy(event, m_activeView == Views.HEX_VIEW ? 1L : 2L);
      }
    }
  }

  /** Contains actions for all kinds of specialized shortcuts. */
  private class ActionShortcut extends AbstractAction
  {
    private static final long serialVersionUID = -3513103611571283107L;

    private final KeyStroke keyStroke;

    public ActionShortcut(KeyStroke keyStroke)
    {
      this.keyStroke = keyStroke;
    }

    @Override
    public void actionPerformed(final ActionEvent event)
    {
      int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

      if (isKeyStroke(KeyEvent.VK_A, ctrl)) {
        // "Select all" action
        final long end = 2 * m_dataProvider.getDataLength();
        selectionModel.setSelectionInterval(0, end);
        m_caret.setPosition(end);
      } else if (isKeyStroke(KeyEvent.VK_V, ctrl)) {
        // "Paste" action
        TransferHandler.getPasteAction().actionPerformed(event);
      } else if (isKeyStroke(KeyEvent.VK_C, ctrl)) {
        // "Copy" action
        TransferHandler.getCopyAction().actionPerformed(event);
      } else if (isKeyStroke(KeyEvent.VK_Z, ctrl)) {
        // "Undo" action
        undo();
      } else if (isKeyStroke(KeyEvent.VK_Y, ctrl)) {
        // "Redo" action
        redo();
      }
    }

    private boolean isKeyStroke(int key, int modifiers)
    {
      if (keyStroke != null) {
        return (keyStroke.getKeyCode() == key) && ((keyStroke.getModifiers() & modifiers) == modifiers);
      } else {
        return false;
      }
    }
  }

  private class ActionTab extends AbstractAction
  {
    private static final long serialVersionUID = -3265020583339369531L;

    @Override
    public void actionPerformed(final ActionEvent event)
    {
      // Switch between hex and ASCII view
      if (m_activeView == Views.HEX_VIEW) {
        m_activeView = Views.ASCII_VIEW;
        // Round up selection and position to even nibbles when switch active view
        // to ASCII view. Constant ~1L clears last bit which effectively makes number even
        m_caret.setPosition(m_caret.getPosition() & ~1L);
        if (!selectionModel.isEmpty()) {
          // If some selection performed, selects all byte of selected nibble
          // Make copy, because we can not change selection when iterate throught seleted ranges
          for (final SelectionModel.Interval range : new ArrayList<>(selectionModel.selected)) {
            selectionModel.addSelectionInterval(range.getStart() & ~1L,
                                                range.getEnd()   & ~1L);
          }
        }
      } else {
        m_activeView = Views.HEX_VIEW;
      }

      fireHexListener(m_activeView);
      m_caret.setVisible(true);
      repaint();
    }
  }

  private class ActionUp extends AbstractAction
  {
    private static final long serialVersionUID = -3513103611571283106L;

    @Override
    public void actionPerformed(final ActionEvent event)
    {
      changeBy(event, -2L * m_bytesPerRow);
    }
  }

  private class ActionWaitingForData extends AbstractAction
  {
    private static final long serialVersionUID = -610823391617272365L;

    /** Offset in range {@code [0; getData().getDataLength())}. */
    private final long m_offset;

    private final int m_size;

    private ActionWaitingForData(final long offset, final int size)
    {
      m_offset = offset;
      m_size = size;
    }

    @Override
    public void actionPerformed(final ActionEvent event)
    {
      if (m_dataProvider.hasData(m_offset, m_size)) {

        JHexView.this.setEnabled(true);
        setDefinitionStatus(DefinitionStatus.DEFINED);

        ((Timer) event.getSource()).stop();
      }
      else if (!m_dataProvider.keepTrying()) {
        ((Timer) event.getSource()).stop();
      }
    }
  }

  /** Represents the undoable edit for a single byte or character. */
  public class DataEdit extends AbstractEdit
  {
    /** Offset of changed data in range {@code [0; getData().getDataLength())}. */
    private final long offset;
    private final byte oldValue, newValue;
    private final Views view;

    public DataEdit(long offset, byte oldValue, byte newValue, Views view)
    {
      super("Typing");
      this.offset = offset;
      this.oldValue = oldValue;
      this.newValue = newValue;
      this.view = view;
    }

    @Override
    public void undo() throws CannotUndoException
    {
      super.undo();
      if (getDefinitionStatus() == DefinitionStatus.DEFINED) {
        setActiveView(view);
        getData().setData(offset, new byte[]{oldValue});
        clearModified(offset, false);
        setCurrentPosition(2 * offset);
      } else {
        throw new CannotUndoException();
      }
    }

    @Override
    public void redo() throws CannotRedoException
    {
      super.redo();
      if (getDefinitionStatus() == DefinitionStatus.DEFINED) {
        setActiveView(view);
        getData().setData(offset, new byte[]{newValue});
        setModified(offset);
        setCurrentPosition(2 * offset + 2);
      } else {
        throw new CannotRedoException();
      }
    }
  }

  /**
   * Handles copy to and paste from clipboard actions.
   *
   * @author argent77
   *
   */
  private class HexTransferHandler extends TransferHandler
  {
    @Override
    public boolean canImport(TransferSupport support)
    {
      // we only import Strings
      return support.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    @Override
    public int getSourceActions(JComponent c)
    {
      return COPY;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support)
    {
      if (support.getComponent() instanceof JHexView && canImport(support)) {
        JHexView hv = (JHexView)support.getComponent();
        try {
          final String data = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
          if (data != null && !support.isDrop()) {
            if (hv.m_caret.getPosition() < 2 * getData().getDataLength()) {
              if (hv.getActiveView() == Views.HEX_VIEW) {
                // processing hex view
                KeyEvent event = new KeyEvent(hv, 0, 0, 0, 0, '\0');
                for (int i = 0; i < data.length(); i++) {
                  char ch = data.charAt(i);
                  if (!Character.isWhitespace(ch)) {
                    event.setKeyChar(ch);
                    hv.m_listener.keyPressed(event);
                  }
                }
              } else {
                // processing ascii view
                KeyEvent event = new KeyEvent(hv, 0, 0, 0, 0, '\0');
                for (int i = 0; i < data.length(); i++) {
                  char ch = data.charAt(i);
                  event.setKeyChar(ch);
                  hv.m_listener.keyPressed(event);
                }
              }
              return true;
            }
          }
        } catch (UnsupportedFlavorException ufe) {
          ufe.printStackTrace();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
      return false;
    }

    @Override
    protected Transferable createTransferable(JComponent c)
    {
      if (c instanceof JHexView) {
        JHexView hv = (JHexView)c;
        StringBuilder sb = new StringBuilder();

        if (hv.m_activeView == Views.HEX_VIEW) {
          for (final SelectionModel.Interval r : hv.selectionModel) {
            final byte[] buffer = m_dataProvider.getData(r.getStart(), (int) r.getLength());
            // processing hex view
            for (final byte b : buffer) {
              sb.append(HEX_BYTES[b & 0xFF]).append(' ');
            }
          }
          // Remove last space
          if (sb.length() != 0) {
            sb.setLength(sb.length() - 1);
          }
        } else {
          for (final SelectionModel.Interval r : hv.selectionModel) {
            final byte[] buffer = m_dataProvider.getData(r.getStart(), (int) r.getLength());
            // processing ascii view
            for (final byte b : buffer) {
              sb.append(ASCII_VIEW_TABLE[b & 0xFF]);
            }
          }
        }

        return new StringSelection(sb.toString());
      }
      return null;
    }
  }

  /**
   * Event listeners are moved into an internal class to avoid publishing the
   * listener methods in the public interface of the JHexView.
   *
   * @author sp
   *
   */
  private class InternalListener implements AdjustmentListener, MouseListener, MouseMotionListener,
      FocusListener, ICaretListener, IDataChangedListener, ComponentListener, KeyListener,
      MouseWheelListener, UndoableEditListener
  {
    /** Nibble, from what dragging started. If -1, then dragging not performed. */
    private long startNibble = -1;

    private void keyPressedInAsciiView(char ch)
    {
      final long offset = m_caret.getPosition() / 2;

      final byte[] data = m_dataProvider.getData(offset, 1);
      if (data == null || data.length == 0) {
        return;
      }
      final byte oldValue = data[0];
      final byte newValue = (byte) ch;

      data[0] = newValue;
      m_dataProvider.setData(offset, data);

      // mark offset as modified
      setModified(offset);

      // register as undoable action
      fireUndoableEditListener(new DataEdit(offset, oldValue, newValue, getActiveView()));

      // Select one byte
      changeBy(false, 2L);
    }

    private void keyPressedInHexView(char ch)
    {
      final int value = Character.digit(ch, 16);

      if (value == -1) {
        return;
      }
      final long offset = m_caret.getPosition() / 2;

      final byte[] data = m_dataProvider.getData(offset, 1);
      if (data == null || data.length == 0) {
        return;
      }

      final byte oldValue = data[0];
      final byte newValue;
      // If position is odd, edit lo nibble, otherwise - hi nibble
      if (m_caret.getPosition() % 2 == 0) {
        newValue = (byte) (oldValue & 0x0F | value << 4);
      } else {
        newValue = (byte) (oldValue & 0xF0 | value);
      }

      data[0] = newValue;
      m_dataProvider.setData(offset, data);

      // mark offset as modified
      setModified(offset);

      // register as undoable action
      fireUndoableEditListener(new DataEdit(offset, oldValue, newValue, getActiveView()));

      // Select one nibble
      changeBy(false, 1L);
    }

    private void showPopupMenu(final MouseEvent event)
    {
      if (m_menuCreator != null) {
        final JPopupMenu menu = m_menuCreator.createMenu(m_caret.getPosition() / 2);

        if (menu != null) {
          menu.show(JHexView.this, event.getX(), event.getY());
        }
      }
    }

    @Override
    public void adjustmentValueChanged(final AdjustmentEvent event)
    {
      if (event.getSource() == m_scrollbar) {
        m_firstRow = event.getValue();
      }
      else {
        m_firstColumn = event.getValue();
      }

      repaint();
    }

    @Override
    public void caretStatusChanged(final Caret source)
    {
      repaint();
    }

    @Override
    public void componentHidden(final ComponentEvent event)
    {
    }

    @Override
    public void componentMoved(final ComponentEvent event)
    {
    }

    @Override
    public void componentResized(final ComponentEvent event)
    {
      setScrollBarMaximum();
    }

    @Override
    public void componentShown(final ComponentEvent event)
    {
    }

    @Override
    public void dataChanged(DataChangedEvent event)
    {
      setScrollBarMaximum();

      repaint();
    }

    @Override
    public void focusGained(final FocusEvent event)
    {
      m_caret.setVisible(true);
      repaint();
    }

    @Override
    public void focusLost(final FocusEvent event)
    {
      repaint();
    }

    @Override
    public void keyPressed(final KeyEvent event)
    {
      if (!isEditable()) {
        return;
      }

      final char ch = event.getKeyChar();
      if (m_activeView == Views.HEX_VIEW) {
        if (m_dataProvider.isEditable() && isHexCharacter(ch)) {
          keyPressedInHexView(ch);
        }
      } else {
        if (m_dataProvider.isEditable() && isPrintableCharacter(ch)) {
          keyPressedInAsciiView(ch);
        }
      }

      repaint();
    }

    @Override
    public void keyReleased(final KeyEvent event)
    {
    }

    @Override
    public void keyTyped(final KeyEvent event)
    {
    }

    @Override
    public void mouseClicked(final MouseEvent event)
    {
    }

    @Override
    public void mouseDragged(final MouseEvent event)
    {
      if (!isEnabled() || startNibble < 0) {
        return;
      }
      final int x = event.getX();
      final int y = event.getY();

      final int nibblesPerRow = 2 * m_bytesPerRow;
      if (y < m_paddingTop - (m_rowHeight - m_charHeight)) {
        scrollToPosition(2 * getFirstVisibleByte() - nibblesPerRow);

        final long newPos = m_caret.getPosition() - nibblesPerRow;
        if (newPos >= startNibble) {
          selectionModel.setSelectionInterval(startNibble, newPos);
          m_caret.setPosition(newPos);
        }
      } else
      if (y >= m_rowHeight * getNumberOfVisibleRows()) {
        scrollToPosition(2 * getFirstVisibleByte() + nibblesPerRow);

        final long newPos = m_caret.getPosition() + nibblesPerRow;
        if (startNibble + newPos <= 2 * m_dataProvider.getDataLength()) {
          selectionModel.setSelectionInterval(startNibble, newPos);
          m_caret.setPosition(newPos);
        }
      } else {
        final long newPos = getNibbleAtCoordinate(x, y);
        if (newPos != -1) {
          selectionModel.setSelectionInterval(startNibble, newPos);
          m_caret.setPosition(newPos);
        }
      }
    }

    @Override
    public void mouseEntered(final MouseEvent event)
    {
    }

    @Override
    public void mouseExited(final MouseEvent event)
    {
    }

    @Override
    public void mouseMoved(final MouseEvent event)
    {
      m_lastMouseX = event.getX();
      m_lastMouseY = event.getY();

      repaint();
    }

    @Override
    public void mousePressed(final MouseEvent event)
    {
      if (!isEnabled()) {
        return;
      }

      if (event.getButton() == MouseEvent.BUTTON1/* || event.getButton() == MouseEvent.BUTTON3*/) {
        requestFocusInWindow();

        final int x = event.getX();
        final int y = event.getY();

        Views oldView = m_activeView;
        if (isInsideHexView(x, y)) {
          m_activeView = Views.HEX_VIEW;
        } else
        if (isInsideAsciiView(x, y)) {
          m_activeView = Views.ASCII_VIEW;
        }

        if (oldView != m_activeView) {
          fireHexListener(m_activeView);
        }

        m_caret.setVisible(true);

        startNibble = getNibbleAtCoordinate(x, y);
        if (startNibble != -1) {
          // double click selects a whole word
          if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2
           && m_activeView == Views.ASCII_VIEW
          ) {
            expandSelection(startNibble / 2);// get byte position
          } else {
            setCurrentPosition(startNibble);
          }
        } else {
          // m_selectionLength = 0 must be notified in case the click position
          // is invalid.
          selectionModel.clearSelection();
        }
        repaint();
      }

      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event)
    {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
      if (event.getButton() == MouseEvent.BUTTON1) {
        startNibble = -1;
      }
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e)
    {
      // Mouse wheel support for scrolling

      if (!isEnabled()) {
        return;
      }

      final int notches = e.getWheelRotation();
      m_scrollbar.setValue(m_scrollbar.getValue() + 3*notches); // scrolling 3 lines per notch
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent e)
    {
      m_undo.addEdit(e.getEdit());
    }
  }

  /**
   * Enumeration that is used to switch the output format of the offsets from
   * 8 bit mode up to 64 bit mode.
   *
   */
  public enum AddressMode {
    BIT8, BIT16, BIT24, BIT32, BIT40, BIT48, BIT56, BIT64
  }

  /**
   * Enumeration that is used to decided whether real data or ??? is shown.
   *
   */
  public enum DefinitionStatus {
    DEFINED, UNDEFINED
  }

  /**
   * Enumeration that is used to decide which view of the component has the
   * focus.
   *
   */
  public enum Views {
    HEX_VIEW, ASCII_VIEW
  }

  /**
   * Shortcuts registered for this component by default.
   *
   */
  public enum Shortcut {
    /** Shortcut associated with the "Select all" action. */
    CTRL_A,
    /** Shortcut associated with the "Copy selected data" action. */
    CTRL_C,
    /** Shortcut associated with the "Paste data" action. */
    CTRL_V,
    /** Shortcut associated with the "Redo" action. */
    CTRL_Y,
    /** Shortcut associated with the "Undo" action. */
    CTRL_Z
  }
  //</editor-fold>
}
