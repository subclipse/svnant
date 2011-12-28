package org.tigris.subversion.svnant;

import org.junit.Test;


public class SvnSvnKitTest extends SvnTest {

    public SvnSvnKitTest( String name ) {
        super( SvnClientType.svnkit );
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testCheckout() throws Exception {
        super.testCheckout();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testListCommand() throws Exception {
        super.testListCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testList() throws Exception {
        super.testList();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testLog() throws Exception {
        super.testLog();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testAddCommit() throws Exception {
        super.testAddCommit();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testCleanup() throws Exception {
        super.testCleanup();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testCopy() throws Exception {
        super.testCopy();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testDelete() throws Exception {
        super.testDelete();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testExport() throws Exception{
        super.testExport();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testImport() throws Exception{
        super.testImport();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testImportNewEntry() throws Exception{
        super.testImportNewEntry();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testMkdir() throws Exception {
        super.testMkdir();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testMove() throws Exception {
        super.testMove();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testProp() throws Exception {
        super.testProp();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testPropgetInvalidProp() throws Exception {
        super.testPropgetInvalidProp();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testDiff() throws Exception {
        super.testDiff();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testKeywords() throws Exception {
        super.testKeywords();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testUpdate() throws Exception {
        super.testUpdate();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testRevert() throws Exception {
        super.testRevert();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testCat() throws Exception {
        super.testCat();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testListener() throws Exception {
        super.testListener();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testIgnore() throws Exception {
        super.testIgnore();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testSingleStatus() throws Exception {
        super.testSingleStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testStatus() throws Exception {
        super.testStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testWcVersion() throws Exception {
        super.testWcVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testWcVersionUnmanaged() throws Exception {
        super.testWcVersionUnmanaged();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testStatusUnmanaged() throws Exception {
        super.testStatusUnmanaged();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testInfo() throws Exception {
        super.testInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testSingleInfo() throws Exception {
        super.testSingleInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testEntry() throws Exception {
        super.testEntry();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testResolve() throws Exception {
        super.testResolve();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testAnnotate() throws Exception {
        super.testAnnotate();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testSwitch() throws Exception {
        super.testSwitch();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testNormalSelector() throws Exception {
        super.testNormalSelector();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testAddedSelector() throws Exception {
        super.testAddedSelector();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testUnversionedSelector() throws Exception {
        super.testUnversionedSelector();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testModifiedSelector() throws Exception {
        super.testModifiedSelector();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testIgnoredSelector() throws Exception {
        super.testIgnoredSelector();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testConflictedSelector() throws Exception {
        super.testConflictedSelector();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testReplacedSelector() throws Exception {
        super.testReplacedSelector();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testEmbeddedSelector() throws Exception {
        super.testEmbeddedSelector();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testAddSvnFileSet() throws Exception {
        super.testAddSvnFileSet();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testCommitSvnFileSet() throws Exception {
        super.testCommitSvnFileSet();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testDeleteSvnFileSet() throws Exception {
        super.testDeleteSvnFileSet();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testKeywordsSvnFileSet() throws Exception {
        super.testKeywordsSvnFileSet();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testRevertSvnFileSet() throws Exception {
        super.testRevertSvnFileSet();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testUpdateSvnFileSet() throws Exception {
        super.testUpdateSvnFileSet();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testSvnFileSetAsRefId() throws Exception {
        super.testSvnFileSetAsRefId();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testSvnFileSetIncludes() throws Exception {
        super.testSvnFileSetIncludes();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testSvnFileSetExcludes() throws Exception {
        super.testSvnFileSetExcludes();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testSvnFileSetNestedInclude() throws Exception {
        super.testSvnFileSetNestedInclude();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testSvnFileSetNestedExclude() throws Exception {
        super.testSvnFileSetNestedExclude();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testSvnFileSetPatternSet() throws Exception {
        super.testSvnFileSetPatternSet();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testSvnExists() throws Exception {
        super.testSvnExists();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testCleanupAfterTests() throws Exception {
        super.testCleanupAfterTests();
    }
    
} /* ENDCLASS */
