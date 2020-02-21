// See LICENSE.md for license information

package tv.porst.jhexview;

import java.util.Collections;
import java.util.Iterator;
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

  /**
   * Currently selected position. Note that this field is twice as large as the
   * length of data because nibbles can be selected.
   */
  @Deprecated long start;
  /** End offset of selection (inclusive). */
  @Deprecated long end;

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

    Interval(long offset0, long offset1) {
      this.start = Math.min(offset0, offset1);
      this.end   = Math.max(offset0, offset1) + 1;// +1 because end of interval exclusive
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
  }
  //</editor-fold>

  /**
   * Returns {@code true} if no data are selected.
   *
   * @return {@code true} if no data are selected.
   */
  public boolean isEmpty() { return start == end; }

  /**
   * Returns {@code true} if the nibble at specified offset is selected.
   *
   * @param offset an offset of nibble
   *
   * @return {@code true} if the nibble at specified offset is selected,
   *         {@code false} otherwise
   */
  public boolean isSelected(long offset) {
    if (end > start) {
      return offset >= start && offset < end;
    } else
    if (end < start) {
      return offset >= end && offset < start;
    }
    return false;
  }

  @Deprecated
  void setSelection(long start, long end) {
    final boolean hasChanges = this.start != start
                            || this.end   != end;
    this.start = start;
    this.end   = end;
    if (hasChanges) {
      fireSelectionEvent();
    }
  }

  @Override
  public Iterator<Interval> iterator() {
    if (start == end) return Collections.emptyIterator();
    return Collections.singleton(new Interval(start, end)).iterator();
  }

  /**
   * Changes the selection to be between {@code offset0} and {@code offset1}
   * inclusive. {@code offset0} doesn't have to be less than or equal to
   * {@code offset1}.
   * <p>
   * If this represents a change to the current selection, then each
   * {@link SelectionListener} is notified of the change.
   *
   * @param offset0 one end of the interval
   * @param offset1 other end of the interval
   *
   * @see #addSelectionListener
   */
  public void setSelectionInterval(long offset0, long offset1) {
    setSelection(
      Math.min(offset0, offset1),
      Math.max(offset0, offset1)
    );
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
