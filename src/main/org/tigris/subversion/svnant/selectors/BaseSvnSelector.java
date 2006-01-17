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
package org.tigris.subversion.svnant.selectors;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.selectors.BaseExtendSelector;
import org.tigris.subversion.svnant.SvnTask;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JavaSvnClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;

/**
 * This is an abstract class that implements all functionality shared
 * between all file selectors in svn-ant. In particular, it implements
 * the handling of the common parameters: javahl and javasvn. It provides
 * the logic to select the approrpriate client adapter. Finally, it implements
 * the method required by all Ant selectors (isSelected) and redirects
 * the control flow to a subclass implementation while providing the
 * appropriate client adapter.  
 * 
 * @author Jean-Pierre Fiset <a href="mailto:jp@fiset.ca">jp@fiset.ca</a>
 *
 */
public abstract class BaseSvnSelector extends BaseExtendSelector {


    
    /**
     * 'javahl' property for file selector. If set,
     * JAVAHL bindings are used, if available. Preempts
     * JavaSVN and command line.
     */
    private boolean javahl = true;
    
    /**
     * 'javasvn' property for file selector. If set,
     * JavaSVN client is used, if available. Preempts
     * command line, but not JAVAHL bindings.  
     */
    private boolean javasvn = true;

    /**
     * Accessor method to 'javahl' property. If reset (false),
     * JavaHL is not used.
     * @param javahl_ New value for javahl property.
     */
    public void setJavahl(boolean javahl_) {
        javahl = javahl_;
    }

    /**
     * Accessor method to 'javasvn' property. If reset (false),
     * JavaSVN is not used.
     * @param javasvn_ New value for javasvn property.
     */
    public void setJavasvn(boolean javasvn_) {
        javasvn = javasvn_;
    }
    
	final public boolean isSelected(File basedir_, String filename_, File file_) throws BuildException {
		return isSelected(getClientAdapter(), basedir_, filename_, file_);
	}
	
	/**
	 * Method that needs to be reimplemented by each subclass. It is equivalent to 'isSelected',
	 * inherited from BaseExtendSelector, with the exception that a SVN client adaptor is provided. 
	 * @param svnClient_ The SVN client that should be used to perform repository access
	 * @param basedir_ A java.io.File object for the base directory
	 * @param filename_ The name of the file to check
	 * @param file_ A File object for this filename
	 * @exception BuildException if an error occurs
	 * @return Returns true if the file should be selected. Otherwise, false. 
	 */
	abstract public boolean isSelected(ISVNClientAdapter svnClient_, File basedir_, String filename_, File file_) throws BuildException;
	
	/**
	 * This method returns a SVN client adapter, based on the property set when the file selector
	 * was declared. More specifically, the 'javahl' and 'javasvn' flags are verified, as well as the
	 * availability of JAVAHL ad JavaSVN adapters, to decide what flavour to use.
	 * @return An instance of SVN client adapter that meets the specified constraints, if any.
	 * @throws BuildException Thrown in a situation where no adapter can fit the constraints.
	 */
	private ISVNClientAdapter getClientAdapter() throws BuildException {
	    ISVNClientAdapter svnClient;
	    
	    if( true == javahl && true == SvnTask.isJavahlAvailable() ) {
	        svnClient = SVNClientAdapterFactory.createSVNClient(JhlClientAdapterFactory.JAVAHL_CLIENT);
	        log("Using javahl");
	    }
	    else if( true == javasvn && true == SvnTask.isJavaSVNAvailable() ) {
	        svnClient = SVNClientAdapterFactory.createSVNClient(JavaSvnClientAdapterFactory.JAVASVN_CLIENT);
	        log("Using javasvn");
	    }
	    else if( true == SvnTask.isCommandLineAvailable() ) {
	        svnClient = SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
	        log("Using command line interface");
	    } 
	    else {
	        throw new BuildException("Cannot use javahl, JavaSVN nor command line svn client");
	    }
	    
	    return svnClient;
	}
}
