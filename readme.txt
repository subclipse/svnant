svnant
=====
You can find latest version of svnant on http://subclipse.tigris.org

svnant uses javahl subversion java bindings  (http://svn.collab.net/repos/svn/trunk/subversion/bindings/java/javahl/) to provide access to the Subversion API.
svnant can also use command line interface to subversion. This is still experimental however.

documentation of svn task is in /doc.


You can get sources using the included ant file. Just type "ant", the default target get the sources corresponding to the version you have.
If you want to get the latest sources, type "ant checkoutLatest"


Once you get the sources, there is a test you can run in build.xml. For that modify test/build.properties and modify urlRepos. Then run ant runTests in main directory


svnant will run on Linux either using command line interface (experimental) or using javahl. Javahl is in subversion distribution.

cchabanois@ifrance.com