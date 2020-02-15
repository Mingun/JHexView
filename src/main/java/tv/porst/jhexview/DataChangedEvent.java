// See LICENSE.md for license information

package tv.porst.jhexview;

import java.util.EventObject;

/**
 * A basic event implementation that indicates that the content of a IDataProvider object
 * has been modified.
 *
 * @author argent77
 */
public class DataChangedEvent extends EventObject
{
  public DataChangedEvent(Object source)
  {
    super(source);
  }

}
