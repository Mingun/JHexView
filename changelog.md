v2.0
====
Major Changes
-------------
Project license changed to LGPL 2.1 or later

Breaking Changes
----------------
- Remove unused parts of embedded splib library and move used parts into `tv.porst.jhexview` package
- `setActiveView` now throw NPE if argument is `null`
- `getMouseOverHighlighted` renamed to `isMouseOverHighlighted`
- `doFlipBytes` renamed to `isFlipBytes`
- `findAscii/findHex` now throws NPE if search pattern is `null` instead of returning
  start offset for search

### Removed
- `getFontSize`. Use `getFont().getSize()` instead
- `setFontSize`. Use `setFont(getFont().deriveFont((float)getFont().getSize()))` instead
- `setFontStyle`. Use `setFont(getFont().deriveFont(...))` instead
- `setSelectionLength`

Other Changes
-------------
- Now component doesn't redrawn when specified setters is called if value of property actually doesn't changed:
  - `setAddressMode`
  - `setBaseAddress`
  - `setBytesPerColumn`
  - `setBytesPerRow`
  - `setColumnSpacing`
  - `setDefinitionStatus`
  - `setHexViewWidth`
- Redraw view when call setters:
  - `setAddressMode`

v1.1
====
- Split enabled and editable states: add `isEditable`/`setEditable` methods
- Now component is created in enabled, but not editable state
- Fix not correctly saved Undo data if edit data at offsets more, than 0xFFFFFFFF

v1.0
====
First maven release

Changes for NearInfinity by Argent77
------------------------------------
- Included dependencies (splib)
- Added Apache Ant build script (replaced to maven by Mingun)
- Find bytes/text support
- Copy/paste support
- Undo/redo support
