svnant
=====
You can find latest version of svnant on http://subclipse.tigris.org

svnant uses svnup Subversion library (http://svnup.tigris.org) to provide access to the Subversion API.

However, the version of svnup used is not the same than the one on the svnup web site. I use a modified version of svnup-0.5b for now.


documentation of svn task is in /doc.


You can get sources using the included ant file. Just type "ant", the default target get the sources corresponding to the version you have.
If you want to get the latest sources, type "ant checkoutLatest"


There is a test you can run in build.xml. For that modify test/build.properties and modify urlRepos. The run ant runTests in main directory


The binary jar is in build/lib

cchabanois@ifrance.com