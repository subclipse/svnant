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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.tigris.subversion.svnclientadapter.ISVNAnnotations;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNScheduleKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;

/**
 * You can set javahl to true or false in test/build.properties
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class SvnTest extends BuildFileTest {
private ISVNClientAdapter svnClient;
private static final String WORKINGCOPY_DIR = "test/svn/workingcopy";

    public SvnTest(String name) {
        super(name);
    }

    static {
        try {
            JhlClientAdapterFactory.setup();
        } catch (SVNClientException e) {
            // if an exception is thrown, javahl is not available or 
            // already registered ...
        }
        try {
            CmdLineClientAdapterFactory.setup();
        } catch (SVNClientException e) {
            // if an exception is thrown, command line interface is not available or
            // already registered ...                
        }
    }
    
    public void setUp() {
        configureProject("test/svn/build.xml");
        boolean javahl = true;
        String javahlProp = getProject().getProperty("javahl");
        if (javahlProp != null)
            javahl = getProject().getProperty("javahl").equalsIgnoreCase("true");
        
        if ((javahl) && (SVNClientAdapterFactory.isSVNClientAvailable(JhlClientAdapterFactory.JAVAHL_CLIENT))) {        
    		svnClient = SVNClientAdapterFactory.createSVNClient(JhlClientAdapterFactory.JAVAHL_CLIENT);
        }
        else
            svnClient = SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
    }

	public void tearDown()
	{
		System.out.print(getLog());
	}

    public void testCheckout() throws SVNClientException {
        executeTarget("testCheckout");

		assertEquals(1,svnClient.getSingleStatus(new File("test/svn/coHEAD/checkoutTest/readme.txt")).getLastChangedRevision().getNumber());
    }
    

	public void testList() throws SVNClientException,MalformedURLException {
       executeTarget("testList");
	   
       // first using a SVNUrl
       String urlRepos = getProject().getProperty("urlRepos");
	   ISVNDirEntry[] list = svnClient.getList(new SVNUrl(urlRepos+"/listTest"),SVNRevision.HEAD,false);
	   assertTrue(list.length > 0);
	   
	   // using a File
	   list = svnClient.getList(new File(WORKINGCOPY_DIR+"/listTest"),SVNRevision.BASE,false);
	   assertTrue(list.length > 0);
	   
	   // there is no BASE for listTest because it was added and committed but
	   // it needs to be updated before there is a BASE for it
	   // this is not what I expected ...
	   list = svnClient.getList(new File(WORKINGCOPY_DIR),SVNRevision.BASE,false);
	   assertTrue(list.length == 0);
	}


    public void testLog() throws SVNClientException {
        executeTarget("testLog");
		String urlRepos = getProject().getProperty("urlRepos");
		ISVNLogMessage[] messages = svnClient.getLogMessages(new File(WORKINGCOPY_DIR+"/logTest/file1.txt"),new SVNRevision.Number(0),SVNRevision.HEAD);
        assertTrue(messages.length > 0);
		assertEquals("logTest directory added to repository",messages[0].getMessage());
        assertEquals(5,messages[0].getChangedPaths().length);
        assertEquals('A',messages[0].getChangedPaths()[0].getAction());
    }

    public void testAddCommit() throws SVNClientException {
       executeTarget("testAddCommit");
	   assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/addCommitTest/file0.add")).getLastChangedRevision().getNumber() > 0);
    }

    public void testCopy() throws SVNClientException {
    	executeTarget("testCopy");
		assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/copyTest/copy1")).getLastChangedRevision().getNumber() > 0);
    } 

	public void testDelete() {
		executeTarget("testDelete");
		assertFalse(new File(WORKINGCOPY_DIR+"/deleteTest/deleteFromWorkingCopy/file0.del").exists());
		assertTrue(new File(WORKINGCOPY_DIR+"/deleteTest/deleteFromWorkingCopy/donotdel.txt").exists());
	}

	public void testExport() {
		executeTarget("testExport");
	}
	
	public void testImport() {
		executeTarget("testImport");
	} 

	
	public void testMkdir() throws SVNClientException {
		executeTarget("testMkdir");
		assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/mkdirTest/testMkdir2")).getLastChangedRevision().getNumber() > 0);
    } 
	
	public void testMove() throws SVNClientException {
		executeTarget("testMove");
		assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/moveTest/dir1Renamed")).getLastChangedRevision().getNumber() > 0);
	}
   
    public void testProp() throws SVNClientException {
        executeTarget("testProp");
        File file = new File(WORKINGCOPY_DIR+"/propTest/file.png");
        ISVNProperty propData = svnClient.propertyGet(file,"svn:mime-type");
        assertTrue(propData != null);
        assertEquals("image/png",propData.getValue());
        assertEquals(file.getAbsoluteFile(),propData.getFile());
        
        propData = svnClient.propertyGet(file,"myPicture");
        assertTrue(propData != null);
        assertEquals(170,propData.getData().length);

        propData = svnClient.propertyGet(new File(WORKINGCOPY_DIR+"/propTest/file.png"),"myProperty");
        assertTrue(propData == null);

		ISVNProperty[] properties = svnClient.getProperties(file);
		assertTrue(properties.length == 2);
		
		properties = svnClient.getProperties(new File(WORKINGCOPY_DIR+"/propTest"));
		assertEquals(0,properties.length);
        
        assertEquals("image/png",getProject().getProperty("propTest.mimeType"));
		file = new File(WORKINGCOPY_DIR+"/propTest/icon2.gif");
        assertTrue(file.exists());
    }

    public void testDiff() {
        executeTarget("testDiff");
        File patchFile = new File(WORKINGCOPY_DIR+"/diffTest/patch.txt");
        assertTrue(patchFile.exists());
        assertTrue(patchFile.length() > 0);
    }
    
    public void testKeywords() throws FileNotFoundException, IOException {
        executeTarget("testKeywords");
        DataInputStream dis = new DataInputStream(new FileInputStream(WORKINGCOPY_DIR+"/keywordsTest/file.txt")); 
        assertEquals("$LastChangedRevision: 1 $",dis.readLine());
        assertEquals("$Author: cedric $",dis.readLine());
        assertEquals("$Id$",dis.readLine());
    }

    public void testUpdate() {
        executeTarget("testUpdate");
    }

    public void testRevert() throws FileNotFoundException, IOException {
        executeTarget("testRevert");
        DataInputStream dis = new DataInputStream(new FileInputStream(WORKINGCOPY_DIR+"/revertTest/file.txt")); 
        assertEquals("first version",dis.readLine());
    }

    public void testCat() throws FileNotFoundException, IOException {
        executeTarget("testCat");
        DataInputStream dis = new DataInputStream(new FileInputStream(WORKINGCOPY_DIR+"/catTest/filecat.txt")); 
        assertEquals("first line",dis.readLine());        
        assertEquals("second line",dis.readLine());
    }

    public void testListener() throws Exception {
        final Set addSet = new HashSet();
        final Set commitSet = new HashSet();
        final Set[] currentSet = new Set[] { null };
        final boolean duplicates = false;

        ISVNNotifyListener listener = new ISVNNotifyListener() {
            public void setCommand(int command) {
                if (command == ISVNNotifyListener.Command.ADD) {
                    currentSet[0] = addSet;
                } else {
                    currentSet[0] = commitSet;
                }
            }
            
            public void logCommandLine(String commandLine) {}
            public void logMessage(String message) {}
            public void logError(String message) {}
            public void logRevision(long revision) {}
            public void logCompleted(String message) {}
  
            public void onNotify(File path, SVNNodeKind kind) {
                currentSet[0].add(path);
            }
        };
        
        Target target = (Target)project.getTargets().get("testListener");
        // first task is "copy", second one is "svn"
        Task task = target.getTasks()[1];
        SvnTask svnTask;
        if (task instanceof UnknownElement) {
            // not sure how ant works but it seems to work using that
            UnknownElement unknownElement = (UnknownElement)task;
            unknownElement.maybeConfigure();
            svnTask = (SvnTask)unknownElement.getTask(); 
        }else {
            svnTask = (SvnTask)task;
        }
 
        svnTask.addNotifyListener(listener);
        executeTarget("testListener");
        
        assertEquals(6,addSet.size()); // 6 for add and 6 for commit        
        assertEquals(6,commitSet.size()); // 6 for add and 6 for commit
        
        assertTrue(addSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest").getCanonicalFile()));
        assertTrue(addSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/dir1").getCanonicalFile()));
        assertTrue(addSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/dir1/file3.txt").getCanonicalFile()));        
        assertTrue(addSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/dir1/file4.txt").getCanonicalFile()));
        assertTrue(addSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/file1.txt").getCanonicalFile()));
        assertTrue(addSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/file2.txt").getCanonicalFile()));

        assertTrue(commitSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest").getCanonicalFile()));
        assertTrue(commitSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/dir1").getCanonicalFile()));
        assertTrue(commitSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/dir1/file3.txt").getCanonicalFile()));        
        assertTrue(commitSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/dir1/file4.txt").getCanonicalFile()));
        assertTrue(commitSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/file1.txt").getCanonicalFile()));
        assertTrue(commitSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/file2.txt").getCanonicalFile()));
        
    }

    public void testIgnore() throws Exception {
        executeTarget("testIgnore");
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/ignoreTest/fileToIgnore.txt")).isIgnored());
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/ignoreTest/dir1/file1.ignore")).isIgnored());
        assertFalse(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/ignoreTest/dir1/file2.donotignore")).isIgnored());
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/ignoreTest/dir1/dir2/file3.ignore")).isIgnored());        
    }

    public void testSingleStatus() throws Exception {
        executeTarget("testStatus");
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/statusTest/added.txt")).isAdded());
        
        // a resource that does not exist is a non managed resource
        assertFalse(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/statusTest/fileThatDoesNotExist.txt")).isManaged());
        
        // same test but in a directory that is not versioned
        assertFalse(null,svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/statusTest/nonManaged.dir/fileThatDoesNotExist.txt")).isManaged());
        
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/statusTest/ignored.txt")).isIgnored());    
        
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/statusTest/committed.txt")).hasRemote());        
        
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/statusTest/deleted.txt")).isDeleted());

        assertEquals("added",getProject().getProperty("testStatus.textStatus"));
        assertEquals("normal",getProject().getProperty("testStatus.propStatus"));
        SVNRevision.Number lastCommit = (SVNRevision.Number)SVNRevision.getRevision(getProject().getProperty("testStatus.lastCommitRevision"));
        assertEquals(null,lastCommit);
        
        SVNRevision.Number revision = (SVNRevision.Number)SVNRevision.getRevision(getProject().getProperty("testStatus.revision"));
        assertEquals(0,revision.getNumber());
        assertNotNull(getProject().getProperty("testStatus.lastCommitAuthor"));
        
    }
    
    public void testStatus() throws Exception {  
      
		executeTarget("testStatus");
        ISVNStatus[] statuses;  
        // getStatus(File, boolean) does not have the same result with command line interface
        // and svnjavahl for now. svnjavahl does not return ignored files for now 
//        statuses = svnClient.getStatus(new File(WORKINGCOPY_DIR+"/statusTest"),false,true);
        // let's verify we don't forget some files (ignored ones for example)
//        assertEquals(8,statuses.length);
        
//        statuses = svnClient.getStatus(new File(WORKINGCOPY_DIR+"/statusTest"),true);
//        assertEquals(9,statuses.length);

//        statuses = svnClient.getStatus(new File(WORKINGCOPY_DIR+"/statusTest/nonManaged.dir").getCanonicalFile(),false);
//        assertEquals(1, statuses.length);

        
        statuses = svnClient.getStatus( new File[] {
            new File(WORKINGCOPY_DIR+"/statusTest/added.txt"),
            new File(WORKINGCOPY_DIR+"/statusTest/managedDir/added in managed dir.txt"),
            new File(WORKINGCOPY_DIR+"/statusTest/nonManaged.dir"),
            new File("nonExistingFile"),
            new File(WORKINGCOPY_DIR+"/statusTest/ignored.txt"),
            new File(WORKINGCOPY_DIR+"/statusTest/nonManaged.dir/statusTest")
            }
        );
        assertEquals(6,statuses.length);
        assertEquals(new File(WORKINGCOPY_DIR+"/statusTest/added.txt").getCanonicalFile(), statuses[0].getFile());
        assertTrue(statuses[0].isAdded());
        
        assertEquals(new File(WORKINGCOPY_DIR+"/statusTest/managedDir/added in managed dir.txt").getAbsoluteFile(), statuses[1].getFile());        
        assertTrue(statuses[1].isManaged());
        assertEquals(SVNNodeKind.FILE,statuses[1].getNodeKind());
        
        assertFalse(statuses[2].isManaged());
        assertEquals(SVNNodeKind.UNKNOWN,statuses[2].getNodeKind());

        assertFalse(statuses[3].isManaged());
        assertEquals(SVNNodeKind.UNKNOWN,statuses[3].getNodeKind());
        
        assertTrue(statuses[4].isIgnored());
        assertEquals(SVNNodeKind.UNKNOWN,statuses[4].getNodeKind()); // an ignored resource is a not versioned one, so its resource kind is UNKNOWN
        
        // make sure that the top most directory is said to be versionned. It is in a directory where there is no
        // .svn directory but it is versionned however. 
        assertTrue(statuses[5].isManaged());
        assertNotNull(statuses[5].getUrl());
        
  
        // this test does not pass with command line interface : there is a problem with long
        // usernames
//        statuses = svnClient.getStatus(new File(WORKINGCOPY_DIR+"/statusTest/longUserName.dir"),true,true);
//        assertEquals(2, statuses.length);
//        assertEquals(new File(WORKINGCOPY_DIR+"/statusTest/longUserName.dir").getAbsoluteFile(), statuses[0].getFile());
        
    }

    public void testInfo() throws Exception {
		executeTarget("testStatus");
        File file = new File(WORKINGCOPY_DIR+"/statusTest/added.txt");
		ISVNInfo info = svnClient.getInfo(file);
		assertEquals(SVNNodeKind.FILE,info.getNodeKind());
        assertNull(info.getLastChangedRevision());
        assertEquals(SVNScheduleKind.ADD,info.getSchedule());
        assertEquals(file.getCanonicalFile(),info.getFile().getCanonicalFile());
        
        file = new File("nonExistingFile");
        info = svnClient.getInfo(file);
        assertEquals(null, info.getUrl());

        // make sure that the top most directory is said to be versionned. It is in a directory where there is no
        // .svn directory but it is versionned however. 
        file = new File(WORKINGCOPY_DIR+"/statusTest/nonManaged.dir/statusTest");
        info = svnClient.getInfo(file);
        assertEquals(SVNNodeKind.DIR,info.getNodeKind());
    }
    
	public void testEntry() throws Exception {
		executeTarget("testEntry");
		
		// first using a SVNUrl
		String urlRepos = getProject().getProperty("urlRepos");
		ISVNDirEntry dirEntry = svnClient.getDirEntry(new SVNUrl(urlRepos+"/entryTest/"),SVNRevision.HEAD);
		assertNotNull(dirEntry);
		assertEquals(SVNNodeKind.DIR,dirEntry.getNodeKind());
		assertEquals("entryTest",dirEntry.getPath());
		
		// using a File
		dirEntry = svnClient.getDirEntry(new File(WORKINGCOPY_DIR+"/entryTest/dir1"),SVNRevision.BASE);
		assertNotNull(dirEntry);
		assertEquals(SVNNodeKind.DIR,dirEntry.getNodeKind());
		
		// this does not work for now because working copy dir needs to be updated
		// before
//		dirEntry = svnClient.getDirEntry(new File(WORKINGCOPY_DIR+"/entryTest/"),SVNRevision.BASE);
//		assertNotNull(dirEntry);
//		assertEquals(SVNNodeKind.DIR,dirEntry.getNodeKind());
//		assertEquals("entryTest",dirEntry.getPath());		
	}

	public void testResolve() throws Exception {
		executeTarget("testResolve");
		File file = new File(WORKINGCOPY_DIR+"/resolveTest/file.txt");
		ISVNStatus status = svnClient.getSingleStatus(file);
		assertTrue(status.getTextStatus() == SVNStatusKind.CONFLICTED);
		svnClient.resolved(file);
		status = svnClient.getSingleStatus(file);
		assertTrue(status.getTextStatus() == SVNStatusKind.MODIFIED);
	}

	public void testAnnotate() throws Exception {
		executeTarget("testAnnotate");
		File file = new File(WORKINGCOPY_DIR+"/annotateTest/file.txt");
		ISVNAnnotations annotations = svnClient.annotate(file,new SVNRevision.Number(2),new SVNRevision.Number(3));
		assertEquals(3,annotations.size());
		assertNull(annotations.getAuthor(0));
		assertEquals(-1,annotations.getRevision(0));
		assertEquals("user1",annotations.getAuthor(1));
		assertEquals(2,annotations.getRevision(1));
		assertEquals("line 2",annotations.getLine(1));
		assertEquals("user2",annotations.getAuthor(2));
		assertEquals(3,annotations.getRevision(2));
		assertEquals("line 3",annotations.getLine(2));
		
		InputStream is = annotations.getInputStream();
		byte[] bytes = new byte[is.available()];
		is.read(bytes);
		assertEquals("line 1\nline 2\nline 3", new String(bytes));
		
	}
    
	
//    public void testRepositoryRoot() throws Exception {
//        executeTarget("testRepositoryRoot");
//        String urlRepos = getProject().getProperty("urlRepos");
//        SVNUrl url = svnClient.getRepositoryRoot(
//                new SVNUrl(urlRepos+"/entryTest/"));
//        assertEquals(new SVNUrl(urlRepos),url);
//    }

    
    public void testSwitch() throws Exception {
    	executeTarget("testSwitch");
    }
    
    public static void main(String[] args) {
        String[] testCaseName = { SvnTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
        //		junit.ui.TestRunner.main(testCaseName);
    }

}
