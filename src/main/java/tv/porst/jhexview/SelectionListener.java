// See LICENSE.md for license information

package tv.porst.jhexview;

import java.util.EventListener;

/**
 * The listener that's notified when a selected range of bytes changes.
 *
 * @author Mingun
 * @since 2.0
 * @see SelectionModel
 */
public interface SelectionListener extends EventListener {
  /**
   * Called whenever the value of the selection changes.
   *
   * @param event the event that characterizes the change.
   */
  void selectionChanged(SelectionEvent event);
}
