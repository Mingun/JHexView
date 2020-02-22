// See LICENSE.md for license information

package tv.porst.jhexview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import javax.swing.event.EventListenerList;

/**
 * This class represents the current state of the selection in {@link JHexView}
 * component. The selection is modeled as a set of intervals, each interval
 * represents a contiguous range of selected byte region.
 *
 * The methods for modifying the set of selected intervals all take a pair of
 * offsets, {@code offset0} and {@code offset1}, that represent a closed interval,
 * i.e. the interval includes both {@code offset0} and {@code offset1}.
 *
 * The model can be iterated to get all selected intervals. Intervals follows in
 * increasing order, do not overlap each other and never empty.
 *
 * Note, that model is not thread-safe.
 *
 * @author Mingun
 * @since 2.0
 */
public class SelectionModel implements Iterable<SelectionModel.Interval> {

  /** List of ranges of selected offsets. */
  protected final ArrayList<Interval> selected = new ArrayList<>();

  /** List containing listeners of all supported types. */
  private final EventListenerList listeners = new EventListenerList();

  //<editor-fold defaultstate="collapsed" desc="Internal classes">
  /**
   * Represents continious interval of selected nibbles. Such intervals never
   * intersects each other.
   */
  public static class Interval {
    /** The first selected nibble in that interval (inclusive). */
    private final long start;
    /** The last selected nibble in that interval (exclusive). */
    private final long end;

    Interval(long start, long end) {
      this.start = start;
      this.end   = end;
    }

    /**
     * Returns the offset of first selected nibble in that interval. That offset
     * always strictly less then {@link #getEnd()}.
     *
     * @return The last selected nibble in that interval (exclusive)
     */
    public long getStart() { return start; }
    /**
     * Returns the offset of last selected nibble in that interval. That offset
     * always strictly more than {@link #getStart()}.
     *
     * @return The last selected nibble in that interval (exclusive)
     */
    public long getEnd() { return end; }
    /**
     * Returns the count of selected nibblies in that interval. The returned value
     * always greater than 0.
     *
     * @return The count of nibblies in that interval
     */
    public long getLength() { return end - start; }

    /**
     * Returns {@code true} if the nibble at specified offset is in inside the interval.
     *
     * @param offset an offset of nibble
     *
     * @return {@code true} if the nibble at specified offset is inside that interval,
     *         {@code false} otherwise
     */
    public boolean contains(long offset) {
      return start <= offset && offset < end;
    }
    /**
     * Check if specified interval contains another, i.e.
     * {@code getStart() <= other.getStart() && other.getEnd() <= getEnd()}
     *
     * @param other an interval to check
     *
     * @return {@code true} if the specified interval contains the entire other
     *         interval and {@code false} otherwise
     */
    public boolean contains(Interval other) {
      return start <= other.start && other.end <= end;
    }

    boolean isEmpty() { return start == end; }

    @Override
    public String toString() {
      return "[" + start + "; " + end + ")";
    }

    @Override
    public int hashCode() {
      int hash = 3;
      hash = 19 * hash + (int) (start ^ (start >>> 32));
      hash = 19 * hash + (int) (end   ^ (end   >>> 32));
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final Interval other = (Interval)obj;
      if (start != other.start) {
        return false;
      }
      return end == other.end;
    }
    static Interval create(long offset0, long offset1) {
      return new Interval(
        Math.min(offset0, offset1),
        Math.max(offset0, offset1) + 1// +1 because end of interval exclusive
      );
    }
  }
  //</editor-fold>

  /**
   * Returns {@code true} if no data are selected.
   *
   * @return {@code true} if no data are selected.
   */
  public boolean isEmpty() { return selected.isEmpty(); }

  /**
   * Returns {@code true} if the nibble at specified offset is selected.
   *
   * @param offset an offset of nibble in range {@code [0; JHexView.getData().getDataLength())}
   *
   * @return {@code true} if the nibble at specified offset is selected,
   *         {@code false} otherwise
   */
  public boolean isSelected(long offset) {
    for (final Interval range : selected) {
      if (range.contains(offset)) return true;
    }
    return false;
  }

  /**
   * Search for an interval that contains specified offset.
   *
   * @param offset an offset of nibble in range {@code [0; JHexView.getData().getDataLength())}
   *
   * @return Interval which {@link Interval#contains(long)} contains} specified
   *         offset or {@code null}, if specified offset is not selected.
   */
  public Interval findInterval(long offset) {
    for (final Interval range : selected) {
      if (range.contains(offset)) return range;
    }
    return null;
  }

