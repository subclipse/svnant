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

import org.tigris.subversion.svnant.commands.SvnCommand;

import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;

import org.tigris.subversion.svnant.types.SvnSetting;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;

import java.util.TimeZone;

import java.io.File;

/**
 * This facade provides a reusable way to configure and access subversion clients.
 * 
 * @author Daniel Kasmeroglu
 *         <a href="mailto:daniel.kasmeroglu@kasisoft.net">daniel.kasmeroglu@kasisoft.net</a>
 *
 */
public class SvnFacade {

    private static final String MSG_MISSING_DEPENDENCY = "Missing '%s' dependencies on the classpath !";

    private static final String  DEFAULT_DATEFORMATTER = "MM/dd/yyyy hh:mm a";

    private static final Boolean DEFAULT_SVNKIT        = Boolean.FALSE;
    private static final Boolean DEFAULT_JAVAHL        = Boolean.TRUE;
    private static final Boolean DEFAULT_FAILONERROR   = Boolean.TRUE;

    private static final String  KEY_FACADE            = "org.tigris.subversion.svnant.SvnFacade";

    private static Boolean       javahlAvailable       = null;
    private static Boolean       svnKitAvailable       = null;
    private static Boolean       commandLineAvailable  = null;

    private SvnSetting           setting               = new SvnSetting( null );

    private SvnSetting           refidsetting          = null;
    private String               refid                 = null;

    /**
     * Returns a facade which is associated with the supplied ant project.
     *
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     *              
     * @return   A new facade. Not <code>null</code>.
     */
    private static final SvnFacade getFacade( ProjectComponent component ) {
        // in general I would prefer to use the key by it's own but the code might
        // become invalid if the ant svn tasks are used in parallel for the same
        // project (which is unlikely to happen), so here we're providing the necessary 
        // distinction.
        if( component instanceof SvnCommand ) {
            // if a command is passed we're using the task for reference
            component = ((SvnCommand) component).getTask();
        }
        String    key    = KEY_FACADE + component.hashCode();
        SvnFacade result = (SvnFacade) component.getProject().getReference( key );
        if( result == null ) {
            result = new SvnFacade();
            component.getProject().addReference( key, result );
        }
        return result;
    }

    /**
     * Returns the settings used by this facade.
     *
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     * 
     * @return   The settings used by this facade. Not <code>null</code>.
     */
    private static final SvnSetting getSetting( ProjectComponent component ) {
        return getFacade( component ).setting;
    }

    private static final SvnSetting getRefidSetting( ProjectComponent component ) {
        SvnFacade facade = getFacade( component );
        if( facade.refidsetting == null ) {
            if( (facade.refid != null) && (facade.refid.length() > 0) ) {
                Object obj = component.getProject().getReference( facade.refid );
                if( obj == null ) {
                    throw new BuildException( "The refid attribute value '" + facade.refid + "' doesn't refer to any object." );
                }
                if( !(obj instanceof SvnSetting) ) {
                    throw new BuildException( "The refid attribute value '" + facade.refid + "' has an unknown type [" + obj.getClass().getName() + "]." );
                }
                facade.refidsetting = (SvnSetting) obj;
            } else {
                facade.refidsetting = facade.setting;
            }
        }
        return facade.refidsetting;
    }

    /**
     * Changes the refid used to access a svnsetting instance.
     *
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     * @param refid       The id of the configuration which has to be used.
     */
    public static final void setRefid( ProjectComponent component, String refid ) {
        getFacade( component ).refid = refid;
    }

    /**
     * Enables/disables the use of the command line client interface.
     *
     * @param component     The ant project component used to access the facade. 
     *                      Not <code>null</code>.
     * @param enable        <code>true</code> <=> Enables the command line client interface.
     */
    public static final void setJavahl( ProjectComponent component, boolean enable ) {
        getSetting( component ).setJavahl( enable );
    }

