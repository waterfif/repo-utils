repo-utils
==========

repo-utils is a utility for comparing two storage repositories containing *.pom and ivy.xml files and writing the 
differences to an output directory.

The class com.waterfieldtech.RepositoryReport has 2 modes of operation: 
 
 * Storage Report Mode
 * Comparison Mode

Storage Report Mode
-------------------

Storage report mode is invoked using the flag **-baseStorageDir** *path* and 
reads all pom and ivy.xml files in a storage directory. 

Comparison Mode
---------------

Comparison mode is invoke using the flags: 
 
 * **-baseStorageDir** *path* : representing a storage directory
 * **-comparisonStorageDir** *path* : representing a second storage directory
 * **-outputDir** *path* : representing an output directory

Comparison mode checks the contents of the comparison storage directory against the contents of the
base storage directory and copies all items that exist in the comparison directory but not the base directory
to an output directory. In addition a content.txt file is added to the output directory indicating the items 
written to the output directory.
