/*
 * Modified Oct. 2014 by argent77
 * - added new methods toByte() and toChar() with various parameter lists
 */
package tv.porst.splib.convert;

import java.awt.event.KeyEvent;

/**
 * Converts helper functions for working with different data types.
 */
public final class ConvertHelpers
{
  /**
   * Tests whether a character is a valid character of a hexadecimal string.
   *
   * @param c
   *          The character to test.
   *
   * @return True, if the character is a hex character. False, otherwise.
   */
  public static boolean isHexCharacter(final char c)
  {
    return c >= '0' && c <= '9'
        || c >= 'a' && c <= 'f'
        || c >= 'A' && c <= 'F';
  }

  /**
   * Tests whether a character is a printable ASCII character.
   *
   * @param c
   *          The character to test.
   *
   * @return True, if the character is a printable ASCII character. False,
   *         otherwise.
   */
  public static boolean isPrintableCharacter(final char c)
  {
    final Character.UnicodeBlock block = Character.UnicodeBlock.of(c);

    return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED && block != null
        && block != Character.UnicodeBlock.SPECIALS;
  }

  /**
   * Converts the byte value into a character representation,
   * using the locale as defined by the OS.
   * @param b The byte value to convert.
   * @return The character representation of the byte value.
   */
  public static char toChar(byte b)
  {
    return b >= 0 ? (char)b : '\uFFFD';
  }
}
