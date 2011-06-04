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
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

import org.tigris.subversion.svnant.SvnAntUtilities;

import java.util.ArrayList;
import java.util.List;

import java.io.File;

/**
 * svn commit. Commit a file, a directory or a set of files to repository
 * 
 * @author Cédric Chabanois (cchabanois@ifrance.com)
 * @author Daniel Kasmeroglu (Daniel.Kasmeroglu@kasisoft.net)
 */
public class Commit extends ResourceSetSvnCommand {

    private static final String MSG_CANT_COMMIT_RESOURCES = "Can't commit resources !";

    private String          message;

    private List<File>      recursivecommit;
    private List<File>      nonrecursivecommit;
    
    public Commit() {
        super( true, true, SVNStatusKind.ADDED );
        recursivecommit     = new ArrayList<File>();
        nonrecursivecommit  = new ArrayList<File>();
        message             = null;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void handleBegin() {
        recursivecommit.clear();
        nonrecursivecommit.clear();
    }

    /**
     * {@inheritDoc}
     */
    protected void handleUnmanaged( File dir ) {
        nonrecursivecommit.add( dir );
    }

    /**
     * {@inheritDoc}
     */
    protected void handleDir( File dir, boolean recurse ) {
        if( recurse ) {
            recursivecommit.add( dir );
        } else {
            nonrecursivecommit.add( dir );
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void handleFile( File file ) {
        nonrecursivecommit.add( file );
    }

    /**
     * {@inheritDoc}
     */
    protected void handleEnd() {

        if( ! nonrecursivecommit.isEmpty() ) {
            try {
                File[] files = nonrecursivecommit.toArray( new File[ nonrecursivecommit.size() ] );
                getClient().commit( files, message, false );
            } catch( SVNClientException ex ) {
                throw ex( ex, MSG_CANT_COMMIT_RESOURCES );
            }
        }

        if( ! recursivecommit.isEmpty() ) {
            try {
                File[] files = recursivecommit.toArray( new File[ recursivecommit.size() ] );
                getClient().commit( files, message, true );
            } catch( SVNClientException ex ) {
                throw ex( ex, MSG_CANT_COMMIT_RESOURCES );
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        super.validateAttributes();
        SvnAntUtilities.attrNotEmpty( "message", message );
    }

    /**
     * {@inheritDoc}
     */
    public void setRecurse( boolean recurse ) {
        super.setRecurse( recurse );
    }

    /**
     * Set the message
     * @param message
     */
    public void setMessage( String message ) {
        this.message = message;
    }

}