  @Override
  public Iterator<Interval> iterator() {
    return Collections.unmodifiableList(selected).iterator();
  }

  /**
   * Changes the selection to be between {@code offset0} and {@code offset1}
   * inclusive. {@code offset0} doesn't have to be less than or equal to
   * {@code offset1}.
   * <p>
   * If this represents a change to the current selection, then each
   * {@link SelectionListener} is notified of the change.
   *
   * @param offset0 one end of the interval in range {@code [0; JHexView.getData().getDataLength())}
   * @param offset1 other end of the interval in range {@code [0; JHexView.getData().getDataLength())}
   *
   * @see #addSelectionListener
   */
  public void setSelectionInterval(long offset0, long offset1) {
    final Interval range = Interval.create(offset0, offset1);
    if (range.isEmpty()) {
      clearSelection();
      return;
    }
    // Replace selection and fire event if selection is changed
    if (selected.size() != 1 || !range.equals(selected.get(0))) {
      selected.clear();
      selected.add(range);
      fireSelectionEvent();
    }
  }
  /**
   * Changes the selection to be the set union of the current selection
   * and the offsets between {@code offset0} and {@code offset1} inclusive.
   * {@code offset0} doesn't have to be less than or equal to {@code offset1}.
   * <p>
   * If this represents a change to the current selection, then each
   * {@link SelectionListener} is notified of the change.
   *
   * @param offset0 one end of the interval
   * @param offset1 other end of the interval
   *
   * @see #addSelectionListener
   * @see #setSelectionInterval
   * @see #removeSelectionInterval
   * @see #clearSelection
   */
  public void addSelectionInterval(long offset0, long offset1) {
    addSelectionInterval(Interval.create(offset0, offset1));
  }
  void addSelectionInterval(Interval key) {
    if (key.isEmpty()) return;

    if (selected.isEmpty()) {
      selected.add(key);
      fireSelectionEvent();
      return;
    }
    final ArrayList<Interval> old = new ArrayList<>(selected);

    selected.clear();
    final Iterator<Interval> it = old.iterator();
    long start = key.start;
    long end   = key.end;
    boolean hasChanges = false;
    Interval firstAfterNew = null;
    while (it.hasNext()) {
      final Interval range = it.next();
      // Situations:
      //           012345 - after any regions
      //    range: **     [0,2)
      //      key:    *** [3,6)
      //   key: ** ***
      // range.end -^ ^-- key.start
      // range.start < key.start by definition of Interval
      if (range.end < key.start) {
        selected.add(range);
        continue;
      }
      // Situations:
      //         01234567 - inside free rigion
      //  range: **     * [0,2) [7,8)
      //    key:    ***   [3,6)
      // to add:    *** *
      //         ^^-- already added by previous cycle
      //         012345 - before any regions
      //  range:     ** [4,6)
      //    key: ***    [0,3)
      // to add: *** **
      if (range.start > key.end) {
        firstAfterNew = range;
        break;
      }
      // Situations:
      //         012345 - touch left
      //  range: ***    [0,3)
      //    key:    *** [3,6)
      // to add: ******
      //         012345 - intersection
      //  range: ****   [0,4)
      //    key:    *** [3,6)
      // to add: ******
      //         012345 - inclusion
      //  range: ****** [0,6)
      //    key:    *** [3,6)
      // to add: ******
      //         01234567 - inclusion
      //  range: ******** [0,8)
      //    key:    ***   [3,6)
      // to add: ********
      //         01234567 - touch both
      //  range: ***   ** [0,3) [6,8)
      //    key:    ***   [3,6)
      // to add: ********
      //         012345 - inclusion
      //  range: ****** [0,6)
      //    key: ***    [0,3)
      // to add: ******
      //         012345 - intersection
      //  range:   **** [2,6)
      //    key: ***    [0,3)
      // to add: ******
      //         012345 - touch right
      //  range:    *** [3,6)
      //    key: ***    [0,3)
      // to add: ******
      if (range.start < start) {
        start = range.start;
        hasChanges = true;
      }
      if (range.end > end) {
        end = range.end;
        hasChanges = true;
      }
    }
    selected.add(new Interval(start, end));
    if (firstAfterNew != null) {
      selected.add(firstAfterNew);
    }
    while (it.hasNext()) {
      selected.add(it.next());
    }
    if (hasChanges) {
      fireSelectionEvent();
    }
  }
  /**
   * Changes the selection to be the set difference of the current selection
   * and the offsets between {@code offset0} and {@code offset1} inclusive.
   * {@code offset0} doesn't have to be less than or equal to {@code offset1}.
   * <p>
   * If this represents a change to the current selection, then each
   * {@link SelectionListener} is notified of the change.
   *
   * @param offset0 one end of the interval.
   * @param offset1 other end of the interval
   *
   * @see #addSelectionListener
   * @see #addSelectionInterval
   * @see #setSelectionInterval
   * @see #clearSelection
   */
  public void removeSelectionInterval(long offset0, long offset1) {
    removeSelectionInterval(Interval.create(offset0, offset1));
  }
  void removeSelectionInterval(Interval key) {
    if (key.isEmpty() || selected.isEmpty()) return;

    final ListIterator<Interval> it = selected.listIterator();
    while (it.hasNext()) {
      final Interval range = it.next();
      // Situations:
      //         012345 - removed interval after current
      //  range: **     [0,2)
      //    key:    *** [3,6)
      // result: **
      //         012345 - touch
      //  range: ***    [0,3)
      //    key:    *** [3,6)
      // result: ***
      if (range.end <= key.start) {
        continue;
      }
      //         012345
      //  range:    *** [3,6)
      //    key: ***    [0,3)
      // result:    ***
      //         012345
      //  range:     ** [4,6)
      //    key: ***    [0,3)
      // result:     **
      if (key.end <= range.start) {
        break;
      }
      //         012345
      //  range: ****** [0,6)
      //    key:    *** [3,6)
      // result: ***
      //         012345
      //  range: ****** [0,6)
      //    key:   ***  [2,5)
      // result: **   *
      //         012345
      //  range: ****** [0,6)
      //    key: ***    [0,3)
      // result:    ***
      if (range.contains(key)) {
        it.remove();
        if (range.start != key.start) {
          it.add(new Interval(range.start, key.start));
        }
        if (range.end != key.end) {
          it.add(new Interval(key.end, range.end));
        }
        fireSelectionEvent();
        break;
      }
      it.remove();
      //         012345
      //  range: ****   [0,4)
      //    key:    *** [3,6)
      // result: ***    [0,3)
      //         012345
      //  range: ****** [0,6)
      //    key:    *** [3,6)
      // result: ***    [0,3)
      //         012345
      //  range: ****** [0,6)
      //    key:   ***  [2,5)
      // result: **   * [0,2) [5,6)
      if (range.start < key.start) {
        it.add(new Interval(range.start, key.start));
      }
      //         012345
      //  range: ****** [0,6)
      //    key:   ***  [2,5)
      // result: **   * [0,2) [5,6)
      //         012345
      //  range: ****** [0,6)
      //    key: ***    [0,3)
      // result:    *** [3,6)
      //         012345
      //  range:   **** [2,6)
      //    key: ***    [0,3)
      // result:    *** [3,6)
      if (key.end < range.end) {
        it.add(new Interval(key.end, range.end));
      }
      fireSelectionEvent();
      break;
    }
  }
  /**
   * Change the selection to the empty set. If this represents a change to the
   * current selection then notify each {@link SelectionListener}.
   *
   * @see #addSelectionListener
   * @see #addSelectionInterval
   * @see #removeSelectionInterval
   * @see #setSelectionInterval
   */
  public void clearSelection() {
    // Clear selection and fire event, if there was selection before
    if (!selected.isEmpty()) {
      selected.clear();
      fireSelectionEvent();
    }
  }
  //</editor-fold>

