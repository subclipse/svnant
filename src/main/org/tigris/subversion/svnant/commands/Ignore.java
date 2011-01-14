/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.tigris.subversion.svnant.commands;

import org.tigris.subversion.svnclientadapter.SVNClientException;

import org.tigris.subversion.svnant.SvnAntUtilities;

import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FileFilter;

/**
 * Ignore. put patterns to svn:ignore property
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class Ignore extends SvnCommand implements FileFilter {

    /** file to ignore */
    private File    file        = null;

    /** directory to which to add patterns */
    private File    dir         = null;

    /** ignore recursively ? (only for dir attribute) */
    private boolean recurse     = false;

    /** pattern to add to .svnignore */
    private String  pattern;

    /**
     * {@inheritDoc}
     */
    public void execute() {

        // deal with the single file
        if( file != null ) {
            svnIgnoreFile( file );
        }

        // deal with a directory
        if( dir != null ) {
            svnIgnorePattern( dir, recurse );
        }
        
    }

    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        SvnAntUtilities.attrsNotSet( "file, dir", true, file, dir );
        if( dir != null ) {
            SvnAntUtilities.attrIsDirectory( "dir", dir );
            SvnAntUtilities.attrNotNull( "pattern", pattern );
        }
        if( file != null ) {
            SvnAntUtilities.attrIsFile( "file", file );
            SvnAntUtilities.attrNull( "pattern", pattern );
        }
    }

    /**
     * Add a file to svn:ignore
     * @param svnClient
     * @param aFile
     */
    private void svnIgnoreFile( File aFile ) {
        try {
            getClient().addToIgnoredPatterns( aFile.getParentFile(), aFile.getName() );
        } catch( SVNClientException e ) {
            throw new BuildException( "Can't add file " + aFile.getAbsolutePath() + "to svn:ignore", e );
        }
    }

    /**
     * add the pattern to svn:ignore property on the directory
     * @param aDir
     * @param recursive
     */
    private void svnIgnorePattern( File aDir, boolean recursive ) {

        // first add the pattern to the directory
        svnIgnorePattern( aDir );

        if( recursive ) {
            File files[] = aDir.listFiles( this );
            for( int i = 0; i < files.length; i++ ) {
                svnIgnorePattern( files[i], true );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean accept( File pathname ) {
        return pathname.isDirectory() && !pathname.getName().equals( getClient().getAdminDirectoryName() );
    }
    
    private void svnIgnorePattern( File aDir ) {
        try {
            getClient().addToIgnoredPatterns( aDir, pattern );
        } catch( SVNClientException e ) {
            throw new BuildException( "Can't add pattern " + pattern + " to svn:ignore for " + aDir.getAbsolutePath(), e );
        }
    }

    /**
     * set file to ignore
     * @param file
     */
    public void setFile( File file ) {
        this.file = file;
    }

    /**
     * set the directory on which we will update the svn:ignore property
     * @param dir
     */
    public void setDir( File dir ) {
        this.dir = dir;
    }

    /**
     * if set, pattern will be added recursively on svn:ignore
     * @param recurse
     */
    public void setRecurse( boolean recurse ) {
        this.recurse = recurse;
    }

    /**
     * set the pattern to add to svn:ignore on the directory
     * @param pattern
     */
    public void setPattern( String pattern ) {
        this.pattern = pattern;
    }

}
