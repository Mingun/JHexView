// See LICENSE.md for license information

package tv.porst.jhexview;

import java.util.EventListener;

public interface IHexViewListener extends EventListener
{
  void stateChanged(HexViewEvent event);
}