    /**
     * Returns <code>true</code> if the java jni javahl client has to be used.
     *
     * @param component  The ant project component used to access the facade. 
     *                          Not <code>null</code>.
     *                          
     * @return   <code>true</code> <=> The java jni javahl client has to be used.
     */
    public static final boolean getJavahl( ProjectComponent component ) {
        Boolean result = getSetting( component ).getJavahl();
        if( result == null ) {
            result = getRefidSetting( component ).getJavahl();
        }
        if( result == null ) {
            result = DEFAULT_JAVAHL;
        }
        return result.booleanValue();
    }

    /**
     * Enables/disables the use of the svnkit client interface.
     *
     * @param component     The ant project component used to access the facade. 
     *                      Not <code>null</code>.
     * @param enable        <code>true</code> <=> Enables the svnkit client interface.
     */
    public static final void setSvnKit( ProjectComponent component, boolean enable ) {
        getSetting( component ).setSvnKit( enable );
    }

    /**
     * Returns <code>true</code> if the java svnkit client has to be used.
     *
     * @param component  The ant project component used to access the facade. 
     *                   Not <code>null</code>.
     *                          
     * @return   <code>true</code> <=> The java jsvnkit client has to be used.
     */
    public static final boolean getSvnKit( ProjectComponent component ) {
        Boolean result = getSetting( component ).getSvnKit();
        if( result == null ) {
            result = getRefidSetting( component ).getSvnKit();
        }
        if( result == null ) {
            result = DEFAULT_SVNKIT;
        }
        return result.booleanValue();
    }

    /**
     * @see SvnSetting#setUsername(String)
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final void setUsername( ProjectComponent component, String username ) {
        getSetting( component ).setUsername( username );
    }

    /**
     * @see SvnSetting#getUsername()
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final String getUsername( ProjectComponent component ) {
        String result = getSetting( component ).getUsername();
        if( result == null ) {
            result = getRefidSetting( component ).getUsername();
        }
        return result;
    }

    /**
     * @see SvnSetting#setPassword(String)
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final void setPassword( ProjectComponent component, String password ) {
        getSetting( component ).setPassword( password );
    }

    /**
     * @see SvnSetting#getPassword()
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final String getPassword( ProjectComponent component ) {
        String result = getSetting( component ).getPassword();
        if( result == null ) {
            result = getRefidSetting( component ).getPassword();
        }
        return result;
    }

    /**
     * @see SvnSetting#setSSLPassword(String)
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final void setSSLPassword( ProjectComponent component, String sslpassword ) {
        getSetting( component ).setSSLPassword( sslpassword );
    }
    
    /**
     * @see SvnSetting#getSSLPassword()
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final String getSSLPassword( ProjectComponent component ) {
        String result = getSetting( component ).getSSLPassword();
        if( result == null ) {
            result = getRefidSetting( component ).getSSLPassword();
        }
        return result;
    }
    
    /**
     * @see SvnSetting#setSSLClientCertPath(File)
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final void setSSLClientCertPath( ProjectComponent component, File newclientcertpath ) {
        getSetting( component ).setSSLClientCertPath( newclientcertpath );
    }
    
    /**
     * @see SvnSetting#getSSLClientCertPath()
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final File getSSLClientCertPath( ProjectComponent component ) {
        File result = getSetting( component ).getSSLClientCertPath();
        if( result == null ) {
            result = getRefidSetting( component ).getSSLClientCertPath();
        }
        return result;
    }
    
    /**
     * @see SvnSetting#setSSHPort(int)
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final void setSSHPort( ProjectComponent component, Integer newsshport ) {
        getSetting( component ).setSSHPort( newsshport );
    }
    
    /**
     * @see SvnSetting#getSSHPort()
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final Integer getSSHPort( ProjectComponent component ) {
        Integer result = getSetting( component ).getSSHPort();
        if( result == null ) {
            result = getRefidSetting( component ).getSSHPort();
        }
        return result;
    }
    
    /**
     * @see SvnSetting#setSSHPassphrase(String)
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final void setSSHPassphrase( ProjectComponent component, String newsshpassphrase ) {
        getSetting( component ).setSSHPassphrase( newsshpassphrase );
    }
    
    /**
     * @see SvnSetting#getSSHPassphrase()
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final String getSSHPassphrase( ProjectComponent component ) {
        String result = getSetting( component ).getSSHPassphrase();
        if( result == null ) {
            result = getRefidSetting( component ).getSSHPassphrase();
        }
        return result;
    }
    
    /**
     * @see SvnSetting#setSSHKeyPath(File)
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final void setSSHKeyPath( ProjectComponent component, File newsshkeypath ) {
        getSetting( component ).setSSHKeyPath( newsshkeypath );
    }
    
    /**
     * @see SvnSetting#getSSHKeyPath()
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final File getSSHKeyPath( ProjectComponent component ) {
        File result = getSetting( component ).getSSHKeyPath();
        if( result == null ) {
            result = getRefidSetting( component ).getSSHKeyPath();
        }
        return result;
    }

    /**
     * @see SvnSetting#setDateFormatter(String)
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final void setDateFormatter( ProjectComponent component, String dateformatter ) {
        getSetting( component ).setDateFormatter( dateformatter );
    }

    /**
     * @see SvnSetting#getDateFormatter()
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final String getDateFormatter( ProjectComponent component ) {
        String result = getSetting( component ).getDateFormatter();
        if( (result == null) || (result.length() == 0) ) {
            result = getRefidSetting( component ).getDateFormatter();
        }
        if( (result == null) || (result.length() == 0) ) {
            result = DEFAULT_DATEFORMATTER;
        }
        return result;
    }

    /**
     * @see SvnSetting#setDateTimezone(String)
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final void setDateTimezone( ProjectComponent component, String timezone ) {
        getSetting( component ).setDateTimezone( timezone );
    }

    /**
     * @see SvnSetting#getDateTimezone()
     * 
     * @param component   The ant project component used to access the facade. Not <code>null</code>.
     */
    public static final TimeZone getDateTimezone( ProjectComponent component ) {
        String zone = getSetting( component ).getDateTimezone();
        if( (zone == null) || (zone.length() == 0) ) {
            zone = getRefidSetting( component ).getDateTimezone();
        }
        if( (zone == null) || (zone.length() == 0) ) {
            return null;
        } else {
            return TimeZone.getTimeZone( zone );
        }
    }

