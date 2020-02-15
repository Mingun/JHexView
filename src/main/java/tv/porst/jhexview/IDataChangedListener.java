// See LICENSE.md for license information

package tv.porst.jhexview;

import java.util.EventListener;

public interface IDataChangedListener extends EventListener
{
  void dataChanged(DataChangedEvent event);
}
