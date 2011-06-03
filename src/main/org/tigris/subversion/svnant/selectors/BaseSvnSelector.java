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

import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;

import org.tigris.subversion.svnant.SvnFacade;
import org.tigris.subversion.svnant.types.SvnSetting;

import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.selectors.BaseExtendSelector;

import java.io.File;

/**
 * This is an abstract class that implements all functionality shared
 * between all file selectors in svn-ant. In particular, it implements
 * the handling of the common parameters: javahl and svnkit. It provides
 * the logic to select the approrpriate client adapter. Finally, it implements
 * the method required by all Ant selectors (isSelected) and redirects
 * the control flow to a subclass implementation while providing the
 * appropriate client adapter.  
 * 
 * @author Jean-Pierre Fiset <a href="mailto:jp@fiset.ca">jp@fiset.ca</a>
 *
 */
public abstract class BaseSvnSelector extends BaseExtendSelector {

    private ISVNClientAdapter clientadapter = null;
    
    /**
     * @see SvnSetting#setCertReject(Boolean)
     */
    public void setCertReject( Boolean newcertreject ) {
        SvnFacade.setCertReject( this, newcertreject );
    }
    
    /**
     * @see SvnSetting#setSSLPassword(String)
     */
    public void setSSLPassword( String newpassword ) {
        SvnFacade.setSSLPassword( this, newpassword );
    }
    
    /**
     * @see SvnSetting#setSSLClientCertPath(File)
     */
    public void setSSLClientCertPath( File newclientcertpath ) {
        SvnFacade.setSSLClientCertPath( this, newclientcertpath );
    }
    
    /**
     * @see SvnSetting#setSSHPort(Integer)
     */
    public void setSSHPort( Integer newsshport ) {
        SvnFacade.setSSHPort( this, newsshport );
    }
    
    /**
     * @see SvnSetting#setSSHPassphrase(String)
     */
    public void setSSHPassphrase( String newsshpassphrase ) {
        SvnFacade.setSSHPassphrase( this, newsshpassphrase );
    }
    
    /**
     * @see SvnSetting#setSSHKeyPath(File)
     */
    public void setSSHKeyPath( File newsshkeypath ) {
        SvnFacade.setSSHKeyPath( this, newsshkeypath );
    }

    /**
     * @see SvnFacade#setUsername(ProjectComponent, String)
     */
    public void setUsername( String username ) {
        SvnFacade.setUsername( this, username );
    }

    /**
     * @see SvnFacade#setPassword(ProjectComponent, String)
     */
    public void setPassword( String password ) {
        SvnFacade.setPassword( this, password );
    }

    /**
     * @see SvnFacade#setRefid(org.apache.tools.ant.ProjectComponent, Reference)
     */
    public void setRefid( Reference refid ) {
        SvnFacade.setRefid( this, refid );
    }

    /**
     * @see SvnFacade#setJavahl(org.apache.tools.ant.ProjectComponent, boolean)
     */
    public void setJavahl( boolean javahl_ ) {
        SvnFacade.setJavahl( this, javahl_ );
    }

    /**
     * @see SvnFacade#setSvnKit(org.apache.tools.ant.ProjectComponent, boolean)
     */
    public void setSvnkit( boolean svnkit_ ) {
        SvnFacade.setSvnKit( this, svnkit_ );
    }

    /**
     * @see SvnFacade#setFailonerror(org.apache.tools.ant.ProjectComponent, boolean)
     */
    public void setFailonerror( boolean failonerror ) {
        SvnFacade.setFailonerror( this, failonerror );
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isSelected( File basedir_, String filename_, File file_ ) {
        if( clientadapter == null ) {
            clientadapter = SvnFacade.getClientAdapter( this );
        }
        return isSelected( clientadapter, basedir_, filename_, file_ );
    }

    /**
     * Method that needs to be reimplemented by each subclass. It is equivalent to 'isSelected',
     * inherited from BaseExtendSelector, with the exception that a SVN client adaptor is provided. 
     * @param svnClient_ The SVN client that should be used to perform repository access
     * @param basedir_ A java.io.File object for the base directory
     * @param filename_ The name of the file to check
     * @param file_ A File object for this filename
     * 
     * @return Returns true if the file should be selected. Otherwise, false. 
     */
    public abstract boolean isSelected( ISVNClientAdapter svnClient_, File basedir_, String filename_, File file_ );

}
