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

import org.apache.tools.ant.BuildException;

import java.io.File;

/**
 * svn Add. Add a file, a directory or a set of files to repository
 * 
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *         
 * @author Daniel Kasmeroglu (Daniel.Kameroglu@kasisoft.net)
 *
 */
public class Add extends ResourceSetSvnCommand {

    /** check directories already under version control during add ? (only for dir attribute) */
    private boolean         force       = false;

    public Add() {
        super( true, false, null );
    }

    /**
     * {@inheritDoc}
     */
    protected void handleUnmanaged( File dir ) {
        svnAddDir( dir, false, force );
    }

    protected void handleDir( File dir, boolean recurse ) {
        svnAddDir( dir, recurse, force );
    }

    protected void handleFile( File file ) {
        svnAddFile( file );
    }

    /**
     * add a file to the repository
     * @param svnClient
     * @param file
     */
    private void svnAddFile( File file ) {
        try {
            getClient().addFile( file );
        } catch( SVNClientException e ) {
            throw new BuildException( "Can't add file " + file.getAbsolutePath() + " to repository", e );
        }
    }

    /**
     * add a directory to the repository
     * @param dir
     * @param recursive
     * @param force
     */
    private void svnAddDir( File dir, boolean recursive, boolean force ) {
        try {
            getClient().addDirectory( dir, recursive, force );
        } catch( SVNClientException e ) {
            throw new BuildException( "Can't add directory " + dir.getAbsolutePath() + " to repository", e );
        }
    }

    /**
     * if set, directory will be added recursively (see setDir)
     * @param recurse
     */
    public void setRecurse( boolean recurse ) {
        super.setRecurse( recurse );
    }

    /**
     * if set, directory will be checked for new content even if already managed by subversion (see setDir)
     * @param force
     */
    public void setForce( boolean force ) {
        this.force = force;
    }

}
