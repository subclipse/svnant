/*
 * Created on 26 févr. 2004
 */
package org.tigris.subversion.svnant;

import org.apache.tools.ant.BuildFileTest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * 
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class SvnserveTest extends BuildFileTest {

    @Before
    public void setUp() {
        configureProject( "test/svnserve/build.xml" );
    }

    @After
    public void tearDown() {
        System.out.print( getLog() );
    }

    public void testSvnservePasswdSucceed() throws Exception {
        executeTarget( "testSvnservePasswdSucceed" );
    }

    public void testSvnservePasswdFail() throws Exception {
        try {
            executeTarget( "testSvnservePasswdFail" );
            Assert.fail(); // it should have failed as an incorrect password has been given
        } catch( Exception e ) {

        }
    }

}