  /**
   * Add a listener to the model that's notified each time a change to the selection occurs.
   *
   * @param listener the listener to add. If {@code null}, NPE is thrown
   *
   * @throws NullPointerException If {@code listener} is {@code null}
   * @see #removeSelectionListener
   * @see #setSelectionInterval
   * @see #addSelectionInterval
   * @see #removeSelectionInterval
   * @see #clearSelection
   */
  public void addSelectionListener(SelectionListener listener) {
    if (listener == null) {
      throw new NullPointerException("Selection change listener of JHexView can't be null");
    }
    listeners.add(SelectionListener.class, listener);
  }
  /**
   * Remove a listener from the model that's notified each time a change to the
   * selection occurs.
   *
   * @param listener the listener to remove. If {@code null}, method do nothing
   *
   * @see #addSelectionListener
   */
  public void removeSelectionListener(SelectionListener listener) {
    listeners.remove(SelectionListener.class, listener);
  }
  /**
   * Notifies all registered HexListeners that the selection has changed.
   */
  private void fireSelectionEvent() {
    SelectionEvent event = null;
    for (final SelectionListener l : listeners.getListeners(SelectionListener.class)) {
      if (event == null) {
        event = new SelectionEvent(this);
      }
      l.selectionChanged(event);
    }
  }
}
