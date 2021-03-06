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
 * @author cedric
 *
 * Commit an unversioned file or tree into the repository. 
 */
public class Import extends SvnCommand {

    private static final String MSG_CANT_IMPORT = "Can't import";

    private static final String MSG_CANT_MAKE_DIR = "Can't make dir %s";

    private SVNUrl  url         = null;

    /** the path to import */
    private File    path        = null;

    private String  message     = null;

    private boolean recurse     = true;

    private String  newentry    = null;
    
    /**
     * {@inheritDoc}
     */
    public void execute() {
        try {
            SVNUrl desturl = url;
            if( newentry != null ) { 
                try {
                    desturl = url.appendPath( newentry );
                    getClient().mkdir( desturl, message );
                } catch( SVNClientException ex ) {
                    throw ex( ex, MSG_CANT_MAKE_DIR, url );
                }
            }
            getClient().doImport( path, desturl, message, recurse );
        } catch( SVNClientException ex ) {
            throw ex( ex, MSG_CANT_IMPORT );
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        SvnAntUtilities.attrNotNull( "url", url );
        SvnAntUtilities.attrNotNull( "path", path );
        SvnAntUtilities.attrNotEmpty( "message", message );
    }

    /**
     * set the url to import to
     * @param url
     */
    public void setUrl( SVNUrl url ) {
        this.url = url;
    }

    /**
     * set the path to import from
     * @param path
     */
    public void setPath( File path ) {
        this.path = path;
    }

    /**
     * set the message for immediate commit
     * @param message
     */
    public void setMessage( String message ) {
        this.message = message;
    }

    /**
     * if not set, import will operate on single directory only.
     * 
     * @param recurse   <code>true</code> <=> Import recursively.
     */
    public void setRecurse( boolean recurse ) {
        this.recurse = recurse;
    }
    
    /**
     * If set a new directory is created below the url.
     * 
     * @param newentry   The name of the sub directory which will receive the imported data.
     */
    public void setNewEntry( String newentry ) {
        this.newentry = newentry;
        if( (this.newentry != null) && (this.newentry.trim().length() == 0) ) {
            this.newentry = null;
        }
    }

}