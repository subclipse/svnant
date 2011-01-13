package org.tigris.subversion.svnant.commands;

import java.io.File;

import org.tigris.subversion.svnant.SvnAntException;

import org.apache.tools.ant.BuildException;

import org.tigris.subversion.svnclientadapter.SVNClientException;

public class Cleanup extends SvnCommand {

    /** directory to cleanup */
    private File    path        = null;

    /**
     * {@inheritDoc}
     */
    public void execute() throws SvnAntException {
        try {
            getClient().cleanup( path );
        } catch( SVNClientException e ) {
            throw new SvnAntException(e);
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
