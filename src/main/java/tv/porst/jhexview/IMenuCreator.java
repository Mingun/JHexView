// See LICENSE.md for license information

package tv.porst.jhexview;

import javax.swing.JPopupMenu;

/**
 * This interface must be implemented by all classes that want to provide
 * context menus for the {@link JHexView} control.
 */
public interface IMenuCreator
{
  /**
   * This function is called to generate a popup menu after the user
   * right-clicked somewhere in the hex control.
   *
   * @param offset The current caret position at the time of the right-click.
   *        Will be in range {@code [0; JHexView.getData().getDataLength())}
   *
   * @return The popup menu suitable for that offset or {@code null} if no popup
   *         menu should be shown.
   */
  JPopupMenu createMenu(long offset);
}
