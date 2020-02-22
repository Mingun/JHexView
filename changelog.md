v2.1
====
Bug fixes
---------
- Fix incorrect work with `getBaseAddress()` method. Bug influence offsets in
  methods:
  - `isModified(long)`
  - `getModifiedCount`
  - `getModifiedOffsets`
  - `IColormap` methods
  - `IMenuCreator.createMenu`

Minor Changes
-------------
- Make class `SelectionModel.Interval` static and override `equals` and `hashCode`
- Add overloads for `findHex/findAscii` that accept and return `long`.
  Old methods, that use `int`, was deprecated and will be removed in 3.0

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
- `JCaret.draw` now requires `Graphics2D` as first parameter explicitly instead of implicitly
  (via cast in implementation)
- Selection API reworked. See new `SelectionModel` class and `getSelectionModel` method

### Removed
- `getFontSize`. Use `getFont().getSize()` instead
- `setFontSize`. Use `setFont(getFont().deriveFont((float)getFont().getSize()))` instead
- `setFontStyle`. Use `setFont(getFont().deriveFont(...))` instead
- `getSelectionLength`. Use new `SelectionModel` API instead
- `setSelectionLength`. Use new `SelectionModel` API instead
- `getLastOffset`
- `getFirstSelectedOffset`. Use new `SelectionModel` API instead
- `getLastSelectedOffset`. Use new `SelectionModel` API instead

Bug fixes
---------
- **Select all** action now correctly set selection length if `getBaseAddress() != 0`

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
- Always drawn caret outline (dotted rectangle) if component has focus,
  not only in editable state for panel without input focus

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
