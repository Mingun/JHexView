// See LICENSE.md for license information

package tv.porst.jhexview;

import java.util.EventObject;

/**
 * An event that characterizes a change in selection. The change is limited to
 * a single inclusive interval. The selection of at least one offset within the
 * range will have changed. A decent {@link SelectionModel} implementation will
 * keep the range as small as possible. {@link SelectionListener}s will
 * generally query the source of the event for the new selected status of each
 * potentially changed offset.
 *
 * @author Mingun
 * @since 2.0
 */
public class SelectionEvent extends EventObject {

  SelectionEvent(Object source) {
    super(source);
  }
}
