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

import java.io.File;

/**
 * svn Delete. Remove files and directories from version control.
 * 
 * @author Cédric Chabanois (cchabanois@ifrance.com)
 * @author Daniel Kasmeroglu (Daniel.Kasmeroglu@kasisoft.net)
 */
public class Delete extends ResourceSetSvnCommand {

    private static final String MSG_CANNOT_DELETE = "Cannot delete file or directory '%s' !";

    private static final String MSG_CANNOT_DELETE_URL = "Cannot delete url '%s'";

    private String          message  = null;

    /** url of the target to delete */
    private SVNUrl          url      = null;

    /** delete will not remove TARGETs that are, or contain, unversioned 
     * or modified items; use the force option to override this behaviour 
     */
    private boolean         force    = false;

    public Delete() {
        super( true, true );
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
        if( url == null ) {
            super.validateAttributes();
        } else {
            SvnAntUtilities.attrNotEmpty( "message", message );
        }
    }

    /**
     * delete directly on repository
     * @param url
     * @param message
     */
    private void deleteUrl( SVNUrl url, String message ) {
        try {
            getClient().remove( new SVNUrl[] { url }, message );
        } catch( SVNClientException ex ) {
            throw ex( ex, MSG_CANNOT_DELETE_URL, url.toString() );
        }
    }

    /**
     * Delete file or directory
     * When file is a directory, all subdirectories/files are deleted too
     * @param file
     * @param force
     */
    private void deleteFile( File file, boolean force ) {
        try {
            getClient().remove( new File[] { file }, force );
        } catch( SVNClientException ex ) {
            throw ex( ex, MSG_CANNOT_DELETE, file.getAbsolutePath() );
        }
    }

    /**
     * set the message for the commit (only when deleting directly from repository using an url)
     * 
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
