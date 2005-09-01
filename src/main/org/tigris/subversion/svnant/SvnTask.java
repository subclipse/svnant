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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JavaSvnClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;

/**
 * Svn Task
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class SvnTask extends Task {
    private String username = null;
    private String password = null;
    private List commands = new ArrayList();
    private boolean javahl = true;
    private boolean javasvn = true;
    private List notifyListeners = new ArrayList();
    
    private static boolean javahlAvailableInitialized = false;
    private static boolean javahlAvailable;
    private static boolean javaSVNAvailableInitialized = false;
    private static boolean javaSVNAvailable;
    private static boolean commandLineAvailableInitialized = false;
    private static boolean commandLineAvailable;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
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
     * @param javahl
     */
    public void setJavasvn(boolean javasvn) {
        this.javasvn = javasvn;
    }

    public void addCheckout(Checkout a) {
        commands.add(a);
    }

    public void addAdd(Add a) {
        commands.add(a);
    }

    public void addCommit(Commit a) {
        commands.add(a);
    }

    public void addCopy(Copy a) {
        commands.add(a);
    }

    public void addDelete(Delete a) {
        commands.add(a);
    }

    public void addExport(Export a) {
        commands.add(a);
    }

    public void addImport(Import a) {
        commands.add(a);
    }

    public void addMkdir(Mkdir a) {
        commands.add(a);
    }

    public void addMove(Move a) {
        commands.add(a);
    }

    public void addUpdate(Update a) {
        commands.add(a);
    }
    
    public void addPropset(Propset a) {
        commands.add(a);
    }
    
    public void addDiff(Diff a) {
        commands.add(a);
    }

    public void addKeywordsSet(Keywordsset a) {
        commands.add(a);
    }
    
    public void addKeywordsAdd(Keywordsadd a) {
        commands.add(a);
    }
    
    public void addKeywordsRemove(Keywordsremove a) {
        commands.add(a);
    }    

    public void addRevert(Revert a) {
        commands.add(a);
    }

    public void addCat(Cat a) {
        commands.add(a);
    }

    public void addPropdel(Propdel a) {
        commands.add(a);
    }
    
    public void addIgnore(Ignore a) {
        commands.add(a);
    }
    
    public void addCreateRepository(CreateRepository a) {
        commands.add(a);
    }
    
//    public void addSummaryStatus(StatusSummary a) {
//        commands.add(a);
//    }

    public void addStatus(Status a) {
    	commands.add(a);
    }
    
    public void addSwitch(Switch a) {
    	commands.add(a);
    }
    
    public void addPropget(Propget a) {
    	commands.add(a);
    }
    
    public void addNotifyListener(ISVNNotifyListener notifyListener) {
        notifyListeners.add(notifyListener);
    }

    /**
     * check if javahl is available
     * @return true if javahl is available
     */
    private boolean isJavahlAvailable() {
    	if (javahlAvailableInitialized == false) {
            // we don't initiliaze javahlAvailable in the static field because we
            // don't want the check to occur if javahl is set to false
            try {
                JhlClientAdapterFactory.setup();
            } catch (SVNClientException e) {
                // if an exception is thrown, javahl is not available or 
                // already registered ...
            }
            javahlAvailable = 
                SVNClientAdapterFactory.isSVNClientAvailable(JhlClientAdapterFactory.JAVAHL_CLIENT);
            javahlAvailableInitialized = true;
        }
        return javahlAvailable;
    }
    
    /**
     * check if JavaSVN is available
     * @return true if JavaSVN is available
     */
    private boolean isJavaSVNAvailable() {
        if (javaSVNAvailableInitialized == false) {
            // we don't initiliaze javaSVNAvailable in the static field because we
            // don't want the check to occur if javaSVN is set to false
            try {
                JavaSvnClientAdapterFactory.setup();
            } catch (SVNClientException e) {
                // if an exception is thrown, JavaSVN is not available or 
                // already registered ...
            }
            javaSVNAvailable = 
                SVNClientAdapterFactory.isSVNClientAvailable(JavaSvnClientAdapterFactory.JAVASVN_CLIENT);
            javaSVNAvailableInitialized = true;
        }
        return javaSVNAvailable;
    }
    
    /**
     * check if command line interface is available
     * @return true if command line interface is available
     */
    private boolean isCommandLineAvailable() {
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
    
    public void execute() throws BuildException {

        ISVNClientAdapter svnClient;
        
        if ((javahl) && (isJavahlAvailable())) {
            svnClient = SVNClientAdapterFactory.createSVNClient(JhlClientAdapterFactory.JAVAHL_CLIENT);
            log("Using javahl");
        }
        else
        if ((javasvn) && isJavaSVNAvailable()) {
            svnClient = SVNClientAdapterFactory.createSVNClient(JavaSvnClientAdapterFactory.JAVASVN_CLIENT);
            log("Using javasvn");
        }
        else
        if (isCommandLineAvailable()) {
            svnClient = SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
            log("Using command line interface");
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
            command.execute(svnClient);
            svnClient.removeNotifyListener(feedback);
        }
        
    }

}
