/*
 * Created on 10 mars 2003
 *
 */
package org.tigris.subversion.svnant;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.tools.ant.BuildFileTest;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Edit test/build.properties and change urlRepos before running these tests
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class SvnTest extends BuildFileTest {
private ISVNClientAdapter svnClient;

    public SvnTest(String name) {
        super(name);
    }

    public void setUp() {
        configureProject("test/build.xml");
		svnClient = SVNClientAdapterFactory.createSVNClient(SVNClientAdapterFactory.JAVAHL_CLIENT);
    }

	public void tearDown()
	{
		System.out.print(getLog());
	}

    public void testCheckout() throws SVNClientException {
        executeTarget("testCheckout");

		assertEquals(1,svnClient.getSingleStatus(new File("test/coHEAD/checkoutTest/readme.txt")).getLastChangedRevision().getNumber());
    }
    

	public void testList() throws SVNClientException,MalformedURLException {
       executeTarget("testList");
	   String urlRepos = getProject().getProperty("urlRepos");
	   ISVNDirEntry[] list = svnClient.getList(new SVNUrl(urlRepos+"/listTest"),SVNRevision.HEAD,false);
	   assertTrue(list.length > 0);
	}


    public void testLog() throws SVNClientException {
        executeTarget("testLog");
		String urlRepos = getProject().getProperty("urlRepos");
		ISVNLogMessage[] messages = svnClient.getLogMessages(new File("test/my_repos/logTest/file1.txt"),new SVNRevision.Number(0),SVNRevision.HEAD);
        assertTrue(messages.length > 0);
		assertEquals("logTest directory added to repository",messages[0].getMessage());
    }

    public void testAddCommit() throws SVNClientException {
       executeTarget("testAddCommit");
	   assertTrue(svnClient.getSingleStatus(new File("test/my_repos/addCommitTest/file0.add")).getLastChangedRevision().getNumber() > 0);
    }

    
    public void testCopy() throws SVNClientException {
    	executeTarget("testCopy");
		assertTrue(svnClient.getSingleStatus(new File("test/my_repos/copyTest/copy1")).getLastChangedRevision().getNumber() > 0);
    } 

	public void testDelete() {
		executeTarget("testDelete");
		assertFalse(new File("test/my_repos/deleteTest/deleteFromWorkingCopy/file0.del").exists());
		assertTrue(new File("test/my_repos/deleteTest/deleteFromWorkingCopy/donotdel.txt").exists());
	}

	public void testExport() {
		executeTarget("testExport");
	}
	
	public void testImport() {
		executeTarget("testImport");
	} 

	
	public void testMkdir() throws SVNClientException {
		executeTarget("testMkdir");
		assertTrue(svnClient.getSingleStatus(new File("test/my_repos/mkdirTest/testMkdir2")).getLastChangedRevision().getNumber() > 0);
    } 
	
	public void testMove() throws SVNClientException {
		executeTarget("testMove");
		assertTrue(svnClient.getSingleStatus(new File("test/my_repos/moveTest/dir1Renamed")).getLastChangedRevision().getNumber() > 0);
	}
   
    public void testProp() throws SVNClientException {
        executeTarget("testProp");
        ISVNProperty propData = svnClient.propertyGet(new File("test/my_repos/propTest/file.png"),"svn:mime-type");
        assertTrue(propData != null);
        assertEquals("image/png",propData.getValue());
        propData = svnClient.propertyGet(new File("test/my_repos/propTest/file.png"),"myPicture");
        assertTrue(propData != null);
        assertEquals(170,propData.getData().length);
    }

    public void testDiff() {
        executeTarget("testDiff");
        File patchFile = new File("test/my_repos/diffTest/patch.txt");
        assertTrue(patchFile.exists());
        assertTrue(patchFile.length() > 0);
    }
    
    
    public void testKeywords() throws FileNotFoundException, IOException {
        executeTarget("testKeywords");
        DataInputStream dis = new DataInputStream(new FileInputStream("test/my_repos/keywordsTest/file.txt")); 
        assertEquals("$LastChangedRevision: 1 $",dis.readLine());
        assertEquals("$Author: cedric $",dis.readLine());
        assertEquals("$Id$",dis.readLine());
    }

    public static void main(String[] args) {
        String[] testCaseName = { SvnTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
        //		junit.ui.TestRunner.main(testCaseName);
    }

}
