package org.tigris.subversion.svnant;

import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;

public class SvnSvnKitTest extends SvnTest {

    static {
        try {
            SvnKitClientAdapterFactory.setup();
        } catch (SVNClientException e) {
            // if an exception is thrown, SVNKit is not available or 
            // already registered ...
            throw new RuntimeException("Cannot load SVNKit binding :", e);
        }
    }

    public SvnSvnKitTest(String name) {
        super(name);
    }

    protected boolean isJavaHLTest() {
        return false;
    }
  
    protected boolean isSVNKitTest() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void setUp() {
        super.setUp();
        svnClient = SVNClientAdapterFactory.createSVNClient(SvnKitClientAdapterFactory.SVNKIT_CLIENT);
    }

    /**
     * {@inheritDoc}
     */
    protected void executeTarget(String targetName) {
        project.setProperty("javahl", "false");
        project.setProperty("svnkit", "true");
        assertPropertyEquals("javahl", "false");
        assertPropertyEquals("svnkit", "true");
        super.executeTarget(targetName);
    }

    /**
     * {@inheritDoc}
     */
    public void testListCommand() {
        super.testListCommand();
    }

    /**
     * {@inheritDoc}
     */
    public void testSingleInfo() throws Exception {
        super.testSingleInfo();
    }
    
    /**
     * {@inheritDoc}
     */
    public void testWcVersionUnmanaged() throws Exception {
        super.testWcVersionUnmanaged();
    }
    
    /**
     * {@inheritDoc}
     */
    public void testInfo() throws Exception {
        super.testInfo();
    }
    
    /**
     * {@inheritDoc}
     */
    public void testImport() {
        super.testImport();
    }

    /**
     * {@inheritDoc}
     */
    public void testImportNewEntry() {
        super.testImportNewEntry();
    }
    

}