    /**
     * Enables/disables the controlflow interruption in case of an error.
     *
     * @param component     The ant project component used to access the facade. 
     *                      Not <code>null</code>.
     * @param enable        <code>true</code> <=> Cause a failure in case of an error. 
     */
    public static final void setFailonerror( ProjectComponent component, boolean enable ) {
        getSetting( component ).setFailonerror( enable );
    }

    /**
     * Returns <code>true</code> if a failure shall abort the build process.
     *
     * @param component  The ant project component used to access the facade. 
     *                          Not <code>null</code>.
     *                          
     * @return   <code>true</code> <=> A failure has to abort the build process.
     */
    public static final boolean getFailonerror( ProjectComponent component ) {
        Boolean result = getSetting( component ).getFailonerror();
        if( result == null ) {
            result = getRefidSetting( component ).getFailonerror();
        }
        if( result == null ) {
            result = DEFAULT_FAILONERROR;
        }
        return result.booleanValue();
    }

    /**
     * Check if javahl is available.
     * 
     * @return  <code>true</code> <=> Javahl is available.
     */
    private static final boolean isJavahlAvailable() {
        if( javahlAvailable == null ) {

            javahlAvailable = Boolean.FALSE;

            try {
                JhlClientAdapterFactory.setup();
            } catch( SVNClientException ex ) {
                // if an exception is thrown, javahl is not available or 
                // already registered ...
            }
            try {
                if( SVNClientAdapterFactory.isSVNClientAvailable( JhlClientAdapterFactory.JAVAHL_CLIENT ) ) {
                    javahlAvailable = Boolean.TRUE;
                }
            } catch( Exception ex ) {
                // If anything goes wrong it's not available. 
            }
        }
        return javahlAvailable.booleanValue();
    }

