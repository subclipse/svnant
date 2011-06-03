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
import org.tigris.subversion.svnclientadapter.SVNUrl;

import org.tigris.subversion.svnant.SvnAntUtilities;

import org.apache.tools.ant.BuildException;

import java.io.File;

/**
 * svn Delete. Remove files and directories from version control.
 * 
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 * @author Daniel Kasmeroglu (Daniel.Kasmeroglu@kasisoft.net)
 */
public class Delete extends ResourceSetSvnCommand {

    /** message for commit (only when target is an url) */
    private String          message  = null;

    /** url of the target to delete */
    private SVNUrl          url      = null;

    /** delete will not remove TARGETs that are, or contain, unversioned 
     * or modified items; use the force option to override this behaviour 
     */
    private boolean         force    = false;

    public Delete() {
        super( true, true, null );
    }
    
    /**
     * {@inheritDoc}
     */
    protected void handleDir( File dir, boolean recurse ) {
        deleteFile( dir, force );
    }

    /**
     * {@inheritDoc}
     */
    protected void handleFile( File file ) {
        deleteFile( file, force );
    }
    
    /**
     * {@inheritDoc}
     */
    protected void handleBegin() {
        if( url != null ) {
            deleteUrl( url, message );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        super.validateAttributes();
        if( url != null ) {
            SvnAntUtilities.attrNotEmpty( "message", message );
        }
    }

    /**
     * delete directly on repository
     * @param anUrl
     * @param aMessage
     */
    private void deleteUrl( SVNUrl anUrl, String aMessage ) {
        try {
            getClient().remove( new SVNUrl[] { anUrl }, aMessage );
        } catch( SVNClientException e ) {
            throw new BuildException( "Cannot delete url " + anUrl.toString(), e );
        }
    }

    /**
     * Delete file or directory
     * When file is a directory, all subdirectories/files are deleted too
     * @param aFile
     * @param appyForce
     */
    private void deleteFile( File aFile, boolean appyForce ) {
        try {
            getClient().remove( new File[] { aFile }, appyForce );
        } catch( SVNClientException e ) {
            throw new BuildException( "Cannot delete file or directory " + aFile.getAbsolutePath(), e );
        }
    }

    /**
     * set the message for the commit (only when deleting directly from repository
     * using an url)
     * @param message
     */
    public void setMessage( String message ) {
        this.message = message;
    }

    /**
     * set url to delete
     * @param url
     */
    public void setUrl( SVNUrl url ) {
        this.url = url;
    }

    /**
     * Set the force flag
     * @param force
     */
    public void setForce( boolean force ) {
        this.force = force;
    }

}
