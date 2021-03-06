// See LICENSE.md for license information

package tv.porst.jhexview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data provider that provides data to the hex view component from a static
 * array. Use this data provider if you already have all the data in memory and
 * do not have to reload memory from an external source.
 */
public final class SimpleDataProvider implements IDataProvider
{
  private final List<IDataChangedListener> listeners = new ArrayList<>();
  private final byte[] m_data;

  public SimpleDataProvider(byte[] data)
  {
    this.m_data = data;
  }

  @Override
  public void addListener(final IDataChangedListener listener)
  {
    if (listener != null && !listeners.contains(listener)) {
        listeners.add(listener);
    }
  }

  @Override
  public byte[] getData(long offset, int length)
  {
    if (offset + length > getDataLength()) {
      length = getDataLength() - (int)offset;
    }
    if (length > 0) {
      return Arrays.copyOfRange(this.m_data, (int) offset, (int) (offset + length));
    } else {
      return new byte[0];
    }
  }

  @Override
  public int getDataLength()
  {
    return this.m_data.length;
  }

  /**
   * @return Always 0
   *
   * @deprecated That method are never used and will be removed in 3.0
   */
  @Deprecated
  public long getOffset()
  {
    return 0L;
  }

  @Override
  public boolean hasData(long offset, int length)
  {
    return true;
  }

  @Override
  public boolean isEditable()
  {
    return true;
  }

  @Override
  public boolean keepTrying()
  {
    return false;
  }

  @Override
  public void removeListener(IDataChangedListener listener)
  {
    if (listener != null) {
      listeners.remove(listener);
    }
  }

  @Override
  public void setData(long offset, byte[] data)
  {
    int length = data.length;
    final int len = getDataLength() - (int)offset;
    if (length > len) {
      length = len;
    }
    if (length > 0) {
      System.arraycopy(data, 0, this.m_data, (int) offset, length);
      fireDataChangedListener();
    }
  }

  protected void fireDataChangedListener()
  {
    if (!listeners.isEmpty()) {
      DataChangedEvent event = new DataChangedEvent(this);
      for (final IDataChangedListener l: listeners) {
        l.dataChanged(event);
      }
    }
  }
}
