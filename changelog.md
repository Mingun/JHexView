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