    /**
     * Check if svnkit is available.
     * 
     * @return  <code>true</code> <=> Svnkit is available.
     */
    private static final boolean isSVNKitAvailable() {
        if( svnKitAvailable == null ) {

            svnKitAvailable = Boolean.FALSE;

            try {
                SvnKitClientAdapterFactory.setup();
            } catch( SVNClientException ex ) {
                // if an exception is thrown, SVNKit is not available or 
                // already registered ...
            }
            try {
                if( SVNClientAdapterFactory.isSVNClientAvailable( SvnKitClientAdapterFactory.SVNKIT_CLIENT ) ) {
                    svnKitAvailable = Boolean.TRUE;
                }
            } catch( Exception ex ) {
                // If anything goes wrong it's not available. 
            }
        }
        return svnKitAvailable.booleanValue();
    }

    /**
     * Check if command line client is available.
     * 
     * @return  <code>true</code> <=> Command line client is available.
     */
    private static final boolean isCommandLineAvailable() {
        if( commandLineAvailable == null ) {
            commandLineAvailable = Boolean.FALSE;
            try {
                CmdLineClientAdapterFactory.setup();
            } catch( SVNClientException ex ) {
                // if an exception is thrown, command line interface is not available or
                // already registered ...                
            }
            try {
                if( SVNClientAdapterFactory.isSVNClientAvailable( CmdLineClientAdapterFactory.COMMANDLINE_CLIENT ) ) {
                    commandLineAvailable = Boolean.TRUE;
                }
            } catch( Exception ex ) {
                // If anything goes wrong it's not available. 
            }
        }
        return commandLineAvailable.booleanValue();
    }

    /**
     * This method returns a SVN client adapter, based on the property set to the svn task. 
     * More specifically, the 'javahl' and 'svnkit' flags are verified, as well as the
     * availability of JAVAHL ad SVNKit adapters, to decide what flavour to use.
     * 
     * @param component  The ant project component used to access the facade. 
     *                   Not <code>null</code>.
     *                      
     * @return  An instance of SVN client adapter that meets the specified constraints, if any.
     *          Not <code>null</code>.
     *          
     * @throws BuildException   Thrown in a situation where no adapter can fit the constraints.
     */
    public static final ISVNClientAdapter getClientAdapter( ProjectComponent component ) throws BuildException {

        ISVNClientAdapter result = null;
        
        boolean javahl      = getJavahl( component );
        boolean svnkit      = getSvnKit( component );
        boolean commandline = false;
        if( javahl ) {
            if( isJavahlAvailable() ) {
                result = SVNClientAdapterFactory.createSVNClient( JhlClientAdapterFactory.JAVAHL_CLIENT );
                component.log( "Using javahl", Project.MSG_VERBOSE );
            } else {
                component.log( String.format( MSG_MISSING_DEPENDENCY, "javahl" ), Project.MSG_ERR );
            }
        }
        if( svnkit && (result == null) ) {
            if( isSVNKitAvailable() ) {
                result = SVNClientAdapterFactory.createSVNClient( SvnKitClientAdapterFactory.SVNKIT_CLIENT );
                component.log( "Using svnkit", Project.MSG_VERBOSE );
            } else {
                component.log( String.format( MSG_MISSING_DEPENDENCY, "svnkit" ), Project.MSG_ERR );
            }
        }
        if( (!javahl) && (!svnkit) ) {
            if( isCommandLineAvailable() ) {
                result      = SVNClientAdapterFactory.createSVNClient( CmdLineClientAdapterFactory.COMMANDLINE_CLIENT );
                commandline = true;
                component.log( "Using command line", Project.MSG_VERBOSE );
            } else {
                component.log( String.format( MSG_MISSING_DEPENDENCY, "commandline" ), Project.MSG_ERR );
            }
        }
        
        if( result == null ) {
            throw new BuildException( "Cannot find javahl, svnkit nor command line svn client" );
        }

        if( getUsername( component ) != null ) {
            result.setUsername( getUsername( component ) );
        }
        if( commandline ) {
            if( getUsername( component ) != null ) {
                result.setUsername( getUsername( component ) );
            }
            if( getPassword( component ) != null ) {
                result.setPassword( getPassword( component ) );
            }
        } else {
            result.addPasswordCallback( 
                new DefaultPasswordCallback( 
                    getUsername          ( component ),
                    getPassword          ( component ),
                    getSSHKeyPath        ( component ),
                    getSSHPassphrase     ( component ),
                    getSSHPort           ( component ),
                    getSSLClientCertPath ( component ),
                    getSSLPassword       ( component )
                ) 
            );
        }

        return result;
        
    }
    
