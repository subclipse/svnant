/*
 * Created on 10 mars 2003
 *
 */
package org.tigris.subversion.svnant;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.tools.ant.BuildFileTest;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.DirEntry;
import org.tigris.subversion.javahl.LogMessage;
import org.tigris.subversion.javahl.PropertyData;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.svnclientadapter.SVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Edit test/build.properties and change urlRepos before running these tests
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class SvnTest extends BuildFileTest {
private SVNClientAdapter svnClient;

    public SvnTest(String name) {
        super(name);
    }

    public void setUp() {
        configureProject("test/build.xml");
		svnClient = new SVNClientAdapter();
    }

	public void tearDown()
	{
		System.out.print(getLog());
	}


    public void testCheckout() {
        executeTarget("testCheckout");
        try {
			assertEquals(1,svnClient.getSingleStatus(new File("test/coHEAD/README.txt")).getLastChangedRevision().getNumber());
		} catch (ClientException e) {
            fail("an exception occured");
		}
    }

	public void testList() {
		try {
			String urlRepos = getProject().getProperty("urlRepos");
			DirEntry[] list = svnClient.getList(new SVNUrl(urlRepos),Revision.HEAD,false);
			assertTrue(list.length > 0);
		} catch (ClientException e) {
			fail("an exception occured");
		} catch (MalformedURLException e) { }
	}

    public void testLog() {
        try {
			String urlRepos = getProject().getProperty("urlRepos");
			LogMessage[] messages = svnClient.getLogMessages(new File("test/my_repos/README.txt"),new Revision.Number(0),Revision.HEAD);
			assertEquals("initial import",messages[0].getMessage());
		} catch (ClientException e) {
            fail("an exception occured");
		}
    }


    public void testAddCommit() {
        executeTarget("testAddCommit");
		try {
			assertTrue(svnClient.getSingleStatus(new File("test/my_repos/toAdd/file0.add")).getLastChangedRevision().getNumber() > 0);
		} catch (ClientException e) {
            fail("an exception occured");
		}
    }
    
    public void testCopy() {
    	executeTarget("testCopy");
		try {
			assertTrue(svnClient.getSingleStatus(new File("test/my_repos/copyTest/copy1")).getLastChangedRevision().getNumber() > 0);
		} catch (ClientException e) {
            fail("an exception occured");
		}
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
	
	public void testMkdir() {
		executeTarget("testMkdir");
		try {
			assertTrue(svnClient.getSingleStatus(new File("test/my_repos/testMkdir2")).getLastChangedRevision().getNumber() > 0);
		} catch (ClientException e) {
            fail("an exception occured");
		}
	} 
	
	public void testMove() {
		executeTarget("testMove");
		try {
			assertTrue(svnClient.getSingleStatus(new File("test/my_repos/moveTest/dir1Renamed")).getLastChangedRevision().getNumber() > 0);
		} catch (ClientException e) {
            fail("an exception occured");
		}
	}
   
    public void testProp() {
        executeTarget("testProp");
        try {
            PropertyData propData = svnClient.propertyGet(new File("test/my_repos/propTest/file.png"),"svn:mime-type");
            assertTrue(propData != null);
            assertEquals("image/png",propData.getValue());
            propData = svnClient.propertyGet(new File("test/my_repos/propTest/file.png"),"myPicture");
            assertTrue(propData != null);
            assertEquals(170,propData.getData().length);
        } catch (ClientException e) {
            fail("an exception occured");
        }
    }

    public static void main(String[] args) {
        String[] testCaseName = { SvnTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
        //		junit.ui.TestRunner.main(testCaseName);
    }

}
