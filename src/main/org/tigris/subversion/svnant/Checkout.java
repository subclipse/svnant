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
package org.tigris.subversion.svnant;

import java.io.File;
import java.net.URL;
import java.text.ParseException;

import org.apache.tools.ant.BuildException;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.svnclientadapter.RevisionUtils;
import org.tigris.subversion.svnclientadapter.SVNClientAdapter;

/**
 * svn Checkout. Check out a working copy from a repository 
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 */
public class Checkout extends SvnCommand {
	
	/** url to checkout from */
	private URL url = null;
	
	/** checkout recursively ? */
	private boolean recurse = true;
	
	/** destinaty directory. */ 
	private File destPath = null;
	
	/** revision to checkout */
	private Revision revision = Revision.HEAD;

	public void execute(SVNClientAdapter svnClient) throws BuildException {
		validateAttributes();

		log("Svn : Checking out a working copy from a repository :");
		try {
			svnClient.checkout(url, destPath, revision, recurse);
		} catch (Exception e) {
			throw new BuildException("Can't checkout", e);
		}
	}

	/**
	 * Ensure we have a consistent and legal set of attributes
	 */
	protected void validateAttributes() throws BuildException {
		if (destPath == null)
			destPath = getProject().getBaseDir();
		if (url == null)
			throw new BuildException("url must be set");
		if (revision == null)
			throw new BuildException("Invalid revision. Revision should be a number, a date in MM/DD/YYYY HH:MM AM_PM format or HEAD, BASE, COMMITED or PREV");

	}

	/**
	 * if false, operate on single directory only 
	 * @param recurse whether you want it to checkout files recursively.
	 */
	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}

	/**
	 * Sets the URL; required.
	 * @param url The url to set
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * Sets the destination directory; required 
	 * @param destPath destination directory for checkout.
	 */
	public void setDestpath(File destPath) {
		this.destPath = destPath;
	}

	/**
	 * Sets the revision
	 * 
	 * @param revision
	 */
	public void setRevision(String revision) {
		try {
			this.revision = RevisionUtils.getRevision(revision);
		} catch (ParseException e) {
			this.revision = null;
		}
	}

}
