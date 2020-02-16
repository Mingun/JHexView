// See LICENSE.md for license information

package tv.porst.jhexview;

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
 * @author Mingun
 * @since 2.0
 */
public class SelectionModel {

  /**
   * Currently selected position. Note that this field is twice as large as the
   * length of data because nibbles can be selected.
   */
  long start;
  /** End offset of selection (inclusive). */
  long end;

  /** List containing listeners of all supported types. */
  private final EventListenerList listeners = new EventListenerList();

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

  /**
   * Add a listener to the model that's notified each time a change to the selection occurs.
   *
   * @param listener the listener to add. If {@code null}, NPE is thrown
   *
   * @throws NullPointerException If {@code listener} is {@code null}
   * @see #removeSelectionListener
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
