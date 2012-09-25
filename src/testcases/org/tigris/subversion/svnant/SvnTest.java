package org.tigris.subversion.svnant;

import org.tigris.subversion.svnclientadapter.utils.SVNStatusUtils;

import org.tigris.subversion.svnclientadapter.ISVNAnnotations;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNScheduleKind;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.text.SimpleDateFormat;

/**
 * Set the property <code>javahl</code> and/or <code>svnkit</code> to
 * <code>true</code> in any of the test/<i>[dir]</i>/build.properties
 * files to run the test cases using the JavaHL or SVNKit clients (as
 * opposed to only the command-line client).
 *
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 * @author Jeremy Whitlock
 * <a href="mailto:jwhitlock@collab.net">jwhitlock@collab.net</a>
 * @author Daniel Rall
 * @author Daniel Kasmeroglu (Daniel.Kasmeroglu@kasisoft.net)
 */
public abstract class SvnTest extends BuildFileTest {

    protected static final File WORKINGCOPY_DIR  = new File("test/svn/workingcopy");

    protected static final File WORKINGCOPY2_DIR = new File("test/svn/workingcopy2");

    protected static final File TEST_DIR         = new File("test/svn/test");

    private boolean             firstcall;
    private SvnClientType       clienttype;
    private ISVNClientAdapter   svnClient;

    public SvnTest( SvnClientType type ) {
        firstcall  = true;
        clienttype = type;
    }

    @Before
    public synchronized void setUp() {
        if( firstcall ) {
            configureProject( "test/svn/build.xml", "svn.client", clienttype.name() );
            svnClient = clienttype.createClient();
            firstcall = false;
        }
    }

    @After
    public void tearDown() {
        System.out.print( getLog() );
    }
 
    @Test
    public void testCheckout() throws Exception {
        executeTarget( "testCheckout" );
        ISVNStatus status = svnClient.getSingleStatus( new File( "test/svn/coHEAD/checkoutTest/readme.txt" ) );
        Assert.assertNotNull( status );
        Assert.assertNotNull( status.getLastChangedRevision() );
        Assert.assertEquals( 1, status.getLastChangedRevision().getNumber() );
    }

    @Test
    public void testListCommand() throws Exception {

        executeTarget( "testListCommand" );

        String url = String.format( "%s/listCommandTest", getProject().getProperty( "urlRepos" ) );
        String[] immediate = getSplittedLine( "IMMEDIATE: " );
        String[] recursive = getSplittedLine( "RECURSIVE: " );
        Arrays.sort( immediate );
        Arrays.sort( recursive );

        // compare the results for the normal listing
        String[] expectedimmediate = new String[] { "subdir1", "subdir2", "readme.txt" };
        Arrays.sort( expectedimmediate );
        Assert.assertEquals( expectedimmediate.length, immediate.length );
        for( int i = 0; i < expectedimmediate.length; i++ ) {
            Assert.assertEquals( String.format( "%s/%s", url, expectedimmediate[i] ), immediate[i] );
        }

        // compare the results for the recursive listing
        String[] expectedrecursive = new String[] { "subdir1", "subdir1/f1.txt", "subdir2", "readme.txt" };
        Arrays.sort( expectedrecursive );
        Assert.assertEquals( expectedrecursive.length, recursive.length );
        for( int i = 0; i < expectedrecursive.length; i++ ) {
            Assert.assertEquals( String.format( "%s/%s", url, expectedrecursive[i] ), recursive[i] );
        }

    }

    private String[] getSplittedLine( String token ) {
        String line = getSelectedLogLineByPrefix( token );
        Assert.assertNotNull(
                        String.format( "the log is expected to contain a line beginning with the token '%s'", token ),
                        line );
        line = line.substring( token.length() );
        return line.split( "," );
    }

    @Test
    public void testList() throws Exception {
        executeTarget( "testList" );

        // first using a SVNUrl
        String urlRepos = getProject().getProperty( "urlRepos" );
        ISVNDirEntry[] list = svnClient.getList( new SVNUrl( urlRepos + "/listTest" ), SVNRevision.HEAD, false );
        Assert.assertTrue( list.length > 0 );

        // using a File
        list = svnClient.getList( new File( WORKINGCOPY_DIR, "listTest" ), SVNRevision.BASE, false );
        Assert.assertTrue( list.length > 0 );

        // there is no BASE for listTest because it was added and committed but
        // it needs to be updated before there is a BASE for it
        // this is not what I expected ...
        list = svnClient.getList( WORKINGCOPY_DIR, SVNRevision.BASE, false );
        Assert.assertTrue( list.length == 0 );
    }

    @Test
    public void testLog() throws Exception {
        executeTarget( "testLog" );
        //    String urlRepos = getProject().getProperty("urlRepos");
        ISVNLogMessage[] messages = svnClient.getLogMessages( new File( WORKINGCOPY_DIR, "logTest/file1.txt" ),
                        new SVNRevision.Number( 0 ), SVNRevision.HEAD );
        Assert.assertTrue( messages.length > 0 );
        Assert.assertEquals( "logTest directory added to repository", messages[0].getMessage() );
        Assert.assertEquals( 5, messages[0].getChangedPaths().length );
        Assert.assertEquals( 'A', messages[0].getChangedPaths()[0].getAction() );
    }

