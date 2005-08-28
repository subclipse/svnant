package org.tigris.subversion.svnant;

import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.javahl.JavaSvnClientAdapterFactory;

public class SvnJavaSVNTest extends SvnTest {

    public SvnJavaSVNTest(String name) {
        super(name);
    }

    static {
        try {
            JavaSvnClientAdapterFactory.setup();
        } catch (SVNClientException e) {
            // if an exception is thrown, javaSVN is not available or 
            // already registered ...
        	throw new RuntimeException("Cannot load JavaSVN binding :", e);
        }
    }

	protected boolean isJavaHLTest()
	{
		return false;
	}
	protected boolean isJavaSVNTest()
	{
		return true;
	}

    public void setUp() {
    	super.setUp();

    	svnClient = SVNClientAdapterFactory.createSVNClient(JavaSvnClientAdapterFactory.JAVASVN_CLIENT);
    }

    /* (non-Javadoc)
     * @see org.apache.tools.ant.BuildFileTest#executeTarget(java.lang.String)
     */
    protected void executeTarget(String targetName) {
    	project.setProperty("javahl", "false");
    	project.setProperty("javasvn", "true");
        assertPropertyEquals("javahl", "false");
        assertPropertyEquals("javasvn", "true");
    	super.executeTarget(targetName);
    }

}
