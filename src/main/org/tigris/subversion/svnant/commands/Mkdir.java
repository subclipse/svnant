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

import java.util.Stack;

import java.io.File;
import java.io.IOException;

/**
 * Creates a directory directly in a repository or creates a directory on disk and schedules it 
 * for addition
 *  
 * @author Cédric Chabanois (cchabanois@ifrance.com)
 */
public class Mkdir extends SvnCommand {

    private static final String MSG_CANONICAL           = "Cannot determine canonical path of %s";

    private static final String MSG_CANT_MAKE_DIRECTORY = "Can't make dir %s";

    /** the url of dir to create */
    private SVNUrl url     = null;

    /** the path to create */
    private File   path    = null;

    /** message (when url is used) */
    private String message = null;

    private boolean makeparents = false;

    /**
     * {@inheritDoc}
     */
    public void execute() {

        if( url != null ) {
            try {
                getClient().mkdir( url, makeparents, message );
            } catch( SVNClientException ex ) {
                throw ex( ex, MSG_CANT_MAKE_DIRECTORY, url );
            }
        } else {
            try {
                if( makeparents ) {
                    try {
                        path = path.getCanonicalFile();
                    } catch( IOException ex ) {
                        throw ex( ex, MSG_CANONICAL, path);
                    }
                    // as we're working on the working copy we need to make sure
                    // that parental directories will be created and added, too
                    Stack<File> parents = new Stack<File>();
                    while( ! path.isDirectory() ) {
                        parents.push( path );
                        path = path.getParentFile();
                    }
                    while( ! parents.isEmpty() ) {
                        getClient().mkdir( parents.pop() );
                    }
                } else {
                }
                getClient().mkdir( path );
            } catch( SVNClientException ex ) {
                throw ex( ex, MSG_CANT_MAKE_DIRECTORY, path );
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        SvnAntUtilities.attrsNotSet( "url, path", true, url, path );
        if( url != null ) {
            SvnAntUtilities.attrNotEmpty( "message", message );
        }
    }

    /**
     * set the url of the new directory
     * @param url
     */
    public void setUrl( SVNUrl url ) {
        this.url = url;
    }

    /**
     * set the path of the new directory
     * @param path
     */
    public void setPath( File path ) {
        this.path = path;
    }

    /**
     * set the message for commit (only when using url)
     * @param message
     */
    public void setMessage( String message ) {
        this.message = message;
    }
    
    /**
     * Forces the creation of the parents first if a copy has to be done from a <code>srcUrl</code>
     * to a <code>destUrl</code>.
     * 
     * @param newmakeparents   <code>true</code> <=> Create parents first.
     */
    public void setMakeParents( boolean newmakeparents ) {
        this.makeparents  = newmakeparents;
    }
    

}