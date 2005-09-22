svnant
======
You can find latest version of svnant on http://subclipse.tigris.org

To provide access to the Subversion API, svnant uses either the javahl
Subversion Java bindings
<http://svn.collab.net/repos/svn/trunk/subversion/bindings/java/javahl/>
(which are JNI-based, and must setup appropriately), or Subverion's
command line programs (which must be installed and in your PATH).

Documentation of the <svn> task is in the /doc directory.

To access the sources, just type "ant" in the directory containing the
build.xml bundled with the distribution.  The default target will
retrieve the sources corresponding to the version you have.  If you
want to get the latest sources, type "ant checkoutLatest".

Once you have the sources, svnant unit tests can be invoked using the
top level build.xml by typing "ant runTests".  These tests provide a
great set of examples of how to use svnant's Ant tasks and data types.

Please send any usage questions to <mailto:users@subclipse.tigris.org>.
