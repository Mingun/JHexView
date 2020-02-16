// See LICENSE.md for license information

package tv.porst.jhexview;

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
 */
public class SelectionModel {

  /**
   * Currently selected position. Note that this field is twice as large as the
   * length of data because nibbles can be selected.
   */
  long start;
  /** End offset of selection (inclusive). */
  long end;

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
  boolean setSelection(long start, long end) {
    final boolean hasChanges = this.start != start
                            || this.end   != end;
    this.start = start;
    this.end   = end;
    return hasChanges;
  }
}