    private static class DefaultPasswordCallback implements ISVNPromptUserPassword {

        private String    username;
        private String    password;
        private String    sshprivatekeypath;
        private String    sshpassphrase;
        private String    sslclientcertpath;
        private String    sslcertpassword;
        private Integer   sshport;
        
        public DefaultPasswordCallback(String newusername, String newpassword, File sshkeypath, String sshphrase, Integer sshp, File sslpath, String sslpassword) {
            username            = newusername;
            password            = newpassword;
            sshprivatekeypath   = sshkeypath != null ? sshkeypath.getAbsolutePath() : null;
            sshpassphrase       = sshphrase;
            sshport             = sshp;
            sslclientcertpath   = sslpath != null ? sslpath.getAbsolutePath() : null;
            sslcertpassword     = sslpassword;
        }
        
        
        /**
         * {@inheritDoc}
         */
        public boolean promptSSL( String realm, boolean maysave ) {
            return (sslclientcertpath != null) || (sslcertpassword != null);
        }

        /**
         * {@inheritDoc}
         */
        public String getSSLClientCertPassword() {
            return sslclientcertpath;
        }

        /**
         * {@inheritDoc}
         */
        public String getSSLClientCertPath() {
            return sslcertpassword;
        }
        
        /**
         * {@inheritDoc}
         */
        public int askTrustSSLServer( String info, boolean allowpermanently ) {
            return ISVNPromptUserPassword.AcceptTemporary;
        }
        
        /**
         * {@inheritDoc}
         */
        public String getSSHPrivateKeyPath() {
            return sshprivatekeypath;
        }

        /**
         * {@inheritDoc}
         */
        public String getSSHPrivateKeyPassphrase() {
            return sshpassphrase;
        }
        
        /**
         * {@inheritDoc}
         */
        public boolean promptSSH( String realm, String username, int sshport, boolean maysave ) {
            if( sshport > 0 ) {
                // the port is obviously given by the url
                this.sshport = Integer.valueOf( sshport );
            }
            return (sshpassphrase != null) || (sshprivatekeypath != null);
        }
        
        /**
         * {@inheritDoc}
         */
        public int getSSHPort() {
            if( sshport == null ) {
                return 22;
            } else {
                return sshport.intValue();
            }
        }
        
        /**
         * {@inheritDoc}
         */
        public boolean userAllowedSave() {
            return false;
        }
        
        /**
         * {@inheritDoc}
         */
        public String getUsername() {
            return username;
        }

        /**
         * {@inheritDoc}
         */
        public String getPassword() {
            return password;
        }

        /**
         * {@inheritDoc}
         */
        public String askQuestion( String realm, String question, boolean showanswer, boolean maysave ) {
            return "";
        }

        /**
         * {@inheritDoc}
         */
        public boolean prompt( String realm, String username, boolean maysave ) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public boolean promptUser( String realm, String username, boolean maysave ) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public boolean askYesNo( String realm, String question, boolean yesisdefault ) {
            return yesisdefault;
        }
        
    } /* ENDCLASS */

} /* ENDCLASS */
