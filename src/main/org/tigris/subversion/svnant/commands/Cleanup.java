package org.tigris.subversion.svnant.commands;

import org.tigris.subversion.svnclientadapter.SVNClientException;

import org.apache.tools.ant.BuildException;

import java.io.File;

public class Cleanup extends SvnCommand {

    /** directory to cleanup */
    private File    path        = null;

    /**
     * {@inheritDoc}
     */
    public void execute() {
        try {
            getClient().cleanup( path );
        } catch( SVNClientException e ) {
            throw new BuildException(e);
        }
    }

    /**
     * Sets the destination directory; required 
     * @param path destination directory for cleanup.
     */
    public void setDir( File path ) {
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        if( path == null ) {
            throw new BuildException( "dir must be set" );
        }
        if( ! path.exists() ) {
            throw new BuildException( "Directory doesn't exist " + path.getAbsolutePath() );
        }
    }

}
