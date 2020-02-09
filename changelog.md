v2.0
====
- Remove unused parts of embedded splib library
- `setActiveView` now throw NPE if argument is `null`
- Now component doesn't redrawed when specified setters is called if value of property actually doesn't changed:
  - `setAddressMode`
  - `setBaseAddress`
  - `setBytesPerColumn`
  - `setBytesPerRow`
  - `setColumnSpacing`
  - `setDefinitionStatus`
  - `setHexViewWidth`
- Redraw view when call setters:
  - `setAddressMode`
- `getMouseOverHighlighted` renamed to `isMouseOverHighlighted`
- `doFlipBytes` renamed to `isFlipBytes`
- `getFontSize` removed. Use `getFont().getSize()`
- `setFontSize` removed. Use `setFont(getFont().deriveFont((float)getFont().getSize()))`
- `setFontStyle` removed. Use `setFont(getFont().deriveFont(...))`
- `findAscii/findHex` now throws NPE if search pattern is `null` instead of returning
  start offset for search

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
