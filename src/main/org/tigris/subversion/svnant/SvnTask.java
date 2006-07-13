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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javasvn.JavaSvnClientAdapterFactory;

/**
 * Svn Task
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class SvnTask extends Task {

	private static boolean javahlAvailableInitialized = false;
    private static boolean javahlAvailable;
    private static boolean javaSVNAvailableInitialized = false;
    private static boolean javaSVNAvailable;
    private static boolean commandLineAvailableInitialized = false;
    private static boolean commandLineAvailable;
	
    private String username = null;
    private String password = null;    
    private boolean javahl = true;
    private boolean javasvn = true;
    private String dateFormatter = null;
    
    private List commands = new ArrayList();
    private List notifyListeners = new ArrayList();
    
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = "\"\"".equals(password) ? "" : password;
    }

    /**
     * set javahl to false to use command line interface
     * @param javahl
     */
    public void setJavahl(boolean javahl) {
        this.javahl = javahl;
    }

    /**
     * set javasvn to false to use command line interface
     * @param javasvn
     */
    public void setJavasvn(boolean javasvn) {
        this.javasvn = javasvn;
    }

    /**
     * @return dateFormatter used to parse revision dates
     */
    public String getDateFormatter()
    {
    	return dateFormatter != null ? dateFormatter : "MM/dd/yyyy hh:mm a";
    }
    
    /**
     * set dateFormatter used to parse revision dates
     * @param dateFormatter
     */
    public void setDateFormatter(String dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    public void addCheckout(Checkout a) {
        addCommand(a);
    }

    public void addAdd(Add a) {
        addCommand(a);
    }

    public void addCommit(Commit a) {
        addCommand(a);
    }

    public void addCopy(Copy a) {
        addCommand(a);
    }

    public void addDelete(Delete a) {
        addCommand(a);
    }

    public void addExport(Export a) {
        addCommand(a);
    }

    public void addImport(Import a) {
        addCommand(a);
    }

    public void addMkdir(Mkdir a) {
        addCommand(a);
    }

    public void addMove(Move a) {
        addCommand(a);
    }

    public void addUpdate(Update a) {
        addCommand(a);
    }
    
    public void addPropset(Propset a) {
        addCommand(a);
    }
    
    public void addDiff(Diff a) {
        addCommand(a);
    }

    public void addKeywordsSet(Keywordsset a) {
        addCommand(a);
    }
    
    public void addKeywordsAdd(Keywordsadd a) {
        addCommand(a);
    }
    
    public void addKeywordsRemove(Keywordsremove a) {
        addCommand(a);
    }    

    public void addRevert(Revert a) {
        addCommand(a);
    }

    public void addCat(Cat a) {
        addCommand(a);
    }

    public void addPropdel(Propdel a) {
        addCommand(a);
    }
    
    public void addIgnore(Ignore a) {
        addCommand(a);
    }
    
    public void addCreateRepository(CreateRepository a) {
        addCommand(a);
    }
    
    public void addWcVersion(WcVersion a) {
        addCommand(a);
    }

    public void addStatus(Status a) {
    	addCommand(a);
    }
    
    public void addSwitch(Switch a) {
    	addCommand(a);
    }
    
    public void addPropget(Propget a) {
    	addCommand(a);
    }
    
    /**
     * Add the info command to the list of commands to execute.
     */
    public void addInfo(Info a) {
        addCommand(a);
    }

    private void addCommand(SvnCommand cmd)
    {
    	cmd.setTask(this);
    	commands.add(cmd);
    }
    
    public void addNotifyListener(ISVNNotifyListener notifyListener) {
        notifyListeners.add(notifyListener);
    }

    /**
     * check if javahl is available
     * @return true if javahl is available
     */
    static public boolean isJavahlAvailable() {
    	if (javahlAvailableInitialized == false) {
            // we don't initiliaze javahlAvailable in the static field because we
            // don't want the check to occur if javahl is set to false
            try {
                JhlClientAdapterFactory.setup();
            } catch (SVNClientException e) {
                // if an exception is thrown, javahl is not available or 
                // already registered ...
            }
            javahlAvailable = false;
            try {
            	javahlAvailable = SVNClientAdapterFactory.isSVNClientAvailable(JhlClientAdapterFactory.JAVAHL_CLIENT);
            } catch (Exception ex) {
            	//If anything goes wrong ... 
            }            

            javahlAvailableInitialized = true;
        }
        return javahlAvailable;
    }
    
    /**
     * check if JavaSVN is available
     * @return true if JavaSVN is available
     */
    static public boolean isJavaSVNAvailable() {
        if (javaSVNAvailableInitialized == false) {
            // we don't initiliaze javaSVNAvailable in the static field because we
            // don't want the check to occur if javaSVN is set to false
            try {
                JavaSvnClientAdapterFactory.setup();
            } catch (SVNClientException e) {
                // if an exception is thrown, JavaSVN is not available or 
                // already registered ...
            }
            javaSVNAvailable = false;
            try {
            	javaSVNAvailable = SVNClientAdapterFactory.isSVNClientAvailable(JavaSvnClientAdapterFactory.JAVASVN_CLIENT);
            } catch (Exception ex) {
            	//If anything goes wrong ... 
            }            
            javaSVNAvailableInitialized = true;
        }
        return javaSVNAvailable;
    }
    
    /**
     * check if command line interface is available
     * @return true if command line interface is available
     */
    static public boolean isCommandLineAvailable() {
        if (commandLineAvailableInitialized == false) {
            try {
                CmdLineClientAdapterFactory.setup();
            } catch (SVNClientException e) {
                // if an exception is thrown, command line interface is not available or
                // already registered ...                
            }
            commandLineAvailable = 
                SVNClientAdapterFactory.isSVNClientAvailable(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
            commandLineAvailableInitialized = true;
        }
        return commandLineAvailable;
    }
    
    public void maybeConfigure() throws BuildException
    {
    	super.maybeConfigure();
    }
    
    public void execute() throws BuildException {

        ISVNClientAdapter svnClient;
        
        if ((javahl) && (isJavahlAvailable())) {
            svnClient = SVNClientAdapterFactory.createSVNClient(JhlClientAdapterFactory.JAVAHL_CLIENT);
            log("Using javahl", Project.MSG_VERBOSE);
        }
        else
        if ((javasvn) && isJavaSVNAvailable()) {
            svnClient = SVNClientAdapterFactory.createSVNClient(JavaSvnClientAdapterFactory.JAVASVN_CLIENT);
            log("Using javasvn", Project.MSG_VERBOSE);
        }
        else
        if (isCommandLineAvailable()) {
            svnClient = SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
            log("Using command line interface", Project.MSG_VERBOSE);
        } 
        else
            throw new BuildException("Cannot use javahl, JavaSVN nor command line svn client");
        

        if (username != null)
            svnClient.setUsername(username);

        if (password != null)
            svnClient.setPassword(password);

        for (int i = 0; i < notifyListeners.size();i++) {
            svnClient.addNotifyListener((ISVNNotifyListener)notifyListeners.get(i));
        }

        for (int i = 0; i < commands.size(); i++) {
            SvnCommand command = (SvnCommand) commands.get(i);
            Feedback feedback = new Feedback(command);
			svnClient.addNotifyListener(feedback);
            command.executeCommand(svnClient);
            svnClient.removeNotifyListener(feedback);
        }
        
    }

}
