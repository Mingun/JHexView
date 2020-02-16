// See LICENSE.md for license information

package tv.porst.jhexview;

/**
 * Interface to be implemented by classes that want to be notified about changed
 * in the caret.
 *
 * @author Sebastian Porst (sp@porst.tv)
 */
public interface ICaretListener
{
  /**
   * Invoked after the caret status changed.
   *
   * @param caret
   *          The caret whose status changed.
   */
  public void caretStatusChanged(Caret caret);
}
