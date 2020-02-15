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
  /**
   * Current selection length in nibbles. This value can be negative if nibbles
   * before the current position are selected.
   */
  long length;

  /**
   * Returns {@code true} if no data are selected.
   *
   * @return {@code true} if no data are selected.
   */
  public boolean isEmpty() { return length == 0; }
}