    @Test
    public void testAddCommit() throws Exception {
        executeTarget( "testAddCommit" );
        Assert.assertTrue( svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "addCommitTest/file0.add" ) )
                        .getLastChangedRevision().getNumber() > 0 );
    }

    @Test
    public void testCleanup() throws Exception {
        executeTarget( "testCleanup" );
        Assert.assertTrue( !new File( WORKINGCOPY_DIR, ".svn/lock" ).exists() );
    }

    @Test
    public void testCopy() throws Exception {
        executeTarget( "testCopy" );
        Assert.assertTrue( svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "copyTest/copy1" ) )
                        .getLastChangedRevision().getNumber() > 0 );
    }

    @Test
    public void testDelete() throws Exception {
        executeTarget( "testDelete" );
        Assert.assertFalse( new File( WORKINGCOPY_DIR, "deleteTest/deleteFromWorkingCopy/file0.del" ).exists() );
        Assert.assertTrue( new File( WORKINGCOPY_DIR, "deleteTest/deleteFromWorkingCopy/donotdel.txt" ).exists() );
    }

    @Test
    public void testExport() throws Exception{
        executeTarget( "testExport" );
    }

    @Test
    public void testImport() throws Exception{
        executeTarget( "testImport" );
        Assert.assertTrue( new File( WORKINGCOPY_DIR, "testImport/subdir/toImport2.txt" ).isFile() );
        Assert.assertTrue( new File( WORKINGCOPY_DIR, "testImport/toImport.txt" ).isFile() );
    }

    @Test
    public void testImportNewEntry() throws Exception{
        executeTarget( "testImportNewEntry" );
        Assert.assertTrue( new File( WORKINGCOPY_DIR, "testImportNewEntry/new/subdir/toImport2.txt" ).isFile() );
        Assert.assertTrue( new File( WORKINGCOPY_DIR, "testImportNewEntry/new/toImport.txt" ).isFile() );
    }

    @Test
    public void testMkdir() throws Exception {
        executeTarget( "testMkdir" );
        Assert.assertTrue( svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "mkdirTest/testMkdir2" ) )
                        .getLastChangedRevision().getNumber() > 0 );
    }

    @Test
    public void testMove() throws Exception {
        executeTarget( "testMove" );
        Assert.assertTrue( svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "moveTest/dir1Renamed" ) )
                        .getLastChangedRevision().getNumber() > 0 );
    }

    @Test
    public void testProp() throws Exception {
        executeTarget( "testProp" );
        File file = new File( WORKINGCOPY_DIR, "propTest/file.png" );
        ISVNProperty propData = svnClient.propertyGet( file, "svn:mime-type" );
        Assert.assertTrue( propData != null );
        Assert.assertEquals( "image/png", propData.getValue() );
        Assert.assertEquals( file.getAbsoluteFile(), propData.getFile() );

        propData = svnClient.propertyGet( file, "myPicture" );
        Assert.assertTrue( propData != null );
        Assert.assertEquals( 170, propData.getData().length );

        propData = svnClient.propertyGet( new File( WORKINGCOPY_DIR, "propTest/file.png" ), "myProperty" );
        Assert.assertTrue( propData == null );

        ISVNProperty[] properties = svnClient.getProperties( file );
        Assert.assertTrue( properties.length == 2 );

        properties = svnClient.getProperties( new File( WORKINGCOPY_DIR, "propTest" ) );
        Assert.assertEquals( 0, properties.length );

        Assert.assertEquals( "image/png", getProject().getProperty( "propTest.mimeType" ) );
        Assert.assertNull( getProject().getProperty( "propTestUrlBeforeCommit.mimeType" ) );
        Assert.assertEquals( "image/png", getProject().getProperty( "propTestUrlAfterCommit.mimeType" ) );
        file = new File( WORKINGCOPY_DIR, "propTest/icon2.gif" );
        Assert.assertTrue( file.exists() );
    }

    @Test
    public void testPropgetInvalidProp() throws Exception {
        executeTarget( "testPropgetInvalidProp" );
        String prop = project.getProperty( "propgetInvalidProp.mime" );
        Assert.assertNull( prop );
    }

    @Test
    public void testDiff() throws Exception {
        executeTarget( "testDiff" );
        File patchFile = new File( WORKINGCOPY_DIR, "diffTest/patch.txt" );
        Assert.assertTrue( patchFile.exists() );
        Assert.assertTrue( patchFile.length() > 0 );
    }

    @Test
    public void testKeywords() throws Exception {
        executeTarget( "testKeywords" );
        BufferedReader reader = new BufferedReader( new FileReader( new File( WORKINGCOPY_DIR, "keywordsTest/file.txt" ) ) );
        Assert.assertEquals( "$LastChangedRevision: 1 $", reader.readLine() );
        Assert.assertEquals( "$Author: cedric $", reader.readLine() );
        Assert.assertEquals( "$Id$", reader.readLine() );
        reader.close();
    }

    @Test
    public void testUpdate() throws Exception {
        executeTarget( "testUpdate" );
    }

    @Test
    public void testRevert() throws Exception {
        executeTarget( "testRevert" );
        BufferedReader reader = new BufferedReader( new FileReader( new File( WORKINGCOPY_DIR, "revertTest/file.txt" ) ) );
        Assert.assertEquals( "first version", reader.readLine() );
        reader.close();
    }

    @Test
    public void testCat() throws Exception {
        executeTarget( "testCat" );
        BufferedReader reader = new BufferedReader( new FileReader( new File( WORKINGCOPY_DIR, "catTest/filecat.txt" ) ) );
        Assert.assertEquals( "first line", reader.readLine() );
        Assert.assertEquals( "second line", reader.readLine() );
        reader.close();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testListener() throws Exception {
        final Set<File> addSet = new HashSet<File>();
        final Set<File> commitSet = new HashSet<File>();
        final Set<File>[] currentSet = new Set[] { null };

        ISVNNotifyListener listener = new ISVNNotifyListener(){

            public void setCommand( int command ) {
                if( command == ISVNNotifyListener.Command.ADD ) {
                    currentSet[0] = addSet;
                } else {
                    currentSet[0] = commitSet;
                }
            }

            public void logCommandLine( String commandLine ) {
            }

            public void logMessage( String message ) {
            }

            public void logError( String message ) {
            }

            public void logRevision( long revision, String path ) {
            }

            public void logCompleted( String message ) {
            }

            public void onNotify( File path, SVNNodeKind kind ) {
                currentSet[0].add( path );
            }
        };

        Target target = (Target) project.getTargets().get( "testListener" );
        // first task is "copy", second one is "svn"
        Task task = target.getTasks()[1];
        SvnTask svnTask;
        if( task instanceof UnknownElement ) {
            // not sure how ant works but it seems to work using that
            UnknownElement unknownElement = (UnknownElement) task;
            unknownElement.maybeConfigure();
            svnTask = (SvnTask) unknownElement.getTask();
        } else {
            svnTask = (SvnTask) task;
        }
        
        svnTask.addNotifyListener( listener );
        executeTarget( "testListener" );

        Assert.assertEquals( 6, addSet.size() ); // 6 for add and 6 for commit        
        Assert.assertEquals( 6, commitSet.size() ); // 6 for add and 6 for commit

        Assert.assertTrue( addSet.contains( new File( WORKINGCOPY_DIR, "listenerTest" ).getCanonicalFile() ) );
        Assert.assertTrue( addSet.contains( new File( WORKINGCOPY_DIR, "listenerTest/dir1" ).getCanonicalFile() ) );
        Assert.assertTrue( addSet.contains( new File( WORKINGCOPY_DIR, "listenerTest/dir1/file3.txt" ).getCanonicalFile() ) );
        Assert.assertTrue( addSet.contains( new File( WORKINGCOPY_DIR, "listenerTest/dir1/file4.txt" ).getCanonicalFile() ) );
        Assert.assertTrue( addSet.contains( new File( WORKINGCOPY_DIR, "listenerTest/file1.txt" ).getCanonicalFile() ) );
        Assert.assertTrue( addSet.contains( new File( WORKINGCOPY_DIR, "listenerTest/file2.txt" ).getCanonicalFile() ) );

        Assert.assertTrue( commitSet.contains( new File( WORKINGCOPY_DIR, "listenerTest" ).getCanonicalFile() ) );
        Assert.assertTrue( commitSet.contains( new File( WORKINGCOPY_DIR, "listenerTest/dir1" ).getCanonicalFile() ) );
        Assert.assertTrue( commitSet.contains( new File( WORKINGCOPY_DIR, "listenerTest/dir1/file3.txt" ).getCanonicalFile() ) );
        Assert.assertTrue( commitSet.contains( new File( WORKINGCOPY_DIR, "listenerTest/dir1/file4.txt" ).getCanonicalFile() ) );
        Assert.assertTrue( commitSet.contains( new File( WORKINGCOPY_DIR, "listenerTest/file1.txt" ).getCanonicalFile() ) );
        Assert.assertTrue( commitSet.contains( new File( WORKINGCOPY_DIR, "listenerTest/file2.txt" ).getCanonicalFile() ) );

    }

    protected void assertTextStatus( ISVNStatus status, SVNStatusKind statusKind ) {
        Assert.assertEquals( status.getTextStatus(), statusKind );
    }

    protected void assertNotTextStatus( ISVNStatus status, SVNStatusKind statusKind ) {
        Assert.assertFalse( status.getTextStatus().equals( statusKind ) );
    }

    protected void assertManaged( ISVNStatus status ) {
        Assert.assertTrue( SVNStatusUtils.isManaged( status ) );
    }

    protected void assertNotManaged( ISVNStatus status ) {
        Assert.assertFalse( SVNStatusUtils.isManaged( status ) );
    }

    protected void assertHasRemote( ISVNStatus status ) {
        Assert.assertTrue( SVNStatusUtils.hasRemote( status ) );
    }

    @Test
    public void testIgnore() throws Exception {
        executeTarget( "testIgnore" );
        assertTextStatus( svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "ignoreTest/fileToIgnore.txt" ) ),
                        SVNStatusKind.IGNORED );
        assertTextStatus( svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "ignoreTest/dir1/file1.ignore" ) ),
                        SVNStatusKind.IGNORED );
        assertNotTextStatus(
                        svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "ignoreTest/dir1/file2.donotignore" ) ),
                        SVNStatusKind.IGNORED );
        assertTextStatus(
                        svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "ignoreTest/dir1/dir2/file3.ignore" ) ),
                        SVNStatusKind.IGNORED );
    }

    @Test
    public void testSingleStatus() throws Exception {
        executeTarget( "testStatus" );
        assertTextStatus( svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "statusTest/added.txt" ) ),
                        SVNStatusKind.ADDED );

        // a resource that does not exist is a non managed resource
        assertNotManaged( svnClient
                        .getSingleStatus( new File( WORKINGCOPY_DIR, "statusTest/fileThatDoesNotExist.txt" ) ) );

        // same test but in a directory that is not versioned
        assertNotManaged( svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "statusTest/nonManaged.dir/fileThatDoesNotExist.txt" ) ) );

        assertTextStatus( svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "statusTest/ignored.txt" ) ),
                        SVNStatusKind.IGNORED );

        assertHasRemote( svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "statusTest/committed.txt" ) ) );

        assertTextStatus( svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "statusTest/deleted.txt" ) ),
                        SVNStatusKind.DELETED );

        Assert.assertEquals( "added", getProject().getProperty( "a_testStatus.textStatus" ) );
        Assert.assertEquals( "non-svn", getProject().getProperty( "a_testStatus.propStatus" ) );
        SVNRevision.Number lastCommit = (SVNRevision.Number) SVNRevision.getRevision( getProject().getProperty(
                        "a_testStatus.lastCommitRevision" ) );
        Assert.assertEquals( null, lastCommit );

        SVNRevision.Number revision = (SVNRevision.Number) SVNRevision.getRevision( getProject().getProperty(
                        "a_testStatus.revision" ) );
        Assert.assertEquals( 0, revision.getNumber() );
        Assert.assertNotNull( getProject().getProperty( "a_testStatus.lastCommitAuthor" ) );

    }

    @Test
    public void testStatus() throws Exception {

        executeTarget( "testStatus" );

        File file = new File( WORKINGCOPY_DIR, "statusTest/added.txt" );
        ISVNInfo info = svnClient.getInfo( file );
        Assert.assertEquals( SVNNodeKind.FILE, info.getNodeKind() );
        Assert.assertNull( info.getLastChangedRevision() );
        Assert.assertEquals( SVNScheduleKind.ADD, info.getSchedule() );
        Assert.assertEquals( file.getCanonicalFile(), info.getFile().getCanonicalFile() );

        // make sure that the top most directory is said to be versionned. It is in a directory where there is no
        // .svn directory but it is versionned however. 
        file = new File( WORKINGCOPY_DIR, "statusTest/nonManaged.dir/statusTest" );
        info = svnClient.getInfo( file );
        Assert.assertEquals( SVNNodeKind.DIR, info.getNodeKind() );

        ISVNStatus[] statuses;
        // getStatus(File, boolean) does not have the same result with command line interface
        // and svnjavahl for now. svnjavahl does not return ignored files for now 
        statuses = svnClient.getStatus( new File( WORKINGCOPY_DIR, "statusTest" ), false, true );
        // let's verify we don't forget some files (ignored ones for example)
        //TODO some test are disabled ?       
        //        assertEquals(8,statuses.length);

        //        statuses = svnClient.getStatus(new File(WORKINGCOPY_DIR+"/statusTest"),true);
        //        assertEquals(9,statuses.length);

        //        statuses = svnClient.getStatus(new File(WORKINGCOPY_DIR+"/statusTest/nonManaged.dir").getCanonicalFile(),false);
        //        assertEquals(1, statuses.length);

        statuses = svnClient.getStatus( new File[] { new File( WORKINGCOPY_DIR, "statusTest/added.txt" ),
                        new File( WORKINGCOPY_DIR, "statusTest/managedDir/added in managed dir.txt" ),
                        new File( WORKINGCOPY_DIR, "statusTest/nonManaged.dir" ), new File( "nonExistingFile" ),
                        new File( WORKINGCOPY_DIR, "statusTest/ignored.txt" ),
                        new File( WORKINGCOPY_DIR, "statusTest/nonManaged.dir/statusTest" ) } );
        Assert.assertEquals( 6, statuses.length );
        Assert.assertEquals( new File( WORKINGCOPY_DIR, "statusTest/added.txt" ).getCanonicalFile(), statuses[0].getFile() );
        assertTextStatus( statuses[0], SVNStatusKind.ADDED );

        Assert.assertEquals( new File( WORKINGCOPY_DIR, "statusTest/managedDir/added in managed dir.txt" ).getAbsoluteFile(),
                        statuses[1].getFile() );
        assertManaged( statuses[1] );
        Assert.assertEquals( SVNNodeKind.FILE, statuses[1].getNodeKind() );

        assertNotManaged( statuses[2] );
        Assert.assertEquals( SVNNodeKind.UNKNOWN, statuses[2].getNodeKind() );

        assertNotManaged( statuses[3] );
        Assert.assertEquals( SVNNodeKind.UNKNOWN, statuses[3].getNodeKind() );

        assertTextStatus( statuses[4], SVNStatusKind.IGNORED );
        Assert.assertEquals( SVNNodeKind.UNKNOWN, statuses[4].getNodeKind() ); // an ignored resource is a not versioned one, so its resource kind is UNKNOWN

        // make sure that the top most directory is said to be versionned. It is in a directory where there is no
        // .svn directory but it is versionned however. 
        assertManaged( statuses[5] );
        Assert.assertNotNull( statuses[5].getUrl() );

        // this test does not pass with command line interface : there is a problem with long
        // usernames
        //      TODO some test are disabled ?       
        //        statuses = svnClient.getStatus(new File(WORKINGCOPY_DIR+"/statusTest/longUserName.dir"),true,true);
        //        assertEquals(2, statuses.length);
        //        assertEquals(new File(WORKINGCOPY_DIR+"/statusTest/longUserName.dir").getAbsoluteFile(), statuses[0].getFile());

        ISVNStatus status = svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "statusTest/committed.txt" ) );

        Assert.assertEquals( status.getTextStatus().toString(), getProject().getProperty( "testStatus.textStatus" ) );
        Assert.assertEquals( status.getPropStatus().toString(), getProject().getProperty( "testStatus.propStatus" ) );
        Assert.assertEquals( status.getLastChangedRevision().toString(),
                        getProject().getProperty( "testStatus.lastCommitRevision" ) );
        Assert.assertEquals( status.getRevision().toString(), getProject().getProperty( "testStatus.revision" ) );
        Assert.assertEquals( status.getLastCommitAuthor(), getProject().getProperty( "testStatus.lastCommitAuthor" ) );
        Assert.assertEquals( new SimpleDateFormat( "yyyy/MM/dd HH:mm" ).format( status.getLastChangedDate() ), getProject()
                        .getProperty( "testStatus.lastChangedDate" ) );
        Assert.assertEquals( status.getUrl().toString(), getProject().getProperty( "testStatus.url" ) );
    }

    @Test
    public void testWcVersion() throws Exception {

        executeTarget( "testStatus" );

        ISVNStatus status = svnClient.getSingleStatus( new File( WORKINGCOPY_DIR, "statusTest" ) );

        Assert.assertEquals( status.getUrl().toString(), getProject().getProperty( "wc.repository.url" ) );
        //        assertEquals("", getProject().getProperty("wc.repository.path"));
        Assert.assertEquals( "4", getProject().getProperty( "wc.revision.max" ) );
        Assert.assertEquals( "4M", getProject().getProperty( "wc.revision.max-with-flags" ) );
        Assert.assertEquals( "4", getProject().getProperty( "wc.committed.max" ) );
        Assert.assertEquals( "4M", getProject().getProperty( "wc.committed.max-with-flags" ) );
        Assert.assertEquals( "true", getProject().getProperty( "wc.modified" ) );
        Assert.assertNull( getProject().getProperty( "wc.mixed" ) );
    }

    @Test
    public void testWcVersionUnmanaged() throws Exception {
        expectBuildExceptionContaining( "testWcVersionUnmanaged", "wcVersion on unversioned directory",
                        "is not under version control !" );
    }

    @Test
    public void testStatusUnmanaged() throws Exception {
        executeTarget( "testStatusUnmanaged" );

        Assert.assertEquals( project.getProperty( "unmanaged1.textStatus" ), "unversioned" );
        Assert.assertEquals( project.getProperty( "unmanaged1.propStatus" ), "non-svn" );
        Assert.assertEquals( project.getProperty( "unmanaged1.lastCommitRevision" ), "" );
        Assert.assertEquals( project.getProperty( "unmanaged1.revision" ), "-1" );
        Assert.assertEquals( project.getProperty( "unmanaged1.lastCommitAuthor" ), "" );
        Assert.assertEquals( project.getProperty( "unmanaged1.url" ), "" );

        Assert.assertEquals( project.getProperty( "unmanagedDir.textStatus" ), "unversioned" );
        Assert.assertEquals( project.getProperty( "unmanagedDir.propStatus" ), "non-svn" );
        Assert.assertEquals( project.getProperty( "unmanagedDir.lastCommitRevision" ), "" );
        Assert.assertEquals( project.getProperty( "unmanagedDir.revision" ), "-1" );
        Assert.assertEquals( project.getProperty( "unmanagedDir.lastCommitAuthor" ), "" );
        Assert.assertEquals( project.getProperty( "unmanagedDir.url" ), "" );

        Assert.assertEquals( project.getProperty( "unmanaged2.textStatus" ), "unversioned" );
        Assert.assertEquals( project.getProperty( "unmanaged2.propStatus" ), "non-svn" );
        Assert.assertEquals( project.getProperty( "unmanaged2.lastCommitRevision" ), "" );
        Assert.assertEquals( project.getProperty( "unmanaged2.revision" ), "-1" );
        Assert.assertEquals( project.getProperty( "unmanaged2.lastCommitAuthor" ), "" );
        Assert.assertEquals( project.getProperty( "unmanaged2.url" ), "" );
    }

    @Test
    public void testInfo() throws Exception {
        expectBuildException( "testInfoNoAttributes", "Dir or file must be set." );
        expectBuildException( "testInfoBadFile", "fakefile.txt:  (Not a versioned resource)" );

        executeTarget( "testInfoDirectory" );

        String[] propNames = new String[] { "svn.info.path", "svn.info.url", "svn.info.repourl", "svn.info.repouuid",
                        "svn.info.rev", "svn.info.nodekind", "svn.info.schedule", "svn.info.author",
                        "svn.info.lastRev", "svn.info.lastDate" };

        for( int i = 0; i < propNames.length; i++ ) {
            assertPropertySet( propNames[i], true );
        }

        propNames = new String[] { "svn.info.name", "svn.info.lastTextUpdate", "svn.info.lastPropUpdate",
                        "svn.info.checksum" };

        // Property shouldn't be set for a directory.
        for( int i = 0; i < propNames.length; i++ ) {
            assertPropertyUnset( propNames[i] );
        }

        executeTarget( "testInfoFile" );

        propNames = new String[] { "svn.info.path", "svn.info.name", "svn.info.url", "svn.info.repourl",
                        "svn.info.repouuid", "svn.info.rev", "svn.info.nodekind", "svn.info.schedule",
                        "svn.info.author", "svn.info.lastRev", "svn.info.lastDate", "svn.info.lastTextUpdate",
                        "svn.info.lastPropUpdate", "svn.info.checksum" };

        for( int i = 0; i < propNames.length; i++ ) {
            assertPropertySet( propNames[i], true );
        }

        executeTarget( "testInfoCustomPrefix" );
        assertPropertySet( "wc.info.path", true );

        executeTarget( "testInfoURL" );

        propNames = new String[] { "svn.info.url", "svn.info.repouuid", "svn.info.repourl", "svn.info.rev",
                        "svn.info.nodekind", "svn.info.author", "svn.info.lastRev", "svn.info.lastDate" };

        for( int i = 0; i < propNames.length; i++ ) {
            assertPropertySet( propNames[i], true );
        }

        executeTarget( "testInfoCustomisedDateFormat" );
        propNames = new String[] { "svn.info.lastDate", };
        for( int i = 0; i < propNames.length; i++ ) {
            assertPropertySet( propNames[i], true );
        }
        SimpleDateFormat formatter = new SimpleDateFormat( "dd-MMM-yyyy HH:mm" );
        assertPropertyEquals( "svn.info.lastDate", formatter.format( new Date( System.currentTimeMillis() ) ) );

    }

    @Test
    public void testSingleInfo() throws Exception {
        executeTarget( "testSingleInfo" );
        Assert.assertEquals( "1", getProject().getProperty( "val_revision" ) );
        Assert.assertEquals( "file1.txt", getProject().getProperty( "val_name" ) );
    }

    @Test
    public void testEntry() throws Exception {
        executeTarget( "testEntry" );

        // first using a SVNUrl
        String urlRepos = getProject().getProperty( "urlRepos" );
        ISVNDirEntry dirEntry = svnClient.getDirEntry( new SVNUrl( urlRepos + "/entryTest/" ), SVNRevision.HEAD );
        Assert.assertNotNull( dirEntry );
        Assert.assertEquals( SVNNodeKind.DIR, dirEntry.getNodeKind() );
        Assert.assertEquals( "entryTest", dirEntry.getPath() );

        // using a File
        dirEntry = svnClient.getDirEntry( new File( WORKINGCOPY_DIR, "entryTest/dir1" ), SVNRevision.BASE );
        Assert.assertNotNull( dirEntry );
        Assert.assertEquals( SVNNodeKind.DIR, dirEntry.getNodeKind() );

        // this does not work for now because working copy dir needs to be updated
        // before
        //    TODO some test are disabled ?       
        //    dirEntry = svnClient.getDirEntry(new File(WORKINGCOPY_DIR+"/entryTest/"),SVNRevision.BASE);
        //    assertNotNull(dirEntry);
        //    assertEquals(SVNNodeKind.DIR,dirEntry.getNodeKind());
        //    assertEquals("entryTest",dirEntry.getPath());   
    }

    @Test
    public void testResolve() throws Exception {
        executeTarget( "testResolve" );
        File file = new File( WORKINGCOPY_DIR, "resolveTest/file.txt" );
        ISVNStatus status = svnClient.getSingleStatus( file );
        Assert.assertTrue( status.getTextStatus() == SVNStatusKind.CONFLICTED );
        svnClient.resolved( file );
        status = svnClient.getSingleStatus( file );
        Assert.assertTrue( status.getTextStatus() == SVNStatusKind.MODIFIED );
    }

    @Test
    public void testAnnotate() throws Exception {
        executeTarget( "testAnnotate" );
        File file = new File( WORKINGCOPY_DIR, "annotateTest/file.txt" );
        ISVNAnnotations annotations = svnClient
                        .annotate( file, new SVNRevision.Number( 2 ), new SVNRevision.Number( 3 ) );
        Assert.assertEquals( 3, annotations.numberOfLines() );
        Assert.assertEquals( 0, annotations.getRevision( 0 ) );
        Assert.assertEquals( "user1", annotations.getAuthor( 1 ) );
        Assert.assertEquals( 2, annotations.getRevision( 1 ) );
        Assert.assertEquals( "line 2", annotations.getLine( 1 ) );
        Assert.assertEquals( "user2", annotations.getAuthor( 2 ) );
        Assert.assertEquals( 3, annotations.getRevision( 2 ) );
        Assert.assertEquals( "line 3", annotations.getLine( 2 ) );

        InputStream is = annotations.getInputStream();
        byte[] bytes = new byte[is.available()];
        is.read( bytes );
        Assert.assertEquals( "line 1\nline 2\nline 3", new String( bytes ) );

    }

    @Test
    public void testSwitch() throws Exception {
        executeTarget( "testSwitch" );
    }

    @Test
    public void testNormalSelector() throws Exception {
        executeTarget( "testNormalSelector" );

        // Count number of files in test directory
        File dir2 = TEST_DIR;
        Assert.assertTrue( dir2.exists() );
        Assert.assertTrue( dir2.isDirectory() );
        Assert.assertEquals( 2, dir2.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir2, "normal1.txt" )).exists() );
        Assert.assertTrue( (new File( dir2, "normal2.txt" )).exists() );
    }

    @Test
    public void testAddedSelector() throws Exception {
        executeTarget( "testAddedSelector" );

        // Count number of files in test directory
        File dir2 = TEST_DIR;
        Assert.assertTrue( dir2.exists() );
        Assert.assertTrue( dir2.isDirectory() );
        Assert.assertEquals( 2, dir2.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir2, "added1.txt" )).exists() );
        Assert.assertTrue( (new File( dir2, "added2.txt" )).exists() );
    }

    @Test
    public void testUnversionedSelector() throws Exception {
        executeTarget( "testUnversionedSelector" );

        // Count number of files in test directory
        File dir2 = TEST_DIR;
        Assert.assertTrue( dir2.exists() );
        Assert.assertTrue( dir2.isDirectory() );
        Assert.assertEquals( 2, dir2.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir2, "unversioned1.txt" )).exists() );
        Assert.assertTrue( (new File( dir2, "unversioned2.txt" )).exists() );
    }

    @Test
    public void testModifiedSelector() throws Exception {
        executeTarget( "testModifiedSelector" );

        // Count number of files in test directory
        File dir2 = TEST_DIR;
        Assert.assertTrue( dir2.exists() );
        Assert.assertTrue( dir2.isDirectory() );
        Assert.assertEquals( 4, dir2.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir2, "modified1.txt" )).exists() );
        Assert.assertTrue( (new File( dir2, "modified2.txt" )).exists() );
        Assert.assertTrue( (new File( dir2, "conflicted1.txt" )).exists() );
        Assert.assertTrue( (new File( dir2, "conflicted2.txt" )).exists() );
    }

    @Test
    public void testIgnoredSelector() throws Exception {
        executeTarget( "testIgnoredSelector" );

        // Count number of files in test directory
        File dir2 = TEST_DIR;
        Assert.assertTrue( dir2.exists() );
        Assert.assertTrue( dir2.isDirectory() );
        Assert.assertEquals( 2, dir2.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir2, "ignored1.txt" )).exists() );
        Assert.assertTrue( (new File( dir2, "ignored2.txt" )).exists() );
    }

    @Test
    public void testConflictedSelector() throws Exception {
        executeTarget( "testConflictedSelector" );

        // Count number of files in test directory
        File dir2 = TEST_DIR;
        Assert.assertTrue( dir2.exists() );
        Assert.assertTrue( dir2.isDirectory() );
        Assert.assertEquals( 2, dir2.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir2, "conflicted1.txt" )).exists() );
        Assert.assertTrue( (new File( dir2, "conflicted2.txt" )).exists() );
    }

    @Test
    public void testReplacedSelector() throws Exception {
        executeTarget( "testReplacedSelector" );

        // Count number of files in test directory
        File dir2 = TEST_DIR;
        Assert.assertTrue( dir2.exists() );
        Assert.assertTrue( dir2.isDirectory() );
        Assert.assertEquals( 2, dir2.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir2, "replaced1.txt" )).exists() );
        Assert.assertTrue( (new File( dir2, "replaced2.txt" )).exists() );
    }

    @Test
    public void testEmbeddedSelector() throws Exception {
        executeTarget( "testEmbeddedSelector" );

        // Count number of files in test directory
        File dir2 = TEST_DIR;
        Assert.assertTrue( dir2.exists() );
        Assert.assertTrue( dir2.isDirectory() );
        Assert.assertEquals( 0, dir2.listFiles().length );

    }

    @Test
    public void testAddSvnFileSet() throws Exception {
        executeTarget( "testAddSvnFileSet" );

        // Count number of files in test directory
        File dir2 = TEST_DIR;
        Assert.assertTrue( dir2.exists() );
        Assert.assertTrue( dir2.isDirectory() );
        Assert.assertEquals( 2, dir2.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir2, "added1.txt" )).exists() );
        Assert.assertTrue( (new File( dir2, "added2.txt" )).exists() );
    }

    @Test
    public void testCommitSvnFileSet() throws Exception {
        executeTarget( "testCommitSvnFileSet" );

        // Count number of files in test directory
        File dir = TEST_DIR;
        Assert.assertTrue( dir.exists() );
        Assert.assertTrue( dir.isDirectory() );
        Assert.assertEquals( 2, dir.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir, "file2.txt" )).exists() );
        Assert.assertTrue( (new File( dir, "dir1" )).exists() );

        File dir2 = new File( TEST_DIR, "dir1" );
        Assert.assertTrue( dir2.exists() );
        Assert.assertTrue( dir2.isDirectory() );
        Assert.assertEquals( 1, dir2.listFiles().length );
        Assert.assertTrue( (new File( dir2, "file1.txt" )).exists() );
    }

    @Test
    public void testDeleteSvnFileSet() throws Exception {
        executeTarget( "testDeleteSvnFileSet" );

        // Count number of files in test directory
        File dir = WORKINGCOPY2_DIR;
        Assert.assertTrue( dir.exists() );
        Assert.assertTrue( dir.isDirectory() );
        File[] files = dir.listFiles();
        Assert.assertEquals( 3, files.length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir, "normal1.txt" )).exists() );
        Assert.assertTrue( (new File( dir, ".svn" )).exists() );
        Assert.assertTrue( (new File( dir, "dir1" )).exists() );

        File dir2 = new File( WORKINGCOPY2_DIR, "dir1" );
        Assert.assertTrue( dir2.exists() );
        Assert.assertTrue( dir2.isDirectory() );
        Assert.assertEquals( 2, dir2.listFiles().length );
        Assert.assertTrue( (new File( dir2, "normal2.txt" )).exists() );
        Assert.assertTrue( (new File( dir2, ".svn" )).exists() );
    }

    @Test
    public void testKeywordsSvnFileSet() throws Exception {
        executeTarget( "testKeywordsSvnFileSet" );

        // Test file1.txt
        File dir = WORKINGCOPY_DIR;
        File file1 = new File( dir, "file1.txt" );
        BufferedReader br1 = new BufferedReader( new InputStreamReader( new FileInputStream( file1 ) ) );
        String content1 = br1.readLine();
        br1.close();
        Assert.assertTrue( content1.matches( ".*[0-9]+.*" ) );

        // Test file2.txt
        File dir2 = new File( WORKINGCOPY_DIR, "dir1" );
        File file2 = new File( dir2, "file2.txt" );
        BufferedReader br2 = new BufferedReader( new InputStreamReader( new FileInputStream( file2 ) ) );
        String content2 = br2.readLine();
        br2.close();
        Assert.assertTrue( content2.matches( ".*[0-9]+.*" ) );
    }

    @Test
    public void testRevertSvnFileSet() throws Exception {
        executeTarget( "testRevertSvnFileSet" );

        // Test deleted1.txt
        File dir = WORKINGCOPY_DIR;
        File deleted1 = new File( dir, "deleted1.txt" );
        Assert.assertTrue( deleted1.exists() );

        // Test deleted2.txt
        File dir2 = new File( WORKINGCOPY_DIR, "dir1" );
        File deleted2 = new File( dir2, "deleted2.txt" );
        Assert.assertTrue( deleted2.exists() );
    }

    @Test
    public void testUpdateSvnFileSet() throws Exception {
        executeTarget( "testUpdateSvnFileSet" );

        // Test missing1.txt
        File dir = WORKINGCOPY_DIR;
        File missing1 = new File( dir, "missing1.txt" );
        Assert.assertTrue( missing1.exists() );

        // Test missing2.txt
        File dir2 = new File( WORKINGCOPY_DIR, "dir1" );
        File missing2 = new File( dir2, "missing2.txt" );
        Assert.assertTrue( missing2.exists() );
    }

    @Test
    public void testSvnFileSetAsRefId() throws Exception {
        executeTarget( "testSvnFileSetAsRefId" );

        // Count number of files in test directory
        File dir = TEST_DIR;
        Assert.assertTrue( dir.exists() );
        Assert.assertTrue( dir.isDirectory() );
        Assert.assertEquals( 2, dir.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir, "added1.txt" )).exists() );
        Assert.assertTrue( (new File( dir, "added2.txt" )).exists() );
    }

    @Test
    public void testSvnFileSetIncludes() throws Exception {
        executeTarget( "testSvnFileSetIncludes" );

        // Count number of files in test directory
        File dir = TEST_DIR;
        Assert.assertTrue( dir.exists() );
        Assert.assertTrue( dir.isDirectory() );
        Assert.assertEquals( 2, dir.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir, "file11.txt" )).exists() );
        Assert.assertTrue( (new File( dir, "dir" )).exists() );

        dir = new File( dir, "dir" );
        Assert.assertEquals( 1, dir.listFiles().length );
        Assert.assertTrue( (new File( dir, "file21.txt" )).exists() );
    }

    @Test
    public void testSvnFileSetExcludes() throws Exception {
        executeTarget( "testSvnFileSetExcludes" );

        // Count number of files in test directory
        File dir = TEST_DIR;
        Assert.assertTrue( dir.exists() );
        Assert.assertTrue( dir.isDirectory() );
        Assert.assertEquals( 2, dir.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir, "file12.txt" )).exists() );
        Assert.assertTrue( (new File( dir, "dir" )).exists() );

        dir = new File( dir, "dir" );
        Assert.assertEquals( 1, dir.listFiles().length );
        Assert.assertTrue( (new File( dir, "file22.txt" )).exists() );
    }

    @Test
    public void testSvnFileSetNestedInclude() throws Exception {
        executeTarget( "testSvnFileSetNestedInclude" );

        // Count number of files in test directory
        File dir = TEST_DIR;
        Assert.assertTrue( dir.exists() );
        Assert.assertTrue( dir.isDirectory() );
        Assert.assertEquals( 2, dir.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir, "file11.txt" )).exists() );
        Assert.assertTrue( (new File( dir, "dir" )).exists() );

        dir = new File( dir, "dir" );
        Assert.assertEquals( 1, dir.listFiles().length );
        Assert.assertTrue( (new File( dir, "file21.txt" )).exists() );
    }

    @Test
    public void testSvnFileSetNestedExclude() throws Exception {
        executeTarget( "testSvnFileSetNestedExclude" );

        // Count number of files in test directory
        File dir = TEST_DIR;
        Assert.assertTrue( dir.exists() );
        Assert.assertTrue( dir.isDirectory() );
        Assert.assertEquals( 2, dir.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir, "file12.txt" )).exists() );
        Assert.assertTrue( (new File( dir, "dir" )).exists() );

        dir = new File( dir, "dir" );
        Assert.assertEquals( 1, dir.listFiles().length );
        Assert.assertTrue( (new File( dir, "file22.txt" )).exists() );
    }

    @Test
    public void testSvnFileSetPatternSet() throws Exception {
        executeTarget( "testSvnFileSetPatternSet" );

        // Count number of files in test directory
        File dir = TEST_DIR;
        Assert.assertTrue( dir.exists() );
        Assert.assertTrue( dir.isDirectory() );
        Assert.assertEquals( 2, dir.listFiles().length );

        // Verify that the expected files are present
        Assert.assertTrue( (new File( dir, "file1.xml" )).exists() );
        Assert.assertTrue( (new File( dir, "dir1" )).exists() );

        dir = new File( dir, "dir1" );
        Assert.assertEquals( 1, dir.listFiles().length );
        Assert.assertTrue( (new File( dir, "file3.xml" )).exists() );
    }

    @Test
    public void testSvnExists() throws Exception {
        executeTarget( "testSvnExists" );

        // Test for expected results
        Assert.assertEquals( "true", project.getProperty( "svnExists.local.checkedin" ) );
        Assert.assertEquals( "true", project.getProperty( "svnExists.local.added" ) );
        Assert.assertEquals( null, project.getProperty( "svnExists.local.private" ) );
        Assert.assertEquals( null, project.getProperty( "svnExists.local.inexistant" ) );
        Assert.assertEquals( "true", project.getProperty( "svnExists.server.checkedin" ) );
        Assert.assertEquals( null, project.getProperty( "svnExists.server.added" ) );
        Assert.assertEquals( null, project.getProperty( "svnExists.server.private" ) );
    }

    /**
     * Asserts whether an Ant property is set.
     */
    private void assertPropertySet( String propName, boolean expectedSet ) {
        if( expectedSet ) {
            Assert.assertNotNull( "Property '" + propName + "' should be set", super.project.getProperty( propName ) );
        } else {
            Assert.assertNull( "Property '" + propName + "' should be null", super.project.getProperty( propName ) );
        }
    }

    /**
     * This is not actually a test case, but a hook to assure that
     * cleanup is handled after all test cases have run (rather than
     * after <i>each</i> test case, which would take longer).
     */
    @Test
    public void testCleanupAfterTests() throws Exception {
        executeTarget( "clean" );
    }

}
